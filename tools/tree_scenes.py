"""
Scene previews of the eastern US trees, rendered from the real block textures (isometric, textured voxels).
  python3 tools/tree_scenes.py
Writes docs/tree_scene_<name>.png.
"""
import math, os, random, sys
from PIL import Image, ImageDraw, ImageFont
sys.path.insert(0, os.path.dirname(__file__))
import trees as T
import tree_textures as TT

ROOT = os.path.join(os.path.dirname(__file__), "..")
BLK = os.path.join(ROOT, "src/main/resources/assets/ryancraft/textures/block")
S = 16                                              # pixels per block edge (1 texel = 1 pixel)
C30, S30 = math.cos(math.radians(30)), math.sin(math.radians(30))
OX, OY = C30 * S, S                                 # cube origin inside a sprite

def proj(x, y, z): return ((x - z) * C30 * S, (x + z) * S30 * S - y * S)

def tex(name): return Image.open(os.path.join(BLK, name + ".png")).convert("RGBA")

def shade(img, k):
    px = img.copy(); data = px.load()
    for x in range(px.width):
        for y in range(px.height):
            r, g, b, a = data[x, y]; data[x, y] = (int(r * k), int(g * k), int(b * k), a)
    return px

def face_sprite(texture, face, k):
    """Project a 16x16 texture onto one face of an isometric cube (affine), shaded."""
    t = shade(texture, k)
    corners = {"top": ((0, 1, 0), (1, 1, 0), (0, 1, 1)), "px": ((1, 1, 0), (1, 1, 1), (1, 0, 0)), "pz": ((0, 1, 1), (1, 1, 1), (0, 0, 1))}[face]
    p0, p1, p2 = [(proj(*c)[0] + OX, proj(*c)[1] + OY) for c in corners]
    # output -> texture: solve [x y] = p0 + u/16 (p1-p0) + v/16 (p2-p0)
    ax, ay = (p1[0] - p0[0]) / 16, (p1[1] - p0[1]) / 16
    bx, by = (p2[0] - p0[0]) / 16, (p2[1] - p0[1]) / 16
    det = ax * by - ay * bx
    ia, ib, ic, id_ = by / det, -bx / det, -ay / det, ax / det
    a, b = ia, ib; c = -(ia * p0[0] + ib * p0[1])
    d, e = ic, id_; f = -(ic * p0[0] + id_ * p0[1])
    W, H = int(2 * C30 * S) + 2, int(2 * S30 * S + S) + 2
    return t.transform((W, H), Image.AFFINE, (a, b, c, d, e, f), resample=Image.NEAREST, fillcolor=(0, 0, 0, 0))

class Sprites:
    def __init__(self): self.cache = {}
    def get(self, key, side, top, face):
        k = (key, face)
        if k not in self.cache:
            texture = top if face == "top" else side
            self.cache[k] = face_sprite(texture, face, {"top": 1.0, "px": 0.8, "pz": 0.62}[face])
        return self.cache[k]

# ---------------------------------------------------------------- terrain textures (our own)
def noise_tex(seed, base, amp, top_fringe=None):
    r = random.Random(seed); img = Image.new("RGBA", (16, 16)); px = img.load(); b = T.hexc(base)
    for x in range(16):
        for y in range(16):
            c = b
            if top_fringe and (y < 3 or (y == 3 and r.random() < .5)): c = T.hexc(top_fringe)
            d = r.randint(-amp, amp); px[x, y] = tuple(max(0, min(255, v + d)) for v in c) + (255,)
    return img

GRASS_TOP = noise_tex(1, "#6a9a42", 14); GRASS_SIDE = noise_tex(2, "#7a5a3a", 10, top_fringe="#6a9a42")
DIRT = noise_tex(3, "#7a5a3a", 10); STONE = noise_tex(4, "#a8a49a", 12)
WATER = Image.new("RGBA", (16, 16), (52, 92, 200, 170))
SYC_WHITE = TT.bark_side(99, "#e6e0d2", "mottled")

# ---------------------------------------------------------------- scene building
class Scene:
    def __init__(self, w, d, seed):
        self.w, self.d = w, d; self.r = random.Random(seed); self.blocks = {}   # (x,y,z) -> (key, side, top, opaque)
    def ground(self, height_fn, top="grass", water=None, rock=None):
        for x in range(self.w):
            for z in range(self.d):
                h = height_fn(x, z)
                for y in range(h - 1, h + 1):
                    if water and water(x, z) and y == h:
                        self.blocks[(x, y - 1, z)] = ("dirt", DIRT, DIRT, True)
                        self.blocks[(x, y, z)] = ("water", WATER, WATER, False)
                        continue
                    if y == h:
                        if rock and rock(x, z): self.blocks[(x, y, z)] = ("stone", STONE, STONE, True)
                        else: self.blocks[(x, y, z)] = ("grass", GRASS_SIDE, GRASS_TOP, True)
                    else: self.blocks[(x, y, z)] = ("dirt", DIRT, DIRT, True)
        self.h = height_fn
    def tree(self, species, x, z, seed):
        name, fn, bark, bark2, leaf, bloom, note = next(sp for sp in T.SPECIES if sp[0] == species)
        key = species.lower().replace(" ", "_")
        side, top, lv = tex(key + "_log"), tex(key + "_log_top"), tex(key + "_leaves")
        t = fn(seed); base = self.h(x, z) + 1
        for (vx, vy, vz), kind in t.v.items():
            p = (x + vx, base + vy, z + vz)
            if p in self.blocks and self.blocks[p][3] and kind in ("leaf", "bloom"): continue
            if kind == "log": self.blocks[p] = (key + "_log", side, top, True)
            elif kind == "log2": self.blocks[p] = ("syc_white", SYC_WHITE, top, True)
            else: self.blocks[p] = (key + "_leaves", lv, lv, False)

    def render(self):
        sp = Sprites(); blocks = self.blocks
        pts = [proj(x + a, y + b, z + c) for (x, y, z) in blocks for a in (0, 1) for b in (0, 1) for c in (0, 1)][::7]
        minx = min(p[0] for p in pts) - S; maxx = max(p[0] for p in pts) + S
        miny = min(p[1] for p in pts) - S; maxy = max(p[1] for p in pts) + S
        img = Image.new("RGBA", (int(maxx - minx), int(maxy - miny)), (0, 0, 0, 0))
        for (x, y, z) in sorted(blocks, key=lambda k: (k[0] + k[1] + k[2], k[1])):
            key, side, top, opaque = blocks[(x, y, z)]
            px, py = proj(x, y, z)
            dest = (int(round(px - OX - minx)), int(round(py - OY - miny)))
            for face, nb in (("pz", (x, y, z + 1)), ("px", (x + 1, y, z)), ("top", (x, y + 1, z))):
                n = blocks.get(nb)
                if n and n[3]: continue                                     # hidden behind something solid
                # leaves draw their inner faces too (like fancy leaves), so canopies look dense, not glassy
                if key == "water" and face != "top": continue
                img.alpha_composite(sp.get(key, side, top, face), dest=dest)
        return img

def font(size, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, size)
    return ImageFont.load_default()

def save(scene_img, title, subtitle, path):
    pad = 40
    out = Image.new("RGB", (scene_img.width + 2 * pad, scene_img.height + 2 * pad + 70), (178, 206, 236))
    out.paste(scene_img, (pad, pad + 70), scene_img)
    d = ImageDraw.Draw(out)
    d.text((pad, 22), title, font=font(32, True), fill=(30, 40, 50)); d.text((pad + 2, 62), subtitle, font=font(17), fill=(60, 72, 88))
    out.save(path); print("wrote", path, out.size)

def rolling(x, z, base=4): return base + int(round(1.2 * math.sin(x * 0.25) + 1.0 * math.cos(z * 0.21) + 0.6 * math.sin((x + z) * 0.4)))

if __name__ == "__main__":
    docs = os.path.join(ROOT, "docs")
    # 1. Hardwood forest
    sc = Scene(46, 46, 1)
    sc.ground(rolling)
    for sp_name, x, z in [("White Oak", 12, 10), ("Tulip Poplar", 30, 8), ("Sugar Maple", 34, 26), ("Red Oak", 14, 32), ("Shagbark Hickory", 24, 20),
                          ("American Beech", 36, 40), ("Black Walnut", 6, 22), ("Flowering Dogwood", 22, 36), ("Eastern Redbud", 28, 32), ("Red Maple", 40, 14),
                          ("Tulip Poplar", 8, 40)]:
        sc.tree(sp_name, x, z, sc.r.randint(0, 9999))
    save(sc.render(), "Kentucky hardwood forest", "White oak, red oak, sugar and red maple, tulip poplars, hickory, walnut and beech, with dogwood and redbud in the understory",
         os.path.join(docs, "tree_scene_forest.png"))
    # 2. Spring woodland edge
    sc = Scene(36, 30, 2)
    sc.ground(lambda x, z: rolling(x, z, 3))
    for sp_name, x, z in [("Eastern Redbud", 8, 8), ("Flowering Dogwood", 16, 6), ("Eastern Redbud", 24, 12), ("Flowering Dogwood", 10, 18),
                          ("Eastern Redbud", 28, 22), ("Red Oak", 20, 22), ("Flowering Dogwood", 4, 26)]:
        sc.tree(sp_name, x, z, sc.r.randint(0, 9999))
    save(sc.render(), "Spring", "Redbud and dogwood blooming along a woodland edge", os.path.join(docs, "tree_scene_spring.png"))
    # 3. Creek bottom with sycamores
    creek = lambda x, z: abs(z - (14 + 4 * math.sin(x * 0.18))) < 2.2
    sc = Scene(44, 30, 3)
    sc.ground(lambda x, z: 4 if creek(x, z) else rolling(x, z, 5), water=creek)
    for sp_name, x, z in [("Sycamore", 10, 6), ("Sycamore", 28, 22), ("Sycamore", 38, 8), ("Black Walnut", 20, 4), ("Red Maple", 18, 26)]:
        sc.tree(sp_name, x, z, sc.r.randint(0, 9999))
    save(sc.render(), "Creek bottom", "Big white-limbed sycamores along a stream, with a black walnut and a red maple", os.path.join(docs, "tree_scene_creek.png"))
    # 4. Cedar glade on limestone
    rr = random.Random(4)
    rocks = {(x, z) for x in range(40) for z in range(30) if rr.random() < 0.35}
    sc = Scene(40, 30, 4)
    sc.ground(lambda x, z: rolling(x, z, 4) + (x // 10), rock=lambda x, z: (x, z) in rocks)
    for sp_name, x, z in [("Eastern Red Cedar", 6, 6), ("Eastern Red Cedar", 14, 10), ("Eastern Red Cedar", 26, 6), ("Eastern Red Cedar", 32, 18),
                          ("Eastern Red Cedar", 10, 22), ("Eastern Red Cedar", 20, 24), ("Ohio Buckeye", 22, 14), ("Sassafras", 34, 26)]:
        sc.tree(sp_name, x, z, sc.r.randint(0, 9999))
    save(sc.render(), "Cedar glade", "Red cedars on rocky limestone ground, with a buckeye and sassafras", os.path.join(docs, "tree_scene_cedars.png"))
