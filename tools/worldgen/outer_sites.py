"""Templates of the outer system, placed by code once per world (space/Worlds.java lists where):

* Ganymede: the Uruk Sulcus survey camp in the shielded equatorial band, ESA's JUICE where it came down in 2035;
* Callisto: HOPE base (the old plan for a crewed outer-planet base, built at last), its fuel depot;
* Saturn: the Kronos aerostat above the haze, a dead storm buoy on the hexagon's wall;
* the rings: the Daphnis depot dug into a moonlet of ice, a broken ice hauler;
* Titan: the Selk habitat, the Huygens lander (2005), the Dragonfly rotorcraft, the Kraken harbour on a methane
  lagoon, the Sotra cryovolcano;
* Enceladus: the Baghdad Sulcus geyser lab, the borehole down to the living sea;
* Uranus: the Meridian aerostat; Miranda: the camp on top of Verona Rupes, the lander that fell off it; Titania: the
  Messina rift station;
* Neptune: the Le Verrier aerostat; Triton: Hili outpost (its terminal knows the orbit of a moon nobody names), the
  geyser field camp;
* the moon nobody names: a survey rig and its shaft.

Ground templates: y = 0 is the ground layer with fill below and air above; floating ones are placed at a fixed
height. The landing pad of every arrival site is at the origin. Logs 28-39 are on their terminals.

    python3 tools/worldgen/outer_sites.py
"""
import json
import math
from pathlib import Path

from make_structures import AIR, S, Build
from overhaul import bars, cylinder, disc
from space_sites import HULL, FOIL, sign
from far_sites import CHAIN, ellipsoid, ground_pad, landing_pad, module_x, module_z, rock

ROOT = Path(__file__).resolve().parents[2]


# ---------------------------------------------------------------- shared pieces
def dome(b, cx, cz, r, shell=None, glass_from=3, floor="metal_grating"):
    """A pressurised dome on the ground, glass above glass_from, a door to the south."""
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            for y in range(1, r + 1):
                d = math.sqrt(x * x + (y * 1.2) ** 2 + z * z)
                if r - 1 < d <= r:
                    b.set(cx + x, y, cz + z, S("station_glass") if y > glass_from else (shell or HULL))
                elif d <= r - 1:
                    b.set(cx + x, y, cz + z, AIR)
            if math.hypot(x, z) <= r - 1:
                b.set(cx + x, 0, cz + z, S(floor))
    for y in (1, 2):
        b.set(cx, y, cz + r - 1, AIR)
        b.set(cx, y, cz + r, AIR)
    b.set(cx, r - 1, cz, S("station_light"))


def mast(b, x, z, h, top="antenna_dish"):
    for y in range(1, h):
        b.set(x, y, z, S("truss") if y % 4 else S("iron_block"))
    b.set(x, h, z, S(top))
    b.set(x, h - 1, z + 1, S("ship_light_red"))


def solar_wing(b, x0, y, z0, length, along_x=True, broken=0.0):
    for k in range(length):
        for w in (-1, 0, 1):
            if b.rnd.random() < broken:
                continue
            if along_x:
                b.set(x0 + k, y, z0 + w, S("solar_panel"))
            else:
                b.set(x0 + w, y, z0 + k, S("solar_panel"))


def aerostat(name, seed, envelope, log, sign_lines, loot="far_station"):
    """A gas-giant aerostat: a ring deck with the landing pad open to the sky in the middle, the envelope as a wide
    ring of gas cells high above (held by chains), cabins round the rim, a terminal, scoop lines hanging down."""
    b = Build(name, seed)
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
    a_col, b_col = envelope
    for x in range(-34, 35):
        for z in range(-34, 35):
            d = math.hypot(x, z)
            if 12 <= d <= 34:
                h = 34 + int(10 * math.cos(d / 34 * math.pi / 2))
                stripe = (int(math.degrees(math.atan2(z, x))) // 15) % 2
                b.set(x, h, z, S(a_col if stripe else b_col))
                if d > 31:
                    b.set(x, h - 1, z, S(b_col))
    for a in range(0, 360, 30):
        ang = math.radians(a)
        for k in range(0, 34):
            x, z = int(round(math.cos(ang) * (16 + k * 0.4))), int(round(math.sin(ang) * (16 + k * 0.4)))
            if 1 + k < 42:
                b.set(x, 1 + k, z, CHAIN)
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
    b.set(16, 1, 1, S("data_terminal", facing="west", log=log))
    b.chest(17, 1, -1, loot, "west")
    b.set(0, 1, 17, S("bunk_bed", facing="north"))
    b.set(-11, 1, 11, S("hangar_lift", facing="south"))
    for (x, z, depth) in ((20, 0, 60), (-20, 0, 60), (0, 20, 60), (0, -20, 110)):
        for y in range(-depth, 0):
            b.set(x, y, z, S("pipe") if depth == 60 else CHAIN)
        b.set(x, -depth - 1, z, S("iron_block") if depth == 60 else S("crane_hook", facing="north"))
    sign(b, 0, 1, 10, sign_lines)
    return b


# ---------------------------------------------------------------- Ganymede
def uruk_camp():
    """URUK SULCUS CAMP: a magnetics survey camp in the bright grooved terrain near the equator, where Ganymede's own
    field turns Jupiter's radiation aside. Two modules end to end, a magnetometer line out across the grooves, a
    comms mast, a rover shed."""
    b = Build("ganymede/uruk_camp", 8100)
    ground_pad(b, 24, "grooved_ice", "ganymede_dark_ice", alt="ganymede_dark_ice", alt_chance=0.2)
    landing_pad(b, 0, 8)
    module_x(b, -20, -8, 3, -14, 2.6)
    module_x(b, -7, 4, 3, -14, 2.6)
    for x in (-8, -7):
        for y in (2, 3):
            b.set(x, y, -14, AIR)
    for y in (2, 3):
        b.set(-14, y, -11, AIR)
        b.set(-14, y, -12, AIR)
    b.set(-14, 1, -10, S("metal_grating"))
    b.set(-17, 2, -14, S("data_terminal", facing="east", log=28))
    b.set(-18, 2, -15, S("lab_bench", facing="east"))
    b.chest(-18, 2, -13, "far_station", "east")
    b.set(-2, 2, -15, S("bunk_bed", facing="east"))
    b.set(1, 2, -13, S("bunk_bed", facing="west"))
    b.set(-4, 2, -13, S("radio_set", facing="north"))
    b.set(2, 2, -15, S("skeleton_sitting", facing="west"))
    mast(b, 8, -18, 12)
    # The magnetometer line: posts with sensor heads every six blocks, a cable between them.
    for k in range(0, 7):
        x = 12 + k * 6
        for y in (1, 2):
            b.set(x, y, 4, bars(north=True, south=True))
        b.set(x, 3, 4, S("observer", facing="up", powered="false"))
        for i in range(1, 6):
            b.set(x + i, 1, 4, S("pipe"))
    # The rover shed: an open roof on trusses over a parked rover.
    for x in range(10, 19):
        for z in range(-14, -6):
            b.set(x, 5, z, FOIL)
    for (x, z) in ((10, -14), (18, -14), (10, -7), (18, -7)):
        for y in range(1, 5):
            b.set(x, y, z, S("truss"))
    for x in range(12, 17):
        for z in range(-12, -9):
            b.set(x, 2, z, S("white_concrete"))
    for (x, z) in ((12, -12), (16, -12), (12, -9), (16, -9)):
        b.set(x, 1, z, S("rover_wheel"))
    b.set(15, 3, -11, S("antenna_dish"))
    b.set(-11, 1, 6, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["URUK SULCUS CAMP", "Ganymede", "shielded band", "do not go poleward"])
    return b


def juice_crash():
    """ESA's JUICE, which ended its mission on Ganymede in 2035: the gold-wrapped body half buried at the end of its
    furrow, one cross of solar wings snapped, the long magnetometer boom flung across the ice."""
    b = Build("ganymede/juice_crash", 8110)
    ground_pad(b, 16, "grooved_ice", "ganymede_dark_ice", clear=12)
    # The furrow it ploughed, from the west.
    for x in range(-16, 0):
        for z in range(-2, 3):
            b.set(x, 0, z, AIR if abs(z) < 2 else S("ganymede_dark_ice"))
            b.set(x, -1, z, S("ganymede_dark_ice"))
    for x in range(-2, 3):
        for z in range(-2, 3):
            for y in range(0, 3):
                b.set(x, y, z, S("gold_block") if (x + y + z) % 3 else S("yellow_concrete"))
    b.set(0, 3, 0, S("antenna_dish"))
    solar_wing(b, 3, 2, 0, 9, along_x=True)
    solar_wing(b, -2, 2, 3, 7, along_x=False, broken=0.3)
    solar_wing(b, -3, 1, -3, 5, along_x=False, broken=0.5)
    for k in range(0, 10):
        b.set(5 + k, 1, -6 - k // 3, S("lightning_rod", facing="east", powered="false", waterlogged="false"))
    sign(b, 6, 1, 6, ["JUICE", "ESA 2023-2035", "Jupiter Icy Moons", "Explorer"])
    return b


# ---------------------------------------------------------------- Callisto
def hope_base():
    """HOPE BASE: the Human Outer Planet Exploration base, planned in 2003 for Callisto because it lies outside
    Jupiter's radiation belts, built at last by the Consortium as the waystation of the outer system. Three domes
    joined by tunnels (quarters, greenhouse, operations), a reactor in a berm, a vehicle bay."""
    b = Build("callisto/hope_base", 8200)
    ground_pad(b, 30, "callisto_regolith", "callisto_ice", alt="callisto_ice", alt_chance=0.08)
    landing_pad(b, 0, 9)
    dome(b, -18, -14, 8)
    dome(b, 0, -22, 9)
    dome(b, 18, -14, 8)
    for (x0, x1) in ((-11, -8), (8, 11)):
        for x in range(x0, x1 + 1):
            for z in range(-17, -14):
                for y in range(1, 4):
                    edge = z in (-17, -15) or y == 3
                    b.set(x, y, z, HULL if edge else AIR)
                b.set(x, 0, z, S("metal_grating"))
    # Quarters (west).
    for z in (-18, -15, -12):
        b.set(-23, 1, z, S("bunk_bed", facing="east"))
    b.set(-18, 1, -19, S("vending_machine", facing="south"))
    b.set(-14, 1, -17, S("locker", facing="west"))
    b.set(-15, 1, -10, S("skeleton_lying", facing="east"))
    # Greenhouse (north): dead rows, one green corner where a seed still grows under a lamp.
    for x in range(-6, 7, 2):
        for z in range(-28, -17):
            b.set(x, 1, z, S("living_soil") if x == 6 and z > -22 else S("dirt"))
            b.set(x, 2, z, S("dead_bush") if x != 6 or z <= -22 else AIR)
    b.set(6, 3, -19, S("station_light"))
    # Operations (east).
    b.set(18, 1, -20, S("data_terminal", facing="south", log=29))
    b.set(16, 1, -20, S("navigation_console", facing="south"))
    b.set(21, 1, -16, S("server_rack", facing="west"))
    b.set(15, 1, -12, S("radio_set", facing="east"))
    b.chest(20, 1, -10, "far_station", "west")
    # The reactor under its berm (south-west) and the cable to the domes.
    for x in range(-10, 11):
        for z in range(-8, 9):
            d = math.hypot(x / 10.0, z / 8.0)
            if d <= 1:
                for y in range(1, int(5 * math.sqrt(1 - d * d)) + 1):
                    b.set(-30 + x, y, 18 + z, S("callisto_regolith"))
    b.set(-30, 1, 18, S("reactor"))
    b.set(-30, 2, 18, AIR)
    for x in range(-27, -18):
        b.set(x, 0, 18, S("pipe"))
    mast(b, 26, 6, 14)
    b.set(-12, 1, 12, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 11, ["HOPE BASE", "Callisto", "outside the belts", "fuel depot west"])
    return b


def fuel_depot():
    """HOPE's fuel depot: four spherical tanks on legs, a pump house, a hose reel, a refuelling arm for landers."""
    b = Build("callisto/fuel_depot", 8210)
    ground_pad(b, 16, "callisto_regolith", "callisto_ice", clear=16)
    for (cx, cz) in ((-7, -7), (7, -7), (-7, 7), (7, 7)):
        for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
            for y in range(1, 4):
                b.set(cx + dx, y, cz + dz, S("truss"))
        ellipsoid(b, cx, 7, cz, 3.6, 3.6, 3.6, S("white_concrete"), inside=S("coolant_tank"))
        b.set(cx, 11, cz, S("ship_light_green"))
    for x in range(-2, 3):
        for z in range(-2, 3):
            for y in range(1, 4):
                b.set(x, y, z, FOIL if abs(x) == 2 or abs(z) == 2 or y == 3 else AIR)
    b.set(0, 1, 2, AIR)
    b.set(0, 2, 2, AIR)
    b.set(0, 1, -1, S("power_cabinet", facing="south"))
    b.set(-1, 1, 0, S("life_support_unit", facing="east"))
    b.chest(1, 1, 0, "hope_depot", "west")
    for k in range(3, 14):
        b.set(0, 1, -k, S("pipe"))
    b.set(0, 1, -14, S("crane_hook", facing="north"))
    sign(b, 3, 1, 3, ["HOPE FUEL DEPOT", "oxygen / fuel rods", "help yourself", "leave some"])
    return b


# ---------------------------------------------------------------- Saturn
def kronos_aerostat():
    """KRONOS: a helium-3 harvester riding Saturn's pale haze."""
    return aerostat("saturn/kronos_aerostat", 8300, ("yellow_concrete", "white_concrete"), 30,
                    ["KRONOS", "aerostat", "helium-3 harvest", "hexagon 2.5 km north"])


def storm_buoy():
    """A weather buoy dropped onto the wall of Saturn's polar hexagon: a squat float, an instrument mast, a beacon
    that stopped blinking."""
    b = Build("saturn/storm_buoy", 8310)
    ellipsoid(b, 0, 0, 0, 4, 2, 4, S("orange_concrete"), inside=AIR)
    disc(b, 0, 0, 0, 3, S("metal_grating"))
    for y in range(1, 9):
        b.set(0, y, 0, S("truss"))
    b.set(0, 9, 0, S("observer", facing="up", powered="false"))
    b.set(0, 10, 0, S("ship_light_red"))
    for (dx, dz) in ((3, 0), (-3, 0), (0, 3), (0, -3)):
        b.set(dx, 2, dz, S("antenna_dish"))
    sign(b, 2, 1, 0, ["STORM BUOY 6", "hexagon wall", "wind 400 km/h", "no signal"], "east")
    return b


def daphnis_depot():
    """THE DAPHNIS DEPOT: dug into a forty-metre moonlet of ring ice. A deck on its flat face with the landing pad,
    a pressurised gallery inside, cutting machines, heavy-water ice stacked for the drives."""
    b = Build("rings/daphnis_depot", 8400)

    def st(x, y, z):
        r = b.rnd.random()
        return S("deuterium_ice") if r < 0.04 else S("dusty_ring_ice") if r < 0.25 else S("ring_ice")

    rock(b, 0, -12, 0, 16, st)
    for x in range(-12, 13):
        for z in range(-12, 13):
            if math.hypot(x, z) <= 12:
                b.set(x, 0, z, HULL if math.hypot(x, z) < 10 else S("metal_grating"))
                for y in range(1, 20):
                    b.set(x, y, z, AIR)
    landing_pad(b, 0, 8)
    # The gallery: a tunnel into the ice under the deck.
    for x in range(-10, 11):
        for z in range(-3, 4):
            for y in range(-6, -1):
                edge = abs(z) == 3 or y in (-6, -2)
                b.set(x, y, z, (S("station_light") if y == -2 and x % 5 == 0 and z == 0 else HULL) if edge else AIR)
    for y in range(-5, 1):
        b.set(10, y, 0, S("ladder", facing="west", waterlogged="false"))
        b.set(11, y, 0, HULL)
    b.set(10, 0, 0, S("iron_trapdoor", facing="west", half="top", open="false", powered="false",
                      waterlogged="false"))
    b.set(-8, -5, -2, S("data_terminal", facing="south", log=31))
    b.chest(-6, -5, 2, "deuterium_cache", "north")
    b.chest(-4, -5, 2, "far_station", "north")
    b.set(0, -5, -2, S("fabricator", facing="south"))
    b.set(4, -5, 2, S("skeleton_curled", facing="west"))
    for x in (6, 7):
        for y in (-5, -4):
            b.set(x, y, -2, S("deuterium_ice"))
    b.set(-11, 1, 3, S("hangar_lift", facing="east"))
    sign(b, 0, 1, 10, ["DAPHNIS DEPOT", "Saturn B ring", "heavy-water ice", "storms: get inside"])
    return b


def ice_hauler_wreck():
    """The ice hauler MARGIT SZABO-OKONJO: a tug with a net of ice blocks, holed by a ring particle storm."""
    b = Build("rings/ice_hauler_wreck", 8410)
    module_x(b, -10, 6, 0, 0, 3.2)
    for x in range(-10, 7):
        if x % 5 == 0:
            for dy in range(-4, 5):
                b.set(x, dy, -4, AIR)
    for x in range(7, 10):
        b.set(x, 0, 0, S("ship_thruster"))
    for x in range(-24, -11):
        for y in range(-3, 4):
            for z in range(-3, 4):
                if b.rnd.random() < 0.7:
                    b.set(x, y, z, S("ring_ice") if b.rnd.random() < 0.8 else S("deuterium_ice"))
                elif (x + y + z) % 4 == 0:
                    b.set(x, y, z, CHAIN)
    b.set(-4, -1, 0, S("skeleton_suited", facing="east"))
    b.chest(2, -1, 1, "deuterium_cache", "north")
    return b


# ---------------------------------------------------------------- Titan
def selk_habitat():
    """SELK HABITAT: by Selk crater, where Dragonfly was sent to look for the chemistry of life. Long modules on
    stilts above the tholin sand, a heated walkway, a sample lab, the airlock with its methane-frosted suits."""
    b = Build("titan/selk_habitat", 8500)
    ground_pad(b, 26, "tholin_sand", "titan_bedrock_ice", alt="hydrocarbon_sludge", alt_chance=0.06)
    landing_pad(b, 0, 8)
    for (x0, x1, cz) in ((-22, -6, -16), (6, 22, -16)):
        for x in range(x0, x1 + 1, 4):
            for z in (cz - 2, cz + 2):
                for y in range(1, 3):
                    b.set(x, y, z, S("truss"))
        module_x(b, x0, x1, 5, cz, 2.8)
    for x in range(-6, 7):
        for z in range(-17, -14):
            b.set(x, 3, z, S("metal_grating"))
            b.set(x, 7, z, FOIL)
        b.set(x, 4, -17, bars(east=True, west=True))
        b.set(x, 4, -14, bars(east=True, west=True))
    for x in (-6, 6):
        for y in (4, 5):
            b.set(x, y, -16, AIR)
    for k in range(0, 4):
        b.set(0, 3 - k, -13 + k, S("metal_grating"))
    b.set(14, 4, -17, S("data_terminal", facing="south", log=32))
    b.set(10, 4, -15, S("lab_bench", facing="north"))
    b.set(18, 4, -15, S("fume_hood", facing="north"))
    b.set(16, 4, -17, S("specimen_tank"))
    b.chest(20, 4, -17, "lab", "south")
    b.chest(-20, 4, -15, "far_station", "north")
    for x in (-18, -14, -10):
        b.set(x, 4, -17, S("bunk_bed", facing="south"))
    b.set(-8, 4, -15, S("locker", facing="north"))
    b.set(-12, 4, -15, S("skeleton_sitting", facing="north"))
    mast(b, 24, 2, 12)
    b.set(-11, 1, 11, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["SELK HABITAT", "Titan", "1.5 bar, -179 C", "methane rain likely"])
    return b


def huygens():
    """ESA's Huygens (2005): the first landing in the outer solar system. A little saucer on a bed of rounded ice
    pebbles, its parachute a faded heap beside it."""
    b = Build("titan/huygens", 8510)
    ground_pad(b, 8, "titan_bedrock_ice", "titan_bedrock_ice", clear=6, alt="tholin_sand", alt_chance=0.3)
    disc(b, 0, 0, 1, 1.6, S("gold_block"))
    disc(b, 0, 0, 2, 1.1, S("white_concrete"))
    b.set(0, 3, 0, S("antenna_dish"))
    for (x, z) in ((3, 2), (4, 3), (3, 3), (4, 2), (5, 3)):
        b.set(x, 1, z, S("orange_wool"))
    for (x, z) in ((-3, -2), (2, -4), (-4, 3)):
        b.set(x, 1, z, S("blue_ice"))
    sign(b, -3, 1, 0, ["HUYGENS", "ESA 14 Jan 2005", "first landing", "in the outer system"], "west")
    return b


def dragonfly():
    """NASA's Dragonfly (2028; on Titan from 2034): an eight-rotor lander the size of a car, sat where it last came
    down. Its flight computer still answers to a repair kit."""
    b = Build("titan/dragonfly", 8520)
    ground_pad(b, 10, "tholin_sand", "titan_bedrock_ice", clear=10)
    for x in range(-3, 4):
        for z in range(-2, 3):
            for y in range(2, 4):
                b.set(x, y, z, S("white_concrete") if y == 3 else S("gold_block"))
    for (x, z) in ((-3, -2), (3, -2), (-3, 2), (3, 2)):
        b.set(x, 1, z, S("iron_bars", waterlogged="false", north="false", south="false", east="false",
                         west="false"))
    for (dx, dz) in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        ax, az = dx * 6, dz * 4
        for k in range(1, 6):
            b.set(dx * k, 4, dz * min(k, 4), S("truss"))
        for (rx, rz) in ((-1, 0), (1, 0), (0, -1), (0, 1), (0, 0)):
            b.set(ax + rx, 5, az + rz, S("light_gray_concrete") if (rx, rz) != (0, 0) else S("iron_block"))
            b.set(ax + rx, 3, az + rz, S("light_gray_concrete") if (rx, rz) != (0, 0) else S("iron_block"))
    b.set(0, 4, 0, S("antenna_dish"))
    b.set(0, 2, 3, S("dragonfly_flight_computer", facing="south", powered="false"))
    sign(b, 3, 1, 5, ["DRAGONFLY", "NASA 2028", "flight computer:", "repair kit"])
    return b


def kraken_harbour():
    """KRAKEN HARBOUR: a jetty out into a lagoon of liquid methane and ethane off Kraken Mare, a sample skiff's empty
    berth, the boathouse with the last skiff kit in its locker, a buoy line."""
    b = Build("titan/kraken_harbour", 8530)
    ground_pad(b, 22, "hydrocarbon_sludge", "titan_bedrock_ice", clear=12, alt="tholin_sand", alt_chance=0.2)
    for x in range(-22, 23):
        for z in range(0, 23):
            d = math.hypot(x / 22.0, (z - 22) / 18.0)
            if d < 1.0:
                for y in range(-4, 1):
                    b.set(x, y, z, S("water"))
                b.set(x, -5, z, S("hydrocarbon_sludge"))
    for z in range(-2, 18):
        for x in (-1, 0, 1):
            b.set(x, 1, z, S("metal_grating"))
        if z % 4 == 0:
            for y in range(-4, 1):
                b.set(-2, y, z, S("truss"))
                b.set(2, y, z, S("truss"))
    for k in range(0, 6):
        b.set(-10 + k * 4, 0, 16, S("orange_concrete"))
        b.set(-10 + k * 4, 1, 16, S("ship_light_red") if k % 2 else S("ship_light_green"))
    for x in range(-12, -5):
        for z in range(-8, -2):
            for y in range(1, 6):
                edge = x in (-12, -6) or z in (-8, -3) or y == 5
                b.set(x, y, z, FOIL if edge else AIR)
    b.set(-9, 1, -3, AIR)
    b.set(-9, 2, -3, AIR)
    b.chest(-11, 1, -7, "skiff", "east")
    b.set(-7, 1, -7, S("locker", facing="west"))
    b.set(-11, 1, -4, S("skeleton_lying", facing="east"))
    sign(b, 3, 2, 2, ["KRAKEN HARBOUR", "methane / ethane", "-179 C, do not swim", "skiff in boathouse"])
    return b


def sotra_vent():
    """Sotra Patera: a cryovolcano. A broad cone of frozen water-ammonia lava, a deep summit pit, old flows fanning
    down its side, an instrument station abandoned on its rim."""
    b = Build("titan/sotra_vent", 8540)
    for x in range(-26, 27):
        for z in range(-26, 27):
            d = math.hypot(x, z)
            if d > 26:
                continue
            h = int(18 * (1 - d / 26) ** 1.3)
            pit = d < 6
            for y in range(-3, h + 1):
                b.set(x, y, z, S("cryolava_ice") if (d < 18 or b.rnd.random() < 0.6) else S("titan_bedrock_ice"))
            if pit:
                for y in range(h - 9, h + 1):
                    b.set(x, y, z, AIR)
                b.set(x, h - 10, z, S("blue_ice"))
            for y in range(h + 1, h + 8):
                b.set(x, y, z, AIR)
    for (sx, sz) in ((1, 0.4), (-0.6, 1), (0.3, -1)):
        for k in range(8, 26):
            x, z = int(sx * k), int(sz * k)
            h = int(18 * (1 - min(1.0, math.hypot(x, z) / 26)) ** 1.3)
            b.set(x, h + 1, z, S("cryolava_ice"))
            b.set(x + 1, h + 1, z, S("blue_ice"))
    b.set(9, 13, 0, S("observer", facing="up", powered="false"))
    b.set(9, 12, 0, S("truss"))
    sign(b, 10, 12, 1, ["SOTRA PATERA", "cryovolcano", "water-ammonia", "dormant?"], "east")
    return b


# ---------------------------------------------------------------- Enceladus
def baghdad_lab():
    """BAGHDAD SULCUS LAB: a geyser lab between the tiger stripes. A low hab, a sample-catcher on a mast in the
    plume, snow piled against everything."""
    b = Build("enceladus/baghdad_lab", 8600)
    ground_pad(b, 20, "enceladus_snow", "enceladus_ice", alt="enceladus_ice", alt_chance=0.1)
    landing_pad(b, 0, 8)
    module_z(b, -18, -6, 3, -14, 2.6)
    for y in (2, 3):
        b.set(-14, y, -6, AIR)
    b.set(-14, 1, -5, S("metal_grating"))
    b.set(-15, 2, -16, S("data_terminal", facing="east", log=33))
    b.set(-13, 2, -12, S("lab_bench", facing="west"))
    b.set(-13, 2, -14, S("specimen_tank"))
    b.chest(-15, 2, -10, "lab", "east")
    b.set(-15, 2, -13, S("skeleton_sitting", facing="east"))
    mast(b, 12, -10, 16, top="observer")
    for x in range(-20, 21):
        for z in range(-20, 21):
            if math.hypot(x, z) > 18 and b.rnd.random() < 0.3:
                b.set(x, 1, z, S("enceladus_snow"))
    b.set(-11, 1, 11, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["BAGHDAD SULCUS LAB", "Enceladus", "plumes: they lift you", "borehole south"])
    return b


def ocean_borehole():
    """The borehole: a melt shaft thirty metres down through the ice to the sea, a winch house over it, a ladder
    and lights to a platform at the water, sample tanks with the vent life glowing in them."""
    b = Build("enceladus/ocean_borehole", 8610)
    ground_pad(b, 10, "enceladus_snow", "enceladus_ice", clear=10)
    for x in range(-2, 3):
        for z in range(-2, 3):
            for y in range(-30, 1):
                edge = abs(x) == 2 or abs(z) == 2
                b.set(x, y, z, S("enceladus_ice") if edge else AIR)
    for y in range(-30, 1):
        b.set(1, y, 0, S("ladder", facing="west", waterlogged="false"))
        if y % 6 == 0:
            b.set(-1, y, 1, S("station_light"))
    for x in range(-1, 2):
        b.set(x, -31, 0, S("metal_grating"))
    for x in range(-4, 5):
        for z in range(-4, 5):
            for y in range(1, 6):
                edge = abs(x) == 4 or abs(z) == 4 or y == 5
                b.set(x, y, z, FOIL if edge else AIR)
    b.set(0, 1, 4, AIR)
    b.set(0, 2, 4, AIR)
    b.set(0, 4, 0, S("crane_hook", facing="north"))
    for (x, z) in ((-3, -3), (-3, 3)):
        b.set(x, 1, z, S("specimen_tank"))
    b.set(3, 1, -3, S("vent_colony"))
    b.chest(3, 1, 3, "bio_lab", "west")
    sign(b, 2, 1, 5, ["BOREHOLE 2", "30 m to the sea", "LIFE BELOW", "handle samples sealed"])
    return b


# ---------------------------------------------------------------- Uranus and its moons
def meridian_aerostat():
    """MERIDIAN: the Uranus probe-and-harvest aerostat, under a sun that never moves."""
    return aerostat("uranus/meridian_aerostat", 8700, ("cyan_concrete", "white_concrete"), 34,
                    ["MERIDIAN", "aerostat", "diamonds fall below", "the sun does not set"])


def rupes_camp():
    """VERONA RUPES CAMP: on the lip of the tallest cliff in the solar system. A survey hut, a winch and rope down
    the face, flags at the edge (the cliff is a few dozen blocks west)."""
    b = Build("miranda/rupes_camp", 8800)
    ground_pad(b, 14, "miranda_ice", "miranda_ice", alt="corona_ice", alt_chance=0.15)
    landing_pad(b, 0, 7)
    for x in range(-12, -5):
        for z in range(4, 11):
            for y in range(1, 5):
                edge = x in (-12, -6) or z in (4, 10) or y == 4
                b.set(x, y, z, HULL if edge else AIR)
    b.set(-9, 1, 4, AIR)
    b.set(-9, 2, 4, AIR)
    b.set(-11, 1, 8, S("data_terminal", facing="east", log=37))
    b.chest(-7, 1, 9, "far_station", "west")
    b.set(-11, 1, 5, S("bunk_bed", facing="east"))
    for k in range(0, 5):
        b.set(-14, 1, -8 + k * 4, S("orange_concrete"))
        b.set(-14, 2, -8 + k * 4, S("ship_light_red"))
    b.set(-13, 1, 0, S("crane_console", facing="west"))
    sign(b, 3, 1, 8, ["VERONA RUPES", "20 km drop (west)", "low gravity:", "you will float down"])
    return b


def fallen_lander():
    """A cargo lander that slid off the top of Verona Rupes and came down at its foot, crumpled on its side."""
    b = Build("miranda/fallen_lander", 8810)
    ground_pad(b, 10, "miranda_ice", "miranda_ice", clear=10, alt="scarp_ice", alt_chance=0.3)
    ellipsoid(b, 0, 2, 0, 5, 2.5, 3, S("light_gray_concrete"), inside=AIR)
    for x in (-5, 5):
        b.set(x, 1, 0, S("lander_engine"))
    b.set(0, 1, -2, S("skeleton_suited", facing="south"))
    b.chest(1, 1, 1, "far_station", "north")
    for k in range(0, 8):
        b.set(6 + k, 1, -2 + (k % 3), S("scrap_pile"))
    return b


def messina_station():
    """MESSINA STATION: a research post on the rim of Titania's Messina rift, a gantry out over the edge with a
    seismometer cable hanging into the dark."""
    b = Build("titania/messina_station", 8900)
    ground_pad(b, 20, "titania_ice", "titania_ice", alt="titania_frost", alt_chance=0.15)
    landing_pad(b, 0, 8)
    dome(b, -14, -10, 7)
    b.set(-14, 1, -15, S("data_terminal", facing="south", log=38))
    b.set(-17, 1, -9, S("bunk_bed", facing="east"))
    b.chest(-11, 1, -12, "far_station", "west")
    b.set(-16, 1, -13, S("server_rack", facing="east"))
    for z in range(8, 22):
        b.set(10, 4, z, S("metal_grating"))
        if z % 3 == 0:
            for y in range(1, 4):
                b.set(9, y, z, S("truss"))
    for y in range(-30, 4):
        b.set(10, y, 22, CHAIN)
    mast(b, 14, -14, 10)
    sign(b, 0, 1, 10, ["MESSINA STATION", "Titania", "rift to the north", "-200 C"])
    return b


# ---------------------------------------------------------------- Neptune and Triton
def le_verrier_aerostat():
    """LE VERRIER: the deepest-flying aerostat ever built, in winds faster than sound."""
    return aerostat("neptune/le_verrier_aerostat", 9000, ("blue_concrete", "white_concrete"), 39,
                    ["LE VERRIER", "aerostat", "wind 2000 km/h", "crouch to hold on"])


def hili_outpost():
    """HILI OUTPOST: Triton's last post, named for the geyser Voyager 2 saw in 1989. A half-buried hab against the
    cold, a nitrogen-frost scrubber, the long-range dish still aimed at something that is not on any chart."""
    b = Build("triton/hili_outpost", 9100)
    ground_pad(b, 22, "cantaloupe_ice", "cantaloupe_ice", alt="nitrogen_ice", alt_chance=0.2)
    landing_pad(b, 0, 8)
    for x in range(-22, -5):
        for z in range(-20, -8):
            for y in range(1, 6):
                inner = -21 < x < -6 and -19 < z < -9 and y < 5
                b.set(x, y, z, AIR if inner else HULL if y < 5 else S("cantaloupe_ice"))
            b.set(x, 0, z, S("metal_grating"))
            b.set(x, 6, z, S("nitrogen_ice"))
    for y in (1, 2):
        b.set(-13, y, -9, AIR)
        b.set(-13, y, -8, AIR)
    b.set(-18, 1, -18, S("data_terminal", facing="south", log=35))
    b.set(-16, 1, -18, S("navigation_console", facing="south"))
    b.set(-9, 1, -18, S("radio_set", facing="south"))
    b.set(-20, 1, -12, S("bunk_bed", facing="east"))
    b.chest(-8, 1, -12, "far_station", "west")
    b.set(-11, 1, -14, S("skeleton_sitting", facing="north"))
    for y in range(1, 10):
        b.set(14, y, -14, S("truss"))
    disc(b, 14, -14, 10, 3, S("light_gray_concrete"))
    b.set(14, 11, -14, S("antenna_dish"))
    b.set(-11, 1, 11, S("hangar_lift", facing="north"))
    sign(b, 0, 1, 10, ["HILI OUTPOST", "Triton", "-235 C", "coldest place we know"])
    return b


def geyser_field_camp():
    """A camp round a live nitrogen geyser: instrument stakes in a ring, a tent of foil, a dark streak downwind."""
    b = Build("triton/geyser_field_camp", 9110)
    ground_pad(b, 12, "cantaloupe_ice", "cantaloupe_ice", clear=12)
    b.set(0, 0, 0, S("nitrogen_geyser", lit="false"))
    for k in range(1, 12):
        b.set(k, 0, 0, S("geyser_soot"))
        b.set(k, 0, 1 if k % 3 else -1, S("geyser_soot"))
    for a in range(0, 360, 45):
        x, z = int(round(6 * math.cos(math.radians(a)))), int(round(6 * math.sin(math.radians(a))))
        b.set(x, 1, z, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for x in range(-8, -3):
        for z in range(-8, -4):
            b.set(x, 1 if x in (-8, -4) else 2, z, FOIL)
    b.chest(-6, 1, -6, "far_station", "south")
    sign(b, -2, 1, 3, ["GEYSER 4", "nitrogen plume", "it will lift you", "8 km on the record"])
    return b


# ---------------------------------------------------------------- the moon nobody names
def survey_rig():
    """A survey rig on a small dark moon with no name on any console: a drill tower over a shaft, a cramped hut,
    and at the bottom of the shaft a cavity whose walls are grown with crystals that make no sound when struck."""
    b = Build("nameless/survey_rig", 9200)
    ground_pad(b, 14, "nameless_rock", "nameless_rock", alt="callisto_regolith", alt_chance=0.2)
    landing_pad(b, 0, 7)
    for x in range(-12, -5):
        for z in range(-12, -5):
            for y in range(1, 5):
                edge = x in (-12, -6) or z in (-12, -6) or y == 4
                b.set(x, y, z, FOIL if edge else AIR)
    b.set(-9, 1, -6, AIR)
    b.set(-9, 2, -6, AIR)
    b.set(-11, 1, -10, S("data_terminal", facing="east", log=36))
    b.set(-7, 1, -11, S("skeleton_curled", facing="west"))
    b.chest(-7, 1, -8, "far_station", "west")
    # The drill tower and the shaft (east), forty blocks down.
    for (dx, dz) in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        for y in range(1, 14):
            b.set(10 + dx, y, dz, S("truss"))
    b.set(10, 14, 0, S("crane_hook", facing="north"))
    for y in range(-40, 1):
        b.set(10, y, 0, AIR)
        b.set(11, y, 0, S("ladder", facing="west", waterlogged="false") if y > -40 else AIR)
        b.set(12, y, 0, S("nameless_rock"))
        if y % 8 == 0:
            b.set(9, y, 0, S("station_light"))
    # The cavity: a rough hollow, its walls studded with the crystals.
    for x in range(-7, 8):
        for y in range(-7, 6):
            for z in range(-7, 8):
                d = math.sqrt(x * x + (y * 1.4) ** 2 + z * z)
                if d <= 6:
                    b.set(10 + x, -46 + y, z, AIR)
                elif d <= 7.2:
                    b.set(10 + x, -46 + y, z, S("silent_crystal") if b.rnd.random() < 0.18 else S("nameless_rock"))
    b.set(10, -51, 0, S("silent_crystal"))
    b.set(12, -50, 2, S("skeleton_lying", facing="north"))
    sign(b, 3, 1, 7, ["SURVEY RIG", "(no designation)", "shaft: 40 m", "do not report"])
    return b


ALL = [uruk_camp, juice_crash, hope_base, fuel_depot, kronos_aerostat, storm_buoy, daphnis_depot, ice_hauler_wreck,
       selk_habitat, huygens, dragonfly, kraken_harbour, sotra_vent, baghdad_lab, ocean_borehole, meridian_aerostat,
       rupes_camp, fallen_lander, messina_station, le_verrier_aerostat, hili_outpost, geyser_field_camp, survey_rig]


def pool(rolls, entries):
    return {"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]},
            "entries": [{"type": "minecraft:item", "name": (i if ":" in i else "intersolarsystem:" + i), "weight": w,
                         "modifier": {"type": "minecraft:set_count",
                                      "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}}
                        for (i, w, lo, hi) in entries]}


def loot_tables():
    tables = {
        "hope_depot": [pool((3, 5), [("oxygen_canister", 8, 1, 3), ("uranium_fuel_rod", 5, 1, 2),
                                     ("battery_cell", 5, 1, 3), ("nitrogen_tank", 4, 1, 2), ("repair_kit", 3, 1, 2),
                                     ("clean_water_canister", 4, 1, 2)])],
        "deuterium_cache": [pool((2, 4), [("deuterium_ice", 6, 1, 4), ("ring_ice", 6, 2, 6), ("iridium_ingot", 2, 1, 1),
                                          ("oxygen_canister", 4, 1, 2)])],
        "bio_lab": [pool((2, 3), [("biosample", 6, 1, 3), ("specimen_tank", 2, 1, 1), ("salvaged_circuit", 4, 1, 2),
                                  ("thermal_module", 1, 1, 1)])],
        "skiff": [pool((1, 1), [("minecraft:oak_boat", 1, 1, 1)]),
                  pool((1, 3), [("tholin_resin", 5, 1, 3), ("oxygen_canister", 4, 1, 2), ("thermal_module", 1, 1, 1)])],
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
        print(f"{b.name:32s} {size}")
    out.write_text(json.dumps(index, indent=1) + "\n")


if __name__ == "__main__":
    main()
