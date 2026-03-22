# Desktop Admin Module

This document describes the purpose, architecture, and maintenance rules for the internal `desktop-admin` module.

## Purpose

`desktop-admin` is the internal desktop administration tool for Morning Alarm.

It is intended for:
- authenticated admin access to the backend
- content management flows such as ringtone administration
- previewing admin-managed content before it reaches end users

## Architecture

The module follows **Clean Architecture** principles adapted for a Compose Desktop application:

```
┌─────────────────────────────────────────────────────┐
│  UI Layer (Compose Desktop)                         │
│  Stateless @Composable functions                    │
│  Receives State, emits events to ViewModel          │
├─────────────────────────────────────────────────────┤
│  Presentation Layer (ViewModels)                    │
│  Orbit MVI ContainerHost<State, SideEffect>         │
│  Owns state, orchestrates use cases                 │
├─────────────────────────────────────────────────────┤
│  Data Layer (Repositories)                          │
│  Encapsulates API calls, hides auth details         │
│  AdminRingtoneRepository → AdminApiClient           │
├─────────────────────────────────────────────────────┤
│  Shared DTOs (`:shared` module)                     │
│  API contracts, request/response models             │
└─────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Library | Purpose |
|-----------|---------|---------|
| State management | Orbit MVI | MVI + UDF pattern via `ContainerHost<State, SideEffect>` |
| DI | Koin | Dependency injection, `koinInject` in Composable scope |
| ViewModel | AndroidX Lifecycle ViewModel | Scoped lifecycle for containers |
| UI | Compose Desktop (Material 3) | Stateless composable functions |
| HTTP client | Ktor Client (OkHttp) | Multiplatform networking |
| Media playback | JavaFX Media | Desktop audio preview |
| Navigation | Custom sealed-class router | Type-safe back stack, no external deps |
| Serialization | Kotlinx Serialization | JSON across all modules |

### Key Patterns

**Navigation** — custom type-safe router in `navigation/` package:
- `Screen` — sealed interface, each destination is a `data object` or `data class` with its arguments
- `NavigationController` — manages a back stack of `Screen` instances via Compose `mutableStateOf`
- `AppRouter` — single `@Composable` that renders the current screen via `when (screen)`
- `replaceAll(screen)` — for login→workspace transitions (prevents back to login)
- `navigateTo(screen)` — push new screen onto stack
- `goBack()` — pop current screen
- To add a new screen: add a case to `Screen`, handle it in `AppRouter`

**Orbit MVI** — every screen has a ViewModel implementing `ContainerHost`:
- `State` — immutable data class describing the full UI state
- `SideEffect` — sealed interface for one-off events (snackbar, navigation, session expiry)
- `intent { }` — coroutine block for async operations
- `reduce { state.copy(...) }` — atomic state mutation
- `postSideEffect(...)` — fire-and-forget event delivery
- Import `org.orbitmvi.orbit.viewmodel.container` (not `org.orbitmvi.orbit.container`) for ViewModel extension

**Koin DI** — single module `desktopAdminModule` in `di/DesktopAdminModule.kt`:
- `single` for app-wide singletons (e.g. `AppPreferences`)
- `factory` with `parametersOf(session)` for session-scoped dependencies (Repository, ViewModel)
- `koinInject<T>()` in `@Composable` scope to retrieve dependencies

**Stateless Composable** — UI functions receive `State` and `ViewModel` (or event callbacks), never hold mutable business state themselves.

**Repository** — encapsulates `bearerToken` and `adminSecret`, so the UI layer never touches auth credentials directly.

## Package Structure

```
desktop-admin/src/main/kotlin/com/morningalarm/desktopadmin/
├── Main.kt                              — Entry point: Koin init + Compose Window
├── config/
│   └── AppPreferences.kt               — OS-level preferences (connection mode, custom URL)
├── data/
│   ├── AdminApiClient.kt               — Ktor HTTP client, multipart uploads, error mapping
│   └── AdminRingtoneRepository.kt      — Facade over ApiClient, hides auth from UI layer
├── di/
│   └── DesktopAdminModule.kt           — Koin module: all DI bindings
├── media/
│   ├── MediaPlaybackController.kt      — Playback state machine, PlayableMedia, MediaPlaybackState
│   └── JavaFxMediaPlaybackEngine.kt    — JavaFX Media implementation with thread safety
├── navigation/
│   ├── Screen.kt                       — Sealed interface with type-safe destinations
│   ├── NavigationController.kt         — Back stack management via Compose state
│   └── AppRouter.kt                    — Screen renderer (when-branch per destination)
└── ui/
    ├── DesktopAdminApp.kt              — Root Composable: theme + NavigationController + AppRouter
    ├── RingtoneDraft.kt                — Form models and DTO mappers
    ├── login/
    │   ├── LoginViewModel.kt           — Orbit MVI: LoginState, LoginSideEffect
    │   └── LoginScreen.kt             — Stateless login form with connection mode selector
    ├── workspace/
    │   ├── WorkspaceViewModel.kt       — Orbit MVI: CRUD, upload, visibility, premium
    │   └── AdminWorkspace.kt           — 3-panel layout: list, editor, preview
    └── components/
        ├── PreviewCard.kt              — Reusable ringtone preview card with playback
        └── RingtoneForm.kt             — Reusable ringtone editor form
```

### Test Structure

```
desktop-admin/src/test/kotlin/com/morningalarm/desktopadmin/
├── TestFixtures.kt                          — Shared fakes, test data factories
├── data/
│   └── AdminApiClientLiveSmokeTest.kt      — Opt-in live smoke test against a running dev server
├── navigation/
│   └── NavigationControllerTest.kt         — Back stack unit tests
└── ui/
    ├── login/
    │   └── LoginViewModelTest.kt           — Login state and error handling
    └── workspace/
        └── WorkspaceViewModelTest.kt       — CRUD, upload, visibility, error tests
```

## Feature Inventory

- Login screen with connection mode selector (Dev localhost / Custom server)
- Login form pre-fills shared dev admin credentials for localhost mode
- Connection mode and custom URL persisted across restarts via OS preferences
- In-memory admin session handling with logout and session-expiration fallback
- Ringtone management workspace:
  - list/search panel
  - create/edit form with media attachment (image + audio)
  - automatic server upload of attached media into server-managed storage
  - WAV/MP3 audio uploads auto-fill ringtone duration
  - visibility management (Public / Private / Inactive)
  - premium toggle
  - delete action
  - live preview card (updates as you type)
  - client-visible ringtone list preview
  - inline playback controls for audio preview

## Rules

### Architecture Rules

- **Follow Clean Architecture.** New features must respect the layered separation: UI → ViewModel → Repository → ApiClient. Do not put business logic in Composable functions.
- **Extend, don't break.** When adding new features, follow the existing architecture. If a new domain area appears (e.g. user management), create a new Repository + ViewModel + Screen set following the same patterns.
- **Orbit MVI for all screens.** Every screen must have its own ViewModel implementing `ContainerHost<State, SideEffect>`. Use `intent {}` for all async work, `reduce {}` for state changes, `postSideEffect()` for one-off events.
- **Koin for all dependencies.** Register all new ViewModels and Repositories in `DesktopAdminModule.kt`. Use `koinInject<T>()` in Composable scope. Never instantiate ViewModels manually in Composable code.
- **Stateless Composable functions.** UI functions must receive state as parameters and emit events via callbacks or ViewModel method calls. Local `remember` state is allowed only for pure UI concerns (scroll position, text field focus).
- **Repository per domain.** Each domain area (ringtones, users, etc.) gets its own Repository that hides auth and API details from the presentation layer.
- **Reusable components in `ui/components/`.** Shared UI elements live in `components/` package. When a composable is used from more than one screen, extract it there.
- **Navigation via `NavigationController`.** All screen transitions go through `NavigationController`. To add a new screen: (1) add a case to `Screen` sealed interface, (2) add a `when`-branch in `AppRouter`, (3) create the screen's ViewModel + Composable in a new `ui/<feature>/` package. Never navigate by manipulating Compose state directly from UI code.
- **Pass transient screen state through `Screen` arguments.** If one screen must hand off transient state to another screen, such as a logout error or initial banner message, model it as data on the target `Screen` and apply it from `AppRouter`. Do not use Koin/service locator lookups inside routers or composables to fetch another screen's ViewModel and mutate it indirectly.

### Testing Rules

- **All new features must be covered by tests.** Every new ViewModel, Repository, or non-trivial utility must have corresponding unit tests.
- **Tests must be updated when code changes.** If you modify a ViewModel's behavior, state shape, or side effects — update the relevant tests in the same task.
- **orbit-test for ViewModel tests.** Initial state is auto-checked by default, so do not call deprecated `expectInitialState()`. Use `viewModel.test(this) { ... }` with `expectState { copy(...) }` and `expectSideEffect(...)` for regular flows. If a test must assert the initial state explicitly, use `settings = TestSettings(autoCheckInitialState = false)` and then `awaitState()` or `expectState`. Remember: `postSideEffect` happens BEFORE the `finally` block in `executeWithErrorHandling`, so `expectSideEffect` must come before the final `expectState { copy(isBusy = false) }`.
- **Hand-rolled fakes, not mocks.** Follow the `FakeAdminRingtoneRepository` pattern in `TestFixtures.kt`. Create fake implementations with call counters and `shouldFail` error injection. Do not use Mockk or Mockito.
- **Test fixtures in `TestFixtures.kt`.** Shared test data factories (`testRingtoneDetail()`, `testRingtoneListItem()`, etc.) and fakes live in a single `TestFixtures.kt` file.
- **Smoke tests are opt-in.** Live integration tests (e.g. `AdminApiClientLiveSmokeTest`) are gated by `DESKTOP_ADMIN_SMOKE_ENABLED` and require a running dev server.
- **Smoke tests must behave like external clients.** When a live smoke test needs fixtures, create them through public/admin HTTP APIs instead of direct database setup.

### General Rules

- Reuse shared DTOs and route contracts from the `shared` module whenever desktop-admin talks to the server.
- Do not treat the desktop client as a security boundary. Real access control must always be enforced by the server.
- Never store production secrets in the repository or hardcode privileged credentials in the client.
- Reuse central secret identifiers from `docs/configuration/secrets.md` and `config/secrets.catalog.toml`; do not invent desktop-admin-only names for shared server credentials.
- Keep bearer token and admin secret only in runtime memory unless a dedicated secure storage decision is documented and implemented.
- React to `401` by dropping the current session and returning the operator to the login screen.
- Any new screen, package, or admin workflow added here must be documented in this file.

### Future: `ui-core` Module

A shared `ui-core` Gradle module is planned for reusable Compose components between the Android app and desktop-admin. When it is created:
- Move generic UI components (e.g. `PreviewCard`, `Badge`) from `desktop-admin/ui/components/` into `ui-core`.
- Desktop-admin will depend on `ui-core` to render mobile-like previews of admin-managed content.
- Keep desktop-specific components (e.g. `JFileChooser` wrappers) in desktop-admin.
