"""
The wiki's recipe page: every RyanCraft recipe, grouped by category and alphabetical within each, generated from the
recipe files so it never drifts out of date. Names come from our lang file and vanilla's (the unpacked client jar's).
  python3 tools/recipes_wiki.py <vanilla assets/minecraft dir>  ->  docs/wiki/Recipes.md
"""
import json, os, glob, sys, collections

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
OUT = os.path.join(HERE, "..", "docs", "wiki", "Recipes.md")
OURS = json.load(open(os.path.join(RES, "assets", "ryancraft", "lang", "en_us.json")))
VAN = {}
if len(sys.argv) > 1 and os.path.exists(os.path.join(sys.argv[1], "lang", "en_us.json")):
    VAN = json.load(open(os.path.join(sys.argv[1], "lang", "en_us.json")))

CATEGORY = {"building": "Building blocks", "blocks": "Building blocks", None: "Building blocks", "decorations": "Decorations",
            "redstone": "Machines and redstone", "equipment": "Tools, weapons and armour", "food": "Food", "misc": "Everything else"}
ORDER = ["Building blocks", "Decorations", "Machines and redstone", "Tools, weapons and armour", "Food", "Everything else"]
TAGS = {"planks": "any planks", "logs": "any log", "logs_that_burn": "any log", "wooden_slabs": "any wooden slab", "wool": "any wool",
        "coals": "coal or charcoal", "stone_crafting_materials": "cobblestone (any)", "stone_tool_materials": "cobblestone (any)",
        "candles": "any candle", "sand": "any sand", "leaves": "any leaves", "saplings": "any sapling", "fishes": "any fish",
        "meat": "any meat", "cooked_meats": "any cooked meat", "dyes": "any dye", "flowers": "any flower"}

def name(ident):
    if isinstance(ident, dict): ident = ident.get("item") or ident.get("id") or ident.get("tag", "?")
    if isinstance(ident, list): return " or ".join(name(i) for i in ident)
    if ident.startswith("#"):
        ns, path = ident[1:].split(":") if ":" in ident else ("minecraft", ident[1:])
        return TAGS.get(path, "any " + path.replace("_", " ").rstrip("s"))
    ns, path = ident.split(":") if ":" in ident else ("minecraft", ident)
    table = OURS if ns == "ryancraft" else VAN
    for kind in ("item", "block"):
        k = f"{kind}.{ns}.{path}"
        if k in table: return table[k]
    return path.replace("_", " ").title()

def result(d):
    r = d.get("result", {})
    if isinstance(r, str): return r, 1
    return r.get("id") or r.get("item"), r.get("count", 1)

def describe(d):
    t = d["type"].split(":")[1]
    if t == "crafting_shaped":
        rows = [row.replace(" ", "·") for row in d["pattern"]]
        key = "; ".join(f"`{k}` {name(v)}" for k, v in sorted(d["key"].items()))
        return "Crafting: " + " / ".join(f"`{r}`" for r in rows) + " (" + key + ")"
    if t == "crafting_shapeless":
        c = collections.Counter(name(i) for i in d["ingredients"])
        return "Crafting (any arrangement): " + ", ".join(f"{n} x{k}" if k > 1 else n for n, k in c.items())
    verb = {"smelting": "Smelt", "blasting": "Blast furnace", "smoking": "Smoker", "campfire_cooking": "Campfire", "stonecutting": "Stonecutter"}[t]
    return f"{verb}: {name(d['ingredient'])}"

def build():
    by_cat = collections.defaultdict(lambda: collections.defaultdict(list))
    for p in sorted(glob.glob(os.path.join(RES, "data", "ryancraft", "recipe", "*.json"))):
        d = json.load(open(p)); rid, count = result(d)
        if not rid: continue
        cat = CATEGORY.get(d.get("category"), "Everything else")
        line = describe(d) + (f", makes {count}" if count > 1 else "")
        by_cat[cat][name(rid)].append(line)
    # an item made several ways sits in the category of its first (crafting) recipe
    seen = {}
    for cat in ORDER:
        for item in list(by_cat[cat]):
            if item in seen: by_cat[seen[item]][item] += by_cat[cat].pop(item)
            else: seen[item] = cat
    total = sum(len(v) for c in by_cat.values() for v in c.values())
    out = ["[← Wiki home](Home.md)", "", "# Recipes", "",
           f"Every RyanCraft recipe ({total} of them), by category and then A to Z. Shaped recipes show the crafting grid row "
           "by row (top to bottom, `·` for an empty slot) with a key to its letters. Generated from the mod's recipe files "
           "by `tools/recipes_wiki.py`, so it's always up to date.", ""]
    out.append("**Categories:** " + " · ".join(f"[{c}](#{c.lower().replace(',', '').replace(' ', '-')})" for c in ORDER if by_cat[c]))
    for cat in ORDER:
        items = by_cat[cat]
        if not items: continue
        out += ["", f"## {cat}", "", "| Item | How to make it |", "|---|---|"]
        for item in sorted(items, key=str.lower):
            out.append(f"| **{item}** | " + "<br>".join(items[item]) + " |")
    open(OUT, "w").write("\n".join(out) + "\n")
    print("wrote", OUT, total, "recipes,", sum(len(v) for v in by_cat.values()), "items")

if __name__ == "__main__":
    build()
