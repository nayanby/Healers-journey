"""Keep shipped pixel art no larger than its decoded in-app texture size.

The app's gameArtPainter downsamples art to roughly 512 px at runtime. Keeping
source art at 1024 px on its longest edge preserves that rendering while
reducing download size. PNG stays PNG, and nearest-neighbour keeps hard edges.
"""

from io import BytesIO
from pathlib import Path

from PIL import Image


root = Path(__file__).resolve().parents[2] / "app/src/main/res/drawable-nodpi"
total_before = total_after = changed = resized = 0

for path in sorted(root.glob("*.png")):
    original = path.read_bytes()
    total_before += len(original)
    with Image.open(BytesIO(original)) as opened:
        image = opened.copy()
    if max(image.size) > 1024:
        scale = 1024 / max(image.size)
        image = image.resize(
            (round(image.width * scale), round(image.height * scale)),
            Image.Resampling.NEAREST,
        )
        resized += 1
    output = BytesIO()
    image.save(output, format="PNG", optimize=True, compress_level=9)
    optimized = output.getvalue()
    if len(optimized) < len(original):
        path.write_bytes(optimized)
        changed += 1
        total_after += len(optimized)
    else:
        total_after += len(original)

print(f"Resized {resized} PNGs; reduced {changed} files")
print(f"Artwork: {total_before / 2**20:.1f} MiB -> {total_after / 2**20:.1f} MiB")
