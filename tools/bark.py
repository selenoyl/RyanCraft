"""Bark for the fifteen trees, built on the classic 1.2.5 log textures (oak, spruce, birch, jungle) the way the planks
are built on the classic oak plank: each species picks the template whose pattern is closest to its real bark, and the
template's shading is gradient-mapped onto the species' own colours. A few species get a detail on top: sycamore's
pale patches, beech's smooth grey, black cherry's lenticels.
  python3 tools/bark.py    (writes textures/block/<species>_log.png; replaces the old procedural bark from tree_textures.py)"""
import os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
OUT = os.path.join(RES, "assets", "ryancraft", "textures", "block")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")

# species: (template, dark, mid, light). Mostly browns, as the real barks are; a few greys where the tree really is grey.
BARK = {
    "white_oak":         ("oak",    (86, 76, 64),   (148, 136, 118), (194, 184, 166)),   # pale, ashy, shallow plates
    "red_oak":           ("oak",    (56, 44, 34),   (102, 86, 70),   (150, 132, 112)),   # dark furrows, lighter ridges
    "sugar_maple":       ("spruce", (68, 56, 44),   (126, 110, 92),  (170, 156, 136)),   # grey-brown shaggy plates
    "red_maple":         ("oak",    (82, 78, 72),   (136, 130, 120), (178, 172, 162)),   # smooth-ish grey
    "tulip_poplar":      ("spruce", (90, 82, 68),   (150, 140, 120), (192, 184, 162)),   # light, neatly furrowed
    "shagbark_hickory":  ("spruce", (58, 50, 42),   (116, 104, 90),  (164, 152, 136)),   # long shaggy strips
    "black_walnut":      ("spruce", (32, 25, 21),   (68, 54, 44),    (104, 88, 74)),     # deep, near-black brown
    "american_beech":    ("birch",  (118, 116, 110), (160, 160, 156), (184, 184, 180)),  # smooth grey with faint marks
    "sycamore":          ("oak",    (94, 84, 68),   (144, 132, 110), (182, 170, 146)),   # brown, flaking to pale patches
    "eastern_redbud":    ("oak",    (50, 40, 34),   (90, 76, 66),    (128, 114, 100)),
    "flowering_dogwood": ("jungle", (56, 46, 40),   (98, 86, 76),    (138, 126, 114)),   # small square plates
    "eastern_red_cedar": ("spruce", (68, 36, 24),   (138, 78, 50),   (178, 118, 84)),    # red-brown, stringy
    "black_cherry":      ("jungle", (30, 22, 22),   (62, 46, 44),    (98, 78, 72)),      # dark plates, "burnt chips"
    "sassafras":         ("spruce", (64, 42, 30),   (120, 82, 56),   (164, 120, 88)),    # orange-brown furrows
    "ohio_buckeye":      ("oak",    (72, 68, 62),   (122, 116, 106), (164, 158, 146)),   # grey, scaly
}

def gradient(t, dark, mid, light):
    if t < 0.5: k = t / 0.5; a, b = dark, mid
    else: k = (t - 0.5) / 0.5; a, b = mid, light
    return tuple(int(a[i] + (b[i] - a[i]) * k) for i in range(3)) + (255,)

def mapped(template, dark, mid, light):
    src = Image.open(os.path.join(CLASSIC, f"{template}_log.png")).convert("RGBA").crop((0, 0, 16, 16))
    lum = [[sum(src.getpixel((x, y))[:3]) / 3 for x in range(16)] for y in range(16)]
    flat = sorted(v for row in lum for v in row); lo, hi = flat[8], flat[-8]           # ignore a few outliers
    out = Image.new("RGBA", (16, 16)); px = out.load()
    for y in range(16):
        for x in range(16):
            t = min(1, max(0, (lum[y][x] - lo) / max(1, hi - lo)))
            px[x, y] = gradient(t, dark, mid, light)
    return out

def sycamore_patches(img, seed=7):
    """Scattered pale patches where the bark has flaked off: irregular, different sizes, wrapping across the edges so
    they don't line up into a grid when the log repeats."""
    r = random.Random(seed); px = img.load()
    cream, olive = (222, 218, 196), (178, 176, 140)
    for cx, cy, rad, col in ((3, 2, 2.3, cream), (11, 6, 1.8, olive), (6, 11, 2.6, cream), (13, 13, 1.4, cream), (1, 8, 1.2, olive)):
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                if dx * dx * 0.8 + dy * dy * 1.3 <= rad * rad + r.random() * 1.2:
                    x, y = (cx + dx) % 16, (cy + dy) % 16
                    v = r.randint(-10, 8)
                    px[x, y] = tuple(max(0, min(255, c + v)) for c in col) + (255,)
    return img

def cherry_lenticels(img, seed=3):
    """Black cherry's short horizontal lenticels, a few pale dashes across the dark plates."""
    r = random.Random(seed); px = img.load()
    for _ in range(6):
        x, y = r.randrange(16), r.randrange(16)
        for k in range(r.randint(2, 3)): px[(x + k) % 16, y] = (128, 108, 98, 255)
    return img

if __name__ == "__main__":
    for sp, (tpl, dark, mid, light) in BARK.items():
        img = mapped(tpl, dark, mid, light)
        if sp == "sycamore": sycamore_patches(img)
        if sp == "black_cherry": cherry_lenticels(img)
        img.save(os.path.join(OUT, f"{sp}_log.png"))
    print("bark ok")
