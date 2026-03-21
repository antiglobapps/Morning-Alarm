// Material Design 3 Shape Scale
// 7 steps replacing the old ad-hoc radii (base=20, card=24, button=24, chip=12, fab=20).
//
// MD3 shape scale uses dp units (1dp = 1px at 1x density).
// All values are circular arc corner radius unless noted.
//
// Cross-platform notes:
//   Android Compose: MaterialTheme.shapes.* — RoundedCornerShape(Xdp)
//     extraSmall  → MaterialTheme.shapes.extraSmall  (4.dp)
//     small       → MaterialTheme.shapes.small        (8.dp)
//     medium      → MaterialTheme.shapes.medium       (12.dp)
//     large       → MaterialTheme.shapes.large        (16.dp)
//     extraLarge  → MaterialTheme.shapes.extraLarge   (28.dp)
//     full        → CircleShape (50% radius)
//
//   iOS SwiftUI: RoundedRectangle(cornerRadius: X, style: .continuous)
//     IMPORTANT: Always use style: .continuous for the squircle (superellipse) curve.
//     This matches the iOS system aesthetic. Android uses circular arcs but the
//     visual difference is minimal at small radii.
//     full → Capsule() shape
//
//   Web prototype: standard border-radius (circular arc)
//     Does not simulate .continuous — acceptable for design reference.

export interface MD3ShapeScale {
  none:       number; //  0dp — dividers, full-width elements
  extraSmall: number; //  4dp — text fields, small chips
  small:      number; //  8dp — chips, input fields
  medium:     number; // 12dp — cards, dialogs, menus
  large:      number; // 16dp — navigation drawer, FAB
  extraLarge: number; // 28dp — large FAB, bottom sheets (top corners)
  full:       number; // 9999 — pill buttons, circular FAB
}

export const shape: MD3ShapeScale = {
  none:       0,
  extraSmall: 4,
  small:      8,
  medium:     12,
  large:      16,
  extraLarge: 28,
  full:       9999,
};

// Component → shape scale mapping
// | Component         | MD3 shape role | dp  | Rationale                              |
// |-------------------|---------------|-----|----------------------------------------|
// | DsPrimaryButton   | full          |9999 | MD3 filled buttons are pill-shaped     |
// | DsGlassButton     | full          |9999 | Pill shape for ringing screen actions  |
// | DsFab             | large         | 16  | MD3 standard FAB = large corner        |
// | DsAlarmCard       | medium        | 12  | MD3 filled card default                |
// | DsSleepCard       | medium        | 12  | MD3 filled card default                |
// | DsSleepTimerCard  | medium        | 12  | MD3 filled card default                |
// | DsPremiumCard     | medium        | 12  | MD3 filled card default                |
// | DsSettingRow      | medium        | 12  | MD3 outlined card                      |
// | DsDayChipRow      | small         |  8  | MD3 chip shape                         |
// | DsMoodChipRow     | small         |  8  | MD3 chip shape                         |
// | DsBottomNav       | none          |  0  | NavigationBar has no corner radius     |

// Legacy radii export — keeps existing consumers (createAppTheme, cssVars) compiling.
// Will be removed after createAppTheme.ts is updated in step 6.
/** @deprecated Use shape instead */
export const radii = {
  base:       shape.extraLarge, // was 20 → 28 (MD3 extraLarge, closest to old base usage)
  card:       shape.medium,     // was 24 → 12 (MD3 card spec)
  button:     shape.full,       // was 24 → 9999 (MD3 filled buttons are pill)
  chip:       shape.small,      // was 12 → 8 (MD3 chip spec)
  glass:      shape.full,       // was 24 → 9999 (ringing screen pill buttons)
  timePicker: shape.medium,     // was 12 → 12 (no change)
  fab:        shape.large,      // was 20 → 16 (MD3 standard FAB)
} as const;
