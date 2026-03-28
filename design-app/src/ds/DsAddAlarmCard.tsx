import { Card, CardContent, Stack, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { cssVar, v } from '../tokens';

// Ghost dashed placeholder card — last item in alarm list
// Shape:     medium (12dp) — via theme MuiCard default
// Dashed border, transparent background, 55% opacity → 75% on hover
//
// Android Compose:
//   Card(
//     modifier = Modifier.clickable { onClick() },
//     colors = CardDefaults.cardColors(containerColor = Color.Transparent),
//     border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
//     // dashed border: use drawBehind with PathEffect.dashPathEffect
//   ) {
//     Column(
//       modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
//       horizontalAlignment = Alignment.CenterHorizontally,
//     ) {
//       Icon(Icons.Default.Add, contentDescription = "Add alarm",
//            modifier = Modifier.size(32.dp).alpha(0.55f),
//            tint = MaterialTheme.colorScheme.onSurfaceVariant)
//       Text("Add alarm", style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.alpha(0.55f))
//     }
//   }
//
// iOS SwiftUI:
//   Button(action: { onAdd() }) {
//     VStack(spacing: 4) {
//       Image(systemName: "plus")
//         .font(.system(size: 32))
//       Text("Add alarm")
//         .font(.system(size: 14))  // bodyMedium
//     }
//     .frame(maxWidth: .infinity)
//     .padding(.vertical, 32)
//     .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//     .opacity(0.55)
//   }
//   .background(
//     RoundedRectangle(cornerRadius: 12, style: .continuous)
//       .strokeBorder(style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
//       .foregroundColor(Color(md3Scheme.outlineVariant))
//   )

interface DsAddAlarmCardProps {
  onClick: () => void;
}

export default function DsAddAlarmCard({ onClick }: DsAddAlarmCardProps) {
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
