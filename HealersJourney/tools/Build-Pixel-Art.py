"""Hand-drawn pixel-grid assets. No antialiasing or filtered 3D artwork."""
from pathlib import Path
from PIL import Image, ImageDraw
import math, random

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/res/drawable-nodpi'
INK='#263847'; GOLD='#e7ba63'; LIGHT='#ffdfa0'; BLUE='#729cba'; TEAL='#71b8a5'
def canvas(n=32):
    im=Image.new('RGBA',(n,n)); return im,ImageDraw.Draw(im)
def box(d, xy, fill, outline=INK, width=2): d.rectangle(xy,fill=fill,outline=outline,width=width)
def poly(d,points,fill,outline=INK,width=2):
    d.polygon(points,fill=fill)
    if outline: d.line(points+[points[0]],fill=outline,width=width)
def line(d,p,fill=LIGHT,width=2): d.line(p,fill=fill,width=width)
def save(im,name): im.resize((192,192),Image.Resampling.NEAREST).save(OUT/f'pixel_{name}.png')
def diamond(d,x,y,r,color): poly(d,[(x,y-r),(x+r,y),(x,y+r),(x-r,y)],color,width=1)

names='home today study medicine fitness social sleep hobbies finance todo care milestone achievement logs sync menu edit add delete check lock left right up down back forward upload download logout shower haircut shaving manscaping nails xp streak week'.split()
for name in names:
    im,d=canvas()
    if name in ['study','fitness','social','hobbies']:
        old={'study':'knowledge','fitness':'strength','social':'social','hobbies':'hobbies'}[name]
        source=Image.open(OUT/f'skill_{old}.png').convert('RGBA')
        source=source.crop(source.getbbox());source.thumbnail((28,28),Image.Resampling.NEAREST)
        im.alpha_composite(source,((32-source.width)//2,(32-source.height)//2));save(im,name);continue
    if name=='home':
        box(d,(7,13,25,28),'#d1ad78');poly(d,[(3,14),(16,3),(29,14)],'#b56e67');line(d,[(7,11),(16,5),(25,11)],'#e7a177');box(d,(13,20,19,28),'#655648');box(d,(9,16,12,20),'#a9d9dd',width=1)
    elif name=='medicine':
        box(d,(11,5,21,11),'#a28b69');box(d,(3,10,29,27),'#a87a55');box(d,(5,12,27,17),'#cca273',width=1);box(d,(14,15,18,21),GOLD,width=1);line(d,[(6,24),(25,24)],'#806046',1)
    elif name in ['today','xp']:
        if name=='today': poly(d,[(18,2),(6,18),(14,18),(12,30),(27,12),(19,12),(22,2)],GOLD);line(d,[(17,6),(10,15),(17,15)],LIGHT,2)
        else: poly(d,[(16,2),(20,11),(30,12),(23,19),(25,29),(16,24),(7,29),(9,19),(2,12),(12,11)],GOLD);line(d,[(15,8),(17,14),(23,15)],LIGHT,2)
    elif name=='sleep':
        poly(d,[(16,3),(10,5),(5,11),(5,20),(10,26),(19,28),(26,23),(28,18),(22,20),(15,18),(12,12),(13,6)],'#e8cb76');diamond(d,25,7,4,'#a3cbdc');line(d,[(8,13),(8,19),(12,23)],LIGHT,2)
    elif name=='finance':
        box(d,(4,8,28,27),'#8f7153');poly(d,[(5,8),(24,3),(24,9)],'#a7b88a');box(d,(19,15,29,23),'#cda577');box(d,(22,18,24,20),GOLD,width=1)
    elif name in ['todo','logs','week']:
        box(d,(5,5,27,29),'#b98c5d');box(d,(7,7,25,27),'#f4e4bc',width=1)
        if name=='todo': box(d,(11,2,21,8),GOLD,width=1)
        if name=='week':
            box(d,(5,6,27,12),'#be7b6f');box(d,(10,3,12,8),'#d6d5bf',width=1);box(d,(20,3,22,8),'#d6d5bf',width=1)
        for y in (14,19,24): line(d,[(11,y),(22,y)],'#9c997b',1)
        if name!='logs':line(d,[(9,17),(13,21),(23,12)],'#47876b',3)
    elif name in ['care','manscaping']:
        box(d,(9,10,24,28),TEAL);box(d,(12,5,19,11),'#9babb1');box(d,(13,3,25,6),'#bcbdac',width=1)
        if name=='care':line(d,[(13,19),(20,19)],'#ecedcf',2);line(d,[(17,15),(17,23)],'#ecedcf',2)
        else:box(d,(11,12,22,16),'#d1d5b9',width=1);line(d,[(14,21),(19,21)],INK,2)
    elif name=='milestone':
        box(d,(7,3,9,28),'#b6a790',width=1);poly(d,[(10,4),(28,4),(23,10),(28,16),(10,16)],'#c47d65');line(d,[(4,29),(18,29)],'#6d7c6a',2)
    elif name=='achievement':
        box(d,(8,4,24,16),GOLD);poly(d,[(8,14),(12,21),(20,21),(24,14)],GOLD);line(d,[(8,7),(3,7),(3,14),(9,17)],GOLD,3);line(d,[(24,7),(29,7),(29,14),(23,17)],GOLD,3);box(d,(14,21,18,25),'#b98d4e',width=1);box(d,(9,26,23,29),GOLD,width=1);line(d,[(11,7),(21,7)],LIGHT,2)
    elif name=='menu':
        for y,c in [(6,BLUE),(15,TEAL),(24,GOLD)]:box(d,(4,y-2,28,y+3),c,width=1);line(d,[(7,y-1),(25,y-1)],'#d4dfc5',1)
    elif name=='edit':
        poly(d,[(4,23),(4,29),(10,29),(28,11),(21,4)],GOLD);poly(d,[(20,5),(25,2),(30,7),(27,12)],'#bf7c83');line(d,[(8,24),(23,9)],LIGHT,2)
    elif name=='add':
        box(d,(12,3,20,29),TEAL);box(d,(3,12,29,20),TEAL);line(d,[(14,5),(14,14),(5,14)],'#bdd9bc',2)
    elif name=='delete':
        box(d,(8,9,25,28),'#b87d73');box(d,(5,6,28,10),'#ce9a83',width=1);box(d,(12,3,21,6),'#d8b090',width=1)
        for x in [12,17,21]:line(d,[(x,13),(x,24)],'#755958',1)
    elif name=='check':
        poly(d,[(3,15),(8,10),(14,17),(25,4),(30,9),(14,28)],TEAL);line(d,[(7,15),(14,22),(26,8)],'#b4d9bd',2)
    elif name=='lock':
        poly(d,[(9,14),(9,7),(13,3),(21,3),(25,7),(25,15),(21,15),(21,8),(13,8),(13,15)],'#a8b7ba');box(d,(6,13,28,29),GOLD);box(d,(15,19,19,24),'#695d4e',width=1)
    elif name in ['left','right','up','down','back','forward']:
        poly(d,[(4,15),(16,3),(21,8),(16,13),(29,13),(29,20),(16,20),(21,25),(16,30)],BLUE);line(d,[(8,15),(16,7)],'#b9d8d6',2)
        if name in ['right','forward']:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        elif name=='up':im=im.transpose(Image.Transpose.ROTATE_270)
        elif name=='down':im=im.transpose(Image.Transpose.ROTATE_90)
    elif name in ['sync','upload','download','logout']:
        if name=='sync':
            line(d,[(5,12),(8,6),(18,4),(26,9)],TEAL,4);poly(d,[(22,3),(29,12),(19,12)],TEAL,width=1);line(d,[(27,20),(23,26),(14,28),(6,23)],BLUE,4);poly(d,[(10,29),(3,20),(13,20)],BLUE,width=1)
        else:
            box(d,(4,23,28,28),'#94a7a1',width=1);poly(d,[(16,3),(27,14),(21,14),(21,23),(11,23),(11,14),(5,14)],TEAL)
            if name=='download':im=im.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
            if name=='logout':im=im.transpose(Image.Transpose.ROTATE_270)
    elif name=='shower':
        line(d,[(5,28),(5,8),(9,4),(16,4),(20,8)],'#a7b5b8',3);poly(d,[(13,12),(22,6),(28,12),(18,18)],BLUE)
        for x,y in [(14,21),(21,20),(26,17)]:line(d,[(x,y),(x-1,y+4)],'#80c4d2',2)
    elif name=='haircut':
        for x in [8,24]:box(d,(x-5,21,x+3,29),BLUE);box(d,(x-3,23,x+1,27),'#a5d2d4',width=1)
        line(d,[(9,23),(24,3)],'#d5d6c6',3);line(d,[(23,23),(9,3)],'#b6c5c4',3);box(d,(14,14,17,17),GOLD,width=1)
    elif name=='shaving':
        box(d,(5,5,28,13),'#aac2c4');box(d,(8,7,25,9),'#e3e6ce',width=1);box(d,(14,13,20,29),TEAL);line(d,[(16,16),(16,25)],'#c8dfc9',1)
    elif name=='nails':
        poly(d,[(8,27),(8,13),(11,13),(11,6),(14,4),(18,4),(21,7),(21,14),(26,14),(26,24),(22,29),(11,29)],'#d8ab85');box(d,(13,6,19,14),'#eed6b2',width=1);line(d,[(13,22),(21,22)],'#ae806a',1)
    elif name=='streak':
        poly(d,[(16,2),(19,10),(24,7),(28,16),(27,24),(22,29),(10,29),(5,24),(4,17),(10,8),(10,18)],'#c77656');poly(d,[(16,13),(22,22),(18,27),(13,27),(10,23)],GOLD,width=1)
    save(im,name)

tiers=['initiate','seeker','builder','keeper','routine','iron','achiever','legend']
for tier,name in enumerate(tiers):
    im,d=canvas(64)
    metal=['#aa8453','#bf895e','#b8c7c8','#e0b768','#e0b768','#c8d9de','#e6c071','#ead9a5'][tier]
    core=['#9d7650','#775847','#557fa4','#dba44e','#508b75','#a45c61','#866aab','#82c5cf'][tier]
    if tier==0:
        poly(d,[(24,15),(40,15),(49,24),(49,40),(40,49),(24,49),(15,40),(15,24)],'#4f493d')
        poly(d,[(25,18),(39,18),(46,25),(46,39),(39,46),(25,46),(18,39),(18,25)],core,width=1)
        for x in [24,31,39]:line(d,[(x,20),(x,44)],'#72563e',1)
        box(d,(28,28,36,36),metal,width=1)
    else:
        if tier==3:
            poly(d,[(32,4),(37,17),(49,12),(47,25),(60,31),(47,37),(50,50),(37,47),(32,61),(26,48),(13,52),(17,38),(4,32),(17,26),(13,13),(26,17)],metal,width=1)
        if tier>=4:
            for side in [-1,1]:
                for i in range(5):
                    x=32+side*(18+i%2*3);y=22+i*6
                    poly(d,[(x,y),(x+side*6,y-5),(x+side*5,y+3),(x,y+8)],'#84a57b' if tier==4 else metal,width=1)
        if tier>=5:
            for side in [-1,1]:
                for i in range(5):
                    poly(d,[(32+side*13,34+i*2),(32+side*(30-i*2),9+i*7),(32+side*(28-i*2),23+i*6),(32+side*14,45)],'#d3dcce' if tier==5 else metal,width=1)
                    if tier==7:line(d,[(32+side*17,35+i*2),(32+side*(27-i*2),20+i*6)],'#8fcbd2',2)
        ytop=14 if tier<6 else 20
        rx=17 if tier==1 else 19
        poly(d,[(32,ytop-4),(32+rx,ytop+3),(32+rx-2,41),(32,56),(32-rx+2,41),(32-rx,ytop+3)],metal,width=2)
        poly(d,[(32,ytop),(32+rx-4,ytop+6),(32+rx-6,39),(32,50),(32-rx+6,39),(32-rx+4,ytop+6)],core,width=1)
        if tier<3:
            for i in range(tier+1):line(d,[(23,26+i*6),(32,21+i*6),(41,26+i*6)],metal,3)
        else:
            diamond(d,32,33,10,'#8abdaf' if tier==4 else '#9dd6d9' if tier==7 else '#b07fc3' if tier==6 else '#c26c69')
            line(d,[(32,25),(27,33),(32,39)],LIGHT,1)
        if tier>=6:
            poly(d,[(19,17),(16,5),(26,11),(32,2),(38,11),(48,5),(45,17)],metal,width=1)
            for x in [23,32,41]:box(d,(x-1,13,x+1,15),'#8a74b4' if tier==6 else '#a0d5da',width=1)
        if tier==7:
            for x,y in [(7,7),(56,7),(5,49),(58,49)]:diamond(d,x,y,3,LIGHT)
    # An unmistakable increase in footprint, while keeping every edge on the 64px grid.
    cropped=im.crop(im.getbbox()); cropped.thumbnail((30+tier*4+(4 if tier==7 else 0),)*2,Image.Resampling.NEAREST)
    badge=Image.new('RGBA',(64,64));badge.alpha_composite(cropped,((64-cropped.width)//2,(64-cropped.height)//2))
    save(badge,'badge_'+name)

companions=['firefly','sprite','star','fox','bunny','cat','owl','cloud','dragon']
for name in companions:
    im,d=canvas(); color={'fox':'#cb8d5d','cat':'#9daebb','owl':'#a88f75','bunny':'#dfd6c2','dragon':'#9eadd2','sprite':'#8bbb88','cloud':'#d2e7e6','firefly':'#e3d480','star':GOLD}[name]
    if name=='star':poly(d,[(16,3),(20,12),(29,13),(22,19),(24,28),(16,24),(8,28),(10,19),(3,13),(12,12)],color,width=1)
    elif name in ['firefly','cloud','sprite']:
        if name in ['cloud','firefly']:box(d,(2,10,12,16),'#dbe9d9',width=1);box(d,(20,10,30,16),'#dbe9d9',width=1)
        poly(d,[(11,7),(21,7),(25,12),(25,23),(21,27),(11,27),(7,23),(7,12)],color,width=1)
        if name=='sprite':poly(d,[(16,8),(11,3),(20,5)],'#70956e',width=1)
    else:
        box(d,(10,16,23,28),color,width=1);box(d,(7,8,26,21),color,width=1)
        for x in [10,21]:box(d,(x-2,4 if name!='bunny' else 0,x+2,10),color,width=1)
        if name=='fox':poly(d,[(23,22),(29,18),(29,27),(23,28)],color,width=1);box(d,(8,15,12,19),'#f0dfbc',width=1);box(d,(21,15,25,19),'#f0dfbc',width=1)
        if name=='dragon':poly(d,[(9,18),(2,12),(4,24),(10,25)],'#737eab',width=1);poly(d,[(23,18),(30,12),(28,24),(23,25)],'#737eab',width=1)
        if name=='owl':box(d,(9,11,15,18),'#eee3c6',width=1);box(d,(19,11,25,18),'#eee3c6',width=1)
        box(d,(9,27,14,29),'#8a7864',width=1);box(d,(20,27,25,29),'#8a7864',width=1)
    box(d,(11,13,13,15),INK,outline=None);box(d,(20,13,22,15),INK,outline=None)
    box(d,(12,13,12,13),'#ffffff',outline=None);box(d,(21,13,21,13),'#ffffff',outline=None)
    save(im,'companion_'+name)

places=['knowledge','medicine','strength','social','hobbies']
for place in places:
    roof={'knowledge':'#638baa','medicine':'#aa795c','strength':'#79946c','social':'#bc8c65','hobbies':'#9b80ad'}[place]
    for stage in range(4):
        im,d=canvas(64)
        box(d,(4,53,59,59),'#688d67',width=1);box(d,(7,51,56,54),'#a3b07a',width=1)
        if stage==0:poly(d,[(11,50),(32,16),(53,50)],'#bfab7d');poly(d,[(25,50),(32,33),(39,50)],'#5d6253',width=1)
        else:
            box(d,(13,25,51,52),'#d9c39b');poly(d,[(7,28),(32,8),(57,28)],roof);line(d,[(14,24),(32,12),(49,24)],'#bdc7a5',2)
            box(d,(28,37,37,52),'#6e6b59',width=1)
            for x in [18,43]:box(d,(x,33,x+5,39),'#92c0ca',width=1)
        if stage>=1:box(d,(5,42,8,52),'#8a7e55',width=1);box(d,(2,35,11,44),'#80a67b',width=1)
        if stage>=2:line(d,[(48,6),(48,24)],INK,2);poly(d,[(49,7),(61,7),(57,12),(49,12)],GOLD,width=1)
        if stage==3:
            for x in [11,51]:box(d,(x-4,23,x+5,53),'#b6c4b9');box(d,(x-5,19,x+6,25),GOLD,width=1)
            for x,y in [(7,12),(57,17),(29,3)]:diamond(d,x,y,2,LIGHT)
        save(im,f'place_{place}_{stage}')

scenes={'meadow':('#b7d9de','#a9bf82'),'sunset':('#e8b48b','#b9b783'),'moonlight':('#23394e','#567262'),'forest':('#a9c6b6','#7fa17a'),'beach':('#9dd5dc','#d6c28c'),'snow':('#b6cedc','#e1e6d9'),'aurora':('#26364f','#637c85'),'sky_islands':('#b4add5','#abc2a0')}
for name,(sky,earth) in scenes.items():
    im,d=canvas(160);im=Image.new('RGBA',(160,240),sky);d=ImageDraw.Draw(im)
    box(d,(69,14,91,30),'#edda98',outline=None);box(d,(65,18,95,26),'#edda98',outline=None)
    if name in ['moonlight','aurora']:
        for i in range(30):x=(i*43)%160;y=(i*29)%100;d.rectangle((x,y,x+1,y+1),fill='#e6dfba')
    poly(d,[(0,97),(20,97),(20,88),(45,88),(45,93),(75,93),(75,82),(115,82),(115,94),(145,94),(145,105),(160,105),(160,240),(0,240)],earth,outline=None)
    rng=random.Random(13)
    for i in range(180):
        x=rng.randrange(160);y=rng.randrange(109,240);d.rectangle((x,y,x+1,y+1),fill='#a1b27b' if name not in ['snow','moonlight','aurora'] else '#7e9691' if name!='snow' else '#becfd0')
    # A connected, stepped trail between the five world places.
    for xy in [(30,52,38,144),(34,78,126,86),(122,52,130,144),(30,136,130,145),(76,82,84,215)]:d.rectangle(xy,fill='#c7b38b')
    for xy in [(31,55,33,135),(38,79,121,80),(123,55,125,135),(78,89,79,210)]:d.rectangle(xy,fill='#e0cda3')
    if name=='forest':
        for x,y in [(0,40),(145,55),(8,165),(145,172)]:box(d,(x+5,y+18,x+9,y+38),'#7e7257',width=1);poly(d,[(x,y+24),(x+7,y),(x+15,y+24)],'#668c6c',width=1)
    if name=='beach':
        d.rectangle((0,215,159,239),fill='#76b8c8')
        for y in [217,227,236]:
            for x in range(0,160,14):d.rectangle((x,y,x+8,y+1),fill='#d3e7d7')
    if name=='snow':
        for i in range(50):x=(i*37)%160;y=(i*23)%240;d.rectangle((x,y,x+1,y+1),fill='#f6f5df')
    if name=='aurora':
        for x in range(160):
            y=int(28+8*math.sin(x/24));d.rectangle((x,y,x,y+6),fill='#7cafaa');d.rectangle((x,y+8,x,y+11),fill='#927fae')
    if name=='sky_islands':
        for x,y in [(6,25),(116,35)]:box(d,(x,y,x+30,y+6),'#cddcbb',width=1);poly(d,[(x,y+6),(x+30,y+6),(x+15,y+24)],'#9b8ca3',width=1)
    im.save(OUT/f'pixel_world_{name}.png')
    room=Image.new('RGBA',(160,120),'#dfcba2');r=ImageDraw.Draw(room)
    for y in range(8,85,12):r.line((0,y,160,y),fill='#d2bd94')
    r.rectangle((0,87,160,119),fill='#aa8867')
    for y in range(88,120,8):r.line((0,y,160,y),fill='#856c57')
    for x in range(0,160,24):r.line((x,88,x,120),fill='#95795f')
    box(r,(16,16,62,64),'#7c6e5c');box(r,(20,20,58,60),sky,width=1);line(r,[(39,20),(39,60)],'#7c6e5c',2);line(r,[(20,40),(58,40)],'#7c6e5c',2)
    box(r,(24,69,58,73),'#998466',width=1);box(r,(112,74,151,79),'#947051',width=1);box(r,(117,79,121,97),'#947051',width=1);box(r,(143,79,147,97),'#947051',width=1)
    room.save(OUT/f'pixel_room_{name}.png')

for name in ['plain','flowers','lanterns','bookshelf','rug','plants','aquarium','telescope','fireplace','trophy']:
    im,d=canvas(48)
    if name=='plain':box(d,(6,33,42,39),'#aa8b67',width=1)
    elif name in ['flowers','plants']:
        for x in [8,21,34]:
            box(d,(x-3,31,x+7,43),'#b78567',width=1);line(d,[(x+2,33),(x+2,16)],'#638563',2)
            if name=='flowers':
                for dx,dy in [(-3,0),(3,0),(0,-3),(0,3)]:box(d,(x+dx,15+dy,x+dx+3,18+dy),'#d79caa' if x!=21 else GOLD,width=1)
            else:poly(d,[(x+2,25),(x-5,15),(x+1,12),(x+5,19),(x+11,11),(x+12,20)],'#7a9e71',width=1)
    elif name=='lanterns':
        for x,y in [(9,10),(24,5),(39,10)]:line(d,[(x,0),(x,y)],INK,1);box(d,(x-5,y,x+5,y+14),'#d7ab5c',width=1);box(d,(x-2,y+3,x+2,y+10),'#f4da8c',width=1)
    elif name=='bookshelf':
        box(d,(6,6,42,43),'#997555')
        for row in [10,24]:
            for i,c in enumerate(['#b67c65','#7c9daa','#a1aa79','#b7a064']):box(d,(10+i*7,row,14+i*7,row+12),c,width=1)
    elif name=='rug':
        box(d,(3,25,45,43),'#b77e8f',width=1);box(d,(7,28,41,40),'#d4b88a',width=1);box(d,(12,31,36,37),'#90728e',width=1);box(d,(5,12,18,23),'#b9a16d',width=1);box(d,(30,12,43,23),'#82a49b',width=1)
    elif name=='aquarium':
        box(d,(3,12,45,40),'#657f84');box(d,(6,15,42,36),'#8ec2cb',width=1)
        for x,y in [(12,22),(26,28),(32,19)]:poly(d,[(x,y),(x+6,y-2),(x+6,y+4),(x,y+3),(x-3,y+5),(x-3,y-1)],'#dbaa6c',width=1)
    elif name=='telescope':
        line(d,[(24,25),(12,44)],'#9a7c5c',3);line(d,[(24,25),(39,44)],'#9a7c5c',3);line(d,[(24,25),(24,44)],'#9a7c5c',2);poly(d,[(8,17),(35,5),(40,14),(13,26)],'#7b95ad');line(d,[(34,6),(38,14)],'#c5d5ce',2)
    elif name=='fireplace':
        box(d,(5,10,43,44),'#aa8e7c');box(d,(12,22,36,44),'#524e4d');poly(d,[(15,41),(19,27),(25,35),(30,25),(34,42)],'#cb8355',width=1);poly(d,[(21,41),(25,34),(29,41)],GOLD,width=1)
    elif name=='trophy':
        box(d,(4,39,44,43),'#937354',width=1);box(d,(12,7,36,22),GOLD);poly(d,[(12,19),(20,28),(28,28),(36,19)],GOLD);box(d,(22,28,26,36),'#b39155',width=1);box(d,(16,35,32,38),GOLD,width=1)
    save(im,'decor_'+name)

files=sorted(OUT.glob('pixel_*.png'));sheet=Image.new('RGB',(960,math.ceil(len(files)/10)*110),'#17283b');d=ImageDraw.Draw(sheet)
for i,path in enumerate(files):
    icon=Image.open(path).convert('RGBA');icon.thumbnail((70,70),Image.Resampling.NEAREST);x=(i%10)*96;y=(i//10)*110;sheet.paste(icon,(x+(96-icon.width)//2,y),icon);d.text((x+3,y+76),path.stem.replace('pixel_','')[:15],fill='#dcd5bd')
target=ROOT/'design/pixel';target.mkdir(parents=True,exist_ok=True);sheet.save(target/'catalog.png')
print(f'Built {len(files)} crisp pixel sprites and a catalog.')
