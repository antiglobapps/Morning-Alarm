package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.modules.ringtone.domain.RingtoneView

fun RingtoneView.toAdminListItemDto(): AdminRingtoneListItemDto = AdminRingtoneListItemDto(
    id = ringtone.id,
    title = ringtone.title,
    description = ringtone.description,
    imageUrl = ringtone.imageUrl,
    audioUrl = ringtone.audioUrl,
    durationSeconds = ringtone.durationSeconds,
    visibility = ringtone.visibility.toDto(),
    isPremium = ringtone.isPremium,
    likesCount = likesCount,
    createdAtEpochSeconds = ringtone.createdAtEpochSeconds,
    updatedAtEpochSeconds = ringtone.updatedAtEpochSeconds,
    createdByAdminId = ringtone.createdByAdminId,
    createdByUserId = ringtone.createdByUserId,
)

fun RingtoneView.toAdminDetailDto(adminUserId: String): AdminRingtoneDetailDto = AdminRingtoneDetailDto(
    id = ringtone.id,
    title = ringtone.title,
    description = ringtone.description,
    imageUrl = ringtone.imageUrl,
    audioUrl = ringtone.audioUrl,
    durationSeconds = ringtone.durationSeconds,
    visibility = ringtone.visibility.toDto(),
    isPremium = ringtone.isPremium,
    likesCount = likesCount,
    createdAtEpochSeconds = ringtone.createdAtEpochSeconds,
    updatedAtEpochSeconds = ringtone.updatedAtEpochSeconds,
    createdByAdminId = ringtone.createdByAdminId,
    createdByUserId = ringtone.createdByUserId,
    preview = toDto(adminUserId),
)

fun RingtoneVisibility.toDto(): RingtoneVisibilityDto = when (this) {
    RingtoneVisibility.INACTIVE -> RingtoneVisibilityDto.INACTIVE
    RingtoneVisibility.PRIVATE -> RingtoneVisibilityDto.PRIVATE
    RingtoneVisibility.PUBLIC -> RingtoneVisibilityDto.PUBLIC
}

fun RingtoneVisibilityDto.toDomain(): RingtoneVisibility = when (this) {
    RingtoneVisibilityDto.INACTIVE -> RingtoneVisibility.INACTIVE
    RingtoneVisibilityDto.PRIVATE -> RingtoneVisibility.PRIVATE
    RingtoneVisibilityDto.PUBLIC -> RingtoneVisibility.PUBLIC
}
