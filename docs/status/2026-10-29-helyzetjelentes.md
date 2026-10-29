# InterSolarSystem – helyzetjelentés

Dátum: 2026-10-29
Állapot: a 0., az 1. és a 2. fázis (2a–2d) kész és a `master` ágon van.
Célja: a projekt átadása egy kollégának. Ez a dokumentum az aktuális állapotot adja; a projekt teljes leírása a `docs/project-brief.md`-ben, az ütemterv a `docs/roadmap.md`-ben, a részletek a specekben és tervekben vannak.

## 1. Röviden

Az InterSolarSystem egy Fabric alapú, posztapokaliptikus űrhajós túlélőmod Minecraft 26.3-ra. A játékos egy halott Földön kimentett technológiából hajót tart fenn, és kockázatos utakon viszi bolygóról bolygóra, a végcél a Proxima-rendszer.

Ma a **hajó magja** működik, vanilla világban: be lehet szállni a hajóba, a hajó felszáll, repül, leszáll, közben elhasználódik, véletlen hibák (incidensek) keletkeznek, ezeket javítani lehet, és a hajó állapota egy HUD-on látható. Minden szerveroldali, mentett, és a tiszta logikát 165 egységteszt védi.

Még **nincs**: Föld világgenerálás, más égitestek, utazási időkonverzió és útszámító, minigame-ek, tesztfelszállás robbanással, több hajótest-osztály, receptek és zsákmány.

## 2. A projekt

- **Név, azonosítók:** mod ID `intersolarsystem` (nem szabad változtatni: a mentésekben és a regisztrált azonosítókban szerepel); Java csomag és Gradle group `hu.kriwdi.intersolarsystem` / `hu.kriwdi`.
- **Platform:** Minecraft 26.3, Fabric (loader 0.20.1, Fabric API 0.165.0+26.3, Loom 1.19-SNAPSHOT, nem-remapelt, Mojang nevek), Java 25.
- **Nyelvek:** a kód, az azonosítók és a kommentek angolul; a játékon belüli szöveg angolul, németül és magyarul (datagenből); a dokumentáció magyarul.
- **Tervezési pillérek** (a teljes szöveg a projektleírásban): mindig legyen mit csinálni; a hajó kockázat, nem egy gomb; a technológiát kimenteni kell, nem feltalálni; lépcsőzetes haladás; minden bolygó önálló identitás; mély, de véges fejlesztések; hangulat előbb, tartalom utána.
- **Licenc:** jelenleg a Fabric sablon CC0-ja; a tulajdonos dönt, marad-e.

## 3. Rögzített döntések

| Téma | Döntés |
|---|---|
| Hajóépítés | Hibrid: előre tervezett hajótest-osztályok, bennük bővíthető belső tér és modulok (nem szabad blokképítésű mozgó hajó). |
| Szóló és multi | Szerver-hiteles architektúra az elejétől; szóló és LAN előbb, a többjátékos finomhangolás később. |
| Főcél | Eljutni a Proxima-rendszerbe és benépesíteni; apró, opcionális lore-morzsák a kihalás okáról. |
| Kilépés utazás közben (MP) | A játékos a hajóba tér vissza; az idő telik, amíg legalább egy játékos online van, egyébként megáll. |
| Időkonverzió | 10x gyorsítás (1 valós nap = 2,4 MC óra); az utazási idő a valós úthosszból, a hajtómű-szorzóból számolódik (a 4. fázisban épül). |
| Haladás | Föld, orbit/ISS, Hold, Mars, aszteroidaöv, Jupiter, Szaturnusz, külső bolygók; minden bolygó külön dimenzió. |
| Kliens/szerver | Minden játékállapot szerveroldali, a kliens csak megjelenít; a minigame-ek eredménye szerveren validált. |

## 4. Fázisok állapota

| Fázis | Tartalom | Állapot |
|---|---|---|
| 0. Előkészítés | repó, Fabric környezet, datagen alap | **kész** |
| 1. Alaprendszerek | `ShipState` és a mentésformátum | **részben kész**: a mentés kész; a tartalom-váz (receptek, loot table-ök, tag-ek) még nincs |
| 2. A hajó magja | belső tér, repülés, kopás és hibák, HUD | **kész** (2a–2d); egy opcionális rendrakási szelet maradt |
| 3. Tesztfelszállás, hibák, minigame-ek | | nincs elkezdve |
| 4. Időrendszer és utazás | | nincs elkezdve (a 2b-ben fix időtartamok vannak) |
| 5–12. Orbit/ISS, Hold, Föld-worldgen, Mars, külső rendszer, multiplayer finomítás, kiadás | | nincs elkezdve |

A 2. fázis "kész, ha" feltétele (a hajó vanilla világban felszáll, repül, leszáll, és a meghibásodás működik) teljesül. A tervből még nincs meg: több hajótest-osztály és bővíthető belső tér (csak a mentőkapszula van, fix szobával).

## 5. Mi működik most

**Játékos szemszögből:**
- A `ship_hatch` blokkal a hajóba lehet szállni (jobb klikk). A belső tér egy saját dimenzió (`intersolarsystem:ship_interior`), egy lezárt kapszulaszoba, amelynek falai és nyílása belülről nem törhetők.
- A hajó a nyílás mellett áll a világban. A nyílás a hajó ajtaja: felszálláskor eltűnik, leszálláskor a szerver újat tesz a cél felszínére.
- Repülés közben senki nem szállhat be vagy ki. Idegen nyílás nem horgonyozza át a hajót.
- A repülés elhasználja az alrendszereket (hajtómű kb. −8 repülésenként); 20% alatt a hajtómű vagy a leszállóegység megakadályozza a felszállást.
- Repülés közben, rossz állapotnál, véletlen incidensek lépnek fel (a rosszabb alrendszer valószínűbb). Az incidensnek 60 másodperces határideje van; javítókészlettel az állomáson megoldható, különben −25 az alrendszerre.
- A hajóban egy HUD mutatja a fázist, a számlálót, az alrendszerek állapotát (zöld/borostyán/piros) és az incidenseket (az utolsó 15 mp-ben villognak).

**Parancsok** (`/iss ship ...`): `info` (mindenki); `set <alrendszer> <0–100>` és `fly <x> <z>` (OP; a `fly` csak a hajó belsejéből, és csak az Overworldbe).
**Tárgyak/blokkok:** `ship_hatch`, `repair_station`, `repair_kit`, `rusted_panel`, `scrap_sensor`. A javítókészlet jelenleg csak a kreatív fülről vagy `/give @s intersolarsystem:repair_kit` paranccsal szerezhető.

## 6. Architektúra

**Modulok (Java csomagok, egy Gradle projekt):** `core` (azonosítók), `ship` (a hajó teljes logikája), `content` (blokkok, tárgyak), plusz üres váz a `travel`, `space`, `worldgen`, `survival` csomagoknak. Source set-ek: `src/main` (közös/szerver), `src/client` (kliens és datagen), `src/test`.

**Elvek:**
- **Tiszta mag, vékony szerveroldali héj.** A döntések (időzítés, valószínűségek, kopás, szabályok, megjelenítési modell) Minecraft nélkül tesztelhető Java osztályokban vannak; a világot érintő kód (teleport, blokkok, tick) vékony kötőréteg.
- **A szerver az egyetlen igazság.** A kliens egy pillanatképet kap és kirajzol; nem dönt, nem ment.
- **Adatvezérelt, ahol lehet:** a szövegek datagenből (`./gradlew runDatagen`, a kimenet a `src/main/generated` mappában, verziókövetve); a hajó-dimenzió datapack JSON.

**Mentés:** a világ `data/intersolarsystem/ship_state.dat` fájlja (`SavedData`), jelenleg **v4** formátum. A betöltés előbb nyers adaton migrál (`ShipStateMigrations`, v1→v2→v3→v4 azonosság-lépések, a keret valódi átalakításra is tesztelt), aztán dekódol. Az új mezők megengedők: hibás típusú érték nem dobja el a hajót. Az újabb formátum **csak olvasható** marad, hogy ne írjuk felül. Egy sérült fájl a vanilla viselkedés szerint alapállapotra esik és a következő mentésnél felülíródik (nincs biztonsági mentés).

**Repülés:** a `ShipFlightController` a szerver tick-jéből léptet egy állapotgépet (`LANDED → TAKEOFF → FLIGHT → LANDING → LANDED`, 10/60/5 mp), az Overworld játékidejéhez kötve, így a szerver szüneteltetésekor megáll, újratöltés után folytatódik. A kritikus világváltozás előbb fut, a fázis léptetése utána, a hangok és üzenetek utoljára.

**Incidensek:** a dobás determinisztikus: a felszálláskor sorsolt, mentett magból és a dobás sorszámából számolódik, így az újratöltés nem sorsolja újra. Kétlépéses dobás (előfordul-e: a teljes esély legfeljebb 25%; melyik alrendszer: az esélyükkel súlyozva).

**Hálózat:** egyetlen szerver→kliens csomag (`intersolarsystem:ship_status`); a hajó dimenziójában lévőknek másodpercenként, változáskor és belépéskor azonnal. A kliens tárolója a `ClientLevel`-hez kötött, így másik világ adata nem villan fel.

## 7. Fejlesztői környezet

- **Szükséges:** JDK 25 (Arch/CachyOS: `jdk25-openjdk`), git; szerkesztő: VS Code (Extension Pack for Java) vagy bármi más; a Gradle-t a repó wrappere hozza.
- **Parancsok:** `./gradlew test` (165 egységteszt), `./gradlew build`, `./gradlew runClient`, `./gradlew runServer` (első indításnál az `run/eula.txt`-ben `eula=true`, ezt mindenki maga fogadja el), `./gradlew runDatagen` (a generált fájlokat commitolni kell).
- **FONTOS:** a játék futása közben **ne futtass `clean build`-et vagy refaktort**. A Java az osztályokat lustán tölti be; a fájlok kicserélése `NoClassDefFoundError`-ral összeomlasztja a futó klienst (egyszer így omlott össze egy leszállás).
- **Kézi próba:** `runClient`, egyjátékos világ, cheats be. A README és a specek végén van a lépéssor szeletenként.
- A `.superpowers/` mappa (munkanapló, git-ignore-olt) a munkamenet eszköze, a projekthez nem tartozik.

## 8. Munkamódszer

Szeletenként, függőleges szelet = egy játszható kis élmény:
1. **Összefoglaló és kérdések** (legfeljebb három, ajánlással), a tulajdonos dönt.
2. **Spec** (`docs/superpowers/specs/`), jóváhagyás.
3. **Terv** (`docs/superpowers/plans/`): feladatok, pontos szignatúrák, tesztnevek, "kész, ha" lefedettség.
4. **Végrehajtás** teszt-vezérelten (előbb bukó teszt), feladatonként commit.
5. **Független review** külön ügynökkel, a Critical és Important találatok javítása teszttel, a Minor találatok jegyzékbe.
6. **Kézi próba** a tulajdonos által (`runClient`), majd merge a `master`-be; a szelet ága törölve. A történet később szeletenként egy commitba lett összevonva (lásd a 13. pontot).
- Ötletek, amelyek nem az aktuális szelethez tartoznak, az "Ötletdobozba" kerülnek (lent), nem épülnek be.
- API-pontosság: a 26.3 API-k verziónként változnak, ezért a megvalósítás a jarokat nézi (`javap`) és nem emlékezetből dolgozik.

## 9. Tapasztalatok a 26.3 API-ról (hogy ne kelljen újra felfedezni)

- A HUD-elem `extractRenderState(GuiGraphicsExtractor, DeltaTracker)` (nem `render`); rajzolás `fill` és `text`. Regisztráció: `HudElementRegistry.addLast`.
- Hálózat: `PayloadTypeRegistry.clientboundPlay()` (nem `playS2C`); a fogadó `ClientPlayNetworking.registerGlobalReceiver`.
- `Level#getHeight` **nem tölt be chunkot**: betöltetlen chunknál a világ minimális magasságát adja. Betöltés nélkül a leszállás a világ aljára tett nyílást (ez egyszer élesben is előjött).
- A dugattyú-reakció `PushReaction.IMMOVEABLE` (nem `BLOCK`).
- `SavedDataType` 4. paramétere (`DataFixTypes`) `null` lehet.
- A `ServerEntityWorldChangeEvents` nincs az entity-events API-ban; a "belépéskor azonnal" logika ezért a sync-szabályban van, nem eseményben.
- A `SplittableRandom` magját a "golden gamma" lépteti húzásonként; egy dobás két számot húz, ezért a dobásokat két lépésnyire kell tolni (különben összefüggenek).
- Egy kivétel a szerver tick-ben **nem újrapróbálódik**, hanem összeomlasztja a szervert.

## 10. Ismert hiányok és halasztott tételek

**Tervezett, de nincs meg:** több hajótest-osztály és bővíthető belső tér; javítókészlet receptje és zsákmánya; adatvezérelt balansz (a kopás- és esély-konstansok kódban vannak); valódi útszámító; Föld-worldgen.

**Halasztott kisebb találatok** (a review-kból, mind jegyezve):
- Javítóállomás: bárhol működik (a kreatív fülön van), a hajó dimenziójára kellene korlátozni; nem-készlet tárgy jobb klikkje az "kell egy készlet" üzenetet adja és blokkot sem lehet mellé rakni; a `showNeedsKit` nem veszi figyelembe az aktív incidenst.
- Egyetlen sérült incidens az összes mentett incidenst eldobja; hibás `hazards` mezőnél nincs WARN.
- Lejárat leszállt állapotban a hajón kívül lévőknek nem szól.
- Leszállás: a spawn-tartalék vízre/lombozatra is tehet nyílást, a világhatár-ellenőrzés csak a célpontra van, a nyílás entitásra kerülhet; csak olvasható mentésnél a "redocked" üzenet félrevezető.
- Hajó-védelem: robbanás és dugattyú nyithat rést a falon; kreatív mód átüti a törhetetlenséget.
- Hiányzó migrációs lépés esetén az adat a megállt verzióként töltődne be, csak olvasható jelölés nélkül.
- `ModLangProvider` tömbjei sorszám szerint indexelnek (új enum érték vagy átrendezés hibás párosítást okozhat); a `LangParityTest` a kulcsokat maga építi, és csak a `%s` helyőrzőt számolja.
- Csomag: az érvénytelen incidens beleszámít az olvasási korlátba; néhány teszt gyenge (−3 szűkítés, pontosan két incidens, érvénytelen típus-sorszám).
- HUD: a panel a bal felső sarokban takarhatja az F3 szöveget; a küldés állapota globális (több hajónál hajónként kell).

## 11. Kockázatok

1. **A csomag formátumának nincs verzió- és hosszelőtagja.** Ha új alrendszer jön vagy az enumok sorrendje változik (3. fázis), a régi kliens csendben rosszul értelmez. Ezt a 3. fázis előtt meg kell oldani (formátum-bájt, a feltételek darabszáma).
2. **A kliens kódnak nincs automatikus tesztje** (futó kliens kell hozzá). A viselkedést a tiszta modell tesztjei és a kézi próba fedik.
3. **Többjátékos és dedikált szerver:** a kézi próbák egyjátékos/LAN jellegűek; hosszú dedikált szerveres teszt és valódi több játékosos terhelés nem volt.
4. **Nincs CI.** A tesztek csak helyben futnak.
5. **Verziók:** a Loom `1.19-SNAPSHOT` nincs rögzítve, a `fabric-api` függés `*`; egy későbbi pillanatkép kódváltozás nélkül is törhet.
6. **Balansz nincs kipróbálva hosszan:** egy túlélő hajó kb. 9–10 repülés után a felszállási küszöb alá esik, és a javítókészlet forrása hiányában földön marad. A 3. fázisnak ezt meg kell terveznie (romokból kimentés).
7. **Méret:** a 3–12. fázis a terv szerint a projekt nagyobbik része (worldgen, bolygók); a becslések egy főre, heti 10–15 órára szóltak, ±50%.
8. **Placeholder textúrák** és egyszerű modellek vannak; a szerző a `fabric.mod.json`-ban még `maku`.

## 12. Javasolt következő lépések

1. **Rendrakási szelet** (kicsi): a 10. pont kisebb találataiból a leglényegesebbek (javítóállomás csak a hajóban, nem-készlet tárgy, incidens-lista robusztusság, lang tömbök sorszám helyett kulccsal), CI hozzáadása (Gradle teszt), a Loom/Fabric API verzió rögzítése, a licenc eldöntése.
2. **Csomagformátum** verzió-bájttal és hosszelőtaggal, a 3. fázis előtt.
3. **3. fázis tervezése:** a javítókészlet forrása (kimentés), a tesztfelszállás kockázata, az első minigame; a "kevés, de erős élmény" elv szerint inkább egy jól megcsinált javítási minigame, mint sok unalmas.
4. **Döntés a nyitott kérdésekről** a 3. fázis előtt: hajótest-osztályok kibontása (milyen sorrendben), a Hold szerepe a mag-hurok tesztjében (6. fázis).

**Ötletdoboz** (nem épült be, kérésre előkerül): navigációs konzol blokk felülettel, valós útszámító és más célok, külső hajómodell és animáció, kamerarázkódás, a HUD billentyűs kapcsolója és áthelyezése, minigame-ek, tesztfelszállás robbanással, a tűz/nyomásvesztés tényleges hatása a világra, más hajótest-osztályok és belső terek, alrendszer-szintek (fejlesztések), vészleszállás vagy `/iss ship land` megszakítás, adatvezérelt balansz.

## 13. Számok

- 8 commit a `master`-en, szeletenként egy (0, 1, 2a, 2b, 2c, 2d, dokumentáció és egy dokumentációs javítás); a fejlesztés közbeni commitok 2026-10-29-én egy előzetes mentés után szeletenként összevonásra kerültek, és minden szeletben a mai `hu.kriwdi` csomagnév szerepel.
- 48 közös, 6 kliens Java fájl (kb. 2650 sor), 22 tesztfájl (kb. 1850 sor), **165 egységteszt**, mind zöld.
- 6 spec és 6 terv a `docs/superpowers/` alatt, mindegyik szelethez.

## 14. Dokumentumok

- `docs/project-brief.md`: **a projekt teljes leírása** (pillérek, rögzített döntések, hajórendszer, világ és bolygók, technikai szabályok). Ezt érdemes először elolvasni.
- `docs/roadmap.md`: a 0–12. fázis becslésekkel és az aktuális állapottal; megmondja, melyik fázishoz van spec és terv (a 3–12. fázishoz még nincs).
- `README.md`: parancsok, a hajó és a HUD rövid leírása.
- `docs/superpowers/specs/`: a tervezési döntések szeletenként (0, 1, 2a, 2b, 2c, 2d).
- `docs/superpowers/plans/`: a végrehajtási tervek ugyanígy; a tervekben a "Global Constraints" és a "Review Focus" a leghasznosabb belépő.
- `docs/status/`: ez a jelentés.
