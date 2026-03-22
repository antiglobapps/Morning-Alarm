package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.api.ringtone.RingtoneRoutes
import com.morningalarm.dto.ringtone.RingtoneDetailResponseDto
import com.morningalarm.dto.ringtone.RingtoneListResponseDto
import com.morningalarm.dto.ringtone.ToggleRingtoneLikeResponseDto
import com.morningalarm.server.modules.ringtone.application.RingtoneService
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.shared.auth.currentAuthPrincipal
import com.morningalarm.server.shared.errors.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.configureRingtoneRoutes(ringtoneService: RingtoneService) {
    get(RingtoneRoutes.LIST) {
        val principal = call.currentAuthPrincipal()
        val filter = when (call.request.queryParameters["filter"]?.lowercase()) {
            null, "all" -> RingtoneListFilter.ALL
            "my" -> RingtoneListFilter.MY
            "system" -> RingtoneListFilter.SYSTEM
            else -> throw ValidationException("Invalid filter. Allowed: all, my, system")
        }
        val items = ringtoneService.list(principal.userId, filter).map { it.toDto(principal.userId) }
        call.respond(HttpStatusCode.OK, RingtoneListResponseDto(items))
    }

    get(RingtoneRoutes.DETAIL) {
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.detail(principal.userId, call.parameters["ringtoneId"].orEmpty())
            .toDto(principal.userId)
        call.respond(HttpStatusCode.OK, RingtoneDetailResponseDto(ringtone))
    }

    post(RingtoneRoutes.TOGGLE_LIKE) {
        val principal = call.currentAuthPrincipal()
        val result = ringtoneService.toggleLike(principal.userId, call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, ToggleRingtoneLikeResponseDto(result.ringtoneId, result.isLikedByUser, result.likesCount))
    }
}
