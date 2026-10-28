"""Package generated trophy PNGs, retaining transparency and original design sources."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'app/src/main/res/drawable-nodpi'
preview = Image.new('RGB', (1200, 300), '#233B4B')
draw = ImageDraw.Draw(preview)
for index, name in enumerate(['learning', 'work', 'fitness', 'social', 'creativity']):
    source = Image.open(ROOT / f'design/trophies/{name}.png').convert('RGBA')
    assert source.getchannel('A').getextrema()[0] == 0, f'{name} must have a transparent background'
    source = source.crop(source.getbbox())
    source.thumbnail((472, 472), Image.Resampling.LANCZOS)
    normalized = Image.new('RGBA', (512,512))
    normalized.alpha_composite(source, ((512-source.width)//2, 496-source.height))
    source = normalized
    source.save(ART / f'gameart_trophy_{name}.png', optimize=True)
    thumb = source.copy()
    thumb.thumbnail((224,224), Image.Resampling.LANCZOS)
    preview.paste(thumb, (index*240+(240-thumb.width)//2, 8+(224-thumb.height)//2), thumb)
    draw.text((index*240+85,256), name.title(), fill='#FFE1A5')
    print(name, source.size, (ART / f'gameart_trophy_{name}.png').stat().st_size)
path = ROOT / 'build/planning-check/trophies-png-preview.png'
path.parent.mkdir(parents=True, exist_ok=True)
preview.save(path)
shelf = Image.open(ART / 'gameart_trophy_shelf.png').convert('RGBA').resize((768,576))
names = ['learning','work','fitness','social','creativity','learning','work','fitness']
for index,name in enumerate(names):
    row,col = divmod(index,4)
    width = round(768*.22)
    item = Image.open(ART / f'gameart_trophy_{name}.png').resize((width,width),Image.Resampling.LANCZOS)
    x = round(768*(.20+col*.20)-width/2)
    y = round(576*(.48 if row == 0 else .79)-width*496/512)
    shelf.alpha_composite(item,(x,y))
shelf.save(path.with_name('trophies-shelf-preview.png'))
