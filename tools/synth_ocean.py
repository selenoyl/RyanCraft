"""Ocean calls: orca whistles and clicks, blue whale song.  python3 tools/synth_ocean.py"""
import numpy as np
from synth_ambience import SR, rng, white, bp, lp, hp, env_adsr, reverb, save, place

def tone(f, harm):
    ph = 2 * np.pi * np.cumsum(f) / SR
    return sum(a * np.sin((k + 1) * ph) for k, a in enumerate(harm))

def orca_call():
    """Rising and falling whistles with a burst of clicks, the way a pod talks."""
    out = np.zeros(int(2.2 * SR)); t = 0.0
    for _ in range(rng.integers(1, 3)):
        d = rng.uniform(.5, .9); n = int(d * SR); u = np.linspace(0, 1, n)
        f0 = rng.uniform(1100, 1600)
        f = f0 * (1 + 0.35 * np.sin(np.pi * u * rng.uniform(1, 2)))
        place(out, tone(f, (1, .3, .1)) * env_adsr(n, .05, .15) * .6, t); t += d + .1
    for k in range(rng.integers(6, 14)):                           # echolocation clicks
        n = int(.004 * SR); place(out, hp(white(n), 2000) * env_adsr(n, .0005, .003), t + k * .03)
    return reverb(bp(out, 300, 9000), .55, 1.8)

def whale_song():
    """Long, low, mournful moans that slide in pitch, deep in the water."""
    d = rng.uniform(3.0, 4.5); n = int(d * SR); u = np.linspace(0, 1, n)
    f = np.interp(u, [0, .3, .6, 1], [rng.uniform(70, 90), rng.uniform(140, 180), rng.uniform(100, 130), rng.uniform(60, 80)])
    s = tone(f, (1, .6, .4, .25, .15)) * env_adsr(n, .6, 1.0)
    s = lp(s, 900) + lp(white(n), 300) * .05 * env_adsr(n, .6, 1.0)
    return reverb(s, .7, 2.5)

if __name__ == "__main__":
    for k in range(3): save(f"critter/orca_call{k+1}", orca_call(), .8)
    for k in range(3): save(f"critter/whale_song{k+1}", whale_song(), .85)
