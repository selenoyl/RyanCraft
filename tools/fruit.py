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

# Cherry pie: the game's own pumpkin pie with the filling recoloured cherry red, done at load time by a
# paletted_permutations atlas source (assets/minecraft/atlases/items.json). We ship only the two palette strips:
# the pumpkin pie's filling shades (the key) and the red they become. Crust and rim pass through untouched.
PIE_FILLING = [(234, 158, 96), (216, 139, 80), (238, 172, 110), (241, 184, 123), (183, 92, 18), (164, 84, 19), (219, 116, 34),
               # the Programmer Art pie (used by the classic setup) has its own filling shades
               (225, 136, 83), (198, 88, 34), (213, 98, 41), (219, 120, 71), (213, 109, 57), (198, 98, 47), (209, 114, 64),
               (226, 129, 81), (219, 106, 51), (231, 140, 94), (237, 147, 102), (209, 101, 45), (203, 90, 34), (186, 77, 23)]

def cherry_shade(c):
    import colorsys
    h, s, v = colorsys.rgb_to_hsv(*[x / 255 for x in c])
    h = (h - 0.085) % 1.0; s = min(0.9, s * 1.35); v *= 0.9
    return tuple(round(x * 255) for x in colorsys.hsv_to_rgb(h, s, v))

def pie_palettes():
    root = os.path.join(os.path.dirname(__file__), "../src/main/resources/assets")
    os.makedirs(f"{root}/ryancraft/textures/palettes", exist_ok=True)
    for name, cols in (("pie_filling", PIE_FILLING), ("pie_cherry", [cherry_shade(c) for c in PIE_FILLING])):
        img = Image.new("RGBA", (len(cols), 1))
        for i, c in enumerate(cols): img.putpixel((i, 0), c + (255,))
        img.save(f"{root}/ryancraft/textures/palettes/{name}.png")
    os.makedirs(f"{root}/minecraft/atlases", exist_ok=True)
    with open(f"{root}/minecraft/atlases/items.json", "w") as f:
        json.dump({"sources": [{"type": "minecraft:paletted_permutations", "textures": ["minecraft:item/pumpkin_pie"],
                                "palette_key": "ryancraft:palettes/pie_filling",
                                "permutations": {"cherry": "ryancraft:palettes/pie_cherry"}}]}, f, indent=2)

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
    for name, fn in (("cherries", cherries), ("black_walnut", walnut), ("shelled_walnut", shelled_walnut)):
        fn().save(f"{OUT}/{name}.png")
    pie_palettes()
    print("ok")
