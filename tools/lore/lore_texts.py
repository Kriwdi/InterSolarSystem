"""The story of the dead Earth: notes, data tapes, terminal logs and technical manuals, in English and Hungarian.

Each entry: id -> {"en": {...}, "hu": {...}} with title, subtitle and pages (tapes: lines of (ticks, speaker, text)).
make_lore.py writes them to assets/intersolarsystem/lore/<language>.json; German falls back to English.
"""

NOTES = {
    "bunker7": {
        "en": {"title": "Bunker 7 - last entry", "subtitle": "Dr. Elena Ward, bunker physician", "pages": [
            "To whoever wakes up next to the wreck:\n\nYou were in the last cryo bay of Bunker 7. The filters gave out on day 2,914. I was the last one awake, so I carried you out myself.",
            "The ship outside is an Aurora research tender, a spare from the Seed Sower programme. She crashed here years ago with nobody aboard. The autopilot still answers. The hatch still opens.",
            "There is nothing left here. The soil is grey, the seas are dry, the cities are tombs. But the tender can fly. The Moon base had helium-3. The oasis in the south had living soil.",
            "Salvage. Repair her. Learn her. The manuals are in the barrel.\n\nAnd if you ever reach the stars, take a handful of living soil with you. That is all of us now.\n\n- E."]},
        "hu": {"title": "7-es bunker - utolsó bejegyzés", "subtitle": "Dr. Elena Ward, a bunker orvosa", "pages": [
            "Annak, aki a roncs mellett ébred:\n\nA 7-es bunker utolsó hibernációs fülkéjében voltál. A szűrők a 2914. napon adták fel. Én voltam az utolsó ébren, így magam hoztalak ki.",
            "A kinti hajó az Aurora egyik kutatóhajója, a Magvető-program tartaléka. Évekkel ezelőtt zuhant le itt, üresen. A robotpilóta még válaszol. Az ajtó még nyílik.",
            "Itt már nincs semmi. A talaj szürke, a tengerek kiszáradtak, a városok sírok. De a hajó még repül. A holdbázison volt hélium-3. Délen, az oázisban élő talaj volt.",
            "Gyűjts. Javítsd meg. Tanuld meg. A kézikönyvek a hordóban vannak.\n\nÉs ha egyszer eljutsz a csillagokig, vigyél magaddal egy marék élő talajt. Most már ez vagyunk mi mindannyian.\n\n- E."]}},
    "halcyon_memo": {
        "en": {"title": "INTERNAL - Halcyon phase III", "subtitle": "Aurora Consortium, climate division, 2058", "pages": [
            "Phase III dispersal approved. 4.1 million tonnes of reflective aerosol over 36 months. Projected cooling: 1.2 degrees.",
            "Risk note (B. Okafor): the monsoon models disagree. Two of five predict a collapse of continental rainfall.\n\nBoard response: the three good models are the newer ones. Proceed.",
            "Handwritten in the margin: 'Two of five is not a rounding error.'"]},
        "hu": {"title": "BELSŐ - Halcyon III. fázis", "subtitle": "Aurora Konzorcium, klímarészleg, 2058", "pages": [
            "A III. fázis szórása jóváhagyva. 4,1 millió tonna fényvisszaverő aeroszol 36 hónap alatt. Várható hűtés: 1,2 fok.",
            "Kockázati megjegyzés (B. Okafor): a monszunmodellek nem egyeznek. Ötből kettő a kontinentális csapadék összeomlását jósolja.\n\nAz igazgatóság válasza: a három jó modell az újabb. Folytassák.",
            "Kézzel a margón: 'Az ötből kettő nem kerekítési hiba.'"]}},
    "verdant_breach": {
        "en": {"title": "Verdant lab - incident 0447", "subtitle": "Containment report, Sector C", "pages": [
            "Strain VRD-7 (carbon-fixing, engineered) detected outside Greenhouse 4. Probable route: irrigation runoff into the river.",
            "Field samples 40 km downstream: VRD-7 has a new behaviour. It breaks down humus. The soil turns grey and loose, like ash. Nothing germinates in it.",
            "We have named it the Grey Bloom. We cannot stop it. We can only document it.\n\nRecommendation: tell the public. Decision: classified."]},
        "hu": {"title": "Verdant-labor - 0447-es incidens", "subtitle": "Elszigetelési jelentés, C szektor", "pages": [
            "A VRD-7 törzs (széndioxid-megkötő, mesterséges) kimutatva a 4-es üvegházon kívül. Valószínű út: az öntözővíz elfolyása a folyóba.",
            "Terepi minták 40 km-rel lejjebb: a VRD-7 új viselkedést mutat. Lebontja a humuszt. A talaj szürke és laza lesz, mint a hamu. Semmi nem csírázik ki benne.",
            "Szürke Virágzásnak neveztük el. Megállítani nem tudjuk. Csak dokumentálni.\n\nJavaslat: tájékoztatni a lakosságot. Döntés: titkos."]}},
    "farmer_diary": {
        "en": {"title": "Miller farm diary", "subtitle": "Spring, the year the rain stopped", "pages": [
            "March. Third dry month. The well is down to a bucket an hour. The wheat came up thin.",
            "May. Grey patches in the south field, like someone spilled cement dust. The cows will not graze there. The dog digs and whines.",
            "July. The whole farm is grey. You can push a stick a metre into the ground. Seeds rot before they sprout.\n\nWe are leaving for the city. They say there is food in the city."]},
        "hu": {"title": "A Miller-tanya naplója", "subtitle": "Tavasz, az az év, amikor elállt az eső", "pages": [
            "Március. A harmadik száraz hónap. A kútból már csak óránként egy vödör jön. A búza gyéren kelt ki.",
            "Május. Szürke foltok a déli táblán, mintha valaki cementport szórt volna szét. A tehenek nem legelnek ott. A kutya ás és nyüszít.",
            "Július. Az egész tanya szürke. Egy botot egy méterre bele lehet nyomni a földbe. A mag elrothad, mielőtt kicsírázna.\n\nBemegyünk a városba. Azt mondják, a városban van élelem."]}},
    "nurse_ashlung": {
        "en": {"title": "Ward 3 shift notes", "subtitle": "Nurse T. Hayes", "pages": [
            "New admissions: 41. All the same: dry cough, grey sputum, fever in the evenings. The doctors call it Ashlung. Spores from the dead soil, they think.",
            "It spreads by breath. We wear two masks now. Masks are rationed.",
            "Day 19 of the quarantine. The army closed the doors from outside. We still have water. I am writing this so someone knows we stayed."]},
        "hu": {"title": "3-as kórterem, műszaknapló", "subtitle": "T. Hayes ápolónő", "pages": [
            "Új felvételek: 41. Mind ugyanaz: száraz köhögés, szürke köpet, esti láz. Az orvosok Hamutüdőnek hívják. Spórák a halott talajból, azt mondják.",
            "Lélegzettel terjed. Most már két maszkot hordunk. A maszk fejadagos.",
            "A karantén 19. napja. A hadsereg kívülről lezárta az ajtókat. Víz még van. Azért írom le, hogy valaki tudja: mi itt maradtunk."]}},
    "child_letter": {
        "en": {"title": "A letter in crayon", "subtitle": "To Grandma", "pages": [
            "Dear Grandma,\n\nWe are in the big hall now with lots of people. Daddy says the ship will take us to the Moon and then somewhere with blue sky.",
            "They give us numbers. Ours is 88412. Daddy says it is a lucky number.\n\nI drew you the rocket. Love, Lily"]},
        "hu": {"title": "Zsírkrétás levél", "subtitle": "Nagyinak", "pages": [
            "Drága Nagyi!\n\nMost egy nagy csarnokban vagyunk sok emberrel. Apa azt mondja, a hajó elvisz minket a Holdra, aztán valahova, ahol kék az ég.",
            "Számot kaptunk. A miénk 88412. Apa szerint szerencseszám.\n\nLerajzoltam neked a rakétát. Puszi, Lily"]}},
    "soldier_short_war": {
        "en": {"title": "Field notebook", "subtitle": "Sgt. D. Mercer, 4th Engineers", "pages": [
            "06:10. Orders: hold the reservoir road at all costs. Three nations want the same water.",
            "11:42. A flash in the north. Then another. The radio says the dams at Kerch and Volta are gone.",
            "19:00. They are calling it the Short War because it lasted one day. It will not feel short to anyone downwind."]},
        "hu": {"title": "Terepi jegyzetfüzet", "subtitle": "D. Mercer őrmester, 4. műszaki zászlóalj", "pages": [
            "06:10. Parancs: tartsuk a víztározóhoz vezető utat minden áron. Három ország akarja ugyanazt a vizet.",
            "11:42. Villanás északon. Aztán még egy. A rádió szerint a kercsi és a voltai gát megsemmisült.",
            "19:00. Rövid Háborúnak hívják, mert egy napig tartott. Annak, aki a szél irányában él, nem lesz rövid."]}},
    "dam_engineer": {
        "en": {"title": "Turbine hall log", "subtitle": "Chief engineer, Gorge Dam", "pages": [
            "The lake is 40 metres below the spillway. The turbines have nothing to turn. We are holding the gates closed anyway: whatever water is left is worth more than power.",
            "Armed men at the north gate asking for the lake. I gave them the keys. There is no one left to fight for."]},
        "hu": {"title": "Turbinacsarnok-napló", "subtitle": "A Szurdok-gát főmérnöke", "pages": [
            "A tó 40 méterrel a túlfolyó alatt van. A turbináknak nincs mit forgatniuk. A zsilipeket így is zárva tartjuk: a maradék víz többet ér, mint az áram.",
            "Fegyveresek az északi kapunál, a tavat kérik. Odaadtam nekik a kulcsokat. Már nincs kiért harcolni."]}},
    "exodus_lottery": {
        "en": {"title": "EXODUS boarding draw - result", "subtitle": "Aurora Consortium, citizen notice", "pages": [
            "Applicant 88412: NOT SELECTED.\n\nThank you for your patience. The Consortium is working to secure further seats in later departures.",
            "On the back, in pencil: 'There are no later departures. Tell Lily it is a lucky number anyway.'"]},
        "hu": {"title": "EXODUS beszállási sorsolás - eredmény", "subtitle": "Aurora Konzorcium, lakossági értesítő", "pages": [
            "88412-es pályázó: NEM NYERT.\n\nKöszönjük türelmét. A Konzorcium további helyeken dolgozik a későbbi indulásokra.",
            "A hátoldalán, ceruzával: 'Nincsenek későbbi indulások. Mondd meg Lilynek, hogy azért szerencseszám.'"]}},
    "launch_director": {
        "en": {"title": "Launch director's desk note", "subtitle": "Karman Spaceport, pad 3", "pages": [
            "Ark-4 is the last. After her the pad has no fuel, no crew, no orders. 2,300 aboard. 41,000 at the fence.",
            "If anyone comes after us: the assembly hall still has the keel of Ark-5. Half a ship. The cranes work if the reactor works. The reactor works if someone fixes the coolant loop.",
            "I am sorry. We picked by lottery because nobody could pick by worth."]},
        "hu": {"title": "Az indításvezető asztali cetlije", "subtitle": "Kármán-űrkikötő, 3-as indítóállás", "pages": [
            "A Bárka-4 az utolsó. Utána az állásnak nincs üzemanyaga, legénysége, parancsa. 2300 ember a fedélzeten. 41 000 a kerítésnél.",
            "Ha valaki jön utánunk: az összeszerelő csarnokban még ott a Bárka-5 gerince. Fél hajó. A darut el lehet indítani, ha megy a reaktor. A reaktor megy, ha valaki megjavítja a hűtőkört.",
            "Sajnálom. Sorsoltunk, mert senki nem tudott érdem szerint választani."]}},
    "colony_overgrown": {
        "en": {"title": "New Eden colony journal", "subtitle": "Week 31", "pages": [
            "The dome kept the spores out for thirty weeks. The garden was green. We had tomatoes.",
            "Week 31: grey at the roots of the beans. The Bloom came in on our own boots. We let the garden grow wild over the habitat to hide the dead ground. It does not help."]},
        "hu": {"title": "Új Éden kolónia, napló", "subtitle": "31. hét", "pages": [
            "A kupola harminc hétig kint tartotta a spórákat. A kert zöld volt. Volt paradicsomunk.",
            "31. hét: szürke a bab gyökerénél. A Virágzást a saját csizmánkon hoztuk be. Hagytuk, hogy a kert benője a lakómodult, hogy eltakarja a halott földet. Nem segít."]}},
    "colony_burnt": {
        "en": {"title": "Scorched colony log", "subtitle": "Last page", "pages": [
            "They came at night for the water tank. We fought. Somebody hit the fuel store.",
            "If you find this: the tank was empty anyway. We had been lying to them for a month."]},
        "hu": {"title": "Leégett kolónia naplója", "subtitle": "Utolsó oldal", "pages": [
            "Éjjel jöttek a víztartályért. Harcoltunk. Valaki eltalálta az üzemanyagraktárt.",
            "Ha megtalálod ezt: a tartály amúgy is üres volt. Egy hónapja hazudtunk nekik."]}},
    "colony_frozen": {
        "en": {"title": "Northern colony - heater log", "subtitle": "Temperature: -31", "pages": [
            "Halcyon was supposed to cool the planet by one degree. Here it took twenty.",
            "We burn furniture now. Then books. I am keeping this notebook for last."]},
        "hu": {"title": "Északi kolónia - fűtésnapló", "subtitle": "Hőmérséklet: -31", "pages": [
            "A Halcyonnak egy fokkal kellett volna hűtenie a bolygót. Itt húszat vitt el.",
            "Most már a bútorokat égetjük. Aztán a könyveket. Ezt a füzetet hagyom utoljára."]}},
    "oasis_keeper": {
        "en": {"title": "The keeper's notes", "subtitle": "Oasis refuge", "pages": [
            "This valley is the test field of the Reversal project: a counter-microbe that eats the Grey Bloom. Here, it worked. The ground is black and alive again.",
            "It spreads slowly, a hand's width a year. The rest of the world does not have centuries.",
            "Living soil carries the Reversal culture. Plant it where you want life. A ship's farm will do. A planet would be better.\n\nWe buried three of our own by the dome. They wanted to be part of the green."]},
        "hu": {"title": "A gondnok jegyzetei", "subtitle": "Oázis menedéktelep", "pages": [
            "Ez a völgy a Visszafordító-projekt kísérleti terepe: egy ellenmikrobáé, ami megeszi a Szürke Virágzást. Itt működött. A föld újra fekete és él.",
            "Lassan terjed, évente egy tenyérnyit. A világ többi részének nincsenek évszázadai.",
            "Az élő talaj hordozza a Visszafordító kultúrát. Ültesd oda, ahol életet akarsz. Egy hajó farmja is megteszi. Egy bolygó jobb volna.\n\nHármunkat eltemettük a kupola mellett. A zöld részei akartak lenni."]}},
    "seed_vault": {
        "en": {"title": "Seed vault ledger", "subtitle": "Svalbard annex, row K", "pages": [
            "1,247 varieties stored dry and cold. Germination test, row K: 3 of 40. The cold kept them, the Bloom never reached the vault.",
            "Note for whoever comes: seeds alone are not enough. They need living soil. The keeper in the southern oasis has it."]},
        "hu": {"title": "A magbank főkönyve", "subtitle": "Svalbardi melléképület, K sor", "pages": [
            "1247 fajta, szárazon és hidegen tárolva. Csírázási próba, K sor: 40-ből 3. A hideg megőrizte őket, a Virágzás sosem jutott be a tárolóba.",
            "Megjegyzés annak, aki jön: a mag egymagában nem elég. Élő talaj kell neki. A déli oázis gondnokának van."]}},
    "radio_operator": {
        "en": {"title": "Radio log, relay tower 9", "subtitle": "Operator on duty: nobody else", "pages": [
            "Day 1,102. Ark-2 (Mars) carrier still on 7.1 GHz, no voice.",
            "Day 1,190. Carrier gone. Then, for eleven seconds, a voice: 'ground the rover, the rover has the key, do not open the...'\n\nNothing since."]},
        "hu": {"title": "Rádiónapló, 9-es átjátszótorony", "subtitle": "Ügyeletes: senki más", "pages": [
            "1102. nap. A Bárka-2 (Mars) vivője még szól a 7,1 GHz-en, beszéd nélkül.",
            "1190. nap. A vivő eltűnt. Aztán tizenegy másodpercig egy hang: 'állítsátok le a rovert, a roveren van a kulcs, ne nyissátok ki a...'\n\nAzóta semmi."]}},
    "drone_tech": {
        "en": {"title": "Maintenance tag", "subtitle": "Autonomous miner MD-14", "pages": [
            "Unit left in autonomous mode when the crews were recalled. It will keep digging until the cells die.",
            "Salvage tip: the circuit boards are intact. Pull the cells, rewire the bus, it will run again."]},
        "hu": {"title": "Karbantartási címke", "subtitle": "MD-14 önjáró bányászdrón", "pages": [
            "A gépet önjáró módban hagyták, amikor a brigádokat visszarendelték. Addig ás, amíg a cellái ki nem merülnek.",
            "Bontási tipp: az áramköri lapok épek. Szedd ki a cellákat, kösd át a buszt, és újra menni fog."]}},
    "wreck_captain": {
        "en": {"title": "Ship's log, MV Albatross", "subtitle": "Captain's last entry", "pages": [
            "The sea went down three metres in a year. Then it went grey and thick. We ran aground on what used to be the shipping lane.",
            "The crew walked to the coast. I stayed. A captain stays with the ship, even when the sea leaves her."]},
        "hu": {"title": "Az MV Albatross hajónaplója", "subtitle": "A kapitány utolsó bejegyzése", "pages": [
            "A tenger egy év alatt három métert apadt. Aztán szürke és sűrű lett. Megfeneklettünk ott, ahol valaha a hajózóút volt.",
            "A legénység gyalog indult a part felé. Én maradtam. A kapitány a hajóval marad, akkor is, ha a tenger hagyja ott."]}},
    "gate_agent": {
        "en": {"title": "Note behind the gate desk", "subtitle": "Gate B7, Aurora International", "pages": [
            "They told us to check every passenger for the cough. Anyone with grey on the handkerchief goes to the field hospital downstairs, not to the plane.",
            "A man begged me to let his daughter board alone. She was not coughing. He was. I let her through. I do not know if that was right.",
            "The last flight to the spaceport left at 06:40. EVAC 099 came back burning at 07:15 and went off the end of runway 27."]},
        "hu": {"title": "Cetli a beszállítópult mögött", "subtitle": "B7-es kapu, Aurora International", "pages": [
            "Azt mondták, minden utast ellenőrizzünk, köhög-e. Akinek szürke a zsebkendője, az a lenti tábori kórházba megy, nem a gépre.",
            "Egy férfi könyörgött, hogy engedjem fel egyedül a lányát. A lány nem köhögött. Ő igen. Átengedtem. Nem tudom, jól tettem-e.",
            "Az utolsó járat 06:40-kor indult az űrkikötőbe. Az EVAC 099 07:15-kor égve jött vissza, és kifutott a 27-es pálya végén."]}},
    "triage_doctor": {
        "en": {"title": "Triage log", "subtitle": "Dr. M. Osei, St. Raphael General", "pages": [
            "Day 6 of the camp. The tents on the car park are full. We sort by colour now: red goes inside, black goes to the trench.",
            "The soldiers at the gate are kind. They bring us water. They are also under orders to shoot anyone who climbs the wire.",
            "I have the cough now. I will keep writing the colours until I cannot hold the pen."]},
        "hu": {"title": "Osztályozási napló", "subtitle": "Dr. M. Osei, St. Raphael kórház", "pages": [
            "A tábor 6. napja. A parkolón a sátrak tele vannak. Már színek szerint osztályozunk: a piros bemegy, a fekete az árokba.",
            "A kapunál álló katonák kedvesek. Vizet hoznak nekünk. Parancsuk is van: lőjenek mindenkire, aki átmászik a drótkerítésen.",
            "Most már én is köhögök. Addig írom a színeket, amíg tartani tudom a tollat."]}},
}

TAPES = {
    "aurora_exodus": {
        "en": {"title": "Tape: Aurora public address", "subtitle": "Recorded broadcast", "lines": [
            (0, "", "[static]"), (40, "Aurora CEO", "Citizens of Earth. The Exodus programme begins today."),
            (140, "Aurora CEO", "Four Arks will carry the seed of humanity to the Moon, to Mars, and beyond."),
            (260, "Aurora CEO", "Seats will be allocated fairly, by public draw."),
            (360, "Crowd", "[shouting, a siren]"), (440, "", "[the recording cuts out]")]},
        "hu": {"title": "Szalag: az Aurora közleménye", "subtitle": "Rögzített adás", "lines": [
            (0, "", "[zúgás]"), (40, "Az Aurora vezérigazgatója", "Föld polgárai! Ma elkezdődik az Exodus-program."),
            (140, "Az Aurora vezérigazgatója", "Négy Bárka viszi az emberiség magját a Holdra, a Marsra és tovább."),
            (260, "Az Aurora vezérigazgatója", "A helyeket igazságosan, nyilvános sorsolással osztjuk ki."),
            (360, "Tömeg", "[kiabálás, sziréna]"), (440, "", "[a felvétel megszakad]")]}},
    "verdant_alarm": {
        "en": {"title": "Tape: Verdant lab, Sector C", "subtitle": "Security recording", "lines": [
            (0, "", "[alarm]"), (30, "Voice 1", "Greenhouse 4 seals are open! Who opened the seals?"),
            (120, "Voice 2", "Irrigation flush, it is scheduled, it runs every Tuesday..."),
            (220, "Voice 1", "Shut it down. Shut the whole valley down."),
            (320, "Voice 2", "It is already in the river.")]},
        "hu": {"title": "Szalag: Verdant-labor, C szektor", "subtitle": "Biztonsági felvétel", "lines": [
            (0, "", "[riasztó]"), (30, "1. hang", "A 4-es üvegház zárjai nyitva! Ki nyitotta ki?"),
            (120, "2. hang", "Öblítés, be van ütemezve, minden kedden lefut..."),
            (220, "1. hang", "Állítsátok le. Zárjátok le az egész völgyet."),
            (320, "2. hang", "Már a folyóban van.")]}},
    "mars_last": {
        "en": {"title": "Tape: Ark-2, last transmission", "subtitle": "Received by relay tower 9", "lines": [
            (0, "", "[heavy static]"), (50, "Ark-2", "...colony status critical. The rover team found something under Jezero."),
            (170, "Ark-2", "Ground the rover. The rover has the key. Do not open the..."),
            (300, "", "[carrier lost]")]},
        "hu": {"title": "Szalag: Bárka-2, utolsó adás", "subtitle": "A 9-es átjátszótorony vette", "lines": [
            (0, "", "[erős zúgás]"), (50, "Bárka-2", "...a kolónia állapota kritikus. A rover-csapat talált valamit a Jezero alatt."),
            (170, "Bárka-2", "Állítsátok le a rovert. A roveren van a kulcs. Ne nyissátok ki a..."),
            (300, "", "[a vivő elveszett]")]}},
    "short_war_broadcast": {
        "en": {"title": "Tape: Emergency broadcast", "subtitle": "Civil defence, channel 1", "lines": [
            (0, "", "[three long tones]"), (60, "Announcer", "This is not a drill. Take shelter immediately."),
            (160, "Announcer", "Stay away from rivers, dams and reservoirs. Do not drink surface water."),
            (280, "Announcer", "Shelters will open their doors when the all-clear sounds."),
            (380, "", "[the tones repeat; there is no all-clear]")]},
        "hu": {"title": "Szalag: Vészhelyzeti adás", "subtitle": "Polgári védelem, 1-es csatorna", "lines": [
            (0, "", "[három hosszú hang]"), (60, "Bemondó", "Ez nem gyakorlat. Azonnal húzódjanak óvóhelyre."),
            (160, "Bemondó", "Kerüljék a folyókat, gátakat, víztározókat. Ne igyanak felszíni vizet."),
            (280, "Bemondó", "Az óvóhelyek a lefújás után nyitják ki az ajtókat."),
            (380, "", "[a hangok ismétlődnek; lefújás nincs]")]}},
    "oasis_message": {
        "en": {"title": "Tape: Message from the oasis", "subtitle": "Looped on the refuge radio", "lines": [
            (0, "", "[birdsong]"), (40, "Keeper", "If you can hear birds on this tape, the oasis is still alive."),
            (150, "Keeper", "Take living soil. Take clean water. Take seeds from the vault in the north."),
            (270, "Keeper", "Do not stay. One valley cannot feed the future. Fly.")]},
        "hu": {"title": "Szalag: Üzenet az oázisból", "subtitle": "Körbejátszva a telep rádióján", "lines": [
            (0, "", "[madárcsicsergés]"), (40, "Gondnok", "Ha madarakat hallasz ezen a szalagon, az oázis még él."),
            (150, "Gondnok", "Vigyél élő talajt. Vigyél tiszta vizet. Vigyél magot az északi magbankból."),
            (270, "Gondnok", "Ne maradj itt. Egy völgy nem tudja eltartani a jövőt. Repülj.")]}},
    "seed_sower_briefing": {
        "en": {"title": "Tape: Seed Sower mission briefing", "subtitle": "Aurora, classified", "lines": [
            (0, "", "[click]"), (30, "Mission control", "Target: Proxima Centauri b. Habitable zone. Liquid water likely."),
            (150, "Mission control", "The Seed Sower carries no passengers. It carries a library of life: seeds, embryos, soil cultures."),
            (290, "Mission control", "Its tender ships will stay in the Solar System as couriers. If the Sower fails, a tender must follow."),
            (420, "", "[end of tape]")]},
        "hu": {"title": "Szalag: A Magvető küldetés eligazítása", "subtitle": "Aurora, titkos", "lines": [
            (0, "", "[kattanás]"), (30, "Irányítás", "Cél: Proxima Centauri b. Lakhatósági zóna. Valószínű a folyékony víz."),
            (150, "Irányítás", "A Magvető nem visz utasokat. Az élet könyvtárát viszi: magokat, embriókat, talajkultúrákat."),
            (290, "Irányítás", "A kísérőhajók a Naprendszerben maradnak futárként. Ha a Magvető elbukik, egy kísérőhajónak kell utána mennie."),
            (420, "", "[a szalag vége]")]}},
}

TERMINALS = {
    "terminal_4": {
        "en": {"title": "COLONY NETWORK", "subtitle": "node 3 / status", "pages": [
            "> status\nDOME INTEGRITY ..... 92%\nWATER ............ 4 DAYS\nSOIL TEST ........ GREY BLOOM POSITIVE\nCOLONISTS ........ 17 -> 9\n\n> message\nWe are sealing the dome from inside. Do not open."]},
        "hu": {"title": "KOLÓNIA-HÁLÓZAT", "subtitle": "3-as csomópont / állapot", "pages": [
            "> status\nKUPOLA ÉPSÉGE ..... 92%\nVÍZ .............. 4 NAP\nTALAJPRÓBA ....... SZÜRKE VIRÁGZÁS POZITÍV\nTELEPESEK ........ 17 -> 9\n\n> uzenet\nBelülről lezárjuk a kupolát. Ne nyissátok ki."]}},
    "terminal_5": {
        "en": {"title": "REVERSAL FIELD STATION", "subtitle": "plot 12 / soil monitor", "pages": [
            "> soil plot12\nGREY BLOOM ....... 0.0%\nREVERSAL CULTURE . ACTIVE\nORGANIC CARBON ... 4.1% (RISING)\n\nNOTE: culture survives in transported soil for > 10 years if kept moist. A ship farm qualifies."]},
        "hu": {"title": "VISSZAFORDÍTÓ TEREPÁLLOMÁS", "subtitle": "12-es parcella / talajfigyelő", "pages": [
            "> talaj parcella12\nSZÜRKE VIRÁGZÁS .. 0,0%\nVISSZAFORDÍTÓ .... AKTÍV\nSZERVES SZÉN ..... 4,1% (NÖ)\n\nMEGJ.: a kultúra elszállított talajban >10 évig él, ha nedvesen tartják. Egy hajó farmja megfelel."]}},
    "terminal_6": {
        "en": {"title": "VERDANT LAB", "subtitle": "strain archive", "pages": [
            "> archive VRD-7\nORIGIN ........... GREENHOUSE 4\nFUNCTION ......... CARBON FIXING\nMUTATION ......... HUMUS DIGESTION (GREY BLOOM)\nCOUNTER-STRAIN ... PROJECT REVERSAL -> FIELD TEST SOUTH\n\n> access denied"]},
        "hu": {"title": "VERDANT-LABOR", "subtitle": "törzsarchívum", "pages": [
            "> archivum VRD-7\nEREDET ........... 4-ES ÜVEGHÁZ\nFUNKCIÓ .......... SZÉNMEGKÖTÉS\nMUTÁCIÓ .......... HUMUSZBONTÁS (SZÜRKE VIRÁGZÁS)\nELLENTÖRZS ....... VISSZAFORDÍTÓ PROJEKT -> DÉLI TEREPPRÓBA\n\n> hozzaferes megtagadva"]}},
    "terminal_7": {
        "en": {"title": "HOSPITAL ADMISSIONS", "subtitle": "ward summary", "pages": [
            "> wards\nWARD 1 .. FULL\nWARD 2 .. FULL\nWARD 3 .. QUARANTINE (ASHLUNG)\nSTAFF ... 4 OF 60 ON DUTY\n\n> last entry\nDoors sealed by order of the military district."]},
        "hu": {"title": "KÓRHÁZI FELVÉTEL", "subtitle": "osztályösszesítő", "pages": [
            "> osztalyok\n1. OSZT. .. TELE\n2. OSZT. .. TELE\n3. OSZT. .. KARANTÉN (HAMUTÜDŐ)\nSZEMÉLYZET 60-BÓL 4 SZOLGÁLATBAN\n\n> utolso bejegyzes\nAz ajtókat a katonai körzet parancsára lezárták."]}},
    "terminal_8": {
        "en": {"title": "SHELTER CONTROL", "subtitle": "bunker systems", "pages": [
            "> systems\nAIR FILTERS ...... 3% LIFE LEFT\nWATER RECYCLER ... OFFLINE\nCRYO BAYS ........ 1 OCCUPIED\nDOOR ............. LOCKED (ALL-CLEAR NEVER RECEIVED)"]},
        "hu": {"title": "ÓVÓHELY-VEZÉRLÉS", "subtitle": "bunkerrendszerek", "pages": [
            "> rendszerek\nLÉGSZŰRŐK ........ 3% ÉLETTARTAM\nVÍZVISSZANYERŐ ... KIKAPCSOLVA\nHIBERNÁCIÓ ....... 1 FÜLKE FOGLALT\nAJTÓ ............. ZÁRVA (LEFÚJÁS NEM ÉRKEZETT)"]}},
    "terminal_9": {
        "en": {"title": "LAUNCH CONTROL", "subtitle": "Karman spaceport", "pages": [
            "> manifest\nARK-1 MOON ....... DEPARTED\nARK-2 MARS ....... DEPARTED (SILENT)\nARK-3 MOON-RELAY . DEPARTED\nARK-4 PROXIMA .... DEPARTED\nARK-5 ............ KEEL ONLY / ASSEMBLY HALL\n\n> note\nReactor coolant loop leaking. Cranes offline."]},
        "hu": {"title": "INDÍTÁSIRÁNYÍTÁS", "subtitle": "Kármán-űrkikötő", "pages": [
            "> utaslista\nBÁRKA-1 HOLD ...... ELINDULT\nBÁRKA-2 MARS ...... ELINDULT (NÉMA)\nBÁRKA-3 HOLD-RELÉ  ELINDULT\nBÁRKA-4 PROXIMA ... ELINDULT\nBÁRKA-5 ........... CSAK GERINC / ÖSSZESZERELŐ CSARNOK\n\n> megjegyzes\nA reaktor hűtőköre szivárog. A darut kikapcsolták."]}},
    "terminal_10": {
        "en": {"title": "RELAY TOWER 9", "subtitle": "signal log", "pages": [
            "> signals\n7.1 GHz ARK-2 ..... LOST (DAY 1190)\n2.3 GHz MOON ...... BEACON ONLY\n0.4 GHz LOCAL ..... OASIS LOOP (BIRDSONG)\n\nThe oasis loop is the only voice left on Earth."]},
        "hu": {"title": "9-ES ÁTJÁTSZÓTORONY", "subtitle": "jelnapló", "pages": [
            "> jelek\n7,1 GHz BÁRKA-2 ... ELVESZETT (1190. NAP)\n2,3 GHz HOLD ...... CSAK JELADÓ\n0,4 GHz HELYI ..... OÁZIS-HUROK (MADÁRHANG)\n\nAz oázis-hurok az utolsó hang a Földön."]}},
    "terminal_11": {
        "en": {"title": "SEED VAULT", "subtitle": "inventory", "pages": [
            "> inventory\nVARIETIES ........ 1247\nVIABLE (EST.) ..... 7%\nTEMPERATURE ...... -18 C\n\nSeeds need living soil to wake. Soil source: southern oasis (Reversal field)."]},
        "hu": {"title": "MAGBANK", "subtitle": "leltár", "pages": [
            "> leltar\nFAJTÁK ........... 1247\nCSÍRAKÉPES (BECS.) 7%\nHŐMÉRSÉKLET ...... -18 C\n\nA mag élő talajban ébred. Talajforrás: déli oázis (Visszafordító-tereppróba)."]}},
    "terminal_12": {
        "en": {"title": "AURORA INTERNATIONAL", "subtitle": "flight operations", "pages": [
            "> flights\nEVAC 097 -> KARMAN ..... DEPARTED\nEVAC 098 -> KARMAN ..... DEPARTED\nEVAC 099 -> KARMAN ..... RETURNED / CRASHED RWY 27\nEVAC 100-131 ........... CANCELLED\n\n> note\nScreening at check-in. Ashlung positive: field hospital, ground floor."]},
        "hu": {"title": "AURORA INTERNATIONAL", "subtitle": "repülésirányítás", "pages": [
            "> jaratok\nEVAC 097 -> KÁRMÁN ..... ELINDULT\nEVAC 098 -> KÁRMÁN ..... ELINDULT\nEVAC 099 -> KÁRMÁN ..... VISSZAFORDULT / LEZUHANT 27\nEVAC 100-131 ........... TÖRÖLVE\n\n> megjegyzes\nSzűrés a check-innél. Hamutüdő-pozitív: tábori kórház, földszint."]}},
    "terminal_13": {
        "en": {"title": "ST. RAPHAEL GENERAL", "subtitle": "triage", "pages": [
            "> triage\nGREEN ........ 0\nYELLOW ....... 0\nRED .......... 312\nBLACK ........ 1904\n\n> order\nCamp gate closed by the military district. Patients may not leave. Staff may not leave."]},
        "hu": {"title": "ST. RAPHAEL KÓRHÁZ", "subtitle": "osztályozás", "pages": [
            "> osztalyozas\nZÖLD ......... 0\nSÁRGA ........ 0\nPIROS ........ 312\nFEKETE ....... 1904\n\n> parancs\nA tábor kapuját a katonai körzet lezárta. Beteg nem távozhat. Személyzet nem távozhat."]}},
    "terminal_14": {
        "en": {"title": "HALCYON ORBITAL LAB", "subtitle": "commander's log, Cmdr. A. Novak", "pages": [
            "> log 4471\nThe last relief flight was cancelled. Kepler and Osei took the capsule down. I stayed: somebody has to keep the station's beacon on for the Arks.",
            "> log 4502\nArk-1 passed overhead today, burning for the Moon. Beautiful. Ark-3 an hour later. I waved. Stupid.",
            "> log 4560\nDebris streams every few hours now: the old satellites are breaking up. I go out on the line only. Never without the line.",
            "> log 4611\nOxygen for nine days. Raised the orbit with the last fuel so HALCYON outlives me. Whoever reads this: the ISS has supplies. Clip on, and look at the Earth for me."]},
        "hu": {"title": "HALCYON ORBITÁLIS LABOR", "subtitle": "parancsnoki napló, A. Novak parancsnok", "pages": [
            "> naplo 4471\nAz utolsó váltójáratot törölték. Kepler és Osei lement a kapszulával. Én maradtam: valakinek égve kell tartania az állomás jeladóját a Bárkáknak.",
            "> naplo 4502\nMa elhúzott fölöttem a Bárka-1, a Hold felé gyorsított. Gyönyörű. Egy órával később a Bárka-3. Integettem. Hülyeség.",
            "> naplo 4560\nMár óránként jönnek a törmelékáramok: szétesnek a régi műholdak. Csak kötélen megyek ki. Soha kötél nélkül.",
            "> naplo 4611\nKilenc napra való oxigén. Az utolsó üzemanyaggal megemeltem a pályát, hogy a HALCYON túléljen. Aki ezt olvassa: az ISS-en van készlet. Csatold be magad, és nézd meg helyettem a Földet."]}},
    "terminal_15": {
        "en": {"title": "ARK-1 COLONY", "subtitle": "Shackleton rim / colony status", "pages": [
            "> status\nCOLONISTS ........ 380\nAWAKE ............ 0\nCRYO HALL ........ NOMINAL (SOLAR TOWERS 61%)\nICE MINE ......... IDLE\nEARTH LINK ....... SILENT SINCE DAY 214",
            "> decision, day 240\nWithout Earth's supply runs the greenhouse cannot feed 380. Council vote: long sleep for everyone, one watch officer by rotation. The rotation ended on day 911.",
            "> wake order\nWake the colony on signal from ARK-5 or any Earth vessel. Wake codes are held by the cryo hall console. Bring seeds: ours did not survive the regolith."]},
        "hu": {"title": "BÁRKA-1 KOLÓNIA", "subtitle": "Shackleton-perem / a kolónia állapota", "pages": [
            "> allapot\nTELEPESEK ........ 380\nÉBREN ............ 0\nHIBERNÁCIÓS CSARNOK  NÉVLEGES (NAPTORNYOK 61%)\nJÉGBÁNYA ......... ÁLL\nFÖLDI KAPCSOLAT .. NÉMA A 214. NAP ÓTA",
            "> dontes, 240. nap\nA földi utánpótlás nélkül az üvegház nem tud 380 embert etetni. A tanács döntése: hosszú alvás mindenkinek, egy ügyeletes forgásban. A forgás a 911. napon megszakadt.",
            "> ebresztesi parancs\nA kolóniát a BÁRKA-5 vagy bármely földi hajó jelére kell felébreszteni. Az ébresztőkódok a hibernációs csarnok konzolján vannak. Hozzatok magot: a mieink nem élték túl a regolitot."]}},
    "terminal_16": {
        "en": {"title": "ARK-2 BRIDGE", "subtitle": "Jezero / ship's log", "pages": [
            "> log, sol 1\nDown on the delta. Leg 4 buckled on touchdown; the hull will never fly again. It does not need to: we are home. 1,900 souls, 40 tonnes of seed, one river that dried up three billion years ago.",
            "> log, sol 640\nPerchlorate leaching works. Greenhouse 1 gives potatoes. The geology team asks for the big rover to survey the delta front: ground radar shows a reflector 30 m down. Approved.",
            "> log, sol 1102\nThe dig has reached it. It is not rock. Dr. Ferreira says it is not anything. I have ordered the pit closed until Earth answers. Earth does not answer."]},
        "hu": {"title": "BÁRKA-2 HÍD", "subtitle": "Jezero / hajónapló", "pages": [
            "> naplo, 1. sol\nLent vagyunk a deltán. A 4-es láb leszálláskor megrogyott; a hajó soha többé nem repül. Nem is kell: itthon vagyunk. 1900 lélek, 40 tonna mag, egy folyó, amely hárommilliárd éve kiszáradt.",
            "> naplo, 640. sol\nA perklorát-kilúgozás működik. Az 1-es üvegház krumplit ad. A geológusok a nagy rovert kérik a delta frontjának felméréséhez: a talajradar 30 méter mélyen visszaverődést mutat. Jóváhagyva.",
            "> naplo, 1102. sol\nA feltárás elérte. Nem kőzet. Dr. Ferreira szerint nem is semmi, amit ismerünk. Elrendeltem, hogy zárják le a gödröt, amíg a Föld nem válaszol. A Föld nem válaszol."]}},
    "terminal_17": {
        "en": {"title": "ARK-2 GEOLOGY LAB", "subtitle": "dig site / Dr. M. Ferreira", "pages": [
            "> dig 0412\nThe surface is smooth to the micrometre and colder than the sediment around it. A seam runs down the middle. Light comes from the seam. It pulses once every 24 h 39 min: one Martian day.",
            "> dig 0419\nThe rover's arm touched the seam and the pulse changed. The rover's memory now holds 2.1 GB we did not put there. The commander wants the rover grounded. I agree. The rover has the key.",
            "> dig 0421\nThe seam is wider this morning. Do not open it. Do not let the rover near it. If you are reading this, you came on a ship: take the rover's memory core to Earth, not to the wall."]},
        "hu": {"title": "BÁRKA-2 GEOLÓGIAI LABOR", "subtitle": "feltárás / Dr. M. Ferreira", "pages": [
            "> asatas 0412\nA felülete mikrométerre sima, és hidegebb a körülötte lévő üledéknél. Középen egy rés fut le. A résből fény jön. 24 óra 39 percenként lüktet egyet: egy marsi nap.",
            "> asatas 0419\nA rover karja megérintette a rést, és a lüktetés megváltozott. A rover memóriájában most 2,1 GB van, amit nem mi tettünk oda. A parancsnok a rover leállítását kéri. Egyetértek. A roveren van a kulcs.",
            "> asatas 0421\nMa reggelre szélesebb lett a rés. Ne nyissátok ki. Ne engedjétek a rovert a közelébe. Ha ezt olvasod, hajóval jöttél: a rover memóriamagját a Földre vidd, ne a falhoz."]}},
    "terminal_18": {
        "en": {"title": "APHRODITE CLOUD STATION", "subtitle": "station chief R. Adeyemi", "pages": [
            "> log, day 1\nFifty-five kilometres up, the air outside is one bar and twenty-seven degrees. Sulphuric, but you can stand on the deck in a light suit. The only place in the system that feels like a summer afternoon.",
            "> log, day 412\nThe Ark schedule came in. Ark-4 skips Venus: 'window missed, crew to wait for Ark-6'. There is no Ark-6 on the list. 214 people on board. I have not told them yet.",
            "> log, day 590\nLobe D tore in the night. We lose thirty metres a day. Below fifty kilometres the acid gets into everything. Kowalczyk takes capsule 2 down to the old surface relay to call Earth. Forty minutes of cooling. She says it is enough.",
            "> log, day 593\nNo call. The lift still works, and the heat shields we made from the acid works' liners are good for a short walk below. Nobody goes down again. We trim ballast and wait. If you came by ship: you are the ship we waited for."]},
        "hu": {"title": "APHRODITE FELHŐÁLLOMÁS", "subtitle": "R. Adeyemi állomásparancsnok", "pages": [
            "> naplo, 1. nap\nÖtvenöt kilométer magasan kint egy bar a nyomás és huszonhét fok. Kénsavas, de könnyű ruhában ki lehet állni a fedélzetre. Az egyetlen hely a Naprendszerben, ami olyan, mint egy nyári délután.",
            "> naplo, 412. nap\nMegjött a Bárkák menetrendje. A Bárka-4 kihagyja a Vénuszt: \"az ablak lezárult, a személyzet várja meg a Bárka-6-ot\". A listán nincs Bárka-6. 214 ember van a fedélzeten. Még nem mondtam meg nekik.",
            "> naplo, 590. nap\nÉjjel kiszakadt a D emelőballon. Napi harminc métert vesztünk. Ötven kilométer alatt a sav mindenbe beeszi magát. Kowalczyk a 2-es kapszulával lemegy a régi felszíni reléhez, hogy hívja a Földet. Negyven perc hűtés. Azt mondja, elég.",
            "> naplo, 593. nap\nNincs hívás. A lift még működik, és a savüzem béléséből gyártott hőpajzsok rövid sétára jók odalent. Senki nem megy le többé. Ballasztot dobunk és várunk. Ha hajóval jöttél: te vagy az a hajó, amire vártunk."]}},
    "terminal_19": {
        "en": {"title": "CAPSULE 2", "subtitle": "Dr. A. Kowalczyk / suit recorder", "pages": [
            "> 00:00\nDown. Hard. The parachute burnt off at six kilometres, the shock struts took the rest. 462 degrees. The suit says forty minutes. The sky is orange and the light comes from everywhere.",
            "> 00:09\nThe relay is dead. Acid in the junction box. I can see Venera 13 from here: a Soviet probe built to last half an hour, still standing after eighty years. Something about that makes me laugh.",
            "> 00:21\nCooling at sixty percent. Not enough to climb back to the lift foot. Tell Aphrodite to stop waiting for Earth. Tell them the basalt here is warm and beautiful. Tell my brother in Gdansk... no. Nobody is in Gdansk."]},
        "hu": {"title": "2-ES KAPSZULA", "subtitle": "Dr. A. Kowalczyk / ruharögzítő", "pages": [
            "> 00:00\nLent vagyok. Keményen. Az ernyő hat kilométeren leégett, a többit a lengéscsillapítók nyelték el. 462 fok. A ruha negyven percet mond. Narancssárga az ég, és mindenhonnan jön a fény.",
            "> 00:09\nA relé halott. Sav van a kapcsolódobozban. Innen látom a Venyera-13-at: egy szovjet szonda, amit fél órára építettek, és nyolcvan év után is áll. Ezen valamiért nevetnem kell.",
            "> 00:21\nA hűtés hatvan százalékon. Nem elég, hogy visszamásszak a lift aljához. Mondjátok meg az Aphroditénak, hogy ne várják tovább a Földet. Mondjátok meg, hogy a bazalt itt meleg és gyönyörű. Mondjátok meg a bátyámnak Gdańskban... nem. Gdańskban nincs senki."]}},
    "terminal_20": {
        "en": {"title": "SUNSHADE CONTROL", "subtitle": "automatic / no crew aboard", "pages": [
            "> status\nCREW ............. 0 (DEPARTED ON ARK-3)\nPOWER RELAY ...... NO RECEIVER\nFLARES LOGGED .... 1214 SINCE DEPARTURE\nSHELTER .......... READY",
            "> schedule\nThe sun's flares follow the active regions. I read them and I sound the alarm 20 seconds before each. Nobody hears it. I keep the schedule. The schedule is what I am for.",
            "> note to visitor\nThe day side is 430 degrees, the night side minus 180. Walk in the base's shadow or wear a heat shield. When the alarm sounds, go down to the flare shelter, or wear radiation shielding. I will tell you when it is over. I have always told them."]},
        "hu": {"title": "SUNSHADE IRÁNYÍTÁS", "subtitle": "automata / személyzet nincs a fedélzeten", "pages": [
            "> allapot\nSZEMÉLYZET ....... 0 (ELMENT A BÁRKA-3-MAL)\nENERGIARELÉ ...... NINCS VEVŐ\nNAPKITÖRÉSEK ..... 1214 AZ INDULÁS ÓTA\nÓVÓHELY .......... KÉSZ",
            "> menetrend\nA napkitörések az aktív területeket követik. Olvasom őket, és mindegyik előtt 20 másodperccel megszólaltatom a riasztót. Senki nem hallja. Tartom a menetrendet. Ezért vagyok.",
            "> uzenet a latogatonak\nA nappali oldal 430 fokos, az éjszakai mínusz 180. A bázis árnyékában járj, vagy viselj hőpajzsot. Ha szól a riasztó, menj le az óvóhelyre, vagy viselj sugárvédelmet. Szólok, ha vége. Nekik is mindig szóltam."]}},
    "terminal_21": {
        "en": {"title": "OCCATOR STATION", "subtitle": "cold archive / Dr. Y. Tanabe", "pages": [
            "> archive\nThe brine under Occator stays liquid at minus forty because of the salt. Microbes kept in it sleep for decades. The Consortium sent us twelve strains to keep: the counter-culture from Project Reversal, the one that eats the Grey Bloom.",
            "> archive, year 3\nThe strains are alive. I checked them every month. Earth stopped asking for reports. The Arks flew past without stopping: Ceres was 'not a priority destination'.",
            "> archive, last\nI am leaving the archive in the tanks and the well open to the brine. If Earth ever needs its soil back, the cure is here, in the cold, waiting. Seal the tanks if you take them. Keep them moist."]},
        "hu": {"title": "OCCATOR ÁLLOMÁS", "subtitle": "hideg archívum / Dr. Y. Tanabe", "pages": [
            "> archivum\nAz Occator alatti sólé a só miatt mínusz negyven fokon is folyékony marad. A benne tartott mikrobák évtizedekig alszanak. A Konzorcium tizenkét törzset küldött megőrzésre: a Visszafordítás-projekt ellenkultúráját, amely felfalja a Szürke Virágzást.",
            "> archivum, 3. ev\nA törzsek élnek. Minden hónapban ellenőriztem őket. A Föld már nem kér jelentést. A Bárkák megállás nélkül elhúztak mellettünk: a Ceres \"nem kiemelt célpont\".",
            "> archivum, utolso\nAz archívumot a tartályokban hagyom, a kutat nyitva a sólé felé. Ha a Földnek valaha vissza kell kapnia a talaját, a gyógymód itt van, a hidegben, és vár. Zárd le a tartályokat, ha elviszed őket. Tartsd őket nedvesen."]}},
    "terminal_22": {
        "en": {"title": "VESTA MINING OUTPOST", "subtitle": "foreman L. Eriksen", "pages": [
            "> shift report\nEvery Ark drive needs thirty kilos of iridium for the plasma chamber. Earth has none left to mine. The metal rocks out here have it in their cores. We cut it, mill it, and the haulers take it to Kármán.",
            "> shift report\nTwelve drives' worth shipped. The INGRID BJORNSTAD left with the thirteenth load and went quiet at the edge of the debris field. We found her broken in two. Captain Haugen is still at the helm.",
            "> to whoever comes\nSmelt the iridium. The drive kit wants it, with a fusion core and magnets: that makes an Ark drive run hot enough for Jupiter. Clip on, always. Out here, letting go is falling forever."]},
        "hu": {"title": "VESTA BÁNYÁSZÁLLOMÁS", "subtitle": "L. Eriksen műszakvezető", "pages": [
            "> muszakjelentes\nMinden Bárka-hajtómű plazmakamrájához harminc kiló irídium kell. A Földön már nincs mit bányászni. Az itteni fémes sziklák magjában van. Kivágjuk, megőröljük, a teherhajók pedig elviszik Kármánba.",
            "> muszakjelentes\nTizenkét hajtóműnyit szállítottunk el. Az INGRID BJØRNSTAD a tizenharmadik rakománnyal indult el, és a törmelékmező szélén elhallgatott. Kettétörve találtuk meg. Haugen kapitány még mindig a kormánynál ül.",
            "> annak, aki jon\nOlvaszd ki az irídiumot. A hajtóműkészlethez az kell, egy fúziós maggal és mágnesekkel együtt: attól lesz elég forró egy Bárka-hajtómű a Jupiterhez. Mindig csatold be magad. Itt kint az elengedés örök zuhanás."]}},
    "terminal_23": {
        "en": {"title": "ZEPHYR AEROSTAT", "subtitle": "harvest master I. Petrova", "pages": [
            "> harvest\nThe scoops drink the ammonia deck and give us hydrogen and helium-3 by the tonne. The deeper the line, the richer the gas. Below the brown deck the pressure crushes a man without a shell. Below the blue deck it boils him too.",
            "> harvest, day 301\nOkafor saw crystals under the water deck: metallic hydrogen, the Consortium's dream, the thing that makes a fusion core sing. He went down the long cable with a heat shield and a pressure shell. Forty minutes. He came back with four crystals and both modules ruined.",
            "> harvest, day 340\nOkafor went down again. The cable came back up cut clean, as if by a blade, at ninety metres. There is nothing down there but cloud. Nothing at all. I have stopped the deep harvest."]},
        "hu": {"title": "ZEPHYR AEROSZTÁT", "subtitle": "I. Petrova betakarítás-vezető", "pages": [
            "> betakaritas\nA kanalak isszák az ammóniaréteget, és tonnaszám adják a hidrogént és a hélium-3-at. Minél mélyebbre ér a vezeték, annál gazdagabb a gáz. A barna réteg alatt a nyomás héj nélkül összeroppantja az embert. A kék réteg alatt meg is főzi.",
            "> betakaritas, 301. nap\nOkafor kristályokat látott a vízfelhők alatt: fémes hidrogént, a Konzorcium álmát, amitől egy fúziós mag dalolni kezd. Hőpajzzsal és nyomásálló héjjal leereszkedett a hosszú kábelen. Negyven perc. Négy kristállyal jött vissza, és mindkét modulja tönkrement.",
            "> betakaritas, 340. nap\nOkafor újra lement. A kábel kilencven méternél tisztán elvágva jött fel, mintha pengével vágták volna. Odalent nincs semmi, csak felhő. Semmi. Leállítottam a mélybetakarítást."]}},
    "terminal_24": {
        "en": {"title": "LOKI OBSERVATORY", "subtitle": "Dr. R. Mendes / dosimeter", "pages": [
            "> dose\nIo sits inside Jupiter's radiation belts. Outside these walls: a lethal dose in a few hours. Inside: a working year's dose in a month. I count the days. The volcano does not care.",
            "> dose, day 28\nLoki overturned last night. The crust of the lava lake sank in a wave that crossed the whole lake, like it does every 540 days. Nobody has seen that from the ground before. I have the whole thing on tape.",
            "> dose, day 31\nThe relay to Europa is down, so the tape stays here. Over the limit now. Shielding module on the rack by the door if you need to go outside. Watch the lake turn over for me."]},
        "hu": {"title": "LOKI OBSZERVATÓRIUM", "subtitle": "Dr. R. Mendes / doziméter", "pages": [
            "> dozis\nAz Io a Jupiter sugárövein belül kering. Ezeken a falakon kívül néhány óra alatt halálos dózist kapsz. Belül egy havi munka egy évnyi dózis. Számolom a napokat. A vulkánt nem érdekli.",
            "> dozis, 28. nap\nA Loki éjjel átfordult. A lávató kérge egy hullámban süllyedt el, ami végigment az egész tavon, ahogy 540 naponként mindig. Ezt még senki nem látta a felszínről. Az egészet felvettem.",
            "> dozis, 31. nap\nAz Európa felé menő relé leállt, így a felvétel itt marad. Már túl vagyok a határértéken. Ha ki kell menned, az ajtó melletti polcon van sugárvédő modul. Nézd meg helyettem, ahogy átfordul a tó."]}},
    "terminal_25": {
        "en": {"title": "THERA DRILL STATION", "subtitle": "Dr. S. Lindqvist", "pages": [
            "> shaft\nThe melt probe broke through at thirty metres. The water under the ice is plus four degrees, salty, and it moves: there are currents, warmed from the sea floor.",
            "> camera 2\nMats. On the vents, on the ice ceiling, everywhere the warm water touches. They glow when the lamp goes off. Specimen tanks 1 to 3 hold samples. They are alive. They are not from Earth.",
            "> last\nThe first life ever found beyond Earth, and there is nobody left on Earth to tell. I have sent it to every address I have. If you read this, then somebody knows. That is enough. Go down the shaft. Look."]},
        "hu": {"title": "THERA FÚRÓÁLLOMÁS", "subtitle": "Dr. S. Lindqvist", "pages": [
            "> akna\nAz olvasztószonda harminc méternél áttörte a jeget. A víz a jég alatt plusz négy fokos, sós, és mozog: áramlatok vannak, a tengerfenékről melegednek.",
            "> 2-es kamera\nTelepek. A kürtőkön, a jégmennyezeten, mindenhol, ahol a meleg víz hozzáér. Világítanak, amikor lekapcsol a lámpa. Az 1-3. mintatartályban vannak minták. Élnek. Nem a Földről valók.",
            "> utolso\nAz első élet, amit valaha a Földön kívül találtak, és nem maradt senki a Földön, akinek elmondhatnám. Minden címre elküldtem, ami megvan. Ha ezt olvasod, akkor valaki tudja. Ez elég. Menj le az aknán. Nézd meg."]}},
    "terminal_26": {
        "en": {"title": "ARK-1 CRYO HALL", "subtitle": "wake console", "pages": [
            "> wake sequence\nWAKE CODES ....... ACCEPTED\nPOD 001-380 ...... NO LIFE SIGNS\nPOWER LOG ........ SOLAR TOWERS FAILED DAY 1690\nBACKUP ........... EXHAUSTED DAY 1702",
            "> watch officer, day 1701\nI am the last one awake. The towers iced over in the long night and the batteries will not last. I cannot wake them: there is nothing to wake them to. No food, no air for 380.",
            "> watch officer, day 1702\nI am going to sit with them. I am sorry. Whoever opened this: you did nothing wrong. We waited for you as long as we could."]},
        "hu": {"title": "BÁRKA-1 HIBERNÁCIÓS CSARNOK", "subtitle": "ébresztőkonzol", "pages": [
            "> ebresztesi sorozat\nÉBRESZTŐKÓDOK .... ELFOGADVA\nKAPSZULA 001-380 .. NINCS ÉLETJEL\nENERGIANAPLÓ ..... NAPTORNYOK LEÁLLTAK, 1690. NAP\nTARTALÉK ......... KIMERÜLT, 1702. NAP",
            "> ugyeletes, 1701. nap\nÉn vagyok az utolsó, aki ébren van. A tornyok eljegesedtek a hosszú éjszakában, és az akkumulátorok nem bírják. Nem tudom felébreszteni őket: nincs mire. Nincs étel, nincs levegő 380 embernek.",
            "> ugyeletes, 1702. nap\nLeülök közéjük. Sajnálom. Bárki is nyitotta meg ezt: nem tettél semmi rosszat. Vártunk rád, ameddig csak tudtunk."]}},
    "terminal_27": {
        "en": {"title": "ARK-2 DIG SITE", "subtitle": "field terminal / Dr. M. Ferreira", "pages": [
            "> dig 0423\nGrey dust comes out of the seam when it pulses. Fine, loose, like ash. The sediment around the wall is going grey too, a hand's width a day. Nothing grows in it.",
            "> dig 0425\nI sequenced the dust. It is VRD-7. Not like it: it is it, the Grey Bloom, older than any rock on this delta. Halcyon did not make the Bloom. They found it in a Martian meteorite and taught it to eat soil faster.",
            "> dig 0426\nSomething knocks on the other side at night, once per sol, on the pulse. I have stopped going down. I am writing this so the next person understands: the wall is not a door to anything. It is a door for something."]},
        "hu": {"title": "BÁRKA-2 FELTÁRÁS", "subtitle": "terepi terminál / Dr. M. Ferreira", "pages": [
            "> asatas 0423\nSzürke por jön ki a résből, amikor lüktet. Finom, laza, mint a hamu. A fal körüli üledék is szürkül, naponta egy tenyérnyit. Semmi nem nő benne.",
            "> asatas 0425\nSzekvenáltam a port. VRD-7. Nem hasonlít rá: ez az, a Szürke Virágzás, régebbi bármelyik kőzetnél ezen a deltán. A Halcyon nem megalkotta a Virágzást. Egy marsi meteoritban találták, és megtanították gyorsabban enni a talajt.",
            "> asatas 0426\nÉjjelente valami kopog a túloldalon, solonként egyszer, a lüktetés ütemére. Nem megyek le többé. Azért írom ezt, hogy a következő megértse: a fal nem ajtó valahová. Ajtó valaminek."]}},
}

MANUALS = {
    "nav_console": {
        "en": {"title": "Navigation console", "subtitle": "Manual N-1, Aurora tender class", "pages": [
            "1. Right click the console on the bridge. The star chart opens: the Solar System strip, the destinations with travel times, and the details of the selected one.",
            "2. Select a destination (click, or the arrow keys) and press SET COURSE (or Enter). The course is stored; the ship does not launch yet.",
            "3. Launch is done from the pilot's seat: see manual N-2 (launch checklist).\n\nDuring a meteor shower, a click on the console engages the autopilot."]},
        "hu": {"title": "Navigációs konzol", "subtitle": "N-1-es kézikönyv, Aurora kísérőhajó-osztály", "pages": [
            "1. Kattints jobb gombbal a konzolra a hídon. Megnyílik a csillagtérkép: a Naprendszer sávja, a célok az utazási időkkel, és a kiválasztott cél részletei.",
            "2. Válassz célt (kattintás vagy nyilak), és nyomd meg az IRÁNY BEÁLLÍTÁSA gombot (vagy Entert). Az irány elmentődik, a hajó még nem indul.",
            "3. Indítani a pilótaülésből kell: lásd az N-2-es kézikönyvet (indítási ellenőrzőlista).\n\nMeteorraj alatt a konzolra kattintva a robotpilóta kapcsol be."]}},
    "launch_checklist": {
        "en": {"title": "Launch checklist", "subtitle": "Manual N-2", "pages": [
            "1. Course set at the navigation console.\n2. Switch panel (left of the pilot's seat): MAIN POWER, FUEL PUMPS, AVIONICS, LIFE SUPPORT all on (green).",
            "3. Reactor, coolant and air filter not empty; coolant pipes and power cable whole (manual S-1).\n4. Pilot in the pilot's seat.",
            "5. Push the throttle (right of the seat) three notches to full. The countdown starts. If anything is missing, the lever springs back and the missing steps are listed.\n\nAfter landing everything powers down."]},
        "hu": {"title": "Indítási ellenőrzőlista", "subtitle": "N-2-es kézikönyv", "pages": [
            "1. Irány beállítva a navigációs konzolon.\n2. Kapcsolótábla (a pilótaülés bal oldalán): FŐ ÁRAM, ÜZEMANYAG-SZIVATTYÚK, AVIONIKA, ÉLETFENNTARTÁS mind bekapcsolva (zöld).",
            "3. Reaktor, hűtőfolyadék és légszűrő nem üres; hűtőcső és tápkábel ép (S-1-es kézikönyv).\n4. Pilóta a pilótaülésben.",
            "5. Told a tolóerőkart (az ülés jobb oldalán) három fokozattal teljesen előre. Indul a visszaszámlálás. Ha valami hiányzik, a kar visszaugrik, és a lista kiírja, mi maradt ki.\n\nLeszállás után minden lekapcsol."]}},
    "meteor_piloting": {
        "en": {"title": "Evasive piloting", "subtitle": "Manual N-3", "pages": [
            "A proximity alert means a meteor shower ahead (about 30 seconds). The radar (top right) shows the rocks around the ship: red ones are on a collision course.",
            "From the pilot's seat steer with W (up), S (down), A (left), D (right). The ship drifts and slows on its own.",
            "Every hit costs 5% hull and 1% navigation. Without a pilot the ship drifts; the autopilot (navigation console) dodges what it can for a little fuel."]},
        "hu": {"title": "Kitérő manőverek", "subtitle": "N-3-as kézikönyv", "pages": [
            "A közelségi riasztás meteorrajt jelent előttünk (kb. 30 másodperc). A radar (jobb fent) mutatja a hajó körüli sziklákat: a pirosak ütközőpályán vannak.",
            "A pilótaülésből kormányozz: W fel, S le, A balra, D jobbra. A hajó magától sodródik és lassul.",
            "Minden találat 5% burkolatba és 1% navigációba kerül. Pilóta nélkül a hajó sodródik; a robotpilóta (navigációs konzol) kitér, amit tud, egy kis üzemanyagért."]}},
    "repairs": {
        "en": {"title": "Field repairs", "subtitle": "Manual R-1", "pages": [
            "Incidents show where they happen:\n- sparking wall panel: short circuit\n- steaming vent: overheating\n- spraying pipe: leak or pressure loss\n- flames on the floor: electrical fire",
            "Tools: soldering iron (sparks), multitool (steam), sealant foam (pipes), extinguisher or a water bucket (fire). A repair kit replaces any of them.",
            "Right click the fault with the tool. Most repairs open a quick board (wiring, valve, pressure). A wrong answer costs 5% and the fault stays. Unfixed incidents cost 25% when their time runs out."]},
        "hu": {"title": "Helyszíni javítás", "subtitle": "R-1-es kézikönyv", "pages": [
            "Az incidensek ott jelennek meg, ahol keletkeznek:\n- szikrázó falpanel: rövidzárlat\n- gőzölgő szellőző: túlmelegedés\n- spriccelő cső: szivárgás vagy nyomásvesztés\n- láng a padlón: elektromos tűz",
            "Szerszámok: forrasztópáka (szikrák), multiszerszám (gőz), tömítőhab (csövek), poroltó vagy vödör víz (tűz). A javítókészlet bármelyiket helyettesíti.",
            "Kattints jobb gombbal a hibára a szerszámmal. A legtöbb javítás egy gyors táblát nyit (vezetékezés, szelep, nyomás). Rossz megoldás 5%-ba kerül, és a hiba megmarad. A le nem javított incidens lejártakor 25%-ot visz el."]}},
    "ship_systems": {
        "en": {"title": "Reactor, coolant, life support", "subtitle": "Manual S-1", "pages": [
            "Engine room:\n- reactor: 8 units, a uranium fuel rod adds 4\n- coolant tank: 4 units, clean water or ice adds 2\n- life support unit: 4 units, an air filter adds 4\nEach flight uses 1 of each. Test hops are free.",
            "Coolant pipes (copper) must join the reactor to both engine ports and to the coolant tank. The power cable (black and yellow) must run from the reactor to the bridge.",
            "Pipes and cables join up by themselves and can be broken and laid again. Recipes: 3 scrap metal = 6 pipes, 3 salvaged wiring = 6 cables."]},
        "hu": {"title": "Reaktor, hűtés, életfenntartás", "subtitle": "S-1-es kézikönyv", "pages": [
            "Gépterem:\n- reaktor: 8 egység, egy urán fűtőelem +4\n- hűtőtartály: 4 egység, tiszta víz vagy jég +2\n- életfenntartó egység: 4 egység, egy légszűrő +4\nMinden repülés mindegyikből 1-et használ. A tesztrepülés ingyenes.",
            "A hűtőcsőnek (réz) össze kell kötnie a reaktort mindkét hajtóműcsatlakozóval és a hűtőtartállyal. A tápkábelnek (fekete-sárga) a reaktortól a hídig kell érnie.",
            "A csövek és kábelek maguktól összeillenek, kibonthatók és újra lerakhatók. Recept: 3 fémhulladék = 6 cső, 3 mentett vezeték = 6 kábel."]}},
    "space_suit": {
        "en": {"title": "EVA suit", "subtitle": "Manual E-1", "pages": [
            "Four pieces: helmet, suit (with the oxygen tank), trousers, boots. Without all four, vacuum takes your air in about 15 seconds.",
            "A full tank lasts about 10 minutes; it refills aboard the ship while life support is above 20%. Below 20% oxygen the suit beeps.",
            "The suit also protects against acid water and radiation on Earth. You cannot eat or drink with the helmet on."]},
        "hu": {"title": "Űrruha", "subtitle": "E-1-es kézikönyv", "pages": [
            "Négy darab: sisak, ruha (oxigéntartállyal), nadrág, csizma. Ha nincs meg mind a négy, a vákuum kb. 15 másodperc alatt elveszi a levegődet.",
            "Egy teli tartály kb. 10 percig tart; a hajóban újratöltődik, ha az életfenntartás 20% felett van. 20% oxigén alatt a ruha sípol.",
            "A ruha a Földön a savas víz és a sugárzás ellen is véd. Sisakban nem lehet enni és inni."]}},
    "diagnostics": {
        "en": {"title": "Diagnostics and test hops", "subtitle": "Manual D-1", "pages": [
            "Worn subsystems can hide faults after a landing. Hidden faults raise the risk that a takeoff ends in an explosion.",
            "The diagnostic console scans the ship (once a minute) and lists what it finds, with a risk estimate.",
            "Test hop: sneak and click the diagnostic console twice within 5 seconds. The ship lifts off and lands in place; every hidden fault shows up as an incident you can repair at home."]},
        "hu": {"title": "Diagnosztika és tesztrepülés", "subtitle": "D-1-es kézikönyv", "pages": [
            "A kopott alrendszerek leszállás után rejtett hibát kaphatnak. A rejtett hiba növeli az esélyét, hogy a felszállás robbanással végződik.",
            "A diagnosztikai konzol átvizsgálja a hajót (percenként egyszer), és kiírja, amit talál, kockázatbecsléssel.",
            "Tesztrepülés: guggolva kattints kétszer a diagnosztikai konzolra 5 másodpercen belül. A hajó felszáll és ugyanott leszáll; minden rejtett hiba incidensként előjön, amit otthon kijavíthatsz."]}},
    "drive_kits": {
        "en": {"title": "Drive upgrades", "subtitle": "Manual P-1", "pages": [
            "Tier 1: x1. Tier 2 (ion drive kit): x3. Tier 3 (fusion drive kit): x10. Install them in order: hold the kit and right click the navigation console while landed.",
            "Ion drive kit: 4 titanium plates, a rare-earth magnet, 2 battery cells, a circuit, a solar cell.\nFusion drive kit: 4 titanium plates, helium-3 regolith (Moon), 2 uranium fuel rods, a circuit, a magnet."]},
        "hu": {"title": "Hajtómű-fejlesztés", "subtitle": "P-1-es kézikönyv", "pages": [
            "1. szint: x1. 2. szint (ionhajtómű-készlet): x3. 3. szint (fúziós hajtómű-készlet): x10. Sorban kell beszerelni: fogd a készletet, és kattints jobb gombbal a navigációs konzolra, amíg a hajó le van szállva.",
            "Ionhajtómű-készlet: 4 titánlemez, ritkaföldfém-mágnes, 2 akkumulátorcella, áramkör, napelemcella.\nFúziós készlet: 4 titánlemez, hélium-3 regolit (Hold), 2 urán fűtőelem, áramkör, mágnes."]}},
    "ship_farm": {
        "en": {"title": "Ship's farm", "subtitle": "Manual F-1", "pages": [
            "Earth's soil is dead (Grey Bloom). Nothing can be tilled or planted outside a ship.",
            "Bring living soil from an oasis. Aboard, a hoe turns it into farmland. Ordinary dirt stays dead even aboard.",
            "Seeds: seed vault, farms, the oasis refuge. Clean water: fill a canister in an oasis pond (other water is polluted)."]},
        "hu": {"title": "A hajó farmja", "subtitle": "F-1-es kézikönyv", "pages": [
            "A Föld talaja halott (Szürke Virágzás). Hajón kívül semmit nem lehet kapálni vagy ültetni.",
            "Hozz élő talajt egy oázisból. A hajóban kapával termőföld lesz belőle. A sima föld a hajóban is halott marad.",
            "Mag: magbank, tanyák, az oázis menedéktelepe. Tiszta víz: tölts meg egy kannát egy oázistóban (minden más víz szennyezett)."]}},
    "salvage": {
        "en": {"title": "Salvage and power", "subtitle": "Manual M-1", "pages": [
            "Take wrecks and machines apart where they stand: the multitool for bolts, panels and boards, the cutting torch for metal. One step per click; each step gives parts.",
            "The recycler turns parts and finds into materials. Feed it a stack (right click), give it power (4 kW, +2 per module) and a chest beside it for the output.",
            "Modules beside the recycler: shredder (twice as fast), smelter (+15%, scrap comes out as iron), sorter (+10% and rare materials). Power: solar panels by day, diesel generators on fuel, battery banks in between, joined by cable."]},
        "hu": {"title": "Bontás és energia", "subtitle": "M-1-es kézikönyv", "pages": [
            "A roncsokat és gépeket a helyükön szedd szét: multiszerszám a csavarokhoz, burkolatokhoz, lapokhoz, vágópisztoly a fémhez. Kattintásonként egy lépés, mindegyik ad alkatrészt.",
            "A bontógép az alkatrészeket és leleteket nyersanyaggá bontja. Adj bele egy köteget (jobb klikk), áramot (4 kW, modulonként +2), és tegyél mellé ládát a kimenetnek.",
            "Modulok a bontógép mellett: daráló (kétszer gyorsabb), olvasztó (+15%, a hulladék vasként jön ki), szétválogató (+10% és ritka anyagok). Áram: napelem nappal, dízelaggregátor üzemanyaggal, akkumulátortelep a kettő között, kábellel összekötve."]}},
    "spaceport": {
        "en": {"title": "Restoring a Kármán spaceport", "subtitle": "Manual K-1, Aurora launch operations", "pages": [
            "Order: power plant, launch control, fabrication hall, fuel depot, assembly crane. Each has a control cabinet; right click it for the checklist.",
            "Every cabinet wants: the facility before it running, the debris around the cabinet cleared (rubble and broken concrete), its materials delivered, and finally a calibration.",
            "The reactor burns uranium fuel rods (one lasts three days) and breaks down now and then: a repair kit at the power plant cabinet. Without power the whole spaceport stops.",
            "Launch control prints manuals (paper in the printer). The fuel depot stores propellant: bitumen or helium-3 regolith."]},
        "hu": {"title": "Egy Kármán-űrkikötő helyreállítása", "subtitle": "K-1-es kézikönyv, Aurora indítási műveletek", "pages": [
            "Sorrend: erőmű, indítóközpont, gyártócsarnok, üzemanyagtelep, szerelődaru. Mindegyiknek van vezérlőszekrénye; jobb klikk rá: ellenőrzőlista.",
            "Minden szekrény kéri: az előző létesítmény működjön, a szekrény körüli törmelék (törmelék és törött beton) legyen eltakarítva, az anyagai legyenek leadva, végül a kalibrálást.",
            "A reaktor urán fűtőelemet éget (egy három napig tart), és időnként meghibásodik: javítókészlet az erőmű szekrényénél. Áram nélkül az egész űrkikötő leáll.",
            "Az indítóközpont kézikönyvet nyomtat (papír a nyomtatóba). Az üzemanyagtelep hajtóanyagot tárol: bitument vagy hélium-3 regolitot."]}},
    "mothership": {
        "en": {"title": "Building the mothership", "subtitle": "Manual K-2, Ark-5 programme", "pages": [
            "The fabricator makes the section kits (sneak + right click chooses the kit, then deliver the materials). Engines, reactor and laboratory need the Moon's materials.",
            "Give the crane console the next kit: keel, hull, engines, reactor, life support, hydroponics, laboratory, quarters, hangar, bridge. The crane builds each section in the hall, block by block.",
            "When all ten are built and the depot holds enough propellant, the mothership can leave."]},
        "hu": {"title": "Az anyahajó építése", "subtitle": "K-2-es kézikönyv, Bárka-5 program", "pages": [
            "A gyártógép készíti a szekciókészleteket (guggolás + jobb klikk: készlet választása, utána add le az anyagokat). A hajtómű, a reaktor és a labor holdi anyagot kér.",
            "A darukonzolnak add a következő készletet: gerinc, törzs, hajtóművek, reaktor, életfenntartás, farm, labor, szállás, hangár, híd. A daru blokkról blokkra építi fel a szekciót a csarnokban.",
            "Ha mind a tíz kész, és a telepen elég hajtóanyag van, az anyahajó indulhat."]}},
}
