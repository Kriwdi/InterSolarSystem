"""The urban block set of the dead Earth: building materials (cracked and sooty concrete, asphalt with markings,
tiles, curtain wall glass) and props (offices, hospitals, streets, airports, car wrecks, jet engines, signs).

One table (BLOCKS) is the single source of truth: this script writes the textures, models, blockstates, item models,
block loot tables, the Java registrations (content/UrbanBlocks.java) and the missing translation rows.

    python3 tools/art/make_urban_assets.py && python3 tools/sync_lang.py
"""
import json
import random
import re
from pathlib import Path

from PIL import Image

from make_textures import hexc, mix, save, shade
from make_models import ASSETS, NS, facing_blockstate, item_from_block, model, write

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/intersolarsystem"
JAVA = ROOT / "src/main/java/hu/kriwdi/intersolarsystem/content/UrbanBlocks.java"
LANG = ROOT / "src/client/java/hu/kriwdi/intersolarsystem/client/datagen/ModLangTable.java"


# ---------------------------------------------------------------- texture helpers
def noise_img(seed, a, b, amount=0.5, size=16):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        for x in range(size):
            px[x, y] = mix(hexc(a), hexc(b), rnd.random() * amount)
    return img, px, rnd


def crack(px, rnd, x, y, steps, colour, size=16):
    for _ in range(steps):
        if 0 <= x < size and 0 <= y < size:
            px[x, y] = colour
        x += rnd.choice((-1, 0, 1))
        y += 1 if rnd.random() < 0.7 else 0


def tex_cracked_concrete():
    img, px, rnd = noise_img(1, "9a9a96", "7c7c78")
    for start in ((3, 0), (11, 2), (7, 8)):
        crack(px, rnd, *start, 9, hexc("4f4f4c"))
    save(img, "block/cracked_concrete")


def tex_sooty_concrete():
    img, px, rnd = noise_img(2, "8e8d89", "6c6b67")
    for x in range(16):
        top = rnd.randint(0, 6)
        for y in range(top, 16):
            f = 0.35 + 0.5 * (16 - y) / 16 * rnd.random()
            px[x, y] = mix(px[x, y], hexc("1b1a18"), min(0.85, f))
    save(img, "block/sooty_concrete")


def tex_rebar_concrete():
    img, px, rnd = noise_img(3, "979792", "777772")
    for y in range(6, 16):
        for x in range(16):
            if y > 6 + abs(x - 8) // 2 and rnd.random() < 0.9:
                px[x, y] = mix(hexc("3e3d3a"), hexc("55534e"), rnd.random())  # broken-out core
    for x in (3, 8, 13):
        for y in range(7, 16):
            px[x, y] = hexc("8a4a24") if y % 3 else hexc("5e3016")  # rusty bars
    save(img, "block/rebar_concrete")


def tex_asphalt(name, seed, cracked=False):
    img, px, rnd = noise_img(seed, "3a3a3b", "252526", 0.8)
    for _ in range(8):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = hexc("5c5b58")  # gravel in the tar
    if cracked:
        crack(px, rnd, 2, 0, 16, hexc("141414"))
        crack(px, rnd, 12, 4, 12, hexc("141414"))
        for _ in range(5):
            px[rnd.randrange(16), rnd.randrange(16)] = hexc("7b7a6e")  # ash in the cracks
    save(img, "block/" + name)
    return img


def tex_road_marking():
    img = tex_asphalt("road_marking", 4)
    px = img.load()
    for y in range(16):
        if y < 10:
            for x in (7, 8):
                px[x, y] = mix(hexc("d8d4c4"), hexc("a8a596"), random.Random(x * 31 + y).random())
    save(img, "block/road_marking")
    img = tex_asphalt("runway_marking", 5)
    px = img.load()
    for y in range(16):
        for x in range(3, 13):
            px[x, y] = mix(hexc("e2dfd2"), hexc("b5b2a5"), random.Random(x * 17 + y * 5).random() * 0.7)
    save(img, "block/runway_marking")


def tex_hazard():
    img, px, rnd = noise_img(6, "9a9a96", "7c7c78")
    for y in range(16):
        for x in range(16):
            stripe = ((x + y) // 4) % 2 == 0
            px[x, y] = mix(hexc("d9a521") if stripe else hexc("1e1e1e"), px[x, y], 0.2)
    save(img, "block/hazard_concrete")


def tex_tiles(name, a, b, grout, size=4, seed=7):
    img, px, rnd = noise_img(seed, a, b, 0.4)
    for y in range(16):
        for x in range(16):
            if x % size == 0 or y % size == 0:
                px[x, y] = hexc(grout)
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = shade(px[x, y], 0.75)  # stains
    save(img, "block/" + name)


def tex_carpet():
    img, px, rnd = noise_img(8, "4a5866", "38434e", 0.6)
    for y in range(0, 16, 2):
        for x in range(16):
            if (x + y) % 4 == 0:
                px[x, y] = shade(px[x, y], 1.12)
    save(img, "block/office_carpet")


def tex_curtain_wall(name, broken):
    rnd = random.Random(9 if broken else 10)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = mix(hexc("2c4250"), hexc("101b22"), (x + y) / 30)
            if (x - y) in (3, 4, 10) and not broken:
                c = mix(c, hexc("8aa3b0"), 0.45)  # reflection streaks
            px[x, y] = c
    for i in range(16):
        px[0, i] = px[15, i] = hexc("6d7378")
        px[i, 0] = px[i, 15] = hexc("6d7378")
        px[i, 8] = hexc("54595d")
    if broken:
        for _ in range(2):
            cx, cy = rnd.randrange(4, 12), rnd.randrange(4, 12)
            for _ in range(5):
                x, y = cx, cy
                dx, dy = rnd.choice((-1, 0, 1)), rnd.choice((-1, 1))
                for _ in range(3):
                    if 0 < x < 15 and 0 < y < 15:
                        px[x, y] = hexc("7f8f97")
                    x += dx
                    y += dy
            px[cx, cy] = hexc("050607")
    save(img, "block/" + name)


def tex_grating():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            hole = x % 4 in (1, 2) and y % 4 in (1, 2)
            px[x, y] = hexc("1c1d1e") if hole else mix(hexc("7e8489"), hexc("5d6266"), ((x * 7 + y * 3) % 5) / 6)
    save(img, "block/metal_grating")


def tex_ceiling_light():
    img, px, rnd = noise_img(11, "cfcfca", "b3b3ad", 0.3)
    for y in range(3, 13):
        for x in range(2, 14):
            px[x, y] = hexc("f4f1dc") if x not in (5, 10) else hexc("d9d6c0")
    for i in range(16):
        px[i, 2] = px[i, 13] = hexc("8b8b86")
    px[7, 7] = hexc("807d6e")  # a dead tube spot
    save(img, "block/ceiling_light")


def tex_terminal_floor():
    img, px, rnd = noise_img(12, "d6d1c4", "bcb6a6", 0.5)
    for y in range(16):
        for x in range(16):
            if (x + y * 2) % 13 == 0:
                px[x, y] = hexc("a39b88")  # marble veins
            if x in (0, 8) or y in (0, 8):
                px[x, y] = shade(px[x, y], 0.85)
    save(img, "block/terminal_floor")


def tex_crate():
    img, px, rnd = noise_img(13, "8a6a40", "6d522f", 0.5)
    for i in range(16):
        for t in (0, 15):
            px[i, t] = px[t, i] = hexc("4a3720")
        px[i, i] = hexc("5c4428")  # diagonal brace
    for (x, y) in ((4, 5), (5, 5), (6, 5), (9, 10), (10, 10)):
        px[x, y] = hexc("2b2b2b")  # stencil marks
    save(img, "block/shipping_crate")


def flat(name, colour, seed, amount=0.25, dark=None):
    img, px, rnd = noise_img(seed, colour, dark or colour, amount)
    save(img, "block/" + name)
    return img, px


def prop_textures():
    """Shared materials for the props."""
    flat("prop_metal_white", "e2e2dd", 20, 0.3, "c8c8c2")
    flat("prop_metal_grey", "8d9399", 21, 0.3, "6f757b")
    flat("prop_metal_dark", "3e4246", 22, 0.3, "2a2d30")
    flat("prop_plastic_black", "222324", 23, 0.2, "151516")
    flat("prop_wood", "8b6a46", 24, 0.4, "6e5235")
    flat("prop_fabric_blue", "3f5a7a", 25, 0.4, "2e4560")
    flat("prop_fabric_green", "5f7d6a", 26, 0.4, "4a6455")
    flat("prop_chrome", "c3c9ce", 27, 0.3, "8e969c")
    flat("prop_rubber", "1b1b1b", 28, 0.3, "2c2c2c")
    flat("prop_glass_dark", "1f2c33", 29, 0.3, "0f171c")
    flat("prop_red", "a8302a", 30, 0.3, "84241f")
    flat("prop_yellow", "d9a521", 31, 0.3, "b68818")
    flat("prop_orange", "d9822b", 32, 0.3, "b66a1f")
    flat("prop_rust", "8a4a24", 33, 0.5, "5e3016")
    flat("prop_sand", "b3a274", 34, 0.4, "8f7f55")
    flat("prop_lamp_off", "9a9480", 35, 0.2, "817c6a")
    flat("prop_lamp_red", "7a1f1a", 36, 0.2, "5a1612")
    flat("prop_lamp_green", "1f5a2a", 37, 0.2, "164520")
    flat("prop_lamp_amber", "b97a14", 38, 0.2, "8c5b0f")
    flat("prop_olive", "5a6340", 39, 0.35, "454d30")
    # Screens: a dead monitor and a flickering departure board.
    img, px, rnd = noise_img(40, "0c1410", "060a08", 0.5)
    for y in range(2, 14, 3):
        for x in range(2, 14):
            if rnd.random() < 0.6:
                px[x, y] = hexc("2d5a3a")
    save(img, "block/prop_screen")
    frames = Image.new("RGBA", (16, 64))
    for f in range(4):
        rnd = random.Random(41 + f)
        for y in range(16):
            for x in range(16):
                c = hexc("0a0a0a")
                if y % 4 in (1, 2) and x % 8 != 7 and rnd.random() < (0.75 if f != 2 else 0.3):
                    c = hexc("e3a22a") if x < 8 else hexc("e8e2c8")
                frames.putpixel((x, y + f * 16), c)
    save(frames, "block/prop_departure_board", frametime=6)
    # Biohazard sign, Exodus and quarantine posters.
    sign = Image.new("RGBA", (16, 16), hexc("d9a521"))
    px = sign.load()
    for i in range(16):
        px[i, 0] = px[i, 15] = px[0, i] = px[15, i] = hexc("1e1e1e")
    for (x, y) in ((7, 3), (8, 3), (6, 4), (9, 4), (7, 7), (8, 7), (4, 10), (5, 10), (10, 10), (11, 10), (5, 11),
                   (10, 11), (7, 8), (8, 8), (7, 9), (8, 9), (6, 11), (9, 11), (4, 9), (11, 9), (6, 5), (9, 5)):
        px[x, y] = hexc("1e1e1e")
    save(sign, "block/prop_biohazard")
    poster = Image.new("RGBA", (16, 16))
    px = poster.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = mix(hexc("0f2340"), hexc("1f4f86"), y / 16)
    for x in range(3, 13):
        px[x, 3] = hexc("e9e4d4")  # EXODUS title
    for y in range(6, 12):
        px[8, y] = hexc("e9e4d4")  # the rocket
        if y > 8:
            px[7, y] = px[9, y] = hexc("c9c3b2")
    px[8, 12] = hexc("d9822b")
    px[8, 13] = hexc("e3a22a")
    for x in range(4, 12):
        px[x, 14] = hexc("d9822b")
    for (x, y) in ((2, 5), (13, 9), (3, 12), (12, 2)):
        px[x, y] = hexc("e8e2c8")  # stars
    save(poster, "block/prop_poster_exodus")
    poster = Image.new("RGBA", (16, 16), hexc("e8e2c8"))
    px = poster.load()
    for x in range(16):
        px[x, 0] = px[x, 1] = px[x, 15] = hexc("a8302a")
    for x in range(3, 13):
        px[x, 4] = hexc("1e1e1e")  # QUARANTINE
        if x % 2:
            px[x, 8] = hexc("4a4a4a")
            px[x, 11] = hexc("4a4a4a")
    for (x, y) in ((7, 6), (8, 6), (7, 9), (8, 9)):
        px[x, y] = hexc("a8302a")
    save(poster, "block/prop_poster_quarantine")
    # Car paints with a dirt line and rust spots.
    for name, base in (("car_red", "8c2a24"), ("car_blue", "2a4a7a"), ("car_white", "d6d4cc"),
                       ("car_yellow", "d1a21f"), ("car_police", "e4e2da"), ("car_ambulance", "e8e6de")):
        img, px, rnd = noise_img(50 + len(name), base, base, 0.1)
        for y in range(16):
            for x in range(16):
                px[x, y] = mix(px[x, y], hexc("6e5a44"), max(0.0, (y - 9) / 10) + (0.5 if rnd.random() < 0.05 else 0))
        if name == "car_police":
            for x in range(16):
                for y in (6, 7, 8):
                    px[x, y] = hexc("1f3f78")
        if name == "car_ambulance":
            for x in range(16):
                px[x, 6] = px[x, 9] = hexc("b0302a")
            for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8), (6, 7), (9, 8)):
                px[x, y] = hexc("b0302a")
        save(img, "block/prop_" + name)
    # Frosted cryo glass and the specimen fluid with Grey Bloom clumps.
    img, px, rnd = noise_img(60, "bcd6e2", "8fb2c4", 0.5)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = hexc("eef6fa")
    for y in range(16):
        px[0, y] = px[15, y] = hexc("6d8a99")
    save(img, "block/prop_frost")
    img, px, rnd = noise_img(61, "5e6e52", "3f4b38", 0.6)
    for _ in range(10):
        cx, cy = rnd.randrange(2, 14), rnd.randrange(2, 14)
        for (dx, dy) in ((0, 0), (1, 0), (0, 1), (-1, 0)):
            if 0 <= cx + dx < 16 and 0 <= cy + dy < 16:
                px[cx + dx, cy + dy] = hexc("9a988c")
    for y in range(0, 16, 5):
        px[3, y] = px[11, (y + 2) % 16] = hexc("c9d1bc")  # bubbles
    save(img, "block/prop_specimen")
    # Jet engine intake fan.
    fan = Image.new("RGBA", (16, 16))
    px = fan.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = (dx * dx + dy * dy) ** 0.5
            import math
            a = math.atan2(dy, dx)
            blade = int((a + math.pi) / (2 * math.pi) * 18 + r * 0.6) % 2 == 0
            c = hexc("9aa1a8") if blade else hexc("5b6268")
            if r < 2.2:
                c = hexc("2a2d30")
            if r > 7.3:
                c = hexc("c8ccd0")
            px[x, y] = c
    save(fan, "block/prop_jet_fan")


# ---------------------------------------------------------------- model helpers
def e(frm, to, tex, faces=None, rot=None, uvs=None):
    """An element with uvs clipped to the texture (pieces may reach outside the block)."""
    dx, dy, dz = (min(16, abs(to[i] - frm[i])) for i in range(3))
    uv = {"north": (0, 16 - dy, dx, 16), "south": (0, 16 - dy, dx, 16), "east": (0, 16 - dy, dz, 16),
          "west": (0, 16 - dy, dz, 16), "up": (0, 0, dx, dz), "down": (0, 0, dx, dz)}
    el = {"from": list(frm), "to": list(to), "faces": {}}
    for d in ("north", "south", "east", "west", "up", "down"):
        t = (faces or {}).get(d, tex)
        if t is None:
            continue
        el["faces"][d] = {"texture": "#" + t, "uv": list((uvs or {}).get(d, uv[d]))}
    if rot:
        el["rotation"] = rot
    return el


FULL = (0, 0, 0, 16, 16, 16)


def m_office_desk():
    return [e((0, 12, 0), (16, 14, 11), "wood"), e((1, 0, 1), (3, 12, 10), "metal_grey"), e((13, 0, 1), (15, 12, 10), "metal_grey"),
            e((9, 2, 2), (13, 12, 10), "metal_grey", faces={"north": "metal_white"}),
            e((4, 14, 6), (12, 21, 7), "plastic_black", faces={"north": "screen"}), e((7, 14, 7), (9, 17, 9), "plastic_black"),
            e((4, 14, 2), (11, 14.5, 4.5), "plastic_black"), e((12, 14, 2), (14, 14.6, 4), "plastic_black"),
            e((1, 14, 7), (3, 14.3, 10), "metal_white")]


def m_office_chair():
    return [e((7, 0, 7), (9, 6, 9), "chrome"), e((3, 0, 7.5), (13, 1, 8.5), "plastic_black"), e((7.5, 0, 3), (8.5, 1, 13), "plastic_black"),
            e((3, 6, 3), (13, 8, 13), "fabric_blue"), e((3, 8, 12), (13, 20, 14), "fabric_blue"), e((7, 6, 13), (9, 10, 14), "chrome"),
            e((2, 9, 5), (3, 10, 11), "plastic_black"), e((13, 9, 5), (14, 10, 11), "plastic_black")]


def m_filing_cabinet():
    els = [e((2, 0, 1), (14, 16, 15), "metal_grey")]
    for y in (2, 6, 10):
        els.append(e((5, y + 1.5, 0.5), (11, y + 2.2, 1), "chrome"))
        els.append(e((2.5, y, 0.8), (13.5, y + 0.4, 1), "metal_dark"))
    els.append(e((3, 13, 0), (13, 15.5, 1), "metal_dark", rot={"origin": [8, 13, 1], "axis": "x", "angle": -22.5}))  # an open drawer flap
    return els


def m_server_rack():
    els = [e((1, 0, 1), (15, 16, 15), "metal_dark", faces={"north": "screen"})]
    for x in (1, 14):
        els.append(e((x, 0, 0), (x + 1, 16, 1), "metal_grey"))
    for y in range(2, 15, 3):
        els.append(e((3, y, 0.6), (4, y + 0.6, 1), "lamp_green" if y % 2 else "lamp_amber"))
    return els


def m_locker():
    return [e((1, 0, 2), (15, 16, 15), "metal_grey"), e((7.6, 0.5, 1.6), (8.4, 15.5, 2), "metal_dark"),
            e((5, 8, 1.5), (6, 10, 2), "chrome"), e((10, 8, 1.5), (11, 10, 2), "chrome"),
            e((2, 12, 1.8), (6.5, 13, 2), "metal_dark"), e((9.5, 12, 1.8), (14, 13, 2), "metal_dark"),
            e((14, 0.5, -3), (15, 14, 2), "metal_grey", rot={"origin": [15, 8, 2], "axis": "y", "angle": -45})]


def m_hospital_bed():
    els = [e((1, 6, 0), (15, 9, 30), "metal_white"), e((2, 9, 1), (14, 11, 29), "fabric_green"),
           e((3, 11, 25), (13, 13, 29), "metal_white"),
           e((1, 9, -0.5), (15, 15, 1), "metal_white"), e((1, 9, 30), (15, 13, 31), "metal_white"),
           e((0.5, 10, 6), (1.5, 13, 20), "chrome"), e((14.5, 10, 6), (15.5, 13, 20), "chrome")]
    for (x, z) in ((2, 2), (13, 2), (2, 27), (13, 27)):
        els += [e((x, 1, z), (x + 1, 6, z + 1), "chrome"), e((x - 0.5, 0, z - 0.5), (x + 1.5, 1, z + 1.5), "rubber")]
    return els


def m_iv_stand():
    return [e((7.5, 0, 7.5), (8.5, 26, 8.5), "chrome"), e((4, 0, 7.5), (12, 1, 8.5), "plastic_black"), e((7.5, 0, 4), (8.5, 1, 12), "plastic_black"),
            e((4, 25, 7.6), (12, 25.6, 8.4), "chrome"), e((4.5, 18, 7), (7.5, 24.5, 9), "glass_dark"),
            e((5.8, 12, 7.8), (6.2, 18, 8.2), "metal_white")]


def m_medical_cabinet():
    return [e((1, 0, 4), (15, 16, 16), "metal_white", faces={"north": "glass_dark"}), e((1, 0, 3.5), (15, 1, 4), "metal_white"),
            e((1, 15, 3.5), (15, 16, 4), "metal_white"), e((7.5, 1, 3.5), (8.5, 15, 4), "metal_white"),
            e((6, 12, 3), (10, 13, 3.5), "red"), e((7.5, 10.5, 3), (8.5, 14.5, 3.5), "red"),
            e((3, 6, 5), (6, 9, 8), "metal_white"), e((10, 6, 5), (12, 10, 7), "orange")]


def m_wheelchair():
    els = [e((3, 6, 4), (13, 7, 12), "fabric_blue"), e((3, 7, 11), (13, 15, 12), "fabric_blue"),
           e((3, 15, 11), (4, 17, 13), "rubber"), e((12, 15, 11), (13, 17, 13), "rubber")]
    for x in (1.5, 13.5):
        els += [e((x, 0, 4), (x + 1, 11, 14), "rubber", faces={"north": None, "south": None}), e((x + 0.2, 4, 8), (x + 0.8, 6, 10), "chrome")]
    els += [e((4, 0, 2), (6, 2, 4), "rubber"), e((10, 0, 2), (12, 2, 4), "rubber"), e((4, 2, 2), (12, 3, 5), "chrome")]
    return els


def m_vending_machine():
    return [e((0, 0, 3), (16, 31, 16), "red", faces={"north": "glass_dark"}), e((0, 0, 2), (16, 2, 3), "metal_dark"),
            e((11, 4, 2.5), (15, 28, 3), "metal_dark"), e((12, 18, 2), (14, 22, 2.5), "screen"),
            e((1, 4, 2.6), (10.5, 29, 3), "glass_dark"), e((2, 6, 2.8), (10, 6.5, 2.9), "chrome"), e((2, 13, 2.8), (10, 13.5, 2.9), "chrome"),
            e((2, 20, 2.8), (10, 20.5, 2.9), "chrome"), e((1, 1, 1.5), (10, 3, 2.5), "plastic_black")]


def m_airport_seats():
    els = [e((-6, 3, 4), (22, 4, 6), "chrome")]
    for i, x in enumerate((-6, 3.5, 13)):
        els += [e((x, 5, 3), (x + 9, 7, 12), "fabric_blue"), e((x, 7, 11), (x + 9, 16, 13), "fabric_blue")]
    for x in (-5, 21):
        els += [e((x, 0, 4), (x + 1, 5, 5), "chrome"), e((x - 0.5, 0, 2), (x + 1.5, 1, 10), "chrome")]
    for x in (3, 12.5, 22):
        els.append(e((x - 0.5, 7, 4), (x + 0.5, 10, 11), "chrome"))
    return els


def m_departure_board():
    return [e((-8, 0, 14), (24, 14, 16), "metal_dark", faces={"north": "board"}), e((-9, -1, 13.5), (25, 0, 16), "metal_dark"),
            e((-9, 14, 13.5), (25, 15, 16), "metal_dark"), e((-1, 15, 15), (0, 20, 16), "chrome"), e((16, 15, 15), (17, 20, 16), "chrome")]


def m_sandbags():
    els = []
    for row, y in enumerate((0, 4, 8)):
        off = 4 if row % 2 else 0
        for x in range(-off, 16, 8):
            x0, x1 = max(0, x), min(16, x + 8)
            if x1 - x0 > 1:
                els.append(e((x0 + 0.3, y, 3 + row), (x1 - 0.3, y + 4, 13 - row), "sand"))
    return els


def m_barbed_wire():
    """A coil of razor wire: square loops turned 45 degrees (diamonds) in a row, held by two rusty stakes."""
    els = []
    for i, x in enumerate((1, 4, 7, 10, 13)):
        r = {"origin": [x + 0.25, 7, 8], "axis": "x", "angle": 45 if i % 2 == 0 else 22.5}
        els += [e((x, 2, 2.5), (x + 0.5, 2.5, 13.5), "metal_grey", rot=r), e((x, 11.5, 2.5), (x + 0.5, 12, 13.5), "metal_grey", rot=r),
                e((x, 2, 2.5), (x + 0.5, 12, 3), "metal_grey", rot=r), e((x, 2, 13), (x + 0.5, 12, 13.5), "metal_grey", rot=r)]
    els += [e((0, 7, 7.7), (16, 7.5, 8.2), "metal_grey"), e((0, 3, 4), (16, 3.4, 4.4), "metal_grey")]
    for x in (2.5, 11.5):
        els.append(e((x, 0, 7.3), (x + 1.2, 13, 8.5), "rust"))
    return els


def m_quarantine_barrier():
    return [e((0, 9, 7), (16, 12, 8), "red", faces={"north": "barrier", "south": "barrier"}),
            e((0, 4, 7), (16, 6, 8), "metal_white"),
            e((1, 0, 4), (2.5, 12, 5.5), "metal_white", rot={"origin": [1.75, 0, 7.5], "axis": "x", "angle": 22.5}),
            e((1, 0, 9.5), (2.5, 12, 11), "metal_white", rot={"origin": [1.75, 0, 7.5], "axis": "x", "angle": -22.5}),
            e((13.5, 0, 4), (15, 12, 5.5), "metal_white", rot={"origin": [14.25, 0, 7.5], "axis": "x", "angle": 22.5}),
            e((13.5, 0, 9.5), (15, 12, 11), "metal_white", rot={"origin": [14.25, 0, 7.5], "axis": "x", "angle": -22.5}),
            e((6, 12, 7.2), (10, 15, 7.8), "lamp_amber")]


def m_warning_sign():
    return [e((7.3, 0, 7.3), (8.7, 18, 8.7), "metal_grey"), e((2, 16, 6.5), (14, 28, 7.3), "metal_grey", faces={"north": "biohazard"},
            rot={"origin": [8, 22, 7], "axis": "z", "angle": 22.5})]


def m_poster(tex):
    return [e((1, 1, 15.4), (15, 15, 16), tex, faces={"south": None}), e((2, 13, 15.2), (3, 14, 15.4), "chrome"),
            e((13, 13, 15.2), (14, 14, 15.4), "chrome")]


def m_traffic_light():
    els = [e((7, 0, 7), (9, 22, 9), "metal_dark"), e((5, 0, 5), (11, 1, 11), "metal_dark"),
           e((5.5, 20, 6), (10.5, 32, 10), "yellow"), e((5, 31, 5.5), (11, 32, 10.5), "metal_dark")]
    for i, lamp in enumerate(("lamp_red", "lamp_amber", "lamp_green")):
        y = 28 - i * 3.5
        els += [e((6.5, y, 5.5), (9.5, y + 3, 6), lamp), e((6, y + 3, 4.5), (10, y + 3.4, 6), "metal_dark")]
    return els


def m_street_lamp():
    return [e((7, 0, 7), (9, 30, 9), "metal_dark"), e((6, 0, 6), (10, 3, 10), "metal_dark"), e((7.5, 29, 1), (8.5, 30, 9), "metal_dark"),
            e((6, 27, -4), (10, 29, 2), "metal_grey", faces={"down": "lamp_off"})]


def m_fire_hydrant():
    return [e((5, 0, 5), (11, 1, 11), "red"), e((5.5, 1, 5.5), (10.5, 9, 10.5), "red"), e((6, 9, 6), (10, 11, 10), "red"),
            e((7, 11, 7), (9, 12, 9), "chrome"), e((3.5, 5, 7), (5.5, 7, 9), "chrome"), e((10.5, 5, 7), (12.5, 7, 9), "chrome"),
            e((7, 4, 3.5), (9, 7, 5.5), "chrome")]


def m_bench():
    els = [e((0, 6, 6), (16, 7, 13), "wood"), e((0, 8, 13), (16, 14, 14), "wood")]
    for x in (1, 14):
        els += [e((x, 0, 7), (x + 1, 6, 12), "metal_dark"), e((x, 6, 12.5), (x + 1, 14, 13.5), "metal_dark")]
    return els


def m_car(paint, siren=None):
    # 3 blocks long (z -15..31), ~1.8 wide (x -6..22), a bit sunk and listing.
    els = [e((-6, 2, -15), (22, 9, 31), paint, faces={"north": "metal_dark", "south": "metal_dark"}),
           e((-5, 9, -6), (21, 16, 18), paint, faces={"north": "glass_dark", "south": "glass_dark", "east": "glass_dark", "west": "glass_dark"}),
           e((-5.2, 9, -2), (21.2, 15.5, -1), paint), e((-5.2, 9, 8), (21.2, 15.5, 9), paint),
           e((-6.5, 3, -15.5), (22.5, 5, -14.5), "chrome"), e((-6.5, 3, 30.5), (22.5, 5, 31.5), "chrome"),
           e((-4, 5, -15.4), (0, 7, -15), "lamp_off"), e((16, 5, -15.4), (20, 7, -15), "lamp_off"),
           e((-4, 5, 31), (0, 7, 31.4), "lamp_red"), e((16, 5, 31), (20, 7, 31.4), "lamp_red")]
    for (x, z) in ((-7, -11), (19, -11), (-7, 21), (19, 21)):
        els.append(e((x, 0, z), (x + 4, 6, z + 6), "rubber"))
    if siren:
        els += [e((1, 16, 4), (7, 18, 6), siren[0]), e((9, 16, 4), (15, 18, 6), siren[1]), e((-1, 16, 4.5), (17, 16.5, 5.5), "chrome")]
    return els


def m_jet_engine():
    els = [e((-8, 0, -12), (24, 30, 28), "metal_white", faces={"north": "fan", "south": "metal_dark"},
             uvs={"north": (0, 0, 16, 16), "south": (0, 0, 16, 16)}),
           e((-9, -1, -14), (25, 31, -12), "chrome", faces={"north": None, "up": "chrome", "down": "chrome", "south": None}),
           e((-2, 6, 28), (18, 24, 32), "metal_dark"), e((2, 30, -4), (14, 32, 20), "metal_grey")]
    return els


def m_runway_light():
    return [e((5, 0, 5), (11, 2, 11), "metal_dark"), e((6, 2, 6), (10, 3.5, 10), "lamp_amber")]


def m_dino_skull():
    t = {"origin": [8, 0, 8], "axis": "x", "angle": -22.5}
    return [e((-4, 0, -12), (20, 14, 8), "bone", faces={"north": "socket"}, rot=t),  # cranium
            e((-1, 0, -28 + 14), (17, 10, -12), "bone"),  # snout
            e((-2, 8, -2), (4, 12, 2), "socket"), e((12, 8, -2), (18, 12, 2), "socket"),
            e((0, -0.01, -14), (16, 3, 4), "bone"),  # lower jaw on the plinth
            e((0, 3, -13), (1.5, 6, -4), "bone"), e((14.5, 3, -13), (16, 6, -4), "bone"),  # teeth rows
            e((-6, -0.02, 8), (22, 2, 30), "metal_dark")]  # display plinth


def m_runway_bollard():
    return [e((6, 0, 6), (10, 12, 10), "yellow", faces={"up": "metal_dark"}), e((5.8, 4, 5.8), (10.2, 6, 10.2), "plastic_black")]


def m_radio_set():
    """A field radio with a tape deck: olive case, speaker grille, two dials, the cassette window, a whip antenna."""
    els = [e((1, 0, 3), (15, 10, 13), "olive", faces={"north": "metal_dark"}), e((1, 10, 5), (15, 11, 11), "metal_dark"),
           e((2, 2, 2.6), (7, 8, 3), "screen"), e((9, 6, 2.5), (11, 8, 3), "chrome"), e((12, 6, 2.5), (14, 8, 3), "chrome"),
           e((9, 2, 2.6), (14, 4.5, 3), "glass_dark"), e((10, 2.8, 2.4), (11, 3.8, 2.6), "rust"), e((12, 2.8, 2.4), (13, 3.8, 2.6), "rust"),
           e((3, 11, 7), (13, 12, 9), "metal_grey"), e((13, 10, 11), (13.6, 30, 11.6), "chrome",
           rot={"origin": [13.3, 10, 11.3], "axis": "z", "angle": -22.5}), e((0, 3, 6), (1, 7, 10), "metal_dark"),
           e((15, 3, 6), (16, 7, 10), "metal_dark")]
    for y in (3, 4.5, 6):
        els.append(e((2.5, y, 2.5), (6.5, y + 0.6, 2.6), "metal_grey"))
    return els


def m_recycler():
    """The recycler: a heavy steel body with an intake hopper on top, a window onto the crusher, warning stripes,
    a status lamp and an output chute at the front."""
    return [e((0, 0, 0), (16, 2, 16), "metal_dark"), e((1, 2, 1), (15, 12, 15), "metal_grey", faces={"north": "metal_dark"}),
            e((0.5, 2, 0.5), (15.5, 3, 15.5), "hazard"), e((3, 12, 3), (13, 13, 13), "metal_dark"),
            e((2, 13, 2), (14, 16, 4), "metal_grey"), e((2, 13, 12), (14, 16, 14), "metal_grey"),
            e((2, 13, 4), (4, 16, 12), "metal_grey"), e((12, 13, 4), (14, 16, 12), "metal_grey"),
            e((4, 12.5, 4), (12, 13, 12), "rust"), e((3, 5, 0.5), (9, 10, 1), "glass_dark"),
            e((4, 6, 0.6), (8, 7, 0.8), "rust"), e((11, 8, 0.5), (13, 10, 1), "lamp_amber"),
            e((10, 3, -2), (14, 5, 1), "metal_dark"), e((15, 4, 5), (16.5, 9, 11), "metal_dark")]


def m_shredder_module():
    els = [e((1, 0, 1), (15, 10, 15), "yellow", faces={"up": "metal_dark"}), e((2, 10, 2), (14, 12, 14), "metal_dark")]
    for x in (3, 7, 11):
        els += [e((x, 10, 4), (x + 2, 13, 12), "chrome"), e((x + 0.5, 12.5, 3.5), (x + 1.5, 13.5, 12.5), "metal_grey")]
    els += [e((0, 4, 6), (1, 8, 10), "metal_dark"), e((15, 4, 6), (16, 8, 10), "metal_dark")]
    return els


def m_smelter_module():
    return [e((1, 0, 1), (15, 3, 15), "metal_dark"), e((2, 3, 2), (14, 12, 14), "rust", faces={"north": "metal_dark"}),
            e((4, 4, 1.5), (12, 8, 2), "lamp_amber"), e((4, 12, 4), (12, 13, 12), "metal_dark"),
            e((5, 12.5, 5), (11, 13.2, 11), "lamp_amber"), e((10, 13, 10), (13, 22, 13), "rust"),
            e((0, 5, 6), (2, 7, 10), "metal_grey"), e((14, 5, 6), (16, 7, 10), "metal_grey")]


def m_sorter_module():
    els = [e((0, 0, 1), (16, 6, 15), "metal_grey"), e((0, 6, 3), (16, 7, 13), "rubber")]
    for x in (2, 7, 12):
        els += [e((x, 7, 2), (x + 2, 12, 4), "metal_dark"), e((x, 11, 4), (x + 2, 12, 12), "metal_dark"),
                e((x + 0.5, 9, 5), (x + 1.5, 11, 11), "chrome")]
    els += [e((6, 12, 6), (10, 15, 10), "metal_dark", faces={"north": "screen"}), e((1, 2, 0.5), (4, 4, 1), "lamp_green")]
    return els


def m_battery_bank():
    els = [e((1, 0, 2), (15, 16, 15), "metal_grey", faces={"north": "metal_dark"})]
    for y in (2, 7, 12):
        for x in (2.5, 6.5, 10.5):
            els += [e((x, y, 1.5), (x + 3, y + 3.5, 2), "yellow"), e((x + 1, y + 3.5, 1.6), (x + 2, y + 4, 2), "chrome")]
    els += [e((6, 15.5, 1.5), (10, 16, 2), "lamp_green"), e((7, 16, 7), (9, 18, 9), "metal_dark")]
    return els


def m_cryo_pod():
    return [e((2, 0, 2), (14, 2, 14), "metal_dark"), e((3, 2, 4), (13, 29, 14), "metal_white", faces={"north": "frost"}),
            e((3.5, 4, 3.5), (12.5, 27, 4), "frost"), e((2.5, 29, 3), (13.5, 31, 14.5), "metal_grey"),
            e((1, 3, 10), (2.5, 28, 11.5), "chrome"), e((13.5, 3, 10), (15, 28, 11.5), "chrome"),
            e((5, 2.2, 2.5), (11, 3.8, 3.5), "metal_dark"), e((6, 2.6, 2.3), (7, 3.4, 2.5), "lamp_green"),
            e((9, 2.6, 2.3), (10, 3.4, 2.5), "lamp_red")]


def m_lab_bench():
    return [e((0, 13, 1), (16, 15, 15), "metal_white"), e((1, 0, 2), (15, 13, 14), "metal_grey", faces={"north": "metal_white"}),
            e((3, 15, 6), (6, 16, 9), "metal_dark"), e((4, 16, 7), (5, 22, 8), "metal_dark",
            rot={"origin": [4.5, 16, 7.5], "axis": "x", "angle": -22.5}), e((3.5, 21, 5), (5.5, 23, 7), "metal_dark"),
            e((9, 15, 4), (11, 19, 6), "glass_dark"), e((12, 15, 8), (14, 18, 10), "specimen"), e((8, 15, 10), (11, 16, 12), "chrome")]


def m_fume_hood():
    return [e((0, 0, 2), (16, 12, 16), "metal_grey"), e((0, 12, 6), (16, 13, 16), "metal_white"),
            e((0, 13, 15), (16, 30, 16), "metal_white"), e((0, 13, 6), (1, 30, 16), "metal_white"), e((15, 13, 6), (16, 30, 16), "metal_white"),
            e((0, 26, 4), (16, 32, 16), "metal_white"), e((1, 18, 5.5), (15, 26, 6), "glass_dark"),
            e((5, 13, 9), (7, 17, 11), "specimen"), e((9, 13, 10), (12, 14, 12), "chrome"), e((6, 29, 9), (10, 32, 13), "chrome")]


def m_specimen_tank():
    return [e((3, 0, 3), (13, 2, 13), "metal_dark"), e((3.5, 2, 3.5), (12.5, 22, 12.5), "specimen"),
            e((3, 22, 3), (13, 24, 13), "metal_dark"), e((7, 24, 7), (9, 28, 9), "chrome"),
            e((2.5, 2, 2.5), (3.5, 22, 3.5), "chrome"), e((12.5, 2, 12.5), (13.5, 22, 13.5), "chrome"),
            e((2.5, 2, 12.5), (3.5, 22, 13.5), "chrome"), e((12.5, 2, 2.5), (13.5, 22, 3.5), "chrome")]


def m_bunk_bed():
    els = []
    for x in (0, 15):
        for z in (0, 15):
            els.append(e((x, 0, z), (x + 1, 28, z + 1), "metal_grey"))
    for y in (3, 17):
        els += [e((0, y, 0), (16, y + 1, 16), "metal_grey"), e((1, y + 1, 1), (15, y + 3, 15), "fabric_green"),
                e((2, y + 3, 11), (14, y + 4.5, 14.5), "metal_white")]
    els += [e((14.5, 4, 3), (15.5, 26, 4), "chrome"), e((14.5, 9, 3), (15.5, 10, 13), "chrome")]
    return els


def m_generator():
    return [e((0, 0, 2), (16, 2, 14), "metal_dark"), e((1, 2, 3), (15, 12, 13), "yellow", faces={"north": "metal_dark"}),
            e((3, 12, 5), (13, 13, 11), "metal_dark"), e((12, 12, 9), (14, 26, 11), "rust"), e((11.5, 24, 8.5), (14.5, 26, 11.5), "rust"),
            e((2, 5, 2.5), (8, 10, 3), "screen"), e((10, 6, 2.5), (11, 7, 3), "lamp_red"), e((-1, 3, 6), (1, 8, 10), "rust")]


def m_blast_door():
    els = [e((-8, 0, 13), (24, 32, 16), "metal_dark")]
    for (y0, y1, x0, x1) in ((2, 30, -6, 22), (4, 28, -7, 23), (6, 26, -8, 24)):
        els.append(e((x0, y0, 12), (x1, y1, 13), "metal_grey"))
    els += [e((6, 12, 11), (10, 20, 12), "chrome"), e((-4, 15.5, 11.5), (20, 16.5, 12), "chrome"),
            e((7.5, 4, 11.5), (8.5, 28, 12), "chrome"), e((-8, 0, 12), (24, 2, 13), "hazard"), e((-8, 30, 12), (24, 32, 13), "hazard")]
    return els


PROP_TEX = {"wood": "prop_wood", "metal_grey": "prop_metal_grey", "metal_white": "prop_metal_white", "metal_dark": "prop_metal_dark",
            "plastic_black": "prop_plastic_black", "screen": "prop_screen", "chrome": "prop_chrome", "fabric_blue": "prop_fabric_blue",
            "fabric_green": "prop_fabric_green", "rubber": "prop_rubber", "glass_dark": "prop_glass_dark", "red": "prop_red",
            "yellow": "prop_yellow", "orange": "prop_orange", "rust": "prop_rust", "sand": "prop_sand", "lamp_off": "prop_lamp_off",
            "lamp_red": "prop_lamp_red", "lamp_green": "prop_lamp_green", "lamp_amber": "prop_lamp_amber",
            "board": "prop_departure_board", "biohazard": "prop_biohazard", "barrier": "hazard_concrete", "fan": "prop_jet_fan",
            "bone": "skeleton_bone", "socket": "skeleton_socket", "poster_exodus": "prop_poster_exodus",
            "poster_quarantine": "prop_poster_quarantine", "frost": "prop_frost", "specimen": "prop_specimen",
            "hazard": "hazard_concrete", "olive": "prop_olive"}
for _p in ("car_red", "car_blue", "car_white", "car_yellow", "car_police", "car_ambulance"):
    PROP_TEX[_p] = "prop_" + _p


# ---------------------------------------------------------------- the table
# name: (kind, model/texture, outline box, options, en, de, hu)
#   kind: cube | facing_cube | prop.  Options: sound, light, collide (False: walk through), drops (None: itself).
def C(tex, sound="stone", **o):
    return ("cube", tex, None, dict(sound=sound, **o))


def F(tex, sound="stone", **o):
    return ("facing_cube", tex, None, dict(sound=sound, **o))


def P(fn, box, sound="metal", **o):
    return ("prop", fn, box, dict(sound=sound, **o))


BLOCKS = {
    "cracked_concrete": (C("cracked_concrete"), "Cracked Concrete", "Rissiger Beton", "Repedt beton"),
    "sooty_concrete": (C("sooty_concrete"), "Sooty Concrete", "Verrußter Beton", "Kormos beton"),
    "rebar_concrete": (C("rebar_concrete"), "Broken Concrete", "Gebrochener Stahlbeton", "Törött vasbeton"),
    "asphalt": (C("asphalt"), "Asphalt", "Asphalt", "Aszfalt"),
    "cracked_asphalt": (C("cracked_asphalt"), "Cracked Asphalt", "Rissiger Asphalt", "Repedt aszfalt"),
    "road_marking": (F("road_marking"), "Road Marking", "Fahrbahnmarkierung", "Útburkolati jel"),
    "runway_marking": (F("runway_marking"), "Runway Marking", "Pistenmarkierung", "Kifutópálya-jelölés"),
    "hazard_concrete": (C("hazard_concrete"), "Hazard Stripe Concrete", "Warnstreifen-Beton", "Figyelmeztető csíkos beton"),
    "floor_tiles": (C("floor_tiles"), "Floor Tiles", "Bodenfliesen", "Padlócsempe"),
    "wall_tiles": (C("wall_tiles"), "Hospital Wall Tiles", "Krankenhaus-Wandfliesen", "Kórházi falicsempe"),
    "office_carpet": (C("office_carpet", "wool"), "Office Carpet", "Büroteppich", "Irodai padlószőnyeg"),
    "curtain_wall": (C("curtain_wall", "glass"), "Curtain Wall", "Glasfassade", "Üvegfal"),
    "broken_curtain_wall": (C("broken_curtain_wall", "glass"), "Shattered Curtain Wall", "Zersplitterte Glasfassade", "Betört üvegfal"),
    "metal_grating": (C("metal_grating", "metal"), "Metal Grating", "Gitterrost", "Fémrács"),
    "ceiling_light": (C("ceiling_light", "glass", light=8), "Emergency Ceiling Light", "Notbeleuchtung", "Vészvilágítás"),
    "terminal_floor": (C("terminal_floor"), "Terminal Floor", "Terminalboden", "Terminálpadló"),
    "shipping_crate": (C("shipping_crate", "wood"), "Shipping Crate", "Frachtkiste", "Szállítóláda"),
    "office_desk": (P(m_office_desk, (0, 0, 0, 16, 14, 11), "wood"), "Office Desk", "Schreibtisch", "Irodai íróasztal"),
    "office_chair": (P(m_office_chair, (3, 0, 3, 13, 20, 14)), "Office Chair", "Bürostuhl", "Irodai szék"),
    "filing_cabinet": (P(m_filing_cabinet, (2, 0, 1, 14, 16, 15)), "Filing Cabinet", "Aktenschrank", "Iratszekrény"),
    "server_rack": (P(m_server_rack, FULL), "Server Rack", "Serverschrank", "Szerverszekrény"),
    "locker": (P(m_locker, (1, 0, 2, 15, 16, 15)), "Locker", "Spind", "Öltözőszekrény"),
    "hospital_bed": (P(m_hospital_bed, (1, 0, 0, 15, 11, 16)), "Hospital Bed", "Krankenhausbett", "Kórházi ágy"),
    "iv_stand": (P(m_iv_stand, (4, 0, 4, 12, 16, 12), collide=False), "IV Stand", "Infusionsständer", "Infúziós állvány"),
    "medical_cabinet": (P(m_medical_cabinet, (1, 0, 3, 15, 16, 16)), "Medical Cabinet", "Medizinschrank", "Gyógyszerszekrény"),
    "wheelchair": (P(m_wheelchair, (1, 0, 2, 15, 15, 14)), "Wheelchair", "Rollstuhl", "Kerekesszék"),
    "vending_machine": (P(m_vending_machine, (0, 0, 2, 16, 16, 16)), "Vending Machine", "Verkaufsautomat", "Italautomata"),
    "airport_seats": (P(m_airport_seats, (0, 0, 2, 16, 16, 13)), "Airport Seats", "Wartebank", "Váróterem-ülések"),
    "departure_board": (P(m_departure_board, (0, 0, 13, 16, 15, 16), light=5), "Departure Board", "Abflugtafel", "Indulási tábla"),
    "sandbags": (P(m_sandbags, (0, 0, 3, 16, 12, 13), "sand"), "Sandbags", "Sandsäcke", "Homokzsákok"),
    "barbed_wire": (P(m_barbed_wire, (0, 1, 1, 16, 15, 15)), "Barbed Wire", "Stacheldraht", "Szögesdrót"),
    "quarantine_barrier": (P(m_quarantine_barrier, (0, 0, 4, 16, 15, 12)), "Quarantine Barrier", "Quarantäne-Absperrung", "Karantén-sorompó"),
    "warning_sign": (P(m_warning_sign, (2, 0, 6, 14, 16, 9), collide=False), "Biohazard Sign", "Biogefährdungsschild", "Biológiai veszély tábla"),
    "poster_exodus": (P(lambda: m_poster("poster_exodus"), (1, 1, 15, 15, 15, 16), "wool", collide=False), "Exodus Poster", "Exodus-Plakat", "Exodus plakát"),
    "poster_quarantine": (P(lambda: m_poster("poster_quarantine"), (1, 1, 15, 15, 15, 16), "wool", collide=False), "Quarantine Notice", "Quarantäne-Aushang", "Karantén-hirdetmény"),
    "traffic_light": (P(m_traffic_light, (5, 0, 5, 11, 16, 11)), "Traffic Light", "Ampel", "Közlekedési lámpa"),
    "street_lamp": (P(m_street_lamp, (6, 0, 6, 10, 16, 10)), "Street Lamp", "Straßenlaterne", "Utcai lámpa"),
    "fire_hydrant": (P(m_fire_hydrant, (4, 0, 4, 12, 12, 12)), "Fire Hydrant", "Hydrant", "Tűzcsap"),
    "park_bench": (P(m_bench, (0, 0, 6, 16, 14, 14), "wood"), "Park Bench", "Parkbank", "Pad"),
    "car_wreck_red": (P(lambda: m_car("car_red"), (0, 0, 0, 16, 16, 16)), "Car Wreck", "Autowrack", "Autóroncs"),
    "car_wreck_blue": (P(lambda: m_car("car_blue"), (0, 0, 0, 16, 16, 16)), "Car Wreck (Blue)", "Autowrack (blau)", "Autóroncs (kék)"),
    "car_wreck_white": (P(lambda: m_car("car_white"), (0, 0, 0, 16, 16, 16)), "Car Wreck (White)", "Autowrack (weiß)", "Autóroncs (fehér)"),
    "taxi_wreck": (P(lambda: m_car("car_yellow"), (0, 0, 0, 16, 16, 16)), "Taxi Wreck", "Taxiwrack", "Taxironcs"),
    "police_car_wreck": (P(lambda: m_car("car_police", ("lamp_red", "prop_blue_dummy")), (0, 0, 0, 16, 16, 16)), "Police Car Wreck", "Polizeiwagenwrack", "Rendőrautó-roncs"),
    "ambulance_wreck": (P(lambda: m_car("car_ambulance", ("lamp_red", "lamp_red")), (0, 0, 0, 16, 16, 16)), "Ambulance Wreck", "Krankenwagenwrack", "Mentőautó-roncs"),
    "jet_engine": (P(m_jet_engine, FULL), "Jet Engine", "Strahltriebwerk", "Sugárhajtómű"),
    "runway_light": (P(m_runway_light, (5, 0, 5, 11, 4, 11), light=4, collide=False), "Runway Light", "Pistenfeuer", "Kifutófény"),
    "runway_bollard": (P(m_runway_bollard, (6, 0, 6, 10, 12, 10)), "Bollard", "Poller", "Terelőoszlop"),
    "dino_skull": (P(m_dino_skull, FULL, "bone"), "Tyrannosaurus Skull", "Tyrannosaurus-Schädel", "Tyrannosaurus-koponya"),
    "radio_set": (P(m_radio_set, (1, 0, 2, 15, 12, 13), cls="RadioSetBlock"), "Radio Set", "Funkgerät", "Rádió"),
    "cryo_pod": (P(m_cryo_pod, (2, 0, 2, 14, 16, 14), light=3), "Cryo Pod", "Kryokapsel", "Hibernációs fülke"),
    "lab_bench": (P(m_lab_bench, (0, 0, 1, 16, 15, 15)), "Lab Bench", "Labortisch", "Laborasztal"),
    "fume_hood": (P(m_fume_hood, (0, 0, 2, 16, 16, 16)), "Fume Hood", "Abzug", "Vegyifülke"),
    "specimen_tank": (P(m_specimen_tank, (3, 0, 3, 13, 16, 13), "glass", light=4), "Specimen Tank", "Probentank", "Mintatartály"),
    "bunk_bed": (P(m_bunk_bed, FULL), "Bunk Bed", "Etagenbett", "Emeletes ágy"),
    "diesel_generator": (P(m_generator, (0, 0, 2, 16, 13, 14), cls="SalvageMachineBlock"), "Diesel Generator", "Dieselgenerator", "Dízelaggregátor"),
    "recycler": (P(m_recycler, FULL, cls="SalvageMachineBlock"), "Recycler", "Recycler", "Bontógép"),
    "shredder_module": (P(m_shredder_module, (1, 0, 1, 15, 13, 15), cls="SalvageMachineBlock"), "Shredder Module", "Schreddermodul", "Darálómodul"),
    "smelter_module": (P(m_smelter_module, (1, 0, 1, 15, 13, 15), cls="SalvageMachineBlock"), "Smelter Module", "Schmelzmodul", "Olvasztómodul"),
    "sorter_module": (P(m_sorter_module, (0, 0, 1, 16, 15, 15), cls="SalvageMachineBlock"), "Sorter Module", "Sortiermodul", "Szétválogató modul"),
    "battery_bank": (P(m_battery_bank, (1, 0, 2, 15, 16, 15), cls="SalvageMachineBlock"), "Battery Bank", "Batteriebank", "Akkumulátortelep"),
    "blast_door": (P(m_blast_door, (0, 0, 12, 16, 16, 16)), "Blast Door", "Druckschutztür", "Robbanásbiztos ajtó"),
}
PROP_TEX["prop_blue_dummy"] = "prop_fabric_blue"


def write_models():
    for name, (spec, *_names) in BLOCKS.items():
        kind, src, box, opts = spec
        if kind == "cube":
            write(ASSETS / f"models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + src}})
            write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": NS + "block/" + name}}})
        elif kind == "facing_cube":
            write(ASSETS / f"models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + src}})
            facing_blockstate(name, "block/" + name)
        else:
            els = src()
            used = sorted({f["texture"][1:] for el in els for f in el["faces"].values()})
            tex = {k: "block/" + PROP_TEX.get(k, k) for k in used}
            tex["particle"] = tex[used[0]]
            for el in els:
                for i in range(3):
                    assert -16 <= el["from"][i] <= 32 and -16 <= el["to"][i] <= 32, (name, el)
            model("block/" + name, tex, els)
            facing_blockstate(name, "block/" + name)
        item_from_block(name, "block/" + name)


SOUNDS = {"stone": "STONE", "metal": "METAL", "wood": "WOOD", "glass": "GLASS", "wool": "WOOL", "sand": "SAND", "bone": "BONE_BLOCK"}


def write_java():
    lines = []
    for name, (spec, *_n) in BLOCKS.items():
        kind, _src, box, o = spec
        props = f"BlockBehaviour.Properties.of().strength({'0.8F' if o['sound'] in ('wool', 'glass') else '1.5F'}, 6.0F)" \
                f".sound(SoundType.{SOUNDS[o['sound']]})"
        if o.get("light"):
            props += f".lightLevel(state -> {o['light']})"
        const = name.upper()
        if kind == "cube":
            lines.append(f'\tpublic static final Block {const} = register("{name}", Block::new, {props});')
        elif kind == "facing_cube":
            lines.append(f'\tpublic static final Block {const} = register("{name}", p -> new ShipFacingBlock(p), {props});')
        else:
            props += ".noOcclusion()"
            if o.get("collide") is False:
                props += ".noCollision()"
            b = ", ".join(str(v) for v in box)
            cls = o.get("cls", "ShipFacingBlock")
            lines.append(f'\tpublic static final Block {const} = register("{name}",\n\t\t\tp -> new {cls}(p, new double[] {{{b}}}),\n\t\t\t{props});')
    src = '''package hu.kriwdi.intersolarsystem.content;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The urban block set of the dead Earth (building materials and props for cities, hospitals and airports).
 * GENERATED by tools/art/make_urban_assets.py: edit the table there, not this file.
 */
public final class UrbanBlocks {
''' + "\n\n".join(lines) + '''

	private UrbanBlocks() {
	}

	private static Block register(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties properties) {
		return ModBlocks.registerExternal(name, factory, properties);
	}

	/** Loads the class, which registers everything above. */
	public static void initialize() {
	}
}
'''
    JAVA.write_text(src)


def write_loot():
    for name in BLOCKS:
        drops = {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": NS + name}],
                                                       "condition": {"type": "minecraft:survives_explosion"}}],
                 "random_sequence": f"intersolarsystem:blocks/{name}"}
        path = DATA / f"loot_table/blocks/{name}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(drops, indent=2) + "\n")


def write_lang():
    s = LANG.read_text(encoding="utf-8")
    out = []
    for name, (_spec, en, de, hu) in BLOCKS.items():
        key = f"block.intersolarsystem.{name}"
        if f'"{key}"' in s:
            continue
        j = lambda t: json.dumps(t, ensure_ascii=False)  # noqa: E731
        out.append(f"\t\tadd(rows, {j(key)},\n\t\t\t\t{j(en)},\n\t\t\t\t{j(de)},\n\t\t\t\t{j(hu)});")
    if out:
        marker = "\n\t\tcheck(rows);"
        s = s.replace(marker, "\n" + "\n".join(out) + marker)
        LANG.write_text(s, encoding="utf-8")
    print("lang rows added:", len(out))


if __name__ == "__main__":
    tex_cracked_concrete()
    tex_sooty_concrete()
    tex_rebar_concrete()
    tex_asphalt("asphalt", 14)
    tex_asphalt("cracked_asphalt", 15, cracked=True)
    tex_road_marking()
    tex_hazard()
    tex_tiles("floor_tiles", "dcdcd4", "c4c4ba", "9a9a92")
    tex_tiles("wall_tiles", "a9c4b4", "8fae9c", "6e8a7a", size=5, seed=16)
    tex_carpet()
    tex_curtain_wall("curtain_wall", False)
    tex_curtain_wall("broken_curtain_wall", True)
    tex_grating()
    tex_ceiling_light()
    tex_terminal_floor()
    tex_crate()
    prop_textures()
    write_models()
    write_java()
    write_loot()
    write_lang()
    print(len(BLOCKS), "urban blocks")
