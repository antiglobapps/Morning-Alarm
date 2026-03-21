package com.morningalarm.desktopadmin.data

import com.morningalarm.api.ApiHeaders
import com.morningalarm.api.admin.ringtone.AdminRingtoneRoutes
import com.morningalarm.api.admin.upload.AdminUploadRoutes
import com.morningalarm.api.auth.AuthRoutes
import com.morningalarm.dto.ApiError
import com.morningalarm.dto.admin.ringtone.AdminRingtoneClientListPreviewResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtonePreviewResponseDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtoneActiveResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneResponseDto
import com.morningalarm.dto.auth.AdminLoginRequestDto
import com.morningalarm.dto.auth.AdminLoginResponseDto
import com.morningalarm.dto.auth.AuthSessionDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.upload.UploadAudioResponseDto
import com.morningalarm.dto.upload.UploadImageResponseDto
import com.morningalarm.dto.upload.UploadedMediaDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File

class SessionExpiredException : RuntimeException("Admin session expired")

class ApiClientException(
    override val message: String,
    val statusCode: Int? = null,
) : RuntimeException(message)

class AdminApiClient(
    baseUrl: String,
) {
    private val normalizedBaseUrl = baseUrl.trimEnd('/')
    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = false
    }
    private val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun adminLogin(
        email: String,
        password: String,
        adminSecret: String,
    ): AuthSessionDto {
        val response = client.post(url(AuthRoutes.ADMIN_LOGIN)) {
            contentType(ContentType.Application.Json)
            setBody(
                AdminLoginRequestDto(
                    email = email.trim(),
                    password = password,
                    adminSecret = adminSecret,
                ),
            )
        }
        return handleResponse<AdminLoginResponseDto>(response).session
    }

    suspend fun listRingtones(token: String, adminSecret: String): List<AdminRingtoneListItemDto> {
        val response = client.get(url(AdminRingtoneRoutes.LIST)) {
            adminAuth(token, adminSecret)
        }
        return handleResponse<AdminRingtoneListResponseDto>(response).items
    }

    suspend fun getRingtoneDetail(
        token: String,
        adminSecret: String,
        ringtoneId: String,
    ): AdminRingtoneDetailDto {
        val response = client.get(url(AdminRingtoneRoutes.DETAIL.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
        }
        return handleResponse<AdminRingtoneDetailResponseDto>(response).ringtone
    }

    suspend fun createRingtone(
        token: String,
        adminSecret: String,
        request: CreateAdminRingtoneRequestDto,
    ): AdminRingtoneDetailDto {
        val response = client.post(url(AdminRingtoneRoutes.CREATE)) {
            adminAuth(token, adminSecret)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handleResponse<CreateAdminRingtoneResponseDto>(response).ringtone
    }

    suspend fun updateRingtone(
        token: String,
        adminSecret: String,
        ringtoneId: String,
        request: UpdateAdminRingtoneRequestDto,
    ): AdminRingtoneDetailDto {
        val response = client.put(url(AdminRingtoneRoutes.UPDATE.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handleResponse<UpdateAdminRingtoneResponseDto>(response).ringtone
    }

    suspend fun deleteRingtone(token: String, adminSecret: String, ringtoneId: String) {
        val response = client.delete(url(AdminRingtoneRoutes.DELETE.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
        }
        handleUnitResponse(response)
    }

    suspend fun toggleActive(token: String, adminSecret: String, ringtoneId: String): ToggleRingtoneActiveResponseDto {
        val response = client.post(url(AdminRingtoneRoutes.TOGGLE_ACTIVE.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
        }
        return handleResponse(response)
    }

    suspend fun togglePremium(token: String, adminSecret: String, ringtoneId: String): ToggleRingtonePremiumResponseDto {
        val response = client.post(url(AdminRingtoneRoutes.TOGGLE_PREMIUM.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
        }
        return handleResponse(response)
    }

    suspend fun getPreview(token: String, adminSecret: String, ringtoneId: String): RingtoneListItemDto {
        val response = client.get(url(AdminRingtoneRoutes.PREVIEW.withRingtoneId(ringtoneId))) {
            adminAuth(token, adminSecret)
        }
        return handleResponse<AdminRingtonePreviewResponseDto>(response).preview
    }

    suspend fun getClientPreview(token: String, adminSecret: String): List<RingtoneListItemDto> {
        val response = client.get(url(AdminRingtoneRoutes.CLIENT_LIST_PREVIEW)) {
            adminAuth(token, adminSecret)
        }
        return handleResponse<AdminRingtoneClientListPreviewResponseDto>(response).items
    }

    suspend fun uploadImage(token: String, adminSecret: String, file: File): UploadedMediaDto {
        val response = client.post(url(AdminUploadRoutes.IMAGE)) {
            adminAuth(token, adminSecret)
            setBody(fileMultipart(file, "image/*"))
        }
        return handleResponse<UploadImageResponseDto>(response).media
    }

    suspend fun uploadAudio(token: String, adminSecret: String, file: File): UploadedMediaDto {
        val response = client.post(url(AdminUploadRoutes.AUDIO)) {
            adminAuth(token, adminSecret)
            setBody(fileMultipart(file, "audio/*"))
        }
        return handleResponse<UploadAudioResponseDto>(response).media
    }

    fun close() {
        client.close()
    }

    private fun url(path: String): String = "$normalizedBaseUrl$path"

    private fun String.withRingtoneId(ringtoneId: String): String = replace("{ringtoneId}", ringtoneId)

    private fun io.ktor.client.request.HttpRequestBuilder.adminAuth(token: String, adminSecret: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(ApiHeaders.ADMIN_SECRET, adminSecret)
    }

    private fun fileMultipart(file: File, fallbackContentType: String): MultiPartFormDataContent {
        val contentType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            else -> fallbackContentType
        }
        return MultiPartFormDataContent(
            formData {
                append(
                    key = "file",
                    value = file.readBytes(),
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, contentType)
                        append(HttpHeaders.ContentDisposition, """form-data; name="file"; filename="${file.name}"""")
                    },
                )
            },
        )
    }

    private suspend inline fun <reified T> handleResponse(response: io.ktor.client.statement.HttpResponse): T {
        return when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> response.body()
            HttpStatusCode.Unauthorized -> throw SessionExpiredException()
            else -> throw response.toApiClientException(json)
        }
    }

    private suspend fun handleUnitResponse(response: io.ktor.client.statement.HttpResponse) {
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.NoContent -> Unit
            HttpStatusCode.Unauthorized -> throw SessionExpiredException()
            else -> throw response.toApiClientException(json)
        }
    }
}

private suspend fun io.ktor.client.statement.HttpResponse.toApiClientException(json: Json): ApiClientException {
    val text = body<String>()
    val error = runCatching { json.decodeFromString<ApiError>(text) }.getOrNull()
    val message = error?.message ?: "Unexpected server response: ${status.value}"
    return ApiClientException(message = message, statusCode = status.value)
}
