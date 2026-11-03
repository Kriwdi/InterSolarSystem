"""Synthesises the mod's sound effects (mono Ogg Vorbis) with numpy and ffmpeg. Seeded noise, stable output.

    python3 tools/audio/make_sounds.py
"""
import json
import subprocess
import tempfile
import wave
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/intersolarsystem"
OUT = ASSETS / "sounds"
RATE = 32000


def lowpass(x, cutoff):
    """One-pole low-pass filter (cutoff in Hz)."""
    a = np.exp(-2 * np.pi * cutoff / RATE)
    y = np.zeros_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc = (1 - a) * v + a * acc
        y[i] = acc
    return y


def bandpass(x, lo, hi):
    return lowpass(x, hi) - lowpass(x, lo)


def envelope(n, attack, release, curve=2.0):
    t = np.linspace(0, 1, n)
    env = np.ones(n)
    a = int(attack * n)
    r = int(release * n)
    if a:
        env[:a] = np.linspace(0, 1, a) ** curve
    if r:
        env[-r:] = np.linspace(1, 0, r) ** curve
    return env


def write(name, samples, peak=0.8):
    samples = samples / (np.max(np.abs(samples)) + 1e-9) * peak
    OUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / "s.wav"
        with wave.open(str(wav), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes((samples * 32767).astype(np.int16).tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libvorbis", "-q:a", "4",
                        str(OUT / (name + ".ogg"))], check=True)


def seconds(s):
    return int(s * RATE)


def breath(seed):
    """Breathing inside a helmet: a filtered inhale, a pause, a longer exhale, with the hiss of the regulator."""
    rnd = np.random.default_rng(seed)
    inhale = bandpass(rnd.normal(size=seconds(0.9)), 300, 1800) * envelope(seconds(0.9), 0.5, 0.3)
    pause = np.zeros(seconds(0.25))
    exhale = bandpass(rnd.normal(size=seconds(1.3)), 150, 1100) * envelope(seconds(1.3), 0.15, 0.6) * 0.8
    hiss = bandpass(rnd.normal(size=seconds(0.35)), 3000, 7000) * envelope(seconds(0.35), 0.1, 0.8) * 0.25
    body = np.concatenate([inhale, pause, exhale, np.zeros(seconds(0.2))])
    body[seconds(0.9) + seconds(0.25): seconds(0.9) + seconds(0.25) + len(hiss)] += hiss
    return body


def alarm():
    """Low oxygen: two descending beeps, a little square-ish."""
    out = []
    for f in (1320, 990):
        t = np.arange(seconds(0.16)) / RATE
        tone = np.sign(np.sin(2 * np.pi * f * t)) * 0.4 + np.sin(2 * np.pi * f * t) * 0.6
        out.append(lowpass(tone, 5000) * envelope(len(t), 0.05, 0.2, 1.0))
        out.append(np.zeros(seconds(0.07)))
    return np.concatenate(out)


def gasp(seed):
    """Choking for air: a short ragged intake that breaks off, with a throat rattle."""
    rnd = np.random.default_rng(seed)
    n = seconds(0.55)
    noise = bandpass(rnd.normal(size=n), 500, 2600)
    t = np.arange(n) / RATE
    rattle = 0.5 + 0.5 * np.sign(np.sin(2 * np.pi * 28 * t))
    env = envelope(n, 0.1, 0.5) * (0.6 + 0.4 * rattle)
    return noise * env


def helmet_clunk(seed):
    """Food against the visor: a dull plastic knock with a short ring."""
    rnd = np.random.default_rng(seed)
    n = seconds(0.3)
    t = np.arange(n) / RATE
    knock = lowpass(rnd.normal(size=n), 900) * np.exp(-t * 40)
    ring = np.sin(2 * np.pi * 410 * t) * np.exp(-t * 18) * 0.4
    return knock + ring


def tape_click(seed):
    """A tape deck: a mechanical clunk and a short motor whine."""
    rnd = np.random.default_rng(seed)
    n = seconds(0.35)
    t = np.arange(n) / RATE
    clunk = lowpass(rnd.normal(size=n), 1500) * np.exp(-t * 35)
    whine = np.sin(2 * np.pi * (180 + 300 * t) * t) * np.exp(-t * 6) * 0.25
    return clunk + whine


def radio_static(seed):
    """A burst of radio static with a carrier tone fading in and out."""
    rnd = np.random.default_rng(seed)
    n = seconds(0.9)
    t = np.arange(n) / RATE
    hiss = bandpass(rnd.normal(size=n), 900, 5000) * (0.6 + 0.4 * np.sin(2 * np.pi * 7 * t))
    carrier = np.sin(2 * np.pi * 1180 * t) * 0.15
    return (hiss + carrier) * envelope(n, 0.1, 0.4)


def hiss(seed):
    """A suit puncture: sharp air escaping through a small hole, wavering."""
    rnd = np.random.default_rng(seed)
    n = seconds(0.8)
    t = np.arange(n) / RATE
    noise = bandpass(rnd.normal(size=n), 2500, 9000) * (0.7 + 0.3 * np.sin(2 * np.pi * 11 * t))
    return noise * envelope(n, 0.05, 0.4)


def heartbeat():
    """Lub-dub: two low thumps."""
    out = []
    for f, amp, gap in ((52, 1.0, 0.12), (46, 0.7, 0.35)):
        n = seconds(0.16)
        t = np.arange(n) / RATE
        out.append(np.sin(2 * np.pi * f * t) * np.exp(-t * 22) * amp)
        out.append(np.zeros(seconds(gap)))
    return np.concatenate(out)


def ear_ring():
    """Tinnitus after pressure loss: a high tone that beats and fades, over a muffled rush."""
    rnd = np.random.default_rng(9)
    n = seconds(1.6)
    t = np.arange(n) / RATE
    tone = np.sin(2 * np.pi * 3900 * t) * 0.5 + np.sin(2 * np.pi * 3907 * t) * 0.5
    rush = lowpass(rnd.normal(size=n), 400) * 0.6
    return (tone * 0.35 + rush) * envelope(n, 0.05, 0.7)


def voice(seed, pitch):
    """Garbled radio speech: syllables of a buzzing glottal source shaped by two vowel formants, a wandering pitch,
    gaps between words, squeezed through a radio band and clipped. Not words, but unmistakably someone talking."""
    rnd = np.random.default_rng(seed)
    vowels = [(730, 1090), (270, 2290), (530, 1840), (570, 840), (300, 870), (660, 1700)]
    parts = []
    for _ in range(rnd.integers(5, 8)):
        n = seconds(rnd.uniform(0.09, 0.2))
        t = np.arange(n) / RATE
        f0 = pitch * (1 + 0.15 * np.sin(2 * np.pi * rnd.uniform(1, 3) * t + rnd.uniform(0, 6)))
        phase = np.cumsum(f0) / RATE
        source = (phase % 1.0) * 2 - 1  # sawtooth: the vocal cords
        f1, f2 = vowels[rnd.integers(len(vowels))]
        v = bandpass(source, f1 * 0.8, f1 * 1.2) + 0.6 * bandpass(source, f2 * 0.85, f2 * 1.15)
        cons = bandpass(rnd.normal(size=seconds(0.03)), 2500, 6000) * 0.4  # a consonant
        parts.append(np.concatenate([cons, v * envelope(n, 0.2, 0.3)]))
        if rnd.random() < 0.3:
            parts.append(np.zeros(seconds(rnd.uniform(0.05, 0.12))))
    x = np.concatenate(parts)
    x = bandpass(x, 300, 3200)
    x = np.tanh(x / (np.max(np.abs(x)) + 1e-9) * 2.5)  # radio overdrive
    x += bandpass(rnd.normal(size=len(x)), 1000, 5000) * 0.12
    return x


SOUNDS = {
    "suit_breath": [("suit_breath1", lambda: breath(1)), ("suit_breath2", lambda: breath(2))],
    "oxygen_alarm": [("oxygen_alarm", alarm)],
    "suffocate": [("suffocate1", lambda: gasp(3)), ("suffocate2", lambda: gasp(4))],
    "helmet_blocked": [("helmet_blocked", lambda: helmet_clunk(5))],
    "tape_start": [("tape_start", lambda: tape_click(6))],
    "tape_static": [("tape_static1", lambda: radio_static(7)), ("tape_static2", lambda: radio_static(8))],
    "suit_leak": [("suit_leak", lambda: hiss(10))],
    "heartbeat": [("heartbeat", heartbeat)],
    "ear_ring": [("ear_ring", ear_ring)],
    "tape_voice": [("tape_voice%d" % i, (lambda i=i: voice(20 + i, [110, 140, 175, 210, 125, 190][i - 1])))
                   for i in range(1, 7)],
}

SUBTITLES = {
    "suit_breath": "subtitles.intersolarsystem.suit_breath",
    "oxygen_alarm": "subtitles.intersolarsystem.oxygen_alarm",
    "suffocate": "subtitles.intersolarsystem.suffocate",
    "helmet_blocked": "subtitles.intersolarsystem.helmet_blocked",
    "tape_start": "subtitles.intersolarsystem.tape_start",
    "tape_static": "subtitles.intersolarsystem.tape_static",
    "suit_leak": "subtitles.intersolarsystem.suit_leak",
    "heartbeat": "subtitles.intersolarsystem.heartbeat",
    "ear_ring": "subtitles.intersolarsystem.ear_ring",
    "tape_voice": "subtitles.intersolarsystem.tape_voice",
}


def main():
    index_path = ASSETS / "sounds.json"
    index = json.loads(index_path.read_text()) if index_path.exists() else {}
    for event, files in SOUNDS.items():
        for name, make in files:
            write(name, make())
        index[event] = {"subtitle": SUBTITLES[event], "sounds": ["intersolarsystem:" + f for f, _ in files]}
    index_path.write_text(json.dumps(index, indent=2) + "\n")
    print("sounds written:", ", ".join(SOUNDS))


if __name__ == "__main__":
    main()
