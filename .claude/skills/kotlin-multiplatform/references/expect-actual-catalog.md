# Planned expect/actual Catalog for Morning Alarm

Expected platform abstractions needed for the alarm app.

| # | Name | Type | Purpose | Why Abstracted |
|---|------|------|---------|----------------|
| 1 | AlarmScheduler | class | Schedule/cancel OS-level alarms | AlarmManager vs UNUserNotificationCenter |
| 2 | AudioPlayer | class | Play alarm melodies and sleep sounds | MediaPlayer vs AVAudioPlayer |
| 3 | VibrationService | class | Alarm vibration patterns | Vibrator vs UINotificationFeedbackGenerator |
| 4 | NotificationService | class | Show alarm notifications | NotificationCompat vs UNNotification |
| 5 | LocalStorage | class | Persist alarm settings locally | SharedPreferences/DataStore vs UserDefaults |
| 6 | BackgroundTaskScheduler | class | Schedule background work | WorkManager vs BGTaskScheduler |
| 7 | ScreenWakeService | class | Wake screen for alarm display | PowerManager vs UIApplication |
| 8 | FullscreenService | class | Show alarm over lock screen | FLAG_SHOW_WHEN_LOCKED vs background notification |
| 9 | Platform | functions | platform(), deviceInfo() | Platform name and device info |
| 10 | Log | object | Logging | android.util.Log vs OSLog |
| 11 | PermissionChecker | class | Check alarm/notification permissions | Android runtime vs iOS authorization |
| 12 | ThemeDetector | object | Detect system dark/light mode | Configuration vs UITraitCollection |

## Implementation Priority

### MVP (Phase 1)
1. AlarmScheduler — core alarm functionality
2. AudioPlayer — melody playback
3. VibrationService — alarm vibration
4. NotificationService — alarm notifications
5. LocalStorage — persist alarms
6. ScreenWakeService — show alarm on lock screen
7. Log — debugging

### Phase 2
8. BackgroundTaskScheduler — sleep timer, pre-alarm
9. FullscreenService — enhanced lock screen experience
10. PermissionChecker — graceful permission handling
11. ThemeDetector — auto theme switching
12. Platform — analytics, diagnostics
