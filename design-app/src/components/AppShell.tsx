import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Box, useTheme } from '@mui/material';
import { DsBottomNav } from '../ds';
import { morningGradient } from '../tokens/colors';
import type { ThemeMode } from '../App';

interface AppShellProps {
  themeMode: ThemeMode;
  onThemeChange: (mode: ThemeMode) => void;
}

export default function AppShell({ }: AppShellProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMorning = theme.palette.mode === 'light';

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        ...(isMorning
          ? { background: morningGradient }
          : { bgcolor: 'background.default' }),
      }}
    >
      {/* Scrollable content */}
      <Box sx={{ flex: 1, overflow: 'auto', minHeight: 0 }}>
        <Outlet />
      </Box>

      {/* Bottom navigation — always at bottom */}
      <DsBottomNav currentPath={location.pathname} onNavigate={(path) => navigate(path)} />
    </Box>
  );
}
