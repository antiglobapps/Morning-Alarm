import { Box, Card, CardContent, Divider, Stack, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import AlarmIcon from '@mui/icons-material/Alarm';
import AddAlarmIcon from '@mui/icons-material/AddAlarm';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import { cssVar, v } from '../tokens';
import DsDayChipRow from '../ds/DsDayChipRow';

// ── Variant A — Ghost card with centered "+" ───────────────
// Looks like a faded alarm card with a large "+" overlay

function AddCardA({ onClick }: { onClick: () => void }) {
  return (
    <Card
      sx={{
        opacity: 0.55,
        cursor: 'pointer',
        border: `1.5px dashed ${v(cssVar.colorOutlineVariant)}`,
        bgcolor: 'transparent',
        boxShadow: 'none',
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
          <AddIcon sx={{ fontSize: 32, color: v(cssVar.colorOnSurfaceVariant) }} />
          <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            Add alarm
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}

// ── Variant B — Skeleton card mimicking alarm card layout ──
// Same structure as DsAlarmCard but with placeholder content

function AddCardB({ onClick }: { onClick: () => void }) {
  return (
    <Card
      sx={{
        opacity: 0.45,
        cursor: 'pointer',
        transition: 'opacity 0.2s',
        '&:hover': { opacity: 0.65 },
      }}
      onClick={onClick}
    >
      <CardContent sx={{ py: 2, '&:last-child': { pb: 2 } }}>
        {/* Row 1: placeholder time + add icon instead of switch */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="displaySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant), lineHeight: 1 }}>
            --:--
          </Typography>
          <Box sx={{
            width: 40, height: 40, borderRadius: '50%',
            bgcolor: v(cssVar.colorPrimaryContainer),
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <AddIcon sx={{ fontSize: 20, color: v(cssVar.colorOnPrimaryContainer) }} />
          </Box>
        </Box>
        <Divider sx={{ my: 1.5 }} />
        {/* Row 2: placeholder melody */}
        <Stack direction="row" alignItems="center" spacing={1}>
          <Box sx={{
            width: 32, height: 32, borderRadius: '50%',
            bgcolor: v(cssVar.colorSecondaryContainer), opacity: 0.5,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <MusicNoteIcon sx={{ fontSize: 16, color: v(cssVar.colorOnSecondaryContainer) }} />
          </Box>
          <Typography variant="bodyLarge" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            Choose melody
          </Typography>
        </Stack>
        {/* Row 3: placeholder days */}
        <Box sx={{ mt: 1.5, opacity: 0.4 }}>
          <DsDayChipRow selectedDays={[]} size="small" />
        </Box>
      </CardContent>
    </Card>
  );
}

// ── Variant C — Outlined card with alarm icon + text ───────
// Clean outlined style, alarm icon prominent

function AddCardC({ onClick }: { onClick: () => void }) {
  return (
    <Card
      variant="outlined"
      sx={{
        cursor: 'pointer',
        borderColor: v(cssVar.colorOutlineVariant),
        borderStyle: 'dashed',
        bgcolor: 'transparent',
        transition: 'background-color 0.2s',
        '&:hover': { bgcolor: v(cssVar.colorSurfaceVariant) },
      }}
      onClick={onClick}
    >
      <CardContent sx={{
        display: 'flex', alignItems: 'center', gap: 2,
        py: 2.5, '&:last-child': { pb: 2.5 },
      }}>
        <Box sx={{
          width: 48, height: 48, borderRadius: 2,
          bgcolor: v(cssVar.colorPrimaryContainer),
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <AddAlarmIcon sx={{ fontSize: 24, color: v(cssVar.colorOnPrimaryContainer) }} />
        </Box>
        <Box sx={{ flex: 1 }}>
          <Typography variant="titleMedium" sx={{ color: v(cssVar.colorOnSurface) }}>
            New alarm
          </Typography>
          <Typography variant="bodySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            Tap to set time and melody
          </Typography>
        </Box>
        <AddIcon sx={{ fontSize: 20, color: v(cssVar.colorOnSurfaceVariant) }} />
      </CardContent>
    </Card>
  );
}

// ── Variant D — Filled card matching alarm style, "+" time ─
// Same visual weight as real cards but clearly a placeholder

function AddCardD({ onClick }: { onClick: () => void }) {
  return (
    <Card
      sx={{
        cursor: 'pointer',
        opacity: 0.6,
        transition: 'opacity 0.2s, transform 0.2s',
        '&:hover': { opacity: 0.8, transform: 'scale(1.01)' },
      }}
      onClick={onClick}
    >
      <CardContent sx={{ py: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Stack direction="row" alignItems="center" spacing={1}>
            <AlarmIcon sx={{ fontSize: 28, color: v(cssVar.colorPrimary) }} />
            <Typography variant="headlineMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
              Set time
            </Typography>
          </Stack>
          <Box sx={{
            width: 36, height: 36, borderRadius: '50%',
            border: `2px solid ${v(cssVar.colorPrimary)}`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <AddIcon sx={{ fontSize: 18, color: v(cssVar.colorPrimary) }} />
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

// ── Variant E — Subtle tinted surface with "+" badge ───────
// PrimaryContainer tint, full-width, lightweight

function AddCardE({ onClick }: { onClick: () => void }) {
  return (
    <Card
      sx={{
        cursor: 'pointer',
        bgcolor: v(cssVar.colorPrimaryContainer),
        opacity: 0.5,
        boxShadow: 'none',
        transition: 'opacity 0.2s',
        '&:hover': { opacity: 0.7 },
      }}
      onClick={onClick}
    >
      <CardContent sx={{
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1,
        py: 2.5, '&:last-child': { pb: 2.5 },
      }}>
        <AddIcon sx={{ fontSize: 20, color: v(cssVar.colorOnPrimaryContainer) }} />
        <Typography variant="titleSmall" sx={{ color: v(cssVar.colorOnPrimaryContainer) }}>
          Add new alarm
        </Typography>
      </CardContent>
    </Card>
  );
}

// ── Screen ─────────────────────────────────────────────────

const VARIANTS = [
  { name: 'A — Ghost dashed',       Component: AddCardA },
  { name: 'B — Skeleton mimic',     Component: AddCardB },
  { name: 'C — Outlined + icon',    Component: AddCardC },
  { name: 'D — Filled with "Set time"', Component: AddCardD },
  { name: 'E — Tinted surface',     Component: AddCardE },
];

export default function DraftAddCardScreen() {
  return (
    <Box sx={{ height: '100%', overflow: 'auto', bgcolor: v(cssVar.colorBackground) }}>
      <Stack spacing={4} sx={{ px: 2, py: 3 }}>
        {VARIANTS.map(({ name, Component }) => (
          <Box key={name}>
            <Typography variant="titleMedium" sx={{ color: v(cssVar.colorOnSurface), mb: 1.5 }}>
              {name}
            </Typography>
            <Component onClick={() => {}} />
          </Box>
        ))}
      </Stack>
    </Box>
  );
}
