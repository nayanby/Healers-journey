"""Render the packaged GLB geometry for asset review; mirrors the Android renderer."""
import json
import math
import struct
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
image = Image.new('RGB', (1500, 370), '#233B4B')
draw = ImageDraw.Draw(image)
for index, name in enumerate(['learning', 'work', 'fitness', 'social', 'creativity']):
    data = (ROOT / f'app/src/main/assets/trophies/{name}.glb').read_bytes()
    length = struct.unpack_from('<I', data, 12)[0]
    doc = json.loads(data[20:20+length]); binary = 28 + length
    assert struct.unpack_from('<III', data)[0:2] == (0x46546c67, 2)
    assert struct.unpack_from('<I', data, 8)[0] == len(data)
    faces = []
    for primitive in doc['meshes'][0]['primitives']:
        accessor = doc['accessors'][primitive['attributes']['POSITION']]
        view = doc['bufferViews'][accessor['bufferView']]
        start = binary + view.get('byteOffset', 0)
        assert start + accessor['count'] * 12 <= len(data)
        color = doc['materials'][primitive['material']]['pbrMetallicRoughness']['baseColorFactor'][:3]
        vertices = []
        for i in range(accessor['count']):
            x,y,z = struct.unpack_from('<3f', data, start+i*12)
            a = math.radians(-20); x,z = x*math.cos(a)+z*math.sin(a),-x*math.sin(a)+z*math.cos(a)
            y -= .85; y,z = y*math.cos(.16)-z*math.sin(.16)+.85,y*math.sin(.16)+z*math.cos(.16)
            vertices.append((x,y,z))
        for i in range(0,len(vertices),3): faces.append((vertices[i:i+3],color))
    faces.sort(key=lambda f:sum(v[2] for v in f[0])/3)
    for vertices,color in faces:
        a,b,c=vertices;u=[b[i]-a[i] for i in range(3)];v=[c[i]-a[i] for i in range(3)]
        n=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]];norm=math.sqrt(sum(k*k for k in n))
        if norm<1e-6 or n[2]<=0:continue
        n=[k/norm for k in n]
        light=max(0,-.40*n[0]+.65*n[1]+.64*n[2]);spec=max(0,-.25*n[0]+.35*n[1]+.90*n[2])**18*.30
        rgb=tuple(max(0,min(255,int((c*(.38+.64*light)+spec)*255))) for c in color)
        points=[(index*300+300*(.5+x*.60*(4/(4-z))),300*(.91-y*.44*(4/(4-z)))) for x,y,z in vertices]
        draw.polygon(points,fill=rgb)
    draw.text((index*300+105,320),name.title(),fill='#FFE1A5')
out=ROOT/'build/planning-check/trophies-preview.png';out.parent.mkdir(parents=True,exist_ok=True);image.save(out)
print(out)
