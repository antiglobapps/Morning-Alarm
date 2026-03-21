package com.morningalarm.dto.upload

import kotlinx.serialization.Serializable

@Serializable
enum class MediaKindDto {
    IMAGE,
    AUDIO,
}

@Serializable
data class UploadedMediaDto(
    val kind: MediaKindDto,
    val url: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val durationSeconds: Int? = null,
)

@Serializable
data class UploadImageResponseDto(
    val media: UploadedMediaDto,
)

@Serializable
data class UploadAudioResponseDto(
    val media: UploadedMediaDto,
)
