"""The worlds of the outer system as data, on top of make_planets.py's builder: Ganymede (grooved terrain, polar
frost), Callisto (craters on craters, sublimation spires, the rings of Valhalla), Saturn (its decks and the polar
hexagon), Saturn's rings (a weightless sheet of ice), Titan (orange haze, seas of liquid methane, tholin dunes),
Enceladus (tiger stripes with geysers, a living sea under the ice), Uranus (cyan decks, diamond hail, a sun that
never moves), Miranda (the Verona Rupes cliff, patchwork coronae), Titania (the Messina rifts), Neptune (blue decks,
the Great Dark Spot), Triton (cantaloupe terrain, nitrogen geysers) and the moon nobody names.

Sizes follow the real ones at the Moon's scale (5500 blocks for 3474 km), at least 2000 blocks; the world borders,
gravity and landing sites are in space/Worlds.java.

    python3 tools/worldgen/make_outer_planets.py
"""
import json

from make_planets import DATA, NS, b, hills, world, write

# Landform features (worldgen/MoonTerrainFeature.java): world -> [(name, config)]
TERRAIN = {
    "ganymede": [("ganymede_grooves", dict(style="grooves", primary="ganymede_dark_ice", secondary="grooved_ice",
                                           accent="ganymede_frost"))],
    "callisto": [("callisto_pinnacles", dict(style="pinnacles", primary="callisto_regolith", secondary="callisto_ice",
                                             accent="pinnacle_frost")),
                 ("valhalla_rings", dict(style="basin_rings", primary="callisto_regolith", secondary="callisto_ice",
                                         accent="pinnacle_frost", centre_x=-1600, centre_z=300, size=1100))],
    "titan": [("titan_dunes", dict(style="dunes", primary="titan_bedrock_ice", secondary="tholin_sand",
                                   accent="hydrocarbon_sludge", height=72))],
    "enceladus": [("tiger_stripes", dict(style="tiger_stripes", primary="enceladus_ice", secondary="enceladus_snow",
                                         accent="fissure_ice", special="geyser_vent", centre_x=0, centre_z=-180,
                                         size=520))],
    "miranda": [("miranda_coronae", dict(style="coronae", primary="miranda_ice", secondary="corona_ice",
                                         accent="scarp_ice")),
                ("verona_rupes", dict(style="escarpment", primary="miranda_ice", secondary="miranda_ice",
                                      accent="scarp_ice", centre_x=100, size=500, height=140))],
    "titania": [("messina_chasmata", dict(style="canyons", primary="titania_ice", secondary="titania_frost",
                                          accent="titania_ice", centre_z=80, height=30))],
    "triton": [("triton_cantaloupe", dict(style="cantaloupe", primary="cantaloupe_ice", secondary="nitrogen_ice",
                                          accent="geyser_soot", special="nitrogen_geyser"))],
}

GAS = {
    "saturn": dict(high="saturn_haze", middle="saturn_belt_cloud", low="water_cloud", storm_block="hexagon_storm_cloud",
                   underside="metallic_hydrogen", underside_rarity=520, storm_x=0, storm_z=-2500, storm_rx=600,
                   storm_rz=600, hexagon=True, bands=120.0),
    "uranus": dict(high="uranus_methane_cloud", middle="deep_sulfide_cloud", low="water_cloud",
                   storm_block="uranus_methane_cloud", underside="diamond_hail", underside_rarity=300, storm_x=9000,
                   storm_z=9000, storm_rx=1, storm_rz=1, bands=70.0),
    "neptune": dict(high="neptune_methane_cloud", middle="deep_sulfide_cloud", low="water_cloud",
                    storm_block="dark_spot_cloud", underside="diamond_hail", underside_rarity=300, storm_x=1800,
                    storm_z=1200, storm_rx=420, storm_rz=220, bands=150.0),
}

BLACK = dict(sky="#000000", fog="#000000", light="#0a0a0c", ambient=0.03, timelines="#intersolarsystem:on_moon")

WORLDS = {
    "ganymede": dict(BLACK, terrain=hills(80, 0.4, 0.5), block="ganymede_dark_ice",
                     surface=dict(base="ganymede_dark_ice", top="ganymede_dark_ice", alt="grooved_ice", alt_chance=0.03,
                                  under="ganymede_dark_ice", depth=3, ore="europa_salt", ore_chance=60,
                                  cap="ganymede_frost", cap_from=2800),
                     craters=[("ganymede_crater_large", 7, 9, 7), ("ganymede_crater_small", 3, 6, 3)]),
    "callisto": dict(BLACK, terrain=hills(78, 0.3), block="callisto_ice",
                     surface=dict(base="callisto_ice", top="callisto_regolith", alt="callisto_ice", alt_chance=0.05,
                                  under="callisto_regolith", depth=3, ore="nickel_iron_ore", ore_chance=80),
                     craters=[("callisto_crater_large", 6, 9, 3), ("callisto_crater_small", 3, 5, 1)]),
    "saturn": dict(sky="#d8c48e", fog="#e8d6a8", light="#1a1610", ambient=0.06,
                   timelines="#intersolarsystem:on_jupiter", terrain=-1.0, block="saturn_haze",
                   particle=("minecraft:white_ash", 0.004), height=320),
    "saturn_rings": dict(sky="#000000", fog="#000000", light="#0a0a0c", ambient=0.05,
                         timelines="#intersolarsystem:in_orbit", terrain=-1.0, block="ring_ice"),
    "titan": dict(sky="#b88a3c", fog="#a8762c", light="#2a1a08", ambient=0.07, timelines="#intersolarsystem:on_moon",
                  terrain=hills(80, 0.45, 0.9), block="titan_bedrock_ice", sea=72,
                  surface=dict(base="titan_bedrock_ice", top="titan_bedrock_ice", alt="hydrocarbon_sludge",
                               alt_chance=0.05, under="titan_bedrock_ice", depth=3, ore="cryolava_ice", ore_chance=90),
                  craters=[], particle=("minecraft:falling_water", 0.012), water="#4a3416"),
    "enceladus": dict(BLACK, terrain=hills(84, 0.15), block="enceladus_ice",
                      surface=dict(base="enceladus_ice", top="enceladus_snow", alt="enceladus_ice", alt_chance=0.08,
                                   under="enceladus_snow", depth=3),
                      craters=[("enceladus_crater", 3, 6, 6)], particle=("minecraft:snowflake", 0.004)),
    "uranus": dict(sky="#8fd8dc", fog="#b8eef0", light="#10181a", ambient=0.06, timelines="#intersolarsystem:on_venus",
                   terrain=-1.0, block="uranus_methane_cloud", particle=("minecraft:white_ash", 0.006), height=320),
    "miranda": dict(BLACK, terrain=hills(80, 0.35), block="miranda_ice",
                    surface=dict(base="miranda_ice", top="miranda_ice", alt="corona_ice", alt_chance=0.05,
                                 under="miranda_ice", depth=3),
                    craters=[("miranda_crater", 4, 7, 5)]),
    "titania": dict(BLACK, terrain=hills(80, 0.35, 0.5), block="titania_ice",
                    surface=dict(base="titania_ice", top="titania_ice", alt="titania_frost", alt_chance=0.06,
                                 under="titania_ice", depth=3),
                    craters=[("titania_crater_large", 6, 9, 6), ("titania_crater_small", 3, 5, 2)]),
    "neptune": dict(sky="#3a5ac0", fog="#5a7ad8", light="#0a1020", ambient=0.05,
                    timelines="#intersolarsystem:on_jupiter", terrain=-1.0, block="neptune_methane_cloud",
                    particle=("minecraft:snowflake", 0.004), height=320),
    "triton": dict(BLACK, terrain=hills(82, 0.2), block="cantaloupe_ice",
                   surface=dict(base="cantaloupe_ice", top="cantaloupe_ice", alt="nitrogen_ice", alt_chance=0.08,
                                under="cantaloupe_ice", depth=3, cap="nitrogen_ice", cap_from=1500),
                   craters=[]),
    "nameless": dict(BLACK, terrain=hills(78, 0.7, 0.6), block="nameless_rock",
                   surface=dict(base="nameless_rock", top="nameless_rock", alt="callisto_regolith", alt_chance=0.1,
                                under="nameless_rock", depth=2),
                   craters=[("nameless_crater", 4, 8, 4)]),
}


def feature(name, cfg):
    out = {"type": NS + cfg.pop("type")}
    for k, v in cfg.items():
        out[k] = b(v) if isinstance(v, str) and k not in ("style",) else v
    write(f"intersolarsystem/worldgen/feature/{name}.json", out)
    write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": NS + name, "placement": []})


def add_to_biome(wid, step, names):
    path = DATA / f"intersolarsystem/worldgen/biome/{wid}_surface.json"
    biome = json.loads(path.read_text())
    for n in names:
        if NS + n not in biome["features"][step]:
            biome["features"][step].append(NS + n)
    path.write_text(json.dumps(biome, indent=2) + "\n")
    return biome


def main():
    for wid, w in WORLDS.items():
        world(wid, w)
        if "sea" in w:
            path = DATA / f"intersolarsystem/worldgen/noise_settings/{wid}.json"
            n = json.loads(path.read_text())
            n["default_fluid"] = "minecraft:water"
            n["sea_level"] = w["sea"]
            path.write_text(json.dumps(n, indent=2) + "\n")
        if "water" in w:
            path = DATA / f"intersolarsystem/worldgen/biome/{wid}_surface.json"
            biome = json.loads(path.read_text())
            biome["effects"]["water_color"] = w["water"]
            path.write_text(json.dumps(biome, indent=2) + "\n")
    for wid, cfg in GAS.items():
        feature(wid + "_decks", dict(cfg, type="gas_decks"))
        add_to_biome(wid, 0, [wid + "_decks"])
    feature("ring_field", dict(type="ring_field", ice="ring_ice", dusty="dusty_ring_ice", heavy="deuterium_ice",
                               plane=120, half_thickness=14))
    add_to_biome("saturn_rings", 0, ["ring_field"])
    for wid, features in TERRAIN.items():
        for (name, cfg) in features:
            feature(name, dict(cfg, type="moon_terrain"))
        add_to_biome(wid, 2, [n for (n, _c) in features])
    feature("enceladus_ocean", dict(type="subsurface_ocean", floor="minecraft:tuff", life="microbial_mat",
                                    colony="vent_colony", vent="minecraft:magma_block", floor_y=12, shell=26))
    add_to_biome("enceladus", 1, ["enceladus_ocean"])
    print("outer worlds written:", ", ".join(WORLDS))


if __name__ == "__main__":
    main()
