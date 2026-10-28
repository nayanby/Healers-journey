# Trophy PNG artwork

Created with the built-in image-generation tool on 29 October 2026. Each category was generated individually. The Learning trophy was the visual reference for the remaining four. Original transparent PNGs are preserved here; the app uses optimized 512 × 512 copies in `app/src/main/res/drawable-nodpi/gameart_trophy_*.png`.

Run `tools/export_trophy_pngs.py` to export runtime assets and a preview. These are dimensional pixel-art PNGs; no trophy models or runtime mesh renderer are packaged.

## Learning prompt

Create ONE production game asset: a Learning trophy icon as a transparent PNG. Front three-quarter view, centered full trophy entirely visible, square canvas, generous 8% transparent padding. A compact warm polished golden trophy pedestal with a large unmistakable OPEN BLUE BOOK as its sculpted top; no conventional cup bowl, no handles. Beautiful dimensional high quality pixel art / polished 2.5D RPG inventory sprite: deliberate crisp pixel clusters, rich chunky bevels, restrained highlights and warm amber shadows, cobalt and turquoise enamel, gold star on front of pedestal, thick dark silhouette. Broad readable shapes, readable at 48dp on a phone; not too finely detailed. Match a cozy colorful pixel-art life RPG with wooden trophy shelves. No text, letters, numbers, watermark, frame, floor or scene. Transparent background, isolated object only. A trophy PNG, not a 3D model or a sprite sheet.

## Matching category prompt template

Create ONE new transparent PNG trophy icon for CATEGORY in the exact same art family as the reference Learning trophy. The reference is a STYLE AND PEDESTAL REFERENCE, not the target subject. Replace the book completely with SUBJECT. Keep the beautifully dimensional golden star-front pedestal, richly colored enamel in the category's color, cozy pixel-art / polished 2.5D RPG inventory sprite style, crisp pixel clusters, chunky readable geometry, strong highlights and amber shadows. Front three-quarter view. Full trophy isolated and centered on square transparent canvas with 8% padding on all edges. One large unmistakable category symbol as the top, small stable gold base; not a traditional cup with handles. Visually distinct silhouette at 48dp mobile size. Similar proportions and lighting to reference. No book, no text, letters, numbers, watermark, floor, scene, border, or additional icons. Actual transparent background. Deliver one single trophy PNG, no sprite sheet.

| Category | SUBJECT |
| --- | --- |
| Work | violet laptop with a gold check mark and an ivory checklist (revised) |
| Fitness | a large EMERALD GREEN DUMBBELL, with gold bar and sturdy dimensional round weights |
| Social | overlapping coral and turquoise chat bubbles with a heart and three dots (revised) |
| Creativity | a rich MAGENTA ARTIST PALETTE with three broad colorful paint dabs and one diagonal gold-handled paintbrush |

The replacement Work and Social artwork and their matching category icons were created with the prompts in `social-work-prompts.json`. Original category icon PNGs are preserved in `design/category-icons/`; run `tools/export_category_pngs.py` after exporting trophies to package them.
