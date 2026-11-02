package hu.kriwdi.intersolarsystem.client.datagen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.kriwdi.intersolarsystem.ship.ShipCommands;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipHullClass;
import hu.kriwdi.intersolarsystem.ship.ShipIncidentType;
import hu.kriwdi.intersolarsystem.ship.ShipSubsystem;

/**
 * Every translatable string of the mod, one row per key with all three languages side by side. Enum texts are keyed by
 * the enum constant (not by position), and {@link #rows()} fails datagen if an enum value has no text, so adding or
 * reordering enum values cannot pair a key with the wrong text.
 */
public final class ModLangTable {
	public enum Language {
		ENGLISH("en_us"), GERMAN("de_de"), HUNGARIAN("hu_hu");

		private final String code;

		Language(String code) {
			this.code = code;
		}

		public String code() {
			return code;
		}
	}

	public record Row(String key, String en, String de, String hu) {
		public String text(Language language) {
			return switch (language) {
				case ENGLISH -> en;
				case GERMAN -> de;
				case HUNGARIAN -> hu;
			};
		}
	}

	private ModLangTable() {
	}

	private static void add(List<Row> rows, String key, String en, String de, String hu) {
		rows.add(new Row(key, en, de, hu));
	}

	public static List<Row> rows() {
		List<Row> rows = new ArrayList<>();
		add(rows, "block.intersolarsystem.repair_station",
				"Repair Station",
				"Reparaturstation",
				"Javítóállomás");
		add(rows, "block.intersolarsystem.rusted_panel",
				"Rusted Panel",
				"Rostige Platte",
				"Rozsdás panel");
		add(rows, "block.intersolarsystem.ship_hatch",
				"Ship Hatch",
				"Schiffsluke",
				"Hajónyílás");
		add(rows, "command.intersolarsystem.ship.destination",
				"Destination: %s, %s",
				"Ziel: %s, %s",
				"Cél: %s, %s");
		add(rows, "command.intersolarsystem.ship.fly_not_inside",
				"You must be inside the ship to fly it",
				"Du musst im Schiff sein, um es zu fliegen",
				"A hajó belsejében kell lenned a repüléshez");
		add(rows, "command.intersolarsystem.ship.fly_not_landed",
				"The ship is not ready to take off",
				"Das Schiff ist nicht startbereit",
				"A hajó nem áll készen a felszállásra");
		add(rows, "command.intersolarsystem.ship.fly_out_of_border",
				"That destination is outside the world border",
				"Dieses Ziel liegt außerhalb der Weltgrenze",
				"Ez a cél a világhatáron kívül van");
		add(rows, "command.intersolarsystem.ship.fly_started",
				"Takeoff sequence started. Destination: %s, %s",
				"Startsequenz eingeleitet. Ziel: %s, %s",
				"A felszállási sorozat elindult. Cél: %s, %s");
		add(rows, "command.intersolarsystem.ship.fly_too_damaged",
				"The engine or the landing gear is too damaged to take off (below 20%)",
				"Das Triebwerk oder das Landegestell ist zu stark beschädigt für den Start (unter 20 %)",
				"A hajtómű vagy a leszállóegység túl sérült a felszálláshoz (20% alatt)");
		add(rows, "command.intersolarsystem.ship.incident_line",
				"Incident: %s, %s (%s left)",
				"Störung: %s, %s (noch %s)",
				"Incidens: %s, %s (még %s)");
		add(rows, "command.intersolarsystem.ship.info",
				"Hull: %s",
				"Rumpf: %s",
				"Hajótest: %s");
		add(rows, "command.intersolarsystem.ship.phase",
				"Phase: %s",
				"Phase: %s",
				"Fázis: %s");
		add(rows, "command.intersolarsystem.ship.phase_remaining",
				"Time left: %s",
				"Verbleibende Zeit: %s",
				"Hátralévő idő: %s");
		add(rows, "command.intersolarsystem.ship.read_only",
				"Ship data was saved by a newer version and is read-only",
				"Die Schiffsdaten stammen von einer neueren Version und sind schreibgeschützt",
				"A hajó adatait egy újabb verzió mentette, ezért csak olvashatók");
		add(rows, "command.intersolarsystem.ship.set",
				"Set %s to %s%%",
				"%s auf %s%% gesetzt",
				"%s beállítva: %s%%");
		add(rows, "command.intersolarsystem.ship.subsystem_line",
				"%s: %s%%",
				"%s: %s%%",
				"%s: %s%%");
		add(rows, "command.intersolarsystem.ship.unknown_subsystem",
				"Unknown subsystem: %s",
				"Unbekanntes Subsystem: %s",
				"Ismeretlen alrendszer: %s");
		add(rows, "item.intersolarsystem.repair_kit",
				"Repair Kit",
				"Reparaturset",
				"Javítókészlet");
		add(rows, "item.intersolarsystem.scrap_sensor",
				"Scrap Sensor",
				"Schrottsensor",
				"Roncsérzékelő");
		add(rows, "message.intersolarsystem.ship.alert_line",
				"%s: %s (%s)",
				"%s: %s (%s)",
				"%s: %s (%s)");
		add(rows, "message.intersolarsystem.ship.boarded",
				"You board the ship",
				"Du betrittst das Schiff",
				"Beszállsz a hajóba");
		add(rows, "message.intersolarsystem.ship.busy",
				"The ship is away or busy",
				"Das Schiff ist abwesend oder beschäftigt",
				"A hajó távol van vagy foglalt");
		add(rows, "message.intersolarsystem.ship.cannot_leave",
				"You cannot leave the ship right now",
				"Du kannst das Schiff gerade nicht verlassen",
				"Most nem tudsz kiszállni a hajóból");
		add(rows, "message.intersolarsystem.ship.countdown.flight",
				"Arrival in %s",
				"Ankunft in %s",
				"Érkezés: %s");
		add(rows, "message.intersolarsystem.ship.countdown.landing",
				"Landing in %s",
				"Landung in %s",
				"Leszállás: %s");
		add(rows, "message.intersolarsystem.ship.countdown.takeoff",
				"Takeoff in %s",
				"Start in %s",
				"Felszállás: %s");
		add(rows, "message.intersolarsystem.ship.exit_fallback",
				"The landing site is gone; you were put at the world spawn",
				"Der Landeplatz existiert nicht mehr; du wurdest zum Weltspawn gesetzt",
				"A leszállóhely megszűnt; a világ spawnjára kerültél");
		add(rows, "message.intersolarsystem.ship.exited",
				"You leave the ship",
				"Du verlässt das Schiff",
				"Kiszállsz a hajóból");
		add(rows, "message.intersolarsystem.ship.incident",
				"ALERT: %s in %s! Fix it with a repair kit at the repair station",
				"ALARM: %s bei %s! Mit einem Reparaturset an der Reparaturstation beheben",
				"RIASZTÁS: %s itt: %s! Javítsd javítókészlettel a javítóállomáson");
		add(rows, "message.intersolarsystem.ship.incident_expired",
				"The %s failure damaged %s",
				"Der Ausfall (%s) hat %s beschädigt",
				"A(z) %s hiba megrongálta ezt: %s");
		add(rows, "message.intersolarsystem.ship.interior_missing",
				"The ship interior is not available",
				"Das Schiffsinnere ist nicht verfügbar",
				"A hajó belső tere nem érhető el");
		add(rows, "message.intersolarsystem.ship.landed",
				"The ship has landed at %s, %s",
				"Das Schiff ist bei %s, %s gelandet",
				"A hajó leszállt itt: %s, %s");
		add(rows, "message.intersolarsystem.ship.landed_spawn",
				"No dry landing site nearby; the ship landed near the world spawn at %s, %s",
				"Kein trockener Landeplatz in der Nähe; das Schiff landete nahe dem Weltspawn bei %s, %s",
				"Nincs száraz leszállóhely a közelben; a hajó a világ spawnja közelében szállt le: %s, %s");
		add(rows, "message.intersolarsystem.ship.not_connected",
				"This hatch is not connected to the ship",
				"Diese Luke ist nicht mit dem Schiff verbunden",
				"Ez a nyílás nincs a hajóhoz kötve");
		add(rows, "message.intersolarsystem.ship.redocked",
				"The ship docks at this hatch",
				"Das Schiff dockt an dieser Luke an",
				"A hajó ehhez a nyíláshoz dokkol");
		add(rows, "message.intersolarsystem.ship.repair_incident",
				"You fixed the %s on %s",
				"Du hast %s bei %s behoben",
				"Kijavítottad: %s itt: %s");
		add(rows, "message.intersolarsystem.ship.repair_nothing",
				"Nothing needs repair",
				"Nichts muss repariert werden",
				"Nincs mit javítani");
		add(rows, "message.intersolarsystem.ship.repair_subsystem",
				"%s restored by %s%%",
				"%s um %s%% wiederhergestellt",
				"%s helyreállítva: %s%%");
		add(rows, "message.intersolarsystem.ship.station_needs_kit",
				"Use a repair kit here. Worst subsystem: %s",
				"Benutze hier ein Reparaturset. Schlechtestes Subsystem: %s",
				"Használj itt javítókészletet. Legrosszabb alrendszer: %s");
		add(rows, ShipCommands.hullKey(ShipHullClass.CARGO_MINER),
				"Cargo/Mining Ship",
				"Fracht-/Bergbauschiff",
				"Teher-/bányászhajó");
		add(rows, ShipCommands.hullKey(ShipHullClass.MOTHERSHIP),
				"Mothership",
				"Mutterschiff",
				"Anyahajó");
		add(rows, ShipCommands.hullKey(ShipHullClass.RESCUE_CAPSULE),
				"Rescue Capsule",
				"Rettungskapsel",
				"Mentőkapszula");
		add(rows, ShipCommands.hullKey(ShipHullClass.RESEARCH_VESSEL),
				"Research Vessel",
				"Forschungsschiff",
				"Kutatóhajó");
		add(rows, ShipCommands.hullKey(ShipHullClass.SCOUT),
				"Scout",
				"Aufklärer",
				"Felderítő");
		add(rows, ShipCommands.incidentKey(ShipIncidentType.FIRE),
				"Fire",
				"Feuer",
				"Tűz");
		add(rows, ShipCommands.incidentKey(ShipIncidentType.LEAK),
				"Leak",
				"Leck",
				"Szivárgás");
		add(rows, ShipCommands.incidentKey(ShipIncidentType.OVERHEAT),
				"Overheating",
				"Überhitzung",
				"Túlmelegedés");
		add(rows, ShipCommands.incidentKey(ShipIncidentType.PRESSURE_LOSS),
				"Pressure loss",
				"Druckverlust",
				"Nyomásvesztés");
		add(rows, ShipCommands.incidentKey(ShipIncidentType.SHORT_CIRCUIT),
				"Short circuit",
				"Kurzschluss",
				"Rövidzárlat");
		add(rows, ShipCommands.phaseKey(ShipFlightPhase.FLIGHT),
				"In flight",
				"Im Flug",
				"Repülés");
		add(rows, ShipCommands.phaseKey(ShipFlightPhase.LANDED),
				"Landed",
				"Gelandet",
				"Leszállva");
		add(rows, ShipCommands.phaseKey(ShipFlightPhase.LANDING),
				"Landing",
				"Landet",
				"Leszállás");
		add(rows, ShipCommands.phaseKey(ShipFlightPhase.TAKEOFF),
				"Taking off",
				"Startet",
				"Felszállás");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.CARGO),
				"Cargo Hold",
				"Laderaum",
				"Raktér");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.ENGINE),
				"Engine",
				"Antrieb",
				"Hajtómű");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.FUEL_ENERGY),
				"Fuel & Energy",
				"Treibstoff & Energie",
				"Üzemanyag és energia");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.HULL_ARMOR),
				"Hull Armor",
				"Rumpfpanzerung",
				"Burkolat és páncél");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.LANDING_LAUNCH),
				"Landing & Launch",
				"Landung & Start",
				"Leszálló- és kilövőegység");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.LIFE_SUPPORT),
				"Life Support",
				"Lebenserhaltung",
				"Életfenntartás");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.NAVIGATION_COMMS),
				"Navigation & Comms",
				"Navigation & Kommunikation",
				"Navigáció és kommunikáció");
		add(rows, ShipCommands.subsystemKey(ShipSubsystem.STABILIZER),
				"Stabilizer",
				"Stabilisator",
				"Stabilizáló egység");
		add(rows, "message.intersolarsystem.ship.station_needs_kit_incident",
				"Use a repair kit here. Urgent: %s in %s",
				"Benutze hier ein Reparaturset. Dringend: %s bei %s",
				"Használj itt javítókészletet. Sürgős: %s itt: %s");
		add(rows, "message.intersolarsystem.ship.station_outside",
				"The repair station only works inside the ship",
				"Die Reparaturstation funktioniert nur im Schiff",
				"A javítóállomás csak a hajó belsejében működik");
		add(rows, "block.intersolarsystem.diagnostic_console",
				"Diagnostic Console",
				"Diagnosekonsole",
				"Diagnosztikai konzol");
		add(rows, "item.intersolarsystem.scrap_metal",
				"Scrap Metal",
				"Altmetall",
				"Fémhulladék");
		add(rows, "item.intersolarsystem.salvaged_wiring",
				"Salvaged Wiring",
				"Geborgene Verkabelung",
				"Kimentett vezeték");
		add(rows, "item.intersolarsystem.salvaged_circuit",
				"Salvaged Circuit",
				"Geborgener Schaltkreis",
				"Kimentett áramkör");
		add(rows, "message.intersolarsystem.ship.repair_fault",
				"You fixed the hidden fault in %s",
				"Du hast den versteckten Fehler in %s behoben",
				"Kijavítottad a rejtett hibát: %s");
		add(rows, "message.intersolarsystem.ship.station_needs_kit_fault",
				"Use a repair kit here. Known hidden fault: %s",
				"Benutze hier ein Reparaturset. Bekannter versteckter Fehler: %s",
				"Használj itt javítókészletet. Ismert rejtett hiba: %s");
		add(rows, "message.intersolarsystem.ship.scan_cooldown",
				"The diagnostic scan is still recalibrating (%s)",
				"Der Diagnosescan kalibriert sich noch (%s)",
				"A diagnosztika még kalibrál (%s)");
		add(rows, "message.intersolarsystem.ship.scan_done",
				"Scan complete: %s known hidden fault(s), %s new",
				"Scan abgeschlossen: %s bekannte versteckte Fehler, %s neu",
				"Vizsgálat kész: %s ismert rejtett hiba, ebből %s új");
		add(rows, "message.intersolarsystem.ship.scan_fault_line",
				" - hidden fault: %s",
				" - versteckter Fehler: %s",
				" - rejtett hiba: %s");
		add(rows, "message.intersolarsystem.ship.scan_risk",
				"Estimated takeoff risk: %s%% (only what the scan could see)",
				"Geschätztes Startrisiko: %s%% (nur was der Scan sehen konnte)",
				"Becsült felszállási kockázat: %s%% (csak amit a vizsgálat látott)");
		add(rows, "message.intersolarsystem.ship.test_confirm",
				"Test hop: sneak and use the console again within 5 seconds to confirm",
				"Testsprung: zum Bestätigen innerhalb von 5 Sekunden erneut schleichend benutzen",
				"Tesztfelszállás: a megerősítéshez 5 másodpercen belül guggolva használd újra a konzolt");
		add(rows, "message.intersolarsystem.ship.test_started",
				"Test hop started: the ship lifts off and lands back here",
				"Testsprung gestartet: das Schiff hebt ab und landet wieder hier",
				"Tesztfelszállás indul: a hajó felemelkedik, majd ugyanide száll vissza");
		add(rows, "message.intersolarsystem.ship.test_found",
				"Test hop: %s hidden fault(s) broke out, %s of them unknown before",
				"Testsprung: %s versteckte Fehler sind aufgetreten, %s davon waren unbekannt",
				"Tesztfelszállás: %s rejtett hiba jött elő, ebből %s eddig ismeretlen volt");
		add(rows, "message.intersolarsystem.ship.test_clean",
				"Test hop: no hidden faults found",
				"Testsprung: keine versteckten Fehler gefunden",
				"Tesztfelszállás: nem jött elő rejtett hiba");
		add(rows, "message.intersolarsystem.ship.takeoff_failed",
				"TAKEOFF FAILED! An explosion at the launch site, the ship is badly damaged",
				"START FEHLGESCHLAGEN! Eine Explosion am Startplatz, das Schiff ist schwer beschädigt",
				"SIKERTELEN FELSZÁLLÁS! Robbanás a kilövőhelyen, a hajó súlyosan megsérült");
		add(rows, "screen.intersolarsystem.wiring.title",
				"Short Circuit: Rewire the Panel",
				"Kurzschluss: Verkabelung erneuern",
				"Rövidzárlat: vezetékezd újra a panelt");
		add(rows, "screen.intersolarsystem.wiring.hint",
				"Click a wire on the left, then the same colour on the right",
				"Klicke links ein Kabel, dann rechts die gleiche Farbe",
				"Kattints egy bal oldali vezetékre, majd jobbra az azonos színűre");
		add(rows, "message.intersolarsystem.ship.board_expired",
				"The repair board is no longer valid",
				"Die Reparaturtafel ist nicht mehr gültig",
				"A javítópanel már nem érvényes");
		add(rows, "message.intersolarsystem.ship.board_no_kit",
				"Hold the repair kit in your main hand while repairing",
				"Halte beim Reparieren das Reparaturset in der Haupthand",
				"Javítás közben tartsd a javítókészletet a fő kezedben");
		add(rows, "message.intersolarsystem.ship.board_failed.wiring",
				"Sparks fly! Wrong wiring damaged %s by %s%%",
				"Funken sprühen! Falsche Verkabelung hat %s um %s%% beschädigt",
				"Szikrák! A rossz bekötés megrongálta ezt: %s (−%s%%)");
		add(rows, "message.intersolarsystem.ship.board_failed.valve",
				"The valve slipped and the temperature spiked: %s lost %s%%",
				"Das Ventil rutschte ab, die Temperatur schoss hoch: %s verlor %s%%",
				"Megcsúszott a szelep, felszökött a hő: %s −%s%%");
		add(rows, "message.intersolarsystem.ship.board_failed.pressure",
				"The pressure surged through the seals: %s lost %s%%",
				"Der Druck schlug durch die Dichtungen: %s verlor %s%%",
				"A nyomás áttörte a tömítéseket: %s −%s%%");
		add(rows, "screen.intersolarsystem.valve.title",
				"Overheating: Close the Cooling Valve",
				"Überhitzung: Kühlventil schließen",
				"Túlmelegedés: zárd el a hűtőszelepet");
		add(rows, "screen.intersolarsystem.valve.hint",
				"Close the valve while the needle is in the green band (space)",
				"Schließe das Ventil, wenn die Nadel im grünen Bereich ist (Leertaste)",
				"Zárd el, amikor a mutató a zöld sávban van (szóköz)");
		add(rows, "screen.intersolarsystem.valve.close",
				"CLOSE VALVE",
				"VENTIL ZU",
				"SZELEP ZÁR");
		add(rows, "screen.intersolarsystem.pressure.title",
				"Pressure Loss: Equalise the Tanks",
				"Druckverlust: Tanks ausgleichen",
				"Nyomásvesztés: egyenlítsd ki a tartályokat");
		add(rows, "screen.intersolarsystem.pressure.hint",
				"Use the valves to bring every tank to the yellow line",
				"Bringe mit den Ventilen jeden Tank auf die gelbe Linie",
				"A szelepekkel hozz minden tartályt a sárga vonalig");
		add(rows, "screen.intersolarsystem.pressure.moves",
				"Valve operations left: %s",
				"Verbleibende Ventilbetätigungen: %s",
				"Hátralévő szelepműveletek: %s");
		add(rows, "item.intersolarsystem.multitool",
				"Multitool",
				"Multiwerkzeug",
				"Multiszerszám");
		add(rows, "block.intersolarsystem.navigation_console",
				"Navigation Console",
				"Navigationskonsole",
				"Navigációs konzol");
		add(rows, "body.intersolarsystem.earth",
				"Earth",
				"Erde",
				"Föld");
		add(rows, "body.intersolarsystem.moon",
				"Moon",
				"Mond",
				"Hold");
		add(rows, "body.intersolarsystem.venus",
				"Venus",
				"Venus",
				"Vénusz");
		add(rows, "body.intersolarsystem.mercury",
				"Mercury",
				"Merkur",
				"Merkúr");
		add(rows, "body.intersolarsystem.mars",
				"Mars",
				"Mars",
				"Mars");
		add(rows, "body.intersolarsystem.ceres",
				"Ceres",
				"Ceres",
				"Ceres");
		add(rows, "body.intersolarsystem.jupiter",
				"Jupiter",
				"Jupiter",
				"Jupiter");
		add(rows, "body.intersolarsystem.saturn",
				"Saturn",
				"Saturn",
				"Szaturnusz");
		add(rows, "body.intersolarsystem.uranus",
				"Uranus",
				"Uranus",
				"Uránusz");
		add(rows, "body.intersolarsystem.neptune",
				"Neptune",
				"Neptun",
				"Neptunusz");
		add(rows, "screen.intersolarsystem.nav.title",
				"Navigation: Star Chart",
				"Navigation: Sternkarte",
				"Navigáció: csillagtérkép");
		add(rows, "screen.intersolarsystem.nav.engine",
				"Engine tier %s (x%s speed)",
				"Antriebsstufe %s (x%s Tempo)",
				"Hajtómű %s. szint (x%s sebesség)");
		add(rows, "screen.intersolarsystem.nav.status.surface",
				"home",
				"Heimat",
				"otthon");
		add(rows, "screen.intersolarsystem.nav.status.flyby",
				"flyby loop",
				"Vorbeiflug",
				"megkerülés");
		add(rows, "screen.intersolarsystem.nav.status.locked",
				"out of reach",
				"unerreichbar",
				"elérhetetlen");
		add(rows, "screen.intersolarsystem.nav.surface_hint",
				"Surface flights on Earth: /iss ship fly <x> <z>",
				"Flüge auf der Erde: /iss ship fly <x> <z>",
				"Repülés a Földön: /iss ship fly <x> <z>");
		add(rows, "screen.intersolarsystem.nav.window_open",
				"Launch window: open",
				"Startfenster: offen",
				"Indulási ablak: nyitva");
		add(rows, "screen.intersolarsystem.nav.window_next",
				"Launch window: +%s%% travel time now, best in %s",
				"Startfenster: jetzt +%s%% Reisezeit, am besten in %s",
				"Indulási ablak: most +%s%% út, a legjobb %s múlva");
		add(rows, "screen.intersolarsystem.nav.flyby",
				"Free-return loop and back home: %s",
				"Rundflug mit freier Rückkehr bis nach Hause: %s",
				"Megkerülés szabad visszatérési pályán, haza: %s");
		add(rows, "screen.intersolarsystem.nav.locked",
				"No landing data yet: the ship cannot go there",
				"Noch keine Landedaten: das Schiff kann nicht dorthin",
				"Még nincs leszállási adat: a hajó nem mehet oda");
		add(rows, "screen.intersolarsystem.nav.locked_assist",
				"No landing data yet (route via a %s swing-by)",
				"Noch keine Landedaten (Route über Swing-by an %s)",
				"Még nincs leszállási adat (útvonal: hintamanőver ennél: %s)");
		add(rows, "screen.intersolarsystem.nav.launch",
				"LAUNCH",
				"START",
				"INDÍTÁS");
		add(rows, "message.intersolarsystem.nav.unreachable",
				"That destination is out of reach",
				"Dieses Ziel ist unerreichbar",
				"Ez a cél elérhetetlen");
		add(rows, "message.intersolarsystem.nav.launch",
				"Course set: %s. Journey time %s. Takeoff sequence started",
				"Kurs gesetzt: %s. Reisezeit %s. Startsequenz eingeleitet",
				"Irány beállítva: %s. Az út hossza %s. A felszállási sorozat elindult");
		add(rows, "command.intersolarsystem.ship.engine",
				"Engine set to tier %s (x%s speed)",
				"Antrieb auf Stufe %s gesetzt (x%s Tempo)",
				"Hajtómű beállítva: %s. szint (x%s sebesség)");
		add(rows, "command.intersolarsystem.ship.engine_line",
				"Engine: tier %s (x%s speed)",
				"Antrieb: Stufe %s (x%s Tempo)",
				"Hajtómű: %s. szint (x%s sebesség)");
		add(rows, "command.intersolarsystem.ship.journey",
				"Journey: loop around %s",
				"Reise: Rundflug um %s",
				"Út: %s megkerülése");
		add(rows, "message.intersolarsystem.travel.meteor_warning",
				"PROXIMITY ALERT: meteor shower ahead! Steer clear at the navigation console within 30 seconds",
				"ANNÄHERUNGSALARM: Meteoritenschauer voraus! Innerhalb von 30 Sekunden an der Navigationskonsole ausweichen",
				"KÖZELSÉGI RIASZTÁS: meteorraj előttünk! 30 másodpercen belül térj ki a navigációs konzolnál");
		add(rows, "message.intersolarsystem.travel.evading",
				"%s fires the thrusters: evasive course",
				"%s zündet die Triebwerke: Ausweichkurs",
				"%s begyújtja a manőverhajtóműveket: kitérő pálya");
		add(rows, "message.intersolarsystem.travel.meteor_evaded",
				"The meteor shower passes harmlessly behind the ship",
				"Der Meteoritenschauer zieht harmlos hinter dem Schiff vorbei",
				"A meteorraj ártalmatlanul elhalad a hajó mögött");
		add(rows, "message.intersolarsystem.travel.meteor_hit",
				"Impacts on the hull! The hull and the sensors are damaged",
				"Einschläge am Rumpf! Rumpf und Sensoren sind beschädigt",
				"Becsapódások a hajótesten! A burkolat és az érzékelők megsérültek");
		add(rows, "message.intersolarsystem.travel.solar_flare",
				"Solar flare! Radiation surges through the electronics and life support",
				"Sonneneruption! Strahlung jagt durch Elektronik und Lebenserhaltung",
				"Napkitörés! Sugárzás söpör végig az elektronikán és az életfenntartáson");
		add(rows, "message.intersolarsystem.travel.wreck",
				"A drifting wreck within reach: salvage tumbles in through the airlock",
				"Ein treibendes Wrack in Reichweite: Bergegut poltert durch die Luftschleuse",
				"Sodródó roncs a közelben: kimentett alkatrészek bukdácsolnak be a zsilipen");
		add(rows, "message.intersolarsystem.travel.signal",
				"Signal received: \"%s\"",
				"Signal empfangen: „%s“",
				"Jel érkezett: „%s”");
		add(rows, "message.intersolarsystem.travel.signal.0",
				"...Ark-3 to any station. Proxima burn complete. Crew in cold sleep. We will call when we wake...",
				"...Arche-3 an alle Stationen. Proxima-Zündung beendet. Crew im Kälteschlaf. Wir melden uns, wenn wir aufwachen...",
				"...Bárka-3 minden állomásnak. Proxima-gyorsítás kész. A legénység hibernálva. Jelentkezünk, ha felébredtünk...");
		add(rows, "message.intersolarsystem.travel.signal.1",
				"...the seed vaults are sealed. If anyone hears this, the codes are in the Svalbard log...",
				"...die Saatgutbunker sind versiegelt. Wer das hört: die Codes stehen im Svalbard-Logbuch...",
				"...a magbankok lezárva. Ha valaki hallja: a kódok a svalbardi naplóban vannak...");
		add(rows, "message.intersolarsystem.travel.signal.2",
				"...it is not the dust. Repeat, it is not the dust. Do not open the...",
				"...es ist nicht der Staub. Wiederhole, es ist nicht der Staub. Öffnet nicht die...",
				"...nem a por az oka. Ismétlem, nem a por. Ne nyissátok ki a...");
		add(rows, "message.intersolarsystem.travel.signal.3",
				"...Lunar Base Tranquility, day 412. Hydroponics failing. Three of us left...",
				"...Mondbasis Tranquility, Tag 412. Hydrokultur fällt aus. Wir sind noch drei...",
				"...Tranquility holdbázis, 412. nap. A hidrokultúra leáll. Hárman maradtunk...");
		add(rows, "message.intersolarsystem.travel.signal.4",
				"...a carrier tone from beyond Neptune, repeating every 4.2 hours. It is not ours...",
				"...ein Trägerton von jenseits des Neptun, alle 4,2 Stunden. Er ist nicht von uns...",
				"...vivőhang a Neptunuszon túlról, 4,2 óránként ismétlődik. Nem a miénk...");
		add(rows, "message.intersolarsystem.travel.signal.5",
				"...to whoever kept the lights on down there: thank you. Ark-1 is leaving orbit...",
				"...an alle, die dort unten das Licht anließen: danke. Arche-1 verlässt den Orbit...",
				"...annak, aki odalent égve hagyta a lámpákat: köszönjük. A Bárka-1 elhagyja a pályát...");
		add(rows, "message.intersolarsystem.travel.signal.6",
				"...Proxima b, twilight belt. Water confirmed. Air... we are not sure about the air...",
				"...Proxima b, Dämmerungsgürtel. Wasser bestätigt. Luft... bei der Luft sind wir unsicher...",
				"...Proxima b, szürkületi sáv. Víz megerősítve. A levegő... a levegőben nem vagyunk biztosak...");
		add(rows, "message.intersolarsystem.travel.signal.7",
				"...this is an automated beacon. The station is empty. Take what you need...",
				"...dies ist ein automatisches Leuchtfeuer. Die Station ist leer. Nehmt, was ihr braucht...",
				"...ez egy automata jeladó. Az állomás üres. Vigyétek, amire szükségetek van...");
		check(rows);
		return List.copyOf(rows);
	}

	/** Fails datagen on a duplicate key or an enum value without a text. */
	private static void check(List<Row> rows) {
		Set<String> keys = new HashSet<>();
		for (Row row : rows) {
			if (!keys.add(row.key())) {
				throw new IllegalStateException("Duplicate translation key: " + row.key());
			}
		}
		List<String> required = new ArrayList<>();
		for (ShipFlightPhase v : ShipFlightPhase.values()) {
			required.add(ShipCommands.phaseKey(v));
		}
		for (ShipHullClass v : ShipHullClass.values()) {
			required.add(ShipCommands.hullKey(v));
		}
		for (ShipIncidentType v : ShipIncidentType.values()) {
			required.add(ShipCommands.incidentKey(v));
		}
		for (ShipSubsystem v : ShipSubsystem.values()) {
			required.add(ShipCommands.subsystemKey(v));
		}
		for (String key : required) {
			if (!keys.contains(key)) {
				throw new IllegalStateException("Missing translation for " + key);
			}
		}
	}
}
