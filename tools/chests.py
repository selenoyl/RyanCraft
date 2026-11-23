"""Special chests (storage/RyanChests.java), drawn on vanilla's chest model: textures in vanilla's 64x64 chest layout
(templates/chests/normal.png), each face given a frame and a fill:
  obsidian_chest    polished obsidian frame, classic obsidian panels, the iron latch
  diamond_chest     diamond frame and latch round clear glass panels (drawn cutout, like glass)
  lead_glass_chest  lead frame and latch round leaded glass panels (drawn cutout)
Plus block and item models (vanilla's special chest item model), loot, recipes, tags and lang.
  python3 tools/chests.py      (tools/resources.py merges the lang)"""
import json, os
from PIL import Image
import moon_assets as M
import metals_assets as MA

HERE = os.path.dirname(os.path.abspath(__file__)); OUT = os.path.join(M.TEX, "entity", "chest")
TPL = Image.open(os.path.join(HERE, "templates", "chests", "normal.png")).convert("RGBA")
cb = lambda n: os.path.join(M.CLASSIC, "block", n + ".png")

def faces(u, v, w, h, d):
    return [(u + d, v, w, d), (u + d + w, v, w, d), (u, v + d, d, h), (u + d, v + d, w, h), (u + d + w, v + d, d, h), (u + 2 * d + w, v + d, w, h)]
FACES = faces(0, 0, 14, 5, 14) + faces(0, 19, 14, 10, 14)
LOCK = (0, 0, 6, 5)
lum = lambda c: sum(c[:3]) / 3

def style(frame_pal, fill, inside, lock_pal):
    """fill(x, y, fx, fy) -> RGBA for a face's inner pixel (fx, fy measured from the face corner)."""
    out = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); po = out.load(); pt = TPL.load()
    for fu, fv, fw, fh in FACES:
        rect = [(fu + i, fv + j) for j in range(fh) for i in range(fw)]
        dark = sum(lum(pt[x, y]) for x, y in rect) / len(rect) < 50        # the inside of the lid and the chest floor
        for x, y in rect:
            fx, fy = x - fu, y - fv
            if fx in (0, fw - 1) or fy in (0, fh - 1): po[x, y] = MA.grad(min(1, lum(pt[x, y]) / 150), frame_pal) + (255,)
            else: po[x, y] = inside(x, y, fx, fy) if dark else fill(x, y, fx, fy)
    lx, ly, lw, lh = LOCK
    for y in range(ly, ly + lh):
        for x in range(lx, lx + lw):
            if pt[x, y][3]: po[x, y] = MA.grad(min(1, lum(pt[x, y]) / 200), lock_pal) + (255,)
    return out

def tile(path):
    im = Image.open(path).convert("RGBA").crop((0, 0, 16, 16)).load()
    return lambda x, y, fx, fy: im[fx % 16, fy % 16]

def glass(tint):
    """Clear glass, drawn cutout like vanilla's: open panes with a pair of bright diagonal glints."""
    def f(x, y, fx, fy):
        return tint + (255,) if (fx + fy) % 11 in (3, 4) and 2 <= fy < 9 else (0, 0, 0, 0)
    return f

def cutout(fn):
    """Snap a fill to fully clear or fully solid, so it draws cutout and the contents show through."""
    def f(*a):
        c = fn(*a); return c[:3] + (255,) if c[3] >= 128 else (0, 0, 0, 0)
    return f

def textures():
    os.makedirs(OUT, exist_ok=True)
    obs = tile(cb("obsidian"))
    style([(14, 10, 22), (40, 30, 58), (78, 62, 104)], obs, lambda *a: (10, 8, 16, 255),
          [(40, 40, 44), (120, 120, 128), (200, 200, 208)]).save(os.path.join(OUT, "obsidian_chest.png"))
    diamond = [(18, 70, 74), (48, 160, 160), (120, 226, 222), (220, 255, 252)]
    empty = lambda *a: (0, 0, 0, 0)                                       # see-through chests: no inside floor or lid lining
    style(diamond, glass((226, 250, 252)), empty, diamond).save(os.path.join(OUT, "diamond_chest.png"))
    lead = [(26, 26, 34), (62, 62, 76), (110, 112, 128)]
    lg = cutout(tile(os.path.join(M.TEX, "block", "leaded_glass.png")))
    style(lead, lg, empty, lead + [(170, 172, 186)]).save(os.path.join(OUT, "lead_glass_chest.png"))

KINDS = {"obsidian_chest": ("Obsidian Chest", "minecraft:block/obsidian"), "diamond_chest": ("Diamond Chest", "minecraft:block/diamond_block"),
         "lead_glass_chest": ("Lead Glass Chest", "ryancraft:block/leaded_glass")}

def lang_entries():
    out = {}
    for k, (t, _) in KINDS.items(): out[f"block.ryancraft.{k}"] = t; out[f"container.ryancraft.{k}"] = t
    return out

def build():
    W, R, L = M.w, "data/ryancraft/recipe/", "data/ryancraft/loot_table/blocks/"
    for k, (t, particle) in KINDS.items():
        W(f"assets/ryancraft/blockstates/{k}.json", {"variants": {"": {"model": f"ryancraft:block/{k}"}}})
        W(f"assets/ryancraft/models/block/{k}.json", {"textures": {"particle": particle}})
        W(f"assets/ryancraft/models/item/{k}.json", {"parent": "minecraft:item/template_chest", "textures": {"particle": particle}})
        W(f"assets/ryancraft/items/{k}.json", {"model": {"type": "minecraft:special", "base": f"ryancraft:item/{k}",
            "model": {"type": "minecraft:chest", "texture": f"ryancraft:{k}"}}})
        W(L + k + ".json", M.drops(k))
    sh = lambda name, pat, key: W(f"{R}{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pat, "key": key,
        "result": {"id": f"ryancraft:{name}", "count": 1}})
    sh("obsidian_chest", ["OOO", "OCO", "OOO"], {"O": "minecraft:obsidian", "C": "minecraft:chest"})
    sh("diamond_chest", ["GDG", "DCD", "GDG"], {"G": "minecraft:glass", "D": "minecraft:diamond", "C": "minecraft:chest"})
    sh("lead_glass_chest", ["LGL", "GCG", "LGL"], {"L": "ryancraft:lead_ingot", "G": "ryancraft:leaded_glass", "C": "minecraft:chest"})
    ids = [f"ryancraft:{k}" for k in KINDS]
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ids)
    M.add_tag("data/minecraft/tags/block/needs_diamond_tool.json", ["ryancraft:obsidian_chest"])
    M.add_tag("data/minecraft/tags/block/wither_immune.json", ["ryancraft:obsidian_chest"])
    M.add_tag("data/minecraft/tags/block/dragon_immune.json", ["ryancraft:obsidian_chest"])
    M.add_tag("data/c/tags/block/chests.json", ids); M.add_tag("data/c/tags/item/chests.json", ids)

if __name__ == "__main__":
    textures(); build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("chests ok")
