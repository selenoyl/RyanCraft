"""Cannons and the musket (combat/Cannons.java, combat/MusketItem.java): the entity texture (carriage of dark classic oak, gunmetal barrel), hand-drawn item
sprites for the cannon and every shot, item models, recipes and lang.
  python3 tools/cannons.py      (tools/resources.py merges the lang)"""
import json, os, random
from PIL import Image
import moon_assets as M
import metals_assets as MA

I = os.path.join(M.TEX, "item"); E = os.path.join(M.TEX, "entity")
cb = lambda n: os.path.join(M.CLASSIC, "block", n + ".png")

def entity_tex():
    im = Image.new("RGBA", (128, 64)); px = im.load()
    planks = MA.recolour(cb("oak_planks"), [(46, 30, 16), (86, 58, 32), (122, 86, 50)]).load()
    r = random.Random(5)
    for y in range(64):
        for x in range(128):
            if x < 64: px[x, y] = planks[x % 16, y % 16]
            else:                                                   # cast iron: dark, faintly mottled, a lighter band now and then
                v = 52 + r.randrange(10) + (14 if y % 12 in (5, 6) else 0)
                px[x, y] = (v, v + 2, v + 8, 255)
    for y in range(24, 42):                                         # wheel faces: an iron tyre round dark spokes
        for x in range(0, 44):
            if (x % 22) in (0, 1, 20, 21) or y in (24, 25, 40, 41): px[x, y] = (44, 44, 50, 255)
    im.save(os.path.join(E, "cannon.png"))

PAL = {"k": (24, 24, 30), "d": (52, 54, 62), "m": (88, 90, 100), "l": (140, 144, 156), "w": (210, 214, 224),
       "o": (120, 30, 10), "r": (210, 70, 24), "y": (255, 180, 60), "Y": (255, 236, 150),
       "b": (70, 46, 24), "B": (118, 82, 46), "c": (196, 176, 130), "C": (150, 130, 92), "s": (90, 72, 50),
       "t": (150, 156, 166), "T": (196, 202, 210), "f": (230, 220, 190)}
ART = {
    "cannonball": ["................", "................", "................", "......kkkk......", "....kkddddkk....", "...kddmmmmddk...",
                   "...kdmwlmmmdk...", "..kdmlwmmmmddk..", "..kdmmmmmmmddk..", "..kddmmmmmdddk..", "...kddmmmdddk...", "...kkddddddkk...",
                   "....kkkddkkk....", "......kkkk......", "................", "................"],
    "heated_shot": ["................", "................", "................", "......oooo......", "....oorrrroo....", "...orryyyyrro...",
                    "...oryYYyyyro...", "..orryYyyyyrro..", "..oryyyyyyyrro..", "..orryyyyyrrro..", "...orryyyrrro...", "...oorrrrrroo...",
                    "....ooorrooo....", "......oooo......", "................", "................"],
    "explosive_shell": ["................", "........f.......", ".......fb.......", "......sbs.......", "......kkkk......", "....kkkkkkkk....",
                        "...kkdkkkkkkk...", "...kdlkkkkkkk...", "..kkdkkkkkkkkk..", "..kkkkkkkkkkkk..", "..kkkkkkkkkkkk..", "...kkkkkkkkkk...",
                        "...kkkkkkkkkk...", "....kkkkkkkk....", "......kkkk......", "................"],
    "chain_shot": ["................", "................", "..kkkk..........", ".kdmmdk.........", ".kmwmdk.........", ".kdmddk.........",
                   "..kkkkd.........", "......d.d.......", ".......d.d......", ".........d.kkkk.", ".........dkdmmdk", "..........kmwmdk",
                   "..........kdmddk", "...........kkkk.", "................", "................"],
    "grapeshot": ["................", "................", ".......ss.......", "......sccs......", ".......ss.......", ".....cCccCc.....",
                  "....cCcCCcCc....", "...cCcCccCcCc...", "...CcCcCCcCcC...", "...cCcCccCcCc...", "...CcCcCCcCcC...", "....cCcCcCcC....",
                  ".....CcCcCc.....", "................", "................", "................"],
    "canister_shot": ["................", "................", "....dddddddd....", "...dTTTTTTTTd...", "...dttttttttd...", "...dTTTTTTTTd...",
                      "...dttttttttd...", "...dtTttttttd...", "...dtTttttttd...", "...dttttttttd...", "...dTTTTTTTTd...", "...dttttttttd...",
                      "...dTTTTTTTTd...", "....dddddddd....", "................", "................"],
    "musket_ball": ["................", "................", "................", "................", "................", "......kkk.......",
                    ".....kmwmk......", ".....kmmmk......", ".....kdmdk......", "......kkk.......", "................", "................",
                    "................", "................", "................", "................"],
    "musket": ["................", "..............k.", ".............kmk", "............kmk.", "...........kmk..", "..........kmk...",
               ".........kmk....", "........kmk.....", ".......kdkB.....", "......kdBBb.....", ".....BBBbbk.....", "....bBBbsk......",
               "...bBBbs.k......", "..bBBbs.........", "..bbbs..........", "...s............"],
    "cannon": ["................", "................", "................", "................", "...........kk...", ".........kkmmk..",
               ".......kkmmddk..", ".....kkmmddkk...", "..kkkmmddkk.....", ".kmdkddkkBBb....", ".kkkkkkBBBBbb...", "...bbBBBBbbBBbs.",
               "..bkkkb..bbBbs..", "..kbbbk...ssbs..", "..kbBbk.........", "...kkk.........."],
}
TITLES = {"musket": "Musket", "cannon": "Cannon", "cannonball": "Cannonball", "heated_shot": "Heated Shot", "chain_shot": "Chain Shot", "grapeshot": "Grapeshot",
          "canister_shot": "Canister Shot", "explosive_shell": "Explosive Shell", "musket_ball": "Musket Ball"}

def sprite(rows):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row[:16]):
            if ch in PAL: px[x, y] = PAL[ch] + (255,)
    return im

def lang_entries():
    out = {f"item.ryancraft.{k}": v for k, v in TITLES.items()}
    out["entity.ryancraft.cannon"] = "Cannon"; out["entity.ryancraft.cannon_shot"] = "Cannon Shot"
    out["entity.ryancraft.cannon.cooling"] = "The barrel is still hot: let it cool and swab it out"
    return out

def build():
    W, R = M.w, "data/ryancraft/recipe/"
    for k, rows in ART.items():
        sprite(rows).save(os.path.join(I, k + ".png"))
        W(f"assets/ryancraft/models/item/{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"ryancraft:item/{k}"}})
        W(f"assets/ryancraft/items/{k}.json", {"model": {"type": "minecraft:model", "model": f"ryancraft:item/{k}"}})
    W("assets/ryancraft/models/item/musket.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": "ryancraft:item/musket"}})
    sh = lambda name, pat, key, n=1, suffix="": W(f"{R}{name}{suffix}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pat,
        "key": key, "result": {"id": f"ryancraft:{name}", "count": n}})
    sl = lambda name, ing, n=1, suffix="": W(f"{R}{name}{suffix}.json", {"type": "minecraft:crafting_shapeless", "category": "equipment",
        "ingredients": ing, "result": {"id": f"ryancraft:{name}", "count": n}})
    sh("cannon", ["III", "LGL", "S S"], {"I": "minecraft:iron_block", "L": "#minecraft:logs", "G": "minecraft:gunpowder", "S": "#minecraft:wooden_slabs"})
    sh("musket", ["  I", " IF", "PS "], {"I": "minecraft:iron_ingot", "F": "minecraft:flint", "P": "#minecraft:planks", "S": "minecraft:stick"})
    sh("cannonball", ["##", "##"], {"#": "ryancraft:lead_ingot"}, 2, "_from_lead")
    sh("cannonball", ["##", "##"], {"#": "minecraft:iron_ingot"}, 2, "_from_iron")
    sl("musket_ball", ["ryancraft:lead_ingot"], 4)
    sh("grapeshot", ["MMM", "MWM", "MMM"], {"M": "ryancraft:musket_ball", "W": "#minecraft:wool"}, 2)
    sh("canister_shot", ["MMM", "MTM", "MMM"], {"M": "ryancraft:musket_ball", "T": "ryancraft:tin_ingot"}, 2)
    sl("chain_shot", ["ryancraft:cannonball", "minecraft:iron_chain", "ryancraft:cannonball"])
    sl("explosive_shell", ["ryancraft:cannonball", "minecraft:gunpowder", "minecraft:gunpowder", "minecraft:string"])
    W(f"{R}heated_shot.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": "ryancraft:cannonball",
        "result": {"id": "ryancraft:heated_shot"}, "experience": 0.1, "cookingtime": 200})

if __name__ == "__main__":
    entity_tex(); build()
    p = os.path.join(M.A, "lang", "en_us.json"); d = json.load(open(p)); d.update(lang_entries()); json.dump(dict(sorted(d.items())), open(p, "w"), indent=2)
    print("cannons ok")
