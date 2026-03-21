import { Box, Card, CardActionArea, CardContent, Typography } from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { cssVar, v } from '../tokens';

// MD3 Outlined Card — settings list item
// Shape:     medium (12dp) — via theme, variant="outlined" → no shadow
// Elevation: level0 — outlined cards have no shadow per MD3 spec
// Icon:      onSurfaceVariant (secondary content, not interactive primary)
// Value:     tertiary for premium badge (distinct from primary/secondary interaction)
// Chevron:   onSurfaceVariant — disclosure affordance, non-interactive color
//
// Android Compose:
//   Use ListItem for better MD3 alignment:
//   ListItem(
//     headlineContent   = { Text(label, style = MaterialTheme.typography.bodyLarge) },
//     leadingContent    = { Icon(icon, contentDescription = null,
//                               tint = MaterialTheme.colorScheme.onSurfaceVariant) },
//     trailingContent   = {
//       Row(verticalAlignment = Alignment.CenterVertically) {
//         Text(value, style = MaterialTheme.typography.bodyMedium,
//              color = if (premium) MaterialTheme.colorScheme.tertiary
//                      else MaterialTheme.colorScheme.onSurfaceVariant)
//         Icon(Icons.AutoMirrored.Default.ChevronRight, contentDescription = null,
//              tint = MaterialTheme.colorScheme.onSurfaceVariant)
//       }
//     },
//     modifier = Modifier.clickable { onClick?.invoke() }
//   )
//
// iOS SwiftUI (inside Form / List):
//   LabeledContent {
//     Text(value)
//       .foregroundColor(premium ? Color(md3Scheme.tertiary) : Color(md3Scheme.onSurfaceVariant))
//   } label: {
//     Label(label, systemImage: iconName)
//       .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//   }
//   Note: iOS List adds its own disclosure indicator — no ChevronRightIcon needed.
//         Use NavigationLink for tappable rows in a NavigationStack.

interface DsSettingRowProps {
  icon:     React.ReactNode;
  label:    string;
  value:    string;
  premium?: boolean;
  onClick?: () => void;
}

export default function DsSettingRow({ icon, label, value, premium = false, onClick }: DsSettingRowProps) {
  return (
    <Card variant="outlined">
      <CardActionArea onClick={onClick}>
        <CardContent
          sx={{
            display:    'flex',
            alignItems: 'center',
            py:         1.5,
            '&:last-child': { pb: 1.5 },
          }}
        >
          {/* Icon: onSurfaceVariant — secondary content, not primary action */}
          <Box sx={{ color: v(cssVar.colorOnSurfaceVariant), mr: 2, display: 'flex' }}>{icon}</Box>

          {/* Label: bodyLarge */}
          <Typography variant="bodyLarge" sx={{ flex: 1, color: v(cssVar.colorOnSurface) }}>
            {label}
          </Typography>

          {/* Value: tertiary for premium badge, onSurfaceVariant otherwise */}
          <Typography
            variant="bodyMedium"
            sx={{
              mr:    0.5,
              color: premium ? v(cssVar.colorTertiary) : v(cssVar.colorOnSurfaceVariant),
            }}
          >
            {value}
          </Typography>

          {/* Chevron: onSurfaceVariant disclosure indicator */}
          <ChevronRightIcon sx={{ color: v(cssVar.colorOnSurfaceVariant), fontSize: 20 }} />
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
