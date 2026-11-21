"""
Calls for the big game and predators (elk, moose, pronghorn, bobcat). All synthesized from scratch.
  python3 tools/synth_biggame.py
"""
import numpy as np
from synth_ambience import SR, rng, white, bp, lp, hp, env_adsr, sweep, reverb, save, seq, place

def tone(f, harm):
    ph = 2 * np.pi * np.cumsum(f) / SR
    return sum(a * np.sin((k + 1) * ph) for k, a in enumerate(harm))

def elk_bugle():
    """A bull elk: a low growl that climbs into a long, eerie, squealing whistle, then breaks into grunts."""
    d = rng.uniform(2.6, 3.2); n = int(d * SR); u = np.linspace(0, 1, n)
    top = rng.uniform(1500, 1900)
    f = np.interp(u, [0, .12, .3, .75, .88, 1], [260, 520, top, top * 1.04, top * .7, 420])
    f *= 1 + .01 * np.sin(2 * np.pi * 6 * u * d)
    whistle = tone(f, (1, .25, .08)) * env_adsr(n, .15, .25)
    growl = tone(f * .5, (1, .8, .6, .4)) * np.clip(1 - u * 3, 0, 1) * .8
    breath = bp(white(n), 1500, 4000) * .04 * env_adsr(n, .2, .3)
    s = hp(whistle + growl + breath, 120)
    grunts = []
    for _ in range(rng.integers(3, 5)):
        g = rng.uniform(.18, .26); m = int(g * SR)
        gs = tone(np.full(m, rng.uniform(110, 140)), (1, .9, .7, .5, .3)) * env_adsr(m, .02, .1)
        grunts.append(lp(gs + bp(white(m), 200, 900) * .4 * env_adsr(m, .02, .1), 1200) * .8)
    return reverb(seq([s] + grunts, .12), .5, 1.8)

def elk_chirp():
    """A cow elk's mew: short, high and squeaky."""
    d = rng.uniform(.25, .4); n = int(d * SR)
    f0 = rng.uniform(800, 1000)
    s = sweep(d, f0, f0 * rng.uniform(1.25, 1.5), .5, (1, .4, .15)) * env_adsr(n, .02, .12)
    return reverb(bp(s + white(n) * .03 * env_adsr(n, .02, .12), 400, 5000), .3, 1.0)

def moose_call():
    """A long, nasal, mournful moan (cow), or a deep 'oonk' grunt (bull)."""
    if rng.random() < .6:
        d = rng.uniform(1.6, 2.4); n = int(d * SR); u = np.linspace(0, 1, n)
        f = np.interp(u, [0, .3, .8, 1], [180, 300, 330, 210]) * (1 + .02 * np.sin(2 * np.pi * 4 * u * d))
        s = tone(f, (1, .9, .8, .7, .55, .4, .3, .2)) * env_adsr(n, .2, .35)
        s = bp(s, 150, 2200) + bp(white(n), 300, 1500) * .08 * env_adsr(n, .2, .35)
        return reverb(s, .45, 1.6)
    parts = []
    for _ in range(rng.integers(1, 3)):
        g = rng.uniform(.3, .4); m = int(g * SR)
        gs = tone(np.linspace(130, 95, m), (1, .9, .7, .5, .3)) * env_adsr(m, .03, .15)
        parts.append(lp(gs, 900))
    return reverb(seq(parts, .4), .4, 1.4)

def pronghorn_snort():
    """A sharp alarm snort: a blast of air through the nose, sometimes twice."""
    parts = []
    for _ in range(rng.integers(1, 3)):
        d = rng.uniform(.15, .25); n = int(d * SR)
        parts.append(bp(white(n), 500, 3500) * env_adsr(n, .005, .12) + tone(np.full(n, 160.0), (1, .5)) * .15 * env_adsr(n, .005, .08))
    return reverb(seq(parts, .18), .25, .9)

def bobcat_scream():
    """A raw, raspy screech, like something being murdered in the woods at night."""
    parts = []
    for _ in range(rng.integers(1, 3)):
        d = rng.uniform(.6, 1.0); n = int(d * SR); u = np.linspace(0, 1, n)
        f = np.interp(u, [0, .2, .7, 1], [600, 1100, 1000, 650])
        rough = 1 + .6 * np.sin(2 * np.pi * 70 * u * d) * (rng.random() * .5 + .5)        # harsh growl in the voice
        s = tone(f, (1, .7, .5, .35, .2)) * rough * env_adsr(n, .05, .2)
        s = s + bp(white(n), 1000, 6000) * .35 * env_adsr(n, .05, .2)
        parts.append(bp(s, 300, 7000))
    return reverb(seq(parts, .25), .5, 1.7)

def bobcat_growl():
    """A low, throaty growl with a spit and hiss on the end."""
    d = rng.uniform(.6, .9); n = int(d * SR); u = np.linspace(0, 1, n)
    am = .55 + .45 * np.sin(2 * np.pi * 26 * u * d)
    g = (lp(white(n), 500) * 2 + tone(np.full(n, 105.0), (1, .7, .5))) * am * env_adsr(n, .05, .1)
    m = int(.25 * SR); hiss = hp(white(m), 2500) * env_adsr(m, .01, .15) * .5
    return reverb(np.concatenate([g, hiss]), .25, .9)

if __name__ == "__main__":
    for k in range(3): save(f"critter/elk_bugle{k+1}", elk_bugle(), .85)
    for k in range(3): save(f"critter/elk_chirp{k+1}", elk_chirp(), .7)
    for k in range(4): save(f"critter/moose_call{k+1}", moose_call(), .8)
    for k in range(3): save(f"critter/pronghorn_snort{k+1}", pronghorn_snort(), .7)
    for k in range(2): save(f"critter/bobcat_scream{k+1}", bobcat_scream(), .8)
    for k in range(3): save(f"critter/bobcat_growl{k+1}", bobcat_growl(), .75)
