import { useState } from 'react';
import { Box, Typography, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { DsAlarmCard, DsFab } from '../ds';
import { spacing } from '../tokens';

interface Alarm {
  id: string;
  time: string;
  label: string;
  days: string[];
  enabled: boolean;
}

const MOCK_ALARMS: Alarm[] = [
  {
    id: '1',
    time: '06:30',
    label: 'Soft Sunrise',
    days: ['Mo', 'Tu', 'We', 'Th', 'Fr'],
    enabled: true,
  },
  {
    id: '2',
    time: '08:00',
    label: 'Forest Calm',
    days: ['Sa', 'Su'],
    enabled: false,
  },
  {
    id: '3',
    time: '07:15',
    label: 'Energy Morning',
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

function getCurrentTime(): string {
  return new Date().toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
}

function getNextAlarm(alarms: Alarm[]): Alarm | undefined {
  return alarms.find((a) => a.enabled);
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
    <Box sx={{ px: spacing.screenHorizontal, pt: spacing.screenTop }}>
      {/* Current time — displayMedium: large expressive clock */}
      <Typography variant="displayMedium" sx={{ mb: 0.5 }}>
        {getCurrentTime()}
      </Typography>
      {/* Greeting — titleLarge: secondary screen label */}
      <Typography variant="titleLarge" color="text.secondary" sx={{ mb: spacing.sectionSpacing }}>
        {getGreeting()} ☀
      </Typography>

      {/* Next alarm indicator */}
      {nextAlarm && (
        <Box sx={{ mb: spacing.sectionSpacing }}>
          {/* labelMedium: small caption */}
          <Typography variant="labelMedium" color="text.secondary">
            Next alarm
          </Typography>
          {/* headlineSmall: prominent next alarm time */}
          <Typography variant="headlineSmall" color="primary.main">
            {nextAlarm.time} · {nextAlarm.days.length === 7 ? 'Every day' : nextAlarm.days.join(' ')}
          </Typography>
        </Box>
      )}

      {/* Alarm cards */}
      <Stack spacing={spacing.stackGap}>
        {alarms.map((alarm) => (
          <DsAlarmCard
            key={alarm.id}
            time={alarm.time}
            label={alarm.label}
            days={alarm.days}
            enabled={alarm.enabled}
            onToggle={() => toggleAlarm(alarm.id)}
            onClick={() => navigate(`/alarm/${alarm.id}`)}
          />
        ))}
      </Stack>

      {/* FAB */}
      <DsFab
        onClick={() => navigate('/alarm/new')}
        sx={{
          position: 'fixed',
          bottom: 96,
          right: 'calc(50% - 195px)',
        }}
      />
    </Box>
  );
}
