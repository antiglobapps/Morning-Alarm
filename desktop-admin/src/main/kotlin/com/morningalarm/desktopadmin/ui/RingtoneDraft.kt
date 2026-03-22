package com.morningalarm.desktopadmin.ui

import com.morningalarm.desktopadmin.data.ApiClientException
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto

internal data class RingtoneDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val durationSeconds: String = "30",
    val visibility: RingtoneVisibilityDto = RingtoneVisibilityDto.INACTIVE,
    val isPremium: Boolean = false,
    val createdByUserId: String? = null,
)

internal fun AdminRingtoneDetailDto.toDraft(): RingtoneDraft = RingtoneDraft(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds.toString(),
    visibility = visibility,
    isPremium = isPremium,
    createdByUserId = createdByUserId,
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
    source = if (createdByUserId != null) RingtoneSourceDto.USER else RingtoneSourceDto.SYSTEM,
    isOwnedByCurrentUser = previousPreview?.isOwnedByCurrentUser ?: false,
)

internal data class RingtoneRequestData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
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
        visibility = visibility,
        isPremium = isPremium,
    )
}

internal fun RingtoneRequestData.toCreateRequest(): CreateAdminRingtoneRequestDto = CreateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    visibility = visibility,
    isPremium = isPremium,
)

internal fun RingtoneRequestData.toUpdateRequest(): UpdateAdminRingtoneRequestDto = UpdateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    visibility = visibility,
    isPremium = isPremium,
)
