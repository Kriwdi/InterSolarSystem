# 5. fázis – Orbit dimenzió és ISS (terv)

Dátum: 2026-11-02

## Összefoglaló

A hajó először hagyja el igazán a Földet: Föld körüli pályára áll, rádokkol a Nemzetközi Űrállomás (ISS) maradványaira, és a játékos űrruhában kiszállhat a vákuumba. Az orbit egy közös űr-dimenzió, amelyet a későbbi fázisok (aszteroidaöv, roncsmezők, állomások) bővítenek.

**Kész, ha:** (1) a navigációs konzolról el lehet repülni Föld körüli pályára és onnan vissza a felszínre; (2) az orbitban mikrogravitáció és vákuum van, űrruha nélkül a játékos megfullad; (3) az űrruha oxigént fogyaszt, és a hajóban újratöltődik; (4) az ISS ott van, bejárható, fosztogatható; (5) a hajó rádokkol az ISS dokkolóportjára.

## Szeletek

| Szelet | Tartalom |
|---|---|
| 5a | Orbit dimenzió (fekete ég, csillagok, nap-éj ciklus), mikrogravitáció (gravitáció-attribútum), vákuum (fulladás), a hajóban a stabilizátor adja a mesterséges gravitációt; utazás a pályára és vissza, dokkolás |
| 5b | Űrruha (sisak, ruha, nadrág, csizma): saját páncélmodell-textúra, oxigéntartály a ruhában, újratöltés a hajóban, oxigénkijelző |
| 5c | Az ISS: eljárással felépített állomás (modulok, rácsos főtartó, napelemszárnyak, ablakok, lámpák), fosztogatható ellátmányládák saját zsákmánytáblával |

## Döntések (alacsony tét)

1. **Égbolt:** a vanilla Föld-égbolt fekete égszínnel és köd nélkül: így a Nap, a Hold és a csillagok látszanak, és nap-éj ciklus van, mint egy valódi keringési pályán. Egy alattunk forgó Föld renderelése saját égbolt-rendereléssel jöhet később (ötletdoboz).
2. **Mikrogravitáció:** a gravitáció-attribútum 15%-ra csökken, az esési sebzés elmarad. A hajóban a stabilizátor adja a mesterséges gravitációt: 20% alatt a hajóban is csökken a gravitáció.
3. **Vákuum:** űrruha (mind a 4 darab) és oxigén nélkül a levegő kb. 15 mp alatt elfogy, utána fulladásos sebzés.
4. **Oxigén:** a ruha tartósága az oxigén (600 = 10 perc). A hajó belsejében, ha az életfenntartás legalább 20%, a tartály gyorsan feltöltődik.
5. **Utazás:** a pálya 6 óra valós idő (0,25 nap), 1x-es hajtóművel 30 mp. A pályáról a felszínre ugyanennyi, a hajó az utolsó felszíni leszállóhelyére tér vissza. A Hold-megkerülés a kiinduló helyre hoz vissza, ami lehet a pálya is.
6. **Az ISS helye:** a dimenzió közepén, a dokkolóport a Harmony csomóponton. Az első érkezéskor épül fel, és megmarad.

## Ötletdoboz

- Saját égbolt-renderelő: a Föld alattunk, a terminátor-vonallal.
- Rakétahátizsák (jetpack) az űrsétához.
- Az ISS modulok megjavítása, a napelemek visszakapcsolása bázisépítéshez.
