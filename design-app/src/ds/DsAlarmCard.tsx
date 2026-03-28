import { Box, Card, CardContent, Stack, Switch, Typography } from '@mui/material';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import { cssVar, v } from '../tokens';
import DsDayChipRow from './DsDayChipRow';

// MD3 Filled Card — Calm Minimal layout
// Shape:     medium (12dp) — via theme MuiCard default
// Border:    1px outlineVariant for subtle separation from gradient background
// Shadow:    level1 soft warm shadow
// Disabled:  onSurface 38% opacity per MD3
//
// Layout (no divider — compact calm style):
//   ┌────────────────────────────────────┐
//   │  07:00                      [switch] │
//   │  🎵 Sunrise Glow                    │
//   │  [Mo] [Tu] [We] [Th] [Fr]          │
//   └────────────────────────────────────┘
//
// Android Compose:
//   Card(
//     modifier = Modifier.clickable { onClick() },
//     colors = CardDefaults.cardColors(containerColor = md3Scheme.surface),
//     border = BorderStroke(1.dp, md3Scheme.outlineVariant),
//   ) {
//     Column(modifier = Modifier.padding(16.dp)) {
//       Row(verticalAlignment = Alignment.CenterVertically) {
//         Text(time, style = MaterialTheme.typography.headlineLarge,
//              modifier = Modifier.weight(1f))
//         Switch(checked = enabled, onCheckedChange = { onToggle() })
//       }
//       Row(verticalAlignment = Alignment.CenterVertically,
//           horizontalArrangement = Arrangement.spacedBy(6.dp),
//           modifier = Modifier.padding(top = 8.dp)) {
//         Icon(Icons.Default.MusicNote, contentDescription = null,
//              modifier = Modifier.size(16.dp),
//              tint = MaterialTheme.colorScheme.onSurfaceVariant)
//         Text(melody, style = MaterialTheme.typography.bodySmall,
//              color = MaterialTheme.colorScheme.onSurfaceVariant)
//       }
//       DayChipRow(selectedDays = days, size = ChipSize.Small,
//                  modifier = Modifier.padding(top = 8.dp))
//     }
//   }
//
// iOS SwiftUI:
//   RoundedRectangle(cornerRadius: 12, style: .continuous) card with:
//   VStack(alignment: .leading, spacing: 0) {
//     HStack {
//       Text(time).font(.system(size: 32))  // headlineLarge
//       Spacer()
//       Toggle("", isOn: $enabled).tint(Color(md3Scheme.primary))
//     }
//     HStack(spacing: 6) {
//       Image(systemName: "music.note")
//         .font(.system(size: 14))
//         .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//       Text(melody).font(.system(size: 14))
//         .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//     }
//     .padding(.top, 8)
//     DayChipRow(selectedDays: days, size: .small)
//       .padding(.top, 8)
//   }
//   .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous)
//     .stroke(Color(md3Scheme.outlineVariant), lineWidth: 1))
//   .opacity(enabled ? 1 : 0.38)

interface DsAlarmCardProps {
  time:     string;
  melody:   string;
  days:     string[];
  enabled:  boolean;
  onToggle: () => void;
  onClick:  () => void;
}

export default function DsAlarmCard({ time, melody, days, enabled, onToggle, onClick }: DsAlarmCardProps) {
  return (
    <Card
      sx={{
        opacity:    enabled ? 1 : 0.38,
        cursor:     'pointer',
        border:     `1px solid ${v(cssVar.colorOutlineVariant)}`,
        transition: 'opacity 0.2s, transform 0.2s',
        '&:hover':  { transform: 'scale(1.005)' },
      }}
      onClick={onClick}
    >
      <CardContent sx={{ px: 2, py: 2, '&:last-child': { pb: 2 } }}>
        {/* Time + switch */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="headlineLarge" sx={{ color: v(cssVar.colorOnSurface), lineHeight: 1.1 }}>
            {time}
          </Typography>
          <Switch
            checked={enabled}
            onChange={(e) => { e.stopPropagation(); onToggle(); }}
            onClick={(e) => e.stopPropagation()}
            color="primary"
          />
        </Box>

        {/* Melody — inline icon + text */}
        <Stack direction="row" alignItems="center" spacing={0.75} sx={{ mt: 1 }}>
          <MusicNoteIcon sx={{ fontSize: 16, color: v(cssVar.colorOnSurfaceVariant) }} />
          <Typography variant="bodySmall" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            {melody}
          </Typography>
        </Stack>

        {/* Days */}
        <Box sx={{ mt: 1 }}>
          <DsDayChipRow selectedDays={days} size="small" />
        </Box>
      </CardContent>
    </Card>
  );
}
