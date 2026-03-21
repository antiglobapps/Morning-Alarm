package com.morningalarm.server.modules.ringtone.domain

data class Ringtone(
    val id: String,
    val title: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val description: String,
    val isActive: Boolean,
    val isPremium: Boolean,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
    val createdByAdminId: String,
    val updatedByAdminId: String,
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
