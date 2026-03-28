# design-app — Module Instructions

## Overview

`design-app` is a standalone React + TypeScript + Vite web prototype for Morning Alarm UI.
It is the **single source of truth for design**: all spacing, colors, layout, typography, and components are defined here.

No Kotlin dependencies — fully standalone.

## Design Flow

```
design-app (web)  →  AI agent reads live UI  →  implements in Android (Compose) / iOS (SwiftUI)
```

**Rules:**
- All design decisions are made in `design-app` first.
- When implementing a screen on Android or iOS, the agent must reference the running `design-app` directly — inspect colors, spacing, layout, and component structure from the live web UI.
- Do not use Figma as a design source. design-app is the reference.
- Dev server: `cd design-app && npm run dev` (port 5173). Theme param: `?theme=morning` or `?theme=night`.

## Structure

```
design-app/
├── src/
│   ├── screens/          — MVP screens (AlarmList, EditAlarm, Sleep, Ringing, Profile, Settings)
│   ├── ds/               — Design System components (DsAlarmCard, DsFab, DsGlassButton, etc.)
│   ├── tokens/           — Design tokens (colors, shape, typography, spacing, radii, elevation)
│   ├── theme/            — MUI themes (morningTheme, nightTheme)
│   └── App.tsx           — App root with routing and theme switching
```

## Screens

| Screen | URL |
|--------|-----|
| Alarm List | `http://localhost:5173/alarm` |
| Edit Alarm | `http://localhost:5173/alarm/new` |
| Sleep | `http://localhost:5173/sleep` |
| Ringing | `http://localhost:5173/ringing` |
| Profile | `http://localhost:5173/profile` |

## Themes

Two visual modes, toggled via UI or `?theme=morning|night` URL param:
- **Morning** — Background `#FFF9F2`, Primary `#C47700`
- **Night** — Background `#0B1020`, Primary `#C5C3FF`

## DS Tokens

| Group | Token names | Type |
|-------|-------------|------|
| `ds-color/*` | `primary`, `on-primary`, `background`, `surface`, ... (MD3 full palette) | COLOR (Morning + Night modes) |
| `ds-color/*` | `ringing-text`, `ringing-glass-bg`, `ringing-glass-border`, `ringing-stop-bg`, `ringing-stop-text` | COLOR (same both modes) |
| `ds-shape/*` | `none`(0), `extra-small`(4), `small`(8), `medium`(12), `large`(16), `extra-large`(28), `full`(9999) | FLOAT |
