package com.morningalarm.server.modules.ringtone.domain

import com.morningalarm.EpochSeconds
import com.morningalarm.Seconds
import com.morningalarm.Url
import com.morningalarm.UserId

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
    val imageUrl: Url,
    val audioUrl: Url,
    val durationSeconds: Seconds,
    val description: String,
    val visibility: RingtoneVisibility,
    val isPremium: Boolean,
    val createdAtEpochSeconds: EpochSeconds,
    val updatedAtEpochSeconds: EpochSeconds,
    val createdByAdminId: UserId?,
    val updatedByAdminId: UserId?,
    val createdByUserId: UserId?,
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
