import { Box, Typography } from '@mui/material';
import { DsPremiumCard } from '../ds';
import { spacing } from '../tokens';

const PREMIUM_FEATURES = [
  'Unlimited meditations',
  'Voice stop',
  'AI melody generation',
  'Exclusive scenes & sound packs',
  'Advanced wake scenarios',
];

export default function ProfileScreen() {
  return (
    <Box sx={{ px: spacing.screenHorizontal, pt: spacing.screenTop }}>
      {/* headlineLarge: primary screen title */}
      <Typography variant="headlineLarge" sx={{ mb: spacing.sectionSpacing }}>
        Profile
      </Typography>

      <Box sx={{ mb: spacing.sectionSpacing }}>
        <DsPremiumCard features={PREMIUM_FEATURES} />
      </Box>
    </Box>
  );
}
