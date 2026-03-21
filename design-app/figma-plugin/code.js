/**
 * Morning Alarm — DS Variables Plugin
 *
 * Creates a "Morning Alarm DS" variable collection with Morning/Night modes,
 * populates it with all MD3 color tokens, then traverses all nodes in the
 * current page and binds matching colors to the corresponding variables.
 *
 * How to run:
 *   1. Figma → Plugins → Development → Import plugin from manifest…
 *   2. Select the figma-plugin folder (containing manifest.json + code.js)
 *   3. Figma → Plugins → Development → Morning Alarm — DS Variables → Run
 */

// ── MD3 Color Tokens ─────────────────────────────────────────────────────

const morning = {
  'primary':               { r: 0xC4/255, g: 0x77/255, b: 0x00/255 },
  'on-primary':            { r: 0xFF/255, g: 0xFF/255, b: 0xFF/255 },
  'primary-container':     { r: 0xFF/255, g: 0xDD/255, b: 0xB3/255 },
  'on-primary-container':  { r: 0x2C/255, g: 0x16/255, b: 0x00/255 },
  'secondary':             { r: 0x6F/255, g: 0x5B/255, b: 0x40/255 },
  'on-secondary':          { r: 0xFF/255, g: 0xFF/255, b: 0xFF/255 },
  'secondary-container':   { r: 0xFB/255, g: 0xDE/255, b: 0xBE/255 },
  'on-secondary-container':{ r: 0x27/255, g: 0x19/255, b: 0x04/255 },
  'tertiary':              { r: 0x51/255, g: 0x64/255, b: 0x3F/255 },
  'on-tertiary':           { r: 0xFF/255, g: 0xFF/255, b: 0xFF/255 },
  'tertiary-container':    { r: 0xD4/255, g: 0xEA/255, b: 0xBB/255 },
  'on-tertiary-container': { r: 0x10/255, g: 0x20/255, b: 0x04/255 },
  'error':                 { r: 0xBA/255, g: 0x1A/255, b: 0x1A/255 },
  'on-error':              { r: 0xFF/255, g: 0xFF/255, b: 0xFF/255 },
  'error-container':       { r: 0xFF/255, g: 0xDA/255, b: 0xD6/255 },
  'on-error-container':    { r: 0x41/255, g: 0x00/255, b: 0x02/255 },
  'background':            { r: 0xFF/255, g: 0xF9/255, b: 0xF2/255 },
  'on-background':         { r: 0x1F/255, g: 0x1B/255, b: 0x16/255 },
  'surface':               { r: 0xFF/255, g: 0xF9/255, b: 0xF2/255 },
  'on-surface':            { r: 0x1F/255, g: 0x1B/255, b: 0x16/255 },
  'surface-variant':       { r: 0xF0/255, g: 0xE0/255, b: 0xCF/255 },
  'on-surface-variant':    { r: 0x50/255, g: 0x45/255, b: 0x3A/255 },
  'outline':               { r: 0x82/255, g: 0x73/255, b: 0x6A/255 },
  'outline-variant':       { r: 0xD5/255, g: 0xC4/255, b: 0xBA/255 },
  'inverse-surface':       { r: 0x35/255, g: 0x2F/255, b: 0x2A/255 },
  'inverse-on-surface':    { r: 0xFA/255, g: 0xF0/255, b: 0xE8/255 },
  'inverse-primary':       { r: 0xFF/255, g: 0xB8/255, b: 0x6B/255 },
  'surface-tint':          { r: 0xC4/255, g: 0x77/255, b: 0x00/255 },
};

const night = {
  'primary':               { r: 0xC5/255, g: 0xC3/255, b: 0xFF/255 },
  'on-primary':            { r: 0x26/255, g: 0x25/255, b: 0x7C/255 },
  'primary-container':     { r: 0x3E/255, g: 0x3E/255, b: 0x93/255 },
  'on-primary-container':  { r: 0xE2/255, g: 0xE0/255, b: 0xFF/255 },
  'secondary':             { r: 0xC8/255, g: 0xBF/255, b: 0xEE/255 },
  'on-secondary':          { r: 0x30/255, g: 0x29/255, b: 0x63/255 },
  'secondary-container':   { r: 0x47/255, g: 0x40/255, b: 0x7A/255 },
  'on-secondary-container':{ r: 0xE5/255, g: 0xDE/255, b: 0xFF/255 },
  'tertiary':              { r: 0xB0/255, g: 0xCB/255, b: 0xEA/255 },
  'on-tertiary':           { r: 0x15/255, g: 0x33/255, b: 0x49/255 },
  'tertiary-container':    { r: 0x2C/255, g: 0x4A/255, b: 0x61/255 },
  'on-tertiary-container': { r: 0xCC/255, g: 0xE5/255, b: 0xFF/255 },
  'error':                 { r: 0xFF/255, g: 0xB4/255, b: 0xAB/255 },
  'on-error':              { r: 0x69/255, g: 0x00/255, b: 0x05/255 },
  'error-container':       { r: 0x93/255, g: 0x00/255, b: 0x0A/255 },
  'on-error-container':    { r: 0xFF/255, g: 0xDA/255, b: 0xD6/255 },
  'background':            { r: 0x0B/255, g: 0x10/255, b: 0x20/255 },
  'on-background':         { r: 0xE5/255, g: 0xE2/255, b: 0xE0/255 },
  'surface':               { r: 0x15/255, g: 0x1C/255, b: 0x33/255 },
  'on-surface':            { r: 0xE5/255, g: 0xE2/255, b: 0xE0/255 },
  'surface-variant':       { r: 0x1C/255, g: 0x25/255, b: 0x40/255 },
  'on-surface-variant':    { r: 0xCB/255, g: 0xC4/255, b: 0xBC/255 },
  'outline':               { r: 0x95/255, g: 0x8E/255, b: 0x86/255 },
  'outline-variant':       { r: 0x50/255, g: 0x45/255, b: 0x3A/255 },
  'inverse-surface':       { r: 0xE5/255, g: 0xE2/255, b: 0xE0/255 },
  'inverse-on-surface':    { r: 0x32/255, g: 0x2F/255, b: 0x2D/255 },
  'inverse-primary':       { r: 0x5C/255, g: 0x5B/255, b: 0x9E/255 },
  'surface-tint':          { r: 0xC5/255, g: 0xC3/255, b: 0xFF/255 },
};

// Ringing palette — fixed, same for both modes
const ringing = {
  'ringing-text':          { r: 1, g: 1, b: 1, a: 1 },
  'ringing-glass-bg':      { r: 1, g: 1, b: 1, a: 0.2 },
  'ringing-glass-border':  { r: 1, g: 1, b: 1, a: 0.25 },
  'ringing-stop-bg':       { r: 0xFF/255, g: 0xB8/255, b: 0x6B/255 },
  'ringing-stop-text':     { r: 0x2B/255, g: 0x2B/255, b: 0x2B/255 },
};

// ── Shape tokens (FLOAT) ─────────────────────────────────────────────────

const shapes = {
  'none':        0,
  'extra-small': 4,
  'small':       8,
  'medium':      12,
  'large':       16,
  'extra-large': 28,
  'full':        9999,
};

// ── Helpers ──────────────────────────────────────────────────────────────

const EPS = 2 / 255; // tolerance for color matching (~0.78%)

function colorsMatch(a, b) {
  if (!a || !b) return false;
  if (Math.abs(a.r - b.r) > EPS) return false;
  if (Math.abs(a.g - b.g) > EPS) return false;
  if (Math.abs(a.b - b.b) > EPS) return false;
  // For alpha: if both have it, compare; otherwise ignore
  if (a.a !== undefined && b.a !== undefined) {
    if (Math.abs(a.a - b.a) > 0.05) return false;
  }
  return true;
}

function rgbKey(c) {
  return `${Math.round(c.r*255)},${Math.round(c.g*255)},${Math.round(c.b*255)},${Math.round((c.a ?? 1)*100)}`;
}

// ── Main ─────────────────────────────────────────────────────────────────

async function main() {
  const page = figma.currentPage;

  // Step 1: Detect which theme mode this page uses (morning or night)
  // by checking the page background or the majority of node colors.
  // We'll create variables for both modes regardless.

  // Step 2: Create or find the Variable Collection
  let collection = figma.variables.getLocalVariableCollections()
    .find(c => c.name === 'Morning Alarm DS');

  let morningModeId;
  let nightModeId;

  if (collection) {
    console.log('Found existing collection, reusing...');
    morningModeId = collection.modes[0]?.modeId;
    nightModeId = collection.modes[1]?.modeId;
  } else {
    console.log('Creating new collection "Morning Alarm DS"...');
    collection = figma.variables.createVariableCollection('Morning Alarm DS');
    morningModeId = collection.modes[0].modeId;
    collection.renameMode(morningModeId, 'Morning');
    nightModeId = collection.addMode('Night');
  }

  // Step 3: Create color variables
  const colorVarMap = {}; // rgbKey(morning) → variable, for node matching
  const allVarsCreated = [];

  // Check existing variables to avoid duplicates
  const existingVars = {};
  for (const v of figma.variables.getLocalVariables('COLOR')) {
    if (v.variableCollectionId === collection.id) {
      existingVars[v.name] = v;
    }
  }
  for (const v of figma.variables.getLocalVariables('FLOAT')) {
    if (v.variableCollectionId === collection.id) {
      existingVars[v.name] = v;
    }
  }

  // Theme-dependent colors
  for (const key of Object.keys(morning)) {
    const name = `ds-color/${key}`;
    let variable = existingVars[name];
    if (!variable) {
      variable = figma.variables.createVariable(name, collection.id, 'COLOR');
    }
    const mVal = morning[key];
    const nVal = night[key];
    variable.setValueForMode(morningModeId, { r: mVal.r, g: mVal.g, b: mVal.b, a: mVal.a ?? 1 });
    variable.setValueForMode(nightModeId,   { r: nVal.r, g: nVal.g, b: nVal.b, a: nVal.a ?? 1 });

    // Index by morning color for node matching
    colorVarMap[rgbKey(mVal)] = variable;
    // Also index by night color
    colorVarMap[rgbKey(nVal)] = variable;

    allVarsCreated.push(name);
  }

  // Ringing colors (same both modes)
  for (const [key, val] of Object.entries(ringing)) {
    const name = `ds-color/${key}`;
    let variable = existingVars[name];
    if (!variable) {
      variable = figma.variables.createVariable(name, collection.id, 'COLOR');
    }
    const fVal = { r: val.r, g: val.g, b: val.b, a: val.a ?? 1 };
    variable.setValueForMode(morningModeId, fVal);
    variable.setValueForMode(nightModeId,   fVal);

    colorVarMap[rgbKey(val)] = variable;
    allVarsCreated.push(name);
  }

  // Shape variables (FLOAT)
  for (const [key, val] of Object.entries(shapes)) {
    const name = `ds-shape/${key}`;
    let variable = existingVars[name];
    if (!variable) {
      variable = figma.variables.createVariable(name, collection.id, 'FLOAT');
    }
    variable.setValueForMode(morningModeId, val);
    variable.setValueForMode(nightModeId,   val);
    allVarsCreated.push(name);
  }

  console.log(`Created/updated ${allVarsCreated.length} variables`);

  // Step 4: Traverse all nodes and bind matching colors
  let bindCount = 0;
  let nodeCount = 0;

  function processNode(node) {
    nodeCount++;

    // Bind fills
    if ('fills' in node && Array.isArray(node.fills)) {
      const fills = [...node.fills];
      let changed = false;
      for (let i = 0; i < fills.length; i++) {
        const fill = fills[i];
        if (fill.type === 'SOLID' && fill.color) {
          const key = rgbKey({ ...fill.color, a: fill.opacity ?? 1 });
          const variable = colorVarMap[key];
          if (variable) {
            // Check if already bound
            const existing = node.boundVariables?.fills?.[i];
            if (!existing || existing.id !== variable.id) {
              try {
                const fillCopy = figma.variables.setBoundVariableForPaint(
                  fills[i], 'color', variable
                );
                fills[i] = fillCopy;
                changed = true;
                bindCount++;
              } catch (e) {
                // Some nodes don't support variable binding
              }
            }
          }
        }
      }
      if (changed) {
        try { node.fills = fills; } catch (e) { /* read-only node */ }
      }
    }

    // Bind strokes
    if ('strokes' in node && Array.isArray(node.strokes)) {
      const strokes = [...node.strokes];
      let changed = false;
      for (let i = 0; i < strokes.length; i++) {
        const stroke = strokes[i];
        if (stroke.type === 'SOLID' && stroke.color) {
          const key = rgbKey({ ...stroke.color, a: stroke.opacity ?? 1 });
          const variable = colorVarMap[key];
          if (variable) {
            try {
              const strokeCopy = figma.variables.setBoundVariableForPaint(
                strokes[i], 'color', variable
              );
              strokes[i] = strokeCopy;
              changed = true;
              bindCount++;
            } catch (e) {}
          }
        }
      }
      if (changed) {
        try { node.strokes = strokes; } catch (e) {}
      }
    }

    // Bind corner radius to shape variables
    if ('cornerRadius' in node && typeof node.cornerRadius === 'number') {
      for (const [key, val] of Object.entries(shapes)) {
        if (Math.abs(node.cornerRadius - val) < 0.5 && val > 0) {
          const name = `ds-shape/${key}`;
          const variable = existingVars[name] ||
            figma.variables.getLocalVariables('FLOAT')
              .find(v => v.name === name && v.variableCollectionId === collection.id);
          if (variable) {
            try {
              node.setBoundVariable('cornerRadius', variable.id);
              bindCount++;
            } catch (e) {}
          }
          break;
        }
      }
    }

    // Recurse children
    if ('children' in node) {
      for (const child of node.children) {
        processNode(child);
      }
    }
  }

  // Process all pages
  for (const p of figma.root.children) {
    processNode(p);
  }

  const msg = `Done! Created ${allVarsCreated.length} variables, bound ${bindCount} properties across ${nodeCount} nodes.`;
  console.log(msg);
  figma.notify(msg);
  figma.closePlugin();
}

main();
