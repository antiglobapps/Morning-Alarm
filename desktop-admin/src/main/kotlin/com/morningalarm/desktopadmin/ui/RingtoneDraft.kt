package com.morningalarm.desktopadmin.ui

import com.morningalarm.desktopadmin.data.ApiClientException
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto

internal data class RingtoneDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val durationSeconds: String = "30",
    val isActive: Boolean = false,
    val isPremium: Boolean = false,
)

internal fun AdminRingtoneDetailDto.toDraft(): RingtoneDraft = RingtoneDraft(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds.toString(),
    isActive = isActive,
    isPremium = isPremium,
)

internal fun RingtoneDraft.toPreview(previousPreview: RingtoneListItemDto?): RingtoneListItemDto = RingtoneListItemDto(
    id = id ?: previousPreview?.id ?: "draft-preview",
    title = title.ifBlank { "Untitled ringtone" },
    imageUrl = imageUrl.trim(),
    audioUrl = audioUrl.trim(),
    durationSeconds = durationSeconds.toIntOrNull() ?: 0,
    description = description.ifBlank { "Description will appear here." },
    isPremium = isPremium,
    likesCount = previousPreview?.likesCount ?: 0,
    isLikedByUser = previousPreview?.isLikedByUser ?: false,
)

internal data class RingtoneRequestData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val isActive: Boolean,
    val isPremium: Boolean,
)

internal fun RingtoneDraft.toCreateOrUpdateRequest(): RingtoneRequestData {
    val duration = durationSeconds.toIntOrNull()
        ?: throw ApiClientException("Duration must be a valid integer")
    return RingtoneRequestData(
        title = title.trim(),
        description = description.trim(),
        imageUrl = imageUrl.trim(),
        audioUrl = audioUrl.trim(),
        durationSeconds = duration,
        isActive = isActive,
        isPremium = isPremium,
    )
}

internal fun RingtoneRequestData.toCreateRequest(): CreateAdminRingtoneRequestDto = CreateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    isActive = isActive,
    isPremium = isPremium,
)

internal fun RingtoneRequestData.toUpdateRequest(): UpdateAdminRingtoneRequestDto = UpdateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    isActive = isActive,
    isPremium = isPremium,
)
