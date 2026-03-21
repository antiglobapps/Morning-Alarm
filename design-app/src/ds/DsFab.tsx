import { Fab, type FabProps } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

// MD3 Standard FAB
// Size:      56×56dp (MD3 spec) — was 64, corrected
// Shape:     large (16dp) — set via theme MuiFab override
// Color:     primaryContainer / onPrimaryContainer — set via theme
// Elevation: level3 (6dp) — set via theme
//
// Android Compose:
//   FloatingActionButton(
//     onClick = onClick,
//     containerColor = MaterialTheme.colorScheme.primaryContainer,
//     contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//   ) { Icon(Icons.Default.Add, contentDescription = null) }
//
// iOS SwiftUI:
//   NOT a native HIG pattern. iOS does not have a FAB concept.
//   Alternatives depending on context:
//     a) NavigationBar trailing button — for "add alarm" on list screen
//        .toolbar { ToolbarItem(placement: .topBarTrailing) { Button { } label: { Image(systemName: "plus") } } }
//     b) Primary button pinned above TabView — ZStack with Button at bottom-right
//     c) For prominent single action at screen bottom — use DsPrimaryButton instead

interface DsFabProps extends Omit<FabProps, 'children'> {
  icon?: React.ReactNode;
}

export default function DsFab({ icon = <AddIcon />, ...props }: DsFabProps) {
  return (
    <Fab
      // color="primary" intentionally omitted — theme MuiFab override sets
      // primaryContainer / onPrimaryContainer per MD3 spec
      sx={{ width: 56, height: 56, ...props.sx }}
      {...props}
    >
      {icon}
    </Fab>
  );
}
