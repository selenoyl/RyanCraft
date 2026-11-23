"""Skylands flowers, hand-drawn in the classic flat style: cloudbloom (a white puffball), sky bell (nodding blue bells),
windflower (a pink anemone) and sunpetal (a golden star). Textures, models (plus potted), loot, dye recipes, tags, lang,
and the sky_flowers patch the Sky Meadow scatters (with some vanilla meadow flowers mixed in).
  python3 tools/sky_flowers.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image
import metals_assets as MA

TEX = os.path.join(MA.TEX, "block")
PAL = {"g": (58, 118, 40), "G": (98, 170, 58), "w": (250, 252, 255), "c": (214, 228, 244), "s": (168, 198, 236),
       "b": (70, 128, 222), "B": (136, 184, 248), "d": (40, 78, 168), "p": (236, 150, 206), "P": (252, 212, 238),
       "k": (110, 52, 96), "y": (240, 196, 40), "Y": (255, 238, 140), "o": (226, 138, 34)}
ART = {
    "cloudbloom": ["................", "................", ".....ww.ww......", "....wwwwwwww....", "...wwwwwwwwww...", "..wwwwwwwwwwcw..",
                   "..wwwwwwwwwccc..", "...ccwwwwccss...", "....sccccss.....", "......sgs.......", ".......g........", "...GG..g..GG....",
                   "....GG.g.GG.....", ".....GGgGG......", ".......g........", ".......g........"],
    "sky_bell":   ["................", ".......gggg.....", "....gggg...gg...", "...g...g....g...", "..BBb..g...Bb...", ".BbBbb.g..BBbb..",
                   ".bbbbd.g..bbbd..", "..d.d..g...d.d..", ".......g.gg.....", ".......gBBb.....", "...GG..gbBbb....", "....GG.gbbbd....",
                   ".....GGg.d.d....", ".......gGG......", ".......g........", ".......g........"],
    "windflower": ["................", "................", "......PP........", "...pp.Pp.pp.....", "...pPPppPPp.....", "....pPkkPp......",
                   "..ppPPkkkPPpp...", "..pPPPkkPPPp....", "....pPPpPPp.....", "...ppP.g.Ppp....", ".......g........", "..GG...g...GG...",
                   "...GGG.g.GGG....", ".....GGgGG......", ".......g........", ".......g........"],
    "sunpetal":   ["................", ".......y........", ".......Y........", "...y..yYy..y....", "....yyYYYyy.....", ".....YYooYY.....",
                   "..yyYYooooYYyy..", ".....YYooYY.....", "....yyYYYyy.....", "...y..yYy..y....", ".......g........", ".......g..GG....",
                   "..GG...g.GG.....", "...GGG.gGG......", ".......g........", ".......g........"],
}
TITLES = {"cloudbloom": "Cloudbloom", "sky_bell": "Sky Bell", "windflower": "Windflower", "sunpetal": "Sunpetal"}
DYES = {"cloudbloom": "white", "sky_bell": "light_blue", "windflower": "pink", "sunpetal": "yellow"}

def sprite(rows):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row[:16]):
            if ch in PAL: px[x, y] = PAL[ch] + (255,)
    return im

def lang_entries():
    out = {}
    for n, t in TITLES.items(): out[f"block.ryancraft.{n}"] = t; out[f"block.ryancraft.potted_{n}"] = f"Potted {t}"
    return out

def build():
    W, D = MA.w, "data/ryancraft/"
    surv = [{"condition": "minecraft:survives_explosion"}]
    for n in ART:
        sprite(ART[n]).save(os.path.join(TEX, n + ".png"))
        W(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
        W(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cross", "textures": {"cross": f"ryancraft:block/{n}"}})
        W(f"assets/ryancraft/blockstates/potted_{n}.json", {"variants": {"": {"model": f"ryancraft:block/potted_{n}"}}})
        W(f"assets/ryancraft/models/block/potted_{n}.json", {"parent": "minecraft:block/flower_pot_cross", "textures": {"plant": f"ryancraft:block/{n}"}})
        W(f"assets/ryancraft/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:block/{n}"}})
        W(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{n}"}})
        W(f"{D}loot_table/blocks/{n}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}], "conditions": surv}]})
        W(f"{D}loot_table/blocks/potted_{n}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:flower_pot"}], "conditions": surv},
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}], "conditions": surv}]})
        W(f"{D}recipe/{DYES[n]}_dye_from_{n}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "group": f"{DYES[n]}_dye",
            "ingredients": [f"ryancraft:{n}"], "result": {"id": f"minecraft:{DYES[n]}_dye", "count": 1}})
    ids = [f"ryancraft:{n}" for n in ART]
    for t in ("small_flowers", "flowers", "bee_food"): MA.add_tag(f"data/minecraft/tags/item/{t}.json", ids)
    for t in ("small_flowers", "flowers"): MA.add_tag(f"data/minecraft/tags/block/{t}.json", ids)
    MA.add_tag("data/minecraft/tags/block/flower_pots.json", [f"ryancraft:potted_{n}" for n in ART])
    # the patch: mostly sky flowers, a few meadow flowers among them
    weights = [(n, 4) for n in ART] + [("minecraft:oxeye_daisy", 1), ("minecraft:cornflower", 1), ("minecraft:allium", 1), ("minecraft:azure_bluet", 1)]
    dist = [{"weight": wt, "data": {"Name": n if ":" in n else f"ryancraft:{n}"}} for n, wt in weights]
    W(f"{D}worldgen/configured_feature/sky_flowers.json", {"type": "minecraft:flower", "config": {"tries": 54, "xz_spread": 6, "y_spread": 2,
        "feature": {"feature": {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:weighted_state_provider", "entries": dist}}},
                    "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}}})
    W(f"{D}worldgen/placed_feature/sky_flowers.json", {"feature": "ryancraft:sky_flowers", "placement": [
        {"type": "minecraft:count", "count": 3}, {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"},
        {"type": "minecraft:biome"}]})

if __name__ == "__main__":
    build()
    p = os.path.join(MA.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("sky flowers ok")
