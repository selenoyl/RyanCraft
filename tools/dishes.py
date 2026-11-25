"""Dishes from the garden crops (food/Dishes.java): icons drawn with tools/food_art.py's shading in the classic item
style, models, recipes (roasting in the furnace, smoker and campfire; crafting for the rest) and lang.
  python3 tools/dishes.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M
import food_art as F

I = os.path.join(M.TEX, "item")
TOAST = [(110, 64, 10), (180, 116, 20), (222, 162, 40), (244, 202, 90), (255, 236, 160)]
BREAD = [(96, 60, 24), (160, 104, 48), (200, 146, 78), (226, 186, 120), (248, 222, 170)]
MEAT = [(70, 30, 14), (118, 56, 28), (150, 80, 42), (180, 110, 64), (214, 156, 110)]
RICE = [(160, 150, 130), (210, 204, 186), (236, 232, 216), (248, 246, 236), (255, 255, 250)]
SQUASH_FLESH = [(160, 80, 10), (226, 132, 30), (244, 168, 56), (252, 200, 100), (255, 232, 170)]

def roasted_corn():
    im = F.corn(); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a and r > 150 and g > 100 and b < 120:                   # the kernels toasted golden, with charred spots
                px[x, y] = (min(255, int(r * 0.95)), int(g * 0.78), int(b * 0.5), 255)
                if (x * 7 + y * 3) % 11 == 0: px[x, y] = (110, 60, 16, 255)
    return im

def baked_squash():
    im = F.blank(); half = F.ellipse(8, 9, 6.6, 5.4)
    F.shade(im, half, [(96, 48, 10), (170, 92, 24), (206, 128, 44), (232, 166, 80), (250, 206, 130)], seed=30)
    flesh = F.ellipse(8, 8.4, 5.0, 3.8)
    F.shade(im, flesh, SQUASH_FLESH, seed=31, outline=False)
    pit = F.ellipse(8, 8.4, 2.4, 1.6)
    F.shade(im, pit, [(140, 70, 10), (196, 110, 30), (220, 140, 50), (236, 170, 80), (250, 210, 140)], seed=32, outline=False, glint=False)
    F.dots(im, {(7, 8), (9, 8), (8, 9)}, (250, 240, 210))                    # a few seeds
    F.dots(im, {(5, 6), (6, 6), (10, 5)}, (130, 70, 14))                     # browned from the oven
    return im

def stuffed_pepper():
    """A red pepper with its top cut off, heaped with rice and meat."""
    im = F.blank()
    body = F.ellipse(7.5, 10, 4.6, 5.0) | F.ellipse(7.5, 7.5, 5.0, 3.0)
    F.shade(im, body, [(100, 6, 6), (170, 14, 10), (214, 30, 20), (244, 66, 40), (255, 140, 110)], seed=33)
    fill = F.ellipse(7.5, 5.5, 4.2, 2.2)
    F.shade(im, fill, RICE, seed=34, glint=False)
    F.dots(im, {(6, 5), (9, 4), (8, 6), (5, 6)}, MEAT[2])
    F.dots(im, {(7, 4), (10, 5)}, MEAT[3])
    return im

def cabbage_roll():
    im = F.blank()
    for cx, cy, s in ((6, 6.5, 40), (10, 10.5, 41)):
        F.shade(im, F.ellipse(cx, cy, 4.6, 2.6, 35), [(40, 96, 30), (84, 150, 50), (128, 190, 80), (170, 222, 120), (222, 248, 190)], seed=s)
        F.shade(im, F.ellipse(cx + 3.2, cy + 2.2, 1.3, 1.3), MEAT, seed=s + 5)           # the filling at the end
        px = im.load()
        for k in range(-2, 3):                                                 # a vein across the leaf
            x, y = int(cx + k), int(cy - k * 0.5)
            if px[x, y][3] and px[x, y][:3] != (40, 96, 30): px[x, y] = (196, 236, 160, 255)
    return im

def sandwich():
    im = F.blank()
    F.shade(im, F.ellipse(8, 10, 7, 2.6, -15), BREAD, seed=40)                                              # bottom slice
    F.shade(im, F.ellipse(8, 8.6, 6.8, 1.6, -15), MEAT, seed=41, glint=False)                               # meat
    F.shade(im, F.ellipse(8, 7.8, 6.6, 1.2, -15), [(26, 74, 20), (60, 140, 40), (100, 180, 60), (140, 210, 90), (190, 236, 140)], seed=42, glint=False)
    F.dots(im, {(4, 8), (7, 7), (11, 7)}, (214, 40, 30))                                                    # tomato
    F.shade(im, F.ellipse(8, 5.8, 7, 2.6, -15), BREAD, seed=43)                                             # top slice
    return im

def garden_salad():
    """Leaves heaped in the classic bowl: they fill the bowl's opening where vanilla's stew sits and mound a little
    above it, with the front of the rim still in front of them."""
    C = os.path.join(M.CLASSIC, "item")
    bowl = Image.open(os.path.join(C, "bowl.png")).convert("RGBA"); stew = Image.open(os.path.join(C, "mushroom_stew.png")).convert("RGBA")
    pb, ps = bowl.load(), stew.load(); r = random.Random(50)
    inside = {(x, y) for y in range(16) for x in range(16) if ps[x, y][3] and ps[x, y] != pb[x, y]}
    top = min(y for _, y in inside)
    heap = {(x, y) for x, y in inside if y <= top + 2} | {(x, y) for x in range(5, 11) for y in (top - 1,)} | {(x, top - 2) for x in range(7, 10)}
    out = bowl.copy(); po = out.load()
    for x, y in sorted(heap, key=lambda p: p[1]):
        if (x, y) in inside or y < top:                                   # the mound rises in front of the back rim
            edge = (x, y - 1) not in heap
            po[x, y] = (F.LEAF[r.randint(3, 4)] if edge else F.LEAF[r.randint(1, 3)]) + (255,)
    for x, y in inside:                                                   # the far side of the bowl, in its shadow
        if y > top + 2: po[x, y] = F.LEAF[0] + (255,)
    for p in ((6, top), (9, top - 1), (10, top + 1)):
        if p in heap: po[p] = (214, 40, 30, 255)                          # tomato
    for p in ((8, top), (5, top + 1)):
        if p in heap: po[p] = (240, 236, 210, 255)                        # onion
    return out

def cornbread():
    im = F.blank(); m = {(x, y) for y in range(4, 13) for x in range(2, 14) if not ((x in (2, 13)) and (y in (4, 12)))}
    F.shade(im, m, [(130, 84, 14), (206, 150, 40), (236, 192, 70), (250, 220, 110), (255, 242, 170)], seed=60, speck=0.15)
    F.dots(im, {(x, 4) for x in range(3, 13)}, (180, 110, 24))              # a browned top edge
    return im

DISHES = {"roasted_corn": ("Roasted Corn", roasted_corn), "baked_squash": ("Baked Squash", baked_squash),
          "stuffed_pepper": ("Stuffed Pepper", stuffed_pepper), "cabbage_roll": ("Cabbage Roll", cabbage_roll),
          "sandwich": ("Sandwich", sandwich), "garden_salad": ("Garden Salad", garden_salad), "cornbread": ("Cornbread", cornbread)}
COOKED = ["minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken", "minecraft:cooked_mutton", "minecraft:cooked_rabbit",
          "ryancraft:cooked_venison", "ryancraft:bison_steak", "ryancraft:cooked_duck", "ryancraft:cooked_turkey"]

def lang_entries(): return {f"item.ryancraft.{k}": v[0] for k, v in DISHES.items()}

def build():
    W, R = M.w, "data/ryancraft/recipe/"
    for k, (_, f) in DISHES.items():
        f().save(os.path.join(I, f"{k}.png"))
        W(f"assets/ryancraft/models/item/{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{k}"}})
        W(f"assets/ryancraft/items/{k}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{k}"}})
    M.w("data/ryancraft/tags/item/cooked_meats.json", {"replace": False, "values": COOKED})
    for src, dst in (("corn", "roasted_corn"), ("squash", "baked_squash")):
        for kind, t in (("smelting", 200), ("smoking", 100), ("campfire_cooking", 600)):
            W(f"{R}{dst}_from_{kind}.json", {"type": f"minecraft:{kind}", "category": "food", "ingredient": f"ryancraft:{src}",
                "result": {"id": f"ryancraft:{dst}"}, "experience": 0.35, "cookingtime": t})
    sl = lambda name, ing, n=1: W(f"{R}{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ing,
        "result": {"id": f"ryancraft:{name}", "count": n}})
    sl("stuffed_pepper", ["ryancraft:pepper", "ryancraft:rice", "#ryancraft:cooked_meats"])
    sl("cabbage_roll", ["ryancraft:cabbage", "ryancraft:rice", "#ryancraft:cooked_meats"], 2)
    sl("sandwich", ["minecraft:bread", "#ryancraft:cooked_meats", ["ryancraft:cabbage", "ryancraft:tomato"]])
    sl("garden_salad", ["minecraft:bowl", "ryancraft:cabbage", "ryancraft:tomato", "ryancraft:onion"])
    sl("cornbread", ["ryancraft:corn", "ryancraft:corn", "ryancraft:corn", "minecraft:egg"], 2)

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("dishes ok")
