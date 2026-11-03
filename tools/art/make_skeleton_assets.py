"""Skeletons of the dead Earth: people in four poses (lying, sitting against a wall, curled up, in a space suit) and
animals (dog, cow, horse). Shaped models built from bone pieces; a skeleton lies with its head to the north.

    python3 tools/art/make_skeleton_assets.py
"""
import random

from PIL import Image

from make_textures import hexc, mix, save, shade
from make_models import NS, facing_blockstate, item_from_block, model


def textures():
    rnd = random.Random(31)
    bone = Image.new("RGBA", (16, 16))
    px = bone.load()
    for y in range(16):
        for x in range(16):
            c = mix(hexc("e3dccb"), hexc("b9ad92"), rnd.random() * 0.5)
            if (x * 7 + y * 3) % 11 == 0:
                c = shade(c, 0.86)
            px[x, y] = c
    for (x, y) in ((3, 4), (4, 5), (5, 5), (11, 10), (12, 11)):  # hairline cracks
        px[x, y] = hexc("8a7f68")
    save(bone, "block/skeleton_bone")
    dark = Image.new("RGBA", (16, 16))
    px = dark.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = mix(hexc("3b352b"), hexc("1e1a15"), rnd.random() * 0.6)  # eye sockets, nose, inside the skull
    save(dark, "block/skeleton_socket")
    suit = Image.new("RGBA", (16, 16))
    px = suit.load()
    for y in range(16):
        for x in range(16):
            c = mix(hexc("e8e6e0"), hexc("c4c0b6"), rnd.random() * 0.4)
            if y % 5 == 0:
                c = shade(c, 0.85)  # quilted seams
            if rnd.random() < 0.06:
                c = hexc("8f8574")  # dust and stains
            px[x, y] = c
    for x in range(16):
        px[x, 7] = hexc("d9822b")  # Aurora orange stripe
    save(suit, "block/skeleton_suit")
    visor = Image.new("RGBA", (16, 16))
    px = visor.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = mix(hexc("3a2f1e"), hexc("12100c"), (x + y) / 30)
            if x == y - 2 or x == 15 - y and 4 < y < 12:
                px[x, y] = hexc("c9c3b2")  # the crack across the gold visor
    for (x, y) in ((3, 2), (4, 2), (2, 3)):
        px[x, y] = hexc("d8b45a")
    save(visor, "block/skeleton_visor")


def b(frm, to, tex="bone", rotation=None, faces=None):
    """A bone piece: explicit uvs so pieces that reach outside the block still map onto the 16x16 texture."""
    dx, dy, dz = (min(16, abs(to[i] - frm[i])) for i in range(3))
    uv = {"north": (0, 0, dx, dy), "south": (0, 0, dx, dy), "east": (0, 0, dz, dy), "west": (0, 0, dz, dy),
          "up": (0, 0, dx, dz), "down": (0, 0, dx, dz)}
    el = {"from": list(frm), "to": list(to), "faces": {}}
    for d in ("north", "south", "east", "west", "up", "down"):
        t = faces.get(d, tex) if faces else tex
        el["faces"][d] = {"texture": "#" + t, "uv": list(uv[d])}
    if rotation:
        el["rotation"] = rotation
    return el


def skull(x, y, z, w=4, h=4, l=4, face="north", tilt=None):
    """A skull with dark eye sockets on the face side."""
    els = [b((x, y, z), (x + w, y + h, z + l), rotation=tilt)]
    if face == "north":
        els.append(b((x + 0.5, y + h * 0.45, z - 0.1), (x + w / 2 - 0.3, y + h * 0.75, z), "socket", rotation=tilt))
        els.append(b((x + w / 2 + 0.3, y + h * 0.45, z - 0.1), (x + w - 0.5, y + h * 0.75, z), "socket", rotation=tilt))
        els.append(b((x + 1, y, z - 0.6), (x + w - 1, y + h * 0.3, z), rotation=tilt))  # jaw
    elif face == "up":
        els.append(b((x + 0.5, y + h, z + 0.6), (x + w / 2 - 0.3, y + h + 0.1, z + 1.8), "socket"))
        els.append(b((x + w / 2 + 0.3, y + h, z + 0.6), (x + w - 0.5, y + h + 0.1, z + 1.8), "socket"))
        els.append(b((x + 1.4, y + h, z + 2.3), (x + w - 1.4, y + h + 0.1, z + 3), "socket"))
    return els


def ribs_flat(z0, z1, x0=5, x1=11, h=2.5, step=1.6):
    """A ribcage lying on its back: arches across the spine (two sides and the top of each rib)."""
    els = []
    z = z0
    while z + 0.8 <= z1:
        els += [b((x0, 0, z), (x0 + 0.8, h, z + 0.8)), b((x1 - 0.8, 0, z), (x1, h, z + 0.8)),
                b((x0, h - 0.8, z), (x1, h, z + 0.8))]
        z += step
    return els


def human_lying():
    els = skull(6, 0, -5, face="up")
    els.append(b((7.5, 0.3, -1), (8.5, 1.3, 12)))  # spine
    els += ribs_flat(0, 7.5)
    els.append(b((7.5, 2.2, 0), (8.5, 2.6, 7)))  # sternum
    els.append(b((5.5, 0, 11), (10.5, 2, 13.5)))  # pelvis
    for x in (5.5, 9.5):
        els += [b((x, 0.3, 13.5), (x + 1, 1.3, 20)), b((x, 0.3, 20.2), (x + 1, 1.2, 26.5)),
                b((x - 0.5, 0, 26.5), (x + 1.5, 1, 28.5))]
    els += [b((3.5, 0.3, 0), (4.5, 1.3, 6)), b((3.3, 0.3, 6.2), (4.3, 1.2, 12)), b((3, 0, 12), (4.8, 0.8, 14)),
            b((11.5, 0.3, 0), (12.5, 1.3, 6)), b((12.2, 0.3, 6), (13.2, 1.2, 11), rotation={"origin": [12, 0, 6], "axis": "y", "angle": -22.5}),
            b((12.5, 0, 11), (14.3, 0.8, 13))]
    return els


def human_sitting():
    tilt = {"origin": [8, 14, 13], "axis": "x", "angle": -22.5}  # the head has dropped forward
    els = skull(6, 14, 10, face="north", tilt=tilt)
    els.append(b((7.5, 2.5, 14), (8.5, 14.2, 15)))  # spine against the wall
    for y in (5.5, 7.3, 9.1, 10.9, 12.7):
        els += [b((5, y, 10.5), (5.8, y + 0.8, 15)), b((10.2, y, 10.5), (11, y + 0.8, 15)),
                b((5.8, y, 10.5), (10.2, y + 0.8, 11.3))]
    els.append(b((5.5, 0, 11), (10.5, 2.5, 14.5)))  # pelvis on the floor
    for x in (5.5, 9.5):
        els += [b((x, 0.3, 4), (x + 1, 1.3, 11)), b((x, 0.3, -3), (x + 1, 1.2, 4)), b((x - 0.5, 0, -5), (x + 1.5, 1.2, -3))]
    els += [b((3.5, 4, 11.5), (4.5, 12, 12.5)), b((3.5, 0.3, 6), (4.5, 1.3, 12)),  # left arm drops to the floor
            b((11.5, 4, 11.5), (12.5, 12, 12.5)), b((10.5, 3, 5), (11.5, 4, 11.5)),  # right arm in the lap
            b((9, 2.5, 4), (11, 3.3, 6))]
    return els


def human_curled():
    els = skull(2, 0, 3, face="north")
    els[0] = b((2, 0, 3), (6, 4, 7))
    els += [b((6, 0.8, 5), (7, 1.8, 10)), b((7, 0.8, 9.5), (10, 1.8, 10.5))]  # bent spine
    for z in (5.5, 7.1, 8.7):
        els += [b((5, 0, z), (9.5, 0.8, z + 0.8)), b((5, 4, z), (9.5, 4.8, z + 0.8)), b((9, 0.8, z), (9.8, 4, z + 0.8))]
    els.append(b((9, 0, 9.5), (12.5, 3, 12.5)))  # pelvis
    els += [b((9.5, 0.3, 4), (10.5, 1.3, 9.5)), b((10.5, 0.3, 3.5), (14.5, 1.3, 4.5)),  # knees pulled up
            b((11, 1.4, 5), (12, 2.4, 10)), b((12, 1.4, 4.5), (15, 2.3, 5.5)),
            b((3, 0.3, 7), (4, 1.3, 12)), b((4, 0.3, 2), (5, 1.3, 7)),  # arms around the knees
            b((6.5, 2, 2.5), (10, 2.8, 3.5))]
    return els


def human_suited():
    vis = {"up": "visor"}
    els = [b((4.5, 0, -7), (11.5, 6, -0.5), "suit", faces=vis),  # helmet, visor up (lying on the back)
           b((5, 6, -6), (11, 6.1, -1.5), "visor"),
           b((4, 0, -0.5), (12, 4.5, 11), "suit"),  # torso
           b((5, 0, 11), (11, 4, 13.5), "suit"),
           b((3.5, 0.5, 11.5), (12.5, 2.5, 12.5), "socket"),  # belt
           b((4.5, 4.5, 1), (11.5, 5.5, 8), "socket"),  # chest unit
           b((5.5, 4.6, 2), (7, 5.6, 3), "visor"),
           b((1.5, 0, 0), (4, 3, 11), "suit"), b((12, 0, 0), (14.5, 3, 10.5), "suit"),  # arms
           b((1.8, 0, 11), (3.8, 2, 13), "socket"), b((12.3, 0, 10.5), (14.3, 2, 12.5), "bone")]  # a bare hand
    for x in (4.5, 8.5):
        els += [b((x, 0, 13.5), (x + 3, 3, 25.5), "suit"), b((x - 0.3, 0, 25.5), (x + 3.3, 4.5, 29), "socket")]
    return els


def dog():
    els = [b((6, 0, -1), (10, 3.5, 3)), b((7, 0, -5), (9, 2, -1)),  # skull and snout
           b((6.2, 1.6, -0.1), (7.4, 2.6, 0), "socket"), b((8.6, 1.6, -0.1), (9.8, 2.6, 0), "socket"),
           b((7.5, 1, 3), (8.5, 2, 16))]
    for z in (4.5, 6, 7.5, 9, 10.5):  # a dog lies on its side: the ribs stand up
        els += [b((5.5, 0, z), (6.3, 4, z + 0.7)), b((9.7, 0, z), (10.5, 4, z + 0.7)), b((6.3, 3.3, z), (9.7, 4, z + 0.7))]
    els.append(b((6, 0, 14.5), (10, 2.5, 17)))
    els += [b((0.5, 0.3, 5.5), (6, 1.2, 6.4)), b((0.5, 0.3, 7.5), (6, 1.2, 8.4)),
            b((10, 0.3, 15), (15.5, 1.2, 15.9)), b((10, 0.3, 16.3), (15, 1.2, 17.2)),
            b((7.6, 0.3, 17), (8.4, 1.1, 24))]
    # A collar tag.
    els.append(b((6.5, 0, 3), (9.5, 0.4, 3.6), "visor"))
    return els


def big_animal(skull_len, horns, leg_len, neck=0):
    els = []
    z0 = -4 - neck
    els += [b((5, 0, z0 - skull_len), (11, 5, z0)), b((6, 0, z0 - skull_len - 3), (10, 3, z0 - skull_len)),
            b((5.4, 2.6, z0 - skull_len - 0.1), (7, 3.8, z0 - skull_len + 0.4), "socket"),
            b((9, 2.6, z0 - skull_len - 0.1), (10.6, 3.8, z0 - skull_len + 0.4), "socket")]
    if horns:
        els += [b((1.5, 3, z0 - 4), (5, 4, z0 - 3), rotation={"origin": [5, 3.5, z0 - 3.5], "axis": "y", "angle": 22.5}),
                b((11, 3, z0 - 4), (14.5, 4, z0 - 3), rotation={"origin": [11, 3.5, z0 - 3.5], "axis": "y", "angle": -22.5})]
    if neck:
        els.append(b((7.3, 1.2, z0), (8.7, 2.8, -4)))
    els.append(b((7.3, 5, -4), (8.7, 6.5, 18)))  # spine
    for z in range(-3, 13, 2):  # ribcage arches over the body
        els += [b((2, 0, z), (3, 5.5, z + 1)), b((13, 0, z), (14, 5.5, z + 1)), b((3, 5.5, z), (13, 6.5, z + 1))]
    els.append(b((4, 0, 15), (12, 4.5, 19)))  # pelvis
    for (x, z) in ((-1, -1), (14.5, 1), (-1, 16), (14.5, 18)):
        els.append(b((x, 0.3, z), (x + 2, 2, z + leg_len)))
    els.append(b((7.5, 2, 19), (8.5, 3, 27)))  # tail
    return els


BLOCKS = {
    "skeleton_lying": human_lying, "skeleton_sitting": human_sitting, "skeleton_curled": human_curled,
    "skeleton_suited": human_suited, "dog_skeleton": dog,
    "cow_skeleton": lambda: big_animal(7, True, 10),
    "horse_skeleton": lambda: big_animal(7, False, 13, neck=2),
}


if __name__ == "__main__":
    textures()
    tex = {"particle": "block/skeleton_bone", "bone": "block/skeleton_bone", "socket": "block/skeleton_socket",
           "suit": "block/skeleton_suit", "visor": "block/skeleton_visor"}
    for name, build in BLOCKS.items():
        els = build()
        for el in els:
            for i in range(3):
                assert -16 <= el["from"][i] <= 32 and -16 <= el["to"][i] <= 32, (name, el)
        model("block/" + name, tex, els)
        facing_blockstate(name, "block/" + name)
        item_from_block(name, "block/" + name)
    print("skeletons:", ", ".join(BLOCKS))
