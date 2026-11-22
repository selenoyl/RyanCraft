"""Isometric sheet of the big game and predators (front, back, young): python3 tools/render_big_game.py -> docs/big_game_iso.png"""
import sys, math; sys.path.insert(0, "/home/user/ryancraft/tools")
import render_iso as R
from PIL import Image, ImageDraw, ImageFilter
BG=(243,239,230); INK=(46,42,36); SUB=(128,118,104)
ROWS=[("Elk","meadows, windswept hills, taiga",[("Bull","elk","elk_bull",1),("Cow","elk","elk",1),("Calf","elk","elk",.5)]),
      ("Moose","taiga, swamps, tundra",[("Bull","moose","moose_bull",1),("Cow","moose","moose",1),("Calf","moose","moose",.5)]),
      ("Pronghorn","plains, savanna, badlands",[("Adult","pronghorn","pronghorn",1),("Fawn","pronghorn","pronghorn_fawn",.5)]),
      ("Grizzly Bear","old-growth taiga, high country",[("Adult","grizzly_bear","grizzly_bear",1),("Cub","grizzly_bear","grizzly_bear",.5)]),
      ("Bobcat","forests, swamps, scrub (scarce)",[("Adult","bobcat","bobcat",1),("Kitten","bobcat","bobcat",.5)])]
ANGLES=[("front", math.radians(-35)), ("back", math.radians(145))]
blocks=[]
for title,sub,items in ROWS:
    ims=[]
    for lab,m,t,sc in items:
        for an,yaw in ANGLES:
            if sc<1 and an=="back": continue
            R.YAW=yaw; im=R.render(m,t,sc); ims.append((f"{lab}" + ("" if an=="front" else " (back)"), im))
    blocks.append((title,sub,ims))
rowh=[max(i.height for _,i in b[2])+90 for b in blocks]
W=max(sum(i.width+40 for _,i in b[2]) for b in blocks)+300
H=sum(rowh)+150
c=Image.new("RGB",(W,H),BG); d=ImageDraw.Draw(c)
d.text((40,36),"RyanCraft: new big game and predators",font=R.font(44,True),fill=INK)
d.text((42,92),"Same scale throughout (1 model pixel = %d screen pixels)." % R.PX,font=R.font(20),fill=SUB)
y=150
for (title,sub,ims),rh in zip(blocks,rowh):
    base=y+rh-60
    d.text((40,base-70),title,font=R.font(30,True),fill=INK); d.text((40,base-30),sub,font=R.font(17),fill=SUB)
    x=300
    for lab,im in ims:
        sh=Image.new("L",(int(im.width*.9)+40,40),0); ImageDraw.Draw(sh).ellipse((20,8,sh.width-20,32),fill=70); sh=sh.filter(ImageFilter.GaussianBlur(7))
        c.paste((205,198,184),(x+im.width//2-sh.width//2,base-22),sh); c.paste(im,(x,base-im.height),im)
        d.text((x+im.width//2,base+24),lab,font=R.font(18),fill=SUB,anchor="mm"); x+=im.width+40
    y+=rh
c.save("/home/user/ryancraft/docs/big_game_iso.png"); print(c.size)
