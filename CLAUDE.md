# Morning Alarm — Project Instructions

## Overview

**Morning Alarm** — mood-based alarm & sleep companion app.
Two emotional modes: **Night** (sleep preparation) and **Morning** (atmospheric wake-up via scene + melody + image).

Tech stack: **Kotlin Multiplatform (KMP)** — shared business logic, native UIs (Jetpack Compose on Android, SwiftUI on iOS), Ktor backend.

## Primary Reference

**All architectural decisions, code conventions, and project-specific rules are documented in [AGENTS.md](AGENTS.md).**

Read [AGENTS.md](AGENTS.md) at the start of each conversation and follow all rules described there.

## Project Modules

```
Morning-Alarm/
├── android/          — Android app (Kotlin, Jetpack Compose, AndroidX Navigation)
├── ios/              — iOS app (Swift, SwiftUI, KMP integration via SKIE)
├── app-shared/       — Shared KMP module: business logic, ViewModels, state (commonMain/androidMain/iosMain)
├── shared/           — Shared DTOs, API contracts, constants (used by both app-shared and server)
├── server/           — Ktor backend server (modular monolith, ports & adapters)
├── design-app/       — Web design prototype app (generates Figma-ready designs for target apps)
└── xy-app/           — Reference KMP template (architecture source, do NOT modify)
```

## Quick Reference

Key topics covered in [AGENTS.md](AGENTS.md):
- Agent behavior (language, comments, code style)
- Kotlin Multiplatform (KMP) usage and best practices
- Swift ↔ KMP bridge using SKIE library
- Server module architecture (see `server/AGENTS.md`)
- UI Kit cross-platform components
- Module-specific guidelines
- Product concept and MVP scope

## Build & Run Commands

```bash
# Shared module
./gradlew :app-shared:build
./gradlew :app-shared:allTests

# Android
./gradlew :android:assembleDebug
./gradlew :android:testDebugUnitTest

# Server (dev mode — embedded H2, no PostgreSQL needed)
./gradlew :server:run
./gradlew :server:test
# Server (prod mode — requires PostgreSQL)
SERVER_DEV_MODE=false ./gradlew :server:run
./gradlew :server:runProd -PprodHost=... -PprodPort=... -PprodPublicUrl=...

# Shared DTOs/contracts
./gradlew :shared:build

# All tests
./gradlew test
```

## Key Libraries

| Component | Library | Purpose |
|-----------|---------|---------|
| State management | Orbit MVI | MVI + UDF pattern for ViewModels |
| DI | Koin | Dependency injection across all modules |
| HTTP client | Ktor Client | Multiplatform networking |
| HTTP server | Ktor Server (Netty) | Backend API |
| Serialization | Kotlinx Serialization | JSON across all modules |
| iOS interop | SKIE | Swift async/await bridge for KMP |
| Image loading | Coil 3 | Cross-platform image loading |
| Navigation (Android) | AndroidX Navigation | Type-safe compose navigation |
| Navigation (iOS) | SwiftUI NavigationStack | Native iOS navigation |

## Important

- **`AGENTS.md` files are the single source of truth** for agent instructions across the entire project. When updating agent instructions, always edit the `AGENTS.md` files — not `CLAUDE.md`. The `CLAUDE.md` files only serve as pointers to the corresponding `AGENTS.md`.
- The `xy-app/` directory is a **read-only reference** — use its patterns, do not modify it.
- Always consult the relevant `AGENTS.md` when making architectural or implementation decisions.
- Important structural changes must include updates to the relevant module-level `AGENTS.md` in the same task.
