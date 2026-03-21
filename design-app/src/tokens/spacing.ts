// Material Design 3 Spacing
// MD3 uses a 4dp baseline grid. MUI's default spacing unit is 8px,
// which is a 2× multiple of the MD3 grid — compatible and widely used.
//
// All preset values are MUI spacing multipliers (× 8px).
// Pixel equivalents are noted in comments.
//
// Cross-platform mapping:
//   Android Compose: Dp values — multiply by 1 (1dp = 1px at 1x density)
//     screenHorizontal → 20.dp horizontal padding
//     cardPadding      → 16.dp internal card padding
//   iOS SwiftUI: CGFloat points — same dp values apply
//     .padding(.horizontal, 20)
//     .padding(16) for card content

export const spacing = {
  // Base unit: 8px (MUI default, 2× MD3 4dp grid)
  base: 8,

  // ── Layout ──
  // Horizontal screen edge padding — MD3 recommends 16dp min, 24dp comfortable
  screenHorizontal: 2.5, // × 8 = 20px — above MD3 minimum, comfortable on mobile
  // Top screen padding (below navigation bar / status bar)
  screenTop:        4,   // × 8 = 32px
  // Gap between major sections on a screen
  sectionSpacing:   3,   // × 8 = 24px — MD3 standard section gap

  // ── Components ──
  // Internal padding for cards (MD3 Card uses 16dp)
  cardPadding:      2,   // × 8 = 16px
  // Gap between stacked cards / list items
  stackGap:         2,   // × 8 = 16px
  // Gap between day-of-week chips
  chipGap:          0.5, // × 8 = 4px — MD3 chip gap
  // Gap between mood chips (slightly wider for readability)
  moodChipGap:      1,   // × 8 = 8px
} as const;

// Legacy key aliases — backward compatibility for existing screens.
// Remove once all screens are updated to the new names.
/** @deprecated Use spacing.screenHorizontal */
export const screenPx      = spacing.screenHorizontal;
/** @deprecated Use spacing.screenTop */
export const screenPt      = spacing.screenTop;
/** @deprecated Use spacing.sectionSpacing */
export const sectionGap    = spacing.sectionSpacing;
