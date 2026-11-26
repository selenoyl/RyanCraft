"""
Second round of abyssal stone studies: each direction has its own grain, the way deepslate's stretched streaks set it
apart from stone, rather than being stone or deepslate recoloured. Each is drawn as a family (stone, top, cobbled,
bricks, polished), with three ores set in and a wall of it tiled out so the pattern reads at a distance.
  python3 tools/abyss_stone_concept.py  ->  docs/abyss_stone_concept.png
"""
import os, sys, math, random
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
CB = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")
N = 16

def clamp(c): return tuple(max(0, min(255, int(v))) for v in c)
def shade(pal, t):
    """Pick one of the palette's flat shades, the way hand-made pixel textures use a handful of tones, not gradients."""
    return clamp(tuple(v * 1.55 for v in pal[max(0, min(len(pal) - 1, int(t * len(pal))))])) + (255,)

def noise(seed, cells):
    """Tileable value noise on a cells x cells grid, smoothly interpolated over the 16 pixels."""
    r = random.Random(seed); g = [[r.random() for _ in range(cells)] for _ in range(cells)]
    def f(x, y):
        fx, fy = x * cells / N, y * cells / N; x0, y0 = int(fx) % cells, int(fy) % cells
        x1, y1 = (x0 + 1) % cells, (y0 + 1) % cells; tx, ty = fx - int(fx), fy - int(fy)
        tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
        a = g[y0][x0] + (g[y0][x1] - g[y0][x0]) * tx; b = g[y1][x0] + (g[y1][x1] - g[y1][x0]) * tx
        return a + (b - a) * ty
    return f

def voronoi(seeds, x, y, sx=1.0, sy=1.0):
    """Nearest and second-nearest seed on the wrapped tile, with an optional stretched metric; returns (d1, d2, index)."""
    best = []
    for i, (px, py) in enumerate(seeds):
        d = min(math.hypot((x - px + ox) * sx, (y - py + oy) * sy) for ox in (-N, 0, N) for oy in (-N, 0, N))
        best.append((d, i))
    best.sort(); return best[0][0], best[1][0], best[0][1]

# ---- the four directions --------------------------------------------------------------------------------------------

def shard(top=False):
    """A. Abyssal shale: tall splintered shards, split by dark fractures, each lit down its left edge. The top shows the
    shards end-on as a cracked mosaic, the way deepslate's top differs from its side."""
    pal = [(14, 22, 30), (26, 38, 48), (38, 54, 64), (54, 72, 82), (76, 96, 104)]
    r = random.Random(11); seeds = [(r.uniform(0, N), r.uniform(0, N)) for _ in range(9 if top else 7)]
    tone = [r.uniform(0.35, 0.7) for _ in seeds]; n = noise(4, 8); im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N):
            d1, d2, i = voronoi(seeds, x + 0.5, y + 0.5, *((1, 1) if top else (1.0, 0.28)))
            t = tone[i] + (n(x, y) - 0.5) * 0.25
            d1l, _, il = voronoi(seeds, x - 0.5, y + 0.5, *((1, 1) if top else (1.0, 0.28)))
            if il != i: t += 0.25                                                    # lit edge of the shard
            if d2 - d1 < (1.1 if top else 0.9): t = 0.02                             # fracture
            px[x, y] = shade(pal, t)
    return im

def pillow(top=False):
    """B. Pillow rock: the round lumps lava makes when it cools under deep water, each bulging toward the light, packed
    together with dark seams. Reads as bubbly from across a cavern."""
    pal = [(16, 14, 30), (30, 26, 50), (46, 40, 70), (64, 58, 92), (90, 84, 120)]
    r = random.Random(23); seeds = [(r.uniform(0, N), r.uniform(0, N)) for _ in range(6)]
    n = noise(8, 8); im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N):
            d1, d2, i = voronoi(seeds, x + 0.5, y + 0.5)
            sxp, syp = seeds[i]
            dx = ((x + 0.5 - sxp + N / 2) % N) - N / 2; dy = ((y + 0.5 - syp + N / 2) % N) - N / 2
            t = 0.75 - d1 / 9 - (dx + dy) * 0.035 + (n(x, y) - 0.5) * 0.2             # domed, lit from the top-left
            if d2 - d1 < 0.9: t = 0.0
            elif d2 - d1 < 1.8: t = min(t, 0.22)
            px[x, y] = shade(pal, t)
    return im

def veined(top=False):
    """C. Abyssalite: near-black stone with thin pale veins wandering through it, like cracked marble turned inside out."""
    pal = [(12, 14, 22), (22, 26, 38), (34, 38, 54), (48, 54, 72)]
    vein, vein_lo = (150, 170, 196, 255), (92, 108, 134, 255)
    base, v1, v2 = noise(31, 4), noise(32, 4), noise(33, 8)
    im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = shade(pal, base(x, y) * 0.9 + random.Random(x * 31 + y).uniform(-0.12, 0.12))
            v = v1(x, y) * 0.75 + v2(x, y) * 0.25
            if abs(v - 0.5) < 0.035: px[x, y] = vein
            elif abs(v - 0.5) < 0.07: px[x, y] = vein_lo
    return im

def pitted(top=False):
    """D. Abyssal pumice: grey-violet rock riddled with gas pockets, each pit shadowed at the top and lit on its lower rim."""
    pal = [(28, 24, 34), (46, 42, 54), (62, 58, 72), (80, 76, 92), (100, 96, 112)]
    r = random.Random(44); n = noise(45, 4); im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N): px[x, y] = shade(pal, 0.45 + (n(x, y) - 0.5) * 0.5 + r.uniform(-0.1, 0.1))
    pits = [(r.randrange(N), r.randrange(N), r.choice([1, 2, 2, 3])) for _ in range(11)]
    for (cx, cy, s) in pits:
        for yy in range(s):
            for xx in range(s if s < 3 else 2):
                px[(cx + xx) % N, (cy + yy) % N] = pal[0] + (255,)
        px[(cx + max(1, s - 1)) % N, (cy + s) % N] = pal[4] + (255,)                 # lit lower rim
        px[cx % N, (cy - 1) % N] = pal[1] + (255,)                                    # shadowed top lip
    return im

DIRS = [("A. Abyssal shale", "tall splintered shards split by dark fractures", shard, (0, 3)),
        ("B. Pillow rock", "round lumps from lava cooled under deep water", pillow, (1, 2)),
        ("C. Abyssalite", "near-black with pale veins wandering through", veined, (2, 2)),
        ("D. Abyssal pumice", "grey-violet, riddled with gas pockets", pitted, (1, 3))]

# ---- the family built from each stone -------------------------------------------------------------------------------

def lum(c): return sum(c[:3]) / 3
def tint(stone, f):
    out = stone.copy(); px = out.load()
    for y in range(N):
        for x in range(N): px[x, y] = clamp(tuple(v * f for v in px[x, y][:3])) + (255,)
    return out

def cobbled(stone):
    """Chunks of the stone with dark mortar between, each chunk taking the stone's own pixels so the grain carries over."""
    r = random.Random(7); seeds = [(r.uniform(0, N), r.uniform(0, N)) for _ in range(10)]
    src = stone.load(); dark = min((src[x, y] for x in range(N) for y in range(N)), key=lum)
    im = Image.new("RGBA", (N, N)); px = im.load(); lift = [r.uniform(0.85, 1.2) for _ in seeds]
    for y in range(N):
        for x in range(N):
            d1, d2, i = voronoi(seeds, x + 0.5, y + 0.5)
            c = src[(x + i * 5) % N, (y + i * 3) % N]
            _, _, il = voronoi(seeds, x - 0.5, y - 0.5)
            f = lift[i] * (1.2 if il != i else 1.0)                                    # lit top-left edge of each chunk
            px[x, y] = dark if d2 - d1 < 1.0 else clamp(tuple(v * f for v in c[:3])) + (255,)
    return im

def bricks(stone):
    """Running-bond bricks cut from the stone, mortared with its darkest tone, lit on the top edge of each brick."""
    src = stone.load(); dark = min((src[x, y] for x in range(N) for y in range(N)), key=lum)
    im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N):
            row = y // 4; off = 4 if row % 2 else 0
            if y % 4 == 3 or (x + off) % 8 == 7: px[x, y] = dark; continue
            c = src[x, y][:3]
            f = 1.25 if y % 4 == 0 else 1.0
            px[x, y] = clamp(tuple(v * f for v in c)) + (255,)
    return im

def polished(stone):
    """Smoothed: the stone's mean colour with a faint trace of its grain, and a bevel round the edge."""
    src = stone.load(); cols = [src[x, y][:3] for x in range(N) for y in range(N)]
    mean = tuple(sum(c[i] for c in cols) / len(cols) for i in range(3))
    im = Image.new("RGBA", (N, N)); px = im.load()
    for y in range(N):
        for x in range(N):
            g = (lum(src[x, y]) - lum(mean)) * 0.25
            f = 1.22 if (x == 0 or y == 0) else 0.7 if (x == N - 1 or y == N - 1) else 1.0
            px[x, y] = clamp(tuple((mean[i] + g) * f for i in range(3))) + (255,)
    return im

def ore_on(stone, ore):
    """The classic ore's mineral, lifted off classic stone and set into this stone. Coal's black is turned ash-grey with
    a pale rim so it still shows up in dark rock."""
    base = Image.open(os.path.join(CB, "stone.png")).convert("RGB").crop((0, 0, N, N))
    o = Image.open(os.path.join(CB, f"{ore}_ore.png")).convert("RGB").crop((0, 0, N, N))
    out = stone.copy(); px = out.load()
    for y in range(N):
        for x in range(N):
            a, b = o.getpixel((x, y)), base.getpixel((x, y))
            if sum(abs(a[i] - b[i]) for i in range(3)) > 60 or max(a) - min(a) > 40:
                if ore == "coal": a = (150, 146, 140) if lum(a) > 40 else (96, 92, 88)
                px[x, y] = a + (255,)
    return out

def tiled(img, cols, rows):
    t = Image.new("RGBA", (cols * N, rows * N))
    for i in range(cols):
        for j in range(rows): t.paste(img, (i * N, j * N))
    return t

def wall(stone, ores):
    """An 8 x 5 block wall of the raw stone with a few ore blocks, to judge how the grain reads from a distance."""
    r = random.Random(5); t = tiled(stone, 8, 5)
    for k, o in enumerate(ores):
        for _ in range(2):
            t.paste(ore_on(stone, o), (r.randrange(8) * N, r.randrange(5) * N))
    return t

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    S, G = 88, 14; FAM = ["stone", "top", "cobbled", "bricks", "polished", "coal", "iron", "diamond"]
    W = 40 + 8 * (S + G) + 30 + 8 * 34 + 40; rowh = S + 100; H = 170 + len(DIRS) * rowh + 20
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Abyssal stone, second round: a grain of its own", font=R.font(44, True), fill=INK)
    d.text((42, 96), "Four directions, each with its own pattern the way deepslate's streaks set it apart from stone. "
           "\nCoal is shown ash-grey so it reads in dark rock. Right: a wall of each, as you'd see it in a cavern.", font=R.font(19), fill=SUB)
    y = 160
    for name, sub, fn, _ in DIRS:
        st = fn(); top = fn(top=True)
        tiles = [st, top, cobbled(st), bricks(st), polished(st)] + [ore_on(st, o) for o in ("coal", "iron", "diamond")]
        d.text((40, y), name, font=R.font(22, True), fill=INK); d.text((40 + R.font(22, True).getlength(name) + 14, y + 6), sub, font=R.font(15), fill=SUB)
        for k, (im, lab) in enumerate(zip(tiles, FAM)):
            x = 40 + k * (S + G)
            c.paste(tiled(im, 2, 2).resize((S, S), Image.NEAREST), (x, y + 36))
            d.text((x + S // 2, y + 42 + S), lab, font=R.font(14, True), fill=INK, anchor="mt")
        wx = 40 + 8 * (S + G) + 30; wl = wall(st, ["coal", "iron", "diamond"])
        c.paste(wl.resize((8 * 34, 5 * 34), Image.NEAREST), (wx, y))
        y += rowh
    c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(HERE, "..", "docs", "abyss_stone_concept.png"))
