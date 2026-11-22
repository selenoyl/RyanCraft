"""
Block textures for the eastern US trees: bark log (side + top), stripped/debarked log (side + top), planks, leaves.
  python3 tools/tree_textures.py
Writes src/main/resources/assets/ryancraft/textures/block/<species>_{log,log_top,planks,leaves}.png and stripped_<species>_{log,log_top}.png
and a preview sheet at docs/tree_textures.png.
"""
import math, os, random
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "src/main/resources/assets/ryancraft/textures/block")

def hexc(h): h = h.lstrip('#'); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c)
def jit(r, c, a): d = r.randint(-a, a); return tuple(max(0, min(255, v + d)) for v in c)

# species: bark colour, bark pattern, inner (stripped) wood, leaf colour (greyscale-tinted in game later), bloom
TREES = {
    "white_oak":        ("#9a958a", "furrows", "#c8a978", "#5f8f3e", None),
    "red_oak":          ("#5a5048", "stripes", "#c49870", "#3f7a2e", None),
    "sugar_maple":      ("#7a7066", "plates", "#dcc8a0", "#3e7a34", None),
    "red_maple":        ("#a8a39a", "smooth", "#d2b892", "#4a8436", None),
    "tulip_poplar":     ("#7e786c", "furrows", "#c8c08a", "#6aa642", None),
    "shagbark_hickory": ("#8c8478", "shaggy", "#c8a882", "#557f34", None),
    "black_walnut":     ("#3e3630", "furrows", "#6a4a32", "#86a846", None),
    "american_beech":   ("#b4b2ac", "smooth", "#d6b894", "#5c8c3a", None),
    "sycamore":         ("#a89a80", "mottled", "#d8c4a0", "#6a9a44", None),
    "eastern_redbud":   ("#4a3a32", "plates", "#b88a6a", "#5c8c3a", "#d0508e"),
    "flowering_dogwood":("#6e6054", "blocky", "#d8c8aa", "#4e8a3a", "#f2eee4"),
    "eastern_red_cedar":("#8a4a32", "stringy", "#b8644a", "#2e5a40", None),
    "black_cherry":     ("#2e2622", "flaky", "#a8604a", "#3c6e2c", None),
    "sassafras":        ("#7a6250", "furrows", "#c89a6a", "#6aa040", None),
    "ohio_buckeye":     ("#8a847a", "plates", "#e0d4b8", "#4c7e30", None),
}

def wander(r, start, p):
    """Column of a wavy crack for y = 0..15 that comes back to where it started, so the texture tiles vertically."""
    n = sum(r.random() < p for _ in range(8))                 # moves each way
    steps = [1] * n + [-1] * n + [0] * (16 - 2 * n); r.shuffle(steps)
    xs, x = [], start
    for st in steps: xs.append(x % 16); x += st
    return xs

def bark_side(seed, base, pattern):
    r = random.Random(seed); b = hexc(base); img = bark_base(r, base); px = img.load()
    if pattern in ("furrows", "stripes"):          # vertical dark furrows, wavy
        cols = [1, 5, 9, 13] if pattern == "furrows" else [2, 7, 12]
        for c in cols:
            for y, x in enumerate(wander(r, c, 0.25)):
                px[x, y] = jit(r, shade(b, .62), 5) + (255,)
                if pattern == "stripes" and r.random() < .5: px[(x + 1) % 16, y] = jit(r, shade(b, 1.18), 5) + (255,)
    elif pattern == "shaggy":                       # long loose vertical strips of uneven width, a few curling loose
        x = r.randrange(3)
        while x < 16:
            wdt = r.choice((2, 3, 3, 4, 5))
            ys = wander(r, x, .18)                   # the gap wanders a little and wraps
            for y in range(16):
                gx = ys[y]
                px[gx, y] = jit(r, shade(b, .5), 6) + (255,)
                if r.random() < .75: px[(gx + 1) % 16, y] = jit(r, shade(b, 1.18), 6) + (255,)
                for k in range(2, wdt):
                    if r.random() < .12: px[(gx + k) % 16, y] = jit(r, shade(b, .84), 5) + (255,)
            if r.random() < .6:                      # a curling strip end: short light lip over a shadow, partial width
                y = r.randrange(16); k0 = 1 + r.randrange(max(1, wdt - 1)); ln = r.choice((1, 2))
                for k in range(k0, min(wdt, k0 + ln)):
                    px[(ys[y] + k) % 16, y] = jit(r, shade(b, 1.28), 5) + (255,)
                    px[(ys[y] + k) % 16, (y + 1) % 16] = jit(r, shade(b, .48), 5) + (255,)
            x += wdt
    elif pattern == "plates":                       # irregular plates: wandering vertical cracks joined by short breaks
        for c in (2, 7, 12):
            for y, x in enumerate(wander(r, c, .3)):
                px[x, y] = jit(r, shade(b, .62), 4) + (255,)
        for _ in range(7):
            x, y = r.randrange(16), r.randrange(16)
            for dx in range(r.randint(2, 3)): px[(x + dx) % 16, y] = jit(r, shade(b, .66), 4) + (255,)
            px[x, (y + 1) % 16] = jit(r, shade(b, 1.12), 4) + (255,)
    elif pattern == "smooth":                       # smooth with a few faint horizontal marks
        for _ in range(6):
            x, y = r.randrange(16), r.randrange(16)
            for dx in range(r.randint(2, 4)): px[min(15, x + dx), y] = jit(r, shade(b, .85), 4) + (255,)
    elif pattern == "mottled":                      # sycamore: patches of white, tan and green-grey
        for _ in range(9):
            cx, cy, rr = r.randrange(16), r.randrange(16), r.uniform(1.5, 3.2)
            col = r.choice([hexc("#ece8de"), hexc("#c8bca0"), hexc("#8a9478")])
            for x in range(16):
                for y in range(16):
                    if (x - cx) ** 2 + (y - cy) ** 2 < rr * rr: px[x, y] = jit(r, col, 6) + (255,)
    elif pattern == "flaky":                        # cherry: dark curling plates with lighter edges
        for _ in range(14):
            x, y = r.randrange(16), r.randrange(16)
            for dx in range(r.randint(2, 4)):
                px[min(15, x + dx), y] = jit(r, shade(b, 1.6), 6) + (255,)
                px[min(15, x + dx), min(15, y + 1)] = jit(r, shade(b, .6), 4) + (255,)
    elif pattern == "stringy":                      # red cedar: long fibrous strips that peel, some weathered grey
        x = 0; strips = 0
        while x < 16:
            w = r.choice((1, 2, 2, 3))
            weathered = r.random() < 0.35 or strips == 2          # always at least one weathered strip
            strips += 1
            strip = hexc("#8e7c72") if weathered else shade(b, r.choice((0.95, 1.05, 1.15)))
            for y in range(16):
                for dx in range(w):
                    if x + dx < 16:
                        c = strip
                        if r.random() < 0.08: c = shade(b, 1.25)              # fibres catching the light
                        px[x + dx, y] = jit(r, c, 5) + (255,)
            # a dark crevice that wanders down the strip's edge
            cx = min(15, x + w)
            for y in range(16):
                if r.random() < 0.2: cx = max(0, min(15, cx + r.choice((-1, 1))))
                if cx < 16: px[cx, y] = jit(r, shade(b, 0.5), 4) + (255,)
            # peeling: short lighter flaps lifting off the strip
            if r.random() < 0.6:
                y0 = r.randrange(12)
                for y in range(y0, y0 + r.randint(2, 4)):
                    px[min(15, x), min(15, y)] = jit(r, shade(b, 1.35), 5) + (255,)
            x += w + 1
    elif pattern == "blocky":                       # dogwood: "alligator hide" of small, uneven, squarish plates
        y = 0
        while y < 16:
            h = r.choice((2, 3, 3, 4))
            x = -r.randint(0, 3)
            while x < 16:
                w = r.choice((2, 3, 3, 4))
                tone = shade(b, r.choice((0.95, 1.0, 1.08, 1.15, 1.2)))
                crack_right, crack_bottom = r.random() < 0.75, r.random() < 0.8   # broken, not a perfect grid
                for dy in range(h):
                    for dx in range(w):
                        X, Y = x + dx, y + dy
                        if 0 <= X < 16 and Y < 16:
                            c = tone
                            if dy == 0 and dx < 2: c = shade(tone, 1.12)                  # worn top edge
                            if (dx == w - 1 and crack_right) or (dy == h - 1 and crack_bottom):
                                c = shade(b, r.choice((0.62, 0.68)))
                            px[X, Y] = jit(r, c, 5) + (255,)
                x += w
            y += h
    return img

# Five-shade wood palettes, structured like the vanilla wood textures (same layout rules, our own pixels).
def palette(wood):
    w = hexc(wood)
    return {"hi": shade(w, 1.08), "lt": shade(w, 1.0), "md": shade(w, .9), "mdk": shade(w, .8), "dk": shade(w, .66), "vdk": shade(w, .54)}

# Ring brightness relative to the first light ring, and the few discrete shades each ring uses.
# Measured as ratios from the vanilla log tops (all of which share these proportions); the pixels are our own.
RINGS = {
    1: [0.97, 1.00, 1.00, 1.04],          # light, slight variation
    2: [0.76, 0.83, 0.83, 0.90],          # dark, speckled
    3: [1.00, 1.02, 1.02, 1.05],          # light, nearly flat
    4: [0.96],                            # flat
    5: [0.75, 0.82, 0.82, 0.89],          # dark, speckled
    6: [0.96],                            # flat
    7: [0.82, 0.85, 0.85, 0.88],          # centre
}

def log_top(seed, bark, wood, stripped=False):
    """Square concentric rings inside a 1px border: bark on a log, darker wood (72%) on a stripped log."""
    r = random.Random(seed); w = hexc(wood); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        for y in range(16):
            k = min(x, y, 15 - x, 15 - y)
            if k == 0:
                c = jit(r, hexc(bark), 8) if not stripped else shade(w, r.choice((0.69, 0.72, 0.72, 0.75)))
            else:
                c = shade(w, r.choice(RINGS[k]))
            px[x, y] = c + (255,)
    return img

def stripped_side(seed, wood):
    """Soft vertical streaks: each column wanders between light and mid tones, with a few darker grain runs."""
    r = random.Random(seed); p = palette(wood); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        tone = r.choice(["lt", "lt", "md", "hi"])
        for y in range(16):
            if r.random() < 0.18: tone = r.choice(["lt", "md", "hi", "lt"])
            px[x, y] = jit(r, p[tone], 3) + (255,)
    for _ in range(5):                                                         # darker grain runs
        x, y0, n = r.randrange(16), r.randrange(16), r.randint(3, 7)
        for y in range(y0, min(16, y0 + n)): px[x, y] = jit(r, p["mdk"], 3) + (255,)
    return img

# Showcase woods: species whose planks get a richer, more exotic colour than their stripped wood, as a reward for builders.
PLANK_COLOR = {"eastern_red_cedar": "#ae4646",     # aromatic cedar: rosy red-violet heartwood
               "black_walnut": "#5c3a2c",          # deep chocolate
               "black_cherry": "#a8482e",          # rich cherry red
               "tulip_poplar": "#94a266"}          # poplar's green-streaked heartwood

def planks(seed, wood):
    """The base game's plank template (classic / Programmer Art oak planks) recoloured to the species' wood:
    every pixel keeps its brightness relative to the template's average, so boards, seams and grain match vanilla."""
    t = Image.open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "template_oak_planks.png")).convert("RGB")
    lum = [0.299 * r + 0.587 * g + 0.114 * b for r, g, b in list(t.getdata())]
    mean = sum(lum) / len(lum); w = hexc(wood)
    img = Image.new("RGBA", (16, 16))
    img.putdata([shade(w, 0.86 * (l / mean) ** 1.35) + (255,) for l in lum])
    return img

def bark_base(r, base):
    """Vanilla-style bark base: vertical streaks with darker crevices, before the species pattern goes on top."""
    b = hexc(base); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        k = r.choice((.9, 1.0, 1.0, 1.08))
        for y in range(16):
            if r.random() < .15: k = r.choice((.85, 1.0, 1.08))
            px[x, y] = jit(r, shade(b, k), 4) + (255,)
    return img

def leaves(seed, leaf, bloom=None, bloom_only=False):
    r = random.Random(seed); l = hexc(leaf)
    # see-through pixels keep a dark leaf colour, which is what shows with Fast leaves (like vanilla's textures)
    img = Image.new("RGBA", (16, 16), shade(l, .42) + (0,)); px = img.load()
    for x in range(16):
        for y in range(16):
            if r.random() < 0.12: px[x, y] = jit(r, shade(l, .42), 4) + (0,); continue                     # holes, like vanilla fancy leaves
            c = l
            if bloom and r.random() < 0.7: c = hexc(bloom)
            elif bloom_only: px[x, y] = jit(r, shade(l, .5), 4) + (0,); continue                          # pure blossom: the gaps are see-through
            px[x, y] = jit(r, shade(c, r.choice((.8, .95, 1.0, 1.1))), 6) + (255,)
    return img

def font(size, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, size)
    return ImageFont.load_default()

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    cols = ["Bark", "Log top", "Stripped", "Stripped top", "Planks", "Leaves"]
    T = 64; rowh = T + 22
    sheet = Image.new("RGB", (260 + len(cols) * (T + 16), 110 + len(TREES) * rowh), (243, 239, 230))
    d = ImageDraw.Draw(sheet)
    d.text((24, 20), "Tree block textures (16x16, shown at 4x)", font=font(30, True), fill=(46, 42, 36))
    for i, c in enumerate(cols): d.text((260 + i * (T + 16) + T // 2, 82), c, font=font(15, True), fill=(90, 82, 70), anchor="mm")
    for k, (name, (bark, pattern, wood, leaf, bloom)) in enumerate(TREES.items()):
        s = 7000 + k * 10
        texs = {"log": bark_side(s, bark, pattern), "log_top": log_top(s + 1, bark, wood),
                "STRIP_log": stripped_side(s + 2, wood), "STRIP_log_top": log_top(s + 3, bark, wood, True),
                "planks": planks(s + 4, PLANK_COLOR.get(name, wood)), "leaves": leaves(s + 5, leaf, bloom, bloom_only=(name == "flowering_dogwood"))}
        for suffix, im in texs.items():
            fn = f"stripped_{name}_{suffix[6:]}" if suffix.startswith("STRIP_") else f"{name}_{suffix}"
            im.save(os.path.join(OUT, fn + ".png"))
        y = 100 + k * rowh
        d.text((24, y + T // 2), name.replace("_", " ").title(), font=font(18, True), fill=(46, 42, 36), anchor="lm")
        for i, suffix in enumerate(["log", "log_top", "STRIP_log", "STRIP_log_top", "planks", "leaves"]):
            big = texs[suffix].resize((T, T), Image.NEAREST)
            bg = Image.new("RGBA", (T, T), (205, 198, 184, 255)); bg.alpha_composite(big)
            sheet.paste(bg.convert("RGB"), (260 + i * (T + 16), y))
    sheet.save(os.path.join(ROOT, "docs", "tree_textures.png")); print("ok")
