"""
Prototype models for mobs not yet in the game, rendered to one preview sheet (nothing is added to the mod).
  python3 tools/prototypes.py  ->  docs/prototypes.png
"""
import copy, math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from critters import box, part, quad, fish, emit_model, emit_textures, ROOT
from big_game import cervid, mirror, CLEAR
import render_iso as R
from PIL import Image, ImageDraw, ImageFilter
from critters import footprint

def pack(parts):
    """critters.pack, with room for whale-sized textures."""
    boxes = [b for p in parts for b in p["boxes"]]
    order = sorted(boxes, key=lambda b: -footprint(b)[1])
    for size in [(32, 32), (64, 32), (64, 64), (128, 64), (128, 128), (256, 128), (256, 256), (512, 256)]:
        W, H = size; x = y = row = 0; ok = True
        for b in order:
            fw, fh, _ = footprint(b)
            if x + fw > W: x, y, row = 0, y + row, 0
            if fw > W or y + fh > H: ok = False; break
            b["uv"] = [x, y]; x += fw; row = max(row, fh)
        if ok: return size
    raise SystemExit("textures too big")

P = []   # (label, subtitle, parts, style, scale)

# ---------------- black-tailed deer: smaller, darker, big mule-deer ears, black-topped tail, forked antlers
BTD_ANTLER = [((-2, -8, -4), (1, 5, 1)), ((-3, -11, -4), (1, 4, 1)), ((-3, -11, -6), (1, 1, 2)), ((-3, -13, -6), (1, 2, 1)),
              ((-2, -9, -6), (1, 1, 2)), ((-2, -11, -7), (1, 2, 1))]
btd = cervid((7, 7, 14), (2, 10), -6, neck=((-1.5, -1, -3), (3, 8, 4)), head=((-2, -3, -8), (4, 4, 5)), snout=((-1.5, -2, -10), (3, 3, 2)),
             ears=((-4, -5, -4), (2, 3, 1)), tail=((-1, 0, 0), (2, 3, 1), 0.3), extra_head=mirror(BTD_ANTLER, "antler", "antler"))
P.append(("Black-tailed Deer", "Pacific coast buck", btd, {
    "body": {"c": "#7a6a58", "noise": 8, "belly": "#d8ccb4", "speckle": {"c": "#5e5040", "p": 0.15}},
    "mane": {"c": "#6e6050", "noise": 6, "front": "#e8e0d0"}, "head": {"c": "#7a6a58", "noise": 5, "side_eyes": "#0a0806", "eye_row": 1, "front": "#3a3028"},
    "snout": {"c": "#5a4c3e", "front": "#1a1410"}, "ear": {"c": "#6a5a4a", "back": "#2a2420"}, "leg": {"c": "#6a5a48", "noise": 5},
    "tail": {"c": "#141210", "bottom": "#e8e0d0"}, "antler": {"c": "#c8b088", "noise": 10}}, 1.0))

# ---------------- alligator: low and long, armoured back, eyes on top of the head
gator = quad((8, 4, 18), (3, 3), (7, 3, 6), snout=(5, 2, 8), tail=(5, 3, 12, -0.05), leg_inset=2)
for p in gator:
    if p["name"] == "body":
        for i in range(4): p["boxes"].append(box(f"scute{i}", (-2.5, -3, -7 + i * 4), (5, 1, 2), "scute"))
    if p["name"] == "tail":
        p["boxes"].append(box("tail_tip", (-1.5, -1, 12), (3, 2, 9), "tail"))
        p["boxes"].append(box("tail_ridge", (-0.5, -2.5, 0), (1, 1, 18), "scute"))
    if p["name"] == "head":
        p["boxes"].append(box("eye_bumps", (-3, -2.5, -4), (6, 1, 2), "scute"))
P.append(("American Alligator", "swamps", gator, {
    "body": {"c": "#3a4a2c", "noise": 10, "belly": "#c8c08a", "speckle": {"c": "#26301c", "p": 0.3}},
    "head": {"c": "#3e4e30", "noise": 8, "bottom": "#c8c08a", "side_eyes": "#d8b020", "eye_row": 0},
    "snout": {"c": "#3a4a2c", "noise": 8, "bottom": "#c8c08a", "front": "#2a3420"}, "leg": {"c": "#34442a", "noise": 8},
    "tail": {"c": "#36462a", "noise": 10, "bottom": "#b8b07a", "rings": {"c": "#26301c", "n": 5}}, "scute": {"c": "#26301c", "noise": 6}}, 1.0))

# ---------------- raptors (perched) and soaring birds (wings out)
def raptor(body, head, tail_len, wing, beak=(1, 2, 3), spread=False):
    bw, bh, bl = body; hw = head
    wr = (0, 0, 1.35) if spread else (0, 0, 0)
    return [
        part("body", (0, 17, 0), [box("body", (-bw / 2, -bh / 2, -bl / 2), body, "body")], "body", rot=(-0.35 if not spread else 0, 0, 0)),
        part("head", (0, 17 - bh / 2 - (0 if spread else 1), -bl / 2 + 1), [box("head", (-hw / 2, -hw + 1, -hw), (hw, hw, hw), "head"),
             box("beak", (-beak[0] / 2, -1, -hw - beak[2] + 1), beak, "beak"), box("hook", (-beak[0] / 2, 0, -hw - beak[2] + 1), (beak[0], 1, 1), "beak")], "head"),
        part("tail", (0, 17 + (1 if not spread else 0), bl / 2 - 1), [box("tail", (-bw / 2 + 0.5, -0.5, 0), (bw - 1, 1, tail_len), "tail")], "tail",
             rot=(0.45 if not spread else 0.05, 0, 0)),
        part("wing_r", (-bw / 2, 17 - bh / 2 + 0.5, -bl / 2 + 1), [box("wing", (-1, 0, -1), (1, wing[0], wing[1]), "wing")], "wing_r", rot=(0, 0, wr[2])),
        part("wing_l", (bw / 2, 17 - bh / 2 + 0.5, -bl / 2 + 1), [box("wing", (0, 0, -1), (1, wing[0], wing[1]), "wing", mirror=True)], "wing_l", rot=(0, 0, -wr[2])),
        part("leg_r", (-1, 17 + bh / 2 - 0.5, 0.5), [box("leg", (-0.5, 0, -0.5), (1, 24 - 17 - bh / 2 + 0.5, 1), "leg")], "leg_r"),
        part("leg_l", (1, 17 + bh / 2 - 0.5, 0.5), [box("leg", (-0.5, 0, -0.5), (1, 24 - 17 - bh / 2 + 0.5, 1), "leg", mirror=True)], "leg_l"),
    ]

EAGLE = {"body": {"c": "#3a2a1c", "noise": 8, "speckle": {"c": "#2a1e14", "p": 0.25}}, "head": {"c": "#f2f0ea", "noise": 4, "side_eyes": "#0a0806", "eye_row": 1},
         "beak": {"c": "#e8b828", "noise": 4}, "tail": {"c": "#f2f0ea", "noise": 4}, "wing": {"c": "#33251a", "noise": 8, "speckle": {"c": "#1e160e", "p": 0.25}},
         "leg": {"c": "#e8b828"}}
P.append(("Bald Eagle", "perched", raptor((5, 6, 9), 4, 6, (6, 10)), EAGLE, 1.0))
P.append(("Bald Eagle", "soaring", raptor((5, 5, 9), 4, 6, (14, 8), spread=True), EAGLE, 1.0))
P.append(("Golden Eagle", "perched", raptor((5, 6, 9), 4, 6, (6, 10)), dict(EAGLE,
         head={"c": "#5a3e22", "noise": 6, "side_eyes": "#0a0806", "eye_row": 1, "top": "#c8962e", "back": "#c8962e"},
         beak={"c": "#2a2420", "noise": 3}, tail={"c": "#3a2a1c", "noise": 6, "rings": {"c": "#5a4632", "n": 3}}), 1.0))
P.append(("Red-tailed Hawk", "", raptor((4, 5, 7), 3, 5, (5, 8)), {
    "body": {"c": "#6a4a2e", "noise": 8, "belly": "#efe4d0", "front": "#efe4d0", "speckle": {"c": "#4a3220", "p": 0.2}},
    "head": {"c": "#6a4a2e", "noise": 6, "side_eyes": "#2a1e14", "eye_row": 1, "bottom": "#efe4d0"}, "beak": {"c": "#3a3632"},
    "tail": {"c": "#b85a2a", "noise": 6, "tip": {"c": "#f0e8d8", "frac": 0.12}}, "wing": {"c": "#5a3e26", "noise": 8, "speckle": {"c": "#3a2818", "p": 0.25}},
    "leg": {"c": "#e8c040"}}, 1.0))
vulture = raptor((5, 5, 9), 3, 6, (14, 8), beak=(1, 1, 2), spread=True)
for p in vulture:
    if p["name"].startswith("wing"): p["rot"][0] = 0; p["rot"][2] *= 0.75     # flatter V: the soaring dihedral
P.append(("Turkey Vulture", "soaring", vulture, {
    "body": {"c": "#2a221e", "noise": 6}, "head": {"c": "#b8302a", "noise": 6, "side_eyes": "#2a1e14", "eye_row": 1},
    "beak": {"c": "#e8e0d0"}, "tail": {"c": "#2a221e", "noise": 6},
    "wing": {"c": "#2a221e", "noise": 6, "bottom": "#8a847c", "tip": {"c": "#9a948a", "frac": 0.3}}, "leg": {"c": "#d8b0a0"}}, 1.0))

# ---------------- ocean: whales have horizontal flukes
def whale(body, fluke, dorsal, flipper, y=None):
    bw, bh, bl = body
    y = y if y is not None else 24 - bh / 2 - 1
    ps = [part("body", (0, y, 0), [box("body", (-bw / 2, -bh / 2, -bl / 2), body, "body"),
                                    box("dorsal", (-0.5, -bh / 2 - dorsal[1], dorsal[2]), (1, dorsal[1], dorsal[0]), "fin"),
                                    box("flipper_r", (-bw / 2 - flipper[0], bh / 4, -bl / 4), (flipper[0], 1, flipper[1]), "fin"),
                                    box("flipper_l", (bw / 2, bh / 4, -bl / 4), (flipper[0], 1, flipper[1]), "fin", mirror=True)], "fish_body")]
    ps.append(part("tail", (0, y, bl / 2), [box("stock", (-bw / 4, -bh / 4, 0), (bw / 2, bh / 2, bl / 4), "body"),
                                           box("fluke", (-fluke[0] / 2, -0.5, bl / 4), (fluke[0], 1, fluke[1]), "fin")], "fish_tail"))
    return ps

P.append(("Orca", "hunts in pods", whale((10, 10, 28), (16, 6), (5, 8, -2), (5, 7)), {
    "body": {"c": "#141416", "noise": 4, "belly": "#f2f2f0", "side_eyes": "#f2f2f0", "eye_row": 1, "eye_ring": "#f2f2f0",
             "front": "#141416", "speckle": {"c": "#1c1c20", "p": 0.1}},
    "fin": {"c": "#141416", "noise": 3}}, 0.7))
P.append(("Blue Whale", "huge and slow; surfaces to spout", whale((14, 12, 64), (22, 8), (3, 2, 20), (5, 14)), {
    "body": {"c": "#5a7084", "noise": 10, "speckle": {"c": "#8aa0b2", "p": 0.25}, "bottom": "#7a8ea0", "side_eyes": "#141416", "eye_row": 7},
    "fin": {"c": "#4e6476", "noise": 8}}, 0.4))
shark = fish((6, 7, 22), (1, 12, 5))
for p in shark:
    if p["name"] == "body":
        p["boxes"] += [box("dorsal1", (-0.5, -5.5, -3), (1, 2, 7), "fin"), box("dorsal2", (-0.5, -7.5, -1), (1, 2, 5), "fin"),
                       box("dorsal3", (-0.5, -9.5, 1), (1, 2, 3), "fin"), box("dorsal4", (-0.5, -10.5, 3), (1, 1, 1), "fin"),
                       box("nose", (-2.5, -2.5, -15), (5, 3, 4), "snout"),
                       box("nose_tip", (-1.5, -2.5, -17), (3, 2, 2), "snout_tip"),
                       # the mouth is underslung, behind the snout tip: a wide dark gape lined with teeth, then the lower jaw
                       box("gape", (-2.5, 0.5, -14), (5, 1, 4), "mouth"),
                       box("jaw", (-2.5, 1.5, -14), (5, 1, 4), "jaw"),
                       box("pec_r", (-9, 2, -6), (6, 1, 5), "fin"), box("pec_l", (3, 2, -6), (6, 1, 5), "fin", mirror=True)]
P.append(("Shark", "", shark, {"body": {"c": "#6a7480", "noise": 6, "belly": "#eceeee"},
                                "snout": {"c": "#6a7480", "noise": 6, "bottom": "#eceeee", "side_eyes": "#0a0a0c", "eye_row": 0},
                                "snout_tip": {"c": "#6a7480", "noise": 6, "bottom": "#d8dcdc"},
                                "fin": {"c": "#5e6874", "noise": 5}, "mouth": {"c": "#2e1418", "noise": 2, "bars": {"c": "#f4f2ea", "n": 2},
                                          "front_map": ["wdwdw"], "front_colors": {"w": "#f4f2ea", "d": "#2e1418"}},
                                "jaw": {"c": "#e6e8e8", "noise": 4, "top": "#2e1418", "front_map": ["wcwcw"], "front_colors": {"w": "#f4f2ea", "c": "#d8dada"}}}, 0.8))
manta = [part("body", (0, 21, 0), [box("disc", (-6, -1, -6), (12, 2, 12), "body"),
                                    box("wing_r", (-14, -0.5, -4), (8, 1, 8), "wing"), box("wing_l", (6, -0.5, -4), (8, 1, 8), "wing", mirror=True),
                                    box("horn_r", (-4, -0.5, -9), (2, 1, 3), "body"), box("horn_l", (2, -0.5, -9), (2, 1, 3), "body", mirror=True)], "fish_body"),
         part("tail", (0, 21, 6), [box("tail", (-0.5, -0.5, 0), (1, 1, 14), "tail")], "fish_tail")]
P.append(("Manta Ray", "", manta, {"body": {"c": "#1e2026", "noise": 5, "bottom": "#eeeeec", "side_eyes": "#0a0a0c", "eye_row": 0,
                                            "speckle": {"c": "#2a2e36", "p": 0.15}},
                                   "wing": {"c": "#1e2026", "noise": 5, "bottom": "#eeeeec", "tip": {"c": "#30343c", "frac": 0.2}},
                                   "tail": {"c": "#1a1c20"}}, 0.8))
# ---------------- butterflies: wings in a V
def butterfly():
    return [part("body", (0, 20, 0), [box("body", (-0.5, -0.5, -2), (1, 1, 4), "body"),
                                      box("antenna_r", (-1, -2.5, -3), (0.5, 2, 0.5), "body"), box("antenna_l", (0.5, -2.5, -3), (0.5, 2, 0.5), "body")], "body"),
            part("wing_r", (-0.5, 19.5, 0), [box("wing", (-6, 0, -3), (6, 0.5, 6), "wing")], "wing_r", rot=(0, 0, 0.5)),
            part("wing_l", (0.5, 19.5, 0), [box("wing", (0, 0, -3), (6, 0.5, 6), "wing", mirror=True)], "wing_l", rot=(0, 0, -0.5))]
P.append(("Monarch", "butterfly", butterfly(), {"body": {"c": "#1a1614"},
         "wing": {"c": "#e8781e", "noise": 8, "speckle": {"c": "#141210", "p": 0.25}, "fringe": "#141210", "tip": {"c": "#141210", "frac": 0.2}}}, 2.0))
P.append(("Eastern Tiger Swallowtail", "butterfly", butterfly(), {"body": {"c": "#1a1614"},
         "wing": {"c": "#f2d23a", "noise": 6, "bars": {"c": "#141210", "n": 3}, "fringe": "#141210", "speckle": {"c": "#141210", "p": 0.08}}}, 2.0))

if __name__ == "__main__":
    made, renders = [], []
    for i, (label, sub, parts, style, scale) in enumerate(P):
        parts = copy.deepcopy(parts); name = f"proto_{i}"
        size = pack(parts); emit_model(name, parts, size); emit_textures(parts, size, {name: style})
        made += [f"{ROOT}/critter_models/{name}.json", f"{ROOT}/textures/entity/{name}.png"]
        R.YAW = math.radians(-35)
        renders.append((label, sub, R.render(name, name, scale)))
    for f in made: os.remove(f)
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    cols = 4
    cw = max(im.width for *_, im in renders) + 40
    rows = [renders[i:i + cols] for i in range(0, len(renders), cols)]
    rh = [max(im.height for *_, im in r) + 90 for r in rows]
    c = Image.new("RGB", (cols * cw + 60, 150 + sum(rh)), BG); d = ImageDraw.Draw(c)
    d.text((30, 28), "Mob prototypes", font=R.font(42, True), fill=INK)
    d.text((32, 84), "Models and textures only, not in the game yet. Ocean animals and butterflies are drawn at different scales to fit.", font=R.font(19), fill=SUB)
    y = 150
    for row, h in zip(rows, rh):
        base = y + h - 70
        for k, (label, sub, im) in enumerate(row):
            x = 30 + k * cw + (cw - im.width) // 2
            sh = Image.new("L", (int(im.width * .9) + 40, 40), 0); ImageDraw.Draw(sh).ellipse((20, 8, sh.width - 20, 32), fill=70)
            sh = sh.filter(ImageFilter.GaussianBlur(7)); c.paste((205, 198, 184), (x + im.width // 2 - sh.width // 2, base - 22), sh)
            c.paste(im, (x, base - im.height), im)
            d.text((30 + k * cw + cw // 2, base + 24), label, font=R.font(22, True), fill=INK, anchor="mm")
            if sub: d.text((30 + k * cw + cw // 2, base + 50), sub, font=R.font(16), fill=SUB, anchor="mm")
        y += h
    out = os.path.join(os.path.dirname(__file__), "..", "docs", "prototypes.png"); c.save(out); print(out, c.size)
