# Pixel artwork

Current individual PNG artwork is in `individual-art/`. Each image is generated separately with the built-in image generation tool. Outfits preserve the same character identity and pose. Prompts and original paths are retained in `individual-art/prompts.json`.

The game retains rounded controls, normal sans-serif text, standard progress rings and the existing colorful navigation icons. Only world artwork uses pixel art. Silkscreen and the previous stepped UI are no longer active.

The earlier grid-drawn asset experiment remains archived here: `catalog.png`, `tools/Build-Pixel-Art.py`, `source-snapshot/` and the unused `pixel_*.png` exports. The original glossy assets and care avatars are also preserved.

New runtime art is copied into `drawable-nodpi/gameart_*.png` after creation and displayed with nearest-neighbor bitmap sampling. Saved unlock IDs, XP thresholds, equipment and care state remain unchanged.
