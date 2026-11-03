# Hajórendszerek 2. kör – terv (2026-11-03)

A felhasználó visszajelzései a Build 83 után, és az egyeztetett döntések. Ez a terv a következő munkák sorrendjét és
a "kész, ha" feltételeket rögzíti.

## Döntések (a felhasználótól)

- **Kormányzás:** a pilótaülésből WASD-vel, és radar is segít (mindkettő).
- **Javítás:** helyszíni hibák és minijátékok együtt. Több egyedi blokk és tárgy kell, nem kell ragaszkodni a teljes
  kockához (vékony, összeilleszthető csövek).
- **Kézzel kezelhető rendszerek:** mind a négy (felszállási eljárás, reaktor és energia, életfenntartás, hajtómű és
  üzemanyag), de maradjanak kezelhetők.
- **Nehézség:** a létszámhoz igazodik (egyedül sok minden automatikus, többen munkamegosztás és párhuzamos hibák),
  és van választható szint is (könnyű / normál / realisztikus).
- **Csövek, kábelek:** építhető hálózat, automatikusan illeszkedő vékony elemekkel; a sérült szakaszt cserélni kell.
- **Felszállás:** rövid ellenőrzőlista: ülés, 3–5 kapcsoló, célválasztás, tolóerőkar (kb. 20–30 mp).
- **ISS:** valódi méret (1 blokk = 1 m, kb. 109 × 73 m), bejárható modulokkal.
- **Hajó kívülről:** értelmes forma a Földön, a Holdon és az ISS-hez dokkolva is.
- **Navigációs konzol:** megfordítva, a székek felé nézzen. **Ülés:** saját űrhajós ülés, amibe le lehet ülni.
- **Ajtó:** 2 blokk magas.
- Korábbról: kutatóhajóval indul a játékos (legfeljebb a Holdig), a nagy anyahajó a Földön épül; a Földön nem lehet
  növényt termeszteni, csak a hajón; 16 élőlény, kidolgozott animációval, pár normál állat csak elzárt bunkerekben.

## Sorrend (a felhasználó rám bízta)

| # | Szelet | Kész, ha |
|---|---|---|
| A | **A hajó kívül és belül, valódi ISS** – formás kutatóhajó-test (lekerekített törzs, pilótafülke-kupola, szárnyak, hajtóműgondolák, függőleges vezérsík, leszállólábak talppal), dokkolva az ISS-nél és a Holdon is; valódi méretű ISS dokkolóporttal; a konzol megfordítva; űrhajós ülés, amibe le lehet ülni; 2 magas ajtó | a hajó mindhárom helyen formás; az ISS valódi méretű és bejárható; ülni lehet; a teszt végigmegy |
| B | **Pilótaülés és kormányzás** – felszállási ellenőrzőlista (kapcsolópanel, tolóerőkar), ülésből WASD-kormányzás, meteorzápor az ablakképen és a radaron, ügyességen múló kitérés | meteorzápor elkerülhető ügyes kormányzással; a felszállás az ellenőrzőlistával megy |
| C | **Interaktív rendszerek és javítás** – helyszíni hibák (szikrázó panel, szivárgó cső, tűz, gőz) szerszámokkal (poroltó, tömítő, forrasztó, vezeték) és minijátékkal; építhető cső- és kábelhálózat; reaktor (fűtőelem, energiaelosztó), életfenntartás (oxigéntartály, szűrő, víz), hajtómű (üzemanyag, hűtés, kalibrálás) | minden alrendszernek van kézzel kezelhető része; a hibák a hajó egy pontján jelennek meg |
| D | **Nehézség** – létszám szerinti munkamegosztás és párhuzamos hibák, választható szint (gamerule) | egyedül és hárman is játszható; a szint állítható |
| E | **Élőlények** – a 16 lény kidolgozott animációval és viselkedéssel, bunkerállatok, a Földön nincs termesztés | a lények a biome-jukban jelennek meg |
| F | **Anyahajó** – kutatóhajó (max. Hold), földi állomások, anyahajó teher-, kutató- és felderítőhajókkal, hajós farm | külön terv készül, egyeztetés után |

Minden szelet végén: CI-build, kliensteszt képernyőképekkel, TUTORIAL.md frissítése.
