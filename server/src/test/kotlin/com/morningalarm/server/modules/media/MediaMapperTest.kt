package com.morningalarm.server.modules.media

import com.morningalarm.dto.upload.MediaKindDto
import com.morningalarm.server.modules.media.api.toAudioUploadResponseDto
import com.morningalarm.server.modules.media.api.toImageUploadResponseDto
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.StoredMedia
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaMapperTest {
    @Test
    fun `image media maps to upload image response dto`() {
        val storedMedia = StoredMedia(
            kind = MediaKind.IMAGE,
            url = "https://cdn.example.com/image.jpg",
            fileName = "image.jpg",
            contentType = "image/jpeg",
            sizeBytes = 123,
            durationSeconds = null,
        )

        val response = storedMedia.toImageUploadResponseDto()

        assertEquals(MediaKindDto.IMAGE, response.media.kind)
        assertEquals("https://cdn.example.com/image.jpg", response.media.url)
        assertEquals("image.jpg", response.media.fileName)
        assertEquals("image/jpeg", response.media.contentType)
        assertEquals(123, response.media.sizeBytes)
        assertNull(response.media.durationSeconds)
    }

    @Test
    fun `audio media maps to upload audio response dto including duration`() {
        val storedMedia = StoredMedia(
            kind = MediaKind.AUDIO,
            url = "https://cdn.example.com/audio.mp3",
            fileName = "audio.mp3",
            contentType = "audio/mpeg",
            sizeBytes = 456,
            durationSeconds = 42,
        )

        val response = storedMedia.toAudioUploadResponseDto()

        assertEquals(MediaKindDto.AUDIO, response.media.kind)
        assertEquals("https://cdn.example.com/audio.mp3", response.media.url)
        assertEquals("audio.mp3", response.media.fileName)
        assertEquals("audio/mpeg", response.media.contentType)
        assertEquals(456, response.media.sizeBytes)
        assertEquals(42, response.media.durationSeconds)
    }
}
