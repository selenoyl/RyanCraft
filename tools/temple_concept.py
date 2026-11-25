"""
Concept art for a new jade temple on the moon (the Great Moon Rabbit's arena), three directions to choose from, built
as box models from our moon blocks (moonstone bricks, chiseled moonstone, jade) and vanilla's sea lantern:
  A. the terraced temple: a stepped ziggurat with the arena on its top,
  B. the moon gate: a round colonnade of jade-capped pillars behind a great circular jade gate,
  C. the jade pagoda: a walled courtyard arena before a pagoda hall with tiered, upturned jade roofs.
  python3 tools/temple_concept.py <vanilla textures dir>  ->  docs/temple_concept.png
"""
import os, sys, math
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
from airship_concept import Big, T, round_layer

R.YAW = math.radians(145)                 # from the south-east, looking at the fronts
BR, CH, PO, CR = "moonstone_bricks", "chiseled_moonstone_bricks", "polished_moonstone", "cracked_moonstone_bricks"
DB, JB, JD, LAMP, DUST = "deep_moonstone_bricks", "jade_bricks", "jade_block", "sea_lantern", "moon_dust"
B16 = 16

def ground(m, half=17, y=0):
    """A patch of moon dust for it to stand on."""
    for x in range(-half, half, 4):
        for z in range(-half, half, 4): m.box(T(DUST), (x * B16, y, z * B16), (4 * B16, 8, 4 * B16))

def blocks(m, tex, x0, y0, z0, w, h, d):
    """A box measured in blocks (y up from the ground, so it's drawn at -y)."""
    m.box(T(tex), (x0 * B16, -(y0 + h) * B16, z0 * B16), (w * B16, h * B16, d * B16))

def lamp_post(m, x, y, z, h=3):
    blocks(m, JB, x, y, z, 1, h, 1); blocks(m, LAMP, x, y + h, z, 1, 1, 1)

def boss(m, x, y, z):
    """A stand-in for the Great Moon Rabbit (about 4 blocks tall), for scale."""
    blocks(m, "white_wool", x - 1, y, z - 1, 2, 3, 2); blocks(m, "white_wool", x - 1, y + 3, z - 1, 2, 1.5, 2)
    for ex in (-1, 0.5): m.box(T("white_wool"), ((x + ex) * B16, -(y + 6.5) * B16, (z - 0.25) * B16), (8, 32, 8))
    blocks(m, "gold_block", x - 1, y + 4.5, z - 1, 2, 0.3, 2)

# ------------------------------------------------------------------ A. the terraced temple
def temple_a():
    m = Big(); ground(m)
    for i, (half, h, tex, trim) in enumerate(((14, 3, DB, JB), (10, 3, BR, JB), (7, 1, PO, JD))):
        y0 = sum((3, 3, 1)[:i])
        blocks(m, tex, -half, y0, -half, 2 * half, h, 2 * half)
        blocks(m, trim, -half - 0.1, y0 + h - 0.4, -half - 0.1, 2 * half + 0.2, 0.4, 2 * half + 0.2)     # a jade band round each tier
    top = 7
    for a in range(24):                                                    # the chiseled ring on the arena floor
        ang = a * math.pi / 12; blocks(m, CH, round(math.cos(ang) * 4.5) - 0.5, top - 0.05, round(math.sin(ang) * 4.5) - 0.5, 1, 0.1, 1)
    for s in range(7):                                                     # the grand stair up the south face
        blocks(m, PO, -2.5, s, 7, 5, 1, (17 - s * 1.43) - 7)
    for cx, cz in ((-7, -7), (6, -7), (-7, 6), (6, 6)):                    # jade pillars and lanterns on the top
        blocks(m, JD, cx, top, cz, 1, 5, 1); blocks(m, LAMP, cx - 0.25, top + 5, cz - 0.25, 1.5, 1, 1.5)
    for cx, cz in ((-14.6, -14.6), (13.6, -14.6), (-14.6, 13.6), (13.6, 13.6)):
        lamp_post(m, cx, 0, cz, 4)
    for x in range(-6, 7, 3):                                              # a low parapet with gaps
        blocks(m, CR if x % 2 else BR, x - 1, top, -7, 2, 1, 1)
    boss(m, 0, top, -1)
    return m

# ------------------------------------------------------------------ B. the moon gate
def temple_b():
    m = Big(); ground(m)
    for r, y, tex in ((14, 0, DB), (13, 1, PO)):                           # the round platform, in two steps
        round_layer(m, T(tex), 0, -(y + 0.5) * B16, 0, r * B16, B16)
    round_layer(m, T(CH), 0, -2.55 * B16, 0, 5 * B16, B16 * 0.2)            # the raised dais at the heart
    round_layer(m, T(PO), 0, -2.25 * B16, 0, 4.5 * B16, B16 * 0.5)
    n = 12
    for k in range(n):                                                     # the colonnade
        a = k * 2 * math.pi / n + math.pi / n
        if abs(math.sin(a) - 1) < 0.3: continue                             # leave the south open for the gate
        x, z = math.cos(a) * 11, math.sin(a) * 11
        blocks(m, CH, x - 1, 2, z - 1, 2, 8, 2); blocks(m, JD, x - 1.25, 10, z - 1.25, 2.5, 1, 2.5)
        b = (k + 1) * 2 * math.pi / n + math.pi / n
        if abs(math.sin(b) - 1) < 0.3: continue
        x2, z2 = math.cos(b) * 11, math.sin(b) * 11                         # the lintel to the next pillar
        steps = 8
        for s in range(steps):
            t = (s + 0.5) / steps; blocks(m, BR, x + (x2 - x) * t - 0.6, 10.2, z + (z2 - z) * t - 0.6, 1.2, 0.8, 1.2)
        mx, mz = (x + x2) / 2, (z + z2) / 2
        blocks(m, LAMP, mx - 0.4, 8.6, mz - 0.4, 0.8, 1, 0.8)                # a lantern hanging between pillars
    # the great moon gate: a ring of jade standing at the south edge
    gz, gy, R_ = 12.5, 2, 5.5
    for a in range(48):
        ang = a * 2 * math.pi / 48
        x, y = math.cos(ang) * R_, math.sin(ang) * R_ + R_ + gy
        if y < gy + 0.5: continue
        blocks(m, JD if a % 4 else JB, x - 0.6, y - 0.6, gz - 1, 1.2, 1.2, 2)
    blocks(m, JB, -7, gy, gz - 1.2, 1.5, 3, 2.4); blocks(m, JB, 5.5, gy, gz - 1.2, 1.5, 3, 2.4)
    for sx in (-8, 7): lamp_post(m, sx, 2, gz + 1, 3)
    boss(m, 0, 2.5, 0)
    return m

# ------------------------------------------------------------------ C. the jade pagoda
def roof(m, x0, z0, w, d, y, tiers=2):
    """Tiered jade roofs, each narrower, with the corners turned up."""
    for t in range(tiers):
        ins = t * 2.2
        blocks(m, JB, x0 + ins - 1, y + t * 3, z0 + ins - 1, w - 2 * ins + 2, 0.6, d - 2 * ins + 2)
        blocks(m, JB, x0 + ins - 0.3, y + t * 3 + 0.6, z0 + ins - 0.3, w - 2 * ins + 0.6, 0.6, d - 2 * ins + 0.6)
        for cx, cz in ((x0 + ins - 1, z0 + ins - 1), (x0 + w - ins, z0 + ins - 1), (x0 + ins - 1, z0 + d - ins), (x0 + w - ins, z0 + d - ins)):
            blocks(m, JD, cx, y + t * 3 + 0.6, cz, 1, 0.8, 1)                 # upturned eaves
        if t < tiers - 1: blocks(m, BR, x0 + ins + 1, y + t * 3 + 1.2, z0 + ins + 1, w - 2 * ins - 2, 1.8, d - 2 * ins - 2)
    top = y + tiers * 3 - 1.8
    blocks(m, JD, x0 + w / 2 - 0.5, top, z0 + d / 2 - 0.5, 1, 2, 1); blocks(m, LAMP, x0 + w / 2 - 0.5, top + 2, z0 + d / 2 - 0.5, 1, 1, 1)

def temple_c():
    m = Big(); ground(m)
    blocks(m, PO, -13, 0, -15, 26, 0.5, 30)                                 # the courtyard floor
    for (x, z, w, d) in ((-13, -15, 1, 30), (12, -15, 1, 30), (-13, 14, 11, 1), (2, 14, 11, 1)):
        blocks(m, BR, x, 0.5, z, w, 4, d); blocks(m, JB, x - 0.2, 4.5, z - 0.2, w + 0.4, 0.5, d + 0.4)   # walls with jade coping
    for a in range(24):
        ang = a * math.pi / 12; blocks(m, CH, round(math.cos(ang) * 4.5) - 0.5, 0.45, round(math.sin(ang) * 4.5) + 2.5, 1, 0.1, 1)
    # the gatehouse in the south wall
    blocks(m, BR, -3.5, 0.5, 13.5, 1.5, 5, 2); blocks(m, BR, 2, 0.5, 13.5, 1.5, 5, 2); blocks(m, BR, -3.5, 4.5, 13.5, 7, 1, 2)
    roof(m, -4, 13, 8, 3, 5.5, tiers=1)
    # the pagoda hall at the north end, on a raised platform
    blocks(m, DB, -9, 0.5, -15, 18, 1.5, 9)
    for x in range(-8, 9, 4):
        blocks(m, JD, x - 0.5, 2, -7.5, 1, 4, 1)                              # columns along its front
    blocks(m, BR, -8, 2, -14, 16, 4, 6)
    for s in range(3): blocks(m, PO, -2.5, s * 0.5, -6 + s * 0.6, 5, 0.5, 1)  # steps up to the hall
    roof(m, -9, -15, 18, 9, 6, tiers=3)
    for x in (-10, 9): lamp_post(m, x, 0.5, 10, 3)
    for x in (-10, 9): lamp_post(m, x, 0.5, -2, 3)
    boss(m, 0, 0.5, 2)
    return m

DESIGNS = [("A. The terraced temple", "a stepped ziggurat; climb the grand stair to the arena on top", temple_a),
           ("B. The moon gate", "a round colonnade behind a great circular jade gate", temple_b),
           ("C. The jade pagoda", "a walled courtyard arena before a pagoda hall with tiered jade roofs", temple_c)]

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    imgs = [(n, s, f().draw(0.2)) for n, s, f in DESIGNS]
    cw = max(i.width for _, _, i in imgs) + 60; ch = max(i.height for _, _, i in imgs) + 120
    W = 3 * cw + 60; H = 170 + ch + 40
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "The Great Moon Rabbit's temple: three directions", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Moonstone bricks, chiseled moonstone, jade and sea lanterns. The white figure is the rabbit, for scale.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + k * cw; y = 160; b = y + ch - 90
        c.paste(im, (x + (cw - im.width) // 2, b - im.height), im)
        d.text((x + cw // 2, b + 26), n, font=R.font(26, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, b + 56), s, font=R.font(17), fill=SUB, anchor="mm")
    c = c.resize((c.width * 2 // 3, c.height * 2 // 3), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "temple_concept.png"))
