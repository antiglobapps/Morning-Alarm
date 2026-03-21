import { Box, Card, CardContent, Slider, Typography } from '@mui/material';
import { cssVar, v } from '../tokens';

// MD3 Filled Card with Slider
// Shape:     medium (12dp) — via theme
// Elevation: level1 (1dp) — via theme
// Timer display: headlineMedium (28sp) — large countable number
// Slider:    primary active track, surfaceVariant inactive — via theme MuiSlider override
//
// Android Compose:
//   Card {
//     Column(modifier = Modifier.padding(16.dp)) {
//       Text("$value min", style = MaterialTheme.typography.headlineMedium)
//       Slider(
//         value = value.toFloat(),
//         onValueChange = { onChange(it.toInt()) },
//         valueRange = min.toFloat()..max.toFloat(),
//         steps = (max - min) / step - 1,
//       )
//     }
//   }
//
// iOS SwiftUI:
//   RoundedRectangle(cornerRadius: 12, style: .continuous) card:
//   VStack(alignment: .leading, spacing: 8) {
//     Text("\(value) min").font(.system(size: 28))  // headlineMedium
//     Slider(value: $floatValue, in: Float(min)...Float(max), step: Float(step))
//       .tint(Color(md3Scheme.primary))
//   }.padding(16)

interface DsSleepTimerCardProps {
  value:    number;
  onChange: (val: number) => void;
  min?:     number;
  max?:     number;
  step?:    number;
}

export default function DsSleepTimerCard({ value, onChange, min = 5, max = 120, step = 5 }: DsSleepTimerCardProps) {
  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          {/* headlineMedium: 28sp / weight 400 — prominent countable number */}
          <Typography variant="headlineMedium" sx={{ color: v(cssVar.colorOnSurface) }}>{value} min</Typography>
        </Box>
        {/* MD3 Slider: primary track, surfaceVariant rail — via theme MuiSlider override */}
        {/* iOS: Slider(...).tint(Color(md3Scheme.primary))                               */}
        <Slider
          value={value}
          onChange={(_, val) => onChange(val as number)}
          min={min}
          max={max}
          step={step}
          color="primary"
        />
      </CardContent>
    </Card>
  );
}
