"""Crop and food item icons in the classic item style, drawn from shapes rather than pixel strings: each piece is a
filled shape shaded from a five-step ramp, lit from the top left (with a highlight glint), outlined in its own darkest
shade, the way vanilla's classic apple, potato and beetroot are. Used by tools/crops.py.
  python3 tools/food_art.py      (previews to the scratch sheet when run alone)"""
import math, random
from PIL import Image

def blank(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

def ramp(dark, light, n=5):
    return [tuple(int(dark[k] + (light[k] - dark[k]) * i / (n - 1)) for k in range(3)) for i in range(n)]

def ellipse(cx, cy, rx, ry, ang=0.0):
    ca, sa = math.cos(math.radians(ang)), math.sin(math.radians(ang))
    def inside(x, y):
        dx, dy = x + 0.5 - cx, y + 0.5 - cy
        u, v = dx * ca + dy * sa, -dx * sa + dy * ca
        return (u / rx) ** 2 + (v / ry) ** 2 <= 1.0
    return {(x, y) for y in range(16) for x in range(16) if inside(x, y)}

def shade(im, mask, rp, light=(-0.7, -0.75), seed=0, glint=True, outline=True, speck=0.0):
    """Lit from the top left: interior pixels pick a ramp step by how far toward the light they sit; edge pixels
    become the outline (ramp[0]); a two-pixel glint near the lit side."""
    if not mask: return
    px = im.load(); r = random.Random(seed)
    cx = sum(x for x, _ in mask) / len(mask); cy = sum(y for _, y in mask) / len(mask)
    R = max(1.0, max(math.hypot(x - cx, y - cy) for x, y in mask))
    edge = {(x, y) for x, y in mask if any((x + dx, y + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    for x, y in mask:
        if outline and (x, y) in edge: px[x, y] = rp[0] + (255,); continue
        t = 0.5 - 0.55 * ((x - cx) * light[0] + (y - cy) * light[1]) / R
        t += r.uniform(-0.08, 0.08)
        if speck and r.random() < speck: t -= 0.25
        i = max(1, min(len(rp) - 2, int(1 + t * (len(rp) - 2))))
        px[x, y] = rp[i] + (255,)
    if glint:
        gx = int(round(cx + light[0] * R * 0.45)); gy = int(round(cy + light[1] * R * 0.45))
        for p in ((gx, gy), (gx + 1, gy), (gx, gy + 1)):
            if p in mask and p not in edge: px[p] = rp[-1] + (255,)

def dots(im, pts, col):
    px = im.load()
    for x, y in pts:
        if 0 <= x < 16 and 0 <= y < 16: px[x, y] = col + (255,)

LEAF = [(26, 74, 20), (50, 122, 34), (78, 160, 46), (116, 196, 70), (170, 226, 120)]

def tomato():
    im = blank(); body = ellipse(7.5, 9, 6.2, 5.6)
    shade(im, body, [(110, 8, 8), (178, 16, 16), (222, 34, 30), (248, 70, 58), (255, 150, 135)], seed=1)
    calyx = {(6, 3), (7, 3), (8, 3), (9, 3), (5, 4), (7, 4), (8, 4), (10, 4), (7, 2), (8, 2)}
    dots(im, calyx, LEAF[1]); dots(im, {(7, 3), (8, 3), (8, 2)}, LEAF[3]); dots(im, {(8, 1)}, (70, 60, 30))
    return im

def pepper():
    im = blank(); body = ellipse(7, 9.5, 3.6, 6.0, -28) | ellipse(8.5, 6.5, 3.8, 3.2, -28)
    shade(im, body, [(100, 6, 6), (170, 14, 10), (214, 30, 20), (244, 66, 40), (255, 140, 110)], seed=2)
    dots(im, {(10, 2), (11, 1), (10, 3), (9, 3)}, LEAF[1]); dots(im, {(11, 1)}, LEAF[3]); dots(im, {(9, 2), (10, 1)}, LEAF[0])
    return im

def corn():
    im = blank(); cob = ellipse(8.5, 7.5, 7.2, 3.3, -45)
    CORN = [(140, 96, 8), (206, 150, 16), (238, 196, 40), (252, 224, 90), (255, 248, 170)]
    shade(im, cob, CORN, seed=3, glint=False)
    px = im.load()
    for x, y in cob:                                                     # rows of kernels
        if px[x, y][:3] != CORN[0] and (x + y) % 2 == 0:
            c = px[x, y]; px[x, y] = (max(0, c[0] - 30), max(0, c[1] - 34), max(0, c[2] - 10), 255)
    husk = ellipse(4.5, 11.5, 4.6, 2.0, -20) | ellipse(4.0, 10.5, 4.2, 1.6, -70)
    shade(im, husk, LEAF, seed=4, glint=False)
    dots(im, {(13, 1), (14, 0), (12, 1)}, (196, 170, 100))               # silk
    return im

def squash():
    im = blank(); body = ellipse(6.0, 10.0, 4.8, 4.6) | ellipse(9.5, 6.0, 2.6, 3.2, -40)
    shade(im, body, [(120, 60, 12), (196, 110, 30), (232, 150, 54), (248, 186, 96), (255, 224, 170)], seed=5)
    dots(im, {(11, 2), (12, 1), (12, 2)}, (60, 80, 30)); dots(im, {(13, 1)}, (90, 110, 40))
    return im

def cabbage():
    im = blank(); head = ellipse(7.5, 8.5, 6.6, 6.0)
    shade(im, head, [(40, 96, 30), (84, 150, 50), (128, 190, 80), (170, 222, 120), (222, 248, 190)], seed=6, glint=False)
    px = im.load(); pale = (176, 220, 128)
    for a in (-60, -20, 25, 70, 115, 160):                               # leaf veins fanning from the heart
        for k in range(2, 6):
            x = int(7.5 + k * math.cos(math.radians(a))); y = int(9 + k * math.sin(math.radians(a)) * 0.9)
            if (x, y) in head and px[x, y][:3] != [(40, 96, 30), (84, 150, 50), (128, 190, 80), (170, 222, 120), (222, 248, 190)][0]: px[x, y] = pale + (255,)
    dots(im, {(6, 7), (7, 7), (7, 8)}, (224, 248, 190))
    return im

def onion():
    im = blank(); bulb = ellipse(7.5, 9.5, 5.3, 5.0) | ellipse(7.5, 5.5, 2.4, 3.0)
    shade(im, bulb, [(110, 56, 16), (176, 104, 36), (212, 148, 66), (236, 190, 110), (252, 228, 170)], seed=7)
    dots(im, {(7, 1), (8, 1), (7, 2), (8, 2)}, (190, 150, 90)); dots(im, {(8, 0)}, (120, 160, 70))     # dry neck, a green tip
    dots(im, {(6, 15), (7, 15), (9, 15), (8, 15)}, (220, 200, 160))                                  # roots
    px = im.load()
    for y in range(5, 14):                                               # papery skin lines
        if (6, y) in bulb and y % 3 == 0: px[6, y] = (210, 160, 100, 255)
    return im

def beans():
    im = blank(); rp = [(70, 20, 14), (124, 40, 24), (164, 62, 36), (200, 96, 62), (236, 160, 130)]
    for cx, cy, a, s in ((5, 6, 30, 1), (10.5, 8, -20, 2), (6.5, 11.5, -50, 3)):
        m = ellipse(cx, cy, 3.4, 2.2, a); shade(im, m, rp, seed=8 + s, speck=0.18)
    return im

def rice():
    im = blank(); px = im.load(); r = random.Random(9)
    grain = ramp((150, 140, 120), (255, 255, 248))
    heap = ellipse(7.5, 11.5, 7.0, 4.0) | ellipse(7.5, 9, 4.5, 4.0)
    shade(im, heap, grain, seed=9, glint=False)
    for _ in range(14):                                                  # single grains catching the light
        x, y = r.randint(2, 13), r.randint(6, 14)
        if (x, y) in heap and (x + 1, y) in heap: px[x, y] = grain[4] + (255,); px[x + 1, y] = grain[3] + (255,)
    return im

def seeds(kind):
    """A small scatter, like vanilla's seed items: four seeds, each a tiny shaded oval in the crop's colours."""
    im = blank()
    rp = {"tomato_seeds": [(120, 90, 40), (196, 160, 80), (226, 196, 110), (244, 222, 150), (255, 244, 200)],
          "pepper_seeds": [(140, 120, 70), (214, 196, 130), (236, 222, 160), (248, 238, 190), (255, 252, 230)],
          "corn_seeds": [(140, 96, 8), (206, 150, 16), (238, 196, 40), (252, 224, 90), (255, 248, 170)],
          "squash_seeds": [(150, 136, 100), (214, 204, 170), (236, 228, 200), (248, 244, 224), (255, 255, 245)],
          "cabbage_seeds": [(30, 20, 14), (66, 44, 30), (96, 66, 44), (126, 92, 64), (170, 136, 104)]}[kind]
    big = kind in ("corn_seeds", "squash_seeds")
    for i, (cx, cy, a) in enumerate(((4.5, 4.5, 30), (11, 4, -40), (5.5, 11, -20), (11.5, 11.5, 50))):
        shade(im, ellipse(cx, cy, 2.8 if big else 2.3, 1.9 if big else 1.6, a), rp, seed=20 + i, glint=False)
    return im

# ---------------------------------------------------------------- the crops in the field, four stages each
FRUIT = {"tomato": [(110, 8, 8), (178, 16, 16), (222, 34, 30), (248, 70, 58), (255, 150, 135)],
         "pepper": [(100, 6, 6), (170, 14, 10), (214, 30, 20), (244, 66, 40), (255, 140, 110)],
         "beans": [(70, 100, 24), (150, 186, 64), (190, 218, 96), (222, 240, 140), (246, 252, 200)],
         "green_pepper": [(26, 74, 20), (50, 122, 34), (78, 160, 46), (116, 196, 70), (170, 226, 120)],
         "corn": [(140, 96, 8), (206, 150, 16), (238, 196, 40), (252, 224, 90), (255, 248, 170)],
         "squash": [(120, 60, 12), (196, 110, 30), (232, 150, 54), (248, 186, 96), (255, 224, 170)],
         "onion": [(110, 56, 16), (176, 104, 36), (212, 148, 66), (236, 190, 110), (252, 228, 170)],
         "rice": [(120, 100, 40), (190, 160, 70), (222, 196, 100), (240, 222, 140), (252, 244, 190)]}
STEM = (70, 110, 34)

def leaf(im, cx, cy, l, w, ang, seed):
    shade(im, ellipse(cx, cy, l, w, ang), LEAF, seed=seed, glint=False, outline=False)

def stem(im, x0, y0, x1, y1, col=STEM):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0)) * 2 + 1
    for k in range(n + 1):
        x = round(x0 + (x1 - x0) * k / n); y = round(y0 + (y1 - y0) * k / n)
        if 0 <= x < 16 and 0 <= y < 16: px[x, y] = col + (255,)

def crop_stage(name, st):
    im = blank(); r = random.Random(f"{name}{st}"); top = 15 - [4, 7, 10, 13][st]
    if name in ("tomato", "pepper", "beans"):                    # a leafy bush on two or three stems
        xs = [4, 8, 12] if st else [6, 10]
        for x in xs:
            stem(im, x, 15, x + r.choice((-1, 0, 1)), top)
            for k in range(st + 1):
                y = 15 - 2 - k * 3
                if y < top: break
                leaf(im, x - 1.6, y, 2.0, 1.1, 20, r.randint(0, 99)); leaf(im, x + 1.6, y - 1, 2.0, 1.1, -20, r.randint(0, 99))
        if st == 2:
            for _ in range(3): x, y = r.randint(3, 12), r.randint(top + 1, 11); dots(im, {(x, y)}, (250, 236, 150))
        if st == 3:
            for x, y in ((4, 7), (9, 5), (12, 9), (6, 11), (10, 12)):
                if name == "beans": shade(im, ellipse(x + 0.5, y + 1, 0.9, 2.2, 10), FRUIT["beans"], seed=x, glint=False)
                elif name == "pepper":                                # long peppers hanging, some still green
                    shade(im, ellipse(x + 0.5, y + 1.5, 1.0, 2.2, 10), FRUIT["pepper" if x % 3 else "green_pepper"], seed=x)
                else: shade(im, ellipse(x + 0.5, y + 0.5, 1.6, 1.6, 0), FRUIT[name], seed=x)
    elif name == "corn":                                         # a tall stalk, long arching leaves, cobs and a tassel
        for x in (5, 11):
            stem(im, x, 15, x, top if st < 3 else 1, (96, 140, 46))
            for k in range(st + 1):
                y = 13 - k * 3
                leaf(im, x - 2.2, y, 2.8, 0.9, 35, r.randint(0, 99)); leaf(im, x + 2.2, y - 1, 2.8, 0.9, -35, r.randint(0, 99))
            if st == 3:
                shade(im, ellipse(x + 1.5, 7.5, 1.2, 2.6, 15), FRUIT["corn"], seed=x)
                dots(im, {(x, 0), (x - 1, 1), (x + 1, 1)}, (214, 190, 120))
    elif name == "rice":                                         # fine blades in a clump; golden drooping heads when grown
        for i in range(7):
            x = 2 + i * 2; h = top + r.randint(0, 2)
            stem(im, x, 15, x + r.choice((-1, 1)), h, LEAF[2] if i % 2 else LEAF[1])
            if st == 3: shade(im, ellipse(x + 1, h + 1.5, 0.9, 1.8, 25), FRUIT["rice"], seed=i, glint=False, outline=False)
    elif name == "onion":                                        # upright hollow leaves; bulbs swelling at the ground
        for i, x in enumerate((4, 7, 10, 12)):
            stem(im, x, 15, x + r.choice((-1, 0, 1)), top + i % 2, LEAF[2 + i % 2])
        if st >= 2:
            for x in (5, 11): shade(im, ellipse(x, 14, 2.4 if st == 3 else 1.7, 1.8 if st == 3 else 1.3), FRUIT["onion"], seed=x)
    elif name == "cabbage":                                      # a rosette of broad leaves closing into a head
        rr = [2.2, 3.4, 4.6, 5.8][st]
        for a in range(0, 360, 60):
            leaf(im, 7.5 + rr * 0.55 * math.cos(math.radians(a)), 15 - rr * 0.6 + rr * 0.3 * math.sin(math.radians(a)), rr * 0.6, rr * 0.38, a, a)
        if st >= 2: shade(im, ellipse(7.5, 15 - rr * 0.6, rr * 0.55, rr * 0.5), [(40, 96, 30), (84, 150, 50), (128, 190, 80), (170, 222, 120), (222, 248, 190)], seed=st)
    elif name == "squash":                                       # broad leaves low and sprawling; a squash in the leaves
        for _ in range(2 + st * 2):
            leaf(im, r.uniform(3, 12), r.uniform(16 - [5, 7, 9, 10][st], 14), 2.3, 1.6, r.uniform(-40, 40), r.randint(0, 99))
        if st == 3:
            shade(im, ellipse(6, 12.5, 3.2, 2.4, -10) | ellipse(9.3, 11.2, 1.8, 1.5, -30), FRUIT["squash"], seed=5)
        if st == 2: dots(im, {(8, 9), (9, 9), (8, 10)}, (250, 200, 60))      # a flower
    return im

ICONS = {"tomato": tomato, "pepper": pepper, "corn": corn, "squash": squash, "cabbage": cabbage, "onion": onion, "beans": beans, "rice": rice}

if __name__ == "__main__":
    st = Image.new("RGBA", (8 * 4 * 68, 68), (120, 150, 90, 255))
    for i, n in enumerate(ICONS):
        for k in range(4):
            t = crop_stage(n, k).resize((64, 64), Image.NEAREST); st.paste(t, ((i * 4 + k) * 68 + 2, 2), t)
    st.save("/tmp/claude-0/-home-user/d01fb490-694d-5325-b287-f3704fa7dedc/scratchpad/crop_art.png")
    names = list(ICONS) + ["tomato_seeds", "pepper_seeds", "corn_seeds", "squash_seeds", "cabbage_seeds"]
    sheet = Image.new("RGBA", (len(names) * 100, 100), (139, 139, 139, 255))
    for i, n in enumerate(names):
        t = (ICONS[n]() if n in ICONS else seeds(n)).resize((96, 96), Image.NEAREST); sheet.paste(t, (i * 100 + 2, 2), t)
    sheet.save("/tmp/claude-0/-home-user/d01fb490-694d-5325-b287-f3704fa7dedc/scratchpad/food_art.png")
