# InterSolarSystem – projektleírás

Ez a dokumentum a projekt eredeti terve, a fejlesztés indításakor rögzítve, a megvalósítás során rögzített döntésekkel kiegészítve. Az aktuális állapot a `docs/status/` alatti helyzetjelentésben és a `docs/roadmap.md`-ben van; a szeletenkénti részletes tervek a `docs/superpowers/` alatt.

## 1. A projekt

- **Név:** InterSolarSystem
- **Platform:** Fabric (Fabric API, Fabric Loom, Java)
- **Minecraft verzió:** 26.3 (a fejlesztés közben nem váltunk)
- **Nyelvek:** a kód, az osztálynevek, az azonosítók és a kommentek angolul; a játékon belüli szövegek angolul, németül és magyarul (lang fájlok, datagen); a dokumentáció magyarul.

Űrhajós túlélő mod. A Föld kihalt, poros, rozsdás világ (posztapokaliptikus hangulat), élet csak elrejtett bunkerekben, állomásokon és ritka oázisokban van. A játékos (multiplayerben egy kis csapat) az utolsó túlélők egyike, és az élet fenntartása a feladata: kimenti a romokból a technológiát, megépít és fenntart egy űrhajót, majd fokozatosan eljut a belső, aztán a külső naprendszerbe.

A játék nem "építs rakétát, bányássz más blokkokat", hanem túlélés, kockázat és hangulat, ahol az űr maga a túlélés terepe.

## 2. Tervezési pillérek

Minden új funkciónak ezekhez kell igazodnia; ha ütközik velük, szólni kell és jobbat javasolni.

1. Mindig legyen mit csinálni: életfenntartás, kutatás, felderítés, javítás, felkészülés.
2. A hajó nem egy gomb, hanem kockázat: meghibásodás, sérülés, hibás tesztrepülés, javító minigame-ek.
3. A technológiát nem feltalálod, hanem kimented a romokból (ezen a részen a mod lehet inventív).
4. Lépcsőzetes haladás: a külső rendszerhez a belső rendszerből származó tárgyak kellenek (űrbeli vagy más égitesteken lévő struktúrákból, esetleg magukból az égitestek ásványaiból).
5. Minden bolygó önálló identitás: saját ég, anyagok, veszélyek, túlélési követelmények.
6. Mély, de véges szintek az alapfejlesztésekben, plusz végtelen horizontális bővítés (új modulok, specializációk). Végjátékban végtelen fejlesztési sor csökkenő hozammal.
7. Hangulat előbb, tartalom utána. Inkább kevés, de erős élmény.

## 3. Rögzített döntések

### Idő és utazás

- **Időkonverzió:** 10x gyorsítás az alap. 1 valós földi nap = 2,4 MC óra, azaz 10 valós nap = 1 MC nap (1 MC nap = 20 valós perc).
- **Utazási idő** (valós játékidő) = valós úthossz (nap) / 10 / hajtómű-szorzó.
- **Hajtómű-szintek:** 1x, 3x, 10x, 30x (a 4. csak a legkülső bolygókhoz és a végjátékhoz).
- A pálya- és gravitációs hatások (indulási ablak, hintamanőver) módosítják az időt és az üzemanyag-szükségletet.
- **Tájékoztató értékek** 1x hajtóművel: Hold ~6 perc, Mars ~7 óra, Jupiter ~73 óra, Neptunusz ~146 óra.
- **Utazás közben van játékmenet, nem átugrás:** animált kilövés és érkezés, közte gyorsított idő, amit véletlen események szakítanak meg (meghibásodás, meteoritzápor, napkitörés, rendszerhiba, sodródó roncsok, jelek). Van esély a zökkenőmentes útra is, ilyenkor a hangsúly a hajó életfenntartásán, ellenőrzésén és a kutatómunkán van.
- **Multiplayerben közös idő:** ha az egyik játékos utazik, a többiek ideje is telik. Aki a Földön marad, a bázist tartja fenn.

### Világ és haladás

- **Dimenziók:** minden bolygó külön dimenzió (saját ég, fény, gravitáció, légkör), plusz egy közös orbit/űr dimenzió (ISS, állomások, roncsmezők, aszteroidaöv), ahol az égitestek renderelt objektumok, nem valódi blokkok.
- **Haladási sorrend:** Föld, orbit/ISS (szondák stb.), Hold, Mars, aszteroidaöv, Jupiter, Szaturnusz, külső bolygók.
- A hajó a mag, ezzel kezdtük. Vanilla világban is fejleszthető és tesztelhető, a világgen később jön.
- **Földi világgen:** a vanilla teljes lecserélése, legalább 10 saját biome, sok illő struktúra.
- **Megújuló források:** üstökösök, meteoritok, aszteroidák (generált, véletlen összetételű célpontok), telepíthető drónok és bányaállomások, gázgyűjtés, jégbányászat, véletlen orbitális események, bolygóspecifikus ércek, farmolás.

### A négy korábban nyitott döntés (lezárva)

| # | Kérdés | Döntés |
|---|---|---|
| 1 | Hajóépítés módja | Hibrid modell: előre tervezett hajótest-osztályok, bennük szabadon bővíthető belső tér és választható modulok. A szabad blokképítés (Valkyrien Skies / Create jelleg) nagyságrendekkel nehezebb. |
| 2 | Szóló és multi prioritás | Mindkettő párhuzamosan: szerver-hiteles architektúra az elejétől, szóló és LAN előbb, a multiplayer finomhangolás később. |
| 3 | Főcél, történet | Eljutni a Proxima-rendszerbe, a lakhatási zónában lévő bolygó(k) felkutatása és benépesítése. Apró, opcionális lore-morzsák a kihalás okáról (csak szöveg és loot, külön rendszer nélkül). |
| 4 | Kilépés utazás közben (MP) | A játékos a hajóba tér vissza; az idő ugyanúgy telik, amíg legalább egy játékos van a szerveren. Ha nincs játékos, az idő megáll. |

## 4. Hajórendszer (a mod magja)

- **Hajótest-osztályok:** mentőkapszula, kutatóhajó, teher-/bányászhajó, felderítő, anyahajó.
- **Alrendszerek** (mind szintezhető és meghibásodhat): hajtómű, üzemanyag/energia, burkolat/páncél, életfenntartás (oxigén, víz, hőmérséklet, légtisztítás, sugárzásvédelem), navigáció/kommunikáció, gravitációs/stabilizáló egység, leszálló- és kilövőegység, raktér.
- **Sérülésmodell:** minden alrendszernek van állapot-értéke (0–100%), ami kopik használattal, eseményekkel és rossz leszállással. Alacsony állapotnál nő a meghibásodás esélye, a hibatípus az alrendszertől függ (szivárgás, túlmelegedés, rövidzárlat, tűz, nyomásvesztés).
- **Javítás:** alkatrészek (romokból és üzemben gyártva) plusz minigame-ek (vezetékezés, szelepbeállítás, nyomáskiegyenlítés, forrasztás). A jó felszerelés csökkentse a minigame-ek számát, hogy ne legyen unalmas.
- **Tesztrepülés:** a játékos a végleges út előtt tesztfelszállást/leszállást végezhet, ahol robbanás vagy összeomlás is lehet. A diagnosztika/szimulátor előre jelezheti a kockázatot, de nem mindent.
- **Belső tér:** bővíthető szobák, gépterem, labor, farm, raktár, munkaállomások, gyártó/3D nyomtató.
- **Űrruha** több szinttel: maszk, űrruha, fejlett szkafander.

## 5. Világ és tartalom

### A Föld (saját világgen)

- **Biome-ok (legalább 10):** hamusivatag, kiszáradt tengerfenék hajóroncsokkal, megkövült erdő, sugárzott kráter, befagyott megapolisz, savas mocsár, szeméthegyek, sósivatag, mély kanyonok, ritka oázis (az utolsó élő biome). Opcionális: elhagyott farmvidék, olajmező, sziklás fennsík.
- **Struktúrák:** bunker (több méret), elhagyott űrközpont kilövőállással, kutatólabor, magbank (Svalbard-szerű), metróalagút-hálózat, napelemfarm, gát, kórház, laktanya, repülőtér, rádiótorony, kihalt nagyvárosok, lezuhant űrobjektumok.
- **Fiktív tartalom:** lezuhant teherkapszulák, félig működő bányászdrónok (megjavíthatók), félbemaradt terraformálási kísérletek, elhagyott kutatóállomások, meghiúsult kolóniák eltérő sorsokkal.

### Méretarányok

- A Föld 20.000 block nagyságú, a többi égitest méretarányosan.
- Minimális méret: 2000 block, kivéve, ha egy égitest annyi tartalmat hordoz, hogy megér egy kisebb világot is.

### Más égitestek (röviden)

- **Hold:** alacsony gravitáció, nincs légkör, holdbázis-romok.
- **Mars:** por, fagy, meghibásodott rover mint kulcs a külső rendszerhez.
- **Aszteroidaöv.**
- **Jupiter és holdjai:** gázgyűjtés, sugárzás.
- **Szaturnusz/Titán, Uránusz, Neptunusz.**
- **Vénusz és Merkúr.**

## 6. Technikai szabályok

- **Modulok:** core, ship, travel, space, worldgen, survival, content.
- **Kliens/szerver szétválasztás az elejétől:** minden játékállapot szerveroldalon van, a kliens csak megjelenít. A minigame-ek eredménye szerveren validált.
- **Adatvezérelt tartalom, ahol lehet** (JSON, datagen): receptek, loot table-ök, tag-ek, lang fájlok, bolygó- és eseményleírások.
- **A hajó és a játékos állapota tartósan mentve** (persistent state). A mentésformátumot korán stabilizáljuk.
- **Teljesítmény:** kevés aktív entitás, a drónok és távoli bázisok egyszerűsített, időalapú "offline" számítással.
- **Világgen:** saját `ChunkGenerator`, `BiomeSource`, density function/noise, struktúrák NBT sablonokkal és jigsaw rendszerrel (több változat, elforgatás, véletlen romlás).
- **API-pontosság:** a Minecraft/Fabric API-k verziónként változnak. Ha nem biztos egy osztály, metódus vagy mapping neve a 26.3-as verzióban, ellenőrizni kell a jarban, a dokumentációban vagy egy példa-modban; API-t kitalálni tilos.

## 7. Hatókör védelme

- Egyszerre egy fázison, azon belül egy kis, lezárható feladaton dolgozunk (függőleges szelet: egy teljes, játszható kis élmény, ne sok félkész dolog).
- Új ötletek, amelyek nem az aktuális fázishoz tartoznak, az Ötletdobozba kerülnek, nem épülnek be.
- Ha valami túl nagy falat, kockázatos, vagy a becslések irreálisak, azt ki kell mondani.
- A leírt tervtől eltérő ötletet nem vezetünk be csendben: javaslat, indoklás, és a tulajdonos döntése.
