package com.morningalarm.dto.ringtone

import com.morningalarm.Seconds
import com.morningalarm.Url
import kotlinx.serialization.Serializable

@Serializable
enum class RingtoneVisibilityDto {
    INACTIVE,
    PRIVATE,
    PUBLIC,
}

@Serializable
enum class RingtoneSourceDto {
    SYSTEM,
    USER,
}

@Serializable
data class RingtoneListItemDto(
    val id: String,
    val title: String,
    val imageUrl: Url,
    val audioUrl: Url,
    val durationSeconds: Seconds,
    val description: String,
    val isPremium: Boolean,
    val likesCount: Int,
    val isLikedByUser: Boolean,
    val source: RingtoneSourceDto,
    val isOwnedByCurrentUser: Boolean,
)

@Serializable
data class ToggleRingtoneLikeResponseDto(
    val ringtoneId: String,
    val isLikedByUser: Boolean,
    val likesCount: Int,
)

@Serializable
data class RingtoneListResponseDto(
    val items: List<RingtoneListItemDto>,
)

@Serializable
data class RingtoneDetailResponseDto(
    val ringtone: RingtoneListItemDto,
)
