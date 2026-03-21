# Apps AGENTS Guide

This folder structure covers client applications and the shared KMP module. The structure and responsibilities follow the official KMP layout: platform apps + a shared module.

## Contents

- `android/` — Android app (Kotlin, Jetpack Compose, AndroidX Navigation, integration with the shared KMP module).
- `ios/` — iOS app (Swift, SwiftUI, KMP integration via SKIE).
- `app-shared/` — shared Kotlin Multiplatform module: shared logic in `commonMain`, platform implementations in `androidMain`/`iosMain`, iOS framework build.

Architecture and structure sources:
- Kotlin Multiplatform: Project structure, source sets (`commonMain`, `androidMain`, `iosMain`).
- Android Developers: KMP project structure (androidApp/iosApp/shared).
- SKIE: official source for Swift ↔ KMP interop.

## Naming Rules

- **Same names for shared entities.** A screen that exists on both platforms uses the same name: `AlarmListScreen.kt` and `AlarmListScreen.swift`. This also applies to ViewModel and state types (`ViewModel`, `ViewState`).
- **Identical feature names.** The feature folder name must match on Android and iOS: `feature/alarm/list`, `feature/sleep`, etc.
- **File name = type name.** Class/struct/enum lives in a file with the same name.
- **Platform implementations.** For `expect/actual`, use the same type name (for example, `AlarmScheduler` in `commonMain`, `actual` implementations in `androidMain`/`iosMain`).

## Where to Create New Files

- **Shared business logic, models, use cases, state:**
  `app-shared/src/commonMain/kotlin/com/morningalarm/feature/<FeatureGroup>/<Feature>/...`
  - Base classes and utilities: `app-shared/src/commonMain/kotlin/com/morningalarm/feature/base/...`
  - Example: `app-shared/src/commonMain/kotlin/com/morningalarm/feature/alarm/list/AlarmListViewModel.kt`
- **Platform implementations (push, notifications, alarm scheduling, OS access):**
  `app-shared/src/androidMain/kotlin/...` and `app-shared/src/iosMain/kotlin/...`
- **Android UI and navigation:**
  `android/src/main/kotlin/com/morningalarm/feature/<FeatureGroup>/<Feature>/...`
  - Example: `android/src/main/kotlin/com/morningalarm/feature/alarm/list/AlarmListScreen.kt`
- **iOS UI and navigation:**
  `ios/MorningAlarm/feature/<FeatureGroup>/<Feature>/...`
  - Example: `ios/MorningAlarm/feature/alarm/list/AlarmListScreen.swift`
- **Android UI Kit components:**
  `android/uikit/src/main/kotlin/com/morningalarm/uikit/...`
- **iOS UI Kit components:**
  `ios/MorningAlarm/ui-kit/...`

## File and Folder Strategy (Android/iOS Mirroring)

- **Shared as the source of truth.** Shared logic and state live in `app-shared`. Platforms consume it without duplication.
- **Mirrored feature structure.** Each feature keeps the same path and set of key entities in `android` and `ios`. If you add `feature/alarm/edit/EditAlarmScreen` on Android, create `feature/alarm/edit/EditAlarmScreen` on iOS.
- **Platform navigation and push live in apps.** Each app uses its native navigation and push integration, while shared models/intents/events for screens stay in `app-shared`.
- **Unified names for screens and flows.** All cross-platform screens, states, and intents share identical names and base location in the tree.
- **Platform-specific via expect/actual.** Declare the interface/expect in `commonMain`, implement strictly in `androidMain`/`iosMain` with the same type name.
- **Minimal platform knowledge in shared.** `app-shared` does not depend on concrete navigation/push libraries; it provides interfaces and models for app-level integration.

## Feature Structure Template

Each feature follows this structure in `app-shared/src/commonMain`:

```
feature/<group>/<feature>/
├── <Feature>ViewModel.kt       — Orbit MVI ViewModel
├── <Feature>ViewState.kt       — sealed interface for UI states (or inside ViewModel)
├── <Feature>SideEffect.kt      — sealed interface for side effects (or inside ViewModel)
└── <Feature>Navigation.kt      — Navigator interface for this screen
```

Platform screens in `android/` and `ios/`:
```
feature/<group>/<feature>/
└── <Feature>Screen.kt (.swift)  — platform-specific UI
```

## Morning Alarm Feature Groups

Based on product concept:
- `feature/alarm/list/` — alarm list screen
- `feature/alarm/edit/` — create/edit alarm
- `feature/alarm/ringing/` — alarm ringing fullscreen
- `feature/sleep/` — sleep library, meditations, ambient sounds
- `feature/profile/` — settings, premium, theme selection

## UI Kit (Platform-Specific Components)

UI Kit is **NOT** cross-platform shared. Each platform has its own UI Kit implementation.

- **Purpose:** Reusable UI components with consistent naming across platforms.
- **Naming:** All components must use the `UiKit` prefix and have identical names on both platforms (e.g., `UiKitButton`, `UiKitAlarmCard`, `UiKitChip`).
- **Android:** Jetpack Compose components in `android/uikit/`.
- **iOS:** SwiftUI components in `ios/MorningAlarm/ui-kit/`.
- **No shared UI Kit code:** Android and iOS UI Kit modules evolve independently while keeping component naming aligned.

## Theme System

Both platforms must implement the same theme system:

### Colors (Morning — Soft Sunrise)
- Background: `#FFF9F2`, Surface: `#FFFFFF`
- Primary: `#FFB86B`, Secondary: `#FFD9A8`, Accent: `#7CC6FF`
- Text Primary: `#2B2B2B`, Text Secondary: `#6F6F6F`

### Colors (Night — Deep Blue Night)
- Background: `#0B1020`, Surface: `#151C33`
- Primary: `#7C8CFF`, Secondary: `#4E5D94`, Accent: `#9BD1FF`
- Text Primary: `#F5F7FF`, Text Secondary: `#A8B0C5`, Glow: `#B8A1FF`

### UI Component Style
- Border radius: 20–24dp
- Large rounded buttons, simple line/soft fill icons
- FAB "+" button for alarm creation
- Alarm cards with large time display, on/off toggle, weekday chips
- Cards with soft shadows (Morning) or subtle elevation (Night)
- Animations: slow and smooth, especially in Night mode
