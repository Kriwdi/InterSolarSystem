# InterSolarSystem – 2b szelet: felszállás, repülés, leszállás

Dátum: 2026-10-29
Állapot: jóváhagyásra vár
Szülő: 2. fázis (hajó mag), 2. szelet. Előző: 2a (belső tér). Következő: 2c kopás és hibák, 2d HUD.

## Megerősített döntések

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Irányítás és cél | Fejlesztői parancs: `/iss ship fly <x> <z>` (OP, csak a hajó belsejéből). A navigációs konzol blokk és felület később jön (navigációs alrendszer). |
| 2 | Külső nyílás és pozíció | A nyílás a hajó **ajtaja**; a hajónak hiteles pozíciója van a mentésben. Felszálláskor a nyílás eltűnik, leszálláskor a szerver új nyílást helyez a cél felszínére. Más nyílás csak újra-dokkoláshoz köt. |
| 3 | Élmény és időtartam | Fix, tickben mért fázisok (10 mp felszállás, 60 mp repülés, 5 mp leszállás), action bar számláló, hangok; a játékosok a hajóban maradnak. |

## Cél

A hajó végigmegy az `LANDED → TAKEOFF → FLIGHT → LANDING → LANDED` állapotgépen egy Overworld-célra. Az állapot tartósan mentett, a hajó pozíciója hiteles, repülés közben nem lehet be- vagy kiszállni.

## Szándékos egyszerűsítések

- Csak az Overworld a cél (a Hold a 6. fázisban jön). A repülés időtartama fix konstans; a valós útszámító (4. fázis) később ezt a képletet cseréli.
- Nincs külső hajómodell vagy animáció, nincs kopás/hiba a repülésben (2c), nincs HUD (2d), nincs kamerarázkódás.
- A leszálláskor a nyílást a szerver a felszínre helyezi; ha a cél környékén 32 blokkon belül nincs száraz hely, a világspawn felszínére szállunk le (üzenettel).

## Állapotmodell (mentésformátum v3)

- `ShipState` bővítés: `phase` (`landed`/`takeoff`/`flight`/`landing`, alapértelmezett `landed`), `phase_started_tick` (hosszú egész, az Overworld játékideje), `destination` (x, z; csak repülés alatt).
- A `landing_site` jelentése: **a hajó hiteles pozíciója**, azaz az utolsó leszállt nyílás helye.
- Új, megengedő (lenient) opcionális mezők; hibás vagy ismeretlen `phase` → `landed` + WARN. A v2→v3 migráció identitás-lépés (az új mezők opcionálisak), a `format_version` 3.
- Az idő forrása az Overworld játékideje (`getGameTime`). A szerver szüneteltetésekor (üres szerver) az idő megáll, így a 4. nyitott döntés (üres szerveren áll az idő) automatikusan teljesül; újratöltés után a repülés ugyanonnan folytatódik.

## Fázisok és átmenetek

Időtartamok (tick): `TAKEOFF = 200`, `FLIGHT = 1200`, `LANDING = 100`.

| Átmenet | Mikor | Mellékhatás |
|---|---|---|
| `LANDED → TAKEOFF` | `/iss ship fly` érvényes feltételekkel | cél és kezdő tick mentése |
| `TAKEOFF → FLIGHT` | 200 tick után | a külső nyílás blokkja eltűnik (ha még a hajó nyílása); hang |
| `FLIGHT → LANDING` | 1200 tick után | hang |
| `LANDING → LANDED` | 100 tick után | leszállóhely keresése, új nyílás lehelyezése, `landing_site` frissítése; hang és üzenet |

- **Parancs feltételei:** a küldő játékos a belső dimenzióban van; a fázis `LANDED`; a mentés nem csak olvasható; a cél a világhatáron belül van. Hibánál lang-kulcsos üzenet, állapotváltozás nélkül.
- **Játékosok:** a hajó belsejében lévők a hajóban maradnak. Aki felszálláskor kívül van, kint marad (a nyílás eltűnik).
- **Kijelzés:** másodpercenként action bar a belső dimenzió játékosainak (pl. "Felszállás: 8 mp", "Érkezés: 0:42", "Leszállás: 3 mp"). Fázisváltáskor egy vanilla hang.

## Nyílás- és kiszállási szabályok (a 2a I4 megoldása)

- **Belső nyílás (kiszállás):** csak `LANDED` fázisban működik; egyébként üzenet ("a hajó nem tud kiszállni most").
- **Külső nyílás (beszállás):** a fázis `LANDED`, és
  - ha a hajónak még nincs pozíciója (első használat): a nyílás pozíciója lesz a hajó pozíciója (mint 2a-ban);
  - ha a nyílás a hajó pozíciója: beszállás;
  - ha a hajó pozíciójának nyílás-blokkja hiányzik (elpusztult): az új nyílás újra-dokkol, a pozíció átkerül ide;
  - egyébként "ez a nyílás nincs a hajóhoz kötve", beszállás nélkül.
- Nem `LANDED` fázisban a külső nyílás: "a hajó távol van / felszállóban".
- Tiszta döntési függvény (`ShipHatchRules`): bemenet a fázis, hogy van-e pozíció, egyezik-e a nyílás a pozícióval, megvan-e a pozíció nyílás-blokkja; kimenet: `BOARD`, `REGISTER`, `REDOCK`, `NOT_CONNECTED`, `BUSY`.

## Leszállóhely keresése

- `ShipLandingSearch.candidates(x, z, radius)`: a cél körüli (x, z) oszlopok spirál sorrendben, a középponttól kifelé, egyediek, a sugáron belül.
- Egy oszlop alkalmas, ha a felszíni cella szabad, alatta tömör, nem folyadék talaj van, van legalább egy szomszédos álló hely (a 2a `findStandingSpot`-ja), és a nyílás cellája nem foglalt. Az első alkalmas oszlop nyer; ha 32 blokkon belül nincs, a világspawn felszíne következik ugyanezzel a kereséssel.

## Parancsok

- `/iss ship fly <x> <z>`: OP (level 2), csak a belső dimenzióból.
- `/iss ship info`: kiegészül a fázissal, a hátralévő idővel és a céllal.

## Tesztelés

- **Egységtesztek (tiszta Java):** az időzítés (`ShipFlightTimeline`: lépés a fázishatáron, hátralévő tick, negatív eltelt idő kezelése, formázás); a v2→v3 migráció és az új mezők körbejárása, hibás típusok kezelése; a `ShipState` átmenetek előfeltételei (csak `LANDED`-ből indul, csak olvasható állapotban nem); a `ShipHatchRules` mind az öt kimenete; a `ShipLandingSearch` (spirál sorrend, egyediség, sugár).
- **Kézi próba (`runClient`):** a teljes sorrend egy koordinátára; kilépés/újratöltés repülés közben (folytatódik); beszállás/kiszállás tiltva repülés alatt; idegen nyílás nem köt; a nyílás kiütése után újra-dokkolás; második játékos a hajóban.
- **Szerver füstteszt:** `runServer`, `Done`, nincs hiba.

## Kész, ha

1. A felszállás–repülés–leszállás sorrend végigmegy; leszálláskor új nyílás áll a cél felszínén, és a kiszállás oda visz.
2. Repülés közben nem lehet be- vagy kiszállni, és idegen nyílás nem horgonyozza át a hajót.
3. A repülés túléli a mentést és az újraindítást (ugyanonnan folytatódik).
4. Az időzítés, a nyílás-szabályok, a v3 mentés és a leszállóhely-keresés tesztjei zöldek; `clean build` és a lang tesztek zöldek.
5. Több játékos a hajóban együtt repül (kézi próba).

## Hatókörön kívül (Ötletdoboz)

Konzol blokk és felület, valós útszámítás és más célok, külső modell és animáció, kamerarázkódás, repülés közbeni események és hibák (2c), HUD (2d), a kisebb 2a-találatok (láva a nyílás mellett stb.), ha nem érintik a leszállóhely-keresést.

## Nyitott pontok (megvalósításkor jar-ellenőrzés, eltérésnél kérdezés)

- Szerver tick esemény (Fabric) és az Overworld játékidejének lekérdezése 26.3-ban.
- Felszín-magasság lekérdezése egy oszlopra (heightmap) és a folyadék-ellenőrzés.
- Világhatár-ellenőrzés a parancsban.
- Hang lejátszása és játékosok listája egy szinten.
