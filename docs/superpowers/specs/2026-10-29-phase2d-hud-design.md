# InterSolarSystem – 2d szelet: hajó-HUD

Dátum: 2026-10-29
Állapot: jóváhagyásra vár
Szülő: 2. fázis (hajó mag), 4. szelet. Előző: 2a, 2b, 2c. Utána: rendrakási szelet a halasztott kisebb találatokkal.

## Megerősített döntések

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Megjelenés | Kompakt panel a képernyő bal felső részén, csak a hajó dimenziójában: fázis és számláló, nyolc alrendszer-sáv (szín és százalék), incidens-sorok. Az action bar számláló megszűnik; a chat-riasztások és hangok maradnak. |
| 2 | Küldés | A hajó dimenziójában lévő játékosoknak: másodpercenként teljes pillanatkép, azonnal is változáskor, belépéskor és dimenzióváltáskor; a kliens két csomag között maga számol vissza. |
| 3 | Jelzések | Színek a meglévő küszöbökből (≥70 zöld, 20–69 borostyán, <20 piros); az incidens sora villog az utolsó 15 mp-ben; minden sávon százalék is van. |

## Cél

A hajó állapota folyamatosan látható a hajóban, a szerver adataiból. A kliens csak megjelenít: nem dönt, nem tárol tartósan.

## Szándékos egyszerűsítések

- Nincs billentyűs ki-be kapcsolás, áthelyezés vagy méretezés; a panel rögzített helyen áll.
- Nincs új mentésformátum: az incidens azonosítója az **alrendszer** (alrendszerenként legfeljebb egy incidens lehet). Ennek biztosítására az `addIncident` mostantól maga kényszeríti ki az egyediséget és a `MAX_ACTIVE` korlátot (visszatérési értéke `boolean`).
- Nincs új szöveg a HUD-hoz: a fázis számlálója a meglévő `message.intersolarsystem.ship.countdown.<fázis>` kulcsokat, a leszállt állapot a `ship.intersolarsystem.phase.landed` kulcsot, az alrendszerek és incidensek a meglévő neveket, az incidens-sorok az `alert_line` kulcsot használják. Ha mégis kell címke (pl. "Hajó"), az új lang kulcs mindhárom nyelven.

## Architektúra

- **Pillanatkép (`ShipStatusSnapshot`, tiszta Java, közös kód):** `phase`, `remainingTicks`, nyolc alrendszer-állapot (enum sorrendben), incidensek listája (`subsystem`, `type`, `remainingTicks`). `static from(ShipState, long nowTick)`. `structuralKey()`: a fázis, az állapotok és az incidensek (alrendszer, típus) időzítők nélkül, a változás-észleléshez.
- **Csomag (`ShipStatusPayload`, szerver→kliens):** a pillanatkép egy `CustomPacketPayload`-ként, `StreamCodec`-kel. Regisztráció a közös kódban (`PayloadTypeRegistry` play S2C).
- **Szerveroldali küldés (`ShipStatusSync`):** a tick-kezelő után fut. Minden tickben, ha vannak játékosok a hajó dimenziójában: ha a `structuralKey` eltér az utoljára küldöttől, vagy a játékidő 20-szal osztható, küld a dimenzió minden játékosának. Belépéskor és dimenzióváltáskor (Fabric események) azonnal küld az érintett játékosnak. A hajó dimenziójából kilépőnek nem küld.
- **Kliens (`src/client`):** egy `ClientShipStatus` tároló az utolsó pillanatképpel és a megérkezés idejével (a kliens játékidejében); egy `HudElement` a vanilla elemek után, amely csak akkor rajzol, ha van pillanatkép és a helyi játékos szintje a hajó belső dimenziója.
- **Megjelenítési modell (`ShipHudModel`, tiszta Java):** a pillanatképből és a mostani időből sorokat állít elő a rajzoláshoz: fázis-sor (kulcs és hátralévő idő), alrendszer-sorok (név-kulcs, százalék, súlyosság `SAFE`/`WARN`/`CRITICAL`), incidens-sorok (típus-kulcs, alrendszer-kulcs, idő, villog-e), a határidő szerint rendezve.
- **Szeparáció:** a pillanatkép, a csomag és a modell a közös kódban; a tároló és a rajzolás a kliensben. A szerver kódja nem hivatkozik kliens osztályra.

## Küszöbök és idő

- Súlyosság: `SAFE` ha az állapot ≥ `ShipIncidentRules.SAFE_CONDITION` (70); `CRITICAL` ha < `ShipFlightRules.MIN_TAKEOFF_CONDITION` (20); különben `WARN`. A kliens ugyanezeket a konstansokat használja.
- Villogás: az incidens-sor az utolsó 300 tickben (15 mp) villog (a játékidőből képzett váltakozás).
- Visszaszámlálás a kliensen: `max(0, csomagbeli hátralévő − (kliens most − megérkezés))`.

## Tesztelés

- **Egységtesztek (tiszta Java):** `ShipStatusSnapshot.from` (leszállt, repülés közben, incidensekkel); `structuralKey` (az időzítők változása nem változtatja, az állapot/incidens/fázis igen); a csomag kódolása és visszaolvasása `FriendlyByteBuf`-fal, ha bootstrap nélkül futtatható (különben a tiszta mezők kódolási sorrendjének tesztje); `ShipHudModel` (súlyossági küszöbök 70/69/20/19, villogás a 300 tick határán, rendezés határidő szerint, nincs incidens-sor ha nincs incidens, visszaszámlálás a megérkezés óta eltelt idővel és nullára szűkítése); `ShipState.addIncident` egyedisége és `MAX_ACTIVE` (a harmadik, illetve az azonos alrendszerű elutasítva, csak olvasható állapotban sem fogad el).
- **Kézi próba (`runClient`):** a panel csak a hajóban látszik és eltűnik kilépéskor; a számláló simán fogy; a sávok színe és százaléka az `/iss ship set` értékeivel egyezik; az incidens-sor megjelenik és villog az utolsó 15 mp-ben; megoldás után eltűnik; kilépés/újratöltés repülés közben azonnal mutatja az állapotot; második játékos (LAN) is látja; F1 elrejti; nyelvváltás.
- **Szerver füstteszt:** `runServer` (a küldés játékos nélkül nem fut), `Done`, nincs hiba.

## Kész, ha

1. A hajó belsejében a HUD mutatja a fázist és a számlálót, az alrendszerek állapotát, és az aktív incidenseket határidővel.
2. A kijelző a szerver adatait tükrözi, a kliens nem dönt, a változás azonnal, az idő másodpercenként frissül és két csomag között simán fogy.
3. Az adat csak a hajó dimenziójában lévőknek megy; belépéskor és dimenzióváltáskor azonnal megérkezik.
4. A pillanatkép, a változás-kulcs, a HUD-modell, a csomag kódolása és az `addIncident` korlátai tiszta Java tesztekkel védettek; `clean build` zöld; a lang tesztek zöldek.
5. Az action bar számláló megszűnt; az incidens-azonosítás az alrendszeren alapul, nincs új mentésformátum.

## Hatókörön kívül (Ötletdoboz)

Billentyűs kapcsoló, áthelyezhető vagy méretezhető panel, animált sávok, hangjelzés a HUD-ról, térkép vagy navigációs kijelző, a rendrakási szelet tételei (javítóállomás csak a hajóban, a nem-készlet tárgyak az állomáson stb.).

## Nyitott pontok (megvalósításkor jar-ellenőrzés, eltérésnél kérdezés)

A Fabric API jarokban megvannak a `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`, `HudElementRegistry`, `HudElement` osztályok; ellenőrizendő:
- a `CustomPacketPayload` és `StreamCodec` 26.3-as felépítése, a play S2C regisztrálás pontos módja;
- a `HudElement#render` szignatúrája és a vanilla elemek utáni regisztráció módja;
- a belépés/dimenzióváltás Fabric eseményei (`ServerPlayConnectionEvents`, `ServerEntityWorldChangeEvents`);
- a rajzolás eszközei (`GuiGraphics` szöveg- és téglalaprajzoló metódusai, színformátum);
- a kliens szintjének dimenziója (`Minecraft#level`) és a kliens játékideje.
