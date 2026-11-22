"""Moobloom + buttercup assets: the moobloom texture (the base game's cow recoloured buttercup yellow), the buttercup
flower, its block/item models, loot, recipe, tags and lang. Run before tools/resources.py (which merges lang_entries)."""
import colorsys, json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

def moobloom_texture():
    cow = Image.open(os.path.join(HERE, "template_cow.png")).convert("RGBA"); out = cow.copy(); px = out.load()
    for y in range(cow.height):
        for x in range(cow.width):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if 22 <= x < 26 and y < 6: continue                                  # horns stay
            if s > 0.25 and (h < 0.1 or h > 0.9) and r > 200: continue             # pink nose and udder stay
            if v < 0.12: continue                                                  # eyes
            if s < 0.12 and v > 0.45:                                              # grey patches -> cream
                k = v / 0.7; c = (int(min(255, 250 * k)), int(min(255, 240 * k)), int(min(255, 205 * k)))
            elif s >= 0.12:                                                        # brown hide -> buttercup yellow
                k = (v / 0.27) ** 0.8; c = colorsys.hsv_to_rgb(0.125, 0.78, min(1, 0.82 * k)); c = tuple(int(255 * t) for t in c)
            else: continue
            px[x, y] = c + (a,)
    out.save(os.path.join(A, "textures", "entity", "moobloom.png"))

BUTTERCUP = [
    "................",
    "................",
    "...YY......YY...",
    "..YHYY....YHYY..",
    "..YYOY....YYOY..",
    "...YY......YY...",
    "....G.....G.....",
    "....G..YY.G.....",
    ".....GYHYYG.....",
    ".....GYYOYG.....",
    "......GYYG......",
    "...L...GG...L...",
    "....L..G...L....",
    ".....L.G..L.....",
    "......LGGL......",
    ".......G........"]
COL = {"Y": (246, 206, 34), "H": (255, 246, 160), "O": (214, 150, 20), "G": (74, 120, 38), "L": (96, 150, 52)}

def buttercup():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); r = random.Random(4)
    for y, row in enumerate(BUTTERCUP):
        for x, ch in enumerate(row):
            if ch in COL: img.putpixel((x, y), tuple(max(0, min(255, v + r.randint(-6, 6))) for v in COL[ch]) + (255,))
    img.save(os.path.join(A, "textures", "block", "buttercup.png"))

def lang_entries():
    return {"entity.ryancraft.moobloom": "Moobloom", "item.ryancraft.moobloom_spawn_egg": "Moobloom Spawn Egg",
            "block.ryancraft.buttercup": "Buttercup", "block.ryancraft.potted_buttercup": "Potted Buttercup"}

if __name__ == "__main__":
    moobloom_texture(); buttercup()
    w(f"{A}/blockstates/buttercup.json", {"variants": {"": {"model": "ryancraft:block/buttercup"}}})
    w(f"{A}/models/block/buttercup.json", {"parent": "minecraft:block/cross", "textures": {"cross": "ryancraft:block/buttercup"}})
    w(f"{A}/blockstates/potted_buttercup.json", {"variants": {"": {"model": "ryancraft:block/potted_buttercup"}}})
    w(f"{A}/models/block/potted_buttercup.json", {"parent": "minecraft:block/flower_pot_cross", "textures": {"plant": "ryancraft:block/buttercup"}})
    w(f"{A}/models/item/buttercup.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:block/buttercup"}})
    w(f"{A}/items/buttercup.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/buttercup"}})
    surv = [{"condition": "minecraft:survives_explosion"}]
    w(f"{D}/ryancraft/loot_table/blocks/buttercup.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:buttercup"}], "conditions": surv}]})
    w(f"{D}/ryancraft/loot_table/blocks/potted_buttercup.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:flower_pot"}], "conditions": surv},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:buttercup"}], "conditions": surv}]})
    w(f"{D}/ryancraft/recipe/yellow_dye_from_buttercup.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "group": "yellow_dye",
        "ingredients": ["ryancraft:buttercup"], "result": {"id": "minecraft:yellow_dye", "count": 1}})
    for tag in ("small_flowers", "flowers"):
        w(f"{D}/minecraft/tags/item/{tag}.json", {"replace": False, "values": ["ryancraft:buttercup"]})
    w(f"{D}/minecraft/tags/block/small_flowers.json", {"replace": False, "values": ["ryancraft:buttercup"]})
    w(f"{D}/minecraft/tags/block/flower_pots.json", {"replace": False, "values": ["ryancraft:potted_buttercup"]})
    print("flora ok")
