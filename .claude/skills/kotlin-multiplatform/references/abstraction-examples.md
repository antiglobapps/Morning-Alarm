# Abstraction Examples for Morning Alarm

Real examples of abstraction decisions with rationale specific to the alarm app domain.

## Good Abstractions (commonMain)

### 1. Alarm Model — Pure Data
```kotlin
// app-shared/src/commonMain/.../model/Alarm.kt
@Serializable
data class Alarm(
    val id: String,
    val time: AlarmTime,
    val repeatDays: Set<DayOfWeek>,
    val melodyId: String,
    val sceneId: String,
    val snoozeMinutes: Int,
    val isEnabled: Boolean,
    val label: String
)

data class AlarmTime(val hour: Int, val minute: Int)
```
**Why commonMain:** Pure data, no platform APIs, used everywhere.

### 2. AlarmListViewModel — Shared State
```kotlin
// app-shared/src/commonMain/.../feature/alarm/list/AlarmListViewModel.kt
class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
) : BaseViewModel<ViewState, SideEffect>() {
    override val container = viewModelScope.container<ViewState, SideEffect>(
        initialState = ViewState.Loading,
        onCreate = { loadAlarms() }
    )

    fun toggleAlarm(alarmId: String) = intent {
        alarmRepository.toggleAlarm(alarmId)
        loadAlarms()
    }
}
```
**Why commonMain:** ViewModel state + business logic, no platform dependency.

### 3. Next Alarm Calculator — Pure Business Logic
```kotlin
// app-shared/src/commonMain/.../domain/NextAlarmCalculator.kt
fun calculateNextAlarmTime(alarm: Alarm, now: LocalDateTime): LocalDateTime {
    // Pure calculation logic — works everywhere
}
```
**Why commonMain:** Math + date logic, fully platform-agnostic.

---

## Good Abstractions (expect/actual)

### 4. AlarmScheduler — Platform Alarm APIs
```kotlin
// commonMain
expect class AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarmId: String)
}

// androidMain — uses AlarmManager
actual class AlarmScheduler(private val context: Context) {
    actual fun schedule(alarm: Alarm) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        // Set exact alarm with AlarmManager
    }
}

// iosMain — uses UNUserNotificationCenter
actual class AlarmScheduler {
    actual fun schedule(alarm: Alarm) {
        let center = UNUserNotificationCenter.current()
        // Schedule local notification trigger
    }
}
```
**Why expect/actual:** Both platforms need alarm scheduling, APIs fundamentally different.

### 5. AudioPlayer — Platform Media APIs
```kotlin
// commonMain
expect class AudioPlayer {
    fun play(resourceId: String, loop: Boolean = false)
    fun stop()
    fun setVolume(volume: Float)
}
```
**Why expect/actual:** Melody playback critical for both, MediaPlayer vs AVAudioPlayer.

### 6. VibrationService — Platform Haptics
```kotlin
// commonMain
expect class VibrationService {
    fun vibrate(pattern: VibrationPattern)
    fun cancel()
}
```
**Why expect/actual:** Alarm vibration needed on both, APIs incompatible.

---

## Platform-Specific (NOT abstracted)

### 7. AlarmListScreen — UI Layout
```kotlin
// Android: android/.../feature/alarm/list/AlarmListScreen.kt
@Composable
fun AlarmListScreen(viewModel: AlarmListViewModel, navigator: Navigator) {
    // Compose layout with LazyColumn, FAB, alarm cards
}

// iOS: ios/.../feature/alarm/list/AlarmListScreen.swift
struct AlarmListScreen: View {
    @ObservedObject var viewModel: AlarmListViewModel
    // SwiftUI layout with List, floating button
}
```
**Why platform-specific:** Compose vs SwiftUI paradigms, different navigation, different themes.

### 8. RingingScreen — Fullscreen Alarm Experience
**Why platform-specific:** This is THE "wow" screen. Each platform needs native fullscreen handling, lock screen display, audio routing, gesture handling. Abstraction would hurt the UX.

### 9. Theme System
```kotlin
// Android: MaterialTheme with MorningColors / NightColors
// iOS: SwiftUI Environment with custom ColorScheme
```
**Why platform-specific:** Theme APIs are fundamentally different between Compose and SwiftUI.
