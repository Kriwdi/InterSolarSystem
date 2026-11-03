"""Textures of the dead Earth (phase 7): ground blocks, ores, debris props, materials, components and titanium tools.

Same rules as make_textures.py: shaded and outlined pixel art, a seeded random so the output is stable, no flat
single-colour faces.

    python3 tools/art/make_earth_textures.py
"""
import math
import random

from PIL import Image

from make_textures import OUTLINE, RUST, STEEL, TEX, grid, hexc, mix, save, shade, value_noise


# ---------------------------------------------------------------- helpers
def noisy(seed, base, spread=0.08, specks=0, speck_cols=(), cells=4, warp=0.0):
    """Base tone with smooth value noise plus single-pixel specks."""
    rnd = random.Random(seed)
    n = value_noise(seed, 16, cells)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            f = 1 + (n[y][x] - 0.5) * 2 * spread + rnd.uniform(-spread, spread) * 0.5
            px[x, y] = shade(base, f)
    for _ in range(specks):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = rnd.choice(speck_cols)
    return img


def cracks(img, seed, colour, cells=5, light=None):
    """Polygon cracks (dried mud, salt): Voronoi cell borders."""
    rnd = random.Random(seed)
    pts = [(rnd.uniform(0, 16), rnd.uniform(0, 16)) for _ in range(cells)]
    pts += [(x + dx, y + dy) for (x, y) in pts for dx in (-16, 0, 16) for dy in (-16, 0, 16) if dx or dy]
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = sorted((math.hypot(x + 0.5 - p[0], y + 0.5 - p[1]) for p in pts))
            if d[1] - d[0] < 0.9:
                px[x, y] = colour
            elif light and d[1] - d[0] < 1.8:
                px[x, y] = shade(px[x, y], light)
    return img


def stone(seed, base, deep=False):
    """Our own stone and deepslate grounds for the ores (vanilla textures are not copied)."""
    img = noisy(seed, base, 0.07, 14, [shade(base, 0.75), shade(base, 1.2)], cells=5)
    px = img.load()
    if deep:
        for y in range(0, 16, 4):  # deepslate: faint horizontal layering
            for x in range(16):
                px[x, (y + (x // 5) % 2) % 16] = shade(px[x, (y + (x // 5) % 2) % 16], 0.85)
    return img


def ore(name, seed, crystal, light, dark, glow=False):
    """Clusters of three-tone crystals with a dark outline pixel, on stone and on deepslate."""
    for deep in (False, True):
        img = stone(seed + (50 if deep else 0), hexc("4a4a50") if deep else hexc("7d7b78"), deep)
        px = img.load()
        rnd = random.Random(seed + (7 if deep else 0))
        for (cx, cy) in [(3, 4), (10, 3), (7, 9), (12, 12), (3, 12)]:
            cx += rnd.randint(-1, 1)
            cy += rnd.randint(-1, 1)
            cells = [(cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)]
            if rnd.random() < 0.6:
                cells.append((cx - 1, cy + 1))
            for (x, y) in cells:
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = crystal
            for (x, y) in cells:
                if 0 <= x + 1 < 16 and 0 <= y + 1 < 16 and (x + 1, y + 1) not in cells:
                    px[x + 1, y + 1] = dark
            if 0 <= cx < 16 and 0 <= cy < 16:
                px[cx, cy] = light
        save(img, "block/" + ("deepslate_" if deep else "") + name)


def outlined(rows, colours):
    pal = {"o": OUTLINE}
    pal.update(colours)
    return grid(rows, pal)


INGOT = [
    "................",
    "................",
    "................",
    "................",
    "......oooooooo..",
    ".....oLLLLLLLmo.",
    "....oLLHHHHLmmo.",
    "...oLHHHHHHmmdo.",
    "..oooooooooomddo",
    "..oLLLLLLLLomddo",
    "..oLHHHHHHLomdo.",
    "..oMMMMMMMMMmo..",
    "..oddddddddddo..",
    "...oooooooooo...",
    "................",
    "................",
]

RAW = [
    "................",
    "................",
    "................",
    "......ooo.......",
    ".....oLHLoo.....",
    "....oLHHmLLo....",
    "...oLHmmdmLLo...",
    "...oLmmLmdmmoo..",
    "..oLHmLHLmdmmLo.",
    "..oLmmmLmddmLHo.",
    "..oMmdmmmdmmmmo.",
    "...oMddmmmddMo..",
    "....oMMddMMMo...",
    ".....ooooooo....",
    "................",
    "................",
]

DUST = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "........o.......",
    ".......oHo......",
    "......oLHLo.....",
    ".....oLLHLLo....",
    "....oLmLLmLLo...",
    "...oLmmLmmmLLo..",
    "..oMmmdmmdmmmMo.",
    "..oddddddddddddo",
    "...oooooooooooo.",
    "................",
    "................",
]


def material(name, rows, light, mid, dark, hi=None):
    save(outlined(rows, {"L": light, "H": hi or shade(light, 1.15), "m": mid, "M": shade(mid, 0.85), "d": dark}),
         "item/" + name)


TOOLS = {
    "pickaxe": [
        "................",
        "....oooooo......",
        "...oHHHHHHoo....",
        "..oHLLLLLLLHo...",
        "..oooooooLLLHo..",
        "........oSoLLo..",
        ".......oSo.oLo..",
        "......oSo..oLo..",
        ".....oSo....oo..",
        "....oSo.........",
        "...oSo..........",
        "..oSo...........",
        ".oSo............",
        ".oo.............",
        "................",
        "................",
    ],
    "axe": [
        "................",
        "......oooo......",
        ".....oHHLLo.....",
        "....oHLLLLLo....",
        "....oLLLLSoo....",
        "....oLLLoSo.....",
        ".....oLoSo......",
        "......oSo.......",
        ".....oSo........",
        "....oSo.........",
        "...oSo..........",
        "..oSo...........",
        ".oSo............",
        ".oo.............",
        "................",
        "................",
    ],
    "shovel": [
        "................",
        "...........ooo..",
        "..........oHLLo.",
        ".........oHLLLo.",
        "........oLLLLo..",
        ".......oSoLLo...",
        "......oSo.oo....",
        ".....oSo........",
        "....oSo.........",
        "...oSo..........",
        "..oSo...........",
        ".oSo............",
        ".oo.............",
        "................",
        "................",
        "................",
    ],
    "hoe": [
        "................",
        "......ooooo.....",
        ".....oHHLLLo....",
        "......oooSLo....",
        ".........oSo....",
        "........oSo.....",
        ".......oSo......",
        "......oSo.......",
        ".....oSo........",
        "....oSo.........",
        "...oSo..........",
        "..oSo...........",
        ".oSo............",
        ".oo.............",
        "................",
        "................",
    ],
    "sword": [
        "................",
        "............ooo.",
        "...........oHLo.",
        "..........oHLLo.",
        ".........oHLLo..",
        "........oHLLo...",
        ".......oHLLo....",
        "..oo..oHLLo.....",
        "..oGooHLLo......",
        "...oGGLLo.......",
        "....oGGo........",
        "...oSooGo.......",
        "..oSo..oGo......",
        ".oSo....oo......",
        ".oo.............",
        "................",
    ],
}


# ---------------------------------------------------------------- ground
def ground():
    ash = noisy(301, hexc("6f6b66"), 0.09, 22, [hexc("8d8984"), hexc("4f4c49"), hexc("a39e97"), hexc("2f2d2b")])
    save(ash, "block/ash")
    cinder = noisy(302, hexc("3d3634"), 0.1, 30, [hexc("5c4a43"), hexc("1f1b1a"), hexc("7a4632")], cells=6)
    px = cinder.load()
    for (x, y) in [(2, 3), (9, 1), (12, 8), (5, 11), (13, 14), (7, 6)]:  # vesicles
        px[x, y] = hexc("141111")
        px[(x + 1) % 16, y] = hexc("231e1d")
    save(cinder, "block/cinder_rock")
    silt = cracks(noisy(303, hexc("8a7a63"), 0.06, 10, [hexc("a08e74"), hexc("6e604d")]), 303, hexc("4a3f31"), 5,
                  light=1.12)
    save(silt, "block/dried_silt")
    salt = cracks(noisy(304, hexc("e3ddd2"), 0.035, 8, [hexc("f7f4ee"), hexc("cfc7b9")]), 304, hexc("b9afa0"), 6,
                  light=1.04)
    save(salt, "block/salt_crust")
    sblock = noisy(305, hexc("d6cfc3"), 0.05, 0, cells=3)
    px = sblock.load()
    for y in range(16):  # cubic halite crystals: light top-left edges
        for x in range(16):
            if x % 5 == 0 or y % 5 == 0:
                px[x, y] = shade(px[x, y], 0.88)
            elif x % 5 == 1 or y % 5 == 1:
                px[x, y] = shade(px[x, y], 1.08)
    save(sblock, "block/salt_block")
    li = Image.open(TEX / "block/salt_crust.png").convert("RGBA")
    px = li.load()
    rnd = random.Random(306)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = rnd.choice([hexc("e9a8c4"), hexc("c97fa5"), hexc("f4cde0")])  # pink lithium salts
    save(li, "block/lithium_brine_crust")
    soil = noisy(307, hexc("5d5a3e"), 0.09, 16, [hexc("3e3c27"), hexc("7a7650")])
    px = soil.load()
    for (x, y) in [(4, 2), (11, 6), (2, 10), (9, 13), (14, 11)]:
        px[x, y] = hexc("9cff57")
    save(soil, "block/irradiated_soil")
    # Trinitite: green desert glass fused by a nuclear blast, with bubbles; a slow pulse.
    frames = Image.new("RGBA", (16, 64))
    for f in range(4):
        t = noisy(308, hexc("4f8c3a"), 0.12, 12, [hexc("2f5a22"), hexc("8fd36b")], cells=3)
        px = t.load()
        for (x, y) in [(3, 3), (10, 5), (6, 11), (13, 13), (1, 8)]:
            px[x, y] = hexc("1c3a14")
            px[(x + 1) % 16, (y + 1) % 16] = hexc("b9f593")
        glow = 1.0 + 0.1 * math.sin(f * math.pi / 2)
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(px[x, y], glow)
        frames.paste(t, (0, f * 16))
    save(frames, "block/trinitite", frametime=12)
    mud = noisy(309, hexc("4a5130"), 0.1, 18, [hexc("6b7a2a"), hexc("2e331d"), hexc("a3c23a")], cells=5)
    save(mud, "block/acidic_mud")
    # Compacted junk: crushed coloured scrap, a car door, cans and wire in a pressed block.
    junk = Image.new("RGBA", (16, 16))
    px = junk.load()
    rnd = random.Random(310)
    cols = [RUST[2], RUST[3], STEEL[3], STEEL[2], hexc("3b5f8a"), hexc("8a2f2b"), hexc("c9a43a"), hexc("2f6b3f"),
            hexc("6a6a6a")]
    y = 0
    while y < 16:
        h = rnd.randint(2, 4)
        x = 0
        while x < 16:
            w = rnd.randint(3, 7)
            c = rnd.choice(cols)
            for yy in range(y, min(16, y + h)):
                for xx in range(x, min(16, x + w)):
                    f = 1.15 if yy == y else 0.75 if yy == y + h - 1 else 1 + rnd.uniform(-0.06, 0.06)
                    px[xx, yy] = shade(c, f)
            x += w
        y += h
    for _ in range(6):
        px[rnd.randrange(16), rnd.randrange(16)] = OUTLINE
    save(junk, "block/compacted_junk")
    tar = noisy(311, hexc("2a2622"), 0.1, 20, [hexc("4d4436"), hexc("151311"), hexc("6b5d47")])
    px = tar.load()
    for (x, y) in [(4, 4), (11, 9), (6, 13)]:  # oily sheen
        px[x, y] = hexc("4b3d5c")
        px[(x + 1) % 16, y] = hexc("3a5560")
    save(tar, "block/tar_sand")
    shale = noisy(312, hexc("353231"), 0.06, 8, [hexc("252322"), hexc("4a4644")], cells=6)
    px = shale.load()
    for y in (2, 6, 7, 11, 14):
        for x in range(16):
            px[x, y] = shade(px[x, y], 0.72 if y % 2 else 1.18)
    save(shale, "block/oil_shale")
    save(noisy(313, hexc("d9d2bd"), 0.05, 20, [hexc("f2eedf"), hexc("b8b09b"), hexc("ffffff")]),
         "block/silica_sand")

    # Petrified wood: grey-brown stone bark, the top shows the rings in agate colours.
    side = Image.new("RGBA", (16, 16))
    px = side.load()
    rnd = random.Random(314)
    for x in range(16):
        base = [hexc("6e6359"), hexc("847769"), hexc("5a5048"), hexc("958676")][(x + x // 3) % 4]
        for y in range(16):
            px[x, y] = shade(base, 1 + rnd.uniform(-0.07, 0.07))
    for (x, y) in [(3, 5), (10, 11), (13, 2)]:
        px[x, y] = hexc("3b332d")
        px[x, y + 1] = hexc("3b332d")
    save(side, "block/petrified_log")
    top = Image.new("RGBA", (16, 16))
    px = top.load()
    ring = [hexc("b0603a"), hexc("d8a76b"), hexc("8e4b33"), hexc("e3c79a"), hexc("9c6a4a"), hexc("c98a5a")]
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            px[x, y] = hexc("6e6359") if r > 7 else ring[int(r * 1.3) % len(ring)]
    save(top, "block/petrified_log_top")
    save(side, "block/petrified_wood")
    # Dead wood: bleached, cracked bark.
    dead = Image.new("RGBA", (16, 16))
    px = dead.load()
    rnd = random.Random(315)
    for x in range(16):
        base = [hexc("8a8279"), hexc("a39b90"), hexc("6f685f"), hexc("948c82")][(x * 3 + x // 4) % 4]
        for y in range(16):
            px[x, y] = shade(base, 1 + rnd.uniform(-0.06, 0.06))
    for x in (4, 11):
        for y in range(rnd.randint(1, 5), rnd.randint(9, 15)):
            px[x, y] = hexc("4b4540")
    save(dead, "block/dead_log")
    dtop = Image.new("RGBA", (16, 16))
    px = dtop.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            px[x, y] = hexc("8a8279") if r > 7 else shade(hexc("b8ad9c"), 0.85 if int(r) % 2 else 1.0)
    for i in range(7):  # radial check cracks
        px[8 + i // 2, 7 - i // 3] = hexc("5a534b")
    save(dtop, "block/dead_log_top")


# ---------------------------------------------------------------- ores
def ores():
    ore("bauxite_ore", 401, hexc("c7713f"), hexc("e8a273"), hexc("6b3219"))
    ore("titanium_ore", 402, hexc("9fb2c4"), hexc("e6f1fb"), hexc("3d4a57"))
    ore("uraninite_ore", 403, hexc("b9e23a"), hexc("efff9a"), hexc("3a4a12"))
    ore("spodumene_ore", 404, hexc("e7a9c8"), hexc("fbe0ee"), hexc("7a3d5c"))
    ore("monazite_ore", 405, hexc("d99b2b"), hexc("ffd36b"), hexc("6d4510"))


# ---------------------------------------------------------------- debris props (model textures)
def props():
    # Scrap pile: tangled sheet metal, pipes and wire.
    scrap = Image.new("RGBA", (16, 16))
    px = scrap.load()
    rnd = random.Random(501)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(rnd.choice([RUST[1], RUST[2], STEEL[2], STEEL[3], RUST[3]]), 1 + rnd.uniform(-0.1, 0.1))
    for y in (3, 9, 13):
        for x in range(16):
            px[x, y] = STEEL[4] if x % 5 else OUTLINE
    for x in range(16):
        px[x, (x * 7) % 16] = hexc("b06b2f")  # copper wire
    save(scrap, "block/scrap_pile")
    rub = noisy(502, hexc("8c8780"), 0.08, 20, [hexc("6a655f"), hexc("aca69e"), hexc("514c47")], cells=6)
    px = rub.load()
    for (x, y) in [(2, 2), (9, 6), (5, 12), (13, 10)]:  # rebar ends
        px[x, y] = RUST[3]
        px[x, y + 1 if y < 15 else y] = RUST[1]
    save(rub, "block/rubble")
    # Toxic barrel: dented yellow drum with the trefoil and green ooze at the lid.
    side = Image.new("RGBA", (16, 16))
    px = side.load()
    for y in range(16):
        for x in range(16):
            c = hexc("c99a1e")
            if y in (2, 3, 12, 13):
                c = hexc("8f6c14")
            f = 0.75 + 0.35 * math.sin(math.pi * (x + 0.5) / 16)
            px[x, y] = shade(c, f)
    tre = ["..k..", ".kkk.", "k.k.k", "kk.kk", ".k.k."]
    for j, row in enumerate(tre):
        for i, ch in enumerate(row):
            if ch == "k":
                px[5 + i, 6 + j] = hexc("1e1f21")
    for (x, y) in [(3, 1), (4, 1), (4, 2), (11, 1), (12, 14)]:
        px[x, y] = RUST[2]
    for y in range(0, 5):
        px[9, y] = hexc("8fe33a")
    px[9, 5] = hexc("5aa52a")
    save(side, "block/toxic_barrel_side")
    top = Image.new("RGBA", (16, 16))
    px = top.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            px[x, y] = hexc("8f6c14") if r > 6 else hexc("a8821a") if r > 1.5 else hexc("2b2f33")
    for (x, y) in [(9, 4), (10, 4), (10, 5), (11, 6), (9, 3)]:
        px[x, y] = hexc("8fe33a")
    save(top, "block/toxic_barrel_top")
    # Mining drone: dented yellow body with black hazard stripes, a sensor eye, treads and a drill.
    body = Image.new("RGBA", (16, 16))
    px = body.load()
    for y in range(16):
        for x in range(16):
            c = hexc("d9a521") if (x + y) // 3 % 4 else hexc("1e1f21") if y > 11 else hexc("c48f19")
            px[x, y] = shade(c, 1 + random.Random(x * 31 + y).uniform(-0.05, 0.05))
    for x in range(16):
        px[x, 0] = shade(px[x, 0], 1.25)
        px[x, 15] = shade(px[x, 15], 0.6)
    for (x, y) in [(5, 5), (6, 5), (5, 6), (6, 6)]:
        px[x, y] = hexc("ff3b2f") if (x, y) == (5, 5) else hexc("8c1d16")
    save(body, "block/mining_drone_body")
    tread = Image.new("RGBA", (16, 16))
    px = tread.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = hexc("2b2b2d") if (x // 2) % 2 else hexc("444447")
            if y in (0, 15):
                px[x, y] = hexc("19191a")
    save(tread, "block/mining_drone_tread")
    drill = Image.new("RGBA", (16, 16))
    px = drill.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(STEEL[4], 0.8 + 0.4 * (((x + y) // 2) % 2))
    save(drill, "block/mining_drone_drill")
    puddle = noisy(503, hexc("151312"), 0.15, 8, [hexc("3a2f4d"), hexc("2f4652"), hexc("4a3d2a")], cells=3)
    save(puddle, "block/tar_puddle")


# ---------------------------------------------------------------- items
def items():
    material("raw_bauxite", RAW, hexc("d98a52"), hexc("a35a2c"), hexc("5e2c12"))
    material("raw_titanium", RAW, hexc("b6c4d0"), hexc("7d8c99"), hexc("3d4752"))
    material("raw_uranium", RAW, hexc("c9ec52"), hexc("7fa12a"), hexc("3c4d10"))
    material("aluminium_ingot", INGOT, hexc("e4e7ea"), hexc("aab0b6"), hexc("6b7076"), hexc("ffffff"))
    material("titanium_ingot", INGOT, hexc("aebccb"), hexc("74859a"), hexc("3a4656"), hexc("dbe7f2"))
    material("uranium_ingot", INGOT, hexc("b4f03c"), hexc("6fa61e"), hexc("2f4a0c"), hexc("eeffa8"))
    material("lithium_dust", DUST, hexc("f0bfd6"), hexc("c9839f"), hexc("7a3d5c"), hexc("fff0f7"))
    material("rare_earth_dust", DUST, hexc("e6b04a"), hexc("b07a22"), hexc("5e3e0c"), hexc("fff0b0"))
    material("bitumen", RAW, hexc("4a4038"), hexc("2a241f"), hexc("120f0d"), hexc("6e5f72"))

    wafer = outlined([
        "................",
        "................",
        ".....oooooo.....",
        "....oLHHHHLo....",
        "...oLHgLgHHLo...",
        "..oLHLgLgLHHLo..",
        "..oHgggggggggo..",
        "..oLHLgLgLHLLo..",
        "..oHgggggggggo..",
        "..oLLLgLgLLLmo..",
        "..oLLLgLgLLmmo..",
        "...oLmmmmmmmo...",
        "....ommmmmmo....",
        ".....oooooo.....",
        "................",
        "................",
    ], {"L": hexc("5d6d8a"), "H": hexc("8fa3c4"), "g": hexc("2e3747"), "m": hexc("3f4a61")})
    save(wafer, "item/silicon")
    plate = outlined([
        "................",
        "................",
        "..oooooooooooo..",
        "..oHHHHHHHHHLo..",
        "..oHLLLLLLLLmo..",
        "..oHLrLLLLrLmo..",
        "..oHLLLLLLLLmo..",
        "..oHLLLLLLLLmo..",
        "..oHLLLLLLLLmo..",
        "..oHLLLLLLLLmo..",
        "..oHLrLLLLrLmo..",
        "..oHLLLLLLLLmo..",
        "..oLmmmmmmmmmo..",
        "..oooooooooooo..",
        "................",
        "................",
    ], {"L": hexc("9eadbd"), "H": hexc("d7e3ee"), "m": hexc("66768a"), "r": hexc("4a5666")})
    save(plate, "item/titanium_plate")
    solar = outlined([
        "................",
        "................",
        ".oooooooooooooo.",
        ".oBbBbBbBbBbBbo.",
        ".obBbBbBbBbBbBo.",
        ".owwwwwwwwwwwwo.",
        ".oBbBbBbBbBbBbo.",
        ".obBbBbBbBbBbBo.",
        ".owwwwwwwwwwwwo.",
        ".oBbBbBbBbBbBbo.",
        ".obBbBbBbBbBbBo.",
        ".oFFFFFFFFFFFFo.",
        ".oooooooooooooo.",
        "................",
        "................",
        "................",
    ], {"B": hexc("2c4f8f"), "b": hexc("3f6bb8"), "w": hexc("c9d3e0"), "F": hexc("9aa1a7")})
    save(solar, "item/solar_cell")
    battery = outlined([
        "................",
        "......oooo......",
        "......oSSo......",
        ".....oooooo.....",
        ".....oLHHmo.....",
        ".....oLPPmo.....",
        ".....oLPPmo.....",
        ".....oLHHmo.....",
        ".....oLGGmo.....",
        ".....oLGGmo.....",
        ".....oLGGmo.....",
        ".....oLGGmo.....",
        ".....oLmmmo.....",
        ".....oooooo.....",
        "................",
        "................",
    ], {"S": hexc("c9ced2"), "L": hexc("4a5056"), "H": hexc("8d959c"), "m": hexc("2b2f33"), "P": hexc("f0bfd6"),
        "G": hexc("37b56a")})
    save(battery, "item/battery_cell")
    magnet = outlined([
        "................",
        "................",
        "...oooo..oooo...",
        "...oRRo..oSSo...",
        "...oRRo..oSSo...",
        "...oRRo..oSSo...",
        "...oHHo..oHHo...",
        "...oHHo..oHHo...",
        "...oHHHooHHHo...",
        "...oHHHHHHHHo...",
        "....oHHHHHHo....",
        ".....oooooo.....",
        "................",
        "...g.......g....",
        "....g.....g.....",
        "................",
    ], {"R": hexc("c8402d"), "S": hexc("dfe3e6"), "H": hexc("b07a22"), "g": hexc("e6b04a")})
    save(magnet, "item/rare_earth_magnet")
    rod = outlined([
        "............oo..",
        "...........oSSo.",
        "..........oSGSo.",
        ".........oSGGo..",
        "........oSGGo...",
        ".......oSGGo....",
        "......oSGGo.....",
        ".....oSGGo......",
        "....oSGGo.......",
        "...oSGGo........",
        "..oSGGo.........",
        ".oSSSo..........",
        ".oSSo...........",
        "..oo............",
        "................",
        "................",
    ], {"S": hexc("9aa1a7"), "G": hexc("b4f03c")})
    save(rod, "item/uranium_fuel_rod")
    ion = outlined([
        "................",
        ".....oooooo.....",
        "....oHHHHHHo....",
        "...oHMMMMMMHo...",
        "...oMBBBBBBMo...",
        "...oMBbbbbBMo...",
        "...oMBbccbBMo...",
        "...oMBbccbBMo...",
        "...oMBbbbbBMo...",
        "...oMBBBBBBMo...",
        "...oHMMMMMMHo...",
        "....oooooooo....",
        ".....cbcbcb.....",
        "......bcbc......",
        ".......cb.......",
        "................",
    ], {"H": hexc("dfe3e6"), "M": hexc("9aa1a7"), "B": hexc("2b2f33"), "b": hexc("3f8fff"), "c": hexc("b9e4ff")})
    save(ion, "item/ion_drive_kit")
    fus = outlined([
        "................",
        ".....oooooo.....",
        "....oTTTTTTo....",
        "...oTMMMMMMTo...",
        "...oMKKKKKKMo...",
        "...oMKyyyyKMo...",
        "...oMKyWWyKMo...",
        "...oMKyWWyKMo...",
        "...oMKyyyyKMo...",
        "...oMKKKKKKMo...",
        "...oTMMMMMMTo...",
        "....oooooooo....",
        ".....WyWyWy.....",
        "......yWyW......",
        ".......Wy.......",
        "................",
    ], {"T": hexc("aebccb"), "M": hexc("74859a"), "K": hexc("1e1f21"), "y": hexc("ff9f2b"), "W": hexc("fff3c4")})
    save(fus, "item/fusion_drive_kit")
    for tool, rows in TOOLS.items():
        save(outlined(rows, {"H": hexc("e3edf6"), "L": hexc("9eb0c2"), "S": hexc("6b4a2e"), "G": hexc("3a4656")}),
             "item/titanium_" + tool)


if __name__ == "__main__":
    ground()
    ores()
    props()
    items()
    print("earth textures written")


def dead_planks():
    """Planks sawn from the bleached dead wood: grey-brown boards with dry cracks and old nail holes."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    rnd = random.Random(701)
    tones = [hexc("9a9083"), hexc("8a8073"), hexc("a69c8e"), hexc("918779")]
    for board in range(4):
        tone = tones[board]
        for y in range(board * 4, board * 4 + 4):
            for x in range(16):
                px[x, y] = shade(tone, 1 + rnd.uniform(-0.05, 0.05) + (0.06 if (x + board * 5) % 7 == 0 else 0))
        for x in range(16):
            px[x, board * 4 + 3] = shade(tone, 0.68)  # gap below each board
        joint = (board * 7 + 3) % 16
        for y in range(board * 4, board * 4 + 3):
            px[joint, y] = shade(tone, 0.72)
        for (dx, dy) in ((2, 1), (13, 1)):
            px[(joint + dx) % 16, board * 4 + dy] = hexc("4b4540")  # nail holes
        if rnd.random() < 0.7:  # a dry crack along the grain
            x0 = rnd.randrange(10)
            for i in range(rnd.randint(3, 6)):
                px[x0 + i, board * 4 + 1 + (i // 3) % 2] = shade(tone, 0.6)
    save(img, "block/dead_planks")


if __name__ == "__main__":
    dead_planks()
