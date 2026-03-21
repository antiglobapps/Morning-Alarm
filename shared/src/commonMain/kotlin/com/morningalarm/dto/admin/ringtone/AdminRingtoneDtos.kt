package com.morningalarm.dto.admin.ringtone

import com.morningalarm.dto.ringtone.RingtoneListItemDto
import kotlinx.serialization.Serializable

@Serializable
data class AdminRingtoneListItemDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val isActive: Boolean,
    val isPremium: Boolean,
    val likesCount: Int,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
)

@Serializable
data class AdminRingtoneDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val isActive: Boolean,
    val isPremium: Boolean,
    val likesCount: Int,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
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
    val isActive: Boolean,
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
    val isActive: Boolean,
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
data class ToggleRingtoneActiveResponseDto(
    val ringtoneId: String,
    val isActive: Boolean,
)

@Serializable
data class ToggleRingtonePremiumResponseDto(
    val ringtoneId: String,
    val isPremium: Boolean,
)
