"""Crow caws for the crow mob: harsh, nasal 'caw-caw', one to three at a time, closer and drier than the ambience crow.
  python3 tools/synth_crow.py"""
import numpy as np
from scipy import signal
from synth_ambience import SR, rng, white, bp, env_adsr, reverb, save, place

def caw():
    n_caws = rng.integers(1, 4)
    out = np.zeros(int((0.5 * n_caws + 0.6) * SR))
    for i in range(n_caws):
        d = rng.uniform(.26, .36); f = rng.uniform(500, 650)
        n = int(d * SR)
        ph = 2 * np.pi * np.cumsum(np.linspace(f * 1.05, f * .78, n)) / SR
        saw = signal.sawtooth(ph) + .4 * signal.sawtooth(2 * ph)
        s = bp(saw + white(n) * .45, 550, 2800, 2) * env_adsr(n, .015, .1)
        place(out, s, i * rng.uniform(.42, .55))
    return reverb(out, .18, .8)

if __name__ == "__main__":
    for k in range(4): save(f"critter/crow_caw{k+1}", caw(), .8)
