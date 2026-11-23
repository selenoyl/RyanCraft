"""Jade and lapis building blocks: bricks (vanilla stone-brick layout in the gem's colours), brick stairs, slabs and
walls, and chiseled blocks: the gem block's own face carved into vanilla's stone-brick and chiseled patterns.
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

def gem_surface(g):
    """The gem block's own face: our Block of Jade, or the classic lapis block."""
    src = os.path.join(TEX, "jade_block.png") if g == "jade" else cb("lapis_block")
    return Image.open(src).convert("RGBA").crop((0, 0, 16, 16))

def relief(g, template, strength=1.0):
    """The gem block's face carved into a vanilla pattern: the template's shading (mortar, bevels, chisel cuts) is
    laid over the gem's own texture, so it reads as that block cut and dressed."""
    base = gem_surface(g); t = Image.open(cb(template)).convert("RGBA").crop((0, 0, 16, 16))
    lum = [[sum(t.getpixel((x, y))[:3]) / 3 for x in range(16)] for y in range(16)]
    mean = sum(sum(r) for r in lum) / 256
    out = base.copy(); px = out.load()
    for y in range(16):
        for x in range(16):
            k = (lum[y][x] / mean) ** strength
            px[x, y] = M.shade_px(px[x, y], k)
    return out

def bricks(g): return relief(g, "stone_bricks", 1.1)

def chiseled(g, motif=None): return relief(g, "chiseled_stone_bricks", 1.1)

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
    chiseled("jade").save(os.path.join(TEX, "chiseled_jade.png"))
    chiseled("lapis").save(os.path.join(TEX, "chiseled_lapis.png"))
    json_files(); print("gem blocks ok")
