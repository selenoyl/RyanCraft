"""Chiseled metal blocks, each with its own embossed motif on a bevelled plate:
  silver   a crescent moon and a star      lead     a diamond lattice of cames, like leaded glass
  tin      a riveted ring                  zinc     honeycomb, after zinc's hexagonal crystals
  aluminum three wing chevrons, for the light metal
  uranium  a glowing core with rays
Imported by metals_assets.py and silver_assets.py."""
import math
from PIL import Image

def grad(t, pal):
    t = min(1.0, max(0.0, t)); i = min(len(pal) - 2, int(t * (len(pal) - 1))); f = t * (len(pal) - 1) - i
    return tuple(int(pal[i][k] + (pal[i + 1][k] - pal[i][k]) * f) for k in range(3))

def motif(m):
    """Set of raised pixels (x, y) inside the 12x12 field (2..13)."""
    S = set()
    if m == "silver":
        for y in range(16):
            for x in range(16):
                if math.hypot(x - 7.5, y - 8) <= 4.6 and math.hypot(x - 9.6, y - 6.8) > 4.0: S.add((x, y))
        S -= {(x, y) for x in range(9, 14) for y in range(2, 8)}
        S |= {(11, 3), (10, 4), (11, 4), (12, 4), (11, 5)}             # the star, in the crescent's hollow
    elif m == "lead":                                                # a window of diamond quarrels, like leaded glass
        for y in range(2, 14):
            for x in range(2, 14):
                d = abs(x - 7.5) + abs(y - 7.5)
                if 5 <= d <= 6 or (d <= 5 and (x == y or x + y == 15)): S.add((x, y))
    elif m == "tin":
        for y in range(16):
            for x in range(16):
                if 3.2 <= math.hypot(x - 7.5, y - 7.5) <= 4.6: S.add((x, y))
        S |= {(3, 3), (12, 3), (3, 12), (12, 12), (7, 7), (8, 8), (7, 8), (8, 7)}
    elif m == "zinc":
        for cx, cy in ((7.5, 4.8), (4.6, 10.2), (10.4, 10.2)):        # three hexagon cells
            for y in range(16):
                for x in range(16):
                    dx, dy = abs(x - cx), abs(y - cy); hexd = max(dx * 0.87 + dy * 0.5, dy)
                    if 1.6 < hexd <= 2.8: S.add((x, y))
    elif m == "aluminum":
        for top in (3, 8):                                            # "^" chevrons, two pixels thick
            for i in range(5):
                for y in (top + i, top + i + 1): S |= {(7 - i, y), (8 + i, y)}
    elif m == "uranium":
        for y in range(16):
            for x in range(16):
                if abs(x - 7.5) + abs(y - 7.5) <= 3: S.add((x, y))
        for i in range(2, 4):
            S |= {(7, 7 - i - 2), (8, 7 - i - 2), (7, 8 + i + 2), (8, 8 + i + 2), (7 - i - 2, 7), (7 - i - 2, 8), (8 + i + 2, 7), (8 + i + 2, 8)}
    return {(x, y) for x, y in S if 2 <= x <= 13 and 2 <= y <= 13}

def chiseled(m, pal, glow=None):
    im = Image.new("RGBA", (16, 16)); px = im.load(); raised = motif(m)
    for y in range(16):
        for x in range(16):
            if x == 0 or y == 0: t = 0.95                               # outer bevel: lit top-left, shaded bottom-right
            elif x == 15 or y == 15: t = 0.2
            elif x == 1 or y == 1: t = 0.35                             # the sunk field's own edge, in shadow
            elif x == 14 or y == 14: t = 0.8
            else: t = 0.5 + 0.04 * ((x * 7 + y * 3) % 3 - 1)
            if (x, y) in raised: t = 1.0 if (x - 1, y) not in raised or (x, y - 1) not in raised else 0.8
            elif (x - 1, y - 1) in raised or (x - 1, y) in raised or (x, y - 1) in raised:
                if 2 <= x <= 13 and 2 <= y <= 13: t = 0.22                  # the motif's shadow
            px[x, y] = grad(t, pal) + (255,)
    if glow:
        for x, y in raised:
            if abs(x - 7.5) + abs(y - 7.5) <= 1.5: px[x, y] = glow + (255,)
    return im
