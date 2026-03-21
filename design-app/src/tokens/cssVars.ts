import { colors, ringingColors, type ThemeVariant } from './colors';
import { typeScale, fontFamilies } from './typography';
import { shape } from './shape';
import { elevation } from './elevation';

// CSS custom property names for Material Design 3 tokens
// Naming: --ds-color-{md3-role-name} using kebab-case
export const cssVar = {
  // ── Primary ──
  colorPrimary:              '--ds-color-primary',
  colorOnPrimary:            '--ds-color-on-primary',
  colorPrimaryContainer:     '--ds-color-primary-container',
  colorOnPrimaryContainer:   '--ds-color-on-primary-container',
  // ── Secondary ──
  colorSecondary:            '--ds-color-secondary',
  colorOnSecondary:          '--ds-color-on-secondary',
  colorSecondaryContainer:   '--ds-color-secondary-container',
  colorOnSecondaryContainer: '--ds-color-on-secondary-container',
  // ── Tertiary ──
  colorTertiary:             '--ds-color-tertiary',
  colorOnTertiary:           '--ds-color-on-tertiary',
  colorTertiaryContainer:    '--ds-color-tertiary-container',
  colorOnTertiaryContainer:  '--ds-color-on-tertiary-container',
  // ── Error ──
  colorError:                '--ds-color-error',
  colorOnError:              '--ds-color-on-error',
  colorErrorContainer:       '--ds-color-error-container',
  colorOnErrorContainer:     '--ds-color-on-error-container',
  // ── Background / Surface ──
  colorBackground:           '--ds-color-background',
  colorOnBackground:         '--ds-color-on-background',
  colorSurface:              '--ds-color-surface',
  colorOnSurface:            '--ds-color-on-surface',
  colorSurfaceVariant:       '--ds-color-surface-variant',
  colorOnSurfaceVariant:     '--ds-color-on-surface-variant',
  // ── Utility ──
  colorOutline:              '--ds-color-outline',
  colorOutlineVariant:       '--ds-color-outline-variant',
  colorShadow:               '--ds-color-shadow',
  colorScrim:                '--ds-color-scrim',
  // ── Inverse ──
  colorInverseSurface:       '--ds-color-inverse-surface',
  colorInverseOnSurface:     '--ds-color-inverse-on-surface',
  colorInversePrimary:       '--ds-color-inverse-primary',
  // ── Tonal ──
  colorSurfaceTint:          '--ds-color-surface-tint',

  // ── Ringing (fixed overlay palette, not part of MD3 scheme) ──
  colorRingingText:          '--ds-color-ringing-text',
  colorRingingGlassBg:       '--ds-color-ringing-glass-bg',
  colorRingingGlassBgHover:  '--ds-color-ringing-glass-bg-hover',
  colorRingingGlassBorder:   '--ds-color-ringing-glass-border',
  colorRingingStopBg:        '--ds-color-ringing-stop-bg',
  colorRingingStopBgHover:   '--ds-color-ringing-stop-bg-hover',
  colorRingingStopText:      '--ds-color-ringing-stop-text',

  // ── Typography (MD3 type scale) ──
  fontFamily:                          '--ds-font-family',
  typescaleDisplayLargeSize:           '--ds-typescale-display-large-size',
  typescaleDisplayLargeWeight:         '--ds-typescale-display-large-weight',
  typescaleDisplayLargeLineHeight:     '--ds-typescale-display-large-line-height',
  typescaleDisplayMediumSize:          '--ds-typescale-display-medium-size',
  typescaleDisplayMediumWeight:        '--ds-typescale-display-medium-weight',
  typescaleDisplayMediumLineHeight:    '--ds-typescale-display-medium-line-height',
  typescaleDisplaySmallSize:           '--ds-typescale-display-small-size',
  typescaleDisplaySmallWeight:         '--ds-typescale-display-small-weight',
  typescaleDisplaySmallLineHeight:     '--ds-typescale-display-small-line-height',
  typescaleHeadlineLargeSize:          '--ds-typescale-headline-large-size',
  typescaleHeadlineLargeWeight:        '--ds-typescale-headline-large-weight',
  typescaleHeadlineLargeLineHeight:    '--ds-typescale-headline-large-line-height',
  typescaleHeadlineMediumSize:         '--ds-typescale-headline-medium-size',
  typescaleHeadlineMediumWeight:       '--ds-typescale-headline-medium-weight',
  typescaleHeadlineMediumLineHeight:   '--ds-typescale-headline-medium-line-height',
  typescaleHeadlineSmallSize:          '--ds-typescale-headline-small-size',
  typescaleHeadlineSmallWeight:        '--ds-typescale-headline-small-weight',
  typescaleHeadlineSmallLineHeight:    '--ds-typescale-headline-small-line-height',
  typescaleTitleLargeSize:             '--ds-typescale-title-large-size',
  typescaleTitleLargeWeight:           '--ds-typescale-title-large-weight',
  typescaleTitleLargeLineHeight:       '--ds-typescale-title-large-line-height',
  typescaleTitleMediumSize:            '--ds-typescale-title-medium-size',
  typescaleTitleMediumWeight:          '--ds-typescale-title-medium-weight',
  typescaleTitleMediumLineHeight:      '--ds-typescale-title-medium-line-height',
  typescaleTitleSmallSize:             '--ds-typescale-title-small-size',
  typescaleTitleSmallWeight:           '--ds-typescale-title-small-weight',
  typescaleTitleSmallLineHeight:       '--ds-typescale-title-small-line-height',
  typescaleBodyLargeSize:              '--ds-typescale-body-large-size',
  typescaleBodyLargeWeight:            '--ds-typescale-body-large-weight',
  typescaleBodyLargeLineHeight:        '--ds-typescale-body-large-line-height',
  typescaleBodyMediumSize:             '--ds-typescale-body-medium-size',
  typescaleBodyMediumWeight:           '--ds-typescale-body-medium-weight',
  typescaleBodyMediumLineHeight:       '--ds-typescale-body-medium-line-height',
  typescaleBodySmallSize:              '--ds-typescale-body-small-size',
  typescaleBodySmallWeight:            '--ds-typescale-body-small-weight',
  typescaleBodySmallLineHeight:        '--ds-typescale-body-small-line-height',
  typescaleLabelLargeSize:             '--ds-typescale-label-large-size',
  typescaleLabelLargeWeight:           '--ds-typescale-label-large-weight',
  typescaleLabelLargeLineHeight:       '--ds-typescale-label-large-line-height',
  typescaleLabelMediumSize:            '--ds-typescale-label-medium-size',
  typescaleLabelMediumWeight:          '--ds-typescale-label-medium-weight',
  typescaleLabelMediumLineHeight:      '--ds-typescale-label-medium-line-height',
  typescaleLabelSmallSize:             '--ds-typescale-label-small-size',
  typescaleLabelSmallWeight:           '--ds-typescale-label-small-weight',
  typescaleLabelSmallLineHeight:       '--ds-typescale-label-small-line-height',

  // ── Shape (MD3 shape scale) ──
  shapeNone:       '--ds-shape-none',
  shapeExtraSmall: '--ds-shape-extra-small',
  shapeSmall:      '--ds-shape-small',
  shapeMedium:     '--ds-shape-medium',
  shapeLarge:      '--ds-shape-large',
  shapeExtraLarge: '--ds-shape-extra-large',
  shapeFull:       '--ds-shape-full',

  // ── Elevation (MD3 levels 0–5) ──
  elevationLevel0: '--ds-elevation-level0',
  elevationLevel1: '--ds-elevation-level1',
  elevationLevel2: '--ds-elevation-level2',
  elevationLevel3: '--ds-elevation-level3',
  elevationLevel4: '--ds-elevation-level4',
  elevationLevel5: '--ds-elevation-level5',
} as const;

// Generate CSS variable values for a given theme variant
export function themeCssVars(variant: ThemeVariant): Record<string, string> {
  const c = colors[variant];
  const r = ringingColors;

  return {
    // Primary
    [cssVar.colorPrimary]:              c.primary,
    [cssVar.colorOnPrimary]:            c.onPrimary,
    [cssVar.colorPrimaryContainer]:     c.primaryContainer,
    [cssVar.colorOnPrimaryContainer]:   c.onPrimaryContainer,
    // Secondary
    [cssVar.colorSecondary]:            c.secondary,
    [cssVar.colorOnSecondary]:          c.onSecondary,
    [cssVar.colorSecondaryContainer]:   c.secondaryContainer,
    [cssVar.colorOnSecondaryContainer]: c.onSecondaryContainer,
    // Tertiary
    [cssVar.colorTertiary]:             c.tertiary,
    [cssVar.colorOnTertiary]:           c.onTertiary,
    [cssVar.colorTertiaryContainer]:    c.tertiaryContainer,
    [cssVar.colorOnTertiaryContainer]:  c.onTertiaryContainer,
    // Error
    [cssVar.colorError]:                c.error,
    [cssVar.colorOnError]:              c.onError,
    [cssVar.colorErrorContainer]:       c.errorContainer,
    [cssVar.colorOnErrorContainer]:     c.onErrorContainer,
    // Background / Surface
    [cssVar.colorBackground]:           c.background,
    [cssVar.colorOnBackground]:         c.onBackground,
    [cssVar.colorSurface]:              c.surface,
    [cssVar.colorOnSurface]:            c.onSurface,
    [cssVar.colorSurfaceVariant]:       c.surfaceVariant,
    [cssVar.colorOnSurfaceVariant]:     c.onSurfaceVariant,
    // Utility
    [cssVar.colorOutline]:              c.outline,
    [cssVar.colorOutlineVariant]:       c.outlineVariant,
    [cssVar.colorShadow]:               c.shadow,
    [cssVar.colorScrim]:                c.scrim,
    // Inverse
    [cssVar.colorInverseSurface]:       c.inverseSurface,
    [cssVar.colorInverseOnSurface]:     c.inverseOnSurface,
    [cssVar.colorInversePrimary]:       c.inversePrimary,
    // Tonal
    [cssVar.colorSurfaceTint]:          c.surfaceTint,

    // Ringing
    [cssVar.colorRingingText]:         r.text,
    [cssVar.colorRingingGlassBg]:      r.glassBg,
    [cssVar.colorRingingGlassBgHover]: r.glassBgHover,
    [cssVar.colorRingingGlassBorder]:  r.glassBorder,
    [cssVar.colorRingingStopBg]:       r.stopBg,
    [cssVar.colorRingingStopBgHover]:  r.stopBgHover,
    [cssVar.colorRingingStopText]:     r.stopText,

    // Typography
    [cssVar.fontFamily]: fontFamilies.web,
    // Display
    [cssVar.typescaleDisplayLargeSize]:        typeScale.displayLarge.fontSize,
    [cssVar.typescaleDisplayLargeWeight]:      String(typeScale.displayLarge.fontWeight),
    [cssVar.typescaleDisplayLargeLineHeight]:  typeScale.displayLarge.lineHeight,
    [cssVar.typescaleDisplayMediumSize]:       typeScale.displayMedium.fontSize,
    [cssVar.typescaleDisplayMediumWeight]:     String(typeScale.displayMedium.fontWeight),
    [cssVar.typescaleDisplayMediumLineHeight]: typeScale.displayMedium.lineHeight,
    [cssVar.typescaleDisplaySmallSize]:        typeScale.displaySmall.fontSize,
    [cssVar.typescaleDisplaySmallWeight]:      String(typeScale.displaySmall.fontWeight),
    [cssVar.typescaleDisplaySmallLineHeight]:  typeScale.displaySmall.lineHeight,
    // Headline
    [cssVar.typescaleHeadlineLargeSize]:        typeScale.headlineLarge.fontSize,
    [cssVar.typescaleHeadlineLargeWeight]:      String(typeScale.headlineLarge.fontWeight),
    [cssVar.typescaleHeadlineLargeLineHeight]:  typeScale.headlineLarge.lineHeight,
    [cssVar.typescaleHeadlineMediumSize]:       typeScale.headlineMedium.fontSize,
    [cssVar.typescaleHeadlineMediumWeight]:     String(typeScale.headlineMedium.fontWeight),
    [cssVar.typescaleHeadlineMediumLineHeight]: typeScale.headlineMedium.lineHeight,
    [cssVar.typescaleHeadlineSmallSize]:        typeScale.headlineSmall.fontSize,
    [cssVar.typescaleHeadlineSmallWeight]:      String(typeScale.headlineSmall.fontWeight),
    [cssVar.typescaleHeadlineSmallLineHeight]:  typeScale.headlineSmall.lineHeight,
    // Title
    [cssVar.typescaleTitleLargeSize]:        typeScale.titleLarge.fontSize,
    [cssVar.typescaleTitleLargeWeight]:      String(typeScale.titleLarge.fontWeight),
    [cssVar.typescaleTitleLargeLineHeight]:  typeScale.titleLarge.lineHeight,
    [cssVar.typescaleTitleMediumSize]:       typeScale.titleMedium.fontSize,
    [cssVar.typescaleTitleMediumWeight]:     String(typeScale.titleMedium.fontWeight),
    [cssVar.typescaleTitleMediumLineHeight]: typeScale.titleMedium.lineHeight,
    [cssVar.typescaleTitleSmallSize]:        typeScale.titleSmall.fontSize,
    [cssVar.typescaleTitleSmallWeight]:      String(typeScale.titleSmall.fontWeight),
    [cssVar.typescaleTitleSmallLineHeight]:  typeScale.titleSmall.lineHeight,
    // Body
    [cssVar.typescaleBodyLargeSize]:        typeScale.bodyLarge.fontSize,
    [cssVar.typescaleBodyLargeWeight]:      String(typeScale.bodyLarge.fontWeight),
    [cssVar.typescaleBodyLargeLineHeight]:  typeScale.bodyLarge.lineHeight,
    [cssVar.typescaleBodyMediumSize]:       typeScale.bodyMedium.fontSize,
    [cssVar.typescaleBodyMediumWeight]:     String(typeScale.bodyMedium.fontWeight),
    [cssVar.typescaleBodyMediumLineHeight]: typeScale.bodyMedium.lineHeight,
    [cssVar.typescaleBodySmallSize]:        typeScale.bodySmall.fontSize,
    [cssVar.typescaleBodySmallWeight]:      String(typeScale.bodySmall.fontWeight),
    [cssVar.typescaleBodySmallLineHeight]:  typeScale.bodySmall.lineHeight,
    // Label
    [cssVar.typescaleLabelLargeSize]:        typeScale.labelLarge.fontSize,
    [cssVar.typescaleLabelLargeWeight]:      String(typeScale.labelLarge.fontWeight),
    [cssVar.typescaleLabelLargeLineHeight]:  typeScale.labelLarge.lineHeight,
    [cssVar.typescaleLabelMediumSize]:       typeScale.labelMedium.fontSize,
    [cssVar.typescaleLabelMediumWeight]:     String(typeScale.labelMedium.fontWeight),
    [cssVar.typescaleLabelMediumLineHeight]: typeScale.labelMedium.lineHeight,
    [cssVar.typescaleLabelSmallSize]:        typeScale.labelSmall.fontSize,
    [cssVar.typescaleLabelSmallWeight]:      String(typeScale.labelSmall.fontWeight),
    [cssVar.typescaleLabelSmallLineHeight]:  typeScale.labelSmall.lineHeight,

    // Elevation — MD3 levels 0–5
    [cssVar.elevationLevel0]: elevation.level0,
    [cssVar.elevationLevel1]: elevation.level1,
    [cssVar.elevationLevel2]: elevation.level2,
    [cssVar.elevationLevel3]: elevation.level3,
    [cssVar.elevationLevel4]: elevation.level4,
    [cssVar.elevationLevel5]: elevation.level5,

    // Shape — MD3 scale
    [cssVar.shapeNone]:       `${shape.none}px`,
    [cssVar.shapeExtraSmall]: `${shape.extraSmall}px`,
    [cssVar.shapeSmall]:      `${shape.small}px`,
    [cssVar.shapeMedium]:     `${shape.medium}px`,
    [cssVar.shapeLarge]:      `${shape.large}px`,
    [cssVar.shapeExtraLarge]: `${shape.extraLarge}px`,
    [cssVar.shapeFull]:       '9999px',
  };
}

// Apply all MD3 CSS variables to a DOM element (default: document.documentElement).
// Call this whenever the theme variant changes so CSS vars stay in sync with MUI theme.
//
// Usage in App.tsx:
//   useEffect(() => applyThemeCssVars(resolvedVariant), [resolvedVariant]);
//
// Cross-platform note: CSS variables are web-only.
//   Android Compose: tokens are consumed via MaterialTheme.colorScheme / .typography / .shapes
//   iOS SwiftUI:     tokens are consumed via an injected ColorScheme environment object
export function applyThemeCssVars(
  variant: ThemeVariant,
  element: HTMLElement = document.documentElement,
): void {
  const vars = themeCssVars(variant);
  Object.entries(vars).forEach(([key, value]) => {
    element.style.setProperty(key, value);
  });
}

// Shorthand: v(cssVar.colorPrimary) → 'var(--ds-color-primary)'
export function v(name: string): string {
  return `var(${name})`;
}
