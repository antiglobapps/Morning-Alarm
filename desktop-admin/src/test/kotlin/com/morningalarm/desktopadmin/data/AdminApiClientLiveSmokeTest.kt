package com.morningalarm.desktopadmin.data

import com.morningalarm.api.auth.DevAdminDefaults
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminApiClientLiveSmokeTest {
    @Test
    fun `desktop admin client can manage ringtone against live dev server`() = runBlocking {
        // This smoke test is enabled only in CI jobs that start a local dev server.
        if (System.getenv("DESKTOP_ADMIN_SMOKE_ENABLED") != "true") {
            return@runBlocking
        }

        val baseUrl = System.getenv("DESKTOP_ADMIN_SMOKE_BASE_URL") ?: "http://127.0.0.1:8080"
        val email = System.getenv("DESKTOP_ADMIN_SMOKE_EMAIL") ?: DevAdminDefaults.EMAIL
        val password = System.getenv("DESKTOP_ADMIN_SMOKE_PASSWORD") ?: DevAdminDefaults.PASSWORD
        val adminSecret = System.getenv("DESKTOP_ADMIN_SMOKE_ADMIN_SECRET") ?: DevAdminDefaults.ACCESS_SECRET

        val client = AdminApiClient(baseUrl)
        var createdRingtoneId: String? = null

        try {
            val session = client.adminLogin(
                email = email,
                password = password,
                adminSecret = adminSecret,
            )
            assertTrue(session.bearerToken.isNotBlank())

            val suffix = System.currentTimeMillis().toString()
            val created = client.createRingtone(
                token = session.bearerToken,
                adminSecret = adminSecret,
                request = CreateAdminRingtoneRequestDto(
                    title = "CI Smoke $suffix",
                    description = "Desktop admin smoke test ringtone",
                    imageUrl = "https://cdn.example.com/ringtones/$suffix.jpg",
                    audioUrl = "https://cdn.example.com/ringtones/$suffix.mp3",
                    durationSeconds = 30,
                    visibility = com.morningalarm.dto.ringtone.RingtoneVisibilityDto.PUBLIC,
                    isPremium = false,
                ),
            )
            createdRingtoneId = created.id

            val detail = client.getRingtoneDetail(
                token = session.bearerToken,
                adminSecret = adminSecret,
                ringtoneId = created.id,
            )
            assertEquals(created.id, detail.id)

            val list = client.listRingtones(
                token = session.bearerToken,
                adminSecret = adminSecret,
            )
            assertTrue(list.any { it.id == created.id })

            val clientPreview = client.getClientPreview(
                token = session.bearerToken,
                adminSecret = adminSecret,
            )
            assertTrue(clientPreview.any { it.id == created.id })
        } finally {
            val createdId = createdRingtoneId
            if (createdId != null) {
                runCatching {
                    val session = client.adminLogin(
                        email = email,
                        password = password,
                        adminSecret = adminSecret,
                    )
                    client.deleteRingtone(
                        token = session.bearerToken,
                        adminSecret = adminSecret,
                        ringtoneId = createdId,
                    )
                }
            }
            client.close()
        }
    }
}
