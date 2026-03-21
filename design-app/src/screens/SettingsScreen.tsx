import { Box, Typography, Stack } from '@mui/material';
import PaletteIcon from '@mui/icons-material/Palette';
import LanguageIcon from '@mui/icons-material/Language';
import MicIcon from '@mui/icons-material/Mic';
import LibraryMusicIcon from '@mui/icons-material/LibraryMusic';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import { DsSettingRow } from '../ds';
import { spacing } from '../tokens';
import type { ThemeMode } from '../App';

interface SettingsScreenProps {
  themeMode: ThemeMode;
  onThemeChange: (mode: ThemeMode) => void;
}

const THEME_OPTIONS: { value: ThemeMode; label: string }[] = [
  { value: 'morning', label: 'Morning' },
  { value: 'night',   label: 'Night'   },
  { value: 'auto',    label: 'Auto'    },
];

export default function SettingsScreen({ themeMode, onThemeChange }: SettingsScreenProps) {
  const themeLabel = THEME_OPTIONS.find((t) => t.value === themeMode)?.label ?? 'Auto';

  const cycleTheme = () => {
    const idx = THEME_OPTIONS.findIndex((t) => t.value === themeMode);
    onThemeChange(THEME_OPTIONS[(idx + 1) % THEME_OPTIONS.length].value);
  };

  return (
    <Box sx={{ px: spacing.screenHorizontal, pt: spacing.screenTop }}>
      {/* headlineLarge: primary screen title */}
      <Typography variant="headlineLarge" sx={{ mb: spacing.sectionSpacing }}>
        Settings
      </Typography>

      <Stack spacing={1.5}>
        <DsSettingRow icon={<PaletteIcon />}      label="Theme"      value={themeLabel} onClick={cycleTheme} />
        <DsSettingRow icon={<LanguageIcon />}     label="Language"   value="EN / RU" />
        <DsSettingRow icon={<MicIcon />}          label="Voice Stop" value="Off" />
        <DsSettingRow icon={<LibraryMusicIcon />} label="Sleep Pack" value="Free" />
        <DsSettingRow icon={<AutoAwesomeIcon />}  label="AI Melody"  value="3 left" />
      </Stack>
    </Box>
  );
}
