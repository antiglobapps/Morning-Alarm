package com.morningalarm.server.modules.media.application

import com.morningalarm.server.modules.media.application.ports.MediaStorage
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.modules.media.domain.StoredMedia
import com.morningalarm.server.shared.audit.AuditEvent
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.errors.ValidationException

class AdminMediaService(
    private val mediaStorage: MediaStorage,
    private val maxImageBytes: Long,
    private val maxAudioBytes: Long,
    private val auditLogger: AuditLogger,
) {
    fun uploadImage(upload: MediaUpload, adminId: String): StoredMedia {
        validateUpload(
            upload = upload,
            expectedKind = MediaKind.IMAGE,
            maxBytes = maxImageBytes,
            contentTypePrefix = "image/",
            contentTypeMessage = "Image upload requires an image/* content type",
        )
        val stored = mediaStorage.store(MediaKind.IMAGE, upload)
        auditLogger.log(AuditEvent.MediaUploaded(kind = "image", fileName = upload.fileName, sizeBytes = upload.bytes.size.toLong(), adminId = adminId))
        return stored
    }

    fun uploadAudio(upload: MediaUpload, adminId: String): StoredMedia {
        validateUpload(
            upload = upload,
            expectedKind = MediaKind.AUDIO,
            maxBytes = maxAudioBytes,
            contentTypePrefix = "audio/",
            contentTypeMessage = "Audio upload requires an audio/* content type",
        )
        val storedMedia = mediaStorage.store(MediaKind.AUDIO, upload)
        val durationSeconds = AudioDurationDetector.detectSeconds(upload) ?: storedMedia.durationSeconds
        auditLogger.log(AuditEvent.MediaUploaded(kind = "audio", fileName = upload.fileName, sizeBytes = upload.bytes.size.toLong(), adminId = adminId))
        return storedMedia.copy(durationSeconds = durationSeconds)
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
