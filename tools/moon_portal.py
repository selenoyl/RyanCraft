"""The jade moon portal's surface (moon/MoonPortalBlock): vanilla's portal swirl (all frames) recoloured to the night
sky, deep blue-black through dusky violet, with a field of stars that drifts slowly across from frame to frame.
Blockstates and models are vanilla's nether portal ones, renamed; plus lang.
  python3 tools/moon_portal.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M

HERE = os.path.dirname(os.path.abspath(__file__)); T = os.path.join(HERE, "templates", "skylands", "assets", "minecraft")
TEX = os.path.join(M.A, "textures", "block")
NIGHT, DUSK = (6, 8, 22), (78, 52, 132)
LANG = {"block.ryancraft.moon_portal": "Moon Portal",
        "message.ryancraft.moon_portal_sealed": "The jade stays dark. The way home lies through the Great Moon Rabbit."}

def lang_entries(): return dict(LANG)

def portal():
    src = Image.open(os.path.join(HERE, "templates", "skylands", "nether_portal.png")).convert("RGBA"); W, H = src.size; frames = H // W
    out = Image.new("RGBA", (W, H)); sp, po = src.load(), out.load()
    r = random.Random(29); stars = [(r.uniform(0, 16), r.uniform(0, 16 * frames), r.choice((1, 1, 1, 2))) for _ in range(26)]
    vals = sorted(sum(c[:3]) / 3 for c in src.get_flattened_data() if c[3]); lo, hi = vals[len(vals) // 20], vals[-len(vals) // 20]
    for y in range(H):
        for x in range(W):
            c = sp[x, y]; t = min(1, max(0, (sum(c[:3]) / 3 - lo) / max(1, hi - lo))) ** 1.6
            po[x, y] = tuple(int(NIGHT[k] + (DUSK[k] - NIGHT[k]) * t) for k in range(3)) + (max(c[3], 215),)
    for f in range(frames):                                                 # stars drift right a pixel every 4 frames
        for sx, sy, size in stars:
            x, y = int(sx + f / 4) % 16, int(sy) % 16
            bright = (236, 238, 255) if size == 2 else (176, 180, 220)
            po[x, f * 16 + y] = bright + (255,)
            if size == 2: po[(x + 1) % 16, f * 16 + y] = (140, 146, 200, 255)
    return out

def build():
    portal().save(os.path.join(TEX, "moon_portal.png"))
    json.dump(json.load(open(os.path.join(T, "textures", "block", "nether_portal.png.mcmeta"))), open(os.path.join(TEX, "moon_portal.png.mcmeta"), "w"))
    bs = json.load(open(os.path.join(T, "blockstates", "nether_portal.json")))
    M.w("assets/ryancraft/blockstates/moon_portal.json", json.loads(json.dumps(bs).replace("minecraft:block/nether_portal", "ryancraft:block/moon_portal")))
    for f in ("nether_portal_ns", "nether_portal_ew"):
        m = json.load(open(os.path.join(T, "models", "block", f + ".json")))
        M.w(f"assets/ryancraft/models/block/{f.replace('nether_portal', 'moon_portal')}.json",
            json.loads(json.dumps(m).replace("minecraft:block/nether_portal", "block/nether_portal").replace("block/nether_portal", "ryancraft:block/moon_portal")))   # vanilla writes these without "minecraft:"

if __name__ == "__main__":
    build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(LANG); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("moon portal ok")
# (the Potion of Air's names and icon are written by hand in assets: effect.ryancraft.air, textures/mob_effect/air.png)
