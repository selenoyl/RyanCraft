"""
Critter models + textures from one spec.
  python3 tools/critters.py
writes  src/main/resources/assets/ryancraft/critter_models/<model>.json  (boxes + UVs, read by CritterModel.java)
        src/main/resources/assets/ryancraft/textures/entity/<texture>.png
        src/main/resources/assets/ryancraft/textures/item/<item>.png   (fish, turkey, raccoon tail icons)
Coordinates are Minecraft model pixels: y points down, feet at y=24, the head faces -z.
"""
import json, math, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/ryancraft")
rnd = random.Random(1792)  # Kentucky statehood

def hexc(h):
    h = h.lstrip('#'); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

# ---------------------------------------------------------------- spec helpers
def box(name, frm, size, paint, mirror=False):
    return {"name": name, "from": list(frm), "size": list(size), "paint": paint, "mirror": mirror}

def part(name, pivot, boxes, role=None, parent=None, rot=(0, 0, 0)):
    return {"name": name, "pivot": list(pivot), "rot": list(rot), "boxes": boxes, "role": role, "parent": parent}

def quad(body, leg, head, snout=None, ears=None, tail=None, head_up=0, head_fwd=0, leg_inset=1, extra_head=()):
    """Generic four-legged critter. Sizes in pixels. tail=(w,h,len,xRot,paint_key)."""
    bw, bh, bl = body; lw, lh = leg; hw, hh, hd = head
    body_top = 24 - lh - bh
    parts = []
    parts.append(part("body", (0, body_top + bh / 2, 0), [box("body", (-bw / 2, -bh / 2, -bl / 2), body, "body")], "body"))
    hb = [box("head", (-hw / 2, -hh / 2, -hd), head, "head")]
    if snout:
        sw, sh, sd = snout
        hb.append(box("snout", (-sw / 2, hh / 2 - sh, -hd - sd), snout, "snout"))
    if ears:
        ew, eh = ears
        hb.append(box("ear_r", (-hw / 2, -hh / 2 - eh, -hd / 2 - 0.5), (ew, eh, 1), "ear"))
        hb.append(box("ear_l", (hw / 2 - ew, -hh / 2 - eh, -hd / 2 - 0.5), (ew, eh, 1), "ear", mirror=True))
    hb += list(extra_head)
    # The head's back face sits 1px inside the body, so turning the head never opens a gap.
    parts.append(part("head", (0, body_top + hh / 2 - head_up, -bl / 2 + 1), hb, "head"))
    for nm, sx, sz, role in [("leg_rf", -1, -1, "leg_rf"), ("leg_lf", 1, -1, "leg_lf"), ("leg_rb", -1, 1, "leg_rb"), ("leg_lb", 1, 1, "leg_lb")]:
        x = sx * (bw / 2 - lw / 2); z = sz * (bl / 2 - lw / 2 - leg_inset)
        parts.append(part(nm, (x, 24 - lh, z), [box("leg", (-lw / 2, 0, -lw / 2), (lw, lh, lw), "leg", mirror=sx > 0)], role))
    if tail:
        tw, th, tl, trot = tail[:4]
        curl = tail[4] if len(tail) > 4 else 0
        tboxes = [box("tail", (-tw / 2, -th / 2, 0), (tw, th, tl), "tail")]
        if curl:   # bushy tip that curls forward over the back
            tboxes.append(box("tail_tip", (-tw / 2, -th / 2 - curl, tl - 2), (tw, curl, 3), "tail"))
        parts.append(part("tail", (0, body_top + th / 2, bl / 2 - 1), tboxes, "tail", rot=(trot, 0, 0)))
    return parts

def bird():
    return [
        part("body", (0, 19, 0), [box("body", (-2, -2, -3), (4, 4, 6), "body")], "body", rot=(-0.25, 0, 0)),
        part("head", (0, 16.5, -2.5), [box("head", (-2, -3, -3), (4, 4, 4), "head"),
                                       box("beak", (-0.5, -1.5, -5), (1, 1, 2), "beak"),
                                       box("crest", (-0.5, -5, -1.5), (1, 2, 2), "crest")], "head"),
        part("tail", (0, 18.5, 2.5), [box("tail", (-1.5, -0.5, 0), (3, 1, 5), "tail")], "tail", rot=(0.35, 0, 0)),
        part("wing_r", (-2, 17.5, -1), [box("wing", (-1, 0, -2), (1, 3, 5), "wing")], "wing_r"),
        part("wing_l", (2, 17.5, -1), [box("wing", (0, 0, -2), (1, 3, 5), "wing", mirror=True)], "wing_l"),
        part("leg_r", (-1, 21, 0.5), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "leg")], "leg_r"),
        part("leg_l", (1, 21, 0.5), [box("leg", (-0.5, 0, -0.5), (1, 3, 1), "leg", mirror=True)], "leg_l"),
    ]

def fish(body, tail, dorsal=None, whiskers=False):
    bw, bh, bl = body; tw, th, tl = tail
    y = 24 - bh / 2 - 1
    parts = [part("body", (0, y, 0), [box("body", (-bw / 2, -bh / 2, -bl / 2), body, "body")] +
                  ([box("dorsal", (-0.5, -bh / 2 - dorsal[1], -dorsal[2] / 2), dorsal, "fin")] if dorsal else []) +
                  ([box("whisker_r", (-bw / 2 - 3, bh / 2 - 1.5, -bl / 2), (3, 1, 1), "whisker"),
                    box("whisker_l", (bw / 2, bh / 2 - 1.5, -bl / 2), (3, 1, 1), "whisker", mirror=True)] if whiskers else []),
                  "fish_body")]
    parts.append(part("tail", (0, y, bl / 2), [box("tail", (-tw / 2, -th / 2, 0), tail, "fin")], "fish_tail"))
    return parts

# ---------------------------------------------------------------- species
MODELS = {
    "black_bear": quad((12, 11, 20), (4, 9), (9, 8, 7), snout=(5, 4, 4), ears=(3, 3), tail=(3, 3, 2, 0.6), head_up=-2, head_fwd=2),
    "raccoon": quad((6, 5, 9), (2, 4), (5, 4, 4), snout=(3, 2, 2), ears=(2, 2), tail=(3, 3, 8, 0.15), head_fwd=1),
    "skunk": quad((6, 5, 9), (2, 3), (4, 4, 4), snout=(2, 2, 2), ears=(1, 1), tail=(5, 5, 8, 1.15), head_fwd=1),
    "mink": quad((4, 4, 10), (2, 2), (4, 3, 4), snout=(2, 2, 1), ears=(1, 1), tail=(2, 2, 6, -0.1), head_fwd=1),
    "opossum": quad((6, 5, 9), (2, 3), (4, 4, 4), snout=(2, 2, 3), ears=(2, 2), tail=(1, 1, 9, -0.25), head_fwd=1),
    "beaver": quad((8, 7, 11), (2, 3), (6, 5, 5), snout=(4, 3, 2), ears=(1, 1), tail=(6, 1, 8, -0.05), head_fwd=1,
                   extra_head=[box("teeth", (-1, 2.5, -7.1), (2, 2, 1), "teeth")]),
    "squirrel": quad((4, 4, 6), (2, 3), (4, 4, 4), ears=(1, 2), tail=(4, 3, 6, 0.75, 4), head_up=1, leg_inset=0),
    "songbird": bird(),
    "chipmunk": quad((4, 3, 5), (1, 2), (3, 3, 3), ears=(1, 1), tail=(1, 1, 4, 1.1), head_up=1, leg_inset=0),
    "coyote": quad((5, 6, 12), (2, 7), (5, 5, 5), snout=(3, 2, 3), ears=(2, 3), tail=(3, 3, 7, -0.55)),
    "bluegill": fish((2, 6, 7), (1, 5, 3), dorsal=(1, 2, 5)),
    "crappie": fish((2, 6, 8), (1, 5, 3), dorsal=(1, 2, 5)),
    "largemouth_bass": fish((3, 5, 11), (1, 5, 3), dorsal=(1, 2, 7)),
    "channel_catfish": fish((3, 3, 13), (1, 4, 4), dorsal=(1, 2, 3), whiskers=True),
}

# paint = per box-paint-key style dict (see painter below)
P = {}
P["black_bear"] = {
    "black": {"body": {"c": "#1d1b1b", "noise": 8}, "head": {"c": "#1d1b1b", "eyes": "#080504", "eye_white": "#ece8de", "eye_row": 3},
              "snout": {"c": "#b08a5e", "nose": "#111111"}, "ear": {"c": "#141212"}, "leg": {"c": "#161414"}, "tail": {"c": "#1d1b1b"}},
    "cinnamon": {"body": {"c": "#7b4a28", "noise": 10}, "head": {"c": "#7b4a28", "eyes": "#1a1210", "eye_white": "#ece8de", "eye_row": 3},
                 "snout": {"c": "#c79a68", "nose": "#111111"}, "ear": {"c": "#6a3d20"}, "leg": {"c": "#5e361c"}, "tail": {"c": "#7b4a28"}},
}
P["raccoon"] = {"raccoon": {
    "body": {"c": "#7f7b74", "noise": 14, "belly": "#9a958c"},
    "head": {"c": "#a8a49c", "mask": "#2a2725", "mask_rows": [1, 2], "cap": "#7f7b74",
             # pale brows over each eye, dark stripe down the nose, pupil with a white glint, charcoal mask, pale cheeks
             "front_map": ["gBsBg",
                           "hpMph",
                           "MMMMM",
                           "WgggW"],
             "front_colors": {"g": "#8a857c", "B": "#e6e2da", "s": "#3a3632", "h": "#f6f6f2", "p": "#050505",
                              "M": "#2a2725", "W": "#e6e2da"}},
    "snout": {"c": "#e2ded6", "nose": "#151515"}, "ear": {"c": "#5f5b55"},
    "leg": {"c": "#2a2826"}, "tail": {"c": "#8a857c", "rings": {"c": "#2a2826", "n": 4}, "tip": {"c": "#2a2826", "frac": 0.15}}}}
P["skunk"] = {"skunk": {
    "body": {"c": "#141414", "noise": 6, "stripe": {"c": "#f2f2ee", "w": 2}},
    "head": {"c": "#141414", "stripe": {"c": "#f2f2ee", "w": 1}, "eyes": "#3a3a3a", "eye_row": 1},
    "snout": {"c": "#1c1c1c", "nose": "#2a2a2a"}, "ear": {"c": "#101010"}, "leg": {"c": "#101010"},
    "tail": {"c": "#161616", "stripe": {"c": "#f2f2ee", "w": 3}, "noise": 10}}}
P["mink"] = {"mink": {
    "body": {"c": "#3b2517", "noise": 8, "belly": "#46301f"}, "head": {"c": "#3b2517", "eyes": "#0c0c0c", "eye_row": 0},
    "snout": {"c": "#3b2517", "bottom": "#e9e2d6", "nose": "#141414"}, "ear": {"c": "#2e1c11"}, "leg": {"c": "#2a190f"}, "tail": {"c": "#2a190f"}}}
P["opossum"] = {"opossum": {
    "body": {"c": "#b8b2a6", "noise": 22, "speckle": {"c": "#5a5650", "p": 0.12}},
    "head": {"c": "#ece8e0", "eyes": "#0a0a0a", "eye_row": 1, "noise": 6}, "snout": {"c": "#ece8e0", "nose": "#e8a0a8"},
    "ear": {"c": "#141414"}, "leg": {"c": "#252322"}, "tail": {"c": "#e3b0aa", "noise": 6, "front": "#3a3634"}}}
P["beaver"] = {"beaver": {
    "body": {"c": "#6b4428", "noise": 12, "belly": "#7d5434"}, "head": {"c": "#6b4428", "eyes": "#0c0c0c", "eye_white": "#ece8de", "eye_inset": 0, "eye_row": 1},
    "snout": {"c": "#5a3820", "nose": "#141010"}, "ear": {"c": "#4e301a"}, "leg": {"c": "#3e2614"},
    "tail": {"c": "#2e2622", "speckle": {"c": "#463a33", "p": 0.4}}, "teeth": {"c": "#e3892a"}}}
P["squirrel"] = {
    # Eastern gray: salt-and-pepper back with a cinnamon wash, white belly and eye ring, silver-fringed tail
    "gray_squirrel": {
        "body": {"c": "#8e8b86", "noise": 6, "grizzle": {"c": ["#5f5c58", "#c2beb6", "#9c7c60"], "p": [0.24, 0.2, 0.16]}, "belly": "#f0ece4"},
        "head": {"c": "#8a857f", "noise": 6, "grizzle": {"c": ["#5f5c58", "#a8825e", "#bdb8b0"], "p": [0.18, 0.25, 0.15]},
                 "bottom": "#f0ece4", "eyes": "#0a0a0a", "eye_row": 1, "nose": "#5a4a44"},
        "ear": {"c": "#9c8f84", "noise": 6}, "leg": {"c": "#8f7c6a", "noise": 8},
        "tail": {"c": "#7a7774", "noise": 8, "grizzle": {"c": ["#3c3a38", "#c8c4bc"], "p": [0.3, 0.2]}, "fringe": "#ece8e2"}},
    # Fox squirrel: bigger, rusty-grizzled back, orange belly, orange-fringed tail
    "fox_squirrel": {
        "body": {"c": "#9a6740", "noise": 6, "grizzle": {"c": ["#5e3e28", "#c08a58", "#7d7168"], "p": [0.24, 0.24, 0.12]}, "belly": "#d9883f"},
        "head": {"c": "#8c6040", "noise": 6, "grizzle": {"c": ["#5e3e28", "#a87a50"], "p": [0.2, 0.25]},
                 "bottom": "#e0a46a", "eyes": "#0a0a0a", "eye_row": 1, "nose": "#4a3020"},
        "ear": {"c": "#c47a42", "noise": 6}, "leg": {"c": "#b8743c", "noise": 8},
        "tail": {"c": "#a4643a", "noise": 8, "grizzle": {"c": ["#4a3020", "#c88a50"], "p": [0.25, 0.2]}, "fringe": "#e29a52"}},
}
CLEAR = {"c": None}
P["songbird"] = {
    "cardinal": {"body": {"c": "#c3161c", "noise": 10}, "head": {"c": "#c81a20", "mask": "#141414", "mask_rows": [1, 3], "side_eyes": "#0a0a0a"},
                 "beak": {"c": "#e8642a"}, "crest": {"c": "#c3161c"}, "tail": {"c": "#9c1418"}, "wing": {"c": "#a8141a", "noise": 12}, "leg": {"c": "#b07a6a"}},
    "goldfinch": {"body": {"c": "#f2cd22", "noise": 8}, "head": {"c": "#f2cd22", "cap": "#121212", "side_eyes": "#0a0a0a"},
                  "beak": {"c": "#e8a26a"}, "crest": CLEAR, "tail": {"c": "#141414"}, "wing": {"c": "#141414", "wingbar": "#f4f4f0"}, "leg": {"c": "#c89a80"}},
    "robin": {"body": {"c": "#4b4a47", "noise": 8, "belly": "#d4612a", "front": "#d4612a"}, "head": {"c": "#2d2c2a", "side_eyes": "#f0f0f0"},
              "beak": {"c": "#e8c22a"}, "crest": CLEAR, "tail": {"c": "#2d2c2a"}, "wing": {"c": "#4b4a47", "noise": 10}, "leg": {"c": "#8a7060"}},
    "blue_jay": {"body": {"c": "#3d72c8", "noise": 10, "belly": "#eef1f5", "front": "#eef1f5"}, "head": {"c": "#3d72c8", "bottom": "#eef1f5", "mask": "#141414", "mask_rows": [3, 3], "side_eyes": "#0a0a0a"},
                 "beak": {"c": "#141414"}, "crest": {"c": "#3d72c8"}, "tail": {"c": "#2f5fb4", "rings": {"c": "#141414", "n": 3}},
                 "wing": {"c": "#3d72c8", "wingbar": "#f4f4f0", "rings": {"c": "#141414", "n": 3}}, "leg": {"c": "#2a2a2a"}},
    "eastern_bluebird": {"body": {"c": "#2f6dd8", "noise": 8, "belly": "#efe9e0", "front": "#c87a3e"}, "head": {"c": "#2f6dd8", "side_eyes": "#0a0a0a"},
                         "beak": {"c": "#1e1e1e"}, "crest": CLEAR, "tail": {"c": "#2a5fc4"}, "wing": {"c": "#2f6dd8", "noise": 10}, "leg": {"c": "#2a2a2a"}},
    "mourning_dove": {"body": {"c": "#a89c8c", "noise": 6, "belly": "#d8b8a8", "front": "#d4b4a2"}, "head": {"c": "#b0a49a", "side_eyes": "#0a0a0a"},
                      "beak": {"c": "#2a2a2a"}, "crest": CLEAR, "tail": {"c": "#8a8070", "tip": {"c": "#e8e4dc", "frac": 0.2}},
                      "wing": {"c": "#9a8e7e", "noise": 6, "speckle": {"c": "#1a1614", "p": 0.15}}, "leg": {"c": "#c87a7a"}},
}
P["chipmunk"] = {"chipmunk": {
    # stripes down the back: dark centre, pale and dark bands along each side
    "body": {"c": "#9a6a44", "noise": 8, "top_cols": ["#e6d8bc", "#1e1610", "#1e1610", "#e6d8bc"],
             "side_rows": ["#1e1610", "#e6d8bc", "#a87048"], "belly": "#efe6d6"},
    "head": {"c": "#9a6a44", "noise": 6,
             "front_map": ["cDc", "EwE", "wnw"],
             "front_colors": {"c": "#e6d8bc", "D": "#3a2a1e", "E": "#0a0a0a", "w": "#e6d8bc", "n": "#3a2a24"}},
    "ear": {"c": "#8a5a38"}, "leg": {"c": "#a87048"}, "tail": {"c": "#7a5638", "noise": 12}}}
P["coyote"] = {"coyote": {
    "body": {"c": "#9a8468", "noise": 6, "grizzle": {"c": ["#6a5a48", "#c0ac8c", "#8a8a86"], "p": [0.25, 0.2, 0.12]}, "belly": "#e6dcc8"},
    "head": {"c": "#a08868", "noise": 6, "grizzle": {"c": ["#7a6650", "#c0ac8c"], "p": [0.2, 0.2]},
             "eyes": "#1e140a", "eye_white": "#ece8de", "eye_inset": 0, "eye_row": 2, "bottom": "#e6dcc8"},
    "snout": {"c": "#c8b494", "noise": 6, "nose": "#1a1410"},
    "ear": {"c": "#8a7458", "back": "#4a3a2a"}, "leg": {"c": "#b8a07c", "noise": 8},
    "tail": {"c": "#8a7a62", "noise": 8, "grizzle": {"c": ["#5a4a3a", "#b8a88c"], "p": [0.25, 0.2]}, "tip": {"c": "#1a1612", "frac": 0.18}}}}
P["bluegill"] = {"bluegill": {"body": {"c": "#5b7a48", "noise": 8, "belly": "#e2a23a", "bars": {"c": "#3e5a30", "n": 5}, "spot": "#1a1f2e", "side_eyes": "#0a0a0a"}, "fin": {"c": "#4a6440"}}}
P["crappie"] = {"crappie": {"body": {"c": "#cdd4c6", "noise": 8, "belly": "#eef0ea", "speckle": {"c": "#2a3028", "p": 0.25}, "side_eyes": "#0a0a0a"}, "fin": {"c": "#9aa294", "speckle": {"c": "#2a3028", "p": 0.3}}}}
P["largemouth_bass"] = {"largemouth_bass": {"body": {"c": "#5d7f3a", "noise": 10, "belly": "#e6e6d6", "band": "#2c3d1c", "side_eyes": "#c8a020"}, "fin": {"c": "#4d6a30"}}}
P["channel_catfish"] = {"channel_catfish": {"body": {"c": "#6a7682", "noise": 8, "belly": "#e8e8e2", "speckle": {"c": "#2a3038", "p": 0.12}, "side_eyes": "#0a0a0a"},
                                            "fin": {"c": "#56616c"}, "whisker": {"c": "#3a4048"}}}

# ---------------------------------------------------------------- uv packing
def footprint(b):
    w, h, d = [max(0, math.ceil(v)) for v in b["size"]]
    return 2 * (w + d), d + h, (w, h, d)

def pack(parts):
    boxes = [b for p in parts for b in p["boxes"]]
    order = sorted(boxes, key=lambda b: -footprint(b)[1])
    for size in [(32, 32), (64, 32), (64, 64), (128, 64), (128, 128)]:
        W, H = size; x = y = row = 0; ok = True
        for b in order:
            fw, fh, _ = footprint(b)
            if x + fw > W: x, y, row = 0, y + row, 0
            if fw > W or y + fh > H: ok = False; break
            b["uv"] = [x, y]; x += fw; row = max(row, fh)
        if ok: return size
    raise SystemExit("textures too big")

# ---------------------------------------------------------------- painting
def noisy(c, amt):
    d = rnd.randint(-amt, amt); return tuple(max(0, min(255, v + d)) for v in c) + (255,)

def paint_box(img, b, style):
    if style.get("c") is None: return  # transparent (e.g. no crest on this bird)
    u, v = b["uv"]; w, h, d = footprint(b)[2]
    amt = style.get("noise", 6)
    base = hexc(style["c"])
    faces = {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "side1": (u, v + d, d, h),
             "front": (u + d, v + d, w, h), "side2": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}
    def put(x, y, col):
        if 0 <= x < img.width and 0 <= y < img.height: img.putpixel((x, y), noisy(col, amt))
    for f, (fx, fy, fw, fh) in faces.items():
        col = hexc(style[f]) if f in style else hexc(style["sides"]) if f.startswith("side") and "sides" in style else base
        for i in range(fw):
            for j in range(fh):
                fc = col
                if "grizzle" in style and f != "bottom" and f not in style:
                    roll = rnd.random()
                    for gc, gp in zip(style["grizzle"]["c"], style["grizzle"]["p"]):
                        if roll < gp: fc = hexc(gc); break
                        roll -= gp
                # depth coordinate t: 0 = front (-z), 1 = back
                t = None
                if f == "side1": t = 1 - (i + .5) / max(1, fw)
                elif f == "side2": t = (i + .5) / max(1, fw)
                elif f == "top": t = 1 - (j + .5) / max(1, fh)
                elif f == "bottom": t = (j + .5) / max(1, fh)
                c = fc
                if "top_cols" in style and f == "top":
                    c = hexc(style["top_cols"][min(i, len(style["top_cols"]) - 1)])
                if "side_rows" in style and f.startswith("side"):
                    c = hexc(style["side_rows"][min(j, len(style["side_rows"]) - 1)])
                if "fringe" in style and (f.startswith("side") or f == "back") and (j == 0 or j == fh - 1) and rnd.random() < 0.55:
                    c = hexc(style["fringe"])
                if "belly" in style:
                    if f == "bottom" or (f.startswith("side") and j >= fh * 0.65): c = hexc(style["belly"])
                if "cap" in style and (f == "top" or (f in ("front", "side1", "side2", "back") and j < max(1, fh * 0.35))):
                    c = hexc(style["cap"])
                if "rings" in style and t is not None and f != "bottom":
                    n = style["rings"]["n"]
                    if int(t * n * 2) % 2 == 1: c = hexc(style["rings"]["c"])
                if "tip" in style and ((t is not None and t > 1 - style["tip"]["frac"]) or f == "back"):
                    c = hexc(style["tip"]["c"])
                if "stripe" in style:
                    sw = style["stripe"]["w"]
                    if f in ("top", "back", "front") and abs(i - (fw - 1) / 2) <= (sw - 1) / 2 + 0.01 and (f != "front" or j < fh / 2):
                        c = hexc(style["stripe"]["c"])
                if "bars" in style and f.startswith("side") and t is not None and j < fh * 0.65:
                    n = style["bars"]["n"]
                    if int(t * n * 2) % 2 == 1: c = hexc(style["bars"]["c"])
                if "band" in style and f.startswith("side") and abs(j - fh * 0.45) < max(1, fh * 0.12):
                    c = hexc(style["band"])
                if "wingbar" in style and f.startswith("side") and j == fh // 2:
                    c = hexc(style["wingbar"])
                if "speckle" in style and rnd.random() < style["speckle"]["p"] and f != "bottom":
                    c = hexc(style["speckle"]["c"])
                if "mask" in style and f in ("front", "side1", "side2") and style["mask_rows"][0] <= j <= style["mask_rows"][1]:
                    if f == "front" or (f == "side1" and i >= fw - 2) or (f == "side2" and i <= 1): c = hexc(style["mask"])
                put(fx + i, fy + j, c)
    fx, fy, fw, fh = faces["front"]
    if "eyes" in style and fw >= 3:
        r = style.get("eye_row", 1)
        inset = style.get("eye_inset", 1 if fw >= 5 else 0)
        left, right = fx + inset, fx + fw - 1 - inset
        for x in (left, right):
            img.putpixel((x, fy + r), hexc(style["eyes"]) + (255,))
        if "eye_white" in style:      # pupil on the outside, white toward the middle (like the cow, pig, polar bear)
            img.putpixel((left + 1, fy + r), hexc(style["eye_white"]) + (255,))
            img.putpixel((right - 1, fy + r), hexc(style["eye_white"]) + (255,))
    if "front_map" in style:       # hand-drawn face, one character per pixel (" " = leave as is)
        for j, row in enumerate(style["front_map"]):
            for i, ch in enumerate(row):
                if ch != " " and i < fw and j < fh:
                    img.putpixel((fx + i, fy + j), hexc(style["front_colors"][ch]) + (255,))
    if "nose" in style:
        cx = fx + fw // 2
        for x in ([cx - 1, cx] if fw % 2 == 0 else [cx]):
            img.putpixel((x, fy), hexc(style["nose"]) + (255,))
    if "side_eyes" in style:
        s1x, s1y, s1w, s1h = faces["side1"]; s2x, s2y, s2w, s2h = faces["side2"]
        r = style.get("eye_row", 1 if s1h > 2 else 0)
        e1 = (s1x + s1w - 2 if s1w > 2 else s1x, s1y + r); e2 = (s2x + 1 if s2w > 2 else s2x, s2y + r)
        if "eye_ring" in style:
            for (ex, ey) in (e1, e2):
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if 0 <= ex + dx < img.width and 0 <= ey + dy < img.height: img.putpixel((ex + dx, ey + dy), hexc(style["eye_ring"]) + (255,))
        img.putpixel(e1, hexc(style["side_eyes"]) + (255,))
        img.putpixel(e2, hexc(style["side_eyes"]) + (255,))
    if "spot" in style:
        s1x, s1y, s1w, s1h = faces["side1"]; s2x, s2y, s2w, s2h = faces["side2"]
        img.putpixel((s1x + s1w - 3, s1y + 2), hexc(style["spot"]) + (255,))
        img.putpixel((s2x + 2, s2y + 2), hexc(style["spot"]) + (255,))

def emit_model(name, parts, size):
    out = {"texture": list(size), "parts": [
        {"name": p["name"], "parent": p["parent"], "pivot": p["pivot"], "rot": p["rot"], "role": p["role"],
         "boxes": [{"uv": b["uv"], "from": b["from"], "size": b["size"], "mirror": b["mirror"]} for b in p["boxes"]]}
        for p in parts]}
    path = os.path.join(ROOT, "critter_models", name + ".json"); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(out, f, indent=1)

def emit_textures(parts, size, variants):
    for tex, styles in variants.items():
        img = Image.new("RGBA", size, (0, 0, 0, 0))
        for p in parts:
            for b in p["boxes"]:
                paint_box(img, b, styles.get(b["paint"], {"c": "#ff00ff"}))
        path = os.path.join(ROOT, "textures/entity", tex + ".png"); os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path)

# ---------------------------------------------------------------- turkey (hand-written TurkeyModel.java UVs)
def turkey_texture():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    def bx(u, v, w, h, d, style, paint="x"):
        b = {"uv": [u, v], "size": [w, h, d]}; paint_box(img, b, style)
    bx(0, 0, 3, 6, 3, {"c": "#c0d0e8", "noise": 10, "bottom": "#c42a2a"})                       # bare blue-gray neck
    bx(12, 0, 4, 4, 4, {"c": "#a8c0e0", "noise": 8, "side_eyes": "#0a0a0a", "bottom": "#c42a2a"})  # head
    bx(28, 0, 2, 1, 2, {"c": "#d8c49a"})                                                       # beak
    bx(28, 4, 1, 3, 1, {"c": "#c41c1c"})                                                       # wattle
    bx(0, 16, 8, 8, 10, {"c": "#4a3220", "noise": 18, "speckle": {"c": "#7a5a2a", "p": 0.25}, "front": "#2a1c12"})  # bronze body
    bx(36, 16, 1, 6, 8, {"c": "#3c2a1c", "noise": 12, "rings": {"c": "#d8d0c0", "n": 4}})      # barred wings
    bx(36, 0, 1, 6, 1, {"c": "#c48a7a"})                                                       # legs
    # Tail fan: chestnut with a dark band and buff tips, painted on both faces
    for face_x in (1, 1 + 16 + 1):
        for i in range(16):
            for j in range(12):
                c = "#6a4426"
                if j < 2: c = "#c8a46a"
                elif j < 4: c = "#1e140c"
                elif (i + j) % 3 == 0: c = "#7e5430"
                img.putpixel((face_x + i, 47 + j), noisy(hexc(c), 8))
    for i in range(34):
        img.putpixel((i, 46), noisy(hexc("#c8a46a"), 6))
    img.save(os.path.join(ROOT, "textures/entity/turkey.png"))

# ---------------------------------------------------------------- item icons
FISH_ICON = {
    "bluegill": ("#5b7a48", "#e2a23a", "deep"), "crappie": ("#cdd4c6", "#eef0ea", "deep"),
    "largemouth_bass": ("#5d7f3a", "#e6e6d6", "long"), "channel_catfish": ("#6a7682", "#e8e8e2", "long"),
}
def fish_icon(name, back, belly, shape, cooked=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if cooked: back, belly = "#9a6430", "#d0a060"
    cx, cy = 7.0, 8.0
    rx, ry = (5.5, 3.6) if shape == "deep" else (6.4, 2.6)
    for x in range(16):
        for y in range(16):
            dx, dy = (x - cx) / rx, (y - cy) / ry
            if dx * dx + dy * dy <= 1.0:
                c = belly if y > cy + 0.5 else back
                if not cooked and name == "crappie" and rnd.random() < 0.2: c = "#2a3028"
                if not cooked and name == "bluegill" and int(x) % 3 == 0 and y <= cy: c = "#3e5a30"
                if not cooked and name == "largemouth_bass" and abs(y - cy) < 0.6: c = "#2c3d1c"
                img.putpixel((x, y), noisy(hexc(c), 10))
    for y in range(int(cy - 3), int(cy + 4)):          # tail
        span = abs(y - cy)
        for x in range(13, 13 + int(1 + span)):
            if x < 16: img.putpixel((x, y), noisy(hexc(back), 8))
    img.putpixel((int(cx - rx + 2), int(cy - 1)), (10, 10, 10, 255))
    if name == "channel_catfish" and not cooked:
        for x in range(0, 3): img.putpixel((x, int(cy + 1)), (40, 44, 50, 255))
    return img

def meat_icon(base, fat, shape):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rows = {"drum": ["......xxxx......", ".....xxxxxx.....", "....xxxxxxxx....", "...xxxxxxxxxx...", "...xxxxxxxxxx...",
                     "...xxxxxxxxxx...", "....xxxxxxxx....", ".....xxxxxx.....", "......xxxx......", "......bbb.......",
                     ".....bbbbb......", "......bbb......."]}[shape]
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch == 'x':
                edge = x == 0 or r[x - 1] != 'x' or x + 1 >= len(r) or r[x + 1] != 'x' or y == 0 or rows[y - 1][x] != 'x' or y + 1 >= len(rows) or rows[y + 1][x] != 'x'
                img.putpixel((x, y + 2), noisy(hexc(fat if edge else base), 10))
            elif ch == 'b': img.putpixel((x, y + 2), noisy(hexc("#efe6cf"), 6))
    return img

def tail_icon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for k in range(12):
        x0, y0 = 2 + k, 13 - k
        for d in range(-2, 3):
            if abs(d) == 2 and k in (0, 11): continue
            c = "#2a2826" if (k // 2) % 2 else "#8a857c"
            if k >= 10: c = "#2a2826"
            px, py = x0 + d, y0 + d
            if 0 <= px < 16 and 0 <= py < 16: img.putpixel((px, py), noisy(hexc(c), 12))
    return img

def save_item(name, img):
    path = os.path.join(ROOT, "textures/item", name + ".png"); os.makedirs(os.path.dirname(path), exist_ok=True); img.save(path)

if __name__ == "__main__":
    for model, parts in MODELS.items():
        size = pack(parts)
        emit_model(model, parts, size)
        emit_textures(parts, size, P[model])
        print(model, size, list(P[model]))
    turkey_texture()
    for name, (back, belly, shape) in FISH_ICON.items():
        save_item(name, fish_icon(name, back, belly, shape))
        save_item("cooked_" + name, fish_icon(name, back, belly, shape, cooked=True))
    save_item("raw_turkey", meat_icon("#e0a090", "#f2c8bc", "drum"))
    save_item("cooked_turkey", meat_icon("#b0702e", "#d29a50", "drum"))
    save_item("raccoon_tail", tail_icon())

# ---------------------------------------------------------------- extra icons (buckets, cap)
def bucket_icon(fish_name):
    back, belly, shape = FISH_ICON[fish_name]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(6, 16):                     # pail
        half = 5 + (15 - y) // 4
        for x in range(8 - half, 8 + half):
            c = "#9aa0a6" if 8 - half < x < 7 + half else "#5a5e62"
            if y == 15: c = "#5a5e62"
            img.putpixel((x, y), noisy(hexc(c), 6))
    for x in range(3, 13):                      # water
        img.putpixel((x, 6), noisy(hexc("#3f76e4"), 10)); img.putpixel((x, 7), noisy(hexc("#3366cc"), 10))
    f = fish_icon(fish_name, back, belly, shape).resize((10, 10), Image.NEAREST)
    img.alpha_composite(f, (3, 0))
    return img

def coonskin_icon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(4, 11):
        for x in range(2, 12):
            if (x - 7) ** 2 / 25 + (y - 10) ** 2 / 36 <= 1: img.putpixel((x, y), noisy(hexc("#7a6a58"), 14))
    for k in range(8):                          # tail
        c = "#2a2826" if k % 2 else "#8a857c"
        for d in range(2):
            img.putpixel((11 + k // 2, 9 + k), noisy(hexc(c), 10)) if 9 + k < 16 else None
            if 12 + k // 2 < 16 and 9 + k < 16: img.putpixel((12 + k // 2, 9 + k), noisy(hexc(c), 10))
    return img

def coonskin_armor():
    """64x32 humanoid armor layer: only the helmet (head) box is painted, ringed fur at the back."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    paint_box(img, {"uv": [0, 0], "size": [8, 8, 8]}, {"c": "#7a6a58", "noise": 16, "back": "#2a2826"})
    for j in range(8):                           # cut the face open
        for i in range(8):
            if j >= 3: img.putpixel((8 + i, 8 + j), (0, 0, 0, 0))
    for j in range(8):                           # ringed tail down the back
        for i in range(3, 5):
            c = "#2a2826" if (j // 2) % 2 else "#a39c90"
            img.putpixel((24 + i, 8 + j), noisy(hexc(c), 8))
    return img

if __name__ == "__main__":
    for n in FISH_ICON: save_item(n + "_bucket", bucket_icon(n))
    save_item("coonskin_cap", coonskin_icon())
    p = os.path.join(ROOT, "textures/entity/equipment/humanoid/coonskin.png"); os.makedirs(os.path.dirname(p), exist_ok=True)
    coonskin_armor().save(p)

# ---------------------------------------------------------------- deer antlers (stage groups, read by DeerModel.java)
ANTLER_UV = [0, 42]           # all antler boxes share one antler-coloured patch of deer.png
def antler_groups():
    """Right-side boxes relative to the antler base; the left side is mirrored. Head-relative pivots.
    y is up-negative, -z is forward. Beams rise, curve forward, and tines rise off the top (whitetail style)."""
    R = {
        "0": [((-0.5, -1, -0.5), (1, 1, 1))],                                                  # little nubs (stage 0)
        "1": [((-0.5, -3, -0.5), (1, 3, 1))],                                                  # spikes
        "2": [((-0.5, -5, -0.5), (1, 5, 1)), ((-0.5, -4, -2.5), (1, 1, 2)), ((-0.5, -6, -2.5), (1, 2, 1))],  # forks
        "3": [((-0.5, -4, -0.5), (1, 4, 1)), ((-0.5, -5, -4.5), (1, 1, 5)),                    # 6-8 point
              ((-0.5, -3, -1.5), (1, 1, 1)), ((-0.5, -7, -1.5), (1, 2, 1)), ((-0.5, -7, -3.5), (1, 2, 1)),
              ((-0.5, -6, -4.5), (1, 1, 1))],
        "4": [((-0.5, -5, -0.5), (1, 5, 1)), ((-0.5, -6, -6.5), (1, 1, 7)),                    # 10-point trophy
              ((-0.5, -4, -1.5), (1, 1, 1)), ((-0.5, -9, -1.5), (1, 3, 1)), ((-0.5, -9, -3.5), (1, 3, 1)),
              ((-0.5, -8, -5.5), (1, 2, 1)), ((-0.5, -7, -6.5), (1, 1, 1))],
        "drop3": [((-0.5, -4, -2.5), (1, 2, 1)), ((-0.5, -4, -4.5), (1, 3, 1))],             # drop tines hang off the beam
        "drop4": [((-0.5, -5, -3.5), (1, 3, 1)), ((-0.5, -5, -5.5), (1, 2, 1)), ((-0.5, -5, -1.5), (1, 2, 1))],
        # --- super-rare racks (stage 4 only) ---
        "cactus": [((-0.5, -5, -0.5), (1, 5, 1)), ((-0.5, -6, -5.5), (1, 1, 6)),             # knobby points everywhere
                   ((-0.5, -8, -1.5), (1, 2, 1)), ((-0.5, -7, -2.5), (1, 1, 1)), ((-0.5, -9, -3.5), (1, 3, 1)),
                   ((-0.5, -7, -4.5), (1, 1, 1)), ((-0.5, -8, -5.5), (1, 2, 1)),
                   ((-1.5, -6, -2.5), (1, 1, 1)), ((-1.5, -6, -4.5), (1, 1, 1)), ((-1.5, -3, -0.5), (1, 1, 1)),
                   ((0.5, -6, -3.5), (1, 1, 1)), ((0.5, -4, -0.5), (1, 1, 1)), ((-1.5, -7, -5.5), (1, 1, 1)),
                   ((-0.5, -5, -2.5), (1, 1, 1)), ((-0.5, -5, -4.5), (1, 2, 1))],
        "palmated": [((-0.5, -4, -0.5), (1, 4, 1)), ((-0.5, -8, -5.5), (1, 4, 6)),            # flat moose-like paddles
                     ((-0.5, -9, -1.5), (1, 1, 1)), ((-0.5, -10, -3.5), (1, 2, 1)), ((-0.5, -9, -5.5), (1, 1, 1)),
                     ((-0.5, -9, -0.5), (1, 1, 1))],
    }
    LOPSIDED_L = [((-0.5, -4, -0.5), (1, 4, 1))]          # a lone spike on the other side
    groups = {}
    for key, boxes in list(R.items()) + [("lopsided", None)]:
        parts = []
        for side, sx in (("r", -1), ("l", 1)):
            bx = boxes if key != "lopsided" else (R["4"] if side == "r" else LOPSIDED_L)
            parts.append({"name": f"antlers_{key}_{side}", "pivot": [sx * 2.5, -10, -2], "rot": [-0.1, sx * 0.4, sx * 0.6],
                          "boxes": [{"uv": ANTLER_UV, "from": list(f), "size": list(sz), "mirror": sx > 0} for f, sz in bx]})
        groups[key] = parts
    return groups

ANTLER_COLOURS = {"": ("#d6c49c", "#bfa97c"), "_dark": ("#957652", "#7e6242"), "_chocolate": ("#5e4430", "#4c3626")}

def paint_antler_patch(img, colour):
    r = random.Random(1775)  # Boonesborough
    light, dark = ANTLER_COLOURS[colour]
    for x in range(0, 64):
        for y in range(42, 64):
            base = hexc(light) if r.random() > 0.25 else hexc(dark)
            img.putpixel((x, y), tuple(max(0, min(255, v + r.randint(-8, 8))) for v in base) + (255,))

def antler_textures():
    base = Image.open(os.path.join(ROOT, "textures/entity/deer.png")).convert("RGBA")
    for colour in ANTLER_COLOURS:
        img = base.copy(); paint_antler_patch(img, colour)
        img.save(os.path.join(ROOT, f"textures/entity/deer{colour}.png"))

if __name__ == "__main__":
    with open(os.path.join(ROOT, "critter_models", "deer_antlers.json"), "w") as f:
        json.dump({"head_pivot": [0, 8, -7], "groups": antler_groups()}, f, indent=1)
    antler_textures()


# ---------------------------------------------------------------- primitive skull mount
def mount_assets():
    """Bleached deer skull nailed to a rough plank. Skull coordinates match the deer head, so the same
    antler groups (deer_antlers.json, head-relative) sit on it exactly."""
    hp = [0, 21, -1]          # where the skull's 'head pivot' sits in the mount model
    # The skull is built hollow: eye sockets go clean through the sides, and two nostril holes
    # (with a bony septum between) run back into the snout.
    parts = [
        part("plank", hp, [box("plank", (-5, -14, 1), (10, 11, 1), "plank")], "mount_plank"),
        part("skull", hp, [
            box("cranium_top", (-3, -10, -6), (6, 2, 7), "skull"),
            box("cranium_bottom", (-3, -7, -6), (6, 2, 7), "skull"),
            box("cranium_back", (-3, -8, -3), (6, 1, 4), "skull"),      # behind the eye sockets
            box("brow", (-3, -8, -6), (6, 1, 1), "skull"),              # in front of the eye sockets
            box("snout_top", (-2.5, -8, -9), (5, 1, 3), "skull"),
            box("snout_wall_r", (-2.5, -7, -9), (1, 1, 3), "skull"),
            box("septum", (-0.5, -7, -9), (1, 1, 3), "skull"),
            box("snout_wall_l", (1.5, -7, -9), (1, 1, 3), "skull"),
            box("snout_bottom", (-2.5, -6, -9), (5, 1, 3), "skull"),
            box("nasal_cavity", (-1.5, -7, -6.25), (3, 1, 0.25), "cavity"),   # dark back wall of the nostrils
        ], "mount_skull"),
    ]
    size = pack(parts)
    size = (64, 64)                                   # antler patch lives at y 42-63, like deer.png
    styles = {"plank": {"c": "#8a6440", "noise": 14, "speckle": {"c": "#6a4a2c", "p": 0.18}},
              "skull": {"c": "#e8e2d2", "noise": 8, "speckle": {"c": "#d2c8b2", "p": 0.15}},
              "cavity": {"c": "#3a322c", "noise": 4}}
    emit_model("deer_mount", parts, size)
    for colour in ANTLER_COLOURS:
        img = Image.new("RGBA", size, (0, 0, 0, 0))
        for p_ in parts:
            for b in p_["boxes"]:
                paint_box(img, b, styles[b["paint"]])
        for x, y in [(3 + 1, 1 + 1), (3 + 8, 1 + 1), (3 + 1, 1 + 9), (3 + 8, 1 + 9)]:   # iron nails on the plank front
            fx, fy = parts[0]["boxes"][0]["uv"]; img.putpixel((fx + 1 + x - 3, fy + 1 + y - 1), (70, 70, 74, 255))
        paint_antler_patch(img, colour)
        img.save(os.path.join(ROOT, f"textures/entity/deer_mount{colour}.png"))
    mount_icons()

MOUNT_ICONS = {   # 16x16 antler silhouettes ('#' pixels), drawn over a skull-on-plank base; tinted by antler colour
    "s1": ["", "", "", "", "    #      #    ", "    #      #    ", "    #      #    "],
    "s2": ["", "", "", "  #  #    #  #  ", "  #  #    #  #  ", "   ###    ###   ", "    #      #    ", "    #      #    "],
    "s3": ["", " # #  ", " # #  #    # # #", " #####      ### ", "#   #      #   #", "    #      #    ", "    #      #    "],
    "s4": ["# # #      # # #", "# # #      # # #", "#####      #####", "#   #      #   #", "    #      #    ", "    #      #    ", "    #      #    "],
    "drop3": ["", " # #        # # ", " #####    ##### ", " #  ##    ##  # ", "  # #      # #  ", "    #      #    ", "    #      #    "],
    "drop4": ["# # #      # # #", "#####      #####", "#  ##      ##  #", " # #        # # ", " # #        # # ", "    #      #    ", "    #      #    "],
    "cactus": ["## # #    # # ##", "# ####    #### #", "######    ######", "## #  #  #  # ##", "  ###      ###  ", "  # #      # #  ", "    #      #    "],
    "palmated": [" # # #    # # # ", "#####      #####", "#####      #####", "#####      #####", "  ##        ##  ", "    #      #    ", "    #      #    "],
    "lopsided": ["# # #           ", "# # #           ", "#####           ", "#   #      #    ", "    #      #    ", "    #      #    ", "    #      #    "],
}

def mount_icons():
    base = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(3, 13):                     # plank
        for y in range(6, 16):
            base.putpixel((x, y), noisy(hexc("#8a6440"), 12))
    for x, y in [(4, 7), (11, 7), (4, 14), (11, 14)]: base.putpixel((x, y), (70, 70, 74, 255))
    for y, row in enumerate(["  ####  ", " ###### ", " #o##o# ", " ###### ", "  ####  ", "  #nn#  ", "   ##   "]):
        for x, ch in enumerate(row):
            if ch == "#": base.putpixel((4 + x, 7 + y), noisy(hexc("#ece6d6"), 6))
            elif ch == "o": base.putpixel((4 + x, 7 + y), (42, 36, 32, 255))
            elif ch == "n": base.putpixel((4 + x, 7 + y), (58, 50, 44, 255))
    save_item("deer_skull_mount_base", base)
    for key, rows in MOUNT_ICONS.items():
        layer = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch == "#" and x < 16: layer.putpixel((x, y), noisy((236, 236, 236), 10))
        save_item(f"deer_skull_mount_antlers_{key}", layer)

if __name__ == "__main__":
    mount_assets()


# ---------------------------------------------------------------- pelts and spawn eggs
def pelt_icon(base, dark, light):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rows = ["   ##      ##   ", "   ###    ###   ", "    ########    ", "   ##########   ", "  ############  ", " ############## ",
            " ############## ", "  ############  ", "  ############  ", "   ##########   ", "   ##########   ", "  ####    ####  ",
            "  ###      ###  ", "       ##       ", "       ##       ", "       #        "]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "#":
                edge = x == 0 or row[x - 1] != "#" or x + 1 >= len(row) or row[x + 1] != "#"
                c = dark if edge else (light if 5 <= x <= 10 and 4 <= y <= 10 else base)
                img.putpixel((x, y), noisy(hexc(c), 12))
    return img

EGGS = {"boar": ("#5a3d2b", "#c8917a"), "deer": ("#a0703f", "#efe4cf"), "duck": ("#9a9a92", "#1f6b3a"),
        "turkey": ("#4a3220", "#c42a2a"), "black_bear": ("#1d1b1b", "#b08a5e"), "raccoon": ("#7f7b74", "#1c1c1c"),
        "skunk": ("#141414", "#f2f2ee"), "mink": ("#3b2517", "#e9e2d6"), "opossum": ("#b8b2a6", "#e3b0aa"),
        "beaver": ("#6b4428", "#2e2622"), "squirrel": ("#8e8b86", "#b06a3a"), "songbird": ("#c3161c", "#3d72c8"),
        "bluegill": ("#5b7a48", "#e2a23a"), "crappie": ("#cdd4c6", "#2a3028"), "largemouth_bass": ("#5d7f3a", "#2c3d1c"),
        "channel_catfish": ("#6a7682", "#2a3038"), "chipmunk": ("#9a6a44", "#1e1610"), "coyote": ("#9a8468", "#e6dcc8")}

def egg_icon(base, spots, seed):
    r = random.Random(seed)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    b, sp = hexc(base), hexc(spots)
    outline = tuple(max(0, int(v * 0.55)) for v in b)
    for y in range(16):
        for x in range(16):
            # egg: narrower at the top
            cy = 8.6; ry = 6.6; rx = 4.6 + 0.9 * max(0, (y - 4) / 8)
            if ((x - 7.5) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0:
                inner = ((x - 7.5) / (rx - 1)) ** 2 + ((y - cy) / (ry - 1)) ** 2 <= 1.0
                c = b if inner else outline
                img.putpixel((x, y), tuple(max(0, min(255, v + r.randint(-6, 6))) for v in c) + (255,))
    for _ in range(7):                                  # spots
        x, y = r.randint(4, 11), r.randint(4, 13)
        if img.getpixel((x, y))[3] and img.getpixel((x, y))[:3] != outline:
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                if r.random() < 0.8 and img.getpixel((x + dx, y + dy))[3]: img.putpixel((x + dx, y + dy), sp + (255,))
    img.putpixel((6, 4), (255, 255, 255, 180)); img.putpixel((5, 5), (255, 255, 255, 140))   # shine
    return img

if __name__ == "__main__":
    for i, (mob, (base, spots)) in enumerate(EGGS.items()):
        save_item(f"{mob}_spawn_egg", egg_icon(base, spots, 1000 + i))
