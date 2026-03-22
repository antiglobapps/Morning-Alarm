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
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityRequestDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityResponseDto
import com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneResponseDto
import com.morningalarm.dto.ringtone.RingtoneDetailResponseDto
import com.morningalarm.dto.ringtone.RingtoneListResponseDto
import com.morningalarm.dto.ringtone.RingtoneSourceDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
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
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RingtoneRoutesTest {
    @Test
    fun `unauthorized request to client and admin ringtone lists returns 401`() = testApp { _, client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get(RingtoneRoutes.LIST).status)
        assertEquals(HttpStatusCode.Unauthorized, client.get(AdminRingtoneRoutes.LIST).status)
    }

    @Test
    fun `admin can create update set visibility preview and delete ringtone through admin api`() =
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
                        visibility = RingtoneVisibilityDto.INACTIVE,
                        isPremium = false,
                    ),
                )
            }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val created = createResponse.body<CreateAdminRingtoneResponseDto>().ringtone
            assertEquals("Sunrise Bells", created.title)
            assertEquals(RingtoneVisibilityDto.INACTIVE, created.visibility)
            assertNull(created.createdByUserId)
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
                        visibility = RingtoneVisibilityDto.PUBLIC,
                        isPremium = true,
                    ),
                )
            }

            assertEquals(HttpStatusCode.OK, updateResponse.status)
            val updated = updateResponse.body<UpdateAdminRingtoneResponseDto>().ringtone
            assertEquals("Sunrise Bells Updated", updated.title)
            assertEquals(RingtoneVisibilityDto.PUBLIC, updated.visibility)
            assertTrue(updated.isPremium)
            assertTrue(updated.updatedAtEpochSeconds >= created.updatedAtEpochSeconds)

            val detailResponse = client.get("${AdminRingtoneRoutes.BASE}/${created.id}") {
                auth(adminToken)
            }
            assertEquals(HttpStatusCode.OK, detailResponse.status)
            val detail = detailResponse.body<AdminRingtoneDetailResponseDto>().ringtone
            assertEquals(updated.id, detail.id)
            assertTrue(detail.preview.isPremium)
            assertEquals(RingtoneSourceDto.SYSTEM, detail.preview.source)

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

            val setVisibilityResponse = client.put("${AdminRingtoneRoutes.BASE}/${created.id}/visibility") {
                auth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(SetRingtoneVisibilityRequestDto(RingtoneVisibilityDto.INACTIVE))
            }
            assertEquals(HttpStatusCode.OK, setVisibilityResponse.status)
            assertEquals(RingtoneVisibilityDto.INACTIVE, setVisibilityResponse.body<SetRingtoneVisibilityResponseDto>().visibility)

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
                    visibility = RingtoneVisibilityDto.PUBLIC,
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
    fun `client api returns only public ringtones and includes like state and source`() = testApp { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken
        val userToken = registerUser(dependencies, "user@example.com").bearerToken

        val publicRingtone = createAdminRingtone(client, adminToken, "Ocean Morning", visibility = RingtoneVisibilityDto.PUBLIC, isPremium = true)
        createAdminRingtone(client, adminToken, "Hidden Draft", visibility = RingtoneVisibilityDto.INACTIVE, isPremium = false)

        val initialList = client.get(RingtoneRoutes.LIST) {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, initialList.status)
        val initialItem = initialList.body<RingtoneListResponseDto>().items.single()
        assertEquals(publicRingtone.id, initialItem.id)
        assertFalse(initialItem.isLikedByUser)
        assertEquals(0, initialItem.likesCount)
        assertTrue(initialItem.isPremium)
        assertEquals(RingtoneSourceDto.SYSTEM, initialItem.source)
        assertFalse(initialItem.isOwnedByCurrentUser)
        assertEquals("https://cdn.example.com/ringtones/ocean-morning.mp3", initialItem.audioUrl)

        val toggleOn = client.post("${RingtoneRoutes.BASE}/${publicRingtone.id}/like-toggle") {
            auth(userToken)
        }
        assertEquals(HttpStatusCode.OK, toggleOn.status)
        val toggleOnBody = toggleOn.body<ToggleRingtoneLikeResponseDto>()
        assertTrue(toggleOnBody.isLikedByUser)
        assertEquals(1, toggleOnBody.likesCount)

        val detail = client.get("${RingtoneRoutes.BASE}/${publicRingtone.id}") {
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

        val toggleOff = client.post("${RingtoneRoutes.BASE}/${publicRingtone.id}/like-toggle") {
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
        val inactiveRingtone = createAdminRingtone(client, adminToken, "Night Draft", visibility = RingtoneVisibilityDto.INACTIVE, isPremium = false)

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
                    visibility = RingtoneVisibilityDto.PUBLIC,
                    isPremium = false,
                ),
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("validation_error", response.body<ApiError>().code)
    }

    @Test
    fun `client list filter returns correct subsets`() = testApp { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken
        val userSession = registerUser(dependencies, "user@example.com")
        val otherUserSession = registerUser(dependencies, "other@example.com")

        createAdminRingtone(client, adminToken, "System Public", visibility = RingtoneVisibilityDto.PUBLIC, isPremium = false)
        createAdminRingtone(client, adminToken, "System Inactive", visibility = RingtoneVisibilityDto.INACTIVE, isPremium = false)
        val privateUserRingtoneId = createUserRingtone(
            dataSource = dependencies.dataSource,
            ownerUserId = userSession.userId,
            title = "Private User Draft",
            visibility = RingtoneVisibilityDto.PRIVATE,
            isPremium = false,
        )

        val allList = client.get("${RingtoneRoutes.LIST}?filter=all") {
            auth(userSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, allList.status)
        val allItems = allList.body<RingtoneListResponseDto>().items
        assertEquals(2, allItems.size)
        assertTrue(allItems.any { it.id == privateUserRingtoneId && it.source == RingtoneSourceDto.USER && it.isOwnedByCurrentUser })
        assertTrue(allItems.any { it.source == RingtoneSourceDto.SYSTEM && !it.isOwnedByCurrentUser })

        val systemList = client.get("${RingtoneRoutes.LIST}?filter=system") {
            auth(userSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, systemList.status)
        val systemItems = systemList.body<RingtoneListResponseDto>().items
        assertEquals(1, systemItems.size)
        assertEquals(RingtoneSourceDto.SYSTEM, systemItems.single().source)
        assertFalse(systemItems.single().isOwnedByCurrentUser)

        val myList = client.get("${RingtoneRoutes.LIST}?filter=my") {
            auth(userSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, myList.status)
        val myItems = myList.body<RingtoneListResponseDto>().items
        assertEquals(1, myItems.size)
        assertEquals(privateUserRingtoneId, myItems.single().id)
        assertEquals(RingtoneSourceDto.USER, myItems.single().source)
        assertTrue(myItems.single().isOwnedByCurrentUser)

        val otherUserAllList = client.get("${RingtoneRoutes.LIST}?filter=all") {
            auth(otherUserSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, otherUserAllList.status)
        val otherUserAllItems = otherUserAllList.body<RingtoneListResponseDto>().items
        assertEquals(1, otherUserAllItems.size)
        assertFalse(otherUserAllItems.any { it.id == privateUserRingtoneId })

        val otherUserMyList = client.get("${RingtoneRoutes.LIST}?filter=my") {
            auth(otherUserSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, otherUserMyList.status)
        assertEquals(0, otherUserMyList.body<RingtoneListResponseDto>().items.size)

        val otherUserSystemList = client.get("${RingtoneRoutes.LIST}?filter=system") {
            auth(otherUserSession.bearerToken)
        }
        assertEquals(HttpStatusCode.OK, otherUserSystemList.status)
        assertEquals(1, otherUserSystemList.body<RingtoneListResponseDto>().items.size)

        val invalidFilter = client.get("${RingtoneRoutes.LIST}?filter=invalid") {
            auth(userSession.bearerToken)
        }
        assertEquals(HttpStatusCode.BadRequest, invalidFilter.status)
    }

    private fun createUserRingtone(
        dataSource: DataSource,
        ownerUserId: String,
        title: String,
        visibility: RingtoneVisibilityDto,
        isPremium: Boolean,
    ): String {
        val slug = title.lowercase().replace(' ', '-')
        val ringtoneId = "user-$slug-$ownerUserId"
        val nowEpochSeconds = 1_700_000_000L
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO ringtones (
                    id, title, image_url, audio_url, duration_seconds, description,
                    visibility, is_premium, created_at_epoch_seconds, updated_at_epoch_seconds,
                    created_by_admin_id, updated_by_admin_id, created_by_user_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtoneId)
                statement.setString(2, title)
                statement.setString(3, "https://cdn.example.com/ringtones/$slug.jpg")
                statement.setString(4, "https://cdn.example.com/ringtones/$slug.mp3")
                statement.setInt(5, 45)
                statement.setString(6, "$title description")
                statement.setString(7, visibility.name)
                statement.setBoolean(8, isPremium)
                statement.setLong(9, nowEpochSeconds)
                statement.setLong(10, nowEpochSeconds)
                statement.setNull(11, java.sql.Types.VARCHAR)
                statement.setNull(12, java.sql.Types.VARCHAR)
                statement.setString(13, ownerUserId)
                statement.executeUpdate()
            }
        }
        return ringtoneId
    }

    private suspend fun createAdminRingtone(
        client: HttpClient,
        adminToken: String,
        title: String,
        visibility: RingtoneVisibilityDto,
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
                    visibility = visibility,
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
