import { useState, useMemo, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { morningTheme, nightTheme } from './theme';
import { applyThemeCssVars } from './tokens';
import AppShell from './components/AppShell';
import AlarmListScreen from './screens/AlarmListScreen';
import EditAlarmScreen from './screens/EditAlarmScreen';
import SleepScreen from './screens/SleepScreen';
import RingingScreen from './screens/RingingScreen';
import ProfileScreen from './screens/ProfileScreen';
import SettingsScreen from './screens/SettingsScreen';
import DesignSystemScreen from './screens/DesignSystemScreen';
import type { ThemeVariant } from './tokens';

export type ThemeMode = 'morning' | 'night' | 'auto';

function getAutoTheme(): ThemeVariant {
  const hour = new Date().getHours();
  return hour >= 6 && hour < 18 ? 'morning' : 'night';
}

export default function App() {
  const [themeMode, setThemeMode] = useState<ThemeMode>('morning');

  const resolvedVariant = useMemo<ThemeVariant>(() => {
    const urlTheme = new URLSearchParams(window.location.search).get('theme');
    if (urlTheme === 'morning' || urlTheme === 'night') return urlTheme;
    return themeMode === 'auto' ? getAutoTheme() : themeMode;
  }, [themeMode]);

  const resolvedTheme = resolvedVariant === 'morning' ? morningTheme : nightTheme;

  // Sync MD3 CSS custom properties with the active theme variant.
  // These vars are used by components that reference tokens directly
  // (e.g. DsGlassButton, RingingScreen) outside of MUI's sx prop system.
  useEffect(() => {
    applyThemeCssVars(resolvedVariant);
  }, [resolvedVariant]);

  return (
    <ThemeProvider theme={resolvedTheme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          {/* Standalone screens — no shell */}
          <Route path="/ringing" element={<RingingScreen />} />
          <Route path="/design-system" element={<DesignSystemScreen />} />

          {/* Main app with bottom navigation */}
          <Route element={<AppShell themeMode={themeMode} onThemeChange={setThemeMode} />}>
            <Route index element={<Navigate to="/alarm" replace />} />
            <Route path="/alarm" element={<AlarmListScreen />} />
            <Route path="/alarm/new" element={<EditAlarmScreen />} />
            <Route path="/alarm/:id" element={<EditAlarmScreen />} />
            <Route path="/sleep" element={<SleepScreen />} />
            <Route path="/settings" element={<SettingsScreen themeMode={themeMode} onThemeChange={setThemeMode} />} />
            <Route path="/profile" element={<ProfileScreen />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
