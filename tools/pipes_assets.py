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
         "diamond": "diamond_block", "obsidian": "obsidian", "fluid": "stone"}
NAMES = {"wooden": "Wooden Pipe", "cobblestone": "Cobblestone Pipe", "stone": "Stone Pipe", "gold": "Golden Pipe", "iron": "Iron Pipe",
         "diamond": "Diamond Pipe", "obsidian": "Obsidian Pipe", "fluid": "Fluid Pipe"}

def pipe_texture(kind):
    """A pipe wall: the material with a darker frame along the pipe's edges and see-through slots between ribs, so you can
    watch the items go by. Laid out for faces 8 px wide (u 4-12)."""
    base = src(PIPES[kind]); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = img.load(); b = base.load()
    for y in range(16):
        for x in range(4, 12):
            c = b[x, y]
            if x in (4, 11): px[x, y] = shade(c, 0.55)                     # the pipe's edges
            elif y % 4 in (1, 2): px[x, y] = (0, 0, 0, 0)                   # glassless slots between ribs
            else: px[x, y] = shade(c, 0.95 if y % 4 == 0 else 0.8)
            if kind == "fluid" and x in (4, 11): px[x, y] = (70, 130, 60, 255)    # green sealant seams
    if kind == "iron":                                                     # iron pipes show their output end
        pass
    img.save(os.path.join(A, "textures", "block", f"{kind}_pipe.png"))
    # the end cap where a pipe meets an inventory: a ring of the material around an open mouth
    cap = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); cp = cap.load()
    for y in range(4, 12):
        for x in range(4, 12):
            ring = x in (4, 11) or y in (4, 11)
            cp[x, y] = shade(b[x, y], 0.6) if ring else ((0, 0, 0, 0) if 5 < x < 10 and 5 < y < 10 else shade(b[x, y], 0.85))
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

def engine_textures():
    trunk = {}
    for hot, col in ((False, (60, 110, 200)), (True, (70, 190, 70))):
        t = Image.new("RGBA", (16, 16)); r = random.Random(3)
        for y in range(16):
            for x in range(16):
                k = 0.7 if y % 4 == 0 else 1.0
                t.putpixel((x, y), tuple(max(0, min(255, int(c * k) + r.randint(-8, 8))) for c in col) + (255,))
        trunk[hot] = t
        t.save(os.path.join(A, "textures", "block", "engine_trunk" + ("_hot" if hot else "") + ".png"))
    for kind, plate in (("redstone", src("oak_planks")), ("stirling", src("cobblestone"))):
        for hot in (False, True):
            img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
            box_uv(img, plate, plate, 0, 0, 16, 4, 16)                   # base plate
            box_uv(img, plate, plate, 0, 20, 16, 4, 16)                  # piston plate
            box_uv(img, trunk[hot], trunk[hot], 64, 0, 8, 12, 8)        # trunk
            os.makedirs(os.path.join(A, "textures", "entity", "engine"), exist_ok=True)
            img.save(os.path.join(A, "textures", "entity", "engine", kind + ("_hot" if hot else "") + ".png"))
        plate_name = "minecraft:block/oak_planks" if kind == "redstone" else "minecraft:block/cobblestone"
        # the block itself is drawn by the renderer; this model gives it a particle and the item a 3D engine
        w(f"assets/ryancraft/models/block/{kind}_engine.json", {"textures": {"particle": plate_name}})
        w(f"assets/ryancraft/blockstates/{kind}_engine.json", {"variants": {"": {"model": f"ryancraft:block/{kind}_engine"}}})
        w(f"assets/ryancraft/models/item/{kind}_engine.json", {"parent": "block/block", "textures": {"plate": plate_name, "trunk": "ryancraft:block/engine_trunk", "particle": plate_name},
            "elements": [{"from": [0, 0, 0], "to": [16, 4, 16], "faces": {d: face([0, 0, 16, 4] if d not in ("up", "down") else [0, 0, 16, 16], "#plate") for d in ("north", "south", "east", "west", "up", "down")}},
                         {"from": [4, 4, 4], "to": [12, 12, 12], "faces": {d: face([4, 4, 12, 12], "#trunk") for d in ("north", "south", "east", "west")}},
                         {"from": [0, 8, 0], "to": [16, 12, 16], "faces": {d: face([0, 0, 16, 4] if d not in ("up", "down") else [0, 0, 16, 16], "#plate") for d in ("north", "south", "east", "west", "up", "down")}}]})
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
    # ender terminal: obsidian body, a violet screen with an eye on the front
    front = obs.copy(); dr = ImageDraw.Draw(front)
    dr.rectangle((2, 2, 13, 11), fill=(30, 10, 50, 255)); dr.rectangle((3, 3, 12, 10), fill=(70, 30, 120, 255))
    for (x, y) in ((6, 6), (7, 6), (8, 6), (9, 6), (6, 7), (7, 7), (8, 7), (9, 7)): front.putpixel((x, y), (40, 200, 160, 255))
    front.putpixel((7, 6), (10, 30, 20, 255)); front.putpixel((8, 7), (10, 30, 20, 255))
    for x in range(3, 13): front.putpixel((x, 13), (150, 80, 220, 255) if x % 2 else (90, 40, 150, 255))
    tside = obs.copy()
    for y in range(16): tside.putpixel((0, y), (110, 50, 170, 255)); tside.putpixel((15, y), (110, 50, 170, 255))
    for n, im in (("ender_terminal_front", front), ("ender_terminal_side", tside)): im.save(os.path.join(A, "textures", "block", n + ".png"))
    w("assets/ryancraft/models/block/ender_terminal.json", {"parent": "minecraft:block/orientable", "textures": {
        "front": "ryancraft:block/ender_terminal_front", "side": "ryancraft:block/ender_terminal_side", "top": "minecraft:block/obsidian"}})
    w("assets/ryancraft/blockstates/ender_terminal.json", {"variants": {f"facing={f}": dict({"model": "ryancraft:block/ender_terminal"}, **({"y": r} if r else {}))
        for f, r in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    for n in ("pump", "tank"): w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
    for n in ("pump", "tank", "ender_terminal"): w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
    # wrench icon
    wr = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); ir = (200, 200, 205, 255); dk = (90, 90, 95, 255)
    for i in range(4, 13): wr.putpixel((i, 15 - i), ir); wr.putpixel((i + 1, 15 - i), dk)
    for (x, y) in ((11, 1), (13, 1), (11, 2), (13, 2), (12, 3), (14, 3), (12, 4), (13, 4), (10, 2), (14, 2)): wr.putpixel((x, y), ir)
    wr.save(os.path.join(A, "textures", "item", "wrench.png"))
    w("assets/ryancraft/models/item/wrench.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": "ryancraft:item/wrench"}})
    w("assets/ryancraft/items/wrench.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/wrench"}})

def data():
    blocks = [f"{k}_pipe" for k in PIPES] + ["redstone_engine", "stirling_engine", "pump", "tank", "ender_terminal"]
    for b in blocks:
        w(f"data/ryancraft/loot_table/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{b}"}],
          "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    mats = {"wooden": "#minecraft:planks", "cobblestone": "minecraft:cobblestone", "stone": "minecraft:stone", "gold": "minecraft:gold_ingot",
            "iron": "minecraft:iron_ingot", "diamond": "minecraft:diamond", "obsidian": "minecraft:obsidian"}
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
        "ender_terminal": (["OEO", "EYE", "OPO"], {"O": "minecraft:obsidian", "E": "minecraft:ender_pearl", "Y": "minecraft:ender_eye", "P": "ryancraft:stone_pipe"}, 1),
    }
    for n, (pat, key, cnt) in shaped.items():
        w(f"data/ryancraft/recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": pat, "key": key, "result": {"id": f"ryancraft:{n}", "count": cnt}})
    axe = ["ryancraft:wooden_pipe", "ryancraft:redstone_engine"]
    pick = [f"ryancraft:{b}" for b in blocks if f"ryancraft:{b}" not in axe and b != "tank"]
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
