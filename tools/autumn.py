"""Autumn Forest: autumn-coloured leaf blocks for the trees that turn, their tree features, and the biome itself
(golden grass, leaf litter, pumpkins). Run after tools/tree_textures.py and tools/wood.py; tools/resources.py merges the lang."""
import json, os
from tree_textures import leaves

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")
# species: (main colour, fleck colour, how many trees per chunk in the Autumn Forest)
AUTUMN = {"sugar_maple": ("#e07a1c", "#f0b830", 2), "red_maple": ("#c8281e", "#e0602a", 2), "tulip_poplar": ("#e0b830", "#c8c040", 1),
          "american_beech": ("#b8862e", "#d0a040", 1), "white_oak": ("#9a4a2a", "#b86a30", 1), "red_oak": ("#8e2a1e", "#b8402a", 1),
          "sassafras": ("#d8582a", "#e8902a", 1), "flowering_dogwood": ("#a82a3a", "#c84a3a", 1),
          # these turn with the seasons too, but never grew in the (now retired) Autumn Forest
          "black_walnut": ("#d8c040", "#e8d868", 0), "shagbark_hickory": ("#c89a28", "#e0b840", 0), "ohio_buckeye": ("#c06a28", "#d89038", 0),
          "sycamore": ("#a8803a", "#c8a050", 0), "black_cherry": ("#c84a28", "#e0a030", 0), "eastern_redbud": ("#d8b030", "#e8cc50", 0)}

def w(rel, obj):
    path = os.path.join(RES, rel); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

def add_tag(rel, values):
    path = os.path.join(RES, rel)
    tag = json.load(open(path)) if os.path.exists(path) else {"replace": False, "values": []}
    tag["values"] = tag["values"] + [v for v in values if v not in tag["values"]]
    w(rel, tag)

def title(sp): return sp.replace("_", " ").title()

def lang_entries():
    out = {f"block.ryancraft.{sp}_autumn_leaves": f"Autumn {title(sp)} Leaves" for sp in AUTUMN}
    out["biome.ryancraft.autumn_forest"] = "Autumn Forest"
    return out

if __name__ == "__main__":
    names = []
    for i, (sp, (main, fleck, n)) in enumerate(AUTUMN.items()):
        name = f"{sp}_autumn_leaves"; names.append(f"ryancraft:{name}")
        leaves(9100 + i * 7, main, fleck, mix=0.3).save(os.path.join(A, "textures", "block", name + ".png"))
        w(f"assets/ryancraft/blockstates/{name}.json", {"variants": {"": {"model": f"ryancraft:block/{name}"}}})
        w(f"assets/ryancraft/models/block/{name}.json", {"parent": "minecraft:block/leaves", "textures": {"all": f"ryancraft:block/{name}"}})
        w(f"assets/ryancraft/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{name}"}})
        loot = open(os.path.join(D, "ryancraft", "loot_table", "blocks", f"{sp}_leaves.json")).read().replace(f"ryancraft:{sp}_leaves", f"ryancraft:{name}")
        w(f"data/ryancraft/loot_table/blocks/{name}.json", json.loads(loot))
        if n == 0: continue
        w(f"data/ryancraft/worldgen/configured_feature/{sp}_autumn.json", {"type": "ryancraft:species_tree", "config": {"species": sp, "autumn": True}})
        w(f"data/ryancraft/worldgen/placed_feature/{sp}_autumn_forest.json", {"feature": f"ryancraft:{sp}_autumn", "placement": [
            {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [{"data": n - 1, "weight": 1}, {"data": n, "weight": 2}, {"data": n + 1, "weight": 1}]}},
            {"type": "minecraft:in_square"}, {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}, {"type": "minecraft:biome"},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
                "state": {"Name": f"ryancraft:{sp}_sapling", "Properties": {"stage": "0"}}}}]})
    for kind in ("block", "item"): add_tag(f"data/minecraft/tags/{kind}/leaves.json", names)
    add_tag("data/minecraft/tags/worldgen/biome/is_forest.json", ["ryancraft:autumn_forest"])
    add_tag("data/minecraft/tags/worldgen/biome/is_overworld.json", ["ryancraft:autumn_forest"])
    # the biome: the Seasonal Forest's ground rules, autumn colours, its own trees, leaf litter and pumpkins
    biome = json.load(open(os.path.join(D, "ryancraft", "worldgen", "biome", "seasonal_forest.json")))
    biome["attributes"]["minecraft:visual/sky_color"] = "#86a8f0"
    biome["effects"] = {"water_color": "#3f76e4", "grass_color": "#b8a24a", "foliage_color": "#d08a2a", "dry_foliage_color": "#a85a26"}
    biome["temperature"] = 0.6
    # keep vanilla's relative feature order (a mismatch between biomes is a "feature order cycle" crash): lichen, trees,
    # grass, mushrooms, leaf litter, pumpkins, cane
    biome["features"][9] = (["minecraft:glow_lichen"] + [f"ryancraft:{sp}_autumn_forest" for sp, v in AUTUMN.items() if v[2]]
        + ["minecraft:patch_bush", "minecraft:patch_grass_forest", "minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal",
           "minecraft:patch_leaf_litter", "minecraft:patch_pumpkin", "minecraft:patch_sugar_cane"])
    w("data/ryancraft/worldgen/biome/autumn_forest.json", biome)
    print("autumn ok", len(names))
