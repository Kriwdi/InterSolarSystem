# InterSolarSystem – 2c szelet: kopás, incidensek, javítás

Dátum: 2026-10-29
Állapot: jóváhagyásra vár
Szülő: 2. fázis (hajó mag), 3. szelet. Előző: 2a, 2b. Következő: 2d HUD. A minigame-ek és a robbanással járó tesztfelszállás a 3. fázisba tartoznak.

## Megerősített döntések

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Kopás és hiba | Kopás fázisonként egy táblázat szerint; hibadobás repülés közben 10 mp-enként, az esély 70% fölött 0, lefelé lineárisan 25%-ig; absztrakt incidens 60 mp határidővel, lejárva −25 az alrendszerre; felszállási kapu: hajtómű vagy leszállóegység < 20% esetén nincs felszállás. Konstansok kódban. |
| 2 | Javítás | `repair_kit` fogyóeszköz és `repair_station` blokk a belső térben; a készlet az aktív incidenst oldja meg, incidens nélkül a legrosszabb alrendszerre +25. Minigame a 3. fázisban. |
| 3 | Kimenetel védelme | Repülésenként mentett véletlen mag; a dobások a magból és a dobás sorszámából számolódnak, az aktív incidensek mentettek. Mentésformátum v4. |

## Cél

A hajó használata kockázatos: a repülés elhasználja az alrendszereket, rossz állapotban incidensek keletkeznek, amiket javítani kell, és a leromlott hajó nem szállhat fel. Ez a 2. pillér (a hajó nem egy gomb) első működő változata.

## Szándékos egyszerűsítések

- Az incidens **absztrakt**: nincs tűz-blokk, nyomásvesztés-hatás vagy entitás. A típus csak a megjelenítést és a későbbi hatásokat jelöli.
- Egy javítókészlet egy műveletet old meg; nincs recept, nincs zsákmány-tábla (3. pillér később), a készlet kreatív fülön és paranccsal szerezhető.
- Nincs HUD (2d), nincs minigame, nincs katasztrofális kimenet (robbanás, összeomlás): 3. fázis.
- Nincs adatvezérelt (JSON) balansz; a konstansok egy helyen vannak, hogy később kiemelhetők legyenek.

## Mentésformátum v4

- `ShipState` bővítés: egy új, beágyazott `hazards` mező (megengedő, opcionális): `seed` (long), `rolls_done` (int), `incidents` (lista).
- Egy incidens: `subsystem` (azonosító), `type` (azonosító), `deadline_tick` (long, az Overworld játékidejében).
- Hibás vagy ismeretlen `hazards` → üres alapállapot + WARN; ismeretlen alrendszer vagy típus egy incidensben → az incidens kimarad + WARN. A v3→v4 migráció identitás-lépés.
- A mező egybe fogja a három új értéket, hogy a nyers kodek a 16-os mezőhatár alatt maradjon (a v3-ban 12 mező van, a v4-ben 13).

## Kopás

A kopás fázisváltáskor egyszer fut le, csökkenti az érintett alrendszereket (0-ra szűkítve). Táblázat (kódban, egy helyen):

| Átmenet | Csökkenés |
|---|---|
| `TAKEOFF → FLIGHT` | Hajtómű −4, Leszálló- és kilövőegység −4, Üzemanyag/energia −3 |
| `FLIGHT → LANDING` | Hajtómű −4, Üzemanyag/energia −6, Stabilizáló −3, Burkolat/páncél −2, Navigáció −2, Életfenntartás −2 |
| `LANDING → LANDED` | Leszálló- és kilövőegység −5, Stabilizáló −3, Burkolat/páncél −2 |

Egy teljes repülés így kb. −8 a hajtóművön; 100%-ról kb. tíz repülés után esik a küszöb alá.

## Hibadobás és incidens

- **Dobás:** a `FLIGHT` fázisban 200 tickenként (a fázis kezdetétől: 200, 400, 600, 800, 1000; öt dobás). A `rolls_done` mutatja, hány dobás futott le; kihagyott dobások pótlódnak, kétszer nem futnak le.
- **Véletlen:** a dobás sorszámából és a mentett magból számolt, determinisztikus generátor (újratöltés nem változtat a kimeneten).
- **Esély alrendszerenként:** 70% vagy fölötte 0; alatta `0,25 × (70 − állapot) / 70`. Egy dobásnál az alrendszerek sorban kumulatív esélyt kapnak (enum sorrend), egyetlen szám dönt; így a rosszabb alrendszer valószínűbb, és egy dobás legfeljebb egy incidenst hoz.
- **Korlátok:** alrendszerenként legfeljebb egy aktív incidens; összesen legfeljebb kettő.
- **Típus az alrendszerből:** Hajtómű → túlmelegedés; Üzemanyag/energia → szivárgás; Burkolat/páncél és Életfenntartás → nyomásvesztés; Navigáció és Stabilizáló → rövidzárlat; Leszálló- és kilövőegység, Raktér → tűz.
- **Határidő:** `deadline = most + 1200` tick (60 mp). Lejáratkor az alrendszer −25-öt kap, az incidens megszűnik; a játékosoknak üzenet. A határidő a repülés végét is túlélheti, és leszállt állapotban is lejár.
- **Megoldás:** javítókészlettel az állomáson (lent).

## Felszállási kapu

`/iss ship fly` elutasít (új üzenet), ha a hajtómű vagy a leszállóegység állapota **< 20**. Tiszta függvény: `ShipFlightRules.canTakeOff(engine, landingLaunch)`.

## Javítás

- **Blokk `repair_station`:** a belső tér egy meghatározott cellájában áll (az elrendezésben új darab-típus), a hajótest védelmét örökli (belül törhetetlen, dugattyú nem mozgatja). Már megépült szobákban a beszállás pótolja, ha a cellája levegő (nem írja felül, amit a játékos oda tett: csak levegőt cserél).
- **Tárgy `repair_kit`:** fogyóeszköz. Jobb klikk a készlettel az állomáson, szerveroldalon:
  1. ha van aktív incidens: a legkorábbi határidejűt oldja meg, a készlet fogy;
  2. különben ha van 100% alatti alrendszer: a legrosszabb +25-öt kap, a készlet fogy;
  3. különben "nincs mit javítani", a készlet nem fogy.
  Kreatív módban a készlet nem fogy (a vanilla fogyasztási szabály szerint).
- Készlet nélkül az állomás üzenetet ad (mire való, mi a legrosszabb alrendszer).

## Megjelenítés

- Incidens keletkezésekor: üzenet és hang a belső dimenzió játékosainak.
- Az action bar a repülés/incidens állapotot együtt mutatja: a fázis számlálója, mellette az aktív incidensek típussal, alrendszerrel és hátralévő idővel. A HUD (2d) később leváltja.
- `/iss ship info` kiegészül az aktív incidensekkel (típus, alrendszer, hátralévő idő).

## Felosztás és sorrend (ha a szelet túl nagynak bizonyul)

1. Kopás és felszállási kapu + v4 formátum (a szelet önmagában is játszható és ellenőrizhető).
2. Incidensek (dobás, határidő, megjelenítés).
3. Javítás (állomás, készlet).
A sorrend lehetővé teszi, hogy az 1. és 2. lépés után megállva a szelet értelmes legyen.

## Tesztelés

- **Egységtesztek (tiszta Java):** a kopás táblázata (átmenetenkénti változások, szűkítés 0-ra); az esély-függvény (70% fölött 0, 0%-nál 0,25, lineáris); a dobás (determinizmus ugyanarra a magra és sorszámra, különböző sorszámokra eltérhet, kizárt alrendszerek, a két-incidens korlát, az egyetlen incidens egy dobásra); a típus-hozzárendelés minden alrendszerre; a felszállási kapu határértékei (19, 20); `ShipState` v4: a hazards körbejárása, hibás mezők (nem végzetes), v3→v4 betöltés, az incidens hozzáadása/megoldása/lejárata és a dirty jelző, csak olvasható állapot; a `rolls_done` pótlás és a kétszeri futás kizárása; az elrendezés új darabja (pontosan egy állomás, belül, a belépési pont szabad).
- **Kézi próba (`runClient`):** több repülés egymás után (kopás látszik), rossz állapotra állítva (`/iss ship set`) incidens keletkezik és a határidő lejár (büntetés); a készlettel az állomáson megoldható; a kapu elutasít 20% alatt; újratöltés közben ugyanaz a kimenet; régi szoba megkapja az állomást.
- **Szerver füstteszt:** `runServer`, `Done`, nincs hiba.

## Kész, ha

1. Egy repülés után az alrendszerek állapota a táblázat szerint csökkent, mentve.
2. Rossz állapotnál repülés közben véletlen incidens keletkezik, látható és mentett; újratöltés nem változtatja a kimenetet.
3. Az incidens készlettel megoldható; lejárva extra sérülést okoz.
4. 20% alatt a hajtómű vagy a leszállóegység megakadályozza a felszállást.
5. A tiszta logika tesztjei zöldek; a `clean build`, a lang és a mentési tesztek zöldek; a szöveg en/de/hu.

## Hatókörön kívül (Ötletdoboz)

Minigame-ek, tesztfelszállás és katasztrofális kimenet, tűz/nyomásvesztés tényleges hatása a világra, javítókészlet receptje és zsákmánya, adatvezérelt balansz, HUD (2d), megszakítás/vészleszállás parancs, alrendszer-szintek (fejlesztések).

## Nyitott pontok (megvalósításkor jar-ellenőrzés, eltérésnél kérdezés)

- A `useItemOn` visszatérési értékei és a készlet fogyasztása (`ItemStack#consume`) 26.3-ban.
- Egyedi tárgy regisztrálása stack-mérettel és kreatív fülre rakása.
- A játékosok hangja és a `Component` összefűzése az action barhoz.
