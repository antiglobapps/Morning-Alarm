package com.morningalarm.server.shared.audit

sealed class AuditEvent {
    // Admin lifecycle
    data class AdminCreated(val adminId: String, val email: String) : AuditEvent()
    data class AdminPromoted(val adminId: String, val email: String) : AuditEvent()

    // Admin login
    data class AdminLoginSuccess(val adminId: String, val email: String) : AuditEvent()
    data class AdminLoginFailure(val email: String, val reason: String) : AuditEvent()

    // Password recovery
    data class PasswordResetRequested(val email: String) : AuditEvent()
    data class PasswordResetConfirmed(val userId: String) : AuditEvent()
    data class SessionsRevoked(val userId: String, val reason: String) : AuditEvent()

    // Ringtone content management
    data class RingtoneCreated(val ringtoneId: String, val title: String, val adminId: String) : AuditEvent()
    data class RingtoneUpdated(val ringtoneId: String, val title: String, val adminId: String) : AuditEvent()
    data class RingtoneDeleted(val ringtoneId: String, val adminId: String) : AuditEvent()
    data class RingtoneActiveToggled(val ringtoneId: String, val isActive: Boolean, val adminId: String) : AuditEvent()
    data class RingtonePremiumToggled(val ringtoneId: String, val isPremium: Boolean, val adminId: String) : AuditEvent()

    // Media uploads
    data class MediaUploaded(val kind: String, val fileName: String, val sizeBytes: Long, val adminId: String) : AuditEvent()
}
