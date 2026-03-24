# Morning Alarm Code Review Style Guide

This document describes the main rules, architectural considerations, and code style requirements that must be taken into account and checked during code review. The information is grouped by system modules. Each module has its own rules and guidance that apply to it.

## 1. Module `shared`

Status: active module.

Purpose:
- A single layer of transport contracts between the server and clients.
- Stores routes, headers, request/response DTOs, and `ApiError`.

Architecture Rules:
- MUST: keep only data-only transport contracts in `shared`.
- MUST: do not move business logic, use-case validation, network calls, state computations, or platform-specific APIs into DTOs.
- MUST: keep public contracts under `com.morningalarm.api.*` and `com.morningalarm.dto.*`.
- MUST: keep admin-only contracts under `com.morningalarm.api.admin.*` and `com.morningalarm.dto.admin.*`.
- MUST: document any new DTOs, routes, headers, and contract models in `shared/AGENTS.md`.

Code Review Focus:
- MUST: check contract backward compatibility and impact on `server` and `desktop-admin`.
- MUST: do not allow mixing public and admin API models without a clear reason.
- MUST: new models must remain serializable and predictable in shape.
- IMPORTANT: prefer additive changes; breaking changes are acceptable only when all consumers and documentation are updated in sync.
- IMPORTANT: pay close attention to `ApiHeaders.REQUEST_ID`, `ApiHeaders.ADMIN_SECRET`, auth DTOs, and error contracts as a cross-module surface.
- IMPORTANT: do not mix `shared` and `app-shared` responsibilities; transport contracts must stay separate from mobile business logic.

External Best Practices:
- IMPORTANT: shared KMP code should live in common source sets, while platform-specific implementations should stay in target-specific source sets.
- IMPORTANT: use `expect/actual` only for real platform-specific needs, not as a replacement for ordinary interfaces.

Sources:
- Local: `AGENTS.md`, `shared/AGENTS.md`, `shared/build.gradle.kts`
- Official: https://kotlinlang.org/docs/multiplatform-discover-project.html
- Official: https://kotlinlang.org/docs/multiplatform-expect-actual.html

## 2. Module `server`

Status: active module.

Purpose:
- The project's Ktor backend.
- Implements a modular monolith with ports and adapters.

Architecture Rules:
- MUST: follow the `modules/<feature>/{domain,application,application/ports,infra,api}` structure.
- MUST: `api` must remain a thin transport layer without business logic, JDBC, file I/O, or complex orchestration.
- MUST: `application` must own use cases, orchestration, transaction boundaries, and application-level validation.
- MUST: `domain` must stay pure and not depend on Ktor, SQL, HTTP, or the file system.
- MUST: `infra` implements ports but must not make product business decisions.
- MUST: route handlers must not call infra directly by bypassing `application`.
- MUST: multi-repository state changes must run inside an explicit transaction boundary in the application layer.
- MUST: JDBC repositories must use the shared transaction/session model rather than ad-hoc connections.
- MUST: shared DTOs from `shared` remain transport contracts, not carriers of domain logic.
- MUST: schema bootstrap must remain centralized; do not scatter schema initialization across random startup points.

Code Review Focus:
- MUST: for any API change, update `server/src/main/resources/openapi/documentation.yaml` in the same change set.
- MUST: for any API change, update tests for success, validation errors, auth/access errors, conflict/not-found, and important edge cases.
- MUST: do not add migrations until the user explicitly gives a release-preparation command.
- MUST: verify that security contracts are not weakened: admin login, admin secret, rate limiting, audit logging, password reset recovery, and session revocation.
- MUST: verify that services are not growing into god services; if unrelated scenarios accumulate, they must be split.
- IMPORTANT: transport-layer mapper files should have direct test coverage if they map domain/application models to shared DTOs.
- IMPORTANT: startup/schema changes must remain backward-safe and idempotent for the dev environment.

External Best Practices:
- IMPORTANT: Ktor recommends using `ContentNegotiation` for serialization, `StatusPages` for centralized error mapping, and `testApplication` for server tests.
- IMPORTANT: OWASP recommends login throttling, a proper audit trail, and increased attention to recovery flows and authentication events.

Sources:
- Local: `AGENTS.md`, `README.md`, `server/AGENTS.md`, `server/build.gradle.kts`
- Official: https://ktor.io/docs/server-testing.html
- Official: https://ktor.io/docs/server-serialization.html
- Official: https://ktor.io/docs/server-status-pages.html
- Official: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- Official: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html

## 3. Module `desktop-admin`

Status: active module.

Purpose:
- An internal desktop client for administering server content.
- It is not a security boundary; security is enforced by the server.

Architecture Rules:
- MUST: follow the `UI -> ViewModel -> Repository -> ApiClient` chain.
- MUST: composable functions must remain stateless with respect to business state.
- MUST: every screen must have a ViewModel built on Orbit MVI with `ContainerHost<State, SideEffect>`.
- MUST: perform all async operations through `intent {}`.
- MUST: update persistent state through `reduce {}`.
- MUST: handle one-off events through `postSideEffect(...)` rather than storing them in persistent UI state.
- MUST: create dependencies and ViewModels through Koin rather than manually in UI code.
- MUST: bearer tokens and admin secrets must not leak into the UI layer.
- MUST: navigation must go only through `NavigationController`, `Screen`, and `AppRouter`.
- MUST: pass transient state between screens through `Screen` arguments, not through side-channel lookups of another ViewModel.

Code Review Focus:
- MUST: do not allow business logic, network calls, or auth handling inside composable functions.
- MUST: on `401`, the application must reset the session and return the user to the login screen.
- MUST: new admin workflows must reuse contracts from `shared` rather than introducing local DTO copies.
- MUST: tests must use hand-rolled fakes rather than Mockk/Mockito.
- MUST: new ViewModels, Repositories, and non-trivial utilities must have tests.
- IMPORTANT: verify the correct order of Orbit state/side effect assertions in tests.
- IMPORTANT: verify that media preview and desktop-specific APIs do not break thread safety and do not mix with domain logic.

External Best Practices:
- IMPORTANT: Compose guidance recommends state hoisting and passing events through callbacks rather than hiding mutable state dependencies.
- IMPORTANT: Koin recommends DI through composable-friendly injection APIs rather than manual dependency creation.
- IMPORTANT: one-off navigation and transient messages should be separated from persistent UI state.

Sources:
- Local: `desktop-admin/AGENTS.md`, `desktop-admin/build.gradle.kts`
- Official: https://developer.android.com/develop/ui/compose/state-hoisting
- Official: https://developer.android.com/develop/ui/compose/navigation
- Official: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-desktop-components.html
- Official: https://insert-koin.io/docs/reference/koin-compose/compose/
- Official: https://orbit-mvi.org/Core/

## 4. Module `web`

Status: active module.

Purpose:
- A public SEO-first marketing website.
- A static Astro site with no server secrets and no admin logic.

Architecture Rules:
- MUST: preserve the module as an SSG with only the minimum necessary client-side JS.
- MUST: the legal routes `/privacy` and `/terms` must remain stable.
- MUST: every public page must have correct `title`, `description`, canonical, Open Graph, and Twitter Card metadata.
- MUST: all images must have meaningful `alt` text.
- MUST: add new pages to the appropriate Header/Footer navigation.
- MUST: do not move server logic, secrets, admin flows, or privileged actions into this module.
- MUST: keep brand colors synchronized with `design-app/src/tokens/colors.ts`.

Code Review Focus:
- MUST: do not turn Astro pages into a client-heavy SPA without a clear need.
- MUST: verify correct file-based routing and proper placement of pages under `src/pages`.
- MUST: verify build-time env usage; only `PUBLIC_` variables may be exposed to client code.
- IMPORTANT: repeated page shell elements should be extracted into layouts/components rather than copied manually.
- IMPORTANT: any SEO changes must be reviewed from both search visibility and accessibility perspectives.

External Best Practices:
- IMPORTANT: Astro uses file-based pages, layouts for shared page structures, and the `PUBLIC_` prefix for client-exposed environment variables.
- IMPORTANT: Google recommends unique and precise page titles, good link text, and quality `alt` text for images.

Sources:
- Local: `web/AGENTS.md`, `web/build.gradle.kts`, `web/package.json`
- Official: https://docs.astro.build/en/basics/astro-pages/
- Official: https://docs.astro.build/en/basics/layouts/
- Official: https://docs.astro.build/en/guides/environment-variables/
- Official: https://developers.google.com/search/docs/fundamentals/seo-starter-guide

## 5. Module `design-app`

Status: active supporting module.

Purpose:
- A standalone web prototype for Morning Alarm.
- A showcase of screens, a design-system playground, and a source of tokens for downstream consumers, including `web`.

Architecture Rules:
- MUST: treat `design-app` as a design/prototype surface rather than as a production client or backend module.
- MUST: build new UI elements through `src/ds` and `src/tokens`, not through chaotic local hardcoded values.
- MUST: Morning/Night visual logic must flow through tokens, theme, and CSS variables.
- MUST: check token changes for impact on `web`, because the web module uses color tokens from here.
- MUST: do not move Kotlin/shared/server responsibilities into this module.
- IMPORTANT: if the module structure changes significantly, create or update module Markdown documentation, because the current `README.md` is template-based and is not the source of truth.

Code Review Focus:
- IMPORTANT: components should remain visually consistent with the design system and should not drift away from shared DS patterns.
- IMPORTANT: screen scenarios and routing should remain reproducible for design review.
- MUST: the Figma plugin and scripts must not contain secrets, environment-specific hardcode, or undocumented dependencies on the local environment.
- IMPORTANT: theme wiring should remain centralized, usually through a shared theme provider and tokens.
- IMPORTANT: reusable DS components should abstract visual patterns rather than copy MUI markup inline.

External Best Practices:
- IMPORTANT: React components should remain pure and predictable; shared state should be lifted to the nearest common owner.
- IMPORTANT: the Material UI theme should be applied from the root of the tree through `ThemeProvider`, not through a set of scattered local overrides.

Sources:
- Local: `AGENTS.md`, `design-app/package.json`, `design-app/src/App.tsx`, `design-app/src/theme/createAppTheme.ts`, `design-app/src/screens/DesignSystemScreen.tsx`, `web/AGENTS.md`
- Official: https://react.dev/learn/keeping-components-pure
- Official: https://react.dev/learn/sharing-state-between-components
- Official: https://mui.com/material-ui/customization/theming/

## 6. Module `mobile-apps`

Status: an architectural container, not an active build module.

Purpose:
- A single source of truth for mobile architecture, naming rules, and mirrored structure.

Architecture Rules:
- MUST: treat `mobile-apps/android-app` and `mobile-apps/ios-app` as native shell applications around shared logic from `app-shared`.
- MUST: shared business logic, feature state, use cases, and shared ViewModels must live in `app-shared`, not in app roots.
- MUST: DTOs and API contracts must live in `shared`, not in `mobile-apps` and not in `app-shared`.
- MUST: Android and iOS feature trees, naming, and conceptual roles must be mirrored as closely as possible.
- MUST: UI Kit must not become cross-platform shared source code; it must remain platform-specific.
- IMPORTANT: changes to `mobile-apps/AGENTS.md` affect the entire future mobile area and require especially strict review.

Code Review Focus:
- MUST: do not allow responsibility mixing between `shared`, `app-shared`, and platform app roots.
- MUST: do not allow platform-specific APIs in conceptually shared rules and shared models.
- IMPORTANT: navigation decisions should originate from side effects and navigator contracts rather than from platform views as a source of business decisions.

External Best Practices:
- IMPORTANT: the official KMP approach assumes native app shells and a shared module, rather than trying to move platform UI into common Kotlin code.

Sources:
- Local: `mobile-apps/AGENTS.md`, `AGENTS.md`
- Official: https://kotlinlang.org/docs/multiplatform-discover-project.html
- Official: https://developer.android.com/kotlin/multiplatform
- Official: https://skie.touchlab.co/features/

## 7. Module `app-shared`

Status: a target module that is not yet actually implemented.

Purpose:
- A future shared Kotlin Multiplatform layer for business logic, shared ViewModel/state, repository interfaces, and platform abstractions for mobile clients.

Architecture Rules:
- MUST: `commonMain` must contain shared business logic, feature state, use cases, interfaces, and cross-platform models.
- MUST: `androidMain` and `iosMain` must contain only platform-specific implementations.
- MUST: do not move DTOs and transport contracts into `app-shared`; they belong to the `shared` module.
- MUST: do not move platform UI, navigation framework code, permission flows, or lifecycle-heavy glue into `app-shared`.
- MUST: use `expect/actual` only for real platform-specific needs such as notifications, alarm scheduling, storage, vibration, audio, and background tasks.
- MUST: do not use `expect/actual` to abstract UI and navigation.
- IMPORTANT: the shared feature structure must remain stable and predictable: `ViewModel`, `ViewState`, `SideEffect`, `Navigation`.
- IMPORTANT: the current documentation in `app-shared/AGENTS.md` partially uses old `android/` and `ios/` paths; review should detect and avoid reinforcing this drift.

Code Review Focus:
- MUST: do not pull Android SDK or Apple framework imports into `commonMain`.
- MUST: do not duplicate feature logic on platforms if it can live in the shared layer.
- MUST: do not mix transport contracts and mobile business logic in one module.
- IMPORTANT: names for screen/viewmodel/state/side effect should match platform equivalents.
- IMPORTANT: the public API of the shared module should be convenient for SKIE and Swift interop.

External Best Practices:
- IMPORTANT: KMP recommends putting shared code in `commonMain` and platform-specific code in target source sets.
- IMPORTANT: `expect/actual` should be used carefully and only when there is a real platform difference.
- IMPORTANT: for the iOS bridge, it is better to keep the shared API thin, stable, and friendly to Swift concurrency/interoperability.

Sources:
- Local: `app-shared/AGENTS.md`, `AGENTS.md`
- Official: https://kotlinlang.org/docs/multiplatform-discover-project.html
- Official: https://kotlinlang.org/docs/multiplatform-expect-actual.html
- Official: https://skie.touchlab.co/features/

## 8. Module `mobile-apps/android-app`

Status: a placeholder/template, not an active part of the root build.

Current Context:
- It is currently a separate Android template project.
- It does not yet reflect the target Morning Alarm architecture.
- `app/build.gradle.kts` uses `appcompat` and `material` rather than the target Compose shell.
- The current `namespace` and `applicationId` use `com.antik.morningalarm`, which conflicts with the project rule `com.morningalarm.android.*`.

Architecture Rules:
- MUST: the Android app root must contain only Android-specific UI, navigation host, lifecycle glue, and integrations.
- MUST: shared business logic must not live in the Android app root; it must live in `app-shared`.
- MUST: navigation must be built on Jetpack Compose Navigation rather than arbitrary direct calls from shared logic.
- MUST: a ViewModel must not call Android navigation APIs directly; navigation must go through side effects and decoupled navigator contracts.
- MUST: UI Kit components must use the `UiKit` prefix and keep naming parity with iOS.

Code Review Focus:
- MUST: do not accept changes that cement `com.antik.*` as the permanent namespace for Morning Alarm.
- MUST: do not treat template artifacts as if they were the finished product architecture.
- MUST: do not duplicate mobile business logic in Android-specific code.
- IMPORTANT: if the module starts being developed further, it must be brought to the target stack of Compose + Koin + shared ViewModel/state.
- IMPORTANT: apply Compose best practices: hoist state, pass callbacks, and do not make composables the source of business decisions.

Sources:
- Local: `mobile-apps/android-app/AGENTS.md`, `mobile-apps/android-app/build.gradle.kts`, `mobile-apps/android-app/app/build.gradle.kts`, `mobile-apps/android-app/settings.gradle.kts`, `settings.gradle.kts`
- Official: https://developer.android.com/develop/ui/compose/state-hoisting
- Official: https://developer.android.com/develop/ui/compose/navigation
- Official: https://developer.android.com/topic/architecture/recommendations

## 9. Module `mobile-apps/ios-app`

Status: a placeholder directory without application code.

Architecture Rules:
- MUST: the iOS app root must contain only SwiftUI UI, `NavigationStack`, platform integrations, and a thin Swift/KMP bridge.
- MUST: shared business logic must come from `app-shared`, not be rewritten in Swift.
- MUST: Swift-side adapters must remain thin and be responsible only for presentation, lifecycle, and Apple frameworks.
- MUST: navigation decisions must not be made directly inside SwiftUI views as part of business logic.
- MUST: UI Kit components must use the `UiKit` prefix and be aligned in naming with Android equivalents.

Code Review Focus:
- MUST: do not allow duplication of shared use cases and state machines in Swift code.
- MUST: do not build a parallel Swift-side service graph if the dependency should already live in the shared Koin graph.
- MUST: do not mix navigation state and domain/business state.
- IMPORTANT: bindings and UI state must have an explicit source of truth.
- IMPORTANT: bridge APIs must be convenient for SKIE and must not drag unnecessary complexity from Kotlin internals into Swift.

External Best Practices:
- IMPORTANT: SwiftUI recommends driving UI changes through state and bindings and keeping an explicit source of truth for state.
- IMPORTANT: `NavigationStack` should be used as a platform navigation container, not as a place for domain logic.

Sources:
- Local: `mobile-apps/ios-app/AGENTS.md`, `mobile-apps/AGENTS.md`
- Official: https://developer.apple.com/tutorials/swiftui-concepts/driving-changes-in-your-ui-with-state-and-bindings
- Official: https://developer.apple.com/documentation/swiftui/navigationstack
- Official: https://skie.touchlab.co/features/
