package com.morningalarm.server.modules.media.application

import com.morningalarm.server.modules.media.application.ports.MediaStorage
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.modules.media.domain.StoredMedia
import com.morningalarm.server.shared.errors.ValidationException

class AdminMediaService(
    private val mediaStorage: MediaStorage,
    private val maxImageBytes: Long,
    private val maxAudioBytes: Long,
) {
    fun uploadImage(upload: MediaUpload): StoredMedia {
        validateUpload(
            upload = upload,
            expectedKind = MediaKind.IMAGE,
            maxBytes = maxImageBytes,
            contentTypePrefix = "image/",
            contentTypeMessage = "Image upload requires an image/* content type",
        )
        return mediaStorage.store(MediaKind.IMAGE, upload)
    }

    fun uploadAudio(upload: MediaUpload): StoredMedia {
        validateUpload(
            upload = upload,
            expectedKind = MediaKind.AUDIO,
            maxBytes = maxAudioBytes,
            contentTypePrefix = "audio/",
            contentTypeMessage = "Audio upload requires an audio/* content type",
        )
        return mediaStorage.store(MediaKind.AUDIO, upload)
    }

    fun resolveMedia(kind: MediaKind, fileName: String) = mediaStorage.resolve(kind, fileName)

    private fun validateUpload(
        upload: MediaUpload,
        expectedKind: MediaKind,
        maxBytes: Long,
        contentTypePrefix: String,
        contentTypeMessage: String,
    ) {
        if (upload.fileName.isBlank()) {
            throw ValidationException("${expectedKind.name.lowercase().replaceFirstChar { it.uppercase() }} fileName must not be blank")
        }
        if (upload.bytes.isEmpty()) {
            throw ValidationException("${expectedKind.name.lowercase().replaceFirstChar { it.uppercase() }} upload must not be empty")
        }
        if (!upload.contentType.lowercase().startsWith(contentTypePrefix)) {
            throw ValidationException(contentTypeMessage)
        }
        if (upload.bytes.size.toLong() > maxBytes) {
            throw ValidationException("${expectedKind.name.lowercase().replaceFirstChar { it.uppercase() }} exceeds max size of $maxBytes bytes")
        }
    }
}
