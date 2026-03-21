package com.morningalarm.server.modules.media.api

import com.morningalarm.dto.upload.MediaKindDto
import com.morningalarm.dto.upload.UploadAudioResponseDto
import com.morningalarm.dto.upload.UploadImageResponseDto
import com.morningalarm.dto.upload.UploadedMediaDto
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.StoredMedia

fun StoredMedia.toImageUploadResponseDto(): UploadImageResponseDto = UploadImageResponseDto(media = toDto())

fun StoredMedia.toAudioUploadResponseDto(): UploadAudioResponseDto = UploadAudioResponseDto(media = toDto())

private fun StoredMedia.toDto(): UploadedMediaDto = UploadedMediaDto(
    kind = kind.toDto(),
    url = url,
    fileName = fileName,
    contentType = contentType,
    sizeBytes = sizeBytes,
    durationSeconds = durationSeconds,
)

private fun MediaKind.toDto(): MediaKindDto = when (this) {
    MediaKind.IMAGE -> MediaKindDto.IMAGE
    MediaKind.AUDIO -> MediaKindDto.AUDIO
}
