"""Shelf mushrooms (flora/ShelfMushrooms.java): turkey tail (banded browns, cream rim) and sulphur shelf (orange, yellow
rim). Each has top (bands round the bark), underside (pale pores) and rim textures, a hand-drawn item, block models
for one to three shelves, a blockstate, loot that drops one per shelf, stew recipe and the worldgen feature.
  python3 tools/shelf_mushrooms.py      (tools/resources.py merges the lang)"""
import json, math, os, random
from PIL import Image
import moon_assets as M

B = os.path.join(M.TEX, "block"); I = os.path.join(M.TEX, "item")
KINDS = {
    "turkey_tail":   {"title": "Turkey Tail", "bands": [(78, 50, 32), (128, 92, 60), (96, 84, 76), (164, 128, 88), (70, 56, 48), (150, 120, 90)],
                      "rim": (226, 212, 180), "under": [(214, 202, 170), (190, 176, 142)], "edge": (52, 36, 26)},
    "sulphur_shelf": {"title": "Sulphur Shelf", "bands": [(206, 96, 28), (226, 122, 34), (238, 146, 42), (232, 132, 36)],
                      "rim": (250, 214, 74), "under": [(246, 222, 112), (226, 196, 86)], "edge": (150, 66, 20)},
}
# shelves: (x1, x2, depth, y) — the bark is at z = 16; each shelf is a main slab and a narrower front lip
SHELVES = [(2, 14, 6, 7), (4, 12, 5, 11.5), (3, 10, 4, 3)]

def top(k):
    """Bands in rings round the bark (texture bottom edge = the bark), the rim at the outside."""
    K = KINDS[k]; im = Image.new("RGBA", (16, 16)); px = im.load(); r = random.Random(len(k))
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - 7.5) * 0.9, 16 - y)
            if d > 13.5: c = K["rim"]
            else:
                c = K["bands"][int(d / 1.6) % len(K["bands"])]
                c = M.shade_px(c + (255,), 0.94 + 0.12 * r.random())[:3]
            px[x, y] = c + (255,)
    return im

def under(k):
    K = KINDS[k]; im = Image.new("RGBA", (16, 16), K["under"][0] + (255,)); px = im.load(); r = random.Random(len(k) + 9)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 5 + r.randrange(3)) % 4 == 0: px[x, y] = K["under"][1] + (255,)     # pores
    return im

def rim(k):
    """The shelf's thin edge: rim colour over a darker underside line."""
    K = KINDS[k]; im = Image.new("RGBA", (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16): px[x, y] = (K["rim"] if y % 2 == 0 else K["bands"][0]) + (255,)
    return im

ICON = [
    "................",
    "................",
    "...........eebb.",
    ".........ecrbbbb",
    "........ecrabbbb",
    ".......ecrraabbb",
    "......ecrraaabbb",
    "......errraaabbb",
    "....eecrraaaabbb",
    "...ecrrraaaaabbb",
    "..ecrraaaaaabbbb",
    "..errraaaaaabbbb",
    "..eeuuuuuuuubbbb",
    "....eeeeeeeebbbb",
    "............bbbb",
    "................"]

def icon(k):
    """A shelf on a stub of bark (b), seen from the side: rim (c), bands (r, a), underside (u), edge (e)."""
    K = KINDS[k]; pal = {"e": K["edge"], "c": K["rim"], "r": K["bands"][1], "a": K["bands"][0], "u": K["under"][1], "b": (98, 76, 50)}
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(ICON):
        for x, ch in enumerate(row):
            if ch in pal: px[x, y] = pal[ch] + (255,)
    for y in range(2, 15):                                          # bark texture on the stub
        for x in range(12, 16):
            if px[x, y][3] and (x + y * 3) % 5 == 0: px[x, y] = (70, 54, 36, 255)
    return im

def model(k, n):
    els = []
    for x1, x2, dep, y in SHELVES[:n]:
        z1 = 16 - dep; faces = lambda: {"up": {"texture": "#top"}, "down": {"texture": "#under"}, "north": {"texture": "#rim"},
                                         "east": {"texture": "#rim"}, "west": {"texture": "#rim"}}
        els.append({"from": [x1, y, z1], "to": [x2, y + 2, 16], "faces": faces()})
        w = (x2 - x1); els.append({"from": [x1 + w * 0.2, y, z1 - 1.5], "to": [x2 - w * 0.2, y + 2, z1], "faces": faces()})
    return {"parent": "minecraft:block/block", "ambientocclusion": False,
            "textures": {"particle": f"ryancraft:block/{k}_top", "top": f"ryancraft:block/{k}_top", "under": f"ryancraft:block/{k}_under", "rim": f"ryancraft:block/{k}_rim"},
            "elements": els}

def lang_entries(): return {f"block.ryancraft.{k}": v["title"] for k, v in KINDS.items()}

# a shelf goes into stew with another shelf or with any vanilla mushroom
ANY_MUSHROOM = ["ryancraft:turkey_tail", "ryancraft:sulphur_shelf", "minecraft:brown_mushroom", "minecraft:red_mushroom"]

def build():
    W, D = M.w, "data/ryancraft/"
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    for k in KINDS:
        top(k).save(os.path.join(B, f"{k}_top.png")); under(k).save(os.path.join(B, f"{k}_under.png")); rim(k).save(os.path.join(B, f"{k}_rim.png"))
        icon(k).save(os.path.join(I, f"{k}.png"))
        for n in (1, 2, 3): W(f"assets/ryancraft/models/block/{k}_{n}.json", model(k, n))
        W(f"assets/ryancraft/blockstates/{k}.json", {"variants": {f"facing={f},shelves={n}": ({"model": f"ryancraft:block/{k}_{n}", "y": r} if r else {"model": f"ryancraft:block/{k}_{n}"})
                                                                  for f, r in rot.items() for n in (1, 2, 3)}})
        W(f"assets/ryancraft/models/item/{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{k}"}})
        W(f"assets/ryancraft/items/{k}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{k}"}})
        W(f"{D}loot_table/blocks/{k}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{k}",
            "functions": [{"function": "minecraft:set_count", "count": n, "add": False, "conditions": [{"condition": "minecraft:block_state_property",
            "block": f"ryancraft:{k}", "properties": {"shelves": str(n)}}]} for n in (2, 3)] + [{"function": "minecraft:explosion_decay"}]}]}]})
        W(f"{D}recipe/mushroom_stew_from_{k}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "group": "mushroom_stew",
            "ingredients": [f"ryancraft:{k}", ANY_MUSHROOM, "minecraft:bowl"], "result": {"id": "minecraft:mushroom_stew", "count": 1}})
    W(f"{D}worldgen/configured_feature/shelf_mushrooms.json", {"type": "ryancraft:shelf_mushrooms", "config": {}})
    W(f"{D}worldgen/placed_feature/shelf_mushrooms.json", {"feature": "ryancraft:shelf_mushrooms", "placement": [
        {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})   # biome checked at the surface, not the world's floor
    M.add_tag("data/minecraft/tags/block/mineable/axe.json", [f"ryancraft:{k}" for k in KINDS])

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("shelf mushrooms ok")
