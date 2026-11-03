"""Two big story sites of the dead Earth.

* The bunker network: an underground jigsaw complex. A command centre (the start) with a shaft and a guard house up to
  the surface; tunnels with cable trays and a rail line branch out to dormitories, a mess hall, the cryo bay, a
  medical bay, an armoury, hydroponics, water treatment and an archive. Collapsed tunnels end some branches.
* The Verdant laboratory: where the Grey Bloom (VRD-7) escaped. Greenhouses (number 4 shattered), the admin and lab
  building, the containment block with its airlock, the dried irrigation channel the strain rode into the river, and
  a sealed level below with the specimen vault.
"""
import math

from make_structures import AIR, CONCRETE, GREY, LGREY, S, WHITE, Build, jigsaw
from big_structures import SKELETON_POSES, collapse, door, facade_building, ground, skeleton, stair_core, dead_ground

WALL = S("gray_concrete")
FLOOR = S("polished_andesite")
TUNNEL_LEN = 24


def tunnel_jigsaw(b, x, z, orientation, pool="bunker_net/next"):
    st, tag = jigsaw("tunnel", "tunnel", pool, orientation, "minecraft:polished_andesite")
    b.set(x, 0, z, st, tag)


def bunker_room(b, x0, z0, x1, z1, h=5):
    b.room(x0, 0, z0, x1, h, z1, WALL, FLOOR)
    for x in range(x0 + 2, x1 - 1, 5):
        b.set(x, h - 1, (z0 + z1) // 2, S("ceiling_light"))


def opening(b, x, z, along_x, width=3, height=3):
    for d in range(-(width // 2), width // 2 + 1):
        for y in range(1, height + 1):
            b.set(x + (d if along_x else 0), y, z + (0 if along_x else d), AIR)


# ---------------------------------------------------------------- bunker network
def command_centre():
    b = Build("bunker_net/command", 7001)
    W, D, H = 21, 21, 7
    bunker_room(b, 0, 0, W - 1, D - 1, H)
    # Map table, consoles round the walls, the generator corner, the shaft to the surface.
    b.fill(8, 1, 8, 12, 1, 12, S("smooth_stone"))
    for x in range(8, 13):
        for z in range(8, 13):
            b.set(x, 2, z, S("white_carpet") if (x + z) % 2 else S("light_gray_carpet"))
    skeleton(b, 10, 1, 13, facing="north", pose="skeleton_sitting")
    for x in range(2, W - 2, 2):
        b.set(x, 1, 1, S("data_terminal", facing="south", log=8) if x == 10 else S("office_desk", facing="south"))
        if x % 4 == 0:
            b.set(x, 1, 2, S("office_chair", facing="north"))
    for z in range(3, D - 3, 2):
        b.set(1, 1, z, S("server_rack", facing="east"))
    b.set(W - 3, 1, D - 3, S("diesel_generator", facing="west"))
    b.set(12, 1, 1, S("radio_set", facing="south"))
    b.set(W - 3, 1, D - 5, S("diesel_generator", facing="west"))
    for z in range(D - 7, D - 1):
        b.set(W - 2, H - 1, z, S("cable", north="true", south="true", east="false", west="false", up="false", down="false"))
    b.chest(W - 2, 1, 2, "bunker", "west")
    b.chest(2, 1, D - 2, "military", "east")
    b.set(4, 3, D - 1, S("poster_exodus", facing="north"))
    # Shaft: from the north-west corner up to a guard house on the surface (26 up), blast door at the bottom.
    sx, sz = 3, 3
    for y in range(1, 30):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                edge = abs(dx) == 1 or abs(dz) == 1
                if y >= H:
                    b.set(sx + dx, y, sz + dz, WALL if edge else AIR)
        if y >= 1:
            b.set(sx, y, sz, S("ladder", facing="south"))
    b.set(sx + 3, 1, sz + 3, S("blast_door", facing="south"))
    # Guard house on the surface: concrete hut with sandbags, a door and a periscope antenna.
    gy = 30
    b.room(sx - 3, gy - 1, sz - 3, sx + 3, gy + 3, sz + 3, S("cracked_concrete"), S("cracked_concrete"))
    b.set(sx, gy - 1, sz, S("ladder", facing="south"))
    b.set(sx, gy, sz, S("iron_trapdoor", facing="north", half="bottom", open="true", powered="false", waterlogged="false"))
    door(b, sx, gy, sz + 3, 1, 2)
    for x in range(sx - 4, sx + 5):
        b.set(x, gy, sz + 5, S("sandbags", facing="south") if x != sx else AIR)
    b.set(sx + 2, gy + 4, sz - 2, S("iron_bars"))
    b.set(sx + 2, gy + 5, sz - 2, S("antenna_dish"))
    b.set(sx - 2, gy, sz - 2, S("warning_sign", facing="south"))
    # Exits: one tunnel from the middle of each side.
    for (x, z, o, along) in ((W // 2, 0, "north_up", True), (W // 2, D - 1, "south_up", True),
                             (0, D // 2, "west_up", False), (W - 1, D // 2, "east_up", False)):
        opening(b, x, z, along)
        tunnel_jigsaw(b, x, z, o, "bunker_net/tunnels")
    return b


def tunnel(variant, seed):
    b = Build("bunker_net/" + variant, seed)
    L = TUNNEL_LEN if variant != "junction" else 13
    b.room(0, 0, 0, 6, 5, L - 1, WALL, FLOOR)
    opening(b, 3, 0, True)
    for z in range(L):
        b.set(3, 1, z, S("rail", shape="north_south", waterlogged="false"))
        b.set(1, 4, z, S("cable", north="true", south="true", east="false", west="false", up="false", down="false"))
        b.set(5, 4, z, S("pipe", north="true", south="true", east="false", west="false", up="false", down="false"))
        if z % 6 == 3:
            b.set(3, 5, z, S("ceiling_light"))
    if variant == "straight":
        opening(b, 3, L - 1, True)
        tunnel_jigsaw(b, 3, L - 1, "south_up")
        if b.rnd.random() < 0.6:
            b.set(3, 1, 12, S("minecart") if False else S("shipping_crate"))
        skeleton(b, 1, 1, 15, facing="east", pose="skeleton_sitting")
    elif variant == "junction":
        opening(b, 3, L - 1, True)
        tunnel_jigsaw(b, 3, L - 1, "south_up")
        for x in (0, 6):
            opening(b, x, 6, False)
        tunnel_jigsaw(b, 0, 6, "west_up")
        tunnel_jigsaw(b, 6, 6, "east_up")
        b.set(1, 1, 2, S("warning_sign", facing="south"))
    elif variant == "collapsed":
        for z in range(12, L):
            for x in range(1, 6):
                for y in range(1, 5):
                    if y <= 1 + (z - 12) // 2 + (x % 2) and b.rnd.random() < 0.9:
                        b.set(x, y, z, b.rnd.choice((S("rubble"), S("gravel"), S("rebar_concrete"), S("cracked_concrete"))))
        skeleton(b, 2, 1, 10, pose="skeleton_curled")
        b.chest(5, 1, 9, "bunker", "west", "barrel")
    tunnel_jigsaw(b, 3, 0, "north_up")
    b.decay(0.02, keep=("minecraft:rail",), cracked=CONCRETE, top_bias=False)
    return b


def room_piece(name, seed, w, d, fill):
    b = Build("bunker_net/" + name, seed)
    bunker_room(b, 0, 0, w - 1, d - 1, 5)
    opening(b, w // 2, 0, True)
    fill(b, w, d)
    tunnel_jigsaw(b, w // 2, 0, "north_up")
    b.decay(0.015, cracked=CONCRETE, top_bias=False)
    return b


def f_dormitory(b, w, d):
    for x in range(1, w - 1, 3):
        for z in (2, d - 3):
            b.set(x, 1, z, S("bunk_bed", facing="north"))
            if b.rnd.random() < 0.3:
                skeleton(b, x + 1, 1, z, pose=b.rnd.choice(SKELETON_POSES))
    for x in range(1, w - 1, 4):
        b.set(x, 1, d // 2, S("locker", facing="south"))
    b.chest(w - 2, 1, d // 2 + 1, "bunker", "west", "barrel")
    b.set(w - 2, 3, d - 1, S("poster_quarantine", facing="north"))


def f_mess(b, w, d):
    for z in (3, 7):
        for x in range(2, w - 2):
            b.set(x, 1, z, S("smooth_stone_slab", type="top"))
            b.set(x, 1, z - 1, S("spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged="false") if x % 2 else AIR)
    for x in range(2, w - 2, 2):
        b.set(x, 1, d - 2, S("barrel", facing="up", open="false"))
    b.chest(1, 1, d - 2, "bunker_food", "east", "barrel")
    b.chest(w - 2, 1, d - 2, "bunker_food", "west")
    b.set(w // 2, 1, d - 2, S("vending_machine", facing="north"))
    skeleton(b, 4, 1, 5, pose="skeleton_lying")


def f_cryo(b, w, d):
    # Rows of cryo pods; most are dark, one stands open and empty (someone walked out of here).
    for x in range(2, w - 2, 3):
        for z in (2, d - 3):
            b.set(x, 1, z, S("cryo_pod", facing="south" if z == 2 else "north"))
    b.set(w // 2, 1, d - 3, AIR)
    b.set(w // 2, 1, d - 4, S("skeleton_lying", facing="north"))
    b.set(1, 1, d // 2, S("data_terminal", facing="east", log=8))
    b.set(w - 2, 1, d // 2, S("diesel_generator", facing="west"))
    b.chest(w - 2, 1, d // 2 + 2, "medical", "west")


def f_medical(b, w, d):
    for x in range(2, w - 2, 3):
        b.set(x, 1, d - 3, S("hospital_bed", facing="north"))
        b.set(x + 1, 1, d - 2, S("iv_stand", facing="north"))
        if b.rnd.random() < 0.5:
            skeleton(b, x, 2, d - 3, pose="skeleton_lying", facing="north")
    for x in range(1, w - 1, 3):
        b.set(x, 1, 1, S("medical_cabinet", facing="south"))
    b.chest(w - 2, 1, 3, "medical", "west")
    b.chest(1, 1, 3, "medical", "east", "barrel")


def f_armoury(b, w, d):
    for x in range(1, w - 1, 2):
        b.set(x, 1, d - 2, S("locker", facing="north"))
    for x in range(2, w - 2):
        b.set(x, 1, d // 2, S("sandbags", facing="south") if x != w // 2 else AIR)
    b.chest(1, 1, 2, "military", "east")
    b.chest(w - 2, 1, 2, "military", "west")
    skeleton(b, w // 2 + 2, 1, d // 2 + 1, pose="skeleton_sitting", facing="north")


def f_hydroponics(b, w, d):
    # Rows of dead plant beds under grow lights, the Grey Bloom got in here too.
    for z in range(2, d - 1, 3):
        for x in range(2, w - 2):
            b.set(x, 1, z, S("ash"))
            b.set(x, 2, z, S("dead_bush") if b.rnd.random() < 0.5 else AIR)
            b.set(x, 4, z, S("ceiling_light") if x % 3 == 0 else AIR)
    b.set(1, 1, 1, S("specimen_tank", facing="south"))
    b.set(w - 2, 1, 1, S("specimen_tank", facing="south"))
    b.chest(w - 2, 1, d - 2, "seed_bank", "west", "barrel")


def f_water(b, w, d):
    for (cx, cz) in ((3, 4), (w - 4, 4)):
        for y in range(1, 5):
            for x in range(cx - 1, cx + 2):
                for z in range(cz - 1, cz + 2):
                    b.set(x, y, z, S("white_concrete") if (x, z) != (cx, cz) else S("water", level=0) if y < 3 else AIR)
    for x in range(1, w - 1):
        b.set(x, 3, d - 2, S("pipe", north="false", south="false", east="true", west="true", up="false", down="false"))
    b.set(w // 2, 1, d - 2, S("diesel_generator", facing="north"))
    b.chest(1, 1, d - 2, "bunker", "east", "barrel")


def f_archive(b, w, d):
    for x in range(1, w - 1, 2):
        for z in (1, 4, 7):
            if z < d - 1:
                b.set(x, 1, z, S("filing_cabinet", facing="south"))
    b.set(w // 2, 1, d - 2, S("office_desk", facing="north"))
    b.set(w // 2 - 1, 1, d - 2, S("data_terminal", facing="north", log=8))
    b.chest(w - 2, 1, d - 2, "lab", "west")
    skeleton(b, w // 2, 1, d - 3, facing="south", pose="skeleton_sitting")


ROOMS = (("dormitory", 7101, 15, 11, f_dormitory, 3), ("mess", 7102, 15, 11, f_mess, 2),
         ("cryo", 7103, 17, 11, f_cryo, 2), ("medical", 7104, 13, 9, f_medical, 2), ("armoury", 7105, 11, 9, f_armoury, 2),
         ("hydroponics", 7106, 15, 13, f_hydroponics, 2), ("water", 7107, 13, 11, f_water, 1),
         ("archive", 7108, 11, 9, f_archive, 2))


def bunker_network_pieces():
    pieces = {"bunker_net/command": command_centre()}
    for v, seed in (("straight", 7011), ("junction", 7012), ("collapsed", 7013)):
        pieces["bunker_net/" + v] = tunnel(v, seed)
    for name, seed, w, d, fill, _ in ROOMS:
        pieces["bunker_net/" + name] = room_piece(name, seed, w, d, fill)
    return pieces


BUNKER_POOLS = {
    "bunker_net/start": [("bunker_net/command", 1)],
    "bunker_net/tunnels": [("bunker_net/straight", 3), ("bunker_net/junction", 2)],
    "bunker_net/next": [("bunker_net/straight", 3), ("bunker_net/junction", 2), ("bunker_net/collapsed", 1)]
    + [("bunker_net/" + r[0], r[5] * 2) for r in ROOMS],
    "bunker_net/ends": [("bunker_net/" + r[0], r[5]) for r in ROOMS],
}


# ---------------------------------------------------------------- the Verdant laboratory
def verdant_lab():
    """Verdant Biosystems, field laboratory: four greenhouse halls (number 4 shattered), the lab building, the
    containment block with an airlock, the irrigation channel, the specimen vault below."""
    b = Build("verdant_lab", 7201)
    W, D = 110, 90
    ground(b, 0, 0, W - 1, D - 1, lambda x, z: S("ash") if b.rnd.random() < 0.7 else dead_ground(b))
    # Perimeter fence with warning signs, the gate and the company sign.
    for x in range(0, W):
        for z in (0, D - 1):
            if not (z == D - 1 and 50 <= x <= 58):
                b.set(x, 1, z, S("iron_bars"))
                b.set(x, 2, z, S("iron_bars") if x % 2 else AIR)
    for z in range(0, D):
        for x in (0, W - 1):
            b.set(x, 1, z, S("iron_bars"))
    for x in range(10, W, 25):
        b.set(x, 1, D - 2, S("warning_sign", facing="south"))
    for x in range(44, 66):
        b.set(x, 5, D + 1, S("green_concrete") if (x // 2) % 2 else S("lime_concrete"))
    for x in (44, 65):
        for y in range(1, 5):
            b.set(x, y, D + 1, S("iron_bars"))
    for x in range(48, 61):
        for z in range(D - 1, D + 6):
            b.set(x, 0, z, S("asphalt"))
    # Greenhouses: four long barrel-vault halls (glass on steel ribs) with planting rows.
    for i in range(4):
        gx = 6 + i * 26
        greenhouse(b, gx, 6, shattered=(i == 3))
    # Lab building (3 floors).
    x0, z0, x1, z1 = 8, 46, 48, 66
    levels = facade_building(b, x0, z0, x1, z1, 3, 5, WHITE, S("curtain_wall"), S("floor_tiles"), roof=S("gray_concrete"),
                             pillars=4, window_rows=(2, 3))
    stair_core(b, x0 + 2, z1 - 2, levels, 15)
    door(b, (x0 + x1) // 2, 1, z1, 2, 3)
    for f, y in enumerate(levels):
        fy = y + 1
        for x in range(x0 + 3, x1 - 2, 4):
            b.set(x, fy, z0 + 3, S("lab_bench", facing="south"))
            b.set(x, fy, z0 + 9, S("lab_bench", facing="north") if x % 8 else S("fume_hood", facing="north"))
            if b.rnd.random() < 0.25:
                skeleton(b, x + 1, fy, z0 + 6, pose=b.rnd.choice(SKELETON_POSES))
        for x in range(x0 + 4, x1 - 3, 6):
            b.set(x, fy, z1 - 3, S("specimen_tank", facing="north"))
        b.set(x1 - 3, fy, z1 - 2, S("data_terminal", facing="north", log=6))
        b.chest(x1 - 2, fy, z0 + 2, "lab", "west")
        b.set(x0 + 1, fy + 2, z0 + 12, S("poster_quarantine", facing="east") if f == 1 else AIR)
    b.chest(x0 + 4, levels[2] + 1, z1 - 4, "research_station", "east", "barrel")
    collapse(b, x0, z0, x1, z1, 10, 16, 0.06)
    # Containment block: windowless, an airlock with decontamination showers, the strain freezers.
    cx0, cz0, cx1, cz1 = 60, 46, 100, 70
    facade_building(b, cx0, cz0, cx1, cz1, 1, 7, S("light_gray_concrete"), S("light_gray_concrete"), S("hazard_concrete"),
                    roof=S("gray_concrete"), pillars=99, window_rows=())
    for x in range(cx0 + 1, cx1):
        b.set(x, 0, (cz0 + cz1) // 2, S("floor_tiles"))
    # Airlock: two doors, showers (water-coloured glass panes), signs.
    ax = (cx0 + cx1) // 2
    for z in range(cz1 - 5, cz1 + 1):
        for x in (ax - 2, ax + 2):
            for y in range(1, 4):
                b.set(x, y, z, S("light_gray_concrete"))
    door(b, ax - 1, 1, cz1, 3, 3)
    for x in range(ax - 1, ax + 2):
        for y in range(1, 4):
            b.set(x, y, cz1 - 5, S("iron_bars") if y > 1 or x != ax else AIR)
    b.set(ax, 4, cz1 - 2, S("light_blue_stained_glass"))
    b.set(ax - 2, 2, cz1 + 1, S("warning_sign", facing="south"))
    b.set(ax + 2, 2, cz1 + 1, S("poster_quarantine", facing="south"))
    for x in range(cx0 + 3, cx1 - 2, 4):
        b.set(x, 1, cz0 + 2, S("specimen_tank", facing="south"))
        b.set(x, 1, cz0 + 6, S("fume_hood", facing="north"))
    for z in range(cz0 + 9, cz1 - 7, 3):
        b.set(cx0 + 2, 1, z, S("server_rack", facing="east"))
        b.set(cx1 - 2, 1, z, S("cryo_pod", facing="west"))  # strain freezers
    b.set(ax + 6, 1, cz1 - 3, S("data_terminal", facing="north", log=6))
    b.chest(cx1 - 3, 1, cz1 - 2, "lab", "west")
    for (x, z) in ((ax - 6, cz0 + 10), (ax + 4, cz0 + 12)):
        skeleton(b, x, 1, z, pose="skeleton_suited")
    # Down to the specimen vault (8 below): a ladder in the containment block.
    vx0, vz0, vx1, vz1 = cx0 + 6, cz0 + 4, cx1 - 6, cz0 + 16
    b.room(vx0, -9, vz0, vx1, -1, vz1, S("light_gray_concrete"), S("hazard_concrete"))
    for y in range(-8, 1):
        b.set(vx0 + 1, y, vz1 - 1, S("ladder", facing="north"))
    for x in range(vx0 + 2, vx1 - 1, 3):
        for z in (vz0 + 1, vz1 - 3):
            b.set(x, -8, z, S("specimen_tank", facing="south"))
    b.set((vx0 + vx1) // 2, -8, (vz0 + vz1) // 2, S("blast_door", facing="south"))
    b.chest(vx1 - 1, -8, vz0 + 1, "lab", "west")
    b.set(vx0 + 3, -8, (vz0 + vz1) // 2, S("data_terminal", facing="east", log=6))
    # The irrigation channel: from the greenhouses down through the fence and out (dry, grey sludge at the bottom).
    for x in range(4, W - 4):
        for z in (40, 41, 42):
            b.set(x, 0, z, S("gray_concrete") if z != 41 else S("acidic_mud"))
            b.set(x, -1, z, S("gray_concrete"))
        if x % 12 == 0:
            b.set(x, 1, 39, S("pipe", north="false", south="true", east="false", west="false", up="false", down="false"))
    for z in range(41, D + 12):
        if z >= D - 1:
            for dx in (-1, 0, 1):
                b.set(W - 6 + dx, 0, z, S("acidic_mud") if dx == 0 else S("gray_concrete"))
    for x in range(W - 7, W - 4):
        b.set(x, 1, D - 1, AIR)
        b.set(x, 2, D - 1, AIR)
    b.decay(0.01, keep=("minecraft:ladder", "intersolarsystem:ash"), cracked=CONCRETE)
    b.clear_above(14)
    return b


def greenhouse(b, x0, z0, shattered=False):
    """A barrel-vault glass hall, 22 x 30, with planting rows; shattered: broken glass and ribs on the ground."""
    w, d, r = 22, 30, 10
    for z in range(z0, z0 + d):
        rib = (z - z0) % 5 == 0
        for i in range(w + 1):
            t = (i - w / 2) / (w / 2)
            y = int(round(r * math.sqrt(max(0.0, 1 - t * t))))
            for yy in range(1, max(1, y) + 1):
                if yy == max(1, y) or i in (0, w):
                    st = S("truss") if rib else S("glass")
                    if shattered and not rib and b.rnd.random() < 0.7:
                        st = AIR
                    b.set(x0 + i, yy, z, st)
                else:
                    b.set(x0 + i, yy, z, AIR)
        for i in range(2, w - 1):
            if i % 4 != 0:
                b.set(x0 + i, 0, z, S("ash") if b.rnd.random() < 0.8 else S("living_soil") if not shattered and b.rnd.random() < 0.05 else S("ash"))
                if b.rnd.random() < 0.2:
                    b.set(x0 + i, 1, z, S("dead_bush"))
            else:
                b.set(x0 + i, 0, z, S("gravel"))
    for i in range(w // 2 - 1, w // 2 + 2):
        for y in range(1, 4):
            b.set(x0 + i, y, z0 + d - 1, AIR)
    b.set(x0 + 1, 1, z0 + d - 2, S("lab_bench", facing="north"))
    b.chest(x0 + w - 1, 1, z0 + 2, "seed_bank", "west", "barrel")
    if shattered:
        # Greenhouse 4: glass on the ground, the irrigation outlet open, a body at the valve.
        for _ in range(40):
            x, z = x0 + b.rnd.randrange(1, w), z0 + b.rnd.randrange(0, d)
            if b.get(x, 1, z) in (None, AIR):
                b.set(x, 1, z, S("glass_pane", north="false", south="false", east="true", west="true", waterlogged="false"))
        b.set(x0 + w // 2, 1, z0, S("pipe", north="true", south="false", east="false", west="false", up="false", down="false"))
        skeleton(b, x0 + w // 2 + 1, 1, z0 + 1, pose="skeleton_suited", facing="north")
        b.set(x0 + w // 2 - 2, 1, z0 + 2, S("specimen_tank", facing="north"))
