"""1.2.5's procedural water and lava, baked into animated textures for the classic pack.

The old game generated both every tick (WaterFX / LavaFX). This reruns the same simulations and records frames.
Water: the game tints water by the biome's water colour, so frames are stored divided by the tint (64, 114, 255) and every
overworld biome gets that water colour when the classic pack is on: the result is 1.2.5's exact deep blue.
Lava: untinted, the old bright orange-yellow."""
import json, math, os, random
from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "resourcepacks", "classic",
                   "assets", "minecraft", "textures", "block")
WATER_TINT = (64, 114, 255)
FRAMES = 32

def water_frames(seed, flow=False):
    r = random.Random(seed); n = 256
    g = [0.0] * n; h = [0.0] * n; i_ = [0.0] * n; j_ = [0.0] * n; out = []
    for tick in range(200 + FRAMES):
        for x in range(16):
            for y in range(16):
                f = 0.0
                if flow:                                       # flowing water smears downward
                    for k in range(y - 2, y + 1): f += g[(x & 15) + (k & 15) * 16]
                else:
                    for k in range(x - 1, x + 2): f += g[(k & 15) + (y & 15) * 16]
                h[x + y * 16] = f / (3.2 if flow else 3.3) + i_[x + y * 16] * 0.8
        for k in range(n):
            i_[k] += j_[k] * 0.05
            if i_[k] < 0: i_[k] = 0
            j_[k] -= (0.3 if flow else 0.1)
            if r.random() < (0.2 if flow else 0.05): j_[k] = 0.5
        g, h = h, g
        if tick >= 200:
            img = Image.new("RGBA", (16, 16)); px = img.load()
            shift = (tick - 200) if flow else 0
            for k in range(n):
                f1 = max(0.0, min(1.0, g[k])); f2 = f1 * f1
                rr, gg, bb, aa = 32 + f2 * 32, 50 + f2 * 64, 255, 146 + f2 * 50
                px[k % 16, (k // 16 + shift) % 16] = (int(255 * rr / WATER_TINT[0]), int(255 * gg / WATER_TINT[1]), int(255 * bb / WATER_TINT[2]), int(aa))
            out.append(img)
    return out

def lava_frames(seed, flow=False):
    r = random.Random(seed); n = 256
    soup = [0.0] * n; pot = [0.0] * n; flame = [0.0] * n; vel = [0.0] * n; out = []
    for tick in range(300 + FRAMES):
        for x in range(16):
            for y in range(16):
                f = 0.0
                l = int(math.sin(y * math.pi * 2 / 16) * 1.2); i1 = int(math.sin(x * math.pi * 2 / 16) * 1.2)
                for k1 in range(x - 1, x + 2):
                    for i2 in range(y - 1, y + 2):
                        f += soup[((k1 + l) & 15) + ((i2 + i1) & 15) * 16]
                idx = x + y * 16
                pot[idx] = f / 10 + (flame[(x & 15) + (y & 15) * 16] + flame[((x + 1) & 15) + (y & 15) * 16]
                                    + flame[((x + 1) & 15) + ((y + 1) & 15) * 16] + flame[(x & 15) + ((y + 1) & 15) * 16]) / 4 * 0.8
                flame[idx] += vel[idx] * 0.01
                if flame[idx] < 0: flame[idx] = 0
                vel[idx] -= 0.06
                if r.random() < 0.005: vel[idx] = 1.5
        soup, pot = pot, soup
        if tick >= 300:
            img = Image.new("RGBA", (16, 16)); px = img.load()
            shift = (tick - 300) // 2 if flow else 0
            for k in range(n):
                f2 = max(0.0, min(1.0, soup[k] * 2))
                px[k % 16, (k // 16 + shift) % 16] = (int(f2 * 100 + 155), int(f2 * f2 * 255), int(f2 ** 4 * 128), 255)
            out.append(img)
    return out

def save(name, frames, size, frametime, pingpong=False):
    """Write the frames as an animated strip. Still fluids play forward then back, so the loop has no seam; flowing
    fluids scroll exactly one tile per loop and blend between frames so the restart is soft."""
    sheet = Image.new("RGBA", (size, size * len(frames)))
    for i, fr in enumerate(frames):
        tile = Image.new("RGBA", (size, size))
        for ox in range(0, size, 16):
            for oy in range(0, size, 16): tile.paste(fr, (ox, oy))
        sheet.paste(tile, (0, i * size))
    sheet.save(os.path.join(OUT, name + ".png"))
    anim = {"frametime": frametime}
    if pingpong: anim["frames"] = list(range(len(frames))) + list(range(len(frames) - 2, 0, -1))
    else: anim["interpolate"] = True
    with open(os.path.join(OUT, name + ".png.mcmeta"), "w") as f: json.dump({"animation": anim}, f)

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    save("water_still", water_frames(1), 16, 2, pingpong=True)
    save("water_flow", water_frames(2, True), 32, 2)
    save("lava_still", lava_frames(3), 16, 3, pingpong=True)
    save("lava_flow", lava_frames(4, True), 32, 3)
    print("ok")
