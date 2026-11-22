"""Thirst: the droplet icons for the thirst bar (9x9, drawn to sit beside the classic hunger shanks) and the death message.
tools/resources.py merges the lang."""
import os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "ryancraft", "textures", "gui", "sprites", "hud")
SHAPE = ["....#....",
         "...#o#...",
         "...#o#...",
         "..#ooo#..",
         ".#ooooo#.",
         ".#ooooo#.",
         ".#ooooo#.",
         "..#ooo#..",
         "...###..."]

def lang_entries():
    return {"tooltip.ryancraft.sips_left": "%s of 3 sips left","death.attack.ryancraft.dehydration": "%1$s died of thirst",
            "death.attack.ryancraft.dehydration.player": "%1$s died of thirst while fighting %2$s"}

def icon(kind):
    im = Image.new("RGBA", (9, 9), (0, 0, 0, 0)); px = im.load()
    OUT_C = (12, 18, 40, 255)
    for y, row in enumerate(SHAPE):
        for x, c in enumerate(row):
            if c == "#": px[x, y] = OUT_C
            elif c == "o":
                if kind == "empty" or (kind == "half" and x > 4): px[x, y] = (34, 38, 52, 255)     # dark, like an empty shank
                else:
                    px[x, y] = (46, 98, 214, 255) if y > 5 or x > 4 else (70, 128, 236, 255)
    if kind != "empty":                                                                      # glint
        for x, y in ((3, 4), (3, 5), (4, 3)): px[x, y] = (186, 218, 255, 255)
    return im

RES = os.path.join(HERE, "..", "src", "main", "resources")

def bottle_levels():
    """Water bottles go down in sips: the liquid overlay (rows 8-13 of the vanilla bottle) cut to two thirds and one third."""
    import json
    src = Image.open(os.path.join(HERE, "template_potion_overlay.png")).convert("RGBA")
    for name, keep_from in (("potion_overlay_two_thirds", 10), ("potion_overlay_one_third", 12)):
        im = src.copy(); px = im.load()
        for y in range(16):
            if y < keep_from:
                for x in range(16): px[x, y] = (0, 0, 0, 0)
        im.save(os.path.join(RES, "assets", "ryancraft", "textures", "item", name + ".png"))
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{name}", "layer1": "minecraft:item/potion"}},
                  open(os.path.join(RES, "assets", "ryancraft", "models", "item", name.replace("potion_overlay", "water_bottle") + ".json"), "w"), indent=2)
    tint = [{"type": "minecraft:potion", "default": -13083194}]
    full = {"type": "minecraft:model", "model": "minecraft:item/potion", "tints": tint}
    os.makedirs(os.path.join(RES, "assets", "minecraft", "items"), exist_ok=True)
    json.dump({"model": {"type": "minecraft:range_dispatch", "property": "minecraft:custom_model_data", "index": 0, "fallback": full, "entries": [
        {"threshold": 1, "model": {"type": "minecraft:model", "model": "ryancraft:item/water_bottle_one_third", "tints": tint}},
        {"threshold": 2, "model": {"type": "minecraft:model", "model": "ryancraft:item/water_bottle_two_thirds", "tints": tint}},
        {"threshold": 3, "model": full}]}}, open(os.path.join(RES, "assets", "minecraft", "items", "potion.json"), "w"), indent=2)

if __name__ == "__main__":
    bottle_levels()
    os.makedirs(OUT, exist_ok=True)
    for k in ("full", "half", "empty"): icon(k).save(os.path.join(OUT, f"thirst_{k}.png"))
    print("thirst ok")
