"""Texture charts for the front page and the wiki, drawn from the mod's current textures (so rerun after art changes):
  docs/chart_ores.png      metals, gems and steel: ores, raw, ingots, nuggets and blocks
  docs/chart_food.png      crops, seeds, dishes, meats and fish
  docs/chart_skylands.png  skystone, sky willow, flowers, zephyrite and its gear
  docs/chart_moon.png      moon stone, meteor iron and jade
  docs/chart_trees.png     every tree's bark, log end, stripped log, planks, leaves and sapling
  docs/chart_gear.png      the new tools and armour
  docs/chart_eggs.png      every spawn egg
  python3 tools/docs_charts.py"""
import colorsys, json, os
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, "..")
A = os.path.join(ROOT, "src/main/resources/assets/ryancraft"); T = os.path.join(A, "textures"); DOCS = os.path.join(ROOT, "docs")
BG, INK, SUB, CARD = (243, 239, 230), (46, 42, 36), (120, 112, 100), (229, 223, 210)
FOLIAGE = (100, 168, 58)

def font(sz, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, sz)
    return ImageFont.load_default()

def tex(path):
    """A 16x16 texture by path ('block/x' or 'item/x'), or by bare name (item first, then block). Grey leaves get tinted."""
    cands = [path] if "/" in path else ["item/" + path, "block/" + path]
    for c in cands:
        p = os.path.join(T, c + ".png")
        if os.path.exists(p):
            im = Image.open(p).convert("RGBA"); im = im.crop((0, 0, im.width, im.width))      # first frame of animations
            if "leaves" in c and grey(im): im = tint(im, FOLIAGE)
            return im
    via = model_texture(path.split("/")[-1])
    if via: return tex(via)
    raise FileNotFoundError(path)

def model_texture(i):
    """The texture an item's model draws with, for items whose picture isn't named after them."""
    try:
        m = json.load(open(os.path.join(A, "items", i + ".json")))["model"]
        while "model" in m and isinstance(m["model"], dict): m = m["model"]
        if "model" not in m: m = (m.get("cases") or [{}])[0].get("model", m.get("fallback", {}))
        ref = m["model"].split(":")[-1]
        for _ in range(4):
            mj = json.load(open(os.path.join(A, "models", ref + ".json"))); t = mj.get("textures", {})
            for k in ("layer0", "all", "side", "cross", "top", "particle"):
                if k in t and not t[k].startswith("#") and t[k].startswith("ryancraft:"): return t[k].split(":")[-1]
            ref = mj.get("parent", "").split(":")[-1]
            if not ref or not os.path.exists(os.path.join(A, "models", ref + ".json")): break
    except Exception:
        pass
    return None

def grey(im):
    px = [p for p in im.getdata() if p[3]]
    return px and sum(colorsys.rgb_to_hsv(*[c / 255 for c in p[:3]])[1] for p in px) / len(px) < 0.12

def tint(im, c):
    out = im.copy(); po = out.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = po[x, y]; po[x, y] = (r * c[0] // 255, g * c[1] // 255, b * c[2] // 255, a)
    return out

def chart(out, title, subtitle, sections, cols=8, ts=112, cw=172, ch=176):
    """sections: [(heading or None, [(label, texture), ...]), ...]. Blocks are shown 2x2-tiled so seams show; items on a card."""
    h = 110 + sum((48 if hd else 0) + ((len(items) + cols - 1) // cols) * ch for hd, items in sections) + 20
    img = Image.new("RGB", (cols * cw + 40, h), BG); d = ImageDraw.Draw(img)
    d.text((24, 22), title, font=font(34, True), fill=INK); d.text((26, 68), subtitle, font=font(17), fill=SUB)
    y = 110
    for hd, items in sections:
        if hd: d.text((24, y + 8), hd, font=font(22, True), fill=INK); y += 48
        for k, (label, path) in enumerate(items):
            x, yy = 20 + (k % cols) * cw, y + (k // cols) * ch
            try: t = tex(path)
            except FileNotFoundError: print("  (no texture for", path + ")"); continue
            if path.startswith("block/") and "sapling" not in path and t.getpixel((0, 0))[3] == 255:
                tile = Image.new("RGBA", (32, 32))
                for a in (0, 16):
                    for b in (0, 16): tile.paste(t.resize((16, 16), Image.NEAREST), (a, b))
                big = tile.resize((ts, ts), Image.NEAREST)
            else:
                card = Image.new("RGBA", (16, 16), CARD + (255,)); card.alpha_composite(t.resize((16, 16), Image.NEAREST)); big = card.resize((ts, ts), Image.NEAREST)
            img.paste(big.convert("RGB"), (x + (cw - ts) // 2, yy))
            lines = wrap(label, 18)
            for i, ln in enumerate(lines): d.text((x + cw // 2, yy + ts + 16 + i * 19), ln, font=font(15, True), fill=INK, anchor="mm")
        y += ((len(items) + cols - 1) // cols) * ch
    img.save(os.path.join(DOCS, out)); print("wrote", out, img.size)

def wrap(s, n):
    words, lines = s.split(), [""]
    for w in words:
        if len(lines[-1]) + len(w) + 1 > n and lines[-1]: lines.append(w)
        else: lines[-1] = (lines[-1] + " " + w).strip()
    return lines[:2]

def lang():
    return json.load(open(os.path.join(A, "lang/en_us.json")))

def name(L, i):
    return L.get("item.ryancraft." + i) or L.get("block.ryancraft." + i) or i.replace("_", " ").title()

def main():
    L = lang()
    metals = ["silver", "lead", "tin", "zinc", "aluminum", "uranium"]
    sec = []
    for m in metals:
        sec.append((name(L, m + "_ingot").replace(" Ingot", ""), [(name(L, n), p) for n, p in (
            (f"{m}_ore", f"block/{m}_ore"), (f"deepslate_{m}_ore", f"block/deepslate_{m}_ore"), (f"raw_{m}", f"item/raw_{m}"),
            (f"raw_{m}_block", f"block/raw_{m}_block"), (f"{m}_ingot", f"item/{m}_ingot"), (f"{m}_nugget", f"item/{m}_nugget"),
            (f"{m}_block", f"block/{m}_block"), (f"chiseled_{m}", f"block/chiseled_{m}"))]))
    sec.append(("Gems", [(name(L, n), p) for n, p in (("ruby_ore", "block/ruby_ore"), ("deepslate_ruby_ore", "block/deepslate_ruby_ore"),
        ("ruby", "item/ruby"), ("ruby_block", "block/ruby_block"), ("sapphire_ore", "block/sapphire_ore"),
        ("deepslate_sapphire_ore", "block/deepslate_sapphire_ore"), ("sapphire", "item/sapphire"), ("sapphire_block", "block/sapphire_block"),
        ("jade_ore", "block/jade_ore"), ("deepslate_jade_ore", "block/deepslate_jade_ore"), ("jade", "item/jade"), ("jade_block", "block/jade_block"))]))
    sec.append(("Steel and tarnished silver", [(name(L, n), "block/" + n) for n in ("steel_block", "cut_steel", "steel_plating", "chiseled_steel",
        "silver_block", "dulled_silver_block", "tarnished_silver_block", "blackened_silver_block") if os.path.exists(os.path.join(T, "block", n + ".png"))]
        + [(name(L, "steel_ingot"), "item/steel_ingot")]))
    chart("chart_ores.png", "Ores, metals and gems", "Six new metals, three gems and steel, as they look in game (blocks shown 2x2).", sec)

    crops = ["tomato", "pepper", "corn", "squash", "cabbage", "onion", "beans", "rice"]
    seeds = [c + "_seeds" for c in crops if os.path.exists(os.path.join(T, "item", c + "_seeds.png"))]
    dishes = ["roasted_corn", "baked_squash", "stuffed_pepper", "cabbage_roll", "sandwich", "garden_salad", "cornbread", "cherry_pie",
              "cherries", "black_walnut", "shelled_walnut", "moon_cake"]
    meats = ["venison", "cooked_venison", "raw_turkey", "cooked_turkey", "raw_duck", "cooked_duck"]
    fish = ["bluegill", "crappie", "yellow_perch", "largemouth_bass", "smallmouth_bass", "rainbow_trout", "walleye", "channel_catfish",
            "northern_pike", "muskellunge"]
    chart("chart_food.png", "Food", "Seasonal crops, the dishes they make, game meats and freshwater fish.", [
        ("Crops", [(name(L, c), "item/" + c) for c in crops]),
        ("Seeds", [(name(L, s), "item/" + s) for s in seeds]),
        ("Crops growing", [(name(L, c), f"block/{c}_stage3") for c in crops if os.path.exists(os.path.join(T, "block", c + "_stage3.png"))]),
        ("Dishes and treats", [(name(L, x), x) for x in dishes]),
        ("Game meats", [(name(L, x), x) for x in meats]),
        ("Freshwater fish", [(name(L, x), x) for x in fish])])

    sky = [("skystone", "block/skystone"), ("zephyrite_ore", "block/zephyrite_ore"), ("zephyrite", "item/zephyrite"), ("zephyrite_block", "block/zephyrite_block"),
           ("zephyr_vent", "block/zephyr_vent_hole"), ("roc_egg", "roc_egg")]
    willow = [("sky_willow_log", "block/sky_willow_log"), ("sky_willow_log", "block/sky_willow_log_top"), ("stripped_sky_willow_log", "block/stripped_sky_willow_log"),
              ("sky_willow_planks", "block/sky_willow_planks"), ("sky_willow_leaves", "block/sky_willow_leaves"), ("sky_willow_sapling", "block/sky_willow_sapling"),
              ("sky_willow_door", "item/sky_willow_door"), ("sky_willow_trapdoor", "block/sky_willow_trapdoor")]
    flowers = ["windflower", "cloudbloom", "sunpetal", "sky_bell"]
    gear = ["sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots"]
    chart("chart_skylands.png", "The Skylands", "Blocks, plants and zephyrite gear from the floating islands.", [
        ("Stone and zephyrite", [(name(L, n), p) for n, p in sky]),
        ("Sky willow", [(name(L, n) + (" (end)" if p.endswith("_top") else ""), p) for n, p in willow]),
        ("Sky flowers", [(name(L, f), "block/" + f) for f in flowers]),
        ("Zephyrite gear", [(name(L, "zephyrite_" + g), "item/zephyrite_" + g) for g in gear])], cols=9)

    moon = ["moon_dust", "moonstone", "cobbled_moonstone", "polished_moonstone", "moonstone_bricks", "cracked_moonstone_bricks", "chiseled_moonstone_bricks",
            "deep_moonstone", "cobbled_deep_moonstone", "polished_deep_moonstone", "deep_moonstone_bricks"]
    ores = ["meteor_iron_ore", "deep_meteor_iron_ore", "raw_meteor_iron", "raw_meteor_iron_block", "meteor_iron_ingot", "meteor_iron_block",
            "moonstone_jade_ore", "deep_moonstone_jade_ore"]
    life = ["moon_herb", "moon_cake", "jade_crown", "moon_mortar"]
    chart("chart_moon.png", "The Moon", "Moonstone, deep moonstone, meteor iron and moon jade, and what the moon gives you.", [
        ("Stone", [(name(L, n), "block/" + n) for n in moon]),
        ("Meteor iron and jade", [(name(L, n), ("item/" if n in ("raw_meteor_iron", "meteor_iron_ingot") else "block/") + n) for n in ores]),
        ("Meteor iron gear", [(name(L, "meteor_iron_" + g), "item/meteor_iron_" + g) for g in gear]),
        ("Finds", [(name(L, n), n) for n in life])], cols=9)

    species = ["white_oak", "red_oak", "sugar_maple", "red_maple", "tulip_poplar", "shagbark_hickory", "black_walnut", "american_beech", "sycamore",
               "eastern_redbud", "flowering_dogwood", "eastern_red_cedar", "black_cherry", "sassafras", "ohio_buckeye"]
    rows = []
    for s in species:
        t = name(L, s + "_planks").replace(" Planks", "")
        rows.append((t, [("Bark", f"block/{s}_log"), ("Log end", f"block/{s}_log_top"), ("Stripped", f"block/stripped_{s}_log"),
                         ("Planks", f"block/{s}_planks"), ("Leaves", f"block/{s}_leaves"), ("Sapling", f"block/{s}_sapling")]))
    chart("chart_trees.png", "Fifteen eastern trees", "Bark, log end, stripped log, planks, leaves (shown in summer green) and sapling for each species.",
          rows, cols=6, ts=96, cw=150, ch=150)

    chart("chart_gear.png", "Tools and armour", "Lead (iron tier, heavy), zephyrite (iron tier, feather-falling boots) and meteor iron (netherite tier).", [
        (name(L, m + "_ingot").replace(" Ingot", ""), [(name(L, f"{m}_{g}"), f"item/{m}_{g}") for g in gear]) for m in ("lead", "zephyrite", "meteor_iron")]
        + [("Also", [(name(L, n), "item/" + n) for n in ("coonskin_cap", "musket", "musket_ball", "cannonball", "grapeshot", "chain_shot",
                                                          "canister_shot", "heated_shot", "explosive_shell") if os.path.exists(os.path.join(T, "item", n + ".png"))])], cols=9)

    eggs = sorted(f[:-4] for f in os.listdir(os.path.join(T, "item")) if f.endswith("_spawn_egg.png"))
    mud = [e for e in eggs if e.startswith("mudling_")]; rest = [e for e in eggs if e not in mud]
    chart("chart_eggs.png", "Spawn eggs", "One for every RyanCraft mob, and one for each mudling kind.", [
        ("Animals", [(name(L, e).replace(" Spawn Egg", ""), "item/" + e) for e in rest]),
        ("Mudlings", [(name(L, e).replace(" Spawn Egg", ""), "item/" + e) for e in mud])], cols=10, ts=80, cw=136, ch=134)

if __name__ == "__main__":
    main()
