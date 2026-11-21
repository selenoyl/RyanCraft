"""
Isometric "field guide" sheet of RyanCraft's mobs, rendered from the same models and textures the game uses.
  python3 tools/render_iso.py [out.png]
Spec-driven critters come from assets/ryancraft/critter_models/*.json; boar, deer, duck and turkey are
transcribed from their hand-written Java models below. Everything is drawn at one scale, so sizes compare.
"""
import json, math, os, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/ryancraft")
PX = 10                          # screen pixels per model pixel
YAW, ELEV = math.radians(-35), math.radians(30)
LIGHT = np.array([-0.35, 0.85, 0.4]); LIGHT /= np.linalg.norm(LIGHT)

def B(uv, frm, size, mirror=False): return {"uv": uv, "from": frm, "size": size, "mirror": mirror}
def Pt(pivot, boxes, rot=(0, 0, 0)): return {"pivot": pivot, "rot": rot, "boxes": boxes}

HAND = {  # Java models (BoarModel, DeerModel, DuckModel, TurkeyModel)
    "boar": (64, 64, [
        Pt([0, 12, -8], [B([0, 0], [-4, -4, -6], [8, 8, 6]), B([28, 0], [-2, 0, -9], [4, 3, 3]),
                         B([42, 0], [-3, 0, -8], [1, 3, 1]), B([42, 0], [2, 0, -8], [1, 3, 1], True)]),
        Pt([0, 13, 0], [B([0, 20], [-5, -5, -8], [10, 9, 16]), B([28, 6], [-1, -7, -7], [2, 2, 12])]),
        *[Pt([x, 17, z], [B([0, 48], [-2, 0, -2], [4, 7, 4])]) for x in (-3, 3) for z in (5, -5)]]),
    "deer": (64, 64, [
        Pt([0, 8, -7], [B([0, 0], [-2, -7, -2], [4, 9, 4]), B([16, 0], [-3, -10, -6], [6, 5, 7]), B([42, 0], [-2, -8, -9], [4, 3, 3])]),
        Pt([0, 11, 0], [B([0, 18], [-4, -4, -8], [8, 8, 16])]),
        Pt([0, 7, 8], [B([48, 30], [-1, 0, 0], [2, 3, 1])], (0.4, 0, 0)),
        *[Pt([x, 15, z], [B([48, 18], [-1.5, 0, -1.5], [3, 9, 3])]) for x in (-2.5, 2.5) for z in (6, -6)]]),
    "duck": (32, 32, [
        Pt([0, 17, -3], [B([0, 0], [-2, -5, -2], [4, 5, 3]), B([14, 0], [-1.5, -2, -4], [3, 1, 2])]),
        Pt([0, 19, 0], [B([0, 9], [-3, -3, -4], [6, 5, 8])]),
        Pt([-3, 16, 0], [B([0, 22], [-1, 0, -3], [1, 3, 6])]), Pt([3, 16, 0], [B([0, 22], [0, 0, -3], [1, 3, 6], True)]),
        *[Pt([x, 21, 1], [B([24, 0], [-0.5, 0, -0.5], [1, 3, 1])]) for x in (-1.5, 1.5)]]),
    "turkey": (64, 64, [
        Pt([0, 11, -4], [B([0, 0], [-1.5, -6, -1.5], [3, 6, 3]), B([12, 0], [-2, -9, -3], [4, 4, 4]),
                         B([28, 0], [-1, -7.5, -5], [2, 1, 2]), B([28, 4], [-0.5, -6.5, -4], [1, 3, 1])]),
        Pt([0, 14, 0], [B([0, 16], [-4, -4, -5], [8, 8, 10])]),
        Pt([0, 12, 4.5], [B([0, 46], [-8, -12, 0], [16, 12, 1])], (-0.3, 0, 0)),
        Pt([-4, 10.5, 0], [B([36, 16], [-1, 0, -4], [1, 6, 8])]), Pt([4, 10.5, 0], [B([36, 16], [0, 0, -4], [1, 6, 8], True)]),
        *[Pt([x, 18, 1], [B([36, 0], [-0.5, 0, -0.5], [1, 6, 1])]) for x in (-1.5, 1.5)]]),
}

def deer_antlers(stage, drop=False, head_pivot=None):
    spec = json.load(open(os.path.join(ROOT, "critter_models", "deer_antlers.json")))
    hp = head_pivot or spec["head_pivot"]; out = []
    if isinstance(stage, str) and not stage.isdigit(): keys = [stage]          # rare racks: cactus, palmated, lopsided
    else:
        stage = int(stage); keys = [str(stage)] + ([f"drop{stage}"] if drop and stage >= 3 else [])
    for k in keys:
        for p in spec["groups"][k]:
            out.append({"pivot": [hp[i] + p["pivot"][i] for i in range(3)], "rot": p["rot"], "boxes": p["boxes"]})
    return out

def load_model(name):
    if name.startswith("deer:"):            # deer:<stage|rare>[:drop]
        bits = name.split(":"); tw, th, parts = HAND["deer"]
        return (tw, th), parts + deer_antlers(bits[1], len(bits) > 2)
    if name.startswith("mount:"):           # mount:<stage|rare>[:drop]
        bits = name.split(":")
        spec = json.load(open(os.path.join(ROOT, "critter_models", "deer_mount.json")))
        return tuple(spec["texture"]), spec["parts"] + deer_antlers(bits[1], len(bits) > 2, head_pivot=[0, 21, -1])
    if name in HAND:
        tw, th, parts = HAND[name]; return (tw, th), parts
    spec = json.load(open(os.path.join(ROOT, "critter_models", name + ".json")))
    return tuple(spec["texture"]), spec["parts"]

def rot_matrix(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]]); Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Rz @ Ry @ Rx

def faces(box):
    """(region u,v,w,h ; P(s,t) as origin + s*E1 + t*E2) for each face, in box-local model space (y down)."""
    u, v = box["uv"]; x0, y0, z0 = box["from"]; w, h, d = box["size"]; x1, y1, z1 = x0 + w, y0 + h, z0 + d
    W, H, D = int(w), int(h), int(d)
    F = [((u + D, v, W, D), (x0, y0, z1), (w, 0, 0), (0, 0, -d)),                 # top
         ((u + D, v + D, W, H), (x0, y0, z0), (w, 0, 0), (0, h, 0)),             # front (-z)
         ((u, v + D, D, H), (x0, y0, z1), (0, 0, -d), (0, h, 0)),                # right (x0)
         ((u + D + W, v + D, D, H), (x1, y0, z0), (0, 0, d), (0, h, 0)),         # left (x1)
         ((u + 2 * D + W, v + D, W, H), (x1, y0, z1), (-w, 0, 0), (0, h, 0)),    # back (+z)
         ((u + D + W, v, W, D), (x0, y1, z0), (w, 0, 0), (0, 0, d))]             # bottom
    out = []
    for reg, o, e1, e2 in F:
        o, e1, e2 = np.array(o, float), np.array(e1, float), np.array(e2, float)
        if box.get("mirror"):     # mirrored box = the plain box reflected across its centre in x
            o[0] = x0 + x1 - o[0]; e1[0] = -e1[0]; e2[0] = -e2[0]
        out.append((reg, o, e1, e2))
    return out

def render(name, textures, scale=1.0, pose=None):
    (tw, th), parts = load_model(name)
    tex = np.array(Image.open(os.path.join(ROOT, "textures/entity", textures + ".png")).convert("RGBA")).astype(float)
    cy_, sy_ = math.cos(YAW), math.sin(YAW); ce, se = math.cos(ELEV), math.sin(ELEV)
    def view(p):                         # model space (y down) -> world (x, -y, -z) -> camera
        x, y, z = p[..., 0] * scale, -p[..., 1] * scale, -p[..., 2] * scale
        x2 = x * cy_ + z * sy_; z2 = -x * sy_ + z * cy_
        y3 = y * ce - z2 * se; z3 = y * se + z2 * ce
        return np.stack([x2, y3, z3], -1)
    polys = []
    for part in parts:
        R = rot_matrix(*part["rot"]); piv = np.array(part["pivot"], float)
        for box in part["boxes"]:
            for (ru, rv, rw, rh), o, e1, e2 in faces(box):
                if rw == 0 or rh == 0: continue
                O, E1, E2 = R @ o + piv, R @ e1, R @ e2
                n = np.cross(E1, E2); nl = np.linalg.norm(n)
                if nl < 1e-9: continue
                nw = np.array([n[0], -n[1], -n[2]]) / nl          # world normal
                # outward check: normal must point away from box centre
                c = np.array(box["from"], float) + np.array(box["size"], float) / 2
                if box.get("mirror"): pass
                cw = R @ c + piv
                if np.dot(R @ (o + (e1 + e2) / 2) + piv - cw, n) < 0: nw = -nw
                bright = 0.5 + 0.5 * max(0.0, float(np.dot(nw, LIGHT)))
                polys.append((view(O), view(O + E1), view(O + E2), (ru, rv, rw, rh), bright))
    allpts = np.array([p for poly in polys for p in poly[:3]] + [poly[1] + poly[2] - poly[0] for poly in polys])
    minx, maxx = allpts[:, 0].min(), allpts[:, 0].max(); miny, maxy = allpts[:, 1].min(), allpts[:, 1].max()
    Wd, Ht = int((maxx - minx) * PX) + 4, int((maxy - miny) * PX) + 4
    img = np.zeros((Ht, Wd, 4)); zbuf = np.full((Ht, Wd), -1e9)
    def scr(p): return np.array([(p[0] - minx) * PX + 2, (maxy - p[1]) * PX + 2]), p[2]
    for A, Bp, C, (ru, rv, rw, rh), bright in polys:
        (a, za), (b, zb), (c, zc) = scr(A), scr(Bp), scr(C)
        M = np.array([b - a, c - a]).T
        if abs(np.linalg.det(M)) < 1e-6: continue
        Minv = np.linalg.inv(M)
        d = b + c - a
        x0, x1 = int(max(0, min(a[0], b[0], c[0], d[0]))), int(min(Wd, max(a[0], b[0], c[0], d[0]) + 1))
        y0, y1 = int(max(0, min(a[1], b[1], c[1], d[1]))), int(min(Ht, max(a[1], b[1], c[1], d[1]) + 1))
        if x1 <= x0 or y1 <= y0: continue
        gx, gy = np.meshgrid(np.arange(x0, x1) + .5, np.arange(y0, y1) + .5)
        rel = np.stack([gx - a[0], gy - a[1]], -1) @ Minv.T
        s, t = rel[..., 0], rel[..., 1]
        inside = (s >= 0) & (s < 1) & (t >= 0) & (t < 1)
        depth = za + s * (zb - za) + t * (zc - za)
        tu = np.clip((ru + np.floor(s * rw)).astype(int), 0, tw - 1); tv = np.clip((rv + np.floor(t * rh)).astype(int), 0, th - 1)
        col = tex[tv, tu]
        ok = inside & (col[..., 3] > 0) & (depth > zbuf[y0:y1, x0:x1])
        region = img[y0:y1, x0:x1]
        region[ok, :3] = col[ok, :3] * bright
        region[ok, 3] = 255
        zbuf[y0:y1, x0:x1][ok] = depth[ok]
    return Image.fromarray(img.clip(0, 255).astype(np.uint8))

MOBS = [  # (label, subtitle, model, texture, scale, extra) ; extra = (texture, scale) of a young one shown alongside
    ("Elk", "Roosevelt bull, with calf", "elk", "elk_bull", 1.0, ("elk", 0.5)),
    ("Moose", "bull, with calf", "moose", "moose_bull", 1.0, ("moose", 0.5)),
    ("Grizzly Bear", "with cub", "grizzly_bear", "grizzly_bear", 1.0, ("grizzly_bear", 0.5)),
    ("Deer", "10-point trophy buck", "deer:4", "deer", 1.06, None),
    ("Pronghorn", "buck, with doe", "pronghorn", "pronghorn_buck", 1.0, ("pronghorn", 1.0)),
    ("Black Bear", "with cub", "black_bear", "black", 1.0, ("black", 0.5)),
    ("Cinnamon Bear", "rare colour phase", "black_bear", "cinnamon", 1.0, None),
    ("Boar", "", "boar", "boar", 1.0, None),
    ("Coyote", "", "coyote", "coyote", 1.0, None),
    ("Bobcat", "", "bobcat", "bobcat", 1.0, None),
    ("Wild Turkey", "tom", "turkey", "turkey", 1.0, None),
    ("Duck", "mallard drake", "duck", "duck", 1.0, None),
    ("Beaver", "", "beaver", "beaver", 1.0, None),
    ("Raccoon", "", "raccoon", "raccoon", 1.0, None),
    ("Opossum", "", "opossum", "opossum", 1.0, None),
    ("Striped Skunk", "", "skunk", "skunk", 1.0, None),
    ("Mink", "", "mink", "mink", 1.0, None),
    ("Fox Squirrel", "", "squirrel", "fox_squirrel", 1.15, None),
    ("Eastern Gray Squirrel", "", "squirrel", "gray_squirrel", 0.85, None),
    ("Eastern Chipmunk", "", "chipmunk", "chipmunk", 1.0, None),
    ("Northern Cardinal", "Kentucky's state bird", "songbird", "cardinal", 0.8, None),
    ("American Goldfinch", "", "songbird", "goldfinch", 0.8, None),
    ("American Robin", "", "songbird", "robin", 0.8, None),
    ("Blue Jay", "", "songbird", "blue_jay", 0.8, None),
    ("Eastern Bluebird", "", "songbird", "eastern_bluebird", 0.8, None),
    ("Mourning Dove", "", "songbird", "mourning_dove", 0.8, None),
    ("Bluegill", "", "bluegill", "bluegill", 1.0, None),
    ("Crappie", "", "crappie", "crappie", 1.0, None),
    ("Largemouth Bass", "", "largemouth_bass", "largemouth_bass", 1.0, None),
    ("Channel Catfish", "", "channel_catfish", "channel_catfish", 1.0, None),
]

def font(size, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + \
             ["/usr/share/fonts/truetype/dejavu/DejaVuSans%s.ttf" % ("-Bold" if bold else "")]:
        if os.path.exists(f): return ImageFont.truetype(f, size)
    return ImageFont.load_default()

def sheet(out):
    BG = (243, 239, 230); INK = (46, 42, 36); SUB = (128, 118, 104)
    cols, label_h, gap = 5, 90, 30
    renders = []
    for label, sub, model, tex, sc, extra in MOBS:
        imgs = [render(model, tex, sc)] + ([render(model, extra[0], extra[1])] if extra else [])
        renders.append((label, sub, imgs))
    cell_w = max(sum(im.width for im in r[2]) + 20 * (len(r[2]) - 1) for r in renders) + 30
    rows = [renders[i:i + cols] for i in range(0, len(renders), cols)]
    row_h = [max(max(im.height for im in r[2]) for r in row) + label_h + gap for row in rows]
    W, H = cols * cell_w + 80, 170 + sum(row_h) + 30
    canvas = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(canvas)
    d.text((40, 36), "RyanCraft field guide: %d animals" % len(MOBS), font=font(44, True), fill=INK)
    d.text((42, 92), "All drawn at the same scale: one model pixel = %d screen pixels." % PX, font=font(20), fill=SUB)
    y = 160
    for row, rh in zip(rows, row_h):
        base_y = y + rh - label_h
        for i, (label, sub, imgs) in enumerate(row):
            cx = 40 + i * cell_w
            total_w = sum(im.width for im in imgs) + 20 * (len(imgs) - 1)
            x = cx + (cell_w - total_w) // 2
            for im in imgs:
                sh = Image.new("L", (int(im.width * 0.9) + 40, 40), 0)
                ImageDraw.Draw(sh).ellipse((20, 8, sh.width - 20, 32), fill=70)
                sh = sh.filter(ImageFilter.GaussianBlur(7))
                canvas.paste((205, 198, 184), (x + im.width // 2 - sh.width // 2, base_y - 22), sh)
                canvas.paste(im, (x, base_y - im.height), im)
                x += im.width + 20
            d.text((cx + cell_w // 2, base_y + 26), label, font=font(24, True), fill=INK, anchor="mm")
            if sub: d.text((cx + cell_w // 2, base_y + 54), sub, font=font(17), fill=SUB, anchor="mm")
        y += rh
    canvas.save(out)
    print("wrote", out, canvas.size)

if __name__ == "__main__":
    sheet(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", "docs", "field_guide.png"))
