"""Mudling textures (64x32, matching client/mudling/MudlingModel): each biome kind painted from its biome's own block
textures (the classic 1.2.5 ones where they exist), with a face, a hat of the biome's stuff, rare gilded / mossy /
charred variants, and the white team sash (tinted in game).
  python3 tools/mudlings_assets.py"""
import os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
OUT = os.path.join(RES, "assets", "ryancraft", "textures", "entity", "mudling")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")
VANILLA = os.path.join(HERE, "templates", "mudling")   # modern vanilla textures the classic pack lacks

# boxes (name: texOffs u, v, w, h, d), as in MudlingModel
BOXES = {"head": (0, 0, 6, 6, 6), "hat": (24, 0, 7, 2, 7), "body": (0, 12, 6, 5, 3), "right_arm": (18, 12, 2, 5, 2),
         "left_arm": (26, 12, 2, 5, 2), "right_leg": (0, 20, 2, 4, 2), "left_leg": (8, 20, 2, 4, 2)}
GRASS, LEAF = (124, 189, 74), (89, 162, 60)

# kind: head, body, limbs, hat (None = no hat), hat tint, eye colour
KINDS = {
    "sod":        ("dirt", "dirt", "dirt", "grass_block_top", GRASS, (24, 18, 12)),
    "twig":       ("oak_planks", "oak_log", "oak_log", "oak_leaves", LEAF, (24, 18, 12)),
    "bark":       ("spruce_log", "spruce_log", "spruce_log", "spruce_leaves", (70, 110, 70), (232, 226, 200)),
    "mud":        ("mud", "mud", "mud", "lily_pad", (60, 130, 50), (226, 214, 180)),
    "sandstone":  ("sandstone", "sandstone", "sandstone", "sandstone_top", None, (40, 28, 16)),
    "terracotta": ("terracotta", "terracotta", "terracotta", None, None, (40, 22, 14)),
    "snowpack":   ("snow", "snow", "snow", "blue_ice", None, (24, 24, 30)),
    "vine":       ("jungle_planks", "jungle_log", "jungle_log", "vine", LEAF, (24, 18, 12)),
    "gravel":     ("gravel", "gravel", "gravel", "stone", None, (20, 20, 22)),
    "shell":      ("sand", "sand", "sand", "calcite", None, (40, 30, 18)),
    "pebble":     ("cobblestone", "cobblestone", "cobblestone", "iron_block", None, (16, 16, 18)),
    "cinder":     ("netherrack", "netherrack", "netherrack", "magma", None, (255, 200, 90)),
}

def tex(name):
    for d in (CLASSIC, VANILLA):
        p = os.path.join(d, name + ".png")
        if os.path.exists(p): return Image.open(p).convert("RGBA").crop((0, 0, 16, 16))
    raise FileNotFoundError(name)

def tinted(img, tint):
    if not tint: return img
    out = img.copy(); px = out.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a: v = (r + g + b) / 3 / 255; px[x, y] = (int(tint[0] * v * 1.2), int(tint[1] * v * 1.2), int(tint[2] * v * 1.2), a)
    return out

def paint_box(img, src, u, v, w, h, d, ox=0):
    """Fill a box's whole UV unwrap with a block texture, sampled at an offset so faces don't all start the same."""
    px = img.load(); sp = src.load()
    for y in range(v, v + d + h):
        for x in range(u, u + 2 * d + 2 * w):
            inside = (y < v + d and u + d <= x < u + d + 2 * w) or y >= v + d
            if inside and 0 <= x < img.width and 0 <= y < img.height:
                px[x, y] = sp[(x + ox) % 16, (y + ox // 2) % 16]

def face(img, eye):
    px = img.load(); fx, fy = 6, 6                      # the head's front face starts here
    for x, y in ((fx + 1, fy + 2), (fx + 4, fy + 2)): px[x, y] = eye + (255,)
    mouth = tuple(max(0, int(c * 0.35)) for c in px[fx + 2, fy + 4][:3]) + (255,)
    px[fx + 2, fy + 4] = mouth; px[fx + 3, fy + 4] = mouth

def kind_texture(k):
    head, body, limbs, hat, hat_tint, eye = KINDS[k]
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    paint_box(img, tex(head), *BOXES["head"], ox=3)
    paint_box(img, tex(body), *BOXES["body"], ox=5)
    for i, part in enumerate(("right_arm", "left_arm", "right_leg", "left_leg")): paint_box(img, tex(limbs), *BOXES[part], ox=2 + i * 3)
    if hat: paint_box(img, tinted(tex(hat), hat_tint), *BOXES["hat"], ox=1)
    face(img, eye)
    return img

def variant(img, kind, eye):
    """The rare colourings: gilded (gold), mossy (mottled moss), charred (near-black with embers)."""
    out = img.copy(); px = out.load(); r = random.Random(len(kind))
    if kind == "gilded": lo, hi = (120, 84, 20), (255, 236, 120)
    elif kind == "mossy": lo, hi = (34, 60, 22), (120, 160, 70)
    else: lo, hi = (14, 12, 12), (70, 62, 58)
    for y in range(out.height):
        for x in range(out.width):
            c = px[x, y]
            if not c[3] or c[:3] == eye: continue
            t = sum(c[:3]) / 765
            if kind == "mossy" and r.random() < 0.3: t *= 0.7
            px[x, y] = tuple(int(lo[i] + (hi[i] - lo[i]) * t) for i in range(3)) + (c[3],)
            if kind == "charred" and r.random() < 0.03: px[x, y] = (230, 110, 40, 255)
    return out

def sash():
    """A white sash over one shoulder and across the body, front and back, and an armband (tinted to the team)."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); px = img.load()
    u, v, w, h, d = BOXES["body"]
    for i in range(h + 1):                                # front (u+d, v+d) and back (u+2d+w, v+d), diagonally
        for fx in (u + d, u + 2 * d + w):
            x = fx + min(w - 1, i + 1); y = v + d + min(h - 1, i)
            px[x, y] = (255, 255, 255, 255)
            if x - 1 >= fx: px[x - 1, y] = (230, 230, 230, 255)
    for x in range(u, u + 2 * d + 2 * w): px[x, v + d] = (255, 255, 255, 255) if x % 3 else (230, 230, 230, 255)   # shoulder strap
    au, av, aw, ah, ad = BOXES["right_arm"]
    for x in range(au, au + 2 * ad + 2 * aw): px[x, av + ad + 1] = (255, 255, 255, 255)                          # armband
    return img

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for k, spec in KINDS.items():
        base = kind_texture(k); base.save(os.path.join(OUT, f"{k}.png"))
        for v in ("gilded", "mossy", "charred"): variant(base, v, spec[5]).save(os.path.join(OUT, f"{k}_{v}.png"))
    sash().save(os.path.join(OUT, "sash.png"))
    print("mudlings ok")
