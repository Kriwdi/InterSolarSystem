# 7. fázis – A halott Föld (terv és megvalósítás)

Dátum: 2026-11-03

## Összefoglaló

A vanilla Föld teljes cseréje. Csak az Overworld marad (a Nether és az End nem jön létre, a portál nem visz sehová), a tengerek kiszáradtak, minden vanilla biome eltűnt, és helyükre 12 saját biome jön. Nincs vanilla mob (se állat, se szörny, se járőr, kereskedő vagy fantom) és nincs vanilla érc; a barlangok, a deepslate és a kőzetváltozatok maradnak. Helyettük az űrprogramhoz kellő ércek jönnek, amelyekből alkatrészek és hajtómű-fejlesztések készülnek. A világ tele van romokkal, roncsokkal és elhagyott létesítményekkel, és a kezdőpont mellett ott a félig betemetett hajó.

**Döntések (a tulajdonossal egyeztetve, 2026-11-03):** minden biome és struktúra egy menetben, egyszerűbb első változatokkal; semmi vanilla mob; a vanilla barlangok és a deepslate maradnak, a vanilla ércek nem, helyettük űrutazáshoz használható ércek (eszközök is lehetnek belőlük); a Nether és az End kikapcsolva.

## Biome-ok (12)

| Biome | Felszín | Jellemző | Veszély |
|---|---|---|---|
| Hamusivatag | hamu, salakkő, kvarchomok | csonkok, salakszikla, hamuszállingózás | – |
| Kiszáradt tengerfenék | kiszáradt iszap, homok, sókéreg | halott korallok, csonthalmok, hajóroncs, bálnacsontváz | – |
| Megkövült erdő | durva föld, kavics | megkövült törzsek, kidőlt fák | – |
| Sugárzó kráter | sugárfertőzött talaj, trinitit | robbanási kráterek izzó trinititüveggel, több uraninit | sugárzás (űrruha véd) |
| Befagyott megapolisz | hó, tömör jég | holt város, magbank, fagyott kolónia | – |
| Savas mocsár | savas iszap, sár | savas tavak (zöld víz), elhalt fák | a víz mar (űrruha véd) |
| Szeméthegyek | préselt hulladék | hulladékhalmok, elromlott bányászdrónok | – |
| Sósivatag | sókéreg, sótömb | lítiumos sókéreg-foltok, sósziklák | – |
| Mély kanyonok | vörös homok, terrakotta-rétegek | óriás kanyon-vájó, színes rétegzett falak, gát | – |
| Oázis | fű, föld, homok | az utolsó élő hely: fák, virágok, tiszta víz | – |
| Elhagyott farmvidék (extra) | barna fű, durva föld | szalmabálák, kiszáradt földek, tanya silóval | – |
| Olajmező (extra) | kátrányos homok, olajpala | kátránytócsák, olajkutak tartállyal | – |

A biome-ok klímazónákhoz kötöttek (vanilla domborzat, saját elhelyezés): a kiszáradt tengerfenék a régi óceánok helyén, a kanyonok a régi folyóvölgyekben, a szeméthegyek a hegyekben, az oázis és a sugárzó kráter ritka.

## Ércek és az űrprogram

| Érc | Termék | Mire jó |
|---|---|---|
| Bauxit | alumínium | akkumulátor, napelemcella |
| Titán | titánrúd, titánlemez | titán szerszámok, fűtőelem, hajtómű-készletek |
| Uraninit (enyhén világít) | dúsított urán | urán fűtőelem |
| Spodumen, lítiumos sókéreg | lítiumpor | akkumulátorcella |
| Monazit | ritkaföldfém-por | ritkaföldfém-mágnes |
| Kvarchomok | szilíciumlapka | napelemcella |
| Olajpala | bitumen | (későbbi üzemanyag) |

Vas és réz csak kimentésből: a fémhulladékból vasrög, a mentett vezetékből réz olvasztható.

**Hajtómű-fejlesztés:** az ionhajtómű-készlet (titánlemez, mágnes, akkumulátor, napelem, áramkör) a navigációs konzolon a 2. szintre, a fúziós hajtómű-készlet (urán fűtőelem, hélium-3 regolit a Holdról) a 3. szintre emeli a hajtóművet. Így a Föld ércei és a Hold kincsei a haladás kulcsai.

## Struktúrák (26 sablon, 2 jigsaw-hálózat)

Bunker (kicsi és kétszintes nagy), űrközpont kilövőállással és kidőlt rakétával, kutatólabor, kutatóállomás, magbank (Svalbard-szerű ék a hegyoldalban), terraformáló kísérlet (üvegkupola), napelemfarm, gát, rádiótorony, kórház, laktanya őrtoronnyal, repülőtér lezuhant géppel, olajkút tartállyal, tanya silóval, lezuhant teherkapszula, műhold, rakétafokozat, bányászdrón-telep aknával, három kolónia eltérő sorssal (benőtt, leégett, befagyott), szárazra került hajóroncs, bálnacsontváz. **Holt város** (jigsaw: tér, utcák, felhőkarcoló, lakóház, bolt) és **metróhálózat** (jigsaw: állomás lépcsővel a felszínre, alagutak, beomlott végek). Mindegyik saját zsákmánytáblával (16 féle).

## Kezdés

Új világban a hajó félig betemetve áll a kezdőpont mellett (égésnyom, roncsok, ellátmányos hordó), az ajtaja a hajó leszállóhelyeként be van jegyezve, így rögtön be lehet szállni.

## Eszközök

`tools/worldgen/make_worldgen.py` (preset, zajbeállítás, felszín, biome-ok, elemek), `make_structures.py` (sablonok kódból, NBT-író: `nbt.py`), `make_content_data.py` (zsákmány, receptek, tagek), `tools/art/make_earth_textures.py` és `make_earth_models.py`.

## Ellenőrzés

Szerveres füstteszt: csak az Overworld létezik, a biome-ok 6400 blokkon belül megvannak, a kezdőpont körül csak saját biome, nincs vanilla érc, van űrérc, a struktúrák 1600 blokkon belül, a kezdőhely megépült. Kliens: képek minden biome-ról és a fő struktúrákról.

## Később (ötletdoboz)

Saját élőlények (mutánsok, drónok), a bányászdrónok megjavítása, rádiójelek a struktúrák felé, lore-terminálok szövegei a Földön, olajfinomítás üzemanyaggá, víz- és levegőszűrés a túléléshez.
