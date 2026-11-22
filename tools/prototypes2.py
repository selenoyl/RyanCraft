"""
Second batch of mob prototypes (models + textures only), rendered to docs/prototypes_2.png.
  python3 tools/prototypes2.py
"""
import copy, math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from critters import box, part, quad, emit_model, emit_textures, ROOT
from big_game import cervid, mirror
from prototypes import pack, raptor
import render_iso as R
from PIL import Image, ImageDraw, ImageFilter

P = []
def add(label, sub, parts, style, scale=1.0): P.append((label, sub, parts, style, scale))

# ---- Zebra: a horse variant with bold stripes and an upright two-tone mane
zebra = cervid((10, 10, 22), (3, 12), -8, neck=((-2, -2, -4), (4, 12, 6)), head=((-2.5, -5, -11), (5, 5, 7)), snout=((-2, -3, -14), (4, 4, 3)),
               ears=((-3, -8, -5), (1, 3, 1)), tail=((-1, 0, 0), (2, 10, 2), 0.35),
               extra_head=[box("mane", (-0.5, -7, -1), (1, 13, 3), "mane2")])
STRIPES = ["#f2f0ea", "#161616"] * 8
add("Zebra", "a horse variant", zebra, {
    "body": {"c": "#f2f0ea", "noise": 4, "rings": {"c": "#161616", "n": 7}},
    "mane": {"c": "#f2f0ea", "noise": 3, "side_rows": STRIPES}, "head": {"c": "#f2f0ea", "noise": 3, "side_rows": STRIPES, "side_eyes": "#0a0a0a", "eye_row": 1},
    "snout": {"c": "#2a2a2a", "noise": 3}, "ear": {"c": "#f2f0ea", "back": "#161616"},
    "leg": {"c": "#f2f0ea", "noise": 3, "side_rows": STRIPES, "bottom": "#161616"}, "tail": {"c": "#f2f0ea", "tip": {"c": "#161616", "frac": 0.4}},
    "mane2": {"c": "#161616", "noise": 3, "side_rows": STRIPES}})

# ---- Bison: huge hump, shaggy front, short curved horns
bison = quad((14, 14, 22), (4, 8), (9, 9, 7), snout=(6, 5, 3), ears=(2, 2), tail=(2, 6, 2, 0.4), head_up=-4,
             extra_head=mirror([((-6.5, -5.5, -4), (2, 1, 1)), ((-7.5, -7.5, -4), (1, 2, 1))], "horn", "horn") + [box("beard", (-2.5, 4.5, -6), (5, 4, 3), "shag")])
bison[0]["boxes"] += [box("hump", (-6, -11, -11), (12, 4, 10), "shag"), box("cape", (-7.5, -7, -11.5), (15, 13, 9), "shag")]
add("American Bison", "plains", bison, {
    "body": {"c": "#5a3c24", "noise": 8}, "shag": {"c": "#3a2616", "noise": 12, "speckle": {"c": "#2a1a0e", "p": 0.3}},
    "head": {"c": "#2e1e12", "noise": 8, "side_eyes": "#0a0806", "eye_row": 4}, "snout": {"c": "#1e140c", "noise": 4},
    "ear": {"c": "#2e1e12"}, "leg": {"c": "#2a1a10", "noise": 6}, "tail": {"c": "#3a2616", "tip": {"c": "#1a100a", "frac": 0.3}},
    "horn": {"c": "#2a2622", "noise": 3}})

# ---- Cougar
add("Cougar", "mountains and big woods", quad((6, 7, 16), (2, 7), (6, 5, 5), snout=(4, 2, 2), ears=(2, 2), tail=(2, 2, 12, -0.3)), {
    "body": {"c": "#b88a5a", "noise": 6, "belly": "#efe4d2"}, "head": {"c": "#b88a5a", "noise": 5, "eyes": "#0a0806", "eye_row": 2, "eye_inset": 1},
    "snout": {"c": "#efe4d2", "nose": "#8a5a4a"}, "ear": {"c": "#b88a5a", "back": "#2a2018"}, "leg": {"c": "#b08254", "noise": 5},
    "tail": {"c": "#b08254", "noise": 5, "tip": {"c": "#2a2018", "frac": 0.18}}})

# ---- River otter, groundhog, porcupine
add("River Otter", "creeks and rivers", quad((4, 4, 12), (2, 2), (4, 3, 4), snout=(3, 2, 1), ears=(1, 1), tail=(3, 2, 9, -0.05)), {
    "body": {"c": "#5a3a24", "noise": 6, "belly": "#8a6a4e"}, "head": {"c": "#5a3a24", "noise": 5, "eyes": "#0a0806", "eye_row": 0, "bottom": "#c8b498"},
    "snout": {"c": "#c8b498", "nose": "#1a1410"}, "ear": {"c": "#4a2e1c"}, "leg": {"c": "#4a2e1c"}, "tail": {"c": "#5a3a24", "noise": 6}})
add("Groundhog", "fields and woodland edges", quad((7, 6, 10), (2, 3), (5, 4, 4), snout=(3, 2, 1), ears=(1, 1), tail=(3, 2, 4, 0.4), head_up=1), {
    "body": {"c": "#7a5a3a", "noise": 8, "grizzle": {"c": ["#a88a62", "#5a3e26"], "p": [0.25, 0.15]}, "belly": "#9a7a54"},
    "head": {"c": "#6a4a30", "noise": 6, "eyes": "#0a0806", "eye_row": 1, "eye_inset": 1}, "snout": {"c": "#c8b49a", "nose": "#1a1410"},
    "ear": {"c": "#5a3e26"}, "leg": {"c": "#3a2616"}, "tail": {"c": "#4a3220", "noise": 6}})
porc = quad((7, 6, 10), (2, 3), (4, 4, 4), snout=(2, 2, 2), tail=(4, 3, 4, 0.3))
for i, (x, z) in enumerate([(-2, -3), (1, -2), (-1, 0), (2, 1), (-2, 2), (0, 3), (1, -4), (-3, 0)]):
    porc[0]["boxes"].append(box(f"quill{i}", (x, -6, z), (1, 3, 1), "quill"))
add("Porcupine", "", porc, {
    "body": {"c": "#3a2e26", "noise": 10, "speckle": {"c": "#d8d0c0", "p": 0.25}}, "head": {"c": "#2a221c", "noise": 5, "eyes": "#0a0806", "eye_row": 1, "eye_inset": 0},
    "snout": {"c": "#2a221c", "nose": "#0a0806"}, "leg": {"c": "#1e1814"}, "tail": {"c": "#3a2e26", "speckle": {"c": "#d8d0c0", "p": 0.3}},
    "quill": {"c": "#e8e0d0", "tip": {"c": "#2a221c", "frac": 0.3}}})

# ---- Box turtle, rattlesnake, bullfrog
turtle = [part("body", (0, 21, 0), [box("shell", (-4, -2, -5), (8, 3, 10), "shell"), box("dome", (-3, -4, -4), (6, 2, 8), "shell"),
                                    box("plastron", (-3.5, 1, -4.5), (7, 1, 9), "belly")], "body"),
          part("head", (0, 21, -5), [box("head", (-1.5, -1.5, -3), (3, 3, 3), "head")], "head")]
for nm, sx, sz in [("leg_rf", -1, -1), ("leg_lf", 1, -1), ("leg_rb", -1, 1), ("leg_lb", 1, 1)]:
    turtle.append(part(nm, (sx * 3.5, 22, sz * 3.5), [box("leg", (-1, 0, -1), (2, 2, 2), "skin", mirror=sx > 0)], nm))
add("Eastern Box Turtle", "", turtle, {
    "shell": {"c": "#3a2a1a", "noise": 8, "speckle": {"c": "#e0a030", "p": 0.3}}, "belly": {"c": "#5a4a2a"},
    "head": {"c": "#5a3a22", "noise": 6, "speckle": {"c": "#e08a30", "p": 0.3}, "side_eyes": "#c83a20", "eye_row": 0}, "skin": {"c": "#5a3a22", "speckle": {"c": "#e08a30", "p": 0.25}}}, 1.6)
snake = [box(f"seg{i}", (3 * math.sin(i * 0.9) - 1.5, -1.5, -11 + i * 2.6), (3, 3, 3.2), "scales") for i in range(9)]
snake += [box("head", (3 * math.sin(-0.9) - 2, -1.5, -14), (4, 2.5, 4), "head"),
          box("tail1", (3 * math.sin(9 * 0.9) - 1, -1, 12.4), (2, 2, 3), "scales"), box("rattle", (3 * math.sin(9 * 0.9) - 1, -1, 15.4), (2, 2, 2), "rattle")]
add("Timber Rattlesnake", "warns you with its rattle", [part("body", (0, 22.5, 0), snake, "body")], {
    "scales": {"c": "#a8946c", "noise": 8, "rings": {"c": "#2a221a", "n": 2}, "belly": "#d8ccaa"},
    "head": {"c": "#9a8660", "noise": 6, "side_eyes": "#c8a020", "eye_row": 0, "front": "#7a6a4a"}, "rattle": {"c": "#c8b48a", "rings": {"c": "#8a7a5a", "n": 2}}}, 1.3)
frog = [part("body", (0, 20, 0), [box("body", (-4, -2.5, -4), (8, 5, 9), "skin"), box("head", (-4, -3, -8), (8, 3, 4), "head"),
                                  box("eye_r", (-3.5, -4.5, -7), (2, 2, 2), "eye"), box("eye_l", (1.5, -4.5, -7), (2, 2, 2), "eye", mirror=True),
                                  box("thigh_r", (-5.5, 0, 0), (2, 3, 5), "skin"), box("thigh_l", (3.5, 0, 0), (2, 3, 5), "skin", mirror=True),
                                  box("foot_r", (-6, 3, -2), (3, 1, 6), "skin"), box("foot_l", (3, 3, -2), (3, 1, 6), "skin", mirror=True),
                                  box("arm_r", (-3.5, 2, -6), (1, 2, 1), "skin"), box("arm_l", (2.5, 2, -6), (1, 2, 1), "skin", mirror=True)], "body")]
add("Bullfrog", "ponds; deep 'jug-o-rum' call", frog, {
    "skin": {"c": "#5a7a34", "noise": 8, "speckle": {"c": "#3a5220", "p": 0.25}, "bottom": "#e8e0a8"},
    "head": {"c": "#6a8a3a", "noise": 6, "bottom": "#f0e8a0", "front": "#5a7a30"},
    "eye": {"c": "#c8a830", "front": "#0a0806", "top": "#5a7a30"}}, 1.4)

# ---- Birds
owl = [part("body", (0, 17, 0), [box("body", (-3, -4, -3), (6, 8, 6), "body")], "body"),
       part("head", (0, 12.5, -0.5), [box("head", (-3, -5, -3), (6, 5, 5), "face"), box("beak", (-0.5, -2.5, -3.5), (1, 1.5, 0.5), "beak"),
                                      box("tuft_r", (-3, -7, -1), (1, 2, 1), "body"), box("tuft_l", (2, -7, -1), (1, 2, 1), "body", mirror=True)], "head"),
       part("wing_r", (-3, 13.5, 0), [box("wing", (-1, 0, -2.5), (1, 7, 5), "wing")], "wing_r"),
       part("wing_l", (3, 13.5, 0), [box("wing", (0, 0, -2.5), (1, 7, 5), "wing", mirror=True)], "wing_l"),
       part("leg_r", (-1.5, 21, -1), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "talon")], "leg_r"),
       part("leg_l", (1.5, 21, -1), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "talon", mirror=True)], "leg_l")]
add("Great Horned Owl", "hoots at night", owl, {
    "body": {"c": "#7a5a3a", "noise": 8, "speckle": {"c": "#3a2a1a", "p": 0.3}, "front": "#c8b08a"},
    "face": {"c": "#7a5a3a", "noise": 6, "speckle": {"c": "#3a2a1a", "p": 0.2},
             "front_map": ["cCCCCc", "CyKKyC", "CCCCCC", "cCwwCc", "cccccc"],
             "front_colors": {"c": "#5a4028", "C": "#b8946a", "y": "#e8b820", "K": "#0a0806", "w": "#f0ead8"}},
    "beak": {"c": "#2a2420"}, "wing": {"c": "#6a4a2e", "noise": 8, "speckle": {"c": "#2e2014", "p": 0.3}}, "talon": {"c": "#c8b08a"}}, 1.2)
heron = [part("body", (0, 10, 2), [box("body", (-2, -2.5, -4), (4, 5, 8), "body"), box("tail", (-1.5, -1, 4), (3, 2, 3), "wing")], "body", rot=(-0.3, 0, 0)),
         part("head", (0, 7, -1), [box("neck_lo", (-1, -1, -1), (2, 4, 2), "neck"), box("neck_hi", (-1, -7, -3), (2, 6, 2), "neck"),
                                   box("head", (-1, -9, -5), (2, 2, 4), "headc"), box("beak", (-0.5, -8.5, -10), (1, 1, 5), "beak"),
                                   box("plume", (-0.5, -9, -2), (1, 1, 3), "plume")], "head"),
         part("wing_r", (-2, 8, 1), [box("wing", (-1, 0, -3), (1, 4, 9), "wing")], "wing_r"),
         part("wing_l", (2, 8, 1), [box("wing", (0, 0, -3), (1, 4, 9), "wing", mirror=True)], "wing_l"),
         part("leg_r", (-1, 12, 2), [box("leg", (-0.5, 0, -0.5), (1, 12, 1), "leg")], "leg_r"),
         part("leg_l", (1, 12, 2), [box("leg", (-0.5, 0, -0.5), (1, 12, 1), "leg", mirror=True)], "leg_l")]
add("Great Blue Heron", "wades the shallows", heron, {
    "body": {"c": "#7a8a9a", "noise": 6}, "wing": {"c": "#6a7a8c", "noise": 6, "tip": {"c": "#3a4452", "frac": 0.2}},
    "neck": {"c": "#9aa4ae", "noise": 4, "front": "#e8e8e2"}, "headc": {"c": "#e8e8e2", "noise": 3, "top": "#1a1a1e", "side_eyes": "#e8c020", "eye_row": 0},
    "beak": {"c": "#d8a830"}, "plume": {"c": "#1a1a1e"}, "leg": {"c": "#6a5a3a"}}, 1.0)
goose = [part("body", (0, 18, 1), [box("body", (-3, -3, -5), (6, 6, 10), "body"), box("tail", (-2, -2, 5), (4, 2, 2), "rump")], "body"),
         part("head", (0, 15, -3), [box("neck", (-1, -8, -1.5), (2, 8, 2), "neck"), box("head", (-1.5, -10, -3.5), (3, 3, 4), "headc"),
                                    box("beak", (-0.5, -9, -5.5), (1, 1, 2), "beak")], "head"),
         part("wing_r", (-3, 15.5, -2), [box("wing", (-1, 0, -1), (1, 5, 9), "wing")], "wing_r"),
         part("wing_l", (3, 15.5, -2), [box("wing", (0, 0, -1), (1, 5, 9), "wing", mirror=True)], "wing_l"),
         part("leg_r", (-1.5, 21, 1), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "leg")], "leg_r"),
         part("leg_l", (1.5, 21, 1), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "leg", mirror=True)], "leg_l")]
add("Canada Goose", "honking flocks", goose, {
    "body": {"c": "#7a6a58", "noise": 6, "belly": "#d8d0c4", "speckle": {"c": "#5a4c3e", "p": 0.2}}, "rump": {"c": "#f2f0ea", "top": "#1a1816"},
    "neck": {"c": "#141210", "noise": 3}, "headc": {"c": "#141210", "noise": 3, "sides": "#141210", "side_rows": ["#141210", "#f2f0ea", "#f2f0ea"],
                                                     "side_eyes": "#0a0806", "eye_row": 0}, "beak": {"c": "#141210"},
    "wing": {"c": "#6a5a48", "noise": 6, "speckle": {"c": "#4a3e30", "p": 0.25}}, "leg": {"c": "#141210"}}, 1.0)
hum = [part("body", (0, 20, 0), [box("body", (-1, -1, -2), (2, 2, 4), "body"), box("tail", (-1, -0.5, 2), (2, 0.5, 2), "body")], "body", rot=(-0.4, 0, 0)),
       part("head", (0, 18.5, -1.5), [box("head", (-1, -2, -2), (2, 2, 2), "headc"), box("beak", (-0.25, -1.25, -5), (0.5, 0.5, 3), "beak")], "head"),
       part("wing_r", (-1, 18.5, -1), [box("wing", (-5, 0, -1), (5, 0.5, 2), "wingblur")], "wing_r", rot=(0, 0, -0.4)),
       part("wing_l", (1, 18.5, -1), [box("wing", (0, 0, -1), (5, 0.5, 2), "wingblur", mirror=True)], "wing_l", rot=(0, 0, 0.4))]
add("Ruby-throated Hummingbird", "visits flowers", hum, {
    "body": {"c": "#4a8a3a", "noise": 6, "belly": "#e8e8de"}, "headc": {"c": "#4a8a3a", "noise": 4, "bottom": "#c8202a", "front": "#c8202a",
                                                                        "side_eyes": "#0a0806", "eye_row": 0}, "beak": {"c": "#141210"},
    "wingblur": {"c": "#9aa49a", "noise": 10}}, 3.0)
peck = [part("body", (0, 15, 0), [box("body", (-2, -4, -2), (4, 8, 4), "body"), box("tail", (-1.5, 4, 0), (3, 6, 1), "body")], "body", rot=(0.15, 0, 0)),
        part("head", (0, 11, -0.5), [box("head", (-1.5, -3.5, -3), (3, 3.5, 3), "headc"), box("crest", (-1, -5, -2), (2, 2, 4), "crest"),
                                     box("beak", (-0.5, -2, -6), (1, 1, 3), "beak")], "head"),
        part("wing_r", (-2, 11.5, 0), [box("wing", (-1, 0, -2), (1, 7, 4), "body")], "wing_r"),
        part("wing_l", (2, 11.5, 0), [box("wing", (0, 0, -2), (1, 7, 4), "body", mirror=True)], "wing_l")]
add("Pileated Woodpecker", "drums on dead trees", peck, {
    "body": {"c": "#141214", "noise": 4}, "headc": {"c": "#141214", "noise": 3, "side_rows": ["#141214", "#f2f0ea", "#141214"], "side_eyes": "#f2f0ea", "eye_row": 1},
    "crest": {"c": "#d8202a", "noise": 5}, "beak": {"c": "#3a3632"}}, 1.4)
quail = [part("body", (0, 20, 0), [box("body", (-2.5, -2.5, -3), (5, 5, 6), "body"), box("tail", (-1.5, -1, 3), (3, 2, 2), "body")], "body"),
         part("head", (0, 17, -2.5), [box("head", (-1.5, -2.5, -2.5), (3, 3, 3), "headc"), box("beak", (-0.5, -1, -3.5), (1, 1, 1), "beak")], "head"),
         part("leg_r", (-1, 22.5, 0), [box("leg", (-0.5, 0, -0.5), (1, 1.5, 1), "leg")], "leg_r"),
         part("leg_l", (1, 22.5, 0), [box("leg", (-0.5, 0, -0.5), (1, 1.5, 1), "leg", mirror=True)], "leg_l")]
add("Northern Bobwhite", "whistles its name", quail, {
    "body": {"c": "#8a5a36", "noise": 8, "speckle": {"c": "#3a2618", "p": 0.25}, "belly": "#e8dcc4"},
    "headc": {"c": "#6a4428", "noise": 5, "side_rows": ["#6a4428", "#f2f0ea", "#3a2618"], "front": "#f2f0ea", "side_eyes": "#0a0806", "eye_row": 1},
    "beak": {"c": "#2a2420"}, "leg": {"c": "#c8a07a"}}, 2.0)

# ---- Jellyfish
jelly = [part("body", (0, 4, 0), [box("bell", (-4, -3, -4), (8, 4, 8), "bell"), box("cap", (-3, -4, -3), (6, 1, 6), "bell"),
                                  box("frill", (-3.5, 1, -3.5), (7, 1, 7), "frill")] +
              [box(f"tent{i}", (x, 2, z), (1, 8 + (i % 3) * 3, 1), "tentacle") for i, (x, z) in
               enumerate([(-3, -3), (2.5, -3), (-3, 2.5), (2.5, 2.5), (-0.5, -3.2), (-0.5, 2.8), (-3.2, -0.5), (2.8, -0.5)])], "body")]
add("Jellyfish", "drifts in warm seas", jelly, {
    "bell": {"c": "#e8a8c8", "noise": 6, "speckle": {"c": "#f4d4e4", "p": 0.3}}, "frill": {"c": "#d07aa8"}, "tentacle": {"c": "#f0c8dc", "noise": 4}}, 1.3)

if __name__ == "__main__":
    made, renders = [], []
    for i, (label, sub, parts, style, scale) in enumerate(P):
        parts = copy.deepcopy(parts); name = f"proto2_{i}"
        size = pack(parts); emit_model(name, parts, size); emit_textures(parts, size, {name: style})
        made += [f"{ROOT}/critter_models/{name}.json", f"{ROOT}/textures/entity/{name}.png"]
        R.YAW = math.radians(-35)
        renders.append((label, sub, R.render(name, name, scale)))
    for f in made: os.remove(f)
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    cols = 4; cw = max(im.width for *_, im in renders) + 40
    rows = [renders[i:i + cols] for i in range(0, len(renders), cols)]
    rh = [max(im.height for *_, im in r) + 90 for r in rows]
    c = Image.new("RGB", (cols * cw + 60, 150 + sum(rh)), BG); d = ImageDraw.Draw(c)
    d.text((30, 28), "Mob prototypes, batch 2", font=R.font(42, True), fill=INK)
    d.text((32, 84), "Models and textures only, not in the game yet. Small animals are drawn larger so you can see them.", font=R.font(19), fill=SUB)
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
    out = os.path.join(os.path.dirname(__file__), "..", "docs", "prototypes_2.png"); c.save(out); print(out, c.size)
