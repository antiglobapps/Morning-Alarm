package com.morningalarm.server.shared.audit

/** Discards all audit events. Used in tests and dev setups where audit output is unwanted. */
class NoOpAuditLogger : AuditLogger {
    override fun log(event: AuditEvent) = Unit
}
