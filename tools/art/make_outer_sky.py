"""Sky sprites of the outer system: Saturn with its rings (seen from its moons and from inside the rings), Uranus on
its side (faint rings standing upright), Neptune (deep blue, the Great Dark Spot, a bright companion cloud) and
Ganymede's aurora (green curtains with red tops, from its own magnetic field).

    python3 tools/art/make_outer_sky.py
"""
import math
import random

from PIL import Image

from make_textures import hexc, mix, save, shade

SATURN_BANDS = [(-1.0, "c9b38a"), (-0.7, "d9c49a"), (-0.45, "b89c70"), (-0.2, "e8d8b0"), (0.0, "dcc69a"),
                (0.18, "c8ae80"), (0.4, "e4d2a8"), (0.65, "b49a72"), (0.85, "a89070"), (1.01, "8a7a68")]


def planet(n, rx, ry, colour_at, light_dir=(-0.6, -0.2)):
    """A lit globe: colour_at(dx, dy) gives the surface colour, limb darkening and a soft terminator on top."""
    img = Image.new("RGBA", (n, n), (0, 0, 0, 255))
    px = img.load()
    for y in range(n):
        for x in range(n):
            dx, dy = (x - n / 2 + 0.5) / rx, (y - n / 2 + 0.5) / ry
            r2 = dx * dx + dy * dy
            if r2 > 1:
                continue
            c = colour_at(dx, dy)
            mu = math.sqrt(max(0.0, 1 - r2))
            light = max(0.07, min(1.0, 0.85 + dx * light_dir[0] + dy * light_dir[1])) * (0.5 + 0.5 * mu)
            px[x, y] = shade(c, light)
    return img


def saturn(n=128):
    rnd = random.Random(611)

    def body(dx, dy):
        lat = dy + 0.01 * math.sin(dx * 9)
        c = hexc(SATURN_BANDS[0][1])
        for (edge, col) in SATURN_BANDS:
            if lat >= edge:
                c = hexc(col)
        # The polar hexagon's dark blue-grey cap, just visible at the top.
        if dy < -0.82:
            c = mix(c, hexc("5a7088"), 0.6)
        return shade(c, 1 + rnd.uniform(-0.03, 0.03))

    img = planet(n, n * 0.24, n * 0.22, body)
    px = img.load()
    # The rings: an ellipse tilted 26 degrees, the Cassini division dark, the planet's shadow across the back.
    tilt = 0.30
    for y in range(n):
        for x in range(n):
            dx, dy = (x - n / 2 + 0.5) / (n / 2), (y - n / 2 + 0.5) / (n / 2)
            r = math.hypot(dx, dy / tilt)
            if not 0.56 < r < 0.98:
                continue
            behind = dy < 0 and math.hypot(dx / 0.48, dy / 0.44) < 1
            if behind:
                continue
            if 0.80 < r < 0.83:
                continue
            band = 0.6 + 0.25 * math.sin(r * 90) + 0.15 * math.sin(r * 37)
            c = mix(hexc("8a7a62"), hexc("f0e2c0"), max(0.0, min(1.0, band)))
            if 0.56 < r < 0.64:
                c = shade(c, 0.55)
            if dy > 0 and abs(dx) < 0.46 and dy < 0.2:
                c = shade(c, 0.35)
            px[x, y] = c
    save(img, "environment/celestial/far/saturn")


def uranus(n=96):
    rnd = random.Random(781)

    def body(dx, dy):
        c = mix(hexc("9fdfe4"), hexc("c8f2f2"), 0.5 + 0.5 * dx)
        return shade(c, 1 + rnd.uniform(-0.015, 0.015))

    img = planet(n, n * 0.34, n * 0.34, body, light_dir=(0.0, -0.7))
    px = img.load()
    # Uranus lies on its side: its thin rings stand nearly upright.
    for y in range(n):
        for x in range(n):
            dx, dy = (x - n / 2 + 0.5) / (n / 2), (y - n / 2 + 0.5) / (n / 2)
            r = math.hypot(dx / 0.22, dy)
            if 0.86 < r < 0.9 or 0.93 < r < 0.95:
                if not (abs(dx) < 0.68 and abs(dy) < 0.68 and dx < 0):
                    px[x, y] = mix(px[x, y], hexc("8aa0a8"), 0.7)
    save(img, "environment/celestial/far/uranus")


def neptune(n=96):
    rnd = random.Random(846)

    def body(dx, dy):
        c = mix(hexc("2a4496"), hexc("5a7ad8"), 0.5 + 0.35 * math.sin(dy * 7) + rnd.uniform(-0.05, 0.05))
        spot = ((dx + 0.2) / 0.2) ** 2 + ((dy - 0.25) / 0.11) ** 2
        if spot < 1:
            c = mix(hexc("10183a"), c, spot)
        comp = ((dx + 0.18) / 0.14) ** 2 + ((dy - 0.12) / 0.04) ** 2
        if comp < 1:
            c = mix(hexc("f4f8ff"), c, comp)
        return c

    save(planet(n, n * 0.34, n * 0.34, body), "environment/celestial/far/neptune")


def aurora(n=96):
    """Curtains of light: additively drawn, so black is nothing."""
    img = Image.new("RGBA", (n, n), (0, 0, 0, 255))
    px = img.load()
    for x in range(n):
        base = int(n * 0.62 + 6 * math.sin(x * 0.11) + 3 * math.sin(x * 0.37))
        height = 20 + 10 * math.sin(x * 0.07 + 1) + 6 * math.sin(x * 0.23)
        bright = 0.45 + 0.55 * abs(math.sin(x * 0.09))
        for y in range(max(0, int(base - height)), min(n, base)):
            t = min(1.0, max(0.0, (base - y) / height))
            c = mix(hexc("3cff8a"), hexc("ff4a6a"), max(0.0, t - 0.55) * 2.2) if t > 0.55 else hexc("3cff8a")
            fade = (1 - t) ** 0.6 * bright * (0.3 if t < 0.08 else 1.0)
            px[x, y] = shade(c, fade)
    save(img, "environment/celestial/far/aurora")


if __name__ == "__main__":
    saturn()
    uranus()
    neptune()
    aurora()
    print("outer sky sprites written")
