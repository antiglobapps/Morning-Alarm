import { Button, type ButtonProps } from '@mui/material';

// MD3 Filled Button
// Shape:     full (pill) — set via theme MuiButton override
// Height:    40dp minimum — set via theme
// Text:      labelLarge (14sp, weight 500) — set via theme button typography
// Shadow:    none (disableElevation) — set via theme
// Color:     primary / onPrimary — set via theme containedPrimary override
//
// Android Compose:
//   Button(onClick = onClick) { Text(label) }
//   Uses FilledButton style from MaterialTheme automatically.
//
// iOS SwiftUI:
//   Button(label) { action() }
//     .buttonStyle(.borderedProminent)
//     .tint(Color(md3Scheme.primary))
//   Note: iOS button height and padding follow system defaults,
//         not the 40dp MD3 spec — do not add fixed height on iOS.

interface DsPrimaryButtonProps extends Omit<ButtonProps, 'variant' | 'color'> {
  label: string;
}

export default function DsPrimaryButton({ label, ...props }: DsPrimaryButtonProps) {
  return (
    <Button
      variant="contained"
      color="primary"
      fullWidth
      {...props}
    >
      {label}
    </Button>
  );
}
