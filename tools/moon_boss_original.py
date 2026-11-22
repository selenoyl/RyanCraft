"""
The Great Moon Rabbit, our own design, drawn the way vanilla's bosses are: hand-shaded boxes (lit tops, darker
bottoms, dark edges), streaky fur, and a face painted with intent (glowing irises in dark sockets, an angry brow,
buck teeth). Three takes: the boss, its eclipse phase (second phase, at half health), and a moon-dust colourway.
  python3 tools/moon_boss_original.py <vanilla textures dir>  ->  docs/moon_boss_original.png
"""
import os, sys, math, random
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
from moon_concept import noise, Model, JADE, METEOR, WOOD, STONE_M, MOON_GLOW, GOLD

def fur(base, streak, seed):
    """Fur: a base colour with short vertical streaks, the way vanilla animal textures suggest hair."""
    r = random.Random(seed); im = noise(base, 7, seed); px = im.load()
    for _ in range(26):
        x, y = r.randrange(16), r.randrange(16)
        for k in range(r.randint(2, 4)): px[x, (y + k) % 16] = streak + (255,)
    return im

PALETTES = {   # fur, streak, belly, inner ear, eye glow, mark glow
    "boss":    ((206, 208, 222), (176, 178, 196), (240, 240, 246), (236, 160, 186), (110, 235, 255), (140, 225, 255)),
    "eclipse": ((70, 66, 112), (52, 48, 88), (120, 112, 168), (190, 90, 200), (255, 120, 220), (230, 110, 255)),
    "dust":    ((150, 146, 136), (124, 120, 112), (196, 192, 182), (200, 150, 150), (255, 210, 110), (255, 200, 120))}

def rabbit(kind):
    furc, streak, belly, inner, eye, mark = PALETTES[kind]
    F, Fs, B, I = fur(furc, streak, 1), fur(tuple(int(c * 0.82) for c in furc), streak, 2), fur(belly, tuple(int(c * 0.9) for c in belly), 3), noise(inner, 6, 4)
    EYE, MARK = Image.new("RGBA", (16, 16), eye + (255,)), Image.new("RGBA", (16, 16), mark + (255,))
    SOCK, WHITE = (24, 22, 34, 255), (250, 250, 250, 255)
    m = Model(70 + len(kind))
    sh = dict(shade=True)
    # hind legs and big feet
    for x in (-13, 5):
        m.box(F, (x, 30, -2), (8, 12, 14), **sh); m.box(Fs, (x, 42, -10), (8, 4, 16), **sh)
        m.box(MARK, (x + (8.05 if x > 0 else -0.25), 33, 2), (0.2, 6, 2)); m.box(MARK, (x + (8.05 if x > 0 else -0.25), 35, 4), (0.2, 2, 4))   # crescent
    m.box(B, (-4, 30, 12), (8, 8, 7), **sh)                                                   # tail
    # torso: fur, a pale belly, darker back
    m.box(F, (-9, 12, -4), (18, 22, 14), **sh); m.box(B, (-7, 15, -5.4), (14, 17, 2), **sh)
    # shoulder guard: a jade-studded moonstone pauldron, and a jade collar
    m.box(STONE_M, (-12, 10, -6), (8, 6, 12), **sh); [m.box(JADE, (-12.4, 11.5, z), (0.6, 2, 2)) for z in (-4, 0, 4)]
    m.box(JADE, (-8, 11, -5.5), (16, 2, 13), **sh)
    # head, with a face painted like vanilla's
    face = {}
    for (ex, ey) in ((2, 4), (9, 4)):
        for dx in range(4):
            for dy in range(3): face[(ex + dx, ey + dy)] = SOCK                               # dark sockets
        face[(ex + 1, ey + 1)] = eye + (255,); face[(ex + 2, ey + 1)] = eye + (255,)            # glowing irises
        face[(ex + 2, ey + 1)] = WHITE if kind != "eclipse" else eye + (255,)                  # a glint
    for (bx, by) in ((2, 2), (3, 2), (4, 3), (12, 2), (11, 2), (10, 3)): face[(bx, by)] = (40, 38, 54, 255)   # angry brows
    m.box(F, (-7, -2, -8), (14, 13, 12), paint=face, **sh)
    m.box(B, (-4, 5, -11), (8, 6, 3), **sh); m.box(noise(inner, 5, 9), (-1.5, 5, -11.6), (3, 2, 1))   # muzzle, nose
    m.box(Image.new("RGBA", (16, 16), WHITE), (-1.6, 10.6, -11.3), (1.4, 2.4, 0.6)); m.box(Image.new("RGBA", (16, 16), WHITE), (0.2, 10.6, -11.3), (1.4, 2.4, 0.6))   # buck teeth
    for x in (-9, 7): m.box(B, (x, 4, -6), (2, 6, 5), **sh)                                   # cheek tufts
    # ears: the left one torn, with a notch
    e = m.part([-4, -2, -1], (-0.12, 0, -0.24)); m.box(F, (-2, -22, -1.5), (4, 22, 3), part=e, **sh); m.box(I, (-1.2, -20, -1.8), (2.4, 17, 0.4), part=e)
    e = m.part([4, -2, -1], (-0.12, 0, 0.30)); m.box(F, (-2, -15, -1.5), (4, 15, 3), part=e, **sh); m.box(F, (-2, -19, -1.5), (2, 4, 3), part=e, **sh)
    m.box(I, (-1.2, -14, -1.8), (2.4, 11, 0.4), part=e)
    # crown, shaded gold with jade
    m.box(GOLD, (-6, -4, -7), (12, 2, 10), **sh)
    for x in (-6, -2, 2): m.box(GOLD, (x, -7, -7), (2, 3, 1), **sh); m.box(JADE, (x + 0.5, -6, -7.4), (1, 1, 0.4))
    # arms and the staff
    for sgn in (-1, 1):
        a = m.part([sgn * 7, 16, -3], (-1.15, sgn * 0.45, 0)); m.box(F, (-2, -1, -2), (4, 13, 4), part=a, **sh); m.box(B, (-2.5, 10, -2.5), (5, 4, 5), part=a, **sh)
    st = m.part([0, 20, -15], (0.35, 0, -0.55))
    m.box(WOOD, (-2, -30, -2), (4, 50, 4), part=st, **sh); m.box(METEOR, (-2.1, 20, -2.1), (4.2, 2.5, 4.2), part=st, **sh)
    for (w, d) in ((8, 6), (6, 8)): m.box(STONE_M, (-w / 2, -37, -d / 2), (w, 7, d), part=st, **sh)
    m.box(JADE, (-4.1, -38, -4.1), (8.2, 1, 8.2), part=st, **sh); m.box(MOON_GLOW if kind != "eclipse" else MARK, (-3, -38.4, -3), (6, 0.5, 6), part=st)
    if kind == "eclipse":                                                                      # the eclipse: a dark halo behind its head
        for k in range(8):
            p = m.part([0, 0, 6], (0, 0, k * math.pi / 4)); m.box(MARK, (-1, -20, 0), (2, 6, 1), part=p)
    return m

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    takes = [("The Great Moon Rabbit", "silver fur, glowing cyan eyes, a torn ear, buck teeth, a jade-studded shoulder guard", "boss"),
             ("Eclipse phase (half health)", "its fur darkens to the moon in eclipse; eyes blaze; a halo of light flares behind it", "eclipse"),
             ("Moon-dust colourway", "an alternative: grey regolith fur and amber eyes, like the moon's own surface", "dust")]
    imgs = [(n, s, rabbit(k).draw(2.4)) for n, s, k in takes]
    cw = max(i.width for _, _, i in imgs) + 40; ch = max(i.height for _, _, i in imgs) + 110
    W, H = 3 * cw + 60, 150 + ch
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "The Great Moon Rabbit: our own design, Minecraft boss style", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Hand-shaded like vanilla's mobs (lit tops, darker undersides, dark edges), with a face painted to look like it means business.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + k * cw; base = 140 + ch - 90
        c.paste(im, (x + (cw - im.width) // 2, base - im.height), im)
        d.text((x + cw // 2, base + 26), n, font=R.font(28, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, base + 60), s, font=R.font(16), fill=SUB, anchor="mm")
    c = c.resize((c.width * 3 // 4, c.height * 3 // 4), Image.LANCZOS); c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "moon_boss_original.png"))
