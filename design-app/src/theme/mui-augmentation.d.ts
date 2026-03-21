// MUI Theme TypeScript augmentation for Material Design 3
// Adds MD3-specific roles to Palette and Typography interfaces.

import '@mui/material/styles';
import '@mui/material/Typography';

declare module '@mui/material/styles' {
  // ── Palette: MD3 surface/utility roles ──
  interface Palette {
    surfaceVariant:    string;
    onSurface:         string;
    onSurfaceVariant:  string;
    outline:           string;
    outlineVariant:    string;
    inverseSurface:    string;
    inverseOnSurface:  string;
    inversePrimary:    string;
    surfaceTint:       string;
    scrim:             string;
  }
  interface PaletteOptions {
    surfaceVariant?:   string;
    onSurface?:        string;
    onSurfaceVariant?: string;
    outline?:          string;
    outlineVariant?:   string;
    inverseSurface?:   string;
    inverseOnSurface?: string;
    inversePrimary?:   string;
    surfaceTint?:      string;
    scrim?:            string;
  }

  // ── Palette: MD3 primary container roles (extend PaletteColor) ──
  interface PaletteColor {
    container?:   string;
    onContainer?: string;
  }
  interface SimplePaletteColorOptions {
    container?:   string;
    onContainer?: string;
  }

  // ── Typography: MD3 type scale variants ──
  interface TypographyVariants {
    displayLarge:   React.CSSProperties;
    displayMedium:  React.CSSProperties;
    displaySmall:   React.CSSProperties;
    headlineLarge:  React.CSSProperties;
    headlineMedium: React.CSSProperties;
    headlineSmall:  React.CSSProperties;
    titleLarge:     React.CSSProperties;
    titleMedium:    React.CSSProperties;
    titleSmall:     React.CSSProperties;
    bodyLarge:      React.CSSProperties;
    bodyMedium:     React.CSSProperties;
    bodySmall:      React.CSSProperties;
    labelLarge:     React.CSSProperties;
    labelMedium:    React.CSSProperties;
    labelSmall:     React.CSSProperties;
  }
  interface TypographyVariantsOptions {
    displayLarge?:   React.CSSProperties;
    displayMedium?:  React.CSSProperties;
    displaySmall?:   React.CSSProperties;
    headlineLarge?:  React.CSSProperties;
    headlineMedium?: React.CSSProperties;
    headlineSmall?:  React.CSSProperties;
    titleLarge?:     React.CSSProperties;
    titleMedium?:    React.CSSProperties;
    titleSmall?:     React.CSSProperties;
    bodyLarge?:      React.CSSProperties;
    bodyMedium?:     React.CSSProperties;
    bodySmall?:      React.CSSProperties;
    labelLarge?:     React.CSSProperties;
    labelMedium?:    React.CSSProperties;
    labelSmall?:     React.CSSProperties;
  }
}

// Allow MD3 typography variants in <Typography variant="...">
declare module '@mui/material/Typography' {
  interface TypographyPropsVariantOverrides {
    displayLarge:   true;
    displayMedium:  true;
    displaySmall:   true;
    headlineLarge:  true;
    headlineMedium: true;
    headlineSmall:  true;
    titleLarge:     true;
    titleMedium:    true;
    titleSmall:     true;
    bodyLarge:      true;
    bodyMedium:     true;
    bodySmall:      true;
    labelLarge:     true;
    labelMedium:    true;
    labelSmall:     true;
  }
}
