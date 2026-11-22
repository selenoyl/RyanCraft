"""The moobloom texture: the base game's mooshroom recoloured sunny yellow (it wears the old-school dandelions)."""
import colorsys, json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft"); D = os.path.join(RES, "data")

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

def moobloom_texture():
    """The base game's mooshroom (its eyes and face) recoloured: sunny yellow hide, cream patches; horns and pink nose kept."""
    src = Image.open(os.path.join(HERE, "template_mooshroom.png")).convert("RGBA"); out = src.copy(); px = out.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if v < 0.12: continue                                                  # eyes, nostrils
            if s > 0.55 and (h < 0.06 or h > 0.94):                                # red hide -> sunny yellow
                k = max(0.86, min(1.08, 0.95 + 0.6 * (v / 0.72 - 1)))
                c = tuple(int(255 * t) for t in colorsys.hsv_to_rgb(0.14, 0.88, min(1, 0.92 * k)))
            elif s < 0.12 and v > 0.4:                                             # grey patches -> cream
                k = max(0.95, min(1.04, 0.98 + 0.3 * (v / 0.68 - 1)))
                c = (int(min(255, 246 * k)), int(min(255, 234 * k)), int(min(255, 168 * k)))
            else: continue                                                         # horns, pink nose and udder
            px[x, y] = c + (a,)
    out.save(os.path.join(A, "textures", "entity", "moobloom.png"))

if __name__ == "__main__":
    moobloom_texture()
    print("moobloom ok")
