#
# Server Module
#

This document explains how the `server` module is structured, which libraries it uses,
how configuration works, how HTTP requests/responses are shaped, and how to extend the
server safely (DTOs, routes, headers, logic modules, repositories).
---

## 1) Server Module Structure

Path: `server/src/main/kotlin/com/morningalarm/server`

High-level layout (modular monolith + ports/adapters):

- `bootstrap/`
  - `ApplicationModule.kt` – Ktor plugin setup + routing wiring.
  - `Routing.kt` – collects and registers all module routes.
  - `Modules.kt` – central dependency wiring (ports -> infra adapters).
  - `AppConfig.kt` – configuration model + env loader.
- `shared/`
  - `errors/` - exception mapping (`StatusPages`).
  - `validation/` – validation helpers.
  - `tracing/` – request id handling (CallId).
  - `health/` – health endpoints (live/ready).
- `modules/<feature>/`
  - `domain/` – pure domain models and rules.
  - `application/` – use cases and orchestration.
  - `application/ports/` – interfaces (repositories, external clients).
  - `infra/` – implementation of ports (DB, HTTP, cache).
  - `api/` – Ktor routes + DTO mappers.

Rule of dependencies:
- `domain` has **no** dependencies on Ktor, DB, HTTP, or infra.
- `application` depends on `domain` + `ports` only.
- `infra` implements ports and depends on external libs.
- `api` depends on `application` and DTO mappers.

---

## 2) Libraries Used (Server)

- Ktor server core, Netty
- Ktor ContentNegotiation (JSON)
- Ktor CallId (request id)
- Ktor CallLogging
- Ktor StatusPages (error mapping)
- Ktor Compression (gzip)
- Kotlinx Serialization (JSON)
- Logback (logging)
- Test: Ktor test host, Kotlin test, Ktor client content negotiation

Notes:
- JSON is handled by Kotlinx Serialization.
- Errors are normalized to `ApiError` (from `shared` module).
- Request ID is enforced by CallId.

---

## 3) Configuration

Config model: `server/bootstrap/AppConfig.kt`

Environment variables:
- `SERVER_HOST` (string)
- `SERVER_PORT` (int)
- `PORT` (int, fallback when `SERVER_PORT` is not set)
- `SERVER_PUBLIC_URL` (string, optional)
- `SERVER_LOG_PUBLIC_URL` (boolean, optional; `1/true/yes/y/on` => enabled)
- `SERVER_MEDIA_STORAGE_DIR` (string, optional; default `./server-data/media`)
- `SERVER_MEDIA_PUBLIC_BASE_URL` (string, optional; absolute base URL used in generated media links)
- `SERVER_MEDIA_MAX_IMAGE_BYTES` (long, optional; default `5242880`)
- `SERVER_MEDIA_MAX_AUDIO_BYTES` (long, optional; default `52428800`)
- `SERVER_DB_URL` (string, optional; default `jdbc:postgresql://localhost:5432/morning_alarm`)
- `SERVER_DB_USER` (string, optional; default `morning_alarm`)
- `SERVER_DB_PASSWORD` (string, optional; default `morning_alarm`)
- `SERVER_DB_DRIVER` (string, optional; default `org.postgresql.Driver`)
- `SERVER_DB_POOL_MAX_SIZE` (int, optional; default `10`)
- `SERVER_JWT_SECRET` (string, optional; default dev-only value, replace in non-dev environments)
- `SERVER_JWT_ISSUER` (string, optional; default `morning-alarm-server`)
- `SERVER_JWT_AUDIENCE` (string, optional; default `morning-alarm-app`)
- `SERVER_ADMIN_EMAILS` (comma-separated string, optional; users with these emails get `ADMIN` role on creation)

Production run task:
```
./gradlew :server:runProd -PprodHost=... -PprodPort=... -PprodPublicUrl=...
```

Local Docker run:
```
docker compose up --build
```

Database migrations policy:
- Do not add migration files yet.
- Until a dedicated release-preparation command is given, schema evolution should stay in bootstrap schema initialization only.
- Migration tooling will be introduced later as a separate release-preparation task.

---

## 4) Request/Response Format

### 4.1 Successful Responses
- Default: JSON responses using Kotlinx Serialization.
- Use DTO classes from the `shared` module (shared with clients).
- Ktor `ContentNegotiation` handles serialization automatically.

### 4.2 Error Responses (Standardized)
All errors must map to `ApiError` (from `shared`):
```json
{
  "code": "validation_error",
  "message": "Human readable message",
  "details": "optional details",
  "requestId": "uuid"
}
```

Exception mapping:
- `ValidationException` → 400
- `NotFoundException` → 404
- `ConflictException` → 409
- unknown error → 500 (`unexpected_error`)

### 4.3 Request ID Header
- Header key: `ApiHeaders.REQUEST_ID` (value `X-Request-Id`)
- If client doesn't send it, server generates a UUID.
- The same header is returned in responses.

---

## 5) How to Add New DTO Models

DTOs live in the `shared` module.

Steps:
1) Create `@Serializable` data class in `shared/src/commonMain/kotlin/com/morningalarm/dto/...`.
2) Keep DTOs **data-only** (no server logic).
3) Use DTOs only in `api` layer on the server.

Example:
```kotlin
@Serializable
data class AlarmDto(
    val id: String,
    val time: String,
    val days: List<String>,
    val melodyId: String,
    val sceneId: String,
    val isEnabled: Boolean
)
```

---

## 6) How to Add New API Routes

Routes belong to the module's `api/` package.

Steps:
1) Create `<Feature>Routes.kt` in `modules/<feature>/api`.
2) Map DTO ↔ domain using dedicated mappers.
3) Use use-cases from `application/`.
4) Create a dedicated test file for the new routes.
5) Update the OpenAPI schema in `server/src/main/resources/openapi/documentation.yaml`.
6) Register in `bootstrap/Routing.kt`.

---

## 7) How to Add a New Logic Module

Steps:
1) Create `modules/<feature>/domain` with pure models.
2) Create `modules/<feature>/application` for use cases.
3) Define ports in `modules/<feature>/application/ports`.
4) Implement ports in `modules/<feature>/infra`.
5) Create routes + DTO mappers in `modules/<feature>/api`.
6) Wire dependencies in `bootstrap/Modules.kt`.
7) Register routes in `bootstrap/Routing.kt`.

### Planned Server Modules for Morning Alarm
- `modules/alarm/` — alarm CRUD, scheduling metadata
- `modules/content/` — melodies, scenes, meditations catalog
- `modules/user/` — user profile, preferences, subscription status
- `modules/premium/` — subscription management, feature gates
- `modules/auth/` — social auth, email auth, password reset, refresh tokens
- `modules/ringtone/` — ringtone catalog, admin CRUD, client visibility rules, per-user likes
- `modules/media/` — admin media uploads and local dev media serving

---

## 8) How to Add Repositories

Repository is a **port** in `application/ports`, and implementation in `infra`.

Steps:
1) Define interface in `ports/`.
2) Implement it in `infra/`.
3) Provide implementation in `ModuleDependencies`.

---

## 9) Relation with `shared` Module

### API Contracts (in `shared`)
Contains shared API contracts:
- Route constants (base paths)
- Header constants
- DTO models (request/response)

Use it when server and client must agree on the same route/header/DTO.

Server should **not** keep DTOs in its own module unless they are server-only.

---

## 10) Testing Guidelines

Tests are in `server/src/test`.
Use `io.ktor.server.testing.testApplication`:
- configure `Application.module(...)`
- create Ktor client with JSON plugin
- assert HTTP status and DTO bodies

Do not use real external services in tests; rely on in-memory or fake ports.

Server endpoint tests are required to cover all meaningful scenarios for every route.
Minimum coverage for each endpoint:
- success / happy path
- request validation failures
- authentication / authorization failures when applicable
- conflict / not found / invalid state scenarios when applicable
- boundary and edge cases for important input variations

It is not acceptable to add only one happy-path test for a new endpoint. Tests must describe the full behavioral contract of the server call.

## 10.1) OpenAPI / Swagger Rule

The server must expose API documentation in web form through Swagger UI.

Current documentation endpoints:
- `/swagger` — interactive Swagger UI
- `/openapi` — OpenAPI HTML rendering
- `/openapi.yaml` — raw OpenAPI schema

Rule:
- Any API change must update the OpenAPI schema in `server/src/main/resources/openapi/documentation.yaml` in the same change set.
- Do not merge endpoint, request, response, header, or error-contract changes without updating the schema.
- OpenAPI endpoint tests must verify that the schema remains exposed after routing changes.
- Run `./gradlew :server:validateOpenApi` after schema changes.
- Keep `operationId`, tags, examples, and security schemes in sync with the actual API behavior.

---

## 11) Auth Module

Implemented module: `modules/auth/`

Purpose:
- authorize users by social provider token
- register and login users by email/password
- request and confirm password reset by email
- refresh expired bearer sessions using refresh token

Endpoints:
- `POST /api/v1/auth/social` — accepts provider (`GOOGLE`, `VK`, `FACEBOOK`, `APPLE`) and social token, returns session and `isNewUser`
- `POST /api/v1/auth/email/register` — creates a new email/password user and returns session
- `POST /api/v1/auth/email/login` — authenticates existing email/password user and returns session
- `POST /api/v1/auth/admin/login` — authenticates admin by email/password and additional admin access secret
- `POST /api/v1/auth/password/reset/request` — creates password reset token and sends it through the email gateway
- `POST /api/v1/auth/password/reset/confirm` — accepts reset token and new password, then returns a fresh session
- `POST /api/v1/auth/token/refresh` — exchanges refresh token for a new bearer session

Notes:
- Bearer token lifetime is 1 day.
- Access token format is JWT and contains current `userId` and `role`.
- Social token validation against Google/VK/Facebook/Apple is not implemented in MVP; the server currently treats the incoming social token as an opaque identity input.
- Auth persistence is PostgreSQL-based.
- Schema is currently bootstrapped by server code on startup; migrations are intentionally postponed until release-preparation.
- Auth endpoints are documented in Swagger/OpenAPI resources.
- Local container setup is defined in root `Dockerfile`, `.dockerignore`, and `docker-compose.yml`.

Admin bootstrap and access hardening:
- There is no public HTTP endpoint that grants admin role.
- `--create-admin --email=... --secret=...` creates the first admin user directly on the server machine and prints a temporary password once.
- `--promote-admin --email=... --secret=...` upgrades an existing user to `ADMIN`.
- Admin login is distinct from normal email login and requires the additional admin access secret from `SERVER_ADMIN_ACCESS_SECRET`.
- Protected admin API routes require both JWT role `ADMIN` and `ApiHeaders.ADMIN_SECRET` (`X-Admin-Secret`) when hardened mode is enabled.
- Recovery baseline currently consists of password reset, bootstrap-based role reissue, and server-side rotation of the admin access secret.

## 12) User and Ringtone Modules

Implemented business user module:
- `modules/user/` — internal business user record created for each auth user

Rules:
- Business users are internal and must not be exposed for browsing other users.
- Business user role is currently `ADMIN` or `USER`.
- Admin role is assigned by server configuration through `SERVER_ADMIN_EMAILS`.

Implemented ringtone module:
- `GET /api/v1/ringtones` — authenticated client list of active ringtones only
- `GET /api/v1/ringtones/{ringtoneId}` — authenticated client detail for active ringtone only
- `POST /api/v1/ringtones/{ringtoneId}/like-toggle` — authenticated like toggle for current user
- `GET /api/v1/admin/ringtones` — admin list of all ringtones
- `GET /api/v1/admin/ringtones/{ringtoneId}` — admin detail
- `POST /api/v1/admin/ringtones` — admin create
- `PUT /api/v1/admin/ringtones/{ringtoneId}` — admin update
- `DELETE /api/v1/admin/ringtones/{ringtoneId}` — admin delete
- `GET /api/v1/admin/ringtones/{ringtoneId}/preview` — admin preview of user-facing ringtone card
- `GET /api/v1/admin/ringtones/client-preview` — admin preview of current client-visible ringtone list
- `POST /api/v1/admin/ringtones/{ringtoneId}/active-toggle` — admin toggle active state
- `POST /api/v1/admin/ringtones/{ringtoneId}/premium-toggle` — admin toggle premium state

Ringtone entity fields:
- `title`
- `imageUrl` (absolute URL)
- `audioUrl` (absolute URL)
- `durationSeconds`
- `description`
- `isActive`
- `isPremium`
- `createdAtEpochSeconds`
- `updatedAtEpochSeconds`
- `createdByAdminId`
- `updatedByAdminId`

Like behavior:
- A user can like or unlike a ringtone through the same toggle endpoint.
- Client list/detail responses must include both `likesCount` and `isLikedByUser`.
- Inactive ringtones are hidden from client list/detail/like endpoints.
- Admin list/detail responses include full content-management state and counts.

## 13) Media Upload Module

Implemented module: `modules/media/`

Purpose:
- receive admin image uploads
- receive admin audio uploads
- store media through a replaceable storage abstraction
- expose local dev media files through public URLs

Current storage model:
- server uses a `MediaStorage` port
- current adapter is local filesystem storage for development
- generated upload responses always return absolute URLs
- future CDN/object-storage adapters should plug into the same port

Endpoints:
- `POST /api/v1/admin/uploads/image` — admin-only image upload
- `POST /api/v1/admin/uploads/audio` — admin-only audio upload
- `GET /media/{kind}/{fileName}` — local dev media file serving for generated absolute URLs

Rules:
- upload endpoints are admin-only
- image upload accepts only `image/*`
- audio upload accepts only `audio/*`
- file size limits are controlled by config
- ringtone create/update flows should use uploaded absolute URLs returned by the upload API

---

End of document.
