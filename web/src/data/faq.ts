export interface FaqItem {
  question: string;
  answer: string;
}

export const faqItems: FaqItem[] = [
  {
    question: 'Is Morning Alarm free?',
    answer: 'Morning Alarm is free to download with a set of built-in scenes and melodies. Premium unlocks the full library of scenes, meditations, ambient sounds, and voice-stop control.',
  },
  {
    question: 'What platforms are supported?',
    answer: 'Morning Alarm is available for iOS and Android. Both platforms share the same features and content library.',
  },
  {
    question: 'How does the Morning/Night mode work?',
    answer: 'The app automatically switches between Morning and Night themes based on time of day (Morning: 6 AM–6 PM, Night: 6 PM–6 AM). The Sleep section always uses Night mode, and the ringing screen always uses Morning mode for maximum visual impact.',
  },
  {
    question: 'Can I use my own music as an alarm?',
    answer: 'Currently Morning Alarm features a curated library of melodies and scenes. Custom music support is planned for a future update.',
  },
  {
    question: 'Does the app work offline?',
    answer: 'Yes. Once downloaded, scenes and melodies are available offline. Your alarms will ring even without an internet connection.',
  },
  {
    question: 'How do I stop an alarm with my voice?',
    answer: 'Voice Stop is a Premium feature. When enabled, simply say "Stop" and the alarm turns off — no need to reach for your phone.',
  },
  {
    question: 'What is the sleep timer?',
    answer: 'The sleep timer lets you fall asleep to meditations or ambient sounds. Set a duration (15, 30, 45, or 60 minutes) and the audio fades out gradually.',
  },
];
