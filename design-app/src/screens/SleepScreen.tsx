import { useState } from 'react';
import { Box, Typography, Stack } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { DsMoodChipRow, DsSleepCard, DsSleepTimerCard, DsPrimaryButton } from '../ds';
import { spacing } from '../tokens';

const MOODS = ['Calm', 'Deep Sleep', 'Rain', 'Forest', 'Ocean'];

interface SleepItem {
  id: string;
  title: string;
  subtitle: string;
  image: string;
}

const FEATURED: SleepItem[] = [
  {
    id: '1',
    title: 'Moon Breathing',
    subtitle: '15 min meditation',
    image: 'https://images.unsplash.com/photo-1532978379173-523e16f371f2?w=400&h=200&fit=crop',
  },
  {
    id: '2',
    title: 'Rain on Window',
    subtitle: '45 min ambient sound',
    image: 'https://images.unsplash.com/photo-1515694346937-94d85e39a29a?w=400&h=200&fit=crop',
  },
  {
    id: '3',
    title: 'Deep Forest Night',
    subtitle: '30 min nature sounds',
    image: 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=400&h=200&fit=crop',
  },
];

export default function SleepScreen() {
  const [selectedMood, setSelectedMood] = useState('Calm');
  const [sleepTimer, setSleepTimer] = useState(30);

  return (
    <Box sx={{ px: spacing.screenHorizontal, pt: spacing.screenTop }}>
      {/* headlineLarge: primary screen title */}
      <Typography variant="headlineLarge" sx={{ mb: spacing.sectionSpacing }}>
        Sleep
      </Typography>

      {/* Mood chips */}
      {/* labelMedium: section caption */}
      <Typography variant="labelMedium" color="text.secondary" sx={{ mb: 1 }}>
        Tonight mood
      </Typography>
      <Box sx={{ mb: spacing.sectionSpacing }}>
        <DsMoodChipRow moods={MOODS} selected={selectedMood} onSelect={setSelectedMood} />
      </Box>

      {/* Featured items */}
      <Typography variant="labelMedium" color="text.secondary" sx={{ mb: 1.5 }}>
        Featured
      </Typography>
      <Stack spacing={spacing.stackGap} sx={{ mb: spacing.sectionSpacing }}>
        {FEATURED.map((item) => (
          <DsSleepCard key={item.id} title={item.title} subtitle={item.subtitle} image={item.image} />
        ))}
      </Stack>

      {/* Sleep timer */}
      <Typography variant="labelMedium" color="text.secondary" sx={{ mb: 1 }}>
        Sleep Timer
      </Typography>
      <Box sx={{ mb: spacing.sectionSpacing }}>
        <DsSleepTimerCard value={sleepTimer} onChange={setSleepTimer} />
      </Box>

      <Box sx={{ mb: 4 }}>
        <DsPrimaryButton label="Start Session" startIcon={<PlayArrowIcon />} />
      </Box>
    </Box>
  );
}
