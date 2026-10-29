"""Package the generated My notes artwork as a small transparent menu PNG."""
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
source = Image.open(root/'design/notes/icon.png').convert('RGBA')
assert source.getchannel('A').getextrema()[0] == 0
source = source.crop(source.getbbox())
source.thumbnail((236,236),Image.Resampling.LANCZOS)
output = Image.new('RGBA',(256,256))
output.alpha_composite(source,((256-source.width)//2,(256-source.height)//2))
output.save(root/'app/src/main/res/drawable-nodpi/gameart_icon_notes.png',optimize=True)
