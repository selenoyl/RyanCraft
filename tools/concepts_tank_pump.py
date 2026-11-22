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

# ---------------- tanks
def tank_frame(fluid=water, level=11, tall=False):
    m = Model(); H = 32 if tall else 16
    m.box((3, 1, 3), (10, level + (16 if tall else 0), 10), fluid)                      # the fluid inside
    m.box((1, 0, 1), (14, H, 14), glass)                                                 # glass body
    for (x, z) in ((0, 0), (14, 0), (0, 14), (14, 14)): m.box((x, 0, z), (2, H, 2), iron_dark)  # dark-iron corner posts
    for y in ((0, H - 1) if not tall else (0, H - 1)):
        m.box((0, y, 0), (16, 1, 2), iron_dark); m.box((0, y, 14), (16, 1, 2), iron_dark)
        m.box((0, y, 2), (2, 1, 12), iron_dark); m.box((14, y, 2), (2, 1, 12), iron_dark)
    return m
def tank_vat():
    m = Model()
    m.box((1, 0, 1), (14, 14, 14), barrel, side=barrel)
    m.box((2, 12, 2), (12, 1, 12), water)                                                # open top, water surface
    for y in (2, 10):                                                                    # iron hoops
        m.box((0, y, 0), (16, 2, 1), iron_dark); m.box((0, y, 15), (16, 2, 1), iron_dark)
        m.box((0, y, 1), (1, 2, 14), iron_dark); m.box((15, y, 1), (1, 2, 14), iron_dark)
    return m
def tank_copper(c=copper):
    m = Model()
    m.box((1, 0, 1), (14, 16, 14), c)
    for (x, z, w_, d_) in ((5, 0.0, 6, 1), (0.0, 5, 1, 6)):                              # round-ish portholes with fluid behind
        m.box((5, 5, 0), (6, 6, 1), glass); m.box((0, 5, 5), (1, 6, 6), glass)
    m.box((6, 6, 0.5), (4, 4, 1), water); m.box((0.5, 6, 6), (1, 4, 4), water)
    m.box((3, 16, 3), (10, 1, 10), c)
    return m

# ---------------- pumps
def pump_screw(turn=0):
    m = Model()
    m.box((0, 0, 0), (16, 2, 16), iron_dark); m.box((0, 14, 0), (16, 2, 16), iron_dark)     # base and cap
    for (x, z) in ((0, 0), (13, 0), (0, 13), (13, 13)): m.box((x, 2, z), (3, 12, 3), caul)   # dark-iron frame posts
    m.box((7, 2, 7), (2, 12, 2), iron)                                                       # the screw's shaft
    for i in range(16):                                                                      # helix blades
        a = turn + i * 45
        m.box((9, 2 + i * 0.72, 7.5), (4, 1, 1), iron, rot=math.radians(a), pivot=(8, 2 + i * 0.72, 8))
        if i % 2 == 0: m.box((9.5, 2.5 + i * 0.72, 7.6), (2, 1, 0.8), water, rot=math.radians(a + 10), pivot=(8, 2.5 + i * 0.72, 8))
    m.box((6, -14, 6), (4, 14, 4), iron_dark)                                                # intake tube down to the pool
    m.box((2, -15, 2), (12, 1, 12), water)
    return m
def pump_hand():
    m = Model()
    m.box((5, 0, 5), (6, 12, 6), caul); m.box((4, 12, 4), (8, 2, 8), iron_dark)            # cast-iron column and cap
    m.box((7, 9, -1), (2, 2, 6), iron_dark); m.box((7, 7, -1), (2, 2, 2), iron_dark)        # spout
    m.box((7, 15, 2), (2, 1, 14), iron_dark, rot=math.radians(0), pivot=(8, 15, 8))         # lever arm
    m.box((7, 14, 7), (2, 2, 2), iron)
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
    rows = [("Tanks", [("Glass + dark iron (my pick)", tank_frame()), ("…stacked, lava", tank_frame(lava, 12, True)),
                       ("Iron-hooped vat", tank_vat()), ("Copper w/ portholes", tank_copper()), ("…weathered", tank_copper(oxid))]),
            ("Pumps", [("Archimedes screw (my pick)", pump_screw()), ("Cast-iron hand pump", pump_hand())]),
            ("New engines", [("Redstone, closed", engine("redstone", 0)), ("Redstone, extended", engine("redstone", 1)),
                             ("Stirling, closed", engine("stirling", 0)), ("Stirling, extended", engine("stirling", 1))])]
    imgs = [[(lbl, render(mdl, 1.0)) for lbl, mdl in items] for _, items in rows]
    cell = 260; W = 40 + 5 * cell; Hh = 60 + len(rows) * (cell + 60)
    sheet = Image.new("RGB", (W, Hh), (238, 234, 226)); d = ImageDraw.Draw(sheet)
    d.text((24, 16), "Tank & pump concepts, new engines", font=R.font(26, True), fill=(40, 36, 30))
    for r, ((title, _), row) in enumerate(zip(rows, imgs)):
        y = 60 + r * (cell + 60); d.text((24, y), title, font=R.font(20, True), fill=(60, 54, 44))
        for i, (lbl, im) in enumerate(row):
            k = min((cell - 30) / im.width, (cell - 40) / im.height, 6)
            im2 = im.resize((max(1, int(im.width * k)), max(1, int(im.height * k))), Image.NEAREST)
            x = 30 + i * cell; sheet.paste(im2, (x + (cell - im2.width) // 2, y + 30 + (cell - 40 - im2.height) // 2), im2)
            d.text((x + 10, y + cell + 8), lbl, font=R.font(14), fill=(60, 54, 44))
    sheet.save("docs/tank_pump_concepts.png"); print("ok")
