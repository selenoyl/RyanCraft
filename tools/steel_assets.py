"""Steel: ingot and block textures (the classic iron ones, darkened to a cold blue-grey), models, recipes, loot, tags.
tools/resources.py merges the lang."""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures")

def lang_entries(): return {"item.ryancraft.steel_ingot": "Steel Ingot", "block.ryancraft.steel_block": "Block of Steel"}

def steel(src):
    im = Image.open(src).convert("RGBA"); px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            l = (r + g + b) / 3
            if max(r, g, b) - min(r, g, b) > 40 and r > b: px[x, y] = (r, g, b, a); continue   # keep any non-metal pixels
            v = int(l * 0.78)
            px[x, y] = (max(0, v - 6), max(0, v - 1), min(255, v + 10), a)
    return im

def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

if __name__ == "__main__":
    steel(os.path.join(CLASSIC, "item", "iron_ingot.png")).save(os.path.join(A, "textures", "item", "steel_ingot.png"))
    steel(os.path.join(CLASSIC, "block", "iron_block.png")).save(os.path.join(A, "textures", "block", "steel_block.png"))
    w("assets/ryancraft/items/steel_ingot.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/steel_ingot"}})
    w("assets/ryancraft/models/item/steel_ingot.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/steel_ingot"}})
    w("assets/ryancraft/blockstates/steel_block.json", {"variants": {"": {"model": "ryancraft:block/steel_block"}}})
    w("assets/ryancraft/models/block/steel_block.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "ryancraft:block/steel_block"}})
    w("assets/ryancraft/items/steel_block.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/steel_block"}})
    w("data/ryancraft/loot_table/blocks/steel_block.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:steel_block"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w("data/ryancraft/recipe/steel_block.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["###", "###", "###"],
        "key": {"#": "ryancraft:steel_ingot"}, "result": {"id": "ryancraft:steel_block", "count": 1}})
    w("data/ryancraft/recipe/steel_ingot_from_block.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["ryancraft:steel_block"], "result": {"id": "ryancraft:steel_ingot", "count": 9}})
    add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:steel_block"])
    add_tag("data/minecraft/tags/block/needs_iron_tool.json", ["ryancraft:steel_block"])
    add_tag("data/minecraft/tags/block/beacon_base_blocks.json", ["ryancraft:steel_block"])
    add_tag("data/minecraft/tags/item/beacon_payment_items.json", ["ryancraft:steel_ingot"])
    print("steel ok")
