import { Chip, Stack } from '@mui/material';
import { spacing } from '../tokens';

// MD3 Filter Chip row — day-of-week selector
// Shape:     small (8dp) — via theme MuiChip override
// Height:    32dp — via theme MuiChip override
// Active:    secondaryContainer / onSecondaryContainer — via theme Chip filled override
// Inactive:  outlineVariant border, onSurfaceVariant text — via theme Chip outlined override
//
// MD3 FilterChip shows a checkmark icon when selected (leadingIcon).
// Web prototype omits the checkmark for visual simplicity.
//
// Android Compose:
//   Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//     ALL_DAYS.forEach { day ->
//       FilterChip(
//         selected  = day in selectedDays,
//         onClick   = { onToggle?.invoke(day) },
//         label     = { Text(day, style = MaterialTheme.typography.labelMedium) },
//         leadingIcon = if (day in selectedDays) {
//           { Icon(Icons.Default.Check, contentDescription = null,
//                  modifier = Modifier.size(FilterChipDefaults.IconSize)) }
//         } else null,
//       )
//     }
//   }
//
// iOS SwiftUI (no native chip — custom toggle button):
//   HStack(spacing: 4) {
//     ForEach(ALL_DAYS) { day in
//       let active = selectedDays.contains(day)
//       Text(day)
//         .font(.system(size: 12, weight: .medium))  // labelMedium
//         .padding(.horizontal, 8).padding(.vertical, 6)
//         .background(active ? Color(md3Scheme.secondaryContainer) : Color.clear)
//         .foregroundColor(active ? Color(md3Scheme.onSecondaryContainer)
//                                 : Color(md3Scheme.onSurfaceVariant))
//         .overlay(RoundedRectangle(cornerRadius: 8, style: .continuous)
//           .stroke(active ? Color.clear : Color(md3Scheme.outlineVariant)))
//         .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
//         .onTapGesture { onToggle?(day) }
//     }
//   }

const ALL_DAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];

interface DsDayChipRowProps {
  selectedDays: string[];
  onToggle?:    (day: string) => void;
  size?:        'small' | 'medium';
}

export default function DsDayChipRow({ selectedDays, onToggle, size = 'medium' }: DsDayChipRowProps) {
  return (
    <Stack direction="row" spacing={spacing.chipGap} sx={{ flexWrap: 'wrap' }}>
      {ALL_DAYS.map((day) => {
        const active = selectedDays.includes(day);
        return (
          <Chip
            key={day}
            label={day}
            size={size}
            // filled → secondaryContainer / onSecondaryContainer (via theme)
            // outlined → outlineVariant border / onSurfaceVariant text (via theme)
            variant={active ? 'filled' : 'outlined'}
            onClick={onToggle ? () => onToggle(day) : undefined}
            // minWidth ensures touch target ≥ 40dp (MD3 minimum interactive size)
            sx={{ minWidth: size === 'small' ? 40 : 48 }}
          />
        );
      })}
    </Stack>
  );
}
