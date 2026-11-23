"""The Money Changer (trade/Currency.java): the Coin Scale (a brass balance on an oak counter) and the villager's
outfit, which is vanilla's librarian overlay with the red hat and trim turned banker's green (the gold band kept).
  python3 tools/money_changer.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); T = os.path.join(HERE, "templates", "villager")

def green(src, dst):
    im = Image.open(src).convert("RGBA"); px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a and r > g + 40 and r > b + 40:                            # the red cloth
                v = r / 255; px[x, y] = (int(40 * v), int(120 * v + 20), int(60 * v + 10), a)
    os.makedirs(os.path.dirname(dst), exist_ok=True); im.save(dst)

def scale_model():
    brass, wood = "#brass", "#wood"
    el = lambda f, t, tex: {"from": f, "to": t, "faces": {d: {"texture": tex} for d in ("north", "south", "east", "west", "up", "down")}}
    return {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_planks", "wood": "minecraft:block/oak_planks", "brass": "minecraft:block/gold_block"},
            "elements": [el([2, 0, 3], [14, 3, 13], wood),                  # the counter
                         el([7, 3, 7], [9, 13, 9], brass),                  # the post
                         el([1.5, 12.5, 7.5], [14.5, 13.5, 8.5], brass),    # the beam
                         el([2.5, 7, 7.75], [3, 12.5, 8.25], brass), el([13, 7, 7.75], [13.5, 12.5, 8.25], brass),   # hangers
                         el([0.5, 6.5, 5.75], [5, 7, 10.25], brass), el([11, 6.5, 5.75], [15.5, 7, 10.25], brass),   # the pans
                         el([7.5, 13, 7.5], [8.5, 14.5, 8.5], brass)]}      # the finial

def lang_entries():
    return {"block.ryancraft.coin_scale": "Coin Scale", "entity.ryancraft.villager.money_changer": "Money Changer"}

def build():
    W = M.w
    green(os.path.join(T, "librarian.png"), os.path.join(M.TEX, "entity", "villager", "profession", "money_changer.png"))
    green(os.path.join(T, "zombie_librarian.png"), os.path.join(M.TEX, "entity", "zombie_villager", "profession", "money_changer.png"))
    W("assets/ryancraft/models/block/coin_scale.json", scale_model())
    W("assets/ryancraft/blockstates/coin_scale.json", {"variants": {f"facing={f}": ({"model": "ryancraft:block/coin_scale", "y": r} if r else {"model": "ryancraft:block/coin_scale"})
                                                                  for f, r in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    W("assets/ryancraft/items/coin_scale.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/coin_scale"}})
    W("data/ryancraft/loot_table/blocks/coin_scale.json", M.drops("coin_scale"))
    W("data/ryancraft/recipe/coin_scale.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["GGG", " G ", "SSS"],
        "key": {"G": "minecraft:gold_ingot", "S": "#minecraft:wooden_slabs"}, "result": {"id": "ryancraft:coin_scale", "count": 1}})
    M.add_tag("data/minecraft/tags/block/mineable/axe.json", ["ryancraft:coin_scale"])
    M.add_tag("data/minecraft/tags/point_of_interest_type/acquirable_job_site.json", ["ryancraft:coin_scale"])

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("money changer ok")
