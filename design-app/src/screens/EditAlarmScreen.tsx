import { useState } from 'react';
import { Box, Typography, Stack } from '@mui/material';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import ImageIcon from '@mui/icons-material/Image';
import SnoozeIcon from '@mui/icons-material/Snooze';
import MicIcon from '@mui/icons-material/Mic';
import { useNavigate, useParams } from 'react-router-dom';
import { DsScreenHeader, DsDayChipRow, DsSettingRow, DsPrimaryButton } from '../ds';
import { spacing } from '../tokens';

export default function EditAlarmScreen() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isNew = !id || id === 'new';

  const [hour,   setHour]   = useState(7);
  const [minute, setMinute] = useState(30);
  const [selectedDays, setSelectedDays] = useState<string[]>(['Mo', 'Tu', 'We', 'Th', 'Fr']);

  const toggleDay = (day: string) => {
    setSelectedDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  return (
    <Box sx={{ px: spacing.screenHorizontal, pt: 2 }}>
      <DsScreenHeader title={isNew ? 'New Alarm' : 'Edit Alarm'} onBack={() => navigate(-1)} />

      {/* Time picker — displayLarge: largest expressive number on screen */}
      <Box sx={{ textAlign: 'center', mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 1 }}>
          <Box
            sx={{
              bgcolor: 'background.paper',
              borderRadius: 3,
              px: 3,
              py: 2,
              cursor: 'pointer',
              '&:hover': { bgcolor: 'action.hover' },
            }}
            onClick={() => setHour((h) => (h + 1) % 24)}
          >
            <Typography variant="displayLarge">
              {String(hour).padStart(2, '0')}
            </Typography>
          </Box>
          <Typography variant="displayLarge">:</Typography>
          <Box
            sx={{
              bgcolor: 'background.paper',
              borderRadius: 3,
              px: 3,
              py: 2,
              cursor: 'pointer',
              '&:hover': { bgcolor: 'action.hover' },
            }}
            onClick={() => setMinute((m) => (m + 5) % 60)}
          >
            <Typography variant="displayLarge">
              {String(minute).padStart(2, '0')}
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* Repeat days */}
      <Typography variant="labelMedium" color="text.secondary" sx={{ mb: 1 }}>
        Repeat
      </Typography>
      <Box sx={{ mb: spacing.sectionSpacing }}>
        <DsDayChipRow selectedDays={selectedDays} onToggle={toggleDay} />
      </Box>

      {/* Settings */}
      <Stack spacing={1.5} sx={{ mb: 4 }}>
        <DsSettingRow icon={<MusicNoteIcon />} label="Melody"     value="Soft Awakening" />
        <DsSettingRow icon={<ImageIcon />}     label="Scene"      value="Sunrise Lake" />
        <DsSettingRow icon={<SnoozeIcon />}    label="Snooze"     value="10 min" />
        <DsSettingRow icon={<MicIcon />}       label="Voice Stop" value="Premium" premium />
      </Stack>

      <DsPrimaryButton label="Save Alarm" onClick={() => navigate('/alarm')} />
    </Box>
  );
}
