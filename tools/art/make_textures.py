"""Generates the mod's textures (16x16 pixel art) into src/main/resources/assets/intersolarsystem/textures.

Item sprites are drawn as character grids with a palette; block textures are built from layered procedural passes
(base metal with noise, bevels, rivets, rust, grime) so they stay consistent. Deterministic: the same script always
writes the same pixels. Run after changing anything here:

    python3 tools/art/make_textures.py
"""
import json
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TEX = ROOT / "src/main/resources/assets/intersolarsystem/textures"


def hexc(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def mix(c1, c2, t):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def shade(c, f):
    return (max(0, min(255, int(c[0] * f))), max(0, min(255, int(c[1] * f))), max(0, min(255, int(c[2] * f))), c[3])


def grid(rows, palette, size=16):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                px[x, y] = palette[ch]
    return img


def save(img, name, frametime=None):
    path = TEX / (name + ".png")
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    meta = Path(str(path) + ".mcmeta")
    if frametime:
        meta.write_text(json.dumps({"animation": {"frametime": frametime}}, indent=2))
    elif meta.exists():
        meta.unlink()


# ---------------------------------------------------------------- palettes
STEEL = [hexc("2b2f33"), hexc("3a3f44"), hexc("4a5056"), hexc("5d646b"), hexc("737b82"), hexc("8d959c")]
RUST = [hexc("4a2414"), hexc("6e3519"), hexc("8f4a1f"), hexc("b0622a"), hexc("c97f3d")]
HAZARD_Y = hexc("d9a521")
HAZARD_K = hexc("1e1f21")
OUTLINE = hexc("15171a")


def metal_base(seed, light=3, var=1):
    """Brushed steel plate: base tone with fine horizontal brushing and soft noise."""
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        streak = rnd.uniform(-0.04, 0.04)
        for x in range(16):
            n = rnd.uniform(-0.05, 0.05) + streak
            base = STEEL[light]
            px[x, y] = shade(base, 1 + n * var)
    return img


def bevel(img, x0, y0, x1, y1, hi=1.18, lo=0.72):
    """Light top-left edge, dark bottom-right edge of a rectangle (inclusive coordinates)."""
    px = img.load()
    for x in range(x0, x1 + 1):
        px[x, y0] = shade(px[x, y0], hi)
        px[x, y1] = shade(px[x, y1], lo)
    for y in range(y0, y1 + 1):
        px[x0, y] = shade(px[x0, y], hi)
        px[x1, y] = shade(px[x1, y], lo)


def rivet(img, x, y):
    px = img.load()
    px[x, y] = STEEL[5]
    if x + 1 < 16 and y + 1 < 16:
        px[x + 1, y + 1] = STEEL[0]
    if x + 1 < 16:
        px[x + 1, y] = STEEL[3]
    if y + 1 < 16:
        px[x, y + 1] = STEEL[1]


def rust_drip(img, x, y, length, seed):
    """A rust streak running down from (x, y), fading out."""
    rnd = random.Random(seed)
    px = img.load()
    for i in range(length):
        yy = y + i
        if yy >= 16:
            break
        t = i / max(1, length - 1)
        c = RUST[max(0, min(4, int(3 - t * 2 + rnd.uniform(-0.6, 0.6))))]
        px[x, yy] = mix(px[x, yy], c, 0.85 - t * 0.55)
        if rnd.random() < 0.3 and x + 1 < 16:
            px[x + 1, yy] = mix(px[x + 1, yy], c, 0.35)


def rust_patch(img, cx, cy, radius, seed, strength=0.8):
    rnd = random.Random(seed)
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= radius + rnd.uniform(-0.8, 0.8):
                c = RUST[rnd.randint(1, 4)]
                px[x, y] = mix(px[x, y], c, strength * (1 - d / (radius + 1.5)) + 0.15)


def grime(img, seed, amount=0.15):
    """Dark specks and a slightly darker lower edge (dirt collects at the bottom)."""
    rnd = random.Random(seed)
    px = img.load()
    for y in range(16):
        for x in range(16):
            f = 1.0 - (0.07 if y >= 13 else 0.0)
            if rnd.random() < amount:
                f *= rnd.uniform(0.82, 0.92)
            px[x, y] = shade(px[x, y], f)


def scratch(img, x, y, dx, dy, length):
    px = img.load()
    for i in range(length):
        xx, yy = x + dx * i, y + dy * i
        if 0 <= xx < 16 and 0 <= yy < 16:
            px[xx, yy] = shade(px[xx, yy], 1.22)


# ---------------------------------------------------------------- blocks
def rusted_panels():
    # Variant 1: four rivets, rust running from them, a scratch.
    a = metal_base(11)
    bevel(a, 0, 0, 15, 15)
    for (x, y) in [(2, 2), (12, 2), (2, 12), (12, 12)]:
        rivet(a, x, y)
    rust_drip(a, 2, 4, 7, 1)
    rust_drip(a, 12, 4, 5, 2)
    rust_patch(a, 11, 13, 2.2, 3, 0.7)
    scratch(a, 5, 6, 1, 1, 4)
    grime(a, 4)
    save(a, "block/rusted_panel")

    # Variant 2: two plates with a welded seam across the middle.
    b = metal_base(21, light=2)
    bevel(b, 0, 0, 15, 7)
    bevel(b, 0, 8, 15, 15)
    px = b.load()
    for x in range(16):
        px[x, 8] = STEEL[0] if x % 3 else STEEL[1]
        if x % 4 == 1:
            px[x, 7] = mix(px[x, 7], RUST[2], 0.6)
    for (x, y) in [(2, 3), (13, 3), (2, 12), (13, 12)]:
        rivet(b, x, y)
    rust_patch(b, 4, 9, 2.0, 5, 0.75)
    rust_drip(b, 13, 5, 3, 6)
    grime(b, 7)
    save(b, "block/rusted_panel_2")

    # Variant 3: a vent grille with soot.
    c = metal_base(31)
    bevel(c, 0, 0, 15, 15)
    px = c.load()
    for y in (4, 6, 8, 10):
        for x in range(4, 12):
            px[x, y] = STEEL[0]
            px[x, y + 1] = STEEL[4]
    bevel(c, 3, 3, 12, 12, hi=0.75, lo=1.2)
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        px[x, y] = STEEL[5]
    rust_drip(c, 6, 12, 4, 8)
    rust_drip(c, 9, 12, 3, 9)
    grime(c, 10, 0.2)
    save(c, "block/rusted_panel_3")


def hatch():
    # Frame: steel with hazard stripes along the edges.
    f = metal_base(41, light=2)
    px = f.load()
    for y in range(16):
        for x in range(16):
            edge = x < 3 or x > 12 or y < 3 or y > 12
            if edge:
                stripe = ((x + y) // 2) % 2 == 0
                c = HAZARD_Y if stripe else HAZARD_K
                px[x, y] = shade(c, random.Random(x * 31 + y).uniform(0.9, 1.05))
    bevel(f, 0, 0, 15, 15)
    bevel(f, 3, 3, 12, 12, hi=0.7, lo=1.15)
    for (x, y) in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        rivet(f, x, y)
    rust_patch(f, 13, 14, 1.8, 42, 0.6)
    grime(f, 43)
    save(f, "block/ship_hatch_frame")

    # Door plate: heavy plate with a pressure seal ring and a stencilled arrow.
    d = metal_base(44, light=3)
    px = d.load()
    for y in range(16):
        for x in range(16):
            r = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 6.2 <= r <= 7.2:
                px[x, y] = STEEL[1]
            elif 5.4 <= r < 6.2:
                px[x, y] = shade(px[x, y], 1.15)
    for (x, y) in [(7, 2), (8, 2), (6, 3), (7, 3), (8, 3), (9, 3)]:
        px[x, y] = mix(px[x, y], hexc("d8d2c0"), 0.7)
    for y in range(4, 6):
        for x in (7, 8):
            px[x, y] = mix(px[x, y], hexc("d8d2c0"), 0.7)
    rust_drip(d, 4, 9, 5, 45)
    grime(d, 46)
    save(d, "block/ship_hatch_door")

    # Wheel: chipped red paint over steel.
    w = Image.new("RGBA", (16, 16))
    px = w.load()
    rnd = random.Random(47)
    red = [hexc("6b1712"), hexc("8e2219"), hexc("b33224"), hexc("d14a35")]
    for y in range(16):
        for x in range(16):
            c = red[2]
            if y < 5:
                c = red[3]
            if y > 11:
                c = red[1]
            if rnd.random() < 0.12:
                c = STEEL[3]
            px[x, y] = c
    save(w, "block/ship_hatch_wheel")


def repair_station():
    # Bench wood-and-steel top.
    t = metal_base(51, light=3)
    px = t.load()
    for y in range(16):
        for x in range(16):
            if y in (0, 15) or x in (0, 15):
                px[x, y] = STEEL[1]
    for (x, y) in [(3, 5), (4, 5), (5, 6), (9, 10), (10, 10), (11, 9)]:
        px[x, y] = mix(px[x, y], hexc("1b1d1f"), 0.6)
    for (x, y) in [(12, 3), (12, 4)]:
        px[x, y] = mix(px[x, y], hexc("2f7d4d"), 0.7)
    rust_patch(t, 2, 12, 1.6, 52, 0.6)
    scratch(t, 6, 2, 1, 0, 5)
    scratch(t, 4, 12, 1, 0, 6)
    grime(t, 53)
    save(t, "block/repair_station_top")

    # Pegboard with outlined tools and a green cross (the "repair" sign).
    p = Image.new("RGBA", (16, 16))
    px = p.load()
    board = hexc("3b4146")
    for y in range(16):
        for x in range(16):
            px[x, y] = board
            if x % 3 == 1 and y % 3 == 1:
                px[x, y] = hexc("23272a")
    bevel(p, 0, 0, 15, 15)
    # Wrench (top) and screwdriver (yellow handle) hanging on the board.
    for (x, y) in [(2, 2), (4, 2), (2, 3), (3, 3), (4, 3), (3, 4), (3, 5), (3, 6)]:
        px[x, y] = hexc("a4abb1")
    for y in range(8, 14):
        px[3, y] = hexc("caa25a")
    px[2, 8] = px[4, 8] = hexc("6e5124")
    for y in range(3, 13):
        px[6, y] = hexc("9aa1a7")
    px[6, 3] = hexc("dfe3e6")
    cross = [(11, 4), (11, 5), (11, 6), (11, 7), (11, 8), (9, 6), (10, 6), (12, 6), (13, 6), (12, 4), (12, 5),
             (12, 7), (12, 8), (9, 7), (10, 7), (13, 7)]
    for (x, y) in cross:
        px[x, y] = hexc("37b56a")
    for (x, y) in [(11, 4), (9, 6), (11, 9)]:
        if 0 <= y < 16:
            px[x, y] = hexc("5fd890")
    rust_drip(p, 14, 10, 5, 54)
    save(p, "block/repair_station_board")

    # Legs / sides / underside: painted dark steel with a yellow safety edge.
    s = metal_base(55, light=1)
    px = s.load()
    for x in range(16):
        px[x, 0] = HAZARD_Y if (x // 2) % 2 == 0 else HAZARD_K
    bevel(s, 0, 1, 15, 15)
    grime(s, 56, 0.25)
    save(s, "block/repair_station_side")

    # Toolbox on the bench.
    b = Image.new("RGBA", (16, 16))
    px = b.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = hexc("a3271c") if y > 3 else hexc("c5382a")
            if y == 7:
                px[x, y] = hexc("6b1712")
            if (x + y) % 9 == 0 and y > 8:
                px[x, y] = hexc("8e2219")
    for x in range(5, 11):
        px[x, 8] = hexc("c9c2a6")
    save(b, "block/repair_station_toolbox")


def console():
    # Cabinet: dark painted steel, vents, a status lamp strip.
    c = metal_base(61, light=1)
    px = c.load()
    bevel(c, 0, 0, 15, 15)
    for y in (9, 11, 13):
        for x in range(3, 13):
            px[x, y] = STEEL[0]
            px[x, y + 1] = STEEL[3]
    for x, col in [(3, "e5483a"), (5, "f2b233"), (7, "4fd27a"), (9, "4fd27a"), (11, "3fa7e0")]:
        px[x, 4] = hexc(col)
        px[x, 5] = shade(hexc(col), 0.6)
    for (x, y) in [(1, 1), (13, 1), (1, 13), (13, 13)]:
        rivet(c, x, y)
    rust_patch(c, 2, 14, 1.6, 62, 0.6)
    grime(c, 63)
    save(c, "block/diagnostic_console_cabinet")

    # Keyboard panel: keys with labels, a big red button, a rotary dial.
    k = metal_base(64, light=2)
    px = k.load()
    bevel(k, 0, 0, 15, 15)
    for row in range(3):
        for col in range(5):
            x, y = 1 + col * 2, 2 + row * 3
            px[x, y] = hexc("cfd4d8")
            px[x, y + 1] = hexc("7d858c")
    for (x, y) in [(12, 3), (13, 3), (12, 4), (13, 4)]:
        px[x, y] = hexc("d83a2c")
    px[12, 3] = hexc("ff7a63")
    for (x, y) in [(12, 9), (13, 9), (14, 10), (14, 11), (13, 12), (12, 12), (11, 11), (11, 10)]:
        px[x, y] = STEEL[5]
    px[12, 10] = px[13, 11] = STEEL[0]
    px[13, 10] = hexc("f2b233")
    grime(k, 65)
    save(k, "block/diagnostic_console_keys")

    # Animated screen: scrolling waveform, a sweeping bar graph, blinking cursor (8 frames).
    frames = 8
    s = Image.new("RGBA", (16, 16 * frames))
    for f in range(frames):
        fr = Image.new("RGBA", (16, 16), hexc("07120d"))
        px = fr.load()
        for y in range(16):
            for x in range(16):
                if y % 2 == 0:
                    px[x, y] = hexc("0b1a13")
        import math
        for x in range(1, 15):
            v = 4 + round(2.2 * math.sin((x + f * 2) * 0.8) + 0.8 * math.sin((x * 3 - f) * 0.5))
            px[x, max(1, min(7, v))] = hexc("5dfa98")
            px[x, max(1, min(7, v)) + 1] = hexc("1f6b3f")
        for i, x in enumerate(range(2, 14, 2)):
            h = 1 + (i * 3 + f) % 5
            for yy in range(14 - h, 14):
                px[x, yy] = hexc("49b8ea")
                px[x + 1, yy] = hexc("2b7ea8")
        if f % 2 == 0:
            px[13, 9] = px[14, 9] = hexc("e8fff2")
        for x in range(16):
            px[x, 0] = hexc("1b3a2b")
            px[x, 15] = hexc("1b3a2b")
        s.paste(fr, (0, f * 16))
    save(s, "block/diagnostic_console_screen", frametime=4)

    # Monitor housing.
    h = metal_base(66, light=2)
    bevel(h, 0, 0, 15, 15)
    grime(h, 67)
    save(h, "block/diagnostic_console_housing")


def navigation_console():
    import math
    # Animated star chart: grid, the Sun, two orbits with planets moving along them, a blinking course marker.
    frames = 16
    sheet = Image.new("RGBA", (16, 16 * frames))
    for f in range(frames):
        fr = Image.new("RGBA", (16, 16), hexc("060b14"))
        px = fr.load()
        for y in range(16):
            for x in range(16):
                if x % 4 == 0 or y % 4 == 0:
                    px[x, y] = hexc("0d1726")
        for (x, y) in [(1, 2), (13, 1), (3, 13), (14, 11), (10, 3), (2, 8)]:
            px[x, y] = hexc("8fa6c2") if (x + f) % 5 else hexc("e8f1ff")
        cx, cy = 7.5, 7.5
        for r, col in ((3.2, "1f4f7a"), (6.0, "1a3d5e")):
            for a in range(0, 360, 6):
                x = int(round(cx + r * math.cos(math.radians(a))))
                y = int(round(cy + r * math.sin(math.radians(a)) * 0.8))
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = hexc(col)
        for (x, y) in [(7, 7), (8, 7), (7, 8), (8, 8)]:
            px[x, y] = hexc("ffd25a")
        px[7, 7] = hexc("fff2b0")
        a1 = math.radians(f * 360 / frames)
        a2 = math.radians(-f * 360 / frames / 2 + 90)
        x1 = int(round(cx + 3.2 * math.cos(a1)))
        y1 = int(round(cy + 3.2 * math.sin(a1) * 0.8))
        x2 = int(round(cx + 6.0 * math.cos(a2)))
        y2 = int(round(cy + 6.0 * math.sin(a2) * 0.8))
        px[x1, y1] = hexc("4fa3ff")
        px[x2, y2] = hexc("e0703a")
        if f % 4 < 2:
            px[x2, max(0, y2 - 1)] = hexc("5dfa98")
        sheet.paste(fr, (0, f * 16))
    save(sheet, "block/navigation_console_chart", frametime=3)

    # Frame of the chart table: brushed steel with a brass trim and engraved degree marks.
    fr = metal_base(71, light=3)
    px = fr.load()
    brass = hexc("b8913c")
    for x in range(16):
        px[x, 0] = shade(brass, 1.1)
        px[x, 1] = brass
        px[x, 15] = shade(brass, 0.7)
        if x % 3 == 0:
            px[x, 3] = STEEL[1]
    bevel(fr, 0, 2, 15, 14)
    grime(fr, 72)
    save(fr, "block/navigation_console_frame")

    # Column: dark steel with bundled cables running down and a status light.
    col = metal_base(73, light=1)
    px = col.load()
    for y in range(16):
        for x, c in ((5, "8e2219"), (6, "b33224"), (9, "1e4f9a"), (10, "2f6fd0")):
            px[x, y] = shade(hexc(c), 0.85 + 0.15 * ((y // 2) % 2))
    for y in (3, 9, 14):
        for x in range(4, 12):
            px[x, y] = STEEL[4]
    px[7, 6] = hexc("5dfa98")
    px[8, 6] = hexc("2a8a52")
    bevel(col, 0, 0, 15, 15)
    grime(col, 74, 0.2)
    save(col, "block/navigation_console_column")

    # Base plate: hazard edge around a riveted steel plate.
    base = metal_base(75, light=2)
    px = base.load()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = HAZARD_Y if (i // 2) % 2 == 0 else HAZARD_K
    for (x, y) in [(3, 3), (11, 3), (3, 11), (11, 11)]:
        rivet(base, x, y)
    rust_patch(base, 12, 12, 1.6, 76, 0.6)
    grime(base, 77)
    save(base, "block/navigation_console_base")

    # Hologram globe: blue ocean, green land, scan lines.
    holo = Image.new("RGBA", (16, 16), hexc("2f78d0"))
    px = holo.load()
    rnd = random.Random(78)
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.3 or (4 < x < 9 and 3 < y < 10):
                px[x, y] = hexc("3fae6a")
            if y % 3 == 0:
                px[x, y] = shade(px[x, y], 1.25)
    save(holo, "block/navigation_console_holo")


# ---------------------------------------------------------------- items
def items():
    o = OUTLINE
    repair_kit = grid([
        "................",
        "......oooo......",
        ".....o....o.....",
        ".....o....o.....",
        "..oooooooooooo..",
        ".oRRRRRRRRRRRRo.",
        ".oRrrrrrrrrrrRo.",
        ".oddddddddddddo.",
        ".oRRRRRWWRRRRRo.",
        ".oRRRRWGGWRRRRo.",
        ".oRRRRGGGGRRRRo.",
        ".oRRRRWGGWRRRRo.",
        ".orrrrrWWrrrrro.",
        ".orrrrrrrrrrrro.",
        "..oooooooooooo..",
        "................",
    ], {"o": o, "R": hexc("c5382a"), "r": hexc("9a2a1f"), "d": hexc("5e1510"), "W": hexc("e9eef0"),
        "G": hexc("37b56a")})
    save(repair_kit, "item/repair_kit")

    sensor = grid([
        "...........a....",
        "..........aL....",
        ".........a......",
        "........a.......",
        ".....ooooo......",
        "....oSSSSSo.....",
        "....oKggggKo....",
        "....oKgGggKo....",
        "....oKggggKo....",
        "....oSSSSSSo....",
        "....oSbSySSo....",
        "....oSSSSSSo....",
        "....oddddddo....",
        ".....oddddo.....",
        "......oooo......",
        "................",
    ], {"o": o, "a": hexc("9aa1a7"), "L": hexc("ff5a3c"), "S": hexc("6b737a"), "K": hexc("2b2f33"),
        "g": hexc("173b2a"), "G": hexc("5dfa98"), "b": hexc("3fa7e0"), "y": hexc("f2b233"), "d": hexc("3a3f44")})
    save(sensor, "item/scrap_sensor")

    # Two torn, bent plates: a light one in front with a rivet hole, a rusty darker one behind.
    scrap = grid([
        "................",
        ".........ooo....",
        "........oMMMoo..",
        ".......oMRRMMMo.",
        "...oooooMRRMMMo.",
        "..oHHHHHHoMMMo..",
        ".oHhhhhhhHoMo...",
        ".oHhhKhhhhHo....",
        "..oHhhhhhhhHo...",
        "...oHhhhhRhhHo..",
        "...oHhhhRRhhho..",
        "....oHhhhhhho...",
        ".....oHhhhoo....",
        "......oHho......",
        ".......oo.......",
        "................",
    ], {"o": o, "H": hexc("c3cacf"), "h": hexc("8d959c"), "K": hexc("2b2f33"), "M": hexc("5d646b"),
        "R": hexc("a35a26")})
    save(scrap, "item/scrap_metal")

    # A coiled bundle of red and blue wire with a copper plug end.
    wiring = grid([
        "................",
        "....oooooo......",
        "...oRRRRRRoo....",
        "..oRroooorRRo...",
        "..oRo.oo.oBBo...",
        "..oRooBBooBRo...",
        "..oRRoBBBBRRo...",
        "...oRRRBBRRo....",
        "....oRRRRRo.....",
        ".....oooRRo.....",
        "........oRRo....",
        ".........oRRoo..",
        "..........oKCCo.",
        "..........oKCco.",
        "...........ooo..",
        "................",
    ], {"o": o, "R": hexc("c8402d"), "r": hexc("8e2219"), "B": hexc("3a6fd0"), "K": hexc("2b2f33"),
        "C": hexc("e0a060"), "c": hexc("a8682e")})
    save(wiring, "item/salvaged_wiring")

    circuit = grid([
        "................",
        "..oooooooooooo..",
        ".oGGgGGGGGGGgGo.",
        ".oGyyyyGGGcGGGo.",
        ".oGyGGyKKKKcGGo.",
        ".oGyGGyKkkKGGGo.",
        ".oGyGGyKKKKGGGo.",
        ".oGyGGGGyGGGbbo.",
        ".oGyyyyyyGGGbbo.",
        ".ogGGGGGyGGGGGo.",
        ".oGGsGGGyyyyyGo.",
        ".oGssGGGGGGGyxo.",
        ".oGGGGGGGGGGxxo.",
        "..oiioiioiioo...",
        "...i..i..i......",
        "................",
    ], {"o": o, "G": hexc("2e7d46"), "g": hexc("235e35"), "y": hexc("d9b24a"), "K": hexc("1c1d20"),
        "k": hexc("3a3d42"), "c": hexc("e8e2c8"), "b": hexc("3fa7e0"), "s": hexc("c8c8c8"),
        "x": hexc("3b2a1d"), "i": hexc("b9c0c6")})
    save(circuit, "item/salvaged_circuit")

    # Multitool: red grip with a yellow band (bottom left), steel body, pliers jaws open at the top right.
    multitool = grid([
        "...........oo...",
        "..........oHHo..",
        "..........oHmo.o",
        "...........omoHo",
        "..........oMmHmo",
        ".........oMmmoo.",
        "........oMmMo...",
        ".......oMmMo....",
        "......oKMMo.....",
        ".....oRRKo......",
        "....oRRrYo......",
        "...oRRrYo.......",
        "..oRRrro........",
        ".oRRrro.........",
        ".odRro..........",
        "..oo............",
    ], {"o": o, "H": hexc("dfe3e6"), "m": hexc("9aa1a7"), "M": hexc("6b737a"), "K": hexc("2b2f33"),
        "R": hexc("c8402d"), "r": hexc("8e2219"), "d": hexc("5e1510"), "Y": hexc("e8b830")})
    save(multitool, "item/multitool")


if __name__ == "__main__":
    rusted_panels()
    hatch()
    repair_station()
    console()
    navigation_console()
    items()
    print("textures written")
