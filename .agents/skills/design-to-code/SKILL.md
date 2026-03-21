---
name: design-to-code
description: Convert a Morning Alarm UI description, wireframe, or screenshot into Android Jetpack Compose and/or iOS SwiftUI code using the project's Morning/Night themes, UiKit naming, and platform navigation conventions. Use for design implementation tasks, not for architecture review.
---

# Design to Code

Converts UI descriptions into Morning Alarm platform code.

## Theme System Reference

### Morning Theme (Soft Sunrise)
```kotlin
val MorningColors = lightColorScheme(
    background = Color(0xFFFFF9F2),
    surface = Color(0xFFFFFFFF),
    primary = Color(0xFFFFB86B),
    secondary = Color(0xFFFFD9A8),
    tertiary = Color(0xFF7CC6FF),
    onBackground = Color(0xFF2B2B2B),
    onSurface = Color(0xFF6F6F6F),
)
```

```swift
extension Color {
    static let morningBackground = Color(hex: "FFF9F2")
    static let morningSurface = Color(hex: "FFFFFF")
    static let morningPrimary = Color(hex: "FFB86B")
}
```

### Night Theme (Deep Blue Night)
```kotlin
val NightColors = darkColorScheme(
    background = Color(0xFF0B1020),
    surface = Color(0xFF151C33),
    primary = Color(0xFF7C8CFF),
    secondary = Color(0xFF4E5D94),
    tertiary = Color(0xFF9BD1FF),
    onBackground = Color(0xFFF5F7FF),
    onSurface = Color(0xFFA8B0C5),
)
```

### UI Component Style
- Border radius: 20-24dp
- Large rounded buttons
- Simple line or soft fill icons
- FAB "+" button
- Alarm cards with large time display
- On/off toggle and weekday chips
- Soft shadows in Morning, subtle elevation in Night
- Slow, smooth motion in Night mode

## Screen Templates

### Alarm List Screen
- Current time
- Greeting based on time of day
- Next alarm indicator
- Scrollable alarm cards
- FAB "+"

### Create/Edit Alarm Screen
- Time picker
- Repeat day chips
- Melody selector
- Scene/image selector
- Snooze duration selector
- Save button

### Sleep Library Screen
- Mood chips
- Featured meditation cards
- Ambient sound cards
- Sleep timer control
- Start Session button

### Ringing Screen
- Fullscreen background image
- Large centered time
- Scenario name
- Short motivational phrase
- Large Snooze and Stop buttons
- Subtle animation

### Profile/Premium Screen
- Theme selector
- Language setting
- Voice stop toggle
- Content packs
- Premium upgrade CTA

## Rules

1. Use `UiKit` components when available.
2. Never hardcode colors outside the theme system.
3. Use the correct theme context for the screen.
4. Generate both Android and iOS versions when the task asks for a shared UX outcome.
5. Use `collectAsStateWithLifecycle()` on Android.
6. Use `@ObservedObject` on iOS.
7. Keep platform-specific navigation patterns intact.
8. Preserve accessibility metadata on both platforms.
