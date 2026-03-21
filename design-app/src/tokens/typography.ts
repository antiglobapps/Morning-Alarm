// Material Design 3 Type Scale
// 15 named roles replacing the old h1–h4/body1/body2/button system.
//
// Values follow the MD3 spec (sp units → rem at 16px base):
//   fontSize:      sp / 16 → rem
//   lineHeight:    sp value (used as px-equivalent in MUI)
//   fontWeight:    MD3 standard weights
//   letterSpacing: sp → em approximation (1sp ≈ 0.0625rem at 16px base)
//
// Cross-platform font family:
//   Android:  Roboto (system default, no override needed)
//   iOS:      SF Pro (via -apple-system / Font.system() in SwiftUI)
//   Web:      Roboto via Google Fonts (current) or -apple-system on macOS/iOS browsers
//
// Android Compose mapping:
//   MaterialTheme.typography.displayLarge / headlineMedium / titleLarge / etc.
//
// iOS SwiftUI mapping:
//   Font.system(size: 57, weight: .regular) for displayLarge, etc.
//   Or use semantic fonts: Font.largeTitle, Font.title, Font.headline, Font.body, Font.caption

export interface MD3TypeStyle {
  fontSize: string;        // rem
  lineHeight: string;      // px (MUI uses px for lineHeight in theme)
  fontWeight: number;
  letterSpacing: string;   // rem/em
}

export interface MD3TypeScale {
  displayLarge:   MD3TypeStyle;
  displayMedium:  MD3TypeStyle;
  displaySmall:   MD3TypeStyle;
  headlineLarge:  MD3TypeStyle;
  headlineMedium: MD3TypeStyle;
  headlineSmall:  MD3TypeStyle;
  titleLarge:     MD3TypeStyle;
  titleMedium:    MD3TypeStyle;
  titleSmall:     MD3TypeStyle;
  bodyLarge:      MD3TypeStyle;
  bodyMedium:     MD3TypeStyle;
  bodySmall:      MD3TypeStyle;
  labelLarge:     MD3TypeStyle;
  labelMedium:    MD3TypeStyle;
  labelSmall:     MD3TypeStyle;
}

// Platform font families
// Android: Roboto is system default — no fontFamily override needed in Compose
// iOS: SF Pro is system default — use Font.system() in SwiftUI
// Web: Roboto loaded via index.css / Google Fonts
export const fontFamilies = {
  android: '"Roboto", "Helvetica", "Arial", sans-serif',
  // -apple-system resolves to SF Pro on iOS/macOS Safari, fallback to Roboto elsewhere
  ios:     '-apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif',
  web:     '"Roboto", "Helvetica", "Arial", sans-serif',
} as const;

// MD3 type scale — identical values for both themes (typography is theme-independent in MD3)
export const typeScale: MD3TypeScale = {
  // ── Display ── (large expressive text, ringing screen time)
  displayLarge: {
    fontSize:      '3.5625rem',  // 57sp
    lineHeight:    '64px',
    fontWeight:    400,
    letterSpacing: '-0.015625rem', // -0.25sp
  },
  displayMedium: {
    fontSize:      '2.8125rem',  // 45sp
    lineHeight:    '52px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },
  displaySmall: {
    fontSize:      '2.25rem',    // 36sp
    lineHeight:    '44px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },

  // ── Headline ── (alarm card time, screen section titles)
  headlineLarge: {
    fontSize:      '2rem',       // 32sp
    lineHeight:    '40px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },
  headlineMedium: {
    fontSize:      '1.75rem',    // 28sp
    lineHeight:    '36px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },
  headlineSmall: {
    fontSize:      '1.5rem',     // 24sp
    lineHeight:    '32px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },

  // ── Title ── (screen headers, card titles, setting labels)
  titleLarge: {
    fontSize:      '1.375rem',   // 22sp
    lineHeight:    '28px',
    fontWeight:    400,
    letterSpacing: '0rem',
  },
  titleMedium: {
    fontSize:      '1rem',       // 16sp
    lineHeight:    '24px',
    fontWeight:    500,
    letterSpacing: '0.009375rem', // 0.15sp
  },
  titleSmall: {
    fontSize:      '0.875rem',   // 14sp
    lineHeight:    '20px',
    fontWeight:    500,
    letterSpacing: '0.00625rem',  // 0.1sp
  },

  // ── Body ── (main readable content, setting row values)
  bodyLarge: {
    fontSize:      '1rem',       // 16sp
    lineHeight:    '24px',
    fontWeight:    400,
    letterSpacing: '0.03125rem',  // 0.5sp
  },
  bodyMedium: {
    fontSize:      '0.875rem',   // 14sp
    lineHeight:    '20px',
    fontWeight:    400,
    letterSpacing: '0.015625rem', // 0.25sp
  },
  bodySmall: {
    fontSize:      '0.75rem',    // 12sp
    lineHeight:    '16px',
    fontWeight:    400,
    letterSpacing: '0.025rem',    // 0.4sp
  },

  // ── Label ── (button text, chip labels, captions)
  labelLarge: {
    fontSize:      '0.875rem',   // 14sp — button text
    lineHeight:    '20px',
    fontWeight:    500,
    letterSpacing: '0.00625rem',  // 0.1sp
  },
  labelMedium: {
    fontSize:      '0.75rem',    // 12sp — chip labels
    lineHeight:    '16px',
    fontWeight:    500,
    letterSpacing: '0.03125rem',  // 0.5sp
  },
  labelSmall: {
    fontSize:      '0.6875rem',  // 11sp — smallest captions
    lineHeight:    '16px',
    fontWeight:    500,
    letterSpacing: '0.03125rem',  // 0.5sp
  },
};

// Component → MD3 type role mapping (for reference when porting to native)
// Android Compose:  Text(style = MaterialTheme.typography.headlineLarge)
// iOS SwiftUI:      Text(...).font(.system(size: 32, weight: .regular))
//
// | Old token  | MD3 role        | Used in                          |
// |------------|-----------------|----------------------------------|
// | h1         | displayMedium   | Ringing screen time              |
// | h2         | headlineLarge   | Alarm card time "07:30"          |
// | h3         | headlineMedium  | Screen headers, premium title    |
// | h4         | titleLarge      | Sleep card title                 |
// | body1      | bodyLarge       | Setting row labels, main text    |
// | body2      | bodyMedium      | Subtitles, secondary text        |
// | button     | labelLarge      | All button text                  |

// Legacy export — keeps existing consumers compiling during migration.
// Remove after all components are updated to use typeScale directly.
/** @deprecated Use typeScale instead */
export const typography = {
  fontFamily: fontFamilies.web,
  h1:     { fontSize: typeScale.displayMedium.fontSize,  fontWeight: typeScale.displayMedium.fontWeight,  letterSpacing: typeScale.displayMedium.letterSpacing },
  h2:     { fontSize: typeScale.headlineLarge.fontSize,  fontWeight: typeScale.headlineLarge.fontWeight },
  h3:     { fontSize: typeScale.headlineSmall.fontSize,  fontWeight: typeScale.headlineSmall.fontWeight },
  h4:     { fontSize: typeScale.titleLarge.fontSize,     fontWeight: typeScale.titleLarge.fontWeight },
  body1:  { fontSize: typeScale.bodyLarge.fontSize,      lineHeight: 1.5 },
  body2:  { fontSize: typeScale.bodyMedium.fontSize,     lineHeight: 1.43 },
  button: { textTransform: 'none' as const,              fontWeight: typeScale.labelLarge.fontWeight },
} as const;
