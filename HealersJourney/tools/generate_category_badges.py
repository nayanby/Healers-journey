"""Build round shelf badges around the game's existing category icons."""

from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"

COLORS = {
    "learning": (26, 78, 119),
    "work": (69, 51, 111),
    "fitness": (26, 100, 97),
    "social": (128, 67, 83),
    "creativity": (111, 63, 106),
}


def make_badge(category: str, center: tuple[int, int, int]) -> None:
    size = 128
    badge = Image.new("RGBA", (size, size))
    draw = ImageDraw.Draw(badge)

    # A small gold loop makes the round medallion look suspended in the shelf.
    draw.ellipse((53, 2, 75, 26), fill="#663415")
    draw.ellipse((56, 5, 72, 23), fill="#E5A93D")
    draw.ellipse((59, 8, 69, 19), fill=(0, 0, 0, 0))
    draw.rounded_rectangle((58, 18, 70, 30), radius=2, fill="#7A411E")
    draw.rectangle((61, 19, 67, 29), fill="#F6C75D")

    draw.ellipse((10, 20, 118, 128), fill="#4B291A")
    draw.ellipse((12, 20, 116, 124), fill="#956022")
    draw.ellipse((15, 21, 113, 119), fill="#FFDB74")
    draw.ellipse((19, 25, 109, 115), fill="#B36A24")
    draw.ellipse((22, 28, 106, 112), fill="#FFD878")
    draw.ellipse((26, 32, 102, 108), fill="#4E321F")
    draw.ellipse((29, 35, 99, 105), fill=center)
    draw.arc((31, 37, 97, 103), 205, 325, fill="#86CAD0", width=2)
    draw.arc((16, 22, 112, 118), 200, 300, fill="#FFF0A2", width=3)
    draw.arc((16, 22, 112, 118), 15, 110, fill="#774019", width=3)

    icon = Image.open(DRAWABLE / f"gameart_icon_activity_{category}.png").convert("RGBA")
    bounds = icon.getbbox()
    if bounds:
        icon = icon.crop(bounds)
    icon.thumbnail((59, 59), Image.Resampling.LANCZOS)
    badge.alpha_composite(icon, ((size - icon.width) // 2, 41 + (59 - icon.height) // 2))

    badge = badge.resize((256, 256), Image.Resampling.NEAREST)
    badge.save(DRAWABLE / f"gameart_badge_{category}.png", optimize=True)


for name, color in COLORS.items():
    make_badge(name, color)
