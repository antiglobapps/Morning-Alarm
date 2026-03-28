import { useState } from 'react';
import { Box, Typography, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { DsAlarmCard, DsAddAlarmCard } from '../ds';
import { cssVar, v, spacing } from '../tokens';

interface Alarm {
  id: string;
  time: string;
  melody: string;
  days: string[];
  enabled: boolean;
}

const MOCK_ALARMS: Alarm[] = [
  {
    id: '1',
    time: '06:30',
    melody: 'Soft Sunrise',
    days: ['Mo', 'Tu', 'We', 'Th', 'Fr'],
    enabled: true,
  },
  {
    id: '2',
    time: '08:00',
    melody: 'Forest Calm',
    days: ['Sa', 'Su'],
    enabled: false,
  },
  {
    id: '3',
    time: '07:15',
    melody: 'Energy Morning',
    days: ['Mo', 'We', 'Fr'],
    enabled: true,
  },
];

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const DAY_ABBR_TO_INDEX: Record<string, number> = { Su: 0, Mo: 1, Tu: 2, We: 3, Th: 4, Fr: 5, Sa: 6 };

function getNextAlarm(alarms: Alarm[]): Alarm | undefined {
  return alarms.find((a) => a.enabled);
}

function getNextAlarmDay(alarm: Alarm): string {
  const today = new Date().getDay();
  const alarmDayIndices = alarm.days.map((d) => DAY_ABBR_TO_INDEX[d]).sort((a, b) => a - b);
  const next = alarmDayIndices.find((d) => d > today);
  const nextDay = next !== undefined ? next : alarmDayIndices[0];
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

export default function AlarmListScreen() {
  const navigate = useNavigate();
  const [alarms, setAlarms] = useState(MOCK_ALARMS);
  const nextAlarm = getNextAlarm(alarms);

  const toggleAlarm = (id: string) => {
    setAlarms((prev) =>
      prev.map((a) => (a.id === id ? { ...a, enabled: !a.enabled } : a))
    );
  };

  return (
    <Box sx={{ px: spacing.screenHorizontal, pt: 0 }}>
      {/* Centered Calm Minimal header */}
      <Box sx={{ textAlign: 'center', pt: '28px', pb: '24px' }}>
        <Typography sx={{
          fontSize: 26, fontWeight: 300, fontStyle: 'italic',
          color: v(cssVar.colorOnSurface),
        }}>
          {getGreeting()}
        </Typography>
        <Box sx={{
          width: 40, height: '0.5px',
          bgcolor: v(cssVar.colorOutlineVariant),
          mx: 'auto', my: '10px',
        }} />
        {nextAlarm ? (
          <>
            <Typography sx={{ fontSize: 13, fontWeight: 400, color: v(cssVar.colorOnSurfaceVariant) }}>
              Next alarm in{' '}
              <Box component="span" sx={{ fontWeight: 600 }}>
                {getTimeUntil(nextAlarm.time)}
              </Box>
            </Typography>
            <Typography sx={{ fontSize: 12, color: v(cssVar.colorOutline), mt: '3px' }}>
              {nextAlarm.time} · {getNextAlarmDay(nextAlarm)}
            </Typography>
          </>
        ) : (
          <Typography sx={{ fontSize: 13, color: v(cssVar.colorOutline) }}>
            No active alarms
          </Typography>
        )}
      </Box>

      {/* Alarm cards */}
      <Stack spacing={spacing.stackGap}>
        {alarms.map((alarm) => (
          <DsAlarmCard
            key={alarm.id}
            time={alarm.time}
            melody={alarm.melody}
            days={alarm.days}
            enabled={alarm.enabled}
            onToggle={() => toggleAlarm(alarm.id)}
            onClick={() => navigate(`/alarm/${alarm.id}`)}
          />
        ))}
        <DsAddAlarmCard onClick={() => navigate('/alarm/new')} />
      </Stack>
    </Box>
  );
}
