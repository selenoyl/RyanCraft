"""The numbered moon block chart (docs/moon_blocks.png), for approving textures by number.
  python3 tools/moon_chart.py"""
import os
from PIL import Image, ImageDraw, ImageFont
HERE = os.path.dirname(os.path.abspath(__file__)); T = os.path.join(HERE, "..", "src", "main", "resources", "assets", "ryancraft", "textures")
ITEMS = [("Moon Dust", "block/moon_dust"), ("Moonstone", "block/moonstone"), ("Cobbled Moonstone", "block/cobbled_moonstone"),
         ("Polished Moonstone", "block/polished_moonstone"), ("Moonstone Bricks", "block/moonstone_bricks"),
         ("Cracked Moonstone Bricks", "block/cracked_moonstone_bricks"), ("Chiseled Moonstone Bricks", "block/chiseled_moonstone_bricks"),
         ("Meteor Iron Ore", "block/meteor_iron_ore"), ("Deep Moonstone (side)", "block/deep_moonstone"), ("Deep Moonstone (top)", "block/deep_moonstone_top"),
         ("Cobbled Deep Moonstone", "block/cobbled_deep_moonstone"), ("Polished Deep Moonstone", "block/polished_deep_moonstone"),
         ("Deep Moonstone Bricks", "block/deep_moonstone_bricks"), ("Deep Meteor Iron Ore", "block/deep_meteor_iron_ore"),
         ("Block of Raw Meteor Iron", "block/raw_meteor_iron_block"), ("Block of Meteor Iron", "block/meteor_iron_block"),
         ("Raw Meteor Iron", "item/raw_meteor_iron"), ("Meteor Iron Ingot", "item/meteor_iron_ingot"),
         ("Jade Ore", "block/jade_ore"), ("Deepslate Jade Ore", "block/deepslate_jade_ore"), ("Moonstone Jade Ore", "block/moonstone_jade_ore"),
         ("Deep Moonstone Jade Ore", "block/deep_moonstone_jade_ore"), ("Block of Jade", "block/jade_block"), ("Jade", "item/jade")]

def font(sz, bold=False):
    for f in (["/usr/share/fonts/opentype/inter/Inter-SemiBold.otf"] if bold else ["/usr/share/fonts/opentype/inter/Inter-Regular.otf"]) + ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(f): return ImageFont.truetype(f, sz)
    return ImageFont.load_default()

if __name__ == "__main__":
    CW, CH, TS, cols = 230, 250, 160, 6; rows = (len(ITEMS) + cols - 1) // cols
    img = Image.new("RGB", (cols * CW + 40, rows * CH + 90), (243, 239, 230)); d = ImageDraw.Draw(img)
    d.text((24, 22), "Moon blocks: reply with numbers to approve or redo", font=font(30, True), fill=(46, 42, 36))
    for k, (name, path) in enumerate(ITEMS):
        x = 20 + (k % cols) * CW; y = 80 + (k // cols) * CH
        t = Image.open(os.path.join(T, path + ".png")).convert("RGBA")
        if path.startswith("block"):
            tile = Image.new("RGBA", (32, 32))
            for a in (0, 16):
                for b in (0, 16): tile.paste(t, (a, b))
            big = tile.resize((TS, TS), Image.NEAREST)
        else:
            bg = Image.new("RGBA", (16, 16), (205, 198, 184, 255)); bg.alpha_composite(t); big = bg.resize((TS, TS), Image.NEAREST)
        img.paste(big.convert("RGB"), (x + (CW - TS) // 2 - 10, y))
        d.ellipse((x + 2, y - 6, x + 38, y + 30), fill=(46, 42, 36)); d.text((x + 20, y + 12), str(k + 1), font=font(18, True), fill=(255, 255, 255), anchor="mm")
        d.text((x + CW // 2 - 10, y + TS + 22), name, font=font(17, True), fill=(46, 42, 36), anchor="mm")
    img.save(os.path.join(HERE, "..", "docs", "moon_blocks.png")); print("chart ok")
