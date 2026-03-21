import { createTheme } from '@mui/material/styles';
import {
  colors,
  typeScale,
  fontFamilies,
  shape,
  elevation,
  type ThemeVariant,
} from '../tokens';


export default function createAppTheme(variant: ThemeVariant) {
  const c = colors[variant];

  return createTheme({
    // ── Palette — full MD3 ColorScheme ──────────────────────────────────
    palette: {
      mode: variant === 'morning' ? 'light' : 'dark',

      primary: {
        main:         c.primary,
        contrastText: c.onPrimary,
        container:    c.primaryContainer,
        onContainer:  c.onPrimaryContainer,
      },
      secondary: {
        main:         c.secondary,
        contrastText: c.onSecondary,
        container:    c.secondaryContainer,
        onContainer:  c.onSecondaryContainer,
      },
      error: {
        main:         c.error,
        contrastText: c.onError,
        container:    c.errorContainer,
        onContainer:  c.onErrorContainer,
      },
      background: {
        default: c.background,
        paper:   c.surface,
      },
      text: {
        primary:   c.onBackground,
        secondary: c.onSurfaceVariant,
      },
      divider: c.outlineVariant,

      // MD3 surface / utility roles (via augmentation)
      surfaceVariant:   c.surfaceVariant,
      onSurface:        c.onSurface,
      onSurfaceVariant: c.onSurfaceVariant,
      outline:          c.outline,
      outlineVariant:   c.outlineVariant,
      inverseSurface:   c.inverseSurface,
      inverseOnSurface: c.inverseOnSurface,
      inversePrimary:   c.inversePrimary,
      surfaceTint:      c.surfaceTint,
      scrim:            c.scrim,
    },

    // ── Shape — MD3 scale (medium = 12dp default) ────────────────────────
    // Individual component overrides are set in components section below.
    shape: {
      borderRadius: shape.medium,
    },

    // ── Typography — full MD3 type scale ────────────────────────────────
    typography: {
      fontFamily: fontFamilies.web,
      // Keep legacy MUI variants mapped to nearest MD3 role
      // so existing components don't break before they're migrated.
      h1:     { ...typeScale.displayMedium,  fontFamily: fontFamilies.web },
      h2:     { ...typeScale.headlineLarge,  fontFamily: fontFamilies.web },
      h3:     { ...typeScale.headlineMedium, fontFamily: fontFamilies.web },
      h4:     { ...typeScale.titleLarge,     fontFamily: fontFamilies.web },
      h5:     { ...typeScale.titleMedium,    fontFamily: fontFamilies.web },
      h6:     { ...typeScale.titleSmall,     fontFamily: fontFamilies.web },
      body1:  { ...typeScale.bodyLarge,      fontFamily: fontFamilies.web },
      body2:  { ...typeScale.bodyMedium,     fontFamily: fontFamilies.web },
      caption:{ ...typeScale.bodySmall,      fontFamily: fontFamilies.web },
      button: {
        ...typeScale.labelLarge,
        fontFamily:    fontFamilies.web,
        textTransform: 'none' as const,
      },
      // MD3 type scale variants (via augmentation)
      displayLarge:   { ...typeScale.displayLarge,   fontFamily: fontFamilies.web },
      displayMedium:  { ...typeScale.displayMedium,  fontFamily: fontFamilies.web },
      displaySmall:   { ...typeScale.displaySmall,   fontFamily: fontFamilies.web },
      headlineLarge:  { ...typeScale.headlineLarge,  fontFamily: fontFamilies.web },
      headlineMedium: { ...typeScale.headlineMedium, fontFamily: fontFamilies.web },
      headlineSmall:  { ...typeScale.headlineSmall,  fontFamily: fontFamilies.web },
      titleLarge:     { ...typeScale.titleLarge,     fontFamily: fontFamilies.web },
      titleMedium:    { ...typeScale.titleMedium,    fontFamily: fontFamilies.web },
      titleSmall:     { ...typeScale.titleSmall,     fontFamily: fontFamilies.web },
      bodyLarge:      { ...typeScale.bodyLarge,      fontFamily: fontFamilies.web },
      bodyMedium:     { ...typeScale.bodyMedium,     fontFamily: fontFamilies.web },
      bodySmall:      { ...typeScale.bodySmall,      fontFamily: fontFamilies.web },
      labelLarge:     { ...typeScale.labelLarge,     fontFamily: fontFamilies.web },
      labelMedium:    { ...typeScale.labelMedium,    fontFamily: fontFamilies.web },
      labelSmall:     { ...typeScale.labelSmall,     fontFamily: fontFamilies.web },
    },

    // ── Elevation — MD3 6-level shadow array ─────────────────────────────
    // MUI uses a 25-slot array (indices 0–24).
    // Map MD3 levels to the first 6 slots; fill rest with level5.
    shadows: [
      elevation.level0,   // 0
      elevation.level1,   // 1
      elevation.level2,   // 2
      elevation.level3,   // 3
      elevation.level4,   // 4
      elevation.level5,   // 5
      // slots 6–24: repeat level5
      ...Array(19).fill(elevation.level5),
    ] as any,

    // ── Component overrides ───────────────────────────────────────────────
    components: {

      // Card — MD3 Filled Card: medium shape (12dp), level1 elevation
      // Android: Card { shape = MaterialTheme.shapes.medium }
      // iOS:     RoundedRectangle(cornerRadius: 12, style: .continuous)
      MuiCard: {
        defaultProps: { elevation: 1 },
        styleOverrides: {
          root: {
            borderRadius:    shape.medium,
            // Disable MUI's dark-mode gradient hack (MD3 handles tonal via surfaceTint)
            backgroundImage: 'none',
          },
        },
      },

      // Button — MD3 Filled Button: pill shape, no shadow, labelLarge text
      // Android: FilledButton / Button (MD3 default)
      // iOS:     .borderedProminent ButtonStyle (tint = primary)
      MuiButton: {
        defaultProps:  { disableElevation: true },
        styleOverrides: {
          root: {
            borderRadius:  shape.full,
            textTransform: 'none',
            // MD3 filled button height = 40dp
            minHeight:     40,
            paddingLeft:   24,
            paddingRight:  24,
          },
          // Filled (contained) — primary / onPrimary
          containedPrimary: {
            backgroundColor: c.primary,
            color:           c.onPrimary,
            '&:hover': {
              // MD3 hover state = 8% onPrimary overlay on primary
              backgroundColor: c.primary,
              opacity:         0.92,
            },
            '&:disabled': {
              backgroundColor: `${c.onSurface}1F`, // 12% opacity
              color:           `${c.onSurface}61`, // 38% opacity
            },
          },
          // Tonal (contained secondary) — secondaryContainer / onSecondaryContainer
          // Android: FilledTonalButton
          // iOS: custom style with secondaryContainer background
          containedSecondary: {
            backgroundColor: c.secondaryContainer,
            color:           c.onSecondaryContainer,
            '&:hover': {
              backgroundColor: c.secondaryContainer,
              opacity:         0.92,
            },
          },
          // Outlined — outline border, primary text
          outlined: {
            borderColor: c.outline,
            color:       c.primary,
            '&:hover': {
              borderColor:     c.primary,
              backgroundColor: `${c.primary}14`, // 8% overlay
            },
          },
          // Text — primary text, no border/background
          text: {
            color: c.primary,
            '&:hover': {
              backgroundColor: `${c.primary}14`,
            },
          },
        },
      },

      // FAB — MD3 standard FAB: large shape (16dp), level3 elevation,
      //        primaryContainer / onPrimaryContainer colors
      // Android: FloatingActionButton (56dp, level3)
      // iOS: NOT a native HIG pattern — use toolbar button or bottom primary button
      MuiFab: {
        styleOverrides: {
          root: {
            borderRadius:    shape.large,
            width:           56,
            height:          56,
            boxShadow:       elevation.level3,
            backgroundColor: c.primaryContainer,
            color:           c.onPrimaryContainer,
            '&:hover': {
              boxShadow:       elevation.level4,
              backgroundColor: c.primaryContainer,
            },
            '&:active': {
              boxShadow: elevation.level3,
            },
          },
        },
      },

      // Chip — MD3 Filter/Assist Chip: small shape (8dp), 32dp height
      // Android: FilterChip / AssistChip
      // iOS: custom RoundedRectangle toggle button (no native chip component)
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: shape.small,
            height:       32,
          },
          // Filled chip = active/selected state → secondaryContainer
          filled: {
            backgroundColor: c.secondaryContainer,
            color:           c.onSecondaryContainer,
          },
          // Outlined chip = inactive state → outlineVariant border
          outlined: {
            borderColor: c.outlineVariant,
            color:       c.onSurfaceVariant,
          },
          // Label uses labelMedium (12sp, weight 500)
          label: {
            fontSize:      typeScale.labelMedium.fontSize,
            fontWeight:    typeScale.labelMedium.fontWeight,
            letterSpacing: typeScale.labelMedium.letterSpacing,
          },
        },
      },

      // Switch — MD3 Switch (52×32dp)
      // Off: surfaceVariant track + outline border, 16dp thumb (outline color)
      // On:  primary track (no border), 24dp thumb (onPrimary color)
      // Thumb sizing: fixed 24×24 box, scale(0.667) when off → visual 16px.
      // This preserves smooth CSS transition on toggle (scale animates well).
      //
      // Android Compose: Switch(checked, onCheckedChange) — MD3 out of the box
      // iOS SwiftUI:     Toggle("", isOn: $val).tint(Color(md3Scheme.primary))
      MuiSwitch: {
        styleOverrides: {
          root: {
            width:   52,
            height:  32,
            padding: 0,
          },
          switchBase: {
            padding: 4,
            '&.Mui-checked': {
              transform: 'translateX(20px)',
              '& .MuiSwitch-thumb': {
                backgroundColor: c.onPrimary,
                transform:       'scale(1)',
              },
              '& + .MuiSwitch-track': {
                backgroundColor: c.primary,
                borderColor:     'transparent',
                opacity:         1,
              },
            },
          },
          thumb: {
            width:           24,
            height:          24,
            backgroundColor: c.outline,
            boxShadow:       'none',
            transform:       'scale(0.667)', // 16/24 — visual 16dp when off
            transition:      'transform 200ms cubic-bezier(0.4, 0, 0.2, 1), background-color 200ms cubic-bezier(0.4, 0, 0.2, 1)',
          },
          track: {
            borderRadius:    shape.full,
            backgroundColor: c.surfaceVariant,
            border:          `2px solid ${c.outline}`,
            opacity:         1,
            transition:      'background-color 200ms cubic-bezier(0.4, 0, 0.2, 1), border-color 200ms cubic-bezier(0.4, 0, 0.2, 1)',
          },
        },
      },

      // Slider — MD3 Slider: primary active track, surfaceVariant inactive track
      // Android: Slider (MD3) — maps 1:1
      // iOS: Slider — use .tint(Color(md3Scheme.primary))
      MuiSlider: {
        styleOverrides: {
          rail:  { backgroundColor: c.surfaceVariant, opacity: 1 },
          track: { backgroundColor: c.primary, borderColor: c.primary },
          thumb: {
            backgroundColor: c.primary,
            '&:hover, &.Mui-focusVisible': {
              boxShadow: `0 0 0 8px ${c.primary}29`, // 16% ripple
            },
          },
        },
      },

      // Paper — disable MUI's dark-mode gradient elevation hack globally
      MuiPaper: {
        styleOverrides: {
          root: { backgroundImage: 'none' },
        },
      },
    },
  });
}
