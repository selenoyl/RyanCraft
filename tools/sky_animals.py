"""Skylands animals (entity/Pegasus.java, entity/CloudSheep.java): the pegasus's feathered wing texture, the cloud
sheep's puff-wool texture (the classic fleece, whitened and lumped like a cloud; tinted in game to the wool colour),
spawn eggs, loot tables and lang.
  python3 tools/sky_animals.py"""
import json, os, random
from PIL import Image
import metals_assets as MA, moon_assets as M
from critters import egg_icon, save_item

HERE = os.path.dirname(os.path.abspath(__file__)); ENT = os.path.join(MA.TEX, "entity")
CLASSIC_WOOL = os.path.join(MA.CLASSIC, "entity", "sheep", "sheep_wool.png")
W_WHITE, W_LIGHT, W_SHADE, W_SHAFT = (248, 249, 252), (228, 232, 240), (198, 206, 222), (172, 184, 206)
EGGS = {"pegasus": ("#f4f4f8", "#9cc8f0"), "cloud_sheep": ("#ffffff", "#b8d8f4")}

def wing_face(flip=False):
    """18 x 11, tip at u=0 (outer end), leading edge at v=0: scalloped coverts in front, long flight feathers behind
    with a ragged trailing edge; primaries at the tip are longest."""
    f = Image.new("RGBA", (18, 11), (0, 0, 0, 0)); px = f.load()
    for u in range(18):
        reach = 10 if u < 7 else 9 if u < 13 else 8                   # how far back each feather reaches
        for v in range(11):
            if v > reach: continue
            if v <= 3:                                                # coverts: rows of small rounded feathers
                c = W_LIGHT if (u + (v % 2) * 2) % 4 == 3 or v == 3 else W_WHITE
            else:                                                     # flight feathers, two pixels wide each
                c = W_SHAFT if u % 2 == 1 and v < reach else W_WHITE if v < reach - 1 else W_LIGHT
                if u % 2 == 1 and v == reach: continue                # notches between feather tips
            px[u, v] = c + (255,)
    return f.transpose(Image.FLIP_LEFT_RIGHT) if flip else f

def wings():
    tex = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for y0, flip in ((0, False), (12, True)):                         # right wing (tip at u=0), left wing (mirrored)
        top = wing_face(flip).transpose(Image.FLIP_TOP_BOTTOM)          # the model maps texture row 0 to the trailing edge
        under = Image.eval(top, lambda c: c)                           # underside a touch greyer
        upx = under.load()
        for y in range(11):
            for x in range(18):
                r, g, b, a = upx[x, y]
                if a: upx[x, y] = (int(r * 0.9), int(g * 0.92), int(b * 0.96), a)
        tex.paste(top, (11, y0)); tex.paste(under, (29, y0))
        for x0, w in ((0, 11), (11, 18), (29, 11), (40, 18)):          # the 1-pixel edges
            for x in range(x0, x0 + w): tex.putpixel((x, y0 + 11), W_SHADE + (255,))
    return tex

def puff():
    """The classic 1.2.5 fleece curls, brightened and cooled to a cloud white (draft B)."""
    src = Image.open(CLASSIC_WOOL).convert("RGBA"); px = src.load(); out = Image.new("RGBA", src.size); po = out.load()
    for y in range(src.height):
        for x in range(src.width):
            c = px[x, y]
            if not c[3]: continue
            v = 0.80 + 0.22 * (sum(c[:3]) / 3 / 255)
            po[x, y] = (min(255, int(246 * v)), min(255, int(249 * v)), min(255, int(255 * v)), 255)
    return out

def loot():
    looting = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}
    MA.w("data/ryancraft/loot_table/entities/pegasus.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:leather", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}, looting]}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:feather", "functions": [      # and a couple of feathers
            {"function": "minecraft:set_count", "count": 2}, looting]}]}]})
    MA.w("data/ryancraft/loot_table/entities/cloud_sheep.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:mutton", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
        {"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this",
         "predicate": {"flags": {"is_on_fire": True}}}]}, looting]}]}]})

LANG = {"entity.ryancraft.pegasus": "Pegasus", "entity.ryancraft.cloud_sheep": "Cloud Sheep",
        "item.ryancraft.pegasus_spawn_egg": "Pegasus Spawn Egg", "item.ryancraft.cloud_sheep_spawn_egg": "Cloud Sheep Spawn Egg"}

if __name__ == "__main__":
    wings().save(os.path.join(ENT, "pegasus_wings.png")); puff().save(os.path.join(ENT, "cloud_sheep_puff.png"))
    for mob, (b, s) in EGGS.items():
        save_item(f"{mob}_spawn_egg", egg_icon(b, s))
        MA.w(f"assets/ryancraft/models/item/{mob}_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{mob}_spawn_egg"}})
        MA.w(f"assets/ryancraft/items/{mob}_spawn_egg.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{mob}_spawn_egg"}})
    loot()
    p = os.path.join(MA.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(LANG); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("sky animals ok")


# ---------------------------------------------------------------- sky roc and its eggs (added with the nests)
import colorsys
def roc_texture():
    """The bald eagle's texture recoloured: brown body to cloud white and pale grey, darkest feathers storm-grey,
    white head kept, yellow beak and feet to deep gold."""
    src = Image.open(os.path.join(ENT, "bald_eagle.png")).convert("RGBA"); px = src.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s > 0.45 and 0.09 < h < 0.18 and v > 0.5: px[x, y] = (226, 172, 40, a)               # beak, feet -> gold
            elif v < 0.12:                                                                        # eyes sit in the white head
                if any(0 <= x + dx < src.width and 0 <= y + dy < src.height and sum(px[x + dx, y + dy][:3]) > 600
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))): continue
                px[x, y] = (110, 118, 138, a)
            elif s > 0.18 and h < 0.12: t = min(1, v / 0.55); px[x, y] = (int(150 + 98 * t), int(158 + 94 * t), int(176 + 78 * t), a)  # brown -> cloud
            elif v < 0.3: px[x, y] = (96, 104, 122, a)                                             # darkest -> storm grey
    return src

EGG_BASE, EGG_SPECK, EGG_CRACK = (240, 238, 228), (150, 176, 206), (120, 110, 96)
def roc_egg(stage):
    r = random.Random(41); im = Image.new("RGBA", (16, 16), EGG_BASE + (255,)); px = im.load()
    for y in range(16):
        for x in range(16):
            k = 0.95 + 0.07 * ((x * 7 + y * 13) % 5) / 4
            px[x, y] = tuple(int(c * k) for c in EGG_BASE) + (255,)
    for _ in range(18): px[r.randrange(16), r.randrange(16)] = EGG_SPECK + (255,)
    cracks = [[(7, 2), (8, 3), (7, 4), (8, 5), (9, 6)], [(3, 8), (4, 9), (5, 9), (6, 10), (6, 11), (11, 9), (12, 10), (11, 11)]]
    for i in range(stage):
        for x, y in cracks[i]: px[x, y] = EGG_CRACK + (255,)
    return im

def roc_assets():
    roc_texture().save(os.path.join(ENT, "sky_roc.png"))
    B = os.path.join(MA.TEX, "block")
    states = {}
    for h in range(3):
        roc_egg(h).save(os.path.join(B, f"roc_egg_{h}.png"))
        faces = {f: {"uv": [4, 3, 12, 13] if f not in ("up", "down") else [4, 4, 12, 12], "texture": "#egg"} for f in ("north", "south", "east", "west", "up", "down")}
        MA.w(f"assets/ryancraft/models/block/roc_egg_{h}.json", {"parent": "minecraft:block/block", "textures": {"egg": f"ryancraft:block/roc_egg_{h}",
            "particle": f"ryancraft:block/roc_egg_{h}"}, "elements": [
            {"from": [4, 0, 4], "to": [12, 10, 12], "faces": faces},
            {"from": [5, 10, 5], "to": [11, 12, 11], "faces": {f: {"uv": [5, 1, 11, 3] if f not in ("up", "down") else [5, 5, 11, 11], "texture": "#egg"}
                                                             for f in ("north", "south", "east", "west", "up")}}]})
        states[f"hatch={h}"] = {"model": f"ryancraft:block/roc_egg_{h}"}
    MA.w("assets/ryancraft/blockstates/roc_egg.json", {"variants": states})
    MA.w("assets/ryancraft/items/roc_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/roc_egg_0"}})
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    MA.w("data/ryancraft/loot_table/blocks/roc_egg.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "ryancraft:roc_egg"}], "conditions": [silk]}]})
    looting = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}
    MA.w("data/ryancraft/loot_table/entities/sky_roc.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:feather", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 5}}, looting]}]}]})
    save_item("sky_roc_spawn_egg", egg_icon("#f2f4f8", "#e2ac28"))
    MA.w("assets/ryancraft/models/item/sky_roc_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/sky_roc_spawn_egg"}})
    MA.w("assets/ryancraft/items/sky_roc_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/sky_roc_spawn_egg"}})
    LANG.update({"entity.ryancraft.sky_roc": "Sky Roc", "item.ryancraft.sky_roc_spawn_egg": "Sky Roc Spawn Egg", "block.ryancraft.roc_egg": "Roc Egg"})
    p = os.path.join(MA.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(LANG); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    roc_assets(); print("roc ok")
