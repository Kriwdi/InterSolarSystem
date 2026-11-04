# InterSolarSystem – Játékos útmutató

Ez az útmutató végigvezet a mod jelenlegi tartalmán: hogyan indulj, mit csinálj a halott Földön, hogyan tartsd életben a hajót, és hogyan juss el a pályára, az ISS-re és a Holdra. A mod fejlesztés alatt áll; ami itt le van írva, az a legutóbbi buildben működik. Amit még nem lehet megcsinálni, azt a végén külön jelöltük.

> **Röviden:** új világban a lezuhant hajód mellett kezdesz. Fosztogasd ki a romokat, bányássz űrérceket, javítsd a hajót, építs űrruhát, aztán a navigációs konzolról indulj a pályára vagy a Holdra. A hajtómű-fejlesztésekhez a Föld érceiből és a Hold hélium-3-jából kell alkatrészeket gyártani.

---

## Tartalom

1. [Telepítés](#1-telepítés)
2. [Az első percek](#2-az-első-percek)
3. [A hajó](#3-a-hajó)
4. [A halott Föld](#4-a-halott-föld)
5. [Nyersanyagok és feldolgozás](#5-nyersanyagok-és-feldolgozás)
6. [Romok és zsákmány](#6-romok-és-zsákmány)
7. [Receptek](#7-receptek)
8. [Kopás, meghibásodás, javítás](#8-kopás-meghibásodás-javítás)
9. [Rejtett hibák, diagnosztika, tesztrepülés](#9-rejtett-hibák-diagnosztika-tesztrepülés)
10. [Navigáció és utazás](#10-navigáció-és-utazás)
11. [Űrruha és oxigén](#11-űrruha-és-oxigén)
12. [Föld körüli pálya és az ISS](#12-föld-körüli-pálya-és-az-iss)
13. [A Hold](#13-a-hold)
14. [Hajtómű-fejlesztés](#14-hajtómű-fejlesztés)
15. [Parancsok](#15-parancsok)
16. [Tippek és gyakori kérdések](#16-tippek-és-gyakori-kérdések)
17. [Ami még nincs kész](#17-ami-még-nincs-kész)

---

## 1. Telepítés

1. Telepítsd a **Fabric Loadert** (0.20.1 vagy újabb) a **Minecraft 26.3**-hoz.
2. Töltsd le a **Fabric API**-t (0.165.0+26.3 vagy újabb), és tedd a `mods` mappába.
3. A mod jar-ját a GitHubon a [`ci-builds` ág `jar` mappájából](https://github.com/Kriwdi/InterSolarSystem/tree/ci-builds/jar) töltheted le. A fájl nevében benne van a build száma (pl. `intersolarsystem-1.0.0+build.70.jar`). Ezt is a `mods` mappába tedd, és **a régi `intersolarsystem` jar-t töröld onnan**, hogy csak egy legyen.
4. Indítsd el a játékot a Fabric profillal, és hozz létre egy **új világot**. A „halott Föld” csak új világban generálódik; a régi világok Föld-része marad, ami volt.

A mod tárgyai kreatív módban az **InterSolarSystem** fülön vannak (sisak ikon). Ha nem fér ki az első oldalra, a fülsor végén lapozó nyilak jelennek meg.

---

## 2. Az első percek

Az új világ kezdőpontja mellett, kelet felé áll a **hajód**: félig betemetve, égésnyommal, roncsokkal körülötte. A hajóteste törhetetlen, a nyugat felé néző ajtaja (a **hajóajtó**) visz be.

**Az első lépések sorrendje:**

1. **Nyisd ki a hordót** a hajó mellett. Benne: **Dr. Elena Ward búcsúlevele** (olvasd el!), a hajó **kézikönyvei**, 2 javítókészlet, multiszerszám, kőcsákány, fáklyák, kenyér, egy vödör víz, fémhulladék, vetőmag, 2 tölgycsemete, gyapjú.
2. **Szállj be** a hajóba (jobb klikk az ajtóra). Nézz körül: a hídon van a navigációs konzol, oldalt a diagnosztikai konzolok, a középső részben az ágyak, a raktár és a javítóállomás, hátul a gépterem.
3. A bal felső sarokban a **hajó állapotjelzője** (HUD) látszik: a fázis (leszállt, visszaszámlálás, repülés) és a nyolc alrendszer állapota százalékban.
4. Menj ki, és **kezdd a fosztogatást**: a környéken romok, lezuhant kapszulák, műholdak, bunkerek vannak (lásd [6. fejezet](#6-romok-és-zsákmány)). A fémhulladékból vas, a mentett vezetékből réz olvasztható.
5. **Gyűjts fát**: a halott fákból (elhalt rönk) saját deszka készül, a rönkből faszén égethető. Élő fák csak az oázisban vannak. **A Föld talaja terméketlen:** kapálni, vetni és csemetét ültetni csak a hajó belsejében lehet.
6. Készíts **kőcsákányt** (a kő megvan a föld alatt), és bányássz bauxitot és spodumént. Vascsákánnyal már titán, uraninit és monazit is bányászható.

> A világban nincs állat és nincs szörny. Élelmet a romokból, az oázisból és a saját farmodról szerezhetsz.

---

## 3. A hajó

### Kívülről

Ahol a hajó leszáll, ott **állva** áll a **kutatóhajó**, mint egy rakéta (a belseje ettől még egy kényelmes, vízszintes szint):

- fehér törzs kék-narancs csíkokkal és kerek ablakokkal;
- fent sötétített üvegű pilótafülke-gyűrű, a csúcson piros jelzőfény;
- alul a hajtóműgyűrű fúvókái;
- három nyilazott borda leszállólábakkal és helyzetjelző fényekkel (piros és zöld);
- az oldalán, a talajszinten a **2 blokk magas ajtó**.

**A hajótest törhetetlen.** Kreatív módban guggolva le lehet bontani, ha takarítani kell. Felszálláskor a hajótest eltűnik, és a célnál újra felépül: a Földön, a Holdon és az ISS-nél is. Az ISS-nél a hajó az állomás elülső dokkolóportjához kapcsolódik, az ajtaja egyenesen a dokkolóadapterbe nyílik.

**A beszállás nem indítja el a hajót.** Felszállni csak a navigációs konzolról lehet.

### Belül (9 × 7 × 21 blokk)

| Rész | Mi van ott |
|---|---|
| **Híd** (orr) | padlóig érő panorámaablak és tetőablak, navigációs konzol, két diagnosztikai konzol, két **űrhajós ülés** |
| **Lakórész** (közép) | két ágy, hordók és két láda (raktár), javítóállomás, kerek ablakok, bal oldalt a hajóajtó |
| **Gépterem** (far) | reaktoroszlop rácsban, hajtóműnyílások a hátsó falban |

A falak, az ablakok, a konzolok és az ajtó **belülről nem bonthatók** (kreatívban sem). A ládákba, hordókba nyugodtan pakolhatsz: ez a hajó raktára, és veled utazik. Ha valahogy mégis kikerülsz a hajó terén kívülre, a játék visszatesz.

### Ülés

Az űrhajós ülésbe **jobb klikkel ülsz le**, és **guggolással (Shift) állsz fel**, mint a csónakból. Az ülésből a konzolok felé nézel. Saját ülést is gyárthatsz (3 gyapjú, 1 bőr, 2 fémhulladék, 1 vasrúd), bárhová lerakhatod.

### Kilátás az ablakokon

Az ablakokon a valódi helyzet látszik:

- **a Földön:** poros, bézs ég, alatta annak a biome-nak a festett, részletes felszíne, ahol a hajó áll (dűnék, repedt sósíkság, városromok felülről, mezők, olajkutak stb.);
- **pályán:** elöl, lent kitölti a látványt a Föld;
- **a Holdon:** kráteres holdfelszín, fekete ég csillagokkal, fölötted a Föld;
- **utazás közben:** a talaj elmarad, a bolygó mögötted zsugorodik, a cél elöl nő, a csillagok úsznak.

Felszálláskor és leszálláskor a kabin remeg, a hajtómű zúg, a visszaszámlálás sípol.

### A HUD

A bal felső sarokban, csak a hajóban:

- **fázis:** leszállt / felszállás 0:04 múlva / érkezés 0:11 múlva / leszállás;
- **nyolc alrendszer:** hajtómű, üzemanyag és energia, burkolat és páncél, életfenntartás, navigáció és kommunikáció, stabilizátor, leszálló- és indítóegység, raktér. Zöld: 70% vagy több, sárga: 20–69%, piros: 20% alatt;
- **aktív incidensek** a hátralévő idővel (az utolsó 15 másodpercben villognak);
- **piros figyelmeztető jel** azoknál az alrendszereknél, amelyeknek ismert rejtett hibája van.

---

## 4. A halott Föld

A tengerek kiszáradtak, és nincs Nether és End (a portál nem visz sehová). Tizenkét biome van:

| Biome | Hogy néz ki | Mit érdemes ott keresni | Veszély |
|---|---|---|---|
| **Hamusivatag** | hamu, salakkő, kvarchomok | csonkok, salakszikla, kvarchomok (szilícium) | – |
| **Kiszáradt tengerfenék** | repedezett iszap, homok, sókéreg | halott korallok, csonthalmok, szárazra került hajóroncs, bálnacsontváz | – |
| **Megkövült erdő** | durva föld, kavics | megkövült fák, bunkerek, laktanya | – |
| **Sugárzó kráter** | sugárfertőzött talaj, izzó trinitit | robbanási kráterek, sok uraninit, mérgező hordók | **sugárzás** |
| **Befagyott megapolisz** | hó, tömör jég | holt város felhőkarcolókkal, magbank, metró, befagyott kolónia | hideg (még nincs hatása) |
| **Savas mocsár** | savas iszap, sár | savas tavak, elhalt fák, benőtt kolónia | **a víz mar** |
| **Szeméthegyek** | préselt hulladék | hulladékhalmok, elromlott bányászdrónok | – |
| **Sósivatag** | sókéreg, sótömb | lítiumos sókéreg, űrközpont, repülőtér, napelemfarm | – |
| **Mély kanyonok** | vörös homok, színes rétegzett falak | óriás kanyonok, gát | magas falak |
| **Oázis** (nagyon ritka, kicsi) | oázisfű, élő talaj, tiszta víz, fák | az utolsó élő hely: a túlélők menedéke, **élő talaj** és **tiszta víz** (ezek kellenek a hajó farmjához) | – |
| **Elhagyott farmvidék** | barna fű, durva föld | szalmabálák, tanya silóval, kiszáradt földek | – |
| **Olajmező** | kátrányos homok, olajpala | olajkutak tartállyal, kátránytócsák (lassítanak) | – |

**Hangulat:** minden biome-nak saját hangja van (szél a hamusivatagban és a sóban, jégrepedés és nyögő tornyok a megapoliszban, Geiger-ketyegés a kráterben, bugyogó sav a mocsárban, csúszó fémhulladék a szeméthegyeken, a szivattyúk dobbanása az olajmezőn, madárcsicsergés az oázisban), saját részecskéi (hamu, hópelyhek, szentjánosbogarak), és mindenfelé ott a régi világ: **elhagyott autók**, **halottak**, elszabadult **rakományládák**, a tengerfenéken és a megkövült erdőben **kövületek**, a megapoliszban **jégtüskék**.

**Veszélyek:**

- **Savas mocsár:** a vízben sebződsz és megmérgeződsz.
- **Sugárzó kráter:** amíg a biome-ban vagy, lassan sebződsz és éhezel.
- **A teljes űrruha (mind a négy darab) mindkettő ellen véd.**

### Az oázis és a hajó farmja

A Föld talaja halott: kapálni, vetni, ültetni csak a **hajó belsejében** lehet, és ott is csak **élő talajon**.

- **Élő talaj:** az oázisban az oázisfű és az alatta lévő föld ásóval kiásva élő talajt ad. A menedéktelep ládáiban is van.
- **A hajóban** kapával az élő talajból termőföld lesz. Közönséges földet hiába viszel fel, az halott marad.
- **Tiszta víz:** az üres **vizeskannát** (3 alumíniumrúd + 1 üveglap a mintában, 2 db) az oázis vizébe mártva tiszta vizes kanna lesz. Máshol a víz szennyezett.
- **Menedéktelep:** minden oázisban áll egy kis telep: üvegházkupola terménnyel, víztisztító torony, lakómodul ágyakkal, ládákkal és terminállal, napelemek, és három sír. Itt éltek az utolsók, akik az oázist életben tartották.

---

## 5. Nyersanyagok és feldolgozás

A vanilla ércek (vas, szén, gyémánt stb.) **nem generálódnak**. A barlangok és a mélypala megmaradtak, de bennük az űrprogram ércei vannak, mindegyik kő- és mélypala-változatban.

| Érc | Hol | Csákány | Mit ad | Feldolgozás |
|---|---|---|---|---|
| **Bauxitérc** (vörösbarna) | 0–140 magasságban, gyakori | kő | nyers bauxit | kemencében → **alumíniumrúd** |
| **Titánérc** (kékesszürke) | -40–70 között | vas | nyers titán | kemencében → **titánrúd** |
| **Uraninitérc** (zöld, halványan világít) | mélyen, a sugárzó kráterben bőven | vas | nyers urán | kemencében → **dúsított urán** |
| **Spodumenérc** (rózsaszín) | -16–96 között | kő | lítiumpor (2–4) | – |
| **Monazitérc** (narancssárga) | mélyen, -64–20 között | vas | ritkaföldfém-por (1–3) | – |
| **Olajpala** | föld alatt, az olajmezőn sok | kő | bitumen | (később üzemanyag) |
| **Lítiumos sókéreg** | a sósivatag felszínén | kézzel/ásóval | lítiumpor | – |
| **Kvarchomok** | a hamusivatagban | ásó | kvarchomok blokk | kemencében → **szilíciumlapka** |

A Szerencse varázslat növeli a hozamot. A Selyemérintés magát az érctömböt adja.

**Vas és réz csak kimentésből van:**

- fémhulladék → **vasrög** (kemence), 9 rögből vasrúd;
- mentett vezeték → **rézrúd** (kemence);
- rozsdás panel → 2 fémhulladék (kézzel).

**Egyéb hasznos átalakítások:**

- elhalt rönk → 4 elhalt fa deszka (ebből lap, lépcső, kerítés, és minden, amihez deszka kell);
- elhalt rönk → faszén (kemence);
- sókéreg → üveg (kemence).

---

## 6. Romok és zsákmány

A struktúrák ládáiban és hordóiban kimentett technológia, élelem és alapanyag van. Mindegyik típusnak saját zsákmánytáblája van.

| Struktúra | Hol | Jellemző zsákmány |
|---|---|---|
| **Kis bunker** (betonbódé homokzsákokkal; lent zsilip, nappali hálófülkével, konyha, rádiósarok, generátorterem, raktár, mosdó, szellőzőakna) | szinte mindenhol | fémhulladék, vezeték, áramkör, javítókészlet, gyapjú, élelem |
| **Nagy bunker** (kétszintes kormányzati óvóhely: parancsnoki terem térképasztallal, híradó, hálóterem, étkező, konyha, gyengélkedő; lent generátorcsarnok, víztisztító, fegyverraktár, archívum, műhely; felszíni blokkház) | hamusivatag, megkövült erdő, kanyon, farmvidék, szeméthegyek, megapolisz | katonai felszerelés, javítóállomás, élelem |
| **Űrközpont** (kilövőállás lángárokkal, 45 m-es szervizto­rony, három darabba tört rakéta, villámhárító-tornyok, víztorony, üzemanyaggömbök, kupolás irányítóbunker lépcsőzetes konzolsorokkal, lelátó, rakétakert) | sósivatag, hamusivatag | titánlemez, alumínium, űrruhadarabok, mágnes, akkumulátor, **ritkán ionhajtómű-készlet** |
| **Kutatólabor** (üvegfalú előcsarnok recepcióval és karanténkapuval, zsilipes BSL-3 labor, fülkék, mintatartályok, hibernációs kapszulák, emeleten irodák és pihenő, tetőgépház) | megkövült erdő, megapolisz, savas mocsár, sósivatag | áramkör, szilícium, ritkaföldfém, lítium, akkumulátor, napelem |
| **Kutatóállomás** (gólyalábas modulok fedett átjárókkal: lakómodul, labor, energiamodul, garázs a hójáróval, meteorológiai árboc, antenna, zászlósor) | sósivatag, megapolisz, sugárzó kráter, tengerfenék | titán, alumínium, űrsisak, javítókészlet |
| **Magbank** (Svalbard-szerű: ékportál fényművészeti panellel, föld- és jéghalom, hosszú alagút, irányítóterem, három trezor: egy lezárt, egy kifosztott, egy beomlott) | befagyott megapolisz | vetőmagok, csemeték, krumpli, répa, bogyó, csontliszt |
| **Terraformáló kísérlet** (gyűrűs légkörfeldolgozó torony, repedt biokupola halott kerttel, kondenzátortartályok, tükörsorok, vezérlőépület, mérgező túlfolyótó) | sugárzó kráter, savas mocsár, sósivatag, hamusivatag | laborzsákmány |
| **Napelemfarm** (panelsorok, inverterek, kerített alállomás transzformátorokkal, kidőlt távvezeték-oszlop, vezérlőépület, beragadt tisztítórobot) | sósivatag, hamusivatag, farmvidék | ipari zsákmány |
| **Gát** (íves gátfal koronaúttal és lámpákkal, árapasztó zsilipek, vízkivételi tornyok, szelepház, turbinacsarnok három generátorral, kiszáradt tározó egy hajóval) | mély kanyonok | ipari zsákmány |
| **Rádióállomás** (50 m-es rácsos árboc pihenőkkel, stúdió, adóterem, gépész szobája, generátorok, ledőlt parabolaantenna, drótkerítés) | szeméthegyek, megkövült erdő, megapolisz, farmvidék | áramkör, vezeték, mágnes, vörös por |
| **St. Raphael kórházkomplexum** (hatszintes L alakú épület, sürgősségi bejárat mentőautókkal, kórtermek, intenzív osztály, műtők, helikopter-leszálló a tetőn, hullaház a pincében, drótkerítéses karanténtábor sátrakkal) | megapolisz, hamusivatag, farmvidék, savas mocsár | orvosi zsákmány, aranyalma, feljegyzések |
| **Katonai tábor** (szögesdrót, kapu őrbódéval, két barakk, étkező konyhával, fegyverraktár, gépjárműszín teherautókkal és dzsippel, őrtornyok, helikopter-leszálló roncsolt helikopterrel) | hamusivatag, kanyon, megkövült erdő, szeméthegyek | fegyver, páncél, TNT, titánlemez |
| **Aurora International repülőtér** (220 blokkos kifutó a végén kiégett, három darabra tört géppel, gurulóút, betonplatform parkoló utasszállítóval, háromszintes terminál utasbeszálló hidakkal és tábori kórházzal, 44 m-es irányítótorony, hangár, üzemanyagtelep, parkoló autóroncsokkal) | sósivatag, kiszáradt tengerfenék (nagyon ritka) | városi, rakomány-, ipari és labor-zsákmány |
| **Olajkút** (valódi alakú himbás kút, tárolótartályok, csővezeték, kezelőfülke, kiömlött olaj) | olajmező | ipari zsákmány, bitumen |
| **Tanya** (kétszintes lakóház tornáccal, konyhával, nappalival és hálószobákkal; pajta istállóval, szénapadlással és traktorral; siló, szélkerekes kút, madárijesztő, karám) | farmvidék | vetőmag, bőr, gyapjú, eszközök |
| **Lezuhant teherkapszula** (kráterben, szétterült ejtőernyővel), **műhold** (széttört napelemszárnyak a csúszásnyom végén), **rakétafokozat** (orral a földbe fúródva, bejárható szerelvényrekesz) | mindenhol | élelem, fémhulladék, alumínium, napelem |
| **Automata bánya** (szállítótorony kötélkerékkel, akna, sínes táró drónokkal, drónhangár töltőállásokkal, szalagszállító, meddőhányók, vezérlőkonténer) | szeméthegyek, kanyon, olajmező | ipari zsákmány; a drónokból áramkör, vezeték, akkumulátor |
| **Kolóniák** (benőtt, leégett, befagyott; központi kupola közösségi térrel és hidropónikus gyűrűvel, öt modul csőfolyosókon: hálóterem, rendelő, műhely, rádió, raktár; leszállópálya egy kompronccsal, rover, napelemek) | oázis/savas mocsár, hamusivatag/kráter, megapolisz | élelem, titán, gyapjú, bőr, űrsisak |
| **Konténerszállító hajó** (dőlt testtel, konténerekkel, felépítménnyel, híddal és gépházzal), **kékbálna-csontváz** | kiszáradt tengerfenék | rakomány, iránytű, térkép, arany |
| **Holt megaváros** (kereszteződések közlekedési lámpákkal, sugárutak autóroncsokkal, katonai ellenőrzőpontok, leomlott épületek; 90–110 m-es üvegfelhőkarcolók szerverszinttel, irodaházak, panelházak, rendőrség cellákkal, természettudományi múzeum T-rex csontvázzal, üzletsor, park) | befagyott megapolisz, hamusivatag | városi, katonai és labor-zsákmány minden épületben |
| **Bunkerhálózat** (föld alatt: parancsnoki központ őrházzal és aknával a felszínre, sínes alagutak kábeltálcákkal, hálótermek emeletes ágyakkal, étkező, hibernációs terem, gyengélkedő, fegyverraktár, hidropónia, víztisztító, archívum, beomlott alagútvégek) | bármely szárazföldi biome, világonként legalább 3 | bunker-, katonai, orvosi, élelmiszer- és labor-zsákmány |
| **Verdant-laboratórium** (ahol a Szürke Virágzás kiszabadult: négy üvegházcsarnok, a 4-es betörve, háromszintes laborépület, elszigetelő blokk zsilippel, öntözőcsatorna, alatta mintatároló trezor) | savas mocsár, megkövült erdő, farmvidék, világonként legalább 2 | labor- és kutatóállomás-zsákmány, magok |
| **Metróhálózat** (állomás lépcsővel, alagutak) | megapolisz, hamusivatag, farmvidék, szeméthegyek | városi zsákmány, élelem |

**A Föld 20 000 blokk széles** (világhatár ±10 000). A ritka nagy romokból (repülőtér, kórház, megaváros, űrközpont, magbank, gát, kolóniák, oázis-menedék, bunkerhálózat, Verdant-labor) minden világban garantáltan van legalább egy-kettő, a középponttól pár kilométerre, a legsíkabb közeli helyen.

### Történet: feljegyzések, adatszalagok, terminálok, kézikönyvek

A romokban megtudhatod, mi történt a Földdel (Halcyon, a Szürke Virágzás, a Hamutüdő, a Rövid Háború, az Exodus).

- **Feljegyzés** (kézírásos cetli): jobb klikk kézben → lapozható olvasó (nyilak, görgő, ←/→ billentyűk).
- **Adatszalag**: csak **adatterminálban vagy rádióban** játszható le: kézben a szalaggal kattints jobb gombbal a gépre. A gépből szól (a közelben mindenki hallja): kattanás, zúgás, a beszélők torzított rádióhangja és a helyszíni effektek. Közben felirat jelenik meg a képernyő alján. A kezdő hordóban van egy **rádió** és egy szalag.
- **Műszaki kézikönyv**: a hajó minden rendszeréhez (navigáció, indítási lista, kitérő manőverek, javítás, reaktor és
  hűtés, űrruha, diagnosztika, hajtómű-fejlesztés, farm). A kezdő hordóban nyolc van, a hajtómű-kézikönyv űrközpontokban.
- **Adatterminál**: a bunkerekben, laborokban, kórházban, rádiótoronyban, magbankban, kolóniákban, oázisban és
  űrközpontban egy-egy terminál a hely saját naplóját mutatja (jobb klikk).
- A tárgy nevén túl a leírásában (tooltip) a címe is látszik, így tudod, mit találtál.

Majdnem minden ládatípusban van esély egy, a helyhez illő feljegyzésre, szalagra vagy kézikönyvre (a kórházban az
ápolónő naplója, a rádiótoronyban a Bárka-2 utolsó adása, az oázisban a gondnok üzenete stb.).

**Városi berendezések:** az új romokban 49 új blokk van (repedt és kormos beton, aszfalt útburkolati jelekkel, üvegfal, csempék, irodai bútorok, kórházi ágyak, infúziós állványok, autó- és mentőautó-roncsok, sugárhajtómű, közlekedési lámpa, homokzsák, szögesdrót, karanténsorompó, plakátok). Kibányászva megkapod őket, így építhetsz is belőlük.

**Csontvázak:** a romokban emberi maradványok (fekvő, falnak dőlve ülő, összegömbölyödött, szkafanderes) és állatok
(kutya, tehén, ló) mesélik el az utolsó perceket. Csontot adnak, ha kibányászod őket.

**Elszórt törmelék:**

- **fémhulladék-kupac:** 1–3 fémhulladék, néha vezeték;
- **törmelék:** kavics, néha fémhulladék;
- **elromlott bányászdrón:** áramkör, vezeték, fémhulladék, néha akkumulátorcella.

---

### Energia, szétszerelés és bontás

**Áramhálózat.** A gépek kábellel (a hajós fekete-sárga kábel, 3 mentett vezeték = 6 kábel) kapcsolódnak az áramforrásokhoz; egymás mellett állva kábel nélkül is.

| Áramforrás | Teljesítmény | Mire figyelj |
|---|---|---|
| **Napelem** | 2 kW | csak nappal, szabad ég alatt |
| **Dízelaggregátor** | 8 kW | üzemanyag kell (jobb klikk vele): szén/faszén, bitumen, szénblokk, fa; csak akkor éget, ha kell az áram |
| **Akkumulátortelep** | tárol | a felesleges napenergiát eltárolja, és hiánykor visszaadja |

Jobb klikk üres kézzel bármelyikre: állapot (üzemanyag, töltöttség).

**Szétszerelés a helyszínen.** A roncsokat, gépeket és berendezéseket (autók, sugárhajtómű, szerverszekrény, italautomata, aggregátor, terminál, bányászdrón, közlekedési lámpa, hibernációs fülke stb.) **lépésenként** szedheted szét:
- **multiszerszám**: csavarok, burkolatok, áramköri lapok;
- **vágópisztoly**: fémvágás (recept: mentett vezeték + fémhulladék + akkumulátorcella).

Minden kattintás egy lépés, és mindegyik ad alkatrészeket. Ha rossz a szerszám, a kijelző megmondja, mi kell. Az utolsó lépés után a tárgy eltűnik.

**Bontógép.** Ami alkatrész vagy talált tárgy, azt nyersanyagra bontja: a fémhulladékból vasrög lesz, a vezetékből réz, az áramkörből szilícium, arany és réz, az üvegszilánkból üveg, a műanyagból és gumiból bitumen, és így tovább (kb. 100 féle tárgyat ismer).
- **Betöltés:** jobb klikk a tárggyal (egyszerre az egész köteg), legfeljebb 4 köteg fér bele.
- **Üres kézzel:** állapot (áram, sor, kihozatal, modulok). Guggolva üres kézzel visszakapod a sort.
- **Áram:** 4 kW kell, plusz 2 kW modulonként.
- **Kimenet:** a kész anyag a mellette álló ládába vagy hordóba megy, ha nincs ilyen, a tetején kiesik.
- **Kihozatal:** alapból 55–75%.
- **Modulok** (a bontógép mellé, bármelyik oldalára téve):
  - **daráló**: kétszer gyorsabb;
  - **olvasztó**: +15% kihozatal, a fémhulladék rögtön vasként jön ki;
  - **szétválogató**: +10% kihozatal, és kijönnek a ritka anyagok is (ritkaföldfém, arany, izzópor stb.).
- Mindhárom modullal közel 100% a kihozatal.

### Kármán-űrkikötő és az anyahajó

Világonként 3 hatalmas űrkikötő van, a középponttól kb. 6–8 km-re (460 × 340 blokk). Itt indultak a Bárkák, és itt építheted meg a saját anyahajódat. Több űrkikötőt is helyreállíthatsz.

**Helyreállítás** (sorrendben, mindegyiknek saját vezérlőszekrénye van; jobb klikk rá: ellenőrzőlista):
1. **Erőmű** (kupolás reaktorépület): kábel, áramkör, titánlemez, urán fűtőelem; kalibrálás nyomás-minijátékkal.
2. **Indítóközpont**: áramkör, vezeték, üvegszilánk, akkumulátor; vezetékes kalibrálás. Itt van a **kézikönyv-nyomtató** (papírral nyomtat).
3. **Gyártócsarnok**: titánlemez, mágnes, áramkör, vas; szelepes kalibrálás. Itt van a **gyártógép**.
4. **Üzemanyagtelep**: cső, fémhulladék, alumínium, áramkör. Utána **hajtóanyagot** tárol: bitumen +2, hélium-3 regolit +12 egység.
5. **Szerelődaru** (az összeszerelő csarnokban): kábel, titán, akkumulátor, mágnes.

Minden szekrény körül **törmeléket** (törmelék, törött beton) kell eltakarítani, és kell, hogy az előző létesítmény működjön. Az **erőmű** urán fűtőelemet éget (egy rúd kb. három napig tart), és időnként **meghibásodik**: ilyenkor javítókészlettel kattints a szekrényre, és jön egy szelepes minijáték. Ha az erőmű áll, az egész űrkikötő leáll.

**Az anyahajó** (kb. 350 m):
- A **gyártógépen** készülnek a szekciókészletek: guggolás + jobb klikk vált a készletek között, utána leadod az anyagokat.
- A **hajtómű-, reaktor- és laborkészlethez holdi anyag kell** (hélium-3 regolit, ilmenit, mare bazalt).
- A **darukonzolnak** sorban add a készleteket: gerinc, törzs, hajtóművek, reaktor, életfenntartás, farm, labor, szállás, hangár, híd. A daru látványosan, blokkról blokkra építi fel a csarnokban (a törzs a legnagyobb, néhány perc).
- Ha mind a tíz kész, és a telepen legalább 600 hajtóanyag van: **guggolás + jobb klikk üres kézzel a darukonzolra = indulás**. A közelben állók a fedélzetre kerülnek.
- **A fedélzeten:**
  - A hídon áll a **kormányállás**: guggolva irányt választasz (Föld körüli vagy Hold körüli pálya), sima kattintással indulsz. Az út kb. 10 perc, közben események történnek.
  - A hangárban a **hangárlift** levisz a pálya alatti égitestre. Egy lerakott hangárlift (recept: titán, antenna, áramkör, mágnes, akkumulátor) bárhonnan visszavisz a hangárba.
  - **Berendezett fedélzetek:** életfenntartó üzem (légtisztítók, oxigéngenerátorok, víztartályok, vezérlőterem), hidropónikus ágyások termő növényekkel és öntözőcsatornával, üvegfalú laborok (biológia, anyagvizsgálat, megfigyelő, a vezető kutató irodája), kabinsor (ágy, szekrény, íróasztal minden kabinban), étkező konyhával, gyengélkedő, társalgó, hangár leszállóbölcsőkkel és irányítófülkével, lépcsőzetes híd állomásokkal és hologram-asztallal.
  - A farmfedélzeten az élő talaj megművelhető.

## 7. Receptek

A receptek a receptkönyvben is megjelennek. Az alábbi táblázatok a pontos hozzávalókat mutatják.

### A hajó felszerelése

| Tárgy | Hozzávalók |
|---|---|
| Javítókészlet | 2 fémhulladék, 1 mentett vezeték, 1 vasrúd, 1 mentett áramkör |
| Javítóállomás | 5 fémhulladék, 1 vasrúd, 2 vezeték, 1 áramkör |
| Diagnosztikai konzol | 5 fémhulladék, 1 üveglap, 2 áramkör, 1 vörös por |
| Navigációs konzol | 3 fémhulladék, 2 üveglap, 1 iránytű, 2 áramkör, 1 vezeték |
| Multiszerszám | 2 vasrúd, 1 áramkör, 1 vezeték, 3 fémhulladék |
| Rozsdás panel (2 db) | 4 fémhulladék |

### Űrruha

| Darab | Hozzávalók |
|---|---|
| Űrsisak | 2 fémhulladék, 1 áramkör, 2 gyapjú, 1 üveglap |
| Űrruha (oxigéntartály) | 5 gyapjú, 2 vezeték, 1 vasrúd, 1 fémhulladék |
| Űrnadrág | 6 gyapjú, 1 fémhulladék |
| Űrcsizma | 2 gyapjú, 2 fémhulladék, 2 bőr |

Állatok nincsenek, ezért gyapjút és bőrt a romokban találsz: bunker, város, tanya, kolónia, kórház. A kezdőhordóban is van gyapjú.

### Alkatrészek

| Alkatrész | Hozzávalók | Darab |
|---|---|---|
| Titánlemez | 2 titánrúd (formátlan) | 1 |
| Napelemcella | 3 üveglap, 3 szilíciumlapka, 2 alumíniumrúd, 1 vezeték | 2 |
| Akkumulátorcella | 1 rézrúd, 4 alumíniumrúd, 2 lítiumpor | 1 |
| Ritkaföldfém-mágnes | 5 ritkaföldfém-por, 1 vasrúd | 2 |
| Urán fűtőelem | 2 titánlemez, 1 dúsított urán | 1 |
| **Ionhajtómű-készlet** | 4 titánlemez, 1 mágnes, 2 akkumulátorcella, 1 áramkör, 1 napelemcella | 1 |
| **Fúziós hajtómű-készlet** | 4 titánlemez, 1 hélium-3 regolit (Hold), 2 urán fűtőelem, 1 áramkör, 1 mágnes | 1 |

### Titán szerszámok

Ugyanaz a minta, mint a vas szerszámoknál, titánrúddal: csákány, fejsze, ásó, kapa, kard. Valamivel jobbak a vasnál, és jóval tartósabbak (900 használat).

---

## 8. Kopás, meghibásodás, javítás

**Kopás:**

- Minden repülés koptatja az alrendszereket; a hajtómű repülésenként kb. 8%-ot veszít.
- Ha a **hajtómű** vagy a **leszálló- és indítóegység** 20% alá esik, a hajó nem tud felszállni.

**Incidensek repülés közben:**

- Repülés közben 10 másodpercenként esély van egy incidensre a sérült alrendszereken. Minél rosszabb az állapot, annál nagyobb az esély; 70% felett nincs esély.
- Az incidens típusai: rövidzárlat, túlmelegedés, szivárgás, nyomásvesztés, tűz.
- **60 másodperc** van a javításra, különben az alrendszer 25%-ot veszít.

**Hol jelenik meg a hiba?** Minden incidens a hajó egy konkrét pontján látszik, az alrendszer helyén:

| Incidens | Mit látsz | Mivel javítod a helyszínen | Minijáték |
|---|---|---|---|
| Rövidzárlat | **szikrázó panel** a falon (szikrák, sercegés) | **forrasztópáka** | vezetékezés |
| Túlmelegedés | **gőzölgő szellőző** a falon (gőz, sziszegés) | **multiszerszám** | szelep |
| Szivárgás, nyomásvesztés | **szivárgó cső** a mennyezeti hűtőcsövön | **tömítőhab** | nyomás |
| Tűz | **elektromos tűz** a padlón (láng, füst) | **poroltó** (vagy egy vödör víz) | nincs, azonnal elalszik |

- Kattints jobb gombbal a hibára a megfelelő szerszámmal. A **javítókészlet** mindenhol működik (helyette).
- Ha rossz eszköz van a kezedben, a chat megmondja, mi kell.
- A **javítóállomás** is megmaradt: oda vive a készletet a legsürgősebb hibát javítja, ugyanúgy, mint eddig.
- Ha a hibát megjavítottad (bárhogyan), a fal vagy a cső visszaáll.

**Szerszámok receptjei:**

| Szerszám | Hozzávalók | Tartósság |
|---|---|---|
| Forrasztópáka | 1 rézrúd, 1 vasrúd, 1 vezeték (átlósan) | 32 javítás |
| Tömítőhab (2 db) | 1 bitumen + 1 üvegpalack | elfogy |
| Poroltó | 1 vasrög, 5 alumíniumrúd, 1 vödör | 12 oltás |
| Légszűrő-patron | 2 gyapjú + 1 faszén | – |

A kezdőhordóban is van forrasztópáka, tömítőhab, poroltó, légszűrő, cső és kábel.

**Multiszerszám:**

- A második kézben tartva a 40% feletti könnyű hibákat minijáték nélkül javítod.
- A súlyosabb hibáknál kisebb táblát kapsz.
- 48 használatig bírja.

---

### A hajó rendszerei: reaktor, hűtés, életfenntartás, csövek, kábelek

A **gépteremben** (a hajó farában):

| Gép | Hol | Mennyi | Mivel töltöd |
|---|---|---|---|
| **Hajóreaktor** | középen, a rács mögött | 8 egység, repülésenként 1 | urán fűtőelem: +4 |
| **Hűtőfolyadék-tartály** | bal hátsó sarok | 4 egység, repülésenként 1 | tiszta vizes kanna vagy jégtömb (jég, tömör jég, kék jég, hótömb): +2 |
| **Életfenntartó egység** | jobb hátsó sarok | 4 egység, repülésenként 1 | légszűrő-patron: +4 |

Jobb klikk üres kézzel: kiírja, mennyi van benne, és mivel töltheted. A tesztrepülés nem fogyaszt.

**Csövek és kábelek.** A hűtőcsövek (rézszínűek) maguktól összeillenek egymással és a gépekkel. Kábelek (fekete-sárgák) ugyanígy.

- A **hűtőcsőnek** össze kell kötnie a reaktort **mindkét hajtóműcsatlakozóval** (a hátsó fal két fúvókája) és a **hűtőtartállyal**.
- A **tápkábelnek** a reaktortól el kell érnie a **hídig** (a mennyezeten fut végig, a válaszfalakon át).
- Csövet és kábelt ki lehet bontani és újra lerakni (hajón kívül is, bárhol). Ha megszakad a vezeték, a hajó nem indul, és az ellenőrzőlista megmondja, mi hiányzik.
- Recept: 3 fémhulladék → 6 cső; 3 mentett vezeték → 6 kábel.

**Az ellenőrzőlista** ezeket is nézi: üzemanyag, hűtőfolyadék, légszűrő, ép csővezeték és kábel.

---

### Nehézség és legénység

A nehézséget az operátor állítja: `/iss difficulty easy|normal|realistic` (paraméter nélkül kiírja a jelenlegit).

| Szint | Mit jelent |
|---|---|
| **Könnyű** | a kapcsolók maguktól bekapcsolnak, a robotpilóta magától kitér, kevesebb incidens, a készletek kétszer annyi ideig tartanak |
| **Normál** | az alap |
| **Realisztikus** | több incidens, egyszerre több, sűrűbb meteorraj, nagyobb találati sebzés; a robotpilóta csak a konzolról kapcsolható |

**A legénység mérete is számít** (akik a hajóban vannak):

- **Egyedül** a hajó többet intéz magától: ha nincs pilóta, a robotpilóta kitér (realisztikus szinten nem), és egyszerre legfeljebb 2 incidens lehet.
- **Többen** több minden romlik el, és egyszerre is: minden új ember +35% incidensesélyt hoz, és eggyel több párhuzamos hibát (legfeljebb 4). Érdemes felosztani a munkát: pilóta, gépész, javító.

---

## 9. Rejtett hibák, diagnosztika, tesztrepülés

**Rejtett hibák:**

- Egy kopott (70% alatti) alrendszer minden leszállásnál **rejtett hibát** kaphat.
- Ez sehol sem látszik, de növeli a **felszállási kockázatot**.
- Ha a felszállás sikertelen, robbanás van az indítóhelyen (blokkot nem tör, de élőlényeket sebez). A hajó a helyén marad, és súlyosan sérül.

**Diagnosztikai konzol** (jobb klikk, a hajóban):

- Átvizsgálja a hajót. Egy rejtett hibát 50% + a navigáció állapotának fele eséllyel talál meg.
- Kiírja az ismert hibákat, és megbecsüli a kockázatot, de csak abból, amit megtalált.
- Percenként egyszer használható.

**Tesztrepülés:**

- **Guggolva kattints kétszer a diagnosztikai konzolra 5 másodpercen belül.**
- A hajó felszáll, és ugyanott leszáll (kb. 30 másodperc).
- Minden rejtett hiba előjön incidensként, így otthon, biztonságban javíthatod őket.
- A tesztrepülés felszállási kockázata feleakkora, és a kopás is fele.

**Jó gyakorlat egy nagy út előtt:** diagnosztika → javítás → tesztrepülés → javítás → indulás.

---

## 10. Navigáció és utazás

Kattints jobb gombbal a **navigációs konzolra** (a hídon). Megnyílik a **csillagtérkép**:

- **felül:** a Naprendszer sávja (bal oldalt a Nap);
- **lista:** minden célpont az állapotával és az utazási idővel a hajód hajtóművével;
- **alul:** a kiválasztott célpont részletei (indítási ablak, időtartam);
- **IRÁNY BEÁLLÍTÁSA** gomb: csak akkor zöld, ha a hajó le van szállva és indítható.

### Indulás: az ellenőrzőlista

1. **Irány:** a navigációs konzolon kattints a célra, majd az **IRÁNY BEÁLLÍTÁSA** gombra (billentyűzettel: fel/le nyíl, Enter). Ha a hajó nem indulhat (sérült, már úton van stb.), piros szöveg írja ki, miért.
2. **Kapcsolók:** a pilótaülés bal oldalán álló **kapcsolótáblán** kapcsold be mind a négyet. Mindegyik oda kattintva vált, ahol a kapcsoló van, balról jobbra: fő áramellátás, üzemanyag-szivattyúk, avionika, életfenntartás. Zöld = be, piros = ki.
3. **Pilóta:** ülj be a **pilótaülésbe** (a középső ülés a navigációs konzol előtt).
4. **Tolóerő:** a jobb oldali **tolóerőkart** kattintásonként egy fokkal told előre. Teljes tolóerőnél indul a visszaszámlálás; ha valami hiányzik, a kar visszaugrik, és a chat felsorolja, mi van még hátra.

Leszállás után a kapcsolók kikapcsolnak, a kar visszaáll: minden útnak a lista az eleje.

Vészmegoldás parancsként, a hajóból: `/iss nav moon` (célok: `earth`, `earth_orbit`, `moon`) azonnal indít, a lista nélkül. A térkép jobb alsó sarkában a mod verziója látszik (pl. `v1.0.0+build.70`), így látod, melyik build van fent.

**Mi történik utána:**

| Fázis | Mennyi ideig | Mit látsz |
|---|---|---|
| Visszaszámlálás | 10 mp | sípolás, az utolsó 3 mp-ben a kabin rázkódik |
| Felszállás és repülés | a cél szerint | hajtóműzaj, a talaj elmarad, csillagok, a cél közeledik |
| Leszállás | 5 mp | rázkódás, feljön a talaj |
| Leszállt | – | üzenet a célról; a hajótest ott áll, ahol landoltál |

Repülés közben **nem lehet kiszállni**. Közben dolgozhatsz a hajóban: javíthatsz, pakolhatsz, figyelheted az ablakot.

**Utazási idők** az alap (1-es) hajtóművel, a Földről:

| Cél | Idő | Megjegyzés |
|---|---|---|
| Föld körüli pálya (ISS) | kb. 0:30 | dokkolás az ISS-hez |
| Hold | kb. 8–9 perc | leszállás a Nyugalom tengerén |
| Többi bolygó | órák, napok | **még zárva**, ezek a világok később jönnek |

Az idő a **játékidővel** telik: ha a szerver megáll, az út is. Az indítási ablakon kívül az út legfeljebb 50%-kal hosszabb, és a konzol mutatja, mikor lesz a következő jó ablak.

**Utazási események** (két percnél hosszabb utaknál, percenként esély):

| Esemény | Mi történik | Mit tehetsz |
|---|---|---|
| **Meteorraj** | 30 mp-ig sziklák zúdulnak a hajó felé; minden találat -5% burkolat, -1% navigáció | **kormányozz a pilótaülésből**: W fel, S le, A balra, D jobbra. A sziklákat látod az ablakon és a jobb felső **radaron** (piros: ütközőpályán). Pilóta nélkül a hajó sodródik; a navigációs konzolon a **robotpilóta** bekapcsolható (üzemanyagba kerül, és nem tér ki mindenből) |
| **Napkitörés** | sérül az elektronika és az életfenntartás | – |
| **Rendszerhiba** | incidens a legrosszabb alrendszeren | javítsd meg |
| **Sodródó roncs** | zsákmány érkezik a zsilipen át | szedd fel |
| **Furcsa jelek** | töredékek arról, mi történt a többi expedícióval | – |

---

## 11. Űrruha és oxigén

**Vákuum** van a pályán és a Holdon. Itt nincs vízalatti fulladás és nincsenek buborékok, helyette:

- **Hipoxia:** teljes ruhában, de üres tartállyal kb. 15 másodpercig bírod. A kép széle elsötétül és a szívveréssel együtt lüktet, zihálsz, lelassulsz, végül sebződsz („kifogyott az oxigénből”).
- **Dekompresszió:** ha hiányzik egy ruhadarab (pl. a sisak), pár másodperc alatt elfogy a levegőd. A kép bederesedik és villódzik, cseng a füled, és gyorsan sebződsz.
- Az **űrruha (mellrész) az oxigéntartály**: vákuumban másodpercenként egy pontot fogy, teli állapotban kb. 10 percig bírja. **A hajóban újratöltődik**, ha az életfenntartás legalább 20%-on van.
- **Sisak-kijelző:** sisakban a bal oldali ív az oxigén, a jobb oldali a ruha tömítése és nyomása. Lent figyelmeztetés jelenik meg (SZIVÁRGÁS, KEVÉS O2, HIPOXIA).
- **Sérülő ruha:** egy erősebb ütés (3+ sebzés) 30% eséllyel kilyukasztja a ruhát. Ilyenkor sziszeg, és négyszer olyan gyorsan fogy az oxigén. **Tömítőhabbal** (jobb klikk a kezedben) befoltozod.
- A teljes ruha a Földön **a sav és a sugárzás ellen is véd**.
- **Sisakban nem lehet enni és inni.** Előbb vedd le (a hajóban vagy ahol van levegő).
- **Hangok:** a ruhában hallod a saját légzésedet; ha 20% alá fogy az oxigén, a ruha sípolva riaszt.

---

## 12. Föld körüli pálya és az ISS

Válaszd a navigációs konzolon a **Föld körüli pálya (ISS)** célt. A hajó felmegy pályára, és dokkol a régi **Nemzetközi Űrállomáshoz**. A hajóajtó a dokkolóporton lesz; kilépve az állomáson vagy.

- **Súlytalanság:** pályán nincs gravitáció és esési sebzés. A hajóban a stabilizátor ad mesterséges gravitációt; ha 20% alá esik, a hajóban is csökken.
  - **Kapaszkodás:** ha egy blokk karnyújtásnyira van, a mozgásgombokkal húzod magad (előre a nézés irányába, ugrás fel, lopakodás le); ha elengeded, megállsz.
  - **Sodródás:** ha minden messze van, semmi nem lassít: amerre ellökted magad, arra mész tovább. A sisak kiírja: **SODRÓDSZ – NINCS KÖTÉL**.
  - **Biztosítókötél** (recept: vas + gumi): pályán egy blokkra használva rácsatolsz. 16 m kötél, minden nálad lévő **kötéltekercs** +16 m (legfeljebb 3, azaz 64 m). A kötél megállít és visszahúz; sárga vonal látszik. Lopakodás + használat: lecsatolás. Ha a rögzítési pontot kibontják, a kötél elszakad.
  - **Manőverező egység (MMU)** (recept: alumínium, áramkör, titánlemez, akkumulátor, két nitrogéntartály): ha nálad van, kapaszkodó nélkül a mozgásgombok a fúvókákat működtetik. Hajtógázt fogyaszt (a sisak mutatja), két **nitrogéntartállyal** utántölthető.
  - **Törmelékáram:** néhány percenként a pálya egy szétesett műholdak törmelékfelhőjét keresztezi. Előtte figyelmeztetés jön (sisak + chat), utána 20 másodpercig aki kint van, és nincs semmi takarásban a törmelék irányából, eltalálható: sebzés, és gyakran **kilyukad a ruha** (tömítőhab!). Bújj be az állomásba vagy egy roncs mögé.
- **Roncsmező az ISS körül** (külön repülés nélkül, kötéllel és MMU-val érhető el): öt **törmelékfelhő** lépcsőként az állomás körül (mindegyikre rá lehet csatolni), mögöttük:
  - a **HALCYON** régi orbitális labor (lakómodul, labormodul, letört napelemszárny; benne a parancsnok naplója és ellátmány),
  - egy **halott közvetítőműhold**, egy **kiégett felsőfokozat**, egy **nagy űrtávcső** (kinyitott fedéllel, műszerekkel), egy **sodródó személyszállító kapszula**.
- **Az ISS valódi méretben** (1 blokk = 1 méter, kb. 120 × 75 méter):
  - elöl a dokkolóadapter, ahová a hajód kapcsolódik, utána Harmony, Destiny, Unity, majd az aranyfóliás orosz szegmens (Zarja, Zvezda);
  - oldalt Columbus, Kibo a raktármodullal és a nyitott kísérleti platformmal, Tranquility az alatta lévő **Cupolával** (hét ablakos kilátókupola, lefelé a Földre), és a Quest zsilip;
  - fölül a Poisk, lent a Nauka és a Rasszvet;
  - fölötte a 109 méteres rácsos főtartó a forgócsuklókkal, a fehér hűtőpanelekkel és **nyolc darab 35 méteres napelemszárnnyal**.
- A modulok belül bejárhatók (3 × 3-as folyosó). **Tíz ellátmányhordó** van bennük: zsákmány, élelem, űrruhadarabok, multiszerszámok.
- Régi világban a kis állomást a következő indításkor lecseréli a valódi méretű; ha a hajód épp ott dokkolt, átkerül az új dokkolóporthoz.
- **Az állomás minden blokkja kibányászható** kimentett anyagnak.
- Pályáról a **Föld** cél visszavisz az utolsó földi leszállóhelyedre, a **Hold** cél pedig a Holdra.

---

**Az űr látványa:** pályán, a Holdon, az anyahajón és a hajó ablakain át a Tejút sávja, színes ködök és sűrű csillagmezők látszanak; utazás közben por és jégszemcsék suhannak el a hajó orra előtt.

---

## 13. A Hold

Válaszd a **Hold** célt a Földről vagy pályáról. A hajó a **Nyugalom tengerén** száll le, az Apollo–11 és a Tranquility bázis romjai között.

- **A világ:** 5500 blokk széles. Szürke regolitfelföldek, sötét tengerek (mare), kráterek kiemelkedő peremmel (a fiatalok fényes kidobott anyaggal).
- **Nyersanyag:**
  - **hélium-3 tartalmú regolit** a felszín közelében: ez kell a fúziós hajtóműhöz;
  - **ilmenit** a mare bazaltban.
- **Ég:** fekete, mindig csillagos, és a Föld egy helyben áll rajta, fázisokkal. Egy holdi nap 2,95 Minecraft-nap, hosszú, sötét éjszakákkal.
- **Hatod gravitáció és vákuum:** kell az űrruha, és nagyokat lehet ugrani.
- **Relikviák:**
  - az Apollo–11, 12, 14, 15, 16 és 17 valódi leszállóhelyén leszállóegységek, kifakult zászlók, műszercsomagok, a 15–17-nél holdjárók;
  - a **Tranquility bázis** romjai ellátmánnyal, napelemmezővel és adatterminálokkal, amelyek a bázis utolsó napjait mesélik.
  - **további történelmi missziók** a valódi helyükön, táblával: Luna–9 (az első lágy leszállás), Surveyor–3 (az Apollo–12 mellett), Luna–17 a **Lunohod–1** holdjáróval és Luna–21 a **Lunohod–2**-vel (keréknyomokkal), Csang'o–3 a Jütu holdjáróval, Csang'o–4 a túlsó oldalon, Csandrajána–3 (Vikram és Pragján) a déli pólus közelében, SLIM (orral lefelé), Luna–24;
  - a **Bárka–1 kolónia** a déli póluson, a Shackleton-kráter peremén: a leszállólábain álló Bárka (bejárható híd és raktér), regolittal fedett lakókupolák alagutakkal (hálóterem, **hibernációs csarnok 380 alvó telepessel**, üvegház, műhely), jégbánya fúrótoronnyal, poláris naptornyok, leszállópályák, tömegkatapult-sín és a Föld felé forduló antennaárboc. A terminálok elmondják, mi történt velük.

A Holdról a **Föld körüli pálya** vagy egyenesen a **Föld** felé indulhatsz vissza.

---

## 14. Hajtómű-fejlesztés

A hajtómű szintje osztja az utazási időt:

| Szint | Szorzó | Hogyan |
|---|---|---|
| 1 | ×1 | alap |
| 2 | ×3 | **ionhajtómű-készlet** (Föld érceiből) |
| 3 | ×10 | **fúziós hajtómű-készlet** (urán a Földről + hélium-3 a Holdról) |
| 4 | ×30 | később (a külső bolygókhoz) |

**Beszerelés:**

- Fogd a készletet a kezedbe, és kattints jobb gombbal a **navigációs konzolra**, miközben a hajó **le van szállva**.
- A szinteket sorban kell beszerelni: a fúziós előtt az ionhajtóművet.
- A készlet elfogy, és a konzol fejlécében az új szint látszik.

**Az ionhajtóműhöz vezető út röviden:**

1. bauxit → alumínium;
2. titán → titánrúd → titánlemez;
3. spodumen → lítium;
4. kvarchomok → szilícium;
5. monazit → ritkaföldfém;
6. ezekből akkumulátorcella, napelemcella és mágnes;
7. végül az **ionhajtómű-készlet**.

---

## 15. Parancsok

| Parancs | Ki használhatja | Mire jó |
|---|---|---|
| `/iss nav <cél>` | mindenki (a hajóban) | indulás a célhoz, mint a navigációs konzolról (`earth`, `earth_orbit`, `moon`) |
| `/iss ship info` | mindenki | a hajó osztálya, az alrendszerek állapota, aktív incidensek |
| `/iss ship set <alrendszer> <érték>` | operátor | alrendszer állapotának beállítása (tesztelésre) |
| `/iss ship fly <x> <z>` | operátor | repülés egy földi koordinátára (a hajóból) |
| `/iss ship engine <1-4>` | operátor | hajtómű-szint beállítása (tesztelésre) |
| `/iss ship meteors` | operátor | meteorraj indítása repülés közben (tesztelésre) |
| `/iss difficulty [szint]` | mindenki megnézheti, operátor állíthatja | nehézség: `easy`, `normal`, `realistic` |

Alrendszer nevek: `engine`, `fuel_energy`, `hull_armor`, `life_support`, `navigation_comms`, `stabilizer`, `landing_launch`, `cargo`.

---

## 16. Tippek és gyakori kérdések

**Nem találom a mod tárgyait kreatívban.**
Az InterSolarSystem fülön vannak (sisak ikon), szükség esetén lapozz a fülsoron.

**Régi világban vagyok, és nincs halott Föld.**
A világgenerálás csak új világban működik. A régi világban a hajó, a pálya és a Hold működik. A régi egyszobás kabin a következő beszálláskor lecserélődik az új hajóra (ami a régi kabinban volt, elvész).

**Nem tudok felszállni.**
Nézd meg a HUD-ot: a hajtómű vagy a leszálló- és indítóegység 20% alatt van? Javítsd meg. Repülés vagy visszaszámlálás közben új utat nem lehet indítani.

**Felszállás közben felrobbant a hajó.**
Rejtett hibád volt. Legközelebb diagnosztizálj és tesztrepülj (lásd [9. fejezet](#9-rejtett-hibák-diagnosztika-tesztrepülés)).

**Fulladok a pályán vagy a Holdon.**
Mind a négy űrruhadarab kell, és az oxigén a hajóban töltődik.

**Honnan szerezzek vasat?**
Fémhulladékból kemencében vasrög lesz. Fémhulladék van a romokban, a fémhulladék-kupacokban, és a rozsdás panelekből is.

**Honnan szerezzek gyapjút, bőrt?**
Bunkerekből, városból, tanyákból, kolóniákból, kórházakból.

**Hol van élelem?**
A romokban (bunkerek, kapszulák, metró, kolóniák), és az oázisban (tök, cukornád). A magbankban és a tanyákon vetőmagot találsz, abból farmolhatsz.

**Éjszaka nem jön semmi?**
Így van: a halott Földön most nincsenek élőlények. Saját lények később jönnek.

**Kiszabadultam a hajóból, és a semmiben lebegek.**
Ha a hajó terén kívülre kerülsz, a játék fél másodpercen belül visszatesz a beszállási pontra.

---

## 17. Ami még nincs kész

- **Saját élőlények** (mutánsok, drónok): most a világ üres.
- A **többi bolygó** (Mars, Vénusz, Merkúr, aszteroidaöv, külső bolygók): a listában látszanak, de zárva vannak.
- **Több hajóosztály** és bővíthető belső tér: most egy alap hajó van.
- **Bányászdrónok megjavítása**, olajfinomítás üzemanyaggá, víz- és levegőszűrés.
- **A 4-es hajtómű-szint** beszerezhető alkatrésze.
- A **kutatóhajó** készlete és dokkolása az anyahajó hangárjába; az anyahajós utazás eseményeinél valódi javítási feladatok.
- A **Bárka–1** telepeseinek felébresztése (a terminálok már utalnak rá).

A fejlesztés állapota a [`docs/roadmap.md`](docs/roadmap.md)-ben követhető.
