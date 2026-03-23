# Mobile Apps AGENTS Guide

This directory is the root container for the future mobile applications.

## Purpose

- keep Android and iOS app roots together under one top-level directory
- provide the shared source of truth for mobile application architecture
- define cross-platform naming, folder organization, and mirroring rules

## Contents

- `mobile-apps/android-app/` — Android application root
- `mobile-apps/ios-app/` — iOS application root

Platform-specific additions live in:
- `mobile-apps/android-app/AGENTS.md`
- `mobile-apps/ios-app/AGENTS.md`

## Ownership Boundary

`mobile-apps/` owns only mobile application structure and mobile-specific
architecture rules.

Keep the following out of this directory when they can live elsewhere:
- shared business logic and feature state machines that belong in `app-shared`
- DTOs and API contracts that belong in `shared`
- backend logic that belongs in `server`

## Mobile Technology Stack

- Android app technology: Kotlin + Jetpack Compose
- iOS app technology: Swift + SwiftUI
- Shared mobile logic: Kotlin Multiplatform in `app-shared`
- Swift interoperability with shared Kotlin: SKIE

## Kotlin Multiplatform (KMP) Usage and References

Detailed and up-to-date information about using Kotlin Multiplatform must be
taken from the official documentation and sample projects listed below.

### Official documentation
- Kotlin Multiplatform - Get Started: https://kotlinlang.org/docs/multiplatform/get-started.html
- Android Developers - Kotlin Multiplatform: https://developer.android.com/kotlin/multiplatform/

### Sample projects
- Kotlin Multiplatform Samples: https://github.com/android/kotlin-multiplatform-samples/tree/main

### Swift ↔ KMP bridge

The project uses the SKIE library for Swift ↔ KMP interoperability.

SKIE provides:
- safer and more idiomatic Swift APIs for KMP code
- improved interoperability with Swift Concurrency (`async/await`)
- better mapping of Kotlin types to Swift
- reduced boilerplate when consuming shared logic from iOS

#### SKIE documentation
- SKIE Features & Documentation: https://skie.touchlab.co/features/

#### SKIE source code
- SKIE GitHub Repository: https://github.com/touchlab/SKIE

### Context7 requirement

When answering questions, explaining implementation details, or making
architectural decisions related to Kotlin Multiplatform or Swift ↔ KMP
interoperability, the agent must use Context7 first to retrieve information
from the sources listed above.

Rules:
- always search Context7 first for relevant sections in the provided documentation
- base explanations strictly on the retrieved documentation or sample code
- reference the specific documentation pages, features, or source modules used
- if the required information is not found in Context7, explicitly state that it is not covered

### Reference template

The `xy-app/` directory contains a reference KMP project template with proven
architecture patterns. Use it as a guide for:
- module structure and Gradle configuration
- ViewModel + Orbit MVI patterns
- Koin DI setup
- navigation patterns on Android and iOS
- server architecture with ports and adapters

Do not modify `xy-app/`. It is read-only reference material.

## Mobile Architecture Source of Truth

### App layering
- `mobile-apps/android-app/` and `mobile-apps/ios-app/` are native app shells around shared product logic from `app-shared`
- business rules, feature state, and reusable cross-platform logic stay in `app-shared`
- platform composition, lifecycle glue, navigation hosts, permissions, and native integrations live in the app roots

### MVI with Orbit
- shared features expose `ViewState` and `SideEffect` models through Orbit-based ViewModels from `app-shared`
- both platforms render persistent `uiState` and react to one-time `sideEffects`
- user actions should be forwarded to shared ViewModel intents instead of duplicating feature logic in platform views

Reference pattern:
```kotlin
BaseViewModel<STATE, SIDE_EFFECT>
  -> PlatformViewModel
  -> ContainerHost<STATE, SIDE_EFFECT>
```

Important flows:
- `uiState: StateFlow<STATE>` for persistent screen state
- `sideEffects: Flow<SIDE_EFFECT>` for one-time events such as navigation or transient messages

### State and side effects
- model loading, content, and error states explicitly in shared feature contracts
- keep navigation events inside side effects, not inside persistent state
- platform-local UI state should only handle purely local presentation concerns

Reference shape:
```kotlin
sealed interface ViewState {
    data object Loading : ViewState
    data class Data(...) : ViewState
    data class Error(val message: String) : ViewState
}

sealed interface SideEffect {
    sealed interface ViewEffect : SideEffect {
        data class ShowMessage(val message: String) : ViewEffect
    }
    sealed interface Navigation : SideEffect {
        data object Back : Navigation
    }
}
```

### Cross-platform mirroring rules
- Android and iOS must implement feature architecture as similarly as practical
- a feature that exists on both platforms must keep the same feature name and the same conceptual role on both sides
- mirror the feature tree between `mobile-apps/android-app/` and `mobile-apps/ios-app/` for shared user flows
- screens, ViewModels, states, intents, and other shared entities should use identical names across platforms when they represent the same feature concept
- file name must match type name
- when using `expect/actual`, keep the same type name in `commonMain`, `androidMain`, and `iosMain`

### Folder strategy
- shared business logic, models, use cases, and feature state belong in `app-shared`
- Android UI, Android navigation wiring, and Android-specific integrations belong in `mobile-apps/android-app/`
- iOS UI, iOS navigation wiring, and Swift or Apple-framework integrations belong in `mobile-apps/ios-app/`
- mirrored features should keep the same base path on both platforms whenever both apps implement the same flow

Example:
- Android: `mobile-apps/android-app/feature/profile/ProfileScreen.kt`
- iOS: `mobile-apps/ios-app/feature/profile/ProfileScreen.swift`

### Navigation boundary
- each platform uses native navigation APIs
- shared feature modules define models, navigator interfaces, intents, and side effects for navigation
- platform apps handle actual route execution without moving business decisions into UI navigation code

### UI Kit
- UI Kit is not shared source code between Android and iOS
- Android and iOS each keep a platform-specific UI Kit implementation
- every reusable component must use the `UiKit` prefix
- component names must be identical on both platforms when they represent the same concept
- file names must match component names
- keep APIs and behavior as close as practical across platforms
- document significant API differences in code comments when parity is intentionally broken

### DI
- use Koin for the shared dependency graph
- platform apps initialize and extend that graph only where a platform integration requires it

## Package Naming

Shared Kotlin code consumed by mobile apps follows the root project rules:
- `com.morningalarm.feature.*`
- `com.morningalarm.data.*`
- `com.morningalarm.di.*`
