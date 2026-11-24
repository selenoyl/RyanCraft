"""
Wood families for the fifteen eastern US trees: blockstates, models, item models, loot, recipes, tags, lang,
worldgen features, plus door / trapdoor / sapling textures (each species gets its own door and trapdoor design).
  python3 tools/wood.py        (run tools/tree_textures.py first for the log/planks/leaves textures)
The structural JSON (blockstate variant tables, model parents, loot/recipe shapes) is derived from the oak family in
the Minecraft jar in the Gradle cache, the same output a data generator would produce, with our own names and textures.
"""
import glob, json, os, random, sys
import zipfile
sys.path.insert(0, os.path.dirname(__file__))
from tree_textures import TREES, hexc, shade, jit, palette
from PIL import Image
import saplings

ROOT = os.path.join(os.path.dirname(__file__), "..")
RES = os.path.join(ROOT, "src/main/resources")
SPECIES = list(TREES)

def w(path, obj):
    p = os.path.join(RES, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2); f.write("\n")

def jar(kind):
    return zipfile.ZipFile(glob.glob(os.path.join(ROOT, f".gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-{kind}-*/*/*.jar"))[0])

def title(sp): return sp.replace("_", " ").title()

# ---------------------------------------------------------------- textures
TEX = os.path.join(RES, "assets/ryancraft/textures")

def door_textures(k, sp):
    """A door made of the species' planks: frame, panels, and one of several window/panel designs."""
    bark, _, wood, leaf, bloom = TREES[sp]
    r = random.Random(9100 + k); p = palette(wood)
    style = ["two_windows", "one_window", "four_panes", "solid", "arched", "slit"][k % 6]
    planks_vertical = k % 2 == 0
    def plank_px(x, y):
        if planks_vertical: t = ("lt", "md", "lt", "dk")[x % 4]
        else: t = ("hi", "lt", "md", "dk")[y % 4]
        return jit(r, p[t], 4)
    top = Image.new("RGBA", (16, 16)); bot = Image.new("RGBA", (16, 16))
    for img, half in ((top, 0), (bot, 1)):
        px = img.load()
        for x in range(16):
            for y in range(16):
                frame = x in (0, 15) or (half == 0 and y == 0) or (half == 1 and y == 15)
                px[x, y] = (jit(r, p["vdk"], 3) if frame else plank_px(x, y)) + (255,)
    tp, bp = top.load(), bot.load()
    clear = (0, 0, 0, 0)
    def hole(px, x0, y0, x1, y1):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1): px[x, y] = clear
        for x in range(x0 - 1, x1 + 2):
            for y in (y0 - 1, y1 + 1): px[x, y] = jit(r, p["vdk"], 3) + (255,)
        for y in range(y0 - 1, y1 + 2):
            for x in (x0 - 1, x1 + 1): px[x, y] = jit(r, p["vdk"], 3) + (255,)
    def panel(px, x0, y0, x1, y1):
        for x in range(x0, x1 + 1):
            px[x, y0] = jit(r, p["dk"], 3) + (255,); px[x, y1] = jit(r, p["hi"], 3) + (255,)
        for y in range(y0, y1 + 1):
            px[x0, y] = jit(r, p["dk"], 3) + (255,); px[x1, y] = jit(r, p["hi"], 3) + (255,)
    if style == "two_windows": hole(tp, 3, 3, 6, 9); hole(tp, 9, 3, 12, 9)
    elif style == "one_window": hole(tp, 4, 3, 11, 9)
    elif style == "four_panes": hole(tp, 3, 3, 6, 6); hole(tp, 9, 3, 12, 6); hole(tp, 3, 9, 6, 12); hole(tp, 9, 9, 12, 12)
    elif style == "solid": panel(tp, 3, 3, 12, 12)
    elif style == "arched":
        hole(tp, 5, 4, 10, 10); tp[5, 4] = tp[10, 4] = jit(r, p["vdk"], 3) + (255,)
    elif style == "slit": hole(tp, 7, 3, 8, 11)
    panel(bp, 3, 2, 12, 7); panel(bp, 3, 9, 12, 13)
    hx = 13 if k % 3 else 2                                     # handle
    bp[hx, 1] = (70, 70, 74, 255); bp[hx, 2] = (110, 110, 116, 255)
    top.save(f"{TEX}/block/{sp}_door_top.png"); bot.save(f"{TEX}/block/{sp}_door_bottom.png")
    door_icon(top, bot).save(f"{TEX}/item/{sp}_door.png")

def door_icon(top, bot):
    """The item icon: the whole door shrunk to vanilla's door-icon size and spot (10 wide, 14 tall, from x 3, y 2),
    sampling evenly spaced rows and columns that always include the frame's edges, so the outline survives."""
    tall = Image.new("RGBA", (16, 32)); tall.paste(top, (0, 0)); tall.paste(bot, (0, 16))
    xs = [round(i * 15 / 9) for i in range(10)]; ys = [round(j * 31 / 13) for j in range(14)]
    icon = Image.new("RGBA", (16, 16)); ip, tp = icon.load(), tall.load()
    for j, y in enumerate(ys):
        for i, x in enumerate(xs): ip[3 + i, 2 + j] = tp[x, y]
    return icon

def trapdoor_texture(k, sp):
    bark, _, wood, leaf, bloom = TREES[sp]
    r = random.Random(9300 + k); p = palette(wood)
    img = Image.new("RGBA", (16, 16)); px = img.load()
    style = ["grate", "cross", "four_holes", "solid", "slats"][k % 5]
    for x in range(16):
        for y in range(16):
            edge = x in (0, 15) or y in (0, 15)
            px[x, y] = jit(r, p["vdk"] if edge else p[("hi", "lt", "md", "dk")[(x if k % 2 else y) % 4]], 4) + (255,)
    clear = (0, 0, 0, 0)
    if style == "grate":
        for x in (3, 4, 11, 12):
            for y in range(3, 13): px[x, y] = clear
    elif style == "cross":
        for x, y in ((a, b) for a in range(2, 14) for b in range(2, 14)):
            if not (7 <= x <= 8 or 7 <= y <= 8): px[x, y] = clear if (x + y) % 4 == 0 else px[x, y]
    elif style == "four_holes":
        for x0, y0 in ((3, 3), (10, 3), (3, 10), (10, 10)):
            for x in range(x0, x0 + 3):
                for y in range(y0, y0 + 3): px[x, y] = clear
    elif style == "slats":
        for y in (3, 6, 9, 12):
            for x in range(2, 14): px[x, y] = clear
    img.save(f"{TEX}/block/{sp}_trapdoor.png")

# ---------------------------------------------------------------- JSON from the oak family
OAK_PARTS = ["log", "wood", "planks", "slab", "stairs", "fence", "fence_gate", "door", "trapdoor", "button", "pressure_plate", "sapling"]

def swap(text, sp):
    text = text.replace("minecraft:block/stripped_oak_", f"ryancraft:block/stripped_{sp}_")
    text = text.replace("minecraft:stripped_oak_", f"ryancraft:stripped_{sp}_")
    text = text.replace("minecraft:block/oak_", f"ryancraft:block/{sp}_")
    text = text.replace("minecraft:item/oak_", f"ryancraft:item/{sp}_")
    text = text.replace("#minecraft:oak_logs", f"#ryancraft:{sp}_logs")
    text = text.replace("minecraft:oak_", f"ryancraft:{sp}_")
    return text

FRUIT = {"black_cherry": ("ryancraft:cherries", 5), "black_walnut": ("ryancraft:black_walnut", 4)}

def resources():
    cj, dj = jar("clientOnly"), jar("common")
    names = []
    for sp in SPECIES:
        blocks = [f"{sp}_{p}" for p in OAK_PARTS] + [f"stripped_{sp}_log", f"stripped_{sp}_wood"]
        for b in blocks:
            oak = b.replace(sp, "oak")
            names.append(b)
            for path in (f"assets/minecraft/blockstates/{oak}.json", f"assets/minecraft/items/{oak}.json"):
                w(path.replace("minecraft", "ryancraft", 1).replace(oak, b), json.loads(swap(cj.read(path).decode(), sp)))
            w(f"data/ryancraft/loot_table/blocks/{b}.json", json.loads(swap(dj.read(f"data/minecraft/loot_table/blocks/{oak}.json").decode(), sp)))
        for f in cj.namelist():
            base = os.path.basename(f)
            if (f.startswith("assets/minecraft/models/block/") or f.startswith("assets/minecraft/models/item/")) and \
                    (base.startswith("oak_") or base.startswith("stripped_oak_")) and "sign" not in base and "shelf" not in base \
                    and "boat" not in base and "leaves" not in base:
                w(f.replace("minecraft", "ryancraft", 1).replace(base, base.replace("oak", sp, 1)), json.loads(swap(cj.read(f).decode(), sp)))
        for f in dj.namelist():
            base = os.path.basename(f)
            if f.startswith("data/minecraft/recipe/") and (base.startswith("oak_") or base.startswith("stripped_oak_")) \
                    and not any(s in base for s in ("sign", "shelf", "boat")):
                w(f"data/ryancraft/recipe/{base.replace('oak', sp, 1)}", json.loads(swap(dj.read(f).decode(), sp)))
        # leaves: our own untinted cube (colour is in the texture), birch-style loot (sapling, sticks; no apples)
        names.append(f"{sp}_leaves")
        w(f"assets/ryancraft/blockstates/{sp}_leaves.json", {"variants": {"": {"model": f"ryancraft:block/{sp}_leaves"}}})
        w(f"assets/ryancraft/models/block/{sp}_leaves.json", {"parent": "minecraft:block/cube_all" if sp == "eastern_red_cedar" else "minecraft:block/leaves",   # tintable: drab in winter
           "textures": {"all": f"ryancraft:block/{sp}_leaves"}})
        w(f"assets/ryancraft/items/{sp}_leaves.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:block/{sp}_leaves"}})
        w(f"data/ryancraft/loot_table/blocks/{sp}_leaves.json",
          json.loads(dj.read("data/minecraft/loot_table/blocks/birch_leaves.json").decode().replace("minecraft:birch_", f"ryancraft:{sp}_")))
        # potted sapling
        names.append(f"potted_{sp}_sapling")
        w(f"assets/ryancraft/blockstates/potted_{sp}_sapling.json", {"variants": {"": {"model": f"ryancraft:block/potted_{sp}_sapling"}}})
        w(f"assets/ryancraft/models/block/potted_{sp}_sapling.json", json.loads(swap(cj.read("assets/minecraft/models/block/potted_oak_sapling.json").decode(), sp)))
        w(f"data/ryancraft/loot_table/blocks/potted_{sp}_sapling.json", json.loads(swap(dj.read("data/minecraft/loot_table/blocks/potted_oak_sapling.json").decode(), sp)))
        # fruit trees drop their fruit from leaves the way oaks drop apples, only more often
        if sp in FRUIT:
            fruit, mult = FRUIT[sp]
            loot = json.loads(swap(dj.read("data/minecraft/loot_table/blocks/oak_leaves.json").decode(), sp).replace("minecraft:apple", fruit))
            for pool in loot["pools"]:
                for e in pool["entries"]:
                    if e.get("name") == fruit:
                        for c in e["conditions"]:
                            if c["condition"] == "minecraft:table_bonus": c["chances"] = [round(x * mult, 4) for x in c["chances"]]
            w(f"data/ryancraft/loot_table/blocks/{sp}_leaves.json", loot)
        # worldgen: the configured feature (one variable tree) and where it is placed
        w(f"data/ryancraft/worldgen/configured_feature/{sp}.json", {"type": "ryancraft:species_tree", "config": {"species": sp}})
        w(f"data/ryancraft/tags/block/{sp}_logs.json", {"values": [f"ryancraft:{sp}_log", f"ryancraft:{sp}_wood", f"ryancraft:stripped_{sp}_log", f"ryancraft:stripped_{sp}_wood"]})
        w(f"data/ryancraft/tags/item/{sp}_logs.json", {"values": [f"ryancraft:{sp}_log", f"ryancraft:{sp}_wood", f"ryancraft:stripped_{sp}_log", f"ryancraft:stripped_{sp}_wood"]})
    # placements: (name, species, one-in-N chunks per attempt, extra attempts)
    PLACED = [(f"{sp}_forest", sp, n) for sp, n in [("white_oak", 3), ("red_oak", 3), ("sugar_maple", 3), ("red_maple", 3),
              ("tulip_poplar", 4), ("shagbark_hickory", 5), ("black_walnut", 5), ("american_beech", 4), ("black_cherry", 5),
              ("sassafras", 5), ("ohio_buckeye", 6)]]
    PLACED += [("eastern_redbud_understory", "eastern_redbud", 4), ("flowering_dogwood_understory", "flowering_dogwood", 4),
               ("sycamore_riverbank", "sycamore", 6), ("sycamore_forest", "sycamore", 10),
               ("eastern_red_cedar_open", "eastern_red_cedar", 5), ("white_oak_lone", "white_oak", 24)]
    for name, sp, chance in PLACED:
        w(f"data/ryancraft/worldgen/placed_feature/{name}.json", {"feature": f"ryancraft:{sp}", "placement": [
            {"type": "minecraft:rarity_filter", "chance": chance},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
            {"type": "minecraft:biome"}, {"type": "ryancraft:temperate_surroundings"},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
                "state": {"Name": f"ryancraft:{sp}_sapling", "Properties": {"stage": "0"}}}}]})
    # fruit recipes: cherry pie like pumpkin pie; a walnut is cracked open in the crafting grid
    w("data/ryancraft/recipe/cherry_pie.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
      "ingredients": ["ryancraft:cherries", "ryancraft:cherries", "ryancraft:cherries", "minecraft:sugar", "#minecraft:eggs"],
      "result": {"count": 1, "id": "ryancraft:cherry_pie"}})
    w("data/ryancraft/recipe/shelled_walnut.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
      "ingredients": ["ryancraft:black_walnut"], "result": {"count": 1, "id": "ryancraft:shelled_walnut"}})
    # vanilla tags
    def each(fmt): return [fmt.format(sp=sp) for sp in SPECIES]
    shared = {
        "logs_that_burn": each("#ryancraft:{sp}_logs"), "planks": each("ryancraft:{sp}_planks"),
        "wooden_slabs": each("ryancraft:{sp}_slab"), "wooden_stairs": each("ryancraft:{sp}_stairs"),
        "wooden_fences": each("ryancraft:{sp}_fence"), "fence_gates": each("ryancraft:{sp}_fence_gate"),
        "wooden_doors": each("ryancraft:{sp}_door"), "wooden_trapdoors": each("ryancraft:{sp}_trapdoor"),
        "wooden_buttons": each("ryancraft:{sp}_button"), "wooden_pressure_plates": each("ryancraft:{sp}_pressure_plate"),
        "leaves": each("ryancraft:{sp}_leaves"), "saplings": each("ryancraft:{sp}_sapling"),
    }
    for kind in ("block", "item"):
        for tag, vals in shared.items(): w(f"data/minecraft/tags/{kind}/{tag}.json", {"replace": False, "values": vals})
    w("data/minecraft/tags/block/overworld_natural_logs.json", {"replace": False, "values": each("ryancraft:{sp}_log")})

def lang_entries():
    lang = {}
    for sp in SPECIES:
        t = title(sp)
        for part in OAK_PARTS + ["leaves"]:
            lang[f"block.ryancraft.{sp}_{part}"] = f"{t} {part.replace('_', ' ').title()}"
        lang[f"block.ryancraft.stripped_{sp}_log"] = f"Stripped {t} Log"
        lang[f"block.ryancraft.stripped_{sp}_wood"] = f"Stripped {t} Wood"
        lang[f"block.ryancraft.potted_{sp}_sapling"] = f"Potted {t} Sapling"
    lang.update({"item.ryancraft.cherries": "Cherries", "item.ryancraft.cherry_pie": "Cherry Pie",
                 "item.ryancraft.black_walnut": "Black Walnut", "item.ryancraft.shelled_walnut": "Shelled Walnut"})
    return lang

if __name__ == "__main__":
    os.makedirs(f"{TEX}/item", exist_ok=True)
    for k, sp in enumerate(SPECIES):
        door_textures(k, sp); trapdoor_texture(k, sp); saplings.sapling(sp).save(f"{TEX}/block/{sp}_sapling.png")
    resources()
    p = os.path.join(RES, "assets/ryancraft/lang/en_us.json")
    lang = json.load(open(p)); lang.update(lang_entries())
    with open(p, "w") as f: json.dump(dict(sorted(lang.items())), f, indent=2, ensure_ascii=False); f.write("\n")
    print("ok")
