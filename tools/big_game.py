"""
Big game and predators: elk, moose, pronghorn, grizzly bear, bobcat. Models, textures and spawn-egg icons,
built with the critters.py spec helpers (kept in their own file so the older critters' textures don't shift).
  python3 tools/big_game.py
Coordinates are model pixels: y points down, feet at y=24, the head faces -z.
"""
from critters import box, part, quad, pack, emit_model, emit_textures, egg_icon, save_item

def mirror(boxes, name, paint):
    """Right-side boxes (x < 0) plus their mirror images on the left."""
    out = []
    for i, (f, s) in enumerate(boxes):
        out.append(box(f"{name}_r{i}", f, s, paint))
        out.append(box(f"{name}_l{i}", (-f[0] - s[0], f[1], f[2]), s, paint, mirror=True))
    return out

def legs(bw, bl, lw, lh, inset=1):
    ps = []
    for nm, sx, sz in [("leg_rf", -1, -1), ("leg_lf", 1, -1), ("leg_rb", -1, 1), ("leg_lb", 1, 1)]:
        x = sx * (bw / 2 - lw / 2); z = sz * (bl / 2 - lw / 2 - inset)
        ps.append(part(nm, (x, 24 - lh, z), [box("leg", (-lw / 2, 0, -lw / 2), (lw, lh, lw), "leg", mirror=sx > 0)], nm))
    return ps

def cervid(body, leg, head_y, neck, head, snout, ears, tail, extra_head=(), extra_body=()):
    """Long-legged grazer with a neck. neck/head/snout = (from, size) in head-pivot space."""
    bw, bh, bl = body; lw, lh = leg
    top = 24 - lh - bh
    parts = [part("body", (0, top + bh / 2, 0), [box("body", (-bw / 2, -bh / 2, -bl / 2), body, "body")] + list(extra_body), "body")]
    hb = [box("neck", *neck, "mane"), box("head", *head, "head"), box("snout", *snout, "snout")]
    ef, es = ears
    hb += [box("ear_r", ef, es, "ear"), box("ear_l", (-ef[0] - es[0], ef[1], ef[2]), es, "ear", mirror=True)]
    hb += list(extra_head)
    parts.append(part("head", (0, top + head_y, -bl / 2 + 1), hb, "head"))
    parts += legs(bw, bl, lw, lh)
    tf, ts, trot = tail
    parts.append(part("tail", (0, top + 1, bl / 2 - 0.5), [box("tail", tf, ts, "tail")], "tail", rot=(trot, 0, 0)))
    return parts

# ---- elk: tall, tan, dark neck and mane, pale rump; bulls carry big swept-back racks
ELK_ANTLER = [((-3, -11, -6), (1, 7, 1)),     # beam rising off the skull
              ((-4, -17, -6), (1, 7, 1)),     # beam stepping out and on up (overlaps the lower beam)
              ((-4, -18, -6), (1, 1, 7)),     # sweeping back
              ((-4, -21, 0), (1, 3, 1)),      # top tine at the back
              ((-4, -20, -3), (1, 2, 1)),     # middle back tine
              ((-3, -6, -9), (1, 1, 3)),      # brow tine
              ((-3, -9, -9), (1, 1, 3)),      # bez tine
              ((-4, -14, -9), (1, 1, 3))]     # royal tine
elk = cervid((10, 11, 22), (3, 14), -9,
             neck=((-3, -1, -4), (6, 14, 7)), head=((-3, -4, -11), (6, 6, 7)), snout=((-2, -2, -15), (4, 4, 4)),
             ears=((-5, -6, -6), (2, 3, 1)), tail=((-1, 0, 0), (2, 3, 1), 0.3),
             extra_head=mirror(ELK_ANTLER, "antler", "antler"))

# ---- moose: huge, dark, humped shoulders, pale legs, long drooping muzzle, bell; bulls carry palmate paddles
MOOSE_ANTLER = [((-5, -5, -7), (2, 1, 2)),     # pedicel
                ((-9, -6, -9), (4, 2, 6)),     # inner paddle
                ((-12, -7, -9), (3, 2, 6)),    # outer paddle, a step higher
                ((-12, -9, -9), (1, 2, 1)), ((-10, -9, -9), (1, 2, 1)), ((-12, -9, -6), (1, 2, 1)),   # points along the rim
                ((-12, -9, -4), (1, 2, 1)), ((-8, -8, -9), (1, 2, 1))]
moose = cervid((11, 13, 22), (3, 17), -5,
               neck=((-3.5, -2, -5), (7, 10, 8)), head=((-3, -4, -13), (6, 6, 9)), snout=((-3.5, -2, -17), (7, 6, 4)),
               ears=((-5, -6, -6), (2, 3, 1)), tail=((-1, 0, 0), (2, 2, 1), 0.3),
               extra_head=mirror(MOOSE_ANTLER, "antler", "antler") + [box("bell", (-0.5, 2, -10), (1, 5, 2), "bell")],
               extra_body=[box("hump", (-4, -9.5, -10), (8, 3, 9), "body")])

# ---- pronghorn: slim and fast, tan over white, white neck bands, short black pronged horns
PRONG_HORN = [((-2, -8, -4), (1, 5, 1)), ((-2, -6, -5), (1, 1, 1)), ((-2, -9, -3), (1, 1, 1))]
pronghorn = cervid((6, 8, 15), (2, 13), -7,          # "racer": long legs, tall ears
                   neck=((-1.5, -1, -3), (3, 9, 4)), head=((-2, -3, -8), (4, 4, 5)), snout=((-1.5, -2, -10), (3, 3, 2)),
                   ears=((-3, -7, -4), (1, 4, 1)), tail=((-1, 0, 0), (2, 2, 1), 0.4),
                   extra_head=mirror(PRONG_HORN + [((-2, -10, -3), (1, 1, 1))], "horn", "horn"))

# ---- grizzly: bigger than the black bear, with the shoulder hump and a dished face
grizzly = quad((14, 13, 24), (5, 10), (10, 9, 8), snout=(5, 5, 5), ears=(3, 3), tail=(3, 3, 2, 0.6), head_up=-2)
grizzly[0]["boxes"].append(box("hump", (-5, -9.5, -10), (10, 3, 8), "body"))

# ---- bobcat: a long-legged, lynx-faced wildcat with tufted ears and a stubby tail
bobcat = quad((5, 5, 13), (2, 8), (5, 4, 4), snout=(3, 2, 2), ears=(2, 3), tail=(2, 2, 3, -0.7), leg_inset=0, extra_head=[   # "lanky"
    box("tuft_r", (-2, -7.5, -2.5), (1, 2, 1), "tuft"), box("tuft_l", (1, -7.5, -2.5), (1, 2, 1), "tuft", mirror=True)])

CLEAR = {"c": None}
ELK = {"body": {"c": "#b48a58", "noise": 8, "back": "#e6d4ac", "belly": "#5e4028", "bottom": "#5e4028"},
       "head": {"c": "#6a4a2e", "noise": 6, "side_eyes": "#100c08", "eye_row": 2},
       "snout": {"c": "#4e3622", "noise": 6, "front": "#2a1c12"}, "mane": {"c": "#4e3420", "noise": 10},
       "ear": {"c": "#7a5838"}, "leg": {"c": "#4a3220", "noise": 6}, "tail": {"c": "#e6d4ac"}}
MOOSE = {"body": {"c": "#2e2018", "noise": 8}, "head": {"c": "#5a4230", "noise": 6, "side_eyes": "#080604", "eye_row": 2},
         "snout": {"c": "#4a3626", "noise": 6, "front": "#241810", "top": "#5a4230"}, "mane": {"c": "#2a1c14", "noise": 8},
         "bell": {"c": "#2a1c14"}, "ear": {"c": "#3a2a1e"}, "leg": {"c": "#9a8a74", "noise": 8, "top": "#2e2018"},
         "tail": {"c": "#2e2018"}}
PRONG = {"body": {"c": "#c48a4c", "noise": 6, "belly": "#f2ece0", "back": "#f2ece0"},
         "mane": {"c": "#c48a4c", "noise": 4, "side_rows": ["#c48a4c", "#f2ece0", "#c48a4c", "#c48a4c", "#f2ece0", "#f2ece0", "#c48a4c", "#c48a4c"]},
         "head": {"c": "#c48a4c", "noise": 4, "side_eyes": "#0a0806", "eye_ring": "#f2ece0", "eye_row": 1, "bottom": "#f2ece0"},
         "ear": {"c": "#b07a40", "back": "#2a2018"}, "leg": {"c": "#c8925a", "noise": 4}, "tail": {"c": "#f2ece0"}}
VARIANTS = {
    "elk": {"elk": dict(ELK, antler=CLEAR),
            "elk_bull": dict(ELK, mane={"c": "#3a2616", "noise": 10}, antler={"c": "#c8b088", "noise": 12, "tip": {"c": "#efe6d0", "frac": 0.25}})},
    "moose": {"moose": dict(MOOSE, antler=CLEAR),
              "moose_bull": dict(MOOSE, antler={"c": "#d0bc96", "noise": 12, "speckle": {"c": "#b8a27a", "p": 0.2}})},
    "pronghorn": {"pronghorn": dict(PRONG, snout={"c": "#c48a4c", "front": "#3a2c20"}, horn=CLEAR),
                  "pronghorn_buck": dict(PRONG, snout={"c": "#2a2018", "noise": 4}, horn={"c": "#1a1816", "noise": 4})},
    "grizzly_bear": {"grizzly_bear": {
        "body": {"c": "#7a5634", "noise": 8, "grizzle": {"c": ["#b89468", "#5a3e26"], "p": [0.25, 0.15]}},
        "head": {"c": "#8a643e", "noise": 6, "grizzle": {"c": ["#c0a070"], "p": [0.2]}, "eyes": "#080504", "eye_white": "#ece8de", "eye_row": 3},
        "snout": {"c": "#9a7a54", "noise": 6, "nose": "#141010"}, "ear": {"c": "#5a3e26"}, "leg": {"c": "#4a3220", "noise": 8},
        "tail": {"c": "#6a4a2c"}}},
    "bobcat": {"bobcat": {
        "body": {"c": "#b48c62", "noise": 8, "speckle": {"c": "#3e2c1c", "p": 0.22}, "belly": "#efe6d8"},
        "head": {"c": "#b48c62", "noise": 6, "front_map": ["cDcDc", "cEcEc", "wwnww"],
                 "front_colors": {"c": "#b48c62", "D": "#3e2c1c", "E": "#c8c040", "w": "#efe6d8", "n": "#6a4a40"}},
        "snout": {"c": "#efe6d8", "noise": 4, "nose": "#7a4a44"}, "ear": {"c": "#6a5038", "back": "#1a1410"},
        "tuft": {"c": "#1a1410"}, "ruff": {"c": "#e8dcc8", "speckle": {"c": "#2a2018", "p": 0.3}},
        "leg": {"c": "#b48c62", "speckle": {"c": "#3e2c1c", "p": 0.15}}, "tail": {"c": "#b48c62", "tip": {"c": "#1a1410", "frac": 0.35}}}},
}
MODELS = {"elk": elk, "moose": moose, "pronghorn": pronghorn, "grizzly_bear": grizzly, "bobcat": bobcat}
EGGS = {"elk": ("#b08454", "#4e3420"), "moose": ("#2e2018", "#9a8a74"), "pronghorn": ("#c48a4c", "#f2ece0"),
        "grizzly_bear": ("#7a5634", "#c0a070"), "bobcat": ("#b48c62", "#3e2c1c")}

if __name__ == "__main__":
    for name, parts in MODELS.items():
        size = pack(parts)
        emit_model(name, parts, size)
        emit_textures(parts, size, VARIANTS[name])
        print(name, size)
    for i, (mob, (b, s)) in enumerate(EGGS.items()):
        save_item(f"{mob}_spawn_egg", egg_icon(b, s, 2000 + i))
