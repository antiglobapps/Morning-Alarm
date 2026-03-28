import { Box, Typography, Stack } from '@mui/material';
import SnoozeIcon from '@mui/icons-material/Snooze';
import AlarmOffIcon from '@mui/icons-material/AlarmOff';
import MicIcon from '@mui/icons-material/Mic';
import { useNavigate } from 'react-router-dom';
import { DsGlassButton } from '../ds';
import { ringingColors, shadows } from '../tokens';

/**
 * THE "wow" screen — fullscreen alarm ringing experience.
 * Always uses Morning theme styling (light, warm).
 * This is the most important screen of the product.
 */
export default function RingingScreen() {
  const navigate = useNavigate();
  const r = ringingColors;
  const s = shadows.ringing;

  return (
    <Box
      sx={{
        position: 'relative',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}
    >
      {/* Background image */}
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          backgroundImage:
            'url(https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=1200&fit=crop)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          filter: 'brightness(0.7)',
          zIndex: 0,
        }}
      />

      {/* Gradient overlay */}
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          background: r.overlayGradient,
          zIndex: 1,
        }}
      />

      {/* Content */}
      <Box
        sx={{
          position: 'relative',
          zIndex: 2,
          textAlign: 'center',
          px: 4,
          width: '100%',
        }}
      >
        <Typography
          sx={{
            fontSize: '5rem',
            fontWeight: 300,
            color: r.text,
            textShadow: s.text,
            lineHeight: 1,
            mb: 1,
          }}
        >
          07:30
        </Typography>

        <Typography sx={{ fontSize: '1.5rem', fontWeight: 400, color: r.textMuted, mb: 1 }}>
          Gentle Morning
        </Typography>

        <Typography sx={{ fontSize: '1rem', fontWeight: 300, color: r.textSubtle, fontStyle: 'italic', mb: 6 }}>
          "Breathe in. New day."
        </Typography>

        <Stack spacing={2} sx={{ width: '100%', maxWidth: 300, mx: 'auto' }}>
          <DsGlassButton label="Snooze 10 min" startIcon={<SnoozeIcon />} onClick={() => navigate('/alarm')} />
          <DsGlassButton label="Stop Alarm" accent startIcon={<AlarmOffIcon />} onClick={() => navigate('/alarm')} />
        </Stack>

        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mt: 4, gap: 1 }}>
          <MicIcon sx={{ color: r.textHint, fontSize: 18 }} />
          <Typography sx={{ color: r.textHint, fontSize: '0.85rem' }}>
            Voice command ready
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
