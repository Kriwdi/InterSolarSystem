# Következő szakaszok – döntések (2026-11-03 este)

## Javítások (először)
- **Vákuum-sebzés:** a vízalatti fulladás helyett kétféle: *hipoxia* (lassan fogyó oxigén: sötétedő, homályos képszél,
  szívverés és zihálás, lassuló mozgás, saját halálüzenet, nincs buborék) és *dekompresszió* (sérült sisak/ruha vagy
  ruha nélkül: deres képszél, remegés, csengő fül, gyors sebzés).
- **Oxigénkijelző:** sisak-HUD ív a képernyő szélén (oxigén, nyomás, ruha állapota, figyelmeztetések).
- **Ruhasérülés:** ütés, meteor, éles roncs kilyukaszthatja → sziszeg, gyorsabban fogy az oxigén; tömítőhabbal/folttal
  javítható.
- **Adatszalag:** csak gépen játszható le (adatterminál, rádió) – a tárgy leírása megmondja, hol; a hangnak működnie
  kell; minden beszélőnek torzított, rádiós beszédutánzata szól (a szöveg a feliratban), mellette effektek.
- **Struktúrák illeszkedése:** nincs lebegő blokk vagy struktúra. Alap/támfal az épületek alá a saját anyagukból a
  talajig, a lejtőn támfal/lépcső; a terep rézsűsen simul a rom széléhez természetes anyagokkal.

## Bontás és energia
- **Szétszerelés:** az összetett dolgokat (gépek, berendezések, roncsok) szerszámmal, lépésenként kell szétszedni a
  helyükön (burkolat le, vezeték ki, alkatrész ki), minden lépés ad valamit.
- **Bontógép:** egy blokk, mellé tett modulokkal bővíthető és gyorsul (daráló, olvasztó, szétválogató…); a tárgyakat
  és alkatrészeket nyersanyagra bontja. Visszanyerés: alapgéppel kb. 50–75%, fejlesztve közel 100%, néha ritka
  melléktermék.
- **Energia:** kábelhálózat – generátor/napelem/reaktor → kábel → gépek; látszik, megy-e áram, a gépek kijelzik.

## Kármán-űrkikötő és anyahajó
- Világonként 3, mindegyik restaurálható (akár több is).
- Restaurálás: mind a négy feladattípus valamilyen formában (vezetékhálózat újrakötése, törmelék eltakarítása,
  alkatrészcsere, kalibrálás/indítás minijátékkal) + a hajós hibajavítás elemei.
- Erőmű: fűtőelem kell bele (napelemmező tartaléknak), és kopik/meghibásodik, karbantartást igényel.
- Anyahajó: kb. 400 m (hangár a kishajóknak, labor, kutatóközpont, pihenő, étkező, farm, gyártó, híd…). A daru építi
  szekciónként, tervrajz alapján, látványosan. Belül: kész fedélzetek, bővíteni csak daruval/tervrajzzal lehet,
  egyébként csak blokkokat lehet cserélni.
- Kishajók: mind helyben gyártott (a kutatóhajóból is új készül, ami dokkolható).
- Progresszió vegyes: az alap anyahajó földi anyagból, a jobb modulokhoz holdi anyag kell.
- Utazás: élő út eseményekkel (meghibásodás, meteorraj, kutatás, farm); a célnál pályára áll, kishajóval szállsz le.

## Látvány és tartalom
- Űr: Tejút és ködök, sűrű színes csillagmező; mozgás érzése utazáskor (elsuhanó por, növő cél, távolodó Föld,
  csíkok). A pálya-dimenzióban is.
- Minden régi struktúra átdolgozva (kicsik is), stílusos belső terekkel, nincs üres ház, szoba nélküli épület; a
  lezuhant dolgok formásabbak; a nagy létesítmények monumentálisak; a biome-ok felturbózva (domborzat, maradványok,
  köd, részecskék, hangok, egyedi formák). Nincs "4×4 kocka", nincs fantáziátlan történet.

## Sorrend
1. Javítások (sebzés, HUD, ruha, szalag, illeszkedés) 2. Energia + bontás 3. Kármán-űrkikötő + anyahajó
4. Űrlátvány 5. Struktúra- és biome-átdolgozás 6. Pálya (súlytalanság + kötél + hátirakéta), roncsok, Hold-missziók.

## Bővíthetőség (2026-11-04)
A felhasználó kérése: később több bolygó, hold és objektum jön, a progresszió szintjén fontosak. Ezért:
- **Anyagok és bontás:** egyetlen tábla (`tools/salvage/make_salvage.py`) írja le, mi miből áll; új égitest anyagai
  és gépei új sorokat kapnak, a ritka kimenetek modulhoz (később magasabb szintű modulhoz) köthetők.
- **Energia:** a `PowerGrid.Role` listába kerül minden új áramforrás/tároló/fogyasztó (reaktor, RTG, fúziós,
  űrkikötő-gépek, más bolygók forrásai); a hálózat ugyanaz marad.
- **Struktúrák:** minden felszíni rom a `flat_site` burkon át kerül a világba (alap, illeszkedés, garancia), más
  égitestek romjai is ezt használják majd.
- **Progresszió:** a gyártás/anyahajó receptjei égitestenkénti anyagokra épülnek (Föld → Hold → Mars …), a
  tervrajzok és modulok szintekre bonthatók.

## Munkaterv a 3. szakaszhoz (Kármán + anyahajó), felügyelet nélkül (2026-11-04 éjjel)
1. **Űrkikötő-struktúra** (`tools/worldgen/karman.py`): egyetlen ~420×320-as sablon a `flat_site` burokban
   (gyűrűn, 3 db, ~6 km-re, `guaranteed`): összeszerelő csarnok (Bárka-5 gerinc, daru, darukonzol), 2 kilövőállás
   (lánghárító árok, szervizto­rony), indítóközpont (terminálok, kézikönyv-nyomtató), erőmű (reaktorcsarnok, 2
   hűtőtorony, turbinacsarnok, napelemmező), gyártócsarnok (gyártógép), üzemanyagtelep (gömbtartályok, szivattyúház),
   lakóblokkok, vasúti rakodó, kerítés kapukkal. Minden létesítményben egy **vezérlőszekrény**.
2. **Restaurálás**: a vezérlőszekrény (FACILITY tulajdonság) feladatlistát ad: anyagleadás + törmelék eltakarítása
   (a bejáratoknál lévő törmelék/ betonacél kibányászása – a szekrény ellenőrzi a megadott pontokat) + kalibrálás
   (gyors minijáték a hajós javítótábla mintájára) + az előző létesítmény működik (erőmű → irányítás → gyártás →
   üzemanyag → daru). Erőmű: fűtőelemet fogyaszt, és időnként meghibásodik (javítókészlet a szekrénynél).
3. **Gyártógép**: áram kell (űrkikötő-erőmű vagy saját hálózat); szekciókészletek és kishajó-tervrajz gyártása
   anyagleadással (guggolva: következő recept). Vegyes progresszió: hajtómű- és reaktorszekció holdi anyagot kér.
4. **Daru**: a darukonzolba adott készletből a daru blokkról blokkra rakja le a szekciót a csarnokban
   (tervrajz: `resources/blueprints/*.txt`, Python generálja), látványosan, percek alatt.
5. **Anyahajó**: ha minden szekció kész és tankolt, a hídon indítható; saját dimenzió (fedélzetek a szekciókból),
   a kishajó a hangárban dokkol; utazás égitestek között eseményekkel (a hajós rendszerek mintájára).

## Állapot (2026-11-04 hajnal, felügyelet nélküli munka)
- **4. Űrlátvány – kész:** Tejút-sáv (8 szegmens), 4 köd, 12 csillagmező a pályán, a Holdon, az anyahajón és a hajó
  ablakain; utazás közben elsuhanó por.
- **5. Átdolgozás – kész:** minden régi felszíni és föld alatti rom újraépítve berendezett belső terekkel
  (`tools/worldgen/overhaul.py`), a nagyok a `flat_site` legsíkabb-hely kereséssel; a `flat_site` a tisztítás után
  újra lerakja a darabokat, így a rom saját földje/hava/növényei megmaradnak. Biome-ok: szintetizált hangulat
  (hurok + egyszeri hangok, `tools/audio/make_ambience.py`), részecskék, autóroncsok, halottak, ládák, kövületek,
  jégtüskék.
- **6. Pálya és Hold – kész:** súlytalanság (kapaszkodás, sodródás), biztosítókötél tekercsekkel, MMU, törmelékáramok,
  roncsmező az ISS körül (`tools/worldgen/space_sites.py`, `space/SpaceSites.java`), történelmi holdmissziók és a
  Bárka–1 kolónia a Shackleton-peremen; két új terminálnapló (14: HALCYON, 15: Bárka–1).
- **Anyahajó:** a szekciók berendezve (életfenntartás, farm, labor, kabinok, étkező, gyengélkedő, hangár, híd).
- **Hátra van:** kutatóhajó-készlet és dokkolás; anyahajós utazási események valódi javítással; a Bárka–1 telepeseinek
  felébresztése (következő égitestek előtti horog).
- **Új égitest: Mars (2026-11-04 hajnal):** fúziós hajtóművel (3-as szint) érhető el; saját dimenzió (10 600
  blokk, 0,38 g, belélegezhetetlen légkör), síkság / dűne / felföld biome-ok, sarki jégsapkák, kráterek, kanyonok;
  vajkaramell ég Phobosszal, Deimosszal és a Föld-csillaggal; valódi missziók (Viking 1–2, Pathfinder, Spirit,
  Opportunity, Curiosity, Perseverance + Ingenuity, Zhurong, InSight); a Bárka–2 kolónia a Jezero-deltán a
  feltárással (a „fal” és a rover „kulcsa” a következő történetszál horga); két új napló (16, 17).
  Bővítés: új égitest = `solar_system.json` sor (`min_tier`), dimenzió + biome-ok (`tools/worldgen/make_mars.py`
  mintájára), helyszínek a `space_sites.py`-ban és egy `*Sites` lista; a hajó nézete (`ShipView.place`) és a
  leszállás (`ShipLanding`) egy-egy ággal bővül.
