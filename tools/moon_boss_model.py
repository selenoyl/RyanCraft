"""The Great Moon Rabbit's in-game model and texture, built from the approved concept (docs/moon_boss_rabbit_eyes.png:
moon-dust fur, bigger vanilla-rabbit eyes, torn ear, buck teeth, jade-studded moonstone pauldron, jade crown, the Moon
Mortar staff). The concept's boxes already use Minecraft's box-UV layout, so they carry straight over; this script
  * regroups them into animatable parts: body (legs, torso, tail, pauldron, collar), head (face, muzzle, teeth, cheeks,
    crown), the two ears (children of the head), the two arms, and the staff,
  * shifts everything so the feet stand on the model's ground line (y = 24),
  * crops the atlas to what's used.
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
    body, head = [], []
    for b in root["boxes"]:
        x, y, z = b["from"]
        (head if y < 10 or (y < 11 and abs(x) < 3) else body).append(b)
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
    # the ears hang from the head: pivots given relative to the head's
    for name, e in (("left_ear", ear_l), ("right_ear", ear_r)):
        pv = [e["pivot"][0] - HEAD_PIVOT[0], e["pivot"][1] + DY - HEAD_PIVOT[1], e["pivot"][2] - HEAD_PIVOT[2]]
        parts.append(part(name, "head", pv, e["rot"], [dict(b, mirror=False) for b in e["boxes"]], "ear"))
    for name, p in (("right_arm", arm_r), ("left_arm", arm_l), ("staff", staff)):
        pv = [p["pivot"][0], p["pivot"][1] + DY, p["pivot"][2]]
        parts.append(part(name, None, pv, p["rot"], [dict(b, mirror=False) for b in p["boxes"]], name))
    # crop the atlas to the used area, rounded up to 16s
    bb = m.atlas.getbbox(); w = (bb[2] + 15) // 16 * 16; h = (bb[3] + 15) // 16 * 16
    m.atlas.crop((0, 0, w, h)).save(os.path.join(A, "textures", "entity", "great_moon_rabbit.png"))
    spec = {"texture": [w, h], "parts": parts}
    p = os.path.join(A, "critter_models", "great_moon_rabbit.json")
    json.dump(spec, open(p, "w"), indent=1); open(p, "a").write("\n")
    tall = max(-min(b["from"][1] + pt["pivot"][1] for pt in parts for b in pt["boxes"]) + 24, 0)
    print("great moon rabbit:", len(parts), "parts,", sum(len(pt["boxes"]) for pt in parts), "boxes, atlas", w, "x", h)

if __name__ == "__main__":
    main()
