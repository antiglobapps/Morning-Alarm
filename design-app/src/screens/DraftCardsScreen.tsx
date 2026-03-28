import { useState } from 'react';
import {
  Box, Card, CardContent, Switch, Typography, Stack, Chip, Divider,
} from '@mui/material';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import AlarmIcon from '@mui/icons-material/Alarm';
import { cssVar, v } from '../tokens';
import DsDayChipRow from '../ds/DsDayChipRow';

// ── Mock data ──────────────────────────────────────────────

interface AlarmData {
  time: string;
  melody: string;
  days: string[];
  enabled: boolean;
}

const ALARMS: AlarmData[] = [
  { time: '07:00', melody: 'Sunrise Glow',   days: ['Mo', 'Tu', 'We', 'Th', 'Fr'], enabled: true },
  { time: '08:30', melody: 'Ocean Breeze',   days: ['Sa', 'Su'],                    enabled: false },
  { time: '06:15', melody: 'Forest Morning', days: ['Mo', 'We', 'Fr'],              enabled: true },
  { time: '09:00', melody: 'Soft Piano',     days: ['Sa'],                           enabled: true },
  { time: '07:45', melody: 'Bird Song',      days: ['Mo', 'Tu', 'We', 'Th', 'Fr'], enabled: false },
];

// ── Shared day dots (compact helpers) ──────────────────────

const ALL_DAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];

function DayDots({ days }: { days: string[] }) {
  return (
    <Stack direction="row" spacing={0.5}>
      {ALL_DAYS.map((d) => (
        <Box
          key={d}
          sx={{
            width: 6, height: 6, borderRadius: '50%',
            bgcolor: days.includes(d) ? v(cssVar.colorPrimary) : v(cssVar.colorOutlineVariant),
          }}
        />
      ))}
    </Stack>
  );
}

function DayLetters({ days }: { days: string[] }) {
  return (
    <Stack direction="row" spacing={0.25}>
      {ALL_DAYS.map((d) => (
        <Typography
          key={d}
          variant="labelSmall"
          sx={{
            width: 20, textAlign: 'center',
            color: days.includes(d) ? v(cssVar.colorPrimary) : v(cssVar.colorOutlineVariant),
            fontWeight: days.includes(d) ? 700 : 400,
          }}
        >
          {d.charAt(0)}
        </Typography>
      ))}
    </Stack>
  );
}

// ── Variant A — Compact single-row ─────────────────────────

function CardVariantA({ alarm }: { alarm: AlarmData; onToggle: () => void }) {
  return (
    <Card sx={{ opacity: alarm.enabled ? 1 : 0.38 }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5, py: 1.5, '&:last-child': { pb: 1.5 } }}>
        <Typography variant="headlineSmall" sx={{ color: v(cssVar.colorOnSurface), minWidth: 72 }}>
          {alarm.time}
        </Typography>
        <Divider orientation="vertical" flexItem />
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography variant="bodySmall" noWrap sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            {alarm.melody}
          </Typography>
          <DayDots days={alarm.days} />
        </Box>
        <Switch checked={alarm.enabled} size="small" color="primary" />
      </CardContent>
    </Card>
  );
}

// ── Variant B — Accent left border ─────────────────────────

function CardVariantB({ alarm }: { alarm: AlarmData; onToggle: () => void }) {
  return (
    <Card
      sx={{
        opacity: alarm.enabled ? 1 : 0.38,
        borderLeft: `4px solid`,
        borderLeftColor: alarm.enabled ? v(cssVar.colorPrimary) : v(cssVar.colorOutlineVariant),
      }}
    >
      <CardContent sx={{ display: 'flex', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ flex: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
            <Typography variant="headlineMedium" sx={{ color: v(cssVar.colorOnSurface) }}>
              {alarm.time}
            </Typography>
          </Box>
          <Stack direction="row" alignItems="center" spacing={0.5} sx={{ mt: 0.5 }}>
            <MusicNoteIcon sx={{ fontSize: 14, color: v(cssVar.colorOnSurfaceVariant) }} />
            <Typography variant="bodySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
              {alarm.melody}
            </Typography>
          </Stack>
          <Box sx={{ mt: 1 }}>
            <DayLetters days={alarm.days} />
          </Box>
        </Box>
        <Switch checked={alarm.enabled} color="primary" />
      </CardContent>
    </Card>
  );
}

// ── Variant C — Split layout (time left, details right) ────

function CardVariantC({ alarm }: { alarm: AlarmData; onToggle: () => void }) {
  return (
    <Card sx={{ opacity: alarm.enabled ? 1 : 0.38 }}>
      <CardContent sx={{ display: 'flex', gap: 2, py: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          bgcolor: v(cssVar.colorPrimaryContainer), borderRadius: 2, px: 2, py: 1,
        }}>
          <Typography variant="headlineLarge" sx={{ color: v(cssVar.colorOnPrimaryContainer), lineHeight: 1 }}>
            {alarm.time}
          </Typography>
        </Box>
        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <Stack direction="row" alignItems="center" spacing={0.5}>
            <MusicNoteIcon sx={{ fontSize: 16, color: v(cssVar.colorOnSurfaceVariant) }} />
            <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
              {alarm.melody}
            </Typography>
          </Stack>
          <Box sx={{ mt: 0.5 }}>
            <DsDayChipRow selectedDays={alarm.days} size="small" />
          </Box>
        </Box>
        <Switch checked={alarm.enabled} color="primary" sx={{ alignSelf: 'center' }} />
      </CardContent>
    </Card>
  );
}

// ── Variant D — Outlined card ──────────────────────────────

function CardVariantD({ alarm }: { alarm: AlarmData; onToggle: () => void }) {
  return (
    <Card
      variant="outlined"
      sx={{
        opacity: alarm.enabled ? 1 : 0.38,
        borderColor: v(cssVar.colorOutlineVariant),
        bgcolor: 'transparent',
      }}
    >
      <CardContent sx={{ display: 'flex', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
        <AlarmIcon sx={{ fontSize: 20, color: v(cssVar.colorPrimary), mr: 1.5 }} />
        <Box sx={{ flex: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
            <Typography variant="headlineMedium" sx={{ color: v(cssVar.colorOnSurface) }}>
              {alarm.time}
            </Typography>
            <Typography variant="bodySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
              {alarm.melody}
            </Typography>
          </Box>
          <Box sx={{ mt: 0.5 }}>
            <DayLetters days={alarm.days} />
          </Box>
        </Box>
        <Chip
          label={alarm.enabled ? 'ON' : 'OFF'}
          size="small"
          sx={{
            bgcolor: alarm.enabled ? v(cssVar.colorPrimaryContainer) : 'transparent',
            color: alarm.enabled ? v(cssVar.colorOnPrimaryContainer) : v(cssVar.colorOnSurfaceVariant),
            border: alarm.enabled ? 'none' : `1px solid ${v(cssVar.colorOutlineVariant)}`,
            fontWeight: 600,
          }}
        />
      </CardContent>
    </Card>
  );
}

// ── Variant E — Expanded / melody-prominent ────────────────

function CardVariantE({ alarm }: { alarm: AlarmData; onToggle: () => void }) {
  return (
    <Card sx={{ opacity: alarm.enabled ? 1 : 0.38 }}>
      <CardContent sx={{ py: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="displaySmall" sx={{ color: v(cssVar.colorOnSurface), lineHeight: 1 }}>
            {alarm.time}
          </Typography>
          <Switch checked={alarm.enabled} color="primary" />
        </Box>
        <Divider sx={{ my: 1.5 }} />
        <Stack direction="row" alignItems="center" spacing={1}>
          <Box sx={{
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            width: 32, height: 32, borderRadius: '50%',
            bgcolor: v(cssVar.colorSecondaryContainer),
          }}>
            <MusicNoteIcon sx={{ fontSize: 16, color: v(cssVar.colorOnSecondaryContainer) }} />
          </Box>
          <Typography variant="bodyLarge" sx={{ color: v(cssVar.colorOnSurface) }}>
            {alarm.melody}
          </Typography>
        </Stack>
        <Box sx={{ mt: 1.5 }}>
          <DsDayChipRow selectedDays={alarm.days} size="small" />
        </Box>
      </CardContent>
    </Card>
  );
}

// ── Screen ─────────────────────────────────────────────────

const VARIANTS = [
  { name: 'A — Compact',          Component: CardVariantA },
  { name: 'B — Accent Border',    Component: CardVariantB },
  { name: 'C — Split Layout',     Component: CardVariantC },
  { name: 'D — Outlined',         Component: CardVariantD },
  { name: 'E — Expanded',         Component: CardVariantE },
];

export default function DraftCardsScreen() {
  const [alarms, setAlarms] = useState(ALARMS);

  const toggle = (idx: number) =>
    setAlarms((prev) => prev.map((a, i) => (i === idx ? { ...a, enabled: !a.enabled } : a)));

  return (
    <Box sx={{ height: '100%', overflow: 'auto', bgcolor: v(cssVar.colorBackground) }}>
      <Stack spacing={4} sx={{ px: 2, py: 3 }}>
        {VARIANTS.map(({ name, Component }) => (
          <Box key={name}>
            <Typography variant="titleMedium" sx={{ color: v(cssVar.colorOnSurface), mb: 1.5 }}>
              {name}
            </Typography>
            <Stack spacing={1.5}>
              {alarms.map((alarm, idx) => (
                <Component key={idx} alarm={alarm} onToggle={() => toggle(idx)} />
              ))}
            </Stack>
          </Box>
        ))}
      </Stack>
    </Box>
  );
}
