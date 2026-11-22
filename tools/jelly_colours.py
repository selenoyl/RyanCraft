"""Jellyfish colour variants: hue/saturation shifts of the pink jellyfish texture."""
import colorsys, os
from PIL import Image
T = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "ryancraft", "textures", "entity")
# name: (target hue, saturation scale, value scale)
COLOURS = {"blue": (0.58, 1.3, 0.95), "purple": (0.78, 1.15, 0.9), "amber": (0.09, 1.4, 1.0), "white": (0.6, 0.15, 1.05), "green": (0.38, 1.0, 0.95)}
if __name__ == "__main__":
    base = Image.open(os.path.join(T, "jellyfish.png")).convert("RGBA")
    for name, (hue, ss, vs) in COLOURS.items():
        im = base.copy(); px = im.load()
        for y in range(im.height):
            for x in range(im.width):
                r, g, b, a = px[x, y]
                if not a: continue
                h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                c = colorsys.hsv_to_rgb(hue, min(1, s * ss), min(1, v * vs))
                px[x, y] = tuple(int(255 * t) for t in c) + (a,)
        im.save(os.path.join(T, f"jellyfish_{name}.png"))
    print("ok")
