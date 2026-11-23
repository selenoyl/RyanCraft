"""The Skylands: block textures and models (skystone, the three clouds, the portal), loot, lang, and the dimension
(vanilla's floating-islands terrain with skystone cores under grass, a bright sky, low clouds, a meadow biome with
drifting cloud patches).
  python3 tools/skylands.py      (tools/resources.py merges the lang)"""
import json, os, random, copy
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); RES = M.RES; T = os.path.join(HERE, "templates", "skylands")
TEX = os.path.join(M.A, "textures", "block")
LANG = {"skystone": "Skystone", "cloud": "Cloud", "blue_cloud": "Blue Cloud", "storm_cloud": "Storm Cloud", "sky_portal": "Skylands Portal"}

def lang_entries():
    out = {f"block.ryancraft.{k}": v for k, v in LANG.items()}; out["biome.ryancraft.sky_meadow"] = "Sky Meadow"; return out

def cloud_tex(base, seed, alpha=200):
    """Soft, slightly lumpy cloud: a pale colour with gentle light and shade, a little see-through."""
    n = M.vnoise(seed, 4); r = random.Random(seed); img = Image.new("RGBA", (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            t = 0.88 + 0.16 * n[y][x] + (r.random() - 0.5) * 0.04
            px[x, y] = tuple(max(0, min(255, int(c * t))) for c in base) + (alpha,)
    return img

def skystone():
    """Pale blue-grey stone with faint white veins, light as the islands it holds up."""
    img = M.rock(70, (150, 160, 176), (204, 212, 224), 0.22); px = img.load(); r = random.Random(71)
    for _ in range(3):
        x, y = r.randrange(16), r.randrange(16)
        for k in range(r.randint(4, 7)):
            px[x % 16, y % 16] = (232, 238, 246, 255); x += 1; y += r.choice((0, 0, 1, -1))
    return img

def portal_tex():
    """Vanilla's nether-portal swirl (all frames), recoloured to a bright sky blue."""
    im = Image.open(os.path.join(T, "nether_portal.png")).convert("RGBA"); px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            v = (r + g + b) / 3 / 255
            px[x, y] = (int(90 + 140 * v), int(150 + 100 * v), 255, a)
    return im

def blocks():
    skystone().save(os.path.join(TEX, "skystone.png"))
    cloud_tex((246, 248, 252), 72).save(os.path.join(TEX, "cloud.png"))
    cloud_tex((150, 200, 248), 73).save(os.path.join(TEX, "blue_cloud.png"))
    cloud_tex((150, 154, 164), 74, 170).save(os.path.join(TEX, "storm_cloud.png"))
    portal_tex().save(os.path.join(TEX, "sky_portal.png"))
    mc = json.load(open(os.path.join(T, "assets", "minecraft", "textures", "block", "nether_portal.png.mcmeta")))
    json.dump(mc, open(os.path.join(TEX, "sky_portal.png.mcmeta"), "w"))
    M.w("assets/ryancraft/blockstates/skystone.json", {"variants": {"": {"model": "ryancraft:block/skystone"}}})
    M.w("assets/ryancraft/models/block/skystone.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "ryancraft:block/skystone"}})
    for c in ("cloud", "blue_cloud", "storm_cloud"):
        M.w(f"assets/ryancraft/blockstates/{c}.json", {"variants": {"": {"model": f"ryancraft:block/{c}"}}})
        M.w(f"assets/ryancraft/models/block/{c}.json", {"parent": "minecraft:block/cube_all", "render_type": "translucent", "textures": {"all": f"ryancraft:block/{c}"}})
    for n in ("skystone", "cloud", "blue_cloud", "storm_cloud"):
        M.w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
        M.w(f"data/ryancraft/loot_table/blocks/{n}.json", M.drops(n) if n == "skystone" else {"type": "minecraft:block", "pools": [{"rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}], "conditions": [{"condition": "minecraft:match_tool",
            "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}]})
    bs = json.load(open(os.path.join(T, "assets", "minecraft", "blockstates", "nether_portal.json")))
    M.w("assets/ryancraft/blockstates/sky_portal.json", json.loads(json.dumps(bs).replace("minecraft:block/nether_portal", "ryancraft:block/sky_portal")))
    for f in ("nether_portal_ns", "nether_portal_ew"):
        m = json.load(open(os.path.join(T, "assets", "minecraft", "models", "block", f + ".json")))
        M.w(f"assets/ryancraft/models/block/{f.replace('nether_portal', 'sky_portal')}.json",
            json.loads(json.dumps(m).replace("minecraft:block/nether_portal", "ryancraft:block/sky_portal")))
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:skystone"])
    M.add_tag("data/minecraft/tags/block/mineable/shovel.json", ["ryancraft:cloud", "ryancraft:blue_cloud", "ryancraft:storm_cloud"])

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
        "attributes": {"minecraft:visual/sky_color": "#8cc8ff", "minecraft:visual/fog_color": "#d4ecff", "minecraft:visual/cloud_height": 48.0,
                       "minecraft:visual/cloud_color": "#f0ffffff",
                       "minecraft:gameplay/bed_rule": {"can_set_spawn": "always", "can_sleep": "when_dark", "error_message": {"translate": "block.minecraft.bed.no_sleep"}},
                       "minecraft:audio/background_music": {"default": {"max_delay": 24000, "min_delay": 12000, "sound": "minecraft:music.game"}}},
        "timelines": "#minecraft:in_overworld"})
    M.w(D + "dimension/skylands.json", {"type": "ryancraft:skylands", "generator": {"type": "minecraft:noise", "settings": "ryancraft:skylands",
        "biome_source": {"type": "minecraft:fixed", "biome": "ryancraft:sky_meadow"}}})
    # drifting cloud patches: clusters of cloud in the open air between islands (mostly white, some blue, some storm)
    for c, chance in (("cloud", 2), ("blue_cloud", 6), ("storm_cloud", 8)):
        M.w(D + f"worldgen/configured_feature/sky_{c}.json", {"type": "ryancraft:sky_cloud", "config": {"state": {"Name": f"ryancraft:{c}"}}})
        M.w(D + f"worldgen/placed_feature/sky_{c}.json", {"feature": f"ryancraft:sky_{c}", "placement": [
            {"type": "minecraft:rarity_filter", "chance": chance}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 40}, "max_inclusive": {"absolute": 200}}},
            {"type": "minecraft:biome"}]})
    M.w(D + "worldgen/placed_feature/sky_willow_islands.json", {"feature": "ryancraft:sky_willow", "placement": [
        {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [{"weight": 3, "data": 1}, {"weight": 2, "data": 2}, {"weight": 1, "data": 0}]}},
        {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"}, {"type": "minecraft:biome"},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
         "state": {"Name": "ryancraft:sky_willow_sapling", "Properties": {"stage": "0"}}}}]})
    b = copy.deepcopy(json.load(open(os.path.join(T, "meadow.json"))))
    b["carvers"] = []
    b["features"] = [[] for _ in range(11)]
    b["features"][4] = ["ryancraft:sky_cloud", "ryancraft:sky_blue_cloud", "ryancraft:sky_storm_cloud"]          # surface structures step
    b["features"][9] = ["ryancraft:sky_willow_islands", "minecraft:patch_tall_grass_2", "minecraft:patch_grass_meadow", "minecraft:flower_meadow", "minecraft:trees_meadow"]
    b["effects"]["grass_color"] = "#7ed65e"; b["effects"]["foliage_color"] = "#6ccf50"
    b["attributes"] = {"minecraft:visual/sky_color": "#8cc8ff"}
    M.w(D + "worldgen/biome/sky_meadow.json", b)

if __name__ == "__main__":
    blocks(); dimension(); print("skylands ok")
