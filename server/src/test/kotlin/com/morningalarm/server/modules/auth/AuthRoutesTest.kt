package com.morningalarm.server.modules.auth

import com.morningalarm.api.ApiHeaders
import com.morningalarm.api.auth.DevAdminDefaults
import com.morningalarm.api.auth.AuthRoutes
import com.morningalarm.dto.ApiError
import com.morningalarm.dto.auth.AdminLoginRequestDto
import com.morningalarm.dto.auth.AdminLoginResponseDto
import com.morningalarm.dto.auth.EmailLoginRequestDto
import com.morningalarm.dto.auth.EmailLoginResponseDto
import com.morningalarm.dto.auth.EmailRegisterRequestDto
import com.morningalarm.dto.auth.EmailRegisterResponseDto
import com.morningalarm.dto.auth.PasswordResetConfirmRequestDto
import com.morningalarm.dto.auth.PasswordResetRequestDto
import com.morningalarm.dto.auth.PasswordResetRequestResponseDto
import com.morningalarm.dto.auth.RefreshTokenRequestDto
import com.morningalarm.dto.auth.RefreshTokenResponseDto
import com.morningalarm.dto.auth.SocialAuthRequestDto
import com.morningalarm.dto.auth.SocialAuthResponseDto
import com.morningalarm.dto.auth.SocialProviderDto
import com.morningalarm.server.bootstrap.AppConfig
import com.morningalarm.server.bootstrap.ModuleDependencies
import com.morningalarm.server.bootstrap.applicationModule
import com.morningalarm.server.bootstrap.createModuleDependencies
import com.morningalarm.server.modules.auth.application.AuthService
import com.morningalarm.server.modules.auth.infra.InMemoryAuthEmailGateway
import io.ktor.client.call.body
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertContains

class AuthRoutesTest {
    @Test
    fun `bootstrap create admin creates first admin with temporary password`() {
        val service = createService(Files.createTempDirectory("morning-alarm-auth-bootstrap").toString())

        val result = service.createAdmin(
            email = "first-admin@example.com",
            displayName = "First Admin",
        )

        assertEquals("first-admin@example.com", result.email)
        assertTrue(result.temporaryPassword.length >= 8)

        val session = service.loginWithEmail(
            email = result.email,
            password = result.temporaryPassword,
        )
        assertEquals("first-admin@example.com", result.email)
        assertEquals(com.morningalarm.dto.auth.UserRoleDto.ADMIN.name, session.role.name)
    }

    @Test
    fun `bootstrap admin keeps admin role after refresh`() {
        val service = createService(Files.createTempDirectory("morning-alarm-auth-bootstrap-refresh").toString())

        val result = service.createAdmin(
            email = "refresh-admin@example.com",
            displayName = "Refresh Admin",
        )

        val loginSession = service.loginWithEmail(
            email = result.email,
            password = result.temporaryPassword,
        )
        val refreshSession = service.refresh(loginSession.refreshToken)

        assertEquals(com.morningalarm.dto.auth.UserRoleDto.ADMIN.name, refreshSession.role.name)
    }

    @Test
    fun `dev admin bootstrap creates shared default admin on empty database`() {
        val service = createService(Files.createTempDirectory("morning-alarm-auth-dev-bootstrap").toString())

        val result = service.createDevAdminIfDatabaseEmpty(
            email = DevAdminDefaults.EMAIL,
            password = DevAdminDefaults.PASSWORD,
            displayName = DevAdminDefaults.DISPLAY_NAME,
        )

        assertNotNull(result)
        assertEquals(DevAdminDefaults.EMAIL, result.email)
        assertEquals(DevAdminDefaults.PASSWORD, result.password)

        val session = service.loginWithEmail(
            email = DevAdminDefaults.EMAIL,
            password = DevAdminDefaults.PASSWORD,
        )
        assertEquals(com.morningalarm.dto.auth.UserRoleDto.ADMIN.name, session.role.name)
    }

    @Test
    fun `dev admin bootstrap does nothing when database already has users`() {
        val service = createService(Files.createTempDirectory("morning-alarm-auth-dev-bootstrap-existing").toString())

        service.registerWithEmail(
            email = "user@example.com",
            password = "very-secret",
            displayName = "User",
        )

        val result = service.createDevAdminIfDatabaseEmpty(
            email = DevAdminDefaults.EMAIL,
            password = DevAdminDefaults.PASSWORD,
            displayName = DevAdminDefaults.DISPLAY_NAME,
        )

        assertEquals(null, result)
    }

    @Test
    fun `social auth creates new user on first login and reuses user on second login`() = testApplicationWithDependencies { dependencies, client ->
        val firstResponse = client.post(AuthRoutes.SOCIAL) {
            contentType(ContentType.Application.Json)
            setBody(
                SocialAuthRequestDto(
                    provider = SocialProviderDto.GOOGLE,
                    token = "google-token-1",
                    email = "social@example.com",
                    displayName = "Social User",
                ),
            )
        }

        assertEquals(HttpStatusCode.OK, firstResponse.status)
        val firstBody = firstResponse.body<SocialAuthResponseDto>()
        assertTrue(firstBody.session.isNewUser)

        val secondResponse = client.post(AuthRoutes.SOCIAL) {
            contentType(ContentType.Application.Json)
            setBody(
                SocialAuthRequestDto(
                    provider = SocialProviderDto.GOOGLE,
                    token = "google-token-1",
                ),
            )
        }

        assertEquals(HttpStatusCode.OK, secondResponse.status)
        val secondBody = secondResponse.body<SocialAuthResponseDto>()
        assertFalse(secondBody.session.isNewUser)
        assertEquals(firstBody.session.userId, secondBody.session.userId)
    }

    @Test
    fun `email register login refresh and reset password flow works`() = testApplicationWithDependencies { dependencies, client ->
        val registerResponse = client.post(AuthRoutes.EMAIL_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailRegisterRequestDto(
                    email = "user@example.com",
                    password = "very-secret",
                    displayName = "User",
                ),
            )
        }

        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val registerBody = registerResponse.body<EmailRegisterResponseDto>()
        assertTrue(registerBody.session.isNewUser)

        val loginResponse = client.post(AuthRoutes.EMAIL_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailLoginRequestDto(
                    email = "user@example.com",
                    password = "very-secret",
                ),
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val loginBody = loginResponse.body<EmailLoginResponseDto>()
        assertFalse(loginBody.session.isNewUser)

        val refreshResponse = client.post(AuthRoutes.TOKEN_REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = loginBody.session.refreshToken))
        }

        assertEquals(HttpStatusCode.OK, refreshResponse.status)
        val refreshBody = refreshResponse.body<RefreshTokenResponseDto>()
        assertEquals(loginBody.session.userId, refreshBody.session.userId)
        assertNotEquals(loginBody.session.refreshToken, refreshBody.session.refreshToken)
        assertEquals(loginBody.session.role, refreshBody.session.role)
        assertTrue(refreshBody.session.bearerToken.isNotBlank())

        val resetRequestResponse = client.post(AuthRoutes.PASSWORD_RESET_REQUEST) {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetRequestDto(email = "user@example.com"))
        }

        assertEquals(HttpStatusCode.OK, resetRequestResponse.status)
        val resetRequestBody = resetRequestResponse.body<PasswordResetRequestResponseDto>()
        assertTrue(resetRequestBody.resetRequested)

        val resetToken = (dependencies.authEmailGateway as InMemoryAuthEmailGateway).lastResetToken("user@example.com")
        assertNotNull(resetToken)

        val resetConfirmResponse = client.post(AuthRoutes.PASSWORD_RESET_CONFIRM) {
            contentType(ContentType.Application.Json)
            setBody(
                PasswordResetConfirmRequestDto(
                    token = resetToken,
                    newPassword = "brand-new-secret",
                ),
            )
        }

        assertEquals(HttpStatusCode.OK, resetConfirmResponse.status)

        val oldPasswordLoginResponse = client.post(AuthRoutes.EMAIL_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailLoginRequestDto(
                    email = "user@example.com",
                    password = "very-secret",
                ),
            )
        }
        assertEquals(HttpStatusCode.Unauthorized, oldPasswordLoginResponse.status)

        val newPasswordLoginResponse = client.post(AuthRoutes.EMAIL_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailLoginRequestDto(
                    email = "user@example.com",
                    password = "brand-new-secret",
                ),
            )
        }
        assertEquals(HttpStatusCode.OK, newPasswordLoginResponse.status)
    }

    @Test
    fun `admin login requires admin secret and admin role`() = testApplicationWithDependencies(
        configOverride = { copy(adminAccessSecret = "top-secret") },
    ) { _, client ->
        client.post(AuthRoutes.EMAIL_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailRegisterRequestDto(
                    email = "admin@example.com",
                    password = "very-secret",
                    displayName = "Admin",
                ),
            )
        }
        client.post(AuthRoutes.EMAIL_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailRegisterRequestDto(
                    email = "user@example.com",
                    password = "very-secret",
                    displayName = "User",
                ),
            )
        }

        val success = client.post(AuthRoutes.ADMIN_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                AdminLoginRequestDto(
                    email = "admin@example.com",
                    password = "very-secret",
                    adminSecret = "top-secret",
                ),
            )
        }
        assertEquals(HttpStatusCode.OK, success.status)
        assertEquals(
            "ADMIN",
            success.body<AdminLoginResponseDto>().session.role.name,
        )

        val wrongSecret = client.post(AuthRoutes.ADMIN_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                AdminLoginRequestDto(
                    email = "admin@example.com",
                    password = "very-secret",
                    adminSecret = "wrong-secret",
                ),
            )
        }
        assertEquals(HttpStatusCode.Forbidden, wrongSecret.status)

        val nonAdmin = client.post(AuthRoutes.ADMIN_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(
                AdminLoginRequestDto(
                    email = "user@example.com",
                    password = "very-secret",
                    adminSecret = "top-secret",
                ),
            )
        }
        assertEquals(HttpStatusCode.Forbidden, nonAdmin.status)
    }

    @Test
    fun `duplicate registration returns conflict error`() = testApplicationWithDependencies { _, client ->
        client.post(AuthRoutes.EMAIL_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailRegisterRequestDto(
                    email = "duplicate@example.com",
                    password = "very-secret",
                ),
            )
        }

        val duplicateResponse = client.post(AuthRoutes.EMAIL_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(
                EmailRegisterRequestDto(
                    email = "duplicate@example.com",
                    password = "very-secret",
                ),
            )
        }

        assertEquals(HttpStatusCode.Conflict, duplicateResponse.status)
        val error = duplicateResponse.body<ApiError>()
        assertEquals("conflict", error.code)
        assertNotNull(duplicateResponse.headers[HttpHeaders.XRequestId])
    }

    @Test
    fun `swagger and openapi endpoints are exposed`() = testApplicationWithDependencies { _, client ->
        val swaggerResponse = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, swaggerResponse.status)
        assertContains(swaggerResponse.body<String>(), "Swagger UI")

        val openApiHtmlResponse = client.get("/openapi")
        assertEquals(HttpStatusCode.OK, openApiHtmlResponse.status)
        assertContains(openApiHtmlResponse.body<String>(), "swagger")

        val openApiYamlResponse = client.get("/openapi.yaml")
        assertEquals(HttpStatusCode.OK, openApiYamlResponse.status)
        assertContains(openApiYamlResponse.body<String>(), "openapi: 3.0.3")
        assertContains(openApiYamlResponse.body<String>(), "/api/v1/auth/email/login")
        assertContains(openApiYamlResponse.body<String>(), "operationId: loginByEmail")
        assertContains(openApiYamlResponse.body<String>(), "bearerAuth")
        assertContains(openApiYamlResponse.body<String>(), "/api/v1/auth/admin/login")
        assertContains(openApiYamlResponse.body<String>(), ApiHeaders.ADMIN_SECRET)
    }

    @Test
    fun `auth repository persists user between dependency recreations`() {
        val dataDir = Files.createTempDirectory("morning-alarm-auth-persist").toString()
        val firstService = createService(dataDir)
        val firstSession = firstService.registerWithEmail(
            email = "persist@example.com",
            password = "very-secret",
            displayName = "Persistent User",
        )

        val secondService = createService(dataDir)
        val secondSession = secondService.loginWithEmail(
            email = "persist@example.com",
            password = "very-secret",
        )

        assertEquals(firstSession.userId, secondSession.userId)
    }

    private fun testApplicationWithDependencies(
        configOverride: (AppConfig.() -> AppConfig) = { this },
        block: suspend io.ktor.server.testing.ApplicationTestBuilder.(ModuleDependencies, HttpClient) -> Unit,
    ) = testApplication {
        val config = configOverride(testConfig(Files.createTempDirectory("morning-alarm-auth-test").toString()))
        val dependencies = createModuleDependencies(config)
        application {
            applicationModule(
                config = config,
                dependencies = dependencies,
            )
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

    private fun createService(dataDir: String): AuthService {
        return createModuleDependencies(testConfig(dataDir)).authService
    }

    private fun testConfig(dataDir: String): AppConfig = AppConfig(
        devMode = true,
        host = "127.0.0.1",
        port = 8080,
        publicUrl = null,
        logPublicUrl = false,
        mediaStorageDir = "$dataDir/media",
        mediaPublicBaseUrl = "http://localhost:8080",
        mediaMaxImageBytes = 1024 * 1024,
        mediaMaxAudioBytes = 5 * 1024 * 1024,
        firebaseBucketName = null,
        firebaseCredentialsPath = null,
        databaseUrl = "jdbc:h2:file:$dataDir/auth-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
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
}
