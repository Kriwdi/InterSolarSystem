"""Entity textures of the dead Earth's creatures (phase 7): scavenger animals, feral drones and survivors.

The boxes here mirror the model layers in src/client/.../client/creature/*Model.java (same texOffs and sizes), so each
face is painted on its own: shaded fur that is darker on the belly, bristles and wool, eyes on the head's front face,
riveted and scratched plates on the drone. Seeded random, stable output.

    python3 tools/art/make_creature_textures.py
"""
import random

from PIL import Image

from make_textures import OUTLINE, TEX, hexc, mix, shade, value_noise


_CACHE = {}


def _cache(key, make):
    if key not in _CACHE:
        _CACHE[key] = make()
    return _CACHE[key]


class Box:
    """A cube's UV unwrap in Minecraft's layout, starting at (u, v), for a w x h x d box."""

    def __init__(self, u, v, w, h, d):
        self.u, self.v, self.w, self.h, self.d = u, v, w, h, d

    def faces(self):
        u, v, w, h, d = self.u, self.v, self.w, self.h, self.d
        return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
                "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def paint(img, box, fn):
    """fn(face, x, y, w, h) -> colour or None, with x, y inside the face."""
    px = img.load()
    for face, (fx, fy, fw, fh) in box.faces().items():
        for y in range(fh):
            for x in range(fw):
                c = fn(face, x, y, fw, fh)
                if c is not None:
                    px[fx + x, fy + y] = c


def fur(seed, base, belly=None, streak=0.12, noise=0.10):
    """Fur shader: value noise, vertical strands, lighter top and darker belly faces."""
    rnd = random.Random(seed)
    n = value_noise(seed, 64, 8)
    strands = [rnd.uniform(-streak, streak) for _ in range(64)]

    def fn(face, x, y, w, h, gx=0, gy=0):
        f = 1 + (n[(y + gy) % 64][(x + gx) % 64] - 0.5) * 2 * noise + strands[(x * 7 + gx) % 64] * (0.5 + (y % 3) / 4)
        c = base
        if face == "top":
            f *= 1.12
        elif face == "bottom" and belly is not None:
            c = belly
        elif face in ("left", "right"):
            f *= 0.92
        f *= 1 + rnd.uniform(-0.05, 0.05)
        return shade(c, f)
    return fn


def outline_edges(img, box, colour, alpha=0.35):
    """Darkens the outer pixel ring of every face slightly, so boxes read as volumes."""
    px = img.load()
    for _, (fx, fy, fw, fh) in box.faces().items():
        for y in range(fh):
            for x in range(fw):
                if x in (0, fw - 1) or y in (0, fh - 1):
                    px[fx + x, fy + y] = mix(px[fx + x, fy + y], colour, alpha)


def new():
    return Image.new("RGBA", (64, 64), (0, 0, 0, 0))


def save_entity(img, name):
    path = TEX / "entity" / (name + ".png")
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


def eye(px, x, y, iris, glint=True):
    px[x, y] = iris
    if glint:
        px[x, y] = shade(iris, 1.4)


# ---------------------------------------------------------------- rustback boar
def boar():
    img = new()
    px = img.load()
    hide = hexc("5b4232")
    belly = hexc("3e2e24")
    rust = hexc("9a4a1e")
    body = Box(0, 22, 10, 16, 9)
    paint(img, body, lambda f, x, y, w, h: _cache('fur(11, hide, belly)', lambda: fur(11, hide, belly))(f, x, y, w, h, 0, 22))
    # The back (local "back" face after the body's rotation) carries the rust-red stripe that names it.
    fx, fy, fw, fh = body.faces()["back"]
    for y in range(fh):
        for x in range(fw):
            t = 1 - abs(x - (fw - 1) / 2) / (fw / 2)
            if t > 0.35:
                px[fx + x, fy + y] = mix(px[fx + x, fy + y], shade(rust, 0.9 + 0.2 * ((x + y) % 3) / 2), t * 0.8)
    outline_edges(img, body, OUTLINE, 0.25)
    # Bristle ridge along the spine: dark roots, rusty tips.
    ridge = Box(38, 22, 3, 13, 2)
    rnd = random.Random(12)
    paint(img, ridge, lambda f, x, y, w, h: shade(mix(hexc("2a1c14"), rust, (1 - y / h) if f != "top" else 0.9),
                                                  rnd.uniform(0.8, 1.2)))
    # Head: same hide, small dark eyes with a red glint, scarred cheek.
    head = Box(0, 0, 8, 8, 7)
    paint(img, head, lambda f, x, y, w, h: _cache('fur(13, hide, belly, noise=0.08)', lambda: fur(13, hide, belly, noise=0.08))(f, x, y, w, h))
    fx, fy, fw, fh = head.faces()["front"]
    for ex in (1, 6):
        px[fx + ex, fy + 2] = hexc("140c08")
        px[fx + ex, fy + 3] = hexc("d23b1e") if ex == 1 else hexc("140c08")
    for i in range(3):  # scar
        px[fx + 5 + (i % 2), fy + 4 + i] = hexc("8a6a58")
    outline_edges(img, head, OUTLINE, 0.25)
    # Snout: pinkish-grey with two nostrils.
    snout = Box(30, 0, 5, 4, 3)
    paint(img, snout, lambda f, x, y, w, h: shade(hexc("8a6a62"), 1.05 if f == "top" else 0.95 - y * 0.03))
    fx, fy, fw, fh = snout.faces()["front"]
    px[fx + 1, fy + 1] = px[fx + 3, fy + 1] = hexc("2a1612")
    px[fx + 1, fy + 2] = px[fx + 3, fy + 2] = hexc("3e221c")
    # Tusks: yellowed ivory with a brown root.
    tusk = Box(46, 0, 1, 3, 1)
    paint(img, tusk, lambda f, x, y, w, h: hexc("e8dcb8") if y == 0 else hexc("cdbb8e") if y == 1 else hexc("7a5a3a"))
    ears = Box(50, 0, 2, 2, 1)
    paint(img, ears, lambda f, x, y, w, h: shade(hide, 0.8 if f == "front" else 1.0))
    # Legs: hide darkening to black hooves.
    legs = Box(0, 48, 4, 6, 4)
    paint(img, legs, lambda f, x, y, w, h: hexc("1e1612") if f == "bottom" or (f not in ("top",) and y >= h - 1)
          else _cache('fur(15, shade(hide, 0.85))', lambda: fur(15, shade(hide, 0.85)))(f, x, y, w, h))
    tail = Box(16, 48, 1, 4, 1)
    paint(img, tail, lambda f, x, y, w, h: shade(rust, 0.7) if y < 3 else hexc("2a1c14"))
    save_entity(img, "rustback_boar")


# ---------------------------------------------------------------- ash ram
def ram():
    img = new()
    px = img.load()
    wool = hexc("8e8a84")
    ash = hexc("6a6660")
    body = Box(0, 16, 9, 16, 8)
    rnd = random.Random(21)
    n = value_noise(21, 64, 8)
    curls = {}

    def fleece(f, x, y, w, h):
        # Matted wool: 2x2 curls with a lit top-left pixel, soot patches from the noise, darker underneath.
        key = (f, x // 2, y // 2, w, h)
        if key not in curls:
            curls[key] = rnd.uniform(0.85, 1.12)
        v = n[y % 64][(x * 3) % 64]
        c = mix(wool, ash, 0.65 if f == "bottom" else 0.1 + 0.35 * max(0.0, 0.45 - v) * 2)
        lit = 1.1 if (x % 2, y % 2) == (0, 0) else 0.9 if (x % 2, y % 2) == (1, 1) else 1.0
        return shade(c, curls[key] * lit)
    paint(img, body, fleece)
    outline_edges(img, body, OUTLINE, 0.2)
    # Head: short dark-grey hair, pale muzzle, amber slit eyes.
    head = Box(0, 0, 6, 6, 8)
    face = hexc("3b3936")
    paint(img, head, lambda f, x, y, w, h: _cache('fur(23, face, shade(face, 0.8), noise=0.08)', lambda: fur(23, face, shade(face, 0.8), noise=0.08))(f, x, y, w, h))
    fx, fy, fw, fh = head.faces()["front"]
    for x in range(1, 5):
        for y in range(3, 6):
            px[fx + x, fy + y] = shade(hexc("6e6862"), 1 - 0.05 * y)
    px[fx + 2, fy + 4] = px[fx + 3, fy + 4] = hexc("2a2624")
    for ex in (0, 5):
        px[fx + ex, fy + 1] = hexc("d9a03a")
    # Wool fringe over the forehead (top face, first rows of the front).
    fx, fy, fw, fh = head.faces()["top"]
    for y in range(fh):
        for x in range(fw):
            if y < 4:
                px[fx + x, fy + y] = fleece("top", x, y, fw, fh)
    outline_edges(img, head, OUTLINE, 0.25)
    # Horns: ridged, bone-grey, darker at the base.
    horn = Box(28, 0, 2, 4, 4)
    paint(img, horn, lambda f, x, y, w, h: shade(hexc("b7ab92"), 0.75 + 0.06 * y + (0.1 if (x + y) % 2 else 0)))
    tip = Box(40, 0, 2, 3, 2)
    paint(img, tip, lambda f, x, y, w, h: shade(hexc("cfc4ac"), 1.0 - 0.08 * y))
    legs = Box(0, 44, 3, 12, 3)
    paint(img, legs, lambda f, x, y, w, h: hexc("1c1a18") if f == "bottom" or y >= h - 2 and f != "top"
          else fleece(f, x, y, w, h) if y < 4 else _cache('fur(25, face)', lambda: fur(25, face))(f, x, y, w, h))
    tail = Box(12, 44, 2, 3, 2)
    paint(img, tail, fleece)
    save_entity(img, "ash_ram")


# ---------------------------------------------------------------- acid toad
def toad():
    img = new()
    px = img.load()
    skin = hexc("6f7a2a")
    belly = hexc("c9c27a")
    n = value_noise(31, 64, 10)
    rnd = random.Random(31)

    def warts(f, x, y, w, h, gx=0, gy=0):
        if f == "bottom":
            return shade(belly, 0.95 + rnd.uniform(-0.05, 0.05))
        v = n[(y + gy) % 64][(x + gx) % 64]
        c = mix(skin, hexc("3e4a18"), 0.5) if v < 0.35 else skin
        if rnd.random() < 0.08:
            c = hexc("b5d63a")  # acid-filled wart
        return shade(c, (1.1 if f == "top" else 0.95) * rnd.uniform(0.94, 1.06))
    body = Box(0, 0, 8, 5, 9)
    paint(img, body, warts)
    # Pale belly line down the sides.
    for side in ("left", "right"):
        fx, fy, fw, fh = body.faces()[side]
        for x in range(fw):
            px[fx + x, fy + fh - 1] = shade(belly, 0.85)
    outline_edges(img, body, OUTLINE, 0.25)
    sacs = Box(34, 0, 2, 2, 3)
    paint(img, sacs, lambda f, x, y, w, h: hexc("d8ff5a") if (x + y) % 3 else hexc("9ad61e"))
    head = Box(0, 14, 7, 3, 4)
    paint(img, head, lambda f, x, y, w, h: warts(f, x, y, w, h, 5, 9))
    fx, fy, fw, fh = head.faces()["front"]
    for x in range(fw):
        px[fx + x, fy + fh - 1] = hexc("2e3410")  # wide mouth line
    eyes = Box(22, 14, 2, 2, 2)
    paint(img, eyes, lambda f, x, y, w, h: hexc("e2d23a") if f == "front" and x == 0 and y == 0
          else hexc("1a1a0a") if f == "front" else shade(skin, 1.05))
    front = Box(0, 22, 2, 3, 2)
    paint(img, front, lambda f, x, y, w, h: warts(f, x, y, w, h, 3, 3))
    hind = Box(8, 22, 3, 2, 5)
    paint(img, hind, lambda f, x, y, w, h: warts(f, x, y, w, h, 7, 1))
    save_entity(img, "acid_toad")


# ---------------------------------------------------------------- sentry drone
def drone():
    img = new()
    px = img.load()
    rnd = random.Random(41)
    paint_col = hexc("6b6a3c")  # faded military olive
    steel = hexc("4a4f55")
    n = value_noise(41, 64, 12)

    def plate(f, x, y, w, h, gx=0, gy=0, base=paint_col):
        v = n[(y + gy) % 64][(x + gx) % 64]
        c = base
        if v < 0.22:
            c = mix(base, hexc("7a3b18"), 0.6)  # rust through the paint
        elif v > 0.7:
            c = mix(base, steel, 0.6)  # paint worn to bare metal
        f_ = 1.12 if f == "top" else 0.8 if f == "bottom" else 1.0
        if x in (0, w - 1) or y in (0, h - 1):
            f_ *= 0.78  # panel seams
        if (x, y) in ((1, 1), (w - 2, 1), (1, h - 2), (w - 2, h - 2)) and w > 4 and h > 3:
            return hexc("a8adb2")  # rivets
        return shade(c, f_ * rnd.uniform(0.95, 1.05))
    body = Box(0, 0, 10, 6, 10)
    paint(img, body, plate)
    # Hazard stripes on the front, a stencilled number on the sides.
    fx, fy, fw, fh = body.faces()["front"]
    for x in range(fw):
        for y in range(2, 4):
            px[fx + x, fy + y] = hexc("d4a52a") if (x + y) % 4 < 2 else hexc("1e1e1e")
    for side in ("left", "right"):
        fx, fy, fw, fh = body.faces()[side]
        for (x, y) in ((3, 2), (4, 2), (5, 2), (5, 3), (4, 4), (3, 4), (3, 3)):
            px[fx + x, fy + y] = hexc("c8c2a8")
    armour = Box(0, 16, 8, 1, 8)
    paint(img, armour, lambda f, x, y, w, h: plate(f, x, y, w, h, 9, 3, shade(paint_col, 0.9)))
    head = Box(32, 16, 6, 4, 5)
    paint(img, head, lambda f, x, y, w, h: plate(f, x, y, w, h, 20, 11, steel))
    fx, fy, fw, fh = head.faces()["front"]
    for x in range(1, 5):
        px[fx + x, fy + 3] = hexc("1a1c1e")  # vent grille
    lens = Box(40, 0, 3, 2, 1)
    paint(img, lens, lambda f, x, y, w, h: (hexc("ff4a2a") if x == 1 and y == 0 else hexc("d0201a"))
          if f == "front" else hexc("2a2c2e"))
    antenna = Box(48, 0, 1, 4, 1)
    paint(img, antenna, lambda f, x, y, w, h: hexc("ff5a3a") if y == 0 else hexc("7d848a"))
    legs = Box(0, 26, 12, 2, 2)
    paint(img, legs, lambda f, x, y, w, h: hexc("8a9096") if x % 4 == 0 and f in ("top", "front", "back")
          else shade(steel, 1.1 if f == "top" else 0.85) if x > 2 else hexc("2a2c2e"))
    save_entity(img, "sentry_drone")


# ---------------------------------------------------------------- survivor
def survivor():
    """Standard 64x64 humanoid layout: a patched hooded coat, a gas mask with round lenses, wrapped boots."""
    img = new()
    px = img.load()
    rnd = random.Random(51)
    coat = hexc("6a5a44")
    patch = hexc("4a5a52")
    cloth = hexc("3a3430")
    n = value_noise(51, 64, 16)

    def fabric(base, gx=0, gy=0):
        def fn(f, x, y, w, h):
            v = n[(y + gy) % 64][(x + gx) % 64]
            c = mix(base, patch, 0.8) if 0.45 < v < 0.5 else base
            if (x + 2 * y) % 7 == 0 and v > 0.6:
                c = shade(c, 0.75)  # stitching
            return shade(c, (1.08 if f == "top" else 0.9 if f == "bottom" else 1.0) * rnd.uniform(0.93, 1.05))
        return fn
    skin = hexc("a07a5a")
    head = Box(0, 0, 8, 8, 8)
    paint(img, head, fabric(hexc("4e4436"), 1, 1))  # hood wrap
    fx, fy, fw, fh = head.faces()["front"]
    for y in range(fh):
        for x in range(fw):
            if 1 <= y <= 3:
                px[fx + x, fy + y] = shade(skin, 0.9 + 0.05 * (x % 2))  # forehead and brows
            elif y >= 3:
                px[fx + x, fy + y] = shade(hexc("2c2f2e"), 1.0 + 0.04 * ((x + y) % 2))  # mask rubber
    for lx in (1, 5):  # round lenses
        for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)):
            px[fx + lx + dx, fy + 3 + dy] = hexc("8fb3b0") if (dx, dy) != (1, 1) else hexc("d8f0ee")
        px[fx + lx - 1 if lx == 1 else fx + lx + 2, fy + 3] = hexc("5a5f60")
    for (dx, dy) in ((3, 5), (4, 5), (3, 6), (4, 6), (2, 7), (3, 7), (4, 7), (5, 7)):  # filter canister
        px[fx + dx, fy + dy] = hexc("6a7064") if dy < 7 else hexc("4a4f46")
    outline_edges(img, head, OUTLINE, 0.2)
    hood = Box(32, 0, 8, 8, 8)
    paint(img, hood, lambda f, x, y, w, h: None if f == "front" or (f in ("left", "right") and y > 5 and x < 3)
          else _cache('fabric(coat, 7, 3)', lambda: fabric(coat, 7, 3))(f, x, y, w, h) if f != "bottom" else None)
    body = Box(16, 16, 8, 12, 4)
    paint(img, body, fabric(coat, 3, 5))
    fx, fy, fw, fh = body.faces()["front"]
    for y in range(fh):
        px[fx + 4, fy + y] = shade(coat, 0.7)  # coat opening
        if y == 7:
            for x in range(fw):
                px[fx + x, fy + y] = hexc("3a2a1a")  # belt
    px[fx + 4, fy + 7] = hexc("a8a090")
    px[fx + 1, fy + 2] = px[fx + 2, fy + 2] = hexc("c8b25a")  # tag
    arms = Box(40, 16, 4, 12, 4)
    paint(img, arms, lambda f, x, y, w, h: hexc("2a2622") if y >= h - 3 and f != "top"
          else _cache('fabric(coat, 9, 2)', lambda: fabric(coat, 9, 2))(f, x, y, w, h))
    legs = Box(0, 16, 4, 12, 4)
    paint(img, legs, lambda f, x, y, w, h: shade(hexc("4a4038"), 0.9 + 0.1 * ((y // 2) % 2)) if y >= h - 4
          else _cache('fabric(cloth, 2, 8)', lambda: fabric(cloth, 2, 8))(f, x, y, w, h))
    # Second layer (jacket over the body) left mostly transparent: a backpack on the back.
    jacket = Box(16, 32, 8, 12, 4)
    paint(img, jacket, lambda f, x, y, w, h: shade(hexc("4a5236"), 1.0 - 0.03 * y) if f == "back" and 1 <= x <= 6
          and 1 <= y <= 9 else None)
    # Left limbs (mirrored in the model; painted the same as the right ones).
    paint(img, Box(32, 48, 4, 12, 4), lambda f, x, y, w, h: hexc("2a2622") if y >= h - 3 and f != "top"
          else _cache('fabric(coat, 11, 4)', lambda: fabric(coat, 11, 4))(f, x, y, w, h))
    paint(img, Box(16, 48, 4, 12, 4), lambda f, x, y, w, h: shade(hexc("4a4038"), 0.9 + 0.1 * ((y // 2) % 2))
          if y >= h - 4 else _cache('fabric(cloth, 4, 6)', lambda: fabric(cloth, 4, 6))(f, x, y, w, h))
    save_entity(img, "survivor")


# ---------------------------------------------------------------- spawn eggs
def spawn_eggs():
    """Item icons: an egg shape in the creature's two colours with its own speckle pattern."""
    eggs = {"rustback_boar": ("5b4232", "9a4a1e"), "ash_ram": ("8e8a84", "3b3936"), "acid_toad": ("6f7a2a", "d8ff5a"),
            "sentry_drone": ("6b6a3c", "d0201a"), "survivor": ("6a5a44", "8fb3b0")}
    for name, (a, b) in eggs.items():
        base, spot = hexc(a), hexc(b)
        rnd = random.Random(name)
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        px = img.load()
        for y in range(16):
            for x in range(16):
                # Egg: wider at the bottom.
                cx, cy = 7.5, 8.8
                rx = 5.6 if y > cy else 5.6 - (cy - y) * 0.18
                ry = 6.8
                d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
                if d <= 1.0:
                    light = 1.25 - 0.35 * ((x - 4) ** 2 + (y - 5) ** 2) ** 0.5 / 8
                    px[x, y] = shade(base, light)
        for _ in range(9):
            x, y = rnd.randrange(3, 13), rnd.randrange(3, 15)
            if px[x, y][3]:
                px[x, y] = spot
                if rnd.random() < 0.5 and x + 1 < 16 and px[x + 1, y][3]:
                    px[x + 1, y] = shade(spot, 0.85)
        # Outline.
        out = img.copy()
        op = out.load()
        for y in range(16):
            for x in range(16):
                if not px[x, y][3] and any(0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3]
                                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    op[x, y] = OUTLINE
        path = TEX / "item" / (name + "_spawn_egg.png")
        out.save(path)


if __name__ == "__main__":
    boar()
    ram()
    toad()
    drone()
    survivor()
    spawn_eggs()
    print("creature textures written")
