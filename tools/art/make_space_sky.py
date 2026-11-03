"""The deep sky: the Milky Way band (8 segments), coloured nebulae and dense star fields, painted with value noise.
They are drawn additively in space (black = nothing), see SkyRendererMixin#drawSpace.

    python3 tools/art/make_space_sky.py
"""
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/intersolarsystem/textures/environment/celestial/space"
N = 256


def noise(seed, size, octaves=6, base=4):
    rnd = np.random.default_rng(seed)
    out = np.zeros((size, size))
    amp, total = 1.0, 0.0
    for o in range(octaves):
        cells = base * 2 ** o
        grid = rnd.random((cells + 1, cells + 1))
        xs = np.linspace(0, cells, size, endpoint=False)
        i = xs.astype(int)
        f = xs - i
        f = f * f * (3 - 2 * f)
        a = grid[np.ix_(i, i)]
        b = grid[np.ix_(i, i + 1)]
        c = grid[np.ix_(i + 1, i)]
        d = grid[np.ix_(i + 1, i + 1)]
        fx = f[None, :]
        fy = f[:, None]
        layer = (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy
        out += layer * amp
        total += amp
        amp *= 0.5
    return out / total


def stars(seed, size, count, colours=True):
    rnd = np.random.default_rng(seed)
    img = np.zeros((size, size, 3))
    for _ in range(count):
        x, y = rnd.integers(0, size, 2)
        b = rnd.random() ** 3
        t = rnd.random()
        col = np.array([1.0, 0.85, 0.7]) if t < 0.25 else np.array([0.7, 0.8, 1.0]) if t < 0.5 else np.array([1, 1, 1.0])
        if not colours:
            col = np.ones(3)
        img[y, x] += col * (0.35 + 0.65 * b)
        if b > 0.85:
            for (dx, dy) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                img[(y + dy) % size, (x + dx) % size] += col * 0.35
    return img


def save(arr, name):
    OUT.mkdir(parents=True, exist_ok=True)
    a = np.clip(arr * 255, 0, 255).astype(np.uint8)
    Image.fromarray(a, "RGB").save(OUT / (name + ".png"))


def milky_way():
    """One long band, cut into 8 square segments that are laid end to end around the sky."""
    w = N * 8
    # One continuous noise field for the whole band (no seams between the segments).
    n = noise(100, w, 8, 12)[:N, :]
    lanes = noise(300, w, 7, 40)[:N, :]
    big = noise(5, N, 5, 2)
    ys = np.linspace(-1, 1, N)[:, None]
    for i in range(8):
        seg = n[:, i * N:(i + 1) * N]
        xs = np.linspace(i / 8, (i + 1) / 8, N)[None, :]
        wobble = 0.15 * np.sin(xs * 12.0) + 0.1 * np.sin(xs * 31.0 + 1.0)
        width = 0.35 + 0.15 * np.sin(xs * 6.28 * 2)
        core = np.exp(-((ys - wobble) / width) ** 2 * 2.5)
        density = core * (0.35 + 0.9 * seg)
        dust = np.clip((lanes[:, i * N:(i + 1) * N] - 0.55) * 4, 0, 1) * core  # dark dust lanes
        glow = np.clip(density - dust * 0.8, 0, 1)
        col = np.stack([glow * 0.95, glow * 0.88, glow * 0.80], axis=-1) * 0.55
        col[..., 2] += np.clip(big[:, :] - 0.5, 0, 1)[:, :] * core * 0.25
        col += stars(400 + i, N, int(900 + 1600 * float(core.mean())), True) * (0.4 + 0.6 * core[..., None])
        save(col, "milky_way_%d" % i)


def nebulae():
    tints = [((0.9, 0.25, 0.35), (0.3, 0.2, 0.8)), ((0.2, 0.6, 0.9), (0.6, 0.2, 0.7)),
             ((0.95, 0.55, 0.2), (0.8, 0.2, 0.3)), ((0.3, 0.9, 0.6), (0.2, 0.4, 0.9))]
    yy, xx = np.mgrid[-1:1:N * 1j, -1:1:N * 1j]
    r = np.sqrt(xx ** 2 + yy ** 2)
    for i, (a, b) in enumerate(tints):
        n1 = noise(500 + i, N, 7, 3)
        n2 = noise(600 + i, N, 6, 5)
        fall = np.clip(1 - r, 0, 1) ** 1.6
        cloud = np.clip(n1 * 1.6 - 0.45, 0, 1) * fall
        wisps = np.clip(n2 * 1.8 - 0.8, 0, 1) * fall
        col = cloud[..., None] * np.array(a) * 0.8 + wisps[..., None] * np.array(b) * 0.9
        col += stars(700 + i, N, 220) * fall[..., None]
        save(col, "nebula_%d" % i)


def star_fields():
    for i in range(4):
        save(stars(800 + i, N, 1400) * 0.9, "stars_%d" % i)


def dust():
    yy, xx = np.mgrid[-1:1:64j, -1:1:64j]
    streak = np.exp(-(xx / 0.08) ** 2) * np.clip(1 - np.abs(yy), 0, 1) ** 2
    col = streak[..., None] * np.array([0.8, 0.85, 0.95])
    a = np.clip(col * 255, 0, 255).astype(np.uint8)
    OUT.mkdir(parents=True, exist_ok=True)
    Image.fromarray(a, "RGB").save(OUT / "dust.png")


if __name__ == "__main__":
    milky_way()
    nebulae()
    star_fields()
    dust()
    print("space sky written")
