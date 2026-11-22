"""Calls for the small things: bobwhite whistle, rattlesnake rattle, alligator bellow.  python3 tools/synth_small.py"""
import numpy as np
from synth_ambience import SR, rng, white, bp, lp, hp, env_adsr, reverb, save, place, seq

def tone(f, harm=(1, .2)):
    ph = 2 * np.pi * np.cumsum(f) / SR
    return sum(a * np.sin((k + 1) * ph) for k, a in enumerate(harm))

def bobwhite():
    """'Bob-WHITE!': a short low note, then a loud rising whistle."""
    n1 = int(.14 * SR); n2 = int(.32 * SR)
    a = tone(np.linspace(1500, 1600, n1)) * env_adsr(n1, .01, .05) * .6
    b = tone(np.interp(np.linspace(0, 1, n2), [0, .7, 1], [1700, 3000, 2800])) * env_adsr(n2, .02, .08)
    return reverb(seq([a, b], .12), .3, 1.0)

def rattle():
    """The buzzing dry rattle of a rattlesnake's tail."""
    d = rng.uniform(1.2, 1.8); n = int(d * SR)
    rate = rng.uniform(45, 60); u = np.arange(n) / SR
    pulses = (np.sin(2 * np.pi * rate * u) > 0.6).astype(float)
    s = hp(white(n), 3000) * pulses * env_adsr(n, .05, .3)
    return reverb(s, .1, .6)

def bellow():
    """An alligator's deep, rumbling bellow."""
    d = rng.uniform(1.0, 1.4); n = int(d * SR)
    am = 0.6 + 0.4 * np.sin(np.linspace(0, d * 2 * np.pi * 18, n))
    s = (lp(white(n), 220) * 2.5 + tone(np.full(n, 55.0), (1, .8, .6, .4))) * am * env_adsr(n, .15, .35)
    return reverb(lp(s, 600), .4, 1.4)

if __name__ == "__main__":
    for k in range(3): save(f"critter/bobwhite_call{k+1}", bobwhite(), .8)
    for k in range(3): save(f"critter/rattlesnake_rattle{k+1}", rattle(), .7)
    for k in range(2): save(f"critter/alligator_bellow{k+1}", bellow(), .85)
