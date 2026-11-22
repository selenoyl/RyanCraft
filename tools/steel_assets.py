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


# ---------------------------------------------------------------- decoration blocks
TPL = os.path.join(HERE, "templates", "steel")
RENAME = {"iron_bars": "steel_bars", "iron_door": "steel_door", "iron_trapdoor": "steel_trapdoor", "cut_copper": "cut_steel"}
DECOR_LANG = {"cut_steel": "Cut Steel", "cut_steel_stairs": "Cut Steel Stairs", "cut_steel_slab": "Cut Steel Slab", "steel_plating": "Steel Plating",
              "chiseled_steel": "Chiseled Steel", "steel_grate": "Steel Grate", "steel_bars": "Steel Bars", "steel_door": "Steel Door",
              "steel_trapdoor": "Steel Trapdoor"}

def rn(name):
    for a, b in RENAME.items():
        if name.startswith(a): return b + name[len(a):]
    return name

def to_steel(src, target_mean=None):
    """Any metal texture (copper, iron) recoloured to steel, matched to the Block of Steel's brightness."""
    im = Image.open(src).convert("RGBA"); a = [px for px in im.get_flattened_data() if px[3]]
    mean = sum((p[0] + p[1] + p[2]) / 3 for p in a) / max(1, len(a))
    k = (target_mean / mean) if target_mean else 0.78
    out = Image.new("RGBA", im.size); po = out.load(); pi = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, al = pi[x, y]
            if not al: continue
            v = int((r + g + b) / 3 * k)
            po[x, y] = (max(0, min(255, v - 6)), max(0, min(255, v - 1)), max(0, min(255, v + 10)), al)
    return out

def plating(base):
    """Riveted steel panels: the steel block with a dark seam and a rivet in each corner."""
    im = base.copy(); px = im.load()
    for i in range(16):
        for (x, y) in ((i, 0), (0, i)): px[x, y] = tuple(max(0, c - 55) for c in px[x, y][:3]) + (255,)
        for (x, y) in ((i, 15), (15, i)): px[x, y] = tuple(max(0, c - 25) for c in px[x, y][:3]) + (255,)
    for x in (2, 13):
        for y in (2, 13):
            px[x, y] = (198, 204, 214, 255); px[x + (1 if x < 8 else -1), y + (1 if y < 8 else -1)] = (70, 74, 84, 255)
    return im

def decor():
    tex = os.path.join(A, "textures", "block")
    steel_block = Image.open(os.path.join(tex, "steel_block.png")).convert("RGBA")
    target = sum((p[0] + p[1] + p[2]) / 3 for p in steel_block.get_flattened_data()) / 256
    to_steel(os.path.join(TPL, "textures", "cut_copper.png"), target).save(os.path.join(tex, "cut_steel.png"))
    to_steel(os.path.join(TPL, "textures", "chiseled_copper.png"), target).save(os.path.join(tex, "chiseled_steel.png"))
    to_steel(os.path.join(TPL, "textures", "copper_grate.png"), target).save(os.path.join(tex, "steel_grate.png"))
    to_steel(os.path.join(TPL, "textures", "iron_trapdoor.png")).save(os.path.join(tex, "steel_trapdoor.png"))
    for n in ("iron_bars", "iron_door_top", "iron_door_bottom"):
        to_steel(os.path.join(CLASSIC, "block", n + ".png")).save(os.path.join(tex, rn(n) + ".png"))
    to_steel(os.path.join(CLASSIC, "item", "iron_door.png")).save(os.path.join(A, "textures", "item", "steel_door.png"))
    plating(steel_block).save(os.path.join(tex, "steel_plating.png"))
    # blockstates and models, from the vanilla iron / cut copper ones
    def fix(o):
        if isinstance(o, dict): return {k: fix(v) for k, v in o.items()}
        if isinstance(o, list): return [fix(v) for v in o]
        if isinstance(o, str) and o.startswith("minecraft:block/") and any(o[16:].startswith(k) for k in RENAME):
            return "ryancraft:block/" + rn(o[16:])
        return o
    for f in os.listdir(os.path.join(TPL, "blockstates")):
        w(f"assets/ryancraft/blockstates/{rn(f[:-5])}.json", fix(json.load(open(os.path.join(TPL, "blockstates", f)))))
    for f in os.listdir(os.path.join(TPL, "models")):
        w(f"assets/ryancraft/models/block/{rn(f[:-5])}.json", fix(json.load(open(os.path.join(TPL, "models", f)))))
    w("assets/ryancraft/blockstates/cut_steel.json", {"variants": {"": {"model": "ryancraft:block/cut_steel"}}})
    for n in ("steel_plating", "chiseled_steel", "steel_grate"):
        w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
        w(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
    items = {"cut_steel": "cut_steel", "cut_steel_stairs": "cut_steel_stairs", "cut_steel_slab": "cut_steel_slab", "steel_plating": "steel_plating",
             "chiseled_steel": "chiseled_steel", "steel_grate": "steel_grate", "steel_trapdoor": "steel_trapdoor_bottom"}
    for item, model in items.items():
        w(f"assets/ryancraft/items/{item}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{model}"}})
    for item in ("steel_bars", "steel_door"):
        tx = "ryancraft:block/steel_bars" if item == "steel_bars" else "ryancraft:item/steel_door"
        w(f"assets/ryancraft/models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tx}})
        w(f"assets/ryancraft/items/{item}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{item}"}})
    # loot
    for f in os.listdir(os.path.join(TPL, "loot")):
        txt = open(os.path.join(TPL, "loot", f)).read()
        for a, b in RENAME.items(): txt = txt.replace("minecraft:" + a, "ryancraft:" + b).replace("blocks/" + a, "blocks/" + b)
        w(f"data/ryancraft/loot_table/blocks/{rn(f[:-5])}.json", json.loads(txt))
    for n in ("cut_steel", "cut_steel_stairs", "steel_plating", "chiseled_steel", "steel_grate"):
        w(f"data/ryancraft/loot_table/blocks/{n}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    # recipes: crafting, and the stonecutter from a Block of Steel (or cut steel)
    shaped = lambda name, pat, key, n: w(f"data/ryancraft/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "building",
        "pattern": pat, "key": key, "result": {"id": f"ryancraft:{name}", "count": n}})
    shaped("cut_steel", ["##", "##"], {"#": "ryancraft:steel_block"}, 4)
    shaped("cut_steel_stairs", ["#  ", "## ", "###"], {"#": "ryancraft:cut_steel"}, 4)
    shaped("cut_steel_slab", ["###"], {"#": "ryancraft:cut_steel"}, 6)
    shaped("chiseled_steel", ["#", "#"], {"#": "ryancraft:cut_steel_slab"}, 1)
    shaped("steel_plating", ["##", "##"], {"#": "ryancraft:cut_steel"}, 4)
    shaped("steel_grate", [" # ", "# #", " # "], {"#": "ryancraft:steel_block"}, 4)
    shaped("steel_bars", ["###", "###"], {"#": "ryancraft:steel_ingot"}, 16)
    shaped("steel_door", ["##", "##", "##"], {"#": "ryancraft:steel_ingot"}, 3)
    shaped("steel_trapdoor", ["##", "##"], {"#": "ryancraft:steel_ingot"}, 1)
    def cut(name, src, n):
        w(f"data/ryancraft/recipe/{name}_stonecutting{'_from_' + src if src != 'steel_block' else ''}.json", {"type": "minecraft:stonecutting",
            "ingredient": f"ryancraft:{src}", "result": {"id": f"ryancraft:{name}", "count": n}})
    for name, n in (("cut_steel", 4), ("cut_steel_stairs", 4), ("cut_steel_slab", 8), ("chiseled_steel", 4), ("steel_plating", 4), ("steel_grate", 4)):
        cut(name, "steel_block", n)
    for name, n in (("cut_steel_stairs", 1), ("cut_steel_slab", 2), ("chiseled_steel", 1), ("steel_plating", 1)):
        cut(name, "cut_steel", n)
    names = ["ryancraft:" + n for n in DECOR_LANG]
    add_tag("data/minecraft/tags/block/mineable/pickaxe.json", names)
    add_tag("data/minecraft/tags/block/needs_stone_tool.json", names)
    add_tag("data/minecraft/tags/block/stairs.json", ["ryancraft:cut_steel_stairs"]); add_tag("data/minecraft/tags/item/stairs.json", ["ryancraft:cut_steel_stairs"])
    add_tag("data/minecraft/tags/block/slabs.json", ["ryancraft:cut_steel_slab"]); add_tag("data/minecraft/tags/item/slabs.json", ["ryancraft:cut_steel_slab"])
    add_tag("data/minecraft/tags/block/doors.json", ["ryancraft:steel_door"]); add_tag("data/minecraft/tags/item/doors.json", ["ryancraft:steel_door"])
    add_tag("data/minecraft/tags/block/trapdoors.json", ["ryancraft:steel_trapdoor"]); add_tag("data/minecraft/tags/item/trapdoors.json", ["ryancraft:steel_trapdoor"])
    print("steel decor ok")

_lang = lang_entries
def lang_entries():
    out = _lang(); out.update({f"block.ryancraft.{k}": v for k, v in DECOR_LANG.items()}); return out

if __name__ == "__main__":
    decor()
