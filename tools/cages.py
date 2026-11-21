"""
Bird cage and chipmunk cage: block models, textures, blockstates, item models, loot, recipes and lang.
  python3 tools/cages.py   (lang is merged by tools/resources.py too)
"""
import json, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources")
TEX = os.path.join(ROOT, "assets/ryancraft/textures/block")

def w(path, obj):
    p = os.path.join(ROOT, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2); f.write("\n")

def jit(r, c, a): d = r.randint(-a, a); return tuple(max(0, min(255, v + d)) for v in c)

# ---------------- textures
def brass(seed):
    r = random.Random(seed); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        for y in range(16):
            c = (196, 150, 62) if (x + y) % 5 else (226, 186, 96)
            if y in (0, 15) or x in (0, 15): c = (150, 108, 40)
            px[x, y] = jit(r, c, 8) + (255,)
    return img

def bars():
    """Brass bars with see-through gaps and three hoops around the cage."""
    r = random.Random(7); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for x in (0, 4, 8, 12):
        for y in range(16):
            px[x, y] = jit(r, (214, 170, 78), 6) + (255,)
    for y in (0, 15):
        for x in range(16):
            px[x, y] = jit(r, (226, 186, 96) if y != 15 else (150, 108, 40), 6) + (255,)
    return img

def wire():
    """Fine grey wire mesh."""
    r = random.Random(9); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for x in range(0, 16, 2):
        for y in range(16): px[x, y] = jit(r, (150, 154, 160), 8) + (255,)
    for y in range(0, 16, 4):
        for x in range(16): px[x, y] = jit(r, (118, 122, 128), 8) + (255,)
    return img

def bedding():
    """Wood shavings."""
    r = random.Random(11); img = Image.new("RGBA", (16, 16)); px = img.load()
    for x in range(16):
        for y in range(16): px[x, y] = jit(r, (214, 186, 132), 10) + (255,)
    for _ in range(22):
        x, y = r.randrange(16), r.randrange(16); c = r.choice([(236, 212, 160), (176, 140, 90), (196, 160, 104)])
        for dx in range(r.randint(1, 3)): px[(x + dx) % 16, y] = jit(r, c, 6) + (255,)
    return img

# ---------------- models
def el(frm, to, tex, faces=("north", "south", "east", "west", "up", "down"), uv=None):
    f = {}
    for face in faces:
        d = {"texture": "#" + (tex[face] if isinstance(tex, dict) else tex)}
        if uv: d["uv"] = uv
        f[face] = d
    return {"from": frm, "to": to, "faces": f}

def bird_cage():
    e = [el([2, 0, 2], [14, 1, 14], "brass"),
         el([2, 1, 2], [14, 15.5, 2], "bars", ("north", "south"), [0, 0, 16, 16]),
         el([2, 1, 14], [14, 15.5, 14], "bars", ("north", "south"), [0, 0, 16, 16]),
         el([2, 1, 2], [2, 15.5, 14], "bars", ("east", "west"), [0, 0, 16, 16]),
         el([14, 1, 2], [14, 15.5, 14], "bars", ("east", "west"), [0, 0, 16, 16]),
         el([2, 15.5, 2], [14, 16, 14], "brass"),
         el([5, 16, 5], [11, 16.5, 11], "brass"),
         el([7.5, 16.5, 7.5], [8.5, 18.5, 8.5], "brass"),
         el([6.5, 18.5, 7.5], [9.5, 19.5, 8.5], "brass"),
         el([3, 7.5, 7.5], [13, 8.5, 8.5], "perch"),
         el([10.5, 1, 3], [12.5, 2.5, 5], "brass")]
    return {"parent": "minecraft:block/block", "ambientocclusion": False,
            "textures": {"particle": "ryancraft:block/bird_cage_brass", "brass": "ryancraft:block/bird_cage_brass",
                         "bars": "ryancraft:block/bird_cage_bars", "perch": "minecraft:block/stripped_oak_log"},
            "elements": e}

def chipmunk_cage():
    e = [el([0, 0, 0], [16, 2, 16], {"up": "bedding", "down": "wood", "north": "wood", "south": "wood", "east": "wood", "west": "wood"})]
    for (x, z) in ((0, 0), (15, 0), (0, 15), (15, 15)):
        e.append(el([x, 2, z], [x + 1, 12, z + 1], "wood"))
    e += [el([0, 12, 0], [16, 13, 1], "wood"), el([0, 12, 15], [16, 13, 16], "wood"),
          el([0, 12, 1], [1, 13, 15], "wood"), el([15, 12, 1], [16, 13, 15], "wood"),
          el([1, 2, 0.5], [15, 12, 0.5], "wire", ("north", "south"), [0, 0, 16, 16]),
          el([1, 2, 15.5], [15, 12, 15.5], "wire", ("north", "south"), [0, 0, 16, 16]),
          el([0.5, 2, 1], [0.5, 12, 15], "wire", ("east", "west"), [0, 0, 16, 16]),
          el([15.5, 2, 1], [15.5, 12, 15], "wire", ("east", "west"), [0, 0, 16, 16]),
          el([1, 12.5, 1], [15, 12.5, 15], "wire", ("up", "down"), [0, 0, 16, 16]),
          el([11, 2, 11], [14, 3, 14], "dish")]
    return {"parent": "minecraft:block/block", "ambientocclusion": False,
            "textures": {"particle": "minecraft:block/oak_planks", "wood": "minecraft:block/oak_planks",
                         "bedding": "ryancraft:block/cage_bedding", "wire": "ryancraft:block/chipmunk_cage_wire",
                         "dish": "minecraft:block/terracotta"},
            "elements": e}

def lang_entries():
    return {
        "block.ryancraft.bird_cage": "Bird Cage", "block.ryancraft.chipmunk_cage": "Chipmunk Cage",
        "tooltip.ryancraft.mob_bag": "Critters: %s / %s",
        "message.ryancraft.bag.has_items": "Empty the bundle first",
        "message.ryancraft.bag.other_kind": "This bundle already holds %s",
        "message.ryancraft.bag.full": "The bundle is full",
        "message.ryancraft.cage.occupied": "Someone already lives here",
        "message.ryancraft.cage.wrong_kind": "That one won't fit in this cage",
        "message.ryancraft.cage.shoulder_bird": "Right-click with an empty hand to take your bird back",
        "message.ryancraft.cage.sneak_hint": "Sneak and right-click to put one in",
        "message.ryancraft.cage.shoulders_full": "No room on your shoulders",
    }

if __name__ == "__main__":
    brass(3).save(f"{TEX}/bird_cage_brass.png"); bars().save(f"{TEX}/bird_cage_bars.png")
    wire().save(f"{TEX}/chipmunk_cage_wire.png"); bedding().save(f"{TEX}/cage_bedding.png")
    for name, model in (("bird_cage", bird_cage()), ("chipmunk_cage", chipmunk_cage())):
        w(f"assets/ryancraft/models/block/{name}.json", model)
        w(f"assets/ryancraft/blockstates/{name}.json", {"variants": {"": {"model": f"ryancraft:block/{name}"}}})
        w(f"assets/ryancraft/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{name}"}})
        w(f"data/ryancraft/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
          "entries": [{"type": "minecraft:item", "name": f"ryancraft:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}],
          "random_sequence": f"ryancraft:blocks/{name}"})
    w("data/ryancraft/recipe/bird_cage.json", {"type": "minecraft:crafting_shaped", "category": "decorations",
      "pattern": [" n ", "bbb", "ccc"], "key": {"n": "minecraft:iron_nugget", "b": "minecraft:iron_bars", "c": "minecraft:copper_ingot"},
      "result": {"count": 1, "id": "ryancraft:bird_cage"}})
    w("data/ryancraft/recipe/chipmunk_cage.json", {"type": "minecraft:crafting_shaped", "category": "decorations",
      "pattern": ["bbb", "b b", "ppp"], "key": {"b": "minecraft:iron_bars", "p": "#minecraft:planks"},
      "result": {"count": 1, "id": "ryancraft:chipmunk_cage"}})
    w("data/minecraft/tags/block/mineable/pickaxe.json", {"replace": False, "values": ["ryancraft:bird_cage"]})
    w("data/minecraft/tags/block/mineable/axe.json", {"replace": False, "values": ["ryancraft:chipmunk_cage"]})
    print("ok")
