# Project AGENTS Guide

This file describes the structure and maintenance conventions for the Morning Alarm project.
It will be expanded over time.

## Agent Behavior (Global)

- Always respond to the user in Russian.
- Always write code comments in English.
- Avoid obvious comments. Comments must explain non-obvious or complex logic, or clarify why a specific line exists when it is not self-evident.
- Always update the relevant module-level Markdown documentation when adding or changing important project structure or behavior.
- When researching third-party libraries, frameworks, SDKs, plugins, or external APIs, always use MCP Context7 first to retrieve documentation and examples.
- If Context7 does not provide enough information, perform web research only on official documentation, official vendor websites, official repositories, or official sample projects.

## Documentation Maintenance (Global)

Documentation updates are mandatory, not optional. When important entities are added, changed, renamed, or removed, the agent must update the relevant module-level `*.md` files in the same task.

Examples of required documentation updates:
- Added a new screen or feature: document the screen name, package/path, and a short purpose description in the module documentation.
- Added a new package or feature folder: document what lives there and why it exists.
- Added or changed a server API endpoint: document the endpoint, request/response purpose, and what it does in the server module documentation.
- Added a new shared domain model, DTO, or contract: document the model name, module/package, and what responsibility it has.
- Added important navigation, DI wiring, or architecture rules: record them in the nearest relevant module `md` file.

Rules:
- Update documentation in the closest relevant module documentation file first, not only in the root file.
- Keep descriptions short and factual.
- Documentation must be updated in the same change set as the code, not postponed.
- If no module documentation file exists in the affected area, create one when the change is important enough to require future orientation.
- If PR/code review comments reveal a critical mistake, architectural pitfall, or recurring anti-pattern, do not limit the fix to code only. Record the lesson as a concrete rule in the relevant module-level `AGENTS.md` so the same class of mistake is less likely to be repeated.

## Testing Rules (Global)

- Server endpoint tests must cover all meaningful scenarios for each HTTP method, not only the happy path.
- For server calls, always add tests for success cases, validation errors, auth/access errors, conflict/not-found cases, and important edge cases when they exist.
- Any API change must update and regenerate the OpenAPI/Swagger schema in the same change set.
- Do not create database migrations until the user explicitly gives a release-preparation command.

## Product Concept

**Morning Alarm** is a mood-based alarm & sleep companion with two emotional modes:

- **Night** — sleep preparation: meditations, ambient sounds, relaxation scenes
- **Morning** — atmospheric wake-up: fullscreen scene + melody + thematic image

**Key UX principle:** minimum actions, maximum atmosphere. Not a list of settings, but ready-made scenarios (Gentle Sunrise, Rainy Sleep, Deep Calm Night, Strong Energy Morning).

### MVP Screens (5 key screens)
1. **Alarm list** — current time, next alarm, active alarms list, FAB "+"
2. **Create/Edit alarm** — time, repeat days, melody, scene/image, snooze
3. **Sleep library** — mood chips, meditations, ambient sounds, sleep timer
4. **Ringing screen** — THE "wow" screen: fullscreen image, time, scenario name, phrase, Snooze/Stop
5. **Profile/Premium** — theme, language, voice stop, packs, upgrade

### Visual Themes
- **Morning (Soft Sunrise):** Background #FFF9F2, Primary #FFB86B, Accent #7CC6FF
- **Night (Deep Blue Night):** Background #0B1020, Primary #7C8CFF, Accent #9BD1FF
- **Auto mode:** Morning 06:00–18:00, Night 18:00–06:00; Sleep section → always Night; Ringing → always Morning

### Navigation
3 bottom tabs: **Alarm** | **Sleep** | **Profile**

## Mobile Tech Summary

The future mobile applications use a native UI + shared logic stack:
- Android: Kotlin + Jetpack Compose
- iOS: Swift + SwiftUI
- Shared mobile logic: Kotlin Multiplatform in `app-shared`
- Swift interoperability with shared Kotlin: SKIE

Detailed mobile architecture, KMP references, Swift ↔ KMP rules, and `xy-app`
reference-template usage are documented in:
- `mobile-apps/AGENTS.md`

## Project Modules

### Module dependency graph
```
mobile-apps (mobile application roots + shared mobile architecture rules)
  ├─ android-app (Android app root)
  │    ↓ depends on
  │  app-shared (KMP library)
  │    ↓ depends on
  │  shared (DTOs, API contracts)
  └─ ios-app (iOS app root)
       ↓ depends on
     app-shared (iOS framework via SKIE)
       ↓ depends on
     shared (iOS framework)

server (Ktor backend)
  ↓ depends on
shared (DTOs, API contracts)

desktop-admin (Compose Desktop admin app)
  ↓ depends on
shared (DTOs, API contracts)

web (Marketing website)
  — standalone frontend, SEO-first public site

design-app (Web prototype)
  — standalone, no Kotlin dependencies
```

### Server module
All server-specific architecture and maintenance instructions are documented in:
`server/AGENTS.md`

Use that file as the single source of truth for server structure, libraries,
configuration, request/response formats, and extension guidelines.

### Web module
The public marketing website lives in:
`web/`

Purpose:
- public SEO-first product website
- product pages, screenshots, download links, legal pages
- no server secrets or privileged admin access

Module-specific implementation and maintenance rules must be documented in:
`web/AGENTS.md`

### Desktop Admin module
The internal admin client lives in:
`desktop-admin/`

Purpose:
- local admin tool for managing server content
- uses shared Kotlin DTOs and route contracts from `shared`
- never acts as a security boundary on its own; admin access is enforced by the server

Module-specific implementation and maintenance rules must be documented in:
`desktop-admin/AGENTS.md`

### Mobile Apps module
The future mobile application roots live in:
`mobile-apps/`

Contents:
- `mobile-apps/android-app/` — Android app root
- `mobile-apps/ios-app/` — iOS app root

Shared mobile architecture and organization rules are documented in:
`mobile-apps/AGENTS.md`

### Android App module
The future Android application root lives in:
`mobile-apps/android-app/`

Purpose:
- Android-specific app bootstrap and entry points
- Android navigation host and Compose screens
- Android platform UI Kit and integrations

Module-specific architecture and maintenance rules are documented in:
`mobile-apps/android-app/AGENTS.md`

### iOS App module
The future iOS application root lives in:
`mobile-apps/ios-app/`

Purpose:
- iOS-specific app bootstrap and entry points
- SwiftUI navigation host and platform screens
- iOS platform UI Kit and Swift ↔ KMP integration layer

Module-specific architecture and maintenance rules are documented in:
`mobile-apps/ios-app/AGENTS.md`

## Platform App Architecture

The shared source of truth for mobile application architecture, mirrored
feature organization, and cross-platform naming rules is:
- `mobile-apps/AGENTS.md`

Platform-specific additions live in:
- `mobile-apps/android-app/AGENTS.md`
- `mobile-apps/ios-app/AGENTS.md`

Keep shared mobile rules in `mobile-apps/AGENTS.md` and reserve platform
module files for Android- or iOS-specific details.

## Package Naming

Base package: `com.morningalarm`

- App shared: `com.morningalarm.feature.*`, `com.morningalarm.data.*`, `com.morningalarm.di.*`
- Server: `com.morningalarm.server.*`
- Shared DTOs: `com.morningalarm.dto.*`, `com.morningalarm.api.*`
- Android app: `com.morningalarm.android.*`
