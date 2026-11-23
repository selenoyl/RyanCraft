"""Writes RyanCraft's JSON data/asset files (sounds.json, loot, recipes, item models, tags, lang). Run: python3 tools/resources.py"""
import sys, json, os, glob
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
    "entity.songbird.mourning_dove": ("songbird/mourning_dove", "Mourning dove coos", 40, False),
    "entity.chipmunk.chip": ("critter/chipmunk_chip", "Chipmunk chips", None, False),
    "entity.coyote.yips": ("critter/coyote_yips", "Coyotes yip", 48, False),
    "entity.coyote.howl": ("critter/coyote_howl", "Coyote howls", 64, False),
    "entity.crow.caw": ("critter/crow_caw", "Crow caws", 48, False),
    "entity.goose.honk": ("critter/goose_honk", "Goose honks", 48, False),
    "entity.goose.hiss": ("critter/goose_hiss", "Goose hisses", None, False),
    "entity.heron.croak": ("critter/heron_croak", "Heron croaks", 32, False),
    "entity.orca.call": ("critter/orca_call", "Orca calls", 48, False),
    "entity.blue_whale.song": ("critter/whale_song", "Whale sings", 96, False),
    "entity.bobwhite.call": ("critter/bobwhite_call", "Bobwhite whistles", 48, False),
    "entity.rattlesnake.rattle": ("critter/rattlesnake_rattle", "Rattlesnake rattles", 16, False),
    "entity.alligator.bellow": ("critter/alligator_bellow", "Alligator bellows", 48, False),
    "entity.elk.bugle": ("critter/elk_bugle", "Bull elk bugles", 96, False),
    "entity.elk.chirp": ("critter/elk_chirp", "Elk mews", None, False),
    "entity.moose.call": ("critter/moose_call", "Moose calls", 48, False),
    "entity.pronghorn.snort": ("critter/pronghorn_snort", "Pronghorn snorts", None, False),
    "entity.bobcat.scream": ("critter/bobcat_scream", "Bobcat screams", 64, False),
    "entity.bobcat.growl": ("critter/bobcat_growl", "Bobcat growls", None, False),
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
FISH = ["bluegill", "crappie", "largemouth_bass", "channel_catfish"] + ['rainbow_trout', 'smallmouth_bass', 'yellow_perch', 'walleye', 'northern_pike', 'muskellunge']
EGG_MOBS = ["boar", "deer", "duck", "turkey", "black_bear", "raccoon", "skunk", "mink", "opossum", "beaver", "squirrel",
            "songbird", "chipmunk", "coyote", "bluegill", "crappie", "largemouth_bass", "channel_catfish", "rainbow_trout", "smallmouth_bass", "yellow_perch", "walleye", "northern_pike", "muskellunge",
            "elk", "moose", "pronghorn", "grizzly_bear", "bobcat", "crow", "zebra", "reindeer", "moobloom", "swan", "mudling", "sky_ray", "pegasus", "cloud_sheep", "sky_roc",
            "black_tailed_deer", "bison", "cougar", "river_otter", "groundhog", "porcupine",
            "bald_eagle", "golden_eagle", "red_tailed_hawk", "turkey_vulture", "great_horned_owl", "great_blue_heron", "canada_goose",
            "orca", "blue_whale", "shark", "manta_ray", "jellyfish",
            "butterfly", "hummingbird", "pileated_woodpecker", "bobwhite", "box_turtle", "rattlesnake", "bullfrog", "alligator"]
ITEMS = FOODS + FISH + ["cooked_" + f for f in FISH] + [f + "_bucket" for f in FISH] + ["raccoon_tail", "coonskin_cap", "cherries", "cherry_pie", "black_walnut", "shelled_walnut"] \
        + [m + "_spawn_egg" for m in EGG_MOBS]
for it in ITEMS:
    w(f"assets/ryancraft/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/" + it}})
    w(f"assets/ryancraft/items/{it}.json", {"model": {"type": "minecraft:model", "model": "ryancraft:item/" + it}})
# Skull mount: one item, icon chosen by custom_model_data string, antlers tinted by its colour
MOUNT_KEYS = ["s1", "s2", "s3", "s4", "drop3", "drop4", "cactus", "palmated", "lopsided"]
def mount_model(k):
    return {"type": "minecraft:model", "model": "ryancraft:item/deer_skull_mount_" + k,
            "tints": [{"type": "minecraft:constant", "value": -1},
                      {"type": "minecraft:custom_model_data", "index": 0, "default": -2702180}]}
for k in MOUNT_KEYS:
    w(f"assets/ryancraft/models/item/deer_skull_mount_{k}.json", {"parent": "minecraft:item/generated",
      "textures": {"layer0": "ryancraft:item/deer_skull_mount_base", "layer1": "ryancraft:item/deer_skull_mount_antlers_" + k}})
w("assets/ryancraft/items/deer_skull_mount.json", {"model": {"type": "minecraft:select", "property": "minecraft:custom_model_data",
  "index": 0, "cases": [{"when": k, "model": mount_model(k)} for k in MOUNT_KEYS], "fallback": mount_model("s4")}})
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
for name in ["skunk", "opossum", "squirrel", "chipmunk"]: loot(name, [])
loot("coyote", [pool("minecraft:bone", 0, 1)])
loot("elk", [pool("ryancraft:venison", 2, 4, True), pool("minecraft:leather", 0, 2)])
loot("moose", [pool("ryancraft:venison", 3, 5, True), pool("minecraft:leather", 1, 3)])
loot("pronghorn", [pool("ryancraft:venison", 1, 2, True), pool("minecraft:leather", 0, 1)])
loot("grizzly_bear", [pool("minecraft:leather", 1, 3)])
loot("bobcat", [])
loot("crow", [pool("minecraft:feather", 0, 2)])
loot("zebra", [pool("minecraft:leather", 0, 2)])
loot("swan", [pool("minecraft:feather", 1, 3), pool("minecraft:chicken", 1, 2, True)])
loot("moobloom", [pool("minecraft:beef", 1, 3, True), pool("minecraft:leather", 0, 2)])
loot("reindeer", [pool("ryancraft:venison", 1, 3, True), pool("minecraft:leather", 0, 2)])
loot("black_tailed_deer", [pool("ryancraft:venison", 1, 3, True), pool("minecraft:leather", 0, 1)])
loot("bison", [pool("minecraft:beef", 3, 5, True), pool("minecraft:leather", 1, 3)])
for name in ["cougar", "river_otter", "groundhog", "porcupine"]: loot(name, [])
for name in ["bald_eagle", "golden_eagle", "red_tailed_hawk", "turkey_vulture", "great_horned_owl", "great_blue_heron"]: loot(name, [pool("minecraft:feather", 0, 2)])
loot("canada_goose", [pool("ryancraft:raw_duck", 1, 1, True), pool("minecraft:feather", 1, 3)])
for name in ["orca", "blue_whale", "shark", "manta_ray"]: loot(name, [])
loot("jellyfish", [pool("minecraft:slime_ball", 0, 1)])
for name in ["butterfly", "box_turtle", "rattlesnake", "bullfrog"]: loot(name, [])
for name in ["hummingbird", "pileated_woodpecker"]: loot(name, [pool("minecraft:feather", 0, 1)])
loot("bobwhite", [pool("ryancraft:raw_duck", 0, 1, True), pool("minecraft:feather", 0, 2)])
loot("alligator", [pool("minecraft:leather", 1, 2)])
for f in FISH:     # the fish item keeps the length of the fish it came from
    fp = pool("ryancraft:" + f, 1, 1, True, looting=False); fp["entries"][0].setdefault("functions", []).insert(0, {"function": "ryancraft:fish_length"})
    loot(f, [fp, pool("minecraft:bone_meal", 1, 1, looting=False, chance=0.05)])

# Armadillos drop their scutes when killed; no brush needed (babies drop nothing)
w("data/minecraft/loot_table/entities/armadillo.json", {"type": "minecraft:entity", "random_sequence": "minecraft:entities/armadillo",
  "pools": [{"rolls": 1, "conditions": [{"condition": "minecraft:entity_properties", "entity": "this",
                                         "predicate": {"flags": {"is_baby": False}}}],
             "entries": [{"type": "minecraft:item", "name": "minecraft:armadillo_scute", "functions": [
                 {"function": "minecraft:set_count", "count": cnt(1, 3)},
                 {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": cnt(0, 1)}]}]}]})

ADULT = [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_baby": False}}}]
LOOTING = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": cnt(0, 1)}
# Goats drop their horns when killed (vanilla: only from ramming)
w("data/minecraft/loot_table/entities/goat.json", {"type": "minecraft:entity", "random_sequence": "minecraft:entities/goat",
  "pools": [{"rolls": cnt(1, 2), "conditions": ADULT, "entries": [{"type": "minecraft:item", "name": "minecraft:goat_horn",
             "functions": [{"function": "minecraft:set_instrument", "options": "#minecraft:regular_goat_horns"}]}]}]})
# Turtles drop scutes when killed (vanilla: only when a baby grows up); vanilla seagrass and lightning bowl kept
w("data/minecraft/loot_table/entities/turtle.json", {"type": "minecraft:entity", "random_sequence": "minecraft:entities/turtle",
  "pools": [
    {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:seagrass",
                              "functions": [{"function": "minecraft:set_count", "count": cnt(0, 2)}, LOOTING]}]},
    {"rolls": 1, "conditions": [{"condition": "minecraft:damage_source_properties",
                                 "predicate": {"tags": [{"id": "minecraft:is_lightning", "expected": True}]}}],
     "entries": [{"type": "minecraft:item", "name": "minecraft:bowl"}]},
    {"rolls": 1, "conditions": ADULT, "entries": [{"type": "minecraft:item", "name": "minecraft:turtle_scute",
                              "functions": [{"function": "minecraft:set_count", "count": cnt(1, 3)}, LOOTING]}]}]})

# Fishing: Kentucky fish join the catch (same overall fish odds as vanilla)
def fish_entry(item, weight, cook=True):
    e = {"type": "minecraft:item", "name": item, "weight": weight}
    if item.startswith("ryancraft:"): e["functions"] = [{"function": "ryancraft:fish_length"}]    # every catch is measured
    if cook: e.setdefault("functions", []).append({"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_on_fire": True}}}]})
    return e
from fishing_assets import fish_table; w("data/minecraft/loot_table/gameplay/fishing/fish.json", fish_table())  # freshwater vs ocean

# ---- tags
w("data/ryancraft/tags/item/kentucky_fish.json", {"values": ["ryancraft:" + f for f in FISH]})
w("data/ryancraft/tags/item/repairs_coonskin.json", {"values": ["ryancraft:raccoon_tail", "minecraft:leather"]})
w("data/minecraft/tags/item/fishes.json", {"replace": False, "values": ["ryancraft:" + f for f in FISH] + ["ryancraft:cooked_" + f for f in FISH]})
w("data/minecraft/tags/item/meat.json", {"replace": False, "values": ["ryancraft:venison", "ryancraft:cooked_venison", "ryancraft:raw_duck",
  "ryancraft:cooked_duck", "ryancraft:raw_turkey", "ryancraft:cooked_turkey"]})
w("data/minecraft/tags/item/head_armor.json", {"replace": False, "values": ["ryancraft:coonskin_cap"]})

# ---- lang
lang.update({
    "entity.ryancraft.boar": "Boar", "entity.ryancraft.deer": "Deer", "entity.ryancraft.deer.trophy": "Trophy Buck", "entity.ryancraft.deer.drop_tine": "Drop-Tine Buck", "entity.ryancraft.duck": "Duck", "entity.ryancraft.turkey": "Wild Turkey",
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
    "item.ryancraft.raccoon_tail": "Raccoon Tail",
    "entity.ryancraft.rainbow_trout": "Rainbow Trout", "item.ryancraft.rainbow_trout": "Raw Rainbow Trout", "item.ryancraft.cooked_rainbow_trout": "Pan-fried Trout", "item.ryancraft.rainbow_trout_bucket": "Bucket of Rainbow Trout", "entity.ryancraft.smallmouth_bass": "Smallmouth Bass", "item.ryancraft.smallmouth_bass": "Raw Smallmouth Bass", "item.ryancraft.cooked_smallmouth_bass": "Cooked Smallmouth Bass", "item.ryancraft.smallmouth_bass_bucket": "Bucket of Smallmouth Bass", "entity.ryancraft.yellow_perch": "Yellow Perch", "item.ryancraft.yellow_perch": "Raw Yellow Perch", "item.ryancraft.cooked_yellow_perch": "Fried Perch", "item.ryancraft.yellow_perch_bucket": "Bucket of Yellow Perch", "entity.ryancraft.walleye": "Walleye", "item.ryancraft.walleye": "Raw Walleye", "item.ryancraft.cooked_walleye": "Fried Walleye", "item.ryancraft.walleye_bucket": "Bucket of Walleye", "entity.ryancraft.northern_pike": "Northern Pike", "item.ryancraft.northern_pike": "Raw Northern Pike", "item.ryancraft.cooked_northern_pike": "Cooked Pike", "item.ryancraft.northern_pike_bucket": "Bucket of Northern Pike", "entity.ryancraft.muskellunge": "Muskellunge", "item.ryancraft.muskellunge": "Raw Muskellunge", "item.ryancraft.cooked_muskellunge": "Cooked Muskie", "item.ryancraft.muskellunge_bucket": "Bucket of Muskellunge",
    "tooltip.ryancraft.fish_length": "%s in", "tooltip.ryancraft.trophy_fish": "Trophy", "tooltip.ryancraft.legendary_fish": "Legendary",
    "entity.ryancraft.coyote": "Coyote",
    "entity.ryancraft.elk": "Elk", "entity.ryancraft.elk.bull": "Bull Elk",
    "entity.ryancraft.moose": "Moose", "entity.ryancraft.moose.bull": "Bull Moose",
    "entity.ryancraft.pronghorn": "Pronghorn", "entity.ryancraft.pronghorn.buck": "Pronghorn Buck",
    "entity.ryancraft.grizzly_bear": "Grizzly Bear", "entity.ryancraft.bobcat": "Bobcat", "entity.ryancraft.crow": "American Crow", "entity.ryancraft.zebra": "Zebra", "entity.ryancraft.reindeer": "Reindeer", "entity.ryancraft.reindeer.trophy": "Trophy Reindeer",
    "entity.ryancraft.reindeer.nontypical": "Non-typical Reindeer",
    "entity.ryancraft.elk.trophy": "Trophy Bull Elk", "entity.ryancraft.elk.nontypical": "Non-typical Bull Elk",
    "entity.ryancraft.black_tailed_deer.trophy": "Trophy Black-tailed Buck", "entity.ryancraft.black_tailed_deer.nontypical": "Non-typical Black-tailed Buck",
    "entity.ryancraft.black_tailed_deer": "Black-tailed Deer", "entity.ryancraft.black_tailed_deer.buck": "Black-tailed Buck",
    "entity.ryancraft.bison": "American Bison", "entity.ryancraft.cougar": "Cougar", "entity.ryancraft.river_otter": "River Otter",
    "entity.ryancraft.groundhog": "Groundhog", "entity.ryancraft.porcupine": "Porcupine",
    "entity.ryancraft.bald_eagle": "Bald Eagle", "entity.ryancraft.golden_eagle": "Golden Eagle", "entity.ryancraft.red_tailed_hawk": "Red-tailed Hawk",
    "entity.ryancraft.turkey_vulture": "Turkey Vulture", "entity.ryancraft.great_horned_owl": "Great Horned Owl",
    "entity.ryancraft.great_blue_heron": "Great Blue Heron", "entity.ryancraft.canada_goose": "Canada Goose",
    "entity.ryancraft.orca": "Orca", "entity.ryancraft.blue_whale": "Blue Whale", "entity.ryancraft.shark": "Shark",
    "entity.ryancraft.manta_ray": "Manta Ray", "entity.ryancraft.jellyfish": "Jellyfish",
    "entity.ryancraft.butterfly": "Butterfly", "entity.ryancraft.hummingbird": "Ruby-throated Hummingbird", "entity.ryancraft.pileated_woodpecker": "Pileated Woodpecker", "entity.ryancraft.bobwhite": "Northern Bobwhite", "entity.ryancraft.box_turtle": "Eastern Box Turtle", "entity.ryancraft.rattlesnake": "Timber Rattlesnake", "entity.ryancraft.bullfrog": "Bullfrog", "entity.ryancraft.alligator": "American Alligator",
    "entity.ryancraft.butterfly.monarch": "Monarch", "entity.ryancraft.butterfly.viceroy": "Viceroy", "entity.ryancraft.butterfly.eastern_tiger_swallowtail": "Eastern Tiger Swallowtail", "entity.ryancraft.butterfly.black_swallowtail": "Black Swallowtail", "entity.ryancraft.butterfly.pipevine_swallowtail": "Pipevine Swallowtail", "entity.ryancraft.butterfly.spicebush_swallowtail": "Spicebush Swallowtail", "entity.ryancraft.butterfly.zebra_swallowtail": "Zebra Swallowtail", "entity.ryancraft.butterfly.red_admiral": "Red Admiral", "entity.ryancraft.butterfly.painted_lady": "Painted Lady", "entity.ryancraft.butterfly.great_spangled_fritillary": "Great Spangled Fritillary", "entity.ryancraft.butterfly.gulf_fritillary": "Gulf Fritillary", "entity.ryancraft.butterfly.mourning_cloak": "Mourning Cloak", "entity.ryancraft.butterfly.question_mark": "Question Mark", "entity.ryancraft.butterfly.red_spotted_purple": "Red-spotted Purple", "entity.ryancraft.butterfly.cabbage_white": "Cabbage White", "entity.ryancraft.butterfly.clouded_sulphur": "Clouded Sulphur", "entity.ryancraft.butterfly.spring_azure": "Spring Azure", "entity.ryancraft.butterfly.common_buckeye": "Common Buckeye", "entity.ryancraft.butterfly.luna_moth": "Luna Moth", "entity.ryancraft.butterfly.zebra_longwing": "Zebra Longwing", "entity.ryancraft.chipmunk": "Eastern Chipmunk",
    "entity.ryancraft.songbird.mourning_dove": "Mourning Dove",
    "item.ryancraft.deer_skull_mount": "Deer Skull Mount",
    "item.ryancraft.deer_skull_mount.s1": "Spike Buck Skull Mount", "item.ryancraft.deer_skull_mount.s2": "Fork Buck Skull Mount",
    "item.ryancraft.deer_skull_mount.s3": "Eight-Point Skull Mount", "item.ryancraft.deer_skull_mount.s4": "Trophy Buck Skull Mount",
    "item.ryancraft.deer_skull_mount.drop": "Drop-Tine Buck Skull Mount", "item.ryancraft.deer_skull_mount.cactus": "Cactus Buck Skull Mount",
    "item.ryancraft.deer_skull_mount.palmated": "Palmated Buck Skull Mount", "item.ryancraft.deer_skull_mount.lopsided": "Lopsided Buck Skull Mount",
    "item.ryancraft.deer_skull_mount.dark": "Dark antlers", "item.ryancraft.deer_skull_mount.chocolate": "Chocolate antlers (rare)",
    "item.ryancraft.deer_skull_mount.rare": "Super rare rack",
    "entity.ryancraft.deer_skull_mount": "Deer Skull Mount",
    "entity.ryancraft.deer.cactus": "Cactus Buck", "entity.ryancraft.deer.palmated": "Palmated Buck", "entity.ryancraft.deer.lopsided": "Lopsided Buck", "item.ryancraft.coonskin_cap": "Coonskin Cap",
    "biome.ryancraft.rainforest": "Rainforest", "biome.ryancraft.seasonal_forest": "Seasonal Forest",
    "biome.ryancraft.shrubland": "Shrubland", "biome.ryancraft.tundra": "Tundra",
    "tag.item.ryancraft.kentucky_fish": "Kentucky Fish", "tag.item.ryancraft.repairs_coonskin": "Coonskin Cap Repair Materials",
})
EGG_NAMES = {"boar": "Boar", "deer": "Deer", "duck": "Duck", "turkey": "Wild Turkey", "black_bear": "Black Bear", "raccoon": "Raccoon",
             "skunk": "Skunk", "mink": "Mink", "opossum": "Opossum", "beaver": "Beaver", "squirrel": "Squirrel", "songbird": "Songbird",
             "chipmunk": "Chipmunk", "coyote": "Coyote", "bluegill": "Bluegill", "crappie": "Crappie", "largemouth_bass": "Largemouth Bass",
             "channel_catfish": "Channel Catfish", "rainbow_trout": "Rainbow Trout", "smallmouth_bass": "Smallmouth Bass", "yellow_perch": "Yellow Perch", "walleye": "Walleye", "northern_pike": "Northern Pike", "muskellunge": "Muskellunge", "elk": "Elk", "moose": "Moose", "pronghorn": "Pronghorn",
             "grizzly_bear": "Grizzly Bear", "bobcat": "Bobcat", "crow": "Crow", "zebra": "Zebra", "reindeer": "Reindeer",
             "black_tailed_deer": "Black-tailed Deer", "bison": "Bison", "cougar": "Cougar", "river_otter": "River Otter",
             "groundhog": "Groundhog", "porcupine": "Porcupine", "bald_eagle": "Bald Eagle", "golden_eagle": "Golden Eagle",
             "red_tailed_hawk": "Red-tailed Hawk", "turkey_vulture": "Turkey Vulture", "great_horned_owl": "Great Horned Owl",
             "great_blue_heron": "Great Blue Heron", "canada_goose": "Canada Goose", "orca": "Orca", "blue_whale": "Blue Whale",
             "shark": "Shark", "manta_ray": "Manta Ray", "jellyfish": "Jellyfish", "butterfly": "Butterfly", "hummingbird": "Ruby-throated Hummingbird", "pileated_woodpecker": "Pileated Woodpecker", "bobwhite": "Northern Bobwhite", "box_turtle": "Eastern Box Turtle", "rattlesnake": "Timber Rattlesnake", "bullfrog": "Bullfrog", "alligator": "American Alligator"}
for m, n in EGG_NAMES.items(): lang[f"item.ryancraft.{m}_spawn_egg"] = f"{n} Spawn Egg"
sys.path.insert(0, os.path.dirname(__file__)); from wood import lang_entries; lang.update(lang_entries())
from cages import lang_entries as cage_lang; lang.update(cage_lang())
from storage_assets import lang_entries as storage_lang; lang.update(storage_lang())
lang["entity.ryancraft.moobloom"] = "Moobloom"; lang["item.ryancraft.moobloom_spawn_egg"] = "Moobloom Spawn Egg"
lang["entity.ryancraft.mudling"] = "Mudling"; lang["item.ryancraft.mudling_spawn_egg"] = "Mudling Spawn Egg"
lang["entity.ryancraft.swan"] = "Mute Swan"; lang["entity.ryancraft.swan.black"] = "Black Swan"; lang["item.ryancraft.swan_spawn_egg"] = "Swan Spawn Egg"
from flora import lang_entries as flora_lang; lang.update(flora_lang())
from autumn import lang_entries as autumn_lang; lang.update(autumn_lang())
from pipes_assets import lang_entries as pipes_lang; lang.update(pipes_lang())
from fluid_blocks_assets import lang_entries as fluid_lang; lang.update(fluid_lang())
from seasons_assets import lang_entries as seasons_lang; lang.update(seasons_lang())
from fishing_assets import lang_entries as fishing_lang; lang.update(fishing_lang())
from thirst_assets import lang_entries as thirst_lang; lang.update(thirst_lang())
from steel_assets import lang_entries as steel_lang; lang.update(steel_lang())
from moon_assets import lang_entries as moon_lang; lang.update(moon_lang())
from biomes import lang_entries as biomes_lang; lang.update(biomes_lang())
from gem_blocks import lang_entries as gem_lang; lang.update(gem_lang())
from skylands import lang_entries as sky_lang; lang.update(sky_lang())
from gear_assets import lang_entries as gear_lang; lang.update(gear_lang())
from metals_assets import lang_entries as metals_lang; lang.update(metals_lang())
from silver_assets import lang_entries as silver_lang; lang.update(silver_lang())
from leaded_glass import lang_entries as glass_lang; lang.update(glass_lang())
from sky_flowers import lang_entries as skyflower_lang; lang.update(skyflower_lang())
w("assets/ryancraft/lang/en_us.json", dict(sorted(lang.items())))
print("ok", len(sounds), "sound events,", len(ITEMS), "items")
