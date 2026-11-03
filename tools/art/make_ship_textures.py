"""Textures of the ship's outer hull and viewports.

    python3 tools/art/make_ship_textures.py
"""
import math
import random

from PIL import Image

from make_textures import RUST, hexc, save, shade


def plating():
    rnd = random.Random(601)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    base = hexc("c9ccc8")
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, 1 + rnd.uniform(-0.035, 0.035))
    # Two panels per block: seams with a light top-left and dark bottom-right edge.
    for x in range(16):
        px[x, 0] = shade(base, 1.15)
        px[x, 15] = shade(base, 0.62)
        px[x, 8] = shade(base, 0.7)
        px[x, 9] = shade(base, 1.1)
    for y in range(16):
        px[0, y] = shade(base, 1.12)
        px[15, y] = shade(base, 0.66)
    for (x, y) in [(2, 2), (13, 2), (2, 6), (13, 6), (2, 11), (13, 11), (2, 13), (13, 13)]:
        px[x, y] = hexc("8d918e")
        px[x + 1, y + 1] = hexc("eef0ec")
    # Weathering: a rust streak from a rivet and scorch at the bottom.
    for i in range(5):
        px[13, 7 + i] = RUST[2] if i < 2 else shade(RUST[3], 1.1 + 0.05 * i)
    for x in range(16):
        if rnd.random() < 0.4:
            px[x, 14] = shade(px[x, 14], 0.8)
    save(img, "block/ship_plating")


def porthole():
    img = Image.open(__import__("make_textures").TEX / "block/ship_plating.png").convert("RGBA")
    px = img.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r < 5.2:
                t = (x + (15 - y)) / 30
                px[x, y] = (int(20 + 40 * t), int(40 + 70 * t), int(70 + 90 * t), 255)
            elif r < 6.4:
                px[x, y] = hexc("3a3f44") if (x + y) % 2 else hexc("5d646b")
            elif r < 7.1:
                px[x, y] = hexc("8d959c")
    for (x, y) in [(5, 4), (4, 5), (6, 4), (4, 6), (9, 10)]:  # reflection
        px[x, y] = hexc("cfe7ff")
    save(img, "block/ship_porthole")


def thruster():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r < 3:
                px[x, y] = hexc("ffd27a") if r < 1.6 else hexc("ff8a2b")
            elif r < 5:
                px[x, y] = hexc("2a2224")
            elif r < 6.2:
                px[x, y] = shade(hexc("4a5f8a"), 0.8 + 0.1 * math.sin(x + y))
            elif r < 7.2:
                px[x, y] = hexc("6b737a")
            else:
                px[x, y] = hexc("3a3f44")
    save(img, "block/ship_thruster")


def viewport():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    frame = hexc("4a5056")
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = frame
        for (x, y) in ((i, 1), (1, i)):
            px[x, y] = hexc("737b82")
        for (x, y) in ((i, 14), (14, i)):
            px[x, y] = hexc("2b2f33")
    for (x, y) in [(3, 2), (2, 3), (4, 2), (2, 4), (12, 13), (13, 12)]:
        px[x, y] = hexc("8d959c")
    # Faint glint lines on the glass (cut-out: everything else stays fully clear).
    for i in range(4):
        px[4 + i, 9 - i] = (220, 235, 255, 255)
    px[10, 5] = (220, 235, 255, 255)
    save(img, "block/ship_viewport")


if __name__ == "__main__":
    plating()
    porthole()
    thruster()
    viewport()
    print("ship textures written")
