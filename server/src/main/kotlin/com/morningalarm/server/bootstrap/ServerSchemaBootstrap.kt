package com.morningalarm.server.bootstrap

import com.morningalarm.server.shared.persistence.SchemaBootstrap

class ServerSchemaBootstrap(
    private val steps: List<SchemaBootstrap>,
) {
    fun ensureCreated() {
        steps.forEach { step ->
            step.ensureCreated()
        }
    }
}
