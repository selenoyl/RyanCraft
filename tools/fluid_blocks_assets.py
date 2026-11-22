"""Glass tank, iron-hooped vat, Archimedes screw pump and cast-iron hand pump: textures, models, blockstates, loot,
recipes and lang. Dark iron comes from the Programmer Art anvil, glass and planks from the classic pack.
No two parts share a face, so nothing z-fights. tools/resources.py merges lang_entries()."""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")
CL = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "block")
def src(p): return Image.open(p).convert("RGBA").crop((0, 0, 16, 16))
def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)
def w(rel, obj):
    p = os.path.join(RES, rel); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2)
def face(uv, tex, rot=0, cull=None):
    f = {"uv": uv, "texture": tex}
    if rot: f["rotation"] = rot
    if cull: f["cullface"] = cull
    return f
def el(frm, to, tex, faces=("north", "south", "east", "west", "up", "down")):
    """An element with each face's UV taken from its own position, so textures stay centred and aligned."""
    x0, y0, z0 = frm; x1, y1, z1 = to; fs = {}
    for d in faces:
        if d in ("north", "south"): uv = [x0, 16 - y1, x1, 16 - y0]
        elif d in ("east", "west"): uv = [z0, 16 - y1, z1, 16 - y0]
        else: uv = [x0, z0, x1, z1]
        fs[d] = face([max(0, min(16, v)) for v in uv], tex)
    return {"from": list(frm), "to": list(to), "faces": fs}

def textures():
    anvil = src(os.path.join(HERE, "template_anvil.png")); dark = anvil.copy(); px = dark.load()
    for y in range(16):
        for x in range(16):
            if min(x, y, 15 - x, 15 - y) == 0: px[x, y] = shade(px[x, y], 0.65)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)): px[x, y] = (150, 150, 156, 255); px[x + 1, y + 1] = (40, 40, 44, 255)
    dark.save(os.path.join(A, "textures", "block", "dark_iron.png"))
    src(os.path.join(CL, "glass.png")).save(os.path.join(A, "textures", "block", "tank_glass.png"))
    planks = src(os.path.join(CL, "oak_planks.png")); vat = planks.copy(); vp = vat.load()
    for y in (3, 4, 11, 12):
        for x in range(16): vp[x, y] = shade(anvil.getpixel((x, y)), 0.95 if y in (3, 11) else 0.6)
    vat.save(os.path.join(A, "textures", "block", "vat_side.png"))
    # parts drawn by the renderers: screw (iron), intake tube (dark iron), handle (dark iron + plank grip)
    iron = src(os.path.join(CL, "iron_block.png")); parts = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for y in range(64):
        for x in range(64):
            if y < 32: parts.putpixel((x, y), iron.getpixel((x % 16, y % 16)) if x < 32 else dark.getpixel((x % 16, y % 16)))
            else: parts.putpixel((x, y), dark.getpixel((x % 16, y % 16)) if x < 32 else planks.getpixel((x % 16, y % 16)))
    parts.save(os.path.join(A, "textures", "entity", "pump_parts.png"))

def models():
    T = "ryancraft:block/"
    # ---- glass tank (multipart: rims and end panes drop away where tanks stack)
    posts = [el((x, 0, z), (x + 2, 16, z + 2), "#iron") for (x, z) in ((0, 0), (14, 0), (0, 14), (14, 14))]
    def rim(y0, y1): return [el((2, y0, 0), (14, y1, 2), "#iron"), el((2, y0, 14), (14, y1, 16), "#iron"),
                             el((0, y0, 2), (2, y1, 14), "#iron"), el((14, y0, 2), (16, y1, 14), "#iron")]
    base = {"parent": "block/block", "render_type": "cutout", "textures": {"iron": T + "dark_iron", "glass": T + "tank_glass", "particle": T + "dark_iron"}}
    w("assets/ryancraft/models/block/tank_posts.json", dict(base, elements=posts))
    w("assets/ryancraft/models/block/tank_rim_top.json", dict(base, elements=rim(14, 16)))
    w("assets/ryancraft/models/block/tank_rim_bottom.json", dict(base, elements=rim(0, 2)))
    w("assets/ryancraft/models/block/tank_glass_sides.json", dict(base, elements=[el((0.5, 0.5, 0.5), (15.5, 15.5, 15.5), "#glass", ("north", "south", "east", "west"))]))
    w("assets/ryancraft/models/block/tank_glass_top.json", dict(base, elements=[el((0.5, 0.5, 0.5), (15.5, 15.5, 15.5), "#glass", ("up",))]))
    w("assets/ryancraft/models/block/tank_glass_bottom.json", dict(base, elements=[el((0.5, 0.5, 0.5), (15.5, 15.5, 15.5), "#glass", ("down",))]))
    w("assets/ryancraft/blockstates/tank.json", {"multipart": [
        {"apply": {"model": T + "tank_posts"}}, {"apply": {"model": T + "tank_glass_sides"}},
        {"when": {"up": "false"}, "apply": {"model": T + "tank_rim_top"}}, {"when": {"up": "false"}, "apply": {"model": T + "tank_glass_top"}},
        {"when": {"down": "false"}, "apply": {"model": T + "tank_rim_bottom"}}, {"when": {"down": "false"}, "apply": {"model": T + "tank_glass_bottom"}}]})
    w("assets/ryancraft/models/item/tank.json", dict(base, elements=posts + rim(14, 16) + rim(0, 2) + [el((0.5, 0.5, 0.5), (15.5, 15.5, 15.5), "#glass")]))
    w("assets/ryancraft/items/tank.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/tank"}})
    # ---- iron-hooped vat: walls and a floor, open top
    vat = {"parent": "block/block", "textures": {"side": T + "vat_side", "wood": "minecraft:block/oak_planks", "particle": "minecraft:block/oak_planks"},
           "elements": [el((1, 0, 1), (15, 1, 15), "#wood"),
                        dict(el((1, 1, 1), (15, 15, 3), "#side"), faces={**el((1, 1, 1), (15, 15, 3), "#side")["faces"], "up": face([1, 1, 15, 3], "#wood")}),
                        dict(el((1, 1, 13), (15, 15, 15), "#side"), faces={**el((1, 1, 13), (15, 15, 15), "#side")["faces"], "up": face([1, 13, 15, 15], "#wood")}),
                        dict(el((1, 1, 3), (3, 15, 13), "#side"), faces={**el((1, 1, 3), (3, 15, 13), "#side")["faces"], "up": face([1, 3, 3, 13], "#wood")}),
                        dict(el((13, 1, 3), (15, 15, 13), "#side"), faces={**el((13, 1, 3), (15, 15, 13), "#side")["faces"], "up": face([13, 3, 15, 13], "#wood")})]}
    w("assets/ryancraft/models/block/vat.json", vat)
    w("assets/ryancraft/blockstates/vat.json", {"variants": {"": {"model": T + "vat"}}})
    w("assets/ryancraft/items/vat.json", {"model": {"type": "minecraft:model", "model": T + "vat"}})
    # ---- Archimedes screw pump: frame only (the screw and intake tube are drawn by the renderer)
    frame = [el((0, 0, 0), (16, 2, 16), "#iron"), el((0, 14, 0), (16, 16, 16), "#iron")] + \
            [el((x, 2, z), (x + 2, 14, z + 2), "#iron") for (x, z) in ((0, 0), (14, 0), (0, 14), (14, 14))]
    w("assets/ryancraft/models/block/pump.json", {"parent": "block/block", "textures": {"iron": T + "dark_iron", "particle": T + "dark_iron"}, "elements": frame})
    w("assets/ryancraft/blockstates/pump.json", {"variants": {"": {"model": T + "pump"}}})
    w("assets/ryancraft/items/pump.json", {"model": {"type": "minecraft:model", "model": T + "pump"}})
    # ---- cast-iron hand pump (spout faces north in the model; the handle is drawn by the renderer)
    hp = [el((4, 0, 4), (12, 2, 12), "#iron"), el((5, 2, 5), (11, 13, 11), "#iron"), el((4, 13, 4), (12, 15, 12), "#iron"),
          el((7, 7, 0), (9, 9, 5), "#iron"), el((6.5, 6.5, 0), (9.5, 9.5, 1), "#iron"),   # the spout, centred and out to the edge: it meets a pipe's mouth el((7.25, 15, 7.25), (8.75, 16.9, 8.75), "#iron")]
    w("assets/ryancraft/models/block/hand_pump.json", {"parent": "block/block", "textures": {"iron": T + "dark_iron", "particle": T + "dark_iron"}, "elements": hp})
    w("assets/ryancraft/blockstates/hand_pump.json", {"variants": {f"facing={f}": dict({"model": T + "hand_pump"}, **({"y": r} if r else {}))
        for f, r in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    w("assets/ryancraft/items/hand_pump.json", {"model": {"type": "minecraft:model", "model": T + "hand_pump"}})

def data():
    for b in ("vat", "hand_pump"):
        w(f"data/ryancraft/loot_table/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{b}"}],
          "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    rec = {"tank": (["IGI", "G G", "IGI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass"}),
           "vat": (["PIP", "P P", "PPP"], {"P": "#minecraft:planks", "I": "minecraft:iron_ingot"}),
           "hand_pump": ([" II", " B ", " I "], {"I": "minecraft:iron_ingot", "B": "minecraft:bucket"})}
    for n, (pat, key) in rec.items():
        w(f"data/ryancraft/recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": pat, "key": key, "result": {"id": f"ryancraft:{n}", "count": 1}})
    for tag, vals in (("mineable/axe", ["ryancraft:vat"]), ("mineable/pickaxe", ["ryancraft:hand_pump", "ryancraft:tank"])):
        p = os.path.join(D, "minecraft", "tags", "block", tag + ".json")
        t = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
        t["values"] = t["values"] + [v for v in vals if v not in t["values"]]
        w(f"data/minecraft/tags/block/{tag}.json", t)

def lang_entries():
    return {"block.ryancraft.tank": "Glass Tank", "block.ryancraft.vat": "Iron-Hooped Vat", "block.ryancraft.hand_pump": "Hand Pump", "block.ryancraft.pump": "Screw Pump"}

if __name__ == "__main__":
    textures(); models(); data(); print("fluid blocks ok")
