package com.morningalarm.server.modules.media.application

import com.morningalarm.server.modules.media.domain.MediaUpload
import java.nio.charset.StandardCharsets
import kotlin.math.ceil

internal object AudioDurationDetector {
    fun detectSeconds(upload: MediaUpload): Int? {
        val contentType = upload.contentType.substringBefore(';').trim().lowercase()
        val extension = upload.fileName.substringAfterLast('.', "").trim().lowercase()
        val durationSeconds = when {
            contentType == "audio/mpeg" || extension == "mp3" -> detectMp3Seconds(upload.bytes)
            contentType == "audio/wav" || contentType == "audio/x-wav" || extension == "wav" -> detectWavSeconds(upload.bytes)
            else -> null
        }
        return durationSeconds?.coerceAtLeast(1)
    }

    private fun detectWavSeconds(bytes: ByteArray): Int? {
        if (bytes.size < 12 || bytes.readAscii(0, 4) != "RIFF" || bytes.readAscii(8, 4) != "WAVE") {
            return null
        }

        var offset = 12
        var byteRate: Long? = null
        var dataSize: Long? = null

        while (offset + 8 <= bytes.size) {
            val chunkId = bytes.readAscii(offset, 4)
            val chunkSize = bytes.readUInt32Le(offset + 4) ?: return null
            val chunkDataOffset = offset + 8
            if (chunkDataOffset > bytes.size) break

            when (chunkId) {
                "fmt " -> {
                    if (chunkSize >= 12 && chunkDataOffset + 12 <= bytes.size) {
                        byteRate = bytes.readUInt32Le(chunkDataOffset + 8)
                    }
                }

                "data" -> {
                    val availableBytes = (bytes.size - chunkDataOffset).toLong().coerceAtLeast(0L)
                    dataSize = minOf(chunkSize, availableBytes)
                }
            }

            val nextOffset = chunkDataOffset.toLong() + chunkSize + (chunkSize % 2)
            if (nextOffset > bytes.size) break
            offset = nextOffset.toInt()
        }

        val resolvedByteRate = byteRate?.takeIf { it > 0 } ?: return null
        val resolvedDataSize = dataSize?.takeIf { it > 0 } ?: return null
        return ceil(resolvedDataSize.toDouble() / resolvedByteRate.toDouble()).toInt()
    }

    private fun detectMp3Seconds(bytes: ByteArray): Int? {
        var offset = skipId3v2(bytes)
        var totalDurationSeconds = 0.0
        var framesCount = 0

        // Scan each frame header so CBR and VBR files are both handled without external codecs.
        while (offset + 4 <= bytes.size) {
            val frameHeader = parseMp3FrameHeader(bytes, offset)
            if (frameHeader == null) {
                offset += 1
                continue
            }
            if (offset + frameHeader.frameLength > bytes.size) {
                break
            }

            totalDurationSeconds += frameHeader.durationSeconds
            framesCount += 1
            offset += frameHeader.frameLength
        }

        if (framesCount == 0 || totalDurationSeconds <= 0.0) {
            return null
        }
        return ceil(totalDurationSeconds).toInt()
    }

    private fun skipId3v2(bytes: ByteArray): Int {
        if (bytes.size < 10 || bytes.readAscii(0, 3) != "ID3") {
            return 0
        }
        val flags = bytes[5].toInt() and 0xFF
        val tagSize = readSynchsafeInt(bytes, 6) ?: return 0
        val footerSize = if ((flags and 0x10) != 0) 10 else 0
        return (10 + tagSize + footerSize).coerceAtMost(bytes.size)
    }

    private fun parseMp3FrameHeader(bytes: ByteArray, offset: Int): Mp3FrameHeader? {
        val byte0 = bytes[offset].toInt() and 0xFF
        val byte1 = bytes[offset + 1].toInt() and 0xFF
        val byte2 = bytes[offset + 2].toInt() and 0xFF

        if (byte0 != 0xFF || (byte1 and 0xE0) != 0xE0) {
            return null
        }

        val versionBits = (byte1 shr 3) and 0x03
        val layerBits = (byte1 shr 1) and 0x03
        val bitrateIndex = (byte2 shr 4) and 0x0F
        val sampleRateIndex = (byte2 shr 2) and 0x03
        val padding = (byte2 shr 1) and 0x01

        if (versionBits == 0x01 || layerBits == 0x00 || bitrateIndex == 0x00 || bitrateIndex == 0x0F || sampleRateIndex == 0x03) {
            return null
        }

        val version = when (versionBits) {
            0x00 -> Mp3Version.MPEG_2_5
            0x02 -> Mp3Version.MPEG_2
            0x03 -> Mp3Version.MPEG_1
            else -> return null
        }
        val layer = when (layerBits) {
            0x01 -> Mp3Layer.LAYER_III
            0x02 -> Mp3Layer.LAYER_II
            0x03 -> Mp3Layer.LAYER_I
            else -> return null
        }

        val sampleRate = sampleRate(version, sampleRateIndex)
        val bitrateKbps = bitrateKbps(version, layer, bitrateIndex)
        val samplesPerFrame = samplesPerFrame(version, layer)
        val frameLength = when (layer) {
            Mp3Layer.LAYER_I -> ((((12L * bitrateKbps * 1000L) / sampleRate) + padding) * 4).toInt()
            else -> ((((samplesPerFrame.toLong() / 8L) * bitrateKbps * 1000L) / sampleRate) + padding).toInt()
        }
        if (frameLength <= 4) {
            return null
        }

        return Mp3FrameHeader(
            frameLength = frameLength,
            durationSeconds = samplesPerFrame.toDouble() / sampleRate.toDouble(),
        )
    }

    private fun sampleRate(version: Mp3Version, sampleRateIndex: Int): Int = when (version) {
        Mp3Version.MPEG_1 -> intArrayOf(44_100, 48_000, 32_000)[sampleRateIndex]
        Mp3Version.MPEG_2 -> intArrayOf(22_050, 24_000, 16_000)[sampleRateIndex]
        Mp3Version.MPEG_2_5 -> intArrayOf(11_025, 12_000, 8_000)[sampleRateIndex]
    }

    private fun bitrateKbps(version: Mp3Version, layer: Mp3Layer, bitrateIndex: Int): Int {
        val mpeg1LayerI = intArrayOf(0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448)
        val mpeg1LayerII = intArrayOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384)
        val mpeg1LayerIII = intArrayOf(0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320)
        val mpeg2LayerI = intArrayOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256)
        val mpeg2LayerIIOrIII = intArrayOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160)

        return when (version) {
            Mp3Version.MPEG_1 -> when (layer) {
                Mp3Layer.LAYER_I -> mpeg1LayerI[bitrateIndex]
                Mp3Layer.LAYER_II -> mpeg1LayerII[bitrateIndex]
                Mp3Layer.LAYER_III -> mpeg1LayerIII[bitrateIndex]
            }

            Mp3Version.MPEG_2, Mp3Version.MPEG_2_5 -> when (layer) {
                Mp3Layer.LAYER_I -> mpeg2LayerI[bitrateIndex]
                Mp3Layer.LAYER_II, Mp3Layer.LAYER_III -> mpeg2LayerIIOrIII[bitrateIndex]
            }
        }
    }

    private fun samplesPerFrame(version: Mp3Version, layer: Mp3Layer): Int = when (layer) {
        Mp3Layer.LAYER_I -> 384
        Mp3Layer.LAYER_II -> 1152
        Mp3Layer.LAYER_III -> if (version == Mp3Version.MPEG_1) 1152 else 576
    }

    private fun ByteArray.readAscii(offset: Int, length: Int): String {
        return String(this, offset, length, StandardCharsets.US_ASCII)
    }

    private fun ByteArray.readUInt32Le(offset: Int): Long? {
        if (offset + 4 > size) return null
        return (this[offset].toLong() and 0xFF) or
            ((this[offset + 1].toLong() and 0xFF) shl 8) or
            ((this[offset + 2].toLong() and 0xFF) shl 16) or
            ((this[offset + 3].toLong() and 0xFF) shl 24)
    }

    private fun readSynchsafeInt(bytes: ByteArray, offset: Int): Int? {
        if (offset + 4 > bytes.size) return null
        return ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)
    }
}

private data class Mp3FrameHeader(
    val frameLength: Int,
    val durationSeconds: Double,
)

private enum class Mp3Version {
    MPEG_1,
    MPEG_2,
    MPEG_2_5,
}

private enum class Mp3Layer {
    LAYER_I,
    LAYER_II,
    LAYER_III,
}
