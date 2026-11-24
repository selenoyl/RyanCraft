"""
Clay pots, placed bowls and placed buckets: textures, block models for every state, item models, recipes, loot, lang.
  python3 tools/storage_assets.py   (lang is merged by tools/resources.py too)
"""
import json, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources")
TEX = os.path.join(ROOT, "assets/ryancraft/textures")
SPOTS = [[(5, 5)], [(1, 5), (9, 5)], [(1, 2), (9, 2), (5, 9)], [(1, 1), (9, 1), (1, 9), (9, 9)]]

def w(path, obj):
    p = os.path.join(ROOT, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2); f.write("\n")

def jit(r, c, a): d = r.randint(-a, a); return tuple(max(0, min(255, v + d)) for v in c)

def solid(seed, base, noise=8, band=None, rows=None):
    r = random.Random(seed); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        for y in range(16):
            c = base
            if band and rows and y in rows: c = band
            px[x, y] = jit(r, c, noise) + (255,)
    return img

def icon(rows, pal, seed):
    r = random.Random(seed); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".": px[x, y] = jit(r, pal[ch], 5) + (255,)
    return img

POT_ICON = [
    "................",
    "................",
    "......LLLL......",
    ".....LllllL.....",
    "......dmmd......",
    ".....dmmmmd.....",
    "....dmhmmmmd....",
    "...dmhmmmmmmd...",
    "...dBBBBBBBBd...",
    "...dmhmmmmmmd...",
    "...dmmmmmmmmd...",
    "....dmmmmmmd....",
    ".....dmmmmd.....",
    "......dddd......",
    "................",
    "................"]

def el(frm, to, faces):
    return {"from": frm, "to": to, "faces": {f: dict(t) for f, t in faces.items()}}

def box_faces(tex, w_, h_, top=None, uvx=0, uvy=0):
    side = {"texture": tex, "uv": [uvx, uvy, uvx + w_, uvy + h_]}
    f = {k: side for k in ("north", "south", "east", "west")}
    f["up"] = {"texture": top or tex, "uv": [0, 0, w_, w_]}; f["down"] = {"texture": tex, "uv": [0, 0, w_, w_]}
    return f

def pot_model(n):
    els = []
    for (x, z) in SPOTS[n - 1]:
        els.append(el([x, 0, z], [x + 6, 5, z + 6], box_faces("#pot", 6, 5, uvx=5, uvy=6)))
        els.append(el([x + 1, 5, z + 1], [x + 5, 6, z + 5], box_faces("#pot", 4, 1, uvx=6, uvy=3)))
        els.append(el([x + 0.5, 6, z + 0.5], [x + 5.5, 6.5, z + 5.5], box_faces("#lid", 5, 0.5, top="#lid")))
        els.append(el([x + 2.5, 6.5, z + 2.5], [x + 3.5, 7, z + 3.5], box_faces("#lid", 1, 0.5)))
    for i, (x, z) in enumerate(SPOTS[n - 1]):      # the band round each pot, tinted by its own colour (tintindex = which pot)
        band = {"texture": "#band", "uv": [0, 0, 6, 2], "tintindex": i}
        els.append(el([x - 0.02, 1, z - 0.02], [x + 6.02, 3, z + 6.02], {k: dict(band) for k in ("north", "south", "east", "west")}))
    return {"parent": "minecraft:block/block", "textures": {"particle": "ryancraft:block/clay_pot", "pot": "ryancraft:block/clay_pot",
            "lid": "ryancraft:block/clay_pot_lid", "band": "ryancraft:block/clay_pot_band"}, "elements": els}

def bowl_model(contents):
    s = {"texture": "#wood"}
    els = [el([5, 0, 5], [11, 1, 11], {k: s for k in ("north", "south", "east", "west", "up", "down")}),
           el([4, 1, 4], [12, 3, 5], {k: s for k in ("north", "south", "east", "west", "up", "down")}),
           el([4, 1, 11], [12, 3, 12], {k: s for k in ("north", "south", "east", "west", "up", "down")}),
           el([4, 1, 5], [5, 3, 11], {k: s for k in ("north", "south", "east", "west", "up", "down")}),
           el([11, 1, 5], [12, 3, 11], {k: s for k in ("north", "south", "east", "west", "up", "down")})]
    tex = {"particle": "ryancraft:block/bowl_wood", "wood": "ryancraft:block/bowl_wood"}
    if contents != "empty":
        els.append(el([5, 2.5, 5], [11, 2.5, 11], {"up": {"texture": "#food", "uv": [5, 5, 11, 11]}}))
        tex["food"] = f"ryancraft:block/bowl_{contents}"
    return {"parent": "minecraft:block/block", "textures": tex, "elements": els}

def bucket_model(contents):
    s = {"texture": "#metal"}
    six = lambda: {k: s for k in ("north", "south", "east", "west", "up", "down")}
    els = [el([5, 0, 5], [11, 1, 11], six()),
           el([5, 1, 5], [11, 7, 6], six()), el([5, 1, 10], [11, 7, 11], six()),
           el([5, 1, 6], [6, 7, 10], six()), el([10, 1, 6], [11, 7, 10], six()),
           el([4.5, 6, 7.5], [5, 9.5, 8.5], six()), el([11, 6, 7.5], [11.5, 9.5, 8.5], six()),
           el([4.5, 9.5, 7.5], [11.5, 10, 8.5], six())]
    tex = {"particle": "ryancraft:block/bucket_metal", "metal": "ryancraft:block/bucket_metal"}
    fluid = {"water": "minecraft:block/water_still", "lava": "minecraft:block/lava_still", "milk": "ryancraft:block/bucket_milk",
             "powder_snow": "minecraft:block/powder_snow"}.get(contents)
    if fluid:
        face = {"texture": "#fluid", "uv": [6, 6, 10, 10]}
        if contents == "water": face["tintindex"] = 0
        els.append(el([6, 6, 6], [10, 6, 10], {"up": face}))
        tex["fluid"] = fluid
    return {"parent": "minecraft:block/block", "ambientocclusion": False, "textures": tex, "elements": els}

def lang_entries():
    return {"block.ryancraft.clay_pot": "Clay Pot", "item.ryancraft.unfired_clay_pot": "Unfired Clay Pot",
            "container.ryancraft.clay_pot": "Clay Pot", "block.ryancraft.placed_bowl": "Bowl", "block.ryancraft.placed_bucket": "Bucket"}

if __name__ == "__main__":
    # textures
    solid(1, (178, 96, 58), 8, (120, 60, 34), (8, 9)).save(f"{TEX}/block/clay_pot.png")
    solid(2, (160, 84, 50), 6).save(f"{TEX}/block/clay_pot_lid.png")
    solid(3, (150, 104, 64), 6, (124, 84, 50), (3, 7, 11)).save(f"{TEX}/block/bowl_wood.png")
    solid(4, (150, 152, 158), 7, (122, 124, 130), (0, 15)).save(f"{TEX}/block/bucket_metal.png")
    solid(5, (244, 242, 236), 4).save(f"{TEX}/block/bucket_milk.png")
    for name, base, bits in (("mushroom_stew", (150, 100, 62), [(196, 160, 120), (110, 70, 44)]),
                             ("beetroot_soup", (150, 30, 40), [(190, 60, 70), (110, 20, 30)]),
                             ("rabbit_stew", (170, 110, 56), [(220, 160, 60), (110, 150, 60), (120, 80, 40)])):
        r = random.Random(sum(map(ord, name))); img = solid(6, base, 6); px = img.load()   # (a stable seed; hash() changes per run)
        for _ in range(14): px[r.randrange(16), r.randrange(16)] = r.choice(bits) + (255,)
        img.save(f"{TEX}/block/bowl_{name}.png")
    icon(POT_ICON, {"L": (150, 80, 46), "l": (186, 104, 64), "d": (110, 56, 32), "m": (178, 96, 58), "h": (214, 136, 90), "B": (120, 60, 34)}, 7).save(f"{TEX}/item/clay_pot.png")
    # dyeable band: a pale grey band (tinted in game: the natural dark band undyed, the dye's colour once dyed)
    solid(9, (236, 236, 236), 10).save(f"{TEX}/block/clay_pot_band.png")
    band = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); bp = band.load(); r = random.Random(10)
    for y, row in enumerate(POT_ICON):
        for x, ch in enumerate(row):
            if ch == "B": bp[x, y] = jit(r, (236, 236, 236), 8) + (255,)
    band.save(f"{TEX}/item/clay_pot_band.png")
    w("assets/ryancraft/models/item/clay_pot.json", {"parent": "minecraft:item/generated",
        "textures": {"layer0": "ryancraft:item/clay_pot", "layer1": "ryancraft:item/clay_pot_band"}})
    w("assets/ryancraft/items/clay_pot.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/clay_pot",
        "tints": [{"type": "minecraft:constant", "value": -1}, {"type": "minecraft:dye", "default": -8897502}]}})
    w("data/minecraft/tags/item/dyeable.json", {"replace": False, "values": ["ryancraft:clay_pot"]})
    icon(POT_ICON, {"L": (130, 136, 150), "l": (164, 170, 184), "d": (104, 108, 122), "m": (152, 158, 172), "h": (184, 190, 204), "B": (136, 142, 156)}, 8).save(f"{TEX}/item/unfired_clay_pot.png")
    # models and blockstates
    for n in range(1, 5): w(f"assets/ryancraft/models/block/clay_pot_{n}.json", pot_model(n))
    w("assets/ryancraft/blockstates/clay_pot.json", {"variants": {f"pots={n}": {"model": f"ryancraft:block/clay_pot_{n}"} for n in range(1, 5)}})
    bowls = ["empty", "mushroom_stew", "beetroot_soup", "rabbit_stew"]
    for c in bowls: w(f"assets/ryancraft/models/block/placed_bowl_{c}.json", bowl_model(c))
    w("assets/ryancraft/blockstates/placed_bowl.json", {"variants": {f"contents={c}": {"model": f"ryancraft:block/placed_bowl_{c}"} for c in bowls}})
    buckets = ["empty", "water", "lava", "milk", "powder_snow"]
    for c in buckets: w(f"assets/ryancraft/models/block/placed_bucket_{c}.json", bucket_model(c))
    w("assets/ryancraft/blockstates/placed_bucket.json", {"variants": {f"contents={c}": {"model": f"ryancraft:block/placed_bucket_{c}"} for c in buckets}})
    for it in ("unfired_clay_pot",):       # (the clay pot's own item model, with its dyeable band, is written above)
        w(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{it}"}})
        w(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{it}"}})
    # loot: a placed bowl or bucket drops itself with its contents
    for blk, prop, items in (("placed_bowl", "contents", {"empty": "bowl", "mushroom_stew": "mushroom_stew", "beetroot_soup": "beetroot_soup", "rabbit_stew": "rabbit_stew"}),
                             ("placed_bucket", "contents", {"empty": "bucket", "water": "water_bucket", "lava": "lava_bucket", "milk": "milk_bucket", "powder_snow": "powder_snow_bucket"})):
        w(f"data/ryancraft/loot_table/blocks/{blk}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:item", "name": f"minecraft:{item}", "conditions": [{"condition": "minecraft:block_state_property",
             "block": f"ryancraft:{blk}", "properties": {prop: state}}]} for state, item in items.items()]}], "random_sequence": f"ryancraft:blocks/{blk}"})
    # recipes: shape the pot from clay, then fire it
    w("data/ryancraft/recipe/unfired_clay_pot.json", {"type": "minecraft:crafting_shaped", "category": "misc",
      "pattern": ["C C", "CCC"], "key": {"C": "minecraft:clay_ball"}, "result": {"count": 1, "id": "ryancraft:unfired_clay_pot"}})
    w("data/ryancraft/recipe/clay_pot.json", {"type": "minecraft:smelting", "category": "blocks", "ingredient": "ryancraft:unfired_clay_pot",
      "result": {"id": "ryancraft:clay_pot"}, "experience": 0.35, "cookingtime": 200})
    print("ok")
