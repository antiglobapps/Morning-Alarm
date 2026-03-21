---
name: feature-scaffold
description: |
  Scaffolds a new feature for the Morning Alarm KMP project. Creates all necessary files across modules:
  ViewModel + State + SideEffect in app-shared/commonMain, Android Screen in android/, iOS Screen stub in ios/,
  Koin DI registration, Navigation route. Triggers on: "create feature", "add screen", "new feature",
  "scaffold", "generate feature".
user-invocable: true
argument-hint: "[feature-group/feature-name]"
allowed-tools: Read, Write, Edit, Glob, Grep, Bash(mkdir *)
---

# Feature Scaffold

Creates a complete feature skeleton across all KMP modules for Morning Alarm.

## Usage

`/feature-scaffold alarm/edit` — creates the EditAlarm feature
`/feature-scaffold sleep/meditation` — creates the Meditation feature

## What Gets Created

For a feature `<group>/<feature>` (e.g., `alarm/edit`):

### 1. Shared ViewModel + State (`app-shared/src/commonMain`)
Path: `app-shared/src/commonMain/kotlin/com/morningalarm/feature/<group>/<feature>/`

Files:
- `<Feature>ViewModel.kt` — Orbit MVI ViewModel with initial state and intent handling
- `<Feature>Navigation.kt` — Navigator interface for this screen

Template for ViewModel:
```kotlin
package com.morningalarm.feature.<group>.<feature>

import com.morningalarm.feature.base.BaseViewModel
import org.orbitmvi.orbit.container

class <Feature>ViewModel(
    // inject dependencies here
) : BaseViewModel<ViewState, SideEffect>() {

    override val container = viewModelScope.container<ViewState, SideEffect>(
        initialState = ViewState.Loading,
        onCreate = { load() }
    )

    private fun load() = intent {
        // TODO: implement
        reduce { ViewState.Loading }
    }

    // Sealed interfaces for state and side effects
    sealed interface ViewState {
        data object Loading : ViewState
        data class Data(/* fields */) : ViewState
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
}
```

Template for Navigation:
```kotlin
package com.morningalarm.feature.<group>.<feature>

interface <Feature>Navigation {
    interface Navigator {
        fun back()
    }
}
```

### 2. Android Screen (`android/`)
Path: `android/src/main/kotlin/com/morningalarm/feature/<group>/<feature>/`

File: `<Feature>Screen.kt`
```kotlin
package com.morningalarm.feature.<group>.<feature>

@Composable
fun <Feature>Screen(
    viewModel: <Feature>ViewModel,
    navigator: <Feature>Navigation.Navigator,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // TODO: implement UI
    when (state) {
        is <Feature>ViewModel.ViewState.Loading -> { /* loading */ }
        is <Feature>ViewModel.ViewState.Data -> { /* content */ }
        is <Feature>ViewModel.ViewState.Error -> { /* error */ }
    }
}
```

### 3. iOS Screen Stub (`ios/`)
Path: `ios/MorningAlarm/feature/<group>/<feature>/`

File: `<Feature>Screen.swift`
```swift
import SwiftUI
import Shared

struct <Feature>Screen: View {
    @ObservedObject var viewModel: <Feature>ViewModel

    var body: some View {
        // TODO: implement UI
        Text("<Feature>")
    }
}
```

### 4. Koin Registration
Add ViewModel factory to the feature's DI module or create one.

### 5. Navigation Route
Add `@Serializable` route data class/object to Android navigation.

## Steps

1. Parse the feature group and name from `$ARGUMENTS`
2. Create directories in all three locations
3. Generate ViewModel + State + Navigation in app-shared
4. Generate Android Screen in android/
5. Generate iOS Screen stub in ios/
6. Register in Koin DI module
7. Add navigation route (Android)
8. Report created files

## Rules

- Follow naming conventions from `app-shared/AGENTS.md`
- Use same feature names across all platforms
- ViewModel contains ViewState and SideEffect as nested sealed interfaces
- Use Orbit MVI container pattern
- Android screens use `collectAsStateWithLifecycle()`
- iOS screens use `@ObservedObject`
