---
name: code-review-kmp
description: Review Morning Alarm KMP code changes for source set placement, Orbit MVI architecture, naming consistency, UI kit conventions, duplication, and server boundary issues. Use for review, PR review, or "check my changes" tasks; do not use for feature implementation.
---

# KMP Code Review

Reviews Morning Alarm code changes for architecture compliance and best practices.

## What to Check

### 1. Source Set Placement
- Pure Kotlin logic must be in `commonMain`, not `androidMain`/`iosMain`
- Platform APIs must NOT be in `commonMain`
- ViewModels and state must be in `app-shared/commonMain`
- UI code must be in platform-specific modules (`android/`, `ios/`)

Red flags:
```kotlin
// Android import in commonMain
import android.content.Context

// Business logic in androidMain
// (should be in commonMain)
fun calculateNextAlarm(...) { ... }
```

### 2. Naming Conventions
- Same feature names across platforms: `AlarmListScreen.kt` and `AlarmListScreen.swift`
- Same ViewModel names on both platforms
- `UiKit` prefix for all UI components: `UiKitButton`, `UiKitAlarmCard`
- File name matches type name
- Feature paths mirrored: `feature/alarm/list/` on both platforms

### 3. Architecture Compliance
- ViewModels use Orbit MVI (`BaseViewModel<State, SideEffect>`)
- State is a sealed interface with `Loading`, `Data`, `Error` variants
- Side effects split into `ViewEffect` and `Navigation`
- Navigation via `Navigator` interface, not direct nav calls
- Repositories are interfaces in `commonMain`, implementations in platform-specific or server code

### 4. Theme Consistency
- No hardcoded colors; use theme tokens
- Morning theme colors: `#FFF9F2`, `#FFB86B`, `#7CC6FF`
- Night theme colors: `#0B1020`, `#7C8CFF`, `#9BD1FF`
- Components adapt to both themes
- Border radius stays in the 20-24dp range

### 5. Server Architecture
- Domain layer has no external dependencies
- Ports & adapters pattern is preserved
- DTOs live in `shared`, not only in `server`
- Error responses use `ApiError` DTO
- Request tracing preserves `X-Request-Id`

### 6. Code Duplication
- Check duplicated logic between `androidMain` and `iosMain`
- Check duplicated logic between `android/` and `ios/`
- If logic is pure Kotlin, question whether it belongs in `commonMain`

### 7. Dependency Injection
- Dependencies are injected via Koin
- No ad hoc service or repository instantiation
- ViewModel factories are registered in Koin modules

## Review Process

1. Get the list of changed files with `git diff --name-only`
2. Categorize files by module
3. Read each changed file
4. Check findings against the rules above
5. Report findings grouped by severity

## Output Format

```text
## Code Review Results

### Critical
- [file:line] Description of issue
  Fix: ...

### Warning
- [file:line] Description of issue
  Fix: ...

### Suggestion
- [file:line] Description of suggestion

### Good patterns noticed
- ...
```
