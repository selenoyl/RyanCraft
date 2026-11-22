"""
Mudling concept art: little biome soldiers made of their biome's own blocks (textures from the classic pack, falling
back to vanilla). Built as box models and drawn with the field-guide renderer, so they sit in the same style.
  python3 tools/mudlings_concept.py   -> docs/mudlings_concept.png
"""
import os, sys, random, math
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, "..", "src", "main", "resources")
CLASSIC = os.path.join(RES, "resourcepacks", "classic", "assets", "minecraft", "textures")
VANILLA = sys.argv[1] if len(sys.argv) > 1 else "/tmp/vanilla_textures"     # unpacked client jar textures (fallback)
TMP = os.path.join(os.environ.get("TMPDIR", "/tmp"), "mudling_atlas")
os.makedirs(TMP, exist_ok=True)

def tex(name, tint=None):
    for root in (CLASSIC, VANILLA):
        for kind in ("block", "item"):
            p = os.path.join(root, kind, name + ".png")
            if os.path.exists(p):
                im = Image.open(p).convert("RGBA").crop((0, 0, 16, 16))
                if tint:
                    a = np.array(im).astype(float); a[..., :3] *= np.array(tint) / 255.0; im = Image.fromarray(a.clip(0, 255).astype(np.uint8))
                return im
    raise FileNotFoundError(name)

GREEN = (110, 170, 70); VINE = (90, 150, 60)

class Model:
    """A box model on its own texture atlas: every box gets a region tiled with one block texture."""
    def __init__(self, seed=1):
        self.atlas = Image.new("RGBA", (256, 256), (0, 0, 0, 0)); self.x = self.y = self.rowh = 0
        self.parts = []; self.r = random.Random(seed)
    def box(self, texture, frm, size, part=None, paint=None):
        w, h, d = [int(round(v)) for v in size]
        rw, rh = 2 * d + 2 * w, d + h
        if self.x + rw > 256: self.x = 0; self.y += self.rowh; self.rowh = 0
        u, v = self.x, self.y; self.x += rw; self.rowh = max(self.rowh, rh)
        ox, oy = self.r.randrange(16), self.r.randrange(16)
        for yy in range(rh):
            for xx in range(rw):
                self.atlas.putpixel((u + xx, v + yy), texture.getpixel(((xx + ox) % 16, (yy + oy) % 16)))
        if paint:                                  # paint(front_region_origin) for faces, eyes...
            for (px, py), c in paint.items(): self.atlas.putpixel((u + d + px, v + d + py), c)
        b = {"uv": [u, v], "from": list(frm), "size": [w, h, d], "mirror": False}
        (part or self.root()).setdefault("boxes", []).append(b)
        return b
    def root(self):
        if not self.parts: self.parts.append({"pivot": [0, 0, 0], "rot": (0, 0, 0), "boxes": []})
        return self.parts[0]
    def part(self, pivot, rot=(0, 0, 0)):
        p = {"pivot": list(pivot), "rot": rot, "boxes": []}; self.parts.append(p); return p
    def draw(self, scale=2.2):
        path = os.path.join(TMP, "atlas_%d" % id(self))
        self.atlas.save(path + ".png")
        R.load_model = lambda name, m=self: ((256, 256), m.parts)
        return R.render("mud", path, scale)

EYE, MOUTH = (20, 18, 16, 255), (40, 26, 20, 255)
def face(w=6, h=5, eyes=((1, 2), (4, 2)), mouth=((2, 4), (3, 4)), glow=None):
    p = {e: (glow or EYE) for e in eyes}
    for m in mouth: p[m] = MOUTH
    return p

def body(m, t_body, t_head=None, t_legs=None, t_arms=None, wide=0, tall=0, head_face=None):
    """The common mudling: stubby legs, a barrel body, a big blocky head (y down; feet at 24)."""
    t_head = t_head or t_body; t_legs = t_legs or t_body; t_arms = t_arms or t_body
    bw = 5 + wide
    m.box(t_legs, (-bw / 2 + 0.5, 21, -1), (2, 3, 2)); m.box(t_legs, (bw / 2 - 2.5, 21, -1), (2, 3, 2))
    m.box(t_body, (-bw / 2, 16 - tall, -1.5), (bw, 5 + tall, 3))
    m.box(t_arms, (-bw / 2 - 1.5, 16 - tall, -1), (1.5, 5, 2)); m.box(t_arms, (bw / 2, 16 - tall, -1), (1.5, 5, 2))
    m.box(t_head, (-3, 10 - tall, -3), (6, 6, 6), paint=head_face if head_face is not None else face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5))))
    return 10 - tall                                    # top of head

def sod():
    m = Model(1); top = body(m, tex("dirt"))
    m.box(tex("grass_block_top", GREEN), (-3.5, top - 1, -3.5), (7, 1, 7))                 # turf cap
    m.box(tex("poppy"), (0, top - 4, 0), (1, 3, 1))                                       # a flower in the cap
    m.box(tex("oak_planks"), (-4.5, 13, -3), (1, 8, 1))                                    # a little wooden sword
    m.box(tex("oak_planks"), (-5.5, 18, -3), (3, 1, 1))
    return m
def twig():
    m = Model(2); top = body(m, tex("oak_log"), t_head=tex("oak_planks"))
    m.box(tex("oak_leaves", GREEN), (-3.5, top - 1.5, -3.5), (7, 3, 7))                    # leafy hood
    m.box(tex("oak_log"), (-4.6, 4, -2.5), (1, 19, 1))                                      # a long twig spear
    m.box(tex("flint"), (-4.6, 2, -2.5), (1, 2, 1))
    return m
def bark():
    m = Model(3); top = body(m, tex("spruce_log"), wide=1, head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5)), glow=(236, 226, 190, 255)))
    m.box(tex("sweet_berry_bush_stage3"), (-2, top - 2, -2), (4, 2, 4))
    m.box(tex("spruce_planks"), (-5, 13, -4.5), (1, 7, 6))                                  # round-ish shield
    return m
def mud():
    m = Model(4); top = body(m, tex("mud"), wide=2, head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5)), glow=(236, 226, 190, 255)))
    m.box(tex("lily_pad", GREEN), (-4.5, top - 1, -4.5), (9, 1, 9))                        # lily pad hat
    return m
def sandstone():
    m = Model(5); top = body(m, tex("sandstone"), t_head=tex("sandstone_top"))
    m.box(tex("red_wool"), (-0.5, top - 3, -2), (1, 3, 5))                                  # helmet crest
    m.box(tex("cut_sandstone"), (-5.5, 11, -4.5), (1, 10, 7))                               # big shield
    m.box(tex("red_sandstone"), (-6, 15, -1.5), (1, 2, 1))
    m.box(tex("stick"), (4.5, 6, -1.5), (1, 15, 1))                                         # spear
    return m
def terracotta():
    m = Model(6); top = body(m, tex("orange_terracotta"), t_head=tex("terracotta"), wide=2, tall=1)
    m.box(tex("stick"), (4, 12, -3), (1, 8, 1)); m.box(tex("red_terracotta"), (3.5, 8, -3.5), (2, 4, 2))   # club
    return m
def snow():
    m = Model(7); top = body(m, tex("snow"), t_head=tex("snow"), head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5))))
    m.box(tex("red_wool"), (-3, top + 5, -3.2), (6, 1, 6))                                  # scarf
    m.box(tex("snow"), (4, 18, -3), (2, 2, 2))                                              # snowball
    m.box(tex("ice"), (-1, top - 2, -1), (2, 2, 2))
    return m
def vine():
    m = Model(8); top = body(m, tex("jungle_log"), head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5)), glow=(236, 226, 190, 255)))
    m.box(tex("vine", VINE), (-3.2, top, -3.3), (6, 8, 1))                                  # vines draped down the front
    m.box(tex("bamboo_block"), (-4.6, 6, -2.5), (1, 17, 1))
    m.box(tex("cocoa_stage2"), (2, top - 2, -1), (2, 2, 2))
    return m
def gravel():
    m = Model(9); top = body(m, tex("gravel"), t_head=tex("stone"))
    m.box(tex("flint"), (4.2, 17, -3), (2, 2, 2))                                           # flint grenade
    m.box(tex("cobblestone"), (-3.5, top - 1, -3.5), (7, 1, 7))
    return m
def shell():
    m = Model(10); top = body(m, tex("sand"), t_head=tex("sand"))
    m.box(tex("calcite"), (-3.5, top - 2, -3.5), (7, 3, 7))                                 # shell helmet
    m.box(tex("kelp_plant", VINE), (3, top + 2, 2), (1, 6, 1))
    m.box(tex("calcite"), (-5.5, 13, -4), (1, 8, 6))                                        # shell shield
    return m
def pebble():
    m = Model(11); top = body(m, tex("cobblestone"), t_head=tex("coal_ore"))
    m.box(tex("iron_block"), (-3.5, top - 1, -3.5), (7, 2, 7))                              # miner's cap
    m.box(tex("glowstone"), (-1, top, -4), (2, 2, 1))                                       # lamp
    m.box(tex("stick"), (4.5, 12, -3), (1, 9, 1)); m.box(tex("iron_block"), (3, 11, -3), (4, 1, 1))   # pick
    return m
def cinder():
    m = Model(12); top = body(m, tex("netherrack"), t_head=tex("magma"),
                              head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5)), glow=(255, 220, 90, 255)))
    m.box(tex("glowstone"), (-1, top - 3, -1), (2, 3, 2))                                   # a flame on its head
    m.box(tex("quartz_block_side"), (4, 15, -3), (1, 6, 1))
    return m
def cloud():
    m = Model(13); top = body(m, tex("white_wool"))
    wl = m.part([-3, 16, 1], (0, 0.5, 0.4)); wr = m.part([3, 16, 1], (0, -0.5, -0.4))
    m.box(tex("white_wool"), (-9, 0, 0), (9, 5, 1), part=wl); m.box(tex("white_wool"), (0, 0, 0), (9, 5, 1), part=wr)
    m.box(tex("light_blue_wool"), (-3.5, top - 1, -3.5), (7, 1, 7))
    return m
def variant(name, texture, seed):
    m = Model(seed); top = body(m, tex(texture), head_face=face(h=6, eyes=((1, 3), (4, 3)), mouth=((2, 5), (3, 5)), glow=(236, 226, 190, 255)) if texture == "coal_block" else None); m.box(tex(texture), (-3.5, top - 1, -3.5), (7, 1, 7)); return m

LINEUP = [("Sod Soldier", "Plains · all-rounder", sod), ("Twig Ranger", "Forest · quick, light hits", twig),
          ("Bark Warden", "Taiga · sturdy", bark), ("Mud Slogger", "Swamp · slows enemies", mud),
          ("Sandstone Legionary", "Desert · shield wall", sandstone), ("Terracotta Brute", "Badlands · heavy hitter", terracotta),
          ("Snowpack Scout", "Snowy · throws snowballs", snow), ("Vine Skirmisher", "Jungle · climbs, ambushes", vine),
          ("Gravel Grenadier", "Mountains · knocks foes back", gravel), ("Shell Guard", "Beach · defensive", shell),
          ("Pebble Miner", "Caves · digs in", pebble), ("Cinder Imp", "Nether · sets things alight", cinder),
          ("Cloud Kite", "Sky dimension · flies", cloud)]
RARES = [("Gilded", "gold_block", 21), ("Mossy", "moss_block", 22), ("Charred", "coal_block", 23)]

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    imgs = [(n, s, f().draw()) for n, s, f in LINEUP]
    rares = [(n, variant(n, t, sd).draw()) for n, t, sd in RARES]
    cols = 5; cw = max(i.width for _, _, i in imgs) + 70; ch = max(i.height for _, _, i in imgs) + 90
    rows = (len(imgs) + cols - 1) // cols
    rh = max(i.height for _, i in rares)
    W = cols * cw + 60; H = 170 + rows * ch + rh + 160
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "Mudlings: concept art", font=R.font(46, True), fill=INK)
    d.text((42, 96), "Little biome soldiers made of their biome's own blocks. Find their camps, scoop them up in a bundle, battle your friends.", font=R.font(20), fill=SUB)
    for k, (n, s, im) in enumerate(imgs):
        x = 30 + (k % cols) * cw; y = 160 + (k // cols) * ch; base = y + ch - 70
        sh = Image.new("L", (im.width + 20, 30), 0); ImageDraw.Draw(sh).ellipse((10, 6, sh.width - 10, 24), fill=80)
        c.paste((205, 198, 184), (x + (cw - sh.width) // 2, base - 16), sh.filter(ImageFilter.GaussianBlur(5)))
        c.paste(im, (x + (cw - im.width) // 2, base - im.height), im)
        d.text((x + cw // 2, base + 22), n, font=R.font(22, True), fill=INK, anchor="mm")
        d.text((x + cw // 2, base + 48), s, font=R.font(16), fill=SUB, anchor="mm")
    y = 160 + rows * ch + 20
    d.text((40, y), "Rare colour variants (about 1 in 20)", font=R.font(26, True), fill=INK)
    x = 60; base = y + 60 + rh
    for n, im in rares:
        c.paste(im, (x, base - im.height), im); d.text((x + im.width // 2, base + 24), n + " Sod Soldier", font=R.font(18, True), fill=INK, anchor="mm")
        x += im.width + 90
    # matchups strip
    d.text((x + 40, y + 60), "Type matchups (examples)", font=R.font(22, True), fill=INK)
    for k, line in enumerate(["Mud beats Sand", "Sand beats Snow", "Snow beats Twig", "Twig beats Mud", "Losers crumble into their biome's blocks;", "winners level up."]):
        d.text((x + 40, y + 100 + k * 30), line, font=R.font(18), fill=SUB if k > 3 else INK)
    c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(HERE, "..", "docs", "mudlings_concept.png"))
