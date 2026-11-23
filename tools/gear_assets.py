"""Tools and armour for a material (metal/GearSet.java): the classic 1.2.5 iron tools and armour recoloured to the
material's palette (handles keep their wood), item models, the armour's equipment asset, recipes, tags and lang.
  python3 tools/gear_assets.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures")

# material: (title, ingredient item, palette dark -> light)
SETS = {
    "zephyrite": ("Zephyrite", "ryancraft:zephyrite", [(28, 84, 100), (64, 150, 168), (120, 212, 224), (200, 248, 250)]),
}
TOOLS = ("sword", "pickaxe", "axe", "shovel", "hoe"); ARMOUR = ("helmet", "chestplate", "leggings", "boots")
SHAPES = {"sword": [" # ", " # ", " S "], "pickaxe": ["###", " S ", " S "], "axe": ["##", "#S", " S"], "shovel": ["#", "S", "S"], "hoe": ["##", " S", " S"],
          "helmet": ["###", "# #"], "chestplate": ["# #", "###", "###"], "leggings": ["###", "# #", "# #"], "boots": ["# #", "# #"]}
ITEM_TAGS = {"sword": "swords", "pickaxe": "pickaxes", "axe": "axes", "shovel": "shovels", "hoe": "hoes",
             "helmet": "head_armor", "chestplate": "chest_armor", "leggings": "leg_armor", "boots": "foot_armor"}

def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

def is_metal(c):
    r, g, b = c[:3]
    return max(r, g, b) - min(r, g, b) < 28                # greys are the iron; browns (handles) are left alone

def recolour(src, pal, keep_wood=True):
    im = Image.open(src).convert("RGBA"); px = im.load()
    vals = [sum(c[:3]) / 3 for c in im.get_flattened_data() if c[3] and (is_metal(c) or not keep_wood)]
    lo, hi = (min(vals), max(vals)) if vals else (0, 255)
    for y in range(im.height):
        for x in range(im.width):
            c = px[x, y]
            if not c[3] or (keep_wood and not is_metal(c)): continue
            t = (sum(c[:3]) / 3 - lo) / max(1, hi - lo); i = min(len(pal) - 2, int(t * (len(pal) - 1))); f = t * (len(pal) - 1) - i
            px[x, y] = tuple(int(pal[i][k] + (pal[i + 1][k] - pal[i][k]) * f) for k in range(3)) + (c[3],)
    return im

def lang_entries():
    out = {}
    for m, (title, _, _) in SETS.items():
        for k in TOOLS + ARMOUR: out[f"item.ryancraft.{m}_{k}"] = f"{title} {k.title()}"
    return out

def build(m):
    title, ing, pal = SETS[m]
    for k in TOOLS + ARMOUR:
        recolour(os.path.join(CLASSIC, "item", f"iron_{k}.png"), pal).save(os.path.join(A, "textures", "item", f"{m}_{k}.png"))
        w(f"assets/ryancraft/models/item/{m}_{k}.json", {"parent": "minecraft:item/handheld" if k in TOOLS else "minecraft:item/generated",
            "textures": {"layer0": f"ryancraft:item/{m}_{k}"}})
        w(f"assets/ryancraft/items/{m}_{k}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{m}_{k}"}})
        key = {"#": ing}
        if "S" in "".join(SHAPES[k]): key["S"] = "minecraft:stick"
        w(f"data/ryancraft/recipe/{m}_{k}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": SHAPES[k],
            "key": key, "result": {"id": f"ryancraft:{m}_{k}", "count": 1}})
        add_tag(f"data/minecraft/tags/item/{ITEM_TAGS[k]}.json", [f"ryancraft:{m}_{k}"])
        if k in ARMOUR: add_tag("data/minecraft/tags/item/trimmable_armor.json", [f"ryancraft:{m}_{k}"])
    for layer in ("humanoid", "humanoid_leggings"):
        out = os.path.join(A, "textures", "entity", "equipment", layer, f"{m}.png"); os.makedirs(os.path.dirname(out), exist_ok=True)
        recolour(os.path.join(CLASSIC, "entity", "equipment", layer, "iron.png"), pal, keep_wood=False).save(out)
    w(f"assets/ryancraft/equipment/{m}.json", {"layers": {"humanoid": [{"texture": f"ryancraft:{m}"}], "humanoid_leggings": [{"texture": f"ryancraft:{m}"}]}})
    w(f"data/ryancraft/tags/item/{m}_tool_materials.json", {"values": [ing]})

if __name__ == "__main__":
    for m in SETS: build(m)
    p = os.path.join(A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("gear ok")
