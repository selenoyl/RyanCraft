"""Cooking (food/Cooking.java): the clay cooking pot (free-standing, or settled onto a campfire's logs) with what's
in it showing at the top (water, milk, or the finished stew, soup, chowder or porridge), the unfired pot, the meal
bowls (the classic mushroom stew recoloured per kind), recipes, and names, including the short ingredient names
meals are named after ("Raw Venison" makes "Venison Stew").
  python3 tools/cooking.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M
import metals_assets as MA

B = os.path.join(M.TEX, "block"); I = os.path.join(M.TEX, "item")
ci = lambda n: os.path.join(M.CLASSIC, "item", n + ".png")
FILLS = {   # base colour, fleck colour
    "water": ((52, 92, 200), (110, 150, 236)), "milk": ((236, 234, 226), (250, 250, 246)),
    "stew": ((112, 64, 34), (168, 104, 52)), "soup": ((106, 140, 52), (170, 196, 80)),
    "chowder": ((222, 206, 162), (246, 232, 190)), "porridge": ((198, 170, 112), (232, 208, 150))}
KINDS = ["stew", "soup", "chowder", "porridge"]

def fill_tex(name):
    base, fleck = FILLS[name]; r = random.Random(name); im = Image.new("RGBA", (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            c = fleck if r.random() < (0.10 if name in ("water", "milk") else 0.22) else base
            k = 0.92 + 0.12 * r.random(); px[x, y] = tuple(min(255, int(v * k)) for v in c) + (255,)
    im.save(os.path.join(B, f"cooking_pot_{name}.png"))

def pot_model(seated, fill):
    dy = -2 if seated else 0                                     # over a campfire it sinks into the flames a little
    el = lambda f, t, faces: {"from": [f[0], f[1] + dy, f[2]], "to": [t[0], t[1] + dy, t[2]], "faces": {d: {"texture": "#pot"} for d in faces}}
    els = [el([3, 0, 3], [13, 9, 13], ("north", "south", "east", "west", "down")),            # the belly
           el([2, 9, 2], [14, 11, 3], ("north", "south", "east", "west", "up", "down")),     # the rim, four sides
           el([2, 9, 13], [14, 11, 14], ("north", "south", "east", "west", "up", "down")),
           el([2, 9, 3], [3, 11, 13], ("north", "south", "east", "west", "up", "down")),
           el([13, 9, 3], [14, 11, 13], ("north", "south", "east", "west", "up", "down")),
           el([0.5, 6, 7], [3, 7.5, 9], ("north", "south", "east", "west", "up", "down")),   # lugs
           el([13, 6, 7], [15.5, 7.5, 9], ("north", "south", "east", "west", "up", "down"))]
    tex = {"particle": "ryancraft:block/clay_pot", "pot": "ryancraft:block/clay_pot"}
    if fill != "empty":
        els.append({"from": [3, 8 + dy, 3], "to": [13, 8 + dy, 13], "faces": {"up": {"texture": "#fill"}}})
        tex["fill"] = f"ryancraft:block/cooking_pot_{fill}"
    else:
        els.append({"from": [3, 1 + dy, 3], "to": [13, 1 + dy, 13], "faces": {"up": {"texture": "#pot"}}})        # the inside floor
    return {"parent": "minecraft:block/block", "ambientocclusion": False, "textures": tex, "elements": els}

def meal_tex(kind):
    bowl = Image.open(ci("bowl")).convert("RGBA"); stew = Image.open(ci("mushroom_stew")).convert("RGBA")
    pb, ps = bowl.load(), stew.load(); spots = {(x, y) for y in range(16) for x in range(16) if ps[x, y][3] and ps[x, y] != pb[x, y] }
    base, fleck = FILLS[kind]
    pal = [tuple(int(v * 0.55) for v in base), base, fleck, tuple(min(255, int(v * 1.15)) for v in fleck)]
    out = MA.recolour(stew, pal, spots); po = out.load()
    for x, y in spots:
        if y >= 9: po[x, y] = pal[0] + (255,)                          # the far side of the bowl, in shadow
    out.save(os.path.join(I, f"meal_{kind}.png"))

def unfired():
    im = Image.open(os.path.join(I, "unfired_clay_pot.png")).convert("RGBA")
    im.save(os.path.join(I, "unfired_cooking_pot.png"))

def short(name):
    for p in ("Raw ", "Cooked ", "Roast ", "Baked ", "Shelled "): name = name.replace(p, "")
    return name

VANILLA = {"apple": "Apple", "bread": "Bread", "porkchop": "Pork", "cooked_porkchop": "Pork", "cod": "Cod", "cooked_cod": "Cod", "salmon": "Salmon",
           "cooked_salmon": "Salmon", "tropical_fish": "Tropical Fish", "beef": "Beef", "cooked_beef": "Beef", "chicken": "Chicken", "cooked_chicken": "Chicken",
           "carrot": "Carrot", "golden_carrot": "Golden Carrot", "potato": "Potato", "baked_potato": "Potato", "rabbit": "Rabbit", "cooked_rabbit": "Rabbit",
           "mutton": "Mutton", "cooked_mutton": "Mutton", "beetroot": "Beetroot", "sweet_berries": "Berry", "glow_berries": "Glow Berry", "melon_slice": "Melon",
           "dried_kelp": "Kelp", "kelp": "Kelp", "brown_mushroom": "Mushroom", "red_mushroom": "Mushroom", "wheat": "Wheat", "egg": "Egg", "sugar": "Sweet",
           "pumpkin": "Pumpkin", "pumpkin_pie": "Pumpkin", "cookie": "Cookie", "honey_bottle": "Honey", "chorus_fruit": "Chorus", "rotten_flesh": "Rotten",
           "spider_eye": "Spider", "poisonous_potato": "Bad Potato", "golden_apple": "Golden Apple"}

def lang_entries():
    out = {"block.ryancraft.cooking_pot": "Cooking Pot", "item.ryancraft.unfired_cooking_pot": "Unfired Cooking Pot", "item.ryancraft.meal": "Meal",
           "item.ryancraft.meal.stew": "%s Stew", "item.ryancraft.meal.soup": "%s Soup", "item.ryancraft.meal.chowder": "%s Chowder",
           "item.ryancraft.meal.porridge": "%s Porridge", "item.ryancraft.meal.made_with": "Made with:",
           "block.ryancraft.cooking_pot.empty": "The pot is empty: pour in water or milk",
           "block.ryancraft.cooking_pot.base_water": "Water, ready for ingredients", "block.ryancraft.cooking_pot.base_milk": "Milk, ready for ingredients",
           "block.ryancraft.cooking_pot.simmering_water": "Simmering in water: %s (%s%%)", "block.ryancraft.cooking_pot.simmering_milk": "Simmering in milk: %s (%s%%)",
           "block.ryancraft.cooking_pot.cold_water": "In cold water: %s (set it on a lit campfire)", "block.ryancraft.cooking_pot.cold_milk": "In cold milk: %s (set it on a lit campfire)",
           "block.ryancraft.cooking_pot.meal": "%s: %s bowls left"}
    for k, v in VANILLA.items(): out[f"meal.ryancraft.ingredient.minecraft.{k}"] = v
    d = json.load(open(os.path.join(M.A, "lang", "en_us.json")))
    for k, v in d.items():
        if k.startswith("item.ryancraft.") and k.count(".") == 2: out[f"meal.ryancraft.ingredient.ryancraft.{k.split('.')[2]}"] = short(v)
    return out

def build():
    W = M.w
    for f in FILLS: fill_tex(f)
    for k in KINDS: meal_tex(k)
    unfired()
    for seated in (False, True):
        for f in ["empty"] + list(FILLS):
            W(f"assets/ryancraft/models/block/cooking_pot{'_seated' if seated else ''}_{f}.json", pot_model(seated, f))
    W("assets/ryancraft/blockstates/cooking_pot.json", {"variants": {f"cooking={c},fill={f},seated={str(s).lower()}": {"model": f"ryancraft:block/cooking_pot{'_seated' if s else ''}_{f}"}
        for c in ("false", "true") for f in ["empty"] + list(FILLS) for s in (False, True)}})
    W("assets/ryancraft/items/cooking_pot.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/cooking_pot_empty"}})
    W("assets/ryancraft/models/item/unfired_cooking_pot.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/unfired_cooking_pot"}})
    W("assets/ryancraft/items/unfired_cooking_pot.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/unfired_cooking_pot"}})
    for k in KINDS: W(f"assets/ryancraft/models/item/meal_{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/meal_{k}"}})
    W("assets/ryancraft/items/meal.json", {"model": {"type": "minecraft:select", "property": "minecraft:custom_model_data", "index": 0,
        "cases": [{"when": k, "model": {"type": "minecraft:model", "model": f"ryancraft:item/meal_{k}"}} for k in KINDS],
        "fallback": {"type": "minecraft:model", "model": "ryancraft:item/meal_stew"}}})
    W("data/ryancraft/loot_table/blocks/cooking_pot.json", M.drops("cooking_pot"))
    W("data/ryancraft/recipe/unfired_cooking_pot.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["C C", "C C", "CCC"],
        "key": {"C": "minecraft:clay_ball"}, "result": {"id": "ryancraft:unfired_cooking_pot", "count": 1}})
    W("data/ryancraft/recipe/cooking_pot.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": "ryancraft:unfired_cooking_pot",
        "result": {"id": "ryancraft:cooking_pot"}, "experience": 0.3, "cookingtime": 200})

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("cooking ok")
