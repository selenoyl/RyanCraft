"""
In-game assets for the prototyped wildlife (tools/prototypes.py, prototypes2.py): models, textures (with variants),
spawn-egg icons. Each entry takes an approved prototype by its label.
  python3 tools/wildlife.py
"""
import copy, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import prototypes as P1, prototypes2 as P2
from prototypes import pack
from critters import emit_model, emit_textures, egg_icon, save_item
from big_game import CLEAR

def proto(label, sub=None):
    for lab, s, parts, style, scale in P1.P + P2.P:
        if lab == label and (sub is None or s == sub): return copy.deepcopy(parts), copy.deepcopy(style)
    raise KeyError(label)

# asset name -> (prototype label, {texture: style overrides}, egg colours)
def entries():
    E = {}
    parts, st = proto("Black-tailed Deer")
    from big_game import staged_antlers
    # forked (mule-deer style) racks by stage: spike; beam; fork; brow tines. Non-typical extras.
    parts = staged_antlers(parts, {1: [((-2, -8, -4), (1, 5, 1))], 2: [((-3, -11, -4), (1, 4, 1))],
                                   3: [((-3, -11, -6), (1, 1, 2)), ((-3, -13, -6), (1, 2, 1))],
                                   4: [((-2, -9, -6), (1, 1, 2)), ((-2, -11, -7), (1, 2, 1))]},
                           [((-4, -12, -3), (1, 1, 1)), ((-3, -14, -4), (1, 2, 1)), ((-2, -7, -6), (1, 2, 1))])
    E["black_tailed_deer"] = (parts, {"black_tailed_deer": dict(st, antler=CLEAR), "black_tailed_deer_buck": st,
                                      "black_tailed_deer_buck_dark": dict(st, antler={"c": "#8a6c4a", "noise": 10}),
                                      "black_tailed_deer_buck_chocolate": dict(st, antler={"c": "#5e4430", "noise": 8})}, ("#7a6a58", "#141210"))
    parts, st = proto("American Bison")
    E["bison"] = (parts, {"bison": st}, ("#5a3c24", "#2a1a10"))
    parts, st = proto("Cougar")
    E["cougar"] = (parts, {"cougar": st}, ("#b88a5a", "#efe4d2"))
    parts, st = proto("River Otter")
    E["river_otter"] = (parts, {"river_otter": st}, ("#5a3a24", "#c8b498"))
    parts, st = proto("Groundhog")
    E["groundhog"] = (parts, {"groundhog": st}, ("#7a5a3a", "#c8b49a"))
    parts, st = proto("Porcupine")
    E["porcupine"] = (parts, {"porcupine": st}, ("#3a2e26", "#e8e0d0"))
    # ---- big birds: raptors get soaring roles (wings fold when perched, spread in flight)
    def soaring(parts):
        for p in parts:
            p["role"] = {"wing_r": "soar_wing_r", "wing_l": "soar_wing_l", "body": "soar_body"}.get(p["role"], p["role"])
        return parts
    for name, label, egg in [("bald_eagle", "Bald Eagle", ("#3a2a1c", "#f2f0ea")), ("golden_eagle", "Golden Eagle", ("#3a2a1c", "#c8962e")),
                             ("red_tailed_hawk", "Red-tailed Hawk", ("#6a4a2e", "#b85a2a"))]:
        parts, st = proto(label, "perched" if "Eagle" in label else None)
        E[name] = (soaring(parts), {name: st}, egg)
    _, vst = proto("Turkey Vulture")
    E["turkey_vulture"] = (soaring(P1.raptor((5, 5, 9), 3, 6, (6, 10), beak=(1, 1, 2))), {"turkey_vulture": vst}, ("#2a221e", "#b8302a"))
    for name, label, egg in [("great_horned_owl", "Great Horned Owl", ("#7a5a3a", "#e8b820")), ("great_blue_heron", "Great Blue Heron", ("#7a8a9a", "#d8a830")),
                             ("canada_goose", "Canada Goose", ("#7a6a58", "#141210"))]:
        parts, st = proto(label)
        E[name] = (parts, {name: st}, egg)
    # ---- ocean: whale flukes beat up and down
    def whale_tail(parts):
        for p in parts:
            if p["role"] == "fish_tail" and any(b["name"] == "fluke" for b in p["boxes"]): p["role"] = "whale_tail"
        return parts
    for name, label, egg in [("orca", "Orca", ("#141416", "#f2f2f0")), ("blue_whale", "Blue Whale", ("#5a7084", "#8aa0b2")),
                             ("shark", "Shark", ("#6a7480", "#eceeee")), ("manta_ray", "Manta Ray", ("#1e2026", "#eeeeec")),
                             ("jellyfish", "Jellyfish", ("#e8a8c8", "#d07aa8"))]:
        parts, st = proto(label)
        E[name] = (whale_tail(parts), {name: st}, egg)
    # ---- small things
    for name, label, egg in [("hummingbird", "Ruby-throated Hummingbird", ("#4a8a3a", "#c8202a")), ("pileated_woodpecker", "Pileated Woodpecker", ("#141214", "#d8202a")),
                             ("bobwhite", "Northern Bobwhite", ("#8a5a36", "#f2f0ea")), ("box_turtle", "Eastern Box Turtle", ("#3a2a1a", "#e0a030")),
                             ("rattlesnake", "Timber Rattlesnake", ("#a8946c", "#2a221a")), ("bullfrog", "Bullfrog", ("#5a7a34", "#e8e0a8")),
                             ("alligator", "American Alligator", ("#3a4a2c", "#c8c08a"))]:
        parts, st = proto(label)
        E[name] = (parts, {name: st}, egg)
    return E

if __name__ == "__main__":
    for i, (name, (parts, variants, egg)) in enumerate(entries().items()):
        size = pack(parts)
        emit_model(name, parts, size)
        emit_textures(parts, size, variants)
        save_item(f"{name}_spawn_egg", egg_icon(egg[0], egg[1], 3000 + i))
        print(name, size, list(variants))
