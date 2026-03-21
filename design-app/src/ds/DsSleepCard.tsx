import { Box, Card, CardContent, CardMedia, Typography } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { cssVar, v } from '../tokens';

// MD3 Filled Card with media
// Shape:     medium (12dp) — via theme
// Elevation: level1 (1dp) — via theme
// Image:     120dp height (content image area, above content row)
//
// Android Compose:
//   Card(modifier = Modifier.clickable { onClick?.invoke() }) {
//     Column {
//       Image(painter, contentDescription = null,
//             modifier = Modifier.fillMaxWidth().height(120.dp),
//             contentScale = ContentScale.Crop)
//       Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
//           verticalAlignment = Alignment.CenterVertically) {
//         Column(modifier = Modifier.weight(1f)) {
//           Text(title,    style = MaterialTheme.typography.titleLarge)
//           Text(subtitle, style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant)
//         }
//         Icon(Icons.Default.PlayArrow,
//              contentDescription = null,
//              tint = MaterialTheme.colorScheme.onSurfaceVariant)
//       }
//     }
//   }
//
// iOS SwiftUI:
//   RoundedRectangle(cornerRadius: 12, style: .continuous) card:
//   VStack(spacing: 0) {
//     AsyncImage(url: URL(string: image)) { img in img.resizable().scaledToFill() }
//       .frame(height: 120).clipped()
//     HStack {
//       VStack(alignment: .leading, spacing: 4) {
//         Text(title).font(.system(size: 22))    // titleLarge
//         Text(subtitle).font(.system(size: 14)) // bodyMedium
//           .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//       }
//       Spacer()
//       Image(systemName: "play.fill")
//         .foregroundColor(Color(md3Scheme.onSurfaceVariant))
//     }.padding(16)
//   }

interface DsSleepCardProps {
  title:    string;
  subtitle: string;
  image:    string;
  onClick?: () => void;
}

export default function DsSleepCard({ title, subtitle, image, onClick }: DsSleepCardProps) {
  return (
    <Card sx={{ overflow: 'hidden', cursor: onClick ? 'pointer' : undefined }} onClick={onClick}>
      <CardMedia component="img" height="120" image={image} alt={title} sx={{ objectFit: 'cover' }} />
      <CardContent sx={{ display: 'flex', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
        <Box sx={{ flex: 1 }}>
          {/* titleLarge: 22sp / weight 400 */}
          <Typography variant="titleLarge" sx={{ color: v(cssVar.colorOnSurface) }}>{title}</Typography>
          {/* bodyMedium: 14sp / onSurfaceVariant */}
          <Typography variant="bodyMedium" sx={{ color: v(cssVar.colorOnSurfaceVariant) }}>
            {subtitle}
          </Typography>
        </Box>
        {/* onSurfaceVariant for non-interactive content icon per MD3 */}
        <PlayArrowIcon sx={{ color: v(cssVar.colorOnSurfaceVariant), fontSize: 28 }} />
      </CardContent>
    </Card>
  );
}
