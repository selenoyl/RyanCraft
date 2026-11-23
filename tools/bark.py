"""Bark for the fifteen trees, built on the classic 1.2.5 log textures (oak, spruce, birch, jungle) the way the planks
are built on the classic oak plank: the template's grain is gradient-mapped onto the species' colours, then each
species' own bark is drawn over it: hickory's peeling strips, walnut and poplar's diamond furrows, dogwood's small
square plates, red oak's long pale ridges, sycamore's patches, cherry's lenticels and so on.
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
    "sky_willow":        ("birch",  (150, 156, 170), (198, 204, 216), (228, 232, 240)),  # silver, faintly blue
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
            t = 0.5 + (t - 0.5) * 0.5                                       # quieter grain: the species pattern carries the look
            px[x, y] = gradient(t, dark, mid, light)
    return out

# ---------------------------------------------------------------------------------------------------- patterns
# Each pattern returns a 16x16 grid of marks: "D" a dark furrow or gap, "L" a raised, lighter ridge or plate edge,
# "A" the species' accent colour (inner bark, lichen, a pale patch), "." leave the template's grain. All of them wrap
# at the edges, so the log tiles seamlessly up the trunk and around it.
def grid(): return [["."] * 16 for _ in range(16)]

def furrows(r, cols, wobble=1, light_side=True):
    """Vertical furrows that wander, with a lit ridge beside each (red oak's long flat ridges, cedar's fibres)."""
    g = grid()
    for x0 in cols:
        # wander out and back so the furrow ends where it started: the log tiles up the trunk with no seam
        steps = [r.choice((-1, 1)) if r.random() < 0.3 * wobble else 0 for _ in range(8)]
        steps = steps + [-v for v in reversed(steps)]
        x = x0
        for y in range(16):
            x += steps[y - 1] if y else 0
            g[y][x % 16] = "D"
            if light_side: g[y][(x + 1) % 16] = "L"
    return g

def diamonds(period=8, slope=2, light=True):
    assert (16 // slope) % period == 0 and 16 % slope == 0, "diamond net must tile vertically"
    """Interlacing furrows that cross in a net of long diamonds (tulip poplar, black walnut, sassafras)."""
    g = grid()
    for y in range(16):
        for x in range(16):
            a, b = (x + y // slope) % period, (x - y // slope) % period
            if a == 0 or b == 0: g[y][x] = "D"
            elif light and (a == 1 or b == period - 1): g[y][x] = "L"
    return g

def braided(r, n, seg=(3, 5), wide=0.0, ridge=0.7, broken=0.0, tight=False):
    """Interlacing furrows. Neighbours swing toward each other and apart in opposite phase, so between them the bark
    rises into long lens-shaped ridges that meet and part like a stretched net (no neat lattice). Each furrow steps
    sideways only every other row, runs out and back by the same amount, and so returns to where it started (no seam).
    {@code wide}: chance a furrow row is doubled. {@code ridge}: chance of a lit pixel on a ridge's crest.
    {@code broken}: chance a furrow pixel is skipped (rougher bark)."""
    g = grid(); spacing = 16 // n
    a = r.randint(*seg); b = 8 - a
    for i in range(n):
        runs = [(a, 1), (a, -1), (b, 1), (b, -1)] if b else [(a, 1), (a, -1)]
        moves = [d if k % 2 == 0 else 0 for length, d in runs for k in range(length)]
        # neighbours mirror each other (below): they meet. "tight" (walnut) also shifts every other furrow half a turn,
        # which packs the net into small diamonds in dark vertical bands
        phase = ((8 if i % 2 else 0) if tight else 0) + r.randrange(2)
        moves = moves[phase:] + moves[:phase]
        if i % 2: moves = [-m for m in moves]
        x = i * spacing
        for y in range(16):
            if r.random() >= broken:
                g[y][x % 16] = "D"
                if r.random() < wide: g[y][(x + (1 if tight else (moves[y] or (1 if i % 2 == 0 else -1)))) % 16] = "D"
            x += moves[y]
    for y in range(16):                                                 # crests: halfway between furrows, lit
        row = [x for x in range(16) if g[y][x] == "D"]
        for x in range(16):
            if g[y][x] == "." and g[y][(x - 1) % 16] == "D" and r.random() < ridge: g[y][x] = "L"
    return g

def plates(r, w, h, jitter=1, edge="L"):
    """Rows of plates with dark joints, each row offset (dogwood's small squares, buckeye and white oak's scales)."""
    g = grid()
    for row in range(16 // h):
        off = r.randrange(w)
        for y in range(row * h, row * h + h):
            for x in range(16):
                if y == row * h: g[y][x] = "D"
                elif (x + off) % w == 0: g[y][x] = "D"
                elif y == row * h + 1 and edge: g[y][x] = edge
        for _ in range(jitter):                                          # a few broken joints so it's not a brick wall
            g[row * h][r.randrange(16)] = "."
    return g

def scales(r, h=4):
    """Irregular scaly plates: rows of plates of varying width, each with a lit top edge (white oak, buckeye)."""
    g = grid()
    for row in range(16 // h):
        x = r.randrange(16); joints = []
        while len(joints) < 6:
            joints.append(x % 16); x += r.choice((2, 3, 4, 5))
            if x - joints[0] >= 16: break
        for y in range(row * h, row * h + h):
            for x in range(16):
                if y == row * h and r.random() < 0.55: g[y][x] = "D"
                elif x in joints and y < row * h + h - (1 if r.random() < 0.4 else 0): g[y][x] = "D"
                elif y == row * h + 1: g[y][x] = "L"
    return g

def strips(r, cols, peel=4):
    """Long loose strips with dark gaps between, their ends curling away pale (shagbark hickory)."""
    g = grid()
    for x0 in cols:
        for y in range(16): g[y][x0 % 16] = "D"
        top = r.randrange(16)
        for k in range(peel):                                            # a strip end lifting off the trunk
            g[(top + k) % 16][(x0 + 1) % 16] = "L"; g[(top + k) % 16][(x0 + 2) % 16] = "L" if k < peel - 1 else "."
    return g

def flecks(r, n, mark, length=(1, 1), horizontal=False):
    g = grid()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16)
        for k in range(r.randint(*length)):
            if horizontal: g[y][(x + k) % 16] = mark
            else: g[(y + k) % 16][x] = mark
    return g

def blotches(r, spots):
    """Irregular patches that wrap (sycamore flaking to cream and olive)."""
    g = grid()
    for cx, cy, rad, mark in spots:
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                if dx * dx * 0.8 + dy * dy * 1.3 <= rad * rad + r.random() * 1.2: g[(cy + dy) % 16][(cx + dx) % 16] = mark
    return g

def merge(*gs):
    out = grid()
    for g in gs:
        for y in range(16):
            for x in range(16):
                if g[y][x] != ".": out[y][x] = g[y][x]
    return out

def apply(img, g, dark, light, accent=None, accent2=None):
    px = img.load()
    for y in range(16):
        for x in range(16):
            m = g[y][x]; c = px[x, y]
            if m == "D": px[x, y] = tuple(int(v * 0.25 + d * 0.75) for v, d in zip(c[:3], dark)) + (255,)
            elif m == "L": px[x, y] = tuple(min(255, int(v * 0.4 + l * 0.6 + 4)) for v, l in zip(c[:3], light)) + (255,)
            elif m == "A" and accent: px[x, y] = accent + (255,)
            elif m == "B" and accent2: px[x, y] = accent2 + (255,)
    return img

def pattern(sp, r):
    """Each species' own bark, drawn on the template grain."""
    if sp == "white_oak": return scales(r, 4)                                                    # pale, shallow scales
    if sp == "red_oak": return furrows(r, (1, 6, 11), wobble=0.5)                               # long flat pale ridges
    if sp == "sugar_maple": return merge(furrows(r, (1, 6, 11), wobble=0.9), flecks(r, 6, "D", (2, 4)), flecks(r, 5, "L", (2, 3)),
                                         flecks(r, 3, "D", (1, 2), horizontal=True))           # long shaggy plates, ragged breaks
    if sp == "red_maple": return merge(flecks(r, 5, "D", (3, 6)), flecks(r, 6, "L", (2, 4)))     # smooth, a few cracks
    if sp == "tulip_poplar": return braided(r, 4, seg=(2, 6), wide=0.0, ridge=0.85, broken=0.08, tight=True)   # a loose net, some furrows broken
    if sp == "shagbark_hickory": return strips(r, (0, 5, 9, 13), peel=5)
    if sp == "black_walnut": return braided(r, 4, seg=(2, 6), wide=0.35, ridge=0.75, broken=0.05, tight=True)   # dark bands, small tight diamonds
    if sp == "american_beech": return merge(flecks(r, 4, "L", (2, 3), horizontal=True), flecks(r, 3, "D", (2, 2), horizontal=True))
    if sp == "sycamore": return blotches(r, ((3, 2, 2.3, "A"), (11, 6, 1.8, "B"), (6, 11, 2.6, "A"), (13, 13, 1.4, "A"), (1, 8, 1.2, "B")))
    if sp == "eastern_redbud": return merge(plates(r, 3, 3, jitter=4, edge=None), flecks(r, 7, "A"))   # fine scales, orange in the cracks
    if sp == "flowering_dogwood": return merge(scales(r, 3), flecks(r, 5, "D"), flecks(r, 4, "L"))   # small uneven plates, broken joints
    if sp == "eastern_red_cedar": return merge(furrows(r, (0, 3, 7, 10, 13), wobble=0.3), flecks(r, 5, "A", (3, 6)))  # stringy, grey weathering
    if sp == "black_cherry": return merge(plates(r, 6, 4, jitter=3, edge=None), flecks(r, 7, "A", (2, 3), horizontal=True))  # chips, lenticels
    if sp == "sassafras": return merge(furrows(r, (1, 5, 9, 13), wobble=1.2), flecks(r, 4, "D", (2, 4)))   # deep, wandering furrows
    if sp == "ohio_buckeye": return scales(r, 3)
    if sp == "sky_willow": return merge(flecks(r, 5, "D", (2, 3), horizontal=True), flecks(r, 4, "L", (2, 4), horizontal=True))   # smooth, birch-like marks
    return grid()

NOISY = {"sugar_maple": 0.35, "flowering_dogwood": 0.35}          # extra pixel noise, so the grain isn't too regular

def noise(img, amount, r):
    px = img.load()
    for y in range(16):
        for x in range(16):
            if r.random() < amount:
                k = r.uniform(0.84, 1.16); c = px[x, y]; px[x, y] = tuple(min(255, int(v * k)) for v in c[:3]) + (255,)
    return img

ACCENT = {"sycamore": ((222, 218, 196), (178, 176, 140)), "eastern_redbud": ((176, 96, 52), None),
          "eastern_red_cedar": ((150, 140, 128), None), "black_cherry": ((132, 112, 100), None)}

if __name__ == "__main__":
    for sp, (tpl, dark, mid, light) in BARK.items():
        img = mapped(tpl, dark, mid, light)
        a = ACCENT.get(sp, (None, None))
        apply(img, pattern(sp, random.Random(sp)), dark, light, a[0], a[1])
        if sp in NOISY: noise(img, NOISY[sp], random.Random(sp + "n"))
        img.save(os.path.join(OUT, f"{sp}_log.png"))
    print("bark ok")
