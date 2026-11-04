"""Sky sprites of the worlds beyond Mars: Jupiter as seen from Io and Europa (banded, with the Great Red Spot,
lit from one side) and the swollen sun of Mercury with its corona.

    python3 tools/art/make_far_sky.py
"""
import math
import random

from PIL import Image

from make_textures import hexc, mix, save, shade

BANDS = [(-1.0, "c9b79a"), (-0.78, "a07a5a"), (-0.6, "e2d6bf"), (-0.42, "b88a64"), (-0.22, "efe6d4"),
         (-0.05, "c49a72"), (0.08, "e8dcc6"), (0.26, "9c7456"), (0.42, "ddd0b6"), (0.62, "b39272"),
         (0.8, "d9cdb6"), (1.01, "c9b79a")]


def jupiter(n=96):
    img = Image.new("RGBA", (n, n), (0, 0, 0, 255))
    px = img.load()
    rnd = random.Random(911)
    for y in range(n):
        for x in range(n):
            dx, dy = (x - n / 2 + 0.5) / (n / 2), (y - n / 2 + 0.5) / (n / 2 * 0.935)
            r2 = dx * dx + dy * dy
            if r2 > 1:
                continue
            # Band colour by latitude, wavy edges.
            lat = dy + 0.012 * math.sin(dx * 11 + dy * 5)
            c = hexc(BANDS[0][1])
            for (edge, col) in BANDS:
                if lat >= edge:
                    c = hexc(col)
            c = shade(c, 1 + rnd.uniform(-0.04, 0.04))
            # The Great Red Spot, south of the equator.
            sx, sy = (dx - 0.28) / 0.22, (dy - 0.36) / 0.11
            s = sx * sx + sy * sy
            if s < 1:
                c = mix(hexc("b03a22"), hexc("d98060"), s * s)
            # Lit from the left: a soft terminator; limb darkening.
            mu = math.sqrt(max(0.0, 1 - r2))
            light = max(0.08, min(1.0, 0.85 - dx * 0.6)) * (0.55 + 0.45 * mu)
            px[x, y] = shade(c, light)
    save(img, "environment/celestial/far/jupiter")


def corona(n=64):
    img = Image.new("RGBA", (n, n), (0, 0, 0, 255))
    px = img.load()
    for y in range(n):
        for x in range(n):
            a = math.atan2(y - n / 2 + 0.5, x - n / 2 + 0.5)
            d = math.hypot(x - n / 2 + 0.5, y - n / 2 + 0.5) / (n / 2)
            ray = 1 + 0.25 * math.sin(a * 7) * math.sin(a * 3 + 1)
            if d < 0.3:
                px[x, y] = hexc("fffaf0")
            elif d < 1:
                t = min(1.0, (d - 0.3) / (0.7 * ray))
                px[x, y] = mix(hexc("ffe2b0"), (0, 0, 0, 255), t ** 0.7)
    save(img, "environment/celestial/far/sun_corona")


if __name__ == "__main__":
    jupiter()
    corona()
    print("far sky sprites written")
