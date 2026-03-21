package com.morningalarm.server.bootstrap

import com.morningalarm.server.modules.auth.api.configureAuthRoutes
import com.morningalarm.server.modules.media.api.configureAdminUploadRoutes
import com.morningalarm.server.modules.media.api.configureMediaFileRoutes
import com.morningalarm.server.modules.ringtone.api.configureAdminRingtoneRoutes
import com.morningalarm.server.modules.ringtone.api.configureRingtoneRoutes
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

fun Route.configureRouting(
    dependencies: ModuleDependencies,
) {
    configureAuthRoutes(dependencies.authService, dependencies.adminAccessSecret)
    configureMediaFileRoutes(dependencies.adminMediaService)
    authenticate("auth-bearer") {
        configureRingtoneRoutes(dependencies.ringtoneService)
        configureAdminRingtoneRoutes(dependencies.ringtoneService, dependencies.adminAccessSecret)
        configureAdminUploadRoutes(dependencies.adminMediaService, dependencies.adminAccessSecret)
    }
}
