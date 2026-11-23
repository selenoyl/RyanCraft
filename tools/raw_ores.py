"""Raw ore art for the new metals, each from its real ore mineral, so none is a copy of raw iron:
  silver  native silver: vanilla raw gold's shape and block, in bright silver           (a tasteful copy)
  lead    galena: cubic grey crystals, stacked                                            (original)
  tin     cassiterite: vanilla raw copper's shape and block, dark brown with glints       (a tasteful copy)
  zinc    sphalerite: an amber, resinous crystal chunk                                    (original)
  aluminum bauxite: a rust-red lump studded with pale pisoliths                           (original)
  uranium pitchblende: a black, bubbly lump with a yellow-green crust                     (original)
Ore spots use the same mineral colours, laid out from the classic iron and gold ores (flipped or turned per metal).
Imported by metals_assets.py."""
import math, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); TPL = os.path.join(HERE, "templates", "metals")

MINERAL = {   # dark -> light
    "silver":   [(96, 98, 108), (158, 162, 172), (212, 216, 226), (250, 252, 255)],
    "lead":     [(46, 50, 64), (88, 94, 112), (138, 144, 162), (198, 204, 220)],
    "tin":      [(40, 27, 21), (82, 55, 38), (132, 94, 62), (216, 190, 154)],
    "zinc":     [(74, 38, 14), (140, 80, 26), (204, 140, 52), (252, 216, 122)],
    "aluminum": [(92, 36, 24), (152, 68, 42), (200, 114, 78), (236, 204, 168)],
    "uranium":  [(16, 16, 14), (40, 40, 36), (72, 74, 64), (118, 122, 104)],
}
CRUST = [(110, 160, 40), (166, 214, 64), (214, 242, 120)]          # autunite-yellow-green on pitchblende
PISOLITH = [(170, 120, 80), (226, 196, 150), (246, 228, 196)]     # pale ooids in bauxite
# ore layout: (classic template, transform)
LAYOUT = {"silver": ("gold", None), "lead": ("iron", "flip"), "tin": ("iron", "rot90"), "zinc": ("gold", "rot90"),
          "aluminum": ("gold", "flip"), "uranium": ("iron", "rot180")}

def grad(t, pal):
    t = min(1.0, max(0.0, t)); i = min(len(pal) - 2, int(t * (len(pal) - 1))); f = t * (len(pal) - 1) - i
    return tuple(int(pal[i][k] + (pal[i + 1][k] - pal[i][k]) * f) for k in range(3))

def load(name): return Image.open(os.path.join(TPL, name)).convert("RGBA").crop((0, 0, 16, 16))

def remap(im, pal, floor=0.0, mask=None):
    """Gradient-map by brightness onto pal, starting at `floor` so the darkest pixels don't go black."""
    px = im.load(); pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3] and (mask is None or (x, y) in mask)]
    vals = [sum(px[x, y][:3]) for x, y in pts]; lo, hi = (min(vals), max(vals)) if vals else (0, 1)
    for x, y in pts:
        c = px[x, y]; px[x, y] = grad(floor + (1 - floor) * (sum(c[:3]) - lo) / max(1, hi - lo), pal) + (c[3],)
    return im

def transform(im, how):
    if how == "flip": return im.transpose(Image.FLIP_LEFT_RIGHT)
    if how == "rot90": return im.transpose(Image.ROTATE_90)
    if how == "rot180": return im.transpose(Image.ROTATE_180)
    return im

def ore(m, classic, deep):
    """The metal's ore: the template ore's spots (turned per metal), in mineral colours, on the plain stone."""
    kind, how = LAYOUT[m]
    cl = lambda n: Image.open(os.path.join(classic, "block", n)).convert("RGBA").crop((0, 0, 16, 16))
    if deep: stone, tpl = load("deepslate.png"), load(f"deepslate_{kind}_ore.png")
    else: stone, tpl = cl("stone.png"), cl(f"{kind}_ore.png")
    plain = stone
    mask_img = Image.new("L", (16, 16)); mp = mask_img.load()
    for y in range(16):
        for x in range(16):
            if abs(sum(plain.getpixel((x, y))[:3]) - sum(tpl.getpixel((x, y))[:3])) > 24: mp[x, y] = 255
    tpl, mask_img = transform(tpl, how), transform(mask_img, how)
    spots = {(x, y) for y in range(16) for x in range(16) if mask_img.getpixel((x, y))}
    spot = remap(tpl.copy(), MINERAL[m], 0.22, spots)
    out = stone.copy(); po, ps = out.load(), spot.load()
    for x, y in spots: po[x, y] = ps[x, y]
    if m == "uranium":                           # a fleck of green crust on the larger spots
        r = random.Random(7 + deep)
        for x, y in sorted(spots):
            if (x + 1, y) in spots and (x, y + 1) in spots and r.random() < 0.35: po[x, y] = CRUST[1] + (255,)
    return out

# ---------------------------------------------------------------- drawing helpers for the originals
def canvas(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

def outline(im, dark):
    """A 1-pixel dark rim around the sprite, like vanilla's raw items."""
    px = im.load(); edge = []
    for y in range(16):
        for x in range(16):
            if px[x, y][3]: continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] == 255 and px[x + dx, y + dy][:3] != dark
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))): edge.append((x, y))
    for x, y in edge: px[x, y] = dark + (255,)
    return im

def blob(cx, cy, rx, ry, seed, wob=0.18):
    r = random.Random(seed); ph = [r.random() * 6.28 for _ in range(3)]
    def inside(x, y):
        a = math.atan2(y - cy, x - cx); k = 1 + wob * sum(math.sin((i + 2) * a + ph[i]) / (i + 1) for i in range(3))
        return ((x - cx) / (rx * k)) ** 2 + ((y - cy) / (ry * k)) ** 2 <= 1
    return inside

def raw_lead():
    """Galena: three cubes, top faces bright, sides shaded."""
    im = canvas(); px = im.load(); P = MINERAL["lead"]
    def cube(x0, y0, s):
        h = s // 2
        for dy in range(h):                              # top: parallelogram leaning right
            for dx in range(s):
                x, y = x0 + dx + (h - 1 - dy), y0 + dy
                if 0 <= x < 16 and 0 <= y < 16: px[x, y] = (P[3] if dx == 0 or dy == 0 else grad(0.82 - 0.05 * dx / s, P)) + (255,)
        for dy in range(s):                              # front face
            for dx in range(s):
                x, y = x0 + dx, y0 + h + dy
                if 0 <= x < 16 and 0 <= y < 16: px[x, y] = grad(0.52 - 0.12 * dy / s + (0.08 if dx == 0 else 0), P) + (255,)
        for dy in range(s):                              # side face
            for dx in range(h):
                x, y = x0 + s + dx, y0 + h + dy - dx - 1
                if 0 <= x < 16 and 0 <= y < 16 and y >= y0 + h - dx - 1: px[x, y] = grad(0.28 - 0.06 * dy / s, P) + (255,)
    cube(1, 5, 6); cube(7, 7, 6); cube(4, 1, 5)
    return outline(im, (30, 32, 42))

def raw_zinc():
    """Sphalerite: a resinous amber chunk with flat facets and a bright glint."""
    im = canvas(); px = im.load(); P = MINERAL["zinc"]
    pts = [(3, 6), (7, 2), (12, 4), (14, 9), (10, 14), (4, 13), (2, 10)]
    def inside(x, y):
        n = len(pts); c = False; j = n - 1
        for i in range(n):
            xi, yi = pts[i]; xj, yj = pts[j]
            if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi) + xi: c = not c
            j = i
        return c
    tones = [0.9, 0.62, 0.4, 0.26, 0.34, 0.5, 0.74]        # one flat tone per facet, lit from the upper left
    cx, cy = 8.0, 8.4
    for y in range(16):
        for x in range(16):
            if not inside(x + 0.5, y + 0.5): continue
            a = math.atan2(y + 0.5 - cy, x + 0.5 - cx)
            angs = [math.atan2(py - cy, px_ - cx) for px_, py in pts]
            k = max(range(len(pts)), key=lambda i: math.cos(a - (angs[i] + angs[(i + 1) % len(pts)]) / 2
                                                         if abs(angs[i] - angs[(i + 1) % len(pts)]) < math.pi else a - (angs[i] + angs[(i + 1) % len(pts)]) / 2 - math.pi))
            px[x, y] = grad(tones[k], P) + (255,)
    for x, y in ((6, 5), (7, 5), (6, 6)): px[x, y] = P[3] + (255,)
    px[11, 6] = grad(0.9, P) + (255,)
    return outline(im, (48, 24, 8))

def raw_aluminum():
    """Bauxite: a rounded rust-red lump with pale pisoliths."""
    im = canvas(); px = im.load(); P = MINERAL["aluminum"]; r = random.Random(31)
    a, b = blob(7.5, 8.5, 6.2, 5.0, 3), blob(10.5, 5.5, 3.6, 3.0, 4)
    for y in range(16):
        for x in range(16):
            if a(x, y) or b(x, y):
                t = 0.62 - 0.05 * (y - 4) + 0.04 * (8 - x) / 4 + (r.random() - 0.5) * 0.14
                px[x, y] = grad(t, P) + (255,)
    for cx, cy in ((4, 8), (8, 6), (11, 10), (7, 11), (11, 4)):
        if px[cx, cy][3] and px[cx + 1, cy + 1][3]:
            px[cx, cy] = PISOLITH[2] + (255,); px[cx + 1, cy] = PISOLITH[1] + (255,); px[cx, cy + 1] = PISOLITH[1] + (255,); px[cx + 1, cy + 1] = PISOLITH[0] + (255,)
    return outline(im, (56, 22, 14))

def raw_uranium():
    """Pitchblende: black, bubbly (botryoidal) lump, crusted yellow-green in the hollows."""
    im = canvas(); px = im.load(); P = MINERAL["uranium"]
    bubbles = [(6, 9, 4.2), (10.5, 8, 3.6), (8, 5, 3.2), (11, 12, 2.6), (4.5, 12.5, 2.4)]
    for y in range(16):
        for x in range(16):
            best = None
            for cx, cy, rr in bubbles:
                d = math.hypot(x - cx, y - cy) / rr
                if d <= 1 and (best is None or d < best[0]): best = (d, cx, cy, rr)
            if best:
                d, cx, cy, rr = best
                hl = max(0, 1 - math.hypot(x - (cx - rr * 0.35), y - (cy - rr * 0.35)) / (rr * 0.6))
                px[x, y] = grad(0.25 + 0.75 * hl * hl - 0.15 * d, P) + (255,)
    for x, y in ((8, 8), (9, 9), (5, 10), (12, 10), (7, 12), (10, 5)):        # crust where bubbles meet
        if px[x, y][3]: px[x, y] = CRUST[1] + (255,)
    for x, y in ((9, 8), (6, 11)):
        if px[x, y][3]: px[x, y] = CRUST[2] + (255,)
    return outline(im, (8, 8, 6))

# ---------------------------------------------------------------- raw blocks (originals)
def galena_block():
    """A jumble of galena cubes of different sizes, overlapping, each lit on its top and left edges."""
    im = Image.new("RGBA", (16, 16)); px = im.load(); P = MINERAL["lead"]; r = random.Random(41)
    for y in range(16):
        for x in range(16): px[x, y] = grad(0.12 + r.random() * 0.08, P) + (255,)
    for _ in range(16):
        x0, y0, n = r.randrange(16), r.randrange(16), r.choice((3, 4, 4, 5, 6)); base = 0.38 + r.random() * 0.25
        for y in range(n):
            for x in range(n):
                t = base + (0.32 if y == 0 else 0.2 if x == 0 else 0) - (0.22 if (x == n - 1 or y == n - 1) else 0)
                px[(x0 + x) % 16, (y0 + y) % 16] = grad(t, P) + (255,)
    return im

def sphalerite_block():
    im = Image.new("RGBA", (16, 16)); px = im.load(); P = MINERAL["zinc"]; r = random.Random(42)
    seeds = [(r.uniform(0, 16), r.uniform(0, 16), r.uniform(0.3, 0.75)) for _ in range(9)]
    for y in range(16):
        for x in range(16):
            ds = sorted(((min(abs(x - sx), 16 - abs(x - sx)) ** 2 + min(abs(y - sy), 16 - abs(y - sy)) ** 2) ** 0.5, t, sx, sy) for sx, sy, t in seeds)
            d0, t, sx, sy = ds[0]
            seam = ds[1][0] - d0 < 0.9
            shade = t + 0.18 * ((sx - x) + (sy - y)) / 8
            px[x, y] = grad(0.08 if seam else shade, P) + (255,)
    for sx, sy, t in seeds: px[int(sx) % 16, int(sy) % 16] = P[3] + (255,)
    return im

def bauxite_block():
    im = Image.new("RGBA", (16, 16)); px = im.load(); P = MINERAL["aluminum"]; r = random.Random(43)
    for y in range(16):
        for x in range(16): px[x, y] = grad(0.45 + (r.random() - 0.5) * 0.3, P) + (255,)
    for cx, cy in ((2, 2), (9, 1), (13, 6), (5, 7), (10, 10), (2, 12), (14, 13), (7, 14)):
        for dx, dy, c in ((0, 0, 2), (1, 0, 1), (0, 1, 1), (1, 1, 0)): px[(cx + dx) % 16, (cy + dy) % 16] = PISOLITH[c] + (255,)
        px[(cx + 2) % 16, (cy + 1) % 16] = P[0] + (255,); px[(cx + 1) % 16, (cy + 2) % 16] = P[0] + (255,)
    return im

def pitchblende_block():
    im = Image.new("RGBA", (16, 16)); px = im.load(); P = MINERAL["uranium"]; r = random.Random(44)
    bubbles = [(r.uniform(0, 16), r.uniform(0, 16), r.uniform(2.6, 4.0)) for _ in range(18)]
    for y in range(16):
        for x in range(16):
            best = None
            for cx, cy, rr in bubbles:
                dx, dy = min(abs(x - cx), 16 - abs(x - cx)), min(abs(y - cy), 16 - abs(y - cy))
                d = math.hypot(dx, dy) / rr
                if d <= 1 and (best is None or d < best[0]): best = (d, rr, x - cx, y - cy)
            if best is None: px[x, y] = (P[1] if r.random() < 0.7 else CRUST[0]) + (255,); continue
            d, rr, ox, oy = best
            hl = max(0, 1 - d - 0.25 * (ox + oy) / rr)
            px[x, y] = grad(0.15 + 0.7 * hl, P) + (255,)
    return im

def raw_item(m):
    if m == "silver": return remap(load("raw_gold.png"), MINERAL["silver"], 0.05)
    if m == "tin":
        im = remap(load("raw_copper.png"), MINERAL["tin"], 0.0); px = im.load()
        for x, y in ((6, 5), (10, 8), (5, 10)):                 # adamantine glints
            if px[x, y][3]: px[x, y] = MINERAL["tin"][3] + (255,)
        return im
    return {"lead": raw_lead, "zinc": raw_zinc, "aluminum": raw_aluminum, "uranium": raw_uranium}[m]()

def raw_block(m):
    if m == "silver": return remap(load("raw_gold_block.png"), MINERAL["silver"], 0.05)
    if m == "tin":
        im = remap(load("raw_copper_block.png"), MINERAL["tin"], 0.0); px = im.load(); r = random.Random(45)
        for _ in range(6): px[r.randrange(16), r.randrange(16)] = MINERAL["tin"][3] + (255,)
        return im
    return {"lead": galena_block, "zinc": sphalerite_block, "aluminum": bauxite_block, "uranium": pitchblende_block}[m]()
