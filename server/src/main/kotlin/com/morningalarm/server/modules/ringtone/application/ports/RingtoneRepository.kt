package com.morningalarm.server.modules.ringtone.application.ports

import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.modules.ringtone.domain.RingtoneLikeToggleResult
import com.morningalarm.server.modules.ringtone.domain.RingtoneView

interface RingtoneRepository {
    fun listForUser(userId: String, filter: RingtoneListFilter): List<RingtoneView>
    fun findForUser(userId: String, ringtoneId: String): RingtoneView?
    fun listForAdmin(): List<RingtoneView>
    fun findForAdmin(ringtoneId: String): RingtoneView?
    fun create(ringtone: Ringtone): Ringtone
    fun update(ringtone: Ringtone): Ringtone?
    fun delete(ringtoneId: String): Boolean
    fun toggleLike(userId: String, ringtoneId: String): RingtoneLikeToggleResult?
}
