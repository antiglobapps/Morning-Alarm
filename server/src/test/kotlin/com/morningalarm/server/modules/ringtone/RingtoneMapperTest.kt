package com.morningalarm.server.modules.ringtone

import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.server.modules.ringtone.api.toDto
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RingtoneMapperTest {
    @Test
    fun `ringtone view maps user-owned ringtone with user source and ownership`() {
        val dto = ringtoneView(
            createdByUserId = "user-1",
            isLikedByUser = true,
            likesCount = 7,
        ).toDto(currentUserId = "user-1")

        assertEquals("rng-1", dto.id)
        assertEquals("Morning Bell", dto.title)
        assertEquals("https://cdn.example.com/ringtone.jpg", dto.imageUrl)
        assertEquals("https://cdn.example.com/ringtone.mp3", dto.audioUrl)
        assertEquals(30, dto.durationSeconds)
        assertEquals("Wake up gently", dto.description)
        assertEquals(true, dto.isPremium)
        assertEquals(7, dto.likesCount)
        assertTrue(dto.isLikedByUser)
        assertEquals(RingtoneSourceDto.USER, dto.source)
        assertTrue(dto.isOwnedByCurrentUser)
    }

    @Test
    fun `ringtone view maps system ringtone with system source and no ownership`() {
        val dto = ringtoneView(
            createdByUserId = null,
            isLikedByUser = false,
            likesCount = 3,
        ).toDto(currentUserId = "user-1")

        assertEquals(RingtoneSourceDto.SYSTEM, dto.source)
        assertFalse(dto.isOwnedByCurrentUser)
        assertFalse(dto.isLikedByUser)
        assertEquals(3, dto.likesCount)
    }

    private fun ringtoneView(
        createdByUserId: String?,
        isLikedByUser: Boolean,
        likesCount: Int,
    ) = RingtoneView(
        ringtone = Ringtone(
            id = "rng-1",
            title = "Morning Bell",
            imageUrl = "https://cdn.example.com/ringtone.jpg",
            audioUrl = "https://cdn.example.com/ringtone.mp3",
            durationSeconds = 30,
            description = "Wake up gently",
            visibility = RingtoneVisibility.PUBLIC,
            isPremium = true,
            createdAtEpochSeconds = 100,
            updatedAtEpochSeconds = 200,
            createdByAdminId = "admin-1",
            updatedByAdminId = "admin-1",
            createdByUserId = createdByUserId,
        ),
        likesCount = likesCount,
        isLikedByUser = isLikedByUser,
    )
}
