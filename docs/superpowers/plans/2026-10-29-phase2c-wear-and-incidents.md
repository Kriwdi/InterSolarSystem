# Phase 2c Wear and Incidents Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A repülés elhasználja az alrendszereket, rossz állapotnál mentett, determinisztikus incidensek keletkeznek, amiket egy javítókészlet old meg; a leromlott hajó nem száll fel.

**Architecture:** A tiszta logika (kopás-táblázat, felszállási kapu, esély, dobás, javítás-választás) Minecraft nélkül tesztelhető. Az `ShipState` v4 egy beágyazott `hazards` mezőt kap (mag, dobásszámláló, aktív incidensek). A szerveroldali réteg (`ShipFlightController`, `RepairStationBlock`, `ShipRepair`) köti a világhoz.

**Tech Stack:** Java 25, Minecraft 26.3, Fabric API, DFU, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-29-phase2c-wear-and-incidents-design.md`

## Global Constraints

- Minecraft 26.3; mod ID `intersolarsystem`; csomag `hu.kriwdi.intersolarsystem.ship` (tárgy/blokk: `...content`).
- Kopás-táblázat, küszöb (`20`), dobási intervallum (`200` tick), határidő (`1200` tick), lejárati sérülés (`25`), javítás (`+25`), legfeljebb `2` aktív incidens: a specben rögzített értékek, egy helyen konstansként.
- Mentésformátum: `CURRENT_FORMAT_VERSION = 4` a 2. feladattól; az új mezők megengedők (lenient), hibás érték nem dobja el a hajót; az újabb formátum csak olvasható.
- Az állapot szerveroldali; a kliens nem dönt.
- Szöveg en_us/de_de/hu_hu, datagenből; a lang tesztek zöldek maradnak.
- **A játék futása közben tilos a Gradle build** (összeomlasztja a futó klienst). Build előtt `pgrep -f '[K]notClient'`, és ha fut, kérdezd meg a felhasználót.
- API-pontosság: a jarból **ellenőrzött** szignatúrák a tervben vannak; eltérésnél **állj meg és kérdezz**.
- Commit üzenet vége: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Ág: `phase2c-wear`.

## Spec-eltérés (indokolt)

A spec a dobást "az alrendszerek kumulatív esélye, egyetlen szám dönt" alakban írta. Ha több alrendszer rossz, az esélyek összege 1 fölé megy, és az enum későbbi tagjai sosem kerülnének sorra. A terv ezért két lépéses dobást használ: **előfordul-e** incidens (a teljes esély `min(0,25, Σ alrendszer-esély)`), majd az érintett alrendszer **az esélyükkel súlyozva** kerül kiválasztásra. A specben rögzített viselkedés megmarad (70% fölött 0, egy rossz alrendszernél 0%-on 0,25 az esély, a rosszabb alrendszer valószínűbb, dobásonként legfeljebb egy incidens). Az eltérést jelezni kell a felhasználónak.

## Review Focus

- Újratöltés repülés közben nem dobja újra a kimenetet: a `rolls_done` és a determinisztikus dobás védi. Teszt: Task 2.
- Sok rossz alrendszernél a későbbi alrendszerek nem éheznek ki, és az összesített esély nem nő 25% fölé. Teszt: Task 2.
- A mentett incidens határideje a jövőben (sérült mentés): a határidő `most + 1200`-ra szűkül, nem fagyaszt. Teszt: Task 2.
- Az állomás csak levegőt cserél, a játékos által odatett blokkot nem írja felül. Kézi ellenőrzés: Task 3, tiszta rész tesztelve.
- A készlet nem fogy, ha nincs mit javítani; kreatívban sosem fogy. Teszt: Task 3 (döntés), kézi: Task 4.

---

### Task 1: Kopás és felszállási kapu

**Files:**
- Create: `ShipWear.java`, `ShipFlightRules.java` (`src/main/java/hu/kriwdi/intersolarsystem/ship/`)
- Modify: `ShipState.java` (`applyWear`), `ShipFlightController.java`, `ShipCommands.java`
- Modify: `LangParityTest.java`, `ModLangProvider.java`; regenerate lang
- Test: `ShipWearTest.java`, `ShipFlightRulesTest.java`, a `ShipStateTest.java` bővítése

**Interfaces:**
- Produces:
  - `ShipWear.forTransition(ShipFlightPhase to) -> Map<ShipSubsystem, Integer>` (pozitív csökkenések; `TAKEOFF` üres): `FLIGHT`: `ENGINE 4, LANDING_LAUNCH 4, FUEL_ENERGY 3`; `LANDING`: `ENGINE 4, FUEL_ENERGY 6, STABILIZER 3, HULL_ARMOR 2, NAVIGATION_COMMS 2, LIFE_SUPPORT 2`; `LANDED`: `LANDING_LAUNCH 5, STABILIZER 3, HULL_ARMOR 2`.
  - `ShipFlightRules.MIN_TAKEOFF_CONDITION = 20` és `static boolean canTakeOff(int engine, int landingLaunch)` (mindkettő ≥ 20).
  - `ShipState.applyWear(Map<ShipSubsystem, Integer> amounts)`: alrendszerenként `setCondition(s, condition(s) - amount)`; csak olvasható állapotban nem tesz semmit.
- Lang kulcs: `command.intersolarsystem.ship.fly_too_damaged` = `The engine or the landing gear is too damaged to take off (below 20%)`.

- [ ] **Step 1: Failing tests.**
  - `ShipWearTest`: `flightTransitionTable`; `landingTransitionTable`; `landedTransitionTable`; `takeoffTransitionIsEmpty`; `engineLosesEightPerFlight` (FLIGHT + LANDING összege = 8).
  - `ShipFlightRulesTest`: `exactlyTwentyCanTakeOff`; `nineteenEngineCannot`; `nineteenLandingLaunchCannot`; `bothHealthyCanTakeOff`.
  - `ShipStateTest`: `applyWearReducesAndClamps` (50 → 46 és 3 − 5 → 0); `applyWearOnReadOnlyDoesNothing` (nem dirty).
  - `LangParityTest.requiredKeys()` kibővül a `fly_too_damaged` kulccsal.
- [ ] **Step 2: Run** `./gradlew test --console=plain` (a build előtt a játék futását ellenőrizd). Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció.** A `ShipFlightController.transition` a `FLIGHT`, `LANDING`, `LANDED` ágban a kritikus változás után, az `advancePhase` **előtt** hívja `state.applyWear(ShipWear.forTransition(to))`-t. A `fly` parancs ellenőrzési sorrendje: belül van, nem csak olvasható, határon belül, `phase != LANDED` → `fly_not_landed`, majd `!canTakeOff(...)` → `fly_too_damaged`, végül `beginTakeoff`. A `ModLangProvider` az új szöveget mindhárom nyelven.
- [ ] **Step 4: Datagen és teszt.** `./gradlew runDatagen --console=plain && ./gradlew test --console=plain`. Expected: PASS.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add flight wear and takeoff gate"`

### Task 2: Incidensek (v4 mentés, dobás, határidő, megjelenítés)

**Files:**
- Create: `ShipIncidentType.java`, `ShipIncident.java`, `ShipHazards.java`, `ShipIncidentRules.java`
- Modify: `ShipState.java`, `ShipStateMigrations.java`, `ShipFlightController.java`, `ShipCommands.java`
- Modify: `LangParityTest.java`, `ModLangProvider.java`; regenerate lang
- Test: `ShipIncidentRulesTest.java`, `ShipHazardsTest.java`, a `ShipStateTest.java` és `ShipStateMigrationsTest.java` bővítése

**Interfaces:**
- Consumes: `ShipSubsystem`, `ShipFlightPhase`, `ShipState` (Task 1).
- Produces:
  - `enum ShipIncidentType { LEAK, OVERHEAT, SHORT_CIRCUIT, FIRE, PRESSURE_LOSS; String id(); static Optional<ShipIncidentType> fromId(String) }`.
  - `record ShipIncident(ShipSubsystem subsystem, ShipIncidentType type, long deadlineTick)`.
  - `record ShipHazards(long seed, int rollsDone, List<ShipIncident> incidents)`; `static final ShipHazards NONE`; `static final Codec<ShipHazards> CODEC` (mezők: `seed`, `rolls_done`, `incidents`; az incidens `subsystem`/`type` azonosító, `deadline_tick`; ismeretlen azonosítójú elem kimarad + WARN).
  - `ShipIncidentRules`: konstansok `FLIGHT_ROLL_INTERVAL_TICKS = 200`, `ROLLS_PER_FLIGHT = 5`, `INCIDENT_DEADLINE_TICKS = 1200`, `EXPIRY_DAMAGE = 25`, `MAX_ACTIVE = 2`, `SAFE_CONDITION = 70`, `MAX_CHANCE = 0.25`; `static double failureChance(int condition)` (≥ 70 → 0; különben `0,25 × (70 − szűkített állapot) / 70`); `static ShipIncidentType typeFor(ShipSubsystem)`: `ENGINE→OVERHEAT`, `FUEL_ENERGY→LEAK`, `HULL_ARMOR→PRESSURE_LOSS`, `LIFE_SUPPORT→PRESSURE_LOSS`, `NAVIGATION_COMMS→SHORT_CIRCUIT`, `STABILIZER→SHORT_CIRCUIT`, `LANDING_LAUNCH→FIRE`, `CARGO→FIRE`; `static int rollsDue(long elapsedTicks)` (`min(ROLLS_PER_FLIGHT, elapsed / 200)`, negatív → 0); `static Optional<ShipSubsystem> roll(long seed, int rollIndex, Map<ShipSubsystem, Integer> conditions, Set<ShipSubsystem> activeSubsystems)`: ha `activeSubsystems.size() ≥ MAX_ACTIVE` → üres; a nem aktív alrendszerek `failureChance`-ei összege `total`; `new SplittableRandom(seed + rollIndex * 0x9E3779B97F4A7C15L)` első `nextDouble()`-je `r1`: ha `r1 ≥ min(MAX_CHANCE, total)` → üres; különben a második `nextDouble() × total` az esélyekkel súlyozva (enum sorrendben) választ.
  - `ShipState` (CURRENT_FORMAT_VERSION = 4; `ShipStateMigrations.defaultSteps()` a `3 → 4` identitás-lépéssel): `ShipHazards hazards()`; mentési mező `hazards` (lenient, hibásnál `NONE` + WARN); `boolean beginTakeoff(int x, int z, long nowTick, long seed)` (a seed és `rolls_done = 0` beállítása, a többi a Task 2/2b szerinti); `void setRollsDone(int)`; `void addIncident(ShipIncident)`; `boolean resolveIncident(ShipIncident)`; `List<ShipIncident> expireIncidents(long nowTick)` (a `deadline ≤ now` incidensek kikerülnek, mindegyikre `setCondition(s, condition − EXPIRY_DAMAGE)`, a lejártak listája visszaadva); `boolean clampIncidentDeadlines(long nowTick)` (a `now + 1200` fölötti határidő `now + 1200`-ra, igaz, ha változott). Minden író csak módosításkor `setDirty()`, csak olvasható állapotban nem tesz semmit.
- Lang kulcsok (en szöveg; de/hu a providerben): `ship.intersolarsystem.incident.<id>` ötre: `Leak`, `Overheating`, `Short circuit`, `Fire`, `Pressure loss`; `message.intersolarsystem.ship.incident` = `ALERT: %s in %s! Fix it with a repair kit at the repair station`; `message.intersolarsystem.ship.incident_expired` = `The %s failure damaged %s`; `message.intersolarsystem.ship.alert_line` = `%s: %s (%s)`; `command.intersolarsystem.ship.incident_line` = `Incident: %s, %s (%s left)`.

- [ ] **Step 1: Failing tests.**
  - `ShipIncidentRulesTest`: `chanceZeroAtSafeAndAbove` (70, 100); `chanceMaxAtZero` (0 → 0,25); `chanceLinear` (35 → 0,125); `chanceClampsOutOfRange` (−10 → 0,25; 150 → 0); `typeForEverySubsystem` (a teljes hozzárendelés); `rollsDueCounts` (0→0, 199→0, 200→1, 1000→5, 5000→5, −5→0); `rollIsDeterministic` (azonos bemenetre azonos kimenet); `noRollWhenAllSafe`; `excludesActiveSubsystems`; `noRollAtMaxActive`; `totalChanceIsCapped` (mind a nyolc alrendszer 0%-on, 1000 különböző mag/sorszámra az incidensek aránya 0,25 ± 0,05); `laterSubsystemsAreChosenToo` (mind nyolc 0%-on, 2000 dobás után mind a nyolc legalább egyszer kiválasztva).
  - `ShipHazardsTest`: `roundTrip` (két incidens); `unknownSubsystemOrTypeDropped`; `emptyByDefault`.
  - `ShipStateTest`: `v3SaveLoadsWithNoHazards`; `hazardsRoundTrip`; `malformedHazardsAreDroppedNotFatal` (`"hazards":5` → a hajótest-osztály megmarad); `beginTakeoffStoresSeedAndResetsRolls`; `addAndResolveIncident`; `expireRemovesDueAndDamages` (határidő pont `now`-nál lejár, `now+1`-nél nem; −25 az alrendszerre); `clampIncidentDeadlines`; `incidentWritesRefusedWhenReadOnly`. A meglévő `beginTakeoff(x, z, tick)` hívások a négy paraméteres alakra cserélve.
  - `ShipStateMigrationsTest`: `v3BecomesV4`.
  - `LangParityTest.requiredKeys()` bővítése a fenti kulcsokkal (az `incident.<id>` kulcsok az enumból).
- [ ] **Step 2: Run** `./gradlew test --console=plain` (a játék futását ellenőrizd). Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció.** Tiszta részek először; majd a `ShipFlightController`: minden tickben, ha vannak incidensek, `clampIncidentDeadlines(now)` és `expireIncidents(now)` (fázistól függetlenül; lejáratkor üzenet a belső játékosoknak); `FLIGHT` fázisban `rollsDue(now − phaseStartedTick)` alapján a `rolls_done+1 .. rollsDue` sorszámú dobások lefutnak (`roll(...)`, siker esetén `addIncident(new ShipIncident(s, typeFor(s), now + 1200))`, üzenet és hang), majd `setRollsDone`. A `fly` parancs a seedet `server.overworld().getRandom().nextLong()`-ból veszi (a `getRandom()` jarból ellenőrizendő). Az action bar a fázis számlálóját és az incidenseket `" | "`-pel fűzi össze (`alert_line`); leszállt állapotban csak az incidensek. Az `info` kiegészül az `incident_line` sorokkal.
- [ ] **Step 4: Datagen és teszt.** `./gradlew runDatagen --console=plain && ./gradlew test --console=plain`. Expected: PASS.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add incidents with saved deterministic rolls (format v4)"`

### Task 3: Javítóállomás és javítókészlet

**Files:**
- Create: `ShipRepairRules.java`, `ShipRepair.java` (`ship/`); `RepairStationBlock.java` (`content/`)
- Modify: `ShipInteriorLayout.java`, `ShipInteriorBuilder.java`, `ShipBoarding.java`, `ModBlocks.java`, `ModItems.java`
- Create: erőforrások (`blockstates/repair_station.json`, `models/block/repair_station.json`, `items/repair_station.json`, `items/repair_kit.json`, `models/item/repair_kit.json`, textúrák)
- Modify: `ModLangProvider.java`, `ModBlockLootTableProvider.java`, `LangParityTest.java`; regenerate
- Test: `ShipRepairRulesTest.java`, a `ShipInteriorLayoutTest.java` módosítása

**Interfaces:**
- Consumes: `ShipIncident`, `ShipState` (Task 2), `ShipSubsystem`.
- Produces:
  - `ShipRepairRules`: `REPAIR_AMOUNT = 25`; `enum Kind { INCIDENT, SUBSYSTEM, NOTHING }`; `record Choice(Kind kind, Optional<ShipIncident> incident, Optional<ShipSubsystem> subsystem)`; `static Choice choose(List<ShipIncident> incidents, Map<ShipSubsystem, Integer> conditions)`: van incidens → a legkorábbi határidejű (`INCIDENT`); különben a 100 alatti legalacsonyabb állapotú alrendszer, azonos állapotnál az enum sorrend szerinti első (`SUBSYSTEM`); különben `NOTHING`.
  - `ShipRepair.useKit(MinecraftServer server, ServerPlayer player, ItemStack kit)`: a `choose` szerint: `INCIDENT` → `resolveIncident` + `consume`; `SUBSYSTEM` → `setCondition(s, condition + 25)` + `consume`; `NOTHING` → üzenet, a készlet nem fogy. A fogyasztás `kit.consume(1, player)` (kreatívban nem fogy, jarból ellenőrizve: `ItemStack#consume(int, LivingEntity)`).
  - `ShipInteriorLayout.Piece.REPAIR_STATION`; az állomás cellája `(7, 1, 5)` (belső sarok); a `structure()` tartalmazza; a `ShipInteriorBuilder.stateFor` az állomás blokkra képez; `ShipInteriorBuilder.ensureStation(ServerLevel interior)`: ha a cella levegő, lehelyezi (más blokkot nem ír felül).
  - `ModBlocks.REPAIR_STATION` (`RepairStationBlock extends ShipHullBlock`, IMMOVEABLE, erős, kreatív fül); `ModItems.REPAIR_KIT` (`Item`, `stacksTo(16)`, kreatív fül). A `RepairStationBlock.useItemOn(...)`: ha a kézben a javítókészlet → szerveren `ShipRepair.useKit`, `SUCCESS_SERVER`; egyébként `TRY_WITH_EMPTY_HAND`; a `useWithoutItem` az `station_needs_kit` üzenetet adja.
- Lang kulcsok (en): `block.intersolarsystem.repair_station` = `Repair Station`; `item.intersolarsystem.repair_kit` = `Repair Kit`; `message.intersolarsystem.ship.station_needs_kit` = `Use a repair kit here. Worst subsystem: %s`; `message.intersolarsystem.ship.repair_incident` = `You fixed the %s on %s`; `message.intersolarsystem.ship.repair_subsystem` = `%s restored by %s%%`; `message.intersolarsystem.ship.repair_nothing` = `Nothing needs repair`.

- [ ] **Step 1: Failing tests.**
  - `ShipRepairRulesTest`: `choosesEarliestDeadlineIncident`; `incidentBeatsLowSubsystem`; `choosesLowestSubsystemWhenNoIncident`; `tieBrokenByEnumOrder`; `nothingWhenAllFull`.
  - `ShipInteriorLayoutTest` módosítása: `interiorIsAirOrLight` engedje a `REPAIR_STATION`-t; új `exactlyOneRepairStationInside` (egy darab, a belső téren belül, nem a belépési pont és nem alatta/fölötte); a `coversWholeBoxExactlyOnce` és a `structureIsEverythingButAir` változatlanul zöld (a darabszámok változatlanok, mert az állomás egy levegő cellát vált).
  - `LangParityTest.requiredKeys()` bővítése.
- [ ] **Step 2: Run** `./gradlew test --console=plain` (a játék futását ellenőrizd). Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció** a fenti szignatúrákkal; a `ShipBoarding.board` a hatch-ellenőrzés után hívja `ensureStation(interior)`-t. Placeholder 16×16 textúrák (az állomás a nyílástól megkülönböztethető, a készlet átlátszó hátterű).
- [ ] **Step 4: Datagen és teszt.** `./gradlew runDatagen --console=plain && ./gradlew test --console=plain`. Expected: PASS.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add repair station and repair kit"`

### Task 4: Kézi ellenőrzés és lezárás

**Files:** Modify: `README.md`.

- [ ] **Step 1: `runServer` füstteszt** (csak ha a kliens nem fut): `./gradlew runServer --console=plain`, `Done (`, nincs hiba.
- [ ] **Step 2: Kézi próba `runClient`-ben (a felhasználó végzi), cheats be:**
  1. Több repülés egymás után: `/iss ship info` mutatja a csökkenő állapotot (kb. −8 hajtómű repülésenként).
  2. `/iss ship set engine 15`, majd `/iss ship fly ...` → elutasít (`too damaged`); 20-ra állítva indul.
  3. `/iss ship set engine 30` és a többi alrendszer alacsonyra, repülés közben incidens keletkezik (action bar, üzenet, hang); az `info` mutatja.
  4. A javítókészlettel (`/give` vagy kreatív fül) az állomáson az incidens megoldódik; ha lejár, −25 és üzenet.
  5. Incidens nélkül a készlet a legrosszabb alrendszert +25-tel javítja; teljesen ép hajónál "nincs mit javítani" és a készlet nem fogy; kreatívban a készlet sosem fogy.
  6. Repülés közben kilépés/újratöltés: ugyanaz az incidens-kimenet (nincs újradobás).
  7. Egy régi (2a/2b-ben épült) szoba beszálláskor megkapja az állomást; a játékos által az állomás cellájába tett blokk nem íródik felül.
  8. Az állomás és a készlet neve a három nyelven jó.
- [ ] **Step 3: README** bővítése (kopás, incidensek, javítás).
- [ ] **Step 4: Run** `./gradlew clean build --console=plain` (a játék futását ellenőrizd). Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Commit** `git add -A && git commit -m "docs: document wear, incidents and repair"`

---

**"Kész, ha" lefedettség:** 1 → Task 1 és Task 4 (1.); 2 → Task 2 és Task 4 (3., 6.); 3 → Task 3 és Task 4 (4., 5.); 4 → Task 1 és Task 4 (2.); 5 → az összes feladat tesztjei, a `clean build` és a lang tesztek.
