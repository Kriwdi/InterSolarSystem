# InterSolarSystem – 1. fázis: ShipState design

Dátum: 2026-10-29
Állapot: jóváhagyásra vár

## Cél

Egy szerveroldali, tartósan mentett `ShipState`, amely a hajó adatait tárolja. Ebből épül a 2. fázis (hajó mag). A fázis célja a **mentésformátum stabilizálása**, nem a játékmenet.

## Megerősített döntések

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Hajók száma | Egy hajó világonként (a csapat közös hajója). Több hajó nincs most beépítve; a verziózott formátum később bővíthető hajólistával. |
| 2 | Tartalom | Csak adat: hajótest-osztály, nyolc alrendszer állapota (0–100), formátumverzió. Kopás, hibák, sérülésmodell nincs (2. fázis). |
| 3 | Kézi teszt | `/iss ship info` és `/iss ship set <subsystem> <value>`; a `set` csak OP-nak. |

## Architektúra

- **Tárolás:** Minecraft `SavedData` + `SavedDataType` (26.3 jarban ellenőrizve: `SavedDataType(Identifier id, Supplier<T>, Codec<T>, DataFixTypes)`, `SavedData#setDirty()`). A tároló osztály neve 26.3-ban `SavedDataStorage` (nem `DimensionDataStorage`). Egy fájl a világ adatmappájában, azonosítója `intersolarsystem:ship_state`.
- **Elvetett alternatíva:** Fabric Data Attachments. Entitáshoz vagy szinthez kötött adatra jó, itt egy szerver-szintű, hajóhoz tartozó objektumra van szükség, ezért a `SavedData` egyszerűbb.
- **Tulajdonjog:** a szerver. A kliens nem tárol és nem módosít állapotot (a parancs szerveren fut).
- **Hozzáférés:** egy `ShipStateAccess.get(MinecraftServer)` jellegű belépési pont a `ship` modulban adja vissza az egyetlen példányt (a pontos tároló-lekérés API-ját a megvalósítás elején jar-ból ellenőrizzük).

## Adatmodell (csomag: `hu.kriwdi.intersolarsystem.ship`)

- `ShipHullClass` (enum, szerializálható azonosítóval): `RESCUE_CAPSULE`, `RESEARCH_VESSEL`, `CARGO_MINER`, `SCOUT`, `MOTHERSHIP`. Alapértelmezett: `RESCUE_CAPSULE`.
- `ShipSubsystem` (enum): `ENGINE`, `FUEL_ENERGY`, `HULL_ARMOR`, `LIFE_SUPPORT`, `NAVIGATION_COMMS`, `STABILIZER`, `LANDING_LAUNCH`, `CARGO`.
- `ShipState extends SavedData`:
  - `int formatVersion` (jelenleg 1),
  - `ShipHullClass hullClass`,
  - alrendszerenként `int condition` (0–100).
  - Alapértelmezett új hajó: minden alrendszer 100.
  - Módosítás után `setDirty()`.
- Az alrendszer-állapot **mindig szűkített**: bármilyen beállított vagy betöltött érték a [0, 100] tartományba kerül.

## Mentésformátum és hibatűrés

- `Codec`-alapú; kulcsok: `format_version`, `hull_class`, `subsystems` (alrendszer-azonosító → egész).
- Ismeretlen `hull_class` → alapértelmezett osztály, figyelmeztetés a logba, a világ betöltődik.
- Hiányzó alrendszer → 100; ismeretlen alrendszer-kulcs → kihagyva, naplózva.
- Tartományon kívüli érték → szűkítés 0–100-ra.
- Újabb `format_version`, mint amit a kód ismer → a betöltés nem dob kivételt; az adat alapértelmezettre áll és naplózva van (nincs csendes felülírás-pusztítás: a naplóüzenet jelzi, hogy a fájl újabb verziójú).
- Migrációs váz: a verziószám helye és egy `migrate(version, data)` pont megvan, de v1-en nincs tényleges migráció.

## Parancs (`/iss ship`)

- `info`: kiírja a hajótest-osztályt és a nyolc alrendszer értékét (en/de/hu szöveggel, lang kulcsokkal).
- `set <subsystem> <value>`: permission level 2 (OP); `value` egész 0–100, a parancs-argumentum tartománya ezt kikényszeríti; sikerüzenet és `setDirty()`.
- A parancs a szerveren fut; az eredményt a szerver küldi.
- A `set` fejlesztői eszköz; a 2. fázisban eltűnhet vagy debug-only marad.

## Tesztelés

- **Egységtesztek (tiszta Java, Minecraft bootstrap nélkül, ahol lehet):** kodek körbejárás; alapértelmezett hajó; szűkítés (-5, 130); ismeretlen hajótest-osztály; hiányzó és ismeretlen alrendszer; újabb formátumverzió.
- **Kézi próba:** `runClient` egyjátékos világ: `/iss ship set engine 40`, kilépés, újratöltés, `/iss ship info` → 40.
- **Szerver füstteszt:** `runServer` hibamentesen indul.

## Kész, ha

1. Újratöltés után a `ShipState` értékei megegyeznek (kézi próba + kodek teszt).
2. A kodek körbejárási tesztje zöld.
3. Hibás, hiányzó vagy ismeretlen mentett adat nem omlasztja össze a világot (tesztek).
4. A mentésformátumnak verziószáma van, és a betöltés kezeli az újabb verziót.
5. `/iss ship info` és `/iss ship set` működik, a `set` csak OP-nak.
6. A teljes tesztcsomag és a `clean build` zöld, a lang kulcsok mindhárom nyelven megvannak.

## Hatókörön kívül (Ötletdoboz)

Kopás és meghibásodás, több hajó, hajó entitás vagy blokk, HUD, hálózati szinkron a kliensnek, alrendszer-szintek (fejlesztések).

## Nyitott pont

- A `SavedDataStorage` pontos lekérése a szerverről és a parancs-regisztráció API-ja 26.3-ban: a megvalósítás első lépése jar-ellenőrzés; eltérésnél megállok és kérdezek.
