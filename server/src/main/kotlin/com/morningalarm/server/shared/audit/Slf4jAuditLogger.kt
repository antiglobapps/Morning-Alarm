package com.morningalarm.server.shared.audit

import org.slf4j.LoggerFactory

/**
 * Writes audit events to a dedicated SLF4J logger named "audit".
 * Security-sensitive events (failures, privilege changes) are logged at WARN level.
 * Normal operational events are logged at INFO level.
 */
class Slf4jAuditLogger : AuditLogger {
    private val logger = LoggerFactory.getLogger("audit")

    override fun log(event: AuditEvent) {
        val message = format(event)
        when (event) {
            is AuditEvent.AdminLoginFailure,
            is AuditEvent.AdminCreated,
            is AuditEvent.AdminPromoted,
            is AuditEvent.SessionsRevoked,
            -> logger.warn(message)
            else -> logger.info(message)
        }
    }

    private fun format(event: AuditEvent): String = when (event) {
        is AuditEvent.AdminCreated ->
            "ADMIN_CREATED adminId=${event.adminId} email=${sanitize(event.email)}"
        is AuditEvent.AdminPromoted ->
            "ADMIN_PROMOTED adminId=${event.adminId} email=${sanitize(event.email)}"
        is AuditEvent.AdminLoginSuccess ->
            "ADMIN_LOGIN_SUCCESS adminId=${event.adminId} email=${sanitize(event.email)}"
        is AuditEvent.AdminLoginFailure ->
            "ADMIN_LOGIN_FAILURE email=${sanitize(event.email)} reason=${sanitize(event.reason)}"
        is AuditEvent.PasswordResetRequested ->
            "PASSWORD_RESET_REQUESTED email=${sanitize(event.email)}"
        is AuditEvent.PasswordResetConfirmed ->
            "PASSWORD_RESET_CONFIRMED userId=${event.userId}"
        is AuditEvent.SessionsRevoked ->
            "SESSIONS_REVOKED userId=${event.userId} reason=${sanitize(event.reason)}"
        is AuditEvent.RingtoneCreated ->
            "RINGTONE_CREATED ringtoneId=${event.ringtoneId} title=\"${sanitize(event.title)}\" adminId=${event.adminId}"
        is AuditEvent.RingtoneUpdated ->
            "RINGTONE_UPDATED ringtoneId=${event.ringtoneId} title=\"${sanitize(event.title)}\" adminId=${event.adminId}"
        is AuditEvent.RingtoneDeleted ->
            "RINGTONE_DELETED ringtoneId=${event.ringtoneId} adminId=${event.adminId}"
        is AuditEvent.RingtoneVisibilityChanged ->
            "RINGTONE_VISIBILITY_CHANGED ringtoneId=${event.ringtoneId} visibility=${event.visibility} adminId=${event.adminId}"
        is AuditEvent.RingtonePremiumToggled ->
            "RINGTONE_PREMIUM_TOGGLED ringtoneId=${event.ringtoneId} isPremium=${event.isPremium} adminId=${event.adminId}"
        is AuditEvent.MediaUploaded ->
            "MEDIA_UPLOADED kind=${event.kind} fileName=\"${sanitize(event.fileName)}\" sizeBytes=${event.sizeBytes} adminId=${event.adminId}"
    }

    /** Strips control characters to prevent log injection. */
    private fun sanitize(value: String): String =
        value.replace(Regex("[\\r\\n\\t]"), "_").replace("\"", "\\\"")
}
