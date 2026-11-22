"""Game fish: models and textures for the newer species, and item icons for every RyanCraft fish built on the base-game
templates (the classic raw/cooked fish and the Programmer Art fish bucket), recoloured per species."""
import colorsys, os, random
from PIL import Image
import critters
from critters import fish, pack, emit_model, emit_textures, save_item

HERE = os.path.dirname(os.path.abspath(__file__))
def hexc(h): h = h.lstrip("#"); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

MODELS = {
    "rainbow_trout": fish((2, 5, 11), (1, 4, 3), dorsal=(1, 2, 4)),
    "smallmouth_bass": fish((3, 5, 10), (1, 5, 3), dorsal=(1, 2, 6)),
    "yellow_perch": fish((2, 5, 8), (1, 4, 3), dorsal=(1, 2, 5)),
    "walleye": fish((3, 4, 12), (1, 4, 3), dorsal=(1, 2, 6)),
    "northern_pike": fish((2, 4, 16), (1, 4, 4), dorsal=(1, 2, 3)),
    "muskellunge": fish((3, 4, 18), (1, 5, 4), dorsal=(1, 2, 3)),
}
STYLES = {
    "rainbow_trout": {"body": {"c": "#6e7a4a", "noise": 8, "belly": "#e8d8cc", "band": "#d8607a", "speckle": {"c": "#1a1a14", "p": 0.18}, "side_eyes": "#0a0a0a"}, "fin": {"c": "#7a8058", "speckle": {"c": "#1a1a14", "p": 0.2}}},
    "smallmouth_bass": {"body": {"c": "#7a6638", "noise": 8, "belly": "#d8cca0", "bars": {"c": "#4a3a20", "n": 6}, "side_eyes": "#c82a20"}, "fin": {"c": "#6a5830"}},
    "yellow_perch": {"body": {"c": "#c8a830", "noise": 8, "belly": "#f0e8c0", "bars": {"c": "#3a4a1a", "n": 6}, "side_eyes": "#0a0a0a"}, "fin": {"c": "#d07a2a"}},
    "walleye": {"body": {"c": "#8a8040", "noise": 8, "belly": "#e8e4cc", "speckle": {"c": "#4a4420", "p": 0.15}, "side_eyes": "#e8e0b0"}, "fin": {"c": "#6a6436"}},
    "northern_pike": {"body": {"c": "#4e6a38", "noise": 8, "belly": "#e8e8c8", "speckle": {"c": "#c8d4a0", "p": 0.25}, "side_eyes": "#c8a020"}, "fin": {"c": "#5a6a3a"}},
    "muskellunge": {"body": {"c": "#8a8a68", "noise": 8, "belly": "#e8e8d8", "bars": {"c": "#4a4a34", "n": 8}, "side_eyes": "#c8a020"}, "fin": {"c": "#7a6a4a"}},
}
# icons for every species: back, belly, fin, pattern (none / speck / bars / band / spots), pattern colour
ICONS = {
    "bluegill": ("#5b7a48", "#e2a23a", "#4a6440", "bars", "#3e5a30"),
    "crappie": ("#bfc8b8", "#eef0ea", "#9aa294", "speck", "#2a3028"),
    "largemouth_bass": ("#5d7f3a", "#e6e6d6", "#4d6a30", "band", "#2c3d1c"),
    "channel_catfish": ("#6a7682", "#e8e8e2", "#56616c", "speck", "#2a3038"),
    "rainbow_trout": ("#6e7a4a", "#e8d8cc", "#7a8058", "band", "#d8607a"),
    "smallmouth_bass": ("#7a6638", "#d8cca0", "#6a5830", "bars", "#4a3a20"),
    "yellow_perch": ("#c8a830", "#f0e8c0", "#d07a2a", "bars", "#3a4a1a"),
    "walleye": ("#8a8040", "#e8e4cc", "#6a6436", "speck", "#4a4420"),
    "northern_pike": ("#4e6a38", "#e8e8c8", "#5a6a3a", "spots", "#c8d4a0"),
    "muskellunge": ("#8a8a68", "#e8e8d8", "#7a6a4a", "bars", "#4a4a34"),
}
EGGS = {"rainbow_trout": ("#6e7a4a", "#d8607a"), "smallmouth_bass": ("#7a6638", "#4a3a20"), "yellow_perch": ("#c8a830", "#3a4a1a"),
        "walleye": ("#8a8040", "#e8e0b0"), "northern_pike": ("#4e6a38", "#c8d4a0"), "muskellunge": ("#8a8a68", "#4a4a34")}

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def cooked_icon(back, seed):
    """The classic cooked fish, faintly tinted toward the species."""
    im = Image.open(os.path.join(HERE, "template_cooked_fish.png")).convert("RGBA"); px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            R, G, B, A = px[x, y]
            if A and max(R, G, B) > 45: px[x, y] = lerp((R, G, B), hexc(back), 0.12) + (A,)
    return im

def recolour(template, back, belly, fin, pattern, pc, seed, cooked=False, bucket=False):
    """Body pixels (anything coloured: not grey metal/fin, not water, not the eye) take the species colours by brightness:
    darker = back, lighter = belly, with the species' markings; grey fins take the fin colour."""
    im = Image.open(os.path.join(HERE, template)).convert("RGBA"); px = im.load(); r = random.Random(seed)
    body = []
    for y in range(im.height):
        for x in range(im.width):
            R, G, B, A = px[x, y]
            if not A: continue
            h, s, v = colorsys.rgb_to_hsv(R / 255, G / 255, B / 255)
            if v < 0.16: continue                                              # outline / eye
            if bucket and (0.5 < h < 0.72 and s > 0.35): continue               # water
            if s < 0.12:
                if bucket: continue                                            # bucket metal
                px[x, y] = lerp((0, 0, 0), hexc(fin), min(1.2, v / 0.7)) + (A,); continue   # fins
            body.append((x, y, v))
    if not body: return im
    vs = [b[2] for b in body]; lo, hi = min(vs), max(vs)
    # the icon fish lies diagonally (head bottom-left, tail top-right): its lateral line is where x + y is middling
    ds = sorted(b[0] + b[1] for b in body); mid = ds[len(ds) // 2]
    for (x, y, v) in body:
        t = (v - lo) / (hi - lo + 1e-6)
        c = lerp(hexc(back), hexc(belly), t ** 1.4)
        if cooked: c = lerp(hexc("#8a5428"), hexc("#d8a868"), t ** 1.2)
        elif pattern == "bars" and (x - y) % 4 == 0 and t < 0.75: c = lerp(c, hexc(pc), 0.7)
        elif pattern == "speck" and r.random() < 0.22: c = hexc(pc)
        elif pattern == "spots" and r.random() < 0.2 and t < 0.8: c = hexc(pc)
        elif pattern == "band" and abs(x + y - mid) < 1.0: c = lerp(c, hexc(pc), 0.75)
        px[x, y] = c + (255,)
    return im

if __name__ == "__main__":
    critters.rnd.seed(4242)
    for name, parts in MODELS.items():
        size = pack(parts); emit_model(name, parts, size); emit_textures(parts, size, {name: STYLES[name]}); print(name, size)
    for i, (name, (back, belly, fin, pattern, pc)) in enumerate(ICONS.items()):
        save_item(name, recolour("template_fish.png", back, belly, fin, pattern, pc, 100 + i))
        save_item("cooked_" + name, cooked_icon(back, 200 + i))
        save_item(name + "_bucket", recolour("template_fish_bucket.png", back, belly, fin, pattern, pc, 300 + i, bucket=True))
    print("icons ok")
