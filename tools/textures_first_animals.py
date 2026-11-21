import random, os
from PIL import Image
random.seed(7)
A=__import__("os").path.join(__import__("os").path.dirname(__file__), "..", "src/main/resources/assets")
def hexc(h): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))
def noisy(c,amt=12):
    d=random.randint(-amt,amt); return tuple(max(0,min(255,v+d)) for v in c)+(255,)
def fill(img,x,y,w,h,col,amt=12):
    for i in range(int(x),int(x+w)):
        for j in range(int(y),int(y+h)):
            if 0<=i<img.width and 0<=j<img.height: img.putpixel((i,j),noisy(hexc(col) if isinstance(col,str) else col,amt))
def box(img,u,v,w,h,d,col,top=None,bottom=None,front=None):
    """Paint all faces of a box-UV cube. Returns the front-face origin."""
    fill(img,u+d,v,w,d,top or col)            # top
    fill(img,u+d+w,v,w,d,bottom or col)       # bottom
    fill(img,u,v+d,d,h,col)                   # right side
    fill(img,u+d,v+d,w,h,front or col)        # front
    fill(img,u+d+w,v+d,d,h,col)               # left side
    fill(img,u+2*d+w,v+d,w,h,col)             # back
    return (u+d,v+d)
def px(img,x,y,col): img.putpixel((x,y),hexc(col)+(255,))
def save(img,path):
    p=os.path.join(A,path); os.makedirs(os.path.dirname(p),exist_ok=True); img.save(p)

# BOAR 64x64: dark bristly brown, pale snout, ivory tusks
im=Image.new("RGBA",(64,64),(0,0,0,0))
fx,fy=box(im,0,0,8,8,6,"#5a3d2b")                 # head
for x,y in [(fx+1,fy+2),(fx+6,fy+2)]: px(im,x,y,"#ffffff"); px(im,x,y+1,"#111111")
sx,sy=box(im,28,0,4,3,3,"#b07d68",front="#c8917a") # snout
px(im,sx+1,sy+1,"#4a2a20"); px(im,sx+2,sy+1,"#4a2a20")
box(im,42,0,1,3,1,"#efe6cf")                       # tusks
box(im,28,6,2,2,12,"#2e1f17")                      # bristles
box(im,0,20,10,9,16,"#5a3d2b",top="#4a3222",bottom="#7a5a44")
for _ in range(60):                                # speckles
    px(im,random.randint(16,26),random.randint(20,35),"#3f2a1d")
box(im,0,48,4,7,4,"#4a3222",bottom="#2a1a12")
fill(im,4,58-3,4,3,"#2a1a12",4); fill(im,0,58-3,16,2,"#3a2618",4)
save(im,"ryancraft/textures/entity/boar.png")

# DEER 64x64: tan with white belly, dark nose, antlers
im=Image.new("RGBA",(64,64),(0,0,0,0))
box(im,0,0,4,9,4,"#a0703f")                         # neck
fx,fy=box(im,16,0,6,5,7,"#a0703f")                  # skull
for x in (fx,fx+5): px(im,x,fy+1,"#111111")
nx,ny=box(im,42,0,4,3,3,"#b58552",front="#c0956a")  # snout
px(im,nx+1,ny,"#222222"); px(im,nx+2,ny,"#222222")
box(im,56,0,1,6,1,"#d9c7a0"); box(im,56,8,3,1,1,"#d9c7a0")
box(im,0,18,8,8,16,"#a0703f",top="#8d6034",bottom="#efe4cf")
for _ in range(18): px(im,random.randint(16,23),random.randint(18,33),"#e8dcc0")  # fawn spots on back
box(im,48,18,3,9,3,"#8d6034",bottom="#2a2018")
fill(im,48,28,12,2,"#3a2a1c",4)
box(im,48,30,2,3,1,"#f2ece0")                       # white tail
save(im,"ryancraft/textures/entity/deer.png")

# DUCK 32x32: mallard-ish: green head, brown chest/grey body, orange bill & legs
im=Image.new("RGBA",(32,32),(0,0,0,0))
fx,fy=box(im,0,0,4,5,3,"#1f6b3a")
px(im,fx,fy+1,"#111111"); px(im,fx+3,fy+1,"#111111")
fill(im,fx,fy+4,4,1,"#f0f0f0",2)                   # white collar
box(im,14,0,3,1,2,"#e8b020")
box(im,0,9,6,5,8,"#9a9a92",top="#7d7d76",front="#7a4a32")
box(im,0,22,1,3,6,"#6e6a62")
fill(im,1,26,6,1,"#3a5fb0",2)                      # blue wing stripe
box(im,24,0,1,3,1,"#e8901c")
save(im,"ryancraft/textures/entity/duck.png")

# ITEMS 16x16
def meat(raw,base,fat,path,shape="steak"):
    im=Image.new("RGBA",(16,16),(0,0,0,0))
    rows=["....xxxxx.......","..xxxxxxxxx.....",".xxxxxxxxxxx....",".xxxxxxxxxxxx...","xxxxxxxxxxxxx...","xxxxxxxxxxxxxx..","xxxxxxxxxxxxxx..",".xxxxxxxxxxxxxx.","..xxxxxxxxxxxxx.","...xxxxxxxxxxxx.","....xxxxxxxxxx..",".....xxxxxxxx...","......xxxxx....."] if shape=="steak" else \
         ["......xxxx......",".....xxxxxx.....","....xxxxxxxx....","....xxxxxxxx....","...xxxxxxxxx....","...xxxxxxxxxx...","..xxxxxxxxxxx...","..xxxxxxxxxx....","...xxxxxxxx.....","....xxxxxx......","....ff..........","...ffff.........","....ff.........."]
    for y,r in enumerate(rows):
        for x,c in enumerate(r):
            if c=='x':
                edge = x==0 or r[x-1]=='.' or x==len(r)-1 or r[x+1]=='.' or y==0 or rows[y-1][x]=='.' or y==len(rows)-1 or rows[y+1][x]=='.' if y+1<len(rows) else True
                im.putpixel((x+1,y+1), noisy(hexc(fat if edge else base),10))
            elif c=='f': im.putpixel((x+1,y+1), noisy(hexc("#efe6cf"),6))
    save(im,path)
meat(True,"#b8463c","#e6a39a","ryancraft/textures/item/venison.png")
meat(False,"#7a4a2a","#a8784a","ryancraft/textures/item/cooked_venison.png")
meat(True,"#e09a8c","#f2c8bc","ryancraft/textures/item/raw_duck.png","leg")
meat(False,"#b0702e","#d29a50","ryancraft/textures/item/cooked_duck.png","leg")

# Blank edition badge: hides the "Java Edition" subtitle under the logo, like the old title screen
save(Image.new("RGBA",(128,16),(0,0,0,0)),"minecraft/textures/gui/title/edition.png")

# Mod icon: boar head
im=Image.new("RGBA",(32,32),(0,0,0,0))
fill(im,4,6,24,22,"#5a3d2b"); fill(im,10,18,12,8,"#c8917a"); fill(im,13,21,2,2,"#4a2a20",0); fill(im,17,21,2,2,"#4a2a20",0)
fill(im,8,11,3,3,"#ffffff",0); fill(im,9,12,2,2,"#111111",0); fill(im,21,11,3,3,"#ffffff",0); fill(im,21,12,2,2,"#111111",0)
fill(im,7,20,2,6,"#efe6cf",0); fill(im,23,20,2,6,"#efe6cf",0); fill(im,4,4,24,3,"#2e1f17")
save(im.resize((128,128),Image.NEAREST),"ryancraft/icon.png")
print("done")
