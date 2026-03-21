import typography from '@tailwindcss/typography';

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}'],
  theme: {
    extend: {
      colors: {
        morning: {
          primary: '#C47700',
          brand: '#FFB86B',
          bg: '#FFF9F2',
          'on-bg': '#1F1B16',
          surface: '#FFF9F2',
          'surface-variant': '#F0E0CF',
          outline: '#82736A',
          'outline-variant': '#D5C4BA',
        },
        night: {
          primary: '#C5C3FF',
          bg: '#0B1020',
          'on-bg': '#E5E2E0',
          surface: '#151C33',
          'surface-variant': '#1C2540',
          'outline-variant': '#50453A',
        },
      },
      fontFamily: {
        sans: ['"Inter"', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [typography],
};
