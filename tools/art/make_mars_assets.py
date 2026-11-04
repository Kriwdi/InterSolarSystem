"""Mars: the ground blocks (rock, regolith, dust, layered sandstone, hematite "blueberries", perchlorate crust, polar
ice), their models and blockstates (random rotations for natural ground), and the sky bodies (Phobos, Deimos, the
small blue Earth, the Martian sun).

    python3 tools/art/make_mars_assets.py
"""
import math
import random

from PIL import Image

from make_textures import TEX, hexc, mix, save, shade
from make_models import ASSETS, NS, item_from_block, write


def grainy(seed, base, spread, specks, cols):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, 1 + rnd.uniform(-spread, spread))
    for _ in range(specks):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = rnd.choice(cols)
        if rnd.random() < 0.35 and x < 15:
            px[x + 1, y] = shade(px[x, y], 0.85)
    return img


def textures():
    # Basaltic bedrock, rust-stained.
    rock = grainy(301, hexc("6e3b2a"), 0.09, 30, [hexc("4c261b"), hexc("8a4f37"), hexc("5a2f22"), hexc("3a1d15")])
    px = rock.load()
    for (x0, y0, dx, dy, n) in [(1, 4, 1, 1, 6), (8, 13, 1, -1, 5), (13, 2, -1, 1, 4)]:
        for i in range(n):
            x, y = x0 + dx * i, y0 + dy * i
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = hexc("2e1710")
    save(rock, "block/mars_rock")
    # Regolith: rusty soil with pebbles of dark basalt.
    save(grainy(302, hexc("a4552f"), 0.08, 22, [hexc("6b3420"), hexc("c46c3c"), hexc("3f2219"), hexc("8a4a2c")]),
         "block/mars_regolith")
    # Dust: fine, bright butterscotch, ripple marks.
    dust = grainy(303, hexc("c97a45"), 0.05, 8, [hexc("e09a62"), hexc("b0683a")])
    px = dust.load()
    for y in range(16):
        for x in range(16):
            if (x + y * 2 + int(2 * math.sin(x * 0.7))) % 7 == 0:
                px[x, y] = shade(px[x, y], 0.9)
    save(dust, "block/mars_dust")
    # Layered sandstone (Gale crater's Mount Sharp): bands of ochre, tan and red.
    ss = Image.new("RGBA", (16, 16))
    px = ss.load()
    rnd = random.Random(304)
    bands = [hexc("b9814f"), hexc("a3683f"), hexc("c99a66"), hexc("8e5435"), hexc("b37a4a")]
    for y in range(16):
        c = bands[(y // 3) % len(bands)]
        for x in range(16):
            px[x, y] = shade(c, 1 + rnd.uniform(-0.05, 0.05))
    save(ss, "block/mars_sandstone")
    # Hematite "blueberries": grey-blue spherules weathering out of the rock.
    hem = Image.open(TEX / "block/mars_rock.png").convert("RGBA")
    px = hem.load()
    for (x, y) in [(3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (9, 13)]:
        for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)):
            if x + dx < 16 and y + dy < 16:
                px[x + dx, y + dy] = hexc("5c6470") if (dx, dy) != (0, 0) else hexc("9aa4b4")
    save(hem, "block/hematite_ore")
    # Perchlorate crust: a pale, cracked salt skin over the red soil.
    pc = grainy(305, hexc("d8cba8"), 0.05, 14, [hexc("efe6c8"), hexc("b8a37e"), hexc("a4552f")])
    px = pc.load()
    for (x0, y0, dx, dy, n) in [(0, 6, 1, 0, 16), (5, 0, 0, 1, 6), (11, 7, 0, 1, 9)]:
        for i in range(n):
            x, y = x0 + dx * i, y0 + dy * i
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = hexc("a4552f")
    save(pc, "block/perchlorate_crust")
    # Polar ice: water ice laid down in thin layers with dust between.
    ice = Image.new("RGBA", (16, 16))
    px = ice.load()
    rnd = random.Random(306)
    for y in range(16):
        dusty = y % 5 == 4
        for x in range(16):
            c = hexc("e9e2da") if not dusty else hexc("c59a7c")
            px[x, y] = shade(c, 1 + rnd.uniform(-0.04, 0.04))
    save(ice, "block/mars_ice")


def sky():
    """Phobos (lumpy, dark), Deimos (a small pale dot), Earth as a blue evening star, and the smaller Martian sun."""
    def body(name, size, draw):
        img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
        draw(img.load(), size)
        save(img, "environment/celestial/mars/" + name)

    def phobos(px, n):
        rnd = random.Random(401)
        for y in range(n):
            for x in range(n):
                dx, dy = (x - n / 2 + 0.5) / (n * 0.46), (y - n / 2 + 0.5) / (n * 0.34)
                lump = 1 + 0.12 * math.sin(math.atan2(dy, dx) * 3 + 0.6)
                if dx * dx + dy * dy <= lump:
                    lit = max(0.25, 1 - (dx + 0.6) * 0.45)
                    c = shade(hexc("8a7a6c"), lit * (1 + rnd.uniform(-0.08, 0.08)))
                    if (x * 7 + y * 3) % 23 == 0:
                        c = shade(c, 0.6)
                    px[x, y] = c

    def deimos(px, n):
        for y in range(n):
            for x in range(n):
                d = math.hypot(x - n / 2 + 0.5, y - n / 2 + 0.5) / (n / 2)
                if d < 0.9:
                    px[x, y] = shade(hexc("b8ab9c"), 1.1 - d * 0.5)

    def earth(px, n):
        for y in range(n):
            for x in range(n):
                d = math.hypot(x - n / 2 + 0.5, y - n / 2 + 0.5) / (n / 2)
                if d < 1:
                    px[x, y] = mix(hexc("cfe6ff"), (0, 0, 0, 255), min(1.0, d ** 1.6))

    def sun(px, n):
        for y in range(n):
            for x in range(n):
                d = math.hypot(x - n / 2 + 0.5, y - n / 2 + 0.5) / (n / 2)
                if d < 1:
                    core = d < 0.45
                    px[x, y] = hexc("fff6e8") if core else mix(hexc("f2c79a"), (0, 0, 0, 255), (d - 0.45) / 0.55)

    body("phobos", 32, phobos)
    body("deimos", 16, deimos)
    body("earth_star", 16, earth)
    body("sun", 32, sun)


BLOCKS = ("mars_rock", "mars_regolith", "mars_dust", "mars_sandstone", "hematite_ore", "perchlorate_crust", "mars_ice")


def models():
    for name in BLOCKS:
        write(ASSETS / "models/block" / (name + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + name}})
        variants = [{"model": NS + "block/" + name}]
        if name not in ("mars_sandstone", "mars_ice"):
            variants += [{"model": NS + "block/" + name, "y": r} for r in (90, 180, 270)]
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": variants}})
        item_from_block(name, "block/" + name)


if __name__ == "__main__":
    textures()
    sky()
    models()
    print("mars assets written")
