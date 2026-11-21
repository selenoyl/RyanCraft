"""
Reference page of the 15 wood families: 3D blocks (log, stripped log, wood, planks, leaves) and the flat textures
(sapling, door, trapdoor).  python3 tools/wood_sheet.py -> docs/wood_families.png
"""
import os
from PIL import Image, ImageDraw, ImageFont
from tree_textures import TREES

ROOT = os.path.join(os.path.dirname(__file__), "..")
T = os.path.join(ROOT, "src/main/resources/assets/ryancraft/textures")

def tex(name, kind="block"): return Image.open(f"{T}/{kind}/{name}.png").convert("RGBA")

def shade(img, k):
    r, g, b, a = img.split()
    return Image.merge("RGBA", [c.point(lambda v: int(v * k)) for c in (r, g, b)] + [a])

def cube(top, side, front=None, S=56):
    """Isometric cube from three 16x16 faces."""
    front = front or side
    W, H = 2 * S, int(2 * S)
    out = Image.new("RGBA", (W, H + S // 2), (0, 0, 0, 0))
    big = lambda im: im.resize((S, S), Image.NEAREST)
    h = S // 2
    # top: diamond
    t = big(top).transform((W, S), Image.AFFINE, (0.5, 1.0, -S / 2, -0.5, 1.0, S / 2), Image.NEAREST)
    t = big(top)
    top_d = Image.new("RGBA", (W, S), (0, 0, 0, 0))
    # map (u,v) -> diamond: x = (u - v) + S, y = (u + v)/2 ; inverse for transform
    top_d = t.transform((W, S), Image.AFFINE, (0.5, 1.0, -S / 2, -0.5, 1.0, S / 2), Image.NEAREST)
    left = shade(big(front), 0.82).transform((S, S + h), Image.AFFINE, (1, 0, 0, -0.5, 1, 0), Image.NEAREST)
    right = shade(big(side), 0.64).transform((S, S + h), Image.AFFINE, (1, 0, 0, 0.5, 1, -h), Image.NEAREST)
    out.alpha_composite(left, (0, h)); out.alpha_composite(right, (S, h)); out.alpha_composite(top_d, (0, 0))
    return out

def font(size, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, size)
    return ImageFont.load_default()

if __name__ == "__main__":
    BG, INK, SUB = (243, 239, 230), (46, 42, 36), (128, 118, 104)
    cols = ["Log", "Stripped log", "Wood", "Stripped wood", "Planks", "Leaves", "Sapling", "Door", "Trapdoor", "Bark", "Log top"]
    CW, RH, LW = 130, 150, 250
    sheet = Image.new("RGB", (LW + CW * len(cols) + 30, 150 + RH * len(TREES)), BG)
    d = ImageDraw.Draw(sheet)
    d.text((30, 28), "RyanCraft wood families", font=font(40, True), fill=INK)
    d.text((32, 80), "15 eastern US trees. Every species also has slabs, stairs, fences, gates, buttons, pressure plates and potted saplings.",
           font=font(18), fill=SUB)
    for i, c in enumerate(cols): d.text((LW + i * CW + CW // 2, 128), c, font=font(15, True), fill=SUB, anchor="mm")
    for k, sp in enumerate(TREES):
        y = 150 + k * RH
        if k % 2 == 0: d.rectangle((14, y, sheet.width - 14, y + RH - 6), fill=(236, 231, 220))
        d.text((30, y + RH // 2 - 3), sp.replace("_", " ").title(), font=font(19, True), fill=INK, anchor="lm")
        log, top, slog, stop = tex(f"{sp}_log"), tex(f"{sp}_log_top"), tex(f"stripped_{sp}_log"), tex(f"stripped_{sp}_log_top")
        planks, leaves = tex(f"{sp}_planks"), tex(f"{sp}_leaves")
        blocks = [cube(top, log), cube(stop, slog), cube(log, log), cube(slog, slog), cube(planks, planks), cube(leaves, leaves)]
        for i, b in enumerate(blocks):
            sheet.paste(b, (LW + i * CW + (CW - b.width) // 2, y + 12), b)
        flat = [tex(f"{sp}_sapling").resize((80, 80), Image.NEAREST)]
        door = Image.new("RGBA", (16, 32)); door.paste(tex(f"{sp}_door_top"), (0, 0)); door.paste(tex(f"{sp}_door_bottom"), (0, 16))
        flat.append(door.resize((56, 112), Image.NEAREST))
        flat += [tex(f"{sp}_trapdoor").resize((80, 80), Image.NEAREST), log.resize((80, 80), Image.NEAREST), top.resize((80, 80), Image.NEAREST)]
        for j, im in enumerate(flat):
            bg = Image.new("RGBA", im.size, (205, 198, 184, 255)) if j != 0 else Image.new("RGBA", im.size, (0, 0, 0, 0))
            bg.alpha_composite(im)
            x = LW + (6 + j) * CW + (CW - im.width) // 2
            sheet.paste(bg, (x, y + (RH - 6 - im.height) // 2), bg)
    out = os.path.join(ROOT, "docs", "wood_families.png"); sheet.save(out); print(out, sheet.size)
