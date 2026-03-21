package com.morningalarm.server.modules.media

import com.morningalarm.api.admin.upload.AdminUploadRoutes
import com.morningalarm.dto.ApiError
import com.morningalarm.dto.upload.MediaKindDto
import com.morningalarm.dto.upload.UploadAudioResponseDto
import com.morningalarm.dto.upload.UploadImageResponseDto
import com.morningalarm.server.bootstrap.AppConfig
import com.morningalarm.server.bootstrap.ModuleDependencies
import com.morningalarm.server.bootstrap.applicationModule
import com.morningalarm.server.bootstrap.createModuleDependencies
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminUploadRoutesTest {
    @Test
    fun `unauthorized upload requests return 401`() = testApplicationWithDependencies { _, client ->
        assertEquals(HttpStatusCode.Unauthorized, client.uploadImage("/tmp/test.jpg", "image/jpeg", byteArrayOf(1)).status)
        assertEquals(HttpStatusCode.Unauthorized, client.uploadAudio("/tmp/test.mp3", "audio/mpeg", byteArrayOf(1)).status)
    }

    @Test
    fun `non admin cannot upload media`() = testApplicationWithDependencies { dependencies, client ->
        val userToken = registerUser(dependencies, "user@example.com").bearerToken

        val response = client.uploadImage("cover.jpg", "image/jpeg", byteArrayOf(1, 2, 3), userToken)
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("forbidden", response.body<ApiError>().code)
    }

    @Test
    fun `admin can upload image and fetch it from media url`() = testApplicationWithDependencies { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val response = client.uploadImage("cover.jpg", "image/jpeg", byteArrayOf(10, 20, 30), adminToken)
        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.body<UploadImageResponseDto>()
        assertEquals(MediaKindDto.IMAGE, body.media.kind)
        assertTrue(body.media.url.startsWith("http://localhost:8080/media/image/"))

        val served = client.get(body.media.url.removePrefix("http://localhost:8080"))
        assertEquals(HttpStatusCode.OK, served.status)
        assertEquals(3, served.body<ByteArray>().size)
    }

    @Test
    fun `admin can upload audio`() = testApplicationWithDependencies { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val response = client.uploadAudio("tone.mp3", "audio/mpeg", byteArrayOf(1, 2, 3, 4), adminToken)
        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.body<UploadAudioResponseDto>()
        assertEquals(MediaKindDto.AUDIO, body.media.kind)
        assertTrue(body.media.url.startsWith("http://localhost:8080/media/audio/"))
        assertEquals(4L, body.media.sizeBytes)
    }

    @Test
    fun `audio upload returns detected duration for wav`() = testApplicationWithDependencies { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val response = client.uploadAudio(
            fileName = "tone.wav",
            contentType = "audio/wav",
            bytes = createWav(durationMillis = 1_000),
            token = adminToken,
        )

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(1, response.body<UploadAudioResponseDto>().media.durationSeconds)
    }

    @Test
    fun `audio upload returns detected duration for mp3`() = testApplicationWithDependencies { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val response = client.uploadAudio(
            fileName = "tone.mp3",
            contentType = "audio/mpeg",
            bytes = createMp3(frameCount = 76),
            token = adminToken,
        )

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(2, response.body<UploadAudioResponseDto>().media.durationSeconds)
    }

    @Test
    fun `admin upload validates media type and size`() = testApplicationWithDependencies { dependencies, client ->
        val adminToken = registerUser(dependencies, "admin@example.com").bearerToken

        val wrongType = client.uploadImage("cover.txt", "text/plain", byteArrayOf(1), adminToken)
        assertEquals(HttpStatusCode.BadRequest, wrongType.status)
        assertEquals("validation_error", wrongType.body<ApiError>().code)

        val tooLargeAudio = client.uploadAudio("huge.mp3", "audio/mpeg", ByteArray(9_000), adminToken)
        assertEquals(HttpStatusCode.BadRequest, tooLargeAudio.status)
        assertEquals("validation_error", tooLargeAudio.body<ApiError>().code)
    }

    private fun testApplicationWithDependencies(
        block: suspend io.ktor.server.testing.ApplicationTestBuilder.(ModuleDependencies, HttpClient) -> Unit,
    ) = testApplication {
        val config = testConfig(Files.createTempDirectory("morning-alarm-upload-test").toString())
        val dependencies = createModuleDependencies(config)
        application {
            applicationModule(config = config, dependencies = dependencies)
        }
        val client = createClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = false
                        explicitNulls = false
                    },
                )
            }
        }
        block(dependencies, client)
    }

    private fun registerUser(dependencies: ModuleDependencies, email: String) = dependencies.authService.registerWithEmail(
        email = email,
        password = "very-secret",
        displayName = email.substringBefore('@'),
    )

    private suspend fun HttpClient.uploadImage(
        fileName: String,
        contentType: String,
        bytes: ByteArray,
        token: String? = null,
    ) = post(AdminUploadRoutes.IMAGE) {
        if (token != null) auth(token)
        setBody(multipartBody(fileName, contentType, bytes))
    }

    private suspend fun HttpClient.uploadAudio(
        fileName: String,
        contentType: String,
        bytes: ByteArray,
        token: String? = null,
    ) = post(AdminUploadRoutes.AUDIO) {
        if (token != null) auth(token)
        setBody(multipartBody(fileName, contentType, bytes))
    }

    private fun multipartBody(fileName: String, contentType: String, bytes: ByteArray): MultiPartFormDataContent {
        return MultiPartFormDataContent(
            formData {
                append(
                    key = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, contentType)
                        append(HttpHeaders.ContentDisposition, """form-data; name="file"; filename="$fileName"""")
                    },
                )
            },
        )
    }

    private fun io.ktor.client.request.HttpRequestBuilder.auth(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    private fun testConfig(dataDir: String): AppConfig = AppConfig(
        devMode = true,
        host = "127.0.0.1",
        port = 8080,
        publicUrl = null,
        logPublicUrl = false,
        mediaStorageDir = "$dataDir/media",
        mediaPublicBaseUrl = "http://localhost:8080",
        mediaMaxImageBytes = 64,
        mediaMaxAudioBytes = 8_192,
        firebaseBucketName = null,
        firebaseCredentialsPath = null,
        databaseUrl = "jdbc:h2:file:$dataDir/upload-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        databaseUser = "sa",
        databasePassword = "",
        databaseDriver = "org.h2.Driver",
        databasePoolMaxSize = 4,
        jwtSecret = "test-secret",
        jwtIssuer = "test-issuer",
        jwtAudience = "test-audience",
        adminEmails = setOf("admin@example.com"),
        adminBootstrapSecret = null,
        adminAccessSecret = null,
        accessTokenTtlSeconds = 24 * 60 * 60L,
        refreshTokenTtlSeconds = 30 * 24 * 60 * 60L,
        passwordResetTokenTtlSeconds = 60 * 60L,
    )

    private fun createWav(durationMillis: Int): ByteArray {
        val sampleRate = 8_000
        val channels = 1
        val bitsPerSample = 8
        val bytesPerSample = bitsPerSample / 8
        val dataSize = sampleRate * durationMillis / 1_000 * channels * bytesPerSample
        val byteRate = sampleRate * channels * bytesPerSample
        val blockAlign = channels * bytesPerSample
        val totalSize = 44 + dataSize
        val bytes = ByteArray(totalSize)

        writeAscii(bytes, 0, "RIFF")
        writeIntLe(bytes, 4, totalSize - 8)
        writeAscii(bytes, 8, "WAVE")
        writeAscii(bytes, 12, "fmt ")
        writeIntLe(bytes, 16, 16)
        writeShortLe(bytes, 20, 1)
        writeShortLe(bytes, 22, channels)
        writeIntLe(bytes, 24, sampleRate)
        writeIntLe(bytes, 28, byteRate)
        writeShortLe(bytes, 32, blockAlign)
        writeShortLe(bytes, 34, bitsPerSample)
        writeAscii(bytes, 36, "data")
        writeIntLe(bytes, 40, dataSize)

        return bytes
    }

    private fun createMp3(frameCount: Int): ByteArray {
        val frameLength = 104
        val frame = ByteArray(frameLength)
        frame[0] = 0xFF.toByte()
        frame[1] = 0xFB.toByte()
        frame[2] = 0x10.toByte()
        frame[3] = 0x00.toByte()

        return ByteArray(frameLength * frameCount).also { bytes ->
            repeat(frameCount) { index ->
                frame.copyInto(bytes, destinationOffset = index * frameLength)
            }
        }
    }

    private fun writeAscii(target: ByteArray, offset: Int, value: String) {
        value.encodeToByteArray().copyInto(target, destinationOffset = offset)
    }

    private fun writeIntLe(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value and 0xFF).toByte()
        target[offset + 1] = ((value shr 8) and 0xFF).toByte()
        target[offset + 2] = ((value shr 16) and 0xFF).toByte()
        target[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLe(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value and 0xFF).toByte()
        target[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
