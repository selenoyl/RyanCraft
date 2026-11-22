"""Thirst: the droplet icons for the thirst bar (9x9, drawn to sit beside the classic hunger shanks) and the death message.
tools/resources.py merges the lang."""
import os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "ryancraft", "textures", "gui", "sprites", "hud")
SHAPE = ["....#....",
         "...#o#...",
         "...#o#...",
         "..#ooo#..",
         ".#ooooo#.",
         ".#ooooo#.",
         ".#ooooo#.",
         "..#ooo#..",
         "...###..."]

def lang_entries():
    return {"death.attack.ryancraft.dehydration": "%1$s died of thirst",
            "death.attack.ryancraft.dehydration.player": "%1$s died of thirst while fighting %2$s"}

def icon(kind):
    im = Image.new("RGBA", (9, 9), (0, 0, 0, 0)); px = im.load()
    OUT_C = (12, 18, 40, 255)
    for y, row in enumerate(SHAPE):
        for x, c in enumerate(row):
            if c == "#": px[x, y] = OUT_C
            elif c == "o":
                if kind == "empty" or (kind == "half" and x > 4): px[x, y] = (34, 38, 52, 255)     # dark, like an empty shank
                else:
                    px[x, y] = (46, 98, 214, 255) if y > 5 or x > 4 else (70, 128, 236, 255)
    if kind != "empty":                                                                      # glint
        for x, y in ((3, 4), (3, 5), (4, 3)): px[x, y] = (186, 218, 255, 255)
    return im

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for k in ("full", "half", "empty"): icon(k).save(os.path.join(OUT, f"thirst_{k}.png"))
    print("thirst ok")
