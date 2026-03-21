---
name: code-review-kmp
description: |
  Reviews code changes in the Morning Alarm KMP project. Checks for: correct source set placement,
  naming convention compliance, architecture violations, code duplication across platforms,
  theme consistency, proper expect/actual usage, Orbit MVI pattern compliance.
  Triggers on: "review code", "review my changes", "check code", "review PR".
user-invocable: true
allowed-tools: Read, Glob, Grep, Bash(git *)
---

# KMP Code Review

Reviews Morning Alarm code changes for architecture compliance and best practices.

## What to Check

### 1. Source Set Placement
- Pure Kotlin logic must be in `commonMain`, not `androidMain`/`iosMain`
- Platform APIs must NOT be in `commonMain`
- ViewModels and state must be in `app-shared/commonMain`
- UI code must be in platform-specific modules (`android/`, `ios/`)

**Red flags:**
```kotlin
// ❌ Android import in commonMain
import android.content.Context

// ❌ Business logic in androidMain
// (should be in commonMain)
fun calculateNextAlarm(...) { ... }
```

### 2. Naming Conventions
- Same feature names across platforms: `AlarmListScreen.kt` ↔ `AlarmListScreen.swift`
- Same ViewModel names: `AlarmListViewModel` on both platforms
- UiKit prefix for all UI components: `UiKitButton`, `UiKitAlarmCard`
- File name = type name
- Feature paths mirrored: `feature/alarm/list/` on both platforms

### 3. Architecture Compliance
- ViewModels use Orbit MVI (`BaseViewModel<State, SideEffect>`)
- State is a sealed interface with `Loading`, `Data`, `Error` variants
- Side effects split into `ViewEffect` and `Navigation`
- Navigation via `Navigator` interface, not direct nav calls
- Repositories are interfaces in `commonMain`, implementations in platform-specific or server

### 4. Theme Consistency
- No hardcoded colors — always use theme tokens
- Morning theme colors: `#FFF9F2`, `#FFB86B`, `#7CC6FF`
- Night theme colors: `#0B1020`, `#7C8CFF`, `#9BD1FF`
- Components adapt to both themes
- Border radius: 20–24dp consistently

### 5. Server Architecture
- Domain layer has no external dependencies
- Ports & adapters pattern respected
- DTOs in `shared` module (not server-local)
- Error responses use `ApiError` DTO
- Request ID tracing via `X-Request-Id`

### 6. Code Duplication
- Check for duplicated logic between `androidMain` and `iosMain`
- Check for duplicated logic between `android/` and `ios/`
- If duplicated → should it be in `commonMain`?

### 7. Dependency Injection
- All dependencies injected via Koin
- No manual instantiation of services/repositories
- ViewModel factories registered in Koin modules

## Review Process

1. Get the list of changed files: `git diff --name-only`
2. Categorize files by module (app-shared, android, ios, server, shared)
3. Read each changed file
4. Check against all rules above
5. Report findings grouped by severity:
   - 🔴 **Critical** — architecture violation, wrong source set, security issue
   - 🟡 **Warning** — naming mismatch, missing platform mirror, hardcoded values
   - 🟢 **Suggestion** — code style, potential improvement, documentation
6. Provide specific fix recommendations with code examples

## Output Format

```
## Code Review Results

### 🔴 Critical
- [file:line] Description of issue
  Fix: ...

### 🟡 Warning
- [file:line] Description of issue
  Fix: ...

### 🟢 Suggestion
- [file:line] Description of suggestion

### ✅ Good patterns noticed
- ...
```
