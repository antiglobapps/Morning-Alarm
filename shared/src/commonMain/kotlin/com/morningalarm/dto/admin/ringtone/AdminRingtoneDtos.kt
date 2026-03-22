package com.morningalarm.dto.admin.ringtone

import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import kotlinx.serialization.Serializable

@Serializable
data class AdminRingtoneListItemDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
    val isPremium: Boolean,
    val likesCount: Int,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
    val createdByAdminId: String?,
    val createdByUserId: String?,
)

@Serializable
data class AdminRingtoneDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
    val isPremium: Boolean,
    val likesCount: Int,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
    val createdByAdminId: String?,
    val createdByUserId: String?,
    val preview: RingtoneListItemDto,
)

@Serializable
data class AdminRingtoneListResponseDto(
    val items: List<AdminRingtoneListItemDto>,
)

@Serializable
data class AdminRingtoneDetailResponseDto(
    val ringtone: AdminRingtoneDetailDto,
)

@Serializable
data class CreateAdminRingtoneRequestDto(
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
    val isPremium: Boolean,
)

@Serializable
data class CreateAdminRingtoneResponseDto(
    val ringtone: AdminRingtoneDetailDto,
)

@Serializable
data class UpdateAdminRingtoneRequestDto(
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
    val isPremium: Boolean,
)

@Serializable
data class UpdateAdminRingtoneResponseDto(
    val ringtone: AdminRingtoneDetailDto,
)

@Serializable
data class DeleteAdminRingtoneResponseDto(
    val ringtoneId: String,
    val deleted: Boolean,
)

@Serializable
data class AdminRingtonePreviewResponseDto(
    val ringtoneId: String,
    val preview: RingtoneListItemDto,
)

@Serializable
data class AdminRingtoneClientListPreviewResponseDto(
    val items: List<RingtoneListItemDto>,
)

@Serializable
data class SetRingtoneVisibilityRequestDto(
    val visibility: RingtoneVisibilityDto,
)

@Serializable
data class SetRingtoneVisibilityResponseDto(
    val ringtoneId: String,
    val visibility: RingtoneVisibilityDto,
)

@Serializable
data class ToggleRingtonePremiumResponseDto(
    val ringtoneId: String,
    val isPremium: Boolean,
)
