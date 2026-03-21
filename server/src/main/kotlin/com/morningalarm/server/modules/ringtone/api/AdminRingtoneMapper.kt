package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.server.modules.ringtone.domain.RingtoneView

fun RingtoneView.toAdminListItemDto(): AdminRingtoneListItemDto = AdminRingtoneListItemDto(
    id = ringtone.id,
    title = ringtone.title,
    description = ringtone.description,
    imageUrl = ringtone.imageUrl,
    audioUrl = ringtone.audioUrl,
    durationSeconds = ringtone.durationSeconds,
    isActive = ringtone.isActive,
    isPremium = ringtone.isPremium,
    likesCount = likesCount,
    createdAtEpochSeconds = ringtone.createdAtEpochSeconds,
    updatedAtEpochSeconds = ringtone.updatedAtEpochSeconds,
)

fun RingtoneView.toAdminDetailDto(): AdminRingtoneDetailDto = AdminRingtoneDetailDto(
    id = ringtone.id,
    title = ringtone.title,
    description = ringtone.description,
    imageUrl = ringtone.imageUrl,
    audioUrl = ringtone.audioUrl,
    durationSeconds = ringtone.durationSeconds,
    isActive = ringtone.isActive,
    isPremium = ringtone.isPremium,
    likesCount = likesCount,
    createdAtEpochSeconds = ringtone.createdAtEpochSeconds,
    updatedAtEpochSeconds = ringtone.updatedAtEpochSeconds,
    preview = toDto(),
)
