import { Chip, Stack } from '@mui/material';
import { spacing } from '../tokens';

// MD3 Filter Chip row — mood selector (single-select / radio group)
// Horizontally scrollable chip group.
// Shape:     small (8dp) — via theme MuiChip override
// Height:    32dp — via theme MuiChip override
// Active:    secondaryContainer / onSecondaryContainer — via theme Chip filled override
// Inactive:  outlineVariant border, onSurfaceVariant text — via theme Chip outlined override
// minWidth:  64dp — mood labels may be short; ensures adequate touch target
//
// Android Compose:
//   LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//     items(moods) { mood ->
//       FilterChip(
//         selected    = mood == selected,
//         onClick     = { onSelect(mood) },
//         label       = { Text(mood, style = MaterialTheme.typography.labelMedium) },
//         leadingIcon = if (mood == selected) {
//           { Icon(Icons.Default.Check, contentDescription = null,
//                  modifier = Modifier.size(FilterChipDefaults.IconSize)) }
//         } else null,
//       )
//     }
//   }
//
// iOS SwiftUI (no native chip — custom toggle button, scrollable):
//   ScrollView(.horizontal, showsIndicators: false) {
//     HStack(spacing: 8) {
//       ForEach(moods) { mood in
//         let active = mood == selected
//         Text(mood)
//           .font(.system(size: 12, weight: .medium))  // labelMedium
//           .padding(.horizontal, 8).padding(.vertical, 6)
//           .frame(minWidth: 64)
//           .background(active ? Color(md3Scheme.secondaryContainer) : Color.clear)
//           .foregroundColor(active ? Color(md3Scheme.onSecondaryContainer)
//                                   : Color(md3Scheme.onSurfaceVariant))
//           .overlay(RoundedRectangle(cornerRadius: 8, style: .continuous)
//             .stroke(active ? Color.clear : Color(md3Scheme.outlineVariant)))
//           .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
//           .onTapGesture { onSelect(mood) }
//       }
//     }.padding(.bottom, 4)
//   }

interface DsMoodChipRowProps {
  moods:    string[];
  selected: string;
  onSelect: (mood: string) => void;
}

export default function DsMoodChipRow({ moods, selected, onSelect }: DsMoodChipRowProps) {
  return (
    <Stack direction="row" spacing={spacing.moodChipGap} sx={{ overflowX: 'auto', pb: 0.5 }}>
      {moods.map((mood) => (
        <Chip
          key={mood}
          label={mood}
          variant={selected === mood ? 'filled' : 'outlined'}
          onClick={() => onSelect(mood)}
          // minWidth 64dp — short mood labels still get adequate touch target
          sx={{ minWidth: 64, flexShrink: 0 }}
        />
      ))}
    </Stack>
  );
}
