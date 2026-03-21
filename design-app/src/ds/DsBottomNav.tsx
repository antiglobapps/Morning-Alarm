import { Box, Typography, useTheme } from '@mui/material';
import AccessAlarmIcon from '@mui/icons-material/AccessAlarm';
import AccessAlarmFilledIcon from '@mui/icons-material/Alarm';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import NightsStayOutlinedIcon from '@mui/icons-material/NightsStayOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import SettingsIcon from '@mui/icons-material/Settings';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import PersonIcon from '@mui/icons-material/Person';
import { elevation } from '../tokens';

// Floating Liquid Glass Tab Bar — Apple HIG inspired, cross-platform
//
// Visual: rounded floating bar with backdrop blur, semi-transparent tinted background,
// and MD3 active pill indicator behind the selected icon.
//
// Height:       64px (icon 24 + pill 28 + label 12 + gaps)
// Corners:      extraLarge (28dp) — pill-shaped bar
// Background:   semi-transparent surface with backdrop-filter: blur(24px)
// Elevation:    level3 (6dp shadow) for floating appearance
// Pill:         full-tab primaryContainer capsule behind selected icon + label (iOS 26 style)
// Selected:     primary icon + label (filled variant)
// Unselected:   onSurfaceVariant icon + label (outlined variant)
//
// Android Compose:
//   NavigationBar(
//     containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
//     modifier = Modifier
//       .padding(horizontal = 16.dp, bottom = 12.dp)
//       .clip(RoundedCornerShape(28.dp))
//       .shadow(6.dp, RoundedCornerShape(28.dp)),
//   ) {
//     tabs.forEach { tab ->
//       NavigationBarItem(
//         selected = currentRoute == tab.route,
//         onClick  = { navController.navigate(tab.route) },
//         icon     = {
//           Icon(
//             if (selected) tab.filledIcon else tab.outlinedIcon,
//             contentDescription = tab.label,
//           )
//         },
//         label    = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
//         colors   = NavigationBarItemDefaults.colors(
//           selectedIconColor   = MaterialTheme.colorScheme.primary,
//           selectedTextColor   = MaterialTheme.colorScheme.primary,
//           unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//           unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//           indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
//         ),
//       )
//     }
//   }
//
// iOS SwiftUI:
//   // iOS 26+ with Liquid Glass:
//   TabView(selection: $selectedTab) {
//     AlarmView()
//       .tabItem {
//         Image(systemName: selectedTab == .alarm ? "alarm.fill" : "alarm")
//         Text("Alarm")
//       }
//       .tag(Tab.alarm)
//     SleepView()
//       .tabItem {
//         Image(systemName: selectedTab == .sleep ? "moon.fill" : "moon")
//         Text("Sleep")
//       }
//       .tag(Tab.sleep)
//     SettingsView()
//       .tabItem {
//         Image(systemName: selectedTab == .settings ? "gearshape.fill" : "gearshape")
//         Text("Settings")
//       }
//       .tag(Tab.settings)
//     ProfileView()
//       .tabItem {
//         Image(systemName: selectedTab == .profile ? "person.fill" : "person")
//         Text("Profile")
//       }
//       .tag(Tab.profile)
//   }
//   .tint(Color(md3Scheme.primary))
//
//   // iOS 18 and below — custom floating glass bar:
//   .background(.ultraThinMaterial)
//   .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
//   .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
//   .padding(.horizontal, 16).padding(.bottom, 12)

// Semi-transparent glass backgrounds derived from theme surface colors
const GLASS_BG_MORNING = 'rgba(255, 249, 242, 0.72)'; // #FFF9F2 at 72%
const GLASS_BG_NIGHT   = 'rgba(21, 28, 51, 0.72)';    // #151C33 at 72%
const GLASS_BORDER_MORNING = 'rgba(255, 255, 255, 0.35)';
const GLASS_BORDER_NIGHT   = 'rgba(255, 255, 255, 0.08)';

interface TabDef {
  label: string;
  outlinedIcon: React.ReactElement;
  filledIcon:   React.ReactElement;
  path:  string;
}

const tabs: TabDef[] = [
  {
    label: 'Alarm',
    outlinedIcon: <AccessAlarmIcon sx={{ fontSize: 24 }} />,
    filledIcon:   <AccessAlarmFilledIcon sx={{ fontSize: 24 }} />,
    path: '/alarm',
  },
  {
    label: 'Sleep',
    outlinedIcon: <NightsStayOutlinedIcon sx={{ fontSize: 24 }} />,
    filledIcon:   <NightsStayIcon sx={{ fontSize: 24 }} />,
    path: '/sleep',
  },
  {
    label: 'Settings',
    outlinedIcon: <SettingsOutlinedIcon sx={{ fontSize: 24 }} />,
    filledIcon:   <SettingsIcon sx={{ fontSize: 24 }} />,
    path: '/settings',
  },
  {
    label: 'Profile',
    outlinedIcon: <PersonOutlineIcon sx={{ fontSize: 24 }} />,
    filledIcon:   <PersonIcon sx={{ fontSize: 24 }} />,
    path: '/profile',
  },
];

interface DsBottomNavProps {
  currentPath: string;
  onNavigate:  (path: string) => void;
  // preview=true renders inline (no fixed positioning) for design system showcase
  preview?:    boolean;
}

export default function DsBottomNav({ currentPath, onNavigate, preview = false }: DsBottomNavProps) {
  const theme = useTheme();
  const isMorning = theme.palette.background.default === '#FFF9F2';
  const glassBg = isMorning ? GLASS_BG_MORNING : GLASS_BG_NIGHT;
  const glassBorder = isMorning ? GLASS_BORDER_MORNING : GLASS_BORDER_NIGHT;

  const selectedIndex = tabs.findIndex((t) => currentPath.startsWith(t.path));

  const glassStyles = {
    borderRadius: '28px',
    bgcolor: glassBg,
    backdropFilter: 'blur(24px)',
    WebkitBackdropFilter: 'blur(24px)',
    border: '0.5px solid',
    borderColor: glassBorder,
    boxShadow: elevation.level3,
  };

  return (
    <Box
      sx={preview ? {
        width: '100%',
        ...glassStyles,
      } : {
        position:  'fixed',
        bottom:    12,
        left:      '50%',
        transform: 'translateX(-50%)',
        width:     'calc(100% - 32px)',
        maxWidth:  398,
        zIndex:    10,
        ...glassStyles,
      }}
    >
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-around',
          alignItems: 'center',
          height: 64,
          px: 1,
        }}
      >
        {tabs.map((tab, idx) => {
          const selected = idx === selectedIndex;
          return (
            <Box
              key={tab.path}
              onClick={() => onNavigate(tab.path)}
              sx={{
                // Tab item — position: relative so the pill is absolutely positioned behind content
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '2px',
                cursor: 'pointer',
                flex: 1,
                py: 0.75,
                color: selected ? 'primary.main' : 'text.secondary',
                transition: 'color 200ms ease',
                '&:hover': {
                  color: selected ? 'primary.main' : 'text.primary',
                },
                // Remove tap highlight on mobile
                WebkitTapHighlightColor: 'transparent',
                userSelect: 'none',
              }}
            >
              {/* iOS 26 Liquid Glass style — capsule covers entire tab area (icon + label) */}
              <Box
                sx={{
                  position: 'absolute',
                  inset: 2,
                  borderRadius: '16px',
                  bgcolor: 'primaryContainer.main',
                  background: (theme) => theme.palette.mode === 'dark'
                    ? theme.palette.primary?.dark ?? '#3E3E93'
                    : '#FFDDB3',
                  opacity: selected ? 1 : 0,
                  transition: 'opacity 200ms ease',
                }}
              />
              {/* Icon — z-index above pill */}
              <Box sx={{ position: 'relative', zIndex: 1, display: 'flex' }}>
                {selected ? tab.filledIcon : tab.outlinedIcon}
              </Box>
              <Typography
                variant="labelSmall"
                sx={{
                  position: 'relative',
                  zIndex: 1,
                  color: 'inherit',
                  lineHeight: 1,
                  fontSize: '0.65rem',
                }}
              >
                {tab.label}
              </Typography>
            </Box>
          );
        })}
      </Box>
    </Box>
  );
}
