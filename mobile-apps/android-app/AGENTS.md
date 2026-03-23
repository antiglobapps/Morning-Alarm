# Android App AGENTS Guide

This directory is the future root of the Android application.

Shared mobile architecture, mirrored feature structure, and cross-platform
naming rules are defined in the parent file:
`mobile-apps/AGENTS.md`

## Purpose

- Android app bootstrap and Android entry points
- Jetpack Compose application UI and screen wiring
- Android navigation host and route registration
- Android-specific UI Kit components and platform integrations

## Ownership Boundary

`mobile-apps/android-app/` owns only Android-specific application code.

Keep the following out of this module when they can live elsewhere:
- shared business logic and feature state machines that belong in `app-shared`
- DTOs and API contracts that belong in `shared`
- backend logic that belongs in `server`

## Android-Specific Architecture

### App layering
- `mobile-apps/android-app/` is the Android shell around shared product logic from `app-shared`
- business rules, feature state, and reusable cross-platform logic should stay in `app-shared`
- Android-specific composition, permissions, lifecycle glue, and UI rendering live here

### DI
- use Koin for Android app wiring
- register feature dependencies in Koin modules close to the owning feature
- prefer `koinViewModel` and `parametersOf` for ViewModel creation when parameters are required

### Navigation
- use Jetpack Compose Navigation
- route contracts should be explicit and serializable
- ViewModels should emit navigation side effects through decoupled navigator interfaces instead of calling Android navigation APIs directly

### UI Kit
- Android UI Kit lives under `mobile-apps/android-app/ui-kit/`
- implement UI Kit with Jetpack Compose
- every reusable component must use the `UiKit` prefix
- component names must stay aligned with iOS where cross-platform equivalents exist
- file names must match component names

### UI Kit divergence policy
- keep APIs and behavior as close as practical to the iOS equivalents
- allow platform-specific differences only when they follow Android platform conventions
- document significant API differences in code comments when parity is intentionally broken

## Package Naming

Base package for Android-specific code:
- `com.morningalarm.android.*`

Shared Kotlin code consumed by Android continues to follow the root project rules:
- `com.morningalarm.feature.*`
- `com.morningalarm.data.*`
- `com.morningalarm.di.*`
