"""
Venison and bison icons in the Programmer Art meat style: drawn from the base game's mutton tones (lit rim top-left,
shaded rims bottom-right, a few pale flecks), each in its own cut so they don't look like mutton or beef. Venison is a
lean, plump teardrop; bison a big round roast with a cap of fat. Used by tools/meats.py.
"""
import random, colorsys
from PIL import Image

LOIN = """
................
................
...........##...
.........#####..
.......#######..
.....########...
....#########...
...#########....
..##########....
..#########.....
..#########.....
..########......
..#######.......
...#####........
................
................
"""
HUMP = """
................
................
................
....######......
..##########....
.############...
.#############..
.#############..
.##############.
.##############.
.##############.
..#############.
..############..
...##########...
.....######.....
................
"""
# the Programmer Art mutton's own tones: lit rim, flesh, highlights, and the shaded rims bottom-right
RAW = dict(lit=(0xad,0x33,0x2e), flesh=[(0xe2,0x62,0x5a),(0xe2,0x72,0x69),(0xe2,0x49,0x40),(0xdd,0x38,0x30),(0xd1,0x2e,0x26)],
           hi=(0xe8,0x8a,0x82), r1=[(0x88,0x22,0x1d),(0x78,0x13,0x0f)], r2=[(0x5d,0x0e,0x0a),(0x47,0x0a,0x06)])
def shade(mask, seed, hi_p, fat_rows=0, fat=None):
    m=[r for r in mask.strip('\n').split('\n')]; on=lambda x,y: 0<=x<16 and 0<=y<16 and m[y][x]=='#'
    rnd=random.Random(seed); im=Image.new('RGBA',(16,16)); px=im.load()
    for y in range(16):
        for x in range(16):
            if not on(x,y): continue
            if not on(x+1,y) or not on(x,y+1): c=rnd.choice(RAW['r2'])
            elif not on(x+2,y) or not on(x,y+2): c=rnd.choice(RAW['r1'])
            elif not on(x-1,y) or not on(x,y-1): c=RAW['lit']
            else: c=RAW['hi'] if rnd.random()<hi_p else rnd.choice(RAW['flesh'])
            if fat and sum(on(x,yy) for yy in range(y-fat_rows,y))<fat_rows and on(x+1,y) and on(x,y+1):
                c=fat[0] if not on(x,y-1) or not on(x-1,y) else fat[1]
            px[x,y]=c+(255,)
    return im
def recolour(im, hue, sat_k, val_k):
    im=im.copy(); px=im.load()
    for y in range(16):
        for x in range(16):
            r,g,b,a=px[x,y]
            if not a: continue
            h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
            if s<0.25 and v>0.7: continue
            px[x,y]=tuple(int(255*t) for t in colorsys.hsv_to_rgb(hue,min(1,s*sat_k),min(1,v*val_k)))+(a,)
    return im
def cook(im, hue, sat_k, val_k, fat_to=None):
    out=recolour(im,hue,sat_k,val_k); px=out.load()
    if fat_to:
        for y in range(16):
            for x in range(16):
                r,g,b,a=px[x,y]
                if a and (r,g,b) in fat_to: px[x,y]=fat_to[(r,g,b)]+(255,)
    return out
FAT=[(244,232,218),(226,208,190)]
loin=shade(LOIN,3,0.07)                             # lean: hardly any pale flecks
hump=shade(HUMP,5,0.16,fat_rows=2,fat=FAT)          # a cap of fat over well-marbled meat
def icons():
    return {'venison':recolour(loin,0.985,1.1,0.82),'cooked_venison':cook(loin,0.045,0.95,0.6),
            'raw_bison':recolour(hump,0.0,1.25,0.66),'bison_steak':cook(hump,0.05,1.0,0.5,{FAT[0]:(214,170,110),FAT[1]:(190,140,84)})}
