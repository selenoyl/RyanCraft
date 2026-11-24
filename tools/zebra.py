"""The zebra's coat on the vanilla horse's 64x64 texture layout -> textures/entity/zebra.png.
Uses the prototype painter (organic stripes, banded mane, striped tail with a black tuft).  python3 tools/zebra.py"""
import copy, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import prototypes2 as P
from critters import emit_textures, ROOT
from PIL import Image

def eyes(img):
    """Horse-style eyes, so they read through the stripes: a white patch broken out of the stripes round each eye, a
    black pupil, and pure white sclera on the back side of it, at vanilla's horse eye spots on the head's side faces."""
    px = img.load()
    for x, y, back in ((4, 21, -1), (15, 21, +1)):
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1): px[x + dx, y + dy] = (242, 240, 234, 255)
        px[x, y] = (10, 10, 10, 255)
        px[x + back, y] = (255, 255, 255, 255)

if __name__ == "__main__":
    label, sub, parts, style, scale = next(x for x in P.P if x[0] == "Zebra")
    parts = copy.deepcopy(parts)
    emit_textures(parts, (64, 64), {"zebra": style})
    path = os.path.join(ROOT, "textures/entity/zebra.png")
    img = Image.open(path).convert("RGBA"); P.zebra_paint(img, parts); eyes(img); img.save(path)
    print("wrote", path)
