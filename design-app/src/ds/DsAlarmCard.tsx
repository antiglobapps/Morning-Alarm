import { Box, Card, CardContent, Switch, Typography } from '@mui/material';
import { cssVar, v } from '../tokens';
import DsDayChipRow from './DsDayChipRow';

// MD3 Filled Card
// Shape:     medium (12dp) — via theme MuiCard default
// Elevation: level1 (1dp) — via theme MuiCard defaultProps
// Disabled:  onSurface 38% opacity per MD3 (not arbitrary 0.55)
//
// Android Compose:
//   Card(
//     modifier = Modifier.clickable { onClick() },
//     colors = CardDefaults.cardColors(),  // surface / onSurface
//   ) {
//     Row(modifier = Modifier.padding(16.dp)) {
//       Column(modifier = Modifier.weight(1f)) {
//         Row(verticalAlignment = Alignment.Bottom) {
//           Text(time,  style = MaterialTheme.typography.headlineLarge)
//           Spacer(modifier = Modifier.width(8.dp))
//           Text(label, style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant)
//         }
//         DayChipRow(selectedDays = days, size = ChipSize.Small)
//       }
//       Switch(checked = enabled, onCheckedChange = { onToggle() })
//     }
//   }
//
// iOS SwiftUI:
//   RoundedRectangle(cornerRadius: 12, style: .continuous) card with:
//   HStack {
//     VStack(alignment: .leading) {
//       HStack(alignment: .lastTextBaseline) {
//         Text(time).font(.system(size: 32))       // headlineLarge
//         Text(label).font(.system(size: 16))      // bodyLarge
//           .foregroundStyle(Color(md3Scheme.onSurfaceVariant))
//       }
//       DayChipRow(selectedDays: days, size: .small)
//     }
//     Toggle("", isOn: $enabled).tint(Color(md3Scheme.primary))
//   }
//   .opacity(enabled ? 1 : 0.38)

interface DsAlarmCardProps {
  time:     string;
  label:    string;
  days:     string[];
  enabled:  boolean;
  onToggle: () => void;
  onClick:  () => void;
}

export default function DsAlarmCard({ time, label, days, enabled, onToggle, onClick }: DsAlarmCardProps) {
  return (
    <Card
      sx={{
        // MD3 disabled state: 38% opacity on content (not card surface)
        opacity:    enabled ? 1 : 0.38,
        cursor:     'pointer',
        transition: 'opacity 0.2s, transform 0.2s',
        '&:hover':  { transform: 'scale(1.01)' },
      }}
      onClick={onClick}
    >
      <CardContent sx={{ display: 'flex', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ flex: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
            {/* headlineLarge: 32sp / weight 400 — alarm time */}
            <Typography variant="headlineLarge" sx={{ lineHeight: 1.1, color: v(cssVar.colorOnSurface) }}>
              {time}
            </Typography>
            {/* bodyLarge: 16sp — alarm label, onSurfaceVariant per MD3 secondary content */}
            <Typography variant="bodyLarge" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
              {label}
            </Typography>
          </Box>
          <Box sx={{ mt: 1 }}>
            <DsDayChipRow selectedDays={days} size="small" />
          </Box>
        </Box>
        {/* MD3 Switch — theme override sets correct track/thumb colors */}
        {/* Android: Switch(checked, onCheckedChange)                    */}
        {/* iOS:     Toggle("", isOn: $enabled).tint(primary)            */}
        <Switch
          checked={enabled}
          onChange={(e) => { e.stopPropagation(); onToggle(); }}
          onClick={(e) => e.stopPropagation()}
          color="primary"
        />
      </CardContent>
    </Card>
  );
}
