"""
Butterflies, built like vanilla's tropical fish: a handful of wing PATTERNS, each painted in a species' COLOURS.
Twenty eastern US species as presets (the same patterns can mix with other colours for endless variety).
  python3 tools/butterflies.py  ->  docs/butterflies.png   (prototype; nothing is added to the game yet)
Wing grid: x = 0 at the body to 7 at the wingtip, z = 0 at the leading edge to 6 at the trailing edge.
"""
import copy, math, os, random, sys
sys.path.insert(0, os.path.dirname(__file__))
from critters import box, part, pack, emit_model, emit_textures, ROOT, hexc
import render_iso as R
from PIL import Image, ImageDraw, ImageFilter

W, D = 8, 7
# Wing outline: forewing tip out front, rounded hindwing behind ('#' = wing)
SHAPE = ["#######.",
         "########",
         "#######.",
         "######..",
         "######..",
         "#####...",
         "###....."]

def inside(x, z): return SHAPE[z][x] == "#"
def edge(x, z):
    return inside(x, z) and any(not (0 <= x + dx < W and 0 <= z + dz < D) or not inside(x + dx, z + dz)
                                for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)) if not (x + dx < 0))

# ---- patterns: (x, z, rng) -> colour key ('a' base, 'b' second, 'c' accent, 'k' black, 'w' white, 'd' dark base)
def veined(x, z, r):                      # monarch: orange panes between black veins, black border with white dots
    if edge(x, z): return "w" if (x + z) % 2 == 0 and x > 1 else "k"
    if x in (2, 5) or z in (3,) or (x == 1 and z < 2): return "k"
    return "a"
def veined_band(x, z, r):                 # viceroy: monarch look plus a black line across the hindwing
    if z == 5 and 0 < x < 5: return "k"
    return veined(x, z, r)
def striped(x, z, r):                     # tiger swallowtail: yellow with black stripes, blue near the tail
    if edge(x, z): return "k"
    if x in (1, 4) and z < 5: return "k"
    if z >= 5 and x >= 1: return "b"
    return "a"
def spot_rows(x, z, r):                   # black swallowtail: black with rows of yellow spots, blue and orange on the hindwing
    if edge(x, z): return "a" if (x + z) % 2 and x > 0 else "k"
    if (x == SHAPE[z].rfind("#") - 1) and z % 2 == 0: return "a"
    if z >= 4 and x in (2, 3): return "b"
    if z == 6 and x == 1: return "c"
    return "k"
def sheen(x, z, r):                       # pipevine / spicebush / red-spotted purple: dark with a coloured hindwing sheen
    if edge(x, z): return "c" if z >= 3 and (x + z) % 2 == 0 else "k"
    if z >= 3 and x <= 4: return "b"
    return "k"
def zebra(x, z, r):                       # zebra swallowtail / zebra longwing: many narrow bands
    if edge(x, z): return "k"
    return "k" if x % 2 == 1 else "a"
def banded(x, z, r):                      # red admiral: black with a bright band, white spots at the tip
    if x >= 5 and z <= 1: return "w" if (x + z) % 2 == 0 else "k"
    if x + z in (5, 6) and z <= 4: return "a"
    if z >= 5 and x <= 4 and edge(x, z): return "a"
    return "k"
def checker(x, z, r):                     # fritillaries / painted lady: orange chequered with black
    if edge(x, z): return "k" if (x + z) % 2 else "d"
    if x >= 5 and z <= 1: return "w"
    return "k" if (x * 3 + z * 5) % 7 == 0 else "a"
def edged(x, z, r):                       # mourning cloak: dark wings with a pale border and blue spots inside it
    if edge(x, z): return "b"
    near = any(0 <= x + dx < W and 0 <= z + dz < D and not inside(x + dx, z + dz) for dx in (-2, 2) for dz in (-1, 0, 1) if x + dx >= 0) or z == 0
    if near and (x + z) % 2 == 0 and x > 0: return "c"
    return "a"
def plain(x, z, r):                       # whites, sulphurs, azures: one colour, a dark tip spot
    if x >= 6 and z <= 1: return "k"
    if x == 4 and z == 2: return "c"
    return "a"
def eyespots(x, z, r):                    # buckeye / luna: eyespots on fore and hind wing
    for (ex, ez) in ((4, 1), (3, 4)):
        d = abs(x - ex) + abs(z - ez)
        if d == 0: return "b"
        if d == 1: return "c"
    if edge(x, z): return "d"
    return "a"

S = [  # (name, pattern, colours, tails)
    ("Monarch", veined, {"a": "#e8781e"}, False),
    ("Viceroy", veined_band, {"a": "#d8661a"}, False),
    ("Eastern Tiger Swallowtail", striped, {"a": "#f2d23a", "b": "#4a7ad8"}, True),
    ("Black Swallowtail", spot_rows, {"a": "#f2d23a", "b": "#4a6ad8", "c": "#e8781e"}, True),
    ("Pipevine Swallowtail", sheen, {"b": "#3a7ad8", "c": "#f2f2ea"}, True),
    ("Spicebush Swallowtail", sheen, {"b": "#5aa8a0", "c": "#f2e28a"}, True),
    ("Zebra Swallowtail", zebra, {"a": "#eef2ea"}, True),
    ("Red Admiral", banded, {"a": "#e0502a"}, False),
    ("Painted Lady", checker, {"a": "#e8904a", "d": "#a85a2a"}, False),
    ("Great Spangled Fritillary", checker, {"a": "#d8823a", "d": "#8a4a1e"}, False),
    ("Gulf Fritillary", checker, {"a": "#f0682a", "d": "#c04a1a"}, False),
    ("Mourning Cloak", edged, {"a": "#5a2a30", "b": "#f2e08a", "c": "#4a6ad8"}, False),
    ("Question Mark", edged, {"a": "#c8682a", "b": "#7a5a8a", "c": "#3a2a24"}, False),
    ("Red-spotted Purple", sheen, {"b": "#3a5ac8", "c": "#e8602a"}, False),
    ("Cabbage White", plain, {"a": "#f2f2ea", "c": "#2a2a2a"}, False),
    ("Clouded Sulphur", plain, {"a": "#f2e05a", "c": "#e8902a"}, False),
    ("Spring Azure", plain, {"a": "#8ab4f0", "c": "#5a7ab8"}, False),
    ("Common Buckeye", eyespots, {"a": "#8a6040", "b": "#3a5ad8", "c": "#e8781e", "d": "#5a3e28"}, False),
    ("Luna Moth", eyespots, {"a": "#b8e8a0", "b": "#3a2a24", "c": "#e8d07a", "d": "#9ac888"}, True),
    ("Zebra Longwing", zebra, {"a": "#f2e05a"}, False),
]
BASE = {"k": "#141210", "w": "#f4f2ea", "a": "#888888", "b": "#888888", "c": "#888888", "d": "#5a5a5a"}

def model(tails):
    wing_r = [box("wing", (-W, 0, -3), (W, 0.5, D), "wing")]
    wing_l = [box("wing", (0, 0, -3), (W, 0.5, D), "wing", mirror=True)]
    if tails:
        wing_r.append(box("tail", (-3, 0, 4), (1, 0.5, 3), "tail")); wing_l.append(box("tail", (2, 0, 4), (1, 0.5, 3), "tail", mirror=True))
    return [part("body", (0, 20, 0), [box("body", (-0.5, -0.5, -3), (1, 1, 6), "body"),
                                      box("antenna_r", (-1.5, -3, -4.5), (0.5, 3, 0.5), "body"), box("antenna_l", (1, -3, -4.5), (0.5, 3, 0.5), "body")], "body"),
            part("wing_r", (-0.5, 19.5, 0), wing_r, "wing_r", rot=(0, 0, 0.35)),
            part("wing_l", (0.5, 19.5, 0), wing_l, "wing_l", rot=(0, 0, -0.35))]

def paint_wings(img, parts, pattern, colours, seed):
    """Overpaint each wing's top and bottom faces with the pattern; everything outside the outline is see-through."""
    r = random.Random(seed); pal = dict(BASE, **colours); px = img.load()
    for wb in [b for p in parts if p["name"] in ("wing_r", "wing_l") for b in p["boxes"] if b["name"] == "wing"]:
        paint_one(img, px, wb["uv"], pattern, pal, random.Random(seed))

def paint_one(img, px, uv, pattern, pal, r):
    """One wing's patch (the left wing's box is mirrored, so the same painting comes out as its mirror image)."""
    u, v = uv
    w_, d_ = W, D
    fw, fh = img.size
    # clear the whole wing patch (top, bottom and the thin sides)
    for x in range(u, u + 2 * (w_ + d_)):
        for y in range(v, v + d_ + 1):
            if x < fw and y < fh: px[x, y] = (0, 0, 0, 0)
    for z in range(D):
        for x in range(W):
            if not inside(x, z): continue
            c = hexc(pal[pattern(x, z, r)])
            c = tuple(max(0, min(255, k + r.randint(-6, 6))) for k in c) + (255,)
            px[u + d_ + (W - 1 - x), v + (D - 1 - z)] = c                     # top: x runs outer->body, z back->front
            under = tuple(int(k * 0.8) for k in c[:3]) + (255,)
            px[u + d_ + w_ + (W - 1 - x), v + z] = under                       # underside, a little duller

def build(i, name, pattern, colours, tails):
    parts = model(tails); n = f"bfly_{i}"
    size = pack(parts); emit_model(n, parts, size)
    style = {"body": {"c": "#1e1a16", "noise": 3}, "wing": {"c": "#888888"}, "tail": {"c": colours.get("a", "#141210") if pattern in (zebra, eyespots) else "#141210"}}
    emit_textures(parts, size, {n: style})
    tex = os.path.join(ROOT, "textures/entity", n + ".png")
    img = Image.open(tex).convert("RGBA"); paint_wings(img, parts, pattern, colours, 500 + i); img.save(tex)
    return n

def game_assets():
    """In-game assets: one model 'butterfly' and a texture per species (butterfly_0 .. butterfly_19)."""
    parts = model(True); size = pack(parts); emit_model("butterfly", parts, size)
    for i, (name, pat, cols, tails) in enumerate(S):
        n = f"butterfly_{i}"
        tail_c = (cols.get("a", "#141210") if pat in (zebra, eyespots) else "#141210") if tails else None
        emit_textures(parts, size, {n: {"body": {"c": "#1e1a16", "noise": 3}, "wing": {"c": "#888888"}, "tail": {"c": tail_c}}})
        tex = os.path.join(ROOT, "textures/entity", n + ".png")
        img = Image.open(tex).convert("RGBA"); paint_wings(img, parts, pat, cols, 500 + i); img.save(tex)
    return [name for name, *_ in S]

if __name__ == "__main__" and "--game" in sys.argv:
    print(game_assets()); sys.exit()

if __name__ == "__main__":
    R.PX = 14; R.ELEV = math.radians(55)
    made, renders = [], []
    for i, (name, pat, cols, tails) in enumerate(S):
        n = build(i, name, pat, cols, tails)
        made += [f"{ROOT}/critter_models/{n}.json", f"{ROOT}/textures/entity/{n}.png"]
        R.YAW = math.radians(-20)
        renders.append((name, R.render(n, n)))
    for f in made: os.remove(f)
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    cols = 5; cw = max(im.width for _, im in renders) + 30; ch = max(im.height for _, im in renders) + 60
    rows = (len(renders) + cols - 1) // cols
    c = Image.new("RGB", (cols * cw + 60, 150 + rows * ch), BG); d = ImageDraw.Draw(c)
    d.text((30, 28), "Butterflies", font=R.font(42, True), fill=INK)
    d.text((32, 84), "Ten wing patterns x species colours, like the tropical fish. Prototype, not in the game yet.", font=R.font(19), fill=SUB)
    for k, (name, im) in enumerate(renders):
        x = 30 + (k % cols) * cw; y = 150 + (k // cols) * ch
        c.paste(im, (x + (cw - im.width) // 2, y + ch - 50 - im.height), im)
        d.text((x + cw // 2, y + ch - 30), name, font=R.font(17, True), fill=INK, anchor="mm")
    out = os.path.join(os.path.dirname(__file__), "..", "docs", "butterflies.png"); c.save(out); print(out, c.size)
