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


SOUNDS = {
    "suit_breath": [("suit_breath1", lambda: breath(1)), ("suit_breath2", lambda: breath(2))],
    "oxygen_alarm": [("oxygen_alarm", alarm)],
    "suffocate": [("suffocate1", lambda: gasp(3)), ("suffocate2", lambda: gasp(4))],
    "helmet_blocked": [("helmet_blocked", lambda: helmet_clunk(5))],
    "tape_start": [("tape_start", lambda: tape_click(6))],
    "tape_static": [("tape_static1", lambda: radio_static(7)), ("tape_static2", lambda: radio_static(8))],
}

SUBTITLES = {
    "suit_breath": "subtitles.intersolarsystem.suit_breath",
    "oxygen_alarm": "subtitles.intersolarsystem.oxygen_alarm",
    "suffocate": "subtitles.intersolarsystem.suffocate",
    "helmet_blocked": "subtitles.intersolarsystem.helmet_blocked",
    "tape_start": "subtitles.intersolarsystem.tape_start",
    "tape_static": "subtitles.intersolarsystem.tape_static",
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
