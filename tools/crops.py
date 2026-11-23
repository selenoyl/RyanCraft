"""Garden crops (food/Crops.java): four growth stages each, drawn in the flat classic style, plus produce and seed
icons (both drawn by tools/food_art.py in the classic item style), block and item models, loot (seeds back at every stage, produce when grown), seed recipes and lang.
  python3 tools/crops.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M
import food_art as FA

B = os.path.join(M.TEX, "block"); I = os.path.join(M.TEX, "item")
G = {"d": (34, 92, 26), "g": (62, 138, 40), "l": (110, 180, 64), "s": (90, 120, 40)}      # dark leaf, leaf, light leaf, stem

def canvas(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))
def put(px, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16: px[int(x), int(y)] = c + (255,)

def stalks(px, r, n, h, lean=0.25, col=G["s"], leaf=True):
    """n upright stems h pixels tall, each with leaves off it."""
    for i in range(n):
        x = 2 + (12 * (i + 0.5) / n) + r.uniform(-1, 1); dx = r.uniform(-lean, lean)
        for k in range(h):
            put(px, x + dx * k, 15 - k, col)
            if leaf and k > 1 and k % 2 == 0:
                side = 1 if (k // 2 + i) % 2 else -1
                put(px, x + dx * k + side, 15 - k, G["g"]); put(px, x + dx * k + 2 * side, 14 - k, G["l"] if k % 4 else G["d"])

def blades(px, r, n, h, col_tip=None):
    for i in range(n):
        x = 1 + r.uniform(0, 14); dx = r.uniform(-0.4, 0.4); hh = int(h * r.uniform(0.7, 1.0))
        for k in range(hh): put(px, x + dx * k, 15 - k, G["g"] if k < hh - 2 else (col_tip or G["l"]))

def blobs(px, r, n, cols, area=(2, 13, 3, 12), size=1):
    for _ in range(n):
        x, y = r.randint(area[0], area[1]), r.randint(area[2], area[3])
        for ox in range(size + 1):
            for oy in range(size + 1): put(px, x + ox, y + oy, cols[0] if (ox + oy) else cols[1])

def stage(name, st):
    im = canvas(); px = im.load(); r = random.Random(name + str(st)); h = [4, 7, 10, 13][st]
    if name in ("tomato", "pepper", "beans"):                       # bushy plants on stems; fruit when grown
        stalks(px, r, 3, h)
        if st == 3:
            fruit = {"tomato": [(200, 36, 28), (250, 110, 90)], "pepper": [(196, 40, 24), (90, 170, 50)], "beans": [(92, 160, 52), (150, 210, 90)]}[name]
            blobs(px, r, 6, fruit, (2, 13, 4, 12), 1 if name == "tomato" else 0)
            if name == "pepper": blobs(px, r, 3, [(90, 160, 46), (130, 200, 70)], (2, 13, 4, 12), 0)
        if st == 2: blobs(px, r, 4, [(240, 220, 120), (255, 245, 180)], (3, 12, 5, 11), 0)            # flowers
    elif name == "corn":                                            # a tall stalk with long leaves; cobs and a tassel when grown
        stalks(px, r, 2, h + (2 if st == 3 else 0), 0.05, (110, 150, 50))
        blades(px, r, 4, h - 2)
        if st == 3:
            for x0 in (5, 10):
                for y in range(6, 10): put(px, x0, y, (236, 200, 60)); put(px, x0 + 1, y, (250, 230, 120))
                put(px, x0, 5, (110, 160, 60))
            for x in range(4, 12, 2): put(px, x, 0, (210, 180, 110))
    elif name == "rice":                                            # fine blades; golden drooping heads when grown
        blades(px, r, 9, h, (200, 180, 90) if st == 3 else None)
        if st == 3:
            for _ in range(5):
                x = r.randint(2, 13); put(px, x, 3, (226, 200, 110)); put(px, x + 1, 4, (226, 200, 110)); put(px, x + 1, 5, (200, 170, 80))
    elif name == "onion":                                           # thin upright leaves; the bulb shows at the bottom
        blades(px, r, 6, h)
        if st >= 2:
            for x0 in (4, 10):
                c = (200, 170, 120) if st == 3 else (220, 210, 170)
                for y in range(13, 16):
                    for x in range(x0 - 1, x0 + 2): put(px, x, y, c)
                put(px, x0, 12, (180, 150, 100))
    elif name == "cabbage":                                         # a rosette that closes into a head
        rad = [2, 3.5, 5, 6.5][st]
        for y in range(16):
            for x in range(16):
                d = ((x - 7.5) ** 2 + ((y - (15 - rad)) * 1.4) ** 2) ** 0.5
                if d <= rad: put(px, x, y, G["l"] if (st == 3 and d < rad * 0.6) else G["g"] if (x + y) % 3 else G["d"])
        if st == 3: blobs(px, r, 3, [(170, 220, 120), (200, 240, 160)], (5, 9, 9, 12), 0)
    elif name == "squash":                                          # broad leaves low on the ground; a squash when grown
        for _ in range(3 + st * 2):
            x, y = r.randint(1, 13), r.randint(16 - h, 14)
            for ox in range(3):
                for oy in range(2): put(px, x + ox, y + oy, G["g"] if (ox + oy) % 2 else G["d"])
        stalks(px, r, 2, max(3, h // 2), 0.6, leaf=False)
        if st == 3:
            for y in range(11, 15):
                for x in range(4, 11): put(px, x, y, (226, 140, 40) if (x + y) % 3 else (250, 186, 80))
            put(px, 7, 10, (100, 120, 40))
    return im

PAL = {"k": (40, 30, 20), "r": (200, 36, 28), "R": (250, 110, 90), "g": (62, 138, 40), "l": (110, 180, 64), "d": (34, 92, 26),
       "y": (236, 200, 60), "Y": (250, 230, 120), "o": (226, 140, 40), "O": (250, 186, 80), "w": (230, 220, 190), "W": (200, 170, 120),
       "b": (120, 90, 50), "c": (150, 210, 90), "s": (220, 210, 170), "p": (196, 40, 24), "P": (240, 90, 60)}
ICONS = {
    "tomato":  ["................", "................", "......g.g.......", ".....gdgdg......", "....krrgrrk.....", "...krRRrrrrk....", "..krRRrrrrrrk...",
                "..krRrrrrrrrk...", "..krrrrrrrrrk...", "..krrrrrrrrrk...", "...krrrrrrrk....", "....krrrrrk.....", ".....kkkkk......", "................", "................", "................"],
    "pepper":  ["................", "........g.......", ".......gd.......", "......kppk......", ".....kpPppk.....", ".....kPpppk.....", ".....kppppk.....",
                ".....kppppk.....", "......kpppk.....", "......kppk......", ".......kpk......", ".......kk.......", "................", "................", "................", "................"],
    "corn":    ["................", ".........gl.....", "........gYYl....", ".......gYyYl....", ".......gyYyl....", "......gYyYl.....", "......gyYyl.....",
                ".....gYyYl......", ".....gyYyl......", "....gYyYl.......", "....gyYl........", "...dgyl.........", "...dgl..........", "..dd............", "................", "................"],
    "squash":  ["................", "........d.......", ".......dg.......", "......kook......", ".....kOOook.....", "....kOoooook....", "....koooooook...",
                "...kOoooooook...", "...kooooooook...", "...kooooooook...", "....kooooook....", ".....kkkkkk.....", "................", "................", "................", "................"],
    "cabbage": ["................", "................", ".....dgggd......", "....dglllgd.....", "...dglcccclgd...", "..dglccsscclgd..", "..dglcsssscclgd.",
                "..dglcsssscclgd.", "..dglccsscclgd..", "...dglcccclgd...", "....dglllgd.....", ".....ddddd......", "................", "................", "................", "................"],
    "onion":   ["................", ".......g........", ".......gd.......", "......g.d.......", "......gkd.......", ".....kWWWk......", "....kWwwwWk.....",
                "...kWwwswwWk....", "...kWwsswwWk....", "...kWwwwwwWk....", "....kWwwwWk.....", ".....kWWWk......", "......kbk.......", "................", "................", "................"],
    "beans":   ["................", "................", "................", ".....k..........", "....kck.........", "...kcck...k.....", "..kccck..kck....",
                "..kcck..kcck....", "..kck..kccck....", "...k..kccck.....", "......kcck......", ".......kk.......", "................", "................", "................", "................"],
    "rice":    ["................", "................", "................", ".........w......", "......w.wsw.....", ".....wsw.w.w....", "......w.wsw.....",
                ".....ws.w.w.....", "....wsw.wsw.....", ".....w.wsw......", "......wsw.......", ".......w........", "................", "................", "................", "................"],
}
SEED_COL = {"tomato_seeds": (230, 200, 120), "pepper_seeds": (240, 220, 150), "corn_seeds": (236, 200, 60), "squash_seeds": (240, 230, 190), "cabbage_seeds": (60, 40, 30)}
TITLES = {"tomato": "Tomato", "pepper": "Pepper", "corn": "Corn", "squash": "Squash", "cabbage": "Cabbage", "onion": "Onion", "beans": "Beans", "rice": "Rice",
          "tomato_seeds": "Tomato Seeds", "pepper_seeds": "Pepper Seeds", "corn_seeds": "Corn Kernels", "squash_seeds": "Squash Seeds", "cabbage_seeds": "Cabbage Seeds"}
SEEDS = {"tomato": "tomato_seeds", "pepper": "pepper_seeds", "corn": "corn_seeds", "squash": "squash_seeds", "cabbage": "cabbage_seeds"}

def seed_icon(name):
    """Seeds in the classic wheat seeds' scatter, in each crop's colour."""
    src = Image.open(os.path.join(M.CLASSIC, "item", "wheat_seeds.png")).convert("RGBA"); px = src.load(); c = SEED_COL[name]
    for y in range(16):
        for x in range(16):
            p = px[x, y]
            if p[3]: k = sum(p[:3]) / 3 / 160; px[x, y] = tuple(max(0, min(255, int(v * k))) for v in c) + (p[3],)
    return src

def icon(rows):
    im = canvas(); px = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in PAL: px[x, y] = PAL[ch] + (255,)
    return im

def lang_entries():
    out = {}
    for k, t in TITLES.items():
        if k in SEEDS.values() or k not in ("onion", "beans", "rice"): out[f"item.ryancraft.{k}"] = t
        else: out[f"item.ryancraft.{k}"] = t
        if k in ICONS: out[f"block.ryancraft.{k}_crop"] = t
    return out

def build():
    W, D = M.w, "data/ryancraft/"
    for name in ICONS:
        for st in range(4):
            FA.crop_stage(name, st).save(os.path.join(B, f"{name}_stage{st}.png"))
            W(f"assets/ryancraft/models/block/{name}_stage{st}.json", {"parent": "minecraft:block/crop", "textures": {"crop": f"ryancraft:block/{name}_stage{st}"}})
        W(f"assets/ryancraft/blockstates/{name}_crop.json", {"variants": {f"age={a}": {"model": f"ryancraft:block/{name}_stage{a}"} for a in range(4)}})
        FA.ICONS[name]().save(os.path.join(I, f"{name}.png"))
        items = [name] + ([SEEDS[name]] if name in SEEDS else [])
        if name in SEEDS: FA.seeds(SEEDS[name]).save(os.path.join(I, f"{SEEDS[name]}.png"))
        for it in items:
            W(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{it}"}})
            W(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{it}"}})
        seed = SEEDS.get(name, name)
        grown = {"condition": "minecraft:block_state_property", "block": f"ryancraft:{name}_crop", "properties": {"age": "3"}}
        fortune = {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 3, "probability": 0.5714286}}
        if name in SEEDS:
            pools = [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                        {"type": "minecraft:item", "name": f"ryancraft:{name}", "conditions": [grown]}, {"type": "minecraft:item", "name": f"ryancraft:{seed}"}]}]},
                     {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{seed}", "functions": [fortune]}], "conditions": [grown]}]
        else:
            pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{name}"}]},
                     {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{name}", "functions": [fortune]}], "conditions": [grown]}]
        W(f"{D}loot_table/blocks/{name}_crop.json", {"type": "minecraft:block", "functions": [{"function": "minecraft:explosion_decay"}], "pools": pools})
        if name in SEEDS:
            W(f"{D}recipe/{SEEDS[name]}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [f"ryancraft:{name}"],
                "result": {"id": f"ryancraft:{SEEDS[name]}", "count": 4 if name == "squash" else 2}})
    M.add_tag("data/minecraft/tags/block/crops.json", [f"ryancraft:{n}_crop" for n in ICONS])
    M.add_tag("data/minecraft/tags/block/maintains_farmland.json", [f"ryancraft:{n}_crop" for n in ICONS])
    M.add_tag("data/minecraft/tags/item/villager_plantable_seeds.json", [f"ryancraft:{SEEDS.get(n, n)}" for n in ICONS])

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("crops ok")
