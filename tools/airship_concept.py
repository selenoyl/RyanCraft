"""
Concept art for zephyrite airships, made to look vanilla: the zephyr balloon (passenger and cargo baskets, parked and
leashed to a fence), the balloon base (four balloons lifting a 12x12 deck) and the Zephyr Zeppelin at its mooring mast.
Built as box models from vanilla block textures (and ours: zephyrite, steel, meteor iron, skystone).
  python3 tools/airship_concept.py <vanilla textures dir>  ->  docs/airship_concept.png
"""
import os, sys, math
import numpy as np
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
import mudlings_concept as MC

HERE = os.path.dirname(os.path.abspath(__file__))
OURS = os.path.join(HERE, "..", "src", "main", "resources", "assets", "ryancraft", "textures", "block")
R.YAW = math.radians(-35)
_cache = {}

def T(name, tint=None):
    key = (name, tint)
    if key not in _cache:
        p = os.path.join(OURS, name + ".png")
        if os.path.exists(p):
            im = Image.open(p).convert("RGBA").crop((0, 0, 16, 16))
            if tint:
                a = np.array(im).astype(float); a[..., :3] *= np.array(tint) / 255.0; im = Image.fromarray(a.clip(0, 255).astype(np.uint8))
        else:
            im = MC.tex(name, tint)
        _cache[key] = np.array(im)
    return _cache[key]

class Big:
    """A box model on a large atlas (shelf-packed, filled with numpy), for models far bigger than a mob."""
    W = 4096
    def __init__(self):
        self.atlas = np.zeros((256, self.W, 4), np.uint8); self.x = self.y = self.rowh = 0
        self.parts = [{"pivot": [0, 0, 0], "rot": (0, 0, 0), "boxes": []}]
    def box(self, tex, frm, size, part=None):
        w, h, d = [max(0, int(round(v))) for v in size]
        rw, rh = 2 * d + 2 * w, d + h
        if rw == 0 or rh == 0: return
        if self.x + rw > self.W: self.x = 0; self.y += self.rowh; self.rowh = 0
        u, v = self.x, self.y; self.x += rw; self.rowh = max(self.rowh, rh)
        if v + rh > self.atlas.shape[0]:
            grow = np.zeros((max(v + rh, self.atlas.shape[0] * 2) - self.atlas.shape[0], self.W, 4), np.uint8)
            self.atlas = np.concatenate([self.atlas, grow])
        reps = (rh // 16 + 2, rw // 16 + 2, 1)
        ox, oy = (u * 7 + v * 3) % 16, (u * 5 + v * 11) % 16
        self.atlas[v:v + rh, u:u + rw] = np.tile(tex, reps)[oy:oy + rh, ox:ox + rw]
        (part or self.parts[0])["boxes"].append({"uv": [u, v], "from": [float(c) for c in frm], "size": [w, h, d], "mirror": False})
    def cbox(self, tex, centre, size, part=None):
        """A box by its centre (x, y, z) and size."""
        self.box(tex, [centre[i] - size[i] / 2 for i in range(3)], size, part)
    def part(self, pivot, rot):
        p = {"pivot": list(pivot), "rot": rot, "boxes": []}; self.parts.append(p); return p
    def draw(self, scale):
        path = os.path.join(MC.TMP, "airship_%d" % id(self))
        Image.fromarray(self.atlas).save(path + ".png")
        R.load_model = lambda name, m=self: ((m.W, m.atlas.shape[0]), m.parts)
        return R.render("air", path, scale)

def rope(m, a, b, tex=None, step=2.0):
    """A rope from a to b, as a run of small cubes (it reads as a taut line at these sizes)."""
    tex = tex if tex is not None else T("brown_wool", (230, 200, 160))
    a, b = np.array(a, float), np.array(b, float); n = max(1, int(np.linalg.norm(b - a) / step))
    for k in range(n + 1):
        p = a + (b - a) * k / n; m.cbox(tex, p, (1, 1, 1))

def round_layer(m, tex, cx, y, cz, r, h, axis="y"):
    """One slice of something round: three overlapping boxes make the circle (like the vanilla conduit's shell)."""
    for fa, fb in ((1.0, 0.62), (0.88, 0.88), (0.62, 1.0)):
        if axis == "y": m.cbox(tex, (cx, y, cz), (2 * r * fa, h, 2 * r * fb))
        else: m.cbox(tex, (cx, y, cz), (h, 2 * r * fa, 2 * r * fb))

# ------------------------------------------------------------------ the zephyr balloon
def envelope(m, cx, bottom, cz, R=32, H=78, colours=("white_wool", "cyan_wool")):
    """The envelope, a fat teardrop in wool bands, with a zephyrite lift ring round its widest part."""
    n = H // 6
    for i in range(n):
        t = (i + 0.5) / n
        r = R * math.sqrt(max(0.0, 1 - ((0.45 - t) / 0.47) ** 2)) if t < 0.45 else R * (1 - (t - 0.45) / 0.55 * 0.8)
        r = max(5, r); y = bottom - H + i * 6 + 3
        ring = abs(t - 0.45) < 0.5 / n
        tex = T("zephyrite_block") if ring else T(colours[(i // 2) % 2])
        round_layer(m, tex, cx, y, cz, r + (1 if ring else 0), 6)
    m.cbox(T(colours[0]), (cx, bottom - H - 1, cz), (12, 2, 12))                         # crown cap
    return R

def basket(m, cx, top, cz, w=30, d=30, h=14):
    """A wicker basket with a spruce rim, hanging from its four corner ropes."""
    wick, rim = T("bamboo_mosaic"), T("spruce_planks")
    x0, z0 = cx - w / 2, cz - d / 2
    m.box(wick, (x0, top, z0), (w, h, 2)); m.box(wick, (x0, top, z0 + d - 2), (w, h, 2))
    m.box(wick, (x0, top, z0 + 2), (2, h, d - 4)); m.box(wick, (x0 + w - 2, top, z0 + 2), (2, h, d - 4))
    m.box(T("spruce_planks"), (x0 + 2, top + h - 2, z0 + 2), (w - 4, 2, d - 4))          # floor
    for (x, z, ww, dd) in ((x0 - 1, z0 - 1, w + 2, 2), (x0 - 1, z0 + d - 1, w + 2, 2), (x0 - 1, z0 + 1, 2, d - 2), (x0 + w - 1, z0 + 1, 2, d - 2)):
        m.box(rim, (x, top - 1, z), (ww, 2, dd))
    return (x0, z0, w, d)

def burner(m, cx, y, cz, flame=True):
    """The burner: an iron frame over the basket with a zephyrite heart, and its flame lifting into the envelope."""
    m.cbox(T("iron_block"), (cx, y, cz), (8, 4, 8))
    m.cbox(T("zephyrite_block"), (cx, y - 3, cz), (4, 2, 4))
    if flame:
        m.cbox(T("shroomlight"), (cx, y - 7, cz), (4, 6, 4)); m.cbox(T("glowstone"), (cx, y - 11, cz), (2, 3, 2))

def balloon(m, cx, gz, cz, colours=("white_wool", "cyan_wool"), cargo=False, flame=True, R=32, H=78):
    """A whole balloon whose basket floor sits at height gz (y of the basket's bottom)."""
    bh = 14; top = gz - bh
    bw, bd = (38, 30) if cargo else (30, 30)
    x0, z0, w, d = basket(m, cx, top, cz, bw, bd, bh)
    env_bottom = top - 34
    envelope(m, cx, env_bottom, cz, R, H, colours)
    m.cbox(T(colours[1]), (cx, env_bottom + 1, cz), (14, 3, 14))                          # the open neck
    burner(m, cx, top - 14, cz, flame)
    for sx in (-1, 1):                                                                    # burner hangers
        rope(m, (cx + sx * 4, top - 12, cz), (cx + sx * 6, env_bottom, cz), T("iron_block"), 2)
    t = 0.45 + 0.55 * (1 - 15 / (0.88 * R)) / 0.8                                       # where the envelope is wide enough
    ey = env_bottom - H + t * H
    for (x, z) in ((x0 + 1, z0 + 1), (x0 + w - 2, z0 + 1), (x0 + 1, z0 + d - 2), (x0 + w - 2, z0 + d - 2)):
        rope(m, (x, top - 1, z), (cx + 10 * (1 if x > cx else -1), ey, cz + 10 * (1 if z > cz else -1)))
    if cargo:                                                                             # the cargo: chests and barrels
        m.box(T("barrel_top"), (x0 + 3, top - 10, z0 + 3), (12, 12, 12))
        m.box(T("oak_planks"), (x0 + w - 17, top - 8, z0 + 4), (14, 10, 12))
        m.box(T("iron_block"), (x0 + w - 11, top - 5, z0 + 3), (2, 3, 1))
        m.box(T("barrel_top"), (x0 + 3, top - 8, z0 + d - 15), (10, 10, 10))
    else:                                                                                 # a sandbag or two on the rim
        for (x, z) in ((x0 - 3, z0 + 8), (x0 - 3, z0 + 18), (x0 + w - 1, z0 + 12)):
            m.box(T("sand"), (x, top + 2, z), (4, 5, 4))
    return env_bottom - H

def piece_balloon():
    m = Big(); balloon(m, 0, 0, 0); return m
def piece_cargo():
    m = Big(); balloon(m, 0, 0, 0, colours=("orange_wool", "white_wool"), cargo=True); return m
def piece_parked():
    m = Big()
    for x in range(-48, 64, 16):                                                          # a bit of meadow
        for z in range(-32, 48, 16): m.box(T("grass_block_top", (124, 189, 107)), (x, 0, z), (16, 2, 16)); m.box(T("dirt"), (x, 2, z), (16, 6, 16))
    balloon(m, 0, 0, 0, colours=("red_wool", "white_wool"), flame=False)
    fx, fz = 46, 30                                                                       # the fence post it's leashed to
    m.box(T("oak_planks"), (fx - 2, -16, fz - 2), (4, 16, 4))
    m.box(T("oak_planks"), (fx - 2, -12, fz - 10), (4, 3, 8)); m.box(T("oak_planks"), (fx - 2, -6, fz - 10), (4, 3, 8))
    lead = T("brown_wool", (150, 110, 80))
    a, b, c = np.array((15, -13, 15.0)), np.array((fx, -14, fz)), None                    # the lead: sagging from basket rim to post
    for k in range(25):
        t = k / 24; p = a + (b - a) * t; p[1] += 6 * math.sin(math.pi * t); m.cbox(lead, p, (1, 1, 1))
    m.cbox(T("iron_block"), (fx, -14, fz), (5, 2, 5))
    return m

# ------------------------------------------------------------------ the balloon base: four balloons lift a 12x12 deck
def piece_base():
    m = Big(); S = 12 * 16; h = S / 2
    pl, frame = T("spruce_planks"), T("stripped_spruce_log")
    m.box(pl, (-h, 0, -h), (S, 4, S))                                                     # the deck
    for (x, z, w, d) in ((-h - 4, -h - 4, S + 8, 4), (-h - 4, h, S + 8, 4), (-h - 4, -h, 4, S), (h, -h, 4, S)):
        m.box(frame, (x, -2, z), (w, 6, d))                                               # frame round the edge
    for i in range(-5, 7, 2):                                                             # a railing
        for (x, z) in ((i * 16 - 1, -h - 3), (i * 16 - 1, h + 1), (-h - 3, i * 16 - 1), (h + 1, i * 16 - 1)):
            m.box(T("spruce_planks"), (x, -14, z), (2, 12, 2))
    for (x, z, w, d) in ((-h - 3, -h - 3, S + 6, 2), (-h - 3, h + 1, S + 6, 2), (-h - 3, -h - 1, 2, S + 2), (h + 1, -h - 1, 2, S + 2)):
        m.box(T("spruce_planks"), (x, -15, z), (w, 2, d))
    # a little cabin: oak walls, glass, a slab roof, a door
    cx0, cz0, cw, cd, ch = -h + 16, -h + 16, 80, 64, 44
    for (x, z, w, d) in ((cx0, cz0, cw, 4), (cx0, cz0 + cd - 4, cw, 4), (cx0, cz0, 4, cd), (cx0 + cw - 4, cz0, 4, cd)):
        m.box(T("oak_planks"), (x, -ch, z), (w, ch, d))
    for x in (cx0 + 14, cx0 + 50): m.box(T("glass"), (x, -32, cz0 + cd - 4.5), (14, 14, 5))
    m.box(T("glass"), (cx0 + cw - 4.5, -32, cz0 + 24), (5, 14, 16))
    m.box(T("oak_log"), (cx0 - 2, -ch - 2, cz0 - 2), (4, ch + 2, 4)); m.box(T("oak_log"), (cx0 + cw - 2, -ch - 2, cz0 + cd - 2), (4, ch + 2, 4))
    m.box(T("oak_log"), (cx0 + cw - 2, -ch - 2, cz0 - 2), (4, ch + 2, 4)); m.box(T("oak_log"), (cx0 - 2, -ch - 2, cz0 + cd - 2), (4, ch + 2, 4))
    m.box(T("spruce_planks"), (cx0 - 6, -ch - 8, cz0 - 6), (cw + 12, 8, cd + 12))
    m.box(T("spruce_planks"), (cx0 + 4, -ch - 14, cz0 + 4), (cw - 8, 6, cd - 8))
    m.box(T("spruce_planks"), (cx0 + 30, -30, cz0 + cd - 3), (14, 30, 4))
    # the gear of a working base, out on the deck
    m.box(T("crafting_table_front"), (16, -16, -h + 20), (16, 16, 16)); m.box(T("crafting_table_top"), (16, -17, -h + 20), (16, 1, 16))
    m.box(T("furnace_front"), (36, -16, -h + 20), (16, 16, 16)); m.box(T("blast_furnace_front"), (56, -16, -h + 20), (16, 16, 16))
    m.box(T("barrel_top"), (16, -16, 20), (16, 16, 16)); m.box(T("barrel_side"), (32, -16, 20), (16, 16, 16)); m.box(T("barrel_top"), (16, -32, 20), (16, 16, 16))
    m.box(T("oak_planks"), (52, -12, 24), (14, 12, 12)); m.box(T("iron_block"), (58, -9, 36), (2, 3, 1))
    m.box(T("red_wool"), (-h + 20, -9, 30), (16, 5, 32)); m.box(T("white_wool"), (-h + 20, -11, 30), (16, 2, 8))   # a bed
    m.box(T("oak_planks"), (-h + 20, -4, 30), (16, 4, 32))
    m.box(T("glowstone"), (h - 26, -24, h - 26), (8, 8, 8)); m.box(T("oak_planks"), (h - 23, -16, h - 23), (2, 16, 2))
    # a balloon at each corner, tethered down to the deck
    cols = [("white_wool", "cyan_wool"), ("red_wool", "white_wool"), ("yellow_wool", "white_wool"), ("lime_wool", "white_wool")]
    for k, (x, z) in enumerate(((-h, -h), (h, -h), (-h, h), (h, h))):
        m.cbox(T("iron_block"), (x, -3, z), (8, 4, 8))                                    # the tether ring
        ex, ez = x, z
        bottom = -230
        envelope(m, ex, bottom, ez, 34, 84, cols[k])
        m.cbox(T(cols[k][1]), (ex, bottom + 1, ez), (14, 3, 14))
        for sx, sz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            rope(m, (x, -5, z), (ex + sx * 9, bottom - 12, ez + sz * 9), step=3)
    return m

# ------------------------------------------------------------------ the zephyr zeppelin and its mooring mast
def piece_zeppelin():
    m = Big()
    L, Rm = 46 * 16, 6.5 * 16                                                             # 46 blocks long, 13 across
    cy = -Rm - 120                                                                        # envelope centre height
    seg = 16
    for i in range(int(L // seg)):
        x = -L / 2 + i * seg + seg / 2; u = (x / (L / 2))
        r = Rm * (1 - abs(u) ** 2.4) ** (1 / 2.4) if u < 0 else Rm * (1 - abs(u) ** 1.7) ** (1 / 1.7)
        r = max(8, r)
        band = i % 6 == 3 and abs(u) < 0.85
        tex = T("zephyrite_block") if band else T("light_gray_wool") if i % 2 else T("white_wool")
        round_layer(m, tex, x, cy, 0, r + (1 if band else 0), seg, axis="x")
    # the meteor-iron keel along the belly
    m.box(T("meteor_iron_block"), (-L / 2 + 120, cy + Rm - 4, -6), (L - 260, 8, 12))
    # tail fins: top, bottom and both sides, white with a red trailing edge, rooted in the tapering hull
    tx, fw = L / 2 - 110, 84
    u = (tx + fw / 2) / (L / 2); rr = Rm * (1 - abs(u) ** 1.7) ** (1 / 1.7) - 10
    m.box(T("white_wool"), (tx, cy - rr - 40, -2), (fw, 40, 4)); m.box(T("red_wool"), (tx + fw, cy - rr - 40, -2), (12, 40, 4))
    m.box(T("white_wool"), (tx, cy + rr, -2), (fw, 36, 4)); m.box(T("red_wool"), (tx + fw, cy + rr, -2), (12, 36, 4))
    for sz in (-1, 1):
        z0 = rr if sz > 0 else -rr - 40
        m.box(T("white_wool"), (tx, cy - 2, z0), (fw, 4, 40)); m.box(T("red_wool"), (tx + fw, cy - 2, z0), (12, 4, 40))
    # the gondola: a long cabin slung under the middle: steel-plated hull, spruce cabin, glass bridge
    gx0, gw, gd, gtop = -170, 300, 56, cy + Rm + 36
    m.box(T("cut_steel"), (gx0, gtop + 32, -gd / 2), (gw, 8, gd))                          # hull
    m.box(T("steel_block"), (gx0 + 8, gtop + 40, -gd / 2 + 6), (gw - 16, 6, gd - 12))
    m.box(T("spruce_planks"), (gx0 + 40, gtop, -gd / 2), (gw - 40, 32, gd))               # cabin
    for k in range(7): m.box(T("glass"), (gx0 + 60 + k * 32, gtop + 8, -gd / 2 - 0.5), (18, 14, 1)); m.box(T("glass"), (gx0 + 60 + k * 32, gtop + 8, gd / 2 - 0.5), (18, 14, 1))
    m.box(T("glass"), (gx0, gtop + 4, -gd / 2 + 4), (40, 28, gd - 8))                     # the bridge, glazed all round
    m.box(T("cut_steel"), (gx0, gtop - 2, -gd / 2), (gw, 4, gd))                          # roof
    for k in range(5):                                                                    # struts up to the envelope
        x = gx0 + 30 + k * 60
        for sz in (-1, 1): m.box(T("stripped_spruce_log"), (x, cy + Rm - 30, sz * (gd / 2 - 6) - 2), (4, gtop - (cy + Rm - 30), 4))
    # engine cars with propellers on outriggers, both sides, aft of the gondola
    for sz in (-1, 1):
        ez = sz * (gd / 2 + 44); ex = gx0 + gw - 40
        m.box(T("steel_block"), (ex - 4, gtop + 6, min(ez, sz * gd / 2) if sz > 0 else ez), (8, 6, abs(ez) - gd / 2))   # outrigger
        m.box(T("iron_block"), (ex - 30, gtop - 6, ez - 14), (40, 26, 28))                  # engine pod
        m.box(T("piston_side"), (ex - 26, gtop - 10, ez - 10), (30, 4, 20))
        m.box(T("iron_block"), (ex + 10, gtop + 3, ez - 4), (8, 8, 8))                      # hub
        for k in range(3):                                                                  # three blades
            p = m.part([ex + 18, gtop + 7, ez], (k * 2 * math.pi / 3 + 0.3, 0, 0))
            m.box(T("stripped_oak_log"), (-1, -34, -4), (2, 30, 8), part=p)
    # the mooring mast at the nose: a skystone base, a steel lattice tower and the mooring cone
    mx = -L / 2 - 40; base = 0
    m.box(T("skystone"), (mx - 40, base - 16, -40), (80, 16, 80))
    top = cy - 6
    for (x, z) in ((mx - 20, -20), (mx + 16, -20), (mx - 20, 16), (mx + 16, 16)):
        m.box(T("steel_block"), (x, top, z), (4, base - 16 - top, 4))
    for yy in range(int(top) + 20, -16, 40):                                              # lattice rings
        m.box(T("cut_steel"), (mx - 20, yy, -20), (40, 3, 40))
    m.box(T("cut_steel"), (mx - 24, top - 6, -24), (48, 6, 48))                            # platform
    m.box(T("iron_block"), (mx - 6, top - 22, -6), (12, 16, 12)); m.box(T("zephyrite_block"), (mx - 4, top - 30, -4), (8, 8, 8))
    m.box(T("iron_block"), (mx + 6, cy - 2, -2), (-L / 2 - mx - 2, 4, 4))                # the boom out to the nose
    return m

PIECES = [("Zephyr Balloon", "two to four riders; the burner's zephyrite heart lifts it", piece_balloon, 1.0),
          ("Cargo Balloon", "a wider basket with chests and barrels; any wool colour", piece_cargo, 1.0),
          ("Parked", "set down on the ground and leashed to a fence post", piece_parked, 0.95)]

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    imgs = [(n, s, f().draw(sc)) for n, s, f, sc in PIECES]
    base = piece_base().draw(0.55)
    zep = piece_zeppelin().draw(0.42)
    cols = 3; cw = max(i.width for _, _, i in imgs) + 80; ch = max(i.height for _, _, i in imgs) + 90
    W = max(cols * cw + 60, zep.width + 120, base.width + 120)
    H = 170 + ch + base.height + 150 + zep.height + 160
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Zephyrite airships: concept art", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Hot air balloons and the Zephyr Zeppelin, vanilla style: wool envelopes, wicker baskets, a zephyrite burner. Vanilla blocks plus our zephyrite, steel and meteor iron.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + k * cw; y = 160; b = y + ch - 70
        c.paste(im, (x + (cw - im.width) // 2, b - im.height), im)
        d.text((x + cw // 2, b + 22), n, font=R.font(24, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, b + 50), s, font=R.font(17), fill=SUB, anchor="mm")
    y = 160 + ch + 10
    d.text((40, y), "Balloon base: a balloon tied to each corner lifts a 12x12 deck, and everything built on it", font=R.font(26, True), fill=INK)
    c.paste(base, ((W - base.width) // 2, y + 50), base)
    y += 50 + base.height + 40
    d.text((40, y), "The Zephyr Zeppelin: 46 blocks long, built in a dry dock; engine-driven propellers, a crew gondola, moored at its mast", font=R.font(26, True), fill=INK)
    c.paste(zep, ((W - zep.width) // 2, y + 50), zep)
    c = c.resize((c.width * 2 // 3, c.height * 2 // 3), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(HERE, "..", "docs", "airship_concept.png"))
