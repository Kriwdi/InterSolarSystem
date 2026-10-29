# Phase 0 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Egy üres, de működő InterSolarSystem Fabric mod, ami MC 26.3-ban elindul, három nyelven jeleníti meg a teszt-blokkot és -tárgyat, és datagen-nel generál tartalmat.

**Architecture:** A hivatalos Fabric example mod sablonból indulunk (egyetlen Gradle projekt, `main` és `client` source set). A modulok (`core`, `ship`, `travel`, `space`, `worldgen`, `survival`, `content`) egyelőre csak csomagok, nem külön Gradle modulok. A tartalom-regisztráció a `content` csomagban van, az azonosító-segéd a `core`-ban.

**Tech Stack:** Java 25, Gradle (wrapper), Fabric Loom (`net.fabricmc.fabric-loom`, nem-remapelt), Fabric API, Mojang nevek, JUnit 5 (csak tiszta logikai tesztekhez).

**Spec:** `docs/superpowers/specs/2026-10-29-phase0-foundation-design.md`

## Global Constraints

- Minecraft verzió: 26.3, fejlesztés közben nem váltunk.
- Mod ID: `intersolarsystem`. Csomag: `hu.kriwdi.intersolarsystem`.
- Kód, azonosítók, kommentek angolul; magyarázat magyarul.
- Játékon belüli szövegek: `en_us`, `de_de`, `hu_hu` lang fájlok, datagen-ből.
- Kliens/szerver szétválasztás: kliens-only osztály csak a `client` source set-ben.
- API-pontosság: a Fabric docs oldalai a 26.1/26.2-t írják le. Ha egy osztály vagy metódus neve a 26.3-ban nem egyezik, **állj meg és kérdezz**, ne találj ki API-t.
- Minden task végén commit; a commit üzenet végén: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## Review Focus

- Egy lang kulcs hiányzik az egyik nyelvből: a játékban nyers kulcs jelenik meg. Teszt: Task 4 lang-paritás teszt.
- A mod dedikált szerveren összeomlik, mert kliens osztályt tölt be: Task 6 `runServer` füstteszt.
- A mod ID eltér a `fabric.mod.json`, a konstans és az asset-mappa között: Task 3 teszt.
- A datagen kimenet elavult a kézzel írt forráshoz képest: Task 5 ellenőrzés `git diff`-fel.
- Hiányzó textúra (lila-fekete kocka): Task 4 manuális ellenőrzés `runClient`-ben.

---

### Task 1: Környezet (JDK 25, szerkesztő)

**Files:** nincs (rendszerszintű).

**Interfaces:** Produces: működő `java` és `javac` 25-ös verzióval.

- [ ] **Step 1: JDK telepítése.** A felhasználó futtatja: `sudo pacman -S jdk25-openjdk`.
- [ ] **Step 2: Ellenőrzés.** Run: `java -version && javac -version`. Expected: mindkettő 25.x.
- [ ] **Step 3: VS Code bővítmények.** Extension Pack for Java telepítése; Run: `code --list-extensions | grep -i java`. Expected: legalább `redhat.java` és `vscjava.vscode-java-debug`.

### Task 2: Sablon és git

**Files:** Create: a projekt gyökerében a sablon fájljai (`build.gradle`, `gradle.properties`, `settings.gradle`, `gradlew`, `src/main`, `src/client`), `.gitignore`.

**Interfaces:** Produces: buildelhető projekt, amelyen a `./gradlew build` lefut.

- [ ] **Step 1: Sablon beszerzése** a hivatalos Fabric sablon-generátorból (`fabricmc.net/develop/template`) vagy a `FabricMC/fabric-example-mod` 26.x ágából. Beállítás: mod name `InterSolarSystem`, mod ID `intersolarsystem`, package `hu.kriwdi.intersolarsystem`, Minecraft 26.3, "split client and common sources" bekapcsolva, datagen bekapcsolva. A kicsomagolt fájlok a projekt gyökerébe kerülnek. Ha a 26.3 nem választható, állj meg és kérdezz.
- [ ] **Step 2: Verziók rögzítése.** A `gradle.properties` verzióit (Minecraft, Loom, Fabric API, loader) hagyd a sablonban, és írd le a commit üzenetben.
- [ ] **Step 3: Build.** Run: `./gradlew build`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 4: `.gitignore` ellenőrzése.** Tartalmazza: `.gradle/`, `build/`, `run/`, `.idea/`, `.vscode/` (a `.vscode/settings.json`-t ne kövesd), `*.iml`. A `src/main/generated` **nincs** kizárva.
- [ ] **Step 5: Commit.** `git add -A && git commit -m "chore: add Fabric template for MC 26.3"`

### Task 3: Csomagváz, azonosító-segéd és entrypointok

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/core/ModIds.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/{core,ship,travel,space,worldgen,survival,content}/package-info.java`
- Modify: a sablon fő mod osztálya → `src/main/java/hu/kriwdi/intersolarsystem/InterSolarSystem.java`
- Modify: `src/main/resources/fabric.mod.json`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/core/ModIdsTest.java`

**Interfaces:**
- Produces: `ModIds.MOD_ID` (`public static final String`, értéke `"intersolarsystem"`); `ModIds.id(String path)` ami a sablon `id(...)` segédjének visszatérési típusát adja vissza (a 26.x docs `ExampleMod.id(name)` mintáját követi, a típust a sablonból vedd át).

- [ ] **Step 1: JUnit 5 függőség** hozzáadása a `build.gradle`-hoz (`testImplementation` és `useJUnitPlatform()`), ha a sablonban nincs. Run: `./gradlew test`. Expected: lefut, 0 teszt.
- [ ] **Step 2: Failing test** `ModIdsTest`:
  - `modIdMatchesFabricModJson`: beolvassa a `fabric.mod.json`-t a classpath-ról, és `assertEquals("intersolarsystem", json.id)` és `assertEquals(ModIds.MOD_ID, json.id)`.
  - `assetDirectoryExists`: a classpath-on létezik az `assets/intersolarsystem/` mappa (ehhez Task 4-ben lesz tartalom; itt a teszt ideiglenesen `@Disabled`, Task 4 Step 5 bekapcsolja).
- [ ] **Step 3: Run** `./gradlew test --tests '*ModIdsTest'`. Expected: FAIL (`ModIds` nem létezik).
- [ ] **Step 4: Implementáció.** `ModIds.java` a fenti szignatúrákkal. A sablon `ExampleMod` osztályát nevezd át `InterSolarSystem`-re (`implements ModInitializer`), a `fabric.mod.json` entrypointjait és `id` mezőjét igazítsd. Minden modulcsomagba `package-info.java` egysoros Javadoc-kal a modul céljáról.
- [ ] **Step 5: Run** `./gradlew test`. Expected: PASS.
- [ ] **Step 6: Commit.** `git commit -m "feat(core): add package skeleton, ModIds and entrypoints"`

### Task 4: Teszt-blokk és -tárgy három nyelven

**Files:**
- Create: `src/main/java/hu/kriwdi/intersolarsystem/content/ModItems.java`
- Create: `src/main/java/hu/kriwdi/intersolarsystem/content/ModBlocks.java`
- Modify: `InterSolarSystem.java` (hívja `ModItems.initialize()`, `ModBlocks.initialize()`)
- Create: `src/main/resources/assets/intersolarsystem/lang/{en_us,de_de,hu_hu}.json` (Task 5 után generáltak lesznek; most kézzel, a datagen átveszi)
- Create: textúrák, modellek, `items/`, `blockstates/` a docs "first item / first block" szerkezete szerint, névterük `intersolarsystem`
- Test: `src/test/java/hu/kriwdi/intersolarsystem/LangParityTest.java`

**Interfaces:**
- Consumes: `ModIds.id(String)` Task 3-ból.
- Produces: `ModItems.SCRAP_SENSOR` (`Item`, azonosító `scrap_sensor`) és `ModBlocks.RUSTED_PANEL` (`Block`, azonosító `rusted_panel`, `BlockItem`-mel); `static void initialize()` mindkettőn.

- [ ] **Step 1: Failing test** `LangParityTest`:
  - `allLanguagesHaveSameKeys`: beolvassa a három lang JSON-t, és `assertEquals(en.keySet(), de.keySet())`, `assertEquals(en.keySet(), hu.keySet())`.
  - `requiredKeysPresent`: mindhárom fájlban van `item.intersolarsystem.scrap_sensor` és `block.intersolarsystem.rusted_panel`, nem üres értékkel.
- [ ] **Step 2: Run** `./gradlew test --tests '*LangParityTest'`. Expected: FAIL.
- [ ] **Step 3: Lang fájlok** a két kulccsal, a három nyelven (en: "Scrap Sensor"/"Rusted Panel", de: "Schrottsensor"/"Rostige Platte", hu: "Roncsérzékelő"/"Rozsdás panel").
- [ ] **Step 4: Regisztráció** a docs mintája szerint: `ResourceKey.create(Registries.ITEM, ...)`, `Registry.register(BuiltInRegistries.ITEM, ...)`, `Item.Properties#setId`, blokknál `BlockBehaviour.Properties.of()` és `BlockItem` `useBlockDescriptionPrefix()`-szel. Kreatív fülre rakás a `CreativeModeTabEvents` eseménnyel. Az osztály- és metódusneveket a 26.3-hoz ellenőrizd; eltérésnél kérdezz.
- [ ] **Step 5: Resource fájlok** (modell, `items/` definíció, blockstate, 16x16 placeholder textúrák, blokk loot table), majd a `ModIdsTest.assetDirectoryExists` `@Disabled`-jének eltávolítása. Run: `./gradlew test`. Expected: PASS.
- [ ] **Step 6: Manuális ellenőrzés.** Run: `./gradlew runClient`, kreatív módban keresd a két tárgyat; a textúrájuk látszik (nem lila-fekete), a nevük a nyelvbeállítással vált (en/de/hu).
- [ ] **Step 7: Commit.** `git commit -m "feat(content): add scrap sensor item and rusted panel block with en/de/hu lang"`

### Task 5: Datagen

**Files:**
- Modify: `build.gradle` (`fabricApi { configureDataGeneration() { client = true } }`, ha a sablon nem tartalmazza)
- Create: `src/client/java/hu/kriwdi/intersolarsystem/datagen/InterSolarSystemDataGenerator.java`
- Create: `src/client/java/hu/kriwdi/intersolarsystem/datagen/ModLangProvider.java` (en/de/hu)
- Create: `src/client/java/hu/kriwdi/intersolarsystem/datagen/ModLootTableProvider.java`
- Modify: `fabric.mod.json` (`"fabric-datagen"` entrypoint)

**Interfaces:**
- Consumes: `ModItems.SCRAP_SENSOR`, `ModBlocks.RUSTED_PANEL`.
- Produces: `src/main/generated/` tartalom: három lang fájl és a `rusted_panel` loot table.

- [ ] **Step 1: Entrypoint és providerek** a docs data-generation oldalai szerint (`DataGeneratorEntrypoint`, `FabricLanguageProvider` nyelvenként, `FabricBlockLootTableProvider`). Az API-neveket a 26.3-hoz ellenőrizd.
- [ ] **Step 2: Run** `./gradlew runDatagen`. Expected: `BUILD SUCCESSFUL`, és a `src/main/generated/assets/intersolarsystem/lang/` alatt megjelenik a három fájl.
- [ ] **Step 3: Kézi duplikátumok eltávolítása.** A Task 4 kézzel írt lang fájljait és a kézi loot table-t töröld, hogy a generált verzió legyen az egyetlen forrás. Run: `./gradlew test runDatagen`. Expected: PASS, és `git status` után egy második `runDatagen` nem változtat semmit (`git diff --stat` üres).
- [ ] **Step 4: Commit.** `git add -A && git commit -m "feat(datagen): generate lang and loot tables"`

### Task 6: Szerver füstteszt és lezárás

**Files:** Create: `README.md` (futtatási parancsok, magyarul).

- [ ] **Step 1: Dedikált szerver.** Run: `./gradlew runServer` (első indításnál a `run/eula.txt`-ben `eula=true`, a felhasználó dönt). Expected: a szerver `Done` üzenettel elindul, a logban nincs `ClassNotFoundException`/`NoClassDefFoundError` a modról. Leállítás: `stop`.
- [ ] **Step 2: README** a parancsokkal: `./gradlew runClient`, `runServer`, `runDatagen`, `test`.
- [ ] **Step 3: Végső ellenőrzés.** Run: `./gradlew clean build`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 4: Commit.** `git commit -m "docs: add README and phase 0 verification"`

---

**"Kész, ha" lefedettség:** 1 → Task 4/6 (`runClient`), 2 → Task 2, 3 → Task 4, 4 → Task 5, 5 → Task 3, 6 → Task 3 (entrypointok) és Task 6 (szerver).
