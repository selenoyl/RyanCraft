"""
Concept art for rotational power (Vintage Story style, made to look vanilla): axles, cogs, a gearbox, a hand crank,
a millstone, windmill sails, a waterwheel and an engine crank, built as box models from vanilla block textures.
  python3 tools/rotation_concept.py <vanilla textures dir>  ->  docs/rotation_concept.png
"""
import os, sys, math
from PIL import Image, ImageDraw, ImageFilter
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
from mudlings_concept import Model, tex

R.YAW = math.radians(-35)
WATER = (60, 110, 220)
T = lambda n, tint=None: tex(n, tint)

def cog(m, cx, cy, cz, r=7, t="oak_planks", hub="stripped_oak_log", axis="z", teeth=8, thick=2):
    """A cog: a plate, a hub, and teeth around the rim (each tooth its own rotated part)."""
    if axis == "z":
        m.box(T(t), (cx - r + 1, cy - r + 1, cz - thick / 2), (2 * r - 2, 2 * r - 2, thick))
        m.box(T(t), (cx - r, cy - r + 3, cz - thick / 2), (2 * r, 2 * r - 6, thick)); m.box(T(t), (cx - r + 3, cy - r, cz - thick / 2), (2 * r - 6, 2 * r, thick))
        m.box(T(hub), (cx - 2, cy - 2, cz - thick / 2 - 1), (4, 4, thick + 2))
        for k in range(teeth):
            p = m.part([cx, cy, cz], (0, 0, k * 2 * math.pi / teeth + math.pi / teeth))
            m.box(T(t), (-1.5, -r - 2.5, -thick / 2), (3, 3, thick), part=p)
    else:   # horizontal cog (axis y)
        m.box(T(t), (cx - r + 1, cy - thick / 2, cz - r + 1), (2 * r - 2, thick, 2 * r - 2))
        m.box(T(t), (cx - r, cy - thick / 2, cz - r + 3), (2 * r, thick, 2 * r - 6)); m.box(T(t), (cx - r + 3, cy - thick / 2, cz - r), (2 * r - 6, thick, 2 * r))
        m.box(T(hub), (cx - 2, cy - thick / 2 - 1, cz - 2), (4, thick + 2, 4))
        for k in range(teeth):
            p = m.part([cx, cy, cz], (0, k * 2 * math.pi / teeth + math.pi / teeth, 0))
            m.box(T(t), (-1.5, -thick / 2, -r - 2.5), (3, thick, 3), part=p)

def axle(m, x0, y, z, length, along="x"):
    if along == "x": m.box(T("stripped_oak_log"), (x0, y - 2, z - 2), (length, 4, 4))
    else: m.box(T("stripped_oak_log"), (z - 2, y - 2, x0), (4, 4, length))

def piece_axle():
    m = Model(1); axle(m, -24, 12, 0, 48)
    for x in (-24, 20): m.box(T("oak_planks"), (x, 8, -4), (4, 8, 8))           # bearings
    return m
def piece_cog():
    m = Model(2); cog(m, 0, 10, 0, r=8); axle(m, -2, 10, -10, 20, along="z") if False else m.box(T("stripped_oak_log"), (-2, 8, -9), (4, 4, 18)); return m
def piece_copper_cog():
    m = Model(3); cog(m, 0, 10, 0, r=8, t="cut_copper", hub="copper_block"); m.box(T("stripped_oak_log"), (-2, 8, -9), (4, 4, 18)); return m
def piece_gearbox():
    m = Model(4); W = "stripped_spruce_log"
    for (x, y, z, w, h, d) in [(-8, 0, -8, 2, 16, 2), (6, 0, -8, 2, 16, 2), (-8, 0, 6, 2, 16, 2), (6, 0, 6, 2, 16, 2),
                               (-8, 0, -8, 16, 2, 2), (-8, 14, -8, 16, 2, 2), (-8, 0, 6, 16, 2, 2), (-8, 14, 6, 16, 2, 2),
                               (-8, 0, -8, 2, 2, 16), (6, 0, -8, 2, 2, 16), (-8, 14, -8, 2, 2, 16), (6, 14, -8, 2, 2, 16)]:
        m.box(T(W), (x, y, z), (w, h, d))
    m.box(T("spruce_planks"), (-8, 14, -8), (16, 2, 16))                                # lid
    cog(m, 0, 8, -1, r=5, t="cut_copper", hub="copper_block", teeth=8)                 # two cogs meshing at right angles
    cog(m, -1, 8, 0, r=5, t="cut_copper", hub="copper_block", axis="y", teeth=8) if False else None
    m.box(T("stripped_oak_log"), (-12, 6, -3), (8, 4, 4)); m.box(T("stripped_oak_log"), (-2, 6, -14), (4, 4, 8))   # axle stubs
    return m
def piece_crank():
    m = Model(5)
    m.box(T("cobblestone"), (-8, 16, -8), (16, 8, 16))                                   # base
    m.box(T("stripped_oak_log"), (-1, 4, -1), (2, 12, 2))                                # upright shaft
    m.box(T("stripped_oak_log"), (-1, 3, -1), (9, 2, 2)); m.box(T("oak_log"), (6, -2, -1), (2, 5, 2))   # arm and handle
    return m
def piece_millstone():
    m = Model(6)
    m.box(T("smooth_stone"), (-8, 18, -8), (16, 6, 16))                                  # bed
    for (w, d) in ((14, 10), (10, 14)): m.box(T("stone"), (-w / 2, 12, -d / 2), (w, 6, d))   # the round runner stone
    m.box(T("stone"), (-6, 11, -6), (12, 1, 12))
    m.box(T("stripped_oak_log"), (-1, 2, -1), (2, 10, 2))                                # spindle
    m.box(T("oak_planks"), (-5, 2, -1), (10, 2, 2))                                      # rynd / crank bar
    m.box(T("oak_planks"), (8, 18, -2), (5, 2, 4))                                         # flour spout
    return m
def piece_sails():
    m = Model(7)
    m.box(T("oak_planks"), (-4, -4, -4), (8, 8, 8)); m.box(T("stripped_oak_log"), (-2, -2, 4), (4, 4, 14))
    for k in range(4):
        p = m.part([0, 0, -2], (0, 0, k * math.pi / 2 + 0.25))
        m.box(T("oak_log"), (-1, -60, -1), (2, 56, 2), part=p)                          # spar
        m.box(T("white_wool"), (1, -58, 0), (12, 40, 1), part=p)                         # cloth
        for yy in (-58, -38, -19): m.box(T("stripped_oak_log"), (1, yy, -0.5), (12, 1, 1), part=p) # lattice
    return m
def piece_waterwheel():
    m = Model(8)
    m.box(T("stripped_oak_log"), (-3, -3, -10), (6, 6, 20))                              # axle through the hub
    for k in range(8):
        a = k * math.pi / 4
        p = m.part([0, 0, 0], (0, 0, a))
        m.box(T("stripped_oak_log"), (-1, -26, -5), (2, 24, 2), part=p); m.box(T("stripped_oak_log"), (-1, -26, 3), (2, 24, 2), part=p)   # spokes
        m.box(T("oak_planks"), (-11, -30, -6), (22, 4, 12), part=p)                     # rim
        q = m.part([0, 0, 0], (0, 0, a + math.pi / 8))
        m.box(T("spruce_planks"), (-1, -38, -6), (2, 9, 12), part=q)                    # paddles
    return m
def piece_engine_crank():
    m = Model(9)
    m.box(T("oak_planks"), (-8, 18, -8), (16, 6, 16)); m.box(T("piston_side"), (-6, 8, -6), (12, 10, 12))   # an engine, roughly
    m.box(T("redstone_block"), (-8, 0, -8), (16, 8, 16)) if False else None
    m.box(T("iron_block"), (8, 11, -1), (8, 2, 2))                                       # connecting rod to a crank
    m.box(T("stripped_oak_log"), (16, 6, -6), (4, 12, 4)); m.box(T("stripped_oak_log"), (16, 10, -18), (4, 4, 30))
    cog(m, 18, 12, 12, r=6, t="cut_copper", hub="copper_block")
    return m

def scene():
    """Waterwheel turning an axle into a gearbox, which turns a millstone."""
    m = Model(10)
    for x in range(-40, 40, 8):                                                           # the mill race
        m.box(T("stone_bricks"), (x, 40, -14), (8, 8, 28))
    m.box(T("water_still", WATER), (-40, 38, -10), (80, 2, 20))
    # waterwheel, its bottom in the water
    for k in range(8):
        a = k * math.pi / 4; p = m.part([0, 10, 0], (0, 0, a))
        m.box(T("oak_planks"), (-11, -30, -6), (22, 4, 12), part=p)
        m.box(T("stripped_oak_log"), (-1, -26, -1), (2, 24, 2), part=p)
        q = m.part([0, 10, 0], (0, 0, a + math.pi / 8)); m.box(T("spruce_planks"), (-1, -38, -6), (2, 9, 12), part=q)
    m.box(T("stripped_oak_log"), (-3, 7, -3), (6, 6, 70))                                 # axle to the mill
    for zz in (20, 44): m.box(T("cobblestone"), (-6, 13, zz), (12, 35, 6))               # posts
    # gearbox
    m.box(T("cobblestone"), (-8, 16, 66), (16, 32, 16))                                    # gearbox stand
    m.box(T("spruce_planks"), (-8, 0, 66), (16, 16, 16)); m.box(T("cut_copper"), (-6, 2, 65), (12, 12, 1))
    # millstone beside it
    m.box(T("stripped_oak_log"), (8, 6, 72), (16, 4, 4))
    m.box(T("cobblestone"), (24, 24, 66), (16, 24, 16))                                    # pedestal
    m.box(T("smooth_stone"), (24, 18, 66), (16, 6, 16))
    for (w, d) in ((14, 10), (10, 14)): m.box(T("stone"), (32 - w / 2, 12, 74 - d / 2), (w, 6, d))
    m.box(T("stripped_oak_log"), (31, 2, 73), (2, 10, 2))
    return m

PIECES = [("Axle", "carries turning power in a line; cheap wooden shafts", piece_axle, 2.2),
          ("Wooden Cog", "joins axles; turns power around corners", piece_cog, 2.6),
          ("Copper Cog", "a sturdier cog for heavy loads (copper ages green)", piece_copper_cog, 2.6),
          ("Gearbox", "an open frame like the crafter; one shaft in, two out", piece_gearbox, 2.4),
          ("Hand Crank", "turn it yourself: slow, cheap, early", piece_crank, 2.4),
          ("Millstone", "grinds grain to flour, cobble to sand, bones to meal", piece_millstone, 2.4),
          ("Windmill Sails", "on a tall tower; faster up high and in storms", piece_sails, 0.9),
          ("Waterwheel", "on a flowing river; stops when the river freezes", piece_waterwheel, 1.1),
          ("Engine Crank", "an engine's pulse turned into steady rotation", piece_engine_crank, 1.6)]

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    imgs = [(n, s, f().draw(sc)) for n, s, f, sc in PIECES]
    sc_img = scene().draw(1.0)
    cols = 3; cw = max(i.width for _, _, i in imgs) + 80; ch = max(i.height for _, _, i in imgs) + 90
    rows = (len(imgs) + cols - 1) // cols
    W = max(cols * cw + 60, sc_img.width + 120); H = 170 + rows * ch + sc_img.height + 140
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Rotational power: concept art", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Vintage Story's idea, vanilla's look: turning power from wind, water, muscle or engines, carried by axles and cogs to machines. Vanilla blocks only.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + (k % cols) * cw; y = 160 + (k // cols) * ch; base = y + ch - 70
        c.paste(im, (x + (cw - im.width) // 2, base - im.height), im)
        d.text((x + cw // 2, base + 22), n, font=R.font(24, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, base + 50), s, font=R.font(17), fill=SUB, anchor="mm")
    y = 160 + rows * ch + 20
    d.text((40, y), "Put together: a waterwheel turns an axle into a gearbox, which drives a millstone", font=R.font(26, True), fill=INK)
    c.paste(sc_img, ((W - sc_img.width) // 2, y + 60), sc_img)
    c = c.resize((c.width * 2 // 3, c.height * 2 // 3), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "rotation_concept.png"))
