package com.morningalarm.server.shared.audit

interface AuditLogger {
    fun log(event: AuditEvent)
}
