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
        top = wing_face(flip)
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
    """The classic fleece, whitened, with soft cloud lumps (light tops, faint blue-grey hollows)."""
    src = Image.open(CLASSIC_WOOL).convert("RGBA"); px = src.load()
    r = random.Random(91); out = Image.new("RGBA", src.size); po = out.load()
    lumps = [(r.uniform(0, 64), r.uniform(0, 32), r.uniform(1.8, 3.4)) for _ in range(70)]
    for y in range(src.height):
        for x in range(src.width):
            c = px[x, y]
            if not c[3]: continue
            best = min(((x - lx) ** 2 + (y - ly) ** 2) / (rr * rr) for lx, ly, rr in lumps)
            t = 1.0 - min(1.0, best) * 0.16 + (r.random() - 0.5) * 0.03
            base = (sum(c[:3]) / 3) / 255
            v = 0.86 + 0.14 * base
            po[x, y] = (min(255, int(250 * t * v)), min(255, int(252 * t * v)), min(255, int(255 * t * v + 4)), 255)
    return out

def loot():
    looting = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}
    MA.w("data/ryancraft/loot_table/entities/pegasus.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:leather", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}, looting]}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:feather", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}, looting]}]}]})
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
