# InterSolarSystem – 0. fázis (előkészítés) design

Dátum: 2026-10-29
Állapot: jóváhagyásra vár

## Megerősített projektdöntések

| # | Döntés | Eredmény |
|---|---|---|
| 1 | Hajóépítés | Hibrid: előre tervezett hajótest-osztályok, bennük bővíthető belső tér és választható modulok |
| 2 | Szóló / multi | Szerver-hitelesített architektúra az elejétől; tesztelés szólóban és LAN-on, multi-finomhangolás később |
| 3 | Főcél | Eljutni a Proxima rendszerbe, lakhatási zónás bolygó(k) felkutatása és benépesítése. Opcionális, apró lore-morzsák a kihalás okáról (csak loot/szöveg, nincs külön rendszer) |
| 4 | Kilépés utazás közben (MP) | A játékos a hajóba tér vissza; az idő telik, amíg legalább egy játékos online; nulla játékosnál megáll |

Egyéb rögzített: Minecraft 26.3, Fabric (Fabric API + Loom, nem-remapelt plugin, Mojang nevek), Java 25 (a Fabric docs 26.1-es oldala alapján; 26.3-ra ellenőrizni), fő könyvtár: `/media/maku/NVME124/Development/UltimateMCMod`.

## Fejlesztői környezet

- Szerkesztő: VS Code + Extension Pack for Java (Neovim később opció).
- JDK 25 (Arch/CachyOS: `jdk25-openjdk`), Gradle a sablon wrapperén át.
- Git a projekt gyökerében, első nap.
- Fejlesztő háttere: C#, Go, minimális C++; Java-ban kezdő → a magyarázatok a Java-specifikus részeket részletezzék.

## Kész, ha (0. fázis)

1. `./gradlew runClient` elindítja a modot Minecraft 26.3-ban hiba nélkül.
2. A repó git alatt van, értelmes `.gitignore`-ral, első commit megvan.
3. Egy teszt-blokk és teszt-tárgy regisztrálva, nevük `en_us` / `de_de` / `hu_hu` lang fájlból jön.
4. A datagen lefut (`./gradlew runDatagen`), és legalább lang + egy recept/loot table generálódik.
5. A modulstruktúra csomagszinten létezik: `core`, `ship`, `travel`, `space`, `worldgen`, `survival`, `content`.
6. Kliens/szerver szétválasztás: a mod `main` (közös/szerver) és `client` entrypointtal indul.

## Lépések

1. JDK 25 telepítése és ellenőrzése.
2. A hivatalos Fabric sablon-generátor / example mod használata; a pontos Loom-, Gradle-, Fabric API-verziókat onnan vesszük (nem emlékezetből).
3. Git init, első commit.
4. Csomagváz (`hu.kriwdi.intersolarsystem.<modul>` – csomagnév egyeztetendő).
5. Teszt-blokk/tárgy + lang + datagen.
6. Kipróbálás `runClient`-tel.

## Hatókörön kívül (Ötletdoboz)

Hajó, utazás, minigame-ek, worldgen: későbbi fázisok.

## Nyitott pont

- Mod ID és csomagnév: javaslat `intersolarsystem` / `hu.kriwdi.intersolarsystem`.
