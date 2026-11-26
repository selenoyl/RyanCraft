"""
Texture studies for a new abyssal stone (three directions) and abyssal ores (the classic ores' mineral pixels lifted off
classic stone and set into each abyssal stone, so they read as the same ores in a new rock).
  python3 tools/abyss_texture_concept.py  ->  docs/abyss_texture_concept.png
"""
import os, sys, math, random
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
CB = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")
OURS = os.path.join(RES, "assets", "ryancraft", "textures", "block")

def clamp(c): return tuple(max(0, min(255, int(v))) for v in c)
def grad(t, stops):
    t = max(0, min(1, t)); k = t * (len(stops) - 1); i = min(len(stops) - 2, int(k)); f = k - i
    return clamp(tuple(stops[i][j] + (stops[i + 1][j] - stops[i][j]) * f for j in range(3)))

def strata():
    """A: abyssal slate, dark blue-grey in wavering horizontal layers, with fine cracks between them."""
    r = random.Random(1); im = Image.new("RGBA", (16, 16)); px = im.load()
    stops = [(22, 26, 40), (38, 44, 62), (56, 64, 86), (78, 88, 112)]
    for y in range(16):
        for x in range(16):
            layer = (y + int(1.2 * math.sin(x * 0.7 + y * 0.3))) % 5
            t = [0.75, 0.55, 0.6, 0.4, 0.15][layer] + r.uniform(-0.08, 0.08)
            px[x, y] = grad(t, stops) + (255,)
    return im

def columns():
    """B: columnar abyssal basalt, deep indigo cut into upright prisms with lit edges."""
    r = random.Random(2); im = Image.new("RGBA", (16, 16)); px = im.load()
    stops = [(18, 18, 36), (34, 34, 60), (52, 54, 84), (74, 78, 112)]
    edges = [0, 4, 9, 13]
    for y in range(16):
        for x in range(16):
            t = 0.45 + r.uniform(-0.1, 0.1)
            if x in edges: t = 0.08
            elif x - 1 in edges: t = 0.82
            if (y + x // 4 * 5) % 11 == 0: t = 0.12                     # the joints across each column
            px[x, y] = grad(t, stops) + (255,)
    return im

def glimmer():
    """C: glimmerstone, near-black blue rock flecked with faintly shining pale-blue mineral, as if lit from within."""
    r = random.Random(3); im = Image.new("RGBA", (16, 16)); px = im.load()
    stops = [(16, 18, 30), (30, 34, 52), (46, 52, 74), (64, 72, 98)]
    src = Image.open(os.path.join(CB, "stone.png")).convert("RGB").crop((0, 0, 16, 16))
    lum = [[sum(src.getpixel((x, y))) / 3 for x in range(16)] for y in range(16)]
    lo, hi = min(min(l) for l in lum), max(max(l) for l in lum)
    for y in range(16):
        for x in range(16): px[x, y] = grad((lum[y][x] - lo) / (hi - lo) * 0.8, stops) + (255,)
    for _ in range(7):
        x, y = r.randrange(16), r.randrange(16)
        px[x, y] = (150, 196, 232, 255)
        if r.random() < 0.5: px[(x + 1) % 16, y] = (96, 140, 182, 255)
    return im

STONES = [("A. Abyssal slate", "layered, like deepslate's deeper cousin", strata), ("B. Abyssal basalt", "upright prisms, deep indigo", columns),
          ("C. Glimmerstone", "near-black, flecked with faint shining blue", glimmer)]
ORES = [("coal", CB), ("iron", CB), ("gold", CB), ("redstone", CB), ("lapis", CB), ("diamond", CB), ("silver", OURS), ("lead", OURS)]

def ore_on(stone, ore, folder):
    """The ore's mineral: its pixels that differ clearly from plain classic stone, set onto the new stone."""
    base = Image.open(os.path.join(CB, "stone.png")).convert("RGB").crop((0, 0, 16, 16))
    o = Image.open(os.path.join(folder, f"{ore}_ore.png")).convert("RGB").crop((0, 0, 16, 16))
    out = stone.copy(); px = out.load()
    for y in range(16):
        for x in range(16):
            a, b = o.getpixel((x, y)), base.getpixel((x, y))
            if sum(abs(a[i] - b[i]) for i in range(3)) > 60 or max(a) - min(a) > 40: px[x, y] = a + (255,)
    return out

def sheet(out):
    BG, INK, SUB, CELL = (243, 239, 230), (46, 42, 36), (128, 118, 104), (196, 190, 178)
    S, G = 96, 18
    W = 40 + 210 + 9 * (S + G) + 20; rowh = S + 70; H = 170 + len(STONES) * rowh + 30
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Abyssal stone and ores: texture studies", font=R.font(44, True), fill=INK)
    d.text((42, 96), "Three directions for the stone, each tiled 3x3, with the abyssal ores (the classic ores' mineral set into that stone).", font=R.font(19), fill=SUB)
    y = 160
    for name, sub, fn in STONES:
        st = fn()
        tile = Image.new("RGBA", (48, 48))
        for i in range(3):
            for j in range(3): tile.paste(st, (i * 16, j * 16))
        d.text((40, y), name, font=R.font(22, True), fill=INK); d.text((40, y + 28), sub, font=R.font(14), fill=SUB)
        big = tile.resize((S + 40, S + 40), Image.NEAREST); c.paste(big, (40 + 20, y + 52 - 10), big) if False else c.paste(tile.resize((S, S), Image.NEAREST), (40, y + 50))
        for k, (ore, folder) in enumerate(ORES):
            x = 250 + k * (S + G)
            im = ore_on(st, ore, folder).resize((S, S), Image.NEAREST); c.paste(im, (x, y + 30))
            d.text((x + S // 2, y + 36 + S), ore, font=R.font(15, True), fill=INK, anchor="mt")
        y += rowh + 20
    c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(HERE, "..", "docs", "abyss_texture_concept.png"))
