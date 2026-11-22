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
    E["black_tailed_deer"] = (parts, {"black_tailed_deer": dict(st, antler=CLEAR), "black_tailed_deer_buck": st}, ("#7a6a58", "#141210"))
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
    return E

if __name__ == "__main__":
    for i, (name, (parts, variants, egg)) in enumerate(entries().items()):
        size = pack(parts)
        emit_model(name, parts, size)
        emit_textures(parts, size, variants)
        save_item(f"{name}_spawn_egg", egg_icon(egg[0], egg[1], 3000 + i))
        print(name, size, list(variants))
