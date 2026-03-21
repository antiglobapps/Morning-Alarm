export interface Feature {
  icon: string;
  title: string;
  description: string;
}

export const morningFeatures: Feature[] = [
  {
    icon: '🌅',
    title: 'Scene-Based Alarms',
    description: 'Wake up to a fullscreen scene — sunrise over a lake, mountain fog, ocean breeze. Each alarm is an experience, not a noise.',
  },
  {
    icon: '🎵',
    title: 'Curated Melodies',
    description: 'Handpicked alarm sounds that ease you awake gently. From soft piano to nature ambience — never a jarring beep again.',
  },
  {
    icon: '✨',
    title: 'Ready-Made Scenarios',
    description: 'Choose "Gentle Sunrise" or "Strong Energy Morning" and everything is set — scene, melody, and mood. Zero configuration needed.',
  },
];

export const nightFeatures: Feature[] = [
  {
    icon: '🌙',
    title: 'Sleep Meditations',
    description: 'Guided sessions designed to quiet your mind. Body scans, breathing exercises, and visualization for restful sleep.',
  },
  {
    icon: '🔊',
    title: 'Ambient Sounds',
    description: 'Rain on a window, crackling fire, ocean waves. Mix and match ambient layers to create your perfect sleep soundscape.',
  },
  {
    icon: '⏱️',
    title: 'Sleep Timer',
    description: 'Set a timer and drift off. Audio fades gradually so there is no abrupt silence — just smooth transition into sleep.',
  },
];
