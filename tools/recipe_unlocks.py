"""Recipe-book unlocks: one advancement per crafting and cooking recipe (as vanilla has), so our recipes show up in
the vanilla recipe book once you've picked up any of their ingredients. Stonecutter recipes need no unlock.
  python3 tools/recipe_unlocks.py   (rewrites data/ryancraft/advancement/recipes/)"""
import json, os, glob, shutil

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
OUT = os.path.join(RES, "data", "ryancraft", "advancement", "recipes")
TAB = {"building": "building_blocks", "blocks": "building_blocks", "decorations": "decorations", "redstone": "redstone",
       "equipment": "combat", "food": "food", "misc": "misc", None: "misc"}

def ingredients(d):
    t = d["type"].split(":")[1]
    if t == "crafting_shaped": raw = list(d["key"].values())
    elif t == "crafting_shapeless": raw = d["ingredients"]
    else: raw = [d["ingredient"]]
    out = []
    for i in raw:
        if isinstance(i, dict): i = i.get("item") or i.get("tag")
        for x in (i if isinstance(i, list) else [i]):
            if x and x not in out: out.append(x)
    return out

def main():
    if os.path.isdir(OUT): shutil.rmtree(OUT)
    n = 0
    for p in sorted(glob.glob(os.path.join(RES, "data", "ryancraft", "recipe", "*.json"))):
        d = json.load(open(p)); rid = os.path.basename(p)[:-5]
        if d["type"] == "minecraft:stonecutting": continue
        crit = {"has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": f"ryancraft:{rid}"}}}
        names = ["has_the_recipe"]
        for k, ing in enumerate(ingredients(d)):
            crit[f"has_{k}"] = {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": ing}]}}
            names.append(f"has_{k}")
        adv = {"parent": "minecraft:recipes/root", "criteria": crit, "requirements": [names],
               "rewards": {"recipes": [f"ryancraft:{rid}"]}}
        path = os.path.join(OUT, TAB.get(d.get("category"), "misc"), rid + ".json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        json.dump(adv, open(path, "w"), indent=2); open(path, "a").write("\n"); n += 1
    print("recipe unlocks:", n)

if __name__ == "__main__":
    main()
