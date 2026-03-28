// Material Design 3 Color System
// Full 30-role ColorScheme for Morning (light) and Night (dark) themes.
// Derived from brand source colors using MD3 HCT tonal palette algorithm:
//   Morning source: #FFB86B (warm orange, Hue ~30)
//   Night source:   #7C8CFF (blue-violet, Hue ~250)
//
// Tonal ladder used: primary=T40(light)/T80(dark), container=T90(light)/T30(dark),
// onPrimary=T100/T20, onContainer=T10/T90 — per MD3 color spec.
//
// Cross-platform mapping:
//   Android Compose: MaterialTheme.colorScheme.*
//   iOS SwiftUI:     Custom ColorScheme struct injected via Environment

export interface MD3ColorScheme {
  // Primary
  primary: string;
  onPrimary: string;
  primaryContainer: string;
  onPrimaryContainer: string;
  // Secondary
  secondary: string;
  onSecondary: string;
  secondaryContainer: string;
  onSecondaryContainer: string;
  // Tertiary
  tertiary: string;
  onTertiary: string;
  tertiaryContainer: string;
  onTertiaryContainer: string;
  // Error
  error: string;
  onError: string;
  errorContainer: string;
  onErrorContainer: string;
  // Background / Surface
  background: string;
  onBackground: string;
  surface: string;
  onSurface: string;
  surfaceVariant: string;
  onSurfaceVariant: string;
  // Utility
  outline: string;
  outlineVariant: string;
  shadow: string;
  scrim: string;
  // Inverse
  inverseSurface: string;
  inverseOnSurface: string;
  inversePrimary: string;
  // Tonal
  surfaceTint: string;
}

// Morning — light scheme (Calm Minimal palette)
// Muted bronze/sand accent, warm cream surfaces, soft and premium feel.
const morning: MD3ColorScheme = {
  // Primary: muted bronze
  primary:              '#C7975D',
  onPrimary:            '#FFFFFF',
  primaryContainer:     '#EED9C1', // soft sand
  onPrimaryContainer:   '#4A4036',

  // Secondary: warm brown
  secondary:            '#6E5740',
  onSecondary:          '#FFFFFF',
  secondaryContainer:   '#EADCCB', // warm beige
  onSecondaryContainer: '#6E5740',

  // Tertiary: muted sage green (kept for compatibility)
  tertiary:             '#51643F',
  onTertiary:           '#FFFFFF',
  tertiaryContainer:    '#D4EABB',
  onTertiaryContainer:  '#102004',

  // Error: MD3 standard
  error:                '#BA1A1A',
  onError:              '#FFFFFF',
  errorContainer:       '#FFDAD6',
  onErrorContainer:     '#410002',

  // Background / Surface — warm cream
  background:           '#F5F1EC',
  onBackground:         '#3F3933',
  surface:              '#FBF7F2',
  onSurface:            '#3F3933',
  surfaceVariant:       '#F2ECE6',
  onSurfaceVariant:     '#6D655D',

  // Utility
  outline:              '#A89F96',
  outlineVariant:       '#E6DDD4',
  shadow:               '#000000',
  scrim:                '#000000',

  // Inverse (used for snackbars, tooltips)
  inverseSurface:       '#352F2A',
  inverseOnSurface:     '#FAF0E8',
  inversePrimary:       '#FFB86B',

  // Tonal surface tint = primary
  surfaceTint:          '#C7975D',
};

// Night — dark scheme
// Source primary: blue-violet (#7C8CFF → T80 for dark mode)
const night: MD3ColorScheme = {
  // Primary: blue-violet tonal family
  primary:              '#C5C3FF', // T80
  onPrimary:            '#26257C', // T20
  primaryContainer:     '#3E3E93', // T30 — deep violet
  onPrimaryContainer:   '#E2E0FF', // T90 — near-white violet

  // Secondary: muted violet-grey
  secondary:            '#C8BFEE', // T80
  onSecondary:          '#302963', // T20
  secondaryContainer:   '#47407A', // T30
  onSecondaryContainer: '#E5DEFF', // T90

  // Tertiary: blue-cyan sky accent
  tertiary:             '#B0CBEA', // T80
  onTertiary:           '#153349', // T20
  tertiaryContainer:    '#2C4A61', // T30
  onTertiaryContainer:  '#CCE5FF', // T90

  // Error: MD3 standard dark
  error:                '#FFB4AB', // T80
  onError:              '#690005', // T20
  errorContainer:       '#93000A', // T30
  onErrorContainer:     '#FFDAD6', // T90

  // Background / Surface (brand dark backgrounds preserved)
  background:           '#0B1020', // T6 — brand dark background
  onBackground:         '#E5E2E0',
  surface:              '#151C33', // T12 — brand dark surface
  onSurface:            '#E5E2E0',
  surfaceVariant:       '#1C2540', // slightly elevated variant
  onSurfaceVariant:     '#CBC4BC', // T80 neutral-variant

  // Utility
  outline:              '#958E86', // T60 neutral-variant
  outlineVariant:       '#50453A', // T30 neutral-variant
  shadow:               '#000000',
  scrim:                '#000000',

  // Inverse (used for snackbars, tooltips)
  inverseSurface:       '#E5E2E0', // T90 — near-white
  inverseOnSurface:     '#322F2D', // T20
  inversePrimary:       '#5C5B9E', // T40

  // Tonal surface tint = primary
  surfaceTint:          '#C5C3FF',
};

// MD3 tonal surface elevation overlays (dark mode only)
// In dark mode, elevated surfaces receive a primary color tint.
// Android Compose: handled automatically by Surface(tonalElevation = X.dp)
// iOS SwiftUI: not applicable — use shadow-only elevation
// Web prototype: apply manually via surfaceTint + opacity
export const tonalOverlay = {
  level0: 0,    //  0% primary overlay
  level1: 0.05, //  5%
  level2: 0.08, //  8%
  level3: 0.11, // 11%
  level4: 0.12, // 12%
  level5: 0.14, // 14%
} as const;

export const colors = { morning, night } as const;

// Calm Minimal vertical gradient for morning theme background
export const morningGradient = 'linear-gradient(180deg, #F5F1EC 0%, #F2ECE6 50%, #EEE7DF 100%)';

// Ringing screen — fixed white-on-image overlay palette (not part of MD3 scheme)
// Used only on the RingingScreen which always renders over a full-bleed photo.
export const ringingColors = {
  text:             '#FFFFFF',
  textMuted:        'rgba(255,255,255,0.9)',
  textSubtle:       'rgba(255,255,255,0.7)',
  textHint:         'rgba(255,255,255,0.5)',
  glassBg:          'rgba(255,255,255,0.2)',
  glassBgHover:     'rgba(255,255,255,0.3)',
  glassBorder:      'rgba(255,255,255,0.25)',
  overlayGradient:  'linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.5) 100%)',
  // Stop button uses morning.inversePrimary — brand orange guaranteed to contrast on dark overlay
  stopBg:           '#FFB86B',
  stopBgHover:      '#FFC97E',
  stopText:         '#2B2B2B',
} as const;

export type ThemeVariant = 'morning' | 'night';
