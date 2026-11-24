"""Boats for our woods (trees/RyanBoats.java): vanilla's oak boat and chest boat (entity textures), the classic oak boat
item icon and vanilla's chest boat icon, each recoloured to a wood's planks by matching brightness ranks (the darkest
wood pixel of the oak boat takes the darkest plank colour, and so on), so every boat is its own planks. Item models,
recipes (vanilla's), boat tags and lang.
  python3 tools/boats.py      (tools/resources.py merges the lang)"""
import colorsys, json, os
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); TPL = os.path.join(HERE, "templates", "boats")
SPECIES = ["white_oak", "red_oak", "sugar_maple", "red_maple", "tulip_poplar", "shagbark_hickory", "black_walnut", "american_beech",
           "sycamore", "eastern_redbud", "flowering_dogwood", "eastern_red_cedar", "black_cherry", "sassafras", "ohio_buckeye", "sky_willow"]

def lum(c): return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]

def is_wood(c):
    """Oak-brown pixels (not the chest's iron latch, not the black outline)."""
    h, s, v = colorsys.rgb_to_hsv(*[k / 255 for k in c[:3]])
    return c[3] and s > 0.18 and v > 0.12 and (h < 0.16 or h > 0.95)

def recolour(template, planks):
    im = Image.open(template).convert("RGBA"); px = im.load()
    pal = sorted((p[:3] for p in Image.open(planks).convert("RGBA").getdata() if p[3]), key=lum)
    wood = [(x, y) for y in range(im.height) for x in range(im.width) if is_wood(px[x, y])]
    order = sorted(lum(px[x, y]) for x, y in wood)
    def rank(l):
        lo, hi = 0, len(order)
        while lo < hi:
            m = (lo + hi) // 2
            if order[m] < l: lo = m + 1
            else: hi = m
        return lo / max(1, len(order) - 1)
    for x, y in wood:
        c = pal[min(len(pal) - 1, int(rank(lum(px[x, y])) * (len(pal) - 1)))]
        px[x, y] = c + (px[x, y][3],)
    return im

def lang_entries():
    t = lambda s: " ".join(w.capitalize() for w in s.split("_"))
    out = {}
    for s in SPECIES:
        out[f"item.ryancraft.{s}_boat"] = f"{t(s)} Boat"; out[f"item.ryancraft.{s}_chest_boat"] = f"{t(s)} Boat with Chest"
        out[f"entity.ryancraft.{s}_boat"] = f"{t(s)} Boat"; out[f"entity.ryancraft.{s}_chest_boat"] = f"{t(s)} Boat with Chest"
    return out

def main():
    T = M.TEX; W = M.w
    for s in SPECIES:
        planks = os.path.join(T, "block", s + "_planks.png")
        for sub, tpl in (("boat", "oak_boat_entity.png"), ("chest_boat", "oak_chest_boat_entity.png")):
            os.makedirs(os.path.join(T, "entity", sub), exist_ok=True)
            recolour(os.path.join(TPL, tpl), planks).save(os.path.join(T, "entity", sub, s + ".png"))
        recolour(os.path.join(TPL, "classic_oak_boat_item.png"), planks).save(os.path.join(T, "item", s + "_boat.png"))
        recolour(os.path.join(TPL, "oak_chest_boat_item.png"), planks).save(os.path.join(T, "item", s + "_chest_boat.png"))
        for it in (s + "_boat", s + "_chest_boat"):
            W(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{it}"}})
            W(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{it}"}})
        W(f"data/ryancraft/recipe/{s}_boat.json", {"type": "minecraft:crafting_shaped", "category": "misc", "group": "boat",
            "pattern": ["# #", "###"], "key": {"#": f"ryancraft:{s}_planks"}, "result": {"id": f"ryancraft:{s}_boat", "count": 1}})
        W(f"data/ryancraft/recipe/{s}_chest_boat.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "group": "chest_boat",
            "ingredients": ["minecraft:chest", f"ryancraft:{s}_boat"], "result": {"id": f"ryancraft:{s}_chest_boat", "count": 1}})
    M.add_tag("data/minecraft/tags/item/boats.json", [f"ryancraft:{s}_boat" for s in SPECIES])
    M.add_tag("data/minecraft/tags/item/chest_boats.json", [f"ryancraft:{s}_chest_boat" for s in SPECIES])
    M.add_tag("data/minecraft/tags/entity_type/boat.json", [f"ryancraft:{s}_boat" for s in SPECIES] + [f"ryancraft:{s}_chest_boat" for s in SPECIES])
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries())
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2, ensure_ascii=False); open(p, "a").write("\n")
    print("boats ok")

if __name__ == "__main__":
    main()
