"""Concrete stairs and slabs for all sixteen colours (deco/ConcreteShapes.java), on vanilla's concrete textures.
Blockstates and models are vanilla's stone-brick ones renamed. Recipes: crafting and stonecutting from concrete.
  python3 tools/concrete_shapes.py      (tools/resources.py merges the lang)"""
import json, os
import moon_assets as M

COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
           "brown", "green", "red", "black"]

def ren(o, kind, dst, c):
    s = json.dumps(o).replace(f"minecraft:block/stone_brick_{kind}", f"ryancraft:block/{dst}").replace('"minecraft:block/stone_bricks"', f'"minecraft:block/{c}_concrete"')
    return json.loads(s)

def lang_entries():
    out = {}
    for c in COLOURS:
        t = " ".join(p.title() for p in c.split("_"))
        out[f"block.ryancraft.{c}_concrete_stairs"] = f"{t} Concrete Stairs"; out[f"block.ryancraft.{c}_concrete_slab"] = f"{t} Concrete Slab"
    return out

def build():
    R, L = "data/ryancraft/recipe/", "data/ryancraft/loot_table/blocks/"
    all_stairs, all_slabs = [], []
    for c in COLOURS:
        base = f"minecraft:{c}_concrete"
        for kind in ("stairs", "slab"):
            dst = f"{c}_concrete_{kind}"
            M.w(f"assets/ryancraft/blockstates/{dst}.json", ren(json.load(open(os.path.join(M.TPL, "blockstates", f"stone_brick_{kind}.json"))), kind, dst, c))
            for f in os.listdir(os.path.join(M.TPL, "models", "block")):
                if f.startswith(f"stone_brick_{kind}"):
                    M.w(f"assets/ryancraft/models/block/{dst}{f[len('stone_brick_' + kind):]}", ren(json.load(open(os.path.join(M.TPL, "models", "block", f))), kind, dst, c))
            M.w(f"assets/ryancraft/items/{dst}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{dst}"}})
        M.w(L + f"{c}_concrete_stairs.json", M.drops(f"{c}_concrete_stairs"))
        sl = f"{c}_concrete_slab"
        M.w(L + sl + ".json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{sl}", "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{"condition": "minecraft:block_state_property", "block": f"ryancraft:{sl}",
             "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]}]}]})
        M.w(f"{R}{c}_concrete_stairs.json", {"type": "minecraft:crafting_shaped", "category": "building", "group": "concrete_stairs",
            "pattern": ["#  ", "## ", "###"], "key": {"#": base}, "result": {"id": f"ryancraft:{c}_concrete_stairs", "count": 4}})
        M.w(f"{R}{sl}.json", {"type": "minecraft:crafting_shaped", "category": "building", "group": "concrete_slab",
            "pattern": ["###"], "key": {"#": base}, "result": {"id": f"ryancraft:{sl}", "count": 6}})
        for n, cnt in ((f"{c}_concrete_stairs", 1), (sl, 2)):
            M.w(f"{R}{n}_from_{c}_concrete_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": base, "result": {"id": f"ryancraft:{n}", "count": cnt}})
        all_stairs.append(f"ryancraft:{c}_concrete_stairs"); all_slabs.append(f"ryancraft:{sl}")
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", all_stairs + all_slabs)
    for tag, sel in (("stairs", all_stairs), ("slabs", all_slabs)):
        M.add_tag(f"data/minecraft/tags/block/{tag}.json", sel); M.add_tag(f"data/minecraft/tags/item/{tag}.json", sel)

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("concrete shapes ok")
