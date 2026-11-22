"""Calls for the big birds: Canada goose honk and hiss, great blue heron croak.  python3 tools/synth_birds.py"""
import numpy as np
from scipy import signal
from synth_ambience import SR, rng, white, bp, lp, hp, env_adsr, reverb, save, place, seq

def honk():
    """'Ah-honk': a nasal two-part honk, one to three times."""
    parts = []
    for _ in range(rng.integers(1, 4)):
        d = rng.uniform(.18, .26); n = int(d * SR); f = rng.uniform(330, 400)
        ph = 2 * np.pi * np.cumsum(np.interp(np.linspace(0, 1, n), [0, .3, 1], [f * .8, f * 1.15, f])) / SR
        s = signal.sawtooth(ph) + .5 * signal.sawtooth(2 * ph)
        parts.append(bp(s, 400, 3200) * env_adsr(n, .02, .08))
    return reverb(seq(parts, .12), .3, 1.1)

def hiss():
    """A goose's angry hiss, open-beaked."""
    d = rng.uniform(.6, .9); n = int(d * SR)
    return reverb(hp(white(n), 1800) * env_adsr(n, .05, .3) * (0.7 + 0.3 * np.sin(np.linspace(0, 30, n))), .15, .8)

def croak():
    """A heron's deep, rough 'frahnk'."""
    d = rng.uniform(.35, .5); n = int(d * SR); f = rng.uniform(150, 190)
    ph = 2 * np.pi * np.cumsum(np.linspace(f, f * .75, n)) / SR
    s = signal.sawtooth(ph) * (1 + .6 * np.sin(np.linspace(0, d * 2 * np.pi * 35, n)))
    return reverb(lp(bp(s + white(n) * .3, 120, 2200), 1800) * env_adsr(n, .02, .15), .35, 1.2)

if __name__ == "__main__":
    for k in range(4): save(f"critter/goose_honk{k+1}", honk(), .8)
    for k in range(2): save(f"critter/goose_hiss{k+1}", hiss(), .6)
    for k in range(3): save(f"critter/heron_croak{k+1}", croak(), .8)
