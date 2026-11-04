"""Templates placed by code in space (not world generation): the wreck field around the ISS and the historic missions
and the Ark-1 colony on the Moon. Written to data/intersolarsystem/structure/orbit/*.nbt and moon/*.nbt; the Java
side (space/SiteTemplates.java) places them at fixed spots.

Orbit templates float: no ground. Moon templates: y = 0 is the regolith layer, with a few layers of fill below it
(the Moon is uneven) and air above it (clears the bumps).

    python3 tools/worldgen/space_sites.py
"""
import json
import math
from pathlib import Path

from make_structures import AIR, LGREY, WHITE, S, Build
from overhaul import bars, cylinder, disc, pane

HULL, FOIL = S("station_hull"), S("station_foil")


def sign(b, x, y, z, lines, facing="south"):
    """A standing sign with up to four lines (text components as plain NBT strings)."""
    msgs = (list(lines) + ["", "", "", ""])[:4]
    rot = {"south": 0, "west": 4, "north": 8, "east": 12}[facing]
    b.set(x, y, z, S("spruce_sign", rotation=rot, waterlogged="false"),
          {"id": "minecraft:sign", "is_waxed": 1,
           "front_text": {"messages": msgs, "color": "black", "has_glowing_text": 0},
           "back_text": {"messages": ["", "", "", ""], "color": "black", "has_glowing_text": 0}})


# ---------------------------------------------------------------- orbit
def tube(b, x0, x1, cy, cz, r, st, inside=AIR, cap=None):
    """A cylinder along x."""
    for x in range(x0, x1 + 1):
        for dy in range(-int(r) - 1, int(r) + 2):
            for dz in range(-int(r) - 1, int(r) + 2):
                d = math.hypot(dy, dz)
                if d <= r + 0.3:
                    end = x in (x0, x1)
                    shell = d > r - 0.9
                    b.set(x, cy + dy, cz + dz, (cap or st) if end else st if shell else inside)


def wing(b, x0, x1, y, z0, z1, broken=0.0):
    for x in range(x0, x1 + 1):
        for z in range(min(z0, z1), max(z0, z1) + 1):
            if b.rnd.random() >= broken:
                b.set(x, y, z, S("solar_panel"))


def halcyon():
    """HALCYON: an old orbital laboratory, decades older than the ISS, abandoned when its orbit was raised to keep it
    as a museum. Base block with a docking node and three modules (living, lab, airlock), one solar wing torn. The
    last crew left a log; the commander did not leave."""
    b = Build("orbit/halcyon", 4101)
    tube(b, 0, 20, 0, 0, 3.2, HULL, cap=FOIL)
    for x in range(1, 20, 4):
        disc_x = x
        for dy in range(-4, 5):
            for dz in range(-4, 5):
                if 3.2 <= math.hypot(dy, dz) <= 3.9:
                    b.set(disc_x, dy, dz, FOIL)
    # Node at the front with docking ports up, down and sideways.
    for (dy, dz) in ((4, 0), (-4, 0), (0, 4), (0, -4)):
        for k in range(1, 4):
            b.set(-1 - (0 if dy or dz else k), dy + (k if dy > 0 else -k if dy < 0 else 0),
                  dz + (k if dz > 0 else -k if dz < 0 else 0), HULL)
    for x in range(-4, 0):
        for dy in range(-2, 3):
            for dz in range(-2, 3):
                if math.hypot(dy, dz) <= 2.3:
                    b.set(x, dy, dz, HULL if math.hypot(dy, dz) > 1.4 else AIR)
    b.set(-4, 0, 0, S("iron_trapdoor", facing="west", half="bottom", open="true", powered="false", waterlogged="false"))
    # Side module (lab) at x 8 going +z.
    for z in range(4, 16):
        for dx in range(-3, 4):
            for dy in range(-3, 4):
                d = math.hypot(dx, dy)
                if d <= 2.6:
                    b.set(8 + dx, dy, z, HULL if d > 1.7 or z == 15 else AIR)
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            b.set(8 + dx, dy, 3, AIR)
            b.set(8 + dx, dy, 2, AIR)
    # Solar wings: one intact, one torn and bent away.
    for k in range(4, 15):
        wing(b, 12, 15, 0, k, k)
    for y in range(4, 6):
        b.set(13, y, 0, S("truss"))
    wing(b, 11, 16, 6, -1, 1, 0.1)
    wing(b, 3, 6, 0, -5, -12, 0.35)
    for k in range(13, 18):
        b.set(5, 1 + (k - 13), -k, S("solar_panel"))
    # Interior of the base block: bunks, panels, a table, the commander, the log terminal.
    for x in range(2, 19, 3):
        b.set(x, 2, 0, S("station_light"))
    for x in (3, 5):
        b.set(x, -1, 2, S("bunk_bed", facing="north"))
    b.set(10, -1, -2, S("control_cabinet", facing="south"))
    b.set(12, -1, -2, S("server_rack", facing="south"))
    b.set(14, -1, -2, S("data_terminal", facing="south", log=14))
    b.set(14, -1, 2, S("radio_set", facing="north"))
    b.set(16, -1, 0, S("smooth_stone_slab", type="top", waterlogged="false"))
    b.set(13, -1, 0, S("skeleton_suited", facing="south"))
    b.chest(18, -1, -1, "space_debris", "west", "barrel")
    b.chest(8, -1, 13, "lab", "north")
    b.set(7, -1, 10, S("lab_bench", facing="east"))
    b.set(9, -1, 8, S("specimen_tank"))
    b.set(8, 1, 12, S("sparking_panel"))
    b.set(8, -1, 6, S("locker", facing="south"))
    return b


def dead_satellite():
    """A geostationary relay pushed down to a graveyard orbit that decayed: gold-foiled bus, two long wings, the big
    dish, thruster pods, an apogee motor."""
    b = Build("orbit/dead_satellite", 4102)
    for x in range(-2, 3):
        for y in range(-2, 3):
            for z in range(-2, 3):
                edge = abs(x) == 2 or abs(y) == 2 or abs(z) == 2
                b.set(x, y, z, (FOIL if (x + y + z) % 3 else S("station_radiator")) if edge else AIR)
    b.chest(0, -1, 0, "space_debris", "north", "barrel")
    b.set(0, 1, 0, S("server_rack", facing="north"))
    b.set(2, 0, 0, AIR)
    for x in range(3, 18):
        for z in (-1, 0, 1):
            b.set(x, 0, z, S("solar_panel") if x % 5 else S("truss"))
            b.set(-x, 0, z, S("solar_panel") if (x % 5 and b.rnd.random() > 0.15) else S("truss") if x % 5 == 0
                  else AIR)
    for dx in range(-4, 5):
        for dz in range(-4, 5):
            d = math.hypot(dx, dz)
            if d <= 4.3:
                b.set(dx, 3 + int(round(d * d / 7)), dz + 0, WHITE if d < 3.6 else LGREY)
    b.set(0, 3, 0, bars())
    b.set(0, 4, 0, bars())
    for (x, z) in ((-2, -3), (2, -3), (-2, 3), (2, 3)):
        b.set(x, -2, z, S("lander_engine"))
    b.set(0, -3, 0, S("lander_engine"))
    return b


def upper_stage():
    """A spent upper stage, tumbling: the tank section, the engine bell, the payload adapter ring, a strap of foil."""
    b = Build("orbit/upper_stage", 4103)
    for i in range(0, 16):
        x = i
        y = i // 3
        for dy in range(-3, 4):
            for dz in range(-3, 4):
                d = math.hypot(dy, dz)
                if d <= 2.4:
                    b.set(x, y + dy, dz, (S("black_concrete") if i % 7 == 0 else WHITE) if d > 1.5 or i in (0, 15)
                          else AIR)
    for dy in range(-2, 3):
        for dz in range(-2, 3):
            if math.hypot(dy, dz) <= 2.2:
                b.set(-1, dy, dz, S("sooty_concrete") if math.hypot(dy, dz) > 1 else S("lander_engine"))
    b.set(-2, 0, 0, S("lander_engine"))
    for dy in range(-3, 4):
        for dz in range(-3, 4):
            if 2.6 <= math.hypot(dy, dz) <= 3.4:
                b.set(16, 5 + dy, dz, S("iron_block"))
    for k in range(0, 8):
        b.set(6 + k, 5 + k // 2, 3, FOIL)
    b.set(8, 2, -2, AIR)
    b.chest(8, 2, 0, "space_debris", "north", "barrel")
    return b


def space_telescope():
    """A great space telescope: the tube with the aperture door open, the aft shroud with instruments, two flat solar
    wings, the high-gain antennas. Its last image is still in the buffer."""
    b = Build("orbit/space_telescope", 4104)
    tube(b, 0, 14, 0, 0, 2.6, S("light_gray_concrete"), cap=FOIL)
    for x in range(0, 15, 3):
        for dy in range(-3, 4):
            for dz in range(-3, 4):
                if 2.5 <= math.hypot(dy, dz) <= 3.0:
                    b.set(x, dy, dz, FOIL)
    for dy in range(-2, 3):
        for dz in range(-2, 3):
            if math.hypot(dy, dz) <= 2.0:
                b.set(14, dy, dz, AIR)
    for dy in range(0, 5):
        for dz in range(-2, 3):
            b.set(15 + dy // 2, 3 + dy, dz, S("light_gray_concrete"))
    for x in range(-5, 0):
        for dy in range(-3, 4):
            for dz in range(-3, 4):
                d = math.hypot(dy, dz)
                if d <= 3.0:
                    b.set(x, dy, dz, FOIL if d > 2.1 or x == -5 else AIR)
    for x in range(-4, 0):
        b.set(x, -1, 0, S("server_rack", facing="north"))
    b.chest(-2, 1, 0, "lab", "south")
    b.set(-1, 0, 2, S("data_terminal", facing="north", log=6))
    for x in range(-3, 9):
        for k in range(4, 8):
            b.set(x, 0, k, S("solar_panel"))
            b.set(x, 0, -k, S("solar_panel"))
    for (y, z) in ((3, 1), (-3, -1)):
        b.set(6, y + (1 if y > 0 else -1), z, bars())
        b.set(6, y + (2 if y > 0 else -2), z, S("antenna_dish"))
    return b


def capsule_adrift():
    """A crew transport adrift: the orbital module (a sphere), the descent capsule, the service module with its two
    wings. One crew member strapped in; the hatch to the orbital module is open."""
    b = Build("orbit/capsule_adrift", 4105)
    for x in range(-3, 4):
        for y in range(-3, 4):
            for z in range(-3, 4):
                d = math.sqrt(x * x + y * y + z * z)
                if d <= 3.0:
                    b.set(x, y, z, S("green_terracotta") if d > 2.2 else AIR)
    for x in range(4, 9):
        r = 2.0 + (x - 4) * 0.45
        for y in range(-4, 5):
            for z in range(-4, 5):
                d = math.hypot(y, z)
                if d <= r:
                    b.set(x, y, z, S("green_terracotta") if d > r - 1 or x == 8 else AIR)
    b.set(3, 0, 0, AIR)
    b.set(4, 0, 0, AIR)
    tube(b, 9, 14, 0, 0, 2.2, S("light_gray_concrete"), inside=S("iron_block"), cap=S("light_gray_concrete"))
    for x in range(10, 14):
        for z in range(3, 9):
            b.set(x, 0, z, S("solar_panel"))
            b.set(x, 0, -z, S("solar_panel"))
    for (dy, dz) in ((0, 0), (1, 1), (-1, -1)):
        b.set(15, dy, dz, S("lander_engine"))
    b.set(6, -1, 0, S("astronaut_seat", facing="west"))
    b.set(6, 0, 0, S("skeleton_suited", facing="west"))
    b.set(6, -1, 1, S("astronaut_seat", facing="west"))
    b.set(6, -1, -1, S("flight_panel", facing="west"))
    b.chest(-1, -2, 0, "space_debris", "north", "barrel")
    b.set(0, 2, 0, S("station_light"))
    return b


def debris_cloud(seed):
    """A loose cloud of fragments: stepping stones for a tethered EVA (each piece is something to clip onto)."""
    b = Build(f"orbit/debris_cloud_{seed}", 4200 + seed)
    pieces = [HULL, FOIL, S("solar_panel"), S("truss"), S("scrap_pile"), S("station_radiator"), S("iron_block"),
              S("rusted_panel")]
    for _ in range(26):
        x, y, z = b.rnd.randint(-9, 9), b.rnd.randint(-5, 5), b.rnd.randint(-9, 9)
        st = b.rnd.choice(pieces)
        size = b.rnd.randint(1, 3)
        for dx in range(size):
            for dz in range(size if st != S("truss") else 1):
                b.set(x + dx, y, z + dz, st)
    b.set(0, 0, 0, S("truss"))
    return b


# ---------------------------------------------------------------- moon
def pad(b, r, fill=4, clear=10):
    """A patch of regolith at y = 0 (radius r) with fill below and air above."""
    for x in range(-r - 1, r + 2):
        for z in range(-r - 1, r + 2):
            d = math.hypot(x, z)
            if d <= r + 0.5:
                b.set(x, 0, z, S("fresh_regolith") if b.rnd.random() < 0.5 else S("lunar_regolith"))
                for y in range(1, fill + 1):
                    b.set(x, -y, z, S("lunar_regolith"))
                for y in range(1, clear + 1):
                    b.set(x, y, z, AIR)


def tracks(b, x0, z0, x1, z1):
    """Wheel tracks in the dust."""
    steps = max(abs(x1 - x0), abs(z1 - z0))
    for i in range(steps + 1):
        x = round(x0 + (x1 - x0) * i / steps)
        z = round(z0 + (z1 - z0) * i / steps)
        b.set(x, 0, z, S("mare_regolith"))


def legs(b, cx, cz, y_top, spread, st=None):
    for (dx, dz) in ((-spread, -spread), (spread, -spread), (-spread, spread), (spread, spread)):
        for y in range(1, y_top):
            b.set(cx + dx, y, cz + dz, st or bars())
        b.set(cx + dx, 1, cz + dz, S("iron_trapdoor", facing="north", half="bottom", open="false", powered="false",
                                     waterlogged="false"))


def luna9():
    b = Build("moon/luna9", 4301)
    pad(b, 5, 2, 6)
    # The sphere and its four opened petals.
    for (dx, dz, f) in ((1, 0, "east"), (-1, 0, "west"), (0, 1, "south"), (0, -1, "north")):
        b.set(dx, 1, dz, S("iron_trapdoor", facing=f, half="bottom", open="false", powered="false",
                            waterlogged="false"))
        b.set(dx * 2, 1, dz * 2, S("iron_trapdoor", facing=f, half="bottom", open="false", powered="false",
                                    waterlogged="false"))
    b.set(0, 1, 0, S("iron_block"))
    b.set(0, 2, 0, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    b.set(1, 2, 1, S("end_rod", facing="up"))
    b.set(-1, 2, -1, S("end_rod", facing="up"))
    sign(b, 3, 1, 3, ["LUNA 9", "First soft landing", "3 Feb 1966", ""])
    return b


def surveyor3():
    b = Build("moon/surveyor3", 4302)
    pad(b, 6, 2, 8)
    for (dx, dz) in ((0, -2), (-2, 2), (2, 2)):
        for y in (1, 2):
            b.set(dx, y, dz, bars())
        b.set(dx, 1, dz, S("iron_trapdoor", facing="north", half="bottom", open="false", powered="false",
                            waterlogged="false"))
    for (x, z) in ((0, 0), (-1, 1), (1, 1), (0, -1)):
        b.set(x, 3, z, S("light_gray_concrete"))
    b.set(0, 4, 0, S("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false"))
    for y in (5, 6):
        b.set(0, y, 0, bars())
    b.set(0, 7, 0, S("solar_panel"))
    b.set(1, 4, 0, S("antenna_dish"))
    b.set(-1, 4, -1, S("observer", facing="north", powered="false"))  # the TV camera
    # The scoop's trenches.
    for x in range(2, 5):
        b.set(x, 0, -2, S("mare_regolith"))
    sign(b, 4, 1, 3, ["SURVEYOR 3", "Visited by Apollo 12", "1967", ""])
    return b


def lunokhod(n):
    """The lander stage with its ramps, and the rover a little way off at the end of its tracks."""
    b = Build(f"moon/lunokhod{n}", 4303 + n)
    pad(b, 10, 2, 8)
    for x in range(-2, 3):
        for z in range(-2, 3):
            b.set(x, 2, z, S("iron_block") if abs(x) == 2 or abs(z) == 2 else S("light_gray_concrete"))
    legs(b, 0, 0, 2, 2)
    for k in range(0, 3):
        b.set(3 + k, 2 - (k + 1) // 2, -1, S("iron_trapdoor", facing="east", half="top" if k == 0 else "bottom",
                                            open="false", powered="false", waterlogged="false"))
        b.set(3 + k, 2 - (k + 1) // 2, 1, S("iron_trapdoor", facing="east", half="top" if k == 0 else "bottom",
                                           open="false", powered="false", waterlogged="false"))
    for x in range(-1, 2):
        b.set(x, 3, -1, S("toxic_barrel") if x == 0 else S("light_gray_concrete"))
    # The rover: a tub on eight wheels, the lid open with its solar cells, the antenna.
    rx, rz = 6, 6
    tracks(b, 5, 0, rx, rz)
    tracks(b, 5, 1, rx - 1, rz + 1)
    for dx in (-1, 0, 1, 2):
        for dz in (-1, 2):
            b.set(rx + dx, 1, rz + dz, S("rover_wheel", facing="east"))
    for dx in (-1, 0, 1, 2):
        for dz in (0, 1):
            b.set(rx + dx, 1, rz + dz, S("gray_concrete"))
            b.set(rx + dx, 2, rz + dz, S("smooth_stone_slab", type="bottom", waterlogged="false"))
    for dz in (0, 1):
        b.set(rx - 2, 2, rz + dz, S("solar_panel"))
        b.set(rx - 3, 3, rz + dz, S("solar_panel"))
    b.set(rx + 2, 3, rz, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    b.set(rx + 3, 1, rz, S("observer", facing="east", powered="false"))
    name = "LUNA 17 / LUNOKHOD 1" if n == 1 else "LUNA 21 / LUNOKHOD 2"
    year = "1970" if n == 1 else "1973"
    sign(b, -4, 1, 4, [name.split(" / ")[0], name.split(" / ")[1], "First rover on" if n == 1 else
                       "Drove 39 km", "another world " + year if n == 1 else "in " + year])
    return b


def change(n):
    """A lander with its four legs and solar wings, and the six-wheeled rover beside it."""
    b = Build(f"moon/change{n}", 4310 + n)
    pad(b, 9, 2, 8)
    legs(b, 0, 0, 3, 2)
    for x in range(-2, 3):
        for y in (3, 4):
            for z in range(-2, 3):
                b.set(x, y, z, FOIL if (x + z + y) % 3 else S("gold_block"))
    for z in range(-1, 2):
        for k in range(3, 7):
            b.set(k, 4, z, S("solar_panel"))
            b.set(-k, 4, z, S("solar_panel"))
    b.set(0, 5, 0, S("antenna_dish"))
    for k in range(1, 4):
        b.set(0, 3 - k + 1, 3 + k, S("iron_trapdoor", facing="south", half="bottom", open="false", powered="false",
                                       waterlogged="false")) if k < 3 else None
    rx, rz = 3, 8
    tracks(b, 0, 5, rx, rz)
    for dx in (-1, 0, 1):
        for dz in (-1, 1):
            b.set(rx + dx, 1, rz + dz, S("rover_wheel", facing="east"))
    b.set(rx, 1, rz, S("gold_block"))
    b.set(rx + 1, 1, rz, FOIL)
    b.set(rx - 1, 1, rz, FOIL)
    for dz in (-1, 1):
        b.set(rx, 2, rz + dz * 2, S("solar_panel"))
    b.set(rx + 1, 2, rz, S("observer", facing="east", powered="false"))
    lines = ["CHANG'E 3", "lander and", "Yutu rover", "2013"] if n == 3 else \
        ["CHANG'E 4", "First landing on", "the far side", "2019"]
    sign(b, -5, 1, 5, lines)
    return b


def chandrayaan3():
    b = Build("moon/chandrayaan3", 4320)
    pad(b, 8, 2, 8)
    legs(b, 0, 0, 3, 2)
    for x in range(-2, 3):
        for y in (3, 4, 5):
            for z in range(-2, 3):
                b.set(x, y, z, S("gold_block") if (x, z) in ((2, 0), (-2, 0)) else FOIL)
    for x in range(-2, 3):
        b.set(x, 5, 3, S("solar_panel"))
        b.set(x, 4, 3, S("solar_panel"))
    b.set(0, 6, 0, S("antenna_dish"))
    # The ramp and the small rover at its foot.
    for k in range(0, 3):
        b.set(3 + k, 3 - k, 0, S("iron_trapdoor", facing="east", half="bottom", open="false", powered="false",
                                  waterlogged="false"))
    for dx in (0, 1, 2):
        for dz in (-1, 1):
            b.set(6 + dx, 1, dz, S("rover_wheel", facing="east"))
        b.set(6 + dx, 1, 0, FOIL)
    b.set(7, 2, 0, S("solar_panel"))
    tracks(b, 9, 0, 13, 2)
    sign(b, -5, 1, 4, ["CHANDRAYAAN-3", "Vikram + Pragyan", "Near the south pole", "2023"])
    return b


def slim():
    """The precision lander that touched down exactly on target and then tipped onto its nose."""
    b = Build("moon/slim", 4321)
    pad(b, 6, 2, 6)
    for y in range(1, 4):
        for x in (-1, 0, 1):
            for z in (-1, 0, 1):
                b.set(x, y, z, FOIL if y < 3 else S("gold_block"))
    b.set(0, 4, 0, S("lander_engine"))
    for x in range(-1, 2):
        b.set(x, 2, 2, S("solar_panel"))
        b.set(x, 3, 2, S("solar_panel"))
    for (dx, dz) in ((2, 0), (-2, 0)):
        b.set(dx, 3, dz, bars())
    b.set(3, 1, 3, S("iron_block"))
    b.set(-3, 1, -2, S("iron_block"))
    sign(b, 4, 1, -3, ["SLIM", "Landed on target,", "nose down", "2024"])
    return b


def luna24():
    b = Build("moon/luna24", 4322)
    pad(b, 5, 2, 8)
    legs(b, 0, 0, 3, 2)
    for x in range(-1, 2):
        for z in range(-1, 2):
            for y in (3, 4):
                b.set(x, y, z, S("light_gray_concrete") if y == 3 else S("iron_block"))
    for (dx, dz) in ((2, 0), (-2, 0), (0, 2), (0, -2)):
        b.set(dx, 3, dz, S("toxic_barrel"))
    b.set(0, 5, 0, bars())
    b.set(0, 6, 0, bars())
    for y in range(-3, 1):
        b.set(1, y, 1, S("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false"))
    sign(b, 3, 1, 3, ["LUNA 24", "Brought soil home", "1976", ""])
    return b


def ark1_colony():
    """ARK-1 at Shackleton: the first Ark set down on the crater rim and became the colony. The ship stands on its
    legs at the centre; around it, regolith-shielded habitat domes joined by tunnels, the cryo hall where 380
    colonists sleep (waiting for Ark-5), the ice mine at the crater edge, polar solar towers, landing pads, the
    long rail of the mass driver, the comms mast turned to an Earth that stopped answering."""
    b = Build("moon/ark1_colony", 4400)
    R = 58
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            d = math.hypot(x, z)
            if d <= R:
                b.set(x, 0, z, S("fresh_regolith") if b.rnd.random() < 0.4 else S("lunar_regolith"))
                for y in range(1, 4):
                    b.set(x, -y, z, S("lunar_regolith"))
                for y in range(1, 14):
                    b.set(x, y, z, AIR)
    # The Ark: a long hull lying on six legs, nose to the north (-z), engines south.
    for z in range(-30, 16):
        r = 6.2 if -24 < z < 10 else max(2.0, 6.2 - (abs(z) - (24 if z < 0 else 10)) * 0.7)
        for dx in range(-7, 8):
            for dy in range(-7, 8):
                d = math.hypot(dx, dy)
                if d <= r:
                    st = AIR
                    if d > r - 1:
                        st = HULL if (z + dx) % 9 else FOIL
                        if -18 < z < 6 and dy == 3 and abs(dx) > 4 and z % 3 == 0:
                            st = S("station_window")
                    b.set(dx, 12 + dy, z, st)
    for (dx, dz) in ((-6, -20), (6, -20), (-7, -4), (7, -4), (-6, 10), (6, 10)):
        for y in range(1, 9):
            b.set(dx + (1 if dx > 0 else -1) * (y < 4), y, dz, S("truss") if y % 3 else S("iron_block"))
        b.set(dx + (1 if dx > 0 else -1), 1, dz, S("iron_block"))
    for (dx, dy) in ((0, 0), (-3, 0), (3, 0), (0, -3), (0, 3)):
        b.set(dx, 12 + dy, 17, S("lander_engine"))
    # Ramp down from the cargo door on the east side.
    for k in range(0, 9):
        for dz in (-2, -1, 0, 1, 2):
            b.set(7 + k, 8 - k, dz, S("metal_grating"))
    for y in range(8, 12):
        for dz in (-2, -1, 0, 1, 2):
            b.set(6, y, dz, AIR)
    # Inside the Ark: the bridge forward, cargo deck aft.
    for z in range(-22, 8):
        for dx in range(-4, 5):
            b.set(dx, 7, z, S("metal_grating"))
    b.set(0, 8, -21, S("mothership_helm", facing="north"))
    b.set(-2, 8, -20, S("navigation_console", facing="north"))
    b.set(2, 8, -20, S("data_terminal", facing="north", log=15))
    b.set(0, 8, -18, S("astronaut_seat", facing="north"))
    for z in range(-6, 6, 3):
        for dx in (-3, 3):
            b.set(dx, 8, z, S("shipping_crate"))
            b.set(dx, 9, z, S("shipping_crate"))
    b.chest(-3, 8, 6, "space_debris", "east")
    for z in range(-20, 6, 5):
        b.set(0, 11, z, S("station_light"))
    # Habitat domes (regolith mounded over), joined by tunnels to the Ark's ramp.
    domes = [(-34, -20, "dorm"), (-36, 14, "cryo"), (30, 26, "greenhouse"), (34, -24, "workshop")]
    for (cx, cz, kind) in domes:
        r = 9
        for x in range(-r - 3, r + 4):
            for z in range(-r - 3, r + 4):
                for y in range(1, r + 3):
                    d = math.sqrt(x * x + (y * 1.2) ** 2 + z * z)
                    if d <= r - 1:
                        b.set(cx + x, y, cz + z, AIR)
                    elif d <= r:
                        b.set(cx + x, y, cz + z, HULL)
                    elif d <= r + 2.2 and kind != "greenhouse":
                        b.set(cx + x, y, cz + z, S("lunar_regolith"))
                    elif d <= r + 0.6 and kind == "greenhouse":
                        b.set(cx + x, y, cz + z, S("station_glass"))
                if math.hypot(x, z) <= r - 1:
                    b.set(cx + x, 0, cz + z, S("polished_andesite"))
        b.set(cx, 7, cz, S("station_light"))
        # Tunnel to the ramp foot at (16, 0).
        x, z = cx, cz
        while abs(x - 16) + abs(z) > 0:
            if abs(x - 16) >= abs(z):
                x += 1 if x < 16 else -1
            else:
                z += 1 if z < 0 else -1
            if math.hypot(x - cx, z - cz) <= r - 2:
                continue
            b.set(x, 0, z, S("metal_grating"))
            b.set(x, 1, z, AIR)
            b.set(x, 2, z, AIR)
            for (dx, dz) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                for y in (1, 2):
                    if b.get(x + dx, y, z + dz) in (None, AIR) and (x + dx, z + dz) != (x, z):
                        if b.get(x + dx, 0, z + dz) not in (S("metal_grating"),):
                            b.set(x + dx, y, z + dz, HULL)
            b.set(x, 3, z, S("lunar_regolith"))
            if b.get(x, 4, z) in (None, AIR):
                b.set(x, 4, z, S("lunar_regolith"))
        if kind == "dorm":
            for k in range(-6, 7, 2):
                b.set(cx + k, 1, cz - 6, S("bunk_bed", facing="south"))
                b.set(cx + k, 1, cz + 6, S("bunk_bed", facing="north"))
            for k in (-4, 4):
                b.set(cx + k, 1, cz, S("locker", facing="east"))
            b.chest(cx + 6, 1, cz, "colony", "west")
            b.set(cx - 2, 1, cz + 1, S("skeleton_suited", facing="south"))
        elif kind == "cryo":
            for a in range(0, 16):
                ang = a * math.pi / 8
                px, pz = int(round(cx + 6 * math.cos(ang))), int(round(cz + 6 * math.sin(ang)))
                b.set(px, 1, pz, S("cryo_pod", facing="north"))
            b.set(cx, 1, cz, S("control_cabinet", facing="north"))
            b.set(cx + 1, 1, cz, S("server_rack", facing="north"))
            b.set(cx - 1, 1, cz, S("battery_bank", facing="north"))
            b.set(cx, 1, cz + 2, S("data_terminal", facing="south", log=15))
        elif kind == "greenhouse":
            for x in range(-6, 7):
                for z in range(-6, 7):
                    if math.hypot(x, z) <= 6.5 and x % 3 != 0:
                        b.set(cx + x, 0, cz + z, S("living_soil") if b.rnd.random() < 0.3 else S("dirt"))
                        b.set(cx + x, 1, cz + z, S("dead_bush") if b.rnd.random() < 0.5 else AIR)
            b.set(cx, 1, cz, S("specimen_tank"))
            b.chest(cx + 3, 1, cz, "seed_bank", "west")
        else:
            b.set(cx - 5, 1, cz, S("repair_station", facing="east"))
            b.set(cx - 5, 1, cz + 2, S("fabricator", facing="east"))
            b.set(cx + 5, 1, cz, S("diesel_generator", facing="west"))
            b.set(cx, 1, cz + 5, S("mining_drone", facing="north"))
            b.chest(cx + 5, 1, cz + 3, "industrial", "west")
    # Ice mine at the crater edge (south): a drill rig over a pit of ice.
    for x in range(-10, 11):
        for z in range(40, 57):
            if math.hypot(x, (z - 48) * 1.2) <= 9:
                for y in range(-3, 1):
                    b.set(x, y, z, AIR if y > -3 else S("packed_ice"))
                if b.rnd.random() < 0.3:
                    b.set(x, -2, z, S("blue_ice"))
    for y in range(1, 16):
        for (dx, dz) in ((-2, 44), (2, 44), (-2, 48), (2, 48)):
            b.set(dx, y, dz, S("truss") if y % 4 else S("iron_block"))
    for y in range(-2, 16):
        b.set(0, y, 46, S("iron_chain", axis="y", waterlogged="false"))
    for (dx, dz) in ((-2, 44), (2, 44), (-2, 48), (2, 48), (0, 44), (0, 48), (-2, 46), (2, 46)):
        b.set(dx, 16, dz, S("iron_block"))
    b.set(0, -2, 46, S("mining_drone", facing="north"))
    # Polar solar towers: tall vertical panels catching the low sun.
    for (cx, cz) in ((-48, -6), (-46, 6), (48, 6), (46, -8)):
        for y in range(1, 22):
            b.set(cx, y, cz, S("truss"))
            if y > 6:
                for dz in (-1, 1):
                    b.set(cx, y, cz + dz, S("solar_panel"))
        b.set(cx, 22, cz, S("ship_light_red"))
    # Landing pads.
    for (cx, cz) in ((-20, 44), (24, -46)):
        disc(b, cx, cz, 0, 6, S("hazard_concrete"))
        disc(b, cx, cz, 0, 4.5, S("gray_concrete"))
    # Mass driver: a long rail on pylons leaving to the west.
    for x in range(-58, -12):
        for z in (-36, -34):
            b.set(x, 3, z, S("iron_block"))
        if x % 6 == 0:
            for y in (1, 2):
                b.set(x, y, -35, S("truss"))
        b.set(x, 3, -35, S("powered_rail", powered="false", shape="east_west", waterlogged="false"))
    # Comms mast pointing back at Earth (north).
    for y in range(1, 26):
        b.set(14, y, -40, bars() if y % 5 else S("iron_block"))
    b.set(14, 26, -40, S("antenna_dish"))
    b.set(14, 25, -41, S("antenna_dish"))
    sign(b, 18, 1, 4, ["ARK-1 COLONY", "Shackleton rim", "Population 380", "(in long sleep)"])
    # The dead the colony could not keep: a row of markers.
    for k in range(0, 9):
        b.set(-10 + k * 2, 1, 30, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    return b


def main():
    builds = [halcyon(), dead_satellite(), upper_stage(), space_telescope(), capsule_adrift()] + \
        [debris_cloud(i) for i in range(1, 6)] + \
        [luna9(), surveyor3(), lunokhod(1), lunokhod(2), change(3), change(4), chandrayaan3(), slim(), luna24(),
         ark1_colony()]
    index = {}
    for b in builds:
        size = b.save(b.name)
        index[b.name] = [min(p[i] for p in b.blocks) for i in range(3)]
        print(f"{b.name:28s} {size}")
    # Where the builder's origin lies inside each template (the Java side places by origin).
    out = Path(__file__).resolve().parents[2] / "src/main/resources/intersolarsystem/space_sites.json"
    out.write_text(json.dumps(index, indent=1) + "\n")


if __name__ == "__main__":
    main()
