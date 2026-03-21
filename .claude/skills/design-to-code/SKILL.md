---
name: design-to-code
description: |
  Converts UI design descriptions or wireframes into platform-specific code for Morning Alarm.
  Generates Jetpack Compose (Android) and/or SwiftUI (iOS) code following the project's theme system
  (Morning/Night themes), UiKit component conventions, and design specifications.
  Triggers on: "implement design", "convert design", "build screen from", "design to code",
  "implement this UI", "create this screen".
user-invocable: true
argument-hint: "[screen-name or description]"
allowed-tools: Read, Write, Edit, Glob, Grep
---

# Design to Code

Converts UI descriptions into Morning Alarm platform code.

## Usage

`/design-to-code alarm list screen` — implements the alarm list screen
`/design-to-code ringing screen` — implements the alarm ringing fullscreen

## Theme System Reference

### Morning Theme (Soft Sunrise)
```kotlin
// Android (Compose)
val MorningColors = lightColorScheme(
    background = Color(0xFFFFF9F2),
    surface = Color(0xFFFFFFFF),
    primary = Color(0xFFFFB86B),
    secondary = Color(0xFFFFD9A8),
    tertiary = Color(0xFF7CC6FF),      // Accent
    onBackground = Color(0xFF2B2B2B),  // Text Primary
    onSurface = Color(0xFF6F6F6F),     // Text Secondary
)
```

```swift
// iOS (SwiftUI)
extension Color {
    static let morningBackground = Color(hex: "FFF9F2")
    static let morningSurface = Color(hex: "FFFFFF")
    static let morningPrimary = Color(hex: "FFB86B")
    // ...
}
```

### Night Theme (Deep Blue Night)
```kotlin
// Android (Compose)
val NightColors = darkColorScheme(
    background = Color(0xFF0B1020),
    surface = Color(0xFF151C33),
    primary = Color(0xFF7C8CFF),
    secondary = Color(0xFF4E5D94),
    tertiary = Color(0xFF9BD1FF),      // Accent
    onBackground = Color(0xFFF5F7FF),  // Text Primary
    onSurface = Color(0xFFA8B0C5),     // Text Secondary
)
```

### UI Component Style
- Border radius: 20–24dp
- Large rounded buttons
- Simple line / soft fill icons
- FAB "+" button (large, round)
- Alarm cards with large time display
- On/off toggle, weekday chips
- Cards with soft shadows (Morning) or subtle elevation (Night)
- Animations: slow and smooth in Night mode

## Screen Templates

### Alarm List Screen
- Current time (large)
- Greeting based on time of day
- Next alarm indicator
- Scrollable alarm cards (time, label, days, on/off toggle)
- FAB "+" button

### Create/Edit Alarm Screen
- Time picker (large, center)
- Repeat day chips (Mon–Sun)
- Melody selector (tap to open)
- Scene/image selector (tap to open)
- Snooze duration selector
- Save button

### Sleep Library Screen
- Mood chips (Calm, Deep Sleep, Rain)
- Featured meditation cards (image + title + duration)
- Ambient sound cards
- Sleep timer control
- Start Session button

### Ringing Screen (THE "wow" screen)
- Fullscreen background image (scene)
- Large time display (center)
- Scenario name
- Short motivational phrase
- Large "Snooze" button
- Large "Stop" button
- Subtle animation (light particles, clouds, glow)

### Profile/Premium Screen
- Theme selector (Morning / Night / Auto)
- Language setting
- Voice stop toggle (premium)
- Content packs
- Premium upgrade CTA

## Rules

1. Always use UiKit-prefixed components when available
2. Follow the project's theme system — never hardcode colors
3. Use the correct theme context (Morning vs Night)
4. Generate both Android (Compose) and iOS (SwiftUI) versions
5. Use `collectAsStateWithLifecycle()` on Android, `@ObservedObject` on iOS
6. Keep platform-specific navigation patterns
7. Use dp/sp units on Android, points on iOS
8. Ensure accessibility: contentDescription (Android), accessibilityLabel (iOS)
