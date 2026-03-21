import { useState } from 'react';
import {
  Box,
  Typography,
  ThemeProvider,
  Divider,
  Stack,
  Switch,
  Slider,
} from '@mui/material';
import { morningTheme, nightTheme } from '../theme';
import { colors, typeScale, spacing, elevation, shape, themeCssVars, cssVar, v } from '../tokens';
import {
  DsSettingRow,
  DsPrimaryButton,
  DsDayChipRow,
  DsMoodChipRow,
  DsAlarmCard,
  DsSleepCard,
  DsSleepTimerCard,
  DsPremiumCard,
  DsGlassButton,
  DsFab,
  DsBottomNav,
} from '../ds';

import AccessAlarmIcon from '@mui/icons-material/AccessAlarm';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import ImageIcon from '@mui/icons-material/Image';
import SnoozeIcon from '@mui/icons-material/Snooze';
import MicIcon from '@mui/icons-material/Mic';
import PaletteIcon from '@mui/icons-material/Palette';
import LanguageIcon from '@mui/icons-material/Language';
import LibraryMusicIcon from '@mui/icons-material/LibraryMusic';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AlarmOffIcon from '@mui/icons-material/AlarmOff';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import SettingsIcon from '@mui/icons-material/Settings';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

const SAMPLE_IMAGE = 'https://images.unsplash.com/photo-1532978379173-523e16f371f2?w=400&h=200&fit=crop';

const allIcons = [
  { name: 'AccessAlarm', node: <AccessAlarmIcon /> },
  { name: 'NightsStay', node: <NightsStayIcon /> },
  { name: 'PersonOutline', node: <PersonOutlineIcon /> },
  { name: 'MusicNote', node: <MusicNoteIcon /> },
  { name: 'Image', node: <ImageIcon /> },
  { name: 'Snooze', node: <SnoozeIcon /> },
  { name: 'Mic', node: <MicIcon /> },
  { name: 'Palette', node: <PaletteIcon /> },
  { name: 'Language', node: <LanguageIcon /> },
  { name: 'LibraryMusic', node: <LibraryMusicIcon /> },
  { name: 'AutoAwesome', node: <AutoAwesomeIcon /> },
  { name: 'PlayArrow', node: <PlayArrowIcon /> },
  { name: 'AlarmOff', node: <AlarmOffIcon /> },
  { name: 'Add', node: <AddIcon /> },
  { name: 'SettingsOutlined', node: <SettingsOutlinedIcon /> },
  { name: 'Settings', node: <SettingsIcon /> },
  { name: 'Star', node: <StarIcon /> },
  { name: 'ChevronRight', node: <ChevronRightIcon /> },
  { name: 'ArrowBack', node: <SnoozeIcon /> },
];

// Renders content in both themes side by side.
// Each panel gets its own scoped CSS custom properties so Figma capture
// can detect var(--ds-...) references and link them to Figma variables.
function DualTheme({ children }: { children: (variant: 'morning' | 'night') => React.ReactNode }) {
  return (
    <Box sx={{ display: 'flex', gap: 3 }}>
      {(['morning', 'night'] as const).map((variant) => (
        <ThemeProvider key={variant} theme={variant === 'morning' ? morningTheme : nightTheme}>
          <Box
            style={themeCssVars(variant) as React.CSSProperties}
            sx={{
              flex: 1,
              bgcolor: 'background.default',
              borderRadius: 3,
              p: 3,
              minWidth: 0,
            }}
          >
            <Typography variant="body2" sx={{ mb: 2, opacity: 0.6, textTransform: 'uppercase', letterSpacing: 1 }}>
              {variant}
            </Typography>
            {children(variant)}
          </Box>
        </ThemeProvider>
      ))}
    </Box>
  );
}

// Interactive bottom nav preview — allows clicking tabs to see selected/unselected states
function BottomNavPreview() {
  const [path, setPath] = useState('/alarm');
  return (
    <Box sx={{ borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}>
      <DsBottomNav currentPath={path} onNavigate={setPath} preview />
    </Box>
  );
}

function SectionTitle({ children }: { children: React.ReactNode }) {
  return (
    <Typography variant="h2" sx={{ mt: 6, mb: 3, fontWeight: 600 }}>
      {children}
    </Typography>
  );
}

// Color swatch grid — uses CSS var for bgcolor so Figma capture links it
function ColorSwatch({ name, hex, cssVarName }: { name: string; hex: string; cssVarName?: string }) {
  return (
    <Box sx={{ textAlign: 'center' }}>
      <Box
        sx={{
          width: 64,
          height: 64,
          borderRadius: 2,
          bgcolor: cssVarName ? v(cssVarName) : hex,
          border: '1px solid rgba(128,128,128,0.2)',
          mb: 0.5,
          mx: 'auto',
        }}
      />
      <Typography variant="body2" sx={{ fontSize: '0.7rem', fontWeight: 500 }}>
        {name}
      </Typography>
      <Typography variant="body2" sx={{ fontSize: '0.65rem', opacity: 0.6 }}>
        {hex}
      </Typography>
    </Box>
  );
}

export default function DesignSystemScreen() {
  const [timerVal, setTimerVal] = useState(30);

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', px: 4, py: 6 }}>
      <Typography variant="h1" sx={{ fontWeight: 600, mb: 1 }}>
        Morning Alarm — Design System
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Token-driven components. Each section renders in both themes side by side.
      </Typography>

      <Divider />

      {/* 1. Color Palette */}
      <SectionTitle>1. Color Palette</SectionTitle>
      <DualTheme>
        {(variant) => {
          const c = colors[variant];
          const entries: [string, string, string][] = [
            ['primary', c.primary, cssVar.colorPrimary],
            ['onPrimary', c.onPrimary, cssVar.colorOnPrimary],
            ['primaryContainer', c.primaryContainer, cssVar.colorPrimaryContainer],
            ['onPrimaryContainer', c.onPrimaryContainer, cssVar.colorOnPrimaryContainer],
            ['secondary', c.secondary, cssVar.colorSecondary],
            ['onSecondary', c.onSecondary, cssVar.colorOnSecondary],
            ['secondaryContainer', c.secondaryContainer, cssVar.colorSecondaryContainer],
            ['onSecondaryContainer', c.onSecondaryContainer, cssVar.colorOnSecondaryContainer],
            ['tertiary', c.tertiary, cssVar.colorTertiary],
            ['onTertiary', c.onTertiary, cssVar.colorOnTertiary],
            ['tertiaryContainer', c.tertiaryContainer, cssVar.colorTertiaryContainer],
            ['onTertiaryContainer', c.onTertiaryContainer, cssVar.colorOnTertiaryContainer],
            ['error', c.error, cssVar.colorError],
            ['onError', c.onError, cssVar.colorOnError],
            ['errorContainer', c.errorContainer, cssVar.colorErrorContainer],
            ['onErrorContainer', c.onErrorContainer, cssVar.colorOnErrorContainer],
            ['background', c.background, cssVar.colorBackground],
            ['onBackground', c.onBackground, cssVar.colorOnBackground],
            ['surface', c.surface, cssVar.colorSurface],
            ['onSurface', c.onSurface, cssVar.colorOnSurface],
            ['surfaceVariant', c.surfaceVariant, cssVar.colorSurfaceVariant],
            ['onSurfaceVariant', c.onSurfaceVariant, cssVar.colorOnSurfaceVariant],
            ['outline', c.outline, cssVar.colorOutline],
            ['outlineVariant', c.outlineVariant, cssVar.colorOutlineVariant],
            ['inverseSurface', c.inverseSurface, cssVar.colorInverseSurface],
            ['inverseOnSurface', c.inverseOnSurface, cssVar.colorInverseOnSurface],
            ['inversePrimary', c.inversePrimary, cssVar.colorInversePrimary],
            ['surfaceTint', c.surfaceTint, cssVar.colorSurfaceTint],
          ];
          return (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
              {entries.map(([name, hex, varName]) => (
                <ColorSwatch key={name} name={name} hex={hex} cssVarName={varName} />
              ))}
            </Box>
          );
        }}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 2. Typography */}
      <SectionTitle>2. Typography</SectionTitle>
      <DualTheme>
        {() => (
          <Stack spacing={1.5}>
            {(Object.entries(typeScale) as [string, typeof typeScale.displayLarge][]).map(([role, spec]) => (
              <Box key={role}>
                <Box
                  sx={{
                    fontSize: spec.fontSize,
                    fontWeight: spec.fontWeight,
                    lineHeight: spec.lineHeight,
                    letterSpacing: spec.letterSpacing,
                    color: 'text.primary',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {role} — The quick brown fox
                </Box>
                <Box sx={{ fontSize: '0.65rem', opacity: 0.5, mt: 0.25, fontFamily: 'monospace' }}>
                  {spec.fontSize} / lh {spec.lineHeight} / w{spec.fontWeight} / ls {spec.letterSpacing}
                </Box>
              </Box>
            ))}
          </Stack>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 3. Spacing & Shape */}
      <SectionTitle>3. Spacing & Shape</SectionTitle>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ mb: 2 }}>Spacing Scale (base: {spacing.base}px)</Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          {[1, 2, 3, 4, 5, 6, 8].map((n) => (
            <Box key={n} sx={{ textAlign: 'center' }}>
              <Box
                sx={{
                  width: n * spacing.base,
                  height: n * spacing.base,
                  bgcolor: 'primary.main',
                  borderRadius: 1,
                  opacity: 0.7,
                }}
              />
              <Typography variant="body2" sx={{ mt: 0.5, fontSize: '0.7rem' }}>
                {n} = {n * spacing.base}px
              </Typography>
            </Box>
          ))}
        </Box>
      </Box>
      <Box>
        <Typography variant="h4" sx={{ mb: 2 }}>MD3 Shape Scale</Typography>
        <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          {(Object.entries(shape) as [string, number][]).map(([name, val]) => (
            <Box key={name} sx={{ textAlign: 'center' }}>
              <Box
                sx={{
                  width: 64,
                  height: 64,
                  bgcolor: 'primaryContainer.main',
                  background: 'var(--ds-color-primary-container)',
                  borderRadius: `${Math.min(val, 32)}px`,
                  border: '2px solid',
                  borderColor: 'primary.main',
                }}
              />
              <Typography variant="body2" sx={{ mt: 0.5, fontSize: '0.65rem', fontWeight: 500 }}>
                {name}
              </Typography>
              <Typography variant="body2" sx={{ fontSize: '0.6rem', opacity: 0.5 }}>
                {val === 9999 ? 'pill' : `${val}dp`}
              </Typography>
            </Box>
          ))}
        </Box>
      </Box>

      <Divider sx={{ my: 4 }} />

      {/* 4. Icons */}
      <SectionTitle>4. Icons</SectionTitle>
      <DualTheme>
        {() => (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
            {allIcons.map(({ name, node }) => (
              <Box key={name} sx={{ textAlign: 'center', width: 72 }}>
                <Box sx={{ color: 'primary.main', fontSize: 28, display: 'flex', justifyContent: 'center' }}>
                  {node}
                </Box>
                <Typography variant="body2" sx={{ fontSize: '0.6rem', mt: 0.5 }}>
                  {name}
                </Typography>
              </Box>
            ))}
          </Box>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 5. Buttons */}
      <SectionTitle>5. Buttons</SectionTitle>
      <DualTheme>
        {() => (
          <Stack spacing={2}>
            <DsPrimaryButton label="Primary Button" />
            <DsPrimaryButton label="Disabled" disabled />
            <Box sx={{ bgcolor: 'grey.900', p: 3, borderRadius: 2 }}>
              <Stack spacing={2}>
                <DsGlassButton label="Glass Button" startIcon={<SnoozeIcon />} />
                <DsGlassButton label="Accent Glass" accent startIcon={<AlarmOffIcon />} />
              </Stack>
            </Box>
          </Stack>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 6. Cards */}
      <SectionTitle>6. Cards</SectionTitle>
      <DualTheme>
        {() => (
          <Stack spacing={2}>
            <DsAlarmCard
              time="07:30"
              label="Soft Sunrise"
              days={['Mo', 'Tu', 'We', 'Th', 'Fr']}
              enabled={true}
              onToggle={() => {}}
              onClick={() => {}}
            />
            <DsSleepCard
              title="Moon Breathing"
              subtitle="15 min meditation"
              image={SAMPLE_IMAGE}
            />
            <DsPremiumCard
              features={['Unlimited meditations', 'Voice stop', 'AI melody generation']}
            />
            <DsSettingRow icon={<MusicNoteIcon />} label="Melody" value="Soft Awakening" />
            <DsSettingRow icon={<MicIcon />} label="Voice Stop" value="Premium" premium />
          </Stack>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 7. Chips */}
      <SectionTitle>7. Chips</SectionTitle>
      <DualTheme>
        {() => (
          <Stack spacing={3}>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Day Chips</Typography>
              <DsDayChipRow selectedDays={['Mo', 'We', 'Fr']} />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Day Chips (small)</Typography>
              <DsDayChipRow selectedDays={['Sa', 'Su']} size="small" />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Mood Chips</Typography>
              <DsMoodChipRow moods={['Calm', 'Deep Sleep', 'Rain', 'Forest', 'Ocean']} selected="Calm" onSelect={() => {}} />
            </Box>
          </Stack>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 8. Controls */}
      <SectionTitle>8. Controls</SectionTitle>
      <DualTheme>
        {() => (
          <Stack spacing={3}>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Switch</Typography>
              <Box sx={{ display: 'flex', gap: 2 }}>
                <Switch defaultChecked color="primary" />
                <Switch color="primary" />
              </Box>
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Slider</Typography>
              <Slider value={timerVal} onChange={(_, v) => setTimerVal(v as number)} min={5} max={120} step={5} color="primary" />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>Sleep Timer Card</Typography>
              <DsSleepTimerCard value={timerVal} onChange={setTimerVal} />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1, opacity: 0.6 }}>FAB</Typography>
              <DsFab />
            </Box>
          </Stack>
        )}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 9. Bottom Navigation (Liquid Glass) */}
      <SectionTitle>9. Bottom Navigation (Liquid Glass)</SectionTitle>
      <DualTheme>
        {() => <BottomNavPreview />}
      </DualTheme>

      <Divider sx={{ my: 4 }} />

      {/* 10. Elevation */}
      <SectionTitle>10. Elevation</SectionTitle>
      <DualTheme>
        {(variant) => (
          <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
            {(Object.entries(elevation) as [string, string][]).map(([level, shadow]) => (
              <Box key={level} sx={{ textAlign: 'center' }}>
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: `${shape.medium}px`,
                    bgcolor: 'background.paper',
                    boxShadow: shadow === 'none' ? 'none' : shadow,
                    border: shadow === 'none' ? '1px solid' : 'none',
                    borderColor: 'outlineVariant' in {} ? 'transparent' : 'divider',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                />
                <Typography variant="body2" sx={{ mt: 0.5, fontSize: '0.65rem', fontWeight: 500 }}>
                  {level}
                </Typography>
                <Typography variant="body2" sx={{ fontSize: '0.6rem', opacity: 0.5 }}>
                  {level === 'level0' ? '0dp' : level === 'level1' ? '1dp' : level === 'level2' ? '3dp' : level === 'level3' ? '6dp' : level === 'level4' ? '8dp' : '12dp'}
                </Typography>
                {variant === 'night' && level !== 'level0' && (
                  <Typography variant="body2" sx={{ fontSize: '0.55rem', opacity: 0.4, mt: 0.25 }}>
                    + tonal overlay
                  </Typography>
                )}
              </Box>
            ))}
          </Box>
        )}
      </DualTheme>

      <Box sx={{ py: 6 }} />
    </Box>
  );
}
