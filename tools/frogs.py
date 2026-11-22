"""Frog species as skins of the vanilla frog (frog variants are data-driven): the temperate frog texture recoloured,
with spots or warts painted on. Each gets a frog_variant file with the biomes it prefers."""
import colorsys, json, os, random
from PIL import Image
HERE = os.path.dirname(os.path.abspath(__file__)); RES = os.path.join(HERE, "..", "src", "main", "resources")
TEX = os.path.join(RES, "assets", "ryancraft", "textures", "entity", "frog")

def hexc(h): h = h.lstrip("#"); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
def mul(c, k): return tuple(max(0, min(255, int(v * k))) for v in c)

# name: (skin, light/belly, spot colour, spot chance, spot size, biomes it prefers)
SPECIES = {
    "green_frog":    ("#5f8a3c", "#e6dfb0", "#34501e", 0.06, 1, ["minecraft:swamp", "minecraft:river", "ryancraft:rainforest"]),
    "leopard_frog":  ("#6e9c4c", "#ece6c4", "#2c3a1a", 0.16, 2, ["minecraft:river", "minecraft:plains", "minecraft:meadow", "minecraft:swamp"]),
    "american_toad": ("#8a6a48", "#d8c8a0", "#4a3424", 0.30, 1, "#minecraft:is_forest"),
    "spring_peeper": ("#c49a6a", "#eadcc0", "#7a5434", 0.10, 2, ["minecraft:forest", "minecraft:birch_forest", "ryancraft:seasonal_forest", "minecraft:flower_forest"]),
    "gray_treefrog": ("#8c9088", "#e4e0cc", "#4a4e48", 0.22, 2, ["minecraft:dark_forest", "minecraft:old_growth_birch_forest", "minecraft:taiga"]),
}

def paint(name, skin, light, spot, p, size, seed):
    t = Image.open(os.path.join(HERE, "template_frog.png")).convert("RGBA"); out = t.copy(); px = out.load(); r = random.Random(seed)
    skin_px = []
    for y in range(t.height):
        for x in range(t.width):
            R, G, B, A = px[x, y]
            if not A: continue
            h, s, v = colorsys.rgb_to_hsv(R / 255, G / 255, B / 255)
            if 0.02 < h < 0.14 and s > 0.45 and v > 0.45:                          # orange skin
                px[x, y] = mul(hexc(skin), (v / 0.8) ** 1.1) + (A,); skin_px.append((x, y))
            elif 0.06 < h < 0.16 and s <= 0.45 and v > 0.6:                        # tan underside / mouth line
                px[x, y] = mul(hexc(light), v / 0.85) + (A,)
    for (x, y) in skin_px:                                                         # spots / warts
        if r.random() < p:
            for dx in range(size):
                for dy in range(size):
                    if (x + dx, y + dy) in skin_px or (dx == 0 and dy == 0): px[x + dx, y + dy] = mul(hexc(spot), 0.9 + 0.2 * r.random()) + (255,)
            if name == "american_toad" and r.random() < 0.4 and (x, y - 1) in skin_px:
                px[x, y - 1] = mul(hexc("#c49a4a"), 1.0) + (255,)                  # warts catch the light
    out.save(os.path.join(TEX, name + ".png"))

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(obj, f, indent=2)

if __name__ == "__main__":
    os.makedirs(TEX, exist_ok=True)
    for i, (name, (skin, light, spot, p, size, biomes)) in enumerate(SPECIES.items()):
        paint(name, skin, light, spot, p, size, 500 + i)
        w(os.path.join(RES, "data", "ryancraft", "frog_variant", name + ".json"), {"asset_id": f"ryancraft:entity/frog/{name}",
          "spawn_conditions": [{"condition": {"type": "minecraft:biome", "biomes": biomes}, "priority": 1}]})
    print("frogs ok")
