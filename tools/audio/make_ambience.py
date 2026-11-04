"""Biome ambience of the dead Earth: a seamless loop and a set of one-shot "additions" per biome, synthesised with
numpy/scipy (seeded, stable). Writes the .ogg files, the sounds.json entries and the list of sound events
(AMBIENCE in ModSounds.java reads src/main/resources/intersolarsystem/ambience.txt).

    python3 tools/audio/make_ambience.py
"""
import json
from pathlib import Path

import numpy as np
from scipy.signal import butter, sosfilt

from make_sounds import ASSETS, RATE, write

ROOT = Path(__file__).resolve().parents[2]


def bp(x, lo, hi, order=2):
    sos = butter(order, [lo, hi], btype="band", fs=RATE, output="sos")
    return sosfilt(sos, x)


def lp(x, hi, order=2):
    return sosfilt(butter(order, hi, btype="low", fs=RATE, output="sos"), x)


def hp(x, lo, order=2):
    return sosfilt(butter(order, lo, btype="high", fs=RATE, output="sos"), x)


def n(seconds):
    return int(seconds * RATE)


def slow_mod(rnd, length, rate, depth, base=1.0):
    """A smooth random amplitude curve (gusts)."""
    pts = max(4, int(length / RATE * rate) + 3)
    knots = base + depth * (rnd.random(pts) * 2 - 1)
    xs = np.linspace(0, pts - 1, length)
    return np.interp(xs, np.arange(pts), knots)


def loopify(x, fade=1.5):
    """Crossfades the tail into the head so the loop has no seam."""
    f = n(fade)
    head, tail = x[:f], x[-f:]
    t = np.linspace(0, 1, f)
    out = x[:-f].copy()
    out[:f] = head * t + tail * (1 - t)
    return out


def wind(rnd, length, lo, hi, gust=0.6, whistle=None):
    noise = rnd.standard_normal(length + n(1))
    w = bp(noise, lo, hi)[n(1):] * slow_mod(rnd, length, 0.35, gust)
    if whistle:
        t = np.arange(length) / RATE
        f = whistle * (1 + 0.08 * np.sin(2 * np.pi * 0.07 * t + rnd.random() * 6))
        w += 0.05 * np.sin(2 * np.pi * np.cumsum(f) / RATE) * slow_mod(rnd, length, 0.2, 0.9, 0.5).clip(0)
    return w


def clicks(rnd, length, rate, tone=3000, decay=0.003):
    """Poisson clicks (Geiger counter, ice ticking, insects)."""
    out = np.zeros(length)
    k = rnd.poisson(rate * length / RATE)
    env = np.exp(-np.arange(n(0.02)) / (decay * RATE))
    click = env * np.sin(2 * np.pi * tone * np.arange(len(env)) / RATE)
    for pos in rnd.integers(0, length - len(click), k):
        out[pos:pos + len(click)] += click * (0.5 + rnd.random())
    return out


def bubbles(rnd, length, rate):
    out = np.zeros(length)
    for pos in rnd.integers(0, length - n(0.2), rnd.poisson(rate * length / RATE)):
        d = n(0.04 + rnd.random() * 0.12)
        t = np.arange(d) / RATE
        f0 = 180 + rnd.random() * 500
        f = f0 * (1 + 3 * t / t[-1])
        out[pos:pos + d] += np.sin(2 * np.pi * np.cumsum(f) / RATE) * np.exp(-t * 30) * (0.3 + rnd.random())
    return out


def creak(rnd, seconds_=1.6, base=90):
    """A long metal groan: a rough sawtooth whose pitch wanders, through resonances."""
    length = n(seconds_)
    t = np.arange(length) / RATE
    f = base * (1 + 0.25 * np.sin(2 * np.pi * (0.4 + rnd.random()) * t) + 0.1 * rnd.standard_normal(length).cumsum()
                / np.sqrt(length) * 0)
    phase = np.cumsum(f) / RATE
    saw = 2 * (phase % 1) - 1
    jitter = 1 + 0.4 * lp(rnd.standard_normal(length), 30)
    x = bp(saw * jitter, 120, 1600) + 0.5 * bp(saw, 600, 900)
    env = np.minimum(1, t / 0.2) * np.minimum(1, (t[-1] - t) / 0.5)
    return x * env


def crack(rnd, ring=2400, decay=0.15):
    length = n(0.9)
    t = np.arange(length) / RATE
    x = rnd.standard_normal(length) * np.exp(-t * 60)
    x += 0.6 * np.sin(2 * np.pi * ring * t) * np.exp(-t / decay) * (1 + 0.3 * np.sin(2 * np.pi * 7 * t))
    x += 0.4 * np.sin(2 * np.pi * ring * 1.47 * t) * np.exp(-t / (decay * 0.6))
    return hp(x, 300)


def rumble(rnd, seconds_=2.5):
    length = n(seconds_)
    t = np.arange(length) / RATE
    x = lp(rnd.standard_normal(length), 140, 4) * 6
    x += 0.3 * clicks(rnd, length, 30, 900, 0.01)
    return x * np.minimum(1, t / 0.3) * np.exp(-np.maximum(0, t - 0.6) * 1.4)


def clatter(rnd, seconds_=1.8):
    """Scrap sliding down a heap: metallic hits with rings, settling."""
    length = n(seconds_)
    out = np.zeros(length)
    k = 18
    for i in range(k):
        pos = int((i / k) ** 1.6 * (length - n(0.4)))
        d = n(0.35)
        t = np.arange(d) / RATE
        f = 400 + rnd.random() * 2600
        hit = (np.sin(2 * np.pi * f * t) + 0.5 * np.sin(2 * np.pi * f * 2.76 * t)) * np.exp(-t * (12 + rnd.random() * 25))
        out[pos:pos + d] += hit * (1 - i / k * 0.7)
    return out + 0.2 * lp(rnd.standard_normal(length), 400) * np.exp(-np.arange(length) / RATE * 2)


def chirp(rnd):
    """A small bird: quick FM sweeps."""
    length = n(0.7)
    out = np.zeros(length)
    for k in range(rnd.integers(2, 5)):
        pos = n(0.12 * k)
        d = n(0.08)
        t = np.arange(d) / RATE
        f = 3000 + 1500 * np.sin(np.pi * t / t[-1]) + rnd.random() * 600
        out[pos:pos + d] += np.sin(2 * np.pi * np.cumsum(f) / RATE) * np.sin(np.pi * t / t[-1])
    return out


def gurgle(rnd):
    return bubbles(rnd, n(1.5), 14) + 0.3 * lp(rnd.standard_normal(n(1.5)), 200)


def thump_loop(rnd, length, period=2.2):
    """A distant machine: a low thump every period seconds, with a squeak."""
    out = np.zeros(length)
    t = np.arange(n(0.5)) / RATE
    thump = np.sin(2 * np.pi * 55 * t) * np.exp(-t * 9)
    for pos in range(0, length - len(t), n(period)):
        out[pos:pos + len(t)] += thump
    return lp(out, 300)


def make(seed, fn):
    rnd = np.random.default_rng(seed)
    return fn(rnd)


L = n(18)
BIOMES = {
    # biome: (loop maker, [addition makers], tick chance of an addition)
    "ash_desert": (lambda r: wind(r, L, 70, 700, 0.7),
                   [lambda r: wind(r, n(3), 200, 2500, 0.9) * np.hanning(n(3)), lambda r: creak(r, 2.0, 70)], 0.004),
    "dried_seabed": (lambda r: wind(r, L, 300, 3000, 0.5) * 0.7 + 0.3 * wind(r, L, 60, 300, 0.4),
                     [lambda r: wind(r, n(2.5), 1500, 6000, 0.9) * np.hanning(n(2.5)), lambda r: crack(r, 1800, 0.06)],
                     0.003),
    "petrified_forest": (lambda r: wind(r, L, 150, 1200, 0.5, whistle=620),
                         [lambda r: crack(r, 900, 0.3), lambda r: creak(r, 1.2, 140)], 0.003),
    "irradiated_crater": (lambda r: 0.25 * wind(r, L, 80, 400, 0.4) + 0.5 * clicks(r, L, 2.5)
                          + 0.06 * np.sin(2 * np.pi * 120 * np.arange(L) / RATE),
                          [lambda r: clicks(r, n(1.8), 90) * np.hanning(n(1.8))], 0.006),
    "frozen_megalopolis": (lambda r: wind(r, L, 200, 1400, 0.8, whistle=480),
                           [lambda r: crack(r, 2600, 0.25), lambda r: creak(r, 2.4, 55), lambda r: rumble(r, 3.0)],
                           0.005),
    "acid_swamp": (lambda r: 0.3 * wind(r, L, 80, 500, 0.3) + bubbles(r, L, 3) + 0.15 * clicks(r, L, 6, 5200, 0.002),
                   [lambda r: gurgle(r)], 0.006),
    "junk_mountains": (lambda r: wind(r, L, 100, 900, 0.7) + 0.2 * clicks(r, L, 1.2, 1800, 0.02),
                       [lambda r: clatter(r), lambda r: creak(r, 1.8, 80)], 0.005),
    "salt_flats": (lambda r: wind(r, L, 600, 4000, 0.4, whistle=1350) * 0.6,
                   [lambda r: crack(r, 3800, 0.05)], 0.003),
    "deep_canyons": (lambda r: wind(r, L, 50, 420, 0.8, whistle=260),
                     [lambda r: rumble(r, 3.5), lambda r: clatter(r, 1.2) * 0.5], 0.003),
    "oasis": (lambda r: 0.4 * wind(r, L, 150, 1500, 0.3) + 0.35 * bubbles(r, L, 1.5)
              + 0.25 * clicks(r, L, 40, 4600, 0.004) * slow_mod(r, L, 0.5, 0.8, 0.6).clip(0),
              [lambda r: chirp(r), lambda r: chirp(r)], 0.008),
    "abandoned_farmland": (lambda r: wind(r, L, 400, 3500, 0.6) * 0.8,
                           [lambda r: creak(r, 1.4, 160), lambda r: creak(r, 0.9, 210)], 0.003),
    "oil_field": (lambda r: 0.6 * thump_loop(r, L) + 0.4 * wind(r, L, 80, 600, 0.5),
                  [lambda r: gurgle(r), lambda r: clatter(r, 0.8) * 0.6], 0.004),
    # Mars: the thin air carries little; a faint high hiss of dust and the far whistle of a dust devil.
    "mars": (lambda r: 0.35 * wind(r, L, 900, 5000, 0.5) + 0.25 * wind(r, L, 120, 500, 0.6, whistle=330),
             [lambda r: wind(r, n(4), 300, 4000, 1.0) * np.hanning(n(4)), lambda r: crack(r, 1500, 0.08) * 0.4],
             0.003),
}


def events():
    """Every ambience sound event, in order (the Java side registers these names)."""
    out = []
    for biome, (_, adds, _) in BIOMES.items():
        out.append(f"ambient.{biome}.loop")
        if adds:
            out.append(f"ambient.{biome}.additions")
    return out


def main():
    index_path = ASSETS / "sounds.json"
    index = json.loads(index_path.read_text())
    for i, (biome, (loop, adds, _)) in enumerate(BIOMES.items()):
        name = f"ambience/{biome}_loop"
        write(name, loopify(make(1000 + i, loop)), peak=0.5)
        index[f"ambient.{biome}.loop"] = {"sounds": [{"name": "intersolarsystem:" + name, "stream": True}]}
        files = []
        for k, add in enumerate(adds):
            fname = f"ambience/{biome}_add{k + 1}"
            write(fname, make(2000 + i * 10 + k, add), peak=0.7)
            files.append({"name": "intersolarsystem:" + fname, "volume": 0.6})
        index[f"ambient.{biome}.additions"] = {"subtitle": f"subtitles.intersolarsystem.ambient.{biome}",
                                               "sounds": files}
    index_path.write_text(json.dumps(index, indent=2) + "\n")
    lst = ROOT / "src/main/resources/intersolarsystem/ambience.txt"
    lst.parent.mkdir(parents=True, exist_ok=True)
    lst.write_text("\n".join(events()) + "\n")
    print("ambience written:", len(BIOMES), "biomes")


if __name__ == "__main__":
    main()
