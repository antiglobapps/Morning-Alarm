package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.server.modules.ringtone.domain.RingtoneView

fun RingtoneView.toDto(currentUserId: String): RingtoneListItemDto = RingtoneListItemDto(
    id = ringtone.id,
    title = ringtone.title,
    imageUrl = ringtone.imageUrl,
    audioUrl = ringtone.audioUrl,
    durationSeconds = ringtone.durationSeconds,
    description = ringtone.description,
    isPremium = ringtone.isPremium,
    likesCount = likesCount,
    isLikedByUser = isLikedByUser,
    source = if (ringtone.createdByUserId != null) RingtoneSourceDto.USER else RingtoneSourceDto.SYSTEM,
    isOwnedByCurrentUser = ringtone.createdByUserId == currentUserId,
)
