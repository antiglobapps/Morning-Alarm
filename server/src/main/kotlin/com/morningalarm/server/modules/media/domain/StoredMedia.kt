package com.morningalarm.server.modules.media.domain

enum class MediaKind {
    IMAGE,
    AUDIO,
}

data class MediaUpload(
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
)

data class StoredMedia(
    val kind: MediaKind,
    val url: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val durationSeconds: Int? = null,
)
