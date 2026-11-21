"""
Synthesizes RyanCraft's original ambience and turkey sounds (no samples used).
Run:  python3 tools/synth_ambience.py   (needs numpy, scipy and ffmpeg with libvorbis)
Writes Ogg Vorbis files to src/main/resources/assets/ryancraft/sounds/.
Any resource pack can replace these with real recordings at the same paths.
"""
import os, subprocess, tempfile, wave
import numpy as np
from scipy import signal

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/ryancraft/sounds")
rng = np.random.default_rng(1205)

# ---------- building blocks ----------
def t_(dur): return np.arange(int(dur * SR)) / SR
def white(n): return rng.standard_normal(n)
def pink(n):
    w = white(n); b, a = [0.049922035, -0.095993537, 0.050612699, -0.004408786], [1, -2.494956002, 2.017265875, -0.522189400]
    return signal.lfilter(b, a, w) * 8
def brown(n):
    x = np.cumsum(white(n)); x = signal.lfilter([1, -1], [1, -0.995], x); return x / (np.abs(x).max() + 1e-9)
def bp(x, lo, hi, order=2): return signal.sosfilt(signal.butter(order, [lo, hi], 'band', fs=SR, output='sos'), x)
def lp(x, f, order=2): return signal.sosfilt(signal.butter(order, f, 'low', fs=SR, output='sos'), x)
def hp(x, f, order=2): return signal.sosfilt(signal.butter(order, f, 'high', fs=SR, output='sos'), x)
def smooth_rand(n, rate_hz, lo=0.0, hi=1.0):
    """Smooth random curve (cubic-interpolated control points)."""
    k = max(4, int(n / SR * rate_hz) + 4)
    pts = rng.uniform(lo, hi, k)
    xs = np.linspace(0, n, k)
    from scipy.interpolate import CubicSpline
    return np.clip(CubicSpline(xs, pts, bc_type='periodic' if False else 'not-a-knot')(np.arange(n)), lo, hi)
def env_adsr(n, a, r, curve=2.0):
    e = np.ones(n); na, nr = max(1, int(a * SR)), max(1, int(r * SR))
    e[:na] = np.linspace(0, 1, na) ** curve if na < n else e[:na]
    if nr < n: e[-nr:] *= np.linspace(1, 0, nr) ** curve
    return e
def sweep(dur, f0, f1, shape=1.0, harm=(1.0,), vib=0.0, vibf=0.0):
    n = int(dur * SR); x = np.linspace(0, 1, n) ** shape
    f = f0 + (f1 - f0) * x
    if vib: f = f * (1 + vib * np.sin(2 * np.pi * vibf * np.arange(n) / SR))
    ph = 2 * np.pi * np.cumsum(f) / SR
    return sum(a * np.sin((i + 1) * ph) for i, a in enumerate(harm))
def reverb(x, wet=0.3, size=1.0, damp=0.4):
    out = np.zeros(len(x) + int(SR * 1.5 * size))
    xx = np.concatenate([x, np.zeros(len(out) - len(x))])
    acc = np.zeros_like(out)
    for d, g in [(1557, .84), (1617, .83), (1491, .85), (1422, .86), (1277, .87), (1356, .86)]:
        d = int(d * size); g = g * (0.78 + 0.08 * min(size, 1.8) / 1.8); b = [0] * d + [1]; a = np.zeros(d + 1); a[0] = 1; a[d] = -g
        y = signal.lfilter(b, a, xx); acc += lp(y, 4500 * (1 - damp) + 600)
    for d, g in [(225, .5), (556, .5), (441, .5)]:
        b = np.zeros(d + 1); b[0] = -g; b[d] = 1; a = np.zeros(d + 1); a[0] = 1; a[d] = -g
        acc = signal.lfilter(b, a, acc)
    return xx * (1 - wet) + acc / 6 * wet
def norm(x, peak=0.9):
    m = np.abs(x).max(); return x / m * peak if m > 0 else x
def loopify(x, xfade=2.0):
    """Make a seamless loop: crossfade the tail into the head."""
    n = int(xfade * SR); body = x[:-n].copy(); tail = x[-n:]
    w = np.linspace(0, 1, n)
    body[:n] = body[:n] * np.sqrt(w) + tail * np.sqrt(1 - w)
    return body
def place(buf, snd, at):
    i = int(at * SR); j = min(len(buf), i + len(snd))
    if i < len(buf): buf[i:j] += snd[:j - i]
def save(name, x, peak=0.9, q=4):
    path = os.path.join(OUT, name + ".ogg"); os.makedirs(os.path.dirname(path), exist_ok=True)
    x = norm(x, peak); pcm = (x * 32767).astype(np.int16)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as f: tmp = f.name
    with wave.open(tmp, "wb") as w: w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", tmp, "-c:a", "libvorbis", "-q:a", str(q), path], check=True)
    os.unlink(tmp); print("wrote", name, f"{len(x)/SR:.1f}s")

# ---------- loops ----------
def wind(dur=24, howl=False):
    n = int(dur * SR); x = pink(n)
    gust = smooth_rand(n, 0.35, 0.15, 1.0) ** 1.6
    body = lp(x, 500) * 0.9 + bp(x, 300, 1400) * 0.4 * gust
    body *= 0.35 + 0.65 * gust
    # whistle: narrow resonance gliding with the gusts
    centre = (480 if howl else 700) + (420 if howl else 300) * smooth_rand(n, 0.2)
    whistle = np.zeros(n); seg = 2048
    for i in range(0, n, seg):
        c = centre[i]; whistle[i:i + seg] = bp(x[max(0, i - 4096):i + seg], c * 0.93, c * 1.07)[-len(whistle[i:i + seg]):]
    body += whistle * (1.8 if howl else 0.8) * gust ** 2
    return loopify(body)

def forest_bed(dur=24):
    n = int(dur * SR); gust = smooth_rand(n, 0.3, 0.1, 1.0)
    rustle = hp(pink(n), 1800) * (0.15 + 0.85 * gust ** 2) * 0.5
    bed = rustle
    for _ in range(int(dur * 2.2)):     # faint distant twitter
        s = sweep(rng.uniform(.04, .09), rng.uniform(3000, 5500), rng.uniform(2500, 6500), harm=(1, .2))
        s *= env_adsr(len(s), .01, .03) * rng.uniform(.03, .09)
        place(bed, lp(s, 5000), rng.uniform(0, dur - .2))
    return loopify(bed)

def cricket_chirp(f):
    pulses = rng.integers(3, 5); out = []
    for _ in range(pulses):
        p = sweep(0.018, f, f * 0.98, harm=(1, .15)) * env_adsr(int(0.018 * SR), .003, .008)
        out += [p, np.zeros(int(0.012 * SR))]
    return np.concatenate(out)

def crickets(dur=20):
    n = int(dur * SR); buf = np.zeros(n)
    for c in range(9):
        f = rng.uniform(3900, 5200); period = rng.uniform(.45, .9); amp = rng.uniform(.25, 1.0)
        t = rng.uniform(0, period)
        while t < dur - .2:
            place(buf, cricket_chirp(f) * amp * rng.uniform(.8, 1.1), t); t += period * rng.uniform(.93, 1.07)
    buf += hp(pink(n), 3000) * 0.02    # night air
    return loopify(reverb(buf, .25, .8)[:n])

def frogs(dur=20):
    n = int(dur * SR); buf = np.zeros(n)
    for _ in range(int(dur * 1.2)):     # croakers
        f = rng.uniform(180, 320); d = rng.uniform(.25, .5); tt = t_(d)
        tone = sweep(d, f, f * .9, harm=(1, .6, .4, .2)) * (0.5 + 0.5 * np.sign(np.sin(2 * np.pi * rng.uniform(18, 32) * tt)))
        tone = bp(tone, 150, 1500) * env_adsr(len(tone), .02, .08) * rng.uniform(.3, 1)
        place(buf, tone, rng.uniform(0, dur - .6))
    for _ in range(int(dur * 4)):       # peepers
        s = sweep(.09, rng.uniform(2300, 2700), rng.uniform(2900, 3300), harm=(1, .1)) * env_adsr(int(.09 * SR), .01, .04)
        place(buf, s * rng.uniform(.1, .4), rng.uniform(0, dur - .2))
    return loopify(reverb(buf, .3, .9)[:n])

def stream(dur=20):
    n = int(dur * SR); buf = hp(pink(n), 800) * 0.08
    times = np.sort(rng.uniform(0, dur - .05, int(dur * 260)))
    for t in times:                      # bubble model: rising sine with fast decay
        f = rng.uniform(400, 2600) * rng.uniform(.8, 1.2); d = rng.uniform(.01, .04)
        tt = t_(d); fr = f * (1 + 2.5 * tt / d * 0.4)
        s = np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-tt / (d / 4))
        place(buf, s * rng.uniform(.05, .3), t)
    return loopify(lp(buf, 7000))

def ocean(dur=30):
    n = int(dur * SR); x = pink(n); env = np.zeros(n); t = 0
    while t < dur:
        p = rng.uniform(6.5, 9.5); i0, i1 = int(t * SR), min(n, int((t + p) * SR)); m = i1 - i0
        u = np.linspace(0, 1, m); env[i0:i1] = np.maximum(env[i0:i1], (u ** 1.5) * np.exp(-((u - .55) / .22) ** 2) + .12)
        t += p
    low = lp(x, 900) * env
    hiss = hp(x, 2500) * env ** 3 * 0.6
    return loopify(low + hiss)

def rain_leaves(dur=20):
    n = int(dur * SR); buf = bp(pink(n), 500, 6000) * 0.25
    clicks = np.zeros(n); idx = rng.integers(0, n, int(dur * 900)); clicks[idx] = rng.uniform(-1, 1, len(idx))
    buf += bp(clicks, 1500, 6000) * 1.2
    buf *= 0.8 + 0.2 * smooth_rand(n, .4)
    return loopify(buf)

def cave_drone(dur=24):
    n = int(dur * SR); x = brown(n)
    low = lp(x, 140, 4) * 1.0
    tt = np.arange(n) / SR
    tones = (np.sin(2 * np.pi * 55 * tt) * .25 + np.sin(2 * np.pi * 55.7 * tt) * .25 + np.sin(2 * np.pi * 82.4 * tt) * .12)
    air = bp(pink(n), 200, 900) * .12 * smooth_rand(n, .15, .3, 1)
    return loopify(reverb(low * .8 + tones * .3 + air, .5, 1.6)[:n])

# ---------- one-shots ----------
def bird(kind):
    parts = []
    if kind == 0:     # warbler: rising/falling syllables
        for _ in range(rng.integers(5, 9)):
            parts += [sweep(rng.uniform(.05, .1), rng.uniform(2500, 4000), rng.uniform(3500, 6000), .7, (1, .25)), np.zeros(int(rng.uniform(.02, .06) * SR))]
    elif kind == 1:   # trill
        f = rng.uniform(4000, 5500)
        for _ in range(rng.integers(10, 18)):
            parts += [sweep(.025, f, f * .8, 1, (1, .2)), np.zeros(int(.015 * SR))]
    elif kind == 2:   # two-note "tea-cher"
        for _ in range(rng.integers(3, 5)):
            parts += [sweep(.12, 4200, 4000, 1, (1, .3)), np.zeros(int(.04 * SR)), sweep(.12, 3300, 3100, 1, (1, .3)), np.zeros(int(.12 * SR))]
    elif kind == 3:   # pure descending whistle
        parts += [sweep(.6, 3800, 2600, .8, (1, .05)), np.zeros(int(.15 * SR)), sweep(.4, 3200, 2400, .8, (1, .05))]
    elif kind == 4:   # chip calls
        for _ in range(rng.integers(3, 6)):
            parts += [sweep(.03, 6000, 4500, 1, (1, .2)), np.zeros(int(rng.uniform(.15, .35) * SR))]
    else:             # robin-like caroling
        for _ in range(rng.integers(4, 7)):
            a, b = rng.uniform(2200, 3200), rng.uniform(2800, 3800)
            parts += [sweep(.18, a, b, 1, (1, .3, .1), vib=.03, vibf=25), np.zeros(int(.09 * SR))]
    s = np.concatenate([p * env_adsr(len(p), .005, .02) if p.any() else p for p in parts])
    return reverb(s, .18, .7)

def woodpecker():
    out = np.zeros(int(1.6 * SR)); t = 0; gap = .045
    for _ in range(rng.integers(14, 22)):
        hit = bp(white(int(.03 * SR)), 700, 1800, 3) * np.exp(-np.arange(int(.03 * SR)) / (SR * .004))
        place(out, hit, t); t += gap; gap *= 1.04
    return reverb(out, .35, 1.0)

def cuckoo():
    out = np.zeros(int(2.6 * SR))
    for i in range(2):
        for f, at in [(720, 0), (595, .32)]:
            s = sweep(.26, f * 1.02, f, 1, (1, .15, .05)) * env_adsr(int(.26 * SR), .04, .12)
            place(out, s, i * 1.1 + at)
    return reverb(lp(out, 3000), .4, 1.2)

def owl(kind):
    pat = [(0, .5), (.8, .25), (1.1, .25), (1.45, .8)] if kind == 0 else [(0, .6), (1.0, .9)]
    out = np.zeros(int(3.2 * SR))
    for at, d in pat:
        f = rng.uniform(360, 420)
        s = sweep(d, f * 1.05, f * .92, .6, (1, .1)) * env_adsr(int(d * SR), .08, .2)
        s += lp(white(len(s)), 1200) * .06 * env_adsr(len(s), .05, .2)
        place(out, s, at)
    return reverb(out, .45, 1.4)

def crow():
    out = np.zeros(int(2.2 * SR))
    for i in range(rng.integers(2, 4)):
        d = .32; f = rng.uniform(480, 620)
        saw = signal.sawtooth(2 * np.pi * np.cumsum(np.linspace(f, f * .8, int(d * SR))) / SR)
        s = bp(saw + white(len(saw)) * .5, 600, 2400, 2) * env_adsr(len(saw), .02, .1)
        place(out, s, i * .55)
    return reverb(out, .3, 1.0)

def wolf_howl():
    d = rng.uniform(2.6, 3.4); n = int(d * SR); u = np.linspace(0, 1, n)
    f = 420 + 330 * np.sin(np.pi * np.minimum(u * 1.4, 1)) ** .6 - 120 * u
    f *= 1 + .012 * np.sin(2 * np.pi * 5.5 * u * d)
    ph = 2 * np.pi * np.cumsum(f) / SR
    s = (np.sin(ph) + .35 * np.sin(2 * ph) + .12 * np.sin(3 * ph)) * env_adsr(n, .4, 1.0)
    return reverb(lp(s, 1800), .6, 1.8)       # distant

def seagull():
    out = np.zeros(int(2.0 * SR))
    for i in range(rng.integers(2, 5)):
        d = rng.uniform(.18, .3)
        s = sweep(d, rng.uniform(1900, 2400), rng.uniform(1100, 1400), .6, (1, .5, .3, .2), vib=.02, vibf=40)
        place(out, bp(s, 900, 5000) * env_adsr(len(s), .02, .06), i * rng.uniform(.25, .35))
    return reverb(out, .25, .9)

def hawk():
    d = 1.4; s = sweep(d, 3400, 2300, .7, (1, .4, .2), vib=.04, vibf=30)
    s = bp(s + white(len(s)) * .25, 1500, 6000) * env_adsr(len(s), .05, .5)
    return reverb(s, .5, 1.8)

def drip():
    f = rng.uniform(1100, 2600); d = .06; tt = t_(d)
    s = np.sin(2 * np.pi * np.cumsum(f * (1 + 1.4 * tt / d)) / SR) * np.exp(-tt / .012)
    tap = hp(white(int(.004 * SR)), 2000) * .4
    s[:len(tap)] += tap
    return reverb(np.concatenate([s, np.zeros(SR)]), .55, 1.7, .2)

def rumble():
    n = int(3.5 * SR); s = lp(brown(n), 110, 4) * env_adsr(n, 1.2, 1.8)
    return reverb(s, .5, 1.8)

def leaves_gust():
    n = int(rng.uniform(2.2, 3.2) * SR); u = np.linspace(0, 1, n)
    return hp(pink(n), 1500) * np.sin(np.pi * u) ** 2 * (0.7 + 0.3 * smooth_rand(n, 6))

def bullfrog():
    d = rng.uniform(.5, .8); n = int(d * SR); tt = np.arange(n) / SR
    s = sweep(d, 140, 115, 1, (1, .8, .5, .3)) * (0.55 + 0.45 * np.sign(np.sin(2 * np.pi * 22 * tt)))
    return reverb(bp(s, 80, 900) * env_adsr(n, .05, .2), .35, 1.0)

# ---------- turkey ----------
def gobble():
    """Rapid warbling 'gobble-obble-obble': a buzzy tone with fast pitch & amplitude flutter."""
    d = rng.uniform(.75, 1.0); n = int(d * SR); u = np.linspace(0, 1, n); tt = np.arange(n) / SR
    rate = 14 + 4 * u                                   # syllable rate speeds up
    flutter = np.sin(2 * np.pi * np.cumsum(rate) / SR)
    f = (850 - 250 * u) * (1 + .22 * flutter)
    ph = 2 * np.pi * np.cumsum(f) / SR
    tone = np.sin(ph) + .6 * np.sin(2 * ph) + .4 * np.sin(3 * ph) + .25 * np.sin(4 * ph)
    amp = (0.45 + 0.55 * np.maximum(flutter, 0) ** .7) * env_adsr(n, .03, .25)
    s = bp(tone * amp, 300, 4500) + bp(white(n), 600, 3000) * .08 * amp
    return reverb(s, .2, .8)

def turkey_cluck():
    out = np.zeros(int(.9 * SR))
    for i in range(rng.integers(1, 4)):
        s = sweep(.07, rng.uniform(1100, 1300), rng.uniform(700, 850), 1, (1, .5, .3)) * env_adsr(int(.07 * SR), .005, .04)
        place(out, bp(s, 400, 4000), i * rng.uniform(.18, .3))
    return out

def turkey_hurt():
    s = sweep(.25, 1400, 900, .6, (1, .6, .4, .3), vib=.06, vibf=35)
    return bp(s + white(len(s)) * .2, 500, 5000) * env_adsr(len(s), .01, .12)

def turkey_death():
    s = sweep(.6, 1200, 500, .8, (1, .6, .4, .2), vib=.08, vibf=22)
    return bp(s + white(len(s)) * .15, 300, 4500) * env_adsr(len(s), .01, .35)

if __name__ == "__main__":
    save("ambience/wind", wind(), .7)
    save("ambience/wind_howl", wind(howl=True), .7)
    save("ambience/forest", forest_bed(), .6)
    save("ambience/crickets", crickets(), .7)
    save("ambience/frogs", frogs(), .7)
    save("ambience/stream", stream(), .7)
    save("ambience/ocean", ocean(), .7)
    save("ambience/rain_leaves", rain_leaves(), .6)
    save("ambience/cave", cave_drone(), .7)
    for k in range(6): save(f"ambience/bird{k+1}", bird(k), .8)
    for k in range(2): save(f"ambience/woodpecker{k+1}", woodpecker(), .8)
    save("ambience/cuckoo1", cuckoo(), .8)
    for k in range(2): save(f"ambience/owl{k+1}", owl(k), .8)
    for k in range(2): save(f"ambience/crow{k+1}", crow(), .8)
    for k in range(2): save(f"ambience/wolf_howl{k+1}", wolf_howl(), .8)
    for k in range(2): save(f"ambience/seagull{k+1}", seagull(), .8)
    save("ambience/hawk1", hawk(), .8)
    for k in range(4): save(f"ambience/drip{k+1}", drip(), .8)
    for k in range(2): save(f"ambience/rumble{k+1}", rumble(), .8)
    for k in range(3): save(f"ambience/leaves{k+1}", leaves_gust(), .7)
    for k in range(2): save(f"ambience/bullfrog{k+1}", bullfrog(), .8)
    for k in range(3): save(f"turkey/gobble{k+1}", gobble(), .85)
    for k in range(3): save(f"turkey/cluck{k+1}", turkey_cluck(), .8)
    for k in range(2): save(f"turkey/hurt{k+1}", turkey_hurt(), .85)
    save("turkey/death1", turkey_death(), .85)

# ---------- Kentucky critters & songbirds ----------
def seq(parts, gap=0.0):
    out = []
    for p in parts:
        out += [p, np.zeros(int(gap * SR))]
    return np.concatenate(out)

def syl(d, f0, f1, shape=1.0, harm=(1, .25), vib=0.0, vibf=0.0, a=.008, r=.03):
    s = sweep(d, f0, f1, shape, harm, vib, vibf); return s * env_adsr(len(s), a, r)

def song_cardinal():
    cheer = [syl(.28, 4200, 1900, .6, (1, .2)) for _ in range(3)]
    birdie = [seq([syl(.07, 2400, 3600, 1), syl(.09, 3600, 2300, 1)]) for _ in range(5)]
    return reverb(seq([seq(cheer, .12), np.zeros(int(.25 * SR)), seq(birdie, .03)]), .2, .8)

def song_goldfinch():
    potato = seq([syl(.05, 3200, 3900), syl(.05, 3500, 4200), syl(.05, 3300, 4000), syl(.08, 4400, 3600)], .06)
    twitter = seq([syl(rng.uniform(.03, .06), rng.uniform(3500, 6000), rng.uniform(3000, 6500), 1, (1, .15)) for _ in range(14)], .015)
    return reverb(seq([potato, np.zeros(int(.3 * SR)), twitter]), .18, .7)

def song_robin():
    phrases = []
    for _ in range(4):
        a, b = rng.uniform(2100, 2700), rng.uniform(2900, 3600)
        phrases.append(seq([syl(.12, a, b, 1, (1, .3, .1), .02, 30), syl(.14, b, a * .95, 1, (1, .3, .1), .02, 30)], .03))
    return reverb(seq(phrases, .22), .2, .8)

def song_blue_jay():
    calls = []
    for _ in range(rng.integers(2, 4)):
        d = .32; s = sweep(d, 3200, 1700, .6, (1, .7, .5, .3))
        s = bp(s + white(len(s)) * .6, 1200, 5500) * env_adsr(len(s), .01, .12)
        calls.append(s)
    return reverb(seq(calls, .18), .25, .9)

def song_bluebird():
    w = [syl(.18, 1900, 2600, 1, (1, .2), .05, 18), syl(.12, 2600, 2200, 1, (1, .2), .05, 18), syl(.25, 2400, 3000, 1, (1, .2), .06, 16)]
    return reverb(lp(seq(w, .05), 6000), .25, .9)

def squirrel_chatter():
    clicks = []
    for _ in range(rng.integers(8, 14)):
        n = int(.025 * SR); c = bp(white(n), 2000, 6000, 3) * np.exp(-np.arange(n) / (SR * .006))
        clicks.append(c)
    whine = syl(.35, 2600, 1800, 1, (1, .4, .2), .04, 20) * .6
    return reverb(seq([seq(clicks, .07), whine], .05), .15, .6)

def raccoon_chitter():
    d = rng.uniform(.5, .8); n = int(d * SR); tt = np.arange(n) / SR
    tone = sweep(d, rng.uniform(1600, 2000), rng.uniform(2200, 2800), 1, (1, .5, .3))
    am = 0.5 + 0.5 * np.sign(np.sin(2 * np.pi * rng.uniform(26, 34) * tt))
    s = bp(tone * am + white(n) * .25 * am, 900, 5000) * env_adsr(n, .02, .15)
    return reverb(s, .15, .6)

def skunk_spray():
    n = int(.9 * SR); u = np.linspace(0, 1, n)
    hiss = bp(white(n), 900, 7000) * (np.exp(-u * 3) + .2 * (np.sin(2 * np.pi * 14 * u * .9) > .6))
    return reverb(hiss * env_adsr(n, .01, .3), .2, .7)

def opossum_hiss():
    n = int(rng.uniform(.5, .8) * SR); u = np.linspace(0, 1, n)
    s = bp(white(n), 1800, 8000) * np.sin(np.pi * u) ** .7
    return s + bp(white(n), 400, 1200) * .15 * np.sin(np.pi * u)

def mink_squeak():
    return reverb(seq([syl(.08, rng.uniform(3500, 4200), rng.uniform(4500, 5200), 1, (1, .3)) for _ in range(rng.integers(2, 4))], .05), .12, .5)

def beaver_slap():
    n = int(.6 * SR); tt = np.arange(n) / SR
    thump = np.sin(2 * np.pi * 70 * tt) * np.exp(-tt / .06)
    splash = hp(white(n), 800) * np.exp(-tt / .12) * .7
    return reverb(thump + splash, .35, 1.2)

if __name__ == "__main__":
    for k in range(2): save(f"songbird/cardinal{k+1}", song_cardinal(), .8)
    for k in range(2): save(f"songbird/goldfinch{k+1}", song_goldfinch(), .8)
    for k in range(2): save(f"songbird/robin{k+1}", song_robin(), .8)
    for k in range(2): save(f"songbird/blue_jay{k+1}", song_blue_jay(), .85)
    for k in range(2): save(f"songbird/bluebird{k+1}", song_bluebird(), .75)
    for k in range(3): save(f"critter/squirrel_chatter{k+1}", squirrel_chatter(), .8)
    for k in range(3): save(f"critter/raccoon_chitter{k+1}", raccoon_chitter(), .8)
    save("critter/skunk_spray1", skunk_spray(), .85)
    for k in range(2): save(f"critter/opossum_hiss{k+1}", opossum_hiss(), .8)
    for k in range(3): save(f"critter/mink_squeak{k+1}", mink_squeak(), .75)
    for k in range(2): save(f"critter/beaver_slap{k+1}", beaver_slap(), .9)
