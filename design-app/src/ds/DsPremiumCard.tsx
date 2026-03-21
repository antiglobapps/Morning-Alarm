import { Box, Card, CardContent, Stack, Typography } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import { cssVar, v } from '../tokens';
import DsPrimaryButton from './DsPrimaryButton';

// MD3 Filled Card — brand gradient variant
// Gradient uses MD3 color roles (primaryContainer + tertiaryContainer) to ensure:
//   - Contrast compliance (onPrimaryContainer text on primaryContainer bg)
//   - Theme-awareness (both morning and night schemes provide correct tones)
//   - AAA contrast guaranteed by MD3 tonal ladder (T10 on T90 = ~12:1 ratio)
//
// Shape:     medium (12dp) — via theme
// Elevation: level2 — elevated/featured card
//
// Android Compose:
//   Card(
//     colors = CardDefaults.cardColors(
//       containerColor = Color.Transparent,  // gradient via Brush
//     ),
//   ) {
//     Box(
//       modifier = Modifier.background(
//         brush = Brush.linearGradient(
//           colors = listOf(
//             MaterialTheme.colorScheme.primaryContainer,
//             MaterialTheme.colorScheme.tertiaryContainer,
//           )
//         )
//       )
//     ) {
//       Column(horizontalAlignment = Alignment.CenterHorizontally,
//              modifier = Modifier.padding(24.dp)) {
//         Icon(Icons.Default.Star, ..., tint = MaterialTheme.colorScheme.onPrimaryContainer)
//         Text("Premium", style = MaterialTheme.typography.headlineSmall,
//              color = MaterialTheme.colorScheme.onPrimaryContainer)
//         features.forEach { Text("✦ $it", color = MaterialTheme.colorScheme.onPrimaryContainer) }
//         Button(onClick = onUpgrade) { Text("Upgrade") }
//       }
//     }
//   }
//
// iOS SwiftUI:
//   ZStack {
//     LinearGradient(
//       colors: [Color(md3Scheme.primaryContainer), Color(md3Scheme.tertiaryContainer)],
//       startPoint: .topLeading, endPoint: .bottomTrailing
//     )
//     VStack(spacing: 12) {
//       Image(systemName: "star.fill")
//         .foregroundColor(Color(md3Scheme.onPrimaryContainer))
//       Text("Premium").font(.system(size: 24)) // headlineSmall
//         .foregroundColor(Color(md3Scheme.onPrimaryContainer))
//       ForEach(features) { f in
//         Text("✦ \(f)").foregroundColor(Color(md3Scheme.onPrimaryContainer))
//       }
//       Button("Upgrade") { onUpgrade?() }.buttonStyle(.borderedProminent)
//     }.padding(24)
//   }
//   .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

interface DsPremiumCardProps {
  features:   string[];
  onUpgrade?: () => void;
}

export default function DsPremiumCard({ features, onUpgrade }: DsPremiumCardProps) {
  return (
    <Card
      elevation={2}
      sx={{
        background: `linear-gradient(135deg, ${v(cssVar.colorPrimaryContainer)} 0%, ${v(cssVar.colorTertiaryContainer)} 100%)`,
      }}
    >
      <CardContent sx={{ textAlign: 'center', py: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-end', gap: 0.5, mb: 2 }}>
          <StarIcon sx={{ fontSize: 24, mb: '2px', color: v(cssVar.colorOnPrimaryContainer), opacity: 0.7 }} />
          <StarIcon sx={{ fontSize: 40,            color: v(cssVar.colorOnPrimaryContainer) }} />
          <StarIcon sx={{ fontSize: 24, mb: '2px', color: v(cssVar.colorOnPrimaryContainer), opacity: 0.7 }} />
        </Box>
        <Typography variant="headlineSmall" sx={{ mb: 2, color: v(cssVar.colorOnPrimaryContainer) }}>
          Premium
        </Typography>
        <Stack spacing={1} sx={{ textAlign: 'left', mb: 3, px: 2 }}>
          {features.map((text) => (
            <Box key={text} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography sx={{ color: v(cssVar.colorOnPrimaryContainer) }}>✦</Typography>
              <Typography variant="bodyLarge" sx={{ color: v(cssVar.colorOnPrimaryContainer) }}>
                {text}
              </Typography>
            </Box>
          ))}
        </Stack>
        <DsPrimaryButton label="Upgrade" onClick={onUpgrade} />
      </CardContent>
    </Card>
  );
}
