package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.api.admin.ringtone.AdminRingtoneRoutes
import com.morningalarm.dto.admin.ringtone.AdminRingtoneClientListPreviewResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtonePreviewResponseDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.DeleteAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityRequestDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneResponseDto
import com.morningalarm.server.modules.ringtone.application.RingtoneService
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.shared.auth.currentAuthPrincipal
import com.morningalarm.server.shared.auth.requireAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureAdminRingtoneRoutes(ringtoneService: RingtoneService, adminAccessSecret: String? = null) {
    get(AdminRingtoneRoutes.LIST) {
        call.requireAdmin(adminAccessSecret)
        call.respond(
            HttpStatusCode.OK,
            AdminRingtoneListResponseDto(ringtoneService.listForAdmin().map { it.toAdminListItemDto() }),
        )
    }

    get(AdminRingtoneRoutes.CLIENT_LIST_PREVIEW) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        call.respond(
            HttpStatusCode.OK,
            AdminRingtoneClientListPreviewResponseDto(ringtoneService.list(principal.userId).map { it.toDto(principal.userId) }),
        )
    }

    get(AdminRingtoneRoutes.DETAIL) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.detailForAdmin(call.parameters["ringtoneId"].orEmpty())
            .toAdminDetailDto(principal.userId)
        call.respond(HttpStatusCode.OK, AdminRingtoneDetailResponseDto(ringtone))
    }

    get(AdminRingtoneRoutes.PREVIEW) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.detailForAdmin(call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, AdminRingtonePreviewResponseDto(ringtone.ringtone.id, ringtone.toDto(principal.userId)))
    }

    post(AdminRingtoneRoutes.CREATE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val request = call.receive<CreateAdminRingtoneRequestDto>()
        val ringtone = ringtoneService.create(
            adminUserId = principal.userId,
            title = request.title,
            imageUrl = request.imageUrl,
            audioUrl = request.audioUrl,
            durationSeconds = request.durationSeconds,
            description = request.description,
            visibility = RingtoneVisibility.valueOf(request.visibility.name),
            isPremium = request.isPremium,
        ).toAdminDetailDto(principal.userId)
        call.respond(HttpStatusCode.Created, CreateAdminRingtoneResponseDto(ringtone))
    }

    put(AdminRingtoneRoutes.UPDATE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val request = call.receive<UpdateAdminRingtoneRequestDto>()
        val ringtone = ringtoneService.update(
            adminUserId = principal.userId,
            ringtoneId = call.parameters["ringtoneId"].orEmpty(),
            title = request.title,
            imageUrl = request.imageUrl,
            audioUrl = request.audioUrl,
            durationSeconds = request.durationSeconds,
            description = request.description,
            visibility = RingtoneVisibility.valueOf(request.visibility.name),
            isPremium = request.isPremium,
        ).toAdminDetailDto(principal.userId)
        call.respond(HttpStatusCode.OK, UpdateAdminRingtoneResponseDto(ringtone))
    }

    delete(AdminRingtoneRoutes.DELETE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtoneId = call.parameters["ringtoneId"].orEmpty()
        ringtoneService.delete(ringtoneId, principal.userId)
        call.respond(HttpStatusCode.OK, DeleteAdminRingtoneResponseDto(ringtoneId, true))
    }

    put(AdminRingtoneRoutes.SET_VISIBILITY) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val request = call.receive<SetRingtoneVisibilityRequestDto>()
        val visibility = RingtoneVisibility.valueOf(request.visibility.name)
        val ringtone = ringtoneService.setVisibility(principal.userId, call.parameters["ringtoneId"].orEmpty(), visibility)
        call.respond(HttpStatusCode.OK, SetRingtoneVisibilityResponseDto(ringtone.ringtone.id, request.visibility))
    }

    post(AdminRingtoneRoutes.TOGGLE_PREMIUM) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.togglePremium(principal.userId, call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, ToggleRingtonePremiumResponseDto(ringtone.ringtone.id, ringtone.ringtone.isPremium))
    }
}
