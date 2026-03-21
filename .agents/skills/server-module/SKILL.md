---
name: server-module
description: Scaffold or extend a Morning Alarm Ktor backend module using the project's modular monolith and ports-and-adapters rules. Use for new API/backend module work; do not use for mobile-only features.
---

# Server Module Scaffold

Creates a backend feature module following ports and adapters.

## What Gets Created

For a module `<feature>`:

### 1. Domain Layer
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/domain/`

```kotlin
data class <Feature>(
    val id: String,
)
```

### 2. Application Layer
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/application/`

```kotlin
class <Feature>UseCase(
    private val repository: <Feature>Repository
) {
    fun getById(id: String): <Feature> {
        return repository.findById(id)
            ?: throw NotFoundException("<Feature> not found: $id")
    }
}
```

### 3. Ports
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/application/ports/`

```kotlin
interface <Feature>Repository {
    fun findById(id: String): <Feature>?
    fun findAll(): List<<Feature>>
    fun save(entity: <Feature>): <Feature>
    fun delete(id: String)
}
```

### 4. Infrastructure
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/infra/`

```kotlin
class InMemory<Feature>Repository : <Feature>Repository {
    private val storage = mutableMapOf<String, <Feature>>()
}
```

### 5. API Layer
Path: `server/src/main/kotlin/com/morningalarm/server/modules/<feature>/api/`

```kotlin
fun Route.configure<Feature>Routes(useCase: <Feature>UseCase) {
    route("/api/<feature>") {
        get { /* list */ }
        get("/{id}") { /* get by id */ }
        post { /* create */ }
        put("/{id}") { /* update */ }
        delete("/{id}") { /* delete */ }
    }
}
```

### 6. Shared DTO
Path: `shared/src/commonMain/kotlin/com/morningalarm/dto/`

```kotlin
@Serializable
data class <Feature>Dto(
    val id: String,
)
```

## Rules

1. Follow `server/AGENTS.md` for server architecture decisions.
2. Keep the domain layer free from framework dependencies.
3. Put contracts in `shared` when they are part of the public API.
4. Start with simple in-memory implementations unless the task requires persistence.
5. Map server errors to `ApiError` and preserve `X-Request-Id`.
