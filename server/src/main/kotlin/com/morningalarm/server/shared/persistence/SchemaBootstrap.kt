package com.morningalarm.server.shared.persistence

interface SchemaBootstrap {
    val name: String

    fun ensureCreated()
}
