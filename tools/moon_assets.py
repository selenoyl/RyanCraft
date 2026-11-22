"""Moon blocks: moon dust, the moonstone family and meteor iron. Textures are the classic 1.2.5 ones (sand, stone,
cobblestone, stone bricks, smooth stone, iron ore, iron) recoloured, plus vanilla's raw iron where 1.2.5 has none.
Blockstates and models for the stairs, slabs and walls come from vanilla's (tools/templates/moon), renamed.
  python3 tools/moon_assets.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); TEX = os.path.join(A, "textures")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures")
TPL = os.path.join(HERE, "templates", "moon", "assets", "minecraft")

LANG = {"moon_dust": "Moon Dust", "moonstone": "Moonstone", "cobbled_moonstone": "Cobbled Moonstone",
        "cobbled_moonstone_stairs": "Cobbled Moonstone Stairs", "cobbled_moonstone_slab": "Cobbled Moonstone Slab",
        "cobbled_moonstone_wall": "Cobbled Moonstone Wall", "polished_moonstone": "Polished Moonstone", "moonstone_bricks": "Moonstone Bricks",
        "cracked_moonstone_bricks": "Cracked Moonstone Bricks", "chiseled_moonstone_bricks": "Chiseled Moonstone Bricks",
        "moonstone_brick_stairs": "Moonstone Brick Stairs", "moonstone_brick_slab": "Moonstone Brick Slab", "moonstone_brick_wall": "Moonstone Brick Wall",
        "meteor_iron_ore": "Meteor Iron Ore", "raw_meteor_iron_block": "Block of Raw Meteor Iron", "meteor_iron_block": "Block of Meteor Iron"}
EXTRA_LANG = {"block.ryancraft.bed.no_sleep_moon": "You can't sleep on the moon", "biome.ryancraft.moon": "The Moon"}
ITEM_LANG = {"raw_meteor_iron": "Raw Meteor Iron", "meteor_iron_ingot": "Meteor Iron Ingot"}

def lang_entries():
    out = {f"block.ryancraft.{k}": v for k, v in LANG.items()}; out.update({f"item.ryancraft.{k}": v for k, v in ITEM_LANG.items()}); out.update(EXTRA_LANG); return out

# --------------------------------------------------------------------------------------------- textures
DUST_TINT, STONE_TINT, METEOR_TINT = (2, 1, -3), (-3, -1, 4), (-4, -2, 8)

def recolour(src, mean, contrast=1.0, tint=(0, 0, 0)):
    """Greyscale the texture, move it to the given mean brightness (keeping its shading), then tint it."""
    im = Image.open(src).convert("RGBA"); px = im.load()
    vals = [(r + g + b) / 3 for r, g, b, a in im.get_flattened_data() if a]
    m0 = sum(vals) / len(vals)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            v = mean + ((r + g + b) / 3 - m0) * contrast
            px[x, y] = tuple(max(0, min(255, int(v + t))) for t in tint) + (a,)
    return im

def sparkle(im, n, seed, mask=None):
    """A few star-like flecks: meteor iron's tell."""
    r = random.Random(seed); px = im.load(); placed = 0; tries = 0
    while placed < n and tries < 500:
        tries += 1; x, y = r.randrange(im.width), r.randrange(im.height)
        if px[x, y][3] == 0 or (mask and (x, y) not in mask): continue
        px[x, y] = (196, 214, 255, 255) if placed % 2 == 0 else (150, 170, 220, 255); placed += 1
    return im

def textures():
    b, i = os.path.join(TEX, "block"), os.path.join(TEX, "item")
    cb = lambda n: os.path.join(CLASSIC, "block", n + ".png")
    recolour(cb("sand"), 184, 0.9, DUST_TINT).save(os.path.join(b, "moon_dust.png"))
    moonstone = recolour(cb("stone"), 158, 1.05, STONE_TINT); moonstone.save(os.path.join(b, "moonstone.png"))
    recolour(cb("cobblestone"), 150, 1.0, STONE_TINT).save(os.path.join(b, "cobbled_moonstone.png"))
    recolour(cb("smooth_stone"), 166, 1.0, STONE_TINT).save(os.path.join(b, "polished_moonstone.png"))
    for src, dst in (("stone_bricks", "moonstone_bricks"), ("cracked_stone_bricks", "cracked_moonstone_bricks"), ("chiseled_stone_bricks", "chiseled_moonstone_bricks")):
        recolour(cb(src), 156, 1.0, STONE_TINT).save(os.path.join(b, dst + ".png"))
    # ore: the classic iron ore's spots, laid on moonstone, in dark star-flecked meteor iron
    stone, ore = Image.open(cb("stone")).convert("RGBA"), Image.open(cb("iron_ore")).convert("RGBA")
    out = moonstone.copy(); po, ps, pr = out.load(), stone.load(), ore.load(); spots = set()
    for y in range(16):
        for x in range(16):
            if ps[x, y] != pr[x, y]:
                v = sum(pr[x, y][:3]) / 3; k = 0.38 if v > 150 else 0.24
                po[x, y] = (int(v * k) - 4, int(v * k) - 2, int(v * k) + 10, 255); spots.add((x, y))
    sparkle(out, 6, 3, spots).save(os.path.join(b, "meteor_iron_ore.png"))
    # metal: iron recoloured to a dark blue-black gunmetal, with flecks
    sparkle(recolour(cb("iron_block"), 74, 0.75, METEOR_TINT), 7, 5).save(os.path.join(b, "meteor_iron_block.png"))
    sparkle(recolour(os.path.join(TPL, "textures", "block", "raw_iron_block.png"), 62, 0.9, METEOR_TINT), 6, 7).save(os.path.join(b, "raw_meteor_iron_block.png"))
    ingot = Image.open(os.path.join(CLASSIC, "item", "iron_ingot.png")).convert("RGBA"); pi = ingot.load()
    for y in range(16):
        for x in range(16):
            r, g, bb, a = pi[x, y]
            if a: v = (r + g + bb) / 3 * 0.5; pi[x, y] = (max(0, int(v - 6)), max(0, int(v - 2)), min(255, int(v + 14)), a)
    sparkle(ingot, 3, 9).save(os.path.join(i, "meteor_iron_ingot.png"))
    raw = Image.open(os.path.join(TPL, "textures", "item", "raw_iron.png")).convert("RGBA"); pr = raw.load()
    for y in range(16):
        for x in range(16):
            r, g, bb, a = pr[x, y]
            if a: v = (r + g + bb) / 3 * 0.48; pr[x, y] = (max(0, int(v - 6)), max(0, int(v - 2)), min(255, int(v + 12)), a)
    sparkle(raw, 3, 11).save(os.path.join(i, "raw_meteor_iron.png"))

# --------------------------------------------------------------------------------------------- json
def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

def renamed(o, a, b, tex):
    s = json.dumps(o).replace(f"minecraft:block/{a}", f"ryancraft:block/{b}")
    s = s.replace('"minecraft:block/cobblestone"', f'"ryancraft:block/{tex}"').replace('"minecraft:block/stone_bricks"', f'"ryancraft:block/{tex}"')
    return json.loads(s)

def drops(name, item=None):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{item or name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}

def silk_or(name, other, fortune=False, count=None):
    alt = {"type": "minecraft:item", "name": f"ryancraft:{other}", "functions": []}
    if fortune: alt["functions"].append({"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"})
    alt["functions"].append({"function": "minecraft:explosion_decay"})
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": f"ryancraft:{name}", "conditions": [silk]}, alt]}]}]}

def json_files():
    cubes = ["moon_dust", "moonstone", "cobbled_moonstone", "polished_moonstone", "moonstone_bricks", "cracked_moonstone_bricks",
             "chiseled_moonstone_bricks", "meteor_iron_ore", "raw_meteor_iron_block", "meteor_iron_block"]
    for n in cubes:
        w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
        w(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
        w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
    for src, dst, tex in (("cobblestone", "cobbled_moonstone", "cobbled_moonstone"), ("stone_brick", "moonstone_brick", "moonstone_bricks")):
        for kind in ("stairs", "slab", "wall"):
            bs = json.load(open(os.path.join(TPL, "blockstates", f"{src}_{kind}.json")))
            w(f"assets/ryancraft/blockstates/{dst}_{kind}.json", renamed(bs, f"{src}_{kind}", f"{dst}_{kind}", tex))
            for f in os.listdir(os.path.join(TPL, "models", "block")):
                if f.startswith(f"{src}_{kind}"):
                    m = json.load(open(os.path.join(TPL, "models", "block", f)))
                    w(f"assets/ryancraft/models/block/{dst}{f[len(src):]}", renamed(m, f"{src}_{kind}", f"{dst}_{kind}", tex))
            item_model = f"ryancraft:block/{dst}_{kind}" + ("_inventory" if kind == "wall" else "")
            w(f"assets/ryancraft/items/{dst}_{kind}.json", {"model": {"type": "minecraft:model", "model": item_model}})
    for n in ITEM_LANG:
        w(f"assets/ryancraft/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{n}"}})
        w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{n}"}})
    # loot
    L = "data/ryancraft/loot_table/blocks/"
    for n in LANG:
        if n in ("moonstone", "meteor_iron_ore") or n.endswith("_slab"): continue
        w(L + n + ".json", drops(n))
    w(L + "moonstone.json", silk_or("moonstone", "cobbled_moonstone"))
    w(L + "meteor_iron_ore.json", silk_or("meteor_iron_ore", "raw_meteor_iron", fortune=True))
    for n in ("cobbled_moonstone_slab", "moonstone_brick_slab"):
        w(L + n + ".json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}", "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{"condition": "minecraft:block_state_property", "block": f"ryancraft:{n}",
             "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]}]}]})
    # recipes
    R = "data/ryancraft/recipe/"
    shaped = lambda name, pat, key, n, cat="building": w(f"{R}{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pat,
        "key": {k: f"ryancraft:{v}" for k, v in key.items()}, "result": {"id": f"ryancraft:{name}", "count": n}})
    shapeless = lambda name, ings, n, out=None: w(f"{R}{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [f"ryancraft:{x}" for x in ings], "result": {"id": f"ryancraft:{out or name}", "count": n}})
    def smelt(name, src, out, xp, blast=True):
        for t, tm in (("smelting", 200), ("blasting", 100)) if blast else (("smelting", 200),):
            w(f"{R}{name}{'_from_blasting' if t == 'blasting' else ''}.json", {"type": f"minecraft:{t}", "category": "misc", "ingredient": f"ryancraft:{src}",
              "result": {"id": f"ryancraft:{out}"}, "experience": xp, "cookingtime": tm})
    smelt("moonstone", "cobbled_moonstone", "moonstone", 0.1, blast=False)
    smelt("cracked_moonstone_bricks", "moonstone_bricks", "cracked_moonstone_bricks", 0.1, blast=False)
    smelt("meteor_iron_ingot", "raw_meteor_iron", "meteor_iron_ingot", 1.0)
    smelt("meteor_iron_ingot_from_ore", "meteor_iron_ore", "meteor_iron_ingot", 1.0)
    shaped("polished_moonstone", ["##", "##"], {"#": "moonstone"}, 4)
    shaped("moonstone_bricks", ["##", "##"], {"#": "polished_moonstone"}, 4)
    shaped("chiseled_moonstone_bricks", ["#", "#"], {"#": "moonstone_brick_slab"}, 1)
    for base, mat in (("cobbled_moonstone", "cobbled_moonstone"), ("moonstone_brick", "moonstone_bricks")):
        shaped(f"{base}_stairs", ["#  ", "## ", "###"], {"#": mat}, 4)
        shaped(f"{base}_slab", ["###"], {"#": mat}, 6)
        shaped(f"{base}_wall", ["###", "###"], {"#": mat}, 6, "misc")
    shaped("meteor_iron_block", ["###", "###", "###"], {"#": "meteor_iron_ingot"}, 1)
    shaped("raw_meteor_iron_block", ["###", "###", "###"], {"#": "raw_meteor_iron"}, 1)
    shapeless("meteor_iron_ingot_from_block", ["meteor_iron_block"], 9, "meteor_iron_ingot")
    shapeless("raw_meteor_iron_from_block", ["raw_meteor_iron_block"], 9, "raw_meteor_iron")
    def cut(name, src, n):
        w(f"{R}{name}_from_{src}_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": f"ryancraft:{src}", "result": {"id": f"ryancraft:{name}", "count": n}})
    for name, n in (("cobbled_moonstone_stairs", 1), ("cobbled_moonstone_slab", 2), ("cobbled_moonstone_wall", 1)): cut(name, "cobbled_moonstone", n)
    for name, n in (("polished_moonstone", 1), ("moonstone_bricks", 1), ("moonstone_brick_stairs", 1), ("moonstone_brick_slab", 2),
                    ("moonstone_brick_wall", 1), ("chiseled_moonstone_bricks", 1)): cut(name, "moonstone", n)
    for name, n in (("moonstone_brick_stairs", 1), ("moonstone_brick_slab", 2), ("moonstone_brick_wall", 1), ("chiseled_moonstone_bricks", 1)): cut(name, "moonstone_bricks", n)
    # tags
    stoney = [f"ryancraft:{n}" for n in LANG if n != "moon_dust"]
    add_tag("data/minecraft/tags/block/mineable/pickaxe.json", stoney)
    add_tag("data/minecraft/tags/block/mineable/shovel.json", ["ryancraft:moon_dust"])
    add_tag("data/minecraft/tags/block/needs_iron_tool.json", ["ryancraft:meteor_iron_ore", "ryancraft:raw_meteor_iron_block", "ryancraft:meteor_iron_block"])
    for kind in ("stairs", "slabs", "walls"):
        k = kind[:-1] if kind != "stairs" else "stairs"
        names = [f"ryancraft:cobbled_moonstone_{k}", f"ryancraft:moonstone_brick_{k}"]
        add_tag(f"data/minecraft/tags/block/{kind}.json", names); add_tag(f"data/minecraft/tags/item/{kind}.json", names)
    add_tag("data/minecraft/tags/block/beacon_base_blocks.json", ["ryancraft:meteor_iron_block"])
    add_tag("data/minecraft/tags/item/beacon_payment_items.json", ["ryancraft:meteor_iron_ingot"])
    add_tag("data/minecraft/tags/item/stone_crafting_materials.json", ["ryancraft:cobbled_moonstone"])
    add_tag("data/minecraft/tags/item/stone_tool_materials.json", ["ryancraft:cobbled_moonstone"])
    # what the moon's caves may carve through
    w("data/ryancraft/tags/block/moon_carver_replaceables.json", {"values": ["ryancraft:moonstone", "ryancraft:moon_dust", "ryancraft:meteor_iron_ore"]})

# --------------------------------------------------------------------------------------------- the moon dimension
def worldgen():
    """The moon: rolling moonstone under a few blocks of dust, a black sky with the stars always out, caves and wide
    old lava tubes through the stone, and meteor iron in it (glowing faintly, so it shows in the dark caves)."""
    D = "data/ryancraft/"
    day = json.load(open(os.path.join(HERE, "templates", "moon", "day_timeline.json")))
    tracks = {k: v for k, v in day["tracks"].items() if k in ("minecraft:visual/sun_angle", "minecraft:visual/star_angle", "minecraft:visual/moon_angle",
              "minecraft:gameplay/sky_light_level", "minecraft:visual/sky_light_factor", "minecraft:visual/sky_light_color", "minecraft:gameplay/monsters_burn")}
    w(D + "timeline/lunar_day.json", {"period_ticks": 24000, "tracks": tracks})
    w(D + "dimension_type/moon.json", {
        "ambient_light": 0.0,
        "attributes": {
            "minecraft:audio/background_music": {"default": {"max_delay": 24000, "min_delay": 12000, "sound": "minecraft:music.game"}},
            "minecraft:gameplay/bed_rule": {"can_set_spawn": "never", "can_sleep": "never", "explodes": False,
                                            "error_message": {"translate": "block.ryancraft.bed.no_sleep_moon"}},
            "minecraft:gameplay/respawn_anchor_works": False,
            "minecraft:visual/sky_color": "#000000", "minecraft:visual/fog_color": "#0b0b10",
            "minecraft:visual/star_brightness": 0.75, "minecraft:visual/sunrise_sunset_color": "#00000000",
            "minecraft:visual/cloud_color": "#00000000"},
        "coordinate_scale": 1.0, "has_ceiling": False, "has_skylight": True, "height": 256, "logical_height": 256, "min_y": 0,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "monster_spawn_block_light_limit": 0, "monster_spawn_light_level": {"type": "minecraft:uniform", "max_inclusive": 7, "min_inclusive": 0},
        "timelines": ["ryancraft:lunar_day"]})
    w(D + "dimension/moon.json", {"type": "ryancraft:moon", "generator": {"type": "minecraft:noise", "settings": "ryancraft:moon",
        "biome_source": {"type": "minecraft:fixed", "biome": "ryancraft:moon"}}})
    noise2d = lambda n, s, k: {"type": "minecraft:mul", "argument1": k, "argument2": {"type": "minecraft:noise", "noise": n, "xz_scale": s, "y_scale": 0.0}}
    terrain = {"type": "minecraft:add",
               "argument1": {"type": "minecraft:y_clamped_gradient", "from_y": 40, "to_y": 130, "from_value": 1.0, "to_value": -1.0},
               "argument2": {"type": "minecraft:add", "argument1": noise2d("minecraft:surface", 0.6, 0.22),
                             "argument2": noise2d("minecraft:erosion", 0.35, 0.45)}}
    zero = 0.0
    router = {k: zero for k in ("barrier", "continents", "depth", "erosion", "fluid_level_floodedness", "fluid_level_spread", "lava",
                                "preliminary_surface_level", "ridges", "temperature", "vegetation", "vein_gap", "vein_ridged", "vein_toggle")}
    router["final_density"] = {"type": "minecraft:interpolated", "argument": terrain}
    dust = {"type": "minecraft:block", "result_state": {"Name": "ryancraft:moon_dust"}}
    w(D + "worldgen/noise_settings/moon.json", {
        "aquifers_enabled": False, "default_block": {"Name": "ryancraft:moonstone"}, "default_fluid": {"Name": "minecraft:air"},
        "disable_mob_generation": True, "legacy_random_source": False, "ore_veins_enabled": False, "sea_level": 0, "spawn_target": [],
        "noise": {"height": 256, "min_y": 0, "size_horizontal": 1, "size_vertical": 2},
        "noise_router": router,
        "surface_rule": {"type": "minecraft:sequence", "sequence": [
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
                "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
             "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}},
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 2, "surface_type": "floor",
                "add_surface_depth": True, "secondary_depth_range": 0}, "then_run": dust}]}})
    def carver(name, prob, hr, vr, ys, lo, hi):
        w(D + f"worldgen/configured_carver/{name}.json", {"type": "minecraft:cave", "config": {
            "probability": prob, "replaceable": "#ryancraft:moon_carver_replaceables", "lava_level": {"absolute": -64},
            "y": {"type": "minecraft:uniform", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}},
            "yScale": {"type": "minecraft:uniform", "min_inclusive": ys[0], "max_exclusive": ys[1]},
            "horizontal_radius_multiplier": {"type": "minecraft:uniform", "min_inclusive": hr[0], "max_exclusive": hr[1]},
            "vertical_radius_multiplier": {"type": "minecraft:uniform", "min_inclusive": vr[0], "max_exclusive": vr[1]},
            "floor_level": {"type": "minecraft:uniform", "min_inclusive": -1.0, "max_exclusive": -0.4}}})
    carver("moon_cave", 0.2, (0.8, 1.6), (0.8, 1.3), (0.2, 0.9), 8, 120)
    carver("moon_lava_tube", 0.05, (2.2, 3.6), (0.9, 1.2), (0.25, 0.45), 12, 70)     # the moon's old lava tubes: wide, low tunnels
    w(D + "worldgen/configured_feature/ore_meteor_iron.json", {"type": "minecraft:ore", "config": {"size": 7, "discard_chance_on_air_exposure": 0.0,
        "targets": [{"state": {"Name": "ryancraft:meteor_iron_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": "ryancraft:moonstone"}}]}})
    w(D + "worldgen/placed_feature/ore_meteor_iron.json", {"feature": "ryancraft:ore_meteor_iron", "placement": [
        {"type": "minecraft:count", "count": 14}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": 4}, "max_inclusive": {"absolute": 90}}},
        {"type": "minecraft:biome"}]})
    features = [[] for _ in range(11)]; features[6] = ["ryancraft:ore_meteor_iron"]
    w(D + "worldgen/biome/moon.json", {"carvers": ["ryancraft:moon_cave", "ryancraft:moon_lava_tube"], "downfall": 0.0,
        "effects": {"water_color": "#3f76e4"}, "features": features, "has_precipitation": False, "spawn_costs": {},
        "spawners": {k: [] for k in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature", "water_ambient", "water_creature")},
        "temperature": 0.5})

if __name__ == "__main__":
    textures(); json_files(); worldgen(); print("moon ok")
