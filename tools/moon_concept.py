"""
Concept art for the Hollow Moon: its creatures, blocks and items (box models with new moon textures in the vanilla
style), and a scene of the moon's surface with the Earth in the sky.
  python3 tools/moon_concept.py <vanilla textures dir>  ->  docs/moon_concept.png
"""
import os, sys, math, random
from PIL import Image, ImageDraw, ImageFilter
sys.path.insert(0, os.path.dirname(__file__))
import render_iso as R
from mudlings_concept import Model, tex
ORIG_LOAD = R.load_model          # Model.draw swaps the loader; the player model needs the real one

def noise(base, amp=14, seed=1, specks=(), size=16):
    r = random.Random(seed); im = Image.new("RGBA", (size, size)); px = im.load()
    for y in range(size):
        for x in range(size):
            d = r.randint(-amp, amp); px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    for col, n in specks:
        for _ in range(n): px[r.randrange(size), r.randrange(size)] = col + (255,)
    return im

def bricks(base, mortar, seed=2):
    im = noise(base, 10, seed); px = im.load()
    for y in range(16):
        for x in range(16):
            if y % 8 == 7 or (x + (4 if (y // 8) % 2 else 0)) % 8 == 7: px[x, y] = mortar + (255,)
    return im

DUST = noise((200, 198, 190), 12, 1, [((170, 168, 160), 20)])
MOONSTONE = bricks((214, 214, 222), (150, 150, 162), 3)
def jade_from_lapis(name):
    """Vanilla's lapis texture, its blues turned to jade greens (so jade reads as green lapis)."""
    im = tex(name).copy(); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if b > r + 18 and b > g:                       # a lapis-blue pixel
                px[x, y] = (int(r * 0.7 + b * 0.12), min(255, int(b * 0.92 + 10)), int(g * 0.75 + b * 0.18), a)
    return im
JADE = jade_from_lapis("lapis_block")
JADE_ORE = jade_from_lapis("lapis_ore")
METEOR = noise((48, 46, 58), 10, 6, [((230, 230, 255), 7), ((120, 150, 255), 6)])
METEOR_ORE = noise((70, 66, 64), 12, 7, [((255, 150, 60), 10), ((255, 210, 120), 6)])
SILVER = noise((214, 214, 226), 10, 8, [((190, 190, 206), 18)])
PINK = noise((236, 170, 190), 8, 9)
SHELL = noise((150, 146, 140), 14, 10, [((200, 198, 190), 20)])
JELLY = noise((190, 220, 255), 10, 11, [((240, 250, 255), 20)])
MOTH = noise((92, 84, 120), 10, 12, [((255, 230, 120), 10)])
GLOW = noise((150, 255, 200), 8, 13)
STONE_M = noise((120, 118, 112), 12, 14)
PORTAL = noise((20, 16, 40), 10, 15, [((140, 100, 220), 14), ((230, 220, 255), 4)])
CAKE = noise((236, 214, 160), 10, 16, [((200, 160, 90), 14)])
GOLD = noise((240, 200, 70), 10, 17)
WOOD = tex("stripped_birch_log")

EYE = (20, 18, 24, 255)
def face(eyes, mouth=(), col=EYE): p = {e: col for e in eyes}; p.update({m: (90, 60, 70, 255) for m in mouth}); return p

def rabbit(m, big=False):
    m.box(SILVER, (-3, 15, -3), (6, 6, 9))                                                 # body
    m.box(SILVER, (-2.5, 10, -6), (5, 5, 5), paint=face(((0, 2), (4, 2)), ((2, 4),)))       # head
    for x in (-2, 0.8):
        m.box(SILVER, (x, 2, -4), (1.2, 8, 2)); m.box(PINK, (x, 3, -4.1), (1.2, 6, 0.2))    # tall ears, pink inside
    for x in (-3, 2): m.box(SILVER, (x, 21, -2), (1, 3, 2)); m.box(SILVER, (x, 20, 3), (1, 4, 3))
    m.box(Image.new("RGBA", (16, 16), (245, 245, 250, 255)), (-1, 15, 6), (2, 2, 2))        # tail
    if big:                                                                                # the boss: its mortar and pestle
        for (w, d) in ((10, 7), (7, 10)): m.box(STONE_M, (-10 - w / 2, 17, 2 - d / 2), (w, 7, d))     # the mortar, beside it
        m.box(DUST, (-13, 16.5, 0), (6, 1, 4))
        p = m.part([-6, 12, 0], (0, 0, 0.7)); m.box(WOOD, (-1, -10, -1), (2, 16, 2), part=p); m.box(WOOD, (-2, 4, -2), (4, 4, 4), part=p)
        m.box(GOLD, (-2.5, 9, -6.2), (5, 1, 5.4)); [m.box(GOLD, (x, 7.5, -6), (1, 1.5, 1)) for x in (-2.5, -0.5, 1.5)]   # a crown
WHITE_FUR = noise((244, 244, 248), 6, 20, [((226, 226, 236), 14)])
DARK_FUR = noise((150, 150, 166), 10, 21, [((120, 120, 138), 14)])
MOON_GLOW = noise((160, 230, 255), 6, 22, [((220, 250, 255), 10)])
EYE_GLOW = Image.new("RGBA", (16, 16), (120, 230, 255, 255))

def great_rabbit_detailed():
    """The boss, built like the ender dragon is: far more parts than an ordinary mob. It sits upright, as the moon
    rabbit of legend does, its pestle raised over a great jade-rimmed mortar of glowing moon dust."""
    m = Model(30)
    # hind quarters and feet
    for x in (-13, 5):
        m.box(SILVER, (x, 30, -2), (8, 12, 14)); m.box(MOON_GLOW, (x + (8 if x > 0 else -0.2), 33, 2), (0.2, 4, 5))   # glowing crescent mark
        m.box(SILVER, (x, 42, -10), (8, 4, 16)); m.box(WHITE_FUR, (x + 1, 45.5, -11), (6, 0.5, 4))                   # long hind feet
    m.box(WHITE_FUR, (-4, 30, 12), (8, 8, 7))                                                                       # fluffy tail
    # upright torso, white chest
    m.box(SILVER, (-9, 12, -4), (18, 22, 14)); m.box(DARK_FUR, (-6, 12, 9.5), (12, 18, 1))                           # back stripe
    m.box(WHITE_FUR, (-7, 15, -5.5), (14, 17, 2))
    s1 = m.part([0, 16, -6], (0, 0, 0.75)); m.box(JADE, (-15, -2, -0.5), (30, 3, 1), part=s1)                       # jade sash
    # head
    m.box(SILVER, (-7, -2, -8), (14, 13, 12))
    m.box(WHITE_FUR, (-4, 5, -11), (8, 6, 3)); m.box(PINK, (-1.5, 5, -11.6), (3, 2, 1))                             # muzzle and nose
    for x in (-9, 7): m.box(WHITE_FUR, (x, 4, -6), (2, 6, 5))                                                      # cheek tufts
    for x in (-6, 2.5):
        m.box(EYE_GLOW, (x, 1, -8.3), (3.5, 3, 0.4)); m.box(Image.new("RGBA", (16, 16), (255, 255, 255, 255)), (x + 0.5, 1.3, -8.5), (1, 1, 0.2))
    for y in (6, 8):                                                                                                 # whiskers
        for x in (-10, 5): m.box(WHITE_FUR, (x, y, -10.5), (5, 0.4, 0.4))
    # long ears, tilting outward, pink inside, dark tips
    for sgn in (-1, 1):
        e = m.part([sgn * 4, -2, -1], (-0.12, 0, sgn * 0.22))
        m.box(SILVER, (-2, -22, -1.5), (4, 22, 3), part=e); m.box(PINK, (-1.2, -20, -1.8), (2.4, 17, 0.4), part=e)
        m.box(DARK_FUR, (-2, -24, -1.5), (4, 3, 3), part=e)
    # crown
    m.box(GOLD, (-6, -4, -7), (12, 2, 10))
    for x in (-6, -2, 2): m.box(GOLD, (x, -7, -7), (2, 3, 1)); m.box(JADE, (x + 0.5, -6, -7.4), (1, 1, 0.4))
    # forearms gripping the mortar staff, its weapon
    for sgn in (-1, 1):
        a = m.part([sgn * 7, 16, -3], (-1.15, sgn * 0.45, 0))
        m.box(SILVER, (-2, -1, -2), (4, 13, 4), part=a); m.box(WHITE_FUR, (-2.5, 11, -2.5), (5, 4, 5), part=a)
    st = m.part([0, 20, -15], (0.35, 0, -0.55))                                                       # the giant mortar staff, raised
    m.box(WOOD, (-2, -30, -2), (4, 50, 4), part=st); m.box(METEOR, (-2.1, 20, -2.1), (4.2, 2.5, 4.2), part=st)   # haft, a simple meteor-iron cap
    for (w, d) in ((8, 6), (6, 8)): m.box(STONE_M, (-w / 2, -37, -d / 2), (w, 7, d), part=st)              # a small mortar head
    for (x, z, w, d) in ((-4, -4, 8, 1), (-4, 3, 8, 1), (-4, -4, 1, 8), (3, -4, 1, 8)): m.box(JADE, (x, -38, z), (w, 1, d), part=st)
    m.box(MOON_GLOW, (-3, -37.6, -3), (6, 0.5, 6), part=st)
    m.box(JADE, (-4.1, -33, -3.1), (8.2, 1, 6.2), part=st)                                            # a jade band
    return m

def moon_rabbit(): m = Model(1); rabbit(m); return m
def great_rabbit(): m = Model(2); rabbit(m, True); return m
def drifter():
    m = Model(3); m.box(JELLY, (-5, 4, -5), (10, 6, 10)); m.box(JELLY, (-4, 2, -4), (8, 2, 8)); m.box(GLOW, (-2, 6, -2), (4, 3, 4))
    for i, (x, z) in enumerate(((-3, -3), (2, -3), (-3, 2), (2, 2), (-0.5, 0))): m.box(JELLY, (x, 10, z), (1, 7 + (i % 3) * 2, 1))
    return m
def crab():
    m = Model(4); m.box(SHELL, (-5, 18, -4), (10, 4, 8), paint=face(((3, 1), (6, 1))))
    m.box(DUST, (-4, 17, -3), (8, 1, 6))
    for s in (-1, 1):
        m.box(SHELL, (s * 6 - 1.5, 18, -6), (3, 2, 3)); m.box(SHELL, (s * 7 - 1.5, 17, -8), (3, 3, 2))   # claws
        for z in (-2, 1, 3): m.box(SHELL, (s * 5.5 - 0.5, 21, z), (1, 3, 1))
    return m
def moth():
    m = Model(5); m.box(MOTH, (-1, 12, -4), (2, 2, 8))
    for s in (-1, 1):
        p = m.part([0, 12, 0], (0, 0, s * 0.3)); m.box(MOTH, (0 if s > 0 else -9, -0.5, -4), (9, 1, 9), part=p)
    m.box(GLOW, (-1, 12.5, 3), (2, 1, 1)); return m
SUIT = noise((232, 232, 236), 8, 18, [((210, 210, 218), 16)])
ZOMBIE = noise((96, 150, 80), 10, 19, [((70, 120, 60), 14)])
VISOR = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for _i in range(16):                                    # clear glass: just a rim and a diagonal glint
    VISOR.putpixel((_i, 0), (180, 210, 235, 255))
for _i in range(1, 4): VISOR.putpixel((_i + 1, _i), (235, 245, 255, 255)); VISOR.putpixel((_i + 2, _i), (235, 245, 255, 255))
def walker():
    """A lost spaceman: a zombie in an old spacesuit, on Steve's proportions (head 8, body 8x12x4, limbs 4x12x4)."""
    m = Model(6)
    for x in (-4, 0): m.box(SUIT, (x, 12, -2), (4, 12, 4))                                 # legs
    m.box(SUIT, (-4, 0, -2), (8, 12, 4)); m.box(Image.new("RGBA", (16, 16), (200, 60, 50, 255)), (-1, 2, -2.2), (2, 2, 0.4))   # suit and its patch
    for x in (-6, 6):                                                                       # arms reaching out, zombie-style
        a = m.part([x, 2, 0], (-1.45, 0, 0))
        m.box(SUIT, (-2, -2, -2), (4, 10, 4), part=a); m.box(ZOMBIE, (-2, 8, -2), (4, 2, 4), part=a)
    m.box(Image.new("RGBA", (16, 16), (160, 160, 168, 255)), (-3, 1, 2), (6, 9, 3))         # air pack
    m.box(ZOMBIE, (-4, -8, -4), (8, 8, 8), paint=face(((1, 3), (2, 3), (5, 3), (6, 3)), ((2, 6), (3, 6), (4, 6), (5, 6)), col=(20, 30, 20, 255)))
    m.box(SUIT, (-5, -9, -5), (10, 1, 10)); m.box(SUIT, (-5, -9, 3), (10, 10, 2))            # helmet shell
    m.box(SUIT, (-5, -9, -5), (1, 10, 10)); m.box(SUIT, (4, -9, -5), (1, 10, 10))
    m.box(VISOR, (-4, -8, -5), (8, 8, 0.5))                                                 # glass visor
    m.box(SUIT, (-5, 0, -5), (10, 1, 10))                                                   # neck ring
    return m

def cube(t): m = Model(7); m.box(t, (-8, 8, -8), (16, 16, 16)); return m
def gravity_well():
    m = Model(8)
    for (x, z) in ((-8, -8), (6, -8), (-8, 6), (6, 6)): m.box(METEOR, (x, 8, z), (2, 16, 2))
    m.box(MOONSTONE, (-8, 8, -8), (16, 3, 16)); m.box(MOONSTONE, (-8, 21, -8), (16, 3, 16)); m.box(GLOW, (-4, 12, -4), (8, 8, 8))
    return m
def portal():
    """Nether-portal sized: a 4x5 frame of jade blocks around a 2x3 opening."""
    m = Model(9)
    for x in range(4): m.box(JADE, (x * 16 - 32, -56, 0), (16, 16, 16)); m.box(JADE, (x * 16 - 32, 8, 0), (16, 16, 16))
    for y in range(3): m.box(JADE, (-32, y * 16 - 40, 0), (16, 16, 16)); m.box(JADE, (16, y * 16 - 40, 0), (16, 16, 16))
    m.box(PORTAL, (-16, -40, 6), (32, 48, 4)); return m

def mortar():
    m = Model(10); p = m.part([0, 12, 0], (0, 0, 0.5))
    m.box(WOOD, (-1, -8, -1), (2, 20, 2), part=p)
    for (w, d) in ((8, 6), (6, 8)): m.box(STONE_M, (-w / 2, -16, -d / 2), (w, 8, d), part=p)
    m.box(JADE, (-3, -17, -3), (6, 1, 6), part=p); m.box(METEOR, (-1.5, 11, -1.5), (3, 2, 3), part=p)
    return m
def crown():
    m = Model(11); m.box(GOLD, (-4, 18, -4), (8, 2, 8))
    for (x, z) in ((-4, -4), (2, -4), (-4, 2), (2, 2), (-1, -4), (-1, 2)): m.box(GOLD, (x, 15, z), (2, 3, 2))
    m.box(JADE, (-1, 16, -4.5), (2, 2, 1)); return m
def cake(): m = Model(12); m.box(CAKE, (-5, 18, -5), (10, 6, 10)); m.box(JADE, (-1, 17, -1), (2, 1, 2)); return m
def ingot(): m = Model(13); m.box(METEOR, (-5, 20, -2.5), (10, 3, 5)); m.box(METEOR, (-4, 19, -1.5), (8, 1, 3)); return m

CREATURES = [("Moon Rabbit", "tame it, or bundle it home for a cage", moon_rabbit, 2.4),
             ("Dust Drifter", "floats over the craters, glowing softly", drifter, 2.4),
             ("Crater Crab", "hides in moon dust and pinches", crab, 2.4),
             ("Lantern Moth", "a night butterfly with glowing wing spots", moth, 2.4),
             ("Lost Spaceman", "a dead Steve in an old spacesuit; walks the craters at night", walker, 2.0)]
THINGS = [("Jade Ore", "rare, deep in mountains and lush caves", lambda: cube(JADE_ORE), 1.2),
          ("Block of Jade", "green lapis; the portal frame material", lambda: cube(JADE), 1.2),
          ("Jade Portal", "nether-portal sized; lit with an eye of ender", portal, 0.42),
          ("Moonstone Bricks", "light building stone", lambda: cube(MOONSTONE), 1.2),
          ("Moon Dust", "light; for gliding gear", lambda: cube(DUST), 1.2),
          ("Meteor Iron Ore", "dug from fresh craters", lambda: cube(METEOR_ORE), 1.2),
          ("Meteor Iron", "stronger than steel, lighter than iron", ingot, 2.4),
          ("Gravity Well", "low gravity around it, back on Earth", gravity_well, 1.2),
          ("Moon Mortar", "the boss's weapon: pound, grind, slam", mortar, 2.2),
          ("Jade Crown", "the Great Moon Rabbit's crown", crown, 2.4),
          ("Moon Cake", "food, and the rabbits' favourite", cake, 2.4)]

def earth_scene(w=360, h=150, seed=4):
    """Pixel-art moonscape: black sky, stars, the blocky Earth, crater-pocked ground. Drawn small, scaled up crisp."""
    r = random.Random(seed); im = Image.new("RGB", (w, h), (6, 6, 14)); px = im.load()
    for _ in range(260): px[r.randrange(w), r.randrange(h - 50)] = (230, 230, 255) if r.random() < .7 else (160, 170, 255)
    cx, cy, rad = 290, 40, 26                                                                 # the Earth, lit from the right
    for y in range(cy - rad, cy + rad + 1):
        for x in range(cx - rad, cx + rad + 1):
            dx, dy = x - cx, y - cy
            if dx * dx + dy * dy > rad * rad: continue
            bx, by = (x // 3), (y // 3)                                                       # chunky "blocks"
            v = (math.sin(bx * 1.3 + by * 0.4) + math.cos(by * 1.1 - bx * 0.6) + math.sin((bx + by) * 0.7)) / 3
            col = (40, 90, 200) if v < 0.15 else ((80, 160, 70) if v < 0.5 else (200, 180, 120))
            if (math.sin(by * 2.1 + bx * 0.3) > 0.85): col = (240, 240, 245)                 # cloud bands
            shade = 0.35 + 0.65 * max(0, min(1, (dx / rad + 0.4)))
            px[x, y] = tuple(int(c * shade) for c in col)
    d = ImageDraw.Draw(im)
    ground = [(x, int(h - 45 + 6 * math.sin(x * 0.03) + 3 * math.sin(x * 0.11))) for x in range(w)]
    for x, gy in ground:
        for y in range(gy, h): px[x, y] = tuple(max(0, min(255, c + r.randint(-8, 8))) for c in ((176, 174, 168) if y < gy + 3 else (150, 148, 142)))
    for (cx2, cy2, rw) in ((60, 128, 22), (170, 138, 30), (250, 124, 14), (320, 140, 18)):   # craters
        d.ellipse((cx2 - rw, cy2 - rw // 3, cx2 + rw, cy2 + rw // 3), fill=(118, 116, 112), outline=(196, 194, 188))
    # a jade temple ruin on the skyline
    tx, ty = 110, ground[110][1]
    for i, (ww, hh) in enumerate(((60, 8), (46, 8), (32, 8), (18, 10))):
        d.rectangle((tx - ww // 2, ty - (i + 1) * 8 - (2 if i == 3 else 0), tx + ww // 2, ty - i * 8), fill=(52, 150, 104), outline=(30, 100, 66))
    d.rectangle((tx - 4, ty - 8, tx + 4, ty), fill=(20, 16, 40))
    return im.resize((w * 4, h * 4), Image.NEAREST)

def lantern_moth_texture():
    """A butterfly variant: the luna moth's wings (butterfly_18) recoloured dusky violet, with glowing moon-gold spots."""
    src = os.path.join(R.ROOT, "textures", "entity", "butterfly_18.png")
    im = Image.open(src).convert("RGBA"); px = im.load(); rnd = random.Random(31)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a: continue
            l = (r + g + b) / 3
            px[x, y] = (int(40 + l * 0.35), int(32 + l * 0.30), int(70 + l * 0.45), a)              # dusky violet
            if l > 150 and rnd.random() < 0.35: px[x, y] = (255, 226, 120, a)                       # glowing spots on the pale parts
    out = os.path.join(os.environ.get("TMPDIR", "/tmp"), "lantern_moth.png"); im.save(out)
    return out

def sheet(out):
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    big = great_rabbit_detailed().draw(2.1)
    crs = [(n, s, f().draw(sc)) for n, s, f, sc in CREATURES]
    skin = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "lost_spaceman_skin.png")
    if os.path.exists(skin):                       # the spaceman uses its real player-layout skin (tools/spaceman_skin.py)
        R.load_model = ORIG_LOAD
        crs = [(n, s, R.render("player", skin[:-4], 2.2) if n == "Lost Spaceman" else im) for n, s, im in crs]
    R.load_model = ORIG_LOAD
    crs = [(n, s, R.render("butterfly", lantern_moth_texture()[:-4], 3.2) if n == "Lantern Moth" else im) for n, s, im in crs]
    ths = [(n, s, f().draw(sc)) for n, s, f, sc in THINGS]
    port = portal().draw(0.62)
    scene = earth_scene()
    W = scene.width + 80
    cw1 = (W - 60) // 3; ch1 = max(i.height for _, _, i in crs) + 90
    cw2 = (W - 60) // 5; ch2 = max(i.height for _, _, i in ths) + 90
    H = 160 + scene.height + 60 + big.height + 120 + 2 * ch1 + 60 + 3 * ch2 + 40
    c = Image.new("RGB", (W, H), BG); d = ImageDraw.Draw(c)
    d.text((40, 36), "The Hollow Moon: concept art", font=R.font(46, True), fill=INK)
    d.text((42, 96), "The actual moon, reached through a portal of jade blocks lit with an eye of ender under a full moon. Low gravity, the Earth in the sky.", font=R.font(20), fill=SUB)
    y = 140; c.paste(scene, (40, y)); y += scene.height + 40
    # boss with the portal beside it
    c.paste(big, ((W - big.width) // 2, y), big)
    d.text((W // 2, y + big.height + 24), "The Great Moon Rabbit (boss)", font=R.font(28, True), fill=INK, anchor="mm")
    d.text((W // 2, y + big.height + 56), "swings a giant mortar staff in the jade temple; slams, kits, flung meteors, a frenzy at half health", font=R.font(18), fill=SUB, anchor="mm")
    y += big.height + 100
    for k, (n, s, im) in enumerate(crs):
        x = 30 + (k % 3) * cw1; base = y + (k // 3) * ch1 + ch1 - 70
        c.paste(im, (x + (cw1 - im.width) // 2, base - im.height), im)
        d.text((x + cw1 // 2, base + 22), n, font=R.font(22, True), fill=INK, anchor="mm"); d.text((x + cw1 // 2, base + 48), s, font=R.font(15), fill=SUB, anchor="mm")
    y += 2 * ch1 + 30
    for k, (n, s, im) in enumerate(ths):
        x = 30 + (k % 5) * cw2; base = y + (k // 5) * ch2 + ch2 - 70
        c.paste(im, (x + (cw2 - im.width) // 2, base - im.height), im)
        d.text((x + cw2 // 2, base + 22), n, font=R.font(22, True), fill=INK, anchor="mm"); d.text((x + cw2 // 2, base + 48), s, font=R.font(15), fill=SUB, anchor="mm")
    c = c.resize((c.width * 3 // 4, c.height * 3 // 4), Image.LANCZOS)
    c.save(out); print("wrote", out, c.size)

if __name__ == "__main__":
    sheet(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "moon_concept.png"))
