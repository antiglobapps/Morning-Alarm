---
name: feature-scaffold
description: Scaffold a new Morning Alarm feature across app-shared, Android, and iOS, including Orbit MVI ViewModel/state, navigation contract, DI wiring, and initial screen stubs. Use for creating new screens or features; do not use for reviewing existing code.
---

# Feature Scaffold

Creates a complete feature skeleton across KMP modules.

## What Gets Created

For a feature `<group>/<feature>`:

### 1. Shared ViewModel + State
Path: `app-shared/src/commonMain/kotlin/com/morningalarm/feature/<group>/<feature>/`

Files:
- `<Feature>ViewModel.kt`
- `<Feature>Navigation.kt`

Template:
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
        reduce { ViewState.Loading }
    }

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

### 2. Android Screen
Path: `android/src/main/kotlin/com/morningalarm/feature/<group>/<feature>/`

```kotlin
package com.morningalarm.feature.<group>.<feature>

@Composable
fun <Feature>Screen(
    viewModel: <Feature>ViewModel,
    navigator: <Feature>Navigation.Navigator,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state) {
        is <Feature>ViewModel.ViewState.Loading -> { /* loading */ }
        is <Feature>ViewModel.ViewState.Data -> { /* content */ }
        is <Feature>ViewModel.ViewState.Error -> { /* error */ }
    }
}
```

### 3. iOS Screen Stub
Path: `ios/MorningAlarm/feature/<group>/<feature>/`

```swift
import SwiftUI
import Shared

struct <Feature>Screen: View {
    @ObservedObject var viewModel: <Feature>ViewModel

    var body: some View {
        Text("<Feature>")
    }
}
```

### 4. Wiring
- Register ViewModel in Koin
- Add Android navigation route
- Keep naming aligned across platforms

## Rules

1. Follow naming conventions from `app-shared/AGENTS.md`.
2. Keep business logic in `commonMain`.
3. Use Orbit MVI state and side effect patterns.
4. Use the same feature names on Android and iOS.
5. Prefer minimal stubs over speculative implementation.
