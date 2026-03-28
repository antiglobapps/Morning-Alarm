import { useState } from 'react';
import {
  Box, Card, CardContent, Chip, Stack, Switch, Typography,
} from '@mui/material';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import AddIcon from '@mui/icons-material/Add';
import AccessAlarmIcon from '@mui/icons-material/AccessAlarm';
import AccessAlarmFilledIcon from '@mui/icons-material/Alarm';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import NightsStayOutlinedIcon from '@mui/icons-material/NightsStayOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import SettingsIcon from '@mui/icons-material/Settings';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import PersonIcon from '@mui/icons-material/Person';

// ── Calm Minimal Palette ───────────────────────────────────

const P = {
  // Background gradient
  bgTop:    '#F5F1EC',
  bgMid:    '#F2ECE6',
  bgBottom: '#EEE7DF',
  // Surface
  card:     '#FBF7F2',
  nav:      '#FBF7F2',
  // Text
  textPrimary:   '#3F3933',
  textSecondary: '#6D655D',
  textTertiary:  '#A89F96',
  // Accent
  accentMain:   '#C7975D',
  accentSoft:   '#EED9C1',
  accentIconBg: '#EADCCB',
  accentIcon:   '#6E5740',
  // Stroke / divider
  strokeLight: '#E6DDD4',
  strokeChip:  '#CFC2B4',
  divider:     '#DDD3C8',
  // Inactive card
  inactiveTime:    '#AAA098',
  inactiveText:    '#A79D94',
  inactiveIconBg:  '#EFE7DE',
  inactiveIcon:    '#B4AAA0',
  // Toggle
  activeTrack:   '#C7975D',
  activeThumb:   '#F9F5F0',
  inactiveTrack: '#D8CEC4',
  inactiveThumb: '#F2ECE6',
  // Nav
  navBorder:      '#E5DBD1',
  navSelected:    '#B98349',
  navUnselected:  '#5F5750',
  // Day chips
  chipSelectedBg:   '#EED9C1',
  chipSelectedText: '#4A4036',
  chipUnselectedBg:     '#FBF7F2',
  chipUnselectedBorder: '#CFC2B4',
  chipUnselectedText:   '#6A6158',
};

// ── Data ───────────────────────────────────────────────────

interface Alarm {
  id: string;
  time: string;
  melody: string;
  days: string[];
  enabled: boolean;
}

const ALARMS: Alarm[] = [
  { id: '1', time: '06:30', melody: 'Soft Sunrise',    days: ['Mo', 'Tu', 'We', 'Th', 'Fr'], enabled: true },
  { id: '2', time: '08:00', melody: 'Forest Calm',     days: ['Sa', 'Su'],                    enabled: false },
  { id: '3', time: '07:15', melody: 'Energy Morning',  days: ['Mo', 'We', 'Fr'],              enabled: true },
];

const ALL_DAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const DAY_MAP: Record<string, number> = { Su: 0, Mo: 1, Tu: 2, We: 3, Th: 4, Fr: 5, Sa: 6 };

function getNextAlarmDay(alarm: Alarm): string {
  const today = new Date().getDay();
  const indices = alarm.days.map((d) => DAY_MAP[d]).sort((a, b) => a - b);
  const next = indices.find((d) => d > today);
  const nextDay = next !== undefined ? next : indices[0];
  const diff = (nextDay - today + 7) % 7;
  if (diff === 0) return 'Today';
  if (diff === 1) return 'Tomorrow';
  return DAY_NAMES[nextDay];
}

function getTimeUntil(timeStr: string): string {
  const [h, m] = timeStr.split(':').map(Number);
  const now = new Date();
  const target = new Date();
  target.setHours(h, m, 0, 0);
  if (target <= now) target.setDate(target.getDate() + 1);
  const diffMs = target.getTime() - now.getTime();
  const hours = Math.floor(diffMs / 3600000);
  const minutes = Math.floor((diffMs % 3600000) / 60000);
  return `${hours}h ${minutes}m`;
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

// ── Header ─────────────────────────────────────────────────

function CalmHeader({ nextAlarm }: { nextAlarm: Alarm | undefined }) {
  if (!nextAlarm) {
    return (
      <Box sx={{ textAlign: 'center', pt: '28px', pb: '24px', px: '24px' }}>
        <Typography sx={{ fontSize: 26, fontWeight: 300, fontStyle: 'italic', color: P.textPrimary }}>
          No active alarms
        </Typography>
        <Box sx={{ width: 40, height: '0.5px', bgcolor: P.divider, mx: 'auto', my: '10px' }} />
        <Typography sx={{ fontSize: 13, color: P.textTertiary }}>
          Create your first alarm
        </Typography>
      </Box>
    );
  }

  const timeUntil = getTimeUntil(nextAlarm.time);
  const nextDay = getNextAlarmDay(nextAlarm);

  return (
    <Box sx={{ textAlign: 'center', pt: '28px', pb: '24px', px: '24px' }}>
      <Typography sx={{ fontSize: 26, fontWeight: 300, fontStyle: 'italic', color: P.textPrimary }}>
        {getGreeting()}
      </Typography>
      <Box sx={{ width: 40, height: '0.5px', bgcolor: P.divider, mx: 'auto', my: '10px' }} />
      <Typography sx={{ fontSize: 13, fontWeight: 400, color: P.textSecondary }}>
        Next alarm in{' '}
        <Box component="span" sx={{ fontWeight: 600 }}>{timeUntil}</Box>
      </Typography>
      <Typography sx={{ fontSize: 12, color: P.textTertiary, mt: '3px' }}>
        {nextAlarm.time} · {nextDay}
      </Typography>
    </Box>
  );
}

// ── Calm Alarm Card ────────────────────────────────────────

function CalmAlarmCard({ alarm, onToggle }: { alarm: Alarm; onToggle: () => void }) {
  const active = alarm.enabled;

  return (
    <Card
      sx={{
        bgcolor: P.card,
        borderRadius: '12px',
        border: `1px solid ${P.strokeLight}`,
        boxShadow: '0 4px 16px rgba(120, 100, 80, 0.06)',
        cursor: 'pointer',
        transition: 'transform 0.2s',
        '&:hover': { transform: 'scale(1.005)' },
      }}
    >
      <CardContent sx={{ px: 2, py: 2, '&:last-child': { pb: 2 } }}>
        {/* Time + toggle */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography sx={{
            fontSize: 32,
            fontWeight: 400,
            lineHeight: 1.1,
            color: active ? P.textPrimary : P.inactiveTime,
          }}>
            {alarm.time}
          </Typography>
          <Switch
            checked={active}
            onChange={(e) => { e.stopPropagation(); onToggle(); }}
            onClick={(e) => e.stopPropagation()}
            sx={{
              width: 52, height: 30,
              p: 0,
              '& .MuiSwitch-switchBase': {
                p: '3px',
                '&.Mui-checked': {
                  transform: 'translateX(22px)',
                  '& + .MuiSwitch-track': {
                    bgcolor: P.activeTrack,
                    opacity: 1,
                  },
                  '& .MuiSwitch-thumb': {
                    bgcolor: P.activeThumb,
                  },
                },
              },
              '& .MuiSwitch-thumb': {
                width: 24, height: 24,
                bgcolor: P.inactiveThumb,
                boxShadow: 'none',
              },
              '& .MuiSwitch-track': {
                borderRadius: 15,
                bgcolor: P.inactiveTrack,
                opacity: 1,
              },
            }}
          />
        </Box>

        {/* Melody */}
        <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mt: 1 }}>
          <MusicNoteIcon sx={{ fontSize: 16, color: active ? P.accentIcon : P.inactiveIcon }} />
          <Typography sx={{
            fontSize: 14, fontWeight: 500,
            color: active ? P.textSecondary : P.inactiveText,
          }}>
            {alarm.melody}
          </Typography>
        </Stack>

        {/* Day chips */}
        <Stack direction="row" spacing="4px" sx={{ mt: 1, flexWrap: 'wrap' }}>
          {ALL_DAYS.map((day) => {
            const selected = alarm.days.includes(day);
            return (
              <Chip
                key={day}
                label={day}
                size="small"
                sx={{
                  height: 28,
                  minWidth: 36,
                  borderRadius: '8px',
                  fontSize: 12,
                  fontWeight: 500,
                  bgcolor: selected
                    ? (active ? P.chipSelectedBg : P.inactiveIconBg)
                    : P.chipUnselectedBg,
                  color: selected
                    ? (active ? P.chipSelectedText : P.inactiveText)
                    : (active ? P.chipUnselectedText : P.inactiveText),
                  border: selected
                    ? 'none'
                    : `1px solid ${active ? P.chipUnselectedBorder : P.inactiveIconBg}`,
                  '& .MuiChip-label': { px: 0.75 },
                }}
              />
            );
          })}
        </Stack>
      </CardContent>
    </Card>
  );
}

// ── Add Alarm Placeholder ──────────────────────────────────

function CalmAddCard({ onClick }: { onClick: () => void }) {
  return (
    <Card
      sx={{
        bgcolor: 'transparent',
        borderRadius: '12px',
        border: `1.5px dashed ${P.strokeChip}`,
        boxShadow: 'none',
        opacity: 0.55,
        cursor: 'pointer',
        transition: 'opacity 0.2s',
        '&:hover': { opacity: 0.75 },
      }}
      onClick={onClick}
    >
      <CardContent sx={{
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        py: 4, '&:last-child': { pb: 4 },
      }}>
        <Stack alignItems="center" spacing={0.5}>
          <AddIcon sx={{ fontSize: 32, color: P.textSecondary }} />
          <Typography sx={{ fontSize: 14, color: P.textSecondary }}>
            Add alarm
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}

// ── Bottom Navigation ──────────────────────────────────────

const tabs = [
  { label: 'Alarm',    outlinedIcon: <AccessAlarmIcon />,      filledIcon: <AccessAlarmFilledIcon />,  path: '/alarm' },
  { label: 'Sleep',    outlinedIcon: <NightsStayOutlinedIcon />, filledIcon: <NightsStayIcon />,       path: '/sleep' },
  { label: 'Settings', outlinedIcon: <SettingsOutlinedIcon />,  filledIcon: <SettingsIcon />,          path: '/settings' },
  { label: 'Profile',  outlinedIcon: <PersonOutlineIcon />,     filledIcon: <PersonIcon />,            path: '/profile' },
];

function CalmBottomNav() {
  const currentPath = '/alarm';
  return (
    <Box sx={{
      width: 'calc(100% - 40px)',
      maxWidth: 361,
      mx: 'auto',
      mb: '12px',
      flexShrink: 0,
      borderRadius: '28px',
      bgcolor: P.nav,
      border: `1px solid ${P.navBorder}`,
      boxShadow: '0 2px 12px rgba(120, 100, 80, 0.08)',
    }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', height: 72, px: 1 }}>
        {tabs.map((tab) => {
          const selected = currentPath === tab.path;
          return (
            <Box
              key={tab.path}
              sx={{
                display: 'flex', flexDirection: 'column', alignItems: 'center',
                gap: '3px', cursor: 'pointer', flex: 1,
                color: selected ? P.navSelected : P.navUnselected,
                transition: 'color 200ms ease',
                '& svg': { fontSize: 24 },
              }}
            >
              {selected ? tab.filledIcon : tab.outlinedIcon}
              <Typography sx={{
                fontSize: 12, fontWeight: 500, color: 'inherit', lineHeight: 1,
              }}>
                {tab.label}
              </Typography>
            </Box>
          );
        })}
      </Box>
    </Box>
  );
}

// ── Screen ─────────────────────────────────────────────────

export default function DraftCalmAlarmScreen() {
  const [alarms, setAlarms] = useState(ALARMS);
  const nextAlarm = alarms.find((a) => a.enabled);

  const toggle = (id: string) =>
    setAlarms((prev) => prev.map((a) => (a.id === id ? { ...a, enabled: !a.enabled } : a)));

  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      background: `linear-gradient(180deg, ${P.bgTop} 0%, ${P.bgMid} 50%, ${P.bgBottom} 100%)`,
    }}>
      {/* Scrollable content */}
      <Box sx={{ flex: 1, overflow: 'auto', minHeight: 0 }}>
        <CalmHeader nextAlarm={nextAlarm} />
        <Stack spacing="16px" sx={{ px: '24px', pb: '24px' }}>
          {alarms.map((alarm) => (
            <CalmAlarmCard key={alarm.id} alarm={alarm} onToggle={() => toggle(alarm.id)} />
          ))}
          <CalmAddCard onClick={() => {}} />
        </Stack>
      </Box>

      {/* Bottom nav */}
      <CalmBottomNav />
    </Box>
  );
}
