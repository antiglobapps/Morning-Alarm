# Source Set Hierarchy in Morning Alarm

## Hierarchy Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       commonMain                             │
│  Pure Kotlin, no platform APIs                              │
│  Examples:                                                   │
│  - Alarm models (Alarm, AlarmTime, Scene, Melody)           │
│  - ViewModels (AlarmListViewModel, EditAlarmViewModel)      │
│  - Business logic (NextAlarmCalculator, SnoozeLogic)        │
│  - Repository interfaces (AlarmRepository, ContentRepo)     │
│  Dependencies: kotlin-stdlib, kotlinx-coroutines,           │
│  kotlinx-serialization, orbit-mvi, koin                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          │                         │
          ▼                         ▼
┌──────────────────┐    ┌───────────────────┐
│  androidMain     │    │     iosMain        │
│  Android APIs    │    │   iOS APIs         │
│  Examples:       │    │   Examples:        │
│  - AlarmManager  │    │   - UNNotification │
│  - MediaPlayer   │    │   - AVAudioPlayer  │
│  - WorkManager   │    │   - BGTaskScheduler│
│  - Vibrator      │    │   - UIFeedback     │
│  Dependencies:   │    │   Dependencies:    │
│  - AndroidX      │    │   - Platform libs  │
└──────────────────┘    └───┬───────────────┘
                            │
                            ├─→ iosArm64Main (device)
                            └─→ iosSimulatorArm64Main (simulator)
```

## Dependency Flow

```
Code in commonMain
  ↓ can use
Nothing platform-specific (only Kotlin stdlib + multiplatform libs)

Code in androidMain
  ↓ can use
commonMain + Android framework + AndroidX

Code in iosMain
  ↓ can use
commonMain + iOS platform APIs + Foundation
```

## Choosing the Right Source Set

```
Q: Where should this code go?

├─ Pure Kotlin? (no platform APIs)
│  └─ commonMain (Alarm models, ViewModels, business rules)
│
├─ Android API? (Context, AlarmManager, MediaPlayer)
│  └─ androidMain
│
└─ iOS API? (UNNotification, AVAudioPlayer, UIKit)
   └─ iosMain
```

## Module Dependency Map

```
shared (DTOs + API contracts)
  ↑ used by
app-shared (KMP business logic)
  ↑ used by
android (Compose UI) + ios (SwiftUI)

shared (DTOs + API contracts)
  ↑ used by
server (Ktor backend)
```
