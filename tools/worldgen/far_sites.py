"""Templates of the worlds beyond Mars, placed by code once per world (space/Worlds.java lists where):

* Venus: the Aphrodite cloud city 55 km up (the ship docks on its pad; a lift goes down to the surface), the lift's
  foot shelter on the surface, Venera 13 and Venera 9 where they came down, Dr. Kowalczyk's descent capsule;
* Mercury: Sunshade Base in the shadow of a crater wall, the MESSENGER impact scar;
* Ceres: the Occator brine station by the bright salt, the little ice volcano Ahuna;
* the asteroid belt: the Vesta Mining Outpost dug into a floating asteroid, the wrecked ore hauler;
* Jupiter: the Zephyr aerostat above the ammonia clouds with its scoop lines, the Galileo probe's heat shield;
* Io: the Loki volcano observatory, shielded; Europa: the Thera drill station with its shaft to the ocean.

Ground templates: y = 0 is the ground layer with fill below and air above; floating ones (city, outpost, aerostat,
wrecks) are placed at a fixed height. The landing pad of every arrival site is at the origin (the ship comes down
there). Logs 18-25 are on their terminals.

    python3 tools/worldgen/far_sites.py
"""
import json
import math
from pathlib import Path

from make_structures import AIR, S, Build
from overhaul import bars, cylinder, disc
from space_sites import HULL, FOIL, sign

ROOT = Path(__file__).resolve().parents[2]
CHAIN = S("iron_chain", axis="y", waterlogged="false")


def ground_pad(b, r, top, under, fill=4, clear=26, alt=None, alt_chance=0.0):
    """A patch of the world's ground at y = 0 (radius r), fill below, air above (room for the ship)."""
    for x in range(-r - 1, r + 2):
        for z in range(-r - 1, r + 2):
            if math.hypot(x, z) <= r + 0.5:
                b.set(x, 0, z, S(alt) if alt and b.rnd.random() < alt_chance else S(top))
                for y in range(1, fill + 1):
                    b.set(x, -y, z, S(under))
                for y in range(1, clear + 1):
                    b.set(x, y, z, AIR)


def landing_pad(b, y, r=8):
    """A plated landing pad with a hazard ring and corner lights."""
    disc(b, 0, 0, y, r, S("hazard_concrete"))
    disc(b, 0, 0, y, r - 1.5, S("gray_concrete"))
    disc(b, 0, 0, y, 1.2, S("yellow_concrete"))
    for (dx, dz) in ((r, 0), (-r, 0), (0, r), (0, -r)):
        b.set(dx, y + 1, dz, S("ship_light_green"))


def ellipsoid(b, cx, cy, cz, rx, ry, rz, shell, inside=None, keep=lambda x, y, z: True):
    for x in range(int(-rx) - 1, int(rx) + 2):
        for y in range(int(-ry) - 1, int(ry) + 2):
            for z in range(int(-rz) - 1, int(rz) + 2):
                d = (x / rx) ** 2 + (y / ry) ** 2 + (z / rz) ** 2
                if d <= 1.0 and keep(x, y, z):
                    outer = (abs(x) + 1) ** 2 / rx ** 2 + y * y / ry ** 2 + z * z / rz ** 2 > 1.0 or \
                        x * x / rx ** 2 + (abs(y) + 1) ** 2 / ry ** 2 + z * z / rz ** 2 > 1.0 or \
                        x * x / rx ** 2 + y * y / ry ** 2 + (abs(z) + 1) ** 2 / rz ** 2 > 1.0
                    if outer:
                        b.set(cx + x, cy + y, cz + z, shell)
                    elif inside is not None:
                        b.set(cx + x, cy + y, cz + z, inside)


def rock(b, cx, cy, cz, r, st, rng_scale=0.35):
    """A lumpy asteroid."""
    for x in range(-r - 2, r + 3):
        for y in range(-r - 2, r + 3):
            for z in range(-r - 2, r + 3):
                bump = 1.0 + rng_scale * math.sin(x * 0.9 + z * 0.5) * math.cos(y * 0.7 - x * 0.3)
                if math.sqrt(x * x + (y * 1.25) ** 2 + z * z) <= r * bump:
                    b.set(cx + x, cy + y, cz + z, st(x, y, z))


# ---------------------------------------------------------------- Venus
def cloud_city():
    """APHRODITE: the Consortium's floating city 55 km above Venus, where the air outside is 1 bar and 27 °C and
    sulphuric. A plated disc slung under five buoyancy lobes (one torn and hanging), the landing pad at its centre,
    glass habitat domes round the rim, the acid works, the lift down to the surface, the keel and the dangling
    sampling lines below."""
    b = Build("venus/cloud_city", 7100)
    R = 30
    # The deck: plating with a ring walkway of grating, the keel trusses under it.
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            d = math.hypot(x, z)
            if d <= R:
                b.set(x, 0, z, S("metal_grating") if R - 3 < d or (d > 10 and (x + z) % 9 == 0) else HULL)
                b.set(x, -1, z, FOIL if d > R - 1.5 or (x % 6 and z % 6) else S("truss"))
                for y in range(1, 28):
                    b.set(x, y, z, AIR)
    for a in range(0, 12):
        ang = a * math.pi / 6
        for k in range(0, R - 2):
            x, z = int(round(math.cos(ang) * k)), int(round(math.sin(ang) * k))
            b.set(x, -2 - k // 6, z, S("truss"))
    for y in range(-8, -1):
        b.set(0, y, 0, S("truss"))
    b.set(0, -9, 0, S("antenna_dish"))
    landing_pad(b, 0, 9)
    # Railings round the rim.
    for a in range(0, 360, 2):
        x, z = int(round(math.cos(math.radians(a)) * R)), int(round(math.sin(math.radians(a)) * R))
        b.set(x, 1, z, bars(north=True, south=True, east=True, west=True))
    # Buoyancy lobes, hung on chains: four whole, the fifth torn and sagging.
    lobes = [(0, -21), (20, -7), (12, 18), (-12, 18), (-20, -7)]
    for i, (cx, cz) in enumerate(lobes):
        torn = i == 3
        top = 30 if not torn else 18
        ellipsoid(b, cx, top, cz, 7, 4.5 if not torn else 2.5, 7,
                  S("white_concrete") if not torn else S("white_wool"),
                  keep=(lambda x, y, z: True) if not torn else (lambda x, y, z: (x + z) % 4 != 0 or y < 0))
        for (dx, dz) in ((-4, -4), (4, -4), (-4, 4), (4, 4)):
            for y in range(1, top - (4 if not torn else 2)):
                if not torn or y < 6 or (dx + dz) % 8:
                    b.set(cx + dx, y, cz + dz, CHAIN)
        if torn:
            for k in range(0, 8):
                b.set(cx + b.rnd.randint(-6, 6), 1, cz + b.rnd.randint(-6, 6), S("white_carpet"))
    # Habitat domes on the rim: two dorms, the lab, the acid works.
    domes = [(22, 6, "dorm"), (-23, 4, "lab"), (6, -24, "acid"), (-8, -24, "dorm2")]
    for (cx, cz, kind) in domes:
        r = 6
        for x in range(-r, r + 1):
            for z in range(-r, r + 1):
                for y in range(1, r + 1):
                    d = math.sqrt(x * x + (y * 1.3) ** 2 + z * z)
                    if r - 1 < d <= r:
                        broken = kind == "dorm2" and y > 3 and b.rnd.random() < 0.35
                        b.set(cx + x, y, cz + z, AIR if broken else S("station_glass") if y > 2 else HULL)
                    elif d <= r - 1:
                        b.set(cx + x, y, cz + z, AIR)
        door = (-1 if cx > 0 else 1, 0) if abs(cx) > abs(cz) else (0, -1 if cz > 0 else 1)
        for k in range(r - 1, r + 1):
            for y in (1, 2):
                b.set(cx + door[0] * k, y, cz + door[1] * k, AIR)
        b.set(cx, 4, cz, S("station_light"))
        if kind in ("dorm", "dorm2"):
            for k in (-3, -1, 1, 3):
                b.set(cx + k, 1, cz + 3, S("bunk_bed", facing="north"))
            b.set(cx - 3, 1, cz - 2, S("locker", facing="east"))
            b.chest(cx + 3, 1, cz - 2, "aphrodite", "west")
            if kind == "dorm2":
                b.set(cx, 1, cz, S("skeleton_curled", facing="south"))
        elif kind == "lab":
            b.set(cx - 2, 1, cz - 2, S("lab_bench", facing="south"))
            b.set(cx, 1, cz - 2, S("fume_hood", facing="south"))
            b.set(cx + 2, 1, cz - 2, S("specimen_tank"))
            b.set(cx - 2, 1, cz + 2, S("server_rack", facing="north"))
            b.set(cx + 1, 1, cz + 2, S("data_terminal", facing="north", log=18))
            b.chest(cx + 3, 1, cz, "lab", "west")
            b.set(cx - 1, 1, cz, S("skeleton_sitting", facing="east"))
        else:
            # The acid works: condenser columns that drew sulphuric acid and water out of the clouds.
            for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
                for y in range(1, 5):
                    b.set(cx + dx, y, cz + dz, S("pipe") if y % 2 else S("coolant_tank", facing="north", level=0))
            b.set(cx, 1, cz, S("toxic_barrel"))
            b.set(cx + 1, 1, cz, S("toxic_barrel"))
            b.chest(cx, 1, cz + 3, "aphrodite", "north")
    # Sampling lines hanging into the clouds below.
    for (x, z) in ((26, 12), (-26, -12), (10, 27), (-14, -26)):
        for y in range(-30, 0):
            b.set(x, y, z, CHAIN)
        b.set(x, -31, z, S("lightning_rod", facing="down", powered="false", waterlogged="false"))
    # The lift to the surface, and its sign.
    b.set(14, 1, 0, S("hangar_lift", facing="west"))
    b.set(14, 1, 1, S("warning_sign", facing="west"))
    sign(b, 14, 1, -1, ["SURFACE LIFT", "460 C / 92 bar", "heat shield +", "pressure shell"], "west")
    sign(b, 0, 1, 10, ["APHRODITE", "Aurora Consortium", "cloud station", "alt. 55 km"])
    # Solar wings round the edge (the sun is bright above the clouds).
    for a in range(15, 360, 30):
        ang = math.radians(a)
        for k in range(R + 1, R + 6):
            x, z = int(round(math.cos(ang) * k)), int(round(math.sin(ang) * k))
            b.set(x, 0, z, S("solar_panel"))
    return b


def lift_foot():
    """The surface end of Aphrodite's lift: a squat shielded shelter on the basalt, the lift, a dead dosimeter."""
    b = Build("venus/lift_foot", 7101)
    ground_pad(b, 7, "venus_regolith", "venus_basalt", clear=8)
    cylinder(b, 0, 0, 1, 4, 4, HULL, cap=HULL)
    for y in (1, 2):
        b.set(0, y, -4, AIR)
    b.set(0, 1, 0, S("hangar_lift", facing="north"))
    b.set(2, 1, 1, S("life_support_unit", facing="west", level=0))
    b.set(-2, 1, 1, S("locker", facing="east"))
    b.set(0, 4, 0, S("station_light"))
    sign(b, 1, 1, -5, ["APHRODITE LIFT", "back up to", "the cloud city"], "north")
    return b


def venera(n):
    """Venera 13 (1982, 127 minutes on the surface, the first colour pictures) or Venera 9 (1975, the first picture
    from another planet's surface): the pressure sphere on its crushable landing ring, the disc-shaped airbrake on
    top, the camera ports, the lens covers thrown off beside it."""
    b = Build(f"venus/venera{n}", 7110 + n)
    ground_pad(b, 6, "venus_regolith", "venus_basalt", clear=8, alt="sulfur_crust", alt_chance=0.1)
    disc(b, 0, 0, 1, 2.6, S("iron_block"), hollow=True)
    for y in range(2, 5):
        disc(b, 0, 0, y, 1.6 if y in (2, 4) else 2.0, S("anvil", facing="north") if y == 3 else S("iron_block"))
    disc(b, 0, 0, 5, 3.4, S("smooth_stone_slab", type="bottom", waterlogged="false"))
    b.set(0, 6, 0, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    b.set(2, 3, 0, S("observer", facing="east", powered="false"))
    b.set(-2, 3, 0, S("observer", facing="west", powered="false"))
    # Lens covers and the colour calibration plate where the camera saw them.
    b.set(3, 1, 2, S("light_weighted_pressure_plate", power=0))
    b.set(-3, 1, -2, S("light_weighted_pressure_plate", power=0))
    b.set(2, 1, -3, S("white_carpet") if n == 13 else S("gray_carpet"))
    sign(b, 4, 1, 0, [f"VENERA {n}", "USSR, " + ("1982" if n == 13 else "1975"),
                      "127 min on the surface" if n == 13 else "53 min on the surface",
                      "first colour images" if n == 13 else "first surface image"], "east")
    return b


def descent_capsule():
    """Capsule 2 of Aphrodite, come down hard on the surface: Dr. Ana Kowalczyk rode it to reach the old surface
    relay and call Earth. The relay mast stands dead beside it, the capsule half-melted."""
    b = Build("venus/descent_capsule", 7120)
    ground_pad(b, 9, "venus_regolith", "venus_basalt", clear=10, alt="sulfur_crust", alt_chance=0.08)
    # The capsule on its side, its parachute burnt to a stain.
    for z in range(-3, 4):
        r = 2.4 if abs(z) < 2 else 1.6
        for dx in range(-3, 4):
            for dy in range(-3, 4):
                d = math.hypot(dx, dy)
                if r - 1 < d <= r:
                    b.set(dx, 2 + dy, z, S("sooty_concrete") if dy < 0 or b.rnd.random() < 0.3 else HULL)
                elif d <= r - 1:
                    b.set(dx, 2 + dy, z, AIR)
    b.set(0, 1, 4, AIR)
    b.set(0, 2, 4, AIR)
    b.set(0, 1, 0, S("data_terminal", facing="south", log=19))
    for k in range(0, 10):
        b.set(-4 - k // 2, 1, 5 + k, S("black_carpet"))
    # The relay mast, dead.
    for y in range(1, 14):
        b.set(6, y, -3, bars() if y % 4 else S("iron_block"))
    b.set(6, 14, -3, S("antenna_dish"))
    b.set(6, 1, -2, S("power_cabinet", facing="south"))
    b.set(5, 1, -1, S("skeleton_suited", facing="east"))
    return b


# ---------------------------------------------------------------- Mercury
def sunshade_base():
    """SUNSHADE BASE: dug into a crater wall on the terminator, where a long shadow keeps it between the burning
    day and the freezing night. A berm of regolith over the halls, a great sunshade of panels on masts, radiator
    fins, the polar-ice tanks, a flare shelter, and the pad at the centre. Its keeper is the base computer."""
    b = Build("mercury/sunshade_base", 7200)
    ground_pad(b, 34, "mercury_regolith", "mercury_rock", alt="hollow_crust", alt_chance=0.02)
    landing_pad(b, 0, 9)
    # The halls (north), under a regolith berm.
    for x in range(-14, 15):
        for z in range(-30, -15):
            for y in range(1, 9):
                hx, hz = abs(x) <= 12, -28 <= z <= -17
                inside = hx and hz and y <= 5
                edge = (abs(x) == 13 or z in (-29, -16)) and y <= 6
                if inside:
                    b.set(x, y, z, AIR)
                elif edge or (y == 6 and hx and hz):
                    b.set(x, y, z, HULL)
                elif y <= 8 - abs(abs(x) - 6) // 3:
                    b.set(x, y, z, S("mercury_regolith"))
    for y in (1, 2, 3):
        for x in (-1, 0, 1):
            b.set(x, y, -16, AIR)
    b.set(0, 4, -16, S("blast_door", facing="south"))
    for x in range(-10, 11, 5):
        b.set(x, 5, -22, S("station_light"))
    b.set(-10, 1, -27, S("server_rack", facing="south"))
    b.set(-9, 1, -27, S("server_rack", facing="south"))
    b.set(-8, 1, -27, S("data_terminal", facing="south", log=20))
    b.set(-7, 1, -27, S("radio_set", facing="south"))
    for x in (4, 6, 8, 10):
        b.set(x, 1, -27, S("bunk_bed", facing="south"))
    b.chest(11, 1, -20, "far_station", "west")
    b.chest(-11, 1, -20, "industrial", "east")
    b.set(0, 1, -24, S("reactor", facing="south", level=0))
    b.set(2, 1, -24, S("coolant_tank", facing="south", level=0))
    b.set(-2, 1, -24, S("battery_bank", facing="south"))
    # The flare shelter: a deep, thick-walled room under the halls, reached by a ladder.
    for x in range(-3, 4):
        for z in range(-26, -19):
            for y in range(-6, -1):
                edge = abs(x) == 3 or z in (-26, -20) or y in (-6, -2)
                b.set(x, y, z, S("iron_block") if edge else AIR)
    for y in range(-5, 1):
        b.set(0, y, -21, S("ladder", facing="south", waterlogged="false"))
    b.set(0, 0, -21, S("iron_trapdoor", facing="south", half="top", open="false", powered="false", waterlogged="false"))
    b.set(-2, -5, -25, S("bunk_bed", facing="east"))
    b.set(2, -5, -25, S("life_support_unit", facing="west", level=0))
    sign(b, 1, 1, -21, ["FLARE SHELTER", "go down when", "the alarm sounds"], "south")
    # The sunshade: a roof of panels on masts, south of the halls, casting the base's own shadow.
    for x in range(-20, 21):
        for z in range(-14, -4):
            b.set(x, 16, z, S("solar_panel") if (x + z) % 7 else S("truss"))
    for (x, z) in ((-20, -14), (20, -14), (-20, -5), (20, -5), (0, -14), (0, -5)):
        for y in range(1, 16):
            b.set(x, y, z, S("truss") if y % 5 else S("iron_block"))
    # Radiator fins (east) and ice tanks (west).
    for z in range(-12, 13, 3):
        for y in range(1, 7):
            for x in range(24, 30):
                b.set(x, y, z, S("station_radiator"))
    for (x, z) in ((-26, -6), (-26, 0), (-26, 6)):
        cylinder(b, x, z, 1, 5, 2, HULL, cap=FOIL)
        b.set(x, 3, z, S("ceres_ice"))
    b.set(12, 1, 6, S("hangar_lift", facing="west"))
    sign(b, 0, 1, 12, ["SUNSHADE BASE", "Mercury", "Consortium solar", "power relay 2"])
    return b


def messenger_impact():
    """MESSENGER (NASA, 2011-2015): the first spacecraft to orbit Mercury, crashed into it on purpose when its
    fuel ran out. A small fresh crater, the ceramic-cloth sunshade torn and scattered, a magnetometer boom."""
    b = Build("mercury/messenger_impact", 7210)
    ground_pad(b, 8, "mercury_regolith", "mercury_rock", clear=6)
    for x in range(-5, 6):
        for z in range(-5, 6):
            d = math.hypot(x, z)
            if d <= 4.5:
                depth = int(3 * (1 - d / 4.5)) + 1
                for y in range(-depth + 1, 1):
                    b.set(x, y, z, AIR)
                b.set(x, -depth, z, S("hollow_crust"))
            elif d <= 6:
                b.set(x, 1, z, S("mercury_regolith"))
    for (x, z) in ((0, 0), (1, -1), (-2, 1)):
        b.set(x, -3, z, S("iron_block"))
    b.set(0, -2, 0, S("scrap_pile"))
    for k in range(0, 6):
        b.set(b.rnd.randint(-7, 7), 2 if b.rnd.random() < 0.3 else 1, b.rnd.randint(-7, 7), S("white_carpet"))
    for k in range(1, 6):
        b.set(5 + k, 1, -2, S("lightning_rod", facing="east", powered="false", waterlogged="false"))
    sign(b, 7, 1, 2, ["MESSENGER", "NASA 2011-2015", "impact 30 April", "2015"], "east")
    return b


# ---------------------------------------------------------------- Ceres
def occator_station():
    """OCCATOR: a brine station by the bright salts of Occator crater. A drill rig over a brine well, the cold
    archive (specimen tanks of brine), the lab, salt crusted on everything, and the landing pad."""
    b = Build("ceres/occator_station", 7300)
    ground_pad(b, 30, "ceres_clay", "ceres_clay", alt="carbonate_salt", alt_chance=0.25)
    landing_pad(b, 0, 8)
    # The bright mound of salt to the east (Cerealia Facula in small).
    for x in range(-12, 13):
        for z in range(-12, 13):
            d = math.hypot(x, z)
            h = int(5 * (1 - d / 12)) if d < 12 else 0
            for y in range(1, h + 1):
                b.set(22 + x if 22 + x <= 30 else 30, y, z, S("carbonate_salt"))
    # The station: two long modules joined by a node, half buried in clay.
    for (x0, x1, z) in ((-26, -10, -14), (-26, -10, 10)):
        for x in range(x0, x1 + 1):
            for dz in range(-3, 4):
                for y in range(0, 6):
                    d = math.hypot(dz, y - 2.5)
                    if d <= 3.2:
                        edge = d > 2.2 or x in (x0, x1)
                        b.set(x, y, z + dz, HULL if edge else AIR)
                if b.get(x, 0, z + dz) in (HULL, AIR):
                    b.set(x, 0, z + dz, HULL)
        for x in range(x0 + 2, x1 - 1, 4):
            b.set(x, 4, z, S("station_light"))
    for z in range(-11, 8):
        for dx in (-1, 0, 1):
            b.set(-18 + dx, 1, z, AIR)
            b.set(-18 + dx, 2, z, AIR)
            b.set(-18 + dx, 0, z, S("metal_grating"))
        b.set(-20, 1, z, HULL)
        b.set(-16, 1, z, HULL)
        b.set(-20, 2, z, HULL)
        b.set(-16, 2, z, HULL)
        for dx in (-2, -1, 0, 1, 2):
            b.set(-18 + dx, 3, z, HULL)
    for x in (-24, -21):
        b.set(x, 1, -16, S("specimen_tank"))
        b.set(x, 1, -12, S("specimen_tank"))
    b.set(-14, 1, -16, S("data_terminal", facing="north", log=21))
    b.set(-13, 1, -12, S("lab_bench", facing="north"))
    b.set(-11, 1, -12, S("fume_hood", facing="north"))
    b.chest(-24, 1, -14, "lab", "east")
    for x in (-24, -22, -20):
        b.set(x, 1, 13, S("bunk_bed", facing="north"))
    b.chest(-12, 1, 12, "colony", "west")
    b.set(-14, 1, 8, S("skeleton_lying", facing="east"))
    # The brine drill: a tower over a well that reaches the salty water 40 m down.
    for y in range(1, 18):
        for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
            b.set(12 + dx, y, -20 + dz, S("truss") if y % 4 else S("iron_block"))
    for y in range(-12, 18):
        b.set(12, y, -20, CHAIN if y > 0 else AIR)
    for y in range(-14, -11):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                b.set(12 + dx, y, -20 + dz, S("water") if y < -12 else S("carbonate_salt"))
    for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2), (0, -2), (0, 2), (-2, 0), (2, 0)):
        b.set(12 + dx, 18, -20 + dz, S("iron_block"))
    b.set(12, 1, -16, S("mining_drone", facing="north"))
    b.set(18, 1, 8, S("hangar_lift", facing="west"))
    sign(b, 0, 1, 10, ["OCCATOR STATION", "Ceres", "Consortium", "cold archive"])
    return b


def ahuna_cone():
    """A little ice volcano (after Ahuna Mons): a steep cone of salt and ice with bright streaks, a dry vent."""
    b = Build("ceres/ahuna_cone", 7310)
    ground_pad(b, 14, "ceres_clay", "ceres_clay", clear=16)
    for x in range(-13, 14):
        for z in range(-13, 14):
            d = math.hypot(x, z)
            h = int(min(12, (13 - d) * 1.4)) if d < 13 else 0
            for y in range(1, h + 1):
                streak = (math.atan2(z, x) * 6 / math.pi) % 1 < 0.18
                b.set(x, y, z, S("carbonate_salt") if streak else S("ceres_ice") if y > h - 2 else S("ceres_clay"))
    for y in range(6, 13):
        for (dx, dz) in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
            b.set(dx, y, dz, AIR)
    b.set(0, 5, 0, S("carbonate_salt"))
    return b


# ---------------------------------------------------------------- the asteroid belt
def mining_outpost():
    """THE VESTA MINING OUTPOST: dug into a forty-metre rock floating in the dark. A deck on the rock's flat face
    with the pad, the ore mill, a quarry face where iridium shows, tether rails strung out to two smaller rocks, a
    docking arm, the crew module, mining drones parked in rows."""
    b = Build("belt/mining_outpost", 7400)

    def body(x, y, z):
        if y < -6 and b.rnd.random() < 0.06:
            return S("nickel_iron_ore")
        if y < -10 and b.rnd.random() < 0.02:
            return S("iridium_ore")
        return S("chondrite") if (x * 7 + y * 3 + z) % 11 else S("carbonaceous_rock")
    rock(b, 0, -12, 0, 16, body)
    for x in range(-22, 23):
        for z in range(-22, 23):
            for y in range(1, 26):
                b.set(x, y, z, AIR)
            if math.hypot(x, z) <= 20:
                b.set(x, 0, z, HULL if math.hypot(x, z) > 9 else b.get(x, 0, z) or HULL)
    landing_pad(b, 0, 9)
    # The ore mill: a hall of grating over hoppers, east.
    for x in range(11, 19):
        for z in range(-6, 7):
            for y in range(1, 7):
                edge = x in (11, 18) or abs(z) == 6 or y == 6
                b.set(x, y, z, FOIL if edge else AIR)
    for z in (-4, -2, 0, 2, 4):
        b.set(14, 1, z, S("shredder_module"))
        b.set(16, 1, z, S("smelter_module"))
    for y in (1, 2):
        b.set(11, y, 0, AIR)
    b.chest(17, 1, -5, "ore_mill", "west")
    # The crew module, west: bunks, the log, a dead man still clipped to the rail outside.
    for x in range(-19, -11):
        for z in range(-4, 5):
            for y in range(1, 6):
                edge = x in (-19, -12) or abs(z) == 4 or y == 5
                b.set(x, y, z, HULL if edge else AIR)
    for y in (1, 2):
        b.set(-12, y, 0, AIR)
    for z in (-3, -1, 1, 3):
        b.set(-18, 1, z, S("bunk_bed", facing="east"))
    b.set(-14, 1, -3, S("data_terminal", facing="south", log=22))
    b.set(-15, 1, 3, S("locker", facing="north"))
    b.chest(-13, 1, 3, "far_station", "west")
    b.set(-15, 4, 0, S("station_light"))
    # The quarry face: a cut into the rock with the iridium core showing.
    for x in range(-6, 7):
        for y in range(-14, 0):
            for z in range(12, 20):
                if math.hypot(x, (y + 7) * 0.8) <= 6:
                    b.set(x, y, z, AIR)
    for (x, y, z) in ((0, -13, 18), (1, -12, 19), (-1, -12, 19), (0, -11, 19), (2, -13, 19)):
        b.set(x, y, z, S("iridium_ore"))
    for (x, y, z) in ((-3, -8, 19), (4, -9, 19), (-5, -11, 17), (3, -5, 18)):
        b.set(x, y, z, S("nickel_iron_ore"))
    b.set(0, -6, 13, S("mining_drone", facing="south"))
    b.set(-3, -10, 15, S("mining_drone", facing="east"))
    # Tether rails out to two small rocks: walk along them clipped on.
    for (tx, ty, tz) in ((40, 6, -18), (-36, -4, 28)):
        rock(b, tx, ty, tz, 5, lambda x, y, z: S("nickel_iron_ore") if (x + y + z) % 5 == 0 else S("chondrite"))
        steps = 60
        for i in range(steps + 1):
            x = round(i / steps * tx)
            y = round(1 + i / steps * (ty - 1))
            z = round(i / steps * tz)
            if b.get(x, y, z) in (None, AIR):
                b.set(x, y, z, S("truss"))
    # The docking arm with a hauler's empty berth, and the lift up to the mothership.
    for z in range(-30, -20):
        b.set(0, 4, z, S("truss"))
        b.set(0, 3, z, S("metal_grating"))
    b.set(0, 5, -30, S("ship_light_red"))
    b.set(6, 1, -6, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["VESTA MINING", "OUTPOST", "Consortium", "clip on - always"])
    return b


def ore_hauler_wreck():
    """The ore hauler INGRID BJORNSTAD: a long freighter broken amidships, its cargo of nickel-iron and iridium
    spilled into a slow cloud of nuggets."""
    b = Build("belt/ore_hauler_wreck", 7410)
    for z in range(-24, 25):
        if -3 <= z <= 2:
            continue
        r = 4.5 if abs(z) < 18 else 4.5 - (abs(z) - 18) * 0.5
        bend = (z // 6) if z > 0 else 0
        for dx in range(-6, 7):
            for dy in range(-6, 7):
                d = math.hypot(dx, dy)
                if r - 1 < d <= r and b.rnd.random() > 0.08:
                    b.set(dx + bend, dy, z, S("rusted_panel") if (z + dy) % 5 == 0 else HULL)
    for z in (-24, -23):
        for (dx, dy) in ((0, 0), (2, 0), (-2, 0)):
            b.set(dx, dy, z - 1, S("ship_thruster"))
    b.set(0, -2, -16, S("mothership_helm", facing="north"))
    b.set(1, -2, -15, S("skeleton_suited", facing="north"))
    b.chest(-2, -3, -10, "space_debris", "east")
    b.chest(2, -3, 10, "ore_mill", "west")
    for _ in range(90):
        x, y, z = b.rnd.randint(-14, 14), b.rnd.randint(-8, 8), b.rnd.randint(-6, 6)
        r = b.rnd.random()
        b.set(x, y, z, S("iridium_ore") if r < 0.08 else S("nickel_iron_ore") if r < 0.5 else S("chondrite"))
    sign(b, 0, 5, -10, ["INGRID BJORNSTAD", "ore hauler", "Vesta - Kármán"])
    return b


# ---------------------------------------------------------------- Jupiter
def aerostat():
    """ZEPHYR: a hot-hydrogen aerostat riding above Jupiter's ammonia clouds. A huge envelope over a ring gondola,
    the pad on the gondola's deck, the scoop lines (pipes and cables) hanging down through the decks for the gas
    harvest, the winch house, the crew ring."""
    b = Build("jupiter/aerostat", 7500)
    R = 22
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            d = math.hypot(x, z)
            if d <= R:
                b.set(x, 0, z, S("metal_grating") if d > R - 4 else HULL)
                b.set(x, -1, z, S("truss") if (x % 4 == 0 or z % 4 == 0) and d > 8 else FOIL)
                for y in range(1, 27):
                    b.set(x, y, z, AIR)
    landing_pad(b, 0, 9)
    # The envelope: a wide dome of hot hydrogen high above, held by cables (the ship lands under it, in the open
    # centre ring).
    for x in range(-34, 35):
        for z in range(-34, 35):
            d = math.hypot(x, z)
            if 12 <= d <= 34:
                h = 34 + int(10 * math.cos(d / 34 * math.pi / 2))
                b.set(x, h, z, S("orange_concrete") if (int(math.degrees(math.atan2(z, x))) // 15) % 2 else
                      S("white_concrete"))
    for a in range(0, 360, 30):
        ang = math.radians(a)
        for k in range(0, 34):
            x, z = int(round(math.cos(ang) * (16 + k * 0.4))), int(round(math.sin(ang) * (16 + k * 0.4)))
            y = 1 + k
            if y < 42:
                b.set(x, y, z, CHAIN)
    # The crew ring: cabins along the rim.
    for a in range(0, 360, 45):
        ang = math.radians(a)
        cx, cz = int(round(math.cos(ang) * 16)), int(round(math.sin(ang) * 16))
        for x in range(-2, 3):
            for z in range(-2, 3):
                for y in range(1, 5):
                    edge = abs(x) == 2 or abs(z) == 2 or y == 4
                    b.set(cx + x, y, cz + z, (S("station_window") if y == 2 and (x == 0 or z == 0) else HULL)
                          if edge else AIR)
        b.set(cx - (1 if cx > 0 else -1) * 2, 1, cz, AIR)
        b.set(cx - (1 if cx > 0 else -1) * 2, 2, cz, AIR)
        b.set(cx, 3, cz, S("station_light"))
    b.set(16, 1, 1, S("data_terminal", facing="west", log=23))
    b.chest(17, 1, -1, "far_station", "west")
    b.chest(-17, 1, 0, "colony", "east")
    b.set(0, 1, 17, S("bunk_bed", facing="north"))
    b.set(1, 1, -17, S("skeleton_sitting", facing="south"))
    b.set(-11, 1, 11, S("hangar_lift", facing="south"))
    # The scoop lines and the winch: pipes down through the ammonia deck, a cable to the water deck.
    for (x, z, depth) in ((20, 0, 60), (-20, 0, 60), (0, 20, 60), (0, -20, 95)):
        for y in range(-depth, 0):
            b.set(x, y, z, S("pipe") if depth == 60 else CHAIN)
        b.set(x, -depth - 1, z, S("iron_block") if depth == 60 else S("crane_hook", facing="north"))
    for x in range(-2, 3):
        for z in range(-23, -18):
            for y in range(1, 4):
                edge = abs(x) == 2 or z in (-23, -19) or y == 3
                b.set(x, y, z, FOIL if edge else AIR)
    b.set(0, 1, -21, S("crane_console", facing="north"))
    b.set(0, 1, -19, AIR)
    b.set(0, 2, -19, AIR)
    sign(b, 0, 1, 10, ["ZEPHYR", "aerostat", "Consortium gas works", "1.3 bar / -110 C"])
    return b


def galileo_probe():
    """The Galileo atmospheric probe (1995): its heat shield cone and the parachute, caught on the hydrosulfide
    clouds 150 km down, where its signal stopped after 58 minutes."""
    b = Build("jupiter/galileo_probe", 7510)
    for y in range(0, 4):
        disc(b, 0, 0, y, 3.2 - y * 0.7, S("brown_terracotta") if y < 2 else S("black_concrete"))
    b.set(0, 4, 0, S("iron_block"))
    b.set(0, 5, 0, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for x in range(-7, 8):
        for z in range(-7, 8):
            d = math.hypot(x, z)
            if d <= 7 and b.rnd.random() < 0.85:
                b.set(x + 5, 2 + int(d / 3), z + 4, S("white_wool") if (x + z) % 3 else S("orange_wool"))
    for k in range(1, 6):
        b.set(k, 3, k, CHAIN)
    sign(b, -3, 1, 0, ["GALILEO PROBE", "NASA 1995", "58 min of data", "then silence"], "west")
    return b


# ---------------------------------------------------------------- Io and Europa
def volcano_observatory():
    """LOKI OBSERVATORY: a bunker of iron and basalt on Io, looking at the Loki lava lake. Thick walls against the
    radiation, a periscope dome, a lava lake beside it, sulphur frost on everything."""
    b = Build("io/volcano_observatory", 7600)
    ground_pad(b, 30, "sulfur_plain", "io_silicate", alt="so2_frost", alt_chance=0.12)
    landing_pad(b, 0, 8)
    # The lava lake (east), crusted at the edge.
    for x in range(14, 31):
        for z in range(-10, 11):
            d = math.hypot((x - 23) * 1.0, z * 1.2)
            if d <= 8:
                b.set(x, 0, z, S("lava"))
                b.set(x, -1, z, S("lava"))
            elif d <= 9.5:
                b.set(x, 0, z, S("basalt", axis="y"))
                b.set(x, 1, z, S("sulfur_crust") if b.rnd.random() < 0.5 else AIR)
    # The bunker (west), walls three thick, entered through a dog-leg.
    for x in range(-26, -11):
        for z in range(-8, 9):
            for y in range(1, 9):
                inner = -23 <= x <= -14 and -5 <= z <= 5 and y <= 5
                if inner:
                    b.set(x, y, z, AIR)
                else:
                    b.set(x, y, z, S("iron_block") if y == 6 or x in (-26, -11) else S("io_silicate"))
    for x in range(-13, -10):
        for y in (1, 2):
            b.set(x, y, 0, AIR)
    for y in (1, 2):
        b.set(-13, y, 0, AIR)
        b.set(-13, y, 1, AIR)
    b.set(-11, 3, 0, S("blast_door", facing="east"))
    b.set(-15, 1, -4, S("data_terminal", facing="west", log=24))
    b.set(-16, 1, -4, S("radio_set", facing="south"))
    b.set(-20, 1, -4, S("server_rack", facing="south"))
    for z in (-1, 2, 4):
        b.set(-22, 1, z, S("bunk_bed", facing="east"))
    b.chest(-17, 1, 4, "shielded_rack", "north")
    b.set(-19, 4, 0, S("station_light"))
    # The periscope dome on the roof, its glass towards the lake.
    for y in range(9, 13):
        b.set(-18, y, 0, S("truss"))
    disc(b, -18, 0, 13, 2.2, S("station_glass"))
    b.set(-18, 14, 0, S("antenna_dish"))
    b.set(-6, 1, 12, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["LOKI OBSERVATORY", "Io", "radiation area", "shielding required"])
    return b


def drill_station():
    """THERA DRILL STATION: on Europa's ice, over the shaft its melt-probe cut 30 m down to the ocean. A drill
    tower, the station dome, the shaft with a ladder and lights down to a pressure hatch at the water, the
    submersible's cradle, specimen tanks with something glowing in them."""
    b = Build("europa/drill_station", 7700)
    ground_pad(b, 26, "europa_ice", "europa_ice", alt="lineae_ice", alt_chance=0.15, fill=2)
    landing_pad(b, 0, 8)
    # The dome (north).
    cx, cz, r = 0, -18, 7
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            for y in range(1, r + 1):
                d = math.sqrt(x * x + (y * 1.2) ** 2 + z * z)
                if r - 1 < d <= r:
                    b.set(cx + x, y, cz + z, S("station_glass") if y > 3 else HULL)
                elif d <= r - 1:
                    b.set(cx + x, y, cz + z, AIR)
    for y in (1, 2):
        b.set(0, y, -11, AIR)
    for x in (-4, -2):
        b.set(x, 1, -21, S("specimen_tank"))
    b.set(0, 1, -22, S("microbial_mat"))
    b.set(2, 1, -21, S("data_terminal", facing="south", log=25))
    b.set(4, 1, -19, S("lab_bench", facing="west"))
    b.chest(-5, 1, -17, "lab", "east")
    b.set(0, 5, -18, S("station_light"))
    # The drill tower and its shaft (east), down to the ocean.
    sx, sz = 14, 0
    for y in range(1, 20):
        for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
            b.set(sx + dx, y, sz + dz, S("truss") if y % 4 else S("iron_block"))
    for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2), (0, -2), (0, 2), (-2, 0), (2, 0)):
        b.set(sx + dx, 20, sz + dz, S("iron_block"))
    for y in range(-34, 1):
        for dx in (-2, -1, 0, 1, 2):
            for dz in (-2, -1, 0, 1, 2):
                edge = abs(dx) == 2 or abs(dz) == 2
                b.set(sx + dx, y, sz + dz, (HULL if y > -26 else S("europa_ice")) if edge
                      else AIR if y > -25 else S("water"))
        b.set(sx - 1, y, sz - 1, S("ladder", facing="south", waterlogged="true" if y <= -25 else "false"))
        if y % 6 == 0:
            b.set(sx + 1, y, sz + 1, S("station_light"))
    for y in range(-6, 20):
        b.set(sx, y, sz, CHAIN)
    b.set(sx, -7, sz, S("crane_hook", facing="north"))
    b.set(sx + 3, 1, sz, S("power_cabinet", facing="east"))
    sign(b, sx - 3, 1, sz, ["THERA SHAFT", "ocean at 30 m", "the water is", "warm. Look."], "west")
    b.set(-12, 1, 6, S("hangar_lift", facing="east"))
    sign(b, 0, 1, 10, ["THERA STATION", "Europa", "Consortium", "ocean access"])
    return b


ALL = [cloud_city, lift_foot, lambda: venera(13), lambda: venera(9), descent_capsule, sunshade_base,
       messenger_impact, occator_station, ahuna_cone, mining_outpost, ore_hauler_wreck, aerostat, galileo_probe,
       volcano_observatory, drill_station]


def pool(rolls, entries):
    return {"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]},
            "entries": [{"type": "minecraft:item", "name": (i if ":" in i else "intersolarsystem:" + i), "weight": w,
                         "modifier": {"type": "minecraft:set_count",
                                      "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}}
                        for (i, w, lo, hi) in entries]}


def loot_tables():
    """The chests of the far stations: what their crews left (modules, iridium, oxygen)."""
    tables = {
        "aphrodite": [pool((2, 4), [("oxygen_canister", 8, 1, 2), ("titanium_plate", 5, 1, 2),
                                    ("salvaged_circuit", 6, 1, 3), ("minecraft:white_wool", 4, 2, 6),
                                    ("thermal_module", 1, 1, 1), ("pressure_module", 1, 1, 1)])],
        "far_station": [pool((2, 4), [("oxygen_canister", 8, 1, 2), ("nitrogen_tank", 4, 1, 1),
                                      ("battery_cell", 4, 1, 2), ("salvaged_circuit", 6, 1, 3),
                                      ("titanium_ingot", 4, 1, 2), ("repair_kit", 3, 1, 1),
                                      ("tank_module", 1, 1, 1)])],
        "shielded_rack": [pool((1, 1), [("radiation_module", 1, 1, 1)]),
                          pool((1, 3), [("oxygen_canister", 6, 1, 2), ("salvaged_circuit", 4, 1, 2)])],
        "ore_mill": [pool((3, 5), [("nickel_iron_ore", 8, 2, 6), ("iridium_ingot", 2, 1, 1),
                                   ("minecraft:iron_ingot", 6, 2, 5), ("scrap_metal", 6, 1, 4),
                                   ("rare_earth_magnet", 2, 1, 1)])],
    }
    for name, pools in tables.items():
        path = ROOT / f"src/main/resources/data/intersolarsystem/loot_table/chests/{name}.json"
        path.write_text(json.dumps({"type": "minecraft:chest", "pools": pools}, indent=2) + "\n")


def main():
    loot_tables()
    out = ROOT / "src/main/resources/intersolarsystem/space_sites.json"
    index = json.loads(out.read_text())
    for make in ALL:
        b = make()
        size = b.save(b.name)
        index[b.name] = [min(p[i] for p in b.blocks) for i in range(3)]
        print(f"{b.name:30s} {size}")
    out.write_text(json.dumps(index, indent=1) + "\n")


if __name__ == "__main__":
    main()
