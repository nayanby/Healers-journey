"""Package updated category PNGs without modifying their original generated artwork."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'app/src/main/res/drawable-nodpi'
preview = Image.new('RGB',(640,360),'#233B4B')
draw = ImageDraw.Draw(preview)
for index,name in enumerate(['work','social']):
    original = Image.open(ROOT/f'design/category-icons/{name}.png').convert('RGBA')
    assert original.getchannel('A').getextrema()[0] == 0, 'Category art must be transparent'
    icon = original.crop(original.getbbox())
    icon.thumbnail((236,236),Image.Resampling.LANCZOS)
    output = Image.new('RGBA',(256,256))
    output.alpha_composite(icon,((256-icon.width)//2,(256-icon.height)//2))
    output.save(ART/f'gameart_icon_activity_{name}.png',optimize=True)
    thumb = output.resize((144,144),Image.Resampling.LANCZOS)
    preview.paste(thumb,(index*320+16,68),thumb)
    trophy = Image.open(ART/f'gameart_trophy_{name}.png').resize((160,160),Image.Resampling.LANCZOS)
    preview.paste(trophy,(index*320+156,60),trophy)
    draw.text((index*320+140,28),name.title(),fill='#FFE1A5')
    for x,size in [(index*320+30,24),(index*320+85,40),(index*320+150,56)]:
        small = output.resize((size,size),Image.Resampling.LANCZOS)
        preview.paste(small,(x,280-size//2),small)
path = ROOT/'build/planning-check/work-social-preview.png'
path.parent.mkdir(parents=True,exist_ok=True)
preview.save(path)
