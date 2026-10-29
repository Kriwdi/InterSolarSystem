# Phase 2b Flight Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A hajó végigmegy a `LANDED → TAKEOFF → FLIGHT → LANDING → LANDED` állapotgépen egy Overworld koordinátára, hiteles pozícióval, tartós mentéssel, és repülés közben nincs be- vagy kiszállás.

**Architecture:** A tiszta logika (időzítés, nyílás-szabályok, leszállóhely-sorrend, `ShipState` átmenetek) Minecraft nélkül tesztelhető. A szerveroldali réteg (`ShipFlightController` tick-kezelő, `ShipLanding`, bővített `ShipBoarding`, `/iss ship fly`) ezeket köti a világhoz.

**Tech Stack:** Java 25, Minecraft 26.3, Fabric API (`ServerTickEvents`, parancs), DFU, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-29-phase2b-flight-design.md`

## Global Constraints

- Minecraft 26.3; mod ID `intersolarsystem`; csomag `hu.kriwdi.intersolarsystem.ship`.
- Időtartamok tickben: `TAKEOFF = 200`, `FLIGHT = 1200`, `LANDING = 100`. Idő forrása az Overworld `getGameTime()`.
- `ShipState` formátum: `CURRENT_FORMAT_VERSION = 3`; az újabb formátum csak olvasható; az új mezők megengedők (lenient), hibás érték nem dobja el a hajót.
- A `landing_site` a hajó hiteles pozíciója.
- Az állapot szerveroldali; hibánál lang-kulcsos üzenet, állapotváltozás nélkül.
- Szöveg en_us/de_de/hu_hu, datagenből; a lang tesztek zöldek maradnak.
- API-pontosság: a tervben jarból **ellenőrzött** szignatúrák vannak; eltérésnél **állj meg és kérdezz**.
- Commit üzenet vége: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Ág: `phase2b-flight`.

## Review Focus

- Újratöltés repülés közben: a repülés folytatódik; ha a mentett kezdő tick a jelenlegi idő után van (sérült vagy visszaállított idő), a hátralévő idő nem lesz negatív vagy túl hosszú. Teszt: Task 1.
- A felszálláskor eltávolított nyílás helyén már más blokk áll: azt nem töröljük. Kézi ellenőrzés: Task 6.
- A cél tenger, láva vagy betöltetlen terület: a keresés és a világspawn-tartalék ad hely. Teszt: Task 1 (spirál), kézi: Task 6.
- Két játékos egyszerre indít repülést: a második `not landed` hibát kap. Teszt: Task 2.
- Beszállási kísérlet visszaszámlálás közben: `BUSY`. Teszt: Task 1.

---

### Task 1: Tiszta logika: fázisok, időzítés, nyílás-szabályok, leszállóhely-sorrend

**Files:**
- Create: `ShipFlightPhase.java`, `ShipFlightTimeline.java`, `ShipHatchRules.java`, `ShipLandingSearch.java` (mind `src/main/java/hu/kriwdi/intersolarsystem/ship/`)
- Test: `ShipFlightTimelineTest.java`, `ShipHatchRulesTest.java`, `ShipLandingSearchTest.java` (`src/test/java/hu/kriwdi/intersolarsystem/ship/`)

**Interfaces:**
- Produces:
  - `enum ShipFlightPhase { LANDED, TAKEOFF, FLIGHT, LANDING; String id(); int durationTicks(); static Optional<ShipFlightPhase> fromId(String) }` (`durationTicks`: 0, 200, 1200, 100; `id` kisbetűs).
  - `ShipFlightTimeline`: `static long remainingTicks(ShipFlightPhase, long startedTick, long nowTick)` (`max(0, duration - max(0, now - started))`, `LANDED` esetén 0); `static boolean isDue(ShipFlightPhase, long startedTick, long nowTick)` (fázis nem `LANDED` és a hátralévő idő 0); `static Optional<ShipFlightPhase> next(ShipFlightPhase)` (`TAKEOFF→FLIGHT→LANDING→LANDED`, `LANDED`-nél üres); `static String formatClock(long ticks)` (felfelé kerekített másodpercek, `m:ss`; 0 tick → `0:00`, 20 tick → `0:01`, 21 → `0:02`, 1200 → `1:00`).
  - `enum HatchAction { BOARD, REGISTER, REDOCK, NOT_CONNECTED, BUSY }` és `ShipHatchRules`: `static HatchAction decide(ShipFlightPhase phase, boolean hasPosition, boolean matchesPosition, boolean positionHatchPresent)`: fázis ≠ `LANDED` → `BUSY`; nincs pozíció → `REGISTER`; egyezik → `BOARD`; a pozíció nyílása hiányzik → `REDOCK`; egyébként `NOT_CONNECTED`. `static boolean canLeave(ShipFlightPhase)` (csak `LANDED`).
  - `ShipLandingSearch`: `record Column(int x, int z)`; `static List<Column> candidates(int centerX, int centerZ, int radius)`: az `(dx,dz)` párok, ahol `dx²+dz² ≤ radius²`, távolság szerint növekvően (azonos távolságon `dx`, majd `dz` növekvő), a középpont az első.

- [ ] **Step 1: Failing tests.**
  - `ShipFlightTimelineTest`: `remainingTicksCountsDown` (TAKEOFF kezdet 100, most 150 → 150); `dueAtExactDuration` (FLIGHT, kezdet 0, most 1200 → due, 1199 → nem); `landedIsNeverDue`; `negativeElapsedClamps` (most < kezdet → a teljes időtartam marad, nem több); `nextChain` (TAKEOFF→FLIGHT→LANDING→LANDED, LANDED üres); `formatClockRoundsUp` (0→`0:00`, 1→`0:01`, 20→`0:01`, 21→`0:02`, 1200→`1:00`, 1220→`1:02`); `idsRoundTrip`.
  - `ShipHatchRulesTest`: mind az öt kimenet külön teszttel (a `BUSY` minden nem `LANDED` fázisra); `canLeaveOnlyWhenLanded`.
  - `ShipLandingSearchTest`: `startsAtCenter`; `allWithinRadiusAndUnique` (radius 5: minden oszlopra `dx²+dz² ≤ 25`, nincs duplikátum); `sortedByDistance`; `radiusZeroIsJustCenter`; `deterministic`.
- [ ] **Step 2: Run** `./gradlew test --console=plain`. Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció** a fenti szignatúrákkal.
- [ ] **Step 4: Run** ugyanazt. Expected: PASS.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add flight phases, timeline, hatch rules and landing search"`

### Task 2: `ShipState` v3: fázis, cél, átmenetek, migráció

**Files:**
- Modify: `ShipState.java`, `ShipStateMigrations.java`
- Test: `ShipStateTest.java`, `ShipStateMigrationsTest.java` (bővítés/módosítás)

**Interfaces:**
- Consumes: `ShipFlightPhase`, `ShipFlightTimeline.next` (Task 1).
- Produces: `ShipState`: `CURRENT_FORMAT_VERSION = 3`; `ShipFlightPhase phase()`; `long phaseStartedTick()`; `Optional<Destination> destination()` (`record Destination(int x, int z)`); `boolean beginTakeoff(int x, int z, long nowTick)` (csak `LANDED`-ből és csak ha nem csak olvasható; siker esetén `TAKEOFF`, cél és kezdő tick beállítva, `setDirty()`, `true`; különben `false` és semmi nem változik); `void advancePhase(long nowTick)` (a `ShipFlightTimeline.next` szerint lép, a kezdő tick `nowTick`, `setDirty()`; `LANDED`-be lépéskor a cél törlődik; `LANDED`-ből vagy csak olvasható állapotban nem tesz semmit). `ShipStateMigrations.defaultSteps()` kibővül a `2 → 3` identitás-lépéssel.
- Mentési mezők (megengedő, opcionális): `phase` (string), `phase_started_tick` (long), `destination_x`, `destination_z` (int). A `phase` mindig kiíródik; hibás vagy ismeretlen értéknél `LANDED` + WARN; a cél csak akkor érvényes, ha mindkét koordináta jelen van. A `phase_started_tick` hibás típusa elhagyva (0).

- [ ] **Step 1: Failing tests.**
  - `ShipStateTest`: `v2SaveLoadsAsLandedV3` (v2 JSON: `LANDED`, nincs cél, `landingSite` megmarad); `flightStateRoundTrip` (`beginTakeoff(100, -50, 500)` után kódolás/dekódolás: `TAKEOFF`, cél `(100,-50)`, kezdő tick 500); `unknownPhaseFallsBackToLanded`; `malformedPhaseFieldsIgnoredNotFatal` (`"phase_started_tick":"x"` és `"phase":5` → a dekódolás sikeres, a hajótest-osztály megmarad); `beginTakeoffOnlyFromLanded` (második hívás `false`, a cél nem változik); `beginTakeoffRefusedWhenReadOnly`; `advanceWalksThroughPhasesAndClearsDestination` (TAKEOFF→FLIGHT→LANDING→LANDED, a kezdő tickek a megadott `nowTick`-ek, a cél a végén üres); `advanceOnLandedDoesNothing` (nem lesz dirty); `partialDestinationIgnored`.
  - `ShipStateMigrationsTest`: a `currentAndNewerUntouched` teszt `"format_version":2` bemenete `ShipState.CURRENT_FORMAT_VERSION`-re, a `v1BecomesCurrent` és `missingVersionTreatedAsV1` elvárása továbbra is `CURRENT_FORMAT_VERSION`; új `v2BecomesV3`.
- [ ] **Step 2: Run** `./gradlew test --console=plain`. Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció.** A nyers kodek mezői `lenientOptionalFieldOf`-fal (`Codec.STRING`, `Codec.LONG`, `Codec.INT`); a getterek a mentett állapotból; `fromRaw` bővül a négy új paraméterrel.
- [ ] **Step 4: Run** ugyanazt. Expected: PASS (minden korábbi teszt is).
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add flight phase and destination to ShipState v3"`

### Task 3: Szövegek (lang)

**Files:**
- Modify: `src/test/java/hu/kriwdi/intersolarsystem/LangParityTest.java`
- Modify: `src/client/java/hu/kriwdi/intersolarsystem/client/datagen/ModLangProvider.java`
- Regenerate: `src/main/generated/.../lang/*.json`

**Interfaces:**
- Produces lang kulcsok (angol szöveg; de/hu fordítás a providerben):
  - `ship.intersolarsystem.phase.<id>` négy fázisra: `Landed`, `Taking off`, `In flight`, `Landing`;
  - `command.intersolarsystem.ship.phase` = `Phase: %s`; `command.intersolarsystem.ship.phase_remaining` = `Time left: %s`; `command.intersolarsystem.ship.destination` = `Destination: %s, %s`;
  - `command.intersolarsystem.ship.fly_started` = `Takeoff sequence started. Destination: %s, %s`; `command.intersolarsystem.ship.fly_not_inside` = `You must be inside the ship to fly it`; `command.intersolarsystem.ship.fly_not_landed` = `The ship is not ready to take off`; `command.intersolarsystem.ship.fly_out_of_border` = `That destination is outside the world border`;
  - `message.intersolarsystem.ship.busy` = `The ship is away or busy`; `message.intersolarsystem.ship.not_connected` = `This hatch is not connected to the ship`; `message.intersolarsystem.ship.cannot_leave` = `You cannot leave the ship right now`; `message.intersolarsystem.ship.redocked` = `The ship docks at this hatch`;
  - `message.intersolarsystem.ship.countdown.takeoff` = `Takeoff in %s`; `message.intersolarsystem.ship.countdown.flight` = `Arrival in %s`; `message.intersolarsystem.ship.countdown.landing` = `Landing in %s`;
  - `message.intersolarsystem.ship.landed` = `The ship has landed at %s, %s`; `message.intersolarsystem.ship.landed_spawn` = `No dry landing site nearby; the ship landed near the world spawn at %s, %s`.

- [ ] **Step 1: Failing test:** a `LangParityTest.requiredKeys()` listája kibővül a fenti kulcsokkal (a fázis-kulcsok a `ShipFlightPhase` enumból). Run `./gradlew test --tests '*LangParityTest' --console=plain`. Expected: FAIL.
- [ ] **Step 2: Implementáció:** a `ModLangProvider` `Texts` rekordja bővül (a fázisnevek tömbje a `ShipFlightPhase` sorrendjében), mindhárom nyelven. A fázisnév-kulcs képzése egy `ShipText.phaseKey(ShipFlightPhase)` segédben (a meglévő `ShipCommands.hullKey` mintájára).
- [ ] **Step 3: Datagen és teszt.** Run `./gradlew runDatagen --console=plain && ./gradlew test --console=plain`. Expected: PASS.
- [ ] **Step 4: Commit** `git add -A && git commit -m "feat(ship): add flight texts (en/de/hu)"`

### Task 4: Vezérlő, leszállás, nyílás-szabályok

**Files:**
- Create: `ShipFlightController.java`, `ShipLanding.java`
- Modify: `ShipBoarding.java`, `InterSolarSystem.java` (`ShipFlightController.register()`)

**Interfaces:**
- Consumes: Task 1–3 osztályai és kulcsai, `ShipStateAccess.get(MinecraftServer)`, `ShipBoardingRules.findStandingSpot`.
- Produces:
  - `ShipFlightController.register()`: `ServerTickEvents.END_SERVER_TICK`-re: ha a fázis ≠ `LANDED` és `ShipFlightTimeline.isDue(...)`, lefuttatja az átmenet mellékhatásait (alább), majd `state.advancePhase(now)`; másodpercenként (ha `now % 20 == 0`) action bar a belső dimenzió játékosainak (`message.intersolarsystem.ship.countdown.<fázis>` + `formatClock(hátralévő)`). A mellékhatások **előbb** futnak, az `advancePhase` utánuk, hogy hiba esetén a következő tick újrapróbálja (az átmenetek idempotensek).
  - Mellékhatások: `TAKEOFF → FLIGHT`: ha a `landing_site` szintje betöltött és ott a `SHIP_HATCH` áll, `removeBlock(pos, false)` (más blokkot nem törlünk); hang. `FLIGHT → LANDING`: hang. `LANDING → LANDED`: `ShipLanding.land(...)`, hang, üzenet a belső játékosoknak.
  - `ShipLanding`: `static Optional<BlockPos> findHatchCell(ServerLevel overworld, int x, int z, int radius)` (a `ShipLandingSearch.candidates` sorrendjében az első alkalmas oszlop: a cella a `getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)` magasságban szabad (kicserélhető) és nem folyadék, alatta tömör nem folyadék blokk áll, és a `ShipBoardingRules.findStandingSpot` ad álló helyet mellette); `static BlockPos land(MinecraftServer server, ShipState state)`: a cél körül 32 sugarú keresés, siker nélkül a világspawn oszlopa körül ugyanez (és `landed_spawn` üzenet), majd `SHIP_HATCH` lehelyezése, `state.setLandingSite(...)` az új helyre, az eredmény visszaadva. Ha a keresés a spawn körül is üres, a spawn oszlop magasságára kényszerítve helyez le.
  - `ShipBoarding` bővítés: beszálláskor `ShipHatchRules.decide(phase, state.landingSite().isPresent(), egyezik-e dimenzió és koordináta, a pozíció nyílás-blokkja megvan-e (a szint hiánya = nincs meg))`: `BOARD` és `REGISTER` (`setLandingSite`) és `REDOCK` (`setLandingSite` + `message.intersolarsystem.ship.redocked`) lép be; `NOT_CONNECTED` és `BUSY` a megfelelő üzenettel kilép. Kiszálláskor `!ShipHatchRules.canLeave(phase)` → `message.intersolarsystem.ship.cannot_leave`, nincs teleport.
- API (jarból ellenőrizve, 26.3): `ServerTickEvents.END_SERVER_TICK` (`onEndTick(MinecraftServer)`); `LevelAccessor#getGameTime()` (default, `ServerLevel`-en hívható); `LevelReader#getHeight(Heightmap.Types, int, int)`; `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`; `BlockBehaviour.BlockStateBase#canBeReplaced()`, `#getFluidState()`; `Level#removeBlock(BlockPos, boolean)`; `ServerLevel#players()`; `Level#playSound(Entity, BlockPos, SoundEvent, SoundSource, float, float)`; `SoundEvents.FIREWORK_ROCKET_LAUNCH` / `BEACON_ACTIVATE` / `ANVIL_LAND`; `ServerPlayer#sendSystemMessage(Component, boolean)` (a `true` az action bar).

- [ ] **Step 1: Failing test:** a tiszta részek lefedettek a Task 1–2-ben; itt a fordítás a kapu. Írj egy kis tesztet a `ShipFlightController`-ből kiemelt tiszta döntésre: `ShipFlightTick.dueTransition(ShipFlightPhase phase, long startedTick, long nowTick)` (`Optional<ShipFlightPhase>` a következő fázissal, ha esedékes) a `ShipFlightTimelineTest`-ben: `dueTransitionReturnsNextWhenDue` (`TAKEOFF`, 0, 200 → `Optional.of(FLIGHT)`), `dueTransitionEmptyWhenNotDue`, `dueTransitionEmptyWhenLanded`. Run. Expected: FAIL (fordítási hiba).
- [ ] **Step 2: Implementáció** (először a `dueTransition` a `ShipFlightTimeline`-ban, majd a szerveroldali osztályok).
- [ ] **Step 3: Run** `./gradlew clean build --console=plain`. Expected: BUILD SUCCESSFUL, minden teszt zöld.
- [ ] **Step 4: Commit** `git add -A && git commit -m "feat(ship): add flight controller, landing and flight-aware hatch rules"`

### Task 5: `/iss ship fly` és `info`

**Files:** Modify: `ShipCommands.java`

**Interfaces:**
- Consumes: `ShipState.beginTakeoff`, `ShipStateAccess`, a Task 3 kulcsai, `ShipDimensions.INTERIOR`.
- Produces: `/iss ship fly <x> <z>` (`requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))`, `x` és `z` `IntegerArgumentType.integer()`); sorrendben ellenőrzi: a küldő `ServerPlayer` a belső dimenzióban van (`fly_not_inside`), a mentés nem csak olvasható (`read_only`), a cél a `server.overworld().getWorldBorder().isWithinBounds(x, z)` szerint határon belül van (`fly_out_of_border`), majd `beginTakeoff(x, z, server.overworld().getGameTime())`; `false` esetén `fly_not_landed`; siker: `fly_started`. Az `info` kiegészül a fázis, a hátralévő idő (`formatClock`, csak ha nem `LANDED`) és a cél soraival.

- [ ] **Step 1: Run** `./gradlew clean build --console=plain` a módosítás előtt. Expected: BUILD SUCCESSFUL (alapállapot).
- [ ] **Step 2: Implementáció** a fenti ellenőrzési sorrenddel.
- [ ] **Step 3: Run** `./gradlew clean build --console=plain`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 4: Commit** `git add -A && git commit -m "feat(ship): add /iss ship fly and flight info"`

### Task 6: Kézi ellenőrzés és lezárás

**Files:** Modify: `README.md` (a repülés leírása és a parancs).

- [ ] **Step 1: `runServer` füstteszt.** `./gradlew runServer --console=plain`, `Done (`, nincs hiba.
- [ ] **Step 2: Kézi próba `runClient`-ben (a felhasználó végzi), egyjátékos, cheats be:**
  1. Nyílás letétele, beszállás (`REGISTER`), `/iss ship fly 200 200` a hajóból: action bar visszaszámlál (10 mp, 1:00, 5 mp), a játékos a hajóban marad.
  2. Visszaszámlálás és repülés közben a belső nyílás nem enged kiszállni (`cannot_leave`).
  3. Felszálláskor a külső nyílás eltűnik; egy új, máshová lerakott nyílás repülés közben `busy`.
  4. Leszállás után a kiszállás az új nyílás mellé visz a (200, 200) környékén; `/iss ship info` `Landed`.
  5. Egy másik, idegen nyílás leszállt hajó mellett `not connected`; a hajó nyílásának kiütése után az idegen nyílás újra-dokkol.
  6. Repülés közben kilépés és újratöltés: a repülés ugyanonnan folytatódik.
  7. Óceán fölé irányított cél: száraz hely vagy a spawn-tartalék és annak üzenete.
  8. Második játékos (LAN) a hajóban együtt repül; kétszeri `fly` indítás: a második hibaüzenetet kap.
  9. Nyelvváltás után az action bar és az üzenetek a választott nyelvűek.
- [ ] **Step 3: README** bővítése.
- [ ] **Step 4: Run** `./gradlew clean build --console=plain`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Commit** `git add -A && git commit -m "docs: document ship flight"`

---

**"Kész, ha" lefedettség:** 1 → Task 4–5 és Task 6 (1., 4.); 2 → Task 1 (`ShipHatchRules`), Task 4 és Task 6 (2., 3., 5.); 3 → Task 2 és Task 6 (6.); 4 → Task 1–3 tesztjei és `clean build`; 5 → Task 6 (8.).
