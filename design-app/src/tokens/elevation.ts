// Material Design 3 Elevation System
// 6 levels (0–5) replacing the old per-theme card/button/fab shadows.
//
// MD3 elevation has two components:
//   1. box-shadow — the physical drop shadow (same for light and dark)
//   2. tonal overlay — primary color tint applied to surface in dark mode
//      (handled separately via tonalOverlay in colors.ts)
//
// Shadow values are CSS box-shadow strings approximating MD3 key+ambient light model:
//   Key light:     stronger, more directional (larger y-offset, less blur)
//   Ambient light: softer, omnidirectional (smaller y-offset, more spread)
//
// Cross-platform mapping:
//   Android Compose:
//     Surface(shadowElevation = X.dp, tonalElevation = X.dp)
//     Tonal overlay is automatic — no manual color mixing needed.
//     Level → dp: level1=1dp, level2=3dp, level3=6dp, level4=8dp, level5=12dp
//
//   iOS SwiftUI:
//     No tonal overlay system — shadows only.
//     .shadow(color: .black.opacity(0.15), radius: R, x: 0, y: Y)
//     Ambient layer ignored; use key light values only.
//     iOS shadow per level:
//       level1: .shadow(color: .black.opacity(0.15), radius: 2,  x: 0, y: 1)
//       level2: .shadow(color: .black.opacity(0.15), radius: 4,  x: 0, y: 2)
//       level3: .shadow(color: .black.opacity(0.15), radius: 8,  x: 0, y: 4)
//       level4: .shadow(color: .black.opacity(0.15), radius: 10, x: 0, y: 6)
//       level5: .shadow(color: .black.opacity(0.15), radius: 12, x: 0, y: 8)

export interface MD3Elevation {
  level0: string;
  level1: string;
  level2: string;
  level3: string;
  level4: string;
  level5: string;
}

// MD3 standard elevation shadows (theme-independent — shadow color is always #000)
export const elevation: MD3Elevation = {
  level0: 'none',
  // 1dp — cards at rest, list items
  level1: '0px 1px 2px rgba(0,0,0,0.3), 0px 1px 3px 1px rgba(0,0,0,0.15)',
  // 3dp — filled cards hovered, navigation drawer
  level2: '0px 1px 2px rgba(0,0,0,0.3), 0px 2px 6px 2px rgba(0,0,0,0.15)',
  // 6dp — FAB, dialogs, menus
  level3: '0px 4px 8px 3px rgba(0,0,0,0.15), 0px 1px 3px rgba(0,0,0,0.3)',
  // 8dp — modal bottom sheets
  level4: '0px 6px 10px 4px rgba(0,0,0,0.15), 0px 2px 3px rgba(0,0,0,0.3)',
  // 12dp — navigation bar (top app bar scrolled)
  level5: '0px 8px 12px 6px rgba(0,0,0,0.15), 0px 4px 4px rgba(0,0,0,0.3)',
};

// Component → elevation level mapping
// | Component         | Level  | dp  | Rationale                                    |
// |-------------------|--------|-----|----------------------------------------------|
// | DsAlarmCard       | level1 | 1dp | Filled card at rest                          |
// | DsSleepCard       | level1 | 1dp | Filled card at rest                          |
// | DsSleepTimerCard  | level1 | 1dp | Filled card at rest                          |
// | DsPremiumCard     | level2 | 3dp | Highlighted/featured card                    |
// | DsSettingRow      | level0 | 0dp | Outlined card — no shadow per MD3 spec       |
// | DsPrimaryButton   | level0 | 0dp | MD3 filled buttons have no shadow            |
// | DsFab             | level3 | 6dp | MD3 FAB default elevation                    |
// | DsBottomNav       | level3 | 6dp | Floating Liquid Glass bar                    |

// Ringing screen overlays — brand-specific fixed values, not part of MD3 elevation.
// These are kept here for colocation with shadow-related tokens.
// iOS: use as-is (glass blur + shadow combination is valid iOS UI).
// Android: glass effect not supported; use filled tonal button instead (no shadow needed).
export const ringingElevation = {
  glass: '0 4px 24px rgba(0,0,0,0.2)',
  stop:  '0 4px 24px rgba(255,184,107,0.5)',
  text:  '0 2px 16px rgba(0,0,0,0.3)',
} as const;

// Legacy shadows export — keeps existing consumers compiling during migration.
// Will be removed after createAppTheme.ts is updated in step 6.
/** @deprecated Use elevation instead */
export const shadows = {
  morning: {
    card:   elevation.level1,
    button: elevation.level0,  // MD3 filled buttons have no shadow
    fab:    elevation.level3,
  },
  night: {
    card:   elevation.level1,
    button: elevation.level0,
    fab:    elevation.level3,
  },
  ringing: ringingElevation,
} as const;
