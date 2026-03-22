package com.morningalarm.server.modules.ringtone

import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import com.morningalarm.server.modules.ringtone.api.toAdminDetailDto
import com.morningalarm.server.modules.ringtone.api.toAdminListItemDto
import com.morningalarm.server.modules.ringtone.api.toDomain
import com.morningalarm.server.modules.ringtone.api.toDto
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdminRingtoneMapperTest {
    @Test
    fun `admin list item mapper preserves ringtone fields`() {
        val dto = ringtoneView(visibility = RingtoneVisibility.PRIVATE, createdByUserId = "user-1").toAdminListItemDto()

        assertEquals("rng-1", dto.id)
        assertEquals("Morning Bell", dto.title)
        assertEquals("Wake up gently", dto.description)
        assertEquals("https://cdn.example.com/ringtone.jpg", dto.imageUrl)
        assertEquals("https://cdn.example.com/ringtone.mp3", dto.audioUrl)
        assertEquals(30, dto.durationSeconds)
        assertEquals(RingtoneVisibilityDto.PRIVATE, dto.visibility)
        assertEquals(true, dto.isPremium)
        assertEquals(4, dto.likesCount)
        assertEquals(100, dto.createdAtEpochSeconds)
        assertEquals(200, dto.updatedAtEpochSeconds)
        assertEquals("admin-1", dto.createdByAdminId)
        assertEquals("user-1", dto.createdByUserId)
    }

    @Test
    fun `admin detail mapper includes client preview based on ringtone mapper`() {
        val dto = ringtoneView(visibility = RingtoneVisibility.PUBLIC, createdByUserId = "admin-1")
            .toAdminDetailDto(adminUserId = "admin-1")

        assertEquals("rng-1", dto.id)
        assertEquals(RingtoneVisibilityDto.PUBLIC, dto.visibility)
        assertEquals("rng-1", dto.preview.id)
        assertEquals(RingtoneSourceDto.USER, dto.preview.source)
        assertTrue(dto.preview.isOwnedByCurrentUser)
        assertTrue(dto.preview.isLikedByUser)
        assertEquals(4, dto.preview.likesCount)
    }

    @Test
    fun `ringtone visibility maps both directions for all values`() {
        assertEquals(RingtoneVisibilityDto.INACTIVE, RingtoneVisibility.INACTIVE.toDto())
        assertEquals(RingtoneVisibilityDto.PRIVATE, RingtoneVisibility.PRIVATE.toDto())
        assertEquals(RingtoneVisibilityDto.PUBLIC, RingtoneVisibility.PUBLIC.toDto())

        assertEquals(RingtoneVisibility.INACTIVE, RingtoneVisibilityDto.INACTIVE.toDomain())
        assertEquals(RingtoneVisibility.PRIVATE, RingtoneVisibilityDto.PRIVATE.toDomain())
        assertEquals(RingtoneVisibility.PUBLIC, RingtoneVisibilityDto.PUBLIC.toDomain())
    }

    private fun ringtoneView(
        visibility: RingtoneVisibility,
        createdByUserId: String?,
    ) = RingtoneView(
        ringtone = Ringtone(
            id = "rng-1",
            title = "Morning Bell",
            imageUrl = "https://cdn.example.com/ringtone.jpg",
            audioUrl = "https://cdn.example.com/ringtone.mp3",
            durationSeconds = 30,
            description = "Wake up gently",
            visibility = visibility,
            isPremium = true,
            createdAtEpochSeconds = 100,
            updatedAtEpochSeconds = 200,
            createdByAdminId = "admin-1",
            updatedByAdminId = "admin-1",
            createdByUserId = createdByUserId,
        ),
        likesCount = 4,
        isLikedByUser = true,
    )
}
