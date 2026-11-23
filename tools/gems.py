"""Ruby and sapphire (deco/Gems.java): stone and deepslate ores, the gem and its block. Rubies come from hot, dry lands
(desert, badlands, savanna); sapphires from snowy and frozen ones. The gems are hand-drawn: a cushion-cut ruby and a
pear-cut sapphire. The ores are emerald ore in each gem's colours (Programmer Art's emerald ore on classic stone, and vanilla's deepslate one); the blocks are the classic diamond
block's plate in each gem's colours.
  python3 tools/gems.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image
import moon_assets as M
import metals_assets as MA

HERE = os.path.dirname(os.path.abspath(__file__)); B = os.path.join(M.TEX, "block"); I = os.path.join(M.TEX, "item")
cb = lambda n: os.path.join(M.CLASSIC, "block", n + ".png")
G = os.path.join(HERE, "templates", "gems")
DEEP, DEEP_ORE = os.path.join(G, "deepslate.png"), os.path.join(G, "deepslate_emerald_ore.png")      # vanilla
PA_STONE, PA_ORE = os.path.join(G, "pa_stone.png"), os.path.join(G, "emerald_ore.png")             # Programmer Art

GEMS = {   # outline, shadow, body, light, glint
    "ruby":     {"title": "Ruby", "o": (70, 8, 22), "d": (138, 18, 40), "m": (204, 34, 58), "l": (246, 98, 116), "w": (255, 214, 220)},
    "sapphire": {"title": "Sapphire", "o": (10, 22, 74), "d": (24, 50, 150), "m": (40, 88, 214), "l": (108, 156, 250), "w": (220, 236, 255)},
}
ART = {
    "ruby": [   # a cushion cut: a bright step facet round a square table, shading to dark lower facets
        "................",
        "................",
        "....oooooooo....",
        "...ollllllllo...",
        "..olwwwwwwwlmo..",
        "..olwmmmmmmlmo..",
        "..olwmwmmmmldo..",
        "..olwmmmmmmldo..",
        "..olwmmmmmmldo..",
        "..olwmmmmmmldo..",
        "..olmllllllldo..",
        "..ommddddddddo..",
        "...oddddddddo...",
        "....oooooooo....",
        "................",
        "................"],
    "sapphire": [   # a pear cut: a teardrop, point up, with a long bright facet down its left side
        "................",
        ".......oo.......",
        "......olmo......",
        "......olmo......",
        ".....olwmmo.....",
        ".....olwmmo.....",
        "....ollwmmdo....",
        "....ollmmmdo....",
        "...ollmmmmmdo...",
        "...olllmmmddo...",
        "...ollmmmmddo...",
        "...olmmmmdddo...",
        "....ommmmddo....",
        ".....oommdo.....",
        ".......ooo......",
        "................"],
}
BIOMES = {"ruby": "desert, badlands and savanna", "sapphire": "snowy and frozen lands"}

def sprite(g):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load(); P = GEMS[g]
    for y, row in enumerate(ART[g]):
        for x, ch in enumerate(row):
            if ch in P and ch != "title": px[x, y] = P[ch] + (255,)
    return im

def gem_ore(base, tpl_stone, tpl_ore, pal):
    """Emerald ore in the gem's colours, on our stone: the emeralds' green pixels are gradient-mapped onto the gem's
    palette by brightness (keeping their shading), and the grey sockets round them are kept as they are."""
    s, o = Image.open(tpl_stone).convert("RGBA"), Image.open(tpl_ore).convert("RGBA")
    differs = {(x, y) for y in range(16) for x in range(16) if abs(sum(s.getpixel((x, y))[:3]) - sum(o.getpixel((x, y))[:3])) > 24}
    green = {(x, y) for x, y in differs if o.getpixel((x, y))[1] > max(o.getpixel((x, y))[0], o.getpixel((x, y))[2]) + 16}
    out = Image.open(base).convert("RGBA").crop((0, 0, 16, 16)); po = out.load(); ro = MA.recolour(o.copy(), pal, green).load()
    for x, y in differs: po[x, y] = ro[x, y] if (x, y) in green else o.getpixel((x, y))
    return out

def textures():
    for g, P in GEMS.items():
        sprite(g).save(os.path.join(I, f"{g}.png"))
        pal = [P["d"], P["m"], P["l"], P["w"]]
        gem_ore(cb("stone"), PA_STONE, PA_ORE, pal).save(os.path.join(B, f"{g}_ore.png"))
        gem_ore(DEEP, DEEP, DEEP_ORE, pal).save(os.path.join(B, f"deepslate_{g}_ore.png"))
        MA.recolour(cb("diamond_block"), [P["o"], P["d"], P["m"], P["l"]]).save(os.path.join(B, f"{g}_block.png"))

def lang_entries():
    out = {}
    for g, P in GEMS.items():
        t = P["title"]
        out.update({f"item.ryancraft.{g}": t, f"block.ryancraft.{g}_ore": f"{t} Ore", f"block.ryancraft.deepslate_{g}_ore": f"Deepslate {t} Ore",
                    f"block.ryancraft.{g}_block": f"Block of {t}"})
    return out

def build():
    W, D, R, L = M.w, "data/ryancraft/", "data/ryancraft/recipe/", "data/ryancraft/loot_table/blocks/"
    for g in GEMS:
        for n in (f"{g}_ore", f"deepslate_{g}_ore", f"{g}_block"):
            W(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
            W(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
            W(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
        W(f"assets/ryancraft/models/item/{g}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{g}"}})
        W(f"assets/ryancraft/items/{g}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{g}"}})
        for o in (f"{g}_ore", f"deepslate_{g}_ore"):
            W(L + o + ".json", M.silk_or(o, g, fortune=True))
            W(f"{R}{g}_from_smelting_{o}.json", {"type": "minecraft:smelting", "category": "misc", "group": g, "ingredient": f"ryancraft:{o}",
                "result": {"id": f"ryancraft:{g}"}, "experience": 1.0, "cookingtime": 200})
            W(f"{R}{g}_from_blasting_{o}.json", {"type": "minecraft:blasting", "category": "misc", "group": g, "ingredient": f"ryancraft:{o}",
                "result": {"id": f"ryancraft:{g}"}, "experience": 1.0, "cookingtime": 100})
        W(L + f"{g}_block.json", M.drops(f"{g}_block"))
        W(f"{R}{g}_block.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["###", "###", "###"],
            "key": {"#": f"ryancraft:{g}"}, "result": {"id": f"ryancraft:{g}_block", "count": 1}})
        W(f"{R}{g}_from_{g}_block.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [f"ryancraft:{g}_block"],
            "result": {"id": f"ryancraft:{g}", "count": 9}})
        # small veins of one to three, like emerald
        W(f"{D}worldgen/configured_feature/ore_{g}.json", {"type": "minecraft:ore", "config": {"size": 3, "discard_chance_on_air_exposure": 0.0,
            "targets": [{"state": {"Name": f"ryancraft:{g}_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
                        {"state": {"Name": f"ryancraft:deepslate_{g}_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]}})
        W(f"{D}worldgen/placed_feature/ore_{g}.json", {"feature": f"ryancraft:ore_{g}", "placement": [{"type": "minecraft:count", "count": 24},
            {"type": "minecraft:in_square"}, {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": -16},
            "max_inclusive": {"absolute": 160}}}, {"type": "minecraft:biome"}]})
    ores = [f"ryancraft:{p}{g}_ore" for g in GEMS for p in ("", "deepslate_")]; blocks = [f"ryancraft:{g}_block" for g in GEMS]
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ores + blocks)
    M.add_tag("data/minecraft/tags/block/needs_iron_tool.json", ores + blocks)
    M.add_tag("data/minecraft/tags/block/beacon_base_blocks.json", blocks)
    M.add_tag("data/minecraft/tags/item/beacon_payment_items.json", [f"ryancraft:{g}" for g in GEMS])
    for g in GEMS:
        M.add_tag(f"data/c/tags/item/gems/{g}.json", [f"ryancraft:{g}"]); M.add_tag("data/c/tags/item/gems.json", [f"ryancraft:{g}"])
        M.add_tag(f"data/c/tags/block/ores/{g}.json", [f"ryancraft:{g}_ore", f"ryancraft:deepslate_{g}_ore"])
        M.add_tag(f"data/c/tags/block/storage_blocks/{g}.json", [f"ryancraft:{g}_block"])

if __name__ == "__main__":
    textures(); build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("gems ok")
