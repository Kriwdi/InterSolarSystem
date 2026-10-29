# Phase 1 ShipState Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Egy szerveroldali, világgal együtt mentett `ShipState` (hajótest-osztály, nyolc alrendszer 0–100 állapottal, formátumverzió) és egy `/iss ship` debug parancs a kézi próbához.

**Architecture:** A tiszta adatmodell (`ShipHullClass`, `ShipSubsystem`, `ShipState` + `CODEC`) Minecraft-bootstrap nélkül tesztelhető. A `ShipStateAccess` köti a `SavedDataType`-ot a szerver `SavedDataStorage`-ához, a `ShipCommands` a parancsot regisztrálja. A szöveg lang kulcsokból jön (datagen).

**Tech Stack:** Java 25, Minecraft 26.3 (`SavedData`, `SavedDataType`, `SavedDataStorage`, Brigadier), Fabric API (command v2), JUnit 5, DFU `Codec` + `JsonOps` a tesztekben.

**Spec:** `docs/superpowers/specs/2026-10-29-phase1-ship-state-design.md`

## Global Constraints

- Minecraft 26.3; mod ID `intersolarsystem`; csomag `hu.kriwdi.intersolarsystem.ship`.
- Egy hajó világonként; mentési azonosító `intersolarsystem:ship_state`.
- Alrendszer-állapot mindig `[0, 100]`; `CURRENT_FORMAT_VERSION = 1`.
- `ShipHullClass`: `RESCUE_CAPSULE` (alapértelmezett), `RESEARCH_VESSEL`, `CARGO_MINER`, `SCOUT`, `MOTHERSHIP`. `ShipSubsystem`: `ENGINE`, `FUEL_ENERGY`, `HULL_ARMOR`, `LIFE_SUPPORT`, `NAVIGATION_COMMS`, `STABILIZER`, `LANDING_LAUNCH`, `CARGO`.
- Parancs: `/iss ship info` (mindenki), `/iss ship set <subsystem> <value>` (`Commands.LEVEL_GAMEMASTERS`, érték 0–100).
- Szöveg: en_us, de_de, hu_hu, datagenből; a kliens nem tárol állapotot.
- API-pontosság: ha egy 26.3 osztály vagy metódus nem egyezik ezzel a tervvel, **állj meg és kérdezz**; a `javap`-pal ellenőrzött szignatúrák a tervben vannak.
- Commit üzenet vége: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Ág: `phase1-ship-state`.

## Review Focus

- Hibás típusú mentett érték (pl. string az alrendszer helyén): a dekódolás hibaeredményt ad, nem kivételt. Teszt: Task 2.
- Óriási vagy negatív szám (`Integer.MAX_VALUE`, `-5`) a mentésben: szűkítés. Teszt: Task 2.
- Újabb `format_version`, mint a kód ismer: nem omlik össze, WARN naplóz. Teszt: Task 2.
- Fordítás helyőrzők száma nyelvenként eltér (`%s`): rossz kimenet. Teszt: Task 3.
- Elgépelt alrendszer-azonosító a parancsban: hibaüzenet, nem kivétel; a `set` nem-OP-nak nem elérhető. Kézi ellenőrzés: Task 4.

---

### Task 1: Enumok

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipHullClass.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipSubsystem.java`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipEnumsTest.java`

**Interfaces:**
- Produces: `enum ShipHullClass { RESCUE_CAPSULE, RESEARCH_VESSEL, CARGO_MINER, SCOUT, MOTHERSHIP; String id(); static Optional<ShipHullClass> fromId(String) }` és `enum ShipSubsystem { ENGINE, FUEL_ENERGY, HULL_ARMOR, LIFE_SUPPORT, NAVIGATION_COMMS, STABILIZER, LANDING_LAUNCH, CARGO; String id(); static Optional<ShipSubsystem> fromId(String); static List<String> ids() }`. Az `id()` a kisbetűs enum-név (`"rescue_capsule"`, `"engine"`).

- [ ] **Step 1: Failing test** `ShipEnumsTest`:
  - `hullClassIdsRoundTrip`: minden `ShipHullClass`-ra `fromId(c.id()) == Optional.of(c)`.
  - `subsystemIdsRoundTrip`: ugyanez `ShipSubsystem`-re; `ShipSubsystem.values().length == 8`, `ShipHullClass.values().length == 5`.
  - `unknownIdsAreEmpty`: `fromId("nope")` üres mindkettőre; `fromId("ENGINE")` is üres (az azonosító kisbetűs, pontos egyezés).
  - `idsListMatchesValues`: `ShipSubsystem.ids()` sorrendben megegyezik az enum sorrendjével.
- [ ] **Step 2: Run** `./gradlew test --tests '*ShipEnumsTest' --console=plain`. Expected: FAIL (fordítási hiba, az enumok nem léteznek).
- [ ] **Step 3: Implementáció** a fenti szignatúrákkal; az `id()` a `name().toLowerCase(Locale.ROOT)`.
- [ ] **Step 4: Run** ugyanazt. Expected: PASS 4/4.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add ShipHullClass and ShipSubsystem enums"`

### Task 2: ShipState és kodek

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipState.java`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/ship/ShipStateTest.java`

**Interfaces:**
- Consumes: `ShipHullClass`, `ShipSubsystem` (Task 1).
- Produces: `public class ShipState extends SavedData` — `public static final int CURRENT_FORMAT_VERSION = 1`; `public static final Codec<ShipState> CODEC`; `public ShipState()` (alapértelmezett: `RESCUE_CAPSULE`, minden alrendszer 100, `isDirty()` false); `int formatVersion()`; `ShipHullClass hullClass()`; `void setHullClass(ShipHullClass)`; `int condition(ShipSubsystem)`; `void setCondition(ShipSubsystem, int)` (szűkít 0–100-ra, `setDirty()` csak ha az érték változott); `static int clamp(int)`.
- A `CODEC` mezői: `format_version` (int, alapértelmezett 1), `hull_class` (string, alapértelmezett `"rescue_capsule"`), `subsystems` (string→int map, alapértelmezett üres). Dekódoláskor: ismeretlen hajótest-osztály → alapértelmezett + WARN; hiányzó alrendszer → 100; ismeretlen alrendszer-kulcs → kihagyva + WARN; érték szűkítve; `format_version` > `CURRENT_FORMAT_VERSION` → teljes alapértelmezett állapot + WARN. Kódoláskor mindig `CURRENT_FORMAT_VERSION`, a hajótest-id és mind a nyolc alrendszer.

- [ ] **Step 1: Failing tests** `ShipStateTest` (a kodek `JsonOps.INSTANCE`-szel, JSON-t Gson `JsonParser`-rel):
  - `defaultShip`: új `ShipState`: osztály `RESCUE_CAPSULE`, mind a 8 alrendszer 100, `isDirty()` false, `formatVersion() == 1`.
  - `setConditionClampsAndMarksDirty`: `setCondition(ENGINE, 130)` → 100; `(ENGINE, -5)` → 0 és `isDirty()` true; azonos érték újra beállítása tiszta állapotból nem lesz dirty.
  - `codecRoundTrip`: állíts be `SCOUT`-ot és `ENGINE=40`, `CARGO=7`; `encodeStart` majd `parse` → ugyanazok az értékek; a kódolt JSON `format_version == 1` és mind a 8 alrendszert tartalmazza.
  - `unknownHullClassFallsBack`: `{"hull_class":"battleship"}` → `RESCUE_CAPSULE`.
  - `missingSubsystemsDefaultTo100`: `{}` és `{"subsystems":{"engine":40}}` → `ENGINE` 40, többi 100.
  - `unknownSubsystemKeyIgnored`: `{"subsystems":{"warp_core":10,"engine":50}}` → `ENGINE` 50, nincs kivétel.
  - `outOfRangeValuesClamped`: `{"subsystems":{"engine":2147483647,"cargo":-5}}` → 100 és 0.
  - `newerFormatVersionLoadsDefaults`: `{"format_version":99,"hull_class":"scout","subsystems":{"engine":10}}` → alapértelmezett állapot (`RESCUE_CAPSULE`, minden 100), nem kivétel.
  - `malformedTypeIsCodecErrorNotException`: `{"subsystems":{"engine":"high"}}` → `parse(...).result()` üres, `error()` jelen van, kivétel nincs.
- [ ] **Step 2: Run** `./gradlew test --tests '*ShipStateTest' --console=plain`. Expected: FAIL (fordítási hiba).
- [ ] **Step 3: Implementáció** `ShipState.java`: `RecordCodecBuilder.create` a három nyers mezővel (`Codec.INT.optionalFieldOf`, `Codec.STRING.optionalFieldOf`, `Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf`), és egy privát, szanálást végző gyártó (nyers értékekből `ShipState`). A naplózás SLF4J (`LoggerFactory.getLogger(ModIds.MOD_ID)`). A belső tárolás `EnumMap<ShipSubsystem, Integer>`.
- [ ] **Step 4: Run** ugyanazt. Expected: PASS 9/9. Ha a `SavedData` osztály betöltése bootstrapet igényel és a teszt ezért bukik, **állj meg és kérdezz**.
- [ ] **Step 5: Commit** `git add -A && git commit -m "feat(ship): add ShipState with lenient codec"`

### Task 3: Hozzáférés, parancs és szövegek

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipStateAccess.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/ship/ShipCommands.java`
- Modify: `src/main/java/hu/kriwdi/intersolarsystem/InterSolarSystem.java` (`ShipCommands.register()` hívás az `onInitialize`-ban)
- Modify: `src/client/java/hu/kriwdi/intersolarsystem/client/datagen/ModLangProvider.java`
- Modify: `src/test/java/hu/kriwdi/intersolarsystem/LangParityTest.java`
- Regenerate: `src/main/generated/assets/intersolarsystem/lang/*.json`

**Interfaces:**
- Consumes: `ShipState` (Task 2), `ShipSubsystem.ids()/fromId`, `ShipHullClass.id()`.
- Produces: `ShipStateAccess.TYPE` (`SavedDataType<ShipState>`, azonosító `ModIds.id("ship_state")`, konstruktor `ShipState::new`, kodek `ShipState.CODEC`) és `static ShipState ShipStateAccess.get(MinecraftServer)` (`server.getDataStorage().computeIfAbsent(TYPE)`); `ShipCommands.register()` a `CommandRegistrationCallback.EVENT`-re.
- Lang kulcsok (angol szöveg; de/hu fordítás a providerben): `command.intersolarsystem.ship.info` = `Hull: %s`; `command.intersolarsystem.ship.subsystem_line` = `%s: %s%%`; `command.intersolarsystem.ship.set` = `Set %s to %s%%`; `command.intersolarsystem.ship.unknown_subsystem` = `Unknown subsystem: %s`; `ship.intersolarsystem.hull.<id>` az öt hajótest-osztályra; `ship.intersolarsystem.subsystem.<id>` a nyolc alrendszerre.

- [ ] **Step 1: API-ellenőrzés (jar, `javap`)** a megvalósítás előtt, eredmény a commit üzenetbe:
  - A `SavedDataType` negyedik paramétere (`DataFixTypes`): ellenőrizd, hogy `null` elfogadott-e (a `SavedDataStorage`/`SavedDataType` bytecode-ja alapján), különben melyik érték használható egy mod saját adatára. Ha nem egyértelmű, **állj meg és kérdezz**.
  - `CommandRegistrationCallback.EVENT` lambda-szignatúrája a `fabric-command-api-v2` jarban.
  - `SharedSuggestionProvider.suggest(Iterable<String>, SuggestionsBuilder)` létezik-e (a javaslatokhoz). Ha nem, a javaslat kimarad (ledger-ruling).
- [ ] **Step 2: Failing test** a `LangParityTest`-ben: 
  - a `requiredKeysPresentAndNonEmpty` kulcslistája kibővül az új `command.intersolarsystem.ship.*` kulcsokkal és minden `ship.intersolarsystem.hull.<id>` / `ship.intersolarsystem.subsystem.<id>` kulccsal (az id-k a `ShipHullClass`/`ShipSubsystem` enumokból).
  - új `placeholderCountsMatchAcrossLanguages`: minden kulcsra a `%s` előfordulások száma azonos en/de/hu között.
- [ ] **Step 3: Run** `./gradlew test --tests '*LangParityTest' --console=plain`. Expected: FAIL (az új kulcsok hiányoznak).
- [ ] **Step 4: Implementáció.**
  - `ShipStateAccess` a fent megadott szignatúrákkal.
  - `ShipCommands.register()`: gyökér `iss` → `ship` → `info` (kiírja a hajótest-osztályt és nyolc sor alrendszert fordítható szövegként) és `set` (`requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))`, `subsystem` szó-argumentum javaslatokkal, `value` `IntegerArgumentType.integer(0, 100)`); ismeretlen alrendszer → `command.intersolarsystem.ship.unknown_subsystem` hibaüzenet, kivétel nélkül. A `set` a `ShipState.setCondition`-t hívja.
  - `ModLangProvider`: az új kulcsok mindhárom nyelven (a provider a két kézi mezőn túl bővül; kulcsok a fenti listából, a `ShipSubsystem.ids()`/`ShipHullClass` alapján generálva, nem kézzel felsorolva).
  - `InterSolarSystem.onInitialize`: `ShipCommands.register()`.
- [ ] **Step 5: Datagen.** Run `./gradlew runDatagen --console=plain`, majd `./gradlew test --console=plain`. Expected: PASS (teljes csomag, a lang tesztek zöldek).
- [ ] **Step 6: Commit** `git add -A && git commit -m "feat(ship): add ShipStateAccess, /iss ship command and lang keys"`

### Task 4: Kézi ellenőrzés és lezárás

**Files:** Modify: `README.md` (a `/iss ship` parancsok és a mentés helye).

- [ ] **Step 1: `runServer` füstteszt.** Run: `./gradlew runServer --console=plain` (az EULA már elfogadva); a logban `Done (`, nincs `NoClassDefFoundError`.
- [ ] **Step 2: Kézi próba `runClient`-ben (a felhasználó végzi, mert én nem látom az ablakot).** Új egyjátékos világ, cheats be:
  1. `/iss ship info` → `Hull: ...` és nyolc sor, mind 100%.
  2. `/iss ship set engine 40` → `Set ... 40%`.
  3. Kilépés, a világ újratöltése, `/iss ship info` → az `engine` 40%.
  4. `/iss ship set engine 150` → a parancs elutasítja (0–100); `/iss ship set warp 10` → ismeretlen alrendszer hibaüzenet, nincs összeomlás.
  5. A játékosnál cheats nélkül (vagy OP nélkül) a `set` nem fut le, az `info` igen.
  6. Nyelvváltás (en/de/hu) után az `info` kimenete a választott nyelvű.
  7. A mentett fájlt (világ `data/` mappa, `ship_state` nevű fájl) hibás tartalomra cserélve a világ betöltődik; az eredményt (alapértelmezett állapot vagy hibaüzenet) a ledgerbe kell írni.
- [ ] **Step 3: README** bővítése a két paranccsal.
- [ ] **Step 4: Run** `./gradlew clean build --console=plain`. Expected: `BUILD SUCCESSFUL`, a tesztek száma a Task 1–3 tesztjeivel együtt zöld.
- [ ] **Step 5: Commit** `git add -A && git commit -m "docs: document /iss ship and ship state persistence"`

---

**"Kész, ha" lefedettség:** 1 → Task 4 Step 2; 2 → Task 2 (`codecRoundTrip`); 3 → Task 2 (hibatűrő tesztek) és Task 4 Step 2.7; 4 → Task 2 (`format_version`, `newerFormatVersionLoadsDefaults`); 5 → Task 3 és Task 4 Step 2; 6 → Task 4 Step 4 és a lang tesztek.
