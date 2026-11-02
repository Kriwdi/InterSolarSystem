# InterSolarSystem

Post-apocalyptic space survival mod for Minecraft 26.3 (Fabric). Salvage a dead Earth, keep a ship alive, and fly towards Proxima.

## Download (dev builds)

Every push is built by GitHub Actions. The latest jar is on the [`ci-builds` branch](../../tree/ci-builds/jar) (with `build.log` and `BUILD_INFO.txt`), and also as an artifact on each run under the **Actions** tab. Put the jar in `.minecraft/mods` next to Fabric API (Minecraft 26.3, Fabric Loader 0.20.1+, Java 25).

## Project layout

| Path | Contents |
|---|---|
| `src/main/java` | Common/server code (`core`, `ship`, `travel`, `space`, `worldgen`, `survival`, `content`) |
| `src/main/resources` | `fabric.mod.json`, hand-made assets (models, textures, blockstates) |
| `src/main/generated` | Datagen output (lang files, loot tables) – committed |
| `src/client/java` | Client-only code (HUD, client state) and datagen providers |
| `src/test/java` | Unit tests |
| `docs/` | Project brief, roadmap, status reports, phase specs and plans |
| `.github/workflows` | CI build |

## Requirements

- JDK 25
- Fabric toolchain via the included Gradle wrapper (no separate Gradle install needed)

## Commands

| Command | What it does |
|---|---|
| `./gradlew runClient` | Starts Minecraft with the mod (dev client) |
| `./gradlew runServer` | Starts a dedicated dev server (first run: accept the EULA in `run/eula.txt`) |
| `./gradlew runDatagen` | Regenerates lang files and loot tables into `src/main/generated` |
| `./gradlew test` | Runs the unit tests |
| `./gradlew build` | Compiles, tests and builds the jar |

Generated files in `src/main/generated` are committed; run `runDatagen` after changing a provider and commit the result.

## Ship state (debug commands)

The world has one persistent ship (`ShipState`, saved in the world as `data/intersolarsystem/ship_state.dat`).

| Command | Who | What it does |
|---|---|---|
| `/iss ship info` | everyone | Shows the hull class and the condition (0-100%) of the eight subsystems |
| `/iss ship set <subsystem> <value>` | OP (level 2) | Sets a subsystem condition; `set` is a development tool and may become debug-only in phase 2 |

Subsystems: `engine`, `fuel_energy`, `hull_armor`, `life_support`, `navigation_comms`, `stabilizer`, `landing_launch`, `cargo`.

## Ship hatch and interior

Place a `ship_hatch` block (creative: Building Blocks tab) and right-click it to board the ship. You arrive in the sealed capsule room in the `intersolarsystem:ship_interior` dimension. Right-clicking the hatch in the wall leaves the ship again, next to the hatch you boarded through. If that hatch is gone, you are put at the world spawn. The hatch cannot be broken inside the ship.

The ship save (`ship_state.dat`) is format version 6; older saves are migrated when loaded (`ShipStateMigrations`).

## Flight

From inside the ship (OP): `/iss ship fly <x> <z>` flies the ship to an Overworld coordinate. Phases: takeoff countdown (10 s), flight (60 s), landing (5 s), shown on the action bar. The ship's hatch is its door: it disappears at liftoff and a new one is placed on the surface near the target when the ship lands. While the ship is not landed nobody can board or leave, and a hatch that is not the ship's own does not connect (it only re-docks the ship if the ship's own hatch was destroyed). If no dry landing spot is found within 32 blocks, the ship lands near the world spawn. Flight state is saved, so a flight resumes after a restart.

## Wear, incidents and repair

Every flight wears the subsystems out (about -8 on the engine per flight). Below 20% the engine or the landing/launch unit blocks takeoff. During flight, every 10 s a roll may cause an **incident** on a damaged subsystem (more likely the worse it is; nothing happens at 70% and above). An incident has a 60 s deadline: fix it with a **Repair Kit** at the **Repair Station** inside the ship (right-click the station with the kit), or the subsystem loses 25%. Without an incident, a kit restores the worst subsystem by 25%. The rolls come from a seed saved at takeoff, so reloading does not change the outcome. `/iss ship info` shows active incidents.

## Salvage and crafting

The technology is salvaged, not invented. Chests in abandoned mineshafts, dungeons, shipwrecks (supply), desert and jungle temples, ruined portals, stronghold corridors, ancient cities, pillager outposts, igloos and village toolsmiths carry **Scrap Metal**, **Salvaged Wiring**, **Salvaged Circuits** and occasionally a ready Repair Kit. (These move into the dead Earth's own ruins in phase 7.)

| Recipe | Ingredients |
|---|---|
| Repair Kit | 2 scrap metal, 1 salvaged wiring, 1 iron ingot, 1 salvaged circuit |
| Diagnostic Console | 5 scrap metal, 1 glass pane, 2 salvaged circuits, 1 redstone |
| Repair Station | 5 scrap metal, 1 iron ingot, 2 salvaged wiring, 1 salvaged circuit |
| Rusted Panel (x2) | 4 scrap metal; a panel breaks back down into 2 scrap metal |
| Multitool | 2 iron ingots, 1 salvaged circuit, 1 salvaged wiring, 3 scrap metal |
| Navigation Console | 2 glass panes, 1 compass, 2 salvaged circuits, 1 salvaged wiring, 3 scrap metal |

## Hidden faults, diagnostics and test hops

A worn subsystem (below 70%) can pick up a **hidden fault** at every landing (up to 50% chance at 0%). Hidden faults show nowhere, but they add to the **takeoff risk**: a fault in the engine, fuel/energy or landing/launch unit, or one of those below 40%, can make a takeoff fail. A failed takeoff is an explosion at the launch site (it hurts creatures but breaks no blocks), the ship stays where it is and takes heavy damage (engine and landing unit -30, hull -20, everything else -10).

- **Diagnostic Console** (inside the ship): right-click to scan. Each hidden fault is found with 50% + half the navigation/comms condition. The scan lists the known faults and estimates the takeoff risk, but only from what it found. One scan per minute.
- **Test hop**: sneak + right-click the console twice within 5 seconds. The ship lifts off and lands back on the spot (10 s + 15 s + 5 s). Every hidden fault breaks out as an incident (as many as the incident limit allows), the takeoff risk is halved and the ship wears half as much.
- Known faults are marked with a red warning sign on the HUD. A repair kit at the station fixes, in this order: the most urgent incident, a known hidden fault, the worst subsystem.

## Repair minigames

Using a repair kit on an incident opens a minigame; the server checks the answer, and only a correct one fixes the incident and uses up the kit. A failed repair costs the subsystem 5% and the incident stays. The game does not pause, and the kit must stay in the main hand. The worse the damaged subsystem, the bigger the board.

| Incident | Minigame |
|---|---|
| Short circuit | **Wiring**: join each coloured terminal on the left to the same colour on the right |
| Overheating | **Valve**: close the valve (button or space) while the sweeping needle is in the green band, once per round |
| Leak, pressure loss | **Pressure**: move units through the valves until every tank sits on the yellow line, within a move limit |
| Fire | none: the kit puts it out |

**Multitool** (craftable, 48 uses): held in the off hand during a repair, a light fault (subsystem at 40% or above) is fixed without a minigame, and harder ones get a smaller board.

## Navigation and travel

The **Navigation Console** (inside the ship; craftable: glass panes, a compass, salvaged circuits, wiring and scrap) opens the star chart: the Solar System as an orrery strip, every destination with its travel time for this ship's engine, the current launch window and a launch button.

- **Time:** one real day of travel is 2400 ticks (the agreed 10x speed-up). The engine tier divides it (x1, x3, x10, x30; `/iss ship engine <1-4>` for testing). Outside the launch window the trip takes up to 50% longer; the console counts down to the next best window. With the first engine the Moon is ~6 minutes away, Mars ~7 hours.
- **Destinations now:** surface flights on Earth (`/iss ship fly <x> <z>`) and a **free-return loop around the Moon** (out, around and back home, ~12 minutes with the first engine). The other bodies are listed with their times but locked until their worlds exist.
- **Travel events** (flights over two minutes, rolled once a minute from the flight's seed): **meteor shower** (right-click the navigation console within 30 s to steer clear, costs a little fuel; otherwise the hull and sensors take hits), **solar flare** (electronics and life support damaged), **system failure** (an incident on the worst subsystem), **drifting wreck** (salvage drifts in through the airlock), **strange signals** (fragments of what happened to the other expeditions).
- Time follows the world's game time: if the server pauses, so does the journey.

The ship save is format version 6 (adds the engine tier and the flight plan).

## Ship HUD

Inside the ship a panel in the top-left corner shows the flight phase and countdown, the condition of the eight subsystems (green at 70% and above, amber 20-69%, red below 20%, with the percentage) and the active incidents with their time left (they blink in the last 15 seconds). The server sends the status once per second, immediately when something changes, and as soon as a player enters the ship; the client only draws it. The old action bar countdown is gone.

## Development notes

- **Art standard:** every block, item, structure and mob is detailed and shaped where it can be: models built from elements instead of plain cubes, facing directions, random texture variants, animated textures, shading and outlines in the pixel art. Textures come from `tools/art/make_textures.py`, block models and blockstates from `tools/art/make_models.py`, and `tools/art/render_model.py <model> <out.png> [facing]` renders an isometric preview without the game. `AssetsTest` validates all models, blockstates, item definitions and textures.
- **Translations** live in one table, `ModLangTable` (all three languages per key). After editing it run `python3 tools/sync_lang.py` (or `./gradlew runDatagen`); `LangTableTest` fails if the committed lang files are out of date.
- **Recipes** are plain data files in `src/main/resources/data/intersolarsystem/recipe`.
- **CI** (`.github/workflows/build.yml`) builds, runs the unit tests and a dedicated-server smoke test that loads all recipes, loot and models and fails on any error from the mod.
- **API probe** (`.github/workflows/api-probe.yml`, run manually): prints the real 26.3 signatures of the classes listed in `tools/api-probe.txt` to the `ci-probe` branch, so code is written against the actual API.
