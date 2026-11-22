"""Fishing loop: the trophy fish plaque, the Angler's Logbook, and the fishing loot table split into
freshwater (our fish, plus salmon) and ocean (vanilla's sea fish). tools/resources.py merges the lang."""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "item")

def lang_entries():
    return {"block.ryancraft.fish_plaque": "Fish Plaque", "item.ryancraft.anglers_logbook": "Angler's Logbook"}

def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def plaque_texture():
    """Dark stained oak board (on the oak plank template) with a lighter routed border."""
    src = Image.open(os.path.join(HERE, "template_oak_planks.png")).convert("RGB"); im = Image.new("RGBA", (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b = src.getpixel((x, y)); k = (r + g + b) / 3 / 160
            border = x in (1, 14) or y in (3, 12)
            inside = 1 < x < 14 and 3 < y < 12
            base = (150, 104, 62) if border else (92, 60, 36)
            if not (border or inside): base = (110, 74, 44)
            px[x, y] = tuple(max(0, min(255, int(c * k))) for c in base) + (255,)
    return im

def logbook_texture():
    """The classic book, rebound in river-blue leather, with a little fish on the cover."""
    im = Image.open(os.path.join(CLASSIC, "book.png")).convert("RGBA"); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a and r > g + 25 and r > b + 25:                  # the red-brown leather
                l = (r + g + b) / 3 / 110
                px[x, y] = (int(46 * l), int(96 * l), int(128 * l), a)
    for (x, y), c in {(6, 7): (220, 200, 120), (7, 7): (220, 200, 120), (8, 7): (220, 200, 120), (7, 6): (220, 200, 120),
                      (7, 8): (220, 200, 120), (5, 6): (220, 200, 120), (5, 8): (220, 200, 120), (9, 7): (40, 40, 40)}.items():
        if px[x, y][3]: px[x, y] = c + (255,)
    return im

FRESH = [("ryancraft:bluegill", 12), ("ryancraft:crappie", 9), ("ryancraft:largemouth_bass", 8), ("ryancraft:yellow_perch", 9),
         ("ryancraft:smallmouth_bass", 6), ("ryancraft:channel_catfish", 5), ("ryancraft:rainbow_trout", 6), ("ryancraft:walleye", 4),
         ("ryancraft:northern_pike", 3), ("ryancraft:muskellunge", 1)]
SEA = [("minecraft:cod", 60), ("minecraft:salmon", 25), ("minecraft:tropical_fish", 2), ("minecraft:pufferfish", 13)]
SMELT = {"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this",
         "predicate": {"flags": {"is_on_fire": True}}}]}
OCEAN = {"condition": "minecraft:location_check", "predicate": {"biomes": "#minecraft:is_ocean"}}
NIGHT = {"condition": "minecraft:time_check", "value": {"min": 13000, "max": 23000}, "period": 24000}

def fish_table():
    def entry(name, weight, extra=None):
        e = {"type": "minecraft:item", "name": name, "weight": weight, "functions": ([{"function": "ryancraft:fish_length"}] if name.startswith("ryancraft") else []) + [SMELT]}
        if extra: e["conditions"] = extra
        return e
    sea = [entry(n, wt) for n, wt in SEA]
    fresh = [entry(n, wt) for n, wt in FRESH] + [entry("minecraft:salmon", 6),
             entry("ryancraft:channel_catfish", 10, [NIGHT]), entry("ryancraft:walleye", 4, [NIGHT])]   # catfish and walleye bite after dark
    return {"type": "minecraft:fishing", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
        {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:loot_table", "value": {"type": "minecraft:fishing", "pools": [{"rolls": 1, "entries": sea}]}, "conditions": [OCEAN]},
            {"type": "minecraft:loot_table", "value": {"type": "minecraft:fishing", "pools": [{"rolls": 1, "entries": fresh}]}}]}]}],
        "random_sequence": "minecraft:gameplay/fishing/fish"}

if __name__ == "__main__":
    plaque_texture().save(os.path.join(A, "textures", "block", "fish_plaque.png"))
    logbook_texture().save(os.path.join(A, "textures", "item", "anglers_logbook.png"))
    t = "ryancraft:block/fish_plaque"
    w("assets/ryancraft/models/block/fish_plaque.json", {"parent": "minecraft:block/block", "textures": {"particle": t, "board": t}, "elements": [
        {"from": [1, 3, 14], "to": [15, 13, 16], "faces": {
            "north": {"uv": [1, 3, 15, 13], "texture": "#board"}, "south": {"uv": [1, 3, 15, 13], "texture": "#board", "cullface": "south"},
            "east": {"uv": [14, 3, 16, 13], "texture": "#board"}, "west": {"uv": [0, 3, 2, 13], "texture": "#board"},
            "up": {"uv": [1, 14, 15, 16], "texture": "#board"}, "down": {"uv": [1, 0, 15, 2], "texture": "#board"}}}],
        "display": {"gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
                    "fixed": {"rotation": [0, 180, 0], "scale": [0.9, 0.9, 0.9]}}})
    w("assets/ryancraft/blockstates/fish_plaque.json", {"variants": {f"facing={f}": {"model": "ryancraft:block/fish_plaque", **({"y": y} if y else {})}
        for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    w("assets/ryancraft/items/fish_plaque.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/fish_plaque"}})
    w("assets/ryancraft/items/anglers_logbook.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/anglers_logbook"}})
    w("assets/ryancraft/models/item/anglers_logbook.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/anglers_logbook"}})
    w("data/ryancraft/loot_table/blocks/fish_plaque.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:fish_plaque"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w("data/ryancraft/recipe/fish_plaque.json", {"type": "minecraft:crafting_shaped", "category": "decorations", "pattern": ["SSS", "SFS"],
        "key": {"S": "#minecraft:wooden_slabs", "F": "minecraft:item_frame"}, "result": {"id": "ryancraft:fish_plaque", "count": 2}})
    w("data/ryancraft/recipe/anglers_logbook.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["minecraft:book", "#minecraft:fishes"], "result": {"id": "ryancraft:anglers_logbook", "count": 1}})
    p = os.path.join(D, "minecraft", "tags", "block", "mineable", "axe.json"); tag = json.load(open(p))
    if "ryancraft:fish_plaque" not in tag["values"]: tag["values"].append("ryancraft:fish_plaque"); json.dump(tag, open(p, "w"), indent=2)
    w("data/minecraft/loot_table/gameplay/fishing/fish.json", fish_table())
    print("fishing ok")
