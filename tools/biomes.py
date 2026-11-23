"""The eastern forest variant biomes (placed in the overworld by BiomeVariants / OverworldBiomeBuilderMixin).
Each starts from its vanilla parent's biome file (tools/templates/biomes), keeps its flowers, grass, mushrooms and mobs,
and swaps the parent's trees for its own mix of our species, at its own density, with its own grass and leaf colour.
  python3 tools/biomes.py"""
import json, os, copy

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
D = os.path.join(RES, "data")
TPL = os.path.join(HERE, "templates", "biomes")

# name: (parent template, {species: weight}, trees per chunk (count, extra_chance), grass, foliage, extras, title)
# species are our configured features (ryancraft:<id>) or vanilla checked placed features ("minecraft:oak_checked").
VARIANTS = {
    "oak_hickory_woods":    ("forest", {"white_oak": 3, "red_oak": 3, "shagbark_hickory": 3, "black_walnut": 1, "minecraft:oak_checked": 1},
                             (7, 0.5), "#9db54c", "#8fae3e", [], "Oak-Hickory Woods"),
    "maple_beech_forest":   ("forest", {"sugar_maple": 4, "american_beech": 4, "red_maple": 1, "minecraft:birch_checked": 1},
                             (10, 0.5), "#5d9e3c", "#4e9a36", [], "Maple-Beech Forest"),
    "tulip_poplar_cove":    ("forest", {"tulip_poplar": 5, "red_maple": 1, "american_beech": 1},
                             (4, 0.5), "#6dbb3c", "#62b337", [], "Tulip Poplar Cove"),
    "sycamore_bottoms":     ("swamp", {"sycamore": 4, "red_maple": 2, "black_walnut": 2},
                             (6, 0.3), "#4f8a34", "#4a8530", [], "Sycamore Bottoms"),
    "cedar_glade":          ("plains", {"eastern_red_cedar": 1},
                             (1, 0.5), "#a6b55a", "#93a64a", ["minecraft:forest_rock"], "Cedar Glade"),
    "redbud_dogwood_glade": ("flower_forest", {"eastern_redbud": 3, "flowering_dogwood": 3, "white_oak": 1},
                             (3, 0.5), "#79c05a", "#6fbd4d", [], "Redbud-Dogwood Glade"),
    "old_field":            ("plains", {"sassafras": 3, "black_cherry": 2, "eastern_red_cedar": 2},
                             (0, 0.6), "#a3bd55", "#97b54a", [], "Old Field"),
    "buckeye_hollow":       ("dark_forest", {"ohio_buckeye": 4, "american_beech": 3, "red_maple": 1},
                             (9, 0.5), "#4f8f3a", "#468a34", [], "Buckeye Hollow"),
}
TREES = ("minecraft:trees_", "minecraft:dark_forest_vegetation")

def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

def checked(sp):
    """A placed feature for one of our trees that only grows where its sapling could (vanilla's *_checked pattern)."""
    if sp.startswith("minecraft:"): return sp
    w(f"data/ryancraft/worldgen/placed_feature/{sp}_checked.json", {"feature": f"ryancraft:{sp}", "placement": [
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
         "state": {"Name": f"ryancraft:{sp}_sapling", "Properties": {"stage": "0"}}}}]})
    return f"ryancraft:{sp}_checked"

def trees(name, mix, density):
    """The variant's trees: a weighted pick among its species, placed density[0] times a chunk (+1 at density[1])."""
    entries = [(checked(sp), wgt) for sp, wgt in mix.items()]
    total = sum(wgt for _, wgt in entries)
    # random_selector: each entry gets its chance of what's left; the last is the default
    feats, left = [], total
    for placed, wgt in entries[:-1]:
        feats.append({"feature": placed, "chance": round(wgt / left, 3)}); left -= wgt
    w(f"data/ryancraft/worldgen/configured_feature/{name}_trees.json", {"type": "minecraft:random_selector",
        "config": {"features": feats, "default": entries[-1][0]}})
    count, extra = density
    w(f"data/ryancraft/worldgen/placed_feature/{name}_trees.json", {"feature": f"ryancraft:{name}_trees", "placement": [
        {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [
            {"weight": int(100 * (1 - extra)), "data": count}, {"weight": int(100 * extra), "data": count + 1}]}},
        {"type": "minecraft:in_square"}, {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}, {"type": "minecraft:biome"}]})
    return f"ryancraft:{name}_trees"

def lang_entries():
    return {f"biome.ryancraft.{n}": v[6] for n, v in VARIANTS.items()}

if __name__ == "__main__":
    for name, (parent, mix, density, grass, foliage, extras, _) in VARIANTS.items():
        b = copy.deepcopy(json.load(open(os.path.join(TPL, parent + ".json"))))
        feat = trees(name, mix, density)
        veg = b["features"][9]
        veg[:] = [f for f in veg if not f.startswith(TREES)]
        veg.insert(0, feat)
        for e in extras:
            (b["features"][2] if e.endswith("rock") else veg).append(e)   # boulders are a local modification (step 2); grass is vegetation
        eff = b["effects"]; eff.pop("grass_color_modifier", None)
        eff["grass_color"] = grass; eff["foliage_color"] = foliage
        w(f"data/ryancraft/worldgen/biome/{name}.json", b)
    names = [f"ryancraft:{n}" for n in VARIANTS]
    forests = [f"ryancraft:{n}" for n in ("oak_hickory_woods", "maple_beech_forest", "tulip_poplar_cove", "redbud_dogwood_glade", "buckeye_hollow")]
    open_ = [f"ryancraft:{n}" for n in ("cedar_glade", "old_field")]
    T = "data/minecraft/tags/worldgen/biome/"
    add_tag(T + "is_overworld.json", names)
    add_tag(T + "is_forest.json", forests)
    add_tag(T + "stronghold_biased_to.json", names)
    add_tag(T + "has_structure/mineshaft.json", names)
    add_tag(T + "has_structure/ruined_portal_standard.json", names)
    add_tag(T + "has_structure/trial_chambers.json", names)
    add_tag(T + "has_structure/village_plains.json", open_)
    add_tag(T + "has_structure/pillager_outpost.json", open_)
    add_tag(T + "has_structure/woodland_mansion.json", ["ryancraft:buckeye_hollow"])
    add_tag(T + "has_structure/swamp_hut.json", ["ryancraft:sycamore_bottoms"])
    print("biomes ok")
