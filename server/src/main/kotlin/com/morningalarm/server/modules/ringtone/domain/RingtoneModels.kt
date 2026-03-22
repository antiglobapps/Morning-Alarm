package com.morningalarm.server.modules.ringtone.domain

enum class RingtoneVisibility {
    INACTIVE,
    PRIVATE,
    PUBLIC,
}

enum class RingtoneListFilter {
    ALL,
    MY,
    SYSTEM,
}

data class Ringtone(
    val id: String,
    val title: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val description: String,
    val visibility: RingtoneVisibility,
    val isPremium: Boolean,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
    val createdByAdminId: String?,
    val updatedByAdminId: String?,
    val createdByUserId: String?,
)

data class RingtoneView(
    val ringtone: Ringtone,
    val likesCount: Int,
    val isLikedByUser: Boolean,
)

data class RingtoneLikeToggleResult(
    val ringtoneId: String,
    val isLikedByUser: Boolean,
    val likesCount: Int,
)
