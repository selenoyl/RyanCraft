"""Leaded glass (metal/LeadedGlass.java): clear (the classic glass's shine marks) or vanilla's 16 stained glass tints,
set in 1-pixel lead cames in two patterns (diamond quarrels, squares) that tile seamlessly; blocks, panes, a door and a
trapdoor. Blockstates and models follow vanilla's stained glass pane; loot drops itself; recipes; lang.
  python3 tools/leaded_glass.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image
import metals_assets as MA

HERE = os.path.dirname(os.path.abspath(__file__)); TEX = os.path.join(MA.TEX, "block"); ITEM = os.path.join(MA.TEX, "item")
VA = os.path.join(HERE, "templates", "glass", "assets", "minecraft"); CLASSIC = MA.CLASSIC
COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
PATTERNS = {"leaded_glass": "Leaded Glass", "square_leaded_glass": "Square Leaded Glass"}
LEAD = [(40, 42, 58), (74, 78, 100), (114, 118, 142), (162, 166, 190)]
CAME, SOLDER, SHADE = (96, 100, 124, 255), (156, 160, 180, 255), (62, 64, 82, 255)

def is_came(pattern, x, y):
    if pattern == "leaded_glass": return (x + y) % 8 == 0 or (x - y) % 8 == 0
    return x % 8 == 0 or y % 8 == 0

def is_joint(pattern, x, y):
    if pattern == "leaded_glass": return (x % 8 == 0 and y % 8 == 0) or (x % 8 == 4 and y % 8 == 4)
    return x % 8 == 0 and y % 8 == 0

def glass(colour):
    """The pane of glass with its border wiped out (cames replace it): classic clear glass, or vanilla's tint."""
    if colour is None:
        im = Image.open(os.path.join(CLASSIC, "block", "glass.png")).convert("RGBA").crop((0, 0, 16, 16)); px = im.load()
        for i in range(16):
            for x, y in ((i, 0), (i, 15), (0, i), (15, i)): px[x, y] = (0, 0, 0, 0)
        return im
    im = Image.open(os.path.join(VA, "textures", "block", f"{colour}_stained_glass.png")).convert("RGBA"); px = im.load()
    inner = px[4, 4] if px[4, 4][3] <= px[5, 5][3] else px[5, 5]
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)): px[x, y] = inner
    return im

def leaded(colour, pattern):
    im = glass(colour); px = im.load()
    for y in range(16):
        for x in range(16):
            if is_joint(pattern, x, y): px[x, y] = SOLDER
            elif is_came(pattern, x, y): px[x, y] = CAME
            elif is_came(pattern, x - 1, y - 1) and px[x, y][3] < 200:      # a hint of shadow under each came
                c = px[x, y]; px[x, y] = (max(0, c[0] - 30), max(0, c[1] - 30), max(0, c[2] - 30), max(c[3], 60))
    return im

def edge():
    im = Image.new("RGBA", (16, 16), CAME); px = im.load()
    for y in range(16):
        px[7, y] = SOLDER; px[8, y] = SHADE
    return im

def framed(pattern, w=16, h=16, handle=None):
    im = leaded(None, pattern); px = im.load()
    for y in range(h):
        for x in range(w):
            if x < 2 or x > 13 or y < 2 or y > 13: px[x, y] = SHADE if (x in (0, 15) or y in (0, 15)) else CAME
    if handle:
        for x, y in handle: px[x, y] = SOLDER
    return im

def door():
    top, bot = leaded(None, "leaded_glass"), leaded(None, "leaded_glass")
    for im, edges in ((top, (0,)), (bot, (15,))):
        px = im.load()
        for y in range(16):
            for x in range(16):
                if x < 2 or x > 13 or y in edges or (y < 2 and 0 in edges) or (y > 13 and 15 in edges):
                    px[x, y] = SHADE if x in (0, 15) or y in (0, 15) else CAME
    px = bot.load(); px[11, 1] = px[11, 2] = px[12, 1] = SOLDER          # the handle near the top of the lower half
    for x in range(2, 14): bot.load()[x, 0] = CAME                        # the rail between the halves
    icon = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); both = Image.new("RGBA", (16, 32)); both.paste(top, (0, 0)); both.paste(bot, (0, 16))
    icon.alpha_composite(both.resize((8, 16), Image.NEAREST), (4, 0))
    return top, bot, icon

def lang_entries():
    out = {"block.ryancraft.leaded_glass_door": "Leaded Glass Door", "block.ryancraft.leaded_glass_trapdoor": "Leaded Glass Trapdoor"}
    for p, t in PATTERNS.items():
        out[f"block.ryancraft.{p}"] = t; out[f"block.ryancraft.{p}_pane"] = t + " Pane"
        for c in COLOURS:
            ct = " ".join(w.title() for w in c.split("_"))
            out[f"block.ryancraft.{c}_{p}"] = f"{ct} {t}"; out[f"block.ryancraft.{c}_{p}_pane"] = f"{ct} {t} Pane"
    return out

def build():
    W, R, L = MA.w, "data/ryancraft/recipe/", "data/ryancraft/loot_table/blocks/"
    pane_bs = json.load(open(os.path.join(VA, "blockstates", "white_stained_glass_pane.json")))
    names, panes = [], []
    for p in PATTERNS:
        for c in [None] + COLOURS:
            n = (f"{c}_" if c else "") + p
            leaded(c, p).save(os.path.join(TEX, n + ".png")); edge().save(os.path.join(TEX, n + "_pane_top.png"))
            W(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
            W(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
            W(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
            pn = n + "_pane"
            W(f"assets/ryancraft/blockstates/{pn}.json", json.loads(json.dumps(pane_bs).replace("minecraft:block/white_stained_glass_pane", f"ryancraft:block/{pn}")))
            for part in ("post", "side", "side_alt", "noside", "noside_alt"):
                tx = {"pane": f"ryancraft:block/{n}", "edge": f"ryancraft:block/{pn}_top"} if part in ("post", "side", "side_alt") else {"pane": f"ryancraft:block/{n}"}
                W(f"assets/ryancraft/models/block/{pn}_{part}.json", {"parent": f"minecraft:block/template_glass_pane_{part}", "textures": tx})
            W(f"assets/ryancraft/models/item/{pn}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:block/{n}"}})
            W(f"assets/ryancraft/items/{pn}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{pn}"}})
            for b in (n, pn): W(L + b + ".json", MA_drop(b))
            names += [f"ryancraft:{n}", f"ryancraft:{pn}"]; panes.append(f"ryancraft:{pn}")
            # recipes: 8 glass round a lead ingot; 6 leaded glass -> 16 panes; the stonecutter swaps patterns
            src = f"minecraft:{c}_stained_glass" if c else "minecraft:glass"
            if p == "leaded_glass":
                W(f"{R}{n}.json", {"type": "minecraft:crafting_shaped", "category": "building", "pattern": ["###", "#L#", "###"],
                    "key": {"#": src, "L": "ryancraft:lead_ingot"}, "result": {"id": f"ryancraft:{n}", "count": 8}})
                other = (f"{c}_" if c else "") + "square_leaded_glass"
                for a, b in ((n, other), (other, n), (pn, other + "_pane"), (other + "_pane", pn)):
                    W(f"{R}{b}_from_{a}_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": f"ryancraft:{a}", "result": {"id": f"ryancraft:{b}", "count": 1}})
                if c:
                    W(f"{R}{n}_from_dye.json", {"type": "minecraft:crafting_shaped", "category": "building", "group": "leaded_glass", "pattern": ["###", "#D#", "###"],
                        "key": {"#": "ryancraft:leaded_glass", "D": f"minecraft:{c}_dye"}, "result": {"id": f"ryancraft:{n}", "count": 8}})
            W(f"{R}{pn}.json", {"type": "minecraft:crafting_shaped", "category": "decorations", "pattern": ["###", "###"],
                "key": {"#": f"ryancraft:{n}"}, "result": {"id": f"ryancraft:{pn}", "count": 16}})
    # door and trapdoor (clear only), on vanilla's iron door / trapdoor models
    top, bot, icon = door()
    import wood; icon = wood.door_icon(top, bot)          # vanilla's door-icon size and spot
    top.save(os.path.join(TEX, "leaded_glass_door_top.png")); bot.save(os.path.join(TEX, "leaded_glass_door_bottom.png")); icon.save(os.path.join(ITEM, "leaded_glass_door.png"))
    framed("square_leaded_glass").save(os.path.join(TEX, "leaded_glass_trapdoor.png"))
    S = os.path.join(HERE, "templates", "silver", "assets", "minecraft")
    import silver_assets as SA
    for kind in ("door", "trapdoor"):
        name = f"leaded_glass_{kind}"; sub = lambda o: json.loads(json.dumps(o).replace(f"minecraft:block/copper_{kind}", f"ryancraft:block/{name}")
                                                          .replace(f"minecraft:item/copper_{kind}", f"ryancraft:item/{name}"))
        W(f"assets/ryancraft/blockstates/{name}.json", sub(json.load(open(os.path.join(S, "blockstates", f"copper_{kind}.json")))))
        W(f"assets/ryancraft/items/{name}.json", sub(json.load(open(os.path.join(S, "items", f"copper_{kind}.json")))))
        for f in os.listdir(os.path.join(S, "models", "block")):
            if f.startswith(f"copper_{kind}"): W(f"assets/ryancraft/models/block/{name}{f[len('copper_' + kind):]}", sub(json.load(open(os.path.join(S, "models", "block", f)))))
    W("assets/ryancraft/models/item/leaded_glass_door.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/leaded_glass_door"}})
    W(L + "leaded_glass_door.json", json.loads(json.dumps(json.load(open(os.path.join(HERE, "templates", "silver", "data", "minecraft", "loot_table", "blocks", "copper_door.json"))))
                                             .replace("minecraft:copper_door", "ryancraft:leaded_glass_door")))
    W(L + "leaded_glass_trapdoor.json", MA_drop("leaded_glass_trapdoor"))
    W(f"{R}leaded_glass_door.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["##", "##", "##"],
        "key": {"#": "ryancraft:leaded_glass"}, "result": {"id": "ryancraft:leaded_glass_door", "count": 3}})
    W(f"{R}leaded_glass_trapdoor.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["###", "###"],
        "key": {"#": "ryancraft:leaded_glass_pane"}, "result": {"id": "ryancraft:leaded_glass_trapdoor", "count": 2}})
    MA.add_tag("data/minecraft/tags/block/impermeable.json", [x for x in names if x not in panes])
    MA.add_tag("data/c/tags/block/glass_blocks.json", [x for x in names if x not in panes]); MA.add_tag("data/c/tags/item/glass_blocks.json", [x for x in names if x not in panes])
    MA.add_tag("data/c/tags/block/glass_panes.json", panes); MA.add_tag("data/c/tags/item/glass_panes.json", panes)
    for t in ("doors", "trapdoors"):
        MA.add_tag(f"data/minecraft/tags/block/{t}.json", [f"ryancraft:leaded_glass_{t[:-1]}"]); MA.add_tag(f"data/minecraft/tags/item/{t}.json", [f"ryancraft:leaded_glass_{t[:-1]}"])
    MA.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", names + ["ryancraft:leaded_glass_door", "ryancraft:leaded_glass_trapdoor"])

def MA_drop(n):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}

if __name__ == "__main__":
    build()
    p = os.path.join(MA.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("leaded glass ok")
