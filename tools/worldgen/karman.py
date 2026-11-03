"""The Kármán spaceport (where the Arks left from) and the mothership the player builds there.

    karman_spaceport() -> Build    the ruin (460 x 340): assembly hall, two launch pads, launch control, the power
                                   plant with cooling towers, the fabrication hall, the fuel depot, housing, rail yard
    write_blueprints()             the mothership's sections for the crane, relative to the crane console (facing
                                   north), into src/main/resources/intersolarsystem/blueprints/<section>.txt

The mothership (Ark-5, finished at last) is ~350 m long: a spine (keel), a ribbed hull with window bands, a cluster
of engines at the stern, a reactor ring, life support, hydroponic farm decks, a laboratory, quarters with a mess,
a hangar bay under the belly for the small ships, and the bridge in the nose.
"""
import math
from pathlib import Path

from make_structures import AIR, CONCRETE, GREY, LGREY, S, WHITE, Build
from big_structures import (SKELETON_POSES, asphalt, collapse, concrete, dead_ground, door, facade_building, ground,
                            skeleton, stair_core)

ROOT = Path(__file__).resolve().parents[2]
BLUEPRINTS = ROOT / "src/main/resources/intersolarsystem/blueprints"

W, D = 460, 340
# The crane console in the template (facing north, towards the ship).
CONSOLE = (235, 1, 132)
# The ship's axis: along x, centred here.
SHIP_X0, SHIP_X1 = 58, 402
SHIP_Y, SHIP_Z = 42, 80


def debris(b, x, y, z, n=10, r=6):
    """Rubble and broken concrete around a cabinet: the restoration's first chore."""
    for _ in range(n):
        dx, dz = b.rnd.randint(-r, r), b.rnd.randint(-r, r)
        if abs(dx) + abs(dz) < 2:
            continue
        if b.get(x + dx, y, z + dz) in (None, AIR):
            b.set(x + dx, y, z + dz, S("rubble") if b.rnd.random() < 0.6 else S("rebar_concrete"))


def karman_spaceport():
    b = Build("karman_spaceport", 9001)
    ground(b, 0, 0, W - 1, D - 1, lambda x, z: dead_ground(b))
    # Roads: a ring road and spurs, all asphalt with markings.
    for x in range(0, W):
        for z in (150, 151, 152, 153, 154, 155, 156):
            b.set(x, 0, z, S("road_marking", facing="east") if z == 153 and x % 6 < 3 else asphalt(b, 0.15))
    for z in range(156, D):
        for x in (200, 201, 202, 203, 204, 205, 206):
            b.set(x, 0, z, S("road_marking", facing="north") if x == 203 and z % 6 < 3 else asphalt(b, 0.15))
    # Rail yard along the hall's south side.
    for x in range(10, W - 10):
        for z in (142, 146):
            b.set(x, 0, z, S("gravel"))
            b.set(x, 1, z, S("rail", shape="east_west", waterlogged="false"))
        if x % 40 < 14 and 30 < x < W - 40:
            for z in (141, 142, 143):
                b.set(x, 1, z, S("rusted_panel") if x % 40 in (0, 13) else S("shipping_crate"))
                b.set(x, 2, z, S("shipping_crate") if b.rnd.random() < 0.5 else AIR)
    # Perimeter fence with a gate.
    for x in range(0, W):
        for z in (0, D - 1):
            if not (z == D - 1 and 196 <= x <= 210):
                b.set(x, 1, z, S("iron_bars"))
                b.set(x, 2, z, S("barbed_wire", facing="north"))
    for z in range(0, D):
        for x in (0, W - 1):
            b.set(x, 1, z, S("iron_bars"))
            b.set(x, 2, z, S("barbed_wire", facing="east"))
    for x in (194, 212):
        for y in range(1, 6):
            b.set(x, y, D - 1, S("gray_concrete"))
    for x in range(194, 213):
        b.set(x, 6, D - 1, S("gray_concrete"))
    b.set(198, 1, D - 2, S("sandbags", facing="south"))
    b.set(208, 1, D - 2, S("sandbags", facing="south"))
    assembly_hall(b)
    for (px, pz, wreck) in ((20, 170, True), (120, 170, False)):
        launch_pad(b, px, pz, wreck)
    launch_control(b, 300, 172)
    power_plant(b, 376, 166)
    fabrication_hall(b, 214, 170)
    fuel_depot(b, 214, 252)
    housing(b, 20, 262)
    solar_field(b, 300, 240)
    b.decay(0.004, keep=("minecraft:rail", "intersolarsystem:asphalt", "intersolarsystem:cracked_asphalt"))
    return b


def assembly_hall(b):
    """The vehicle assembly hall: 390 x 100 x 80, a steel frame half clad, the roof girders, the bridge crane, the
    great door to the pads, the crane console and its cabinet, and Ark-5's abandoned keel blocks."""
    x0, x1, z0, z1, h = 40, 430, 26, 134, 82
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            b.set(x, 0, z, concrete(b, 0.08) if (x + z) % 9 else S("hazard_concrete") if z in (z0 + 4, z1 - 4) else concrete(b))
    for x in range(x0, x1 + 1, 10):
        for z in (z0, z1):
            for y in range(1, h):
                b.set(x, y, z, S("truss"))
    for z in range(z0, z1 + 1, 10):
        for x in (x0, x1):
            for y in range(1, h):
                b.set(x, y, z, S("truss"))
    # Cladding: rusted panels on the lower half, gaps higher up where it fell off.
    for x in range(x0, x1 + 1):
        for z in (z0, z1):
            for y in range(1, h):
                if b.get(x, y, z) in (None, AIR):
                    keep = y < 30 or b.rnd.random() < max(0.2, 1.0 - y / 90)
                    if z == z1 and 200 <= x <= 270 and y < 72:
                        continue  # the great door (open)
                    if keep:
                        b.set(x, y, z, S("rusted_panel") if (x // 10 + y // 6) % 3 else S("station_hull"))
    for z in range(z0, z1 + 1):
        for x in (x0, x1):
            for y in range(1, h):
                if b.get(x, y, z) in (None, AIR) and (y < 40 or b.rnd.random() < 0.6):
                    b.set(x, y, z, S("rusted_panel"))
    # Roof girders and patches of roof.
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x % 10 == 0 or z % 18 == 8:
                b.set(x, h, z, S("truss"))
            elif b.rnd.random() < 0.55:
                b.set(x, h, z, S("rusted_panel"))
    # The bridge crane: two runways along the walls, a girder across, the trolley and the hook over the ship.
    for x in range(x0 + 1, x1):
        for z in (z0 + 2, z1 - 2):
            b.set(x, h - 4, z, S("yellow_concrete"))
    cx = (x0 + x1) // 2
    for z in range(z0 + 2, z1 - 1):
        for dx in (-1, 0, 1):
            b.set(cx + dx, h - 3, z, S("yellow_concrete") if dx else S("truss"))
    b.set(cx, h - 4, SHIP_Z, S("metal_grating"))
    for y in range(SHIP_Y + 34, h - 4):
        b.set(cx, y, SHIP_Z, S("chain", axis="y"))
    b.set(cx, SHIP_Y + 33, SHIP_Z, S("crane_hook", facing="north"))
    # Assembly cradles along the floor (where the ship rests) and Ark-5's abandoned keel blocks.
    for x in range(SHIP_X0 + 10, SHIP_X1 - 10, 24):
        for z in range(SHIP_Z - 8, SHIP_Z + 9):
            h2 = 1 + abs(z - SHIP_Z) // 2
            for y in range(1, h2 + 1):
                b.set(x, y, z, S("yellow_concrete") if y == h2 else S("gray_concrete"))
                b.set(x + 1, y, z, S("gray_concrete"))
    for x in range(120, 150):
        for z in range(SHIP_Z - 2, SHIP_Z + 3):
            b.set(x, 1, z, S("station_hull") if (x + z) % 5 else S("truss"))
    # Crane console booth with its cabinet, a terminal and debris.
    cxc, cy, czc = CONSOLE
    for x in range(cxc - 5, cxc + 6):
        for z in range(czc - 2, czc + 2):
            b.set(x, 0, z, S("metal_grating"))
        b.set(x, 1, czc - 2, S("iron_bars") if abs(x - cxc) > 1 else AIR)
    b.set(cxc, cy, czc, S("crane_console", facing="north"))
    b.set(cxc + 3, cy, czc + 1, S("crane_cabinet", facing="north"))
    b.set(cxc - 3, cy, czc + 1, S("data_terminal", facing="north", log=9))
    debris(b, cxc + 3, 1, czc - 3, 12, 6)
    b.chest(cxc - 5, 1, czc + 1, "space_center", "north")
    b.set(cxc - 4, 1, czc + 1, S("poster_exodus", facing="north"))
    skeleton(b, cxc + 1, 1, czc + 1, facing="north", pose="skeleton_sitting")


def launch_pad(b, x0, z0, wreck):
    """A launch pad: concrete deck, flame trench, the service tower with swing arms; one pad still holds the
    burnt-out lower stage of an Ark booster that never left."""
    for x in range(x0, x0 + 80):
        for z in range(z0, z0 + 80):
            b.set(x, 0, z, concrete(b, 0.12))
    cx, cz = x0 + 40, z0 + 40
    for x in range(cx - 6, cx + 7):
        for z in range(cz - 30, cz + 31):
            if abs(x - cx) <= 4 and z > cz - 6:
                for y in range(-6, 1):
                    b.set(x, y, z, AIR if y > -6 else S("blackstone"))
    for x in range(cx - 10, cx + 11):
        for z in range(cz - 10, cz + 11):
            if abs(x - cx) > 4 or z <= cz - 6:
                b.set(x, 0, z, S("polished_blackstone"))
    tx, tz = cx + 16, cz - 4
    for y in range(1, 72):
        for (dx, dz) in ((0, 0), (5, 0), (0, 5), (5, 5)):
            b.set(tx + dx, y, tz + dz, S("truss"))
        if y % 8 == 0:
            for dx in range(0, 6):
                for dz in range(0, 6):
                    b.set(tx + dx, y, tz + dz, S("metal_grating"))
            if y in (32, 48, 64):
                for d in range(1, 12):
                    b.set(tx - d, y, tz + 2, S("truss"))
    for y in range(1, 72):
        b.set(tx + 2, y, tz + 2, S("ladder", facing="north"))
    b.set(tx + 3, 73, tz + 3, S("station_light"))
    b.set(tx + 2, 72, tz + 2, S("antenna_dish"))
    if wreck:
        r = 7
        for y in range(1, 34):
            for x in range(cx - r, cx + r + 1):
                for z in range(cz - r, cz + r + 1):
                    d = math.hypot(x - cx, z - cz)
                    if r - 1 <= d <= r and b.rnd.random() > 0.15 + y * 0.012:
                        b.set(x, y, z, S("sooty_concrete") if y < 8 or b.rnd.random() < 0.3 else WHITE)
        for (dx, dz) in ((-4, 0), (4, 0), (0, -4), (0, 4)):
            b.set(cx + dx, 1, cz + dz, S("jet_engine", facing="north"))
    else:
        for x in range(cx - 3, cx + 4):
            for z in range(cz - 3, cz + 4):
                b.set(x, 1, z, S("hazard_concrete"))
    b.chest(tx - 1, 1, tz, "space_center", "north", "barrel")
    for x in range(x0, x0 + 80, 10):
        b.set(x, 1, z0, S("runway_light", facing="north"))


def launch_control(b, x0, z0):
    """Launch control: three floors of consoles, the room the countdowns came from; the manual printer."""
    x1, z1 = x0 + 56, z0 + 40
    levels = facade_building(b, x0, z0, x1, z1, 3, 6, LGREY, S("curtain_wall"), S("office_carpet"), roof=S("gray_concrete"),
                             pillars=4, window_rows=(2, 3, 4))
    stair_core(b, x0 + 2, z1 - 2, levels, 18)
    door(b, (x0 + x1) // 2, 1, z0, 3, 3)
    y = levels[1] + 1
    for row in range(3):
        for x in range(x0 + 6, x1 - 5, 3):
            b.set(x, y, z0 + 8 + row * 6, S("office_desk", facing="north"))
            b.set(x, y, z0 + 9 + row * 6, S("office_chair", facing="north"))
            if b.rnd.random() < 0.15:
                skeleton(b, x, y, z0 + 10 + row * 6, pose="skeleton_sitting", facing="north")
    for x in range(x0 + 4, x1 - 3, 8):
        b.set(x, y + 2, z0 + 1, S("departure_board", facing="south"))
    for x in range(x0 + 10, x1 - 10, 4):
        b.set(x, levels[0] + 1, z1 - 3, S("server_rack", facing="south"))
    cy = levels[0] + 1
    b.set(x0 + 6, cy, z0 + 4, S("control_cabinet", facing="north"))
    b.set(x0 + 10, cy, z0 + 4, S("manual_printer", facing="north"))
    b.set(x0 + 12, cy, z0 + 4, S("data_terminal", facing="north", log=9))
    b.set(x0 + 14, cy, z0 + 4, S("radio_set", facing="north"))
    debris(b, x0 + 6, cy, z0 + 8, 12, 6)
    b.chest(x1 - 2, cy, z0 + 2, "space_center", "west")
    b.chest(x1 - 2, levels[2] + 1, z1 - 2, "lab", "west", "barrel")
    b.set(x0 + 30, levels[2] + 1, z0 + 20, S("office_desk", facing="north"))
    b.set(x0 + 30, levels[2] + 1, z0 + 21, S("skeleton_sitting", facing="north"))  # the launch director
    collapse(b, x0, z0, x1, z1, 12, 19, 0.05)


def power_plant(b, x0, z0):
    """The power plant: the reactor building (a domed cylinder), the turbine hall, two cooling towers."""
    cx, cz, r = x0 + 22, z0 + 22, 18
    for y in range(0, 34):
        rr = r if y < 22 else int(math.sqrt(max(0, r * r - (y - 22) ** 2 * 2.6)))
        for x in range(cx - r, cx + r + 1):
            for z in range(cz - r, cz + r + 1):
                d = math.hypot(x - cx, z - cz)
                if y == 0 and d <= r:
                    b.set(x, y, z, S("hazard_concrete") if d > r - 2 else S("polished_andesite"))
                elif rr > 0 and rr - 1 <= d <= rr:
                    b.set(x, y, z, LGREY)
                elif d < rr - 1 and y > 0:
                    b.set(x, y, z, AIR)
    door(b, cx - 1, 1, cz + r, 3, 4)
    for y in range(1, 14):
        for x in range(cx - 4, cx + 5):
            for z in range(cz - 4, cz + 5):
                if math.hypot(x - cx, z - cz) <= 4.5:
                    b.set(x, y, z, S("reactor") if (x, z) == (cx, cz) and y % 4 == 1 else S("iron_block") if math.hypot(x - cx, z - cz) > 3.5 else S("light_blue_stained_glass") if y > 2 else S("iron_block"))
    b.set(cx, 14, cz, S("station_light"))
    for (dx, dz) in ((-9, 0), (9, 0), (0, -9)):
        for y in range(1, 6):
            b.set(cx + dx, y, cz + dz, S("coolant_tank") if y == 1 else S("pipe", up="true", down="true", north="false", south="false", east="false", west="false"))
    b.set(cx + 6, 1, cz + r - 3, S("power_cabinet", facing="south"))
    b.set(cx + 8, 1, cz + r - 3, S("data_terminal", facing="south", log=9))
    debris(b, cx + 6, 1, cz + r - 6, 12, 5)
    b.chest(cx - 8, 1, cz + 10, "industrial", "east")
    skeleton(b, cx + 2, 1, cz + 8, pose="skeleton_suited")
    # Turbine hall.
    tx0, tz0 = x0 + 2, z0 + 46
    facade_building(b, tx0, tz0, tx0 + 64, tz0 + 18, 1, 12, S("gray_concrete"), S("glass"), S("polished_andesite"),
                    roof=S("rusted_panel"), pillars=6, window_rows=(8, 9))
    for x in range(tx0 + 6, tx0 + 60, 18):
        for z in range(tz0 + 5, tz0 + 14):
            for y in range(1, 6):
                b.set(x + (z % 2), y, z, S("iron_block") if y < 5 else S("yellow_concrete"))
        b.set(x + 6, 1, tz0 + 9, S("diesel_generator", facing="north"))
    door(b, tx0 + 30, 1, tz0, 4, 5)
    # Cooling towers (hyperboloids).
    for (tcx, tcz) in ((x0 + 22, z0 + 100), (x0 + 60, z0 + 96)):
        for y in range(0, 52):
            t = (y - 34) / 34
            rr = 10 + 6 * t * t
            for x in range(int(tcx - rr - 1), int(tcx + rr + 2)):
                for z in range(int(tcz - rr - 1), int(tcz + rr + 2)):
                    if x >= W - 1:
                        continue
                    d = math.hypot(x - tcx, z - tcz)
                    if rr - 1 <= d <= rr and not (y < 4 and (x + z) % 5 == 0):
                        b.set(x, y, z, S("light_gray_concrete") if y % 9 else S("gray_concrete"))
                    elif d < rr - 1 and y == 0:
                        b.set(x, y, z, S("gravel"))


def fabrication_hall(b, x0, z0):
    x1, z1 = x0 + 70, z0 + 56
    facade_building(b, x0, z0, x1, z1, 1, 20, S("rusted_panel"), S("glass"), S("polished_andesite"), roof=S("rusted_panel"),
                    pillars=8, window_rows=(14, 15, 16))
    door(b, (x0 + x1) // 2 - 4, 1, z0, 9, 10)
    b.set(x0 + 35, 1, z0 + 30, S("fabricator", facing="north"))
    b.set(x0 + 26, 1, z0 + 8, S("fabrication_cabinet", facing="north"))
    debris(b, x0 + 26, 1, z0 + 12, 12, 6)
    for x in range(x0 + 4, x1 - 3, 6):
        b.set(x, 1, z1 - 3, S("shipping_crate"))
        b.set(x, 2, z1 - 3, S("shipping_crate") if b.rnd.random() < 0.5 else AIR)
        b.set(x + 2, 1, z1 - 3, S("lab_bench", facing="north"))
    for x in range(x0 + 4, x1 - 3, 10):
        b.set(x, 1, z0 + 3, S("locker", facing="south"))
    for z in range(z0 + 6, z1 - 6, 12):
        b.set(x0 + 4, 1, z, S("recycler", facing="east"))
        b.set(x0 + 4, 1, z + 1, S("shredder_module", facing="east"))
    b.chest(x1 - 3, 1, z0 + 3, "industrial", "west")
    b.chest(x1 - 3, 1, z0 + 5, "space_center", "west", "barrel")
    for y in range(12, 19):
        b.set(x0 + 35, y, z0 + 30, S("chain", axis="y"))
    b.set(x0 + 35, 11, z0 + 30, S("crane_hook", facing="north"))


def fuel_depot(b, x0, z0):
    for (tx, tz) in ((x0 + 12, z0 + 14), (x0 + 36, z0 + 14), (x0 + 12, z0 + 40), (x0 + 36, z0 + 40)):
        r = 9
        for y in range(0, 22):
            for x in range(tx - r, tx + r + 1):
                for z in range(tz - r, tz + r + 1):
                    d = math.sqrt((x - tx) ** 2 + (y - 11) ** 2 + (z - tz) ** 2)
                    if r - 1 <= d <= r:
                        b.set(x, y + 4, z, WHITE if y % 6 else S("light_gray_concrete"))
        for (dx, dz) in ((-5, -5), (5, -5), (-5, 5), (5, 5)):
            for y in range(0, 8):
                b.set(tx + dx, y, tz + dz, S("truss"))
    px, pz = x0 + 58, z0 + 24
    b.room(px, 0, pz, px + 14, 6, pz + 12, S("cracked_concrete"), S("hazard_concrete"))
    door(b, px, 1, pz + 6, 1, 3)
    b.set(px + 4, 1, pz + 3, S("fuel_cabinet", facing="south"))
    b.set(px + 8, 1, pz + 3, S("diesel_generator", facing="south"))
    debris(b, px + 7, 1, pz + 8, 8, 4)
    for x in range(x0 + 12, px):
        b.set(x, 2, pz + 6, S("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    b.set(px + 12, 1, pz + 10, S("warning_sign", facing="west"))


def housing(b, x0, z0):
    for i in range(3):
        bx = x0 + i * 58
        levels = facade_building(b, bx, z0, bx + 50, z0 + 16, 4, 5, S("bricks"), S("glass"), S("spruce_planks"),
                                 roof=S("gray_concrete"), pillars=3, window_rows=(2, 3))
        stair_core(b, bx + 25, z0 + 2, levels, 20)
        door(b, bx + 24, 1, z0 + 16, 3, 3)
        for f, y in enumerate(levels):
            for x in range(bx + 2, bx + 49, 4):
                b.set(x, y + 1, z0 + 2, S("bunk_bed", facing="south"))
                b.set(x + 2, y + 1, z0 + 14, S("locker", facing="north"))
                if b.rnd.random() < 0.15:
                    skeleton(b, x + 1, y + 1, z0 + 6, pose=b.rnd.choice(SKELETON_POSES))
            if f == 0:
                for x in range(bx + 6, bx + 45, 6):
                    b.set(x, y + 1, z0 + 8, S("smooth_stone_slab", type="top"))
                b.set(bx + 4, y + 1, z0 + 8, S("vending_machine", facing="east"))
            b.chest(bx + 48, y + 1, z0 + 8, "bunker_food" if f % 2 else "civic", "west", "barrel")
        collapse(b, bx, z0, bx + 50, z0 + 16, 12, 21, 0.06)


def solar_field(b, x0, z0):
    for x in range(x0, x0 + 70, 3):
        for z in range(z0, z0 + 90, 5):
            if b.rnd.random() < 0.85:
                b.set(x, 1, z, S("iron_bars"))
                b.set(x, 2, z, S("solar_panel"))
                b.set(x + 1, 2, z, S("solar_panel") if b.rnd.random() < 0.8 else AIR)


# ---------------------------------------------------------------- the mothership blueprints
def ship_radius(t):
    """Hull radius along the ship (t = 0 stern .. 1 bow)."""
    if t < 0.06:
        return 14 + t / 0.06 * 8
    if t > 0.82:
        return max(4.0, 22 - (t - 0.82) / 0.18 * 18)
    return 22


def write_blueprints():
    BLUEPRINTS.mkdir(parents=True, exist_ok=True)
    cx, cy, cz = CONSOLE
    sections = {k: [] for k in ("keel", "hull", "engines", "reactor", "life_support", "hydroponics", "laboratory",
                                "quarters", "hangar", "bridge")}
    length = SHIP_X1 - SHIP_X0

    def put(sec, x, y, z, state):
        sections[sec].append((x - cx, y - cy, z - cz, state))

    def st(name, **props):
        full = name if ":" in name else "intersolarsystem:" + name
        if props:
            full += "[" + ",".join(f"{k}={v}" for k, v in sorted(props.items())) + "]"
        return full

    # Keel: a 6 x 6 box spine and rib rings every 12 blocks.
    for x in range(SHIP_X0, SHIP_X1 + 1):
        t = (x - SHIP_X0) / length
        for dy in range(-3, 3):
            for dz in range(-3, 3):
                if dy in (-3, 2) or dz in (-3, 2):
                    put("keel", x, SHIP_Y - 16 + dy, SHIP_Z + dz, st("truss") if (x + dy) % 4 == 0 else st("station_hull"))
        if (x - SHIP_X0) % 12 == 0:
            r = ship_radius(t) - 1
            for a in range(0, 360, 3):
                y = SHIP_Y + round(r * math.sin(math.radians(a)))
                z = SHIP_Z + round(r * math.cos(math.radians(a)))
                put("keel", x, y, z, st("truss"))
    # Hull: the shell with window bands, the Aurora stripe and the deck floors inside.
    for x in range(SHIP_X0, SHIP_X1 + 1):
        t = (x - SHIP_X0) / length
        r = ship_radius(t)
        seen = set()
        for a10 in range(0, 3600, 6):
            a = math.radians(a10 / 10)
            y = SHIP_Y + round(r * math.sin(a))
            z = SHIP_Z + round(r * math.cos(a))
            if (y, z) in seen:
                continue
            seen.add((y, z))
            dy = y - SHIP_Y
            band = abs(dy) <= 1 and 0.08 < t < 0.8 and x % 4 != 0
            stripe = abs(z - SHIP_Z) < 3 and dy > 0 and 0.1 < t < 0.75
            state = st("station_window") if band else st("ship_stripe") if stripe else st("station_hull") if (x // 6 + dy // 5) % 7 else st("station_foil")
            put("hull", x, y, z, state)
        if 0.06 < t < 0.9:
            for deck in (-10, 0, 10):
                half = int(math.sqrt(max(0, (r - 1) ** 2 - deck ** 2)))
                for z in range(SHIP_Z - half, SHIP_Z + half + 1):
                    put("hull", x, SHIP_Y + deck - 1, z, st("metal_grating") if abs(z - SHIP_Z) < 2 else st("ship_plating"))
    # Bulkheads with doors every 40 blocks.
    for x in range(SHIP_X0 + 30, SHIP_X1 - 50, 40):
        r = ship_radius((x - SHIP_X0) / length) - 1
        for y in range(SHIP_Y - int(r), SHIP_Y + int(r) + 1):
            for z in range(SHIP_Z - int(r), SHIP_Z + int(r) + 1):
                if math.hypot(y - SHIP_Y, z - SHIP_Z) < r and not (abs(z - SHIP_Z) <= 1 and (y - SHIP_Y + 10) % 10 in (0, 1, 2)):
                    put("hull", x, y, z, st("ship_plating"))
    # Engines: five nozzles at the stern, their housings and fuel lines.
    for (dy, dz) in ((0, 0), (9, 9), (9, -9), (-9, 9), (-9, -9)):
        for x in range(SHIP_X0 - 14, SHIP_X0):
            rr = 3 + (SHIP_X0 - x) * 0.35
            for y in range(-8, 9):
                for z in range(-8, 9):
                    d = math.hypot(y, z)
                    if rr - 1 <= d <= rr:
                        put("engines", x, SHIP_Y + dy + y, SHIP_Z + dz + z, st("ship_thruster") if x < SHIP_X0 - 10 else st("rusted_panel"))
        for x in range(SHIP_X0, SHIP_X0 + 16):
            put("engines", x, SHIP_Y + dy, SHIP_Z + dz, st("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
        put("engines", SHIP_X0 + 16, SHIP_Y + dy, SHIP_Z + dz, st("lander_engine"))
    # Reactor ring.
    rx = SHIP_X0 + 50
    for x in range(rx - 4, rx + 5):
        for a in range(0, 360, 5):
            y = SHIP_Y + round(8 * math.sin(math.radians(a)))
            z = SHIP_Z + round(8 * math.cos(math.radians(a)))
            put("reactor", x, y, z, st("reactor") if x == rx else st("ship_plating"))
    for x in range(rx - 3, rx + 4):
        put("reactor", x, SHIP_Y, SHIP_Z, st("minecraft:beacon") if x == rx else st("cable", east="true", west="true", north="false", south="false", up="false", down="false"))
    for z in (-6, 6):
        put("reactor", rx + 6, SHIP_Y - 9, SHIP_Z + z, st("coolant_tank"))
    # Life support: machines and air lines on the lower deck behind the reactor.
    for x in range(SHIP_X0 + 64, SHIP_X0 + 90, 3):
        for z in (-12, 12):
            put("life_support", x, SHIP_Y - 10, SHIP_Z + z, st("life_support_unit"))
            put("life_support", x, SHIP_Y - 6, SHIP_Z + z, st("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    # Hydroponics: rows of living soil with lights on the middle deck.
    for x in range(SHIP_X0 + 100, SHIP_X0 + 160):
        for z in range(SHIP_Z - 14, SHIP_Z + 15):
            if abs(z - SHIP_Z) > 2 and z % 3:
                put("hydroponics", x, SHIP_Y - 1, z, st("living_soil"))
            if x % 6 == 0 and z % 5 == 0:
                put("hydroponics", x, SHIP_Y + 8, z, st("ceiling_light"))
    # Laboratory on the upper deck.
    for x in range(SHIP_X0 + 100, SHIP_X0 + 150, 4):
        for z in (-10, 10):
            put("laboratory", x, SHIP_Y + 10, SHIP_Z + z, st("lab_bench", facing="south" if z < 0 else "north"))
            put("laboratory", x + 2, SHIP_Y + 10, SHIP_Z + z, st("specimen_tank"))
        put("laboratory", x, SHIP_Y + 10, SHIP_Z - 14, st("fume_hood", facing="south"))
    # Quarters and the mess on the upper deck forward.
    for x in range(SHIP_X0 + 170, SHIP_X0 + 240, 3):
        for z in (-14, 14):
            put("quarters", x, SHIP_Y + 10, SHIP_Z + z, st("bunk_bed", facing="south" if z < 0 else "north"))
        if x % 9 == 0:
            for z in range(SHIP_Z - 4, SHIP_Z + 5):
                put("quarters", x, SHIP_Y, z, st("smooth_stone_slab", type="top"))
    # Hangar: the open bay under the belly with its doors and lights.
    for x in range(SHIP_X0 + 170, SHIP_X0 + 240):
        for z in range(SHIP_Z - 12, SHIP_Z + 13):
            put("hangar", x, SHIP_Y - 12, z, st("hazard_concrete") if z in (SHIP_Z - 12, SHIP_Z + 12) else st("metal_grating"))
        if x % 10 == 0:
            put("hangar", x, SHIP_Y - 3, SHIP_Z, st("crane_hook", facing="north"))
    put("hangar", SHIP_X0 + 200, SHIP_Y - 10, SHIP_Z + 4, st("hangar_lift", facing="north"))
    # Bridge: the nose's command deck with consoles and the forward windows.
    bx = SHIP_X1 - 50
    for x in range(bx, bx + 30):
        for z in range(SHIP_Z - 8, SHIP_Z + 9):
            put("bridge", x, SHIP_Y + 3, z, st("office_carpet"))
    for z in range(SHIP_Z - 6, SHIP_Z + 7, 2):
        put("bridge", bx + 22, SHIP_Y + 4, z, st("data_terminal", facing="east", log=0))
    put("bridge", bx + 18, SHIP_Y + 4, SHIP_Z, st("mothership_helm", facing="east"))
    for x in range(SHIP_X1 - 22, SHIP_X1 + 1):
        r = ship_radius((x - SHIP_X0) / length)
        for a in range(-60, 61, 6):
            y = SHIP_Y + 4 + round(r * 0.5 * math.sin(math.radians(a)))
            z = SHIP_Z + round(r * math.cos(math.radians(90 - a)) * 0.6)
            put("bridge", x, y, z, st("station_window"))
    for name, blocks in sections.items():
        lines = ["# %s: %d blocks, relative to the crane console (facing north)" % (name, len(blocks))]
        blocks.sort(key=lambda b_: (b_[1], b_[0], b_[2]))
        seen = set()
        for (x, y, z, s) in blocks:
            if (x, y, z) in seen:
                continue
            seen.add((x, y, z))
            lines.append(f"{x} {y} {z} {s}")
        import gzip
        with gzip.GzipFile(BLUEPRINTS / (name + ".txt.gz"), "wb", mtime=0) as f:
            f.write(("\n".join(lines) + "\n").encode())
        old = BLUEPRINTS / (name + ".txt")
        if old.exists():
            old.unlink()
        print(f"blueprint {name:14s} {len(seen)}")
