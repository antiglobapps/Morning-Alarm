---
name: kotlin-multiplatform
description: Help decide what belongs in commonMain, androidMain, iosMain, or expect/actual in Morning Alarm. Use for KMP abstraction, source set placement, dependency placement, and Swift/Android interoperability questions; do not use for purely platform UI implementation.
---

# Kotlin Multiplatform: Platform Abstraction Decisions

Expert guidance for KMP architecture in Morning Alarm.

## Decision Tree

Central question: should this code be reused across platforms?

```text
Q: Is it used by 2+ platforms?
├─ NO  -> Keep platform-specific
└─ YES -> Continue

Q: Is it pure Kotlin?
├─ YES -> commonMain
└─ NO  -> Continue

Q: Does behavior vary by platform?
├─ YES -> expect/actual
└─ UI-heavy or lifecycle-heavy -> Keep platform-specific
```

## Source Set Hierarchy

- `commonMain`: business logic, models, ViewModels, repository interfaces
- `androidMain`: Android platform implementations
- `iosMain`: iOS platform implementations

## What to Abstract

### Always share
- Alarm models
- ViewModels and state
- Repository interfaces
- Time calculations and business rules
- DTOs in `shared`

### Share via expect/actual
- Alarm scheduling
- Audio playback
- Vibration
- Local storage
- Notifications
- Background tasks

### Keep platform-specific
- Navigation
- Screen layouts
- UI Kit components
- App lifecycle code
- Permission flows
- Theme implementations

## Common Pitfalls

### Over-abstraction
```kotlin
expect fun NavigationComponent(...)
```

### Under-sharing
```kotlin
fun calculateNextAlarmTime(alarm: Alarm): Long { ... }
```

### Platform code in commonMain
```kotlin
import android.app.AlarmManager
```

### Premature abstraction
```kotlin
expect fun showNotification(...)
```

## Quick Reference

| Code type | Recommended location |
|-----------|----------------------|
| Alarm data models | `commonMain` |
| ViewModel + state | `commonMain` |
| Repository interfaces | `commonMain` |
| Time calculations | `commonMain` |
| Alarm scheduling | `expect/actual` |
| Audio playback | `expect/actual` |
| Screen layouts | Platform-specific |
| Navigation | Platform-specific |
| UI Kit | Platform-specific |
