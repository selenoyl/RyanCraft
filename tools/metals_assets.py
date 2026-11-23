"""The new metals (metal/Metals.java): silver, lead, tin, zinc, aluminum, uranium. Textures recoloured from the classic
1.2.5 iron ore, ingot and block (and vanilla's raw iron, nugget and deepslate ore, which 1.2.5 lacks); models, loot,
smelting and crafting, tags, ore veins and lang.
  python3 tools/metals_assets.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); TEX = os.path.join(A, "textures")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures"); TPL = os.path.join(HERE, "templates", "metals")

# metal: title, palette dark -> light, needs iron tool, veins (count, size, min y, max y, shape)
METALS = {
    "silver":   ("Silver",   [(78, 84, 92), (150, 156, 166), (208, 212, 220), (248, 250, 255)], True,  (4, 7, -48, 32, "trapezoid")),
    "lead":     ("Lead",     [(40, 42, 58), (74, 78, 100), (114, 118, 142), (162, 166, 190)],  False, (6, 9, -32, 64, "trapezoid")),
    "tin":      ("Tin",      [(104, 104, 98), (164, 164, 156), (210, 208, 200), (240, 240, 232)], False, (10, 9, 0, 96, "trapezoid")),
    "zinc":     ("Zinc",     [(84, 100, 100), (134, 156, 154), (182, 202, 198), (224, 238, 234)], False, (6, 8, -16, 64, "trapezoid")),
    "aluminum": ("Aluminum", [(126, 130, 138), (184, 188, 194), (220, 224, 228), (250, 250, 252)], False, (5, 8, 32, 112, "uniform")),
    "uranium":  ("Uranium",  [(34, 62, 26), (66, 116, 38), (116, 178, 58), (184, 232, 104)],   True,  (2, 4, -64, -16, "uniform")),
}

def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(obj, open(p, "w"), indent=2)

def add_tag(rel, values):
    p = os.path.join(RES, rel)
    tag = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
    tag["values"] += [v for v in values if v not in tag["values"]]; w(rel, tag)

def grad(t, pal):
    i = min(len(pal) - 2, int(t * (len(pal) - 1))); f = t * (len(pal) - 1) - i
    return tuple(int(pal[i][k] + (pal[i + 1][k] - pal[i][k]) * f) for k in range(3))

def recolour(src, pal, mask=None):
    """Gradient-map a texture (or only the masked pixels) onto the palette by brightness."""
    im = Image.open(src).convert("RGBA").crop((0, 0, 16, 16)) if isinstance(src, str) else src; px = im.load()
    pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3] and (mask is None or (x, y) in mask)]
    vals = [sum(px[x, y][:3]) / 3 for x, y in pts]
    lo, hi = (min(vals), max(vals)) if vals else (0, 255)
    for x, y in pts:
        c = px[x, y]; px[x, y] = grad((sum(c[:3]) / 3 - lo) / max(1, hi - lo), pal) + (c[3],)
    return im

def ore(stone, ore_tpl, pal):
    """The template ore's spots, recoloured to the metal, on the plain stone."""
    s, o = Image.open(stone).convert("RGBA").crop((0, 0, 16, 16)), Image.open(ore_tpl).convert("RGBA").crop((0, 0, 16, 16))
    spots = {(x, y) for y in range(16) for x in range(16) if abs(sum(s.getpixel((x, y))[:3]) - sum(o.getpixel((x, y))[:3])) > 24}
    out = s.copy(); po = out.load(); ro = recolour(o.copy(), pal, spots).load()
    for x, y in spots: po[x, y] = ro[x, y]
    return out

def lang_entries():
    out = {}
    for m, (t, *_r) in METALS.items():
        out.update({f"block.ryancraft.{m}_ore": f"{t} Ore", f"block.ryancraft.deepslate_{m}_ore": f"Deepslate {t} Ore",
                    f"block.ryancraft.raw_{m}_block": f"Block of Raw {t}", f"block.ryancraft.{m}_block": f"Block of {t}",
                    f"item.ryancraft.raw_{m}": f"Raw {t}", f"item.ryancraft.{m}_ingot": f"{t} Ingot", f"item.ryancraft.{m}_nugget": f"{t} Nugget"})
    return out

def build(m):
    title, pal, iron_tool, (count, size, lo, hi, shape) = METALS[m]
    raw_pal = [tuple(int(c * 0.82) for c in p) for p in pal]
    B, I = os.path.join(TEX, "block"), os.path.join(TEX, "item")
    ore(os.path.join(CLASSIC, "block", "stone.png"), os.path.join(CLASSIC, "block", "iron_ore.png"), pal).save(os.path.join(B, f"{m}_ore.png"))
    ore(os.path.join(TPL, "deepslate.png"), os.path.join(TPL, "deepslate_iron_ore.png"), pal).save(os.path.join(B, f"deepslate_{m}_ore.png"))
    recolour(os.path.join(TPL, "raw_iron_block.png"), raw_pal).save(os.path.join(B, f"raw_{m}_block.png"))
    recolour(os.path.join(CLASSIC, "block", "iron_block.png"), pal).save(os.path.join(B, f"{m}_block.png"))
    recolour(os.path.join(TPL, "raw_iron.png"), raw_pal).save(os.path.join(I, f"raw_{m}.png"))
    recolour(os.path.join(CLASSIC, "item", "iron_ingot.png"), pal).save(os.path.join(I, f"{m}_ingot.png"))
    recolour(os.path.join(TPL, "iron_nugget.png"), pal).save(os.path.join(I, f"{m}_nugget.png"))
    blocks = [f"{m}_ore", f"deepslate_{m}_ore", f"raw_{m}_block", f"{m}_block"]
    for b in blocks:
        w(f"assets/ryancraft/blockstates/{b}.json", {"variants": {"": {"model": f"ryancraft:block/{b}"}}})
        w(f"assets/ryancraft/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{b}"}})
        w(f"assets/ryancraft/items/{b}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{b}"}})
    for it in (f"raw_{m}", f"{m}_ingot", f"{m}_nugget"):
        w(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{it}"}})
        w(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{it}"}})
    L = "data/ryancraft/loot_table/blocks/"
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    for o in (f"{m}_ore", f"deepslate_{m}_ore"):
        w(L + o + ".json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"ryancraft:{o}", "conditions": [silk]},
            {"type": "minecraft:item", "name": f"ryancraft:raw_{m}", "functions": [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:ore_drops"}, {"function": "minecraft:explosion_decay"}]}]}]}]})
    for b in (f"raw_{m}_block", f"{m}_block"):
        w(L + b + ".json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{b}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    R = "data/ryancraft/recipe/"
    for src, name in ((f"raw_{m}", f"{m}_ingot"), (f"{m}_ore", f"{m}_ingot_from_ore"), (f"deepslate_{m}_ore", f"{m}_ingot_from_deepslate_ore")):
        for t, tm, suf in (("smelting", 200, ""), ("blasting", 100, "_blasting")):
            w(f"{R}{name}{suf}.json", {"type": f"minecraft:{t}", "category": "misc", "ingredient": f"ryancraft:{src}",
              "result": {"id": f"ryancraft:{m}_ingot"}, "experience": 0.7, "cookingtime": tm})
    nine = lambda out, inp, n: w(f"{R}{out}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["###", "###", "###"],
        "key": {"#": f"ryancraft:{inp}"}, "result": {"id": f"ryancraft:{n}", "count": 1}})
    back = lambda out, inp, n, cnt: w(f"{R}{out}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [f"ryancraft:{inp}"],
        "result": {"id": f"ryancraft:{n}", "count": cnt}})
    nine(f"{m}_block", f"{m}_ingot", f"{m}_block"); back(f"{m}_ingot_from_block", f"{m}_block", f"{m}_ingot", 9)
    nine(f"raw_{m}_block", f"raw_{m}", f"raw_{m}_block"); back(f"raw_{m}_from_block", f"raw_{m}_block", f"raw_{m}", 9)
    nine(f"{m}_ingot_from_nuggets", f"{m}_nugget", f"{m}_ingot"); back(f"{m}_nugget", f"{m}_ingot", f"{m}_nugget", 9)
    names = [f"ryancraft:{b}" for b in blocks]
    add_tag("data/minecraft/tags/block/mineable/pickaxe.json", names)
    add_tag(f"data/minecraft/tags/block/{'needs_iron_tool' if iron_tool else 'needs_stone_tool'}.json", names)
    if m == "silver":
        add_tag("data/minecraft/tags/block/beacon_base_blocks.json", ["ryancraft:silver_block"])
        add_tag("data/minecraft/tags/item/beacon_payment_items.json", ["ryancraft:silver_ingot"])
    D = "data/ryancraft/worldgen/"
    targets = [{"state": {"Name": f"ryancraft:{m}_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
               {"state": {"Name": f"ryancraft:deepslate_{m}_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]
    w(D + f"configured_feature/ore_{m}.json", {"type": "minecraft:ore", "config": {"size": size, "discard_chance_on_air_exposure": 0.0, "targets": targets}})
    height = {"type": f"minecraft:{shape}", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}
    w(D + f"placed_feature/ore_{m}.json", {"feature": f"ryancraft:ore_{m}", "placement": [{"type": "minecraft:count", "count": count},
        {"type": "minecraft:in_square"}, {"type": "minecraft:height_range", "height": height}, {"type": "minecraft:biome"}]})
    if m == "lead":   # the richest lead lies under swamps
        w(D + "placed_feature/ore_lead_swamp.json", {"feature": "ryancraft:ore_lead", "placement": [{"type": "minecraft:count", "count": 8},
            {"type": "minecraft:in_square"}, {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -16},
             "max_inclusive": {"absolute": 60}}}, {"type": "minecraft:biome"}]})

if __name__ == "__main__":
    for m in METALS: build(m)
    p = os.path.join(A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("metals ok")
