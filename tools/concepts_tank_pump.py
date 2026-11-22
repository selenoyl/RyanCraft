"""Concept renders: tank and pump designs (and the new engines), built from vanilla/classic materials."""
import math, os, sys
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import render_iso as R

SC = "/tmp/claude-0/-home-user/d01fb490-694d-5325-b287-f3704fa7dedc/scratchpad"
CL = "src/main/resources/resourcepacks/classic/assets/minecraft/textures/block/"
PA = SC + "/pa5/assets/minecraft/textures/block/"; VT = SC + "/vt/assets/minecraft/textures/block/"
def T(p): return Image.open(p).convert("RGBA").crop((0, 0, 16, 16))
def tint(im, c):
    im = im.copy(); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]; px[x, y] = (r * c[0] // 255, g * c[1] // 255, b * c[2] // 255, 255 if a else 0)
    return im
glass = T(CL + "glass.png"); iron_dark = T(PA + "anvil.png"); caul = T(PA + "cauldron_side.png")
water = tint(T(CL + "water_still.png"), (64, 114, 255)); lava = T(CL + "lava_still.png")
planks = T(CL + "oak_planks.png"); copper = T(VT + "copper_block.png"); oxid = T(VT + "oxidized_copper.png")
barrel = T(VT + "barrel_side.png"); hop = T(PA + "hopper_outside.png"); dirt_water = water
iron = T(CL + "iron_block.png")

class Model:
    def __init__(self): self.atlas = Image.new("RGBA", (512, 512), (0, 0, 0, 0)); self.u = self.v = self.rowh = 0; self.parts = []
    def box(self, frm, size, top, side=None, bottom=None, rot=None, pivot=None):
        """frm/size in pixels, y up from the ground. side/bottom default to top."""
        side = side or top; bottom = bottom or top
        w, h, d = size; wi, hi, di = (max(1, math.ceil(v)) for v in size); fw, fh = 2 * di + 2 * wi, di + hi
        if self.u + fw > 512: self.u = 0; self.v += self.rowh; self.rowh = 0
        u, v = self.u, self.v; self.u += fw; self.rowh = max(self.rowh, fh)
        def put(tex, x0, y0, ww, hh):
            if ww <= 0 or hh <= 0: return
            t = Image.new("RGBA", (ww, hh))
            for yy in range(hh):
                for xx in range(ww): t.putpixel((xx, yy), tex.getpixel((xx % 16, yy % 16)))
            self.atlas.paste(t, (x0, y0))
        put(top, u + di, v, wi, di); put(bottom, u + di + wi, v, wi, di)
        for i, x0 in enumerate((u, u + di, u + di + wi, u + 2 * di + wi)): put(side, x0, v + di, di if i % 2 == 0 else wi, hi)
        x, y, z = frm
        b = {"uv": [u, v], "from": [x - 8, -(y + h), z - 8], "size": [w, h, d], "mirror": False}
        if rot is not None:
            px_, py_, pz_ = pivot
            b["from"] = [x - px_, -(y + h) + py_, z - pz_]
            self.parts.append({"pivot": [px_ - 8, 24 - py_, pz_ - 8], "rot": [0, rot, 0], "boxes": [b]})
        else:
            self.parts.append({"pivot": [0, 24, 0], "rot": [0, 0, 0], "boxes": [b]})

def render(m, scale=1.0):
    path = os.path.join(R.ROOT, "textures/entity/_concept.png"); m.atlas.save(path)
    R.load_model = lambda name, m=m: ((512, 512), m.parts)
    img = R.render("x", "_concept", scale); os.remove(path); return img

# ---------------- materials for the refined designs
def riveted(base, k=0.55):
    """Dark iron with a rivet in each corner and a darker edge."""
    t = base.copy(); px = t.load()
    for y in range(16):
        for x in range(16):
            if min(x, y, 15 - x, 15 - y) == 0: px[x, y] = shade(px[x, y], 0.6)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)): px[x, y] = (150, 150, 156, 255); px[x + 1, y + 1] = (40, 40, 44, 255)
    return t
def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)
dark = riveted(iron_dark)
def clean_glass():
    t = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = t.load()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)): px[x, y] = (200, 220, 230, 200)
    for i in range(3, 8): px[i, 11 - i] = (240, 250, 255, 220); px[i + 1, 11 - i] = (220, 235, 245, 160)   # a highlight streak
    return t
cglass = clean_glass()
def copper_plate(c):
    t = c.copy(); px = t.load()
    for x in range(16): px[x, 7] = shade(px[x, 7], 0.75); px[x, 8] = shade(px[x, 8], 1.12)            # a seam band
    for x in (1, 5, 10, 14): px[x, 5] = shade(px[x, 5], 0.55); px[x, 10] = shade(px[x, 10], 0.55)      # rivets along it
    for y in range(16): px[0, y] = shade(px[0, y], 0.7); px[15, y] = shade(px[15, y], 0.7)
    return t
def porthole(c):
    t = copper_plate(c); px = t.load()
    for y in range(4, 12):
        for x in range(4, 12):
            corner = (x in (4, 11)) and (y in (4, 11))
            if corner: continue
            ring = x in (4, 11) or y in (4, 11) or (x in (5, 10) and y in (5, 10))
            px[x, y] = (54, 52, 56, 255) if ring else (0, 0, 0, 0)
    return t
def vat_side():
    t = barrel.copy(); px = t.load()
    for y in (3, 4, 11, 12):
        for x in range(16): px[x, y] = shade(iron_dark.getpixel((x, y)), 0.9 if y in (3, 11) else 0.6)
    return t
def vat_rim():
    t = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = t.load()
    for y in range(16):
        for x in range(16):
            if min(x, y, 15 - x, 15 - y) < 2: px[x, y] = planks.getpixel((x, y))
    return t

# ---------------- tanks
def pane():
    """A clean glass pane: faint edge, clear middle, a small highlight in each corner (the same on every face)."""
    t = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = t.load()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)): px[x, y] = (190, 214, 226, 170)
    for (x, y) in ((1, 1), (2, 1), (1, 2), (14, 14), (13, 14), (14, 13)): px[x, y] = (236, 248, 255, 200)
    return t
glass_pane = pane()
def tank_frame(fluid=water, level=11, tall=False):
    """A dark-iron frame along all twelve edges with a glass pane inset on every face: symmetrical from every side."""
    m = Model(); H = 32 if tall else 16
    m.box((2, 2, 2), (12, min(level + (16 if tall else 0), H - 4), 12), fluid)
    m.box((1, 1, 1), (14, H - 2, 14), glass_pane)
    for (x, z) in ((0, 0), (14, 0), (0, 14), (14, 14)): m.box((x, 0, z), (2, H, 2), dark)           # vertical edges
    for y in (0, H - 2):                                                                                  # top and bottom edges
        m.box((2, y, 0), (12, 2, 2), dark); m.box((2, y, 14), (12, 2, 2), dark)
        m.box((0, y, 2), (2, 2, 12), dark); m.box((14, y, 2), (2, 2, 12), dark)
    return m
def tank_vat():
    """Walls and a floor, so you look down past the plank rim at the water inside; hoops only around the sides."""
    m = Model(); vs = vat_side()
    m.box((1, 0, 1), (14, 1, 14), planks)                                                 # floor
    m.box((3, 1, 3), (10, 11, 10), water)                                                 # the water, up to just below the rim
    m.box((1, 1, 1), (14, 13, 2), planks, side=vs); m.box((1, 1, 13), (14, 13, 2), planks, side=vs)
    m.box((1, 1, 3), (2, 13, 10), planks, side=vs); m.box((13, 1, 3), (2, 13, 10), planks, side=vs)
    return m

def copper_side(c):
    """Copper with a centred round porthole framed in dark iron, and a rivet near each corner."""
    t = c.copy(); px = t.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5; r = (dx * dx + dy * dy) ** 0.5
            if r < 2.6: px[x, y] = (0, 0, 0, 0)                                 # the window
            elif r < 3.9: px[x, y] = (52, 50, 54, 255) if r > 3.2 else (84, 82, 88, 255)   # its dark-iron ring
    for (x, y) in ((2, 3), (13, 3), (2, 12), (13, 12)): px[x, y] = shade(px[x, y], 0.55); px[x, y - 1] = shade(px[x, y - 1], 1.2)
    return t
def tank_copper(c=copper):
    """Symmetrical: rims top and bottom, a porthole centred on each side, a centred hatch on the lid."""
    m = Model(); side = copper_side(c)
    m.box((3, 3, 3), (10, 9, 10), water)
    m.box((1, 2, 1), (14, 12, 14), c, side=side)
    m.box((0, 0, 0), (16, 2, 16), shade_img(c, 0.8)); m.box((0, 14, 0), (16, 2, 16), shade_img(c, 0.8))   # rims
    m.box((6, 16, 6), (4, 1, 4), dark)                                                                      # hatch
    return m
def shade_img(t, k):
    t = t.copy(); px = t.load()
    for y in range(16):
        for x in range(16): px[x, y] = shade(px[x, y], k)
    return t

# ---------------- pumps
def pump_screw(turn=0):
    m = Model()
    m.box((0, 0, 0), (16, 2, 16), dark); m.box((0, 14, 0), (16, 2, 16), dark)
    for (x, z) in ((0, 0), (14, 0), (0, 14), (14, 14)): m.box((x, 2, z), (2, 12, 2), dark)
    m.box((7, 2, 7), (2, 12, 2), iron)
    for i in range(10):
        a = turn + i * 40
        m.box((9, 2.5 + i * 1.1, 7), (5, 1, 2), iron, rot=math.radians(a), pivot=(8, 2.5 + i * 1.1, 8))
        m.box((9.5, 3.4 + i * 1.1, 7.2), (3, 0.6, 1.6), water, rot=math.radians(a + 6), pivot=(8, 3.4 + i * 1.1, 8))
    m.box((6, -12, 6), (4, 12, 4), dark); m.box((2, -13, 2), (12, 1, 12), water)
    return m
def pump_hand():
    m = Model()
    m.box((4, 0, 4), (8, 2, 8), dark)                                                    # foot
    m.box((5, 2, 5), (6, 11, 6), caul); m.box((4, 13, 4), (8, 2, 8), dark)              # column and cap
    m.box((7, 9, 0.9), (2, 2, 4.1), dark); m.box((7, 7.4, 0.9), (2, 1.6, 1), dark)      # spout (clear of the column's face)
    m.box((7.25, 15, 7.25), (1.5, 2, 1.5), iron)                                         # pivot, ending below the handle
    m.box((7, 17, 1), (2, 1, 14), dark)                                                  # handle
    m.box((6.5, 16.6, 13.6), (3, 1.8, 2.4), planks)                                      # grip
    return m

def engine(kind, stroke):
    m = Model(); tex = Image.open(f"src/main/resources/assets/ryancraft/textures/entity/engine/{kind}{'_hot' if stroke else ''}.png").convert("RGBA")
    m.atlas.paste(tex, (0, 0))
    def bx(uv, frm, size, dy=0): m.parts.append({"pivot": [0, 24, 0], "rot": [0, 0, 0], "boxes": [{"uv": uv, "from": [frm[0], -(frm[1] + size[1] + dy), frm[2]], "size": size, "mirror": False}]})
    bx([0, 0], (-8, 0, -8), [16, 4, 16]); bx([0, 20], (-8, 4, -8), [16, 4, 16], stroke * 8)
    if stroke: m.parts.append({"pivot": [0, 24, 0], "rot": [0, 0, 0], "boxes": [{"uv": [64, 0], "from": [-6, -(4 + stroke * 8), -6], "size": [12, stroke * 8, 12], "mirror": False}]})
    bx([64, 32], (-4, 8, -4), [8, 8, 8])
    return m

if __name__ == "__main__":
    exposed = T(VT.replace("vt", "vt") + "copper_block.png")
    rows = [("Tanks", [("Glass + dark iron", tank_frame()), ("...stacked, lava", tank_frame(lava, 12, True)),
                       ("Iron-hooped vat", tank_vat()), ("Copper", tank_copper()), ("...exposed", tank_copper(T(VT + "exposed_copper.png")))]),
            ("Copper weathering", [("...weathered", tank_copper(T(VT + "weathered_copper.png"))), ("...oxidized", tank_copper(oxid))]),
            ("Pumps", [("Archimedes screw", pump_screw()), ("...turning", pump_screw(20)), ("Cast-iron hand pump", pump_hand())]),
]
    imgs = [[(lbl, render(mdl, 1.0)) for lbl, mdl in items] for _, items in rows]
    cell = 260; W = 40 + 5 * cell; Hh = 60 + len(rows) * (cell + 60)
    sheet = Image.new("RGB", (W, Hh), (238, 234, 226)); d = ImageDraw.Draw(sheet)
    d.text((24, 16), "Tanks & pumps, refined", font=R.font(26, True), fill=(40, 36, 30))
    for r, ((title, _), row) in enumerate(zip(rows, imgs)):
        y = 60 + r * (cell + 60); d.text((24, y), title, font=R.font(20, True), fill=(60, 54, 44))
        for i, (lbl, im) in enumerate(row):
            k = min((cell - 30) / im.width, (cell - 40) / im.height, 6)
            im2 = im.resize((max(1, int(im.width * k)), max(1, int(im.height * k))), Image.NEAREST)
            x = 30 + i * cell; sheet.paste(im2, (x + (cell - im2.width) // 2, y + 30 + (cell - 40 - im2.height) // 2), im2)
            d.text((x + 10, y + cell + 8), lbl, font=R.font(14), fill=(60, 54, 44))
    sheet.save("docs/tank_pump_concepts.png"); print("ok")
