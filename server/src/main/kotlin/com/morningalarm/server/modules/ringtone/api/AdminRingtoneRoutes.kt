package com.morningalarm.server.modules.ringtone.api

import com.morningalarm.api.admin.ringtone.AdminRingtoneRoutes
import com.morningalarm.dto.admin.ringtone.AdminRingtoneClientListPreviewResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtonePreviewResponseDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.DeleteAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtoneActiveResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneResponseDto
import com.morningalarm.server.modules.ringtone.application.RingtoneService
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
            AdminRingtoneClientListPreviewResponseDto(ringtoneService.list(principal.userId).map { it.toDto() }),
        )
    }

    get(AdminRingtoneRoutes.DETAIL) {
        call.requireAdmin(adminAccessSecret)
        val ringtone = ringtoneService.detailForAdmin(call.parameters["ringtoneId"].orEmpty()).toAdminDetailDto()
        call.respond(HttpStatusCode.OK, AdminRingtoneDetailResponseDto(ringtone))
    }

    get(AdminRingtoneRoutes.PREVIEW) {
        call.requireAdmin(adminAccessSecret)
        val ringtone = ringtoneService.detailForAdmin(call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, AdminRingtonePreviewResponseDto(ringtone.ringtone.id, ringtone.toDto()))
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
            isActive = request.isActive,
            isPremium = request.isPremium,
        ).toAdminDetailDto()
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
            isActive = request.isActive,
            isPremium = request.isPremium,
        ).toAdminDetailDto()
        call.respond(HttpStatusCode.OK, UpdateAdminRingtoneResponseDto(ringtone))
    }

    delete(AdminRingtoneRoutes.DELETE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtoneId = call.parameters["ringtoneId"].orEmpty()
        ringtoneService.delete(ringtoneId, principal.userId)
        call.respond(HttpStatusCode.OK, DeleteAdminRingtoneResponseDto(ringtoneId, true))
    }

    post(AdminRingtoneRoutes.TOGGLE_ACTIVE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.toggleActive(principal.userId, call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, ToggleRingtoneActiveResponseDto(ringtone.ringtone.id, ringtone.ringtone.isActive))
    }

    post(AdminRingtoneRoutes.TOGGLE_PREMIUM) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val ringtone = ringtoneService.togglePremium(principal.userId, call.parameters["ringtoneId"].orEmpty())
        call.respond(HttpStatusCode.OK, ToggleRingtonePremiumResponseDto(ringtone.ringtone.id, ringtone.ringtone.isPremium))
    }
}
