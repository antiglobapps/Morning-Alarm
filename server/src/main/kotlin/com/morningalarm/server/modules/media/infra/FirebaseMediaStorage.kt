package com.morningalarm.server.modules.media.infra

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.morningalarm.server.modules.media.application.ports.MediaStorage
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.modules.media.domain.StoredMedia
import com.morningalarm.server.shared.errors.ValidationException
import java.io.File
import java.io.FileInputStream
import java.util.UUID

/**
 * Stores media files in Firebase Storage (Google Cloud Storage bucket).
 * Files are uploaded with public-read ACL and served directly via GCS URL.
 */
class FirebaseMediaStorage(
    credentialsPath: String,
    private val bucketName: String,
) : MediaStorage {

    private val storage = run {
        val credentials = FileInputStream(credentialsPath).use { stream ->
            GoogleCredentials.fromStream(stream)
        }
        StorageOptions.newBuilder()
            .setCredentials(credentials)
            .build()
            .service
    }

    override fun store(kind: MediaKind, upload: MediaUpload): StoredMedia {
        val extension = extractExtension(upload.fileName, upload.contentType)
        val generatedName = "${kind.name.lowercase()}-${UUID.randomUUID()}$extension"
        val blobPath = "media/${kind.name.lowercase()}/$generatedName"

        val blobId = BlobId.of(bucketName, blobPath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(upload.contentType)
            .setAcl(listOf(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
            .build()

        storage.create(blobInfo, upload.bytes)

        val publicUrl = "https://storage.googleapis.com/$bucketName/$blobPath"

        return StoredMedia(
            kind = kind,
            url = publicUrl,
            fileName = generatedName,
            contentType = upload.contentType,
            sizeBytes = upload.bytes.size.toLong(),
            durationSeconds = null,
        )
    }

    override fun resolve(kind: MediaKind, fileName: String): File? {
        // Files are served directly from Firebase Storage, not from local filesystem
        return null
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
