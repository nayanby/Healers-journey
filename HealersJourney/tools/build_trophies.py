"""Export five lightweight, genuinely three-dimensional GLB trophy meshes. No external libraries."""
import json
import math
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/assets/trophies'
GOLD = (0.95, 0.65, 0.17, 1)
LIGHT = (1.0, 0.84, 0.40, 1)
DARK = (0.20, 0.16, 0.23, 1)


class Model:
    def __init__(self):
        self.parts = []

    def triangles(self, points, color):
        self.parts.append((points, color))

    def box(self, x, y, z, w, h, d, color):
        v = [(x+dx*w/2, y+dy*h/2, z+dz*d/2) for dx,dy,dz in
             [(-1,-1,-1),(1,-1,-1),(1,1,-1),(-1,1,-1),(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)]]
        self.triangles([v[i] for i in [0,2,1,0,3,2,4,5,6,4,6,7,0,4,7,0,7,3,1,2,6,1,6,5,3,7,6,3,6,2,0,1,5,0,5,4]],color)

    def lathe(self, profile, color, cx=0, cz=0, segments=20):
        points=[]
        for (r1,y1),(r2,y2) in zip(profile,profile[1:]):
            for i in range(segments):
                a,b=2*math.pi*i/segments,2*math.pi*(i+1)/segments
                q=[(cx+r1*math.cos(a),y1,cz+r1*math.sin(a)),(cx+r1*math.cos(b),y1,cz+r1*math.sin(b)),
                   (cx+r2*math.cos(b),y2,cz+r2*math.sin(b)),(cx+r2*math.cos(a),y2,cz+r2*math.sin(a))]
                points += [q[k] for k in (0,2,1,0,3,2)]
        self.triangles(points,color)

    def orb(self,x,y,z,r,color):
        self.lathe([(r*math.sin(math.pi*i/8), y-r*math.cos(math.pi*i/8)) for i in range(9)],color,x,z,12)

    def handle(self,side):
        # Curved, solid handles in the XY plane, attached to both sides of the bowl.
        p=[]
        for i in range(14):
            a,b=-math.pi*.65+i*math.pi*1.3/14,-math.pi*.65+(i+1)*math.pi*1.3/14
            for j in range(6):
                def v(t,k):
                    u=2*math.pi*k/6
                    return (side*(.36+(.23+.045*math.cos(u))*math.cos(t)),1.12+(.28+.045*math.cos(u))*math.sin(t),.045*math.sin(u))
                q=[v(a,j),v(b,j),v(b,j+1),v(a,j+1)]
                p += [q[k] for k in ((0,2,1,0,3,2) if side < 0 else (0,1,2,0,2,3))]
        self.triangles(p,GOLD)

    def export(self,path):
        data=bytearray(); views=[]; access=[]; primitives=[]; materials=[]
        for vertices,color in self.parts:
            normals=[]
            for i in range(0,len(vertices),3):
                a,b,c=vertices[i:i+3]; u=[b[k]-a[k] for k in range(3)];v=[c[k]-a[k] for k in range(3)]
                n=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]]
                length=math.sqrt(sum(k*k for k in n)) or 1
                normals += [tuple(k/length for k in n)]*3
            ids=[]
            for values in (vertices,normals):
                offset=len(data)
                for value in values:data.extend(struct.pack('<3f',*value))
                views.append(dict(buffer=0,byteOffset=offset,byteLength=len(data)-offset,target=34962))
                accessor=dict(bufferView=len(views)-1,componentType=5126,count=len(values),type='VEC3')
                if values is vertices:
                    accessor['min']=[min(v[k] for v in values) for k in range(3)]
                    accessor['max']=[max(v[k] for v in values) for k in range(3)]
                access.append(accessor);ids.append(len(access)-1)
            materials.append(dict(pbrMetallicRoughness=dict(baseColorFactor=color,metallicFactor=.65,roughnessFactor=.3),doubleSided=True))
            primitives.append(dict(attributes=dict(POSITION=ids[0],NORMAL=ids[1]),material=len(materials)-1,mode=4))
        doc=dict(asset=dict(version='2.0',generator='Life RPG trophy builder'),scene=0,scenes=[dict(nodes=[0])],nodes=[dict(mesh=0)],meshes=[dict(primitives=primitives)],
                 buffers=[dict(byteLength=len(data))],bufferViews=views,accessors=access,materials=materials)
        header=json.dumps(doc,separators=(',',':')).encode();header+=b' '*((-len(header))%4);data+=b'\0'*((-len(data))%4)
        path.write_bytes(struct.pack('<III',0x46546c67,2,12+8+len(header)+8+len(data))+struct.pack('<II',len(header),0x4e4f534a)+header+struct.pack('<II',len(data),0x004e4942)+data)


def build(name,accent):
    m=Model()
    m.lathe([(0,0),(.40,0),(.43,.04),(.43,.13),(.36,.18),(.34,.23),(0,.23)],DARK)
    m.lathe([(.37,.14),(.39,.17),(.36,.22),(.33,.24)],GOLD)
    m.box(0,.10,.407,.36,.075,.025,LIGHT)
    m.lathe([(0,.23),(.18,.23),(.19,.30),(.09,.37),(.085,.64),(.20,.70)],GOLD)
    m.handle(-1);m.handle(1)
    m.lathe([(.10,.64),(.22,.75),(.36,.93),(.43,1.19),(.44,1.27),(.39,1.27),(.37,1.17),(.29,.96),(.12,.81),(0,.81)],GOLD)
    m.lathe([(.43,1.20),(.46,1.24),(.46,1.29),(.39,1.29)],LIGHT)
    m.lathe([(.27,.87),(.32,.94),(.34,1.0)],accent)
    if name=='learning':
        m.box(0,1.37,0,.58,.09,.36,accent);m.box(0,1.44,0,.53,.07,.33,(1,.94,.75,1));m.box(0,1.49,0,.58,.04,.36,accent)
        m.box(-.02,1.58,0,.44,.09,.30,GOLD);m.box(-.02,1.65,0,.40,.05,.27,(1,.94,.75,1));m.box(-.02,1.69,0,.44,.035,.30,LIGHT)
    elif name=='work':
        m.box(0,1.51,0,.63,.40,.24,accent);m.box(0,1.55,.135,.64,.04,.035,LIGHT)
        m.box(-.12,1.77,0,.045,.14,.09,GOLD);m.box(.12,1.77,0,.045,.14,.09,GOLD);m.box(0,1.83,0,.28,.04,.09,LIGHT)
        m.box(0,1.52,.16,.09,.12,.04,GOLD)
    elif name=='fitness':
        m.box(0,1.49,0,.72,.085,.085,LIGHT)
        for x in (-.25,.25):
            m.box(x,1.49,0,.13,.38,.32,accent);m.box(x*1.45,1.49,0,.07,.28,.25,GOLD)
    elif name=='social':
        for x,y in ((-.17,1.45),(.17,1.40)):
            m.lathe([(0,y-.16),(.15,y-.16),(.10,y+.13),(0,y+.13)],accent,cx=x,segments=12)
            m.orb(x,y+.25,0,.14,LIGHT)
        m.box(0,1.43,.14,.26,.07,.10,GOLD)
    else:
        m.orb(0,1.53,0,.31,accent)
        for x,y,c in [(-.15,1.64,(1,.38,.37,1)),(.08,1.73,(.25,.8,1,1)),(.16,1.5,(1,.88,.2,1))]:m.orb(x,y,.25,.065,c)
        m.box(-.27,1.61,.09,.055,.65,.055,LIGHT);m.orb(-.27,1.92,.09,.065,GOLD)
    OUT.mkdir(parents=True,exist_ok=True);m.export(OUT/f'{name}.glb')
    print(f'{name}: {sum(len(v)//3 for v,c in m.parts)} triangles')

for name,accent in [('learning',(.16,.58,.86,1)),('work',(.48,.33,.75,1)),('fitness',(.14,.68,.50,1)),('social',(.91,.35,.42,1)),('creativity',(.71,.32,.78,1))]:build(name,accent)
