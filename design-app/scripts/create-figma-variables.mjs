#!/usr/bin/env node
/**
 * Creates Figma Variables from Morning Alarm DS tokens via REST API.
 *
 * Usage:
 *   node create-figma-variables.mjs <FIGMA_TOKEN> <FILE_KEY>
 *
 * This creates a "Morning Alarm DS" variable collection with Morning/Night modes
 * and all MD3 color tokens + shape scale as Figma Variables.
 */

const TOKEN = process.argv[2];
const FILE_KEY = process.argv[3];

if (!TOKEN || !FILE_KEY) {
  console.error('Usage: node create-figma-variables.mjs <FIGMA_TOKEN> <FILE_KEY>');
  process.exit(1);
}

// ── MD3 Color tokens (both modes) ────────────────────────────────────────

const morning = {
  primary:              '#C47700',
  onPrimary:            '#FFFFFF',
  primaryContainer:     '#FFDDB3',
  onPrimaryContainer:   '#2C1600',
  secondary:            '#6F5B40',
  onSecondary:          '#FFFFFF',
  secondaryContainer:   '#FBDEBE',
  onSecondaryContainer: '#271904',
  tertiary:             '#51643F',
  onTertiary:           '#FFFFFF',
  tertiaryContainer:    '#D4EABB',
  onTertiaryContainer:  '#102004',
  error:                '#BA1A1A',
  onError:              '#FFFFFF',
  errorContainer:       '#FFDAD6',
  onErrorContainer:     '#410002',
  background:           '#FFF9F2',
  onBackground:         '#1F1B16',
  surface:              '#FFF9F2',
  onSurface:            '#1F1B16',
  surfaceVariant:       '#F0E0CF',
  onSurfaceVariant:     '#50453A',
  outline:              '#82736A',
  outlineVariant:       '#D5C4BA',
  inverseSurface:       '#352F2A',
  inverseOnSurface:     '#FAF0E8',
  inversePrimary:       '#FFB86B',
  surfaceTint:          '#C47700',
};

const night = {
  primary:              '#C5C3FF',
  onPrimary:            '#26257C',
  primaryContainer:     '#3E3E93',
  onPrimaryContainer:   '#E2E0FF',
  secondary:            '#C8BFEE',
  onSecondary:          '#302963',
  secondaryContainer:   '#47407A',
  onSecondaryContainer: '#E5DEFF',
  tertiary:             '#B0CBEA',
  onTertiary:           '#153349',
  tertiaryContainer:    '#2C4A61',
  onTertiaryContainer:  '#CCE5FF',
  error:                '#FFB4AB',
  onError:              '#690005',
  errorContainer:       '#93000A',
  onErrorContainer:     '#FFDAD6',
  background:           '#0B1020',
  onBackground:         '#E5E2E0',
  surface:              '#151C33',
  onSurface:            '#E5E2E0',
  surfaceVariant:       '#1C2540',
  onSurfaceVariant:     '#CBC4BC',
  outline:              '#958E86',
  outlineVariant:       '#50453A',
  inverseSurface:       '#E5E2E0',
  inverseOnSurface:     '#322F2D',
  inversePrimary:       '#5C5B9E',
  surfaceTint:          '#C5C3FF',
};

// Ringing colors (fixed, same for both modes)
const ringing = {
  ringingText:          '#FFFFFF',
  ringingGlassBg:       'rgba(255,255,255,0.2)',
  ringingGlassBorder:   'rgba(255,255,255,0.25)',
  ringingStopBg:        '#FFB86B',
  ringingStopText:      '#2B2B2B',
};

// ── Shape tokens (FLOAT, same for both modes) ────────────────────────────

const shapes = {
  none:       0,
  extraSmall: 4,
  small:      8,
  medium:     12,
  large:      16,
  extraLarge: 28,
  full:       9999,
};

// ── Helpers ──────────────────────────────────────────────────────────────

function hexToFigma(hex) {
  const r = parseInt(hex.slice(1, 3), 16) / 255;
  const g = parseInt(hex.slice(3, 5), 16) / 255;
  const b = parseInt(hex.slice(5, 7), 16) / 255;
  return { r, g, b, a: 1 };
}

function rgbaToFigma(rgba) {
  const match = rgba.match(/rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*([\d.]+))?\s*\)/);
  if (!match) throw new Error(`Cannot parse: ${rgba}`);
  return {
    r: parseInt(match[1]) / 255,
    g: parseInt(match[2]) / 255,
    b: parseInt(match[3]) / 255,
    a: match[4] !== undefined ? parseFloat(match[4]) : 1,
  };
}

function colorToFigma(val) {
  if (val.startsWith('#')) return hexToFigma(val);
  if (val.startsWith('rgb')) return rgbaToFigma(val);
  throw new Error(`Unknown color format: ${val}`);
}

// kebab-case converter: onPrimary → on-primary, ringingStopBg → ringing-stop-bg
function toKebab(str) {
  return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
}

// ── Build API payload ───────────────────────────────────────────────────

const payload = {
  variableCollections: [
    {
      action: 'CREATE',
      id: 'coll_ds',
      name: 'Morning Alarm DS',
      initialModeId: 'mode_morning',
    },
  ],
  variableModes: [
    {
      action: 'UPDATE',
      id: 'mode_morning',
      name: 'Morning',
      variableCollectionId: 'coll_ds',
    },
    {
      action: 'CREATE',
      id: 'mode_night',
      name: 'Night',
      variableCollectionId: 'coll_ds',
    },
  ],
  variables: [],
  variableModeValues: [],
};

// Color variables — theme-dependent
for (const key of Object.keys(morning)) {
  const varId = `var_color_${key}`;
  payload.variables.push({
    action: 'CREATE',
    id: varId,
    name: `ds-color/${toKebab(key)}`,
    variableCollectionId: 'coll_ds',
    resolvedType: 'COLOR',
  });
  payload.variableModeValues.push(
    { variableId: varId, modeId: 'mode_morning', value: colorToFigma(morning[key]) },
    { variableId: varId, modeId: 'mode_night',   value: colorToFigma(night[key]) },
  );
}

// Ringing colors — same for both modes
for (const [key, val] of Object.entries(ringing)) {
  const varId = `var_color_${key}`;
  const figmaVal = colorToFigma(val);
  payload.variables.push({
    action: 'CREATE',
    id: varId,
    name: `ds-color/${toKebab(key)}`,
    variableCollectionId: 'coll_ds',
    resolvedType: 'COLOR',
  });
  payload.variableModeValues.push(
    { variableId: varId, modeId: 'mode_morning', value: figmaVal },
    { variableId: varId, modeId: 'mode_night',   value: figmaVal },
  );
}

// Shape variables — FLOAT, same for both modes
for (const [key, val] of Object.entries(shapes)) {
  const varId = `var_shape_${key}`;
  payload.variables.push({
    action: 'CREATE',
    id: varId,
    name: `ds-shape/${toKebab(key)}`,
    variableCollectionId: 'coll_ds',
    resolvedType: 'FLOAT',
  });
  payload.variableModeValues.push(
    { variableId: varId, modeId: 'mode_morning', value: val },
    { variableId: varId, modeId: 'mode_night',   value: val },
  );
}

// ── Send to Figma API ───────────────────────────────────────────────────

console.log(`Creating ${payload.variables.length} variables in file ${FILE_KEY}...`);
console.log(`  Colors: ${Object.keys(morning).length} theme + ${Object.keys(ringing).length} ringing`);
console.log(`  Shapes: ${Object.keys(shapes).length}`);

const resp = await fetch(`https://api.figma.com/v1/files/${FILE_KEY}/variables`, {
  method: 'POST',
  headers: {
    'X-Figma-Token': TOKEN,
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(payload),
});

const result = await resp.json();

if (resp.ok) {
  console.log('\n✓ Variables created successfully!');
  console.log(`  Status: ${result.status}`);
  if (result.meta) {
    const vars = result.meta.variables ? Object.keys(result.meta.variables).length : 0;
    const colls = result.meta.variableCollections ? Object.keys(result.meta.variableCollections).length : 0;
    console.log(`  Collections: ${colls}, Variables: ${vars}`);
  }
} else {
  console.error('\n✗ Failed to create variables:');
  console.error(JSON.stringify(result, null, 2));
  process.exit(1);
}
