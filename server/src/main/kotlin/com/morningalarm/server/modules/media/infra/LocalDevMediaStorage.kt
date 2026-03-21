package com.morningalarm.server.modules.media.infra

import com.morningalarm.server.modules.media.application.ports.MediaStorage
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.modules.media.domain.StoredMedia
import com.morningalarm.server.shared.errors.ValidationException
import java.io.File
import java.util.UUID

class LocalDevMediaStorage(
    storageDir: String,
    private val publicBaseUrl: String,
) : MediaStorage {
    private val rootDir = File(storageDir).absoluteFile.also { it.mkdirs() }
    private val imageDir = File(rootDir, "images").also { it.mkdirs() }
    private val audioDir = File(rootDir, "audio").also { it.mkdirs() }

    override fun store(kind: MediaKind, upload: MediaUpload): StoredMedia {
        val extension = extractExtension(upload.fileName, upload.contentType)
        val generatedName = "${kind.name.lowercase()}-${UUID.randomUUID()}$extension"
        val targetFile = File(directoryFor(kind), generatedName)
        targetFile.writeBytes(upload.bytes)
        return StoredMedia(
            kind = kind,
            url = "${publicBaseUrl.trimEnd('/')}/media/${kind.name.lowercase()}/$generatedName",
            fileName = generatedName,
            contentType = upload.contentType,
            sizeBytes = upload.bytes.size.toLong(),
            durationSeconds = null,
        )
    }

    override fun resolve(kind: MediaKind, fileName: String): File? {
        if (fileName.isBlank() || fileName.contains("..") || fileName.contains('/') || fileName.contains('\\')) {
            return null
        }
        val candidate = File(directoryFor(kind), fileName).absoluteFile
        return if (candidate.exists() && candidate.parentFile == directoryFor(kind).absoluteFile) candidate else null
    }

    private fun directoryFor(kind: MediaKind): File = when (kind) {
        MediaKind.IMAGE -> imageDir
        MediaKind.AUDIO -> audioDir
    }

    private fun extractExtension(fileName: String, contentType: String): String {
        val fromName = fileName.substringAfterLast('.', "").trim().lowercase()
        if (fromName.isNotBlank()) return ".$fromName"

        return when (contentType.lowercase()) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "audio/mpeg" -> ".mp3"
            "audio/wav", "audio/x-wav" -> ".wav"
            "audio/ogg" -> ".ogg"
            "audio/mp4" -> ".m4a"
            else -> throw ValidationException("Unsupported file extension for content type: $contentType")
        }
    }
}
