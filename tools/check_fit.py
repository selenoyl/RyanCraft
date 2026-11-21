"""Measures gaps between model parts (rest pose). Every part should touch or overlap its neighbour.
  python3 tools/check_fit.py        -> prints any part that floats free of the rest of its model"""
import itertools, sys
import numpy as np
sys.path.insert(0, __import__("os").path.dirname(__file__))
from render_iso import HAND, load_model, rot_matrix
import json, os, glob

def part_boxes(part):
    R = rot_matrix(*part["rot"]); piv = np.array(part["pivot"], float)
    out = []
    for b in part["boxes"]:
        lo = np.array(b["from"], float); sz = np.array(b["size"], float)
        corners = np.array([lo + sz * np.array(c) for c in itertools.product((0, 1), repeat=3)])
        pts = corners @ R.T + piv
        out.append((pts.min(0), pts.max(0)))
    return out

def gap(a, b):
    """Largest axis separation between two AABBs (<= 0 means touching/overlapping)."""
    return max(max(a[0][i] - b[1][i], b[0][i] - a[1][i]) for i in range(3))

def check(name):
    _, parts = load_model(name)
    pb = [part_boxes(p) for p in parts]
    names = [p.get("name", f"part{i}") for i, p in enumerate(parts)]
    def part_gap(i, k): return min(gap(a, b) for a in pb[i] for b in pb[k])
    # start from the biggest part (the body) and walk everything that touches
    vol = [sum(np.prod(b[1] - b[0]) for b in boxes) for boxes in pb]
    seen = {int(np.argmax(vol))}; frontier = list(seen)
    while frontier:
        i = frontier.pop()
        for k in range(len(parts)):
            if k not in seen and part_gap(i, k) <= 0.01:
                seen.add(k); frontier.append(k)
    problems = []
    # Joints that pivot (heads) must be sunk into what they attach to, not just touching it
    for k, p in enumerate(parts):
        if p.get("role") == "head":
            body = [i for i, q in enumerate(parts) if q.get("role") in ("body", "fish_body")]
            if body:
                depth = -min(part_gap(k, i) for i in body)
                if depth < 0.99:
                    problems.append(f"  {names[k]}: only {max(depth, 0):.2f}px into the body (needs 1px so it can turn without a seam)")
    for k in range(len(parts)):
        if k not in seen:
            nearest = min(part_gap(k, i) for i in seen)
            problems.append(f"  {names[k]}: floats {nearest:.2f}px away from the body")
    return problems

if __name__ == "__main__":
    specs = sorted(os.path.basename(f)[:-5] for f in glob.glob(os.path.join(os.path.dirname(__file__), "..",
                   "src/main/resources/assets/ryancraft/critter_models/*.json"))) + list(HAND)
    bad = 0
    for n in specs:
        probs = check(n)
        print(("OK   " if not probs else "GAP  ") + n)
        for p in probs: print(p); bad += 1
    sys.exit(1 if bad else 0)
