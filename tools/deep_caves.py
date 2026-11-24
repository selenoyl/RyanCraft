"""The Abyss (deep/DeepCaves.java): a cave layer below the deepslate, y=-64 down to bedrock at y=-128.
Writes
  * blocks: abyssal stone (classic stone, recoloured slate-blue), cobbled, polished and bricks; toxic sludge; toxic moss and
    lumen moss (vanilla's moss, recoloured); their models, loot, recipes, tags and lang
  * the five cavern biomes (data/ryancraft/worldgen/biome) and their features
  * the built-in data pack resourcepacks/deep_caves, which deepens the overworld: its dimension type and noise settings
    (vanilla's, with min y -128; below y=-64 the density is our own big caverns, crusted top and bottom; abyssal stone
    below -64 in the surface rule), and vanilla's placed features that measure height from the world's bottom pinned to
    the heights they had, so ores and cave features stay exactly where (and as dense as) they always were; the Abyss gets
    its own copies of the ones it wants (ores, dripstone, glow lichen, sculk, lush plants)
  python3 tools/deep_caves.py      (tools/resources.py merges the lang)"""
import copy, json, os, random
from PIL import Image
import moon_assets as M
import metals_assets as MA

HERE = os.path.dirname(os.path.abspath(__file__)); TPL = os.path.join(HERE, "templates", "deep"); V = os.path.join(TPL, "vanilla")
PACK = os.path.join(M.RES, "resourcepacks", "deep_caves")
TOP, FLOOR, ABYSS_SURFACE = -64, -128, None
ABYSSAL = [(26, 28, 42), (46, 50, 70), (70, 76, 102), (100, 108, 138)]
BIOMES = ["abyssal_caverns", "toxic_caves", "frozen_caverns", "sunken_wilds", "hollow_dark"]
TITLES = {"abyssal_caverns": "Abyssal Caverns", "toxic_caves": "Toxic Caves", "frozen_caverns": "Frozen Caverns",
          "sunken_wilds": "Sunken Wilds", "hollow_dark": "Hollow Dark"}
STONES = ["abyssal_stone", "cobbled_abyssal_stone", "polished_abyssal_stone", "abyssal_bricks"]

def jw(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2); f.write("\n")

def cb(n): return os.path.join(M.CLASSIC, "block", n + ".png")

# ------------------------------------------------------------------ textures
def textures():
    B = os.path.join(M.TEX, "block")
    MA.recolour(cb("stone"), ABYSSAL).save(os.path.join(B, "abyssal_stone.png"))
    MA.recolour(cb("cobblestone"), ABYSSAL).save(os.path.join(B, "cobbled_abyssal_stone.png"))
    MA.recolour(cb("stone_bricks"), ABYSSAL).save(os.path.join(B, "abyssal_bricks.png"))
    # polished: the stone, smoothed, inside a one-pixel bevel
    st = MA.recolour(cb("stone"), ABYSSAL); px = st.load(); r = random.Random(5)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            base = ABYSSAL[1] if edge else ABYSSAL[2]
            if x == 15 or y == 15: base = ABYSSAL[0]
            d = r.randint(-4, 4); px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    st.save(os.path.join(B, "polished_abyssal_stone.png"))
    moss = os.path.join(TPL, "moss_block.png")
    MA.recolour(moss, [(70, 110, 20), (120, 176, 34), (170, 222, 60), (214, 246, 120)]).save(os.path.join(B, "toxic_moss.png"))
    MA.recolour(moss, [(150, 136, 60), (204, 192, 96), (240, 232, 150), (255, 252, 214)]).save(os.path.join(B, "lumen_moss.png"))
    # toxic sludge: a murky olive-black muck with sickly green swirls and bright bubbles
    r = random.Random(11); im = Image.new("RGBA", (16, 16)); px = im.load()
    import math
    for y in range(16):
        for x in range(16):
            w = math.sin((x + y * 0.6) * 0.8) + math.sin((x * 0.5 - y) * 0.7)
            c = (34, 44, 18) if w < -0.4 else (48, 66, 22) if w < 0.6 else (74, 104, 26)
            d = r.randint(-4, 4); px[x, y] = tuple(max(0, v + d) for v in c) + (255,)
    for _ in range(6):
        x, y = r.randrange(15), r.randrange(15)
        px[x, y] = (170, 230, 60, 255); px[x + 1, y] = (120, 186, 40, 255); px[x, y + 1] = (120, 186, 40, 255); px[x + 1, y + 1] = (26, 34, 12, 255)
    im.save(os.path.join(B, "toxic_sludge.png"))

# ------------------------------------------------------------------ blocks: models, loot, recipes, tags
def blocks():
    W = M.w
    for b in STONES + ["toxic_sludge", "toxic_moss", "lumen_moss"]:
        W(f"assets/ryancraft/blockstates/{b}.json", {"variants": {"": {"model": f"ryancraft:block/{b}"}}})
        W(f"assets/ryancraft/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{b}"}})
        W(f"assets/ryancraft/items/{b}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{b}"}})
        if b != "abyssal_stone": W(f"data/ryancraft/loot_table/blocks/{b}.json", M.drops(b))
    W("data/ryancraft/loot_table/blocks/abyssal_stone.json", M.silk_or("abyssal_stone", "cobbled_abyssal_stone"))
    R = "data/ryancraft/recipe/"
    W(R + "abyssal_stone.json", {"type": "minecraft:smelting", "category": "blocks", "ingredient": "ryancraft:cobbled_abyssal_stone",
        "result": {"id": "ryancraft:abyssal_stone"}, "experience": 0.1, "cookingtime": 200})
    W(R + "polished_abyssal_stone.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["##", "##"],
        "key": {"#": "ryancraft:abyssal_stone"}, "result": {"id": "ryancraft:polished_abyssal_stone", "count": 4}})
    W(R + "abyssal_bricks.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["##", "##"],
        "key": {"#": "ryancraft:polished_abyssal_stone"}, "result": {"id": "ryancraft:abyssal_bricks", "count": 4}})
    for out in ("polished_abyssal_stone", "abyssal_bricks"):
        W(R + f"{out}_from_abyssal_stone_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": "ryancraft:abyssal_stone",
            "result": {"id": f"ryancraft:{out}", "count": 1}})
    stones = [f"ryancraft:{b}" for b in STONES]
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", stones)
    M.add_tag("data/minecraft/tags/block/mineable/shovel.json", ["ryancraft:toxic_sludge"])
    M.add_tag("data/minecraft/tags/block/mineable/hoe.json", ["ryancraft:toxic_moss", "ryancraft:lumen_moss"])
    # abyssal stone behaves like deep stone: ores, carvers, moss and dripstone all treat it so
    for t in ("base_stone_overworld", "deepslate_ore_replaceables", "moss_replaceable", "dripstone_replaceable_blocks",
              "sculk_replaceable", "lush_ground_replaceable", "azalea_root_replaceable"):
        M.add_tag(f"data/minecraft/tags/block/{t}.json", ["ryancraft:abyssal_stone"])
    M.add_tag("data/ryancraft/tags/block/abyss_replaceable.json", ["ryancraft:abyssal_stone", "minecraft:deepslate", "minecraft:tuff",
        "minecraft:stone", "minecraft:gravel", "minecraft:dirt", "minecraft:clay"])
    M.add_tag("data/minecraft/tags/item/stone_tool_materials.json", ["ryancraft:cobbled_abyssal_stone"])
    M.add_tag("data/minecraft/tags/item/stone_crafting_materials.json", ["ryancraft:cobbled_abyssal_stone"])

# ------------------------------------------------------------------ features
def state(b, props=None):
    s = {"Name": b}
    if props: s["Properties"] = props
    return {"type": "minecraft:simple_state_provider", "state": s}

def patch(ground, surface, radius=(3, 7), depth=1, veg=None, veg_chance=0.0, edge=0.3, replaceable="#ryancraft:abyss_replaceable"):
    return {"type": "minecraft:vegetation_patch", "config": {"depth": depth, "extra_bottom_block_chance": 0.0, "extra_edge_column_chance": edge,
        "ground_state": ground, "replaceable": replaceable, "surface": surface, "vegetation_chance": veg_chance,
        "vegetation_feature": veg or {"feature": {"type": "minecraft:no_op", "config": {}}, "placement": []},
        "vertical_range": 5, "xz_radius": {"type": "minecraft:uniform", "min_inclusive": radius[0], "max_inclusive": radius[1]}}}

def abyss_placement(count, up=False):
    return [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": FLOOR + 2}, "max_inclusive": {"absolute": TOP - 2}}},
            {"type": "minecraft:environment_scan", "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
             "direction_of_search": "up" if up else "down", "max_steps": 12, "target_condition": {"type": "minecraft:solid"}},
            {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1 if up else 1}, {"type": "minecraft:biome"}]

def simple_block(b, props=None):
    return {"feature": {"type": "minecraft:simple_block", "config": {"to_place": state(b, props)}}, "placement": []}

FEATURES = {   # name: (configured feature, placement count, on the ceiling?)
    "toxic_sludge_floor": (patch(state("ryancraft:toxic_sludge"), "floor", (3, 7), 2), 90, False),
    "toxic_moss_ceiling": (patch(state("ryancraft:toxic_moss"), "ceiling", (2, 5)), 70, True),
    "frozen_floor": (patch(state("minecraft:packed_ice"), "floor", (3, 7), 2, simple_block("minecraft:snow", {"layers": "2"}), 0.6), 90, False),
    "powder_snow_floor": (patch(state("minecraft:powder_snow"), "floor", (1, 3), 2), 20, False),
    "blue_ice_ceiling": (patch(state("minecraft:blue_ice"), "ceiling", (2, 5)), 50, True),
    "lumen_moss_ceiling": (patch(state("ryancraft:lumen_moss"), "ceiling", (2, 4)), 60, True),
    "wilds_floor": (patch(state("minecraft:moss_block"), "floor", (4, 7), 1, {"feature": "minecraft:moss_vegetation", "placement": []}, 0.8), 110, False),
    "wilds_grass": (patch(state("minecraft:grass_block"), "floor", (2, 4), 1,
                          {"feature": {"type": "minecraft:simple_random_selector", "config": {"features": [simple_block("minecraft:short_grass"),
                           simple_block("minecraft:short_grass"), simple_block("minecraft:fern"), simple_block("minecraft:dandelion"),
                           simple_block("minecraft:poppy"), simple_block("minecraft:azure_bluet")]}}, "placement": []}, 0.7), 50, False),
}

# vanilla features the Abyss gets its own copies of, placed only within it (y -128 to -64), by vanilla's own configuration
ABYSS_COPIES = ["dripstone_cluster", "large_dripstone", "pointed_dripstone", "glow_lichen", "sculk_vein", "sculk_patch_deep_dark",
                "lush_caves_ceiling_vegetation", "cave_vines", "rooted_azalea_tree", "spore_blossom", "ore_tuff", "ore_gravel"]
ABYSS_ORES = {"ore_iron": ("minecraft:ore_iron", 8), "ore_gold": ("minecraft:ore_gold_buried", 4), "ore_redstone": ("minecraft:ore_redstone", 6),
              "ore_lapis": ("minecraft:ore_lapis_buried", 3), "ore_coal": ("minecraft:ore_coal_buried", 6)}

def abyss_copies():
    for n in ABYSS_COPIES:
        d = json.load(open(os.path.join(V, "placed", n + ".json")))
        for pl in d["placement"]:
            if pl.get("type") == "minecraft:height_range":
                pl["height"] = {"type": "minecraft:uniform", "min_inclusive": {"absolute": FLOOR + 1}, "max_inclusive": {"absolute": TOP - 1}}
        M.w(f"data/ryancraft/worldgen/placed_feature/abyss_{n}.json", d)
    for n, (cf, count) in ABYSS_ORES.items():
        M.w(f"data/ryancraft/worldgen/placed_feature/abyss_{n}.json", {"feature": cf, "placement": [{"type": "minecraft:count", "count": count},
            {"type": "minecraft:in_square"}, {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
            "min_inclusive": {"absolute": FLOOR + 2}, "max_inclusive": {"absolute": TOP - 2}}}, {"type": "minecraft:biome"}]})

def ore_abyssal_diamond():
    M.w("data/ryancraft/worldgen/configured_feature/ore_abyssal_diamond.json", {"type": "minecraft:ore", "config": {"size": 8,
        "discard_chance_on_air_exposure": 0.4, "targets": [{"state": {"Name": "minecraft:deepslate_diamond_ore"},
        "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]}})
    M.w("data/ryancraft/worldgen/placed_feature/ore_abyssal_diamond.json", {"feature": "ryancraft:ore_abyssal_diamond", "placement": [
        {"type": "minecraft:count", "count": 3}, {"type": "minecraft:in_square"}, {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
        "min_inclusive": {"absolute": FLOOR + 4}, "max_inclusive": {"absolute": TOP - 6}}}, {"type": "minecraft:biome"}]})

def features():
    for n, (cf, count, up) in FEATURES.items():
        M.w(f"data/ryancraft/worldgen/configured_feature/{n}.json", cf)
        M.w(f"data/ryancraft/worldgen/placed_feature/{n}.json", {"feature": f"ryancraft:{n}", "placement": abyss_placement(count, up)})
    ore_abyssal_diamond(); abyss_copies()

# ------------------------------------------------------------------ biomes
def biomes():
    lush = json.load(open(os.path.join(V, "biome_lush_caves.json")))
    ores = lush["features"][6]
    common_late = ["ryancraft:abyss_glow_lichen"]
    per = {
        "abyssal_caverns": (["ryancraft:abyss_dripstone_cluster", "ryancraft:abyss_large_dripstone", "ryancraft:abyss_pointed_dripstone"], "minecraft:music.overworld.dripstone_caves", 0.8, "#3f76e4", "#202436"),
        "toxic_caves": (["ryancraft:toxic_sludge_floor", "ryancraft:toxic_moss_ceiling"], "minecraft:music.overworld.dripstone_caves", 0.9, "#58a028", "#1c2a10"),
        "frozen_caverns": (["ryancraft:frozen_floor", "ryancraft:powder_snow_floor", "ryancraft:blue_ice_ceiling"], "minecraft:music.overworld.frozen_peaks", -0.7, "#3d57d6", "#b8d4ee"),
        "sunken_wilds": (["ryancraft:lumen_moss_ceiling", "ryancraft:wilds_floor", "ryancraft:wilds_grass", "ryancraft:abyss_lush_caves_ceiling_vegetation",
                          "ryancraft:abyss_cave_vines", "ryancraft:abyss_rooted_azalea_tree", "ryancraft:abyss_spore_blossom"], "minecraft:music.overworld.lush_caves", 0.7, "#3f76e4", "#5a7a48"),
        "hollow_dark": (["ryancraft:abyss_sculk_vein", "ryancraft:abyss_sculk_patch_deep_dark"], "minecraft:music.overworld.deep_dark", 0.8, "#3f76e4", "#0a0c14"),
    }
    monsters = lush["spawners"]["monster"]
    for b in BIOMES:
        extra, music, temp, water, fog = per[b]
        d = copy.deepcopy(lush)
        d["temperature"] = temp; d["downfall"] = 0.4
        d["effects"] = {"water_color": water}
        d["attributes"] = {"minecraft:audio/background_music": {"default": {"max_delay": 24000, "min_delay": 12000, "sound": music}},
                           "minecraft:visual/sky_color": "#78a7ff", "minecraft:visual/fog_color": fog}
        f = [[] for _ in range(11)]
        f[2] = ["minecraft:amethyst_geode"]; f[3] = ["minecraft:monster_room_deep"]
        f[6] = ["ryancraft:abyss_ore_tuff", "ryancraft:abyss_ore_gravel"] + [f"ryancraft:abyss_{n}" for n in ABYSS_ORES] + ["ryancraft:ore_abyssal_diamond"]
        f[8] = ["minecraft:spring_water", "minecraft:spring_lava"]
        veg = [e for e in extra if "sculk" not in e]
        f[7] = [e for e in extra if "sculk" in e]
        f[9] = common_late + veg
        d["features"] = f
        sp = {k: [] for k in lush["spawners"]}
        if b != "hollow_dark":
            mon = copy.deepcopy(monsters)
            if b == "frozen_caverns":
                for m in mon:
                    if m["type"] == "minecraft:skeleton": m["type"] = "minecraft:stray"
            if b == "toxic_caves":
                for m in mon:
                    if m["type"] == "minecraft:slime": m["weight"] = 300
            sp["monster"] = mon
            sp["ambient"] = copy.deepcopy(lush["spawners"]["ambient"])
        if b == "sunken_wilds":
            for k in ("axolotls", "underground_water_creature", "water_ambient"): sp[k] = copy.deepcopy(lush["spawners"][k])
        d["spawners"] = sp
        d["has_precipitation"] = True
        jw(os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", b + ".json"), d)

# ------------------------------------------------------------------ the data pack that deepens the world
def Y():
    return {"type": "minecraft:y_clamped_gradient", "from_y": -2032, "to_y": 2032, "from_value": -2032.0, "to_value": 2032.0}

def grad(y0, v0, y1, v1):
    return {"type": "minecraft:y_clamped_gradient", "from_y": y0, "to_y": y1, "from_value": v0, "to_value": v1}

def add(a, b): return {"type": "minecraft:add", "argument1": a, "argument2": b}

def abyss_density():
    """Positive = rock. Big open caverns from the cave-cheese noise (stretched wide), crusted solid against the deepslate above
    and down to the bedrock below, with tall stone pillars here and there."""
    cheese = {"type": "minecraft:noise", "noise": "minecraft:cave_cheese", "xz_scale": 0.5, "y_scale": 0.6}
    pillars = {"type": "minecraft:mul", "argument1": -0.6, "argument2": {"type": "minecraft:noise", "noise": "minecraft:pillar", "xz_scale": 3.0, "y_scale": 0.15}}
    return {"type": "minecraft:interpolated", "argument": add(add(0.18, cheese), add(add(grad(FLOOR, 1.8, FLOOR + 14, 0.0), grad(TOP - 9, 0.0, TOP, 1.8)),
            {"type": "minecraft:max", "argument1": 0.0, "argument2": {"type": "minecraft:mul", "argument1": -1.0, "argument2": pillars}}))}

def pack():
    jw(os.path.join(PACK, "pack.mcmeta"), {"pack": {"description": "RyanCraft: the Abyss, a cave layer below the deepslate (y -64 to -128)",
        "min_format": [94, 1], "max_format": [94, 1]}})
    dt = json.load(open(os.path.join(V, "dimension_type_overworld.json")))
    dt["min_y"] = FLOOR; dt["height"] = 320 - FLOOR; dt["logical_height"] = 320 - FLOOR
    jw(os.path.join(PACK, "data", "minecraft", "dimension_type", "overworld.json"), dt)
    ns = json.load(open(os.path.join(V, "noise_settings_overworld.json")))
    ns["noise"]["min_y"] = FLOOR; ns["noise"]["height"] = 320 - FLOOR
    r = ns["noise_router"]
    r["final_density"] = {"type": "minecraft:range_choice", "input": Y(), "min_inclusive": -1000000.0, "max_exclusive": float(TOP),
                          "when_in_range": abyss_density(), "when_out_of_range": r["final_density"]}
    abyss_rule = {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": "ryancraft:abyss",
        "true_at_and_below": {"absolute": TOP - 6}, "false_at_and_above": {"absolute": TOP}}, "then_run": {"type": "minecraft:block",
        "result_state": {"Name": "ryancraft:abyssal_stone"}}}
    seq = ns["surface_rule"]["sequence"]; seq.insert(len(seq) - 1, abyss_rule)
    jw(os.path.join(PACK, "data", "minecraft", "worldgen", "noise_settings", "overworld.json"), ns)
    # pin the placed features that count from the world's bottom to the heights they had over a bottom of -64
    pinned = []
    for fn in sorted(os.listdir(os.path.join(V, "placed"))):
        d = json.load(open(os.path.join(V, "placed", fn))); changed = [False]
        def fix(o):
            if isinstance(o, dict):
                if o.get("type") == "minecraft:height_range" or "height" in o and isinstance(o.get("height"), dict) and "type" in o["height"]:
                    h = o["height"]
                    for k in ("min_inclusive", "max_inclusive"):
                        v = h.get(k)
                        if isinstance(v, dict) and "above_bottom" in v:
                            h[k] = {"absolute": TOP + v["above_bottom"]}; changed[0] = True
                for v in o.values(): fix(v)
            elif isinstance(o, list):
                for v in o: fix(v)
        fix(d)
        if changed[0]:
            jw(os.path.join(PACK, "data", "minecraft", "worldgen", "placed_feature", fn), d); pinned.append(fn[:-5])
    return pinned

def lang_entries():
    out = {"block.ryancraft.abyssal_stone": "Abyssal Stone", "block.ryancraft.cobbled_abyssal_stone": "Cobbled Abyssal Stone",
           "block.ryancraft.polished_abyssal_stone": "Polished Abyssal Stone", "block.ryancraft.abyssal_bricks": "Abyssal Bricks",
           "block.ryancraft.toxic_sludge": "Toxic Sludge", "block.ryancraft.toxic_moss": "Toxic Moss", "block.ryancraft.lumen_moss": "Lumen Moss"}
    for b, t in TITLES.items(): out[f"biome.ryancraft.{b}"] = t
    return out

if __name__ == "__main__":
    textures(); blocks(); features(); biomes(); pinned = pack()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries())
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2, ensure_ascii=False); open(p, "a").write("\n")
    print("abyss ok; pinned:", " ".join(pinned))
