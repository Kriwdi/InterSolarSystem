# InterSolarSystem – helyzetjelentés

Dátum: 2026-11-02
Előzmény: `2026-10-29-helyzetjelentes.md` (az architektúra, az elvek és a 26.3 API-tapasztalatok ott vannak, ezek érvényesek).

## Mi történt

**Repó és infrastruktúra**
- Helyreállított mappaszerkezet (`src/main`, `src/client`, `src/test`), `.gitignore`.
- CI minden pushra: build, egységtesztek, dedikált szerveres füstteszt (betölti a recepteket, a zsákmányt, a modelleket, és hibára elbukik). A jar a `ci-builds` ágon.
- API-szonda munkafolyamat: a valódi 26.3 szignatúrákat írja ki a `ci-probe` ágra (`tools/api-probe.txt`).
- Licenc: All Rights Reserved; szerző: Kriwdi.
- **Az eredeti 165 egységteszt elveszett** (nem került fel a repóba). Az új kód tesztekkel érkezik (jelenleg 34 teszt), a régi logika tesztjei hiányoznak.

**Rendrakási szelet**
- A státuszcsomag formátumszámot és alrendszer-darabszámot kapott (a 10. pont 1. kockázata megszűnt).
- Javítóállomás: csak a hajóban működik; más tárgy átmegy (lehet mellé építeni); kit nélkül az aktív incidenst mondja először.
- Egy sérült mentett incidens már nem dobja el a többit.
- Nyelvi tábla (`ModLangTable`): minden kulcs egy sorban, három nyelven; az enumok konstans szerint, nem sorszám szerint; `tools/sync_lang.py`.
- Fabric API minimum verzió rögzítve. A Loom marad `1.19-SNAPSHOT` (stabil kiadás nem ismert ehhez a verzióhoz; a CI jelezné a törést).

**3. fázis** (spec: `docs/superpowers/specs/2026-11-02-phase3-design.md`)
- 3a: kimentés a vanilla romok ládáiból, receptek (javítókészlet, konzol, állomás, panel).
- 3b: rejtett hibák, diagnosztikai konzol, tesztfelszállás, sikertelen felszállás robbanással. Mentés v5.
- 3c: vezetékezés minigame (rövidzárlat), szerveren validálva.

## Kézi próba (runClient, egyjátékos, cheats be)

1. Új világ, `/give @s intersolarsystem:ship_hatch`, rakd le, szállj be. A szobában a javítóállomás mellett ott a diagnosztikai konzol (régi világban magától bekerül, ha a hely üres).
2. Keress egy elhagyott bányát vagy hajóroncsot: a ládákban fémhulladék, vezeték, áramkör. Barkácsolj javítókészletet.
3. `/iss ship set engine 15`, `/iss ship set landing_launch 25`, `/iss ship set navigation_comms 30`, aztán egy rövid repülés (`/iss ship fly <x> <z>`). Leszállás után a konzolon jobb klikk: vizsgálat, ismert hibák, becsült kockázat.
4. Guggolva kétszer a konzolra: tesztfelszállás. Elő kell jönnie a rejtett hibáknak (HUD: piros figyelmeztető jel).
5. Rossz állapotú hajóval addig próbálkozz, amíg egy felszállás el nem bukik: robbanás a nyílásnál, a blokkok épek maradnak, a hajó a helyén, súlyos sérülés.
6. Rövidzárlatnál (`navigation_comms` vagy `stabilizer` incidens) a javítókészlettel az állomáson megnyílik a vezetékezés: jó bekötés javít, rossz szikrázik (−5%).

## Ismert hiányok és kockázatok

- A régi logika (repülés, kopás, beszállás, leszállás, HUD-modell, szinkron) egységtesztjei hiányoznak.
- A vezetékezés képernyő és a HUD jele csak kézi próbával ellenőrizhető.
- A régi 10. pont halasztott tételei közül nyitva: leszállás vízre/lombozatra a spawn-tartaléknál, robbanás/dugattyú a hajó falán, HUD takarja az F3-at.

## Ötletdoboz

Lásd a 3. fázis spec végét és a korábbi jelentést; újak: roncsérzékelő iránymutatással, választható blokkromboló robbanás, navigációs konzol a `/iss ship fly` helyett.

## Kliens-ellenőrzés (képernyőképek, CI)

- A kliens a CI-ban Vulkan (lavapipe) hátterrel indul; a `ci-screens` ágon minden push után friss képek vannak.
- Javítva: a pályán kék volt az ég (a `minecraft:the_void` biome égszíne felülírta a dimenzióét) → saját `orbital_void` biome fekete éggel.
- Javítva: a Holdon a `MOTION_BLOCKING` heightmap a világ aljáét (0) adta vissza, így az Apollo-helyszínek és a bázis a föld alá épültek, és a leszállás is oda tett volna → oszlop-letapogatás (`MoonSites.surface`), a füsttesztben ellenőrizve.
- Az Apollo-leszálló formásabb: nyolcszögletű arany leszállófokozat, ezüst felszállófokozat ablakkal, dokkolóalagúttal és radarral.
- A Föld fázisa a Holdról helyes: holdi délben „újföld” (sötét korong), holdi éjjel „teljes Föld”.
