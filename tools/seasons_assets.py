"""Seasons: the season calendar item (texture, model, recipe, lang). tools/resources.py merges the lang."""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft")

def lang_entries(): return {"item.ryancraft.season_calendar": "Season Calendar"}

def calendar():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    PAPER, SHADE, EDGE = (234, 234, 234, 255), (214, 214, 214, 255), (140, 140, 132, 255)
    RED, DRED, RING, CELL = (196, 52, 40, 255), (140, 30, 24, 255), (90, 90, 96, 255), (168, 168, 160, 255)
    for y in range(3, 15):
        for x in range(2, 14): px[x, y] = PAPER if (x + y) % 5 else SHADE
    for x in range(2, 14): px[x, 14] = EDGE                       # page bottom and right edge, shaded like vanilla paper
    for y in range(3, 15): px[13, y] = EDGE
    for y in range(3, 6):                                          # red header band
        for x in range(2, 14): px[x, y] = RED if y < 5 else DRED
    for x in (4, 11):                                              # binding rings
        px[x, 2] = RING; px[x, 3] = RING
    for gy in range(3):                                            # the days
        for gx in range(4):
            px[3 + gx * 2 + (gx > 1), 7 + gy * 2] = CELL
    px[10, 11] = RED; px[9, 11] = DRED                             # today, circled
    return im

if __name__ == "__main__":
    calendar().save(os.path.join(A, "textures", "item", "season_calendar.png"))
    json.dump({"model": {"type": "minecraft:model", "model": "ryancraft:item/season_calendar"}}, open(os.path.join(A, "items", "season_calendar.json"), "w"), indent=2)
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "ryancraft:item/season_calendar"}},
              open(os.path.join(A, "models", "item", "season_calendar.json"), "w"), indent=2)
    json.dump({"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ["minecraft:paper", "minecraft:paper", "minecraft:paper", "minecraft:clock"],
               "result": {"id": "ryancraft:season_calendar", "count": 1}},
              open(os.path.join(RES, "data", "ryancraft", "recipe", "season_calendar.json"), "w"), indent=2)
    print("seasons ok")
