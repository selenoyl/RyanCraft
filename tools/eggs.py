"""Regenerates every RyanCraft spawn egg from the classic base-game egg (tools/classic_spawn_egg*.png)."""
import critters, big_game, wildlife, fish2
from critters import egg_icon, save_item

EXTRA = {"reindeer": ("#e8e2d6", "#6a5a48"), "butterfly": ("#e08a2a", "#1a1414"), "moobloom": ("#f2c626", "#fbf2d0"), "swan": ("#f4f4f0", "#e8902a"), "mudling": ("#7a5638", "#6aa83a"), "sky_ray": ("#b8d4f0", "#ffffff"), "pegasus": ("#f4f4f8", "#9cc8f0"), "cloud_sheep": ("#ffffff", "#b8d8f4"), "sky_roc": ("#f2f4f8", "#e2ac28")}

MUDLING_KINDS = {"sod": ("#7a5638", "#6aa83a"), "twig": ("#8a6a40", "#c8a060"), "bark": ("#4a3424", "#7a5a38"), "mud": ("#5a4030", "#8a6a50"),
                 "sandstone": ("#e0cc94", "#c0a060"), "terracotta": ("#b0603c", "#d08858"), "snowpack": ("#f0f4f8", "#a8c8e8"), "vine": ("#3a6a2a", "#8ac04a"),
                 "gravel": ("#8a8684", "#c4c0bc"), "shell": ("#e8dcc8", "#c89878"), "pebble": ("#6a6a6a", "#a8a8a8"), "cinder": ("#3a2a2a", "#e05a20"),
                 "kite": ("#e8f0ff", "#7aa8e8")}
for k, c in MUDLING_KINDS.items(): EXTRA[f"mudling_{k}"] = c

def lang_entries():
    return {f"item.ryancraft.mudling_{k}_spawn_egg": f"{k.title()} Mudling Spawn Egg" for k in MUDLING_KINDS}

if __name__ == "__main__":
    eggs = dict(critters.EGGS); eggs.update(big_game.EGGS)
    eggs.update({n: e[2] for n, e in wildlife.entries().items()}); eggs.update(fish2.EGGS); eggs.update(EXTRA)
    for mob, (b, s) in eggs.items(): save_item(f"{mob}_spawn_egg", egg_icon(b, s))
    print(len(eggs), "eggs")
    import json, os
    from critters import ROOT
    for k in MUDLING_KINDS:                                               # item definitions and models for the per-kind eggs
        n = f"mudling_{k}_spawn_egg"
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{n}"}}, open(os.path.join(ROOT, "models/item", n + ".json"), "w"), indent=2)
        json.dump({"model": {"type": "minecraft:model", "model": f"ryancraft:item/{n}"}}, open(os.path.join(ROOT, "items", n + ".json"), "w"), indent=2)
    p = os.path.join(ROOT, "lang/en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
