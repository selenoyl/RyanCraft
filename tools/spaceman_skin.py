"""
The Lost Spaceman's skin (64x64, the standard player layout): a lavender-grey spacesuit with navy trim, black gloves
and boots, and a chest panel; under a helmet with a tinted glass visor, a dead Steve's skull.
  python3 tools/spaceman_skin.py  ->  docs/lost_spaceman_skin.png and docs/lost_spaceman.png (render)
"""
import os, sys, random
from PIL import Image
sys.path.insert(0, os.path.dirname(__file__))

R_ = random.Random(7)
SUIT = [(168, 150, 178), (157, 140, 163), (147, 131, 152), (174, 158, 191)]
NAVY, NAVY2 = (71, 73, 115), (77, 87, 137)
DARK, DARK2, BLACK = (26, 24, 32), (35, 36, 56), (15, 14, 16)
TAN, GOLD, GREY = (232, 200, 150), (222, 186, 96), (120, 116, 128)
BONE = [(226, 220, 200), (214, 208, 188), (236, 232, 214)]
HAIR = [(106, 70, 40), (86, 56, 32), (120, 82, 50)]
SOCKET = (40, 34, 36)

def suit(): return SUIT[R_.randrange(3)] if R_.random() < .85 else SUIT[3]
img = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); px = img.load()

def faces(u, v, w, h, d):
    """Regions of a box's faces in the skin: name -> (x, y, w, h)."""
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}

def paint(region, f):
    x0, y0, w, h = region
    for y in range(h):
        for x in range(w):
            c = f(x, y, w, h)
            if c: px[x0 + x, y0 + y] = c if len(c) == 4 else c + (255,)

def limb(u, v, glove=False, boot=False, front_dots=False):
    for name, reg in faces(u, v, 4, 12, 4).items():
        def f(x, y, w, h, name=name):
            if name in ("top", "bottom"): return BLACK if (boot and name == "bottom") else suit()
            if y in (3, 4) and not boot: return NAVY if (x + y) % 3 else NAVY2           # shoulder band
            if boot and y == 6: return NAVY                                                # knee band
            if (glove or boot) and y >= 9:
                if front_dots and name == "front" and y == 10 and x in (1, 2): return TAN
                return BLACK if y > 9 else DARK
            return suit()
        paint(reg, f)

# head: a dead Steve's skull, a few tufts of his hair left
for name, reg in faces(0, 0, 8, 8, 8).items():
    def f(x, y, w, h, name=name):
        bone = BONE[R_.randrange(3)]
        if name == "top": return HAIR[R_.randrange(3)] if R_.random() < .55 else bone
        if name == "back": return HAIR[R_.randrange(3)] if (y < 4 and R_.random() < .7) else bone
        if name in ("left", "right"): return HAIR[R_.randrange(3)] if (y < 2 and R_.random() < .6) else bone
        if name == "front":
            if y == 0 and R_.random() < .6: return HAIR[R_.randrange(3)]
            if y in (3, 4) and x in (1, 2, 5, 6): return SOCKET                            # eye sockets
            if y == 5 and x in (3, 4): return SOCKET if x == 3 else (60, 52, 52)          # nose hole
            if y == 6 and 2 <= x <= 5: return SOCKET if x % 2 else (240, 236, 220)        # teeth
            if y == 7 and 2 <= x <= 5: return (200, 194, 176)
        return bone
    paint(reg, f)

# helmet (hat layer): suit all round, a tinted glass visor in front with a glint
for name, reg in faces(32, 0, 8, 8, 8).items():
    def f(x, y, w, h, name=name):
        if name == "front":
            if 1 <= x <= 6 and 1 <= y <= 6:
                if (x, y) in ((5, 1), (6, 2)): return GOLD + (230,)
                if (x, y) in ((1, 5), (2, 6)): return (220, 230, 255, 170)
                return (90, 120, 190, 70)                                                   # stained-glass tint
            return NAVY if y == 7 else suit()
        if name == "top": return DARK if (x, y) == (3, 3) else suit()
        if name in ("left", "right") and y in (3, 4) and x in (3, 4): return NAVY2          # helmet bolts
        return suit()
    paint(reg, f)

# body: suit with a chest control panel and a belt
for name, reg in faces(16, 16, 8, 12, 4).items():
    def f(x, y, w, h, name=name):
        if name == "front":
            if 2 <= x <= 5 and 2 <= y <= 6:
                if (x, y) in ((3, 3), (5, 5)): return TAN
                if (x, y) in ((4, 4), (2, 6)): return (90, 160, 230)
                return DARK2 if (x + y) % 2 else DARK
            if y == 9: return NAVY
        if name in ("left", "right", "back") and y == 9: return NAVY
        return suit()
    paint(reg, f)
# jacket layer: the air pack on the back
x0, y0, w, h = faces(16, 32, 8, 12, 4)["back"]
for y in range(1, 10):
    for x in range(1, 7): px[x0 + x, y0 + y] = (GREY if (x in (1, 6) or y in (1, 9)) else (140, 136, 150)) + (255,)
px[x0 + 3, y0 + 3] = TAN + (255,); px[x0 + 4, y0 + 3] = (90, 160, 230, 255)

limb(40, 16, glove=True, front_dots=True); limb(32, 48, glove=True, front_dots=True)   # arms
limb(0, 16, boot=True, front_dots=True); limb(16, 48, boot=True, front_dots=True)      # legs

if __name__ == "__main__":
    import render_iso as R
    docs = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs")
    path = os.path.join(docs, "lost_spaceman_skin.png"); img.save(path)
    r = R.render("player", path[:-4], 3.0)
    big = img.resize((256, 256), Image.NEAREST)
    out = Image.new("RGBA", (r.width + 300, max(r.height, 280)), (243, 239, 230, 255))
    out.paste(r, (0, 0), r); out.paste(big, (r.width + 30, 10), big)
    out.save(os.path.join(docs, "lost_spaceman.png")); print("wrote", out.size)
