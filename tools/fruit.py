"""
Item textures for tree fruit: cherries, cherry pie, black walnut (in its green husk) and shelled walnut.
All original pixel art.  python3 tools/fruit.py
"""
import math, os, random
from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), "../src/main/resources/assets/ryancraft/textures/item")

def from_art(art, pal):
    rows = art.strip("\n").split("\n"); assert len(rows) == 16 and all(len(r) == 16 for r in rows), [len(r) for r in rows]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c != ".": px[x, y] = pal[c] + (255,)
    return img

CHERRIES = """
................
...........gL...
..........gGLL..
.........g.G.L..
........g..G....
.......g...G....
......g....G....
.....g......G...
....g.......G...
..oRRo....oRRo..
.oRHRRo..oRHRRo.
.oHRRRo..oHRRRo.
.oRRRro..oRRRro.
.orRrro..orRrro.
..oooo....oooo..
................
"""

def cherries():
    return from_art(CHERRIES, {"o": (64, 6, 18), "r": (120, 14, 32), "R": (168, 24, 44), "H": (232, 110, 120),
                               "g": (78, 92, 34), "G": (110, 128, 46), "L": (84, 140, 52)})

def outline(img, col):
    px = img.load(); out = img.copy(); po = out.load()
    for x in range(16):
        for y in range(16):
            if px[x, y][3] == 0 and any(0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3]
                                        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                po[x, y] = col + (255,)
    return out

def cherry_pie():
    """A pie in three-quarter view: golden lattice over deep red filling, crimped rim, baked side."""
    r = random.Random(4); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    cx, cy, rx, ry = 7.5, 7.0, 6.6, 3.9
    crust = {"hi": (240, 196, 120), "md": (214, 160, 84), "dk": (170, 112, 52), "vdk": (120, 74, 34)}
    fill = {"hi": (214, 56, 70), "md": (166, 24, 42), "dk": (112, 12, 28)}
    for x in range(16):
        for y in range(16):
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if d <= 1.0:                                         # top surface
                if d > 0.72:                                     # crimped rim
                    c = crust["hi"] if (x + y) % 2 == 0 else crust["md"]
                elif x in (4, 8, 11) or y in (5, 8):              # lattice strips
                    c = crust["hi"] if x < cx else crust["md"]
                else:
                    c = fill["md"] if r.random() < .7 else (fill["hi"] if r.random() < .45 else fill["dk"])
                px[x, y] = c + (255,)
            elif y > cy and abs(x - cx) <= rx and ((x - cx) / rx) ** 2 + ((y - 3.0 - cy) / ry) ** 2 <= 1.0:   # side
                c = crust["dk"] if x % 2 else crust["md"]
                if ((x - cx) / rx) ** 2 + ((y - 3.0 - cy) / ry) ** 2 > 0.7: c = crust["vdk"]
                px[x, y] = c + (255,)
    return outline(img, (78, 44, 20))

def sphere(cx, cy, rad, base, dark, light, speck, seed):
    r = random.Random(seed); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for x in range(16):
        for y in range(16):
            dx, dy = (x - cx) / rad, (y - cy) / rad
            if dx * dx + dy * dy <= 1:
                lit = -dx * .55 - dy * .65 + math.sqrt(max(0, 1 - dx * dx - dy * dy)) * .5
                c = light if lit > .62 else base if lit > .1 else dark
                if r.random() < speck: c = tuple(int(v * .78) for v in c)
                px[x, y] = c + (255,)
    return img

def walnut():
    """A black walnut still in its round green husk, with a stub of stem."""
    img = sphere(7.5, 8.8, 5.9, (112, 138, 44), (78, 98, 30), (150, 176, 70), .18, 7)
    img = outline(img, (44, 56, 18)); px = img.load()
    px[8, 1] = px[8, 2] = (92, 70, 44, 255); px[9, 1] = (70, 52, 32, 255)
    return img

def shelled_walnut():
    """The kernel: two lumpy, deeply wrinkled lobes either side of a groove (it looks a bit like a brain)."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    lo, md, hi, dk = (146, 94, 50), (188, 134, 76), (224, 180, 116), (104, 64, 32)
    cx, cy = 7.5, 8.0
    for x in range(16):
        for y in range(16):
            ang = math.atan2(y - cy, x - cx)
            rad = 1 + .13 * math.sin(ang * 7)                                    # lumpy edge
            d = math.hypot((x - cx) / 6.5, (y - cy) / 5.2) / rad
            if d > 1: continue
            if abs(x - cx) < .6: c = dk
            else:
                side = 1 if x > cx else -1
                lobe = ((x - cx - side * 3.2) / 2.6) ** 2 + ((y - cy) / 3.4) ** 2     # each half is a rounded lobe
                fold = math.sin(y * 2.3 + abs(x - cx) * .9)
                c = hi if lobe < .35 and fold > -.2 else md if fold > -.45 else lo
                if fold < -.85: c = dk
            px[x, y] = c + (255,)
    return outline(img, (86, 52, 26))

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for name, fn in (("cherries", cherries), ("cherry_pie", cherry_pie), ("black_walnut", walnut), ("shelled_walnut", shelled_walnut)):
        fn().save(f"{OUT}/{name}.png")
    print("ok")
