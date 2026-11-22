"""Swan model + textures (mute swan, black swan) for the shared critter renderer."""
import random
import critters
from critters import box, part, pack, emit_model, emit_textures

swan = [
    part("body", (0, 19, 0), [box("body", (-3, -2.5, -5), (6, 5, 10), "body"),
                              box("tail", (-2, -3.5, 4), (4, 2, 3), "body")], "body"),
    part("head", (0, 17, -4), [box("neck", (-1, -9, -1), (2, 9, 2), "neck"),
                               box("head", (-1.5, -11, -3.5), (3, 2.5, 4), "head"),
                               box("beak", (-1, -10.25, -6.5), (2, 1.5, 3), "beak"),
                               box("knob", (-0.5, -11, -4.5), (1, 1, 1), "knob")], "head"),
    part("wing_r", (-3, 17, -3), [box("wing", (-1, 0, 0), (1, 4, 8), "wing")], "wing_r"),
    part("wing_l", (3, 17, -3), [box("wing", (0, 0, 0), (1, 4, 8), "wing", mirror=True)], "wing_l"),
    part("leg_r", (-1.5, 21.5, 1), [box("leg", (-0.5, 0, -0.5), (1, 2.5, 1), "leg")], "leg_r"),
    part("leg_l", (1.5, 21.5, 1), [box("leg", (-0.5, 0, -0.5), (1, 2.5, 1), "leg", mirror=True)], "leg_l"),
]
VARIANTS = {
    "swan": {"body": {"c": "#f2f2ee", "noise": 5, "bottom": "#e4e4de"}, "neck": {"c": "#f4f4f0", "noise": 4},
             "head": {"c": "#f4f4f0", "noise": 4, "side_eyes": "#141414"}, "beak": {"c": "#e8802a", "front": "#2a2420"},
             "knob": {"c": "#1a1614"}, "wing": {"c": "#eaeae4", "noise": 6}, "leg": {"c": "#2a2a2a"}},
    "black_swan": {"body": {"c": "#1c1c20", "noise": 6, "bottom": "#26262a"}, "neck": {"c": "#1e1e22", "noise": 5},
                   "head": {"c": "#1e1e22", "noise": 5, "side_eyes": "#c82a2a"}, "beak": {"c": "#c42a2a", "front": "#f0ece4"},
                   "knob": {"c": "#c42a2a"}, "wing": {"c": "#1a1a1e", "noise": 6, "tip": {"c": "#f0f0ec", "frac": 0.25}}, "leg": {"c": "#2a2a2a"}},
}
if __name__ == "__main__":
    critters.rnd.seed(77) if hasattr(critters, "rnd") else random.seed(77)
    size = pack(swan); emit_model("swan", swan, size); emit_textures(swan, size, VARIANTS); print("swan", size)
