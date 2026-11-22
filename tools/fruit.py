"""
Item textures for tree fruit: cherries, cherry pie, black walnut (in its green husk) and shelled walnut.
All original pixel art.  python3 tools/fruit.py
"""
import json, math, os, random
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

# Cherry pie: a copy of the vanilla pumpkin pie with its filling hue-shifted to cherry red (read from the
# Minecraft jar in the Gradle cache). Its own texture, so resource packs and Programmer Art don't touch it.
PIE_FILLING = [(234, 158, 96), (216, 139, 80), (238, 172, 110), (241, 184, 123), (183, 92, 18), (164, 84, 19), (219, 116, 34)]

def cherry_shade(c):
    import colorsys
    h, s, v = colorsys.rgb_to_hsv(*[x / 255 for x in c])
    h = (h - 0.085) % 1.0; s = min(0.9, s * 1.35); v *= 0.9
    return tuple(round(x * 255) for x in colorsys.hsv_to_rgb(h, s, v))

def cherry_pie():
    import glob, io, zipfile
    root = os.path.join(os.path.dirname(__file__), "..")
    jar = glob.glob(os.path.join(root, ".gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-clientOnly-*/*/*.jar"))[0]
    img = Image.open(io.BytesIO(zipfile.ZipFile(jar).read("assets/minecraft/textures/item/pumpkin_pie.png"))).convert("RGBA")
    px = img.load(); red = {c: cherry_shade(c) for c in PIE_FILLING}
    for x in range(img.width):
        for y in range(img.height):
            p = px[x, y]
            if p[3] and p[:3] in red: px[x, y] = red[p[:3]] + (p[3],)
    return img

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
    """The kernel: two lumpy, wrinkled lobes either side of a wandering groove, tilted and a little lopsided like a real one."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    lo, md, hi, dk = (146, 94, 50), (188, 134, 76), (224, 180, 116), (104, 64, 32)
    cx, cy, tilt = 7.6, 8.2, math.radians(-22)
    for x in range(16):
        for y in range(16):
            # rotate into the kernel's own frame
            u = (x - cx) * math.cos(tilt) - (y - cy) * math.sin(tilt)
            v = (x - cx) * math.sin(tilt) + (y - cy) * math.cos(tilt)
            ang = math.atan2(v, u)
            rad = 1 + .11 * math.sin(ang * 7 + 0.8) + .06 * math.sin(ang * 3)          # lumpy, uneven edge
            d = math.hypot(u / (6.4 if u > 0 else 5.6), v / 5.0) / rad                    # one lobe bigger than the other
            if d > 1: continue
            groove = 0.5 * math.sin(v * 0.9)                                              # the groove wanders
            if abs(u - groove) < .6: c = dk
            else:
                side = 1 if u > groove else -1
                lobe = ((u - side * (3.4 if side > 0 else 2.8)) / 2.6) ** 2 + ((v + side * 0.4) / 3.3) ** 2
                fold = math.sin(v * 2.3 + abs(u - groove) * .9 + (0.0 if side > 0 else 1.3))   # wrinkles out of step
                c = hi if lobe < .35 and fold > -.2 else md if fold > -.45 else lo
                if fold < -.85: c = dk
            px[x, y] = c + (255,)
    return outline(img, (86, 52, 26))

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for name, fn in (("cherries", cherries), ("cherry_pie", cherry_pie), ("black_walnut", walnut), ("shelled_walnut", shelled_walnut)):
        fn().save(f"{OUT}/{name}.png")
    print("ok")
