"""Meat icons in the Programmer Art style: venison on the base game's mutton cut (lean, deep red), and a classic diagonal
turkey drumstick. Also removes the classic pack's outlined beef/chicken/porkchop so the Programmer Art ones show,
matching rabbit and mutton."""
import colorsys, os
from PIL import Image
HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
OUT = os.path.join(RES, "assets", "ryancraft", "textures", "item")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures", "item")

def recolour(template, hue, sat_k, val_k, keep_light=True):
    im = Image.open(os.path.join(HERE, template)).convert("RGBA"); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if not a: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if keep_light and s < 0.18 and v > 0.7: continue                   # bone and fat stay
            c = colorsys.hsv_to_rgb(hue, min(1, s * sat_k), min(1, v * val_k))
            px[x, y] = tuple(int(255 * t) for t in c) + (a,)
    return im

DRUMSTICK = """
................
................
.........OOOO...
.......OOMMLMO..
......OMMLHHLMO.
.....OMMLHHLLMO.
.....OMMLLLMMDO.
....OMMMLMMMMDO.
....OMMMMMMMDDO.
....ODMMMMMDDO..
...OBODMMDDDO...
..OBbOODDDOO....
.OBbbO.OOO......
OBBbO...........
ObbO............
.OO.............
"""
def drumstick(cooked):
    pal = {"O": (110, 52, 30) if cooked else (150, 70, 70), "D": (150, 80, 36) if cooked else (205, 110, 110),
           "M": (188, 112, 52) if cooked else (232, 148, 142), "L": (214, 146, 74) if cooked else (244, 184, 176),
           "H": (236, 186, 110) if cooked else (252, 214, 206), "B": (240, 232, 214), "b": (196, 186, 166)}
    rows = DRUMSTICK.strip("\n").split("\n"); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal: img.putpixel((x, y), pal[ch] + (255,))
    return img

PEMMICAN = """
................
................
................
.......OOOOO....
.....OOMLMMDO...
....OMLMMRMDDO..
...OMMMRMMMDDO..
..OMMLMMMMRDDO..
..ODMMMRMMDDO...
..ODDMMMMDDO....
...ODDDMDDO.....
....OODDDO......
......OOO.......
................
................
................
"""
def pemmican():
    """A cake of pemmican: dark dried bison and pale fat, flecked with berries."""
    pal = {"O": (58, 34, 22), "D": (96, 56, 34), "M": (132, 82, 50), "L": (196, 160, 118), "R": (150, 30, 44)}
    rows = PEMMICAN.strip("\n").split("\n"); img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal: img.putpixel((x, y), pal[ch] + (255,))
    return img

if __name__ == "__main__":
    import meat_cuts                                                     # venison and bison have cuts of their own
    for name, im in meat_cuts.icons().items(): im.save(os.path.join(OUT, f"{name}.png"))
    pemmican().save(os.path.join(OUT, "pemmican.png"))
    drumstick(False).save(os.path.join(OUT, "raw_turkey.png")); drumstick(True).save(os.path.join(OUT, "cooked_turkey.png"))
    for n in ("beef", "cooked_beef", "chicken", "cooked_chicken", "porkchop", "cooked_porkchop"):
        p = os.path.join(CLASSIC, n + ".png")
        if os.path.exists(p): os.remove(p)
    print("meats ok")
