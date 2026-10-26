# Individual Life RPG artwork

Created with the built-in image generation tool, one image per call. Original PNGs are preserved here before integration. Character outfits use the same base avatar as an identity and pose reference.

The application uses rounded controls and clean sans-serif typography. Pixel art applies to artwork only.

## Asset groups

- Ten full character outfits, from Traveler through Golden Knight.
- Nine companions matching the existing unlock IDs.
- Nine decorations plus the plain room.
- Eight outdoor scenery backgrounds and a cozy room interior.
- Five category locations.

Existing unlock thresholds, care scores, saved equipment, and game history remain authoritative.

Generation prompts and original paths are recorded in prompts.json after generation.

## Integration

Home locations and their category journey windows share the exact same building PNG. Journey heroes and weekly trails continue the equipped scenery, while text and controls retain the standard readable UI.

The debug build exposes a one-time "Unlock all cosmetics for testing" action in Customize. It grants all existing cosmetic IDs without changing XP, activity history or weekly adventure claims; normal release builds omit this control.

Validation: all 42 resource mappings and PNG alpha channels checked; offline debug build, quest/unlock/backup checks, and Today/care checks passed. The artwork update was installed and Home was visually checked on the Samsung test device. Category-window installation and applying the debug unlock remain pending while the device is disconnected.
