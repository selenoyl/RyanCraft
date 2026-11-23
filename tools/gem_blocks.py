"""Jade and lapis building blocks: bricks (vanilla stone-brick layout in the gem's colours), brick stairs, slabs and
walls, and chiseled blocks with a carved motif (jade: the moon rabbit under its moon; lapis: a star).
  python3 tools/gem_blocks.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); RES = M.RES; TEX = os.path.join(M.A, "textures", "block")
cb = lambda n: os.path.join(M.CLASSIC, "block", n + ".png")

GEMS = {"jade": {"block": "ryancraft:jade_block", "dark": (30, 92, 56), "light": (128, 202, 146), "fleck": None},
        "lapis": {"block": "minecraft:lapis_block", "dark": (24, 44, 120), "light": (82, 118, 214), "fleck": (226, 190, 76)}}

def lang_entries():
    out = {}
    for g, title in (("jade", "Jade"), ("lapis", "Lapis")):
        out[f"block.ryancraft.{g}_bricks"] = f"{title} Bricks"; out[f"block.ryancraft.{g}_brick_stairs"] = f"{title} Brick Stairs"
        out[f"block.ryancraft.{g}_brick_slab"] = f"{title} Brick Slab"; out[f"block.ryancraft.{g}_brick_wall"] = f"{title} Brick Wall"
        out[f"block.ryancraft.chiseled_{g}"] = f"Chiseled {title}"
    return out

RABBIT = [   # a sitting moon rabbit, facing left, with its moon: "#" carved groove, "+" lit edge
    "................",
    "................",
    "..........++....",
    ".........+##+...",
    "...#.#...+##+...",
    "...#.#....++....",
    "...###..........",
    "..####..........",
    "..#####.........",
    "...######.......",
    "...#######......",
    "...########.....",
    "....#######.....",
    "....##..##......",
    "................",
    "................"]
STAR = [
    "................",
    "................",
    "................",
    ".......#........",
    ".......#........",
    "......###.......",
    "..###########...",
    "....#######.....",
    ".....#####......",
    ".....##.##......",
    "....##...##.....",
    "....#.....#.....",
    "................",
    "................",
    "................",
    "................"]

def bricks(g):
    p = GEMS[g]
    img = M.mapped(cb("stone_bricks"), p["dark"], p["light"]); px = img.load(); n = M.vnoise((21 if g == "jade" else 22), 4)
    for y in range(16):
        for x in range(16): px[x, y] = M.shade_px(px[x, y], 0.93 + 0.14 * n[y][x])
    if p["fleck"]: M.dots(img, random.Random(5), 5, p["fleck"])
    return img

def chiseled(g, motif):
    p = GEMS[g]
    img = M.rock((21 if g == "jade" else 22), p["dark"], p["light"], 0.12)
    mid = tuple((a + b) // 2 for a, b in zip(p["dark"], p["light"]))
    img = M.frame(img, tuple(min(255, c + 30) for c in p["light"]), tuple(int(c * 0.6) for c in p["dark"]))
    px = img.load(); groove = tuple(int(c * 0.7) for c in p["dark"]); lit = tuple(min(255, c + 40) for c in p["light"])
    for y, row in enumerate(motif):
        for x, ch in enumerate(row):
            if ch == "#": px[x, y] = groove + (255,)
            elif ch == "+": px[x, y] = lit + (255,)
    for y in range(15):                                   # a lit lower-right lip on every groove pixel
        for x in range(15):
            if motif[y][x] == "#" and motif[y + 1][x + 1] == ".": px[x + 1, y + 1] = M.shade_px(px[x + 1, y + 1], 1.15)
    if p["fleck"]: M.dots(img, random.Random(9), 3, p["fleck"])
    return img

def json_files():
    for g in GEMS:
        blk = GEMS[g]["block"]
        cubes = [f"{g}_bricks", f"chiseled_{g}"]
        for n in cubes:
            M.w(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{n}"}}})
            M.w(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
            M.w(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{n}"}})
        dst, tex = f"{g}_brick", f"{g}_bricks"
        for kind in ("stairs", "slab", "wall"):
            bs = json.load(open(os.path.join(M.TPL, "blockstates", f"stone_brick_{kind}.json")))
            M.w(f"assets/ryancraft/blockstates/{dst}_{kind}.json", M.renamed(bs, f"stone_brick_{kind}", f"{dst}_{kind}", tex))
            for f in os.listdir(os.path.join(M.TPL, "models", "block")):
                if f.startswith(f"stone_brick_{kind}"):
                    m = json.load(open(os.path.join(M.TPL, "models", "block", f)))
                    M.w(f"assets/ryancraft/models/block/{dst}{f[len('stone_brick'):]}", M.renamed(m, f"stone_brick_{kind}", f"{dst}_{kind}", tex))
            M.w(f"assets/ryancraft/items/{dst}_{kind}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{dst}_{kind}" + ("_inventory" if kind == "wall" else "")}})
        L = "data/ryancraft/loot_table/blocks/"
        for n in (f"{g}_bricks", f"chiseled_{g}", f"{dst}_stairs", f"{dst}_wall"): M.w(L + n + ".json", M.drops(n))
        M.w(L + f"{dst}_slab.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{dst}_slab", "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{"condition": "minecraft:block_state_property", "block": f"ryancraft:{dst}_slab",
             "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]}]}]})
        R = "data/ryancraft/recipe/"
        sh = lambda name, pat, key, n, cat="building": M.w(f"{R}{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pat,
            "key": key, "result": {"id": f"ryancraft:{name}", "count": n}})
        sh(f"{g}_bricks", ["##", "##"], {"#": blk}, 4)
        sh(f"{dst}_stairs", ["#  ", "## ", "###"], {"#": f"ryancraft:{tex}"}, 4)
        sh(f"{dst}_slab", ["###"], {"#": f"ryancraft:{tex}"}, 6)
        sh(f"{dst}_wall", ["###", "###"], {"#": f"ryancraft:{tex}"}, 6, "misc")
        sh(f"chiseled_{g}", ["#", "#"], {"#": f"ryancraft:{dst}_slab"}, 1)
        for src in (blk, f"ryancraft:{tex}"):
            for name, n in ((f"{g}_bricks", 1), (f"{dst}_stairs", 1), (f"{dst}_slab", 2), (f"{dst}_wall", 1), (f"chiseled_{g}", 1)):
                if src.endswith(tex) and name == f"{g}_bricks": continue
                M.w(f"{R}{name}_from_{src.split(':')[1]}_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": src, "result": {"id": f"ryancraft:{name}", "count": n}})
        names = [f"ryancraft:{n}" for n in (f"{g}_bricks", f"chiseled_{g}", f"{dst}_stairs", f"{dst}_slab", f"{dst}_wall")]
        M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", names)
        M.add_tag("data/minecraft/tags/block/needs_stone_tool.json", names)
        for kind, k in (("stairs", "stairs"), ("slabs", "slab"), ("walls", "wall")):
            M.add_tag(f"data/minecraft/tags/block/{kind}.json", [f"ryancraft:{dst}_{k}"]); M.add_tag(f"data/minecraft/tags/item/{kind}.json", [f"ryancraft:{dst}_{k}"])

if __name__ == "__main__":
    for g in GEMS: bricks(g).save(os.path.join(TEX, f"{g}_bricks.png"))
    chiseled("jade", RABBIT).save(os.path.join(TEX, "chiseled_jade.png"))
    chiseled("lapis", STAR).save(os.path.join(TEX, "chiseled_lapis.png"))
    json_files(); print("gem blocks ok")
