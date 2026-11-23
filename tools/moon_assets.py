"""Moon blocks: moon dust, the moonstone family and meteor iron. Textures are the classic 1.2.5 ones (sand, stone,
cobblestone, stone bricks, smooth stone, iron ore, iron) recoloured, plus vanilla's raw iron where 1.2.5 has none.
Blockstates and models for the stairs, slabs and walls come from vanilla's (tools/templates/moon), renamed.
  python3 tools/moon_assets.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); TEX = os.path.join(A, "textures")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures")
TPL = os.path.join(HERE, "templates", "moon", "assets", "minecraft")

LANG = {"moon_dust": "Moon Dust", "moonstone": "Moonstone", "cobbled_moonstone": "Cobbled Moonstone",
        "cobbled_moonstone_stairs": "Cobbled Moonstone Stairs", "cobbled_moonstone_slab": "Cobbled Moonstone Slab",
        "cobbled_moonstone_wall": "Cobbled Moonstone Wall", "polished_moonstone": "Polished Moonstone", "moonstone_bricks": "Moonstone Bricks",
        "cracked_moonstone_bricks": "Cracked Moonstone Bricks", "chiseled_moonstone_bricks": "Chiseled Moonstone Bricks",
        "moonstone_brick_stairs": "Moonstone Brick Stairs", "moonstone_brick_slab": "Moonstone Brick Slab", "moonstone_brick_wall": "Moonstone Brick Wall",
        "meteor_iron_ore": "Meteor Iron Ore", "deep_moonstone": "Deep Moonstone", "cobbled_deep_moonstone": "Cobbled Deep Moonstone",
        "polished_deep_moonstone": "Polished Deep Moonstone", "deep_moonstone_bricks": "Deep Moonstone Bricks", "deep_meteor_iron_ore": "Deep Meteor Iron Ore", "raw_meteor_iron_block": "Block of Raw Meteor Iron", "meteor_iron_block": "Block of Meteor Iron"}
EXTRA_LANG = {"block.ryancraft.bed.no_sleep_moon": "You can't sleep on the moon", "biome.ryancraft.moon": "The Moon"}
ITEM_LANG = {"raw_meteor_iron": "Raw Meteor Iron", "meteor_iron_ingot": "Meteor Iron Ingot"}

def lang_entries():
    out = {f"block.ryancraft.{k}": v for k, v in LANG.items()}; out.update({f"item.ryancraft.{k}": v for k, v in ITEM_LANG.items()}); out.update(EXTRA_LANG); return out

# --------------------------------------------------------------------------------------------- textures
DUST_TINT, STONE_TINT, METEOR_TINT = (2, 1, -3), (-3, -1, 4), (-4, -2, 8)

def recolour(src, mean, contrast=1.0, tint=(0, 0, 0)):
    """Greyscale the texture, move it to the given mean brightness (keeping its shading), then tint it."""
    im = Image.open(src).convert("RGBA"); px = im.load()
    vals = [(r + g + b) / 3 for r, g, b, a in im.get_flattened_data() if a]
    m0 = sum(vals) / len(vals)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            v = mean + ((r + g + b) / 3 - m0) * contrast
            px[x, y] = tuple(max(0, min(255, int(v + t))) for t in tint) + (a,)
    return im

def sparkle(im, n, seed, mask=None):
    """A few star-like flecks: meteor iron's tell."""
    r = random.Random(seed); px = im.load(); placed = 0; tries = 0
    while placed < n and tries < 500:
        tries += 1; x, y = r.randrange(im.width), r.randrange(im.height)
        if px[x, y][3] == 0 or (mask and (x, y) not in mask): continue
        px[x, y] = (196, 214, 255, 255) if placed % 2 == 0 else (150, 170, 220, 255); placed += 1
    return im

# Each block keeps a vanilla template's structure (stone grain, cobbles, bricks, deepslate layers, ore spots), mapped
# onto its own moon palette, with lunar detail on top. Everything wraps, so blocks tile without seams.
def mapped(src, dark, light, contrast=1.0):
    """The template's shading gradient-mapped from dark to light (contrast < 1 quiets the grain)."""
    im = Image.open(src).convert("RGBA").crop((0, 0, 16, 16)); px = im.load()
    vals = sorted((r + g + b) / 3 for r, g, b, a in im.get_flattened_data() if a); lo, hi = vals[4], vals[-4]
    out = Image.new("RGBA", (16, 16)); po = out.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            t = min(1, max(0, ((r + g + b) / 3 - lo) / max(1, hi - lo))); t = 0.5 + (t - 0.5) * contrast
            po[x, y] = tuple(int(dark[k] + (light[k] - dark[k]) * t) for k in range(3)) + (a,)
    return out

def dots(img, r, n, col, size=1, avoid=None):
    """Small specks (grit, crystal flecks, olivine): single pixels or little two-pixel clusters."""
    px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16)
        if avoid and (x, y) in avoid: continue
        px[x, y] = col + (255,)
        if size > 1: px[(x + r.choice((-1, 1))) % 16, (y + r.choice((0, 1))) % 16] = tuple(min(255, c + 8) for c in col) + (255,)
    return img

def pits(img, r, n, deep, rim):
    """Pockmarks: a dark hollow with a lit lower rim (micrometeorite pits in dust, gas pockets in basalt)."""
    px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16)
        px[x, y] = deep + (255,); px[(x + 1) % 16, y] = tuple((a + b) // 2 for a, b in zip(deep, px[(x + 1) % 16, y][:3])) + (255,)
        px[x, (y + 1) % 16] = rim + (255,); px[(x + 1) % 16, (y + 1) % 16] = rim + (255,)
    return img

def ore(base, template_stone, template_ore, shadow, body, shine, glint, seed):
    """The template ore's spots as dark metal nuggets: shaded body, a lit edge and a star-bright glint or two."""
    out = base.copy(); po = out.load(); ps = Image.open(template_stone).convert("RGBA").load(); pr = Image.open(template_ore).convert("RGBA").load()
    spots = []
    for y in range(16):
        for x in range(16):
            if abs(sum(ps[x, y][:3]) - sum(pr[x, y][:3])) > 24:
                v = sum(pr[x, y][:3]) / 3; spots.append((x, y, v))
    vs = sorted(v for _, _, v in spots)
    for x, y, v in spots:
        t = vs.index(v) / max(1, len(vs) - 1)
        po[x, y] = (shadow if t < 0.35 else body if t < 0.8 else shine) + (255,)
    r = random.Random(seed)
    for x, y, _ in r.sample(spots, 3): po[x, y] = glint + (255,)
    return out

def crescent(img, groove, edge):
    """A carved crescent moon (chiseled moonstone bricks)."""
    px = img.load()
    for y in range(16):
        for x in range(16):
            a = (x - 7.5) ** 2 + (y - 7.5) ** 2; b = (x - 9.3) ** 2 + (y - 6.4) ** 2
            if a <= 25 and b > 16: px[x, y] = groove + (255,)
            elif a <= 30 and b > 20 and x < 8: px[x, y] = edge + (255,)
    return img

def frame(img, light, dark):
    """A vanilla metal-block bevel: lit top and left edge, shadowed bottom and right."""
    px = img.load()
    for k in range(16):
        px[k, 0] = light + (255,); px[0, k] = light + (255,); px[k, 15] = dark + (255,); px[15, k] = dark + (255,)
    return img

def widmanstatten(img, r, bright, dark):
    """Cut meteoric iron's crossing bands (kamacite plates) at steep, uneven angles, broken into short runs of
    different lengths, with a soft shadow beside each, so it reads as metal grown in a crystal, not a fence.
    Slopes and periods divide 16, so it tiles."""
    px = img.load()
    runs = {}
    for y in range(16):
        for x in range(16):
            a, b = (x + 2 * y) % 16, (x - 2 * y) % 16
            band = a in (0, 1) and (y // 2 + x // 5) % 3 != 2 or b in (8,) and (y // 3 + x // 4) % 3 != 1
            if band: px[x, y] = tuple(int(c * 0.25 + br * 0.75) for c, br in zip(px[x, y][:3], bright)) + (255,)
            elif a == 2 or b == 9: px[x, y] = tuple(int(c * 0.4 + d * 0.6) for c, d in zip(px[x, y][:3], dark)) + (255,)
    return img

def rust(img, r, n, cols):
    px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16)
        if px[x, y][3] == 0: continue
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            if r.random() < 0.7 and px[(x + dx) % 16, (y + dy) % 16][3]: px[(x + dx) % 16, (y + dy) % 16] = r.choice(cols) + (255,)
    return img

def item_map(src, dark, light):
    """An item sprite (ingot, raw lump) gradient-mapped, keeping its outline and transparency."""
    im = Image.open(src).convert("RGBA"); px = im.load()
    vals = [(r + g + b) / 3 for r, g, b, a in im.get_flattened_data() if a]; lo, hi = min(vals), max(vals)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a: t = ((r + g + b) / 3 - lo) / max(1, hi - lo); px[x, y] = tuple(int(dark[k] + (light[k] - dark[k]) * t) for k in range(3)) + (a,)
    return im

# palettes
DUST = ((150, 148, 142), (206, 204, 198))                     # pale, faintly warm regolith
STONE = ((128, 126, 122), (190, 188, 182))                    # anorthosite: the light highland rock
FELDSPAR = (226, 226, 220)
BASALT = ((46, 48, 56), (102, 104, 114))                      # mare basalt: dark, a little blue
OLIVINE = (98, 122, 72)
IRON = ((30, 32, 40), (120, 126, 140))                        # dark gunmetal


# ---------------------------------------------------------------- original moon stone (no vanilla template)
def vnoise(seed, scale=4):
    """Smooth value noise, 0..1, wrapping at 16 px (a coarse grid of random heights, blended)."""
    r = random.Random(seed); n = 16 // scale
    g = [[r.random() for _ in range(n)] for _ in range(n)]
    out = [[0.0] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            gx, gy = x / scale, y / scale; x0, y0 = int(gx) % n, int(gy) % n; x1, y1 = (x0 + 1) % n, (y0 + 1) % n
            fx, fy = gx - int(gx), gy - int(gy); fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
            a = g[y0][x0] + (g[y0][x1] - g[y0][x0]) * fx; b = g[y1][x0] + (g[y1][x1] - g[y1][x0]) * fx
            out[y][x] = a + (b - a) * fy
    return out

def rock(seed, dark, light, grain=0.35):
    """A stone surface: soft value noise for broad tone, plus per-pixel grain."""
    r = random.Random(seed); n1, n2 = vnoise(seed, 8), vnoise(seed + 1, 4)
    img = Image.new("RGBA", (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            t = 0.55 * n1[y][x] + 0.45 * n2[y][x] + (r.random() - 0.5) * grain
            t = min(1, max(0, t))
            px[x, y] = tuple(int(dark[k] + (light[k] - dark[k]) * t) for k in range(3)) + (255,)
    return img

def shade_px(c, k): return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)

def craters(img, spots):
    """Impact craters: a bowl shaded from dark (top-left, in shadow) to light (bottom-right, catching the sun), and a
    raised rim lit on the top-left and shadowed on the bottom-right."""
    px = img.load()
    for cx, cy, rad in spots:
        for dy in range(-rad - 2, rad + 3):
            for dx in range(-rad - 2, rad + 3):
                d = (dx * dx + dy * dy) ** 0.5; x, y = int(cx + dx) % 16, int(cy + dy) % 16
                lean = (dx + dy) / (rad * 2.0)                                       # -1 top-left .. +1 bottom-right
                if d <= rad - 0.5: px[x, y] = shade_px(px[x, y], 0.82 + 0.16 * lean)
                elif d <= rad + 0.7: px[x, y] = shade_px(px[x, y], 1.14 - 0.26 * (lean + 0.5))
    return img

def breccia(seed, matrix, shard_dark, shard_light, n=8):
    """Lunar breccia: angular fragments of different tones welded together. Each pixel belongs to its nearest seed
    point (wrapping), which carves the block into sharp polygons; the seams between them are the dark matrix, and
    each fragment is lit along its upper edge."""
    r = random.Random(seed); c = 16 / 3                                    # a jittered 3x3 grid: evenly spread fragments
    pts = [((i + r.uniform(0.15, 0.85)) * c, (j + r.uniform(0.15, 0.85)) * c) for i in range(3) for j in range(3)]
    n = len(pts); tones = [0.35 + 0.65 * ((k * 0.618 + r.random() * 0.3) % 1) for k in range(n)]
    def owner(x, y):
        best = None
        for i, (px_, py_) in enumerate(pts):
            dx = min(abs(x - px_), 16 - abs(x - px_)); dy = min(abs(y - py_), 16 - abs(y - py_))
            d = dx * dx * 1.0 + dy * dy * 1.3
            if best is None or d < best[0]: best = (d, i)
        return best[1]
    own = [[owner(x + 0.5, y + 0.5) for x in range(16)] for y in range(16)]
    img = rock(seed, shade_px(matrix, 0.8)[:3], matrix, 0.15); px = img.load(); g = random.Random(seed + 7)
    for y in range(16):
        for x in range(16):
            i = own[y][x]
            if own[y][(x + 1) % 16] != i or own[(y + 1) % 16][x] != i: continue          # seam: leave the matrix
            col = tuple(int(shard_dark[k] + (shard_light[k] - shard_dark[k]) * tones[i]) for k in range(3))
            k = 1.14 if own[(y - 1) % 16][x] != i else 0.88 if own[(y + 2) % 16][x] != i else 1.0
            px[x, y] = shade_px(col + (255,), k * (0.95 + g.random() * 0.1))
    return img

def laths(img, seed, col, n=5):
    """Long thin crystal blades in polished stone (plagioclase laths): faint, all leaning roughly the same way."""
    r = random.Random(seed); px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16); step = r.choice((1, 2))
        for k in range(r.randint(3, 5)):
            xx, yy = (x + k) % 16, (y - k // step) % 16
            px[xx, yy] = tuple(int(a * 0.55 + b * 0.45) for a, b in zip(px[xx, yy][:3], col)) + (255,)
    return img

def tiles(img, size, light, dark, mortar):
    """Square tiles with a bevel: lit top-left edges, shadowed bottom-right, and a thin mortar line."""
    px = img.load()
    for y in range(16):
        for x in range(16):
            tx, ty = x % size, y % size
            if tx == size - 1 or ty == size - 1: px[x, y] = mortar + (255,)
            elif tx == 0 or ty == 0: px[x, y] = tuple(int((a + b) / 2) for a, b in zip(px[x, y][:3], light)) + (255,)
            elif tx == size - 2 or ty == size - 2: px[x, y] = tuple(int((a + b) / 2) for a, b in zip(px[x, y][:3], dark)) + (255,)
    return img

def cracks(img, seed, col, n=3):
    """Jagged fractures: each wanders mostly one way, with the odd kink and a short branch."""
    r = random.Random(seed); px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16); dx, dy = r.choice(((1, 0), (0, 1), (1, 1), (1, -1)))
        for k in range(r.randint(5, 9)):
            px[x % 16, y % 16] = col + (255,)
            if r.random() < 0.35: x += dy; y += dx                                   # a kink sideways
            x += dx; y += dy
            if k == 3 and r.random() < 0.5: px[(x + dy) % 16, (y + dx) % 16] = col + (255,)
    return img

def vesicles(img, seed, n, hole, rim):
    """Vesicular basalt: round gas bubbles, some 1 px, some 2x2, each with a lit lower lip."""
    r = random.Random(seed); px = img.load()
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16); big = r.random() < 0.4
        cells = [(0, 0), (1, 0), (0, 1), (1, 1)] if big else [(0, 0)]
        for dx, dy in cells: px[(x + dx) % 16, (y + dy) % 16] = hole + (255,)
        lip = 2 if big else 1
        for dx in range(lip): px[(x + dx) % 16, (y + lip) % 16] = rim + (255,)
    return img

def columns_side(img, dark, light):
    """Basalt columns seen from the side: vertical joints every few pixels with a lit face beside each."""
    px = img.load()
    for x in (0, 5, 11):
        for y in range(16):
            xx = x + (1 if (y // 5) % 2 and x == 5 else 0)
            px[xx % 16, y] = dark + (255,); px[(xx + 1) % 16, y] = tuple(int((a + b) / 2) for a, b in zip(px[(xx + 1) % 16, y][:3], light)) + (255,)
    return img

def hex_top(img, dark, light):
    """Basalt columns seen from the top: a tiling net of hexagons (two per row, offset), with lit inner edges."""
    px = img.load()
    for y in range(16):
        for x in range(16):
            # a hex grid with cells 8 wide and 8 tall, odd rows shifted by 4: tiles at 16
            row = y // 8; ox = 4 if row % 2 else 0; cx = (x + ox) % 8; cy = y % 8
            edge = cy == 0 or (cy in (1, 2) and cx in (0, 7)) or (cy >= 3 and cx == 0) or (cy in (6, 7) and cx in (0, 7) and False)
            slant = (cy == 1 and cx in (1, 6)) or (cy == 7 and cx in (1, 6))
            if edge or slant: px[x, y] = dark + (255,)
            elif cy == 1 or cx == 1: px[x, y] = tuple(int((a + b) / 2) for a, b in zip(px[x, y][:3], light)) + (255,)
    return img

def textures():
    b, i = os.path.join(TEX, "block"), os.path.join(TEX, "item")
    cb = lambda n: os.path.join(CLASSIC, "block", n + ".png")
    dt = lambda n: os.path.join(HERE, "templates", "moon", "textures", n + ".png")
    r = lambda k: random.Random(k)
    # moon dust: soft powder, fine dark grit, a few bright grains, tiny impact pits
    dust = mapped(cb("sand"), *DUST, contrast=0.55)
    dots(dust, r(1), 12, (124, 122, 116)); dots(dust, r(2), 6, (224, 222, 216)); pits(dust, r(3), 2, (112, 110, 106), (214, 212, 206))
    dust.save(os.path.join(b, "moon_dust.png"))
    # moonstone: pale stone with white feldspar crystals and pockmarks
    ms = craters(rock(40, *STONE), ((4, 5, 3), (12, 12, 2), (12, 3, 1)))
    dots(ms, r(4), 5, FELDSPAR)
    ms.save(os.path.join(b, "moonstone.png"))
    breccia(41, STONE[0], (110, 108, 104), (216, 214, 208)).save(os.path.join(b, "cobbled_moonstone.png"))
    laths(rock(42, (166, 164, 158), (206, 204, 198), 0.15), 43, (230, 230, 226)).save(os.path.join(b, "polished_moonstone.png"))
    bricks = tiles(rock(44, (150, 148, 142), (198, 196, 190), 0.2), 8, (220, 218, 212), (120, 118, 112), (96, 94, 90))
    craters(bricks, ((11, 4, 2),)); bricks.save(os.path.join(b, "moonstone_bricks.png"))
    cracks(bricks.copy(), 45, (100, 98, 94), 3).save(os.path.join(b, "cracked_moonstone_bricks.png"))
    chis = frame(rock(46, (160, 158, 152), (204, 202, 196), 0.15), (220, 218, 212), (104, 102, 98))
    crescent(chis, (110, 108, 104), (222, 220, 214)).save(os.path.join(b, "chiseled_moonstone_bricks.png"))
    # meteor iron ore: dark metal nuggets in moonstone
    ore(ms, cb("stone"), cb("iron_ore"), (34, 34, 40), (70, 72, 82), (146, 152, 166), (222, 232, 255), 10).save(os.path.join(b, "meteor_iron_ore.png"))
    # deep moonstone: dark basalt in vanilla deepslate's layers, with gas pockets and a little olivine
    deep = vesicles(columns_side(rock(50, *BASALT), (30, 30, 36), (118, 120, 130)), 51, 6, (24, 24, 30), (116, 118, 128))
    dots(deep, r(12), 2, OLIVINE); deep.save(os.path.join(b, "deep_moonstone.png"))
    vesicles(hex_top(rock(52, *BASALT), (30, 30, 36), (118, 120, 130)), 53, 3, (24, 24, 30), (116, 118, 128)).save(os.path.join(b, "deep_moonstone_top.png"))
    breccia(54, BASALT[0], (40, 42, 50), (118, 120, 132)).save(os.path.join(b, "cobbled_deep_moonstone.png"))
    laths(rock(55, (56, 58, 68), (92, 94, 106), 0.12), 56, (140, 146, 162), 5).save(os.path.join(b, "polished_deep_moonstone.png"))
    vesicles(tiles(rock(57, *BASALT, 0.2), 8, (124, 126, 136), (34, 34, 40), (22, 22, 28)), 58, 3, (24, 24, 30), (116, 118, 128)).save(os.path.join(b, "deep_moonstone_bricks.png"))
    ore(deep, dt("deepslate"), dt("deepslate_iron_ore"), (60, 62, 70), (118, 122, 134), (190, 196, 212), (232, 240, 255), 17).save(os.path.join(b, "deep_meteor_iron_ore.png"))
    # meteor iron: cut-meteorite Widmanstatten bands in a bevelled block; raw is black fusion crust with rust
    blk = mapped(cb("iron_block"), (70, 72, 82), (104, 108, 120), contrast=0.4)
    frame(widmanstatten(blk, r(18), (146, 152, 168), (50, 52, 60)), (124, 128, 140), (38, 40, 46)).save(os.path.join(b, "meteor_iron_block.png"))
    raw = mapped(os.path.join(TPL, "textures", "block", "raw_iron_block.png"), (26, 24, 24), (84, 80, 78))
    rust(raw, r(19), 6, [(118, 70, 40), (148, 94, 54)]); dots(raw, r(20), 3, (196, 206, 226))
    raw.save(os.path.join(b, "raw_meteor_iron_block.png"))
    ingot = item_map(os.path.join(CLASSIC, "item", "iron_ingot.png"), *IRON); pi = ingot.load()
    for y in range(16):
        for x in range(16):
            if pi[x, y][3] and (x + 2 * y) % 6 == 0 and sum(pi[x, y][:3]) > 150: pi[x, y] = tuple(min(255, c + 30) for c in pi[x, y][:3]) + (255,)   # a hint of the bands
    ingot.save(os.path.join(i, "meteor_iron_ingot.png"))
    lump = item_map(os.path.join(TPL, "textures", "item", "raw_iron.png"), (22, 20, 20), (96, 92, 90))
    rust(lump, r(21), 3, [(118, 70, 40), (148, 94, 54)]); lp = lump.load()
    for x, y in ((6, 5), (9, 8)):
        if lp[x, y][3]: lp[x, y] = (210, 220, 240, 255)
    lump.save(os.path.join(i, "raw_meteor_iron.png"))

# --------------------------------------------------------------------------------------------- json
def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

def renamed(o, a, b, tex):
    s = json.dumps(o).replace(f"minecraft:block/{a}", f"ryancraft:block/{b}")
    s = s.replace('"minecraft:block/cobblestone"', f'"ryancraft:block/{tex}"').replace('"minecraft:block/stone_bricks"', f'"ryancraft:block/{tex}"')
    return json.loads(s)

def drops(name, item=None):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{item or name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}

def silk_or(name, other, fortune=False, count=None):
    alt = {"type": "minecraft:item", "name": f"ryancraft:{other}", "functions": []}
    if fortune: alt["functions"].append({"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"})
    alt["functions"].append({"function": "minecraft:explosion_decay"})
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": f"ryancraft:{name}", "conditions": [silk]}, alt]}]}]}

def json_files():
    cubes = ["moon_dust", "moonstone", "cobbled_moonstone", "polished_moonstone", "moonstone_bricks", "cracked_moonstone_bricks",
             "chiseled_moonstone_bricks", "meteor_iron_ore", "raw_meteor_iron_block", "meteor_iron_block",
             "cobbled_deep_moonstone", "polished_deep_moonstone", "deep_moonstone_bricks", "deep_meteor_iron_ore"]
    for n in cubes:
        w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
        w(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
        w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
    w("assets/ryancraft/blockstates/deep_moonstone.json", {"variants": {"axis=y": {"model": "ryancraft:block/deep_moonstone"},
        "axis=z": {"model": "ryancraft:block/deep_moonstone", "x": 90}, "axis=x": {"model": "ryancraft:block/deep_moonstone", "x": 90, "y": 90}}})
    w("assets/ryancraft/models/block/deep_moonstone.json", {"parent": "minecraft:block/cube_column",
        "textures": {"end": "ryancraft:block/deep_moonstone_top", "side": "ryancraft:block/deep_moonstone"}})
    w("assets/ryancraft/items/deep_moonstone.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/deep_moonstone"}})
    for src, dst, tex in (("cobblestone", "cobbled_moonstone", "cobbled_moonstone"), ("stone_brick", "moonstone_brick", "moonstone_bricks")):
        for kind in ("stairs", "slab", "wall"):
            bs = json.load(open(os.path.join(TPL, "blockstates", f"{src}_{kind}.json")))
            w(f"assets/ryancraft/blockstates/{dst}_{kind}.json", renamed(bs, f"{src}_{kind}", f"{dst}_{kind}", tex))
            for f in os.listdir(os.path.join(TPL, "models", "block")):
                if f.startswith(f"{src}_{kind}"):
                    m = json.load(open(os.path.join(TPL, "models", "block", f)))
                    w(f"assets/ryancraft/models/block/{dst}{f[len(src):]}", renamed(m, f"{src}_{kind}", f"{dst}_{kind}", tex))
            item_model = f"ryancraft:block/{dst}_{kind}" + ("_inventory" if kind == "wall" else "")
            w(f"assets/ryancraft/items/{dst}_{kind}.json", {"model": {"type": "minecraft:model", "model": item_model}})
    for n in ITEM_LANG:
        w(f"assets/ryancraft/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{n}"}})
        w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{n}"}})
    # loot
    L = "data/ryancraft/loot_table/blocks/"
    for n in LANG:
        if n in ("moonstone", "meteor_iron_ore", "deep_moonstone", "deep_meteor_iron_ore") or n.endswith("_slab"): continue
        w(L + n + ".json", drops(n))
    w(L + "moonstone.json", silk_or("moonstone", "cobbled_moonstone"))
    w(L + "meteor_iron_ore.json", silk_or("meteor_iron_ore", "raw_meteor_iron", fortune=True))
    w(L + "deep_moonstone.json", silk_or("deep_moonstone", "cobbled_deep_moonstone"))
    w(L + "deep_meteor_iron_ore.json", silk_or("deep_meteor_iron_ore", "raw_meteor_iron", fortune=True))
    for n in ("cobbled_moonstone_slab", "moonstone_brick_slab"):
        w(L + n + ".json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}", "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{"condition": "minecraft:block_state_property", "block": f"ryancraft:{n}",
             "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]}]}]})
    # recipes
    R = "data/ryancraft/recipe/"
    shaped = lambda name, pat, key, n, cat="building": w(f"{R}{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pat,
        "key": {k: f"ryancraft:{v}" for k, v in key.items()}, "result": {"id": f"ryancraft:{name}", "count": n}})
    shapeless = lambda name, ings, n, out=None: w(f"{R}{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [f"ryancraft:{x}" for x in ings], "result": {"id": f"ryancraft:{out or name}", "count": n}})
    def smelt(name, src, out, xp, blast=True):
        for t, tm in (("smelting", 200), ("blasting", 100)) if blast else (("smelting", 200),):
            w(f"{R}{name}{'_from_blasting' if t == 'blasting' else ''}.json", {"type": f"minecraft:{t}", "category": "misc", "ingredient": f"ryancraft:{src}",
              "result": {"id": f"ryancraft:{out}"}, "experience": xp, "cookingtime": tm})
    smelt("moonstone", "cobbled_moonstone", "moonstone", 0.1, blast=False)
    smelt("cracked_moonstone_bricks", "moonstone_bricks", "cracked_moonstone_bricks", 0.1, blast=False)
    smelt("meteor_iron_ingot", "raw_meteor_iron", "meteor_iron_ingot", 1.0)
    smelt("deep_moonstone", "cobbled_deep_moonstone", "deep_moonstone", 0.1, blast=False)
    smelt("meteor_iron_ingot_from_deep_ore", "deep_meteor_iron_ore", "meteor_iron_ingot", 1.0)
    shaped("polished_deep_moonstone", ["##", "##"], {"#": "cobbled_deep_moonstone"}, 4)
    shaped("deep_moonstone_bricks", ["##", "##"], {"#": "polished_deep_moonstone"}, 4)
    smelt("meteor_iron_ingot_from_ore", "meteor_iron_ore", "meteor_iron_ingot", 1.0)
    shaped("polished_moonstone", ["##", "##"], {"#": "moonstone"}, 4)
    shaped("moonstone_bricks", ["##", "##"], {"#": "polished_moonstone"}, 4)
    shaped("chiseled_moonstone_bricks", ["#", "#"], {"#": "moonstone_brick_slab"}, 1)
    for base, mat in (("cobbled_moonstone", "cobbled_moonstone"), ("moonstone_brick", "moonstone_bricks")):
        shaped(f"{base}_stairs", ["#  ", "## ", "###"], {"#": mat}, 4)
        shaped(f"{base}_slab", ["###"], {"#": mat}, 6)
        shaped(f"{base}_wall", ["###", "###"], {"#": mat}, 6, "misc")
    shaped("meteor_iron_block", ["###", "###", "###"], {"#": "meteor_iron_ingot"}, 1)
    shaped("raw_meteor_iron_block", ["###", "###", "###"], {"#": "raw_meteor_iron"}, 1)
    shapeless("meteor_iron_ingot_from_block", ["meteor_iron_block"], 9, "meteor_iron_ingot")
    shapeless("raw_meteor_iron_from_block", ["raw_meteor_iron_block"], 9, "raw_meteor_iron")
    def cut(name, src, n):
        w(f"{R}{name}_from_{src}_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": f"ryancraft:{src}", "result": {"id": f"ryancraft:{name}", "count": n}})
    for name, n in (("cobbled_moonstone_stairs", 1), ("cobbled_moonstone_slab", 2), ("cobbled_moonstone_wall", 1)): cut(name, "cobbled_moonstone", n)
    for name, n in (("polished_moonstone", 1), ("moonstone_bricks", 1), ("moonstone_brick_stairs", 1), ("moonstone_brick_slab", 2),
                    ("moonstone_brick_wall", 1), ("chiseled_moonstone_bricks", 1)): cut(name, "moonstone", n)
    for name, n in (("moonstone_brick_stairs", 1), ("moonstone_brick_slab", 2), ("moonstone_brick_wall", 1), ("chiseled_moonstone_bricks", 1)): cut(name, "moonstone_bricks", n)
    # tags
    stoney = [f"ryancraft:{n}" for n in LANG if n != "moon_dust"]
    add_tag("data/minecraft/tags/block/mineable/pickaxe.json", stoney)
    add_tag("data/minecraft/tags/block/mineable/shovel.json", ["ryancraft:moon_dust"])
    add_tag("data/minecraft/tags/block/needs_iron_tool.json", ["ryancraft:meteor_iron_ore", "ryancraft:deep_meteor_iron_ore", "ryancraft:raw_meteor_iron_block", "ryancraft:meteor_iron_block"])
    for kind in ("stairs", "slabs", "walls"):
        k = kind[:-1] if kind != "stairs" else "stairs"
        names = [f"ryancraft:cobbled_moonstone_{k}", f"ryancraft:moonstone_brick_{k}"]
        add_tag(f"data/minecraft/tags/block/{kind}.json", names); add_tag(f"data/minecraft/tags/item/{kind}.json", names)
    add_tag("data/minecraft/tags/block/beacon_base_blocks.json", ["ryancraft:meteor_iron_block"])
    add_tag("data/minecraft/tags/item/beacon_payment_items.json", ["ryancraft:meteor_iron_ingot"])
    add_tag("data/minecraft/tags/item/stone_crafting_materials.json", ["ryancraft:cobbled_moonstone"])
    add_tag("data/minecraft/tags/item/stone_tool_materials.json", ["ryancraft:cobbled_moonstone"])
    # what the moon's caves may carve through
    w("data/ryancraft/tags/block/moon_carver_replaceables.json", {"values": ["ryancraft:moonstone", "ryancraft:moon_dust",
        "ryancraft:meteor_iron_ore", "ryancraft:deep_moonstone", "ryancraft:deep_meteor_iron_ore"]})

# --------------------------------------------------------------------------------------------- the moon dimension
def worldgen():
    """The moon: rolling moonstone under a few blocks of dust, a black sky with the stars always out, caves and wide
    old lava tubes through the stone, and meteor iron in it (glowing faintly, so it shows in the dark caves)."""
    D = "data/ryancraft/"
    day = json.load(open(os.path.join(HERE, "templates", "moon", "day_timeline.json")))
    tracks = {k: v for k, v in day["tracks"].items() if k in ("minecraft:visual/sun_angle", "minecraft:visual/star_angle", "minecraft:visual/moon_angle",
              "minecraft:gameplay/sky_light_level", "minecraft:visual/sky_light_factor", "minecraft:visual/sky_light_color", "minecraft:gameplay/monsters_burn")}
    w(D + "timeline/lunar_day.json", {"period_ticks": 24000, "tracks": tracks})
    w(D + "dimension_type/moon.json", {
        "ambient_light": 0.0,
        "attributes": {
            "minecraft:audio/background_music": {"default": {"max_delay": 24000, "min_delay": 12000, "sound": "minecraft:music.game"}},
            "minecraft:gameplay/bed_rule": {"can_set_spawn": "never", "can_sleep": "never", "explodes": False,
                                            "error_message": {"translate": "block.ryancraft.bed.no_sleep_moon"}},
            "minecraft:gameplay/respawn_anchor_works": False,
            "minecraft:visual/sky_color": "#000000", "minecraft:visual/fog_color": "#000000",
            "minecraft:visual/star_brightness": 0.75, "minecraft:visual/sunrise_sunset_color": "#00000000",
            "minecraft:visual/cloud_color": "#00000000"},
        "coordinate_scale": 1.0, "has_ceiling": False, "has_skylight": True, "height": 256, "logical_height": 256, "min_y": 0,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "monster_spawn_block_light_limit": 0, "monster_spawn_light_level": {"type": "minecraft:uniform", "max_inclusive": 7, "min_inclusive": 0},
        "timelines": ["ryancraft:lunar_day"]})
    w(D + "dimension/moon.json", {"type": "ryancraft:moon", "generator": {"type": "minecraft:noise", "settings": "ryancraft:moon",
        "biome_source": {"type": "minecraft:fixed", "biome": "ryancraft:moon"}}})
    noise2d = lambda n, s, k: {"type": "minecraft:mul", "argument1": k, "argument2": {"type": "minecraft:noise", "noise": n, "xz_scale": s, "y_scale": 0.0}}
    terrain = {"type": "minecraft:add",
               "argument1": {"type": "minecraft:y_clamped_gradient", "from_y": 40, "to_y": 130, "from_value": 1.0, "to_value": -1.0},
               "argument2": {"type": "minecraft:add", "argument1": noise2d("minecraft:surface", 0.6, 0.22),
                             "argument2": noise2d("minecraft:erosion", 0.35, 0.45)}}
    zero = 0.0
    router = {k: zero for k in ("barrier", "continents", "depth", "erosion", "fluid_level_floodedness", "fluid_level_spread", "lava",
                                "preliminary_surface_level", "ridges", "temperature", "vegetation", "vein_gap", "vein_ridged", "vein_toggle")}
    router["final_density"] = {"type": "minecraft:interpolated", "argument": terrain}
    dust = {"type": "minecraft:block", "result_state": {"Name": "ryancraft:moon_dust"}}
    w(D + "worldgen/noise_settings/moon.json", {
        "aquifers_enabled": False, "default_block": {"Name": "ryancraft:moonstone"}, "default_fluid": {"Name": "minecraft:air"},
        "disable_mob_generation": True, "legacy_random_source": False, "ore_veins_enabled": False, "sea_level": 0, "spawn_target": [],
        "noise": {"height": 256, "min_y": 0, "size_horizontal": 1, "size_vertical": 2},
        "noise_router": router,
        "surface_rule": {"type": "minecraft:sequence", "sequence": [
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
                "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
             "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}},
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": "ryancraft:deep_moonstone",
                "true_at_and_below": {"absolute": 24}, "false_at_and_above": {"absolute": 36}},
             "then_run": {"type": "minecraft:block", "result_state": {"Name": "ryancraft:deep_moonstone", "Properties": {"axis": "y"}}}},
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 2, "surface_type": "floor",
                "add_surface_depth": True, "secondary_depth_range": 0}, "then_run": dust}]}})
    def carver(name, prob, hr, vr, ys, lo, hi):
        w(D + f"worldgen/configured_carver/{name}.json", {"type": "minecraft:cave", "config": {
            "probability": prob, "replaceable": "#ryancraft:moon_carver_replaceables", "lava_level": {"absolute": -64},
            "y": {"type": "minecraft:uniform", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}},
            "yScale": {"type": "minecraft:uniform", "min_inclusive": ys[0], "max_exclusive": ys[1]},
            "horizontal_radius_multiplier": {"type": "minecraft:uniform", "min_inclusive": hr[0], "max_exclusive": hr[1]},
            "vertical_radius_multiplier": {"type": "minecraft:uniform", "min_inclusive": vr[0], "max_exclusive": vr[1]},
            "floor_level": {"type": "minecraft:uniform", "min_inclusive": -1.0, "max_exclusive": -0.4}}})
    carver("moon_cave", 0.2, (0.8, 1.6), (0.8, 1.3), (0.2, 0.9), 8, 120)
    carver("moon_lava_tube", 0.05, (2.2, 3.6), (0.9, 1.2), (0.25, 0.45), 12, 70)     # the moon's old lava tubes: wide, low tunnels
    canyon = json.load(open(os.path.join(HERE, "templates", "moon", "canyon.json")))      # ravines, as on Earth but more of them
    cfg = canyon["config"]; cfg.pop("debug_settings", None); cfg["replaceable"] = "#ryancraft:moon_carver_replaceables"
    cfg["lava_level"] = {"absolute": -64}; cfg["probability"] = 0.03
    cfg["y"] = {"type": "minecraft:uniform", "min_inclusive": {"absolute": 20}, "max_inclusive": {"absolute": 90}}
    w(D + "worldgen/configured_carver/moon_ravine.json", canyon)
    w(D + "worldgen/configured_feature/ore_meteor_iron.json", {"type": "minecraft:ore", "config": {"size": 7, "discard_chance_on_air_exposure": 0.0,
        "targets": [{"state": {"Name": "ryancraft:meteor_iron_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": "ryancraft:moonstone"}},
                    {"state": {"Name": "ryancraft:deep_meteor_iron_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": "ryancraft:deep_moonstone"}}]}})
    w(D + "worldgen/placed_feature/ore_meteor_iron.json", {"feature": "ryancraft:ore_meteor_iron", "placement": [
        {"type": "minecraft:count", "count": 14}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": 1}, "max_inclusive": {"absolute": 90}}},
        {"type": "minecraft:biome"}]})
    features = [[] for _ in range(11)]; features[6] = ["ryancraft:ore_meteor_iron"]
    w(D + "worldgen/biome/moon.json", {"carvers": ["ryancraft:moon_cave", "ryancraft:moon_lava_tube", "ryancraft:moon_ravine"],
        "attributes": {"minecraft:visual/fog_color": "#000000", "minecraft:visual/sky_color": "#000000"}, "downfall": 0.0,
        "effects": {"water_color": "#3f76e4"}, "features": features, "has_precipitation": False, "spawn_costs": {},
        "spawners": {k: [] for k in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature", "water_ambient", "water_creature")},
        "temperature": 0.5})

if __name__ == "__main__":
    textures(); json_files(); worldgen(); print("moon ok")
