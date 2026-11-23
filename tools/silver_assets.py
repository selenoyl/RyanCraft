"""The silver tarnish set (metal/Silver.java): four stages (silver, dulled, tarnished, blackened) of the block, cut silver,
its stairs and slab, chiseled silver, grate, door, trapdoor and bulb, plus waxed twins. Textures gradient-map vanilla's
copper set (templates/silver) onto each stage's palette; blockstates, models, item definitions and loot are vanilla's
copper files renamed. Waxed blocks reuse the plain models.
  python3 tools/silver_assets.py      (tools/resources.py merges the lang)"""
import json, os, re
from PIL import Image
import metals_assets as MA

HERE = os.path.dirname(os.path.abspath(__file__)); RES = MA.RES; A = MA.A; TEX = MA.TEX
TPL = os.path.join(HERE, "templates", "silver"); VA = os.path.join(TPL, "assets", "minecraft"); VD = os.path.join(TPL, "data", "minecraft")
STAGES = ["", "dulled_", "tarnished_", "blackened_"]; TITLES = ["", "Dulled ", "Tarnished ", "Blackened "]
PALS = [
    [(78, 84, 92), (150, 156, 166), (208, 212, 220), (248, 250, 255)],      # bright silver
    [(82, 80, 72), (142, 138, 124), (190, 184, 164), (224, 218, 196)],      # dulled: a warm yellow-grey film
    [(58, 46, 44), (104, 84, 74), (148, 124, 100), (186, 166, 136)],        # tarnished: bronze-brown
    [(24, 24, 34), (46, 46, 64), (76, 78, 100), (116, 120, 142)],           # blackened: blue-black
]
SHAPES = ["silver_block", "cut_silver", "cut_silver_stairs", "cut_silver_slab", "chiseled_silver", "silver_grate", "silver_door", "silver_trapdoor", "silver_bulb"]
COPPER = {"cut_silver": "cut_copper", "cut_silver_stairs": "cut_copper_stairs", "cut_silver_slab": "cut_copper_slab", "chiseled_silver": "chiseled_copper",
          "silver_grate": "copper_grate", "silver_door": "copper_door", "silver_trapdoor": "copper_trapdoor", "silver_bulb": "copper_bulb"}
TOKEN = re.compile(r"minecraft:((?:block/|item/)?)(cut_copper\w*|chiseled_copper\w*|copper_(?:door|trapdoor|bulb|grate)\w*)")

def rename(name, st):
    return name.replace("cut_copper", f"{st}cut_silver").replace("chiseled_copper", f"{st}chiseled_silver").replace("copper_", f"{st}silver_")

def renamed(obj, st, ids_st=None):
    """Swap copper references for silver; models and textures use stage `st`, block/item ids use `ids_st`."""
    ids_st = st if ids_st is None else ids_st
    txt = TOKEN.sub(lambda m: "ryancraft:" + m.group(1) + rename(m.group(2), st if m.group(1) else ids_st), json.dumps(obj))
    return json.loads(txt)

def keep(c):
    """Pixels the recolour leaves alone: the bulb's warm glow and its redstone dot."""
    r, g, b = c[:3]
    return (r > 150 and g < 70 and b < 70) or (r > 200 and g > 170 and b < 150 and g / max(r, 1) > 0.78)

def tint(src, pal, mask_keep=False):
    im = Image.open(src).convert("RGBA"); px = im.load()
    pts = [(x, y) for y in range(im.height) for x in range(im.width) if px[x, y][3] and not (mask_keep and keep(px[x, y]))]
    vals = [sum(px[x, y][:3]) / 3 for x, y in pts]; lo, hi = min(vals), max(vals)
    for x, y in pts:
        c = px[x, y]; px[x, y] = MA.grad((sum(c[:3]) / 3 - lo) / max(1, hi - lo), pal) + (c[3],)
    return im

def textures():
    B, I = os.path.join(VA, "textures", "block"), os.path.join(VA, "textures", "item")
    for s, st in enumerate(STAGES):
        pal = PALS[s]
        if s: MA.recolour(os.path.join(MA.CLASSIC, "block", "iron_block.png"), pal).save(os.path.join(TEX, "block", f"{st}silver_block.png"))
        for f in os.listdir(B):
            tint(os.path.join(B, f), pal, "bulb" in f).save(os.path.join(TEX, "block", rename(f, st)))
        tint(os.path.join(I, "copper_door.png"), pal).save(os.path.join(TEX, "item", f"{st}silver_door.png"))

def lang_entries():
    out = {}
    for s, st in enumerate(STAGES):
        for wx in (False, True):
            pre = ("Waxed " if wx else "") + TITLES[s]
            for k in SHAPES:
                if k == "silver_block": t = f"{pre}Silver" if s else f"{pre}Block of Silver"
                else: t = pre + " ".join(p.title() for p in k.split("_"))
                if s == 0 and not wx and k == "silver_block": continue      # Metals names the plain block
                out[f"block.ryancraft.{'waxed_' if wx else ''}{st}{k}"] = t.strip()
    return out

def ld(path): return json.load(open(path))

def json_files():
    W, R, L = MA.w, "data/ryancraft/recipe/", "data/ryancraft/loot_table/blocks/"
    every = []
    for s, st in enumerate(STAGES):
        for wx in (False, True):
            ids = ("waxed_" if wx else "") + st
            for k in SHAPES:
                n = ids + k; every.append(n)
                if k == "silver_block":
                    if not (s == 0 and not wx):
                        W(f"assets/ryancraft/blockstates/{n}.json", {"variants": {"": {"model": f"ryancraft:block/{st}silver_block"}}})
                        W(f"assets/ryancraft/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{st}silver_block"}})
                        if not wx: W(f"assets/ryancraft/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"ryancraft:block/{n}"}})
                        W(L + n + ".json", MA_drop(n))
                    continue
                c = COPPER[k]
                W(f"assets/ryancraft/blockstates/{n}.json", renamed(ld(os.path.join(VA, "blockstates", f"{c}.json")), st))
                W(f"assets/ryancraft/items/{n}.json", renamed(ld(os.path.join(VA, "items", f"{c}.json")), st))
                if k == "silver_door": W(L + n + ".json", renamed(ld(os.path.join(VD, "loot_table", "blocks", "copper_door.json")), st, ids))
                elif k == "cut_silver_slab": W(L + n + ".json", renamed(ld(os.path.join(VD, "loot_table", "blocks", "cut_copper_slab.json")), st, ids))
                else: W(L + n + ".json", MA_drop(n))
        for f in os.listdir(os.path.join(VA, "models", "block")):     # models once per stage (waxed share them)
            W(f"assets/ryancraft/models/block/{rename(f, st)}", renamed(ld(os.path.join(VA, "models", "block", f)), st))
        W(f"assets/ryancraft/models/item/{st}silver_door.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{st}silver_door"}})
        # recipes, for plain and waxed alike
        for wx in (False, True):
            p = ("waxed_" if wx else "") + st; r = lambda x: f"ryancraft:{p}{x}"
            sh = lambda name, pat, key, cnt, cat="building": W(f"{R}{p}{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pat,
                "key": key, "result": {"id": r(name), "count": cnt}})
            sh("cut_silver", ["##", "##"], {"#": r("silver_block")}, 4)
            sh("cut_silver_stairs", ["#  ", "## ", "###"], {"#": r("cut_silver")}, 4)
            sh("cut_silver_slab", ["###"], {"#": r("cut_silver")}, 6)
            sh("chiseled_silver", ["#", "#"], {"#": r("cut_silver_slab")}, 1)
            sh("silver_grate", [" # ", "# #", " # "], {"#": r("silver_block")}, 4)
            for name, cnt, src in (("cut_silver", 4, "silver_block"), ("cut_silver_stairs", 4, "silver_block"), ("cut_silver_slab", 8, "silver_block"),
                                   ("chiseled_silver", 4, "silver_block"), ("silver_grate", 4, "silver_block"), ("cut_silver_stairs", 1, "cut_silver"),
                                   ("cut_silver_slab", 2, "cut_silver"), ("chiseled_silver", 1, "cut_silver")):
                W(f"{R}{p}{name}_from_{p}{src}_stonecutting.json", {"type": "minecraft:stonecutting", "ingredient": r(src), "result": {"id": r(name), "count": cnt}})
            if wx:
                for k in SHAPES:
                    W(f"{R}{p}{k}_from_honeycomb.json", {"type": "minecraft:crafting_shapeless", "category": "building",
                        "ingredients": [f"ryancraft:{st}{k}", "minecraft:honeycomb"], "group": f"{p}{k}", "result": {"id": f"ryancraft:{p}{k}", "count": 1}})
        if s == 0:
            I = "ryancraft:silver_ingot"
            sh0 = lambda name, pat, key, cnt, cat: W(f"{R}{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pat,
                "key": key, "result": {"id": f"ryancraft:{name}", "count": cnt}})
            sh0("silver_door", ["##", "##", "##"], {"#": I}, 3, "redstone")
            sh0("silver_trapdoor", ["##", "##"], {"#": I}, 1, "redstone")
            sh0("silver_bulb", [" S ", "SBS", " R "], {"S": "ryancraft:silver_block", "B": "minecraft:blaze_rod", "R": "minecraft:redstone"}, 4, "redstone")
    names = [f"ryancraft:{n}" for n in every]
    MA.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", names)
    MA.add_tag("data/minecraft/tags/block/needs_stone_tool.json", names)
    MA.add_tag("data/minecraft/tags/block/beacon_base_blocks.json", [x for x in names if x.endswith("silver_block")])
    for tag, k in (("stairs", "cut_silver_stairs"), ("slabs", "cut_silver_slab"), ("doors", "silver_door"), ("trapdoors", "silver_trapdoor")):
        sel = [x for x in names if x.endswith(k)]
        MA.add_tag(f"data/minecraft/tags/block/{tag}.json", sel); MA.add_tag(f"data/minecraft/tags/item/{tag}.json", sel)

def MA_drop(n):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"ryancraft:{n}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}

if __name__ == "__main__":
    textures(); json_files()
    p = os.path.join(A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("silver ok")
