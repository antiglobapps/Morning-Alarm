package com.morningalarm.server.modules.ringtone.application

import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.ringtone.application.ports.RingtoneRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneLikeToggleResult
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.shared.errors.NotFoundException
import com.morningalarm.server.shared.errors.ValidationException

class RingtoneService(
    private val ringtoneRepository: RingtoneRepository,
    private val tokenFactory: TokenFactory,
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
        return ringtoneRepository.create(ringtone).toAdminView()
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
        return (ringtoneRepository.update(ringtone)
            ?: throw NotFoundException("Ringtone not found: $ringtoneId")).toAdminView()
    }

    fun setVisibility(adminUserId: String, ringtoneId: String, visibility: RingtoneVisibility): RingtoneView {
        val existing = detailForAdmin(ringtoneId).ringtone
        return ringtoneRepository.update(
            existing.copy(
                visibility = visibility,
                updatedAtEpochSeconds = nowEpochSeconds(),
                updatedByAdminId = requireAdminUserId(adminUserId),
            ),
        )?.toAdminView() ?: throw NotFoundException("Ringtone not found: $ringtoneId")
    }

    fun togglePremium(adminUserId: String, ringtoneId: String): RingtoneView {
        val existing = detailForAdmin(ringtoneId).ringtone
        return ringtoneRepository.update(
            existing.copy(
                isPremium = !existing.isPremium,
                updatedAtEpochSeconds = nowEpochSeconds(),
                updatedByAdminId = requireAdminUserId(adminUserId),
            ),
        )?.toAdminView() ?: throw NotFoundException("Ringtone not found: $ringtoneId")
    }

    fun delete(ringtoneId: String) {
        requireRingtoneId(ringtoneId)
        if (!ringtoneRepository.delete(ringtoneId)) {
            throw NotFoundException("Ringtone not found: $ringtoneId")
        }
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

    private fun Ringtone.toAdminView(): RingtoneView = RingtoneView(
        ringtone = this,
        likesCount = ringtoneRepository.findForAdmin(id)?.likesCount ?: 0,
        isLikedByUser = false,
    )

    private fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1000L
}
