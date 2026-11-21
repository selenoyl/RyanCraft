"""
Eastern US tree designs: voxel sketches of each species' silhouette, rendered as an isometric sheet.
  python3 tools/trees.py [out.png]
These are design previews (shapes, sizes, colours) to agree on before the trees become world features.
"""
import math, os, random, sys
from PIL import Image, ImageDraw, ImageFont

def hexc(h): h = h.lstrip('#'); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

class Tree:
    def __init__(self, seed): self.v = {}; self.r = random.Random(seed)
    def put(self, x, y, z, kind, force=False):
        k = (round(x), round(y), round(z))
        if force or k not in self.v or (self.v[k] in ("leaf", "bloom") and kind in ("log", "log2")): self.v[k] = kind
    def trunk(self, h, w=1, y0=0, kind="log"):
        for y in range(y0, y0 + h):
            for dx in range(w):
                for dz in range(w): self.put(dx - (w - 1) // 2, y, dz - (w - 1) // 2, kind, True)
    def line(self, a, b, kind="log"):
        n = int(max(abs(b[i] - a[i]) for i in range(3))) * 2 + 1
        for i in range(n + 1):
            t = i / n; self.put(*(a[j] + (b[j] - a[j]) * t for j in range(3)), kind)
    def blob(self, c, rx, ry, rz, density=0.92, kind="leaf", flat_bottom=False):
        cx, cy, cz = c
        for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
            for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
                for z in range(int(cz - rz) - 1, int(cz + rz) + 2):
                    d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 + ((z - cz) / rz) ** 2
                    if flat_bottom and y < cy - ry * 0.3: continue
                    edge = d > 0.7
                    if d <= 1.0 and (not edge or self.r.random() < density): self.put(x, y, z, kind)
    def limb(self, base, angle, rise, length, kind="log"):
        end = (base[0] + math.cos(angle) * length, base[1] + rise, base[2] + math.sin(angle) * length)
        self.line(base, end, kind); return end

# ---------------------------------------------------------------- species
def white_oak(s):        # massive, wide-spreading, low horizontal limbs
    t = Tree(s); t.trunk(6, 2)
    for k in range(6):
        a = k * math.tau / 6 + t.r.uniform(-.3, .3)
        e = t.limb((0, 5 + t.r.random(), 0), a, t.r.uniform(2, 4), t.r.uniform(6, 8))
        t.blob(e, 3.6, 2.6, 3.6)
    t.blob((0, 11, 0), 5, 3.2, 5)
    return t

def red_oak(s):          # straighter trunk, limbs angled up, rounded crown
    t = Tree(s); t.trunk(9)
    for k in range(5):
        a = k * math.tau / 5 + t.r.uniform(-.3, .3)
        e = t.limb((0, 6 + k % 2, 0), a, t.r.uniform(4, 6), t.r.uniform(3.5, 5))
        t.blob(e, 3, 2.6, 3)
    t.blob((0, 13, 0), 5.2, 4.2, 5.2)
    return t

def sugar_maple(s):      # big, dense, full egg-shaped crown that comes down low
    t = Tree(s); t.trunk(7)
    for k in range(5):
        a = k * math.tau / 5 + .4
        e = t.limb((0, 4, 0), a, 3, 4)
        t.blob(e, 3, 2.6, 3, density=.97)
    t.blob((0, 11, 0), 6, 6.5, 6, density=.98)
    return t

def red_maple(s):        # slimmer, upright oval with a few limbs showing
    t = Tree(s); t.trunk(9)
    for k in range(3):
        a = k * math.tau / 3 + .2; t.limb((0, 4, 0), a, 4, 2)
    t.blob((0, 11, 0), 3.4, 5.5, 3.4, density=.93)
    return t

def tulip_poplar(s):     # very tall, straight, narrow crown high up (Kentucky's state tree)
    t = Tree(s); t.trunk(22)
    for y in range(13, 21, 2):
        a = t.r.uniform(0, math.tau); t.limb((0, y, 0), a, 1.5, 2.5)
    t.blob((0, 20, 0), 4, 7, 4, density=.94)
    return t

def shagbark_hickory(s): # tall, narrow, irregular clumps
    t = Tree(s); t.trunk(15)
    for y in range(9, 17, 2):
        a = t.r.uniform(0, math.tau); e = t.limb((0, y, 0), a, 2, t.r.uniform(2, 3.5))
        t.blob(e, 2.6, 2.2, 2.6, density=.85)
    t.blob((0, 17, 0), 3, 3, 3)
    return t

def black_walnut(s):     # open, airy rounded crown on long arching limbs
    t = Tree(s); t.trunk(9)
    for k in range(5):
        a = k * math.tau / 5 + t.r.uniform(-.3, .3)
        e = t.limb((0, 7, 0), a, t.r.uniform(3, 5), t.r.uniform(5, 6.5))
        t.blob(e, 3, 2.2, 3, density=.6)
    t.blob((0, 13, 0), 4, 3, 4, density=.6)
    return t

def american_beech(s):   # short trunk, low, wide, dense dome
    t = Tree(s); t.trunk(5)
    for k in range(6):
        a = k * math.tau / 6; t.limb((0, 3, 0), a, 2, 4)
    t.blob((0, 8, 0), 6.5, 5, 6.5, density=.97, flat_bottom=False)
    return t

def sycamore(s):         # huge, open, white upper limbs over mottled bark
    t = Tree(s); t.trunk(7, 2)
    for k in range(5):
        a = k * math.tau / 5 + t.r.uniform(-.4, .4)
        mid = t.limb((0, 6, 0), a, 4, 4)
        e = t.limb(mid, a + t.r.uniform(-.5, .5), t.r.uniform(3, 5), t.r.uniform(4, 6), "log2")
        t.blob(e, 3.2, 2.6, 3.2, density=.7)
    t.blob((0, 17, 0), 4, 3, 4, density=.7)
    return t

def eastern_redbud(s):   # small vase: forks low into several leaning stems, wide flat crown of pink blossom
    t = Tree(s); t.trunk(3)
    for k in range(4):
        a = k * math.tau / 4 + .3; e = t.limb((0, 3, 0), a, 3, 3)
        t.blob((e[0], e[1] + 1, e[2]), 2.6, 1.4, 2.6, kind="bloom", density=.8)
    t.blob((0, 7.5, 0), 3, 1.4, 3, kind="bloom", density=.8)
    return t

def flowering_dogwood(s):  # small, flat, spreading tiers of white blossom with clear air between, on a visible trunk
    t = Tree(s); t.trunk(11)
    for y, r in ((3, 4.8), (7, 3.8), (11, 2.6)):
        for k in range(5):
            a = k * math.tau / 5 + y; t.limb((0, y, 0), a, 0, r - 1.4)
        t.blob((0, y + 1, 0), r, 0.55, r, kind="bloom", density=.9)          # one flat layer of blossom per tier
    return t

def eastern_red_cedar(s):  # dense dark column
    t = Tree(s); t.trunk(3)
    for y in range(2, 14):
        r = 2.4 - (y - 2) * 0.16
        t.blob((0, y, 0), max(.8, r), 0.7, max(.8, r), density=.97)
    t.put(0, 14, 0, "leaf"); t.put(0, 15, 0, "leaf")
    return t

def black_cherry(s):     # tall dark trunk, small irregular crown pushed to the top
    t = Tree(s); t.trunk(13)
    for k in range(3):
        a = k * math.tau / 3 + .5; e = t.limb((0, 10, 0), a, 2.5, 3.5)
        t.blob(e, 2.4, 2, 2.4, density=.8)
    t.blob((0, 15, 0), 3, 2.6, 3, density=.85)
    return t

def sassafras(s):        # small-medium, layered irregular clumps
    t = Tree(s); t.trunk(9)
    for y in (4, 6, 8):
        for k in range(2):
            a = t.r.uniform(0, math.tau); e = t.limb((0, y, 0), a, 1, t.r.uniform(2, 3.5))
            t.blob(e, 2.2, 1.2, 2.2, density=.85)
    t.blob((0, 10, 0), 2.6, 2, 2.6)
    return t

def ohio_buckeye(s):     # short and stout: low, wide, flat-bottomed rounded crown
    t = Tree(s); t.trunk(4)
    for k in range(4):
        a = k * math.tau / 4 + .8; t.limb((0, 3, 0), a, 2, 3)
    t.blob((0, 7.5, 0), 5.5, 3.6, 5.5, density=.96)
    return t

SPECIES = [  # name, builder, bark, bark2 (upper limbs), leaf, bloom, note
    ("White Oak", white_oak, "#9a958a", None, "#5f8f3e", None, "massive, wide-spreading"),
    ("Red Oak", red_oak, "#5a5048", None, "#3f7a2e", None, "rounded crown"),
    ("Sugar Maple", sugar_maple, "#7a7066", None, "#3e7a34", None, "dense; orange-red in fall"),
    ("Red Maple", red_maple, "#a8a39a", None, "#4a8436", None, "smaller; scarlet in fall"),
    ("Tulip Poplar", tulip_poplar, "#7e786c", None, "#6aa642", None, "Kentucky's state tree; very tall"),
    ("Shagbark Hickory", shagbark_hickory, "#8c8478", None, "#557f34", None, "tall, narrow, shaggy bark"),
    ("Black Walnut", black_walnut, "#3e3630", None, "#86a846", None, "open, airy crown"),
    ("American Beech", american_beech, "#b4b2ac", None, "#5c8c3a", None, "smooth silver bark, low dome"),
    ("Sycamore", sycamore, "#a89a80", "#ece8de", "#6a9a44", None, "white upper limbs"),
    ("Eastern Redbud", eastern_redbud, "#4a3a32", None, "#5c8c3a", "#d0508e", "spring: pink blossom"),
    ("Flowering Dogwood", flowering_dogwood, "#5a4c40", None, "#4e8a3a", "#f2eee4", "spring: white blossom tiers"),
    ("Eastern Red Cedar", eastern_red_cedar, "#8a4a32", None, "#2e5a40", None, "dense dark column"),
    ("Black Cherry", black_cherry, "#2e2622", None, "#3c6e2c", None, "dark, flaky bark"),
    ("Sassafras", sassafras, "#7a6250", None, "#6aa040", None, "layered; mitten-shaped leaves"),
    ("Ohio Buckeye", ohio_buckeye, "#8a847a", None, "#4c7e30", None, "dense rounded crown"),
]

# ---------------------------------------------------------------- isometric voxel renderer
S = 10                    # pixels per block edge
C30, S30 = math.cos(math.radians(30)), math.sin(math.radians(30))

def proj(x, y, z): return ((x - z) * C30 * S, (x + z) * S30 * S - y * S)

def render(t, bark, bark2, leaf, bloom, seed):
    r = random.Random(seed)
    cols = {"log": hexc(bark), "log2": hexc(bark2 or bark), "leaf": hexc(leaf), "bloom": hexc(bloom or leaf)}
    pts = [proj(x + a, y + b, z + c) for (x, y, z) in t.v for a in (0, 1) for b in (0, 1) for c in (0, 1)]
    # include a 2-block player figure for scale, 3 blocks to the side
    fig = [(-5, 0, 5), (-5, 1, 5)]
    pts += [proj(x + a, y + b, z + c) for (x, y, z) in fig for a in (0, 1) for b in (0, 1) for c in (0, 1)]
    minx = min(p[0] for p in pts); maxx = max(p[0] for p in pts); miny = min(p[1] for p in pts); maxy = max(p[1] for p in pts)
    W, H = int(maxx - minx) + 8, int(maxy - miny) + 8
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
    off = lambda p: (p[0] - minx + 4, p[1] - miny + 4)
    vox = dict(t.v)
    for x, y, z in fig: vox[(x, y, z)] = "fig"
    cols["fig"] = (70, 92, 140)
    order = sorted(vox, key=lambda k: (k[0] + k[1] + k[2], k[1]))
    N = 4
    for (x, y, z) in order:
        kind = vox[(x, y, z)]
        base = cols[kind]
        jitter = r.randint(-10, 10) if kind in ("leaf", "bloom") else r.randint(-4, 4)
        faces = []
        if (x, y + 1, z) not in vox: faces.append(("top", 1.0))
        if (x + 1, y, z) not in vox: faces.append(("px", 0.80))
        if (x, y, z + 1) not in vox: faces.append(("pz", 0.62))
        for f, shade in faces:
            for i in range(N):
                for j in range(N):
                    u0, u1, v0, v1 = i / N, (i + 1) / N, j / N, (j + 1) / N
                    if f == "top": q = [(x + u0, y + 1, z + v0), (x + u1, y + 1, z + v0), (x + u1, y + 1, z + v1), (x + u0, y + 1, z + v1)]
                    elif f == "px": q = [(x + 1, y + 1 - v0, z + u0), (x + 1, y + 1 - v0, z + u1), (x + 1, y + 1 - v1, z + u1), (x + 1, y + 1 - v1, z + u0)]
                    else: q = [(x + u0, y + 1 - v0, z + 1), (x + u1, y + 1 - v0, z + 1), (x + u1, y + 1 - v1, z + 1), (x + u0, y + 1 - v1, z + 1)]
                    tex = r.randint(-14, 14) if kind in ("leaf", "bloom") else (r.randint(-9, 9) if j % 2 == 0 else r.randint(-16, 4))
                    if kind == "bloom" and r.random() < 0.18: c = cols["leaf"]       # a little green among the blossom
                    else: c = base
                    if kind == "log2" and r.random() < 0.3: c = hexc(bark)           # mottled sycamore bark
                    col = tuple(max(0, min(255, int((v + jitter + tex) * shade))) for v in c) + (255,)
                    d.polygon([off(proj(*p)) for p in q], fill=col)
    return img

def font(size, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + \
             ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, size)
    return ImageFont.load_default()

def sheet(out):
    BG = (243, 239, 230); INK = (46, 42, 36); SUB = (128, 118, 104)
    tiles = []
    for k, (name, fn, bark, bark2, leaf, bloom, note) in enumerate(SPECIES):
        tiles.append((name, note, render(fn(100 + k), bark, bark2, leaf, bloom, 500 + k)))
    cols = 5; cw = max(t[2].width for t in tiles) + 30
    rows = [tiles[i:i + cols] for i in range(0, len(tiles), cols)]
    rh = [max(t[2].height for t in row) + 80 for row in rows]
    W, H = cols * cw + 60, sum(rh) + 150
    canvas = Image.new("RGB", (W, H), BG); dr = ImageDraw.Draw(canvas)
    dr.text((30, 26), "Eastern US trees: design sketches", font=font(40, True), fill=INK)
    dr.text((32, 78), "Same scale for every tree. The blue figure is a 2-block-tall player.", font=font(19), fill=SUB)
    y = 120
    for row, h in zip(rows, rh):
        for i, (name, note, im) in enumerate(row):
            x = 30 + i * cw + (cw - im.width) // 2
            canvas.paste(im, (x, y + h - 70 - im.height), im)
            dr.text((30 + i * cw + cw // 2, y + h - 48), name, font=font(22, True), fill=INK, anchor="mm")
            dr.text((30 + i * cw + cw // 2, y + h - 22), note, font=font(16), fill=SUB, anchor="mm")
        y += h
    canvas.save(out); print("wrote", out, canvas.size)

if __name__ == "__main__":
    sheet(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", "docs", "trees.png"))
