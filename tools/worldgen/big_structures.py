"""The monumental ruins of the dead Earth: the international airport and the hospital complex (the big city lives in
city_structures.py). Built with the voxel builder of make_structures.py; every piece is plain code so the ruins are
reproducible.

Conventions: x/z are the template's own axes (the structure is rotated as a whole when placed), y = 0 is the ground
layer. Props face "north" when their front looks to -z.
"""
import math

from make_structures import AIR, CONCRETE, GLASS, LGREY, S, WHITE, Build

FACE = {(0, -1): "north", (0, 1): "south", (1, 0): "east", (-1, 0): "west"}
SKELETON_POSES = ("skeleton_lying", "skeleton_curled", "skeleton_sitting")


# ---------------------------------------------------------------- shared helpers
def ground(b, x0, z0, x1, z1, pick):
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            b.set(x, 0, z, pick(x, z))


def dead_ground(b):
    r = b.rnd.random()
    return S("coarse_dirt") if r < 0.45 else S("ash") if r < 0.75 else S("gravel") if r < 0.9 else S("dried_silt")


def asphalt(b, cracked=0.12):
    return S("cracked_asphalt") if b.rnd.random() < cracked else S("asphalt")


def concrete(b, cracked=0.15):
    r = b.rnd.random()
    return S("cracked_concrete") if r < cracked else S("light_gray_concrete") if r < 0.8 else S("smooth_stone")


def skeleton(b, x, y, z, facing=None, pose=None):
    b.set(x, y, z, S(pose or b.rnd.choice(SKELETON_POSES), facing=facing or b.rnd.choice(list(FACE.values()))))


def ladder_shaft(b, x, z, y0, y1, facing="south"):
    for y in range(y0, y1 + 1):
        b.set(x, y, z, S("ladder", facing=facing))


def collapse(b, x0, z0, x1, z1, y_from, y_to, amount, rubble=True):
    """Knocks holes into the upper part of a building (more near the top) and drops rubble below."""
    keep_prefix = ("minecraft:chest", "minecraft:barrel", "minecraft:ladder")
    for (x, y, z), st in list(b.blocks.items()):
        if not (x0 <= x <= x1 and z0 <= z <= z1 and y_from <= y <= y_to) or st == AIR:
            continue
        if st[0] in keep_prefix or "skeleton" in st[0] or "data_terminal" in st[0]:
            continue
        h = (y - y_from) / max(1, y_to - y_from)
        if b.rnd.random() < amount * (0.3 + 1.7 * h * h):
            b.set(x, y, z, AIR)
            if rubble and b.rnd.random() < 0.25:
                # The debris lands on the next floor down.
                for yy in range(y - 1, 0, -1):
                    below = b.get(x, yy, z)
                    if below is not None and below != AIR:
                        if b.get(x, yy + 1, z) in (None, AIR):
                            b.set(x, yy + 1, z, S("rubble"))
                        break


def facade_building(b, x0, z0, x1, z1, floors, storey, wall, glass, floor, roof=None, pillars=4, window_rows=(1, 2, 3)):
    """A building shell: walls with glazed bands between pillars, a slab per floor, a flat roof. Returns floor heights."""
    top = floors * storey
    levels = [f * storey for f in range(floors)]
    for y in range(0, top + 1):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                edge = x in (x0, x1) or z in (z0, z1)
                if y % storey == 0:
                    b.set(x, y, z, (roof or floor) if y == top else (wall if edge else floor))
                elif edge:
                    along = (x - x0) if z in (z0, z1) else (z - z0)
                    corner = x in (x0, x1) and z in (z0, z1)
                    band = (y % storey) in window_rows
                    b.set(x, y, z, glass if band and not corner and along % pillars != 0 else wall)
                else:
                    b.set(x, y, z, AIR)
    return levels


def stair_core(b, x, z, levels, top):
    """A ladder shaft through every slab (with a hole in each floor)."""
    for y in range(1, top):
        b.set(x, y, z, S("ladder", facing="south"))
    for y in levels[1:]:
        b.set(x, y, z, S("ladder", facing="south"))


def door(b, x, y, z, width=1, height=2):
    for dx in range(width):
        for dy in range(height):
            b.set(x + dx, y + dy, z, AIR)


# ---------------------------------------------------------------- airliner
def airliner(b, x0, y0, zc, length=46, facing_east=True, broken=False, burnt=False):
    """A wide-body airliner along x: round fuselage, cabin with seats, wings with engines, tail with the Aurora livery.
    The nose points to +x when facing_east. broken: the fuselage is split in three and the tail is torn off."""
    hull = S("black_concrete") if burnt else WHITE
    belly = S("gray_concrete") if burnt else S("light_gray_concrete")
    r = 3.6
    cy = y0 + 4
    gaps = (length // 3, length // 3 + 2, 2 * length // 3 + 1, 2 * length // 3 + 3) if broken else ()
    sx = 1 if facing_east else -1

    def X(i):
        return x0 + i * sx

    for i in range(length):
        if broken and (gaps[0] <= i < gaps[1] or gaps[2] <= i < gaps[3]):
            continue
        # Tapering at the nose and the tail.
        k = 1.0
        if i > length - 6:
            k = max(0.35, (length - i) / 6)
        elif i < 8:
            k = max(0.45, 0.45 + i / 14)
        rr = r * k
        tail_lift = max(0, (8 - i)) * 0.35 if i < 8 else 0
        sag = 0
        if broken:
            sag = -1 if i < gaps[0] else (1 if i >= gaps[3] else 0)
        for dy in range(-4, 5):
            for dz in range(-4, 5):
                d = math.hypot(dy, dz)
                y = int(round(cy + dy + tail_lift)) + sag
                if d <= rr + 0.45 and d >= rr - 0.55:
                    st = hull if dy >= -1 else belly
                    if dy == 1 and abs(dz) >= 2 and i % 2 == 0 and 6 < i < length - 7:
                        st = S("tinted_glass") if not burnt else AIR  # window rows
                    if burnt and b.rnd.random() < 0.18:
                        st = AIR
                    b.set(X(i), y, zc + dz, st)
                elif d < rr - 0.55:
                    b.set(X(i), y, zc + dz, AIR)
        # Cabin floor and seats.
        if 6 < i < length - 7 and k >= 0.9:
            fy = cy - 2 + sag
            for dz in range(-2, 3):
                b.set(X(i), fy, zc + dz, S("office_carpet") if not burnt else S("sooty_concrete"))
            if i % 2 == 0 and not burnt:
                for dz in (-2, 2):
                    b.set(X(i), fy + 1, zc + dz, S("airport_seats", facing="east" if facing_east else "west"))
                if b.rnd.random() < 0.15:
                    skeleton(b, X(i), fy + 1, zc + b.rnd.choice((-1, 1)), pose="skeleton_sitting",
                             facing="east" if facing_east else "west")
    # Cockpit windows.
    for dz in (-1, 0, 1):
        b.set(X(length - 3), cy + 1, zc + dz, S("tinted_glass") if not burnt else AIR)
    # Wings (swept back), engines below them.
    wing_i = length // 2 - 2
    for side in (-1, 1):
        for s in range(3, 19):
            back = s // 3
            for c in range(0, max(2, 7 - s // 4)):
                i = wing_i - back - c
                y = cy - 2 + s // 8
                if broken and side == -1 and s > 10:
                    continue  # one wing tip lies broken off
                st = belly if c > 0 else hull
                if burnt and b.rnd.random() < 0.2:
                    st = S("sooty_concrete")
                b.set(X(i), y, zc + side * s, st)
        eng_z = zc + side * 8
        b.set(X(wing_i - 3), cy - 4, eng_z, S("jet_engine", facing="east" if facing_east else "west"))
    if broken:
        for s in range(11, 19):
            b.set(X(wing_i + 6 - s // 3), y0 + 1, zc - s - 3, belly if b.rnd.random() > 0.2 else S("sooty_concrete"))
    # Tail fin with the Aurora stripe, stabilizers.
    for h in range(0, 9):
        for c in range(0, 6 - h // 2):
            st = S("orange_concrete") if h in (4, 5) else S("blue_concrete") if h > 6 else hull
            b.set(X(c + h // 2), cy + 4 + h, zc, st)
    for side in (-1, 1):
        for s in range(1, 7):
            for c in range(0, 4 - s // 3):
                b.set(X(c + s // 3), cy + 3, zc + side * s, belly)
    # Landing gear (or the scrape marks of the belly landing).
    if not broken:
        for (i, dz) in ((length - 8, 0), (wing_i - 1, -3), (wing_i - 1, 3)):
            for y in range(y0 + 1, cy - 3):
                b.set(X(i), y, zc + dz, S("iron_bars"))
            b.set(X(i), y0 + 1, zc + dz, S("black_concrete"))
    else:
        for i in range(-20, 0):
            b.set(X(i), y0, zc + (i % 3) - 1, S("sooty_concrete"))


# ---------------------------------------------------------------- the international airport
def airport_complex():
    """Aurora International: a 220 m runway with a crashed airliner at its end, the taxiway, a concrete apron with a
    parked airliner, the three-storey terminal with jet bridges, a 44 m control tower, a hangar, the fuel farm, the
    landside road and the parking lot. The terminal was the last evacuation point: barricades, quarantine posters,
    a field hospital in the check-in hall."""
    b = Build("airport", 5001)
    W, D = 220, 132
    # Ground everywhere first (the structure stands on a levelled box).
    ground(b, 0, 0, W - 1, D - 1, lambda x, z: dead_ground(b))
    # Runway (z 2..14): asphalt, edge lines, dashed centre line, threshold bars, touchdown zones, lights.
    for x in range(0, W):
        for z in range(2, 15):
            st = asphalt(b, 0.1)
            if z in (3, 13):
                st = S("runway_marking", facing="east")
            if z in (8,) and (x // 6) % 2 == 0 and 14 < x < W - 14:
                st = S("runway_marking", facing="east")
            if (x < 10 or x > W - 11) and z % 2 == 0 and 4 <= z <= 12:
                st = S("runway_marking", facing="east")
            if (30 <= x < 40 or W - 40 <= x < W - 30) and z in (5, 6, 10, 11):
                st = S("runway_marking", facing="east")
            b.set(x, 0, z, st)
        if x % 8 == 0:
            b.set(x, 1, 1, S("runway_light", facing="north"))
            b.set(x, 1, 15, S("runway_light", facing="south"))
    # Runway numbers as big painted blocks (09 / 27) near each end.
    for (sx, digits) in ((12, "09"), (W - 24, "27")):
        for di, ch in enumerate(digits):
            glyph = {"0": ("###", "#.#", "#.#", "#.#", "###"), "9": ("###", "#.#", "###", "..#", "###"),
                     "2": ("###", "..#", "###", "#..", "###"), "7": ("###", "..#", "..#", "..#", "..#")}[ch]
            for gy, row in enumerate(glyph):
                for gx, c in enumerate(row):
                    if c == "#":
                        b.set(sx + gy, 0, 6 + di * 4 + gx, S("runway_marking", facing="east"))
    # Taxiway (z 22..27) and two links to the runway, yellow centre line.
    for x in range(16, W - 16):
        for z in range(22, 28):
            b.set(x, 0, z, asphalt(b, 0.08))
        b.set(x, 0, 25, S("yellow_terracotta"))
    for lx in (28, W - 34):
        for x in range(lx, lx + 6):
            for z in range(15, 22):
                b.set(x, 0, z, asphalt(b, 0.08))
        for z in range(15, 25):
            b.set(lx + 3, 0, z, S("yellow_terracotta"))
    for x in range(16, W - 16, 10):
        b.set(x, 1, 21, S("runway_bollard", facing="north"))
    # Apron (z 28..62): concrete slabs with expansion joints and parking stands.
    for x in range(40, 184):
        for z in range(28, 63):
            st = concrete(b, 0.12)
            if x % 8 == 0 or z % 8 == 0:
                st = S("gray_concrete")
            b.set(x, 0, z, st)
    for sx in (72, 112, 152):
        for z in range(34, 60):
            b.set(sx, 0, z, S("yellow_terracotta"))
    # Parked airliner at the middle stand (nose to the terminal), service trucks, baggage carts.
    airliner(b, 104, 0, 46, length=40, facing_east=True)
    for (x, z, car) in ((150, 40, "car_wreck_white"), (95, 56, "car_wreck_white"), (130, 33, "ambulance_wreck")):
        b.set(x, 1, z, S(car, facing=b.rnd.choice(("east", "west"))))
    for x in range(140, 152, 2):
        b.set(x, 1, 52, S("shipping_crate"))
        if b.rnd.random() < 0.5:
            b.set(x, 2, 52, S("shipping_crate"))
    # The crashed airliner at the east end of the runway: broken in three, burnt, skeletons around.
    airliner(b, W - 6, 0, 9, length=48, facing_east=False, broken=True, burnt=True)
    for _ in range(14):
        x, z = b.rnd.randrange(W - 70, W - 4), b.rnd.randrange(2, 22)
        if b.get(x, 1, z) in (None, AIR):
            b.set(x, 1, z, S("rubble") if b.rnd.random() < 0.6 else S("scrap_pile"))
    for _ in range(5):
        x, z = b.rnd.randrange(W - 60, W - 10), b.rnd.randrange(16, 22)
        skeleton(b, x, 1, z, pose="skeleton_suited" if b.rnd.random() < 0.2 else "skeleton_lying")
    b.chest(W - 30, 3, 9, "cargo", "north")
    b.chest(W - 14, 3, 9, "wreck", "south", "barrel")
    terminal(b, 48, 63, 175, 98)
    control_tower(b, 186, 66)
    hangar(b, 2, 30)
    fuel_farm(b, 190, 30)
    # Landside: the road (z 100..106) and the parking lot (z 108..130) with wrecks and lamps.
    for x in range(0, W):
        for z in range(100, 107):
            st = asphalt(b, 0.15)
            if z == 103 and x % 6 < 3:
                st = S("road_marking", facing="east")
            b.set(x, 0, z, st)
        if x % 14 == 0:
            b.set(x, 1, 99, S("street_lamp", facing="south"))
    cars = ("car_wreck_red", "car_wreck_blue", "car_wreck_white", "taxi_wreck", "car_wreck_white")
    for x in range(40, 180):
        for z in range(108, 131):
            b.set(x, 0, z, asphalt(b, 0.2))
            if x % 4 == 0 and z in (108, 119, 120, 130):
                b.set(x, 0, z, S("road_marking", facing="north"))
    for row_z in (110, 117, 122, 129):
        for x in range(42, 178, 4):
            if b.rnd.random() < 0.55:
                b.set(x + 1, 1, row_z, S(b.rnd.choice(cars), facing="north" if row_z in (110, 122) else "south"))
    for x in range(40, 180, 20):
        b.set(x, 1, 119, S("street_lamp", facing="east"))
    # Evacuation queue: barriers from the parking lot to the terminal door, a military checkpoint, sandbags.
    for z in range(99, 108, 2):
        b.set(108, 1, z, S("quarantine_barrier", facing="east"))
        b.set(114, 1, z, S("quarantine_barrier", facing="east"))
    for x in range(104, 120):
        if x not in range(109, 114):
            b.set(x, 1, 107, S("sandbags", facing="south"))
    for x in (100, 122):
        b.set(x, 1, 107, S("warning_sign", facing="south"))
    b.set(111, 1, 100, S("skeleton_lying", facing="north"))
    b.set(110, 1, 104, S("skeleton_curled", facing="east"))
    # Light decay over everything except the paved areas.
    b.decay(0.008, keep=("intersolarsystem:asphalt", "intersolarsystem:cracked_asphalt", "intersolarsystem:runway_marking",
                         "minecraft:ladder"))
    b.clear_above(16)
    return b


def terminal(b, x0, z0, x1, z1):
    """Three storeys: arrivals and check-in on the ground, departures and gates above, offices on top. The airside
    wall (z0) is glass from floor to roof, three jet bridges reach out over the apron."""
    storey = 7
    levels = facade_building(b, x0, z0, x1, z1, 3, storey, LGREY, S("curtain_wall"), S("terminal_floor"),
                             roof=S("metal_grating"), pillars=6, window_rows=(1, 2, 3, 4, 5))
    top = 3 * storey
    # Broken panes here and there.
    for (x, y, z), st in list(b.blocks.items()):
        if st == S("curtain_wall") and b.rnd.random() < 0.18:
            b.set(x, y, z, S("broken_curtain_wall") if b.rnd.random() < 0.6 else AIR)
    # A curved canopy over the landside entrance and the entrance doors.
    for x in range(x0 + 20, x1 - 19):
        b.set(x, 5, z1 + 1, S("smooth_stone_slab", type="bottom"))
        b.set(x, 5, z1 + 2, S("smooth_stone_slab", type="bottom"))
        b.set(x, 4, z1 + 3, S("smooth_stone_slab", type="top"))
    for x in range(x0 + 20, x1 - 19, 12):
        for y in range(1, 5):
            b.set(x, y, z1 + 3, S("iron_bars"))
    for dx in range(54, 74):
        door(b, x0 + dx, 1, z1, 1, 4)
    # Atrium: a void through the middle two storeys over the hall.
    mid = (x0 + x1) // 2
    for x in range(mid - 12, mid + 13):
        for z in range(z0 + 8, z0 + 18):
            b.set(x, levels[1], z, AIR)
    for x in range(mid - 12, mid + 13):
        b.set(x, levels[1] + 1, z0 + 7, S("iron_bars"))
        b.set(x, levels[1] + 1, z0 + 18, S("iron_bars"))
    # Stair/ladder cores at both ends.
    for cx in (x0 + 3, x1 - 3):
        stair_core(b, cx, z1 - 3, levels, top)
    # Ground floor: check-in counters, departure boards, a field hospital, the security line, baggage belts.
    y = 1
    for x in range(x0 + 8, x1 - 8, 2):
        if not (mid - 14 <= x <= mid + 14):
            b.set(x, y, z0 + 24, S("office_desk", facing="south"))
            if b.rnd.random() < 0.3:
                b.set(x, y, z0 + 25, S("shipping_crate"))
    for x in range(x0 + 10, x1 - 10, 16):
        b.set(x, 5, z1 - 1, S("departure_board", facing="north"))
        b.set(x, 5, z0 + 1, S("departure_board", facing="south"))
    # Field hospital (the evacuation was screened for Ashlung here).
    for i, x in enumerate(range(mid - 10, mid + 11, 3)):
        for z in (z0 + 10, z0 + 15):
            b.set(x, y, z, S("hospital_bed", facing="north"))
            if b.rnd.random() < 0.55:
                skeleton(b, x, y + 1, z, facing="south", pose="skeleton_lying")
        b.set(x + 1, y, z0 + 12, S("iv_stand", facing="north"))
    for x in (mid - 13, mid + 13):
        for z in range(z0 + 8, z0 + 19, 2):
            b.set(x, y, z, S("quarantine_barrier", facing="east"))
    b.set(mid, y, z0 + 20, S("medical_cabinet", facing="south"))
    b.set(mid + 3, y, z0 + 20, S("data_terminal", facing="south", log=12))
    for x in range(x0 + 8, x0 + 30, 4):  # security gates
        for dz in range(0, 3):
            b.set(x, y + dz // 3, z0 + 20 + dz, S("iron_bars"))
    for x in range(x1 - 30, x1 - 6):  # baggage belt
        b.set(x, y, z0 + 4, S("metal_grating"))
        b.set(x, y, z0 + 5, S("metal_grating"))
        if b.rnd.random() < 0.3:
            b.set(x, y + 1, z0 + 4, S("shipping_crate"))
    b.chest(x0 + 2, y, z0 + 30, "civic", "east")
    b.chest(x1 - 2, y, z0 + 2, "cargo", "west", "barrel")
    for p in range(6):
        b.set(x0 + 12 + p * 18, 3, z1 - 1, S("poster_exodus" if p % 2 == 0 else "poster_quarantine", facing="north"))
    # Departures (second floor): rows of seats at the gates, a café, vending machines.
    y = levels[1] + 1
    for gx in (x0 + 18, x0 + 62, x0 + 104):
        for row in range(3):
            for x in range(gx - 8, gx + 9, 2):
                b.set(x, y, z0 + 4 + row * 3, S("airport_seats", facing="north"))
                if b.rnd.random() < 0.12:
                    skeleton(b, x, y, z0 + 5 + row * 3, pose="skeleton_curled")
        b.set(gx, y, z0 + 2, S("office_desk", facing="north"))
        b.set(gx + 1, y + 3, z0 + 1, S("departure_board", facing="south"))
    for x in range(x0 + 40, x0 + 52, 3):
        b.set(x, y, z1 - 2, S("vending_machine", facing="north"))
    for x in range(x1 - 40, x1 - 22, 3):
        for z in (z1 - 10, z1 - 6):
            b.set(x, y, z, S("park_bench", facing="north"))
    b.chest(x1 - 4, y, z1 - 4, "civic", "west", "barrel")
    b.chest(x0 + 6, y, z0 + 26, "bunker_food", "east")
    # Offices (third floor): desks, filing cabinets, the operations centre with server racks.
    y = levels[2] + 1
    for x in range(x0 + 6, x1 - 6, 4):
        for z in (z0 + 6, z0 + 14, z1 - 10):
            if b.rnd.random() < 0.8:
                b.set(x, y, z, S("office_desk", facing="north"))
                b.set(x, y, z + 1, S("office_chair", facing="north"))
    for x in range(x0 + 4, x1 - 4, 9):
        b.set(x, y, z1 - 2, S("filing_cabinet", facing="north"))
    for z in range(z0 + 20, z0 + 30):
        b.set(x1 - 8, y, z, S("server_rack", facing="west"))
        b.set(x1 - 12, y, z, S("server_rack", facing="east"))
    b.set(x1 - 10, y, z0 + 19, S("data_terminal", facing="north", log=12))
    b.chest(x1 - 10, y, z0 + 31, "lab", "north")
    skeleton(b, x0 + 30, y, z0 + 15, pose="skeleton_sitting", facing="north")
    for y2 in (levels[0] + 6, levels[1] + 6, levels[2] + 6):
        for x in range(x0 + 6, x1 - 5, 10):
            for z in range(z0 + 6, z1 - 4, 10):
                if b.get(x, y2, z) == AIR:
                    b.set(x, y2, z, S("ceiling_light"))
    # Jet bridges: enclosed corridors on legs from the departures floor to the stands.
    for gx in (x0 + 24, x0 + 64, x0 + 104):
        jy = levels[1] + 1
        for z in range(z0 - 14, z0):
            for dx in range(-2, 3):
                for dy in range(-1, 4):
                    edge = abs(dx) == 2 or dy in (-1, 3)
                    st = S("station_hull") if edge else AIR
                    if edge and dy in (1,) and abs(dx) == 2 and z % 2:
                        st = S("tinted_glass")
                    b.set(gx + dx, jy + dy, z, st)
            if z % 6 == 0:
                for y in range(1, jy - 1):
                    b.set(gx - 1, y, z, S("iron_bars"))
                    b.set(gx + 1, y, z, S("iron_bars"))
        door(b, gx - 1, jy, z0, 3, 3)
        for dx in range(-2, 3):  # the cab end hangs open over the apron
            for dy in range(0, 3):
                b.set(gx + dx, jy + dy, z0 - 15, AIR if abs(dx) < 2 and dy < 3 else S("station_hull"))
    collapse(b, x0, z0, x1, z1, levels[2], top, 0.06)
    # One end of the roof came down into the offices.
    for x in range(x1 - 26, x1 - 10):
        for z in range(z0 + 2, z0 + 14):
            if b.rnd.random() < 0.7:
                b.set(x, top, z, AIR)
            if b.rnd.random() < 0.35:
                b.set(x, levels[2] + 1, z, S("rubble") if b.rnd.random() < 0.7 else S("rebar_concrete"))


def control_tower(b, x0, z0):
    """A 44 m tower: a concrete shaft with a ladder, the glazed cab with consoles and radar on top."""
    h = 40
    for y in range(1, h):
        for x in range(x0, x0 + 9):
            for z in range(z0, z0 + 9):
                d = max(abs(x - x0 - 4), abs(z - z0 - 4))
                if d == 4 or (d == 3 and (x - x0) % 8 == 0 and (z - z0) % 8 == 0):
                    b.set(x, y, z, LGREY if not (y % 8 == 0) else S("gray_concrete"))
                elif d < 4:
                    b.set(x, y, z, AIR)
    for y in range(1, h + 1):
        b.set(x0 + 4, y, z0 + 4, S("ladder", facing="south"))
    door(b, x0 + 4, 1, z0 + 8, 1, 2)
    for y in range(4, h, 6):
        b.set(x0 + 4, y, z0, S("broken_curtain_wall"))
    # Cab: wider, glazed all round, sloped glass, consoles.
    cx, cz = x0 + 4, z0 + 4
    for y in range(h, h + 6):
        for x in range(cx - 6, cx + 7):
            for z in range(cz - 6, cz + 7):
                d = max(abs(x - cx), abs(z - cz))
                if y == h:
                    b.set(x, y, z, S("metal_grating") if d <= 6 else AIR)
                elif y == h + 5:
                    b.set(x, y, z, S("gray_concrete") if d <= 6 else AIR)
                elif d == 6:
                    b.set(x, y, z, S("curtain_wall") if y < h + 5 and b.rnd.random() > 0.2 else AIR)
                else:
                    b.set(x, y, z, AIR)
    b.set(cx, h, cz, S("ladder", facing="south"))
    for x in range(cx - 4, cx + 5):
        for (z, f) in ((cz - 5, "south"), (cz + 5, "north")):
            if x != cx:
                b.set(x, h + 1, z, S("office_desk", facing=f) if x % 2 else S("data_terminal", facing=f, log=12 if x == cx + 3 else 10))
    skeleton(b, cx - 2, h + 1, cz - 4, facing="north", pose="skeleton_sitting")
    b.chest(cx + 5, h + 1, cz, "radio", "west")
    for y in range(h + 6, h + 10):
        b.set(cx, y, cz, S("iron_bars"))
    b.set(cx, h + 10, cz, S("antenna_dish"))
    b.set(cx + 3, h + 6, cz + 3, S("station_light"))


def hangar(b, x0, z0):
    """A maintenance hangar with an arched roof, open towards the taxiway, a small jet inside on jacks."""
    w, d, rise = 34, 30, 16
    for z in range(z0, z0 + d):
        for x in range(x0, x0 + w):
            b.set(x, 0, z, concrete(b, 0.1))
        for i in range(0, w):
            t = (i - (w - 1) / 2) / ((w - 1) / 2)
            y = int(round(rise * (1 - t * t)))
            for yy in range(1, y + 1):
                if z in (z0 + d - 1,) or yy == y or (yy > y - 1):
                    b.set(x0 + i, yy, z, S("rusted_panel") if b.rnd.random() > 0.12 else AIR)
            for yy in range(1, y):
                if z != z0 + d - 1:
                    b.set(x0 + i, yy, z, AIR)
        if (z - z0) % 6 == 0:
            for i in range(0, w):
                t = (i - (w - 1) / 2) / ((w - 1) / 2)
                b.set(x0 + i, int(round(rise * (1 - t * t))) - 1, z, S("truss"))
    # The open front: sliding doors pushed aside.
    for y in range(1, 10):
        b.set(x0 + 1, y, z0, S("rusted_panel"))
        b.set(x0 + w - 2, y, z0, S("rusted_panel"))
    airliner(b, x0 + 4, 0, z0 + 14, length=24, facing_east=True)
    for x in range(x0 + 2, x0 + w - 2, 6):
        b.set(x, 1, z0 + d - 2, S("shipping_crate"))
        b.set(x + 1, 1, z0 + d - 2, S("locker", facing="north"))
    b.chest(x0 + 3, 1, z0 + d - 3, "industrial", "east")
    b.chest(x0 + w - 4, 1, z0 + d - 3, "cargo", "west", "barrel")


def fuel_farm(b, x0, z0):
    for (cx, cz) in ((x0 + 7, z0 + 7), (x0 + 7, z0 + 22), (x0 + 22, z0 + 14)):
        for y in range(0, 9):
            for x in range(cx - 6, cx + 7):
                for z in range(cz - 6, cz + 7):
                    d = math.hypot(x - cx, z - cz)
                    if y == 8 and d <= 6:
                        b.set(x, y, z, S("rusted_panel") if b.rnd.random() > 0.1 else AIR)
                    elif 5.3 <= d <= 6.2:
                        b.set(x, y, z, S("white_concrete") if y % 3 else S("light_gray_concrete"))
                    elif d < 5.3 and y > 0:
                        b.set(x, y, z, AIR)
                    elif d < 5.3:
                        b.set(x, y, z, S("tar_sand"))
        for y in range(1, 9):
            b.set(cx + 6, y, cz, S("ladder", facing="east"))
    for x in range(x0 - 2, x0 + 30):  # bund wall
        b.set(x, 1, z0 - 2, S("cracked_concrete"))
        b.set(x, 1, z0 + 31, S("cracked_concrete"))
    b.set(x0 + 14, 1, z0 + 30, S("warning_sign", facing="south"))
    b.chest(x0 + 16, 1, z0 + 2, "industrial", "south", "barrel")


# ---------------------------------------------------------------- the hospital complex
def hospital_complex():
    """St. Raphael General: a six-storey L-shaped hospital (wards, ICU, operating theatres, a rooftop helipad), the
    emergency entrance with ambulances, the morgue in the basement, and the quarantine camp on the car park where the
    Ashlung patients were held behind wire."""
    b = Build("hospital", 5101)
    W, D = 96, 84
    ground(b, 0, 0, W - 1, D - 1, lambda x, z: dead_ground(b))
    # Streets around it.
    for x in range(0, W):
        for z in range(0, 7):
            b.set(x, 0, z, S("road_marking", facing="east") if z == 3 and x % 6 < 3 else asphalt(b, 0.15))
    for z in range(0, D):
        for x in range(0, 7):
            b.set(x, 0, z, S("road_marking", facing="north") if x == 3 and z % 6 < 3 else asphalt(b, 0.15))
    for x in range(10, W, 14):
        b.set(x, 1, 7, S("street_lamp", facing="south"))
    b.set(7, 1, 7, S("traffic_light", facing="north"))
    b.set(14, 1, 8, S("fire_hydrant", facing="north"))
    # Main wing (along x) and the ward wing (along z): 6 storeys of 5.
    storey, floors = 5, 6
    wings = ((10, 10, 70, 26), (54, 26, 70, 72))
    for (x0, z0, x1, z1) in wings:
        facade_building(b, x0, z0, x1, z1, floors, storey, WHITE, S("glass"), S("floor_tiles"), roof=S("gray_concrete"),
                        pillars=3, window_rows=(2, 3))
    top = floors * storey
    # Open the wall between the two wings on every floor.
    for f in range(floors):
        for x in range(55, 70):
            for dy in range(1, storey):
                b.set(x, f * storey + dy, 26, AIR)
    # The red cross and the name band on the main facade.
    for (x, y) in ((40, 26), (40, 27), (40, 28), (39, 27), (41, 27)):
        b.set(x, y, 9, S("red_concrete"))
    for x in range(24, 57):
        b.set(x, top - 1, 9, S("blue_concrete") if x % 2 else S("white_concrete"))
    # Stair cores.
    for (x, z) in ((12, 24), (68, 70)):
        stair_core(b, x, z, [f * storey for f in range(floors)], top)
    # Ground floor: lobby with seats and reception, pharmacy, the emergency room with its own entrance.
    y = 1
    for dx in range(36, 44):
        door(b, dx, 1, 10, 1, 3)
    for x in range(30, 50, 3):
        for z in (14, 17):
            b.set(x, y, z, S("airport_seats", facing="north"))
    for x in range(32, 48):
        b.set(x, y, 22, S("office_desk", facing="north") if x % 2 else S("smooth_stone"))
    b.set(40, y, 23, S("data_terminal", facing="north", log=13))
    for x in range(14, 26, 2):
        b.set(x, y, 24, S("medical_cabinet", facing="north"))
    b.chest(14, y, 12, "medical", "east")
    b.chest(25, y, 12, "medical", "west", "barrel")
    # Emergency room: canopy, ambulances, trolleys.
    for x in range(58, 68):
        for z in range(4, 10):
            b.set(x, 4, z, S("smooth_stone_slab", type="top"))
    for x in (58, 67):
        for yy in range(1, 4):
            b.set(x, yy, 4, S("iron_bars"))
    door(b, 60, 1, 10, 6, 3)
    b.set(61, 1, 6, S("ambulance_wreck", facing="north"))
    b.set(76, 1, 14, S("ambulance_wreck", facing="west"))
    b.set(80, 1, 22, S("police_car_wreck", facing="north"))
    for x in range(58, 69, 2):
        for z in (14, 19):
            b.set(x, y, z, S("hospital_bed", facing="east"))
            if b.rnd.random() < 0.5:
                skeleton(b, x, y + 1, z, pose="skeleton_lying")
    b.set(64, y, 23, S("wheelchair", facing="north"))
    b.set(66, y, 23, S("iv_stand", facing="north"))
    # Ward floors: a central corridor, beds in bays on both sides, nurse stations, IV stands, skeletons.
    for f in range(1, floors):
        fy = f * storey + 1
        # Main wing: corridor along x at z 17..19.
        for x in range(12, 54, 4):
            for z in (12, 23):
                kind = "hospital_bed"
                b.set(x, fy, z, S(kind, facing="south" if z == 12 else "north"))
                b.set(x + 2, fy, z, S("iv_stand", facing="north"))
                if b.rnd.random() < 0.35:
                    skeleton(b, x, fy + 1 if b.rnd.random() < 0.5 else fy, z + (1 if z == 12 else -1), pose="skeleton_lying")
            for z in (16, 20):
                b.set(x + 1, fy, z, S("white_stained_glass_pane", north="false", south="false", east="true", west="true",
                                      waterlogged="false") if b.rnd.random() < 0.5 else AIR)
        # Ward wing: corridor along z at x 61..63.
        for z in range(28, 70, 4):
            for x in (56, 68):
                b.set(x, fy, z, S("hospital_bed", facing="east" if x == 56 else "west"))
                b.set(x, fy, z + 2, S("medical_cabinet", facing="east" if x == 56 else "west"))
                if b.rnd.random() < 0.3:
                    skeleton(b, x + (1 if x == 56 else -1), fy, z, pose=b.rnd.choice(SKELETON_POSES))
        b.set(62, fy, 30, S("office_desk", facing="north"))
        b.set(62, fy, 31, S("office_chair", facing="north"))
        b.set(61, fy, 30, S("data_terminal", facing="north", log=7))
        b.chest(60, fy, 46, "medical", "east")
        for z in range(40, 60, 9):
            b.set(63, fy, z, S("wheelchair", facing=b.rnd.choice(("north", "south"))))
        if f == 3:
            # ICU: the beds face the monitors, the doors are sealed with quarantine tape.
            for x in range(14, 50, 5):
                b.set(x, fy + 1, 13, S("server_rack", facing="south"))
            for x in range(20, 42, 2):
                b.set(x, fy, 18, S("quarantine_barrier", facing="north"))
            b.set(30, fy + 1, 9 + 1, S("poster_quarantine", facing="south"))
        if f == 2:
            # Operating theatres: tables under lamps.
            for x in (20, 34, 48):
                b.set(x, fy, 18, S("smooth_stone"))
                b.set(x, fy + 1, 18, S("skeleton_lying", facing="east"))
                b.set(x, fy + 3, 18, S("ceiling_light"))
                b.set(x - 2, fy, 17, S("medical_cabinet", facing="east"))
        if f == floors - 1:
            b.chest(50, fy, 24, "medical", "north", "barrel")
            b.chest(66, fy, 68, "lab", "north")
        for x in range(14, 54, 8):
            b.set(x, (f + 1) * storey - 1, 18, S("ceiling_light"))
        for z in range(30, 70, 8):
            b.set(62, (f + 1) * storey - 1, z, S("ceiling_light"))
    # Rooftop helipad on the main wing: a hazard circle with an H, and a landing light.
    cx, cz = 30, 18
    for x in range(cx - 7, cx + 8):
        for z in range(cz - 7, cz + 8):
            d = math.hypot(x - cx, z - cz)
            if d <= 7.4 and 10 < z < 26:
                st = S("hazard_concrete") if d > 6 else S("gray_concrete")
                b.set(x, top, z, st)
    for (x, z) in [(cx - 2, cz + dz) for dz in range(-3, 4)] + [(cx + 2, cz + dz) for dz in range(-3, 4)] + \
            [(cx + dx, cz) for dx in (-1, 0, 1)]:
        b.set(x, top, z, S("white_concrete"))
    for (x, z) in ((cx - 7, cz), (cx + 7, cz), (cx, cz - 6), (cx, cz + 6)):
        b.set(x, top + 1, z, S("runway_light", facing="north"))
    b.set(60, top + 1, 60, S("antenna_dish"))
    # Basement morgue under the main wing: a long cold room with drawers and the dead nobody claimed.
    b.room(14, -6, 12, 50, -1, 24, S("wall_tiles"), S("floor_tiles"))
    for y in range(-5, 1):
        b.set(13 + 1, y, 23, S("ladder", facing="north"))
    b.set(14, 0, 23, S("ladder", facing="north"))
    for x in range(16, 49, 2):
        b.set(x, -5, 13, S("iron_trapdoor", facing="south", half="bottom", open="false", powered="false", waterlogged="false"))
        b.set(x, -4, 13, S("iron_trapdoor", facing="south", half="bottom", open="true", powered="false", waterlogged="false"))
    for x in range(18, 48, 4):
        b.set(x, -5, 18, S("smooth_stone"))
        b.set(x, -4, 18, S("skeleton_lying", facing="east"))
    b.set(48, -5, 22, S("data_terminal", facing="north", log=13))
    b.chest(16, -5, 22, "medical", "east")
    # The quarantine camp on the car park east of the hospital: tents in rows, barbed wire, sandbag gate, signs.
    for x in range(74, W):
        for z in range(30, D):
            b.set(x, 0, z, asphalt(b, 0.25))
    for x in range(74, W):
        b.set(x, 1, 30, S("barbed_wire", facing="north"))
        b.set(x, 1, D - 1, S("barbed_wire", facing="north"))
    for z in range(30, D):
        if not 50 <= z <= 54:
            b.set(73, 1, z, S("barbed_wire", facing="east"))
        b.set(W - 1, 1, z, S("barbed_wire", facing="east"))
    for z in (49, 55):
        for yy in (1, 2):
            b.set(72, yy, z, S("sandbags", facing="east"))
    b.set(72, 1, 47, S("warning_sign", facing="west"))
    b.set(72, 1, 57, S("warning_sign", facing="west"))
    for tx in range(77, W - 4, 7):
        for tz in range(34, D - 6, 9):
            tent(b, tx, tz)
    # A trench at the far end where the dead were laid.
    for x in range(78, W - 3):
        for z in range(D - 5, D - 2):
            b.set(x, 0, z, S("coarse_dirt"))
            b.set(x, -1, z, S("coarse_dirt"))
            if b.rnd.random() < 0.25:
                b.set(x, 0, z, S(b.rnd.choice(SKELETON_POSES), facing=b.rnd.choice(("east", "west"))))
    collapse(b, 10, 10, 70, 72, storey * 4, top + 1, 0.08)
    b.decay(0.01, keep=("minecraft:ladder", "intersolarsystem:asphalt", "intersolarsystem:cracked_asphalt"),
            cracked={"minecraft:white_concrete": "calcite"}, top_bias=True)
    b.clear_above(14)
    return b


def tent(b, x0, z0):
    """A white quarantine tent (5 wide, 7 long) with two cots inside."""
    for z in range(z0, z0 + 7):
        for dx in range(0, 5):
            h = 3 - abs(dx - 2)
            b.set(x0 + dx, h, z, S("white_wool"))
            if dx in (0, 4):
                b.set(x0 + dx, 1, z, S("white_wool"))
            for yy in range(1, h):
                if 0 < dx < 4:
                    b.set(x0 + dx, yy, z, AIR)
    for dx in (1, 2, 3):
        b.set(x0 + dx, 1, z0, AIR)
        if dx == 2:
            b.set(x0 + dx, 2, z0, AIR)
    b.set(x0 + 1, 1, z0 + 2, S("hospital_bed", facing="north"))
    if b.rnd.random() < 0.6:
        b.set(x0 + 3, 1, z0 + 4, S("skeleton_lying", facing="north"))
    if b.rnd.random() < 0.3:
        b.chest(x0 + 3, 1, z0 + 6, "medical", "north", "barrel")
    b.set(x0 + 2, 1, z0 - 1, S("poster_quarantine", facing="north") if b.rnd.random() < 0.3 else AIR)
