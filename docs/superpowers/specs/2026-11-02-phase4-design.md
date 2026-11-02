# 4. fázis – Időrendszer és utazás (terv)

Dátum: 2026-11-02

## Összefoglaló

A hajó eddig csak a Földön ugrált fix 60 másodperces repülésekkel. A 4. fázis bevezeti a valós távolságokon alapuló utazási időt (10x gyorsítás, hajtómű-szorzó, indulási ablak), a navigációs konzolt a célválasztáshoz, és az utazás közbeni eseményeket, hogy egy hosszú úton mindig legyen mit csinálni.

**Kész, ha:** (1) a Naprendszer égitestjei egy adatfájlban vannak, és a navigációs konzol megmutatja mindegyik utazási idejét a hajtómű és az indulási ablak szerint; (2) a hajó elindulhat egy valódi hosszú útra (a Hold megkerülése szabad visszatérési pályán, mert a Hold dimenziója az 5–6. fázisban jön), és a repülés hossza a számításból jön; (3) utazás közben véletlen események jönnek, amelyekre reagálni lehet; (4) a mentés és újratöltés nem változtat az eredményen.

## Szeletek

| Szelet | Tartalom |
|---|---|
| 4a | Égitest-katalógus (JSON), útszámító (időkonverzió, hajtómű, indulási ablak), hajtómű-szint és repülési terv a mentésben (v6), változó repülési idő |
| 4b | Navigációs konzol: formás blokk és képernyő, célválasztás, a Hold megkerülése |
| 4c | Utazási események: meteorraj (kitérés a konzolon), napkitörés, rendszerhiba, sodródó roncs (zsákmány), rejtélyes jelek (lore) |

## Döntések (alacsony tét)

1. **Időkonverzió:** 1 valós nap = 2400 tick (0,1 MC nap, 2 valós perc). Utazási idő = valós napok × 2400 / hajtómű-szorzó × ablak-szorzó.
2. **Hajtómű-szintek:** 1x, 3x, 10x, 30x. Most minden hajó 1x; a fejlesztés útja a későbbi tartalomhoz kötődik, addig OP-parancs állítja (`/iss ship engine <szint>`).
3. **Indulási ablak:** minden égitestnek van szinódikus periódusa (valós napokban, 10x-esen konvertálva). Az ideális ablakban 1,0x az idő, a legrosszabb pillanatban 1,5x. A konzol megmutatja, mennyi idő van a következő ablakig.
4. **Hintamanőver:** ebben a fázisban csak a katalógusban jelölt lehetőség (pl. Jupiter a külső bolygókhoz); a tényleges útvonal-választás az 5. fázis térképén jön.
5. **Elérhetőség most:** Föld (felszíni koordináta, 60 mp-es ugrás, mint eddig) és a **Hold megkerülése** (oda-vissza egy úton, kb. 12 perc 1x-es hajtóművel, visszatérés a kiinduló helyre). A többi égitest látszik a konzolon (utazási idővel), de zárolva van a dimenzióik elkészültéig.
6. **Incidensek hosszú úton:** 60 mp-ig tartó repülésnél 10 mp-enként, hosszabbnál percenként egy dobás.
7. **Offline idő:** az időzítés a világ játékidejéhez kötött; ha a szerver áll (nincs játékos és a szerver szünetel), az utazás is áll. Ez megfelel a rögzített döntésnek.

## Ötletdoboz

- Valódi pályatérkép hintamanőver-választással (5. fázis).
- Hajtómű-fejlesztés romokból kimentett alkatrészekkel.
- Életfenntartás-fogyasztás (oxigén, víz, élelem) hosszú utakon.
