package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.Clock
import java.time.Instant

class JavaClock : Clock {
    override fun epochSeconds(): Long = Instant.now().epochSecond
}
