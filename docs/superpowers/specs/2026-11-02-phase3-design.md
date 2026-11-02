# 3. fázis – Tesztfelszállás, hibák, minigame-ek (terv)

Dátum: 2026-11-02

## Összefoglaló

A hajó eddig "csak" kopott, és a javítókészletet nem lehetett megszerezni, így kb. 10 repülés után a hajó földön ragadt. A 3. fázis a hajót valódi kockázattá teszi: a kopott alrendszerekben **rejtett hibák** keletkeznek, amelyeket **diagnosztikával** részben, **tesztfelszállással** teljesen felszínre lehet hozni, és egy rossz állapotú hajó felszálláskor **katasztrofálisan meghibásodhat** (robbanás, megszakított felszállás, súlyos sérülés). A javításhoz kell egy **forrás** (kimentés romokból, gyártás), és az első **javító minigame**.

**Kész, ha:** (1) a javítókészlet túlélő játékban megszerezhető (romokból kimentett alkatrészekből gyártható); (2) a kopott hajóban rejtett hibák keletkeznek, a diagnosztikai konzol ezek egy részét kimutatja, a tesztfelszállás mindet; (3) egy rossz hajó felszállása felrobbanhat/megszakadhat; (4) legalább egy minigame működik, az eredménye szerveren validált.

## Szeletek

| Szelet | Tartalom | Állapot |
|---|---|---|
| 3a | Kimentés és gyártás: hulladék-alkatrészek, javítókészlet recept, zsákmány a vanilla romokban, rozsdás panel szétszedése | kész |
| 3b | Rejtett hibák, diagnosztikai konzol, tesztfelszállás, katasztrofális felszállás | kész |
| 3c | Első minigame: vezetékezés (rövidzárlat javítása) | kész |
| 3d | További minigame-ek (szelepbeállítás, nyomáskiegyenlítés), a felszerelés csökkenti a minigame-ek számát | később |

## Döntések (alacsony tét, eldöntve; ha nem tetszik, könnyen változtatható)

1. **Kimentés a világgen előtt.** A saját Föld-világgen a 7. fázis, addig a vanilla struktúrák ládái adják a kimentett technológiát (elhagyott bánya, hajóroncs, sivatagi/dzsungel templom, romos portál, falusi kovács, ősi város, próbakamra, erőd, ostromállás-maradvány). A 7. fázisban ugyanezek a tárgyak a saját struktúrákba költöznek. Így a 3. pillér ("a technológiát kimented, nem feltalálod") már most érvényes.
2. **Új tárgyak:** fémhulladék (`scrap_metal`), kimentett vezeték (`salvaged_wiring`), kimentett áramkör (`salvaged_circuit`). A javítókészlet recept: 2 fémhulladék + 1 vezeték + 1 áramkör + 1 vasrúd. A rozsdás panel kitörve fémhulladékot ad (ez a hajóépítés "maradéka").
3. **Rejtett hiba (latent fault):** alrendszerenként legfeljebb egy. Leszálláskor keletkezhet, ha az alrendszer 70% alatt van; esély: (70 − állapot) / 140, tehát 0%-nál 50%. A hiba nem látszik, amíg ki nem mutatják.
4. **Diagnosztikai konzol** (új blokk a hajóban): egy vizsgálat minden rejtett hibát a navigáció/kommunikáció állapotától függő eséllyel mutat ki (50% + állapot/2, tehát 100%-nál biztos). Megmutatja a becsült felszállási kockázatot is, de csak a már ismert hibákkal számolva ("előre jelzi, de nem mindent"). Egy vizsgálat percenként egyszer futtatható.
5. **Tesztfelszállás:** a konzolon guggolva + jobb klikk (5 mp-en belül kétszer, megerősítésként). Rövid ugrás ugyanarra a helyre (10 mp felszállás, 15 mp lebegés, 5 mp leszállás). Minden rejtett hiba incidensként jelentkezik, a katasztrófa esélye fele akkora. Kevesebbet kopik, mint egy valódi repülés.
6. **Katasztrofális felszállás:** a felszállás végén dől el. Az esélyt a hajtómű, az üzemanyag/energia és a leszálló-/kilövőegység rejtett hibái és alacsony állapota adják (legfeljebb 60%, tesztnél 30%). Ilyenkor: robbanás a leszállóhelynél (élőlényeket sebez, **blokkokat nem rombol**, hogy a bázis ne vesszen el), a felszállás megszakad, a hajó a helyén marad, és súlyos sérülést kap (hajtómű −30, leszállóegység −30, burkolat −20, a többi −10). A rejtett hibák kiderülnek.
7. **Javítás sorrendje** az állomáson: aktív incidens → ismert rejtett hiba → legrosszabb alrendszer.
8. **Mentés:** v5 formátum (új mezők: rejtett hibák, repülés típusa); a v4 → v5 lépés azonosság, a hiányzó mezők alapértelmezettek.

## Ötletdoboz (nem ebben a fázisban)

- A roncsérzékelő (`scrap_sensor`) mutassa a legközelebbi kimenthető roncs irányát.
- Robbanáskor tényleges blokkrombolás opcióként (szerver-beállítás).
- Navigációs konzol felülettel a `/iss ship fly` helyett.
