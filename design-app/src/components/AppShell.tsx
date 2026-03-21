import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Box } from '@mui/material';
import { DsBottomNav } from '../ds';
import type { ThemeMode } from '../App';

interface AppShellProps {
  themeMode: ThemeMode;
  onThemeChange: (mode: ThemeMode) => void;
}

export default function AppShell({ }: AppShellProps) {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        maxWidth: 430,
        mx: 'auto',
        bgcolor: 'background.default',
        position: 'relative',
      }}
    >
      <Box sx={{ flex: 1, overflow: 'auto', pb: '88px' }}>
        <Outlet />
      </Box>

      <DsBottomNav currentPath={location.pathname} onNavigate={(path) => navigate(path)} />
    </Box>
  );
}
