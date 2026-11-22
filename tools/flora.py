"""The moobloom texture: the base game's mooshroom recoloured sunny yellow (it wears the old-school dandelions)."""
import colorsys, json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

def moobloom_texture():
    """The base game's mooshroom (its eyes and face) recoloured: sunny yellow hide, cream patches; horns and pink nose kept."""
    src = Image.open(os.path.join(HERE, "template_mooshroom.png")).convert("RGBA"); out = src.copy(); px = out.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if v < 0.12: continue                                                  # eyes, nostrils
            if s > 0.55 and (h < 0.06 or h > 0.94):                                # red hide -> sunny yellow
                k = max(0.86, min(1.08, 0.95 + 0.6 * (v / 0.72 - 1)))
                c = tuple(int(255 * t) for t in colorsys.hsv_to_rgb(0.14, 0.88, min(1, 0.92 * k)))
            elif s < 0.12 and v > 0.4:                                             # grey patches -> cream
                k = max(0.95, min(1.04, 0.98 + 0.3 * (v / 0.68 - 1)))
                c = (int(min(255, 246 * k)), int(min(255, 234 * k)), int(min(255, 168 * k)))
            else: continue                                                         # horns, pink nose and udder
            px[x, y] = c + (a,)
    out.save(os.path.join(A, "textures", "entity", "moobloom.png"))

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

def rose():
    """The original 1.2.5 rose: the classic pack's rose tile (shipped as its poppy), copied as the rose's own texture so it
    keeps the old look whatever packs are on."""
    classic = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block", "poppy.png")
    Image.open(classic).convert("RGBA").save(os.path.join(A, "textures", "block", "rose.png"))
    w(f"{A}/blockstates/rose.json", {"variants": {"": {"model": "ryancraft:block/rose"}}})
    w(f"{A}/models/block/rose.json", {"parent": "minecraft:block/cross", "textures": {"cross": "ryancraft:block/rose"}})
    w(f"{A}/blockstates/potted_rose.json", {"variants": {"": {"model": "ryancraft:block/potted_rose"}}})
    w(f"{A}/models/block/potted_rose.json", {"parent": "minecraft:block/flower_pot_cross", "textures": {"plant": "ryancraft:block/rose"}})
    w(f"{A}/models/item/rose.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:block/rose"}})
    w(f"{A}/items/rose.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/rose"}})
    surv = [{"condition": "minecraft:survives_explosion"}]
    w(f"{D}/ryancraft/loot_table/blocks/rose.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:rose"}], "conditions": surv}]})
    w(f"{D}/ryancraft/loot_table/blocks/potted_rose.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:flower_pot"}], "conditions": surv},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:rose"}], "conditions": surv}]})
    # 1 rose bush -> 2 roses -> 2 red dye, the same as the bush's own vanilla recipe (rose bush -> 2 red dye)
    w(f"{D}/ryancraft/recipe/rose_from_rose_bush.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["minecraft:rose_bush"], "result": {"id": "ryancraft:rose", "count": 2}})
    w(f"{D}/ryancraft/recipe/red_dye_from_rose.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "group": "red_dye",
        "ingredients": ["ryancraft:rose"], "result": {"id": "minecraft:red_dye", "count": 1}})
    for tag in ("small_flowers", "flowers"):
        w(f"{D}/minecraft/tags/item/{tag}.json", {"replace": False, "values": ["ryancraft:rose"]})
    w(f"{D}/minecraft/tags/block/small_flowers.json", {"replace": False, "values": ["ryancraft:rose"]})
    w(f"{D}/minecraft/tags/block/flower_pots.json", {"replace": False, "values": ["ryancraft:potted_rose"]})

def lang_entries():
    return {"block.ryancraft.rose": "Rose", "block.ryancraft.potted_rose": "Potted Rose"}

if __name__ == "__main__":
    moobloom_texture(); rose()
    print("flora ok")
