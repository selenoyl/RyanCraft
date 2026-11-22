"""
Tree crowns built the way vanilla's fancy oak does it: limbs reaching out from the trunk, each ending in a
flat-bottomed leaf cluster, placed inside a per-species crown outline. Prototype for TreeSpecies.java.
  python3 tools/trees2.py [out.png]
"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(__file__))
import trees as T
from PIL import Image, ImageDraw

def round_env(f): return math.sqrt(max(0, 1 - (2 * f - 1) ** 2))
def egg(p):
    return lambda f: math.sqrt(max(0, 1 - ((p - f) / p) ** 2)) if f < p else math.sqrt(max(0, 1 - ((f - p) / (1 - p)) ** 2))
def vase(f): return min(1.0, 0.35 + 0.9 * f) if f < 0.8 else max(0.3, 1 - (f - 0.8) * 3.5)

class Crown(T.Tree):
    def cluster(self, c, size=1.0, kind="leaf", layers=(2, 3, 3, 2), density=0.85):
        cx, cy, cz = c
        for i, base in enumerate(layers):
            y = round(cy) + i - 1                         # the limb ends in the second layer: flat bottom, round top
            rr = base * size
            for dx in range(-4, 5):
                for dz in range(-4, 5):
                    d = math.hypot(dx, dz)
                    if d > rr + 0.4: continue
                    if d > rr - 0.6 and self.r.random() > density: continue   # ragged rim
                    self.put(round(cx) + dx, y, round(cz) + dz, kind)
        self.put(cx, cy, cz, "log")

    def crown(self, h, trunk_h, base_f, env, width, tips, slope, size=1.0, w=1, limb="log", density=0.85,
              layers=(2, 3, 3, 2), top=True):
        self.trunk(trunk_h, w)
        y0, y1 = h * base_f, h - 2
        ga = self.r.random() * math.tau
        for k in range(tips):
            f = (k + self.r.random()) / tips                       # spread up the crown
            y = y0 + f * (y1 - y0)
            dist = env(f) * width * (0.5 + 0.5 * self.r.random())
            ga += 2.39996 + self.r.uniform(-.3, .3)                  # golden angle, so limbs don't line up
            tip = (math.cos(ga) * dist, y, math.sin(ga) * dist)
            start = min(trunk_h - 1, max(2, y - dist * slope))
            self.line((0, start, 0), tip, limb if start > trunk_h * 0.6 else "log")
            self.cluster(tip, size * (0.85 + 0.3 * self.r.random()), layers=layers, density=density)
        if top: self.cluster((0, trunk_h, 0), size, layers=layers, density=density)

def white_oak(s):
    t = Crown(s); h = t.r.randint(12, 15)
    t.crown(h, int(h * .55), .45, egg(.4), 7.5, 11, .35, w=2, density=.75); return t
def red_oak(s):
    t = Crown(s); h = t.r.randint(14, 17)
    t.crown(h, int(h * .7), .45, round_env, 5, 10, .7, density=.8); return t
def sugar_maple(s):
    t = Crown(s); h = t.r.randint(15, 18)
    t.crown(h, int(h * .7), .35, egg(.38), 5.5, 14, .8, density=.9); return t
def red_maple(s):
    t = Crown(s); h = t.r.randint(12, 15)
    t.crown(h, int(h * .75), .4, round_env, 3.6, 9, 1.0, density=.8); return t
def tulip_poplar(s):
    t = Crown(s); h = t.r.randint(22, 27)
    t.crown(h, h - 3, .5, egg(.45), 4.2, 15, 1.0, size=.9); return t
def shagbark_hickory(s):
    t = Crown(s); h = t.r.randint(16, 20)
    t.crown(h, h - 3, .45, round_env, 3.8, 10, .8, size=.85, density=.75); return t
def black_walnut(s):
    t = Crown(s); h = t.r.randint(13, 16)
    t.crown(h, int(h * .6), .4, egg(.55), 6.5, 9, .5, size=.85, density=.65); return t
def american_beech(s):
    t = Crown(s); h = t.r.randint(10, 13)
    t.crown(h, int(h * .6), .38, egg(.3), 6.5, 12, .45, density=.85); return t
def sycamore(s):
    t = Crown(s); h = t.r.randint(17, 21)
    t.crown(h, int(h * .55), .45, round_env, 7, 9, .7, w=2, limb="log2", density=.65); return t
def eastern_redbud(s):
    t = Crown(s); h = t.r.randint(8, 9)
    t.crown(h, 3, .55, vase, 4, 6, 1.1, size=.75, layers=(2, 3, 2), top=False); return t
def flowering_dogwood(s):
    t = Crown(s); h = t.r.randint(8, 9)
    t.crown(h, 4, .55, egg(.45), 4.5, 6, .55, size=.8, layers=(2, 3, 2), density=.8); return t
def black_cherry(s):
    t = Crown(s); h = t.r.randint(14, 18)
    t.crown(h, h - 3, .6, round_env, 3.6, 8, .8, size=.9, density=.8); return t
def sassafras(s):
    t = Crown(s); h = t.r.randint(9, 12)
    t.crown(h, h - 2, .45, egg(.4), 3.6, 8, .4, size=.8, layers=(2, 3, 2), density=.8); return t
def ohio_buckeye(s):
    t = Crown(s); h = t.r.randint(9, 11)
    t.crown(h, int(h * .6), .42, round_env, 4.5, 9, .6, density=.9); return t

NEW = dict(white_oak=white_oak, red_oak=red_oak, sugar_maple=sugar_maple, red_maple=red_maple, tulip_poplar=tulip_poplar,
           shagbark_hickory=shagbark_hickory, black_walnut=black_walnut, american_beech=american_beech, sycamore=sycamore,
           eastern_redbud=eastern_redbud, flowering_dogwood=flowering_dogwood, eastern_red_cedar=T.eastern_red_cedar,
           black_cherry=black_cherry, sassafras=sassafras, ohio_buckeye=ohio_buckeye)

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "trees2.png"
    tiles = []
    for k, (name, fn, bark, bark2, leaf, bloom, note) in enumerate(T.SPECIES):
        f = NEW[name.lower().replace(" ", "_")]
        ims = [T.render(f(300 + k * 7 + j), bark, bark2, leaf if not bloom else bloom, None, 9 + j) for j in range(2)]
        w = sum(i.width for i in ims) + 10; hh = max(i.height for i in ims)
        tile = Image.new("RGBA", (w, hh + 24), (243, 239, 230, 255)); x = 0
        for i in ims: tile.paste(i, (x, hh - i.height), i); x += i.width + 10
        ImageDraw.Draw(tile).text((4, hh + 4), name, fill=(40, 40, 40))
        tiles.append(tile)
    cols = 5; cw = max(t.width for t in tiles); rh = max(t.height for t in tiles)
    sheet = Image.new("RGB", (cols * cw, 3 * rh), (243, 239, 230))
    for i, t in enumerate(tiles): sheet.paste(t, ((i % cols) * cw, (i // cols) * rh + rh - t.height), t)
    sheet.save(out); print(out, sheet.size)
