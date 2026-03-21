# Desktop Admin Module

This document describes the purpose and maintenance rules for the internal `desktop-admin` module.

## Purpose

`desktop-admin` is the internal desktop administration tool for Morning Alarm.

It is intended for:
- authenticated admin access to the backend
- content management flows such as ringtone administration
- previewing admin-managed content before it reaches end users

## Current Status

The module now contains a Compose Desktop MVP for internal ringtone administration.

Implemented parts:
- application entry point at `src/main/kotlin/com/morningalarm/desktopadmin/Main.kt`
- persistent base URL preference in `config/AppPreferences.kt`
- centralized admin API client in `data/AdminApiClient.kt`
- URL-based media playback layer in `media/` for previewing admin-managed remote audio/media inside the desktop app on macOS/Windows
- CI live smoke test in `src/test/kotlin/com/morningalarm/desktopadmin/data/AdminApiClientLiveSmokeTest.kt`
- Java 21 Gradle toolchain for local desktop builds
- login screen with connection mode selector (Dev localhost / Custom server), admin email/password/admin secret
- login form pre-fills shared dev admin credentials for localhost mode
- connection mode and custom URL persisted across restarts via OS preferences
- in-memory admin session handling with logout and session-expiration fallback
- ringtone management workspace with:
  - list/search
  - create/edit form
  - local image/audio attachment buttons instead of raw media URL editing
  - automatic server upload of attached media into server-managed storage
  - WAV/MP3 audio uploads auto-fill ringtone duration
  - active/premium toggles
  - delete action
  - live preview of the current ringtone card while editing
  - right-side preview panel next to the form editor
  - preview of current client-visible ringtone list
  - inline playback controls for live preview and published ringtone cards, backed by shared URL media playback state

Main package structure:
- `config` — local desktop preferences and runtime configuration
- `data` — HTTP client, session-expiration handling, API error mapping
- `media` — desktop media playback controller/engine abstractions for remote media URLs
- `ui` — Compose Desktop screens and admin workspace

## CI

- GitHub Actions runs desktop-admin checks selectively when `desktop-admin`, `server`, `shared`, root Gradle files, or CI scripts change.
- The desktop-admin CI job starts its own isolated local dev server in dev mode and waits for `/health/ready` before running tests.
- Smoke tests must seed their own data through public/admin HTTP APIs and must not rely on direct database setup.
- The current live smoke entry point is `data/AdminApiClientLiveSmokeTest.kt`.

## Rules

- Reuse shared DTOs and route contracts from the `shared` module whenever desktop-admin talks to the server.
- Do not treat the desktop client as a security boundary. Real access control must always be enforced by the server.
- Never store production secrets in the repository or hardcode privileged credentials in the client.
- Reuse central secret identifiers from `docs/configuration/secrets.md` and `config/secrets.catalog.toml`; do not invent desktop-admin-only names for shared server credentials.
- Keep bearer token and admin secret only in runtime memory unless a dedicated secure storage decision is documented and implemented.
- React to `401` by dropping the current session and returning the operator to the login screen.
- Any new screen, package, or admin workflow added here must be documented in this file.
