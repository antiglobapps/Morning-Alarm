import { Box, Card, CardContent, Divider, Stack, Typography } from '@mui/material';
import AlarmIcon from '@mui/icons-material/Alarm';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import { cssVar, v } from '../tokens';

// ── Helpers ────────────────────────────────────────────────

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

function getGreetingIcon() {
  const hour = new Date().getHours();
  if (hour >= 6 && hour < 18)
    return <WbSunnyIcon sx={{ fontSize: 20, color: v(cssVar.colorPrimary) }} />;
  return <NightsStayIcon sx={{ fontSize: 20, color: v(cssVar.colorPrimary) }} />;
}

// Next alarm day label (singular, not all days)
function getNextDay(): string {
  return 'Tomorrow, Wed';
}
const NEXT_TIME = '06:30';

// ── Variant A — Minimal: greeting + next alarm inline ──────

function HeaderA() {
  return (
    <Box sx={{ px: 2.5, pt: 4, pb: 2 }}>
      <Typography variant="titleLarge" sx={{ color: v(cssVar.colorOnSurface) }}>
        {getGreeting()}
      </Typography>
      <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mt: 1 }}>
        <AlarmIcon sx={{ fontSize: 18, color: v(cssVar.colorPrimary) }} />
        <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
          {NEXT_TIME}
        </Typography>
        <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOutline) }}>
          ·
        </Typography>
        <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
          {getNextDay()}
        </Typography>
      </Stack>
    </Box>
  );
}

// ── Variant B — Big time + greeting below ──────────────────

function HeaderB() {
  return (
    <Box sx={{ px: 2.5, pt: 4, pb: 2 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 0.5 }}>
        {getGreetingIcon()}
        <Typography variant="bodyLarge" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
          {getGreeting()}
        </Typography>
      </Stack>
      <Typography variant="displayLarge" sx={{ color: v(cssVar.colorOnSurface), lineHeight: 1, mb: 1 }}>
        {NEXT_TIME}
      </Typography>
      <Typography variant="labelMedium" sx={{ color: v(cssVar.colorPrimary) }}>
        {getNextDay()}
      </Typography>
    </Box>
  );
}

// ── Variant C — Card-style header ──────────────────────────

function HeaderC() {
  return (
    <Box sx={{ px: 2.5, pt: 4, pb: 2 }}>
      <Typography variant="titleLarge" sx={{ color: v(cssVar.colorOnSurface), mb: 2 }}>
        {getGreeting()}
      </Typography>
      <Card>
        <CardContent sx={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          py: 1.5, '&:last-child': { pb: 1.5 },
        }}>
          <Stack direction="row" alignItems="center" spacing={1.5}>
            <Box sx={{
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              width: 40, height: 40, borderRadius: '50%',
              bgcolor: v(cssVar.colorPrimaryContainer),
            }}>
              <AlarmIcon sx={{ fontSize: 20, color: v(cssVar.colorOnPrimaryContainer) }} />
            </Box>
            <Box>
              <Typography variant="labelSmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
                Next alarm
              </Typography>
              <Typography variant="headlineSmall" sx={{ color: v(cssVar.colorOnSurface), lineHeight: 1.2 }}>
                {NEXT_TIME}
              </Typography>
            </Box>
          </Stack>
          <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorPrimary) }}>
            {getNextDay()}
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}

// ── Variant D — Two-column split ───────────────────────────

function HeaderD() {
  return (
    <Box sx={{ px: 2.5, pt: 4, pb: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Stack direction="row" alignItems="center" spacing={0.75}>
            {getGreetingIcon()}
            <Typography variant="titleMedium" sx={{ color: v(cssVar.colorOnSurface) }}>
              {getGreeting()}
            </Typography>
          </Stack>
        </Box>
        <Box sx={{ textAlign: 'right' }}>
          <Typography variant="headlineLarge" sx={{ color: v(cssVar.colorPrimary), lineHeight: 1 }}>
            {NEXT_TIME}
          </Typography>
          <Typography variant="labelSmall" sx={{ color: v(cssVar.colorOnSurfaceVariant), mt: 0.25 }}>
            {getNextDay()}
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}

// ── Variant E — Stacked with divider ───────────────────────

function HeaderE() {
  return (
    <Box sx={{ px: 2.5, pt: 4, pb: 2 }}>
      <Typography variant="headlineSmall" sx={{ color: v(cssVar.colorOnSurface) }}>
        {getGreeting()}
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      <Stack direction="row" alignItems="baseline" spacing={1}>
        <Typography variant="displayMedium" sx={{ color: v(cssVar.colorPrimary), lineHeight: 1 }}>
          {NEXT_TIME}
        </Typography>
        <Box>
          <Typography variant="bodySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            Next alarm
          </Typography>
          <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurface) }}>
            {getNextDay()}
          </Typography>
        </Box>
      </Stack>
    </Box>
  );
}

// ── Screen ─────────────────────────────────────────────────

const VARIANTS = [
  { name: 'A — Minimal inline',   Component: HeaderA },
  { name: 'B — Big time',         Component: HeaderB },
  { name: 'C — Card header',      Component: HeaderC },
  { name: 'D — Two-column split', Component: HeaderD },
  { name: 'E — Stacked divider',  Component: HeaderE },
];

export default function DraftHeadersScreen() {
  return (
    <Box sx={{ height: '100%', overflow: 'auto', bgcolor: v(cssVar.colorBackground) }}>
      <Stack spacing={4} sx={{ px: 2, py: 3 }}>
        {VARIANTS.map(({ name, Component }) => (
          <Box key={name}>
            <Typography variant="titleMedium" sx={{ color: v(cssVar.colorOnSurface), mb: 1, px: 0.5 }}>
              {name}
            </Typography>
            <Box sx={{
              border: `1px solid ${v(cssVar.colorOutlineVariant)}`,
              borderRadius: 2,
              overflow: 'hidden',
            }}>
              <Component />
            </Box>
          </Box>
        ))}
      </Stack>
    </Box>
  );
}
