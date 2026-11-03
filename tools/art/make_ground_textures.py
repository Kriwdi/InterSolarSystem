"""Painted ground seen from the ship's windows: seamless aerial views (512 px) of every dead-Earth biome and of the
Moon, drawn with periodic noise so they can be tiled. They live in the celestial atlas next to the Earth's phases.

    python3 tools/art/make_ground_textures.py
"""
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/intersolarsystem/textures/environment/celestial/ground"
N = 512


def noise(seed, power=2.0, low=2):
    """Periodic fractal noise in 0..1 (random spectrum with a 1/f^power falloff)."""
    rng = np.random.default_rng(seed)
    f = np.fft.fftfreq(N) * N
    fx, fy = np.meshgrid(f, f)
    r = np.sqrt(fx ** 2 + fy ** 2)
    r[0, 0] = 1
    amp = 1.0 / r ** power
    amp[r < low] = 0
    phase = rng.uniform(0, 2 * np.pi, (N, N))
    img = np.real(np.fft.ifft2(amp * np.exp(1j * phase)))
    img -= img.min()
    return img / img.max()


def cells(seed, count):
    """Periodic Voronoi: distance to the nearest and second nearest point (for cracks and plates)."""
    rng = np.random.default_rng(seed)
    pts = rng.uniform(0, N, (count, 2))
    yy, xx = np.mgrid[0:N, 0:N]
    d1 = np.full((N, N), 1e9)
    d2 = np.full((N, N), 1e9)
    for px, py in pts:
        for ox in (-N, 0, N):
            for oy in (-N, 0, N):
                d = np.hypot(xx - px - ox, yy - py - oy)
                closer = d < d1
                d2 = np.where(closer, d1, np.minimum(d2, d))
                d1 = np.where(closer, d, d1)
    return d1, d2


def palette(t, stops):
    """Maps 0..1 onto a list of (position, (r, g, b)) colour stops."""
    out = np.zeros(t.shape + (3,))
    pos = [p for p, _ in stops]
    cols = np.array([c for _, c in stops], dtype=float)
    for ch in range(3):
        out[..., ch] = np.interp(t, pos, cols[:, ch])
    return out


def shade(rgb, light):
    return np.clip(rgb * light[..., None], 0, 255)


def relief(height, strength=3.0):
    """Light from the north-west: brighter where the height rises towards the light."""
    gx = np.roll(height, -1, 1) - np.roll(height, 1, 1)
    gy = np.roll(height, -1, 0) - np.roll(height, 1, 0)
    return np.clip(1.0 - (gx + gy) * strength * N / 64, 0.55, 1.35)


def dots(img, seed, count, radius, colour, shadow=None):
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:N, 0:N]
    for _ in range(count):
        x, y = rng.uniform(0, N, 2)
        r = rng.uniform(radius * 0.6, radius * 1.3)
        for ox in (-N, 0, N):
            for oy in (-N, 0, N):
                if shadow is not None:
                    m = np.hypot(xx - x - ox - r * 0.6, yy - y - oy - r * 0.6) < r
                    img[m] = img[m] * 0.6
                m = np.hypot(xx - x - ox, yy - y - oy) < r
                img[m] = colour
    return img


def save(name, rgb):
    OUT.mkdir(parents=True, exist_ok=True)
    Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA").save(OUT / (name + ".png"))


def ash_desert():
    h = noise(11, 2.4) * 0.7 + 0.3 * (0.5 + 0.5 * np.sin(np.linspace(0, 14 * np.pi, N))[None, :] * noise(12, 1.6))
    rgb = palette(h, [(0, (95, 88, 80)), (0.5, (150, 140, 126)), (1, (190, 180, 165))])
    rgb = shade(rgb, relief(h, 2.5))
    cinder = noise(13, 1.8) > 0.72
    rgb[cinder] = rgb[cinder] * 0.55
    return dots(rgb, 14, 40, 3, (70, 64, 58), shadow=True)


def dried_seabed():
    d1, d2 = cells(21, 160)
    crack = (d2 - d1) < 1.6
    h = noise(22, 2.2)
    rgb = palette(h, [(0, (150, 128, 98)), (0.6, (182, 160, 124)), (1, (205, 190, 160))])
    rgb = shade(rgb, 0.92 + 0.12 * (d1 / d1.max()))
    rgb[crack] = (88, 72, 55)
    salt = noise(23, 2.0) > 0.75
    rgb[salt] = rgb[salt] * 0.4 + np.array([235, 232, 222]) * 0.6
    return rgb


def petrified_forest():
    h = noise(31, 2.1)
    rgb = palette(h, [(0, (88, 72, 58)), (0.5, (118, 100, 82)), (1, (140, 128, 112))])
    rgb = shade(rgb, relief(h, 2))
    rgb = dots(rgb, 32, 140, 2.5, (60, 50, 44), shadow=True)
    return dots(rgb, 33, 60, 1.5, (170, 160, 150))


def irradiated_crater():
    h = noise(41, 2.2)
    yy, xx = np.mgrid[0:N, 0:N]
    ang = np.arctan2(yy - N / 2, xx - N / 2)
    rays = 0.5 + 0.5 * np.sin(ang * 23 + noise(42, 1.5) * 6)
    rgb = palette(h * 0.6 + rays * 0.4, [(0, (92, 98, 60)), (0.5, (135, 132, 80)), (1, (170, 160, 104))])
    rgb = shade(rgb, relief(h, 2.5))
    glow = noise(43, 1.7) > 0.8
    rgb[glow] = (120, 230, 110)
    return rgb


def frozen_megalopolis():
    h = noise(51, 2.0)
    rgb = palette(h, [(0, (200, 205, 212)), (1, (238, 241, 245))])
    rng = np.random.default_rng(52)
    block = 64
    for bx in range(0, N, block):
        for by in range(0, N, block):
            # Streets around each city block, buildings (with shadows) inside.
            rgb[by:by + 6, bx:bx + block] = (95, 100, 108)
            rgb[by:by + block, bx:bx + 6] = (95, 100, 108)
            for _ in range(rng.integers(2, 5)):
                w, d = rng.integers(10, 26, 2)
                x, y = bx + rng.integers(8, block - w - 2), by + rng.integers(8, block - d - 2)
                tone = rng.integers(150, 215)
                rgb[y + 4:y + d + 4, x + 4:x + w + 4] = rgb[y + 4:y + d + 4, x + 4:x + w + 4] * 0.55
                rgb[y:y + d, x:x + w] = (tone, tone + 4, tone + 10)
                if rng.random() < 0.3:
                    rgb[y + d // 3:y + d // 3 + 4, x:x + w] = (60, 62, 70)  # a collapsed roof
    return rgb


def acid_swamp():
    h = noise(61, 2.3)
    rgb = palette(h, [(0, (58, 62, 34)), (0.5, (86, 92, 48)), (1, (110, 112, 66))])
    pools = noise(62, 2.0) > 0.66
    rgb[pools] = palette(noise(63, 1.6)[pools], [(0, (110, 190, 40)), (1, (170, 230, 80))])
    return dots(rgb, 64, 70, 2, (40, 36, 30), shadow=True)


def junk_mountains():
    h = noise(71, 2.0)
    rgb = palette(h, [(0, (80, 74, 70)), (0.5, (128, 118, 108)), (1, (160, 152, 140))])
    rgb = shade(rgb, relief(h, 3.5))
    rng = np.random.default_rng(72)
    speck = rng.random((N, N))
    colours = np.array([[150, 70, 40], [60, 80, 120], [180, 170, 60], [200, 200, 200], [40, 40, 40]])
    pick = rng.integers(0, len(colours), (N, N))
    m = speck < 0.08
    rgb[m] = colours[pick[m]]
    return rgb


def salt_flats():
    d1, d2 = cells(81, 220)
    crack = (d2 - d1) < 1.2
    h = noise(82, 2.0)
    rgb = palette(h, [(0, (222, 222, 216)), (1, (246, 246, 242))])
    rgb[crack] = (178, 176, 170)
    lith = noise(83, 2.2) > 0.78
    rgb[lith] = rgb[lith] * 0.6 + np.array([226, 170, 190]) * 0.4
    return rgb


def deep_canyons():
    h = noise(91, 2.5)
    bands = 0.5 + 0.5 * np.sin(h * 30)
    rgb = palette(bands * 0.5 + h * 0.5, [(0, (120, 62, 36)), (0.4, (176, 96, 52)), (0.7, (206, 140, 88)),
                                          (1, (226, 176, 120))])
    rgb = shade(rgb, relief(h, 4))
    gorge = np.abs(noise(92, 2.6) - 0.5) < 0.025
    rgb[gorge] = (52, 30, 22)
    return rgb


def oasis():
    h = noise(101, 2.2)
    rgb = palette(h, [(0, (48, 96, 36)), (0.5, (74, 138, 52)), (1, (110, 168, 70))])
    water = noise(102, 2.6) > 0.72
    rgb[water] = palette(noise(103, 1.5)[water], [(0, (40, 90, 170)), (1, (80, 140, 210))])
    rgb = dots(rgb, 104, 160, 4, (34, 76, 30), shadow=True)
    return dots(rgb, 105, 50, 1.2, (240, 220, 90))


def abandoned_farmland():
    rgb = np.zeros((N, N, 3))
    rng = np.random.default_rng(111)
    field = 128
    tones = [(150, 132, 84), (170, 150, 92), (128, 112, 74), (182, 168, 112), (110, 98, 70)]
    for fx in range(0, N, field):
        for fy in range(0, N, field):
            tone = np.array(tones[rng.integers(len(tones))])
            rows = 0.92 + 0.08 * np.sin(np.arange(field) * (0.8 if rng.random() < 0.5 else 0.0) + 0.5)
            patch = tone[None, None, :] * rows[:, None, None] if rng.random() < 0.5 else \
                tone[None, None, :] * rows[None, :, None]
            rgb[fy:fy + field, fx:fx + field] = patch
            rgb[fy:fy + 3, fx:fx + field] = (96, 84, 64)
            rgb[fy:fy + field, fx:fx + 3] = (96, 84, 64)
    rgb = shade(rgb, 0.85 + 0.3 * noise(112, 2.0))
    return dots(rgb, 113, 20, 3, (190, 170, 90), shadow=True)


def oil_field():
    h = noise(121, 2.1)
    rgb = palette(h, [(0, (44, 40, 36)), (0.5, (72, 64, 56)), (1, (98, 88, 76))])
    tar = noise(122, 2.4) > 0.7
    rgb[tar] = (18, 16, 14)
    for x in range(32, N, 128):
        for y in range(48, N, 128):
            rgb[y:y + 14, x:x + 14] = (120, 112, 100)
            rgb[y + 5:y + 9, x + 14:x + 30] = (150, 60, 40)
    return rgb


def moon():
    h = noise(131, 2.6)
    rgb = palette(h, [(0, (96, 96, 98)), (0.5, (138, 138, 140)), (1, (172, 172, 170))])
    mare = noise(132, 3.0) > 0.62
    rgb[mare] = rgb[mare] * 0.62
    rng = np.random.default_rng(133)
    yy, xx = np.mgrid[0:N, 0:N]
    for _ in range(90):
        x, y = rng.uniform(0, N, 2)
        r = rng.uniform(3, 26) if rng.random() < 0.85 else rng.uniform(26, 60)
        for ox in (-N, 0, N):
            for oy in (-N, 0, N):
                d = np.hypot(xx - x - ox, yy - y - oy)
                inside = d < r
                rim = (d >= r) & (d < r * 1.18)
                # Bowl darker on the lit side, the rim lit towards the north-west, shadowed opposite.
                side = ((xx - x - ox) + (yy - y - oy)) / max(r, 1)
                rgb[inside] = rgb[inside] * (0.78 + 0.18 * np.clip(side[inside], -1, 1))[..., None]
                rgb[rim] = rgb[rim] * (1.08 - 0.12 * np.clip(side[rim], -1, 1))[..., None]
    return rgb


ALL = {
    "ash_desert": ash_desert, "dried_seabed": dried_seabed, "petrified_forest": petrified_forest,
    "irradiated_crater": irradiated_crater, "frozen_megalopolis": frozen_megalopolis, "acid_swamp": acid_swamp,
    "junk_mountains": junk_mountains, "salt_flats": salt_flats, "deep_canyons": deep_canyons, "oasis": oasis,
    "abandoned_farmland": abandoned_farmland, "oil_field": oil_field, "moon": moon,
}

if __name__ == "__main__":
    for name, make in ALL.items():
        save(name, make())
        print("ground", name)
