import { Box, IconButton, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { cssVar, v } from '../tokens';

// MD3 Small Top App Bar
// Title:      titleLarge (22sp / weight 400) per MD3 TopAppBar spec
// Back icon:  onSurface color (standard navigation icon color)
//
// Android Compose:
//   TopAppBar(
//     title = {
//       Text(title, style = MaterialTheme.typography.titleLarge)
//     },
//     navigationIcon = {
//       IconButton(onClick = onBack) {
//         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
//              tint = MaterialTheme.colorScheme.onSurface)
//       }
//     },
//   )
//   Note: TopAppBar handles elevation, surface color, and status bar padding automatically.
//         Do not replicate those manually in Compose.
//
// iOS SwiftUI:
//   Do NOT replicate this component on iOS.
//   Use NavigationStack with system navigation bar instead:
//     .navigationTitle(title)          // inline title for pushed detail screens
//     .navigationBarBackButtonHidden(false)  // use system back button
//   For root screens use .navigationBarTitleDisplayMode(.large) → displaySmall equivalent.

interface DsScreenHeaderProps {
  title:  string;
  onBack: () => void;
}

export default function DsScreenHeader({ title, onBack }: DsScreenHeaderProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
      <IconButton onClick={onBack} sx={{ mr: 1, color: v(cssVar.colorOnSurface) }}>
        <ArrowBackIcon />
      </IconButton>
      <Typography variant="titleLarge" sx={{ color: v(cssVar.colorOnSurface) }}>{title}</Typography>
    </Box>
  );
}
