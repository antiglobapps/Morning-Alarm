---
name: kotlin-multiplatform
description: |
  Platform abstraction decision-making for Morning Alarm KMP project. Guides when to abstract vs keep platform-specific,
  source set placement (commonMain, platform-specific), expect/actual patterns. Covers primary targets
  (Android, iOS) with server considerations. Triggers on: abstraction decisions ("should I share this?"),
  source set placement questions, expect/actual creation, build.gradle.kts work, incorrect placement detection,
  KMP dependency suggestions.
---

# Kotlin Multiplatform: Platform Abstraction Decisions

Expert guidance for KMP architecture in Morning Alarm — deciding what to share vs keep platform-specific.

## When to Use This Skill

Making platform abstraction decisions:
- "Should I create expect/actual or keep Android-only?"
- "Can I share this ViewModel logic?"
- "Where does this code belong — commonMain, androidMain, iosMain?"
- "This uses Android Context — can it be abstracted?"
- "Is this code in the wrong module?"
- Detecting incorrect placements

## Abstraction Decision Tree

**Central question:** "Should this code be reused across platforms?"

```
Q: Is it used by 2+ platforms?
├─ NO  → Keep platform-specific
│         Example: Android-only alarm scheduling via AlarmManager
│
└─ YES → Continue ↓

Q: Is it pure Kotlin (no platform APIs)?
├─ YES → commonMain
│         Example: Alarm model, business rules, time calculations
│
└─ NO  → Continue ↓

Q: Does it vary by platform?
├─ YES → expect/actual
│  Example: AlarmScheduler (uses AlarmManager on Android, UNUserNotificationCenter on iOS)
│
└─ Complex/UI-related
   → Keep platform-specific
   Example: Navigation (Compose Navigation vs NavigationStack too different)

Final check:
Q: Maintenance cost of abstraction < duplication cost?
├─ YES → Proceed with abstraction
└─ NO  → Duplicate (simpler)
```

## Source Set Hierarchy for Morning Alarm

```
┌─────────────────────────────────────────────┐
│ commonMain = Contract (pure Kotlin)          │
│ - Business logic, alarm models, state        │
│ - ViewModels (Orbit MVI)                     │
│ - Repository interfaces                      │
│ - No platform APIs                           │
└────────────┬────────────────────────────────┘
             │
             ├────────────────────┐
             ▼                    ▼
   ┌───────────────────┐  ┌──────────────────┐
   │ androidMain        │  │ iosMain           │
   │ Android platform   │  │ iOS platform      │
   │ - AlarmManager     │  │ - UNNotification  │
   │ - MediaPlayer      │  │ - AVAudioPlayer   │
   │ - NotificationMgr  │  │ - BackgroundTask  │
   └───────────────────┘  └──────────────────┘
```

## What to Abstract vs Keep Platform-Specific

### Always Abstract (commonMain)
- **Alarm models** (data classes, enums)
- **ViewModels** (state management via Orbit MVI)
- **Repository interfaces** (data access contracts)
- **Business rules** (alarm time calculations, snooze logic)
- **DTOs** (shared with server via `shared` module)

### Abstract via expect/actual
- **Alarm scheduling** (AlarmManager vs UNUserNotificationCenter)
- **Audio playback** (MediaPlayer vs AVAudioPlayer)
- **Vibration** (Vibrator vs UINotificationFeedbackGenerator)
- **Local storage** (SharedPreferences vs UserDefaults)
- **Notifications** (NotificationCompat vs UNNotification)
- **Background tasks** (WorkManager vs BGTaskScheduler)

### Never Abstract
- **Navigation** (Compose Navigation vs NavigationStack)
- **Screen layouts** (Compose vs SwiftUI)
- **UI Kit components** (each platform has own implementation)
- **App lifecycle** (Activity vs UIApplicationDelegate)
- **Permissions** (Android runtime vs iOS Info.plist)
- **Theme system** (MaterialTheme vs SwiftUI Environment)

## Common Pitfalls

### 1. Over-Abstraction
```kotlin
// ❌ BAD — Navigation is too platform-specific
expect fun NavigationComponent(...)
```
**Fix:** Keep platform-specific, accept duplication

### 2. Under-Sharing
```kotlin
// ❌ BAD — duplicated alarm logic in Android and iOS
fun calculateNextAlarmTime(alarm: Alarm): Long { ... }
```
**Fix:** Move to commonMain (pure Kotlin)

### 3. Platform code in commonMain
```kotlin
// ❌ BAD — Android API in commonMain
import android.app.AlarmManager
```
**Fix:** Use expect/actual or dependency injection

### 4. Premature Abstraction
```kotlin
// ❌ BAD — only used on Android currently
expect fun showNotification(...)
```
**Fix:** Wait until iOS actually needs it, then abstract

## Quick Reference

| Code Type | Location | Reason |
|-----------|----------|--------|
| Alarm data models | commonMain | Pure Kotlin, works everywhere |
| ViewModel + state | commonMain | Orbit MVI, shared logic |
| Repository interfaces | commonMain | Contracts for data access |
| Time calculations | commonMain | Pure business logic |
| Alarm scheduling | expect/actual | OS-specific alarm APIs |
| Audio playback | expect/actual | OS-specific media APIs |
| Local notifications | expect/actual | Different notification systems |
| Screen layouts | Platform-specific | Compose vs SwiftUI |
| Navigation | Platform-specific | Fundamentally different paradigms |
| UI Kit | Platform-specific | Native feel required |

## See Also

- [references/abstraction-examples.md](references/abstraction-examples.md) - Morning Alarm specific abstraction examples
- [references/expect-actual-catalog.md](references/expect-actual-catalog.md) - Planned expect/actual pairs for Morning Alarm
- [references/source-set-hierarchy.md](references/source-set-hierarchy.md) - Visual source set diagram
