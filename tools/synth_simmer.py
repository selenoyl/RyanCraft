"""The cooking pot's simmer: a low, soft rolling boil. Three takes of about three seconds: a dark rumble under
irregular bubble pops (short rising blips, low and round), a few bigger glugs. Played while a pot is boiling.
  python3 tools/synth_simmer.py"""
import numpy as np
from synth_ambience import SR, rng, lp, bp, save, place

def bubble(f0, dur, amp):
    t = np.arange(int(dur * SR)) / SR
    f = f0 * (1 + 1.6 * t / dur)                                  # the pitch rises as the bubble opens
    ph = 2 * np.pi * np.cumsum(f) / SR
    return amp * np.sin(ph) * np.exp(-t / (dur * 0.35)) * (1 - np.exp(-t * 900))

def take(seed, dur=3.0):
    r = np.random.default_rng(seed); n = int(dur * SR)
    bed = lp(r.standard_normal(n), 260, 3) * 0.35                   # the rolling boil under it all
    bed *= 0.7 + 0.3 * np.sin(2 * np.pi * r.uniform(0.6, 1.2) * np.arange(n) / SR)
    out = bed.copy()
    t = 0.0
    while t < dur - 0.1:
        t += r.exponential(0.07)
        big = r.random() < 0.12
        place(out, bubble(r.uniform(90, 140) if big else r.uniform(170, 380), r.uniform(0.06, 0.12) if big else r.uniform(0.02, 0.05),
                                r.uniform(0.6, 1.0) if big else r.uniform(0.15, 0.45)), t)
    out = bp(out, 60, 1800, 2)
    fade = int(0.08 * SR); out[:fade] *= np.linspace(0, 1, fade); out[-fade:] *= np.linspace(1, 0, fade)
    return out

if __name__ == "__main__":
    for i in range(3): save(f"block/cooking_pot/simmer{i + 1}", take(70 + i), peak=0.7)
