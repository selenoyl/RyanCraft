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
SUPPORTS = ["none", "tripod", "bar_x", "bar_z", "fire_lit", "fire_off"]
HERE = os.path.dirname(os.path.abspath(__file__))

def fill_tex(name):
    base, fleck = FILLS[name]; r = random.Random(name); im = Image.new("RGBA", (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            c = fleck if r.random() < (0.10 if name in ("water", "milk") else 0.22) else base
            k = 0.92 + 0.12 * r.random(); px[x, y] = tuple(min(255, int(v * k)) for v in c) + (255,)
    im.save(os.path.join(B, f"cooking_pot_{name}.png"))

def iron_tex():
    """Cast iron: dark, faintly mottled, with a few bright grains."""
    r = random.Random(7); im = Image.new("RGBA", (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            v = 44 + r.randrange(12) + (18 if r.random() < 0.04 else 0)
            px[x, y] = (v, v, v + 6, 255)
    im.save(os.path.join(B, "cooking_pot.png"))

SIDES = ("north", "south", "east", "west")
def box(f, t, faces=SIDES + ("up", "down"), tex="#iron", rot=None):
    e = {"from": f, "to": t, "faces": {d: {"texture": tex} for d in faces}}
    if rot: e["rotation"] = rot
    return e

def pot_model(support, fill):
    """A little cast-iron cauldron on three stub feet. Over a campfire it hangs by its bail from a chain: from the top of
    an iron tripod (legs splayed out over the fire), or from a crane bar spanning to the blocks either side."""
    els = [box([3, 1, 3], [13, 9, 13], SIDES + ("down",)),                     # the belly
           box([2, 3, 2], [14, 8, 14], SIDES + ("up", "down")),                # its bulge
           box([3, 2, 3], [13, 9, 3.5]), box([3, 2, 12.5], [13, 9, 13]),       # the inside walls (a box's faces only face
           box([3, 2, 3.5], [3.5, 9, 12.5]), box([12.5, 2, 3.5], [13, 9, 12.5]),   # outward, so the belly can't show them)
           box([2.5, 9, 2.5], [13.5, 11, 3.5]), box([2.5, 9, 12.5], [13.5, 11, 13.5]),      # the rim
           box([2.5, 9, 3.5], [3.5, 11, 12.5]), box([12.5, 9, 3.5], [13.5, 11, 12.5]),
           box([4, 0, 4], [5, 1, 5]), box([11, 0, 4], [12, 1, 5]), box([7.5, 0, 11], [8.5, 1, 12])]   # feet
    tex = {"particle": "ryancraft:block/cooking_pot", "iron": "ryancraft:block/cooking_pot"}
    if fill != "empty":
        els.append({"from": [3.5, 8.5, 3.5], "to": [12.5, 8.5, 12.5], "faces": {"up": {"texture": "#fill"}}})
        tex["fill"] = f"ryancraft:block/cooking_pot_{fill}"
    else:
        els.append({"from": [3.5, 2, 3.5], "to": [12.5, 2, 12.5], "faces": {"up": {"texture": "#iron"}}})
    if support in ("tripod", "bar_x", "bar_z"):
        els += [box([2, 10, 7.75], [2.5, 13.5, 8.25]), box([13.5, 10, 7.75], [14, 13.5, 8.25]),   # the bail
                box([2, 13.5, 7.75], [14, 14, 8.25])]
        top = 18 if support == "tripod" else 17
        els.append(box([7.6, 14, 7.6], [8.4, top, 8.4]))                                          # the chain
    if support in ("fire_lit", "fire_off"):                                  # sitting in a campfire: vanilla's logs (and flames) under it
        camp = json.load(open(os.path.join(HERE, "templates", "campfire", "template_campfire.json" if support == "fire_lit" else "campfire_off.json")))
        for e in els: e["from"][1] += 6; e["to"][1] += 6
        logs = camp["elements"]
        if support == "fire_lit":                                            # the campfire's own flames, cut short under the pot
            for e in logs[5:]:
                e["to"][1] = 14
                for f in e["faces"].values(): f["uv"] = [0, 3, 16, 16]
            def tongue(x, z, w, y0, y1, ang, uv):                         # a small upright flame, turned about its own middle
                return {"from": [x - w / 2, y0, z], "to": [x + w / 2, y1, z], "shade": False,
                        "rotation": {"origin": [x, y0, z], "axis": "y", "angle": ang},
                        "faces": {"north": {"uv": uv, "texture": "#fire"}, "south": {"uv": uv, "texture": "#fire"}}}
            for cx, cz in ((1.8, 1.8), (14.2, 1.8), (1.8, 14.2), (14.2, 14.2)):          # crossed flames at each corner
                logs += [tongue(cx, cz, 5, 3, 12, 45, [3, 4, 13, 16]), tongue(cx, cz, 5, 3, 12, -45, [3, 4, 13, 16])]
            for cx, cz, ang in ((8, 1.2, 0), (8, 14.8, 0), (1.2, 8, 90), (14.8, 8, 90)):  # lower licks up the middle of each side
                logs.append(tongue(cx, cz, 6, 4, 10, 0, [5, 8, 11, 16]) if ang == 0 else
                            {"from": [cx, 4, cz - 3], "to": [cx, 10, cz + 3], "shade": False,
                             "faces": {"west": {"uv": [5, 8, 11, 16], "texture": "#fire"}, "east": {"uv": [5, 8, 11, 16], "texture": "#fire"}}})
        els = logs + els
        tex.update({"log": "minecraft:block/campfire_log", "lit_log": "minecraft:block/campfire_log_lit", "fire": "minecraft:block/campfire_fire"})
        if support == "fire_off": tex["particle"] = "minecraft:block/campfire_log"
    if support == "tripod":
        leg = lambda axis, ang: box([7.5, -16, 7.5], [8.5, 18, 8.5], rot={"origin": [8, 18, 8], "axis": axis, "angle": ang})
        els += [leg("z", 22.5), leg("z", -22.5), leg("x", 22.5)]
        els.append(box([7, 17.5, 7], [9, 19, 9]))                                                 # the lashing at the top
    elif support == "bar_x":
        els.append(box([0, 17, 7.5], [16, 18, 8.5]))
    elif support == "bar_z":
        els.append(box([7.5, 17, 0], [8.5, 18, 16]))
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
    out = {"block.ryancraft.cooking_pot": "Cooking Pot", "item.ryancraft.meal": "Meal",
           "item.ryancraft.meal.stew": "%s Stew", "item.ryancraft.meal.soup": "%s Soup", "item.ryancraft.meal.chowder": "%s Chowder",
           "item.ryancraft.meal.porridge": "%s Porridge", "item.ryancraft.meal.made_with": "Made with:",
           "block.ryancraft.cooking_pot.empty": "The pot is empty: pour in water or milk",
           "block.ryancraft.cooking_pot.base_water": "Water, ready for ingredients", "block.ryancraft.cooking_pot.base_milk": "Milk, ready for ingredients",
           "block.ryancraft.cooking_pot.simmering_water": "Simmering in water: %s (%s%%)", "block.ryancraft.cooking_pot.simmering_milk": "Simmering in milk: %s (%s%%)",
           "block.ryancraft.cooking_pot.cold_water": "In cold water: %s (set it on a lit campfire)", "block.ryancraft.cooking_pot.cold_milk": "In cold milk: %s (set it on a lit campfire)",
           "block.ryancraft.cooking_pot.meal": "%s: %s bowls left",
           "container.ryancraft.campfire": "Campfire", "container.ryancraft.campfire.time_left": "Burning: %s left on this piece",
           "container.ryancraft.campfire.no_fire": "Not burning", "container.ryancraft.campfire.no_fuel": "no fuel left",
           "container.ryancraft.campfire.burning": "Fire: %s, then %s", "container.ryancraft.campfire.out": "The fire is out (%s)"}
    for k, v in VANILLA.items(): out[f"meal.ryancraft.ingredient.minecraft.{k}"] = v
    d = json.load(open(os.path.join(M.A, "lang", "en_us.json")))
    for k, v in d.items():
        if k.startswith("item.ryancraft.") and k.count(".") == 2: out[f"meal.ryancraft.ingredient.ryancraft.{k.split('.')[2]}"] = short(v)
    return out

def build():
    W = M.w
    for f in FILLS: fill_tex(f)
    for k in KINDS: meal_tex(k)
    iron_tex()
    for sup in SUPPORTS:
        for f in ["empty"] + list(FILLS):
            W(f"assets/ryancraft/models/block/cooking_pot_{sup}_{f}.json", pot_model(sup, f))
    def model_for(sup, lit):
        return ("fire_lit" if lit == "true" else "fire_off") if sup == "fire" else sup
    W("assets/ryancraft/blockstates/cooking_pot.json", {"variants": {f"cooking={c},fill={f},lit={lit},support={sup}": {"model": f"ryancraft:block/cooking_pot_{model_for(sup, lit)}_{f}"}
        for c in ("false", "true") for f in ["empty"] + list(FILLS) for lit in ("false", "true") for sup in ("none", "tripod", "bar_x", "bar_z", "fire")}})
    W("assets/ryancraft/items/cooking_pot.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/cooking_pot_none_empty"}})
    for k in KINDS: W(f"assets/ryancraft/models/item/meal_{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/meal_{k}"}})
    W("assets/ryancraft/items/meal.json", {"model": {"type": "minecraft:select", "property": "minecraft:custom_model_data", "index": 0,
        "cases": [{"when": k, "model": {"type": "minecraft:model", "model": f"ryancraft:item/meal_{k}"}} for k in KINDS],
        "fallback": {"type": "minecraft:model", "model": "ryancraft:item/meal_stew"}}})
    loot = M.drops("cooking_pot")                                              # a pot in the fire gives back the campfire too
    loot["pools"].append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:campfire"}], "conditions": [
        {"condition": "minecraft:block_state_property", "block": "ryancraft:cooking_pot", "properties": {"support": "fire"}}]})
    W("data/ryancraft/loot_table/blocks/cooking_pot.json", loot)
    W("data/ryancraft/recipe/cooking_pot.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["I I", "I I", "III"],
        "key": {"I": "minecraft:iron_ingot"}, "result": {"id": "ryancraft:cooking_pot", "count": 1}})
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:cooking_pot"])

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("cooking ok")
