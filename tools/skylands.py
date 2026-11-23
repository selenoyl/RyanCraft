"""The Skylands: block textures and models (skystone, the portal), loot, lang, and the dimension
(vanilla's floating-islands terrain with skystone cores under grass, a bright sky, vanilla clouds drifting just
below the islands, a meadow biome with waterfalls spilling off the island cliffs).
  python3 tools/skylands.py      (tools/resources.py merges the lang)"""
import json, os, random, copy
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); RES = M.RES; T = os.path.join(HERE, "templates", "skylands")
TEX = os.path.join(M.A, "textures", "block")
ITEM_LANG = {"zephyrite": "Zephyrite"}
LANG = {"zephyrite_ore": "Zephyrite Ore", "zephyrite_block": "Block of Zephyrite", "skystone": "Skystone", "sky_portal": "Skylands Portal", "zephyr_vent": "Zephyr Vent"}

def lang_entries():
    out = {f"block.ryancraft.{k}": v for k, v in LANG.items()}; out.update({f"item.ryancraft.{k}": v for k, v in ITEM_LANG.items()}); out["biome.ryancraft.sky_meadow"] = "Sky Meadow"; return out

def skystone():
    """Cloud strata: pale blue-grey layers, gently wavy, as if the islands were built up from settled cloud."""
    cols = [(170, 182, 204), (196, 206, 224), (214, 222, 236), (150, 164, 190)]
    r = random.Random(83); img = Image.new("RGBA", (16, 16)); px = img.load(); n = M.vnoise(83, 4)
    bands = [r.randrange(len(cols)) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            yy = (y + int(n[x][0] * 3)) % 16; k = 0.94 + 0.12 * n[y][x] + (r.random() - .5) * 0.06
            px[x, y] = M.shade_px(cols[bands[yy]], k)
    return img

def portal_tex():
    """Vanilla's portal swirl (all frames), recoloured to a cloud and sky: bright whorls of cloud-white over sky blue."""
    im = Image.open(os.path.join(T, "nether_portal.png")).convert("RGBA"); px = im.load()
    sky, cloud = (70, 150, 240), (252, 254, 255)
    vals = sorted((r + g + b) / 3 for r, g, b, a in im.get_flattened_data()); lo, hi = vals[len(vals) // 20], vals[-len(vals) // 20]
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            v = min(1, max(0, ((r + g + b) / 3 - lo) / max(1, hi - lo))) ** 1.4      # stretched: deep sky between bright cloud whorls
            px[x, y] = tuple(int(sky[k] + (cloud[k] - sky[k]) * v) for k in range(3)) + (max(a, 200),)
    return im

Z_DARK, Z_MID, Z_LIGHT, Z_GLINT = (60, 150, 170), (110, 210, 222), (180, 244, 246), (244, 255, 255)
ZEPH = [   # a zephyrite shard: a pale cyan crystal, lit from the top left
    "................",
    "..........oo....",
    ".........olho...",
    "........olhlmo..",
    ".......olhlmmo..",
    "......olhlmmdo..",
    ".....olllmmdo...",
    "....olhlmmdo....",
    "...olhlmmdo.....",
    "..olllmmdo......",
    "..ommmddo.......",
    "...oddoo........",
    "....oo..........",
    "................",
    "................",
    "................"]

def sprite(rows, pal):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal: px[x, y] = pal[ch] + (255,)
    return img

def zephyrite_textures():
    ore = skystone(); px = ore.load(); r = random.Random(75)
    for cx, cy in ((4, 4), (11, 6), (6, 11), (12, 12)):                     # crystal clusters poking out of the stone
        for dx, dy, c in ((0, 0, Z_LIGHT), (1, 0, Z_MID), (0, 1, Z_MID), (1, 1, Z_DARK), (-1, 1, Z_DARK), (0, -1, Z_GLINT)):
            px[(cx + dx) % 16, (cy + dy) % 16] = c + (255,)
    ore.save(os.path.join(TEX, "zephyrite_ore.png"))
    blk = Image.new("RGBA", (16, 16)); pb = blk.load(); n = M.vnoise(76, 4)
    for y in range(16):                                                        # facets: diagonal bands of light and shade
        for x in range(16):
            t = ((x + 2 * y) % 8) / 8 * 0.5 + n[y][x] * 0.5
            c = Z_DARK if t < 0.35 else Z_MID if t < 0.7 else Z_LIGHT
            pb[x, y] = c + (255,)
    M.frame(blk, Z_GLINT, Z_DARK).save(os.path.join(TEX, "zephyrite_block.png"))
    IT = os.path.join(M.A, "textures", "item")
    sprite(ZEPH, {"o": (30, 80, 96), "d": Z_DARK, "m": Z_MID, "l": Z_LIGHT, "h": Z_GLINT}).save(os.path.join(IT, "zephyrite.png"))

def vent_top():
    """Skystone round a dark vent mouth, a spiral of pale zephyr light breathing out of it."""
    import math
    img = skystone(); px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5; d = math.hypot(dx, dy)
            if d <= 6.2:
                ang = math.atan2(dy, dx); swirl = (ang / (2 * math.pi) * 3 + d / 2.2) % 1.0
                if d >= 5.3: px[x, y] = M.shade_px((120, 132, 156), 1.0 if dy > 0 else 1.15)     # the rim
                elif swirl < 0.28: px[x, y] = (Z_LIGHT if d < 2.5 else Z_MID) + (255,)
                else: px[x, y] = M.shade_px((40, 56, 78), 0.85 + 0.25 * (1 - d / 5.3))
    return img

def vent_side():
    """Skystone strata with a band of vent slits glowing faintly near the top."""
    img = skystone(); px = img.load()
    for x in range(1, 15):
        if x % 3 == 0: continue
        px[x, 2] = M.shade_px((40, 56, 78), 1.0); px[x, 3] = Z_MID + (255,) if x % 3 == 1 else M.shade_px((40, 56, 78), 1.1)
    for x in range(16): px[x, 4] = M.shade_px(px[x, 4][:3], 0.85)
    return img

def blocks():
    zephyrite_textures()
    for n in ("zephyrite_ore", "zephyrite_block"):
        M.w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
        M.w(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
        M.w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
    for n in ITEM_LANG:
        M.w(f"assets/ryancraft/models/item/{n}.json", {"parent": "minecraft:item/handheld" if n == "glider" else "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{n}"}})
        M.w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{n}"}})
    M.w("data/ryancraft/loot_table/blocks/zephyrite_ore.json", M.silk_or("zephyrite_ore", "zephyrite", fortune=True))
    M.w("data/ryancraft/loot_table/blocks/zephyrite_block.json", M.drops("zephyrite_block"))
    R = "data/ryancraft/recipe/"
    M.w(R + "zephyrite_block.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["###", "###", "###"],
        "key": {"#": "ryancraft:zephyrite"}, "result": {"id": "ryancraft:zephyrite_block", "count": 1}})
    M.w(R + "zephyrite_from_block.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ["ryancraft:zephyrite_block"],
        "result": {"id": "ryancraft:zephyrite", "count": 9}})
    M.w(R + "zephyrite_from_ore.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": "ryancraft:zephyrite_ore",
        "result": {"id": "ryancraft:zephyrite"}, "experience": 0.8, "cookingtime": 200})
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:zephyrite_ore", "ryancraft:zephyrite_block"])
    M.add_tag("data/minecraft/tags/block/needs_iron_tool.json", ["ryancraft:zephyrite_ore", "ryancraft:zephyrite_block"])
    M.add_tag("data/minecraft/tags/block/beacon_base_blocks.json", ["ryancraft:zephyrite_block"])
    M.add_tag("data/minecraft/tags/item/beacon_payment_items.json", ["ryancraft:zephyrite"])

    skystone().save(os.path.join(TEX, "skystone.png"))
    vent_top().save(os.path.join(TEX, "zephyr_vent_top.png")); vent_side().save(os.path.join(TEX, "zephyr_vent_side.png"))
    M.w("assets/ryancraft/blockstates/zephyr_vent.json", {"variants": {"": {"model": "ryancraft:block/zephyr_vent"}}})
    M.w("assets/ryancraft/models/block/zephyr_vent.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": "ryancraft:block/zephyr_vent_top", "side": "ryancraft:block/zephyr_vent_side", "bottom": "ryancraft:block/skystone"}})
    M.w("assets/ryancraft/items/zephyr_vent.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/zephyr_vent"}})
    M.w("data/ryancraft/loot_table/blocks/zephyr_vent.json", M.drops("zephyr_vent"))
    M.w("data/ryancraft/recipe/zephyr_vent.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": [" S ", "SZS", " S "],
        "key": {"S": "ryancraft:skystone", "Z": "ryancraft:zephyrite"}, "result": {"id": "ryancraft:zephyr_vent", "count": 1}})
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:zephyr_vent"])
    portal_tex().save(os.path.join(TEX, "sky_portal.png"))
    mc = json.load(open(os.path.join(T, "assets", "minecraft", "textures", "block", "nether_portal.png.mcmeta")))
    json.dump(mc, open(os.path.join(TEX, "sky_portal.png.mcmeta"), "w"))
    M.w("assets/ryancraft/blockstates/skystone.json", {"variants": {"": {"model": "ryancraft:block/skystone"}}})
    M.w("assets/ryancraft/models/block/skystone.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "ryancraft:block/skystone"}})
    M.w("assets/ryancraft/items/skystone.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/skystone"}})
    M.w("data/ryancraft/loot_table/blocks/skystone.json", M.drops("skystone"))
    bs = json.load(open(os.path.join(T, "assets", "minecraft", "blockstates", "nether_portal.json")))
    M.w("assets/ryancraft/blockstates/sky_portal.json", json.loads(json.dumps(bs).replace("minecraft:block/nether_portal", "ryancraft:block/sky_portal")))
    for f in ("nether_portal_ns", "nether_portal_ew"):
        m = json.load(open(os.path.join(T, "assets", "minecraft", "models", "block", f + ".json")))
        M.w(f"assets/ryancraft/models/block/{f.replace('nether_portal', 'sky_portal')}.json",
            json.loads(json.dumps(m).replace("minecraft:block/nether_portal", "ryancraft:block/sky_portal")))
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:skystone"])

def dimension():
    D = "data/ryancraft/"
    ns = json.load(open(os.path.join(T, "floating_islands.json")))
    ns["default_block"] = {"Name": "ryancraft:skystone"}
    ns["default_fluid"] = {"Name": "minecraft:air"}
    ns["surface_rule"] = {"type": "minecraft:sequence", "sequence": [
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": False, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:grass_block", "Properties": {"snowy": "false"}}}},
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": True, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:dirt"}}}]}
    M.w(D + "worldgen/noise_settings/skylands.json", ns)
    M.w(D + "dimension_type/skylands.json", {
        "ambient_light": 0.1, "coordinate_scale": 1.0, "has_ceiling": False, "has_skylight": True, "height": 256, "logical_height": 256, "min_y": 0,
        "infiniburn": "#minecraft:infiniburn_overworld", "monster_spawn_block_light_limit": 0,
        "monster_spawn_light_level": {"type": "minecraft:uniform", "max_inclusive": 7, "min_inclusive": 0},
        "attributes": {"minecraft:visual/sky_color": "#8cc8ff", "minecraft:visual/fog_color": "#d4ecff", "minecraft:visual/cloud_height": 64.0,
                       "minecraft:visual/cloud_color": "#f0ffffff",
                       "minecraft:gameplay/bed_rule": {"can_set_spawn": "always", "can_sleep": "when_dark", "error_message": {"translate": "block.minecraft.bed.no_sleep"}},
                       "minecraft:audio/background_music": {"default": {"max_delay": 24000, "min_delay": 12000, "sound": "minecraft:music.game"}}},
        "timelines": "#minecraft:in_overworld"})
    M.w(D + "dimension/skylands.json", {"type": "ryancraft:skylands", "generator": {"type": "minecraft:noise", "settings": "ryancraft:skylands",
        "biome_source": {"type": "minecraft:fixed", "biome": "ryancraft:sky_meadow"}}})
    M.w(D + "worldgen/placed_feature/sky_willow_islands.json", {"feature": "ryancraft:sky_willow", "placement": [
        {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [{"weight": 3, "data": 1}, {"weight": 2, "data": 2}, {"weight": 1, "data": 0}]}},
        {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"}, {"type": "minecraft:biome"},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
         "state": {"Name": "ryancraft:sky_willow_sapling", "Properties": {"stage": "0"}}}}]})
    M.w(D + "worldgen/configured_feature/ore_zephyrite.json", {"type": "minecraft:ore", "config": {"size": 5, "discard_chance_on_air_exposure": 0.0,
        "targets": [{"state": {"Name": "ryancraft:zephyrite_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": "ryancraft:skystone"}}]}})
    M.w(D + "worldgen/placed_feature/ore_zephyrite.json", {"feature": "ryancraft:ore_zephyrite", "placement": [
        {"type": "minecraft:count", "count": 10}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 20}, "max_inclusive": {"absolute": 220}}},
        {"type": "minecraft:biome"}]})
    # waterfalls: springs in the island cliffs, spilling off the edges into the open sky
    M.w(D + "worldgen/configured_feature/sky_spring.json", {"type": "minecraft:spring_feature", "config": {"hole_count": 1, "requires_block_below": True,
        "rock_count": 4, "state": {"Name": "minecraft:water", "Properties": {"falling": "true"}},
        "valid_blocks": ["ryancraft:skystone", "minecraft:dirt", "minecraft:grass_block"]}})
    M.w(D + "worldgen/placed_feature/sky_spring.json", {"feature": "ryancraft:sky_spring", "placement": [
        {"type": "minecraft:count", "count": 40}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 30}, "max_inclusive": {"absolute": 220}}},
        {"type": "minecraft:biome"}]})
    # updrafts: zephyr vents set into the island grass now and then
    M.w(D + "worldgen/configured_feature/zephyr_vent.json", {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider",
        "state": {"Name": "ryancraft:zephyr_vent"}}}})
    M.w(D + "worldgen/placed_feature/zephyr_vent.json", {"feature": "ryancraft:zephyr_vent", "placement": [
        {"type": "minecraft:count", "count": 3}, {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
            {"type": "minecraft:matching_blocks", "blocks": "minecraft:grass_block"},
            {"type": "minecraft:matching_blocks", "offset": [0, 1, 0], "blocks": "minecraft:air"}]}},
        {"type": "minecraft:biome"}]})
    b = copy.deepcopy(json.load(open(os.path.join(T, "meadow.json"))))
    b["carvers"] = []
    b["features"] = [[] for _ in range(11)]
    b["features"][6] = ["ryancraft:ore_zephyrite"]
    b["features"][8] = ["ryancraft:sky_spring"]
    b["features"][2] = ["ryancraft:zephyr_vent"]
    b["features"][4] = ["ryancraft:mudling_camp"]                                                            # cloud kite camps                                                             # local modifications step                                                              # fluid springs step
    b["features"][9] = ["ryancraft:sky_willow_islands", "ryancraft:sky_flowers", "minecraft:patch_tall_grass_2", "minecraft:patch_grass_meadow", "minecraft:flower_meadow", "minecraft:trees_meadow"]
    b["effects"]["grass_color"] = "#7ed65e"; b["effects"]["foliage_color"] = "#6ccf50"
    b["attributes"] = {"minecraft:visual/sky_color": "#8cc8ff"}
    M.w(D + "worldgen/biome/sky_meadow.json", b)

if __name__ == "__main__":
    blocks(); dimension(); print("skylands ok")
