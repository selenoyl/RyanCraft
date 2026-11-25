"""
Texture studies for the tropical trees (coconut palm, date palm, Joshua tree, saguaro), built the way our other woods are:
the classic log grain gradient-mapped to each species' colours with its own bark drawn over it (bark.py), and the
Programmer Art plank and leaf templates recoloured (tree_textures.py). Each gets log, log top, stripped log, planks,
leaves and one extra block, shown flat and as a little build.
  python3 tools/tropic_wood_concept.py <vanilla textures dir>  ->  docs/tropic_wood_concept.png
"""
import os, sys, math, random
import numpy as np
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
import bark as BK
import tree_textures as TT
from airship_concept import Big

R.YAW = math.radians(-35)

def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c)

def put(img, x, y, c):
    img.putpixel((x % 16, y % 16), tuple(c) + (255,))

# ------------------------------------------------------------------ coconut palm: pale grey-tan, ringed with old leaf scars
def coconut_log():
    dark, mid, light = (92, 84, 70), (150, 140, 120), (196, 186, 164)
    img = BK.mapped("jungle", dark, mid, light); r = random.Random(3)
    for ring in range(4):                                       # a ring scar every four rows, wobbling a little
        y0 = ring * 4
        for x in range(16):
            dy = 1 if (x + ring * 5) % 7 == 0 else 0
            put(img, x, y0 + dy, shade(dark, 0.95 + r.random() * 0.1)); put(img, x, y0 + dy + 1, shade(light, 0.98 + r.random() * 0.06))
    return img
def coconut_planks():
    img = TT.planks(11, "#c9ab7c"); r = random.Random(4)
    for _ in range(14):                                         # palm wood's dark flecks (its fibre bundles)
        x, y = r.randrange(16), r.randrange(16)
        if y % 4 != 3: put(img, x, y, (120, 94, 62))
    return img
def coconut_block():
    """A coconut, block-sized: coarse brown husk fibres."""
    r = random.Random(5); img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            k = 0.8 + 0.3 * ((x * 3 + y * 7 + r.randrange(5)) % 6) / 5
            c = shade((126, 84, 48), k)
            if (x + y * 2) % 5 == 0: c = shade((86, 56, 32), 1.0)
            put(img, x, y, c)
    for x, y in ((4, 5), (8, 4), (6, 9)): put(img, x, y, (52, 34, 22))      # the coconut's three "eyes"
    return img

# ------------------------------------------------------------------ date palm: covered in "boots", the cut bases of old fronds
def date_log():
    """Option A, the boots: staggered rows of rounded frond bases, each a cup with a lit rim, set in dark fibre and
    interlocking with the row below, like rough shingles."""
    dark, mid, light = (52, 36, 24), (116, 84, 54), (170, 132, 90)
    img = Image.new("RGBA", (16, 16)); r = random.Random(12)
    for y in range(16):
        for x in range(16): put(img, x, y, shade((74, 52, 34), 0.9 + r.random() * 0.2))     # the fibre between
    BOOT = [".LLLL.",
            "LhhhhL",
            "mmmmmm",
            "mmmmmm",
            ".mmmm.",
            "..DD.."]
    pal = {"L": light, "h": shade(light, 0.9), "m": mid, "D": dark}
    for row in range(4):
        y0 = row * 4; off = 0 if row % 2 == 0 else 4
        for b in range(2):
            x0 = off + b * 8
            for dy, line in enumerate(BOOT):
                for dx, ch in enumerate(line):
                    if ch in pal: put(img, x0 + dx + 1, y0 + dy, shade(pal[ch], 0.94 + r.random() * 0.1))
    return img
def date_log_b():
    """Option B, the woven fibre: a criss-cross of coarse brown fibre over the trunk, with the occasional stub."""
    dark, mid, light = (62, 44, 30), (124, 92, 62), (170, 134, 96)
    img = BK.mapped("oak", dark, mid, light); r = random.Random(13)
    for y in range(16):
        for x in range(16):
            if (x + 2 * y) % 6 == 0: put(img, x, y, shade(dark, 1.0 + r.random() * 0.1))
            elif (x - 2 * y) % 6 == 0: put(img, x, y, shade(light, 0.95 + r.random() * 0.08))
    for x, y in ((3, 2), (11, 6), (6, 11), (14, 13)):            # stubs
        put(img, x, y, light); put(img, x + 1, y, light); put(img, x, y + 1, dark); put(img, x + 1, y + 1, dark)
    return img
def date_planks(): return TT.planks(21, "#b67a46")
def date_cluster():
    """A hanging cluster of dates: plump amber and brown fruit packed along pale stalks that fan down from the top."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); r = random.Random(6)
    stalks = [2, 5, 8, 11, 14]
    for i, x in enumerate(stalks):
        for y in range(0, 16):
            sx = x + (y // 6) * (1 if i > 2 else -1 if i < 2 else 0)
            if y < 3 or y % 4 == 0: put(img, sx, y, (210, 184, 120))
            if y >= 2:                                                       # dates hug each stalk, two wide
                for dx in (-1, 0):
                    if r.random() < 0.85:
                        put(img, sx + dx, y, r.choice([(204, 120, 44), (176, 92, 34), (138, 64, 26), (222, 150, 62), (110, 48, 22)]))
    return img

# ------------------------------------------------------------------ Joshua tree: shaggy, with dead spines hanging in grey-tan strips
def joshua_log():
    dark, mid, light = (70, 62, 52), (124, 112, 94), (168, 156, 136)
    img = BK.mapped("oak", dark, mid, light); r = random.Random(7)
    for x in range(0, 16, 2):
        y0 = r.randrange(16)
        for k in range(r.randint(5, 9)):                         # long hanging strips of old leaves
            put(img, x + (k // 4), y0 + k, light if k % 3 else (196, 186, 150))
        put(img, x + 1, y0 + 2, dark)
    return img
def joshua_planks(): return TT.planks(31, "#bcac8a")
def joshua_leaves(): return frond_leaves(33, "#93ad5c", conifer=True, holes=0.2)

# ------------------------------------------------------------------ saguaro: dusty olive ribs with clusters of spines; its "wood" is the ribs
SAG = {"ridge": (126, 158, 92), "lit": (104, 140, 76), "mid": (84, 118, 62), "groove": (54, 82, 44)}
def saguaro_block():
    """Ribs every four pixels: a lit ridge, two mid tones and a shadowed groove, faintly mottled. Areoles (the little
    felt pads spines grow from) sit on the ridges, staggered rib to rib, each a tan dot with a pale spine or two."""
    img = Image.new("RGBA", (16, 16)); r = random.Random(8)
    tone = ["ridge", "lit", "mid", "groove"]
    for x in range(16):
        for y in range(16): put(img, x, y, shade(SAG[tone[x % 4]], 0.95 + r.random() * 0.08))
    for rib in range(4):
        x = rib * 4
        for y in range(rib % 2 * 2, 16, 4):                       # staggered: neighbouring ribs offset by two rows
            put(img, x, y, (196, 176, 128))                       # the areole
            put(img, x + 1, y - 1, (236, 230, 204))               # a spine, pointing up and out
            if (y + rib) % 8 == 0: put(img, x - 1 if x else 15, y + 1, (226, 218, 188))
    return img
def saguaro_top():
    """The crown: the ribs gathering to a rounded top, with a felted tip of areoles in the middle."""
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5); a = math.atan2(y - 7.5, x - 7.5)
            seg = (a + math.pi) / (2 * math.pi) * 16
            k = seg % 4
            c = SAG["ridge"] if k < 1 else SAG["lit"] if k < 2 else SAG["mid"] if k < 3 else SAG["groove"]
            if d < 3.2: c = (148, 150, 100)                       # the felted crown
            if d < 1.6: c = (176, 164, 116)
            put(img, x, y, c)
    for x, y in ((6, 6), (9, 6), (7, 9), (5, 8), (10, 9)): put(img, x, y, (222, 210, 170))
    return img
def saguaro_ribs():
    """Saguaro rib planks: the cactus's woody skeleton, grey and weathered, in long slats with gaps and the odd knot."""
    img = Image.new("RGBA", (16, 16)); r = random.Random(9)
    for x in range(16):
        gap = x % 4 == 3
        for y in range(16):
            c = (62, 54, 44) if gap else shade((170, 156, 130), 0.86 + 0.18 * r.random())
            if not gap and x % 4 == 0: c = shade(c, 1.08)                       # each slat's lit edge
            put(img, x, y, c)
    for x, y in ((1, 3), (5, 10), (9, 5), (13, 12)):                            # knots where the spines once grew
        put(img, x, y, (112, 98, 78)); put(img, x + 1, y, (96, 84, 66))
    return img

def frond_leaves(seed, colour, conifer=False, holes=0.16):
    """Fuller tropical leaves: the Programmer Art leaf template recoloured (tree_textures.leaves), with most of its gaps
    filled (about a sixth left see-through instead of two fifths, so canopies don't vanish), then herringbone streaks of
    leaflets drawn over it, lit along one side of each, so the fronds read as fronds."""
    img = TT.leaves(seed, colour, conifer=conifer); r = random.Random(seed)
    base = tuple(int(colour.lstrip("#")[i:i + 2], 16) for i in (0, 2, 4))
    px_ = img.load(); open_ = [(x, y) for y in range(16) for x in range(16) if px_[x, y][3] == 0]
    r.shuffle(open_)
    keep = int(256 * holes)
    for x, y in open_[keep:]: px_[x, y] = shade(base, r.choice((0.62, 0.72, 0.8))) + (255,)
    for k in range(4):                                            # leaflets: short diagonal strokes either side of a midrib
        x0 = (k * 4 + 1) % 16
        for y in range(16):
            if px_[x0 % 16, y][3] == 0: continue
            if y % 2 == 0:
                put(img, x0 + 1, y, shade(base, 1.18)); put(img, x0 - 1, y + 1, shade(base, 1.05))
            put(img, x0, y, shade(base, 0.7))                     # the midrib, darker
    return img

SPECIES = [
    ("Coconut palm", "pale and ringed with leaf scars; flecked pale planks",
     [("Log", coconut_log()), ("Log top", TT.log_top(41, "#968c78", "#c9ab7c")), ("Stripped", TT.stripped_side(42, "#c9ab7c")),
      ("Planks", coconut_planks()), ("Fronds", frond_leaves(43, "#5aa83a")), ("Coconut", coconut_block())]),
    ("Date palm", "covered in boots, the cut bases of old fronds; honey-brown planks",
     [("Log", date_log()), ("Log top", TT.log_top(51, "#845e3c", "#b67a46")), ("Stripped", TT.stripped_side(52, "#b67a46")),
      ("Planks", date_planks()), ("Fronds", frond_leaves(53, "#7c9c6c")), ("Dates", date_cluster())]),
    ("Joshua tree", "shaggy with hanging dead spines; pale fibrous planks",
     [("Log", joshua_log()), ("Log top", TT.log_top(61, "#7c7060", "#bcac8a")), ("Stripped", TT.stripped_side(62, "#bcac8a")),
      ("Planks", joshua_planks()), ("Spiky tufts", joshua_leaves())]),
    ("Saguaro", "ribbed green flesh with rows of spines; its woody ribs make planks",
     [("Saguaro", saguaro_block()), ("Top", saguaro_top()), ("Rib planks", saguaro_ribs())]),
]

def preview(textures):
    """A little build: a two-block trunk, a planks block beside it and a leaf block on top."""
    m = Big(); arr = {n: np.array(t.convert("RGBA")) for n, t in textures}
    log = arr.get("Log", arr.get("Saguaro")); planks = arr.get("Planks", arr.get("Rib planks"))
    leaf = arr.get("Fronds", arr.get("Spiky tufts"))
    for k in range(2): m.box(log, (0, -(k + 1) * 16, 0), (16, 16, 16))
    m.box(planks, (20, -16, 0), (16, 16, 16))
    if leaf is not None:
        opaque = leaf.copy(); opaque[..., 3] = np.where(opaque[..., 3] > 0, 255, 0)
        m.box(opaque, (0, -48, 0), (16, 16, 16)); m.box(opaque, (-16, -40, 0), (16, 8, 16))
    return m.draw(0.36)

def sheet(out):
    BG, INK, SUB, CELL = (243, 239, 230), (46, 42, 36), (128, 118, 104), (196, 190, 178)
    S, PADX = 128, 26
    W = 60 + 6 * (S + PADX) + 380; rowh = S + 120; H = 170 + len(SPECIES) * rowh + 30
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Tropical woods: texture studies", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Built like our other woods: classic log grain recoloured with each species' own bark, and the Programmer Art plank and leaf templates.", font=R.font(20), fill=SUB)
    y = 160
    for name, sub, tex in SPECIES:
        d.text((40, y), name, font=R.font(28, True), fill=INK); d.text((40 + 14 * len(name) + 30, y + 8), sub, font=R.font(17), fill=SUB)
        for k, (label, t) in enumerate(tex):
            x = 40 + k * (S + PADX)
            d.rectangle((x, y + 44, x + S, y + 44 + S), fill=CELL)
            big = t.convert("RGBA").resize((S, S), Image.NEAREST); c.paste(big, (x, y + 44), big)
            d.text((x + S // 2, y + 56 + S), label, font=R.font(17, True), fill=INK, anchor="mt")
        pv = preview(tex)
        c.paste(pv, (W - 380 + (360 - pv.width) // 2, y + 30 + max(0, (S + 60 - pv.height) // 2)), pv)
        y += rowh
    c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "tropic_wood_concept.png"))
