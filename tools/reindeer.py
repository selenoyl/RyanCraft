"""Reindeer coats: the whitetail's textures with the coat recoloured pale grey-white (antlers untouched).
  python3 tools/reindeer.py  ->  textures/entity/reindeer{,_dark,_chocolate}.png"""
import os
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/ryancraft/textures/entity")
ANTLER_ROW = 42          # deer.png: everything from this row down is the antler patch

for suffix in ("", "_dark", "_chocolate"):
    img = Image.open(os.path.join(ROOT, f"deer{suffix}.png")).convert("RGBA"); px = img.load()
    for x in range(img.width):
        for y in range(min(ANTLER_ROW, img.height)):
            r, g, b, a = px[x, y]
            if a == 0: continue
            lum = 0.3 * r + 0.59 * g + 0.11 * b
            if lum < 50: continue                                  # eyes, nose, hooves stay dark
            v = 120 + lum * 0.62                                   # pale coat, keeping the shading
            px[x, y] = (min(255, int(v * 1.0)), min(255, int(v * 0.98)), min(255, int(v * 0.94)), a)
    img.save(os.path.join(ROOT, f"reindeer{suffix}.png"))
print("ok")
