"""The worlds beyond Mars, as data: dimension types, dimensions, terrain (noise settings), biomes, surface dressing and
craters for Venus, Mercury, Ceres, the asteroid belt (a void with drifting rocks), Jupiter's cloud decks (a void with
the decks), Io and Europa (with its ocean under the ice). Sizes follow the real ones at the Moon's scale (5500 blocks
for 3474 km); the world borders are set in Java (space/Worlds.java).

    python3 tools/worldgen/make_planets.py
"""
import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data"
NS = "intersolarsystem:"
NO_SPAWNS = {"argument": {"spawn_costs": {}, "spawns_by_category": {
    c: [] for c in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature",
                    "water_ambient", "water_creature")}}, "modifier": "overlay"}


def write(rel, data):
    path = DATA / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def b(name):
    return name if ":" in name else NS + name


MOON_TYPE = json.loads((DATA / "intersolarsystem/dimension_type/moon.json").read_text())
MOON_NOISE = json.loads((DATA / "intersolarsystem/worldgen/noise_settings/moon.json").read_text())


def hills(base_y, amplitude, broad=0.0):
    """Rolling terrain around base_y: a height gradient plus blended noise (and a slow broad noise)."""
    right = {"type": "minecraft:mul", "left": {"type": "minecraft:old_blended_noise", "smear_scale_multiplier": 8.0,
                                               "xz_factor": 120.0, "xz_scale": 0.25, "y_factor": 80.0,
                                               "y_scale": 0.125}, "right": amplitude}
    if broad:
        right = {"type": "minecraft:add", "left": right, "right": {"type": "minecraft:mul", "left": {
            "type": "minecraft:noise", "noise": "minecraft:erosion", "xz_scale": 0.12, "y_scale": 0.0},
            "right": broad}}
    return {"type": "minecraft:squeeze", "input": {"type": "minecraft:interpolated", "cell_size_xz": 8,
                                                    "cell_size_y": 4, "input": {
        "type": "minecraft:add", "left": {"type": "minecraft:gradient", "axis": "y", "from_coordinate": base_y - 40,
                                          "from_value": 1.2, "to_coordinate": base_y + 40, "to_value": -1.2},
        "right": right}}}


# id: dict(type colours, timelines, terrain, default block, biomes {name: (surface feature, craters, extras)})
WORLDS = {
    "venus": dict(sky="#c88a46", fog="#a8662e", light="#2a1a0c", ambient=0.08, timelines="#intersolarsystem:on_venus",
                  terrain=hills(78, 0.55, 0.8), block="venus_basalt",
                  surface=dict(base="venus_basalt", top="venus_regolith", alt="sulfur_crust", alt_chance=0.06,
                               under="venus_regolith", depth=3, ore="sulfur_crust", ore_chance=40),
                  craters=[("venus_crater", 6, 9, 14)], particle=("minecraft:white_ash", 0.01), lava=True),
    "mercury": dict(sky="#000000", fog="#000000", light="#0a0a0a", ambient=0.02, timelines="#intersolarsystem:on_moon",
                    terrain=hills(80, 0.5), block="mercury_rock",
                    surface=dict(base="mercury_rock", top="mercury_regolith", alt="hollow_crust", alt_chance=0.03,
                                 under="mercury_regolith", depth=3, ore="minecraft:iron_ore", ore_chance=60,
                                 cap="ceres_ice", cap_from=3600),
                    craters=[("mercury_crater_large", 7, 9, 6), ("mercury_crater_small", 3, 6, 2)]),
    "ceres": dict(sky="#000000", fog="#000000", light="#0a0a0a", ambient=0.02, timelines="#intersolarsystem:on_moon",
                  terrain=hills(76, 0.35), block="ceres_clay",
                  surface=dict(base="ceres_clay", top="ceres_clay", alt="carbonate_salt", alt_chance=0.02,
                               under="ceres_clay", depth=4, ore="ceres_ice", ore_chance=8),
                  craters=[("ceres_crater_large", 7, 9, 5), ("ceres_crater_small", 3, 6, 2)]),
    "asteroid_belt": dict(sky="#000000", fog="#000000", light="#0a0a0a", ambient=0.04,
                          timelines="#intersolarsystem:in_orbit", terrain=-1.0, block="chondrite",
                          special="asteroid_field"),
    "jupiter": dict(sky="#7a92c0", fog="#d8c49a", light="#1a1610", ambient=0.06, timelines="#intersolarsystem:on_jupiter",
                    terrain=-1.0, block="water_cloud", special="cloud_decks", particle=("minecraft:white_ash", 0.006),
                    height=320),
    "io": dict(sky="#000000", fog="#000000", light="#0a0806", ambient=0.03, timelines="#intersolarsystem:on_moon",
               terrain=hills(80, 0.55, 0.6), block="io_silicate",
               surface=dict(base="io_silicate", top="sulfur_plain", alt="so2_frost", alt_chance=0.08,
                            under="sulfur_plain", depth=4, ore="sulfur_crust", ore_chance=20),
               craters=[], lava=True, particle=("minecraft:ash", 0.004)),
    "europa": dict(sky="#000000", fog="#000000", light="#0a0a0c", ambient=0.03, timelines="#intersolarsystem:on_moon",
                   terrain=hills(82, 0.18), block="europa_ice",
                   surface=dict(base="europa_ice", top="europa_ice", alt="lineae_ice", alt_chance=0.12,
                                under="europa_ice", depth=2, ore="europa_salt", ore_chance=50),
                   craters=[], special="europa_ocean"),
}


def timelines():
    write("intersolarsystem/tags/timeline/on_venus.json", {"values": ["#minecraft:universal"]})
    write("intersolarsystem/tags/timeline/on_jupiter.json", {"values": ["#minecraft:universal", "minecraft:day"]})


def world(wid, w):
    t = copy.deepcopy(MOON_TYPE)
    t["ambient_light"] = w["ambient"]
    t["attributes"]["minecraft:visual/sky_color"] = w["sky"]
    t["attributes"]["minecraft:visual/fog_color"] = w["fog"]
    t["attributes"]["minecraft:visual/ambient_light_color"] = w["light"]
    t["timelines"] = w["timelines"]
    if "height" in w:
        # Room above the decks for the aerostat and its envelope.
        t["height"] = w["height"]
        t["logical_height"] = w["height"]
    write(f"intersolarsystem/dimension_type/{wid}.json", t)
    n = copy.deepcopy(MOON_NOISE)
    n["default_block"] = b(w["block"])
    n["material_rule"] = NS + wid
    n["noise_router"]["final_density"] = w["terrain"]
    write(f"intersolarsystem/worldgen/noise_settings/{wid}.json", n)
    write(f"intersolarsystem/worldgen/material_rule/{wid}.json", {"type": "minecraft:block", "result_state": b(w["block"])})
    biome = wid + "_surface"
    write(f"intersolarsystem/dimension/{wid}.json", {"type": NS + wid, "generator": {
        "type": "minecraft:noise", "biome_source": {"type": "minecraft:fixed", "biome": NS + biome},
        "settings": NS + wid}})
    steps = [[] for _ in range(11)]
    if "surface" in w:
        name = wid + "_surface_dressing"
        cfg = {"type": NS + "planet_surface"}
        for k, v in w["surface"].items():
            cfg[k] = b(v) if isinstance(v, str) else v
        write(f"intersolarsystem/worldgen/feature/{name}.json", cfg)
        write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": NS + name, "placement": []})
        steps[0].append(NS + name)
    if "special" in w:
        name = w["special"]
        write(f"intersolarsystem/worldgen/feature/{name}.json", {"type": NS + name})
        write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": NS + name, "placement": []})
        steps[0 if name != "europa_ocean" else 1].append(NS + name)
    for (cname, lo, hi, rarity) in w.get("craters", []):
        s = w["surface"]
        write(f"intersolarsystem/worldgen/feature/{cname}.json", {
            "type": NS + "crater", "min_radius": lo, "max_radius": hi, "floor": b(s["top"]), "fresh": b(s["alt"]),
            "rim": b(s["base"])})
        write(f"intersolarsystem/worldgen/placed_feature/{cname}.json", {"feature": NS + cname, "placement": [
            {"type": "minecraft:rarity_filter", "chance": rarity}, {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})
        steps[3].append(NS + cname)
    if w.get("lava"):
        steps[1].append("minecraft:lake_lava_surface")
    steps[3] = sorted(steps[3])
    attrs = {"minecraft:gameplay/natural_mob_spawns": NO_SPAWNS, "minecraft:visual/sky_color": w["sky"],
             "minecraft:visual/fog_color": w["fog"]}
    if "particle" in w:
        attrs["minecraft:visual/ambient_particles"] = {"argument": [{"particle": {"type": w["particle"][0]},
                                                                     "probability": w["particle"][1]}],
                                                       "modifier": "append"}
    write(f"intersolarsystem/worldgen/biome/{biome}.json", {
        "attributes": attrs, "carvers": [], "downfall": 0.0, "effects": {"water_color": "#3f5a6a"},
        "features": steps, "has_precipitation": False, "temperature": 0.5})


if __name__ == "__main__":
    timelines()
    for wid, w in WORLDS.items():
        world(wid, w)
    print("planets written:", ", ".join(WORLDS))
