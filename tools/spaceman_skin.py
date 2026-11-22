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


# ---------------------------------------------------------------- colour variants
import colorsys
VARIANTS = {            # name: (hue 0-1 or None to keep, saturation scale, brightness scale)
    "lavender": (None, 1.0, 1.0), "white": (None, 0.12, 1.22), "blue": (0.60, 1.5, 1.05),
    "red": (0.99, 1.9, 0.98), "orange": (0.07, 1.9, 1.05), "green": (0.36, 1.2, 1.0)}

def variant(base_skin, face_mask, hue, sat, val):
    """Same suit, new colour: shift the hue (and saturation/brightness) of every suit pixel. The face, the
    tan studs and gold glints stay as they are."""
    out = base_skin.copy(); px = out.load()
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            if a == 0 or (x, y) in face_mask: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if 0.03 < h < 0.18 and s > 0.15 and v > 0.45: continue           # tan studs, gold glints stay as they are
            if hue is not None: h = hue + (h - 0.75) * 0.3 if s > 0.05 else hue   # keep the small hue variation between suit and trim
            nr, ng, nb = colorsys.hsv_to_rgb(h % 1, min(1, s * sat), min(1, v * val))
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return out

def make_variants(docs):
    import render_iso as R
    base = make()
    orig = Image.open(os.path.join(HERE, "templates", "lost_spaceman_base.png")).convert("RGBA")
    face = {(x, y) for y in range(64) for x in range(64) if orig.getpixel((x, y)) != base.getpixel((x, y))}
    renders = []
    for name, (h, s, v) in VARIANTS.items():
        sk = variant(base, face, h, s, v); p = os.path.join(docs, f"lost_spaceman_{name}.png"); sk.save(p)
        renders.append((name, R.render("player", p[:-4], 2.0)))
    W = sum(r.width for _, r in renders) + 40 * len(renders) + 40; H = max(r.height for _, r in renders) + 90
    sheet = Image.new("RGB", (W, H), (243, 239, 230)); d = __import__("PIL.ImageDraw", fromlist=["ImageDraw"]).Draw(sheet)
    x = 40
    for name, r in renders:
        sheet.paste(r, (x, H - 70 - r.height), r); d.text((x + r.width // 2, H - 40), name.title(), font=R.font(24, True), fill=(46, 42, 36), anchor="mm")
        x += r.width + 40
    sheet.save(os.path.join(docs, "lost_spaceman_variants.png")); print("variants", sheet.size)

if __name__ == "__main__":
    make_variants(os.path.join(HERE, "..", "docs"))
