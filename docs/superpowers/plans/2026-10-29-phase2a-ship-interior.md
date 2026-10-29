# Phase 2a Ship Interior Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Egy `ship_hatch` blokk, amellyel a játékos a hajó saját dimenziójú, lezárt belső terébe száll be és onnan vissza; a leszállóhely és a szoba mentett; a mentésformátum v2 migrációs vázzal.

**Architecture:** A tiszta logika (migráció, szoba-elrendezés, kiszállási hely keresése) Minecraft nélkül tesztelhető. A szerveroldali réteg (`ShipInteriorBuilder`, `ShipBoarding`, `ShipHatchBlock`) ezeket köti a világhoz. A dimenzió adatvezérelt (datapack JSON).

**Tech Stack:** Java 25, Minecraft 26.3, Fabric API, DFU (`Dynamic`, `Codec`), JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-29-phase2a-ship-interior-design.md`

## Global Constraints

- Minecraft 26.3; mod ID `intersolarsystem`; csomag `hu.kriwdi.intersolarsystem.ship` (blokk: `...content`).
- Dimenzió azonosítója `intersolarsystem:ship_interior`; blokk: `ship_hatch`; szoba belső tere 7×4×5, a külső héj 9×6×7.
- Mentésformátum: `CURRENT_FORMAT_VERSION = 2`; az újabb formátum csak olvasható marad (nincs írás, a beszállás működik).
- Az állapot szerveroldali; a kliens nem tárol és nem dönt.
- Szöveg en_us/de_de/hu_hu, datagenből; a lang tesztek zöldek maradnak.
- API-pontosság: a jarból **ellenőrzött** szignatúrák a tervben vannak; ami nincs ellenőrizve, annak lépése jar-ellenőrzéssel kezdődik. Eltérésnél **állj meg és kérdezz**.
- Commit üzenet vége: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Ág: `phase2a-ship-interior`.

## Review Focus

- A játékos a belső nyílás kiütésével csapdába eshet. Védelem: a nyílás a belső dimenzióban törhetetlen. Kézi ellenőrzés: Task 4.
- Szoba újraépítése kétszer ugyanazt adja (idempotens, nincs duplikált nyílás). Teszt: Task 2 (egyedi koordináták), kézi: Task 4.
- A belső dimenzió nem töltődött be (hibás JSON): a beszállás hibaüzenetet ad, nem NPE-t. Teszt: Task 3 (JSON-ok léteznek és jó típusra mutatnak); kézi: Task 5.
- Hibás vagy hiányos mentett leszállóhely (hiányzó z, ismeretlen dimenzió): a betöltés nem bukik el, a kiszállás a tartalék helyre visz. Teszt: Task 1 és Task 4 (`ShipBoardingRulesTest`).
- A kliens oldali nyílás-használat nem teleportál és nem módosít állapotot (nincs dupla beszállás). Kézi ellenőrzés: Task 5.

---

### Task 1: Migrációs váz és `ShipState` v2

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipStateMigrations.java`
- Modify: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipState.java`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipStateMigrationsTest.java`
- Test (bővítés): `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipStateTest.java`

**Interfaces:**
- Produces: `ShipStateMigrations`:
  - `interface Step { <T> Dynamic<T> apply(Dynamic<T> data); }`
  - `record Migration(int from, Step step)`
  - `static final List<Migration> DEFAULT_STEPS` (egyetlen elem: `from = 1`, identitás lépés, mert az új mezők opcionálisak)
  - `static <T> Dynamic<T> migrate(Dynamic<T> data, List<Migration> steps, int targetVersion)`: a verzió a `format_version` (hiányzó vagy < 1 → 1); amíg a verzió < `targetVersion`, a `from == verzió` lépés lefut, majd a `format_version` a következő értékre áll; ha nincs ilyen lépés, a ciklus WARN-nal megáll; verzió ≥ cél esetén érintetlen.
  - `static <T> Dynamic<T> migrate(Dynamic<T> data)`: `DEFAULT_STEPS` és `ShipState.CURRENT_FORMAT_VERSION`.
- Produces: `ShipState`: `CURRENT_FORMAT_VERSION = 2`; `record LandingSite(String dimension, int x, int y, int z)`; `Optional<LandingSite> landingSite()`; `void setLandingSite(LandingSite)`; `boolean interiorBuilt()`; `void setInteriorBuilt(boolean)` (mindhárom író csak módosításkor `setDirty()`, és `readOnly` esetén nem tesz semmit). `CODEC = Codec.of(RAW_CODEC, migratingDecoder)`: a dekódoló a bemenetet `Dynamic`-ként migrálja, majd a nyers kodekkel dekódol; a kódolás a nyers kodeké. A nyers mezők: a meglévők + `landing_dimension` (string), `landing_x`, `landing_y`, `landing_z` (int), `interior_built` (bool), mind `Optional`, a `format_version` és `interior_built` mindig kiíródik; a leszállóhely csak akkor érvényes, ha mind a négy mező jelen van (egyébként WARN és üres).

- [ ] **Step 1: Failing tests.** `ShipStateMigrationsTest` (JSON `Dynamic`-kel, `new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseString(...))`):
  - `v1BecomesCurrent`: `{"format_version":1}` → `format_version == 2`.
  - `missingVersionTreatedAsV1`: `{}` → `format_version == 2`.
  - `currentAndNewerUntouched`: `{"format_version":2,"x":1}` és `{"format_version":99}` változatlan.
  - `injectedStepRenamesField`: egy teszt-lépéslistával (`from=1`, `data.renameField("subsystems","conditions")`, cél 2): a bemenet `subsystems` kulcsa `conditions` lesz.
  - `stepsChainInOrder`: két lépés (1→2 hozzáad `a`, 2→3 hozzáad `b`), cél 3: mindkettő lefut, a verzió 3.
  - `missingStepStopsWithoutException`: cél 3, csak az 1→2 lépés van: nincs kivétel, a verzió 2 marad.
  
  `ShipStateTest` bővítés (és a meglévő `defaultShip`/`codecRoundTrip` asszerciói `ShipState.CURRENT_FORMAT_VERSION`-ra cserélve az `1` helyett):
  - `v1SaveLoadsAsCurrent`: `{"format_version":1,"hull_class":"scout","subsystems":{"engine":40}}` → `ENGINE` 40, `SCOUT`, `landingSite()` üres, `interiorBuilt()` hamis, nem csak olvasható.
  - `landingSiteRoundTrip`: beállított `LandingSite("minecraft:overworld", 10, 64, -3)` körbejár; a kódolt JSON-ban `interior_built` szerepel hamis értékkel.
  - `partialLandingSiteIgnored`: `{"landing_dimension":"minecraft:overworld","landing_x":1,"landing_y":2}` → üres leszállóhely, nincs kivétel.
  - `landingSiteAndInteriorFlagMarkDirty`: `setLandingSite`/`setInteriorBuilt(true)` dirty-re vált; ugyanannak az értéknek újra beállítása tiszta állapotból nem.
  - `readOnlyRefusesNewWrites`: `{"format_version":99}` állapotnál `setLandingSite`/`setInteriorBuilt(true)` nem változtat és nem dirty.
- [ ] **Step 2: Run** `./gradlew test --console=plain`. Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció** a fenti szignatúrákkal. A `migrate` generikus `Dynamic<T>`-n dolgozik (`data.get("format_version").asInt(1)`, `data.set("format_version", data.createInt(n))`). A `Codec.of(encoder, decoder)` dekódolója `<T> DataResult<Pair<ShipState, T>> decode(DynamicOps<T> ops, T input)`: `migrate(new Dynamic<>(ops, input))`, majd `RAW_CODEC.decode(ops, migrated.getValue())`.
- [ ] **Step 4: Run** `./gradlew test --console=plain`. Expected: PASS (a korábbi 21 teszt + az újak).
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add save migration framework and ShipState v2"`

### Task 2: Szoba-elrendezés

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipInteriorLayout.java`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipInteriorLayoutTest.java`

**Interfaces:**
- Produces: `ShipInteriorLayout`: `enum Piece { HULL_PANEL, LIGHT, HATCH, AIR }`; `record Cell(int dx, int dy, int dz)`; `record Placement(Cell cell, Piece piece)`; `static final int SIZE_X = 9, SIZE_Y = 6, SIZE_Z = 7`; `static ShipInteriorLayout capsule()`; `List<Placement> placements()` (a teljes 9×6×7 doboz minden cellája pontosan egyszer); `Cell hatch()`; `Cell entry()` (a lábak helye).
- Tervezés: a héj (a doboz felülete) `HULL_PANEL`, kivéve a nyílást `(4, 2, 0)` (`HATCH`); a belső cellák `AIR`, kivéve négy `LIGHT` a belső mennyezeti rétegben `(2,4,2) (6,4,2) (2,4,4) (6,4,4)`; a belépési pont `(4, 1, 3)`.

- [ ] **Step 1: Failing tests** `ShipInteriorLayoutTest`:
  - `coversWholeBoxExactlyOnce`: `placements().size() == 9*6*7`, minden `Cell` egyedi, a koordináták a dobozon belül vannak.
  - `exactlyOneHatchOnTheShell`: pontosan egy `HATCH`, és az a doboz felületén van.
  - `shellIsClosed`: a felület minden cellája `HULL_PANEL` vagy `HATCH`.
  - `interiorIsAirOrLight`: a belső cellák (x 1..7, y 1..4, z 1..5) mind `AIR` vagy `LIGHT`; pontosan négy `LIGHT`.
  - `entryIsStandable`: az `entry()` és az fölötte lévő cella `AIR`, az alatta lévő cella `HULL_PANEL`.
  - `deterministic`: két `capsule()` hívás azonos elrendezést ad.
- [ ] **Step 2: Run** `./gradlew test --tests '*ShipInteriorLayoutTest' --console=plain`. Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció** a fenti szignatúrákkal és tervezéssel.
- [ ] **Step 4: Run** ugyanazt. Expected: PASS 6/6.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add capsule interior layout"`

### Task 3: A `ship_hatch` blokk

**Files:**
- Modify: `src/main/java/hu/kriwdi/intersolarsystem/content/ModBlocks.java` (`SHIP_HATCH`, kreatív fül)
- Create: `src/main/resources/assets/intersolarsystem/{blockstates/ship_hatch.json,models/block/ship_hatch.json,items/ship_hatch.json,textures/block/ship_hatch.png}`
- Modify: `src/client/java/.../datagen/ModLangProvider.java`, `ModBlockLootTableProvider.java`
- Modify: `src/test/java/hu/kriwdi/intersolarsystem/LangParityTest.java`
- Regenerate: `src/main/generated/...`

**Interfaces:**
- Produces: `ModBlocks.SHIP_HATCH` (`Block`, azonosító `ship_hatch`, a `rusted_panel` mintájára, a Task 4 később `ShipHatchBlock`-ra cseréli a gyártót); lang kulcsok: `block.intersolarsystem.ship_hatch`, `message.intersolarsystem.ship.boarded`, `message.intersolarsystem.ship.exited`, `message.intersolarsystem.ship.exit_fallback`, `message.intersolarsystem.ship.interior_missing`.
- Angol szövegek: `Ship Hatch`; `You board the ship`; `You leave the ship`; `The landing site is gone; you were put at the world spawn`; `The ship interior is not available`. A de/hu fordítás a providerben.

- [ ] **Step 1: Failing test:** a `LangParityTest.requiredKeys()` listája kibővül a fenti öt kulccsal. Run `./gradlew test --tests '*LangParityTest' --console=plain`. Expected: FAIL (a kulcsok hiányoznak).
- [ ] **Step 2: Implementáció.** Blokk és erőforrások a `rusted_panel` mintájára (`cube_all` modell, egyszerű 16×16 placeholder textúra, megkülönböztethető a panelektől); a `ModLangProvider` `Texts` rekordja bővül az új szövegekkel mindhárom nyelven; a loot provider `dropSelf(ModBlocks.SHIP_HATCH)`; a blokk a `BUILDING_BLOCKS` fülre kerül.
- [ ] **Step 3: Datagen és teszt.** Run `./gradlew runDatagen --console=plain && ./gradlew test --console=plain`. Expected: PASS.
- [ ] **Step 4: Commit** `git add -A && git commit -m "feat(content): add ship hatch block and boarding texts"`

### Task 4: Dimenzió, építő, beszállás

**Files:**
- Create: `src/main/resources/data/intersolarsystem/dimension_type/ship_interior.json`
- Create: `src/main/resources/data/intersolarsystem/dimension/ship_interior.json`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipDimensions.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipBoardingRules.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipInteriorBuilder.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipBoarding.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/content/ShipHatchBlock.java`
- Modify: `ModBlocks.java` (a `SHIP_HATCH` gyártója `ShipHatchBlock::new`)
- Test: `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipDimensionDataTest.java`, `ShipBoardingRulesTest.java`

**Interfaces:**
- Consumes: `ShipInteriorLayout`, `ShipState` (Task 1–2), `ModBlocks.SHIP_HATCH` (Task 3), `ShipStateAccess.get(MinecraftServer)`.
- Produces:
  - `ShipDimensions.INTERIOR` (`ResourceKey<Level>`, `ModIds.id("ship_interior")`) és `ShipDimensions.ANCHOR` (`BlockPos`, `(0, 64, 0)`, a szoba legkisebb sarka).
  - `ShipBoardingRules.findStandingSpot(Cell hatch, Predicate<Cell> isSolid, Predicate<Cell> isFree) -> Optional<Cell>`: a nyílás négy vízszintes szomszédját `+x, +z, -x, -z` sorrendben nézi; az első `n`, ahol `isFree(n)`, `isFree(n+fölfelé)` és `isSolid(n+lefelé)`. (A `Cell` a `ShipInteriorLayout.Cell`.)
  - `ShipInteriorBuilder.build(ServerLevel interior)`: a `capsule()` minden elemét lehelyezi az `ANCHOR`-hoz képest (`Piece` → blokk: `HULL_PANEL` = `ModBlocks.RUSTED_PANEL`, `LIGHT` = `Blocks.SEA_LANTERN`, `HATCH` = `ModBlocks.SHIP_HATCH`, `AIR` = levegő); a lehelyezés előtt a cél chunk betöltődik.
  - `ShipBoarding.use(ServerLevel level, BlockPos hatchPos, ServerPlayer player)`: ha `level.dimension() == ShipDimensions.INTERIOR` → kiszállás, különben beszállás (a spec szerint).
  - `ShipHatchBlock extends Block`: a `useWithoutItem` szerveren `ShipBoarding.use`-t hív; kliensen sikert ad vissza teleport nélkül; a `ShipDimensions.INTERIOR`-ban a nyílás nem törhető (a leállítási folyamat 0).
- A dimenzió JSON-ok: a `dimension_type` a vanilla `the_end.json` sémájából indul (a 26.3 mezői: `attributes`, `default_clock`, `skybox`, `timelines` stb.), a sárkányharc, az ambient hang és a zene nélkül, `has_fixed_time` igaz, `monster_spawn_*` letiltva; a `dimension` JSON `type = intersolarsystem:ship_interior`, generátor `minecraft:flat` üres rétegekkel és `minecraft:the_void` biommal.

- [ ] **Step 1: API-ellenőrzés (jar, `javap`), az eredmény a commit üzenetbe:**
  - `BlockBehaviour#useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)` visszatérése `InteractionResult`; ellenőrizd a 26.3-as sikerértékek nevét (`SUCCESS`, `SUCCESS_SERVER`, `PASS`) a jarban.
  - `ServerPlayer#teleport(TeleportTransition)` és a `TeleportTransition(ServerLevel, Vec3, Vec3, float, float, PostTeleportTransition)` konstruktor (a tervben ellenőrzött); a használandó `PostTeleportTransition` értéke `TeleportTransition.DO_NOTHING`.
  - A törésidő felülírható metódusa a `BlockBehaviour`-ban (a `getDestroyProgress` 26.3-as neve és szignatúrája).
  - A világspawn lekérdezése a szerveren (a `ServerLevel`/`MinecraftServer` 26.3-as metódusa). Ha nem egyértelmű, **állj meg és kérdezz**.
  - Chunk betöltése szerveroldalon a lehelyezés előtt (`ServerLevel#getChunk(int, int)` vagy megfelelője).
- [ ] **Step 2: Failing tests.**
  - `ShipDimensionDataTest`: a classpath-on léteznek a két JSON; a `dimension` JSON `type` értéke `intersolarsystem:ship_interior`; a `dimension_type` JSON-ban `has_fixed_time` igaz (szövegben/JSON-ban ellenőrizve), és nincs `has_ender_dragon_fight` igaz érték.
  - `ShipBoardingRulesTest` (lambdákkal): `picksFirstFreeNeighbourInOrder` (+x elsőbbség); `skipsNeighbourWithoutFloor`; `skipsNeighbourWithoutHeadroom`; `emptyWhenNoSpot`.
- [ ] **Step 3: Run** `./gradlew test --console=plain`. Expected: FAIL (fordítási hiba és hiányzó JSON).
- [ ] **Step 4: Implementáció.** Először a JSON-ok és a tiszta `ShipBoardingRules`, utána a szerveroldali osztályok. Beszállás: `ShipStateAccess.get(server)`; ha a belső szint nem elérhető (`server.getLevel(INTERIOR) == null`) → `message.intersolarsystem.ship.interior_missing` hibaüzenet, nincs teleport; ha `!interiorBuilt()` vagy a nyílás-cella nem a `SHIP_HATCH` blokk → `ShipInteriorBuilder.build`, `setInteriorBuilt(true)`; a külső nyílás pozíciója `setLandingSite(...)` (csak ha nem csak olvasható); teleport a `ANCHOR + entry()` közepére; `message.intersolarsystem.ship.boarded`. Kiszállás: a leszállóhely szintje és a `findStandingSpot` a külső nyíláshoz; siker → teleport + `message.intersolarsystem.ship.exited`; különben a világspawn + `message.intersolarsystem.ship.exit_fallback`.
- [ ] **Step 5: Run** `./gradlew clean build --console=plain`. Expected: BUILD SUCCESSFUL, minden teszt zöld.
- [ ] **Step 6: Commit** `git add -A && git commit -m "feat(ship): add ship interior dimension, builder and boarding"`

### Task 5: Kézi ellenőrzés és lezárás

**Files:** Modify: `README.md` (beszállás leírása, a belső dimenzió).

- [ ] **Step 1: `runServer` füstteszt.** Run: `./gradlew runServer --console=plain` (EULA elfogadva), a `Done (` megjelenik, nincs `NoClassDefFoundError`; a dimenzió betöltődik: a konzolon `execute in intersolarsystem:ship_interior run setblock 20 64 20 minecraft:stone` sikeres.
- [ ] **Step 2: Kézi próba `runClient`-ben (a felhasználó végzi):**
  1. Kreatívban lerakni a `ship_hatch`-et, jobb klikk → a lezárt, megvilágított szobában vagy; a szöveg "You board the ship"-szerű.
  2. A szoba belső nyílásán jobb klikk → vissza a külső nyílás mellé.
  3. Kilépés, a világ újratöltése, újra beszállás: a szoba és a leszállóhely megvan; a belső nyílás törése túlélő módban nem megy.
  4. A külső nyílás kiütése, majd a belső nyílás használata → kiszállás a világspawnra és a tartalék üzenet.
  5. Második játékos (LAN) beszállhat ugyanabba a szobába.
  6. A kliens oldali kattintás nem okoz dupla üzenetet vagy dupla teleportot.
  7. Nyelvváltás után az üzenetek a választott nyelvűek.
- [ ] **Step 3: README** bővítése.
- [ ] **Step 4: Run** `./gradlew clean build --console=plain`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Commit** `git add -A && git commit -m "docs: document ship hatch and interior"`

---

**"Kész, ha" lefedettség:** 1 → Task 4 és Task 5 (1–2. kézi); 2 → Task 5 (3. kézi); 3 → Task 1; 4 → Task 2, Task 4 teszt és `clean build`; 5 → Task 5 (5. kézi).
