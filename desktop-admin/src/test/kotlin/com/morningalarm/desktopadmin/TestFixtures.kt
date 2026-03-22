package com.morningalarm.desktopadmin

import com.morningalarm.desktopadmin.data.AdminRingtoneRepository
import com.morningalarm.desktopadmin.ui.AdminSession
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.auth.AuthSessionDto
import com.morningalarm.dto.auth.UserRoleDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import com.morningalarm.dto.upload.MediaKindDto
import com.morningalarm.dto.upload.UploadedMediaDto
import java.io.File

internal fun testAuthSession() = AuthSessionDto(
    userId = "admin-1",
    role = UserRoleDto.ADMIN,
    bearerToken = "test-bearer",
    refreshToken = "test-refresh",
    expiresAtEpochSeconds = 9999999999L,
    isNewUser = false,
)

internal fun testAdminSession() = AdminSession(
    baseUrl = "http://localhost:8080",
    adminSecret = "test-secret",
    authSession = testAuthSession(),
)

internal fun testRingtoneListItem(
    id: String = "ringtone-1",
    title: String = "Morning Sunrise",
    visibility: RingtoneVisibilityDto = RingtoneVisibilityDto.PUBLIC,
    isPremium: Boolean = false,
) = AdminRingtoneListItemDto(
    id = id,
    title = title,
    description = "A gentle morning melody",
    imageUrl = "https://example.com/image.jpg",
    audioUrl = "https://example.com/audio.mp3",
    durationSeconds = 30,
    visibility = visibility,
    isPremium = isPremium,
    likesCount = 5,
    createdAtEpochSeconds = 1700000000L,
    updatedAtEpochSeconds = 1700000000L,
    createdByAdminId = "admin-1",
    createdByUserId = null,
)

internal fun testPreviewItem(
    id: String = "ringtone-1",
    title: String = "Morning Sunrise",
    isPremium: Boolean = false,
) = RingtoneListItemDto(
    id = id,
    title = title,
    imageUrl = "https://example.com/image.jpg",
    audioUrl = "https://example.com/audio.mp3",
    durationSeconds = 30,
    description = "A gentle morning melody",
    isPremium = isPremium,
    likesCount = 5,
    isLikedByUser = false,
    source = RingtoneSourceDto.SYSTEM,
    isOwnedByCurrentUser = false,
)

internal fun testRingtoneDetail(
    id: String = "ringtone-1",
    title: String = "Morning Sunrise",
    visibility: RingtoneVisibilityDto = RingtoneVisibilityDto.PUBLIC,
    isPremium: Boolean = false,
) = AdminRingtoneDetailDto(
    id = id,
    title = title,
    description = "A gentle morning melody",
    imageUrl = "https://example.com/image.jpg",
    audioUrl = "https://example.com/audio.mp3",
    durationSeconds = 30,
    visibility = visibility,
    isPremium = isPremium,
    likesCount = 5,
    createdAtEpochSeconds = 1700000000L,
    updatedAtEpochSeconds = 1700000000L,
    createdByAdminId = "admin-1",
    createdByUserId = null,
    preview = testPreviewItem(id = id, title = title, isPremium = isPremium),
)

internal class FakeAdminRingtoneRepository : AdminRingtoneRepository {

    var ringtones = mutableListOf(testRingtoneListItem())
    var detail = testRingtoneDetail()
    var clientPreview = listOf(testPreviewItem())
    var shouldFail: Exception? = null

    var createCallCount = 0
    var updateCallCount = 0
    var deleteCallCount = 0
    var setVisibilityCallCount = 0
    var togglePremiumCallCount = 0
    var uploadImageCallCount = 0
    var uploadAudioCallCount = 0
    var closeCallCount = 0

    private fun failIfNeeded() {
        shouldFail?.let { throw it }
    }

    override suspend fun listRingtones(): List<AdminRingtoneListItemDto> {
        failIfNeeded()
        return ringtones
    }

    override suspend fun getRingtoneDetail(ringtoneId: String): AdminRingtoneDetailDto {
        failIfNeeded()
        return detail
    }

    override suspend fun createRingtone(request: CreateAdminRingtoneRequestDto): AdminRingtoneDetailDto {
        failIfNeeded()
        createCallCount++
        return detail
    }

    override suspend fun updateRingtone(ringtoneId: String, request: UpdateAdminRingtoneRequestDto): AdminRingtoneDetailDto {
        failIfNeeded()
        updateCallCount++
        return detail
    }

    override suspend fun deleteRingtone(ringtoneId: String) {
        failIfNeeded()
        deleteCallCount++
    }

    override suspend fun setVisibility(ringtoneId: String, visibility: RingtoneVisibilityDto): SetRingtoneVisibilityResponseDto {
        failIfNeeded()
        setVisibilityCallCount++
        return SetRingtoneVisibilityResponseDto(ringtoneId = ringtoneId, visibility = visibility)
    }

    override suspend fun togglePremium(ringtoneId: String): ToggleRingtonePremiumResponseDto {
        failIfNeeded()
        togglePremiumCallCount++
        return ToggleRingtonePremiumResponseDto(ringtoneId = ringtoneId, isPremium = !detail.isPremium)
    }

    override suspend fun getPreview(ringtoneId: String): RingtoneListItemDto {
        failIfNeeded()
        return detail.preview
    }

    override suspend fun getClientPreview(): List<RingtoneListItemDto> {
        failIfNeeded()
        return clientPreview
    }

    override suspend fun uploadImage(file: File): UploadedMediaDto {
        failIfNeeded()
        uploadImageCallCount++
        return UploadedMediaDto(
            kind = MediaKindDto.IMAGE,
            url = "https://storage.example.com/uploaded-image.jpg",
            fileName = file.name,
            contentType = "image/jpeg",
            sizeBytes = 1024,
            durationSeconds = null,
        )
    }

    override suspend fun uploadAudio(file: File): UploadedMediaDto {
        failIfNeeded()
        uploadAudioCallCount++
        return UploadedMediaDto(
            kind = MediaKindDto.AUDIO,
            url = "https://storage.example.com/uploaded-audio.mp3",
            fileName = file.name,
            contentType = "audio/mpeg",
            sizeBytes = 2048,
            durationSeconds = 45,
        )
    }

    override fun close() {
        closeCallCount++
    }
}
