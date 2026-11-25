"""Moon life (entity/MoonRabbit.java, moon/MoonHerbBlock.java): the moon rabbit's silvery coat (vanilla's white rabbit,
cooled to moon silver with lavender shading, deep blue eyes; pink nose kept), the moon herb's sprite and models, loot, the
herb patches and rabbit spawns in the moon biome, spawn egg and lang.
  python3 tools/moon_life.py"""
import colorsys, json, os
from PIL import Image
import moon_assets as M
from critters import egg_icon, save_item

HERE = os.path.dirname(os.path.abspath(__file__)); TPL = os.path.join(HERE, "templates", "moon_life")
ENT, BLOCK = os.path.join(M.A, "textures", "entity"), os.path.join(M.A, "textures", "block")
SILVER = [(92, 94, 128), (150, 152, 182), (204, 206, 226), (240, 242, 252)]
EYE = (54, 82, 176)
LANG = {"entity.ryancraft.moon_rabbit": "Moon Rabbit", "item.ryancraft.moon_rabbit_spawn_egg": "Moon Rabbit Spawn Egg",
        "block.ryancraft.moon_herb": "Moon Herb", "block.ryancraft.potted_moon_herb": "Potted Moon Herb"}

def grad(t, pal):
    t = min(1, max(0, t)); i = min(len(pal) - 2, int(t * (len(pal) - 1))); f = t * (len(pal) - 1) - i
    return tuple(int(pal[i][k] + (pal[i + 1][k] - pal[i][k]) * f) for k in range(3))

def rabbit():
    im = Image.open(os.path.join(TPL, "white.png")).convert("RGBA"); px = im.load()
    vals = [sum(px[x, y][:3]) / 3 for y in range(im.height) for x in range(im.width) if px[x, y][3]]
    lo, hi = min(vals), max(vals)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if r > 130 and g < 110 and b < 120 and r - g > 50: px[x, y] = (EYE if r > 180 else tuple(c - 24 for c in EYE)) + (a,); continue   # red eyes -> deep moon blue
            if s > 0.2 and (h < 0.05 or h > 0.85): continue                     # pink nose, inner ears
            if v < 0.15: continue
            px[x, y] = grad((sum((r, g, b)) / 3 - lo) / max(1, hi - lo), SILVER) + (a,)
    return im

HERB = ["................", "................", "................", "......c..c......", ".....cLc.Lc.....", "......L..L......", "..c...Ll.L...c..",
        ".cLc..lL.Ll.cLc.", "..L....LlL....L.", "..lL...lLl..Ll..", "...Ll..lL..lL...", "....LlllLllL....", ".....lLLLLl.....", "......lLLl......",
        ".......ll.......", "................"]
def herb():
    pal = {"L": (176, 196, 186), "l": (122, 146, 138), "c": (196, 214, 255)}
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(HERB):
        for x, ch in enumerate(row):
            if ch in pal: px[x, y] = pal[ch] + (255,)
    return im

def build():
    rabbit().save(os.path.join(ENT, "moon_rabbit.png")); herb().save(os.path.join(BLOCK, "moon_herb.png"))
    W = M.w
    W("assets/ryancraft/blockstates/moon_herb.json", {"variants": {"": {"model": "ryancraft:block/moon_herb"}}})
    W("assets/ryancraft/models/block/moon_herb.json", {"parent": "minecraft:block/cross", "textures": {"cross": "ryancraft:block/moon_herb"}})
    W("assets/ryancraft/blockstates/potted_moon_herb.json", {"variants": {"": {"model": "ryancraft:block/potted_moon_herb"}}})
    W("assets/ryancraft/models/block/potted_moon_herb.json", {"parent": "minecraft:block/flower_pot_cross", "textures": {"plant": "ryancraft:block/moon_herb"}})
    W("assets/ryancraft/models/item/moon_herb.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:block/moon_herb"}})
    W("assets/ryancraft/items/moon_herb.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/moon_herb"}})
    surv = [{"condition": "minecraft:survives_explosion"}]
    W("data/ryancraft/loot_table/blocks/moon_herb.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:moon_herb"}], "conditions": surv}]})
    W("data/ryancraft/loot_table/blocks/potted_moon_herb.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:flower_pot"}], "conditions": surv},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:moon_herb"}], "conditions": surv}]})
    M.add_tag("data/minecraft/tags/block/flower_pots.json", ["ryancraft:potted_moon_herb"])
    W("data/ryancraft/loot_table/entities/moon_rabbit.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:rabbit_hide", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}]})
    save_item("moon_rabbit_spawn_egg", egg_icon("#dcdeea", "#a8a0c8"))
    W("assets/ryancraft/models/item/moon_rabbit_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/moon_rabbit_spawn_egg"}})
    W("assets/ryancraft/items/moon_rabbit_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/moon_rabbit_spawn_egg"}})
    # herb patches on the dust, and rabbits, in the moon biome
    D = "data/ryancraft/worldgen/"
    W(D + "configured_feature/patch_moon_herb.json", {"type": "minecraft:random_patch", "config": {"tries": 24, "xz_spread": 5, "y_spread": 2,
        "feature": {"feature": {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": {"Name": "ryancraft:moon_herb"}}}},
                    "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                        {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                        {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": "ryancraft:moon_dust"}]}}]}}})
    W(D + "placed_feature/patch_moon_herb.json", {"feature": "ryancraft:patch_moon_herb", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"},
        {"type": "minecraft:biome"}]})
    p = os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", "moon.json"); b = json.load(open(p))
    if "ryancraft:patch_moon_herb" not in b["features"][9]: b["features"][9].append("ryancraft:patch_moon_herb")
    b["spawners"]["creature"] = [{"type": "ryancraft:moon_rabbit", "weight": 10, "minCount": 2, "maxCount": 3},
                                 {"type": "ryancraft:butterfly", "weight": 4, "minCount": 1, "maxCount": 2}]      # lantern moths
    json.dump(b, open(p, "w"), indent=2)
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(LANG); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    build(); print("moon life ok")


# ---------------------------------------------------------------- lost spacemen (entity/LostSpaceman.java)
import shutil
SUITS = ["lavender", "white", "blue", "red", "orange", "green"]
def spacemen():
    out = os.path.join(ENT, "lost_spaceman"); os.makedirs(out, exist_ok=True)
    docs = os.path.join(HERE, "..", "docs")
    for s in SUITS: shutil.copy(os.path.join(docs, f"lost_spaceman_{s}.png"), os.path.join(out, f"{s}.png"))   # skins from tools/spaceman_skin.py
    looting = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}
    M.w("data/ryancraft/loot_table/entities/lost_spaceman.json", {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:rotten_flesh", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}, looting]}]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:killed_by_player"}, {"condition": "minecraft:random_chance", "chance": 0.1}],
         "entries": [{"type": "minecraft:item", "name": "minecraft:tinted_glass"}]}]})            # a piece of the old visor
    save_item("lost_spaceman_spawn_egg", egg_icon("#b4a6d8", "#2a2a36"))
    M.w("assets/ryancraft/models/item/lost_spaceman_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/lost_spaceman_spawn_egg"}})
    M.w("assets/ryancraft/items/lost_spaceman_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/lost_spaceman_spawn_egg"}})
    p = os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", "moon.json"); b = json.load(open(p))
    b["spawners"]["monster"] = [{"type": "ryancraft:lost_spaceman", "weight": 8, "minCount": 1, "maxCount": 2}]
    json.dump(b, open(p, "w"), indent=2)
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p))
    d.update({"entity.ryancraft.lost_spaceman": "Lost Spaceman", "item.ryancraft.lost_spaceman_spawn_egg": "Lost Spaceman Spawn Egg"})
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    spacemen(); print("spacemen ok")


# ---------------------------------------------------------------- the Great Moon Rabbit and its relics
DUST = [(120, 116, 104), (160, 156, 144), (196, 192, 180), (226, 222, 210)]           # moon-dust fur
def recoat(pal, eyes, big=False):
    im = Image.open(os.path.join(TPL, "white.png")).convert("RGBA"); px = im.load()
    vals = [sum(px[x, y][:3]) / 3 for y in range(im.height) for x in range(im.width) if px[x, y][3]]
    lo, hi = min(vals), max(vals)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            if r > 120 and g < 110: px[x, y] = eyes[0] + (a,); continue                    # the eyes
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s > 0.2 and (h < 0.05 or h > 0.85): continue                                 # pink nose, inner ears
            px[x, y] = grad((sum((r, g, b)) / 3 - lo) / max(1, hi - lo), pal) + (a,)
    if big:                                                                                 # bigger eyes: 2x2, with a glint
        for ex in (37, 41):
            for ey in (5, 6): px[ex, ey] = eyes[0] + (255,)
        for gx in (37, 40): px[gx, 5] = eyes[1] + (255,)
    return im

def gear():
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0)); px = im.load()
    HAFT, HAFT_D, STONE, STONE_D, STONE_L = (92, 66, 44), (64, 44, 30), (170, 168, 160), (120, 118, 112), (204, 202, 194)
    GOLD, GOLD_D, JADE = (232, 186, 52), (176, 128, 30), (84, 196, 122)
    for y in range(16):
        for x in range(4): px[x, y] = (HAFT if x % 2 == 0 else HAFT_D) + (255,)
    for y in range(7):
        for x in range(4, 20): px[x, y] = (STONE_L if y < 4 and x < 12 else STONE if (x + y) % 3 else STONE_D) + (255,)
    for y in range(7, 11):
        for x in range(4, 16): px[x, y] = (STONE_D if (x + y) % 2 else STONE) + (255,)
    for y in range(16, 22):
        for x in range(0, 20): px[x, y] = (JADE if x % 5 == 2 and y >= 21 else GOLD if (x + y) % 4 else GOLD_D) + (255,)
    for y in range(22, 24):
        for x in range(0, 4): px[x, y] = GOLD + (255,)
    return im

def sprite(rows, pal):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal: px[x, y] = pal[ch] + (255,)
    return im

CROWN_ICON = ["................", "................", "................", "................", "..g....g....g...", "..gg..ggg..gg...",
              "..gGg.gGg.gGg...", "..gGGgGJGgGGg...", "..gGGGGGGGGGg...", "..gJGGJGGJGGg...", "..gGGGGGGGGGg...", "..dddddddddddd..",
              "................", "................", "................", "................"]
MORTAR_ICON = ["..........sSSs..", ".........sLLLSs.", "........sLddLSs.", "........sLddLs..", ".........sSSs...", "........hh......",
               ".......hh.......", "......hh........", ".....hh.........", "....hh..........", "...hh...........", "..hh............",
               ".hh.............", "hh..............", "h...............", "................"]

def relics():
    recoat(DUST, [(30, 34, 52), (220, 226, 255)], big=True).save(os.path.join(ENT, "great_moon_rabbit.png"))
    recoat(SILVER, [(220, 40, 40), (255, 160, 160)]).save(os.path.join(ENT, "angry_moon_rabbit.png"))
    recoat([(52, 46, 66), (86, 78, 102), (122, 114, 140), (156, 150, 176)], [(240, 40, 30), (255, 160, 150)]).save(os.path.join(ENT, "angry_moon_rabbit_brute.png"))
    gear().save(os.path.join(ENT, "great_moon_rabbit_gear.png"))
    I = os.path.join(M.A, "textures", "item")
    sprite(CROWN_ICON, {"g": (176, 128, 30), "G": (232, 186, 52), "J": (84, 196, 122), "d": (120, 84, 20)}).save(os.path.join(I, "jade_crown.png"))
    sprite(MORTAR_ICON, {"s": (120, 118, 112), "S": (150, 148, 140), "L": (204, 202, 194), "d": (70, 68, 64), "h": (92, 66, 44)}).save(os.path.join(I, "moon_mortar.png"))
    for it, parent in (("jade_crown", "minecraft:item/generated"), ("moon_mortar", "minecraft:item/handheld")):
        M.w(f"assets/ryancraft/models/item/{it}.json", {"parent": parent, "textures": {"layer0": f"ryancraft:item/{it}"}})
        M.w(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{it}"}})
    # the crown as worn: a gold band with jade stones round the top of the head (humanoid armour layout)
    eq = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); px = eq.load()
    for x in range(0, 32):                                    # the head's four sides, top two rows: band and points
        px[x, 8 + 0] = ((84, 196, 122) if x % 4 == 2 else (232, 186, 52)) + (255,)
        if x % 4 == 0: px[x, 8 - 1 + 0] = (232, 186, 52, 255)
    for x in range(8, 16):
        for y in range(0, 8):
            if x in (8, 15) or y in (0, 7): px[x, y] = (232, 186, 52, 255)                 # the band seen from above
    out = os.path.join(ENT, "equipment", "humanoid"); os.makedirs(out, exist_ok=True); eq.save(os.path.join(out, "jade_crown.png"))
    M.w("assets/ryancraft/equipment/jade_crown.json", {"layers": {"humanoid": [{"texture": "ryancraft:jade_crown"}]}})
    M.w("data/ryancraft/tags/item/jade_crown_repair.json", {"values": ["ryancraft:jade"]})
    M.w("data/ryancraft/loot_table/entities/great_moon_rabbit.json", {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:jade_crown"}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:moon_mortar"}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:raw_meteor_iron", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 3, "max": 6}}]}]}]})
    save_item("great_moon_rabbit_spawn_egg", egg_icon("#c4c0b4", "#54c47a"))
    M.w("assets/ryancraft/models/item/great_moon_rabbit_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/great_moon_rabbit_spawn_egg"}})
    M.w("assets/ryancraft/items/great_moon_rabbit_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/great_moon_rabbit_spawn_egg"}})
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p))
    d.update({"entity.ryancraft.great_moon_rabbit": "Great Moon Rabbit", "entity.ryancraft.angry_moon_rabbit": "Angry Moon Rabbit",
              "entity.ryancraft.boss_meteor": "Meteor", "item.ryancraft.jade_crown": "Jade Crown", "item.ryancraft.moon_mortar": "Moon Mortar",
              "item.ryancraft.great_moon_rabbit_spawn_egg": "Great Moon Rabbit Spawn Egg"})
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    relics(); print("relics ok")


# ---------------------------------------------------------------- jade temples (worldgen/JadeTempleFeature.java)
def temples():
    D = "data/ryancraft/worldgen/"
    M.w(D + "configured_feature/jade_temple.json", {"type": "ryancraft:jade_temple", "config": {}})
    # just one temple, by the landing site: the feature itself only builds in that chunk
    M.w(D + "placed_feature/jade_temple.json", {"feature": "ryancraft:jade_temple", "placement": [{"type": "minecraft:biome"}]})
    p = os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", "moon.json"); b = json.load(open(p))
    b["features"][4] = ["ryancraft:jade_temple"]; json.dump(b, open(p, "w"), indent=2)
    item = lambda n, lo, hi, w: {"type": "minecraft:item", "name": n, "weight": w, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}
    M.w("data/ryancraft/loot_table/chests/jade_temple.json", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("ryancraft:raw_meteor_iron", 2, 6, 20), item("ryancraft:meteor_iron_ingot", 1, 3, 10), item("ryancraft:jade", 2, 5, 15),
            item("ryancraft:moon_herb", 2, 6, 15), item("minecraft:golden_carrot", 2, 5, 10), item("minecraft:ender_eye", 1, 2, 5),
            {"type": "minecraft:item", "name": "minecraft:potion", "weight": 15, "functions": [{"function": "minecraft:set_potion", "id": "ryancraft:air"}]},
            {"type": "minecraft:item", "name": "minecraft:potion", "weight": 5, "functions": [{"function": "minecraft:set_potion", "id": "ryancraft:long_air"}]}]}]})

if __name__ == "__main__":
    temples(); print("temples ok")


# ---------------------------------------------------------------- force field generator (moon/ForceField.java)
def force_field():
    B = os.path.join(M.A, "textures", "block")
    iron = Image.open(os.path.join(B, "meteor_iron_block.png")).convert("RGBA")
    def face(core, active):
        im = iron.copy(); px = im.load()
        for y in range(16):
            for x in range(16):
                if x in (0, 15) or y in (0, 15): px[x, y] = (40, 42, 52, 255)
        for y in range(4, 12):
            for x in range(4, 12):
                edge = x in (4, 11) or y in (4, 11)
                c = (30, 34, 44) if edge else core[0] if (x + y) % 3 else core[1]
                if active and not edge and 6 <= x <= 9 and 6 <= y <= 9: c = core[2]
                px[x, y] = c + (255,)
        return im
    dim = [(46, 110, 74), (36, 90, 60), (60, 140, 96)]
    lit = [(84, 196, 122), (64, 170, 104), (200, 255, 220)]
    for active in (False, True):
        k = "_on" if active else ""
        face(lit if active else dim, active).save(os.path.join(B, f"force_field_generator_side{k}.png"))
        top = iron.copy(); px = top.load()
        for i in range(16): px[i, i] = px[15 - i, i] = ((120, 230, 160) if active else (60, 120, 84)) + (255,)
        top.save(os.path.join(B, f"force_field_generator_top{k}.png"))
        M.w(f"assets/ryancraft/models/block/force_field_generator{k}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "side": f"ryancraft:block/force_field_generator_side{k}", "top": f"ryancraft:block/force_field_generator_top{k}", "bottom": "ryancraft:block/meteor_iron_block"}})
    M.w("assets/ryancraft/blockstates/force_field_generator.json", {"variants": {"active=false": {"model": "ryancraft:block/force_field_generator"},
                                                                              "active=true": {"model": "ryancraft:block/force_field_generator_on"}}})
    M.w("assets/ryancraft/items/force_field_generator.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/force_field_generator"}})
    M.w("data/ryancraft/loot_table/blocks/force_field_generator.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "ryancraft:force_field_generator"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    M.w("data/ryancraft/recipe/force_field_generator.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["IGI", "GDG", "IRI"],
        "key": {"I": "ryancraft:meteor_iron_ingot", "G": "minecraft:glass", "D": "minecraft:diamond_block", "R": "minecraft:redstone_block"},
        "result": {"id": "ryancraft:force_field_generator", "count": 1}})
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:force_field_generator"])
    M.add_tag("data/minecraft/tags/block/needs_iron_tool.json", ["ryancraft:force_field_generator"])
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p))
    d.update({"block.ryancraft.force_field_generator": "Force Field Generator",
              "message.ryancraft.force_field": "Field radius %s blocks, %s%% charged, about %s minutes left",
              "message.ryancraft.force_field_full": "The generator can't hold any more charge"})
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    force_field(); print("force field ok")


# ---------------------------------------------------------------- gravity well (moon/GravityWell.java)
def gravity_well():
    B = os.path.join(M.A, "textures", "block")
    im = Image.open(os.path.join(B, "chiseled_moonstone_bricks.png")).convert("RGBA"); px = im.load()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if x in (0, 15) or y in (0, 15): px[x, y] = (54, 56, 68, 255)            # a meteor-iron rim
            elif d < 2.2: px[x, y] = (226, 230, 255, 255)                               # the pale, weightless core
            elif d < 3.4: px[x, y] = (150, 160, 210, 255)
    im.save(os.path.join(B, "gravity_well.png"))
    M.w("assets/ryancraft/blockstates/gravity_well.json", {"variants": {"powered=false": {"model": "ryancraft:block/gravity_well"}, "powered=true": {"model": "ryancraft:block/gravity_well"}}})
    M.w("assets/ryancraft/models/block/gravity_well.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "ryancraft:block/gravity_well"}})
    M.w("assets/ryancraft/items/gravity_well.json", {"model": {"type": "minecraft:model", "model": "ryancraft:block/gravity_well"}})
    M.w("data/ryancraft/loot_table/blocks/gravity_well.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "ryancraft:gravity_well"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    M.w("data/ryancraft/recipe/gravity_well.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["DMD", "MSM", "DMD"],
        "key": {"D": "ryancraft:moon_dust", "M": "ryancraft:meteor_iron_ingot", "S": "ryancraft:chiseled_moonstone_bricks"},
        "result": {"id": "ryancraft:gravity_well", "count": 1}})
    M.add_tag("data/minecraft/tags/block/mineable/pickaxe.json", ["ryancraft:gravity_well"])
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d["block.ryancraft.gravity_well"] = "Gravity Well"
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    gravity_well(); print("gravity well ok")


# ---------------------------------------------------------------- moon cake
CAKE = ["................", "................", "................", "....gggggggg....", "...gGGGGGGGGg...", "..gGGhGGGGhGGg..", "..gGGGGhhGGGGg..",
        "..dgGGGGGGGGgd..", "..ddggggggggdd..", "..dDDDDDDDDDDd..", "..dDdDDdDDdDDd..", "..dDDDDDDDDDDd..", "...dddddddddd...", "................",
        "................", "................"]
def moon_cake():
    sprite(CAKE, {"g": (150, 176, 166), "G": (196, 218, 208), "h": (196, 214, 255), "d": (150, 108, 60), "D": (212, 166, 96)}).save(
        os.path.join(M.A, "textures", "item", "moon_cake.png"))
    M.w("assets/ryancraft/models/item/moon_cake.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/moon_cake"}})
    M.w("assets/ryancraft/items/moon_cake.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/moon_cake"}})
    M.w("data/ryancraft/recipe/moon_cake.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [
        "ryancraft:moon_herb", "ryancraft:moon_herb", "minecraft:wheat", "minecraft:wheat", "minecraft:sugar", "minecraft:egg"],
        "result": {"id": "ryancraft:moon_cake", "count": 1}})
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d["item.ryancraft.moon_cake"] = "Moon Cake"
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    moon_cake(); print("moon cake ok")


# ---------------------------------------------------------------- dust drifters (entity/DustDrifter.java)
def drifters():
    src = Image.open(os.path.join(ENT, "jellyfish_white.png")).convert("RGBA"); px = src.load()
    pal = [(120, 120, 168), (176, 180, 222), (218, 222, 246), (246, 248, 255)]
    vals = [sum(px[x, y][:3]) / 3 for y in range(src.height) for x in range(src.width) if px[x, y][3]]; lo, hi = min(vals), max(vals)
    for y in range(src.height):
        for x in range(src.width):
            c = px[x, y]
            if c[3]: px[x, y] = grad((sum(c[:3]) / 3 - lo) / max(1, hi - lo), pal) + (c[3],)
    src.save(os.path.join(ENT, "dust_drifter.png"))
    M.w("data/ryancraft/loot_table/entities/dust_drifter.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "minecraft:glowstone_dust", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}]}]}]})
    save_item("dust_drifter_spawn_egg", egg_icon("#dcdef4", "#9a90d0"))
    M.w("assets/ryancraft/models/item/dust_drifter_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/dust_drifter_spawn_egg"}})
    M.w("assets/ryancraft/items/dust_drifter_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/dust_drifter_spawn_egg"}})
    p = os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", "moon.json"); b = json.load(open(p))
    b["spawners"]["ambient"] = [{"type": "ryancraft:dust_drifter", "weight": 6, "minCount": 1, "maxCount": 3}]; json.dump(b, open(p, "w"), indent=2)
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p))
    d.update({"entity.ryancraft.dust_drifter": "Dust Drifter", "item.ryancraft.dust_drifter_spawn_egg": "Dust Drifter Spawn Egg"})
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    drifters(); print("drifters ok")


# ---------------------------------------------------------------- crater crabs (entity/CraterCrab.java)
def crabs():
    from critters import box, part, emit_model, emit_textures
    from prototypes import pack
    parts = [part("body", (0, 21, 0), [box("shell", (-4, -1.5, -3), (8, 3, 6), "shell"),
                                        box("eye_r", (-2, -3.5, -3), (1, 2, 1), "eye"), box("eye_l", (1, -3.5, -3), (1, 2, 1), "eye", mirror=True)], "body"),
             part("claw_r", (-4, 21, -3), [box("claw", (-3, -1, -3), (3, 2, 3), "claw")], "leg_rf"),
             part("claw_l", (4, 21, -3), [box("claw", (0, -1, -3), (3, 2, 3), "claw", mirror=True)], "leg_lf")]
    for i, z in enumerate((-1.5, 0.5, 2.5)):
        parts.append(part(f"leg_r{i}", (-4, 22, z), [box("leg", (-3, 0, -0.5), (3, 1, 1), "leg")], "leg_rb" if i % 2 else "leg_lb"))
        parts.append(part(f"leg_l{i}", (4, 22, z), [box("leg", (0, 0, -0.5), (3, 1, 1), "leg", mirror=True)], "leg_lb" if i % 2 else "leg_rb"))
    size = pack(parts); emit_model("crater_crab", parts, size)
    emit_textures(parts, size, {"crater_crab": {"shell": {"c": "#a8a49a", "noise": 10, "speckle": {"c": "#6e6a62", "p": 0.25}},
        "claw": {"c": "#b8b2a4", "noise": 8, "speckle": {"c": "#5a5650", "p": 0.2}}, "leg": {"c": "#8e8a80", "noise": 6}, "eye": {"c": "#1e1c22"}}})
    M.w("data/ryancraft/loot_table/entities/crater_crab.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item",
        "name": "ryancraft:moon_dust", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}]}]}]})
    save_item("crater_crab_spawn_egg", egg_icon("#a8a49a", "#5a5650"))
    M.w("assets/ryancraft/models/item/crater_crab_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/crater_crab_spawn_egg"}})
    M.w("assets/ryancraft/items/crater_crab_spawn_egg.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/crater_crab_spawn_egg"}})
    p = os.path.join(M.RES, "data", "ryancraft", "worldgen", "biome", "moon.json"); b = json.load(open(p))
    if not any(e["type"] == "ryancraft:crater_crab" for e in b["spawners"]["creature"]):
        b["spawners"]["creature"].append({"type": "ryancraft:crater_crab", "weight": 6, "minCount": 1, "maxCount": 3})
    json.dump(b, open(p, "w"), indent=2)
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p))
    d.update({"entity.ryancraft.crater_crab": "Crater Crab", "item.ryancraft.crater_crab_spawn_egg": "Crater Crab Spawn Egg"})
    json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)

if __name__ == "__main__":
    crabs(); print("crabs ok")
