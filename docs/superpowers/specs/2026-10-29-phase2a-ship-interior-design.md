# InterSolarSystem – 2a szelet: hajó belső tér és beszállás

Dátum: 2026-10-29
Állapot: jóváhagyásra vár
Szülő: 2. fázis (hajó mag), 1. szelet. Következő szeletek: 2b repülés, 2c kopás és hibák, 2d HUD.

## Megerősített döntések

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Ábrázolás | A belső tér egy **saját dimenzió** (`intersolarsystem:ship_interior`), a külső egy leszállóhely a világban. Nincs mozgó blokkszerkezet. |
| 2 | "Repülés" a 2. fázisban | Időzített, szerveroldali állapot a belső dimenzióban, cél az Overworld egy koordinátája (2b szelet). |
| 3 | Első szelet | A `RESCUE_CAPSULE` belső tere és a be-/kiszállás (ez a dokumentum). |

## Cél

Egy játékos (vagy több) be tud szállni a hajóba a világban álló nyílással, a hajó belső terébe kerül, és onnan vissza tud térni. A belső tér és a leszállóhely tartósan mentett.

## Szándékos egyszerűsítések (a szelet döntései)

- **Egy nyílás-blokk, két szerep:** `ship_hatch`. Az interior dimenzión kívül használva beszállít, az interior dimenzióban használva kiszállít. Nincs külön blokk a két irányra.
- **A leszállóhely a nyílás:** az utoljára használt külső nyílás pozíciója a hajó `landing_site`-ja. Kiszálláskor a játékos ide kerül (a nyílás mellé). Ha a nyílás eltűnt vagy a hely nem biztonságos, tartalék: az Overworld világspawnja.
- **Belső tér procedurálisan épül**, nem NBT sablonból: egy lezárt szoba a `rusted_panel` blokkokból, világítással és egy belső nyílással. Az NBT sablonok a további hajótest-osztályokhoz (később) jönnek.
- **Egy hajó, egy belső tér** világonként; több játékos ugyanazt a teret használja.
- **Nincs** külső hajómodell, repülés, kopás, életfenntartás, HUD, más hajótest-osztály.

## Architektúra

- **Dimenzió:** adatvezérelt (datapack JSON): `dimension_type` + `dimension` (`data/intersolarsystem/dimension_type/ship_interior.json`, `data/intersolarsystem/dimension/ship_interior.json`). Üres (void) generátor, fix világosság, nincs időjárás, nincs égbolt-ciklus. A JSON mezők 26.3-as sémáját a megvalósítás elején jar-ból/vanilla adatból ellenőrizzük.
- **`ShipState` bővítés (formátum v2):** `landing_site` (opcionális: dimenzió-azonosító + x, y, z) és `interior_built` (bool). A v1 mentés v2-re migrálódik (`landing_site` nincs, `interior_built` hamis).
- **Migrációs váz (halasztott tétel):** a betöltés először nyers `Dynamic`-ot dekódol, a `format_version` alapján sorban lefuttatja a migrációkat (`migrate(from, data)` lépések), majd típusosan dekódol. A v1→v2 lépés az első valódi migráció. Az újabb formátumú mentés továbbra is csak olvasható.
- **Szoba-elrendezés (tiszta Java):** `ShipInteriorLayout` a szoba blokkjait relatív pozíciókkal adja (7×4×5 belső tér, falak/padló/mennyezet `rusted_panel`, világítás, egy `ship_hatch`), valamint a játékos belépési pontját. Ez a Minecraft nélkül tesztelhető; a `ShipInteriorBuilder` a szerveren ezt helyezi le.
- **Beszállás/kiszállás (`ShipBoarding`):** szerveroldali; a `ship_hatch` használatából hívódik. Beszálláskor: `landing_site` mentése, belső tér felépítése (ha még nincs), a játékos átteleportálása a belépési pontra. Kiszálláskor: teleport a `landing_site` mellé vagy a tartalék helyre.
- **Szöveg:** lang kulcsok (datagen) a nyílás nevére és az üzenetekre (en/de/hu), pl. beszálláskor rövid üzenet, tartalék kiszállásnál figyelmeztetés.

## Mentés és hibatűrés

- `landing_site` hibás vagy ismeretlen dimenzió → törölve, WARN; kiszállás a tartalék helyre.
- `interior_built` igaz, de a szoba hiányzik (pl. törölt chunk) → a beszállás újraépíti a szobát (az épület idempotens; felülírja a saját blokkjait).
- Újabb formátum → csak olvasható, a beszállás ilyenkor is működik, de nem ír (nem módosítja a `landing_site`-ot).

## Tesztelés

- **Egységtesztek (Minecraft bootstrap nélkül):**
  - migráció: v1 JSON → v2 állapot (`landing_site` üres, `interior_built` hamis); v2 körbejárás; újabb verzió csak olvasható marad; hibás `landing_site` nem dönti el a betöltést.
  - `ShipInteriorLayout`: pontosan egy `ship_hatch`; a belső tér körül zárt (nincs nyitott oldal); a belépési pont a szobán belül van és szabad hely; a méretek a megadottak.
- **Kézi próba (`runClient`):** nyílás lerakása (kreatív), jobb klikk → a szobában vagy; belső nyílás → vissza a külsőhöz; kilépés–újratöltés után is működik; a nyílás kiütése után kiszállás a világspawnra; második játékos (LAN) beszállhat.
- **Szerver füstteszt:** `runServer` hibamentes; a dimenzió betöltődik.

## Kész, ha

1. A lerakott `ship_hatch` használata a lezárt, megvilágított szobába visz; a belső nyílás visszavisz a külsőhöz.
2. A `landing_site` és a szoba túléli a kilépést és az újraindítást.
3. Egy v1 mentés v2-re migrálódik (teszt), és a migrációs váz a nyers adaton dolgozik.
4. A `ShipInteriorLayout` tesztjei zöldek; a `clean build` és a lang tesztek zöldek.
5. Egy második játékos is beszállhat (kézi próba).

## Hatókörön kívül (Ötletdoboz)

Repülés és felszállás (2b), kopás és hibák (2c), HUD (2d), külső hajómodell vagy animáció, NBT sablonok és más hajótest-osztályok, életfenntartás a belső térben, a sérült `ship_state.dat` biztonsági mentése (külön apró tétel, ha kell).

## Nyitott pontok (megvalósításkor jar-ellenőrzés, eltérésnél kérdezés)

- A `dimension_type`/`dimension` JSON mezői 26.3-ban.
- A teleport API (`ServerPlayer` más dimenzióba), és a célchunk betöltése a szoba építéséhez.
- A blokk-használat felülírandó metódusa 26.3-ban (jobb klikk a `ship_hatch`-en).
- A `Dynamic`-alapú dekódolás a `SavedDataType`-ban használt `Codec` mellett (a migráció a kodek `Dynamic`-ján fut).
