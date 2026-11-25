"""
Concept art for the tropics and hardiness zones: the Palm Oasis (a desert variation) and the Tropical Coast (a savanna
variation), with their trees (coconut palm, date palm, Joshua tree, saguaro), plants (pineapple, banana, agave, aloe,
hibiscus) and animals (flamingo, iguana, toucan, roadrunner, jackrabbit). Box models in vanilla textures, tinted.
  python3 tools/tropics_concept.py <vanilla textures dir>  ->  docs/tropics_concept.png
"""
import os, sys, math
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
from airship_concept import Big, T

R.YAW = math.radians(145)
B = 16
LIME = (124, 206, 66); FROND = (92, 168, 58); DATE_FROND = (110, 150, 74)

def bk(m, tex, x, y, z, w, h, d, part=None):
    """A box in blocks, y up from the ground."""
    m.box(tex, (x * B, -(y + h) * B, z * B), (w * B, h * B, d * B), part)

def px(m, tex, x, y, z, w, h, d, part=None):
    """A box in pixels, y up from the ground."""
    m.box(tex, (x, -(y + h), z), (w, h, d), part)

def ground(m, tex, half=3, depth=1):
    for x in range(-half, half):
        for z in range(-half, half): bk(m, tex, x, -depth, z, 1, depth, 1)

def frond(m, x, y, z, ang, droop, length, tint):
    """A palm frond: a run of leaf boxes arcing out and down from the crown."""
    leaf = T("jungle_leaves", tint)
    for k in range(length):
        t = k / length
        fx = x + math.cos(ang) * (k + 1) * 0.9; fz = z + math.sin(ang) * (k + 1) * 0.9
        fy = y - droop * t * t * length * 0.5
        bk(m, leaf, fx - 0.5, fy, fz - 0.5, 1, 0.5, 1)
        if 1 < k < length - 1:                                  # the leaflets either side
            sx, sz = -math.sin(ang) * 0.7, math.cos(ang) * 0.7
            bk(m, leaf, fx + sx - 0.4, fy - 0.3, fz + sz - 0.4, 0.8, 0.4, 0.8); bk(m, leaf, fx - sx - 0.4, fy - 0.3, fz - sz - 0.4, 0.8, 0.4, 0.8)

# ------------------------------------------------------------------ trees
def coconut_palm():
    m = Big(); ground(m, T("sand"), 4)
    log = T("jungle_log", (230, 210, 180)); x = z = 0.0
    for k in range(9):                                           # a leaning, curving trunk
        x += 0.32 + k * 0.02
        bk(m, log, x - 0.5, k, z - 0.5, 1, 1, 1)
    top = 9
    for a in range(7):
        frond(m, x, top, z, a * 2 * math.pi / 7, 1.4, 5, FROND)
    for dx, dz in ((0.6, 0), (-0.3, 0.6), (-0.3, -0.6)):          # coconuts under the crown
        bk(m, T("brown_terracotta"), x + dx - 0.3, top - 0.9, z + dz - 0.3, 0.6, 0.6, 0.6)
    return m

def date_palm():
    m = Big(); ground(m, T("sand"), 4)
    log = T("stripped_jungle_log", (200, 170, 130))
    for k in range(10): bk(m, log, -0.5, k, -0.5, 1, 1, 1)
    for k in range(0, 10, 2): bk(m, T("jungle_log", (170, 140, 100)), -0.6, k, -0.6, 1.2, 0.3, 1.2)   # the stubs of old fronds
    for a in range(8): frond(m, 0, 10, 0, a * 2 * math.pi / 8 + 0.3, 1.0, 5, DATE_FROND)
    for a in range(4):                                           # hanging clusters of dates
        ang = a * math.pi / 2 + 0.6; cx, cz = math.cos(ang) * 0.9, math.sin(ang) * 0.9
        bk(m, T("orange_terracotta"), cx - 0.3, 8.6, cz - 0.3, 0.6, 1.2, 0.6)
    return m

def joshua_tree():
    m = Big(); ground(m, T("sand"), 4)
    bark = T("dark_oak_log", (180, 160, 130)); tuft = T("spruce_leaves", (150, 190, 110))
    for k in range(4): bk(m, bark, -0.5, k, -0.5, 1, 1, 1)
    def arm(x, y, z, dx, dz, n):
        for k in range(n):
            x += dx; z += dz; bk(m, bark, x - 0.4, y + k * 0.8, z - 0.4, 0.8, 1, 0.8)
        bk(m, tuft, x - 0.8, y + n * 0.8, z - 0.8, 1.6, 1.4, 1.6)                    # the spiky tuft at each tip
    arm(0, 4, 0, 0.5, 0, 3); arm(0, 4, 0, -0.5, 0.2, 2); arm(0, 3.5, 0, 0, 0.55, 3); arm(0, 4.5, 0, -0.2, -0.5, 3)
    return m

def saguaro():
    m = Big(); ground(m, T("sand"), 4)
    c = T("cactus_side")
    for k in range(9): bk(m, c, -0.5, k, -0.5, 1, 1, 1)
    bk(m, T("cactus_top"), -0.5, 9, -0.5, 1, 0.1, 1)
    for sx, y0, h in ((1, 3, 4), (-1, 4, 3)):                     # the two arms, out and up
        bk(m, c, 0.5 if sx > 0 else -1.5, y0, -0.5, 1, 1, 1)
        for k in range(h): bk(m, c, 1.5 if sx > 0 else -2.5, y0 + k, -0.5, 1, 1, 1)
    return m

# ------------------------------------------------------------------ plants
def pineapple():
    m = Big(); ground(m, T("grass_block_top", LIME), 1)
    px(m, T("yellow_terracotta"), -3, 1, -3, 6, 8, 6); px(m, T("orange_terracotta"), -2, 2, -3.2, 4, 6, 0.3)
    for k in range(5):
        a = k * 2 * math.pi / 5
        p = m.part([0, -9, 0], (0.45, a, 0))
        m.box(T("jungle_leaves", (90, 150, 70)), (-1, -7, -1), (2, 7, 2), p)
    for k in range(6):                                           # the low leaves round its base
        a = k * math.pi / 3; p = m.part([0, -1, 0], (1.1, a, 0))
        m.box(T("jungle_leaves", (80, 140, 64)), (-1, -9, -1), (2, 9, 1), p)
    return m

def banana():
    m = Big(); ground(m, T("grass_block_top", LIME), 1)
    px(m, T("jungle_log", (170, 190, 120)), -2, 0, -2, 4, 30, 4)
    for k in range(6):
        a = k * math.pi / 3; p = m.part([0, -30, 0], (0.9, a, 0))
        m.box(T("jungle_leaves", (110, 190, 80)), (-4, -22, -0.5), (8, 22, 1), p)
    for k in range(3): px(m, T("yellow_wool"), 2, 16 + k * 3, -2, 5, 3, 4)
    return m

def agave():
    m = Big(); ground(m, T("sand"), 1)
    for k in range(10):
        a = k * 2 * math.pi / 10; tilt = 0.5 + (k % 2) * 0.35
        p = m.part([0, -1, 0], (tilt, a, 0)); m.box(T("white_wool", (110, 160, 140)), (-1.5, -16, -1), (3, 16, 2), p)
    return m

def aloe():
    m = Big(); ground(m, T("sand"), 1)
    for k in range(8):
        a = k * 2 * math.pi / 8; p = m.part([0, -1, 0], (0.45 + (k % 3) * 0.15, a, 0))
        m.box(T("lime_terracotta", (190, 230, 170)), (-1, -12, -1), (2, 12, 2), p)
    px(m, T("orange_wool"), -0.5, 1, -0.5, 1, 18, 1); px(m, T("orange_wool"), -1.5, 17, -1.5, 3, 4, 3)
    return m

def hibiscus():
    m = Big(); ground(m, T("grass_block_top", LIME), 1)
    px(m, T("oak_leaves", (70, 140, 60)), -6, 0, -6, 12, 10, 12)
    for (x, y, z) in ((-6.5, 6, -2), (2, 9, -6.5), (-2, 10, 3), (5.7, 4, 2)):
        px(m, T("red_wool"), x, y, z, 3, 3, 3); px(m, T("yellow_wool"), x + 1, y + 1, z + (-0.3 if z < 0 else 2.3), 1, 1, 1)
    return m

# ------------------------------------------------------------------ animals (in pixels, facing east)
def flamingo():
    m = Big(); pink, dark = T("pink_wool"), T("black_wool")
    for lx in (-1.5, 1): px(m, T("pink_terracotta"), lx, 0, 0, 1, 14, 1)
    px(m, pink, -4, 14, -2.5, 9, 6, 5)
    px(m, pink, 4, 18, -0.5, 1.5, 9, 1.5); px(m, pink, 4, 26, -1.5, 4, 3, 3)
    px(m, T("white_wool"), 8, 26.5, -1, 2, 2, 2); px(m, dark, 9.5, 25.5, -0.6, 1.2, 1.5, 1.2)
    return m

def iguana():
    m = Big(); g = T("lime_wool", (190, 230, 150))
    px(m, g, -6, 1, -2, 12, 4, 4); px(m, g, 6, 2, -1.5, 4, 3, 3)
    for k in range(4): px(m, g, -6 - (k + 1) * 3, 1.5, -0.5 - (0.3 if k else 0), 3, 1.5 - k * 0.2, 1)
    for k in range(5): px(m, T("green_terracotta"), -4 + k * 2, 5, -0.5, 1, 1.5, 1)       # the spiny crest
    for (x, z) in ((-4, -3), (-4, 2), (3, -3), (3, 2)): px(m, g, x, 0, z, 1.5, 1.5, 1)
    px(m, T("orange_terracotta"), 8.5, 1, -1, 1.5, 1.5, 2)                                 # its dewlap
    return m

def toucan():
    m = Big(); bl = T("black_wool")
    px(m, bl, -3, 4, -2, 6, 7, 4); px(m, T("white_wool"), 2.5, 7, -1.5, 1, 3, 3)
    px(m, bl, 1, 10, -1.5, 3, 3, 3); px(m, T("orange_wool"), 4, 10.5, -1, 5, 2, 2); px(m, T("red_wool"), 8, 10.5, -1, 1, 1.5, 2)
    px(m, bl, -6, 4.5, -1, 3, 1, 2)
    for z in (-1, 0.5): px(m, T("blue_wool"), 0, 0, z, 1, 4, 1)
    return m

def roadrunner():
    m = Big(); br = T("white_wool", (176, 150, 116))
    for lx in (-0.5, 1): px(m, T("blue_terracotta"), lx, 0, -0.5 + (lx > 0) * 0.5, 1, 6, 1)
    px(m, br, -3, 6, -2, 7, 4, 4); px(m, T("brown_terracotta"), -10, 7.5, -1, 7, 1.5, 2)   # and its long tail
    px(m, br, 3, 9, -1.5, 3, 3, 3); px(m, T("black_wool"), 3.5, 12, -0.5, 2, 2, 1)         # its crest
    px(m, T("gray_terracotta"), 6, 9.8, -0.5, 3, 1, 1)
    return m

def jackrabbit():
    m = Big(); fur = T("white_wool", (206, 178, 136))
    px(m, fur, -4, 2, -2, 8, 5, 4); px(m, fur, 3, 5, -1.5, 4, 4, 3)
    for z in (-1.4, 0.4): px(m, fur, 4, 9, z, 1, 7, 1)                                     # those ears
    px(m, T("black_wool"), 4.2, 15, -1.4, 0.6, 1, 2.8)
    for (x, z) in ((-4, -2.5), (-4, 1.5), (2, -2), (2, 1)): px(m, fur, x, 0, z, 2, 2, 1)
    px(m, T("white_wool"), -5, 4, -1, 1, 2, 2)
    return m

SECTIONS = [("Trees", 0.42, [("Coconut palm", "leaning over the Tropical Coast", coconut_palm), ("Date palm", "round the Palm Oasis pools", date_palm),
                              ("Joshua tree", "shaggy, with spiky tufts", joshua_tree), ("Saguaro", "a giant cactus with arms", saguaro)]),
            ("Plants", 1.6, [("Pineapple", "tropical: heaters all year", pineapple), ("Banana", "tropical: heaters all year", banana),
                             ("Agave", "warm: greenhouse in summer", agave), ("Aloe", "warm: soothes burns", aloe), ("Hibiscus", "tropical: a flowering shrub", hibiscus)]),
            ("Animals", 4.0, [("Flamingo", "wades the lagoons", flamingo), ("Green iguana", "basks on warm rocks", iguana), ("Toucan", "in the palms", toucan),
                              ("Roadrunner", "darts across the oasis", roadrunner), ("Jackrabbit", "black-tailed, big-eared", jackrabbit)])]

def biome_swatch(grass, label):
    m = Big()
    for x in range(-4, 4):
        for z in range(-2, 2): bk(m, T("grass_block_top", grass) if grass else T("sand"), x, -1, z, 1, 1, 1)
    return m

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    rows = []
    for title, sc, items in SECTIONS:
        rows.append((title, [(n, s, f().draw(sc)) for n, s, f in items]))
    cw = max(im.width for _, it in rows for _, _, im in it) + 50
    W = 5 * cw + 60
    heights = [max(im.height for _, _, im in it) + 130 for _, it in rows]
    H = 170 + sum(heights) + 40
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "The tropics: concept art", font=R.font(46, True), fill=INK)
    d.text((42, 96), "The Palm Oasis (a desert variation) and the Tropical Coast (a savanna variation, lime-green grass). Warm and tropical plants grow outdoors only here; elsewhere, in a heated greenhouse.", font=R.font(20), fill=SUB)
    y = 160
    for (title, items), h in zip(rows, heights):
        d.text((40, y), title, font=R.font(30, True), fill=INK)
        for k, (n, s, im) in enumerate(items):
            x = 30 + k * cw; b = y + h - 60
            c.paste(im, (x + (cw - im.width) // 2, b - im.height), im)
            d.text((x + cw // 2, b + 18), n, font=R.font(22, True), fill=INK, anchor="mm")
            d.text((x + cw // 2, b + 42), s, font=R.font(16), fill=SUB, anchor="mm")
        y += h
    c = c.resize((c.width * 2 // 3, c.height * 2 // 3), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "tropics_concept.png"))
