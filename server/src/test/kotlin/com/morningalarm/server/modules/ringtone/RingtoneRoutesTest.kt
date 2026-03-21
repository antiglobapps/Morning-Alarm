package com.morningalarm.server.modules.ringtone

import com.morningalarm.api.ApiHeaders
import com.morningalarm.api.admin.ringtone.AdminRingtoneRoutes
import com.morningalarm.api.ringtone.RingtoneRoutes
import com.morningalarm.dto.ApiError
import com.morningalarm.dto.admin.ringtone.AdminRingtoneClientListPreviewResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListResponseDto
import com.morningalarm.dto.admin.ringtone.AdminRingtonePreviewResponseDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtoneActiveResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneResponseDto
import com.morningalarm.dto.ringtone.RingtoneDetailResponseDto
import com.morningalarm.dto.ringtone.RingtoneListResponseDto
import com.morningalarm.dto.ringtone.ToggleRingtoneLikeResponseDto
import com.morningalarm.server.bootstrap.AppConfig
import com.morningalarm.server.bootstrap.ModuleDependencies
import com.morningalarm.server.testConfig
import com.morningalarm.server.testApp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RingtoneRoutesTest {
    @Test
    fun `unauthorized request to client and admin ringtone lists returns 401`() = testApp { _, client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get(RingtoneRoutes.LIST).status)
        assertEquals(HttpStatusCode.Unauthorized, client.get(AdminRingtoneRoutes.LIST).status)
    }

    @Test
    fun `admin can create update toggle preview and delete ringtone through admin api`() =
        testApp { dependencies, client ->
            val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

            val createResponse = client.post(AdminRingtoneRoutes.CREATE) {
                auth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(
                    CreateAdminRingtoneRequestDto(
                        title = "Sunrise Bells",
                        description = "Soft bright bells for gentle wakeup.",
                        imageUrl = "https://cdn.example.com/ringtones/sunrise.jpg",
                        audioUrl = "https://cdn.example.com/ringtones/sunrise.mp3",
                        durationSeconds = 42,
                        isActive = false,
                        isPremium = false,
                    ),
                )
            }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val created = createResponse.body<CreateAdminRingtoneResponseDto>().ringtone
            assertEquals("Sunrise Bells", created.title)
            assertFalse(created.isActive)
            assertEquals("https://cdn.example.com/ringtones/sunrise.mp3", created.audioUrl)

            val listResponse = client.get(AdminRingtoneRoutes.LIST) {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, listResponse.status)
            assertEquals(1, listResponse.body<AdminRingtoneListResponseDto>().items.size)

            val updateResponse = client.put("${AdminRingtoneRoutes.BASE}/${created.id}") {
                auth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateAdminRingtoneRequestDto(
                        title = "Sunrise Bells Updated",
                        description = "Updated ringtone description.",
                        imageUrl = "https://cdn.example.com/ringtones/sunrise-updated.jpg",
                        audioUrl = "https://cdn.example.com/ringtones/sunrise-updated.mp3",
                        durationSeconds = 55,
                        isActive = true,
                        isPremium = true,
                    ),
                )
            }

            assertEquals(HttpStatusCode.OK, updateResponse.status)
            val updated = updateResponse.body<UpdateAdminRingtoneResponseDto>().ringtone
            assertEquals("Sunrise Bells Updated", updated.title)
            assertTrue(updated.isActive)
            assertTrue(updated.isPremium)
            assertTrue(updated.updatedAtEpochSeconds >= created.updatedAtEpochSeconds)

            val detailResponse = client.get("${AdminRingtoneRoutes.BASE}/${created.id}") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, detailResponse.status)
            val detail = detailResponse.body<AdminRingtoneDetailResponseDto>().ringtone
            assertEquals(updated.id, detail.id)
            assertTrue(detail.preview.isPremium)

            val previewResponse = client.get("${AdminRingtoneRoutes.BASE}/${created.id}/preview") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, previewResponse.status)
            val preview = previewResponse.body<AdminRingtonePreviewResponseDto>()
            assertEquals(created.id, preview.ringtoneId)
            assertEquals(updated.title, preview.preview.title)

            val togglePremiumResponse = client.post("${AdminRingtoneRoutes.BASE}/${created.id}/premium-toggle") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, togglePremiumResponse.status)
            assertFalse(togglePremiumResponse.body<ToggleRingtonePremiumResponseDto>().isPremium)

            val toggleActiveResponse = client.post("${AdminRingtoneRoutes.BASE}/${created.id}/active-toggle") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, toggleActiveResponse.status)
            assertFalse(toggleActiveResponse.body<ToggleRingtoneActiveResponseDto>().isActive)

            val deleteResponse = client.delete("${AdminRingtoneRoutes.BASE}/${created.id}") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            val missingDetail = client.get("${AdminRingtoneRoutes.BASE}/${created.id}") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.NotFound, missingDetail.status)
        }

    @Test
    fun `non admin cannot access admin ringtone endpoints`() = testApp { dependencies, client ->
        val userToken = registerUser(dependencies, "user@example.com").bearerToken

        val listResponse = client.get(AdminRingtoneRoutes.LIST) {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.Forbidden, listResponse.status)

        val createResponse = client.post(AdminRingtoneRoutes.CREATE) {
            auth(userToken)
            contentType(ContentType.Application.Json)
            setBody(
                CreateAdminRingtoneRequestDto(
                    title = "Rain Touch",
                    description = "Rain ambience ringtone.",
                    imageUrl = "https://cdn.example.com/ringtones/rain.jpg",
                    audioUrl = "https://cdn.example.com/ringtones/rain.mp3",
                    durationSeconds = 30,
                    isActive = true,
                    isPremium = false,
                ),
            )
        }

        assertEquals(HttpStatusCode.Forbidden, createResponse.status)
        assertEquals("forbidden", createResponse.body<ApiError>().code)
    }

    @Test
    fun `admin api requires admin secret header when hardened access is enabled`() = testApp(
        configOverride = { copy(adminAccessSecret = "top-secret") },
    ) { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val missingSecret = client.get(AdminRingtoneRoutes.LIST) {
            auth(adminToken)
        }
        assertEquals(HttpStatusCode.Forbidden, missingSecret.status)

        val success = client.get(AdminRingtoneRoutes.LIST) {
            auth(adminToken, "top-secret")
        }
        assertEquals(HttpStatusCode.OK, success.status)
    }

    @Test
    fun `client api returns only active ringtones and includes like state`() = testApp { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken
        val userToken = registerUser(dependencies, "user@example.com").bearerToken

        val activeRingtone = createAdminRingtone(client, adminToken, "Ocean Morning", isActive = true, isPremium = true)
        createAdminRingtone(client, adminToken, "Hidden Draft", isActive = false, isPremium = false)

        val initialList = client.get(RingtoneRoutes.LIST) {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, initialList.status)
        val initialItem = initialList.body<RingtoneListResponseDto>().items.single()
        assertEquals(activeRingtone.id, initialItem.id)
        assertFalse(initialItem.isLikedByUser)
        assertEquals(0, initialItem.likesCount)
        assertTrue(initialItem.isPremium)
        assertEquals("https://cdn.example.com/ringtones/ocean-morning.mp3", initialItem.audioUrl)

        val toggleOn = client.post("${RingtoneRoutes.BASE}/${activeRingtone.id}/like-toggle") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, toggleOn.status)
        val toggleOnBody = toggleOn.body<ToggleRingtoneLikeResponseDto>()
        assertTrue(toggleOnBody.isLikedByUser)
        assertEquals(1, toggleOnBody.likesCount)

        val detail = client.get("${RingtoneRoutes.BASE}/${activeRingtone.id}") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, detail.status)
        val detailBody = detail.body<RingtoneDetailResponseDto>().ringtone
        assertTrue(detailBody.isLikedByUser)
        assertEquals(1, detailBody.likesCount)
        assertTrue(detailBody.isPremium)

        val previewList = client.get(AdminRingtoneRoutes.CLIENT_LIST_PREVIEW) {
            auth(adminToken)
        }
        assertEquals(HttpStatusCode.OK, previewList.status)
        assertEquals(1, previewList.body<AdminRingtoneClientListPreviewResponseDto>().items.size)

        val toggleOff = client.post("${RingtoneRoutes.BASE}/${activeRingtone.id}/like-toggle") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, toggleOff.status)
        val toggleOffBody = toggleOff.body<ToggleRingtoneLikeResponseDto>()
        assertFalse(toggleOffBody.isLikedByUser)
        assertEquals(0, toggleOffBody.likesCount)
    }

    @Test
    fun `inactive ringtone is hidden from client detail and like endpoints`() = testApp { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken
        val userToken = registerUser(dependencies, "user@example.com").bearerToken
        val inactiveRingtone = createAdminRingtone(client, adminToken, "Night Draft", isActive = false, isPremium = false)

        val detailResponse = client.get("${RingtoneRoutes.BASE}/${inactiveRingtone.id}") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.NotFound, detailResponse.status)

        val likeResponse = client.post("${RingtoneRoutes.BASE}/${inactiveRingtone.id}/like-toggle") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.NotFound, likeResponse.status)
    }

    @Test
    fun `admin create validates absolute media urls`() = testApp { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val response = client.post(AdminRingtoneRoutes.CREATE) {
            auth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(
                CreateAdminRingtoneRequestDto(
                    title = "Broken",
                    description = "Invalid URLs",
                    imageUrl = "/relative/image.jpg",
                    audioUrl = "/relative/audio.mp3",
                    durationSeconds = 12,
                    isActive = true,
                    isPremium = false,
                ),
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("validation_error", response.body<ApiError>().code)
    }

    private suspend fun createAdminRingtone(
        client: HttpClient,
        adminToken: String,
        title: String,
        isActive: Boolean,
        isPremium: Boolean,
    ): com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto {
        val slug = title.lowercase().replace(' ', '-')
        return client.post(AdminRingtoneRoutes.CREATE) {
            auth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(
                CreateAdminRingtoneRequestDto(
                    title = title,
                    description = "$title description",
                    imageUrl = "https://cdn.example.com/ringtones/$slug.jpg",
                    audioUrl = "https://cdn.example.com/ringtones/$slug.mp3",
                    durationSeconds = 60,
                    isActive = isActive,
                    isPremium = isPremium,
                ),
            )
        }.body<CreateAdminRingtoneResponseDto>().ringtone
    }

    private fun registerUser(dependencies: ModuleDependencies, email: String) = dependencies.authService.registerWithEmail(
        email = email,
        password = "very-secret",
        displayName = email.substringBefore('@'),
    )

    private fun io.ktor.client.request.HttpRequestBuilder.auth(token: String, adminSecret: String? = null) {
        header(HttpHeaders.Authorization, "Bearer $token")
        if (adminSecret != null) {
            header(ApiHeaders.ADMIN_SECRET, adminSecret)
        }
    }

}
