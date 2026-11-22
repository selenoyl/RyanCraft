"""Pipes, engines, pump, tank and the Ender Terminal: textures (cut from the classic block textures, so they match the
old look), multipart pipe models, item models, loot, recipes, tags and lang. tools/resources.py merges lang_entries()."""
import json, os, random
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")
def src(n): return Image.open(os.path.join(CLASSIC, n + ".png")).convert("RGBA").crop((0, 0, 16, 16))
def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2)
def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)

PIPES = {"wooden": "oak_planks", "cobblestone": "cobblestone", "stone": "stone", "gold": "gold_block", "iron": "iron_block",
         "diamond": "diamond_block", "obsidian": "obsidian", "ender": "obsidian", "fluid": "stone"}
NAMES = {"wooden": "Wooden Pipe", "cobblestone": "Cobblestone Pipe", "stone": "Stone Pipe", "gold": "Golden Pipe", "iron": "Iron Pipe",
         "diamond": "Diamond Pipe", "obsidian": "Obsidian Pipe", "ender": "Ender Pipe", "fluid": "Fluid Pipe"}

def pipe_texture(kind):
    """A pipe wall that's mostly open: the material only along the pipe's two edges, with a thin rib every 8 px, so the
    items riding inside are easy to see. Laid out for faces 8 px wide (u 4-12), running along v."""
    base = src(PIPES[kind]); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load(); b = base.load()
    for y in range(16):
        for x in range(4, 12):
            c = b[x, y]
            if x in (4, 11): px[x, y] = shade(c, 0.6)                            # the pipe's edges
            elif y % 8 == 0: px[x, y] = shade(c, 0.9)                            # a thin rib
            else: px[x, y] = (0, 0, 0, 0)                                        # open: see the items
            if kind == "fluid" and x in (4, 11): px[x, y] = (70, 130, 60, 255)    # green sealant seams
            if kind == "ender" and (x in (4, 11) and y % 4 == 2 or y % 8 == 0 and x in (6, 9)): px[x, y] = (40, 170, 140, 255)  # ender-pearl teal
    img.save(os.path.join(A, "textures", "block", f"{kind}_pipe.png"))
    # the end cap where a pipe meets an inventory: a ring of the material around an open mouth
    cap = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); cp = cap.load()
    for y in range(4, 12):
        for x in range(4, 12):
            ring = x in (4, 11) or y in (4, 11)
            cp[x, y] = shade(b[x, y], 0.6) if ring else (0, 0, 0, 0)
            if kind == "ender" and ring and (x + y) % 3 == 0: cp[x, y] = (40, 170, 140, 255)
    cap.save(os.path.join(A, "textures", "block", f"{kind}_pipe_end.png"))

def face(uv, tex, cull=None, rot=0):
    f = {"uv": uv, "texture": tex}
    if rot: f["rotation"] = rot
    if cull: f["cullface"] = cull
    return f

def pipe_models(kind):
    t = f"ryancraft:block/{kind}_pipe"; e = f"ryancraft:block/{kind}_pipe_end"
    s = [4, 0, 12, 16]                                                       # a full pipe-wall strip
    core = {"from": [4, 4, 4], "to": [12, 12, 12], "faces": {d: face([4, 4, 12, 12], "#pipe") for d in ("north", "south", "east", "west", "up", "down")}}
    w(f"assets/ryancraft/models/block/{kind}_pipe_core.json", {"parent": "block/block", "render_type": "cutout", "ambientocclusion": False,
        "textures": {"pipe": t, "particle": t}, "elements": [core]})
    arm = {"from": [4, 4, 0], "to": [12, 12, 4], "faces": {"east": face([4, 0, 12, 4], "#pipe", rot=90), "west": face([4, 0, 12, 4], "#pipe", rot=90),
           "up": face([4, 0, 12, 4], "#pipe"), "down": face([4, 0, 12, 4], "#pipe"), "north": face([4, 4, 12, 12], "#end", "north")}}
    w(f"assets/ryancraft/models/block/{kind}_pipe_arm.json", {"parent": "block/block", "render_type": "cutout", "ambientocclusion": False,
        "textures": {"pipe": t, "end": e, "particle": t}, "elements": [arm]})
    rot = {"north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
    parts = [{"apply": {"model": f"ryancraft:block/{kind}_pipe_core"}}]
    for d, r in rot.items(): parts.append({"when": {d: "true"}, "apply": dict({"model": f"ryancraft:block/{kind}_pipe_arm"}, **r)})
    w(f"assets/ryancraft/blockstates/{kind}_pipe.json", {"multipart": parts})
    # item: a straight run of pipe
    w(f"assets/ryancraft/models/item/{kind}_pipe.json", {"parent": "block/block", "textures": {"pipe": t, "particle": t}, "display": {
        "gui": {"rotation": [30, 225, 0], "scale": [0.9, 0.9, 0.9]}},
        "elements": [{"from": [4, 4, 0], "to": [12, 12, 16], "faces": {"east": face([4, 0, 12, 16], "#pipe", rot=90), "west": face([4, 0, 12, 16], "#pipe", rot=90),
                      "up": face([4, 0, 12, 16], "#pipe"), "down": face([4, 0, 12, 16], "#pipe"), "north": face([4, 4, 12, 12], "#pipe"), "south": face([4, 4, 12, 12], "#pipe")}}]})
    w(f"assets/ryancraft/items/{kind}_pipe.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{kind}_pipe"}})

def tile(img, tex, box):
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1): img.putpixel((x, y), tex.getpixel((x % 16, y % 16)))

def box_uv(img, tex_side, tex_top, u, v, w_, h, d):
    """Paint a model box's UV layout (Minecraft cube unwrap) with a top/bottom texture and a side texture."""
    tile(img, tex_top, (u + d, v, u + d + w_, v + d)); tile(img, tex_top, (u + d + w_, v, u + d + 2 * w_, v + d))
    tile(img, tex_side, (u, v + d, u + 2 * d + 2 * w_, v + d + h))

def rows(img, y0, y1):
    """A horizontal band of a 16x16 texture, tiled to height y1-y0 (for thin box sides)."""
    out = Image.new("RGBA", (16, y1 - y0))
    for y in range(y1 - y0): 
        for x in range(16): out.putpixel((x, y), img.getpixel((x, y0 + y)))
    return out

def face_paint(img, tex, x0, y0, w_, h):
    t = tex.resize((w_, h), Image.NEAREST) if tex.size != (w_, h) else tex
    img.paste(t, (x0, y0))

def box_faces(img, u, v, w_, h, d, top, bottom, side):
    """Minecraft's cube unwrap: top/bottom up top, then the four sides in a row."""
    face_paint(img, top, u + d, v, w_, d); face_paint(img, bottom, u + d + w_, v, w_, d)
    for i, x0 in enumerate((u, u + d, u + d + w_, u + 2 * d + w_)):
        face_paint(img, side, x0, v + d, d if i % 2 == 0 else w_, h)

def trimmed(tex, dark=0.45):
    """A plate face with the piston's dark trim around the edge."""
    t = tex.copy()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)): t.putpixel((x, y), shade(tex.getpixel((x, y)), dark))
    return t

def strip(tex, h, dark=0.45):
    """A plate's side: a band of the material with dark trim top and bottom (like the piston head's rim)."""
    t = Image.new("RGBA", (16, h))
    for y in range(h):
        for x in range(16):
            c = tex.getpixel((x, y + 6))
            t.putpixel((x, y), shade(c, dark) if y in (0, h - 1) else c)
    return t

def heat_glass(hot):
    """BuildCraft's heat trunk: checkered glowing glass with dark corners: blue when cool, green when warm."""
    a, b = ((70, 190, 70), (40, 140, 40)) if hot else ((40, 110, 230), (20, 70, 200))
    t = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            t.putpixel((x, y), (34, 30, 28, 255) if edge else (a if (x // 2 + y // 2) % 2 else b) + (255,))
    return t

def engine_textures():
    p_top, p_side, p_inner = src("piston_top"), src("piston_side"), src("piston_inner")
    planks, stone, cob, iron = src("oak_planks"), src("stone"), src("cobblestone"), src("iron_block")
    stone_head = p_top.copy()                                             # the piston head's iron square, set in stone
    for y in range(16):
        for x in range(16):
            r, g, b, al = p_top.getpixel((x, y))
            if not (4 <= x <= 11 and 4 <= y <= 11): stone_head.putpixel((x, y), stone.getpixel((x, y)))
    bellows = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16): bellows.putpixel((x, y), shade(iron.getpixel((x, y)), 0.35 if y % 3 == 0 else 0.55))
    for hot in (False, True):
        glass = heat_glass(hot)
        glass.save(os.path.join(A, "textures", "block", "engine_trunk" + ("_hot" if hot else "") + ".png"))
        for kind in ("redstone", "stirling"):
            mat = planks if kind == "redstone" else stone
            img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
            base_side = strip(mat if kind == "redstone" else cob, 4)
            if kind == "stirling":                                         # a furnace mouth in the base, lit while burning
                furn = src("furnace_front_on" if hot else "furnace_front")
                base_side.paste(furn.crop((4, 9, 12, 13)), (4, 0))
            box_faces(img, 0, 0, 16, 4, 16, trimmed(mat if kind == "redstone" else cob), mat, base_side)                   # base plate
            box_faces(img, 0, 20, 16, 4, 16, p_top if kind == "redstone" else stone_head, trimmed(mat), strip(mat, 4))     # piston-head plate
            box_faces(img, 64, 0, 12, 8, 12, bellows, bellows, bellows)                                                     # bellows chamber
            box_faces(img, 64, 32, 8, 8, 8, glass, glass, glass)                                                            # heat trunk
            os.makedirs(os.path.join(A, "textures", "entity", "engine"), exist_ok=True)
            img.save(os.path.join(A, "textures", "entity", "engine", kind + ("_hot" if hot else "") + ".png"))
    for kind in ("redstone", "stirling"):
        base = "minecraft:block/oak_planks" if kind == "redstone" else "minecraft:block/cobblestone"
        head = "minecraft:block/piston_top" if kind == "redstone" else "minecraft:block/stone"
        w(f"assets/ryancraft/models/block/{kind}_engine.json", {"textures": {"particle": base}})
        w(f"assets/ryancraft/blockstates/{kind}_engine.json", {"variants": {"": {"model": f"ryancraft:block/{kind}_engine"}}})
        w(f"assets/ryancraft/models/item/{kind}_engine.json", {"parent": "block/block", "textures": {
            "base": base, "head": head, "trunk": "ryancraft:block/engine_trunk", "particle": base},
            "elements": [
                {"from": [0, 0, 0], "to": [16, 4, 16], "faces": {**{d: face([0, 6, 16, 10], "#base") for d in ("north", "south", "east", "west")},
                                                                 "up": face([0, 0, 16, 16], "#base"), "down": face([0, 0, 16, 16], "#base")}},
                {"from": [0, 4, 0], "to": [16, 8, 16], "faces": {**{d: face([0, 6, 16, 10], "#base") for d in ("north", "south", "east", "west")},
                                                                 "up": face([0, 0, 16, 16], "#head"), "down": face([0, 0, 16, 16], "#base")}},
                {"from": [4, 8, 4], "to": [12, 16, 12], "faces": {d: face([4, 4, 12, 12], "#trunk") for d in ("north", "south", "east", "west", "up")}}]})
        w(f"assets/ryancraft/items/{kind}_engine.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{kind}_engine"}})

def simple_blocks():
    iron, glass, obs = src("iron_block"), src("glass"), src("obsidian")
    # pump: iron casing with dark bands and an intake underneath
    side = iron.copy(); top = iron.copy(); bottom = iron.copy()
    for x in range(16):
        for y in (3, 12): side.putpixel((x, y), shade(iron.getpixel((x, y)), 0.5))
    for y in range(5, 11):
        for x in range(5, 11): bottom.putpixel((x, y), (30, 30, 34, 255) if 6 <= x <= 9 and 6 <= y <= 9 else shade(iron.getpixel((x, y)), 0.6))
        for x in range(6, 10): top.putpixel((x, y), (60, 90, 170, 255) if 7 <= y <= 8 else shade(iron.getpixel((x, y)), 0.7))
    for n, im in (("pump_side", side), ("pump_top", top), ("pump_bottom", bottom)): im.save(os.path.join(A, "textures", "block", n + ".png"))
    w("assets/ryancraft/models/block/pump.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "side": "ryancraft:block/pump_side", "top": "ryancraft:block/pump_top", "bottom": "ryancraft:block/pump_bottom"}})
    # tank: a glass box with a metal-edged frame
    tank = glass.copy()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)): tank.putpixel((x, y), shade(iron.getpixel((x, y)), 0.7))
    tank.save(os.path.join(A, "textures", "block", "tank.png"))
    w("assets/ryancraft/models/block/tank.json", {"parent": "block/block", "render_type": "cutout", "textures": {"t": "ryancraft:block/tank", "particle": "ryancraft:block/tank"},
        "elements": [{"from": [2, 0, 2], "to": [14, 16, 14], "faces": {d: face([2, 0, 14, 16] if d not in ("up", "down") else [2, 2, 14, 14], "#t") for d in ("north", "south", "east", "west", "up", "down")}}]})
    # ender terminal: an end portal frame turned on its side (eye socket facing you, no eye), with redstone fixed to it
    frame = src("end_portal_frame_top"); front = frame.copy(); fp = front.load()
    red, red_dk = (200, 30, 20, 255), (110, 10, 8, 255)
    for i in range(16):                                    # redstone dust traced along the frame's border
        for (x, y) in ((i, 1), (1, i), (14, i)):
            if fp[x, y][1] < 140 and (i % 5 != 3): fp[x, y] = red if (i % 2) else red_dk
    for (x, y) in ((7, 14), (8, 14), (7, 15), (8, 15)): fp[x, y] = red
    front.save(os.path.join(A, "textures", "block", "ender_terminal_front.png"))
    w("assets/ryancraft/models/block/ender_terminal.json", {"parent": "block/block", "textures": {
        "front": "ryancraft:block/ender_terminal_front", "side": "minecraft:block/end_portal_frame_side", "back": "minecraft:block/end_stone",
        "red": "minecraft:block/redstone_block", "particle": "minecraft:block/end_portal_frame_side"},
        "elements": [
            {"from": [0, 0, 3], "to": [16, 16, 16], "faces": {"north": face([0, 0, 16, 16], "#front"), "south": face([0, 0, 16, 16], "#back"),
                "east": face([0, 3, 16, 16], "#side", rot=90), "west": face([0, 3, 16, 16], "#side", rot=270),
                "up": face([0, 3, 16, 16], "#side", rot=180), "down": face([0, 3, 16, 16], "#side")}},
            {"from": [1, 16, 6], "to": [3, 18, 8], "faces": {d: face([0, 0, 2, 2], "#red") for d in ("north", "south", "east", "west", "up")}},
            {"from": [13, 16, 6], "to": [15, 18, 8], "faces": {d: face([0, 0, 2, 2], "#red") for d in ("north", "south", "east", "west", "up")}},
            {"from": [6, 16, 10], "to": [10, 17, 14], "faces": {d: face([0, 0, 4, 1] if d != "up" else [0, 0, 4, 4], "#red") for d in ("north", "south", "east", "west", "up")}}]})
    w("assets/ryancraft/blockstates/ender_terminal.json", {"variants": {f"facing={f}": dict({"model": "ryancraft:block/ender_terminal"}, **({"y": r} if r else {}))
        for f, r in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    for n in ("pump", "tank"): w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
    for n in ("pump", "tank", "ender_terminal"): w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
    # wrench icon: a classic open-jaw spanner on the diagonal, centred in the slot
    WR = [
        "................",
        "...........HH...",
        "..........HLLH..",
        ".........HLDDL..",
        ".........HLD....",
        "........HLLD.HH.",
        ".......HLLLLHLLH",
        "......HLLDDLLLD.",
        ".....HLLDD.DDD..",
        "....HLLDD.......",
        "...HLLDD........",
        "..HLLDD.........",
        ".HLLDD..........",
        ".HLDD...........",
        "..DD............",
        "................"]
    pal = {"H": (236, 236, 240, 255), "L": (190, 192, 198, 255), "D": (110, 112, 120, 255)}
    wr = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(WR):
        for x, ch in enumerate(row):
            if ch in pal: wr.putpixel((x, y), pal[ch])
    wr.save(os.path.join(A, "textures", "item", "wrench.png"))
    w("assets/ryancraft/models/item/wrench.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": "ryancraft:item/wrench"}})
    w("assets/ryancraft/items/wrench.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/wrench"}})

def data():
    blocks = [f"{k}_pipe" for k in PIPES] + ["redstone_engine", "stirling_engine", "pump", "tank", "ender_terminal"]
    for b in blocks:
        w(f"data/ryancraft/loot_table/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{b}"}],
          "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    mats = {"wooden": "#minecraft:planks", "cobblestone": "minecraft:cobblestone", "stone": "minecraft:stone", "gold": "minecraft:gold_ingot",
            "iron": "minecraft:iron_ingot", "diamond": "minecraft:diamond", "obsidian": "minecraft:obsidian", "ender": "minecraft:ender_pearl"}
    for k, m in mats.items():      # the old recipe: material, glass, material -> 8 pipes
        w(f"data/ryancraft/recipe/{k}_pipe.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["MGM"],
          "key": {"M": m, "G": "minecraft:glass"}, "result": {"id": f"ryancraft:{k}_pipe", "count": 8}})
    w("data/ryancraft/recipe/fluid_pipe.json", {"type": "minecraft:crafting_shapeless", "category": "redstone",
      "ingredients": ["ryancraft:stone_pipe", "minecraft:green_dye"], "result": {"id": "ryancraft:fluid_pipe", "count": 1}})
    shaped = {
        "wrench": (["I I", " I ", " I "], {"I": "minecraft:iron_ingot"}, 1),
        "redstone_engine": (["PPP", " G ", "RXR"], {"P": "#minecraft:planks", "G": "minecraft:glass", "R": "minecraft:redstone", "X": "minecraft:piston"}, 1),
        "stirling_engine": (["CCC", " G ", "XFX"], {"C": "minecraft:cobblestone", "G": "minecraft:glass", "X": "minecraft:piston", "F": "minecraft:furnace"}, 1),
        "pump": (["IRI", "IBI", "ITI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "B": "minecraft:bucket", "T": "ryancraft:tank"}, 1),
        "tank": (["GGG", "G G", "GGG"], {"G": "minecraft:glass"}, 1),
        "ender_terminal": ([" C ", "RFR", " P "], {"C": "minecraft:comparator", "R": "minecraft:redstone", "F": "minecraft:end_portal_frame", "P": "ryancraft:ender_pipe"}, 1),
    }
    for n, (pat, key, cnt) in shaped.items():
        w(f"data/ryancraft/recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": pat, "key": key, "result": {"id": f"ryancraft:{n}", "count": cnt}})
    # end portal frames: mineable with a diamond pickaxe, and they drop themselves with Silk Touch
    w("data/minecraft/loot_table/blocks/end_portal_frame.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": "minecraft:end_portal_frame"}], "conditions": [{"condition": "minecraft:match_tool", "predicate": {
        "items": ["minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe"],
        "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}]})
    for tag, vals in (("needs_diamond_tool", ["minecraft:end_portal_frame"]),):
        p = os.path.join(D, "minecraft", "tags", "block", tag + ".json")
        t = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
        t["values"] = t["values"] + [v for v in vals if v not in t["values"]]
        w(f"data/minecraft/tags/block/{tag}.json", t)
    axe = ["ryancraft:wooden_pipe", "ryancraft:redstone_engine"]
    pick = [f"ryancraft:{b}" for b in blocks if f"ryancraft:{b}" not in axe and b != "tank"] + ["minecraft:end_portal_frame"]
    for tag, vals in (("mineable/axe", axe), ("mineable/pickaxe", pick)):
        p = os.path.join(D, "minecraft", "tags", "block", tag + ".json")
        t = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
        t["values"] = t["values"] + [v for v in vals if v not in t["values"]]
        w(f"data/minecraft/tags/block/{tag}.json", t)

def lang_entries():
    out = {f"block.ryancraft.{k}_pipe": n for k, n in NAMES.items()}
    out.update({"block.ryancraft.redstone_engine": "Redstone Engine", "block.ryancraft.stirling_engine": "Stirling Engine",
                "block.ryancraft.pump": "Pump", "block.ryancraft.tank": "Tank", "block.ryancraft.ender_terminal": "Ender Terminal",
                "item.ryancraft.wrench": "Wrench", "itemGroup.ryancraft.pipes": "RyanCraft Pipes", "gui.ryancraft.terminal.search": "Search"})
    return out

if __name__ == "__main__":
    for k in PIPES: pipe_texture(k); pipe_models(k)
    engine_textures(); simple_blocks(); data()
    print("pipes ok")
