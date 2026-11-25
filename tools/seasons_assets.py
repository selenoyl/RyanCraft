"""Seasons: the season calendar item (texture, model, recipe, lang). tools/resources.py merges the lang."""
import json, os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "ryancraft")

def lang_entries(): return {"item.ryancraft.season_calendar": "Season Calendar", "block.ryancraft.season_calendar": "Season Calendar"}

SEASON_COLOURS = [((96, 168, 72), (64, 124, 48)), ((224, 176, 44), (176, 128, 24)),     # spring, summer
                  ((206, 110, 40), (150, 70, 24)), ((120, 168, 214), (76, 118, 168))]   # autumn, winter
PICTURES = [   # 7x5 little pictures for each season: blossom, sun, falling leaf, snowflake
    ["..P.P..", ".PPYPP.", "..PPP..", "...G...", "..GGG.."],
    ["Y..Y..Y", "..YYY..", "YYYOYYY", "..YYY..", "Y..Y..Y"],
    ["....OO.", "..OORO.", ".OROO..", "OOO....", ".B....."],
    ["W..W..W", ".W.W.W.", "WWWWWWW", ".W.W.W.", "W..W..W"]]
PIC_PAL = {"P": (236, 150, 186), "Y": (246, 214, 70), "G": (84, 150, 60), "O": (212, 112, 38), "R": (176, 60, 28),
           "B": (110, 74, 40), "W": (210, 232, 250)}

def page(sub):
    """One page of the calendar on the wall: binding rings, a header in the season's colour, its little picture,
    and three dots along the bottom for early, mid and late (the current one filled)."""
    season, phase = sub // 3, sub % 3
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    PAPER, SHADE, EDGE, RING, DOT = (236, 234, 226, 255), (220, 216, 206, 255), (150, 146, 136, 255), (90, 90, 96, 255), (190, 186, 176, 255)
    hi, lo = SEASON_COLOURS[season]
    for y in range(1, 15):
        for x in range(2, 14): px[x, y] = PAPER if (x * 3 + y) % 7 else SHADE
    for x in range(2, 14): px[x, 14] = EDGE
    for y in range(1, 15): px[13, y] = EDGE
    for y in range(2, 5):
        for x in range(2, 13): px[x, y] = (hi if y < 4 else lo) + (255,)
    for x in (4, 11): px[x, 1] = RING; px[x, 2] = RING
    for r, row in enumerate(PICTURES[season]):
        for c, ch in enumerate(row):
            if ch in PIC_PAL: px[4 + c, 6 + r] = PIC_PAL[ch] + (255,)
    for i, x in enumerate((5, 8, 11)):
        px[x, 12] = (lo + (255,)) if i == phase else DOT
    return im

def wall_assets():
    for sub in range(12):
        page(sub).save(os.path.join(A, "textures", "block", f"season_calendar_{sub}.png"))
        model = {"parent": "minecraft:block/block", "textures": {"page": f"ryancraft:block/season_calendar_{sub}", "particle": f"ryancraft:block/season_calendar_{sub}"},
                 "elements": [{"from": [2, 1, 15], "to": [14, 15, 16], "faces": {
                     "north": {"uv": [2, 1, 14, 15], "texture": "#page"}, "south": {"uv": [2, 1, 14, 15], "texture": "#page"},
                     "up": {"uv": [2, 1, 14, 2], "texture": "#page"}, "down": {"uv": [2, 14, 14, 15], "texture": "#page"},
                     "east": {"uv": [13, 1, 14, 15], "texture": "#page"}, "west": {"uv": [2, 1, 3, 15], "texture": "#page"}}}]}
        json.dump(model, open(os.path.join(A, "models", "block", f"season_calendar_{sub}.json"), "w"), indent=2)
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    json.dump({"variants": {f"facing={f},page={sub}": dict({"model": f"ryancraft:block/season_calendar_{sub}"}, **({"y": r} if r else {}))
               for f, r in rot.items() for sub in range(12)}}, open(os.path.join(A, "blockstates", "season_calendar.json"), "w"), indent=2)
    json.dump({"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "ryancraft:season_calendar"}],
               "conditions": [{"condition": "minecraft:survives_explosion"}]}]},
              open(os.path.join(RES, "data", "ryancraft", "loot_table", "blocks", "season_calendar.json"), "w"), indent=2)

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
    wall_assets()
    print("seasons ok")
