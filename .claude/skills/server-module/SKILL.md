---
name: server-module
description: |
  Scaffolds a new server feature module for the Morning Alarm Ktor backend. Creates domain models,
  use cases, ports, infra implementations, API routes, and DTO mappers following the modular monolith
  + ports & adapters architecture. Triggers on: "add server module", "create API endpoint",
  "new server feature", "add backend module".
user-invocable: true
argument-hint: "[module-name]"
allowed-tools: Read, Write, Edit, Glob, Grep, Bash(mkdir *)
---

# Server Module Scaffold

Creates a complete server feature module following ports & adapters pattern.

## Usage

`/server-module alarm` — creates the alarm server module
`/server-module content` — creates the content catalog module

## What Gets Created

For a module `<feature>`:

### 1. Domain Layer
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/domain/`

```kotlin
// <Feature>.kt — pure domain model
data class <Feature>(
    val id: String,
    // domain fields
)
```

### 2. Application Layer (Use Cases)
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/application/`

```kotlin
// <Feature>UseCase.kt
class <Feature>UseCase(
    private val repository: <Feature>Repository
) {
    fun getById(id: String): <Feature> {
        return repository.findById(id)
            ?: throw NotFoundException("<Feature> not found: $id")
    }
}
```

### 3. Ports (Interfaces)
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/application/ports/`

```kotlin
// <Feature>Repository.kt
interface <Feature>Repository {
    fun findById(id: String): <Feature>?
    fun findAll(): List<<Feature>>
    fun save(entity: <Feature>): <Feature>
    fun delete(id: String)
}
```

### 4. Infrastructure (Port Implementations)
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/infra/`

```kotlin
// InMemory<Feature>Repository.kt — for MVP
class InMemory<Feature>Repository : <Feature>Repository {
    private val storage = mutableMapOf<String, <Feature>>()
    // implement interface methods
}
```

### 5. API Layer (Routes + Mappers)
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/api/`

```kotlin
// <Feature>Routes.kt
fun Route.configure<Feature>Routes(useCase: <Feature>UseCase) {
    route("/api/<feature>") {
        get { /* list */ }
        get("/{id}") { /* get by id */ }
        post { /* create */ }
        put("/{id}") { /* update */ }
        delete("/{id}") { /* delete */ }
    }
}

// <Feature>Mapper.kt — domain ↔ DTO mapping
fun <Feature>.toDto(): <Feature>Dto { ... }
fun <Feature>Dto.toDomain(): <Feature> { ... }
```

### 6. Shared DTO
Path: `shared/src/commonMain/kotlin/com/morningalarm/dto/`

```kotlin
@Serializable
data class <Feature>Dto(
    val id: String,
    // DTO fields matching API contract
)
```

### 7. Wiring
- Register in `bootstrap/Modules.kt`
- Register routes in `bootstrap/Routing.kt`

### 8. Tests
Path: `server/src/test/kotlin/com/morningalarm/server/modules/<feature>/`

```kotlin
class <Feature>RoutesTest {
    @Test
    fun `GET all returns list`() = testApplication {
        // ...
    }
}
```

## Rules

- Follow `server/AGENTS.md` for all architecture decisions
- Domain layer has NO dependencies on Ktor, DB, or infra
- Application depends only on domain + ports
- Infra implements ports
- API layer handles DTO mapping
- All errors map to `ApiError` DTO
- Use `X-Request-Id` header for tracing
- Start with InMemory repositories for MVP
