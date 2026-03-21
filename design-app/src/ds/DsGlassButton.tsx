import { Button, type ButtonProps } from '@mui/material';
import { ringingElevation, shape, cssVar, v } from '../tokens';

// Glass Button — ringing screen only
//
// Two variants:
//   default (glass): backdrop blur overlay — snooze / dismiss actions
//   accent:          warm orange fill — primary stop action
//
// Platform divergence:
//   iOS:     backdrop-filter blur maps to UIVisualEffectView / .background(.ultraThinMaterial)
//            This is a native iOS pattern and renders with real-time background sampling.
//            SwiftUI: .background(.ultraThinMaterial).clipShape(Capsule())
//
//   Android: blur is NOT natively supported in Compose (requires RenderEffect API 31+,
//            not recommended for buttons). Use FilledTonalButton instead:
//            FilledTonalButton(onClick) { Text(label) }  — uses secondaryContainer colors.
//            For the accent (stop) variant use FilledButton with inversePrimary color.
//
//   Web prototype: backdrop-filter: blur(12px) approximates iOS visual.

interface DsGlassButtonProps extends Omit<ButtonProps, 'variant'> {
  label: string;
  // accent=true → stop button (warm orange fill, primary action)
  // accent=false → glass button (blur overlay, secondary action)
  accent?: boolean;
}

export default function DsGlassButton({ label, accent = false, ...props }: DsGlassButtonProps) {
  if (accent) {
    return (
      <Button
        variant="contained"
        size="large"
        disableElevation
        sx={{
          borderRadius:  shape.full,
          minHeight:     56,
          px:            4,
          fontSize:      '1.2rem',
          fontWeight:    600,
          bgcolor:       v(cssVar.colorRingingStopBg),
          color:         v(cssVar.colorRingingStopText),
          boxShadow:     ringingElevation.stop,
          textTransform: 'none',
          '&:hover': { bgcolor: v(cssVar.colorRingingStopBgHover), boxShadow: ringingElevation.stop },
          ...props.sx,
        }}
        {...props}
      >
        {label}
      </Button>
    );
  }

  return (
    <Button
      variant="contained"
      size="large"
      disableElevation
      sx={{
        borderRadius:    shape.full,
        minHeight:       56,
        px:              4,
        fontSize:        '1.1rem',
        textTransform:   'none',
        bgcolor:         v(cssVar.colorRingingGlassBg),
        backdropFilter:  'blur(12px)',
        WebkitBackdropFilter: 'blur(12px)',
        color:           v(cssVar.colorRingingText),
        border:          `1px solid ${v(cssVar.colorRingingGlassBorder)}`,
        boxShadow:       ringingElevation.glass,
        '&:hover': { bgcolor: v(cssVar.colorRingingGlassBgHover) },
        ...props.sx,
      }}
      {...props}
    >
      {label}
    </Button>
  );
}
