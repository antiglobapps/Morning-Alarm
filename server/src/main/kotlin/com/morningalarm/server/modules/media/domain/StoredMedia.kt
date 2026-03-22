package com.morningalarm.server.modules.media.domain

import com.morningalarm.Seconds
import com.morningalarm.Url

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
    val url: Url,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val durationSeconds: Seconds? = null,
)
