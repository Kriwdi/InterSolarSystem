# InterSolarSystem

Post-apocalyptic space survival mod for Minecraft 26.3 (Fabric). Salvage a dead Earth, keep a ship alive, and fly towards Proxima.

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

The ship save (`ship_state.dat`) is format version 4; older saves are migrated when loaded (`ShipStateMigrations`).

## Flight

From inside the ship (OP): `/iss ship fly <x> <z>` flies the ship to an Overworld coordinate. Phases: takeoff countdown (10 s), flight (60 s), landing (5 s), shown on the action bar. The ship's hatch is its door: it disappears at liftoff and a new one is placed on the surface near the target when the ship lands. While the ship is not landed nobody can board or leave, and a hatch that is not the ship's own does not connect (it only re-docks the ship if the ship's own hatch was destroyed). If no dry landing spot is found within 32 blocks, the ship lands near the world spawn. Flight state is saved, so a flight resumes after a restart.

## Wear, incidents and repair

Every flight wears the subsystems out (about -8 on the engine per flight). Below 20% the engine or the landing/launch unit blocks takeoff. During flight, every 10 s a roll may cause an **incident** on a damaged subsystem (more likely the worse it is; nothing happens at 70% and above). An incident has a 60 s deadline: fix it with a **Repair Kit** at the **Repair Station** inside the ship (right-click the station with the kit), or the subsystem loses 25%. Without an incident, a kit restores the worst subsystem by 25%. The rolls come from a seed saved at takeoff, so reloading does not change the outcome. Get kits from the creative inventory (Ingredients) or `/give @s intersolarsystem:repair_kit`. `/iss ship info` shows active incidents.

## Ship HUD

Inside the ship a panel in the top-left corner shows the flight phase and countdown, the condition of the eight subsystems (green at 70% and above, amber 20-69%, red below 20%, with the percentage) and the active incidents with their time left (they blink in the last 15 seconds). The server sends the status once per second, immediately when something changes, and as soon as a player enters the ship; the client only draws it. The old action bar countdown is gone.

## Layout

- `src/main` – common and server code, `hu.kriwdi.intersolarsystem.{core,ship,travel,space,worldgen,survival,content}`
- `src/client` – client-only code and datagen
- `docs/superpowers` – design specs and implementation plans
