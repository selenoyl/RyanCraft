"""Writes RyanCraft's JSON data/asset files (sounds.json, loot, recipes, item models, tags, lang). Run: python3 tools/resources.py"""
import json, os, glob
R = os.path.join(os.path.dirname(__file__), "..", "src/main/resources")
SND = os.path.join(R, "assets/ryancraft/sounds")
def w(path, obj):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: json.dump(obj, f, indent=2, ensure_ascii=False); f.write("\n")

# ---- sounds.json: event -> files found on disk
def files(prefix):
    out = sorted(os.path.relpath(f, SND)[:-4] for f in glob.glob(os.path.join(SND, prefix + "*.ogg")))
    return [x for x in out if x.rstrip("0123456789") == prefix]
EVENTS = {  # event: (file prefix, subtitle, attenuation distance or None, stream)
    "ambience.wind": ("ambience/wind", "Wind blows", None, True), "ambience.wind_howl": ("ambience/wind_howl", "Wind howls", None, True),
    "ambience.forest": ("ambience/forest", "Leaves rustle", None, True), "ambience.crickets": ("ambience/crickets", "Crickets chirp", None, True),
    "ambience.frogs": ("ambience/frogs", "Frogs sing", None, True), "ambience.stream": ("ambience/stream", "Water babbles", 24, True),
    "ambience.ocean": ("ambience/ocean", "Waves crash", None, True), "ambience.rain_leaves": ("ambience/rain_leaves", "Rain patters on leaves", None, True),
    "ambience.cave": ("ambience/cave", None, None, True),
    "ambience.bird": ("ambience/bird", "Bird sings", 32, False), "ambience.woodpecker": ("ambience/woodpecker", "Woodpecker drums", 40, False),
    "ambience.cuckoo": ("ambience/cuckoo", "Cuckoo calls", 48, False), "ambience.owl": ("ambience/owl", "Owl hoots", 40, False),
    "ambience.crow": ("ambience/crow", "Crow caws", 48, False), "ambience.wolf_howl": ("ambience/wolf_howl", "Distant wolf howls", 64, False),
    "ambience.seagull": ("ambience/seagull", "Gull cries", 40, False), "ambience.hawk": ("ambience/hawk", "Hawk screeches", 64, False),
    "ambience.drip": ("ambience/drip", "Water drips", 16, False), "ambience.rumble": ("ambience/rumble", "Cave rumbles", 40, False),
    "ambience.leaves": ("ambience/leaves", "Leaves rustle", 16, False), "ambience.bullfrog": ("ambience/bullfrog", "Bullfrog croaks", 32, False),
    "ambience.stone_slide": ("ambience/stone_slide", "Stones slide in the dark", 48, False),
    "ambience.gust": ("ambience/gust", "Wind gusts", None, False),
    "ambience.cicadas": ("ambience/cicadas", "Cicadas buzz", None, True),
    "ambience.splash": ("ambience/splash", "Water splashes", 16, False),
    "ambience.waterfall": ("ambience/waterfall", "Waterfall roars", 24, True),
    "entity.turkey.gobble": ("turkey/gobble", "Turkey gobbles", 48, False), "entity.turkey.cluck": ("turkey/cluck", "Turkey clucks", None, False),
    "entity.turkey.hurt": ("turkey/hurt", "Turkey hurts", None, False), "entity.turkey.death": ("turkey/death", "Turkey dies", None, False),
    "entity.squirrel.chatter": ("critter/squirrel_chatter", "Squirrel chatters", None, False),
    "entity.raccoon.chitter": ("critter/raccoon_chitter", "Raccoon chitters", None, False),
    "entity.skunk.spray": ("critter/skunk_spray", "Skunk sprays", None, False), "entity.opossum.hiss": ("critter/opossum_hiss", "Hiss", None, False),
    "entity.mink.squeak": ("critter/mink_squeak", "Squeak", None, False), "entity.beaver.slap": ("critter/beaver_slap", "Beaver slaps tail", 32, False),
    "entity.songbird.cardinal": ("songbird/cardinal", "Cardinal sings", 32, False), "entity.songbird.goldfinch": ("songbird/goldfinch", "Goldfinch sings", 32, False),
    "entity.songbird.robin": ("songbird/robin", "Robin sings", 32, False), "entity.songbird.blue_jay": ("songbird/blue_jay", "Blue jay calls", 32, False),
    "entity.songbird.bluebird": ("songbird/bluebird", "Bluebird warbles", 32, False),
}
sounds, lang = {}, {}
for ev, (prefix, sub, att, stream) in EVENTS.items():
    fs = files(prefix)
    assert fs, f"no files for {ev} ({prefix})"
    entries = []
    for f in fs:
        e = {"name": "ryancraft:" + f}
        if att: e["attenuation_distance"] = att
        if stream: e["stream"] = True
        entries.append(e)
    sounds[ev] = {"sounds": entries}
    if sub:
        sounds[ev]["subtitle"] = "subtitles.ryancraft." + ev; lang["subtitles.ryancraft." + ev] = sub
w("assets/ryancraft/sounds.json", sounds)

# ---- items
FOODS = ["venison", "cooked_venison", "raw_duck", "cooked_duck", "raw_turkey", "cooked_turkey"]
FISH = ["bluegill", "crappie", "largemouth_bass", "channel_catfish"]
ITEMS = FOODS + FISH + ["cooked_" + f for f in FISH] + [f + "_bucket" for f in FISH] + ["raccoon_tail", "coonskin_cap"]
for it in ITEMS:
    w(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/" + it}})
    w(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/" + it}})
w("assets/ryancraft/equipment/coonskin.json", {"layers": {"humanoid": [{"texture": "ryancraft:coonskin"}]}})

# ---- recipes
for raw, cooked in [("venison", "cooked_venison"), ("raw_duck", "cooked_duck"), ("raw_turkey", "cooked_turkey")] + [(f, "cooked_" + f) for f in FISH]:
    for kind, t, sfx in [("smelting", 200, ""), ("smoking", 100, "_from_smoking"), ("campfire_cooking", 600, "_from_campfire_cooking")]:
        w(f"data/ryancraft/recipe/{cooked}{sfx}.json", {"type": "minecraft:" + kind, "category": "food", "cookingtime": t,
          "experience": 0.35, "ingredient": "ryancraft:" + raw, "result": {"id": "ryancraft:" + cooked}})
w("data/ryancraft/recipe/coonskin_cap.json", {"type": "minecraft:crafting_shaped", "category": "equipment",
  "pattern": ["LTL", "L L"], "key": {"L": "minecraft:leather", "T": "ryancraft:raccoon_tail"}, "result": {"id": "ryancraft:coonskin_cap"}})

# ---- loot
def cnt(a, b): return {"type": "minecraft:uniform", "min": a, "max": b}
def pool(item, a, b, cook=False, looting=True, chance=None):
    fns = [{"function": "minecraft:set_count", "count": cnt(a, b)}]
    if cook: fns.append({"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_on_fire": True}}}]})
    if looting: fns.append({"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": cnt(0, 1)})
    p = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": fns}]}
    if chance: p["conditions"] = [{"condition": "minecraft:random_chance", "chance": chance}]
    return p
def loot(name, pools):
    w(f"data/ryancraft/loot_table/entities/{name}.json", {"type": "minecraft:entity", "pools": pools, "random_sequence": "ryancraft:entities/" + name})
loot("turkey", [pool("ryancraft:raw_turkey", 1, 2, True), pool("minecraft:feather", 1, 3)])
loot("black_bear", [pool("minecraft:leather", 0, 2)])
loot("raccoon", [pool("ryancraft:raccoon_tail", 1, 1, chance=0.5)])
loot("mink", [pool("minecraft:leather", 0, 1)])
loot("beaver", [pool("minecraft:leather", 0, 1), pool("minecraft:stick", 0, 2, looting=False)])
loot("songbird", [pool("minecraft:feather", 0, 1)])
for name in ["skunk", "opossum", "squirrel"]: loot(name, [])
for f in FISH:
    loot(f, [pool("ryancraft:" + f, 1, 1, True, looting=False), pool("minecraft:bone_meal", 1, 1, looting=False, chance=0.05)])

# Fishing: Kentucky fish join the catch (same overall fish odds as vanilla)
def fish_entry(item, weight, cook=True):
    e = {"type": "minecraft:item", "name": item, "weight": weight}
    if cook: e["functions"] = [{"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_on_fire": True}}}]}]
    return e
w("data/minecraft/loot_table/gameplay/fishing/fish.json", {"type": "minecraft:fishing", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
    fish_entry("minecraft:cod", 36), fish_entry("minecraft:salmon", 15), fish_entry("minecraft:tropical_fish", 2, False),
    fish_entry("minecraft:pufferfish", 13, False), fish_entry("ryancraft:bluegill", 12), fish_entry("ryancraft:crappie", 9),
    fish_entry("ryancraft:largemouth_bass", 8), fish_entry("ryancraft:channel_catfish", 5)]}],
    "random_sequence": "minecraft:gameplay/fishing/fish"})

# ---- tags
w("data/ryancraft/tags/item/kentucky_fish.json", {"values": ["ryancraft:" + f for f in FISH]})
w("data/ryancraft/tags/item/repairs_coonskin.json", {"values": ["ryancraft:raccoon_tail", "minecraft:leather"]})
w("data/minecraft/tags/item/fishes.json", {"replace": False, "values": ["ryancraft:" + f for f in FISH] + ["ryancraft:cooked_" + f for f in FISH]})
w("data/minecraft/tags/item/meat.json", {"replace": False, "values": ["ryancraft:venison", "ryancraft:cooked_venison", "ryancraft:raw_duck",
  "ryancraft:cooked_duck", "ryancraft:raw_turkey", "ryancraft:cooked_turkey"]})
w("data/minecraft/tags/item/head_armor.json", {"replace": False, "values": ["ryancraft:coonskin_cap"]})

# ---- lang
lang.update({
    "entity.ryancraft.boar": "Boar", "entity.ryancraft.deer": "Deer", "entity.ryancraft.duck": "Duck", "entity.ryancraft.turkey": "Wild Turkey",
    "entity.ryancraft.black_bear": "Black Bear", "entity.ryancraft.black_bear.cinnamon": "Cinnamon Bear",
    "entity.ryancraft.raccoon": "Raccoon", "entity.ryancraft.skunk": "Striped Skunk", "entity.ryancraft.mink": "Mink",
    "entity.ryancraft.opossum": "Opossum", "entity.ryancraft.beaver": "Beaver",
    "entity.ryancraft.squirrel": "Eastern Gray Squirrel", "entity.ryancraft.squirrel.fox": "Fox Squirrel",
    "entity.ryancraft.songbird": "Northern Cardinal", "entity.ryancraft.songbird.goldfinch": "American Goldfinch",
    "entity.ryancraft.songbird.robin": "American Robin", "entity.ryancraft.songbird.blue_jay": "Blue Jay",
    "entity.ryancraft.songbird.bluebird": "Eastern Bluebird",
    "entity.ryancraft.bluegill": "Bluegill", "entity.ryancraft.crappie": "Crappie", "entity.ryancraft.largemouth_bass": "Largemouth Bass",
    "entity.ryancraft.channel_catfish": "Channel Catfish",
    "item.ryancraft.venison": "Raw Venison", "item.ryancraft.cooked_venison": "Cooked Venison",
    "item.ryancraft.raw_duck": "Raw Duck", "item.ryancraft.cooked_duck": "Cooked Duck",
    "item.ryancraft.raw_turkey": "Raw Turkey", "item.ryancraft.cooked_turkey": "Roast Turkey",
    "item.ryancraft.bluegill": "Raw Bluegill", "item.ryancraft.cooked_bluegill": "Fried Bluegill",
    "item.ryancraft.crappie": "Raw Crappie", "item.ryancraft.cooked_crappie": "Fried Crappie",
    "item.ryancraft.largemouth_bass": "Raw Largemouth Bass", "item.ryancraft.cooked_largemouth_bass": "Cooked Largemouth Bass",
    "item.ryancraft.channel_catfish": "Raw Channel Catfish", "item.ryancraft.cooked_channel_catfish": "Fried Catfish",
    "item.ryancraft.bluegill_bucket": "Bucket of Bluegill", "item.ryancraft.crappie_bucket": "Bucket of Crappie",
    "item.ryancraft.largemouth_bass_bucket": "Bucket of Largemouth Bass", "item.ryancraft.channel_catfish_bucket": "Bucket of Channel Catfish",
    "item.ryancraft.raccoon_tail": "Raccoon Tail", "item.ryancraft.coonskin_cap": "Coonskin Cap",
    "biome.ryancraft.rainforest": "Rainforest", "biome.ryancraft.seasonal_forest": "Seasonal Forest",
    "biome.ryancraft.shrubland": "Shrubland", "biome.ryancraft.tundra": "Tundra",
    "tag.item.ryancraft.kentucky_fish": "Kentucky Fish", "tag.item.ryancraft.repairs_coonskin": "Coonskin Cap Repair Materials",
})
w("assets/ryancraft/lang/en_us.json", dict(sorted(lang.items())))
print("ok", len(sounds), "sound events,", len(ITEMS), "items")
