"""
The Lost Spaceman's skin: the spacesuit skin in tools/templates/lost_spaceman_base.png, unchanged except the face.
The dark visor becomes see-through tinted glass (like stained glass) on the outer layer, and behind it, on the head
layer, a dead Steve's skull.
  python3 tools/spaceman_skin.py  ->  docs/lost_spaceman_skin.png and docs/lost_spaceman.png (render)
"""
import os, sys
from PIL import Image
sys.path.insert(0, os.path.dirname(__file__))
HERE = os.path.dirname(os.path.abspath(__file__))

BONE, BONE2, SOCKET, TOOTH = (226, 220, 200), (210, 204, 184), (40, 34, 36), (242, 238, 222)
GLASS = (90, 120, 190, 80)

def is_visor(c): return c[3] > 0 and (sum(c[:3]) < 240 or c[0] > 200)        # the dark visor and its gold glints

def skull_pixel(x, y):
    """The skull's face on the head's front, 8x8."""
    if y in (3, 4) and x in (1, 2, 5, 6): return SOCKET                         # eye sockets
    if y == 5 and x in (3, 4): return SOCKET                                    # nose hole
    if y == 6 and 1 <= x <= 6: return SOCKET if x % 2 else TOOTH                # teeth
    return BONE if (x + y) % 3 else BONE2

def make():
    img = Image.open(os.path.join(HERE, "templates", "lost_spaceman_base.png")).convert("RGBA"); px = img.load()
    # head layer: front face (8,8) and the visor's wrap onto the sides (right face 0,8 / left face 16,8)
    for y in range(8):
        for x in range(8):
            if is_visor(px[8 + x, 8 + y]): px[8 + x, 8 + y] = skull_pixel(x, y) + (255,)
            if is_visor(px[0 + x, 8 + y]): px[0 + x, 8 + y] = (BONE if y % 2 else BONE2) + (255,)
            if is_visor(px[16 + x, 8 + y]): px[16 + x, 8 + y] = (BONE if y % 2 else BONE2) + (255,)
    # outer layer: the visor turns to tinted glass; its gold glints stay, a little see-through
    for (x0, y0) in ((40, 8), (32, 8), (48, 8)):
        for y in range(8):
            for x in range(8):
                c = px[x0 + x, y0 + y]
                if is_visor(c): px[x0 + x, y0 + y] = (c[:3] + (200,)) if c[0] > 200 else GLASS
    return img

if __name__ == "__main__":
    import render_iso as R
    docs = os.path.join(HERE, "..", "docs")
    img = make(); path = os.path.join(docs, "lost_spaceman_skin.png"); img.save(path)
    r = R.render("player", path[:-4], 3.0)
    big = img.resize((256, 256), Image.NEAREST)
    out = Image.new("RGBA", (r.width + 300, max(r.height, 280)), (243, 239, 230, 255))
    out.paste(r, (0, 0), r); out.paste(big, (r.width + 30, 10), big)
    out.save(os.path.join(docs, "lost_spaceman.png")); print("wrote", out.size)
