package com.morningalarm.server.modules.ringtone.application

import com.morningalarm.server.modules.auth.application.ports.Clock
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.ringtone.application.ports.RingtoneRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneLikeToggleResult
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.shared.audit.AuditEvent
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.errors.NotFoundException
import com.morningalarm.server.shared.errors.ValidationException

class RingtoneService(
    private val ringtoneRepository: RingtoneRepository,
    private val tokenFactory: TokenFactory,
    private val auditLogger: AuditLogger,
    private val clock: Clock,
) {
    fun list(userId: String, filter: RingtoneListFilter = RingtoneListFilter.ALL): List<RingtoneView> =
        ringtoneRepository.listForUser(userId, filter)

    fun detail(userId: String, ringtoneId: String): RingtoneView {
        requireRingtoneId(ringtoneId)
        return ringtoneRepository.findForUser(userId, ringtoneId)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")
    }

    fun listForAdmin(): List<RingtoneView> = ringtoneRepository.listForAdmin()

    fun detailForAdmin(ringtoneId: String): RingtoneView {
        requireRingtoneId(ringtoneId)
        return ringtoneRepository.findForAdmin(ringtoneId)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")
    }

    fun create(
        adminUserId: String,
        title: String,
        imageUrl: String,
        audioUrl: String,
        durationSeconds: Int,
        description: String,
        visibility: RingtoneVisibility,
        isPremium: Boolean,
    ): RingtoneView {
        val nowEpochSeconds = nowEpochSeconds()
        val ringtone = Ringtone(
            id = tokenFactory.newRingtoneId(),
            title = title.trim().ifBlank { throw ValidationException("Ringtone title must not be blank") },
            imageUrl = validateImageUrl(imageUrl),
            audioUrl = validateAudioUrl(audioUrl),
            durationSeconds = validateDuration(durationSeconds),
            description = description.trim().ifBlank { throw ValidationException("Ringtone description must not be blank") },
            visibility = visibility,
            isPremium = isPremium,
            createdAtEpochSeconds = nowEpochSeconds,
            updatedAtEpochSeconds = nowEpochSeconds,
            createdByAdminId = requireAdminUserId(adminUserId),
            updatedByAdminId = adminUserId,
            createdByUserId = null,
        )
        val saved = ringtoneRepository.create(ringtone)
        auditLogger.log(AuditEvent.RingtoneCreated(ringtoneId = ringtone.id, title = ringtone.title, adminId = adminUserId))
        return saved.toAdminView(likesCount = 0)
    }

    fun update(
        adminUserId: String,
        ringtoneId: String,
        title: String,
        imageUrl: String,
        audioUrl: String,
        durationSeconds: Int,
        description: String,
        visibility: RingtoneVisibility,
        isPremium: Boolean,
    ): RingtoneView {
        requireRingtoneId(ringtoneId)
        val existing = ringtoneRepository.findForAdmin(ringtoneId)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")
        val ringtone = Ringtone(
            id = ringtoneId,
            title = title.trim().ifBlank { throw ValidationException("Ringtone title must not be blank") },
            imageUrl = validateImageUrl(imageUrl),
            audioUrl = validateAudioUrl(audioUrl),
            durationSeconds = validateDuration(durationSeconds),
            description = description.trim().ifBlank { throw ValidationException("Ringtone description must not be blank") },
            visibility = visibility,
            isPremium = isPremium,
            createdAtEpochSeconds = existing.ringtone.createdAtEpochSeconds,
            updatedAtEpochSeconds = nowEpochSeconds(),
            createdByAdminId = existing.ringtone.createdByAdminId,
            updatedByAdminId = requireAdminUserId(adminUserId),
            createdByUserId = existing.ringtone.createdByUserId,
        )
        val saved = ringtoneRepository.update(ringtone)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")
        auditLogger.log(AuditEvent.RingtoneUpdated(ringtoneId = ringtoneId, title = ringtone.title, adminId = adminUserId))
        return saved.toAdminView(likesCount = existing.likesCount)
    }

    fun setVisibility(adminUserId: String, ringtoneId: String, visibility: RingtoneVisibility): RingtoneView {
        val existingView = detailForAdmin(ringtoneId)
        val ringtone = ringtoneRepository.update(
            existingView.ringtone.copy(
                visibility = visibility,
                updatedAtEpochSeconds = nowEpochSeconds(),
                updatedByAdminId = requireAdminUserId(adminUserId),
            ),
        ) ?: throw NotFoundException("Ringtone not found: $ringtoneId")
        auditLogger.log(AuditEvent.RingtoneVisibilityChanged(ringtoneId = ringtoneId, visibility = visibility.name, adminId = adminUserId))
        return ringtone.toAdminView(likesCount = existingView.likesCount)
    }

    fun togglePremium(adminUserId: String, ringtoneId: String): RingtoneView {
        val existingView = detailForAdmin(ringtoneId)
        val saved = ringtoneRepository.update(
            existingView.ringtone.copy(
                isPremium = !existingView.ringtone.isPremium,
                updatedAtEpochSeconds = nowEpochSeconds(),
                updatedByAdminId = requireAdminUserId(adminUserId),
            ),
        ) ?: throw NotFoundException("Ringtone not found: $ringtoneId")
        val view = saved.toAdminView(likesCount = existingView.likesCount)
        auditLogger.log(AuditEvent.RingtonePremiumToggled(ringtoneId = ringtoneId, isPremium = view.ringtone.isPremium, adminId = adminUserId))
        return view
    }

    fun delete(ringtoneId: String, adminUserId: String) {
        requireRingtoneId(ringtoneId)
        if (!ringtoneRepository.delete(ringtoneId)) {
            throw NotFoundException("Ringtone not found: $ringtoneId")
        }
        auditLogger.log(AuditEvent.RingtoneDeleted(ringtoneId = ringtoneId, adminId = adminUserId))
    }

    fun toggleLike(userId: String, ringtoneId: String): RingtoneLikeToggleResult {
        requireRingtoneId(ringtoneId)
        return ringtoneRepository.toggleLike(userId, ringtoneId)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")
    }

    private fun validateImageUrl(value: String): String {
        return validateAbsoluteUrl(value, "Ringtone imageUrl must be an absolute URL")
    }

    private fun validateAudioUrl(value: String): String {
        return validateAbsoluteUrl(value, "Ringtone audioUrl must be an absolute URL")
    }

    private fun validateAbsoluteUrl(value: String, message: String): String {
        val normalized = value.trim()
        if (!(normalized.startsWith("http://") || normalized.startsWith("https://"))) {
            throw ValidationException(message)
        }
        return normalized
    }

    private fun validateDuration(value: Int): Int {
        if (value <= 0) {
            throw ValidationException("Ringtone durationSeconds must be positive")
        }
        return value
    }

    private fun requireRingtoneId(ringtoneId: String) {
        if (ringtoneId.isBlank()) {
            throw ValidationException("Ringtone id must not be blank")
        }
    }

    private fun requireAdminUserId(adminUserId: String): String {
        if (adminUserId.isBlank()) {
            throw ValidationException("Admin user id must not be blank")
        }
        return adminUserId
    }

    private fun Ringtone.toAdminView(likesCount: Int = 0): RingtoneView = RingtoneView(
        ringtone = this,
        likesCount = likesCount,
        isLikedByUser = false,
    )

    private fun nowEpochSeconds(): Long = clock.epochSeconds()
}
