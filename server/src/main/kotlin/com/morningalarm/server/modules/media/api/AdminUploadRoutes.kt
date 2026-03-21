package com.morningalarm.server.modules.media.api

import com.morningalarm.api.admin.upload.AdminUploadRoutes
import com.morningalarm.server.modules.media.application.AdminMediaService
import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.shared.auth.currentAuthPrincipal
import com.morningalarm.server.shared.auth.requireAdmin
import com.morningalarm.server.shared.errors.NotFoundException
import com.morningalarm.server.shared.errors.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.toInputStream

fun Route.configureAdminUploadRoutes(adminMediaService: AdminMediaService, adminAccessSecret: String? = null) {
    post(AdminUploadRoutes.IMAGE) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val upload = call.receiveSingleFileUpload()
        call.respond(HttpStatusCode.Created, adminMediaService.uploadImage(upload, principal.userId).toImageUploadResponseDto())
    }

    post(AdminUploadRoutes.AUDIO) {
        call.requireAdmin(adminAccessSecret)
        val principal = call.currentAuthPrincipal()
        val upload = call.receiveSingleFileUpload()
        call.respond(HttpStatusCode.Created, adminMediaService.uploadAudio(upload, principal.userId).toAudioUploadResponseDto())
    }
}

fun Route.configureMediaFileRoutes(adminMediaService: AdminMediaService) {
    route("/media") {
        get("{kind}/{fileName}") {
            val kind = when (call.parameters["kind"]?.lowercase()) {
                "image", "images" -> MediaKind.IMAGE
                "audio" -> MediaKind.AUDIO
                else -> throw NotFoundException("Media kind not found")
            }
            val file = adminMediaService.resolveMedia(kind, call.parameters["fileName"].orEmpty())
                ?: throw NotFoundException("Media file not found")
            call.respondFile(file)
        }
    }
}

private suspend fun ApplicationCall.receiveSingleFileUpload(): MediaUpload {
    val multipart = receiveMultipart()
    var upload: MediaUpload? = null

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                if (upload != null) {
                    throw ValidationException("Only one file part is allowed")
                }
                upload = MediaUpload(
                    fileName = part.originalFileName?.trim().orEmpty(),
                    contentType = part.contentType?.toString().orEmpty(),
                    bytes = part.provider().toInputStream().readBytes(),
                )
            }
            else -> Unit
        }
        part.dispose()
    }

    return upload ?: throw ValidationException("Multipart request must contain a file")
}
