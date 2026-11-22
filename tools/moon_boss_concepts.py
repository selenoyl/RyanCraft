"""
Four takes on the Great Moon Rabbit in the style of Minecraft's own bosses (Warden, Ender Dragon, Wither, Elder
Guardian): darker palettes, glowing eyes and markings, chunky menacing silhouettes.
  python3 tools/moon_boss_concepts.py <vanilla textures dir>  ->  docs/moon_boss_concepts.png
"""
import os, sys, math
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
import moon_concept as MC
from moon_concept import noise, Model, MOONSTONE, JADE, METEOR, WOOD, STONE_M, MOON_GLOW

DARK = noise((44, 46, 64), 10, 40, [((34, 36, 52), 20), ((58, 60, 82), 12)])
NIGHT = noise((28, 30, 44), 8, 41, [((20, 22, 34), 18)])
BONE = noise((214, 210, 196), 8, 42, [((190, 186, 170), 16)])
CYAN = Image.new("RGBA", (16, 16), (90, 240, 255, 255))
VIOLET = Image.new("RGBA", (16, 16), (200, 110, 255, 255))
CRACK = noise((150, 255, 210), 6, 43)
PLATE = noise((170, 172, 186), 10, 44, [((140, 142, 158), 18)])

def staff(m, pivot, rot, scale=1.0):
    st = m.part(pivot, rot); k = scale
    m.box(WOOD, (-2 * k, -30 * k, -2 * k), (4 * k, 50 * k, 4 * k), part=st); m.box(METEOR, (-2.1 * k, 20 * k, -2.1 * k), (4.2 * k, 2.5 * k, 4.2 * k), part=st)
    for (w, d) in ((8, 6), (6, 8)): m.box(STONE_M, (-w / 2 * k, -37 * k, -d / 2 * k), (w * k, 7 * k, d * k), part=st)
    m.box(JADE, (-4.1 * k, -38 * k, -4.1 * k), (8.2 * k, 1 * k, 8.2 * k), part=st); m.box(MOON_GLOW, (-3 * k, -38.4 * k, -3 * k), (6 * k, 0.5 * k, 6 * k), part=st)

def ears(m, fur, inner, y, spread=0.22, length=22, tip=None):
    for sgn in (-1, 1):
        e = m.part([sgn * 4, y, -1], (-0.12, 0, sgn * spread))
        m.box(fur, (-2, -length, -1.5), (4, length, 3), part=e); m.box(inner, (-1.2, -length + 2, -1.8), (2.4, length - 5, 0.4), part=e)
        if tip: m.box(tip, (-2, -length - 2, -1.5), (4, 3, 3), part=e)

def warden():
    """Warden-style: hulking, eyeless, dark; a glowing moon core in its chest; ears that 'listen'."""
    m = Model(50)
    for x in (-13, 5): m.box(DARK, (x, 30, -2), (8, 12, 14)); m.box(DARK, (x, 42, -10), (8, 4, 16))
    m.box(DARK, (-11, 8, -5), (22, 24, 15))                                                 # broad, heavy torso
    m.box(NIGHT, (-5, 14, -5.5), (10, 10, 1)); m.box(CYAN, (-3, 16, -5.8), (6, 6, 0.5))     # chest cavity, glowing core
    for sgn in (-1, 1):                                                                      # long arms hanging to the ground
        a = m.part([sgn * 13, 10, 0], (-0.35, 0, sgn * 0.12)); m.box(DARK, (-2.5, 0, -2.5), (5, 26, 5), part=a); m.box(NIGHT, (-3, 25, -3), (6, 4, 6), part=a)
    m.box(DARK, (-7, -3, -8), (14, 12, 12))                                                 # eyeless head
    m.box(NIGHT, (-4, 4, -10), (8, 5, 2)); m.box(CYAN, (-2, 6, -10.2), (4, 1, 0.3))          # a glowing mouth slit
    for x in (-6, 3): m.box(CYAN, (x, 1, -8.2), (3, 1, 0.3))                                 # glowing scars where eyes would be
    ears(m, DARK, CYAN, -3, spread=0.35, length=24)                                          # ears glow when it hears you
    staff(m, [14, 30, -6], (0.2, 0, 0.15))
    return m

def dragon():
    """Ender Dragon-style: armoured in moonstone plates, jade spines down the back, glowing violet eyes."""
    m = Model(51)
    for x in (-13, 5): m.box(PLATE, (x, 30, -2), (8, 12, 14)); m.box(DARK, (x, 42, -10), (8, 4, 16))
    m.box(DARK, (-9, 12, -4), (18, 22, 14))
    m.box(PLATE, (-9.5, 12, -5.5), (19, 10, 2)); m.box(PLATE, (-9.5, 24, -5.5), (19, 6, 2))  # breastplates
    for i in range(5): m.box(JADE, (-1.5, 10 + i * 5, 9 + (i % 2)), (3, 4, 4))               # jade spines down the back
    m.box(DARK, (-7, -2, -8), (14, 13, 12)); m.box(PLATE, (-7.5, -3, -8.5), (15, 4, 13))     # head and brow plate
    m.box(DARK, (-4, 5, -12), (8, 6, 4))                                                     # long muzzle, dragon-like
    for x in (-6, 2.5): m.box(VIOLET, (x, 2, -8.3), (3.5, 2, 0.4))
    m.box(BONE, (-3, 10, -12.2), (1, 2, 0.4)); m.box(BONE, (2, 10, -12.2), (1, 2, 0.4))      # fangs
    ears(m, DARK, VIOLET, -3, spread=0.28, length=24, tip=PLATE)
    for sgn in (-1, 1):
        a = m.part([sgn * 7, 16, -3], (-1.15, sgn * 0.45, 0)); m.box(DARK, (-2, -1, -2), (4, 13, 4), part=a); m.box(PLATE, (-2.5, 10, -2.5), (5, 4, 5), part=a)
    staff(m, [0, 20, -15], (0.35, 0, -0.55))
    return m

def wither():
    """Wither-style: floating, gaunt and bony, three-eared, glowing markings; no legs, a trailing tail of dust."""
    m = Model(52)
    m.box(BONE, (-1.5, 10, -1.5), (3, 26, 3))                                                # spine
    for i in range(4): m.box(BONE, (-8, 14 + i * 4, -3), (16, 2, 6))                         # ribs
    m.box(NIGHT, (-6, 14, -2), (12, 14, 4))
    for i in range(3): m.box(MOON_GLOW, (-3 + i, 36 + i * 3, -1), (4 - i, 3, 3))             # trailing moon-dust tail
    m.box(BONE, (-7, -2, -8), (14, 12, 12))                                                  # skull-like head
    for x in (-5.5, 2.5): m.box(NIGHT, (x, 1, -8.3), (3, 3, 0.4)); m.box(CYAN, (x + 1, 2, -8.6), (1, 1, 0.3))
    m.box(NIGHT, (-3, 7, -8.3), (6, 2, 0.4))
    ears(m, BONE, NIGHT, -2, spread=0.32, length=20, tip=NIGHT)
    e = m.part([0, -2, -1], (-0.1, 0, 0)); m.box(BONE, (-2, -18, -1.5), (4, 18, 3), part=e)  # a third, broken ear
    for sgn in (-1, 1):                                                                       # two side heads, like the Wither's
        h = m.part([sgn * 13, 6, 0], (0, sgn * -0.3, 0)); m.box(BONE, (-4, -4, -4), (8, 8, 8), part=h)
        m.box(CYAN, (-3, -2, -4.3), (2, 1, 0.3), part=h); m.box(CYAN, (1, -2, -4.3), (2, 1, 0.3), part=h)
        m.box(BONE, (-1.5, -12, -1), (3, 8, 2), part=h)
    staff(m, [8, 14, -8], (0.6, 0, 0.3))
    return m

def statue():
    """Elder Guardian-style: an ancient moonstone statue of a rabbit come to life, glowing cracks, one great eye."""
    m = Model(53)
    m.box(MOONSTONE, (-14, 36, -14), (28, 8, 28))                                            # its plinth
    for x in (-13, 5): m.box(MOONSTONE, (x, 24, -2), (8, 12, 14)); m.box(MOONSTONE, (x, 32, -10), (8, 4, 12))
    m.box(MOONSTONE, (-10, 4, -5), (20, 22, 15))
    for (x, y, h) in ((-6, 6, 9), (3, 12, 8), (-2, 18, 6)): m.box(CRACK, (x, y, -5.3), (1, h, 0.4))   # glowing cracks
    m.box(MOONSTONE, (-8, -9, -9), (16, 14, 13))
    m.box(NIGHT, (-3, -5, -9.3), (6, 5, 0.4)); m.box(CRACK, (-1.5, -4, -9.5), (3, 3, 0.3))   # one great glowing eye
    m.box(JADE, (-8.5, -11, -9.5), (17, 2, 14))                                              # a jade circlet
    ears(m, MOONSTONE, JADE, -9, spread=0.18, length=24, tip=JADE)
    for sgn in (-1, 1):
        a = m.part([sgn * 7, 10, -3], (-1.15, sgn * 0.45, 0)); m.box(MOONSTONE, (-2.5, -1, -2.5), (5, 13, 5), part=a)
    staff(m, [0, 14, -15], (0.35, 0, -0.55), scale=1.1)
    return m

CONCEPTS = [("A: Warden-style", "Moon Beast: hulking and eyeless, a glowing moon core in its chest; its ears glow when it hears you", warden),
            ("B: Ender Dragon-style", "Armoured Moon Rabbit: moonstone plates, jade spines, glowing violet eyes and fangs", dragon),
            ("C: Wither-style", "Hollow Moon Rabbit: floating, bony, with two side heads and a trailing tail of moon dust", wither),
            ("D: Elder Guardian-style", "Moonstone Idol: the temple's statue come to life; glowing cracks, one great eye", statue)]

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    imgs = [(n, s, f().draw(2.0)) for n, s, f in CONCEPTS]
    cw = max(i.width for _, _, i in imgs) + 60; ch = max(i.height for _, _, i in imgs) + 110
    W, H = 2 * cw + 60, 150 + 2 * ch
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "The Great Moon Rabbit: boss concepts", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Four takes in the style of Minecraft's own bosses. All carry the mortar staff.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + (k % 2) * cw; y = 140 + (k // 2) * ch; base = y + ch - 90
        c.paste(im, (x + (cw - im.width) // 2, base - im.height), im)
        d.text((x + cw // 2, base + 26), n, font=R.font(28, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, base + 60), s, font=R.font(17), fill=SUB, anchor="mm")
    c = c.resize((c.width * 3 // 4, c.height * 3 // 4), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "moon_boss_concepts.png"))
