"""The zebra's coat on the vanilla horse's 64x64 texture layout -> textures/entity/zebra.png.
Uses the prototype painter (organic stripes, banded mane, striped tail with a black tuft).  python3 tools/zebra.py"""
import copy, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import prototypes2 as P
from critters import emit_textures, ROOT
from PIL import Image

if __name__ == "__main__":
    label, sub, parts, style, scale = next(x for x in P.P if x[0] == "Zebra")
    parts = copy.deepcopy(parts)
    emit_textures(parts, (64, 64), {"zebra": style})
    path = os.path.join(ROOT, "textures/entity/zebra.png")
    img = Image.open(path).convert("RGBA"); P.zebra_paint(img, parts); img.save(path)
    print("wrote", path)
