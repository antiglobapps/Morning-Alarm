# iOS App AGENTS Guide

This directory is the future root of the iOS application.

Shared mobile architecture, mirrored feature structure, and cross-platform
naming rules are defined in the parent file:
`mobile-apps/AGENTS.md`

## Purpose

- iOS app bootstrap and iOS entry points
- SwiftUI application UI and screen wiring
- SwiftUI navigation host and route handling
- iOS-specific UI Kit components and Swift ↔ KMP integration

## Ownership Boundary

`mobile-apps/ios-app/` owns only iOS-specific application code.

Keep the following out of this module when they can live elsewhere:
- shared business logic and feature state machines that belong in `app-shared`
- DTOs and API contracts that belong in `shared`
- backend logic that belongs in `server`

## iOS-Specific Architecture

### App layering
- `mobile-apps/ios-app/` is the iOS shell around shared product logic from `app-shared`
- business rules, feature state, and reusable cross-platform logic should stay in `app-shared`
- SwiftUI composition, Apple platform integrations, and presentation glue live here

### Swift ↔ KMP integration
- consume shared Kotlin code through the generated iOS framework and SKIE-friendly Swift APIs
- do not recreate shared business logic in Swift when it already exists in `app-shared`
- keep Swift-specific adapters thin and focused on platform presentation, lifecycle, and Apple frameworks

### DI
- shared dependency graph is built with Koin
- iOS should initialize Koin through the shared platform entry point from `iosMain`
- keep Swift-side dependency glue minimal and avoid parallel service graphs unless a platform API requires it

### Navigation
- use SwiftUI `NavigationStack` for app navigation
- platform routes may use Swift enums or path models, but navigation decisions should still originate from decoupled navigator contracts and side effects
- avoid embedding feature business decisions directly into SwiftUI navigation code

### UI Kit
- iOS UI Kit lives under `mobile-apps/ios-app/ui-kit/`
- implement UI Kit with SwiftUI
- every reusable component must use the `UiKit` prefix
- component names should stay aligned with Android where cross-platform equivalents exist
- file names must match component names

### UI Kit divergence policy
- keep APIs and behavior as close as practical to the Android equivalents
- allow platform-specific differences only when they follow Apple platform conventions
- document significant API differences in code comments when parity is intentionally broken
