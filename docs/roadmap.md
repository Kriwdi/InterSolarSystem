# InterSolarSystem – ütemterv

Az eredeti ütemterv a 0–12. fázisra, az aktuális állapottal (2026-10-29). Az időbecslések **egy főre, heti 10–15 órás hobbi-fejlesztésre** szólnak, ±50%. Mindig csak az aktuális fázist építjük; minden fázis elején rövid összefoglaló és "kész, ha" feltétel készül.

| Fázis | Tartalom | Becslés | Állapot |
|---|---|---|---|
| 0. Előkészítés | döntések, repo, Fabric környezet, datagen alap | 1–3 hét | **kész** |
| 1. Alaprendszerek és tartalom-váz | alaprendszerek, mentés, tartalom-váz | 3–5 hét | **kész**: mentésformátum (v5), receptek, zsákmány, nyelvi tábla |
| 2. A hajó magja | hajótest, mozgás, belső tér, alrendszerek, alap sérülésmodell, HUD | 6–10 hét | **kész** (2a–2d). Kész, ha: vanilla világban felszáll, repül, leszáll, és a meghibásodás működik. Hiányzik: több hajótest-osztály, bővíthető belső tér |
| 3. Tesztfelszállás, hibák, minigame-ek | tesztfelszállás robbanással, a javítás minigame-ekkel | 4–6 hét | **nagyrészt kész** (3a kimentés és gyártás, 3b rejtett hibák, diagnosztika, tesztfelszállás, sikertelen felszállás, 3c vezetékezés minigame). Hátra: 3d további minigame-ek, kézi próba |
| 4. Időrendszer és utazás | időkonverzió, útszámító, eseményrendszer, offline szimuláció | 3–5 hét | nincs elkezdve; a 2b-ben fix időtartamok vannak |
| 5. Orbit dimenzió és ISS | közös űr-dimenzió, állomások, roncsmezők | 5–8 hét | nincs elkezdve |
| 6. Hold | első más égitest | 4–6 hét | nincs elkezdve. **Itt dől el, hogy a mag-hurok (túlélés, romok fosztogatása, hajóépítés, kockázatos út, új bolygó) szórakoztató-e. Ha nem, nem építünk tovább rá, hanem először ezt javítjuk.** |
| 7. A halott Föld világgen | saját chunk generator, biome-ok (legalább 10), struktúrák, kezdő élmény | 8–14 hét | nincs elkezdve |
| 8. Mars és a külső rendszer kapuja | Mars, meghibásodott rover mint kulcs | 6–10 hét | nincs elkezdve |
| 9. Aszteroidaöv, üstökösök, megújuló források | generált célpontok, drónok, bányaállomások | 4–7 hét | nincs elkezdve |
| 10. Külső bolygók | Jupiter, Szaturnusz/Titán, Uránusz, Neptunusz (és Vénusz, Merkúr) | bolygónként 3–6 hét | nincs elkezdve |
| 11. Multiplayer finomítás, balansz, polish | | 6–10 hét | nincs elkezdve |
| 12. Kiadás és fenntartás | | | nincs elkezdve |

## Tervek és specek

- **Megvan** (a `docs/superpowers/` alatt): spec és terv a 0., 1., 2a, 2b, 2c, 2d szeletre, és a 3. fázis terve (`specs/2026-11-02-phase3-design.md`).
- **Nincs meg:** spec vagy terv a 3–12. fázisra. Ezek a fázisok csak a fenti egysoros leírással és a `docs/project-brief.md` tartalmával rendelkeznek; minden fázis elején a szokásos folyamat indul (összefoglaló, kérdések ajánlással, spec, terv, végrehajtás, review).

## Következő lépések (2026-11-02)

1. **Kézi próba** a 3. fázisra (`runClient`): kimentés a ládákból, receptek, diagnosztika, tesztfelszállás, sikertelen felszállás, vezetékezés.
2. **3d szelet:** szelepbeállítás (túlmelegedés) és nyomáskiegyenlítés (nyomásvesztés) minigame; a jobb felszerelés csökkentse a minigame-ek számát.
3. **A 2. fázis maradéka:** több hajótest-osztály, bővíthető belső tér.
4. **4. fázis:** időkonverzió és útszámító.

A 2026-11-02-i munka összefoglalója: `docs/status/2026-11-02-helyzetjelentes.md`.
