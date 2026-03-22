package com.morningalarm.dto.upload

import com.morningalarm.Seconds
import com.morningalarm.Url
import kotlinx.serialization.Serializable

@Serializable
enum class MediaKindDto {
    IMAGE,
    AUDIO,
}

@Serializable
data class UploadedMediaDto(
    val kind: MediaKindDto,
    val url: Url,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val durationSeconds: Seconds? = null,
)

@Serializable
data class UploadImageResponseDto(
    val media: UploadedMediaDto,
)

@Serializable
data class UploadAudioResponseDto(
    val media: UploadedMediaDto,
)
