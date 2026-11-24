"""The Great Moon Rabbit's in-game model and texture, built from the approved concept (docs/moon_boss_rabbit_eyes.png:
moon-dust fur, bigger vanilla-rabbit eyes, torn ear, buck teeth, jade-studded moonstone pauldron, jade crown, the Moon
Mortar staff). The concept's boxes already use Minecraft's box-UV layout, so they carry straight over; this script
  * regroups them into animatable parts: body (legs, torso, tail, pauldron, collar), head (face, muzzle, teeth, cheeks,
    crown as its own part on the head), the two ears (children of the head), the two arms, and the staff,
  * shifts everything so the feet stand on the model's ground line (y = 24),
  * crops the atlas to what's used, adds the frenzy's halo of rays behind the head, and draws the glow sheets: the
    frenzy's eyes and halo, and the staff's jade top for the summon.
Writes assets/ryancraft/critter_models/great_moon_rabbit.json (parts carry a "parent") and
textures/entity/great_moon_rabbit.png.   python3 tools/moon_boss_model.py <vanilla textures dir>   (the unpacked client jar's assets/minecraft/textures)"""
import json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import moon_boss_original as MB

HERE = os.path.dirname(os.path.abspath(__file__)); A = os.path.join(HERE, "..", "src/main/resources/assets/ryancraft")
DY = -22                                    # the concept's feet stand at y 46; the model's ground is y 24
HEAD_PIVOT = [0, 11 + DY, -2]               # the neck

def main():
    m = MB.rabbit("dust", None, None, MB.vrabbit_big)
    root, *others = m.parts                 # others: ear (left), ear (right), arm, arm, staff
    ear_l, ear_r, arm_r, arm_l, staff = others
    body, head, crown = [], [], []
    for b in root["boxes"]:
        x, y, z = b["from"]
        if y < -3: crown.append(b)                       # the crown: its band (y -4), points (-7) and jade studs (-6)
        elif y < 10 or (y < 11 and abs(x) < 3): head.append(b)
        else: body.append(b)
    def shift(boxes, pivot):
        out = []
        for b in boxes:
            x, y, z = b["from"]
            out.append({"uv": b["uv"], "from": [round(x - pivot[0], 3), round(y + DY - pivot[1], 3), round(z - pivot[2], 3)], "size": b["size"], "mirror": False})
        return out
    def part(name, parent, pivot, rot, boxes, role=None):
        return {"name": name, "parent": parent, "pivot": pivot, "rot": [round(r, 4) for r in rot], "role": role, "boxes": boxes}
    # (shift() moves a box down by DY onto the ground line and makes it relative to the part's pivot)
    parts = [part("body", None, [0, 0, 0], (0, 0, 0), shift(body, [0, 0, 0]), "body"),
             part("head", None, HEAD_PIVOT, (0, 0, 0), shift(head, HEAD_PIVOT), "head")]
    # the crown sits on the head as its own part, so it can tumble off when the rabbit falls
    CROWN_PIVOT = [0, -5 + DY, -2]
    parts.append(part("crown", "head", [CROWN_PIVOT[i] - HEAD_PIVOT[i] for i in range(3)], (0, 0, 0), shift(crown, CROWN_PIVOT), "crown"))
    # the ears hang from the head: pivots given relative to the head's
    for name, e in (("left_ear", ear_l), ("right_ear", ear_r)):
        pv = [e["pivot"][0] - HEAD_PIVOT[0], e["pivot"][1] + DY - HEAD_PIVOT[1], e["pivot"][2] - HEAD_PIVOT[2]]
        parts.append(part(name, "head", pv, e["rot"], [dict(b, mirror=False) for b in e["boxes"]], "ear"))
    for name, p in (("right_arm", arm_r), ("left_arm", arm_l), ("staff", staff)):
        pv = [p["pivot"][0], p["pivot"][1] + DY, p["pivot"][2]]
        parts.append(part(name, None, pv, p["rot"], [dict(b, mirror=False) for b in p["boxes"]], name))
    # crop the atlas to the used area, rounded up to 16s, plus a 16-pixel strip below for the halo's rays
    bb = m.atlas.getbbox(); w = (bb[2] + 15) // 16 * 16; h0 = (bb[3] + 15) // 16 * 16; h = h0 + 16
    atlas = m.atlas.crop((0, 0, w, h)); atlas.save(os.path.join(A, "textures", "entity", "great_moon_rabbit.png"))
    # the eclipse halo: twelve rays, long and short, fanned behind the head (hidden until the frenzy). Their strip of the
    # atlas is empty, so the normal pass draws nothing; the glow pass draws them from the eyes sheet.
    parts.append(part("halo", "head", [0, -7, 7.5], (0, 0, 0), [], "halo"))
    for i in range(12):
        long = i % 2 == 0
        parts.append(part("halo_ray_%d" % i, "halo", [0, 0, 0], (0, 0, i * math.pi / 6),
                          [{"uv": [0 if long else 8, h0], "from": [-1, -22 if long else -17, -0.5], "size": [2, 11 if long else 6, 1], "mirror": False}], "halo"))
    # frenzy: a glow layer of just its eyes, blazing violet (drawn full-bright over the model)
    from PIL import Image
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0)); gp = glow.load(); ap = atlas.load()
    face = next(b for b in head if b["size"] == [14, 13, 12])
    fu, fv = face["uv"][0] + 12, face["uv"][1] + 12          # the face's front: u + depth, v + depth
    for x0 in (2, 10):
        for dx in (0, 1):
            for dy in (0, 1): gp[fu + x0 + dx, fv + 4 + dy] = (236, 120, 255, 255)
    gp[fu + 2, fv + 4] = (255, 220, 255, 255); gp[fu + 11, fv + 4] = (255, 220, 255, 255)
    for y in range(h0, h):
        for x in range(16): gp[x, y] = (230 - (y - h0) * 3, 150 + (y - h0) * 4, 255, 255)
    glow.save(os.path.join(A, "textures", "entity", "great_moon_rabbit_eyes.png"))
    # the summon: the staff's jade top, glowing (its own pixels, brightened, drawn full-bright)
    jade = Image.new("RGBA", (w, h), (0, 0, 0, 0)); jp = jade.load()
    for b in staff["boxes"]:
        if b["from"][1] >= -30: continue                       # just the mortar head at the top
        (u, v), (bw, bh, bd) = b["uv"], [math.ceil(x) for x in b["size"]]
        for y in range(v, v + bd + bh):
            for x in range(u, u + 2 * (bd + bw)):
                r, g, bl, a = ap[x, y]
                if a: jp[x, y] = (min(255, int(r * 1.3) + 30), min(255, int(g * 1.3) + 40), min(255, int(bl * 1.3) + 30), 255)
    jade.save(os.path.join(A, "textures", "entity", "great_moon_rabbit_jade.png"))
    spec = {"texture": [w, h], "parts": parts}
    p = os.path.join(A, "critter_models", "great_moon_rabbit.json")
    json.dump(spec, open(p, "w"), indent=1); open(p, "a").write("\n")
    tall = max(-min(b["from"][1] + pt["pivot"][1] for pt in parts for b in pt["boxes"]) + 24, 0)
    print("great moon rabbit:", len(parts), "parts,", sum(len(pt["boxes"]) for pt in parts), "boxes, atlas", w, "x", h)

if __name__ == "__main__":
    main()
