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
				"PROXIMITY ALERT: meteor shower ahead! Pilot: steer through it from the pilot's seat (W/A/S/D). No pilot? Engage the autopilot at the navigation console",
				"ANNÄHERUNGSALARM: Meteoritenschauer voraus! Pilot: vom Pilotensitz aus hindurchsteuern (W/A/S/D). Kein Pilot? Autopilot an der Navigationskonsole einschalten",
				"KÖZELSÉGI RIASZTÁS: meteorraj előttünk! Pilóta: a pilótaülésből kormányozz át rajta (W/A/S/D). Nincs pilóta? Kapcsold be a robotpilótát a navigációs konzolnál");
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
		add(rows, "body.intersolarsystem.earth_orbit",
				"Earth Orbit (ISS)",
				"Erdumlaufbahn (ISS)",
				"Föld körüli pálya (ISS)");
		add(rows, "screen.intersolarsystem.nav.status.orbit",
				"dock at ISS",
				"ISS-Andockung",
				"dokkolás az ISS-en");
		add(rows, "screen.intersolarsystem.nav.to_orbit",
				"Climb to orbit and dock at the ISS: %s",
				"Aufstieg in den Orbit und Andocken an der ISS: %s",
				"Felszállás pályára és dokkolás az ISS-en: %s");
		add(rows, "screen.intersolarsystem.nav.in_orbit",
				"The ship is docked at the ISS (%s from the surface)",
				"Das Schiff ist an der ISS angedockt (%s von der Oberfläche)",
				"A hajó az ISS-en dokkol (%s a felszíntől)");
		add(rows, "message.intersolarsystem.orbit.docked",
				"Docking complete. Welcome aboard what is left of the ISS. Suit up before you open the hatch",
				"Andocken abgeschlossen. Willkommen auf dem, was von der ISS übrig ist. Raumanzug anlegen, bevor du die Luke öffnest",
				"Dokkolás kész. Üdv azon, ami az ISS-ből megmaradt. Vedd fel az űrruhát, mielőtt kinyitod a zsilipet");
		add(rows, "hud.intersolarsystem.vacuum",
				"VACUUM - NO SUIT SEAL",
				"VAKUUM - ANZUG NICHT DICHT",
				"VÁKUUM - NINCS ZÁRT RUHA");
		add(rows, "item.intersolarsystem.space_helmet",
				"Space Helmet",
				"Raumhelm",
				"Űrsisak");
		add(rows, "item.intersolarsystem.space_suit",
				"Space Suit (Oxygen Tank)",
				"Raumanzug (Sauerstofftank)",
				"Űrruha (oxigéntartály)");
		add(rows, "item.intersolarsystem.space_pants",
				"Space Suit Trousers",
				"Raumanzughose",
				"Űrruha-nadrág");
		add(rows, "item.intersolarsystem.space_boots",
				"Space Boots",
				"Raumstiefel",
				"Űrcsizma");
		add(rows, "block.intersolarsystem.station_hull",
				"Station Hull Panel",
				"Stationsrumpfplatte",
				"Állomás burkolatpanel");
		add(rows, "block.intersolarsystem.station_foil",
				"Insulated Hull (Gold Foil)",
				"Isolierter Rumpf (Goldfolie)",
				"Szigetelt burkolat (aranyfólia)");
		add(rows, "block.intersolarsystem.station_window",
				"Station Window",
				"Stationsfenster",
				"Állomásablak");
		add(rows, "block.intersolarsystem.station_light",
				"Station Light Panel",
				"Stationsleuchtpaneel",
				"Állomás fénypanel");
		add(rows, "block.intersolarsystem.truss",
				"Station Truss",
				"Stationsgitterträger",
				"Rácsos főtartó");
		add(rows, "block.intersolarsystem.solar_panel",
				"Solar Array Cell",
				"Solarzellenfeld",
				"Napelemcella");
		add(rows, "screen.intersolarsystem.nav.deorbit",
				"Back down to Earth, to the last landing site: %s",
				"Zurück zur Erde, zum letzten Landeplatz: %s",
				"Vissza a Földre, az utolsó leszállóhelyre: %s");
		add(rows, "screen.intersolarsystem.nav.status.landing",
				"landing",
				"Landung",
				"leszállás");
		add(rows, "screen.intersolarsystem.nav.landing",
				"Land on the %s near Tranquility Base: %s",
				"Landung auf dem %s bei der Tranquility-Basis: %s",
				"Leszállás itt: %s, a Tranquility bázis mellett: %s");
		add(rows, "screen.intersolarsystem.nav.landed_here",
				"The ship stands on the %s (%s to here)",
				"Das Schiff steht auf dem %s (%s bis hierher)",
				"A hajó itt áll: %s (%s ide)");
		add(rows, "message.intersolarsystem.moon.landed",
				"Touchdown on the Moon, Sea of Tranquility. Apollo 11 is just to the west. Suit up: there is no air out there",
				"Aufsetzen auf dem Mond, Meer der Ruhe. Apollo 11 liegt westlich. Raumanzug anlegen: draußen gibt es keine Luft",
				"Leszállás a Holdon, a Nyugalom tengerén. Az Apollo 11 nyugatra van. Vedd fel a ruhát: odakint nincs levegő");
		add(rows, "block.intersolarsystem.lunar_rock",
				"Lunar Rock (Anorthosite)",
				"Mondgestein (Anorthosit)",
				"Holdkőzet (anortozit)");
		add(rows, "block.intersolarsystem.lunar_regolith",
				"Lunar Regolith",
				"Mondregolith",
				"Holdi regolit");
		add(rows, "block.intersolarsystem.mare_regolith",
				"Mare Regolith",
				"Mare-Regolith",
				"Mare-regolit");
		add(rows, "block.intersolarsystem.fresh_regolith",
				"Fresh Ejecta",
				"Frisches Auswurfmaterial",
				"Friss becsapódási törmelék");
		add(rows, "block.intersolarsystem.mare_basalt",
				"Mare Basalt",
				"Mare-Basalt",
				"Mare-bazalt");
		add(rows, "block.intersolarsystem.helium3_regolith",
				"Helium-3 Rich Regolith",
				"Helium-3-reiches Regolith",
				"Hélium-3-ban gazdag regolit");
		add(rows, "block.intersolarsystem.ilmenite_ore",
				"Ilmenite Ore",
				"Ilmeniterz",
				"Ilmenitérc");
		add(rows, "block.intersolarsystem.lander_engine",
				"Descent Engine Bell",
				"Abstiegstriebwerk",
				"Leszállóhajtómű-fúvóka");
		add(rows, "block.intersolarsystem.lunar_flag",
				"Faded Flag",
				"Verblichene Flagge",
				"Kifakult zászló");
		add(rows, "block.intersolarsystem.rover_wheel",
				"Rover Wheel (Wire Mesh)",
				"Roverrad (Drahtgeflecht)",
				"Holdjáró-kerék (drótháló)");
		add(rows, "block.intersolarsystem.antenna_dish",
				"High-Gain Antenna",
				"Richtantenne",
				"Nagy nyereségű antenna");
		add(rows, "block.intersolarsystem.data_terminal",
				"Data Terminal",
				"Datenterminal",
				"Adatterminál");
		add(rows, "lore.intersolarsystem.terminal.header",
				"[Tranquility Base - log %s]",
				"[Tranquility-Basis - Protokoll %s]",
				"[Tranquility bázis - %s. napló]");
		add(rows, "lore.intersolarsystem.terminal.0",
				"Day 1. The last supply flight is down. Twelve of us. The view of Earth is the worst part: it used to be blue.",
				"Tag 1. Der letzte Versorgungsflug ist gelandet. Wir sind zwölf. Der Blick auf die Erde ist das Schlimmste: sie war einmal blau.",
				"1. nap. Leszállt az utolsó ellátmányjárat. Tizenketten vagyunk. A Föld látványa a legrosszabb: valaha kék volt.");
		add(rows, "lore.intersolarsystem.terminal.1",
				"Day 212. The Ark ships need helium-3. We are mining the regolith around the clock. The ilmenite gives us oxygen.",
				"Tag 212. Die Archen brauchen Helium-3. Wir fördern rund um die Uhr Regolith. Das Ilmenit liefert uns Sauerstoff.",
				"212. nap. A Bárkáknak hélium-3 kell. Éjjel-nappal bányásszuk a regolitot. Az ilmenitből nyerjük az oxigént.");
		add(rows, "lore.intersolarsystem.terminal.2",
				"Day 390. Ark-3 left orbit today. There was no seat for us. They said the next ship would come back for the base.",
				"Tag 390. Arche-3 hat heute den Orbit verlassen. Für uns war kein Platz. Sie sagten, das nächste Schiff holt die Basis ab.",
				"390. nap. A Bárka-3 ma elhagyta a pályát. Nekünk nem jutott hely. Azt mondták, a következő hajó visszajön a bázisért.");
		add(rows, "lore.intersolarsystem.terminal.3",
				"Day 412. Hydroponics failing. Three of us left. If you are reading this, the codes for the seed vault are in the Svalbard log. Take the helium. Go.",
				"Tag 412. Hydrokultur fällt aus. Wir sind noch drei. Wer das liest: die Codes für den Saatgutbunker stehen im Svalbard-Logbuch. Nimm das Helium. Flieg.",
				"412. nap. Leáll a hidrokultúra. Hárman maradtunk. Ha ezt olvasod: a magbank kódjai a svalbardi naplóban vannak. Vidd a héliumot. Indulj.");
		add(rows, "itemGroup.intersolarsystem",
				"InterSolarSystem",
				"InterSolarSystem",
				"InterSolarSystem");
		add(rows, "block.intersolarsystem.ash",
				"Ash",
				"Asche",
				"Hamu");
		add(rows, "block.intersolarsystem.cinder_rock",
				"Cinder Rock",
				"Schlackengestein",
				"Salakkő");
		add(rows, "block.intersolarsystem.dried_silt",
				"Dried Silt",
				"Getrockneter Schlick",
				"Kiszáradt iszap");
		add(rows, "block.intersolarsystem.salt_crust",
				"Salt Crust",
				"Salzkruste",
				"Sókéreg");
		add(rows, "block.intersolarsystem.salt_block",
				"Salt Block",
				"Salzblock",
				"Sótömb");
		add(rows, "block.intersolarsystem.lithium_brine_crust",
				"Lithium Brine Crust",
				"Lithiumsole-Kruste",
				"Lítiumos sókéreg");
		add(rows, "block.intersolarsystem.irradiated_soil",
				"Irradiated Soil",
				"Verstrahlte Erde",
				"Sugárfertőzött talaj");
		add(rows, "block.intersolarsystem.trinitite",
				"Trinitite",
				"Trinitit",
				"Trinitit");
		add(rows, "block.intersolarsystem.acidic_mud",
				"Acidic Mud",
				"Saurer Schlamm",
				"Savas iszap");
		add(rows, "block.intersolarsystem.compacted_junk",
				"Compacted Junk",
				"Gepresster Schrott",
				"Préselt hulladék");
		add(rows, "block.intersolarsystem.tar_sand",
				"Tar Sand",
				"Teersand",
				"Kátrányos homok");
		add(rows, "block.intersolarsystem.oil_shale",
				"Oil Shale",
				"Ölschiefer",
				"Olajpala");
		add(rows, "block.intersolarsystem.silica_sand",
				"Silica Sand",
				"Quarzsand",
				"Kvarchomok");
		add(rows, "block.intersolarsystem.petrified_log",
				"Petrified Log",
				"Versteinerter Stamm",
				"Megkövült rönk");
		add(rows, "block.intersolarsystem.petrified_wood",
				"Petrified Wood",
				"Versteinertes Holz",
				"Megkövült fa");
		add(rows, "block.intersolarsystem.dead_log",
				"Dead Log",
				"Toter Stamm",
				"Elhalt rönk");
		add(rows, "block.intersolarsystem.bauxite_ore",
				"Bauxite Ore",
				"Bauxiterz",
				"Bauxitérc");
		add(rows, "block.intersolarsystem.deepslate_bauxite_ore",
				"Deepslate Bauxite Ore",
				"Tiefenschiefer-Bauxiterz",
				"Mélypala bauxitérc");
		add(rows, "block.intersolarsystem.titanium_ore",
				"Titanium Ore",
				"Titanerz",
				"Titánérc");
		add(rows, "block.intersolarsystem.deepslate_titanium_ore",
				"Deepslate Titanium Ore",
				"Tiefenschiefer-Titanerz",
				"Mélypala titánérc");
		add(rows, "block.intersolarsystem.uraninite_ore",
				"Uraninite Ore",
				"Uraniniterz",
				"Uraninitérc");
		add(rows, "block.intersolarsystem.deepslate_uraninite_ore",
				"Deepslate Uraninite Ore",
				"Tiefenschiefer-Uraniniterz",
				"Mélypala uraninitérc");
		add(rows, "block.intersolarsystem.spodumene_ore",
				"Spodumene Ore",
				"Spodumenerz",
				"Spodumenérc");
		add(rows, "block.intersolarsystem.deepslate_spodumene_ore",
				"Deepslate Spodumene Ore",
				"Tiefenschiefer-Spodumenerz",
				"Mélypala spodumenérc");
		add(rows, "block.intersolarsystem.monazite_ore",
				"Monazite Ore",
				"Monaziterz",
				"Monazitérc");
		add(rows, "block.intersolarsystem.deepslate_monazite_ore",
				"Deepslate Monazite Ore",
				"Tiefenschiefer-Monaziterz",
				"Mélypala monazitérc");
		add(rows, "block.intersolarsystem.scrap_pile",
				"Scrap Pile",
				"Schrotthaufen",
				"Fémhulladék-kupac");
		add(rows, "block.intersolarsystem.rubble",
				"Rubble",
				"Trümmer",
				"Törmelék");
		add(rows, "block.intersolarsystem.toxic_barrel",
				"Toxic Waste Barrel",
				"Giftmüllfass",
				"Mérgezőhulladék-hordó");
		add(rows, "block.intersolarsystem.mining_drone",
				"Broken Mining Drone",
				"Defekte Bergbaudrohne",
				"Elromlott bányászdrón");
		add(rows, "block.intersolarsystem.tar_puddle",
				"Tar Puddle",
				"Teerpfütze",
				"Kátránytócsa");
		add(rows, "item.intersolarsystem.raw_bauxite",
				"Raw Bauxite",
				"Rohbauxit",
				"Nyers bauxit");
		add(rows, "item.intersolarsystem.aluminium_ingot",
				"Aluminium Ingot",
				"Aluminiumbarren",
				"Alumíniumrúd");
		add(rows, "item.intersolarsystem.raw_titanium",
				"Raw Titanium",
				"Rohtitan",
				"Nyers titán");
		add(rows, "item.intersolarsystem.titanium_ingot",
				"Titanium Ingot",
				"Titanbarren",
				"Titánrúd");
		add(rows, "item.intersolarsystem.raw_uranium",
				"Raw Uranium",
				"Roh-Uran",
				"Nyers urán");
		add(rows, "item.intersolarsystem.uranium_ingot",
				"Enriched Uranium",
				"Angereichertes Uran",
				"Dúsított urán");
		add(rows, "item.intersolarsystem.lithium_dust",
				"Lithium Dust",
				"Lithiumstaub",
				"Lítiumpor");
		add(rows, "item.intersolarsystem.rare_earth_dust",
				"Rare Earth Dust",
				"Seltenerdstaub",
				"Ritkaföldfém-por");
		add(rows, "item.intersolarsystem.silicon",
				"Silicon Wafer",
				"Siliziumscheibe",
				"Szilíciumlapka");
		add(rows, "item.intersolarsystem.bitumen",
				"Bitumen",
				"Bitumen",
				"Bitumen");
		add(rows, "item.intersolarsystem.titanium_plate",
				"Titanium Plate",
				"Titanplatte",
				"Titánlemez");
		add(rows, "item.intersolarsystem.solar_cell",
				"Solar Cell",
				"Solarzelle",
				"Napelemcella");
		add(rows, "item.intersolarsystem.battery_cell",
				"Battery Cell",
				"Akkuzelle",
				"Akkumulátorcella");
		add(rows, "item.intersolarsystem.rare_earth_magnet",
				"Rare Earth Magnet",
				"Seltenerdmagnet",
				"Ritkaföldfém-mágnes");
		add(rows, "item.intersolarsystem.uranium_fuel_rod",
				"Uranium Fuel Rod",
				"Uran-Brennstab",
				"Urán fűtőelem");
		add(rows, "item.intersolarsystem.ion_drive_kit",
				"Ion Drive Kit",
				"Ionenantrieb-Bausatz",
				"Ionhajtómű-készlet");
		add(rows, "item.intersolarsystem.fusion_drive_kit",
				"Fusion Drive Kit",
				"Fusionsantrieb-Bausatz",
				"Fúziós hajtómű-készlet");
		add(rows, "item.intersolarsystem.titanium_pickaxe",
				"Titanium Pickaxe",
				"Titanspitzhacke",
				"Titán csákány");
		add(rows, "item.intersolarsystem.titanium_axe",
				"Titanium Axe",
				"Titanaxt",
				"Titán fejsze");
		add(rows, "item.intersolarsystem.titanium_shovel",
				"Titanium Shovel",
				"Titanschaufel",
				"Titán ásó");
		add(rows, "item.intersolarsystem.titanium_hoe",
				"Titanium Hoe",
				"Titanhacke",
				"Titán kapa");
		add(rows, "item.intersolarsystem.titanium_sword",
				"Titanium Sword",
				"Titanschwert",
				"Titán kard");
		add(rows, "message.intersolarsystem.engine.installed",
				"Engine upgraded to tier %s (x%s speed).",
				"Antrieb auf Stufe %s aufgerüstet (x%s Geschwindigkeit).",
				"A hajtómű %s. szintre fejlesztve (x%s sebesség).");
		add(rows, "message.intersolarsystem.engine.already",
				"The engine is already tier %s.",
				"Der Antrieb ist bereits Stufe %s.",
				"A hajtómű már %s. szintű.");
		add(rows, "message.intersolarsystem.engine.needs_previous",
				"Install the tier %s engine first.",
				"Zuerst den Antrieb der Stufe %s einbauen.",
				"Előbb a %s. szintű hajtóművet kell beszerelni.");
		add(rows, "message.intersolarsystem.engine.not_landed",
				"The engine can only be swapped while the ship is landed.",
				"Der Antrieb kann nur nach der Landung getauscht werden.",
				"A hajtóművet csak leszállt hajón lehet cserélni.");
		add(rows, "message.intersolarsystem.hazard.acid",
				"The acid burns through your clothes!",
				"Die Säure frisst sich durch die Kleidung!",
				"A sav átmarja a ruhádat!");
		add(rows, "message.intersolarsystem.hazard.radiation",
				"Radiation! Your Geiger counter is screaming.",
				"Strahlung! Der Geigerzähler kreischt.",
				"Sugárzás! A Geiger-számláló vijjog.");
		add(rows, "biome.intersolarsystem.ash_desert",
				"Ash Desert",
				"Aschewüste",
				"Hamusivatag");
		add(rows, "biome.intersolarsystem.dried_seabed",
				"Dried Seabed",
				"Ausgetrockneter Meeresgrund",
				"Kiszáradt tengerfenék");
		add(rows, "biome.intersolarsystem.petrified_forest",
				"Petrified Forest",
				"Versteinerter Wald",
				"Megkövült erdő");
		add(rows, "biome.intersolarsystem.irradiated_crater",
				"Irradiated Crater",
				"Verstrahlter Krater",
				"Sugárzó kráter");
		add(rows, "biome.intersolarsystem.frozen_megalopolis",
				"Frozen Megalopolis",
				"Gefrorene Megastadt",
				"Befagyott megapolisz");
		add(rows, "biome.intersolarsystem.acid_swamp",
				"Acid Swamp",
				"Säuresumpf",
				"Savas mocsár");
		add(rows, "biome.intersolarsystem.junk_mountains",
				"Junk Mountains",
				"Schrottberge",
				"Szeméthegyek");
		add(rows, "biome.intersolarsystem.salt_flats",
				"Salt Flats",
				"Salzwüste",
				"Sósivatag");
		add(rows, "biome.intersolarsystem.deep_canyons",
				"Deep Canyons",
				"Tiefe Schluchten",
				"Mély kanyonok");
		add(rows, "biome.intersolarsystem.oasis",
				"Oasis",
				"Oase",
				"Oázis");
		add(rows, "biome.intersolarsystem.abandoned_farmland",
				"Abandoned Farmland",
				"Verlassenes Ackerland",
				"Elhagyott farmvidék");
		add(rows, "biome.intersolarsystem.oil_field",
				"Oil Field",
				"Ölfeld",
				"Olajmező");
		add(rows, "biome.intersolarsystem.lunar_surface",
				"Lunar Surface",
				"Mondoberfläche",
				"Holdfelszín");
		add(rows, "biome.intersolarsystem.orbital_void",
				"Earth Orbit",
				"Erdumlaufbahn",
				"Föld körüli pálya");
		add(rows, "block.intersolarsystem.ship_plating",
				"Ship Plating",
				"Schiffspanzerung",
				"Hajóburkolat");
		add(rows, "block.intersolarsystem.ship_porthole",
				"Ship Porthole",
				"Bullauge",
				"Hajóablak");
		add(rows, "block.intersolarsystem.ship_thruster",
				"Ship Thruster",
				"Schiffstriebwerk",
				"Hajtóműfúvóka");
		add(rows, "block.intersolarsystem.ship_viewport",
				"Viewport",
				"Sichtfenster",
				"Kilátóablak");
		add(rows, "block.intersolarsystem.dead_planks",
				"Dead Planks",
				"Totholzbretter",
				"Elhalt fa deszka");
		add(rows, "block.intersolarsystem.dead_slab",
				"Dead Slab",
				"Totholzstufe",
				"Elhalt fa lap");
		add(rows, "block.intersolarsystem.dead_stairs",
				"Dead Stairs",
				"Totholztreppe",
				"Elhalt fa lépcső");
		add(rows, "block.intersolarsystem.dead_fence",
				"Dead Fence",
				"Totholzzaun",
				"Elhalt fa kerítés");
		add(rows, "message.intersolarsystem.nav.blocked.read_only",
				"The ship's save could not be read, so it stays grounded (see the log)",
				"Der Schiffsspeicherstand konnte nicht gelesen werden, das Schiff bleibt am Boden (siehe Log)",
				"A hajó mentése nem olvasható, ezért nem szállhat fel (lásd a naplót)");
		add(rows, "message.intersolarsystem.nav.blocked.flying",
				"The ship is already under way: wait until it has landed",
				"Das Schiff ist schon unterwegs: warte, bis es gelandet ist",
				"A hajó már úton van: várd meg, amíg leszáll");
		add(rows, "message.intersolarsystem.nav.blocked.no_site",
				"The ship has no landing site: leave and board again through its hatch",
				"Das Schiff hat keinen Landeplatz: steig aus und durch die Luke wieder ein",
				"A hajónak nincs leszállóhelye: szállj ki, és szállj be újra az ajtaján");
		add(rows, "message.intersolarsystem.nav.blocked.not_aboard",
				"You can only launch from inside the ship",
				"Starten geht nur von Bord des Schiffes",
				"Indítani csak a hajó belsejéből lehet");
		add(rows, "screen.intersolarsystem.nav.already_here",
				"The ship is already here: choose another destination",
				"Das Schiff ist schon hier: wähle ein anderes Ziel",
				"A hajó már itt van: válassz másik célt");
		add(rows, "block.intersolarsystem.ship_plating_stairs",
				"Ship Plating Stairs",
				"Schiffsbeplankungstreppe",
				"Hajóburkolat-lépcső");
		add(rows, "block.intersolarsystem.ship_plating_slab",
				"Ship Plating Slab",
				"Schiffsbeplankungsstufe",
				"Hajóburkolat-lap");
		add(rows, "block.intersolarsystem.ship_stripe",
				"Ship Livery Plating",
				"Schiffsbeplankung mit Zierstreifen",
				"Csíkos hajóburkolat");
		add(rows, "block.intersolarsystem.ship_light_red",
				"Port Navigation Light",
				"Backbord-Positionslicht",
				"Bal oldali helyzetjelző fény");
		add(rows, "block.intersolarsystem.ship_light_green",
				"Starboard Navigation Light",
				"Steuerbord-Positionslicht",
				"Jobb oldali helyzetjelző fény");
		add(rows, "block.intersolarsystem.ship_hatch_top",
				"Ship Hatch (Upper Half)",
				"Schiffsluke (obere Hälfte)",
				"Hajóajtó (felső fele)");
		add(rows, "block.intersolarsystem.astronaut_seat",
				"Astronaut Seat",
				"Astronautensitz",
				"Űrhajós ülés");
		add(rows, "entity.intersolarsystem.seat",
				"Seat",
				"Sitz",
				"Ülés");
		add(rows, "block.intersolarsystem.station_radiator",
				"Station Radiator",
				"Stationsradiator",
				"Állomás-hőleadó panel");
		add(rows, "block.intersolarsystem.station_glass",
				"Station Glass",
				"Stationsglas",
				"Állomásüveg");
		add(rows, "message.intersolarsystem.barren_soil",
				"The dead soil is barren: plants only grow in a ship's farm",
				"Der tote Boden ist unfruchtbar: Pflanzen wachsen nur in der Schiffsfarm",
				"A halott talaj terméketlen: növény csak a hajó farmján nő");
		add(rows, "message.intersolarsystem.helmet_blocks_food",
				"You can't eat or drink with the helmet on",
				"Mit aufgesetztem Helm kannst du nicht essen oder trinken",
				"Sisakban nem tudsz enni vagy inni");
		add(rows, "subtitles.intersolarsystem.suit_breath",
				"Breathing in the suit",
				"Atmen im Anzug",
				"Légzés a szkafanderben");
		add(rows, "subtitles.intersolarsystem.oxygen_alarm",
				"Low oxygen alarm",
				"Sauerstoffalarm",
				"Oxigénriasztás");
		add(rows, "subtitles.intersolarsystem.suffocate",
				"Gasping for air",
				"Nach Luft schnappen",
				"Fulladozás");
		add(rows, "subtitles.intersolarsystem.helmet_blocked",
				"Food bumps the visor",
				"Essen stößt an das Visier",
				"Az étel a sisakrostélynak ütközik");
		add(rows, "block.intersolarsystem.ship_window",
				"Tinted Cockpit Window",
				"Getöntes Cockpitfenster",
				"Sötétített pilótafülke-ablak");
		add(rows, "block.intersolarsystem.oasis_grass",
				"Oasis Grass",
				"Oasengras",
				"Oázisfű");
		add(rows, "block.intersolarsystem.living_soil",
				"Living Soil",
				"Lebendiger Boden",
				"Élő talaj");
		add(rows, "item.intersolarsystem.water_canister",
				"Water Canister",
				"Wasserkanister",
				"Vizeskanna");
		add(rows, "item.intersolarsystem.clean_water_canister",
				"Clean Water Canister",
				"Kanister mit sauberem Wasser",
				"Tiszta vizes kanna");
		add(rows, "message.intersolarsystem.dead_soil",
				"This soil is dead: a ship's farm needs living soil from an oasis",
				"Dieser Boden ist tot: die Schiffsfarm braucht lebendigen Boden aus einer Oase",
				"Ez a talaj halott: a hajó farmjához élő talaj kell egy oázisból");
		add(rows, "message.intersolarsystem.water_polluted",
				"This water is polluted: clean water is found only at an oasis",
				"Dieses Wasser ist verseucht: sauberes Wasser gibt es nur in einer Oase",
				"Ez a víz szennyezett: tiszta víz csak az oázisban van");
		add(rows, "message.intersolarsystem.water_filled",
				"You fill the canister with clean water",
				"Du füllst den Kanister mit sauberem Wasser",
				"Megtöltöd a kannát tiszta vízzel");
		add(rows, "message.intersolarsystem.travel.autopilot",
				"%s engages the autopilot: it will dodge what it can (fuel cost)",
				"%s schaltet den Autopiloten ein: er weicht aus, so gut er kann (kostet Treibstoff)",
				"%s bekapcsolja a robotpilótát: kitér, amit tud (üzemanyagba kerül)");
		add(rows, "message.intersolarsystem.travel.meteor_hits",
				"The meteor shower is behind us: %s hits, hull -%s%%",
				"Der Meteoritenschauer liegt hinter uns: %s Treffer, Hülle -%s%%",
				"A meteorraj mögöttünk: %s találat, burkolat -%s%%");
		add(rows, "message.intersolarsystem.nav.course_set",
				"Course set: %s. Pilot to the seat: switches on, then push the throttle to full",
				"Kurs gesetzt: %s. Pilot an den Sitz: Schalter ein, dann den Schubhebel ganz nach vorn",
				"Irány beállítva: %s. Pilóta az ülésbe: kapcsolók be, aztán told előre teljesen a tolóerőkart");
		add(rows, "message.intersolarsystem.checklist.header",
				"Launch checklist, still to do:",
				"Startcheckliste, noch offen:",
				"Indítási ellenőrzőlista, még hátra van:");
		add(rows, "message.intersolarsystem.checklist.course",
				"choose a destination at the navigation console",
				"ein Ziel an der Navigationskonsole wählen",
				"válassz célt a navigációs konzolon");
		add(rows, "message.intersolarsystem.checklist.switch",
				"switch on: %s (switch panel left of the pilot's seat)",
				"einschalten: %s (Schalttafel links vom Pilotensitz)",
				"kapcsold be: %s (kapcsolótábla a pilótaülés bal oldalán)");
		add(rows, "message.intersolarsystem.checklist.seat",
				"push the throttle from the pilot's seat",
				"den Schubhebel vom Pilotensitz aus bedienen",
				"a tolóerőkart a pilótaülésből told előre");
		add(rows, "message.intersolarsystem.switch.name.power",
				"main power",
				"Hauptstrom",
				"fő áramellátás");
		add(rows, "message.intersolarsystem.switch.name.fuel",
				"fuel pumps",
				"Treibstoffpumpen",
				"üzemanyag-szivattyúk");
		add(rows, "message.intersolarsystem.switch.name.avionics",
				"avionics",
				"Avionik",
				"avionika");
		add(rows, "message.intersolarsystem.switch.name.life",
				"life support",
				"Lebenserhaltung",
				"életfenntartás");
		add(rows, "message.intersolarsystem.switch.power",
				"Main power: %s",
				"Hauptstrom: %s",
				"Fő áramellátás: %s");
		add(rows, "message.intersolarsystem.switch.fuel",
				"Fuel pumps: %s",
				"Treibstoffpumpen: %s",
				"Üzemanyag-szivattyúk: %s");
		add(rows, "message.intersolarsystem.switch.avionics",
				"Avionics: %s",
				"Avionik: %s",
				"Avionika: %s");
		add(rows, "message.intersolarsystem.switch.life",
				"Life support: %s",
				"Lebenserhaltung: %s",
				"Életfenntartás: %s");
		add(rows, "message.intersolarsystem.switch.on",
				"ON",
				"EIN",
				"BE");
		add(rows, "message.intersolarsystem.switch.off",
				"OFF",
				"AUS",
				"KI");
		add(rows, "block.intersolarsystem.flight_panel",
				"Flight Switch Panel",
				"Flugschalttafel",
				"Repülési kapcsolótábla");
		add(rows, "block.intersolarsystem.throttle",
				"Throttle",
				"Schubhebel",
				"Tolóerőkar");
		add(rows, "screen.intersolarsystem.nav.set_course",
				"SET COURSE",
				"KURS SETZEN",
				"IRÁNY BEÁLLÍTÁSA");
		add(rows, "hud.intersolarsystem.radar.title",
				"METEOR SHOWER",
				"METEORITENSCHAUER",
				"METEORRAJ");
		add(rows, "hud.intersolarsystem.radar.steer",
				"Steer: W/A/S/D",
				"Steuern: W/A/S/D",
				"Kormányzás: W/A/S/D");
		add(rows, "hud.intersolarsystem.radar.no_pilot",
				"No pilot! Sit in the pilot's seat",
				"Kein Pilot! Setz dich auf den Pilotensitz",
				"Nincs pilóta! Ülj a pilótaülésbe");
		add(rows, "hud.intersolarsystem.radar.hits",
				"Hits: %s",
				"Treffer: %s",
				"Találat: %s");
		add(rows, "command.intersolarsystem.ship.meteors_failed",
				"No meteor shower: the ship is not in flight, or another event is under way",
				"Kein Meteoritenschauer: das Schiff fliegt nicht, oder ein anderes Ereignis läuft",
				"Nincs meteorraj: a hajó nem repül, vagy már zajlik egy esemény");
		add(rows, "block.intersolarsystem.pipe",
				"Coolant Pipe",
				"Kühlmittelrohr",
				"Hűtőcső");
		add(rows, "block.intersolarsystem.cable",
				"Power Cable",
				"Stromkabel",
				"Tápkábel");
		add(rows, "block.intersolarsystem.reactor",
				"Ship Reactor",
				"Schiffsreaktor",
				"Hajóreaktor");
		add(rows, "block.intersolarsystem.coolant_tank",
				"Coolant Tank",
				"Kühlmitteltank",
				"Hűtőfolyadék-tartály");
		add(rows, "block.intersolarsystem.life_support_unit",
				"Life Support Unit",
				"Lebenserhaltungseinheit",
				"Életfenntartó egység");
		add(rows, "block.intersolarsystem.sparking_panel",
				"Sparking Panel",
				"Funkende Verkleidung",
				"Szikrázó panel");
		add(rows, "block.intersolarsystem.steam_vent",
				"Steam Vent",
				"Dampfauslass",
				"Gőzölgő szellőző");
		add(rows, "block.intersolarsystem.leaking_pipe",
				"Leaking Pipe",
				"Leckendes Rohr",
				"Szivárgó cső");
		add(rows, "block.intersolarsystem.ship_fire",
				"Electrical Fire",
				"Kabelbrand",
				"Elektromos tűz");
		add(rows, "item.intersolarsystem.soldering_iron",
				"Soldering Iron",
				"Lötkolben",
				"Forrasztópáka");
		add(rows, "item.intersolarsystem.sealant",
				"Sealant Foam",
				"Dichtschaum",
				"Tömítőhab");
		add(rows, "item.intersolarsystem.fire_extinguisher",
				"Fire Extinguisher",
				"Feuerlöscher",
				"Poroltó");
		add(rows, "item.intersolarsystem.air_filter",
				"Air Filter Cartridge",
				"Luftfilterpatrone",
				"Légszűrő-patron");
		add(rows, "message.intersolarsystem.fault.needs",
				"%s here: fix it with a %s (or a %s)",
				"%s hier: reparieren mit %s (oder %s)",
				"%s itt: javítsd meg ezzel: %s (vagy: %s)");
		add(rows, "message.intersolarsystem.machine.full",
				"%s is full",
				"%s ist voll",
				"%s tele van");
		add(rows, "message.intersolarsystem.machine.level",
				"%s: %s / %s",
				"%s: %s / %s",
				"%s: %s / %s");
		add(rows, "message.intersolarsystem.machine.hint.reactor",
				"Each flight burns one unit; a uranium fuel rod adds four.",
				"Jeder Flug verbraucht eine Einheit; ein Uran-Brennstab bringt vier.",
				"Minden repülés egy egységet éget el; egy urán fűtőelem négyet ad.");
		add(rows, "message.intersolarsystem.machine.hint.coolant_tank",
				"Each flight uses one unit; a clean water canister or a block of ice adds two.",
				"Jeder Flug verbraucht eine Einheit; ein Kanister sauberes Wasser oder ein Eisblock bringt zwei.",
				"Minden repülés egy egységet használ; egy tiszta vizes kanna vagy egy jégtömb kettőt ad.");
		add(rows, "message.intersolarsystem.machine.hint.life_support_unit",
				"Each flight wears the filter by one; a new air filter cartridge restores four.",
				"Jeder Flug nutzt den Filter um eins ab; eine neue Luftfilterpatrone stellt vier her.",
				"Minden repülés egyet koptat a szűrőn; egy új légszűrő-patron négyet ad vissza.");
		add(rows, "message.intersolarsystem.checklist.pipes_engine",
				"lay the coolant pipe from the reactor to both engine ports",
				"das Kühlmittelrohr vom Reaktor zu beiden Triebwerksanschlüssen verlegen",
				"vezesd a hűtőcsövet a reaktortól mindkét hajtóműcsatlakozóig");
		add(rows, "message.intersolarsystem.checklist.pipes_coolant",
				"connect the coolant tank to the reactor's pipes",
				"den Kühlmitteltank an die Rohre des Reaktors anschließen",
				"kösd rá a hűtőfolyadék-tartályt a reaktor csöveire");
		add(rows, "message.intersolarsystem.checklist.cable_bridge",
				"lay the power cable from the reactor to the bridge",
				"das Stromkabel vom Reaktor zur Brücke verlegen",
				"vezesd a tápkábelt a reaktortól a hídig");
		add(rows, "message.intersolarsystem.checklist.empty.reactor",
				"refuel the reactor (uranium fuel rod)",
				"den Reaktor nachladen (Uran-Brennstab)",
				"töltsd fel a reaktort (urán fűtőelem)");
		add(rows, "message.intersolarsystem.checklist.empty.coolant_tank",
				"fill the coolant tank (clean water or ice)",
				"den Kühlmitteltank füllen (sauberes Wasser oder Eis)",
				"töltsd fel a hűtőfolyadék-tartályt (tiszta víz vagy jég)");
		add(rows, "message.intersolarsystem.checklist.empty.life_support_unit",
				"put a new air filter into the life support unit",
				"eine neue Luftfilterpatrone in die Lebenserhaltung setzen",
				"tegyél új légszűrő-patront az életfenntartó egységbe");
		add(rows, "difficulty.intersolarsystem.easy",
				"easy",
				"leicht",
				"könnyű");
		add(rows, "difficulty.intersolarsystem.normal",
				"normal",
				"normal",
				"normál");
		add(rows, "difficulty.intersolarsystem.realistic",
				"realistic",
				"realistisch",
				"realisztikus");
		add(rows, "command.intersolarsystem.difficulty.info",
				"Difficulty: %s. Crew aboard: %s, so up to %s incidents at once",
				"Schwierigkeit: %s. Besatzung an Bord: %s, also bis zu %s Störungen gleichzeitig",
				"Nehézség: %s. Legénység a hajón: %s, így egyszerre legfeljebb %s incidens");
		add(rows, "command.intersolarsystem.difficulty.set",
				"Difficulty set to %s",
				"Schwierigkeit auf %s gesetzt",
				"Nehézség beállítva: %s");
		add(rows, "command.intersolarsystem.difficulty.unknown",
				"Unknown difficulty: %s (easy, normal, realistic)",
				"Unbekannte Schwierigkeit: %s (easy, normal, realistic)",
				"Ismeretlen nehézség: %s (easy, normal, realistic)");
		add(rows, "item.intersolarsystem.note",
				"Note",
				"Notiz",
				"Feljegyzés");
		add(rows, "item.intersolarsystem.data_tape",
				"Data Tape",
				"Datenband",
				"Adatszalag");
		add(rows, "item.intersolarsystem.manual",
				"Technical Manual",
				"Technisches Handbuch",
				"Műszaki kézikönyv");
		add(rows, "subtitles.intersolarsystem.tape_start",
				"Tape clicks into place",
				"Band rastet ein",
				"Szalag bekattan");
		add(rows, "subtitles.intersolarsystem.tape_static",
				"Tape hisses",
				"Band rauscht",
				"Szalag sistereg");
		add(rows, "lore.intersolarsystem.use_hint",
				"Right click to read",
				"Rechtsklick zum Lesen",
				"Jobb klikk: elolvasás");
		add(rows, "lore.intersolarsystem.play_hint",
				"Play it in a data terminal or a radio set (right click the deck with the tape)",
				"In einem Datenterminal oder Funkgerät abspielen (mit dem Band auf das Gerät rechtsklicken)",
				"Adatterminálban vagy rádióban játszható le (jobb klikk a géppel, kézben a szalaggal)");
		add(rows, "block.intersolarsystem.skeleton_lying",
				"Skeleton (Lying)",
				"Skelett (liegend)",
				"Csontváz (fekvő)");
		add(rows, "block.intersolarsystem.skeleton_sitting",
				"Skeleton (Sitting)",
				"Skelett (sitzend)",
				"Csontváz (ülő)");
		add(rows, "block.intersolarsystem.skeleton_curled",
				"Skeleton (Curled Up)",
				"Skelett (zusammengerollt)",
				"Csontváz (összegömbölyödött)");
		add(rows, "block.intersolarsystem.skeleton_suited",
				"Astronaut's Remains",
				"Überreste eines Astronauten",
				"Űrhajós maradványai");
		add(rows, "block.intersolarsystem.dog_skeleton",
				"Dog Skeleton",
				"Hundeskelett",
				"Kutyacsontváz");
		add(rows, "block.intersolarsystem.cow_skeleton",
				"Cow Skeleton",
				"Kuhskelett",
				"Tehéncsontváz");
		add(rows, "block.intersolarsystem.horse_skeleton",
				"Horse Skeleton",
				"Pferdeskelett",
				"Lócsontváz");
		add(rows, "block.intersolarsystem.cracked_concrete",
				"Cracked Concrete",
				"Rissiger Beton",
				"Repedt beton");
		add(rows, "block.intersolarsystem.sooty_concrete",
				"Sooty Concrete",
				"Verrußter Beton",
				"Kormos beton");
		add(rows, "block.intersolarsystem.rebar_concrete",
				"Broken Concrete",
				"Gebrochener Stahlbeton",
				"Törött vasbeton");
		add(rows, "block.intersolarsystem.asphalt",
				"Asphalt",
				"Asphalt",
				"Aszfalt");
		add(rows, "block.intersolarsystem.cracked_asphalt",
				"Cracked Asphalt",
				"Rissiger Asphalt",
				"Repedt aszfalt");
		add(rows, "block.intersolarsystem.road_marking",
				"Road Marking",
				"Fahrbahnmarkierung",
				"Útburkolati jel");
		add(rows, "block.intersolarsystem.runway_marking",
				"Runway Marking",
				"Pistenmarkierung",
				"Kifutópálya-jelölés");
		add(rows, "block.intersolarsystem.hazard_concrete",
				"Hazard Stripe Concrete",
				"Warnstreifen-Beton",
				"Figyelmeztető csíkos beton");
		add(rows, "block.intersolarsystem.floor_tiles",
				"Floor Tiles",
				"Bodenfliesen",
				"Padlócsempe");
		add(rows, "block.intersolarsystem.wall_tiles",
				"Hospital Wall Tiles",
				"Krankenhaus-Wandfliesen",
				"Kórházi falicsempe");
		add(rows, "block.intersolarsystem.office_carpet",
				"Office Carpet",
				"Büroteppich",
				"Irodai padlószőnyeg");
		add(rows, "block.intersolarsystem.curtain_wall",
				"Curtain Wall",
				"Glasfassade",
				"Üvegfal");
		add(rows, "block.intersolarsystem.broken_curtain_wall",
				"Shattered Curtain Wall",
				"Zersplitterte Glasfassade",
				"Betört üvegfal");
		add(rows, "block.intersolarsystem.metal_grating",
				"Metal Grating",
				"Gitterrost",
				"Fémrács");
		add(rows, "block.intersolarsystem.ceiling_light",
				"Emergency Ceiling Light",
				"Notbeleuchtung",
				"Vészvilágítás");
		add(rows, "block.intersolarsystem.terminal_floor",
				"Terminal Floor",
				"Terminalboden",
				"Terminálpadló");
		add(rows, "block.intersolarsystem.shipping_crate",
				"Shipping Crate",
				"Frachtkiste",
				"Szállítóláda");
		add(rows, "block.intersolarsystem.office_desk",
				"Office Desk",
				"Schreibtisch",
				"Irodai íróasztal");
		add(rows, "block.intersolarsystem.office_chair",
				"Office Chair",
				"Bürostuhl",
				"Irodai szék");
		add(rows, "block.intersolarsystem.filing_cabinet",
				"Filing Cabinet",
				"Aktenschrank",
				"Iratszekrény");
		add(rows, "block.intersolarsystem.server_rack",
				"Server Rack",
				"Serverschrank",
				"Szerverszekrény");
		add(rows, "block.intersolarsystem.locker",
				"Locker",
				"Spind",
				"Öltözőszekrény");
		add(rows, "block.intersolarsystem.hospital_bed",
				"Hospital Bed",
				"Krankenhausbett",
				"Kórházi ágy");
		add(rows, "block.intersolarsystem.iv_stand",
				"IV Stand",
				"Infusionsständer",
				"Infúziós állvány");
		add(rows, "block.intersolarsystem.medical_cabinet",
				"Medical Cabinet",
				"Medizinschrank",
				"Gyógyszerszekrény");
		add(rows, "block.intersolarsystem.wheelchair",
				"Wheelchair",
				"Rollstuhl",
				"Kerekesszék");
		add(rows, "block.intersolarsystem.vending_machine",
				"Vending Machine",
				"Verkaufsautomat",
				"Italautomata");
		add(rows, "block.intersolarsystem.airport_seats",
				"Airport Seats",
				"Wartebank",
				"Váróterem-ülések");
		add(rows, "block.intersolarsystem.departure_board",
				"Departure Board",
				"Abflugtafel",
				"Indulási tábla");
		add(rows, "block.intersolarsystem.sandbags",
				"Sandbags",
				"Sandsäcke",
				"Homokzsákok");
		add(rows, "block.intersolarsystem.barbed_wire",
				"Barbed Wire",
				"Stacheldraht",
				"Szögesdrót");
		add(rows, "block.intersolarsystem.quarantine_barrier",
				"Quarantine Barrier",
				"Quarantäne-Absperrung",
				"Karantén-sorompó");
		add(rows, "block.intersolarsystem.warning_sign",
				"Biohazard Sign",
				"Biogefährdungsschild",
				"Biológiai veszély tábla");
		add(rows, "block.intersolarsystem.poster_exodus",
				"Exodus Poster",
				"Exodus-Plakat",
				"Exodus plakát");
		add(rows, "block.intersolarsystem.poster_quarantine",
				"Quarantine Notice",
				"Quarantäne-Aushang",
				"Karantén-hirdetmény");
		add(rows, "block.intersolarsystem.traffic_light",
				"Traffic Light",
				"Ampel",
				"Közlekedési lámpa");
		add(rows, "block.intersolarsystem.street_lamp",
				"Street Lamp",
				"Straßenlaterne",
				"Utcai lámpa");
		add(rows, "block.intersolarsystem.fire_hydrant",
				"Fire Hydrant",
				"Hydrant",
				"Tűzcsap");
		add(rows, "block.intersolarsystem.park_bench",
				"Park Bench",
				"Parkbank",
				"Pad");
		add(rows, "block.intersolarsystem.car_wreck_red",
				"Car Wreck",
				"Autowrack",
				"Autóroncs");
		add(rows, "block.intersolarsystem.car_wreck_blue",
				"Car Wreck (Blue)",
				"Autowrack (blau)",
				"Autóroncs (kék)");
		add(rows, "block.intersolarsystem.car_wreck_white",
				"Car Wreck (White)",
				"Autowrack (weiß)",
				"Autóroncs (fehér)");
		add(rows, "block.intersolarsystem.taxi_wreck",
				"Taxi Wreck",
				"Taxiwrack",
				"Taxironcs");
		add(rows, "block.intersolarsystem.police_car_wreck",
				"Police Car Wreck",
				"Polizeiwagenwrack",
				"Rendőrautó-roncs");
		add(rows, "block.intersolarsystem.ambulance_wreck",
				"Ambulance Wreck",
				"Krankenwagenwrack",
				"Mentőautó-roncs");
		add(rows, "block.intersolarsystem.jet_engine",
				"Jet Engine",
				"Strahltriebwerk",
				"Sugárhajtómű");
		add(rows, "block.intersolarsystem.runway_light",
				"Runway Light",
				"Pistenfeuer",
				"Kifutófény");
		add(rows, "block.intersolarsystem.runway_bollard",
				"Bollard",
				"Poller",
				"Terelőoszlop");
		add(rows, "block.intersolarsystem.dino_skull",
				"Tyrannosaurus Skull",
				"Tyrannosaurus-Schädel",
				"Tyrannosaurus-koponya");
		add(rows, "block.intersolarsystem.cryo_pod",
				"Cryo Pod",
				"Kryokapsel",
				"Hibernációs fülke");
		add(rows, "block.intersolarsystem.lab_bench",
				"Lab Bench",
				"Labortisch",
				"Laborasztal");
		add(rows, "block.intersolarsystem.fume_hood",
				"Fume Hood",
				"Abzug",
				"Vegyifülke");
		add(rows, "block.intersolarsystem.specimen_tank",
				"Specimen Tank",
				"Probentank",
				"Mintatartály");
		add(rows, "block.intersolarsystem.bunk_bed",
				"Bunk Bed",
				"Etagenbett",
				"Emeletes ágy");
		add(rows, "block.intersolarsystem.diesel_generator",
				"Diesel Generator",
				"Dieselgenerator",
				"Dízelaggregátor");
		add(rows, "block.intersolarsystem.blast_door",
				"Blast Door",
				"Druckschutztür",
				"Robbanásbiztos ajtó");
		add(rows, "death.attack.intersolarsystem.hypoxia",
				"%1$s ran out of oxygen",
				"%1$s ging der Sauerstoff aus",
				"%1$s kifogyott az oxigénből");
		add(rows, "death.attack.intersolarsystem.hypoxia.player",
				"%1$s ran out of oxygen whilst fighting %2$s",
				"%1$s ging im Kampf gegen %2$s der Sauerstoff aus",
				"%1$s kifogyott az oxigénből %2$s ellen harcolva");
		add(rows, "death.attack.intersolarsystem.decompression",
				"%1$s was exposed to the vacuum",
				"%1$s war dem Vakuum ausgesetzt",
				"%1$s a vákuumnak volt kitéve");
		add(rows, "death.attack.intersolarsystem.decompression.player",
				"%1$s was exposed to the vacuum whilst fighting %2$s",
				"%1$s war im Kampf gegen %2$s dem Vakuum ausgesetzt",
				"%1$s a vákuumnak volt kitéve %2$s ellen harcolva");
		add(rows, "message.intersolarsystem.suit_punctured",
				"Suit punctured! Seal it with sealant foam",
				"Anzug durchlöchert! Mit Dichtschaum abdichten",
				"Kilyukadt a ruha! Tömítőhabbal foltozd be");
		add(rows, "message.intersolarsystem.suit_sealed",
				"Suit sealed",
				"Anzug abgedichtet",
				"A ruha befoltozva");
		add(rows, "subtitles.intersolarsystem.suit_leak",
				"Suit hisses",
				"Anzug zischt",
				"A ruha sziszeg");
		add(rows, "subtitles.intersolarsystem.heartbeat",
				"Heart pounds",
				"Herz pocht",
				"Szívverés");
		add(rows, "subtitles.intersolarsystem.ear_ring",
				"Ears ring",
				"Ohren klingeln",
				"Cseng a fül");
		add(rows, "subtitles.intersolarsystem.tape_voice",
				"Voice on the tape",
				"Stimme auf dem Band",
				"Hang a szalagon");
		add(rows, "hud.intersolarsystem.helmet.oxygen",
				"O2",
				"O2",
				"O2");
		add(rows, "hud.intersolarsystem.helmet.suit",
				"SUIT",
				"ANZUG",
				"RUHA");
		add(rows, "hud.intersolarsystem.helmet.leak",
				"LEAK",
				"LECK",
				"SZIVÁRGÁS");
		add(rows, "hud.intersolarsystem.helmet.low",
				"O2 LOW",
				"O2 NIEDRIG",
				"KEVÉS O2");
		add(rows, "hud.intersolarsystem.helmet.hypoxia",
				"HYPOXIA",
				"HYPOXIE",
				"HIPOXIA");
		add(rows, "hud.intersolarsystem.helmet.decompression",
				"NO SEAL - VACUUM",
				"NICHT DICHT - VAKUUM",
				"NEM ZÁR - VÁKUUM");
		add(rows, "hud.intersolarsystem.helmet.pressure",
				"PRESS",
				"DRUCK",
				"NYOMÁS");
		add(rows, "block.intersolarsystem.radio_set",
				"Radio Set",
				"Funkgerät",
				"Rádió");
		add(rows, "message.intersolarsystem.tape_blank",
				"The tape is blank",
				"Das Band ist leer",
				"A szalag üres");
		add(rows, "message.intersolarsystem.radio_silence",
				"Only static. Nobody is broadcasting.",
				"Nur Rauschen. Niemand sendet.",
				"Csak zúgás. Senki nem ad.");
		add(rows, "block.intersolarsystem.recycler",
				"Recycler",
				"Recycler",
				"Bontógép");
		add(rows, "block.intersolarsystem.shredder_module",
				"Shredder Module",
				"Schreddermodul",
				"Darálómodul");
		add(rows, "block.intersolarsystem.smelter_module",
				"Smelter Module",
				"Schmelzmodul",
				"Olvasztómodul");
		add(rows, "block.intersolarsystem.sorter_module",
				"Sorter Module",
				"Sortiermodul",
				"Szétválogató modul");
		add(rows, "block.intersolarsystem.battery_bank",
				"Battery Bank",
				"Batteriebank",
				"Akkumulátortelep");
		add(rows, "item.intersolarsystem.plastic_scrap",
				"Plastic Scrap",
				"Plastikschrott",
				"Műanyaghulladék");
		add(rows, "item.intersolarsystem.rubber_scrap",
				"Rubber Scrap",
				"Gummireste",
				"Gumihulladék");
		add(rows, "item.intersolarsystem.glass_shards",
				"Glass Shards",
				"Glasscherben",
				"Üvegszilánkok");
		add(rows, "item.intersolarsystem.cutting_torch",
				"Cutting Torch",
				"Schneidbrenner",
				"Vágópisztoly");
		add(rows, "machine.intersolarsystem.recycler.status",
				"Recycler: power %1$s/%2$s kW, %3$s items queued, yield %4$s-%5$s%%, modules [%6$s] (S shredder, M smelter, R sorter)",
				"Recycler: Strom %1$s/%2$s kW, %3$s Teile in der Warteschlange, Ausbeute %4$s-%5$s%%, Module [%6$s] (S Schredder, M Schmelze, R Sortierer)",
				"Bontógép: áram %1$s/%2$s kW, %3$s tárgy sorban, kihozatal %4$s-%5$s%%, modulok [%6$s] (S daráló, M olvasztó, R szétválogató)");
		add(rows, "machine.intersolarsystem.recycler.no_power",
				"No power: run a cable from a generator, solar panel or battery bank.",
				"Kein Strom: Kabel von einem Generator, Solarpanel oder einer Batteriebank verlegen.",
				"Nincs áram: vezess kábelt egy aggregátortól, napelemtől vagy akkumulátortelepről.");
		add(rows, "machine.intersolarsystem.recycler.cannot",
				"%s cannot be recycled",
				"%s kann nicht recycelt werden",
				"%s nem bontható");
		add(rows, "machine.intersolarsystem.recycler.full",
				"The recycler's hopper is full",
				"Der Trichter des Recyclers ist voll",
				"A bontógép tölcsére tele van");
		add(rows, "machine.intersolarsystem.recycler.loaded",
				"Loaded (%s/%s)",
				"Eingelegt (%s/%s)",
				"Betöltve (%s/%s)");
		add(rows, "machine.intersolarsystem.generator.status",
				"Fuel for %s min, %s kW when running",
				"Treibstoff für %s min, %s kW im Betrieb",
				"Üzemanyag %s percre, működés közben %s kW");
		add(rows, "machine.intersolarsystem.generator.full",
				"The tank is full",
				"Der Tank ist voll",
				"A tartály tele van");
		add(rows, "machine.intersolarsystem.battery.status",
				"Charge %s%%",
				"Ladung %s%%",
				"Töltöttség %s%%");
		add(rows, "machine.intersolarsystem.module.shredder_module",
				"Shredder: twice as fast for the recycler it touches",
				"Schredder: doppelt so schnell für den angrenzenden Recycler",
				"Daráló: kétszer gyorsabb a mellette álló bontógép");
		add(rows, "machine.intersolarsystem.module.smelter_module",
				"Smelter: +15%% yield, scrap metal comes out as iron",
				"Schmelze: +15%% Ausbeute, Schrott kommt als Eisen heraus",
				"Olvasztó: +15%% kihozatal, a fémhulladék vasként jön ki");
		add(rows, "machine.intersolarsystem.module.sorter_module",
				"Sorter: +10%% yield and rare materials",
				"Sortierer: +10%% Ausbeute und seltene Stoffe",
				"Szétválogató: +10%% kihozatal és ritka anyagok");
		add(rows, "salvage.intersolarsystem.needs_tool",
				"Step %s/%s: %s - needs %s",
				"Schritt %s/%s: %s - braucht %s",
				"%s/%s. lépés: %s - ehhez kell: %s");
		add(rows, "salvage.intersolarsystem.progress",
				"Step %s/%s done (%s). Next: %s with %s",
				"Schritt %s/%s erledigt (%s). Weiter: %s mit %s",
				"%s/%s. lépés kész (%s). Következik: %s, ehhez: %s");
		add(rows, "salvage.intersolarsystem.done",
				"Taken apart",
				"Zerlegt",
				"Szétszerelve");
		add(rows, "salvage.intersolarsystem.step.wheels",
				"Unbolt the wheels",
				"Räder abschrauben",
				"Kerekek lecsavarozása");
		add(rows, "salvage.intersolarsystem.step.battery",
				"Pull the battery",
				"Batterie ausbauen",
				"Akkumulátor kiszerelése");
		add(rows, "salvage.intersolarsystem.step.body",
				"Cut up the body",
				"Karosserie zerschneiden",
				"Karosszéria szétvágása");
		add(rows, "salvage.intersolarsystem.step.engine",
				"Strip the engine",
				"Motor zerlegen",
				"Motor szétszedése");
		add(rows, "salvage.intersolarsystem.step.cowling",
				"Unbolt the cowling",
				"Verkleidung abschrauben",
				"Burkolat lecsavarozása");
		add(rows, "salvage.intersolarsystem.step.fan",
				"Cut out the fan",
				"Fan herausschneiden",
				"Ventilátor kivágása");
		add(rows, "salvage.intersolarsystem.step.core",
				"Strip the core",
				"Kerntriebwerk zerlegen",
				"Mag szétszedése");
		add(rows, "salvage.intersolarsystem.step.panels",
				"Take off the panels",
				"Abdeckungen abnehmen",
				"Burkolatlemezek leszedése");
		add(rows, "salvage.intersolarsystem.step.boards",
				"Pull the boards",
				"Platinen ziehen",
				"Áramköri lapok kihúzása");
		add(rows, "salvage.intersolarsystem.step.frame",
				"Cut up the frame",
				"Rahmen zerschneiden",
				"Váz szétvágása");
		add(rows, "salvage.intersolarsystem.step.front",
				"Remove the front",
				"Front abnehmen",
				"Előlap leszedése");
		add(rows, "salvage.intersolarsystem.step.electronics",
				"Pull the electronics",
				"Elektronik ausbauen",
				"Elektronika kiszerelése");
		add(rows, "salvage.intersolarsystem.step.cabinet",
				"Cut up the cabinet",
				"Gehäuse zerschneiden",
				"Szekrény szétvágása");
		add(rows, "salvage.intersolarsystem.step.panel",
				"Remove the control panel",
				"Bedienfeld ausbauen",
				"Kezelőpanel kiszerelése");
		add(rows, "salvage.intersolarsystem.step.wiring",
				"Pull the wiring",
				"Verkabelung ziehen",
				"Vezetékek kihúzása");
		add(rows, "salvage.intersolarsystem.step.display",
				"Remove the display",
				"Anzeige ausbauen",
				"Kijelző kiszerelése");
		add(rows, "salvage.intersolarsystem.step.control",
				"Remove the control unit",
				"Steuereinheit ausbauen",
				"Vezérlőegység kiszerelése");
		add(rows, "salvage.intersolarsystem.step.canopy",
				"Lift off the canopy",
				"Haube abnehmen",
				"Fedél leemelése");
		add(rows, "salvage.intersolarsystem.step.shell",
				"Cut up the shell",
				"Hülle zerschneiden",
				"Héj szétvágása");
		add(rows, "salvage.intersolarsystem.step.screen",
				"Remove the screen",
				"Bildschirm ausbauen",
				"Képernyő kiszerelése");
		add(rows, "salvage.intersolarsystem.step.board",
				"Pull the main board",
				"Hauptplatine ziehen",
				"Alaplap kihúzása");
		add(rows, "salvage.intersolarsystem.step.case",
				"Open the case",
				"Gehäuse öffnen",
				"Ház szétszedése");
		add(rows, "salvage.intersolarsystem.step.cells",
				"Pull the cells",
				"Zellen ausbauen",
				"Cellák kiszerelése");
		add(rows, "salvage.intersolarsystem.step.chassis",
				"Cut up the chassis",
				"Fahrgestell zerschneiden",
				"Alváz szétvágása");
		add(rows, "salvage.intersolarsystem.step.lines",
				"Disconnect the lines",
				"Leitungen trennen",
				"Vezetékek leválasztása");
		add(rows, "salvage.intersolarsystem.step.bell",
				"Cut up the nozzle",
				"Düse zerschneiden",
				"Fúvóka szétvágása");
		add(rows, "salvage.intersolarsystem.step.lamps",
				"Take out the lamps",
				"Lampen ausbauen",
				"Lámpák kiszerelése");
		add(rows, "salvage.intersolarsystem.step.pole",
				"Cut down the pole",
				"Mast absägen",
				"Oszlop levágása");
		add(rows, "salvage.intersolarsystem.step.lamp",
				"Take out the lamp",
				"Lampe ausbauen",
				"Lámpa kiszerelése");
		add(rows, "salvage.intersolarsystem.step.sash",
				"Lift out the sash",
				"Frontscheibe ausbauen",
				"Tolóablak kiemelése");
		add(rows, "salvage.intersolarsystem.step.doors",
				"Take off the doors",
				"Türen abnehmen",
				"Ajtók leszedése");
		add(rows, "salvage.intersolarsystem.step.mattress",
				"Take off the mattress",
				"Matratze abnehmen",
				"Matrac leszedése");
		add(rows, "salvage.intersolarsystem.step.bolts",
				"Undo the bolts",
				"Bolzen lösen",
				"Csavarok kioldása");
		add(rows, "salvage.intersolarsystem.step.leaf",
				"Cut up the door leaf",
				"Türblatt zerschneiden",
				"Ajtólap szétvágása");
		add(rows, "salvage.intersolarsystem.step.feed",
				"Remove the feed horn",
				"Speisehorn ausbauen",
				"Tápfej kiszerelése");
		add(rows, "salvage.intersolarsystem.step.dish",
				"Cut up the dish",
				"Schüssel zerschneiden",
				"Tányér szétvágása");
		add(rows, "salvage.intersolarsystem.step.cell",
				"Lift out the cell",
				"Zelle ausbauen",
				"Cella kiemelése");
		add(rows, "salvage.intersolarsystem.step.seats",
				"Unbolt the seats",
				"Sitze abschrauben",
				"Ülések lecsavarozása");
		add(rows, "salvage.intersolarsystem.step.beam",
				"Cut the beam",
				"Träger schneiden",
				"Tartógerenda elvágása");
		add(rows, "salvage.intersolarsystem.step.locker",
				"Cut open the locker",
				"Spind aufschneiden",
				"Szekrény felvágása");
		add(rows, "salvage.intersolarsystem.step.drawers",
				"Empty the drawers",
				"Schubladen leeren",
				"Fiókok kiürítése");
		add(rows, "block.intersolarsystem.power_cabinet",
				"Power Plant Control Cabinet",
				"Kraftwerks-Schaltschrank",
				"Erőmű vezérlőszekrény");
		add(rows, "block.intersolarsystem.control_cabinet",
				"Launch Control Cabinet",
				"Startkontroll-Schaltschrank",
				"Indítóközpont vezérlőszekrény");
		add(rows, "block.intersolarsystem.fabrication_cabinet",
				"Fabrication Hall Control Cabinet",
				"Fertigungshallen-Schaltschrank",
				"Gyártócsarnok vezérlőszekrény");
		add(rows, "block.intersolarsystem.fuel_cabinet",
				"Fuel Depot Control Cabinet",
				"Treibstofflager-Schaltschrank",
				"Üzemanyagtelep vezérlőszekrény");
		add(rows, "block.intersolarsystem.crane_cabinet",
				"Assembly Crane Control Cabinet",
				"Montagekran-Schaltschrank",
				"Szerelődaru vezérlőszekrény");
		add(rows, "block.intersolarsystem.fabricator",
				"Fabricator",
				"Fertigungsmaschine",
				"Gyártógép");
		add(rows, "block.intersolarsystem.crane_console",
				"Crane Console",
				"Kranpult",
				"Darukonzol");
		add(rows, "block.intersolarsystem.manual_printer",
				"Manual Printer",
				"Handbuchdrucker",
				"Kézikönyv-nyomtató");
		add(rows, "block.intersolarsystem.crane_hook",
				"Crane Hook",
				"Kranhaken",
				"Darukampó");
		add(rows, "item.intersolarsystem.keel_kit",
				"Mothership Keel Kit",
				"Kielbausatz (Mutterschiff)",
				"Anyahajó-gerinc készlet");
		add(rows, "item.intersolarsystem.hull_kit",
				"Mothership Hull Kit",
				"Rumpfbausatz (Mutterschiff)",
				"Anyahajó-törzs készlet");
		add(rows, "item.intersolarsystem.engine_kit",
				"Mothership Engine Kit",
				"Triebwerksbausatz (Mutterschiff)",
				"Anyahajó-hajtómű készlet");
		add(rows, "item.intersolarsystem.reactor_kit",
				"Mothership Reactor Kit",
				"Reaktorbausatz (Mutterschiff)",
				"Anyahajó-reaktor készlet");
		add(rows, "item.intersolarsystem.life_support_kit",
				"Mothership Life Support Kit",
				"Lebenserhaltungsbausatz (Mutterschiff)",
				"Anyahajó-életfenntartó készlet");
		add(rows, "item.intersolarsystem.hydroponics_kit",
				"Mothership Hydroponics Kit",
				"Hydrokulturbausatz (Mutterschiff)",
				"Anyahajó-farm készlet");
		add(rows, "item.intersolarsystem.laboratory_kit",
				"Mothership Laboratory Kit",
				"Laborbausatz (Mutterschiff)",
				"Anyahajó-labor készlet");
		add(rows, "item.intersolarsystem.quarters_kit",
				"Mothership Quarters Kit",
				"Quartierbausatz (Mutterschiff)",
				"Anyahajó-szállás készlet");
		add(rows, "item.intersolarsystem.hangar_kit",
				"Mothership Hangar Kit",
				"Hangarbausatz (Mutterschiff)",
				"Anyahajó-hangár készlet");
		add(rows, "item.intersolarsystem.bridge_kit",
				"Mothership Bridge Kit",
				"Brückenbausatz (Mutterschiff)",
				"Anyahajó-híd készlet");
		add(rows, "item.intersolarsystem.research_ship_kit",
				"Research Ship Kit",
				"Forschungsschiff-Bausatz",
				"Kutatóhajó-készlet");
		add(rows, "spaceport.intersolarsystem.facility.power",
				"Power Plant",
				"Kraftwerk",
				"Erőmű");
		add(rows, "spaceport.intersolarsystem.facility.control",
				"Launch Control",
				"Startkontrolle",
				"Indítóközpont");
		add(rows, "spaceport.intersolarsystem.facility.fabrication",
				"Fabrication Hall",
				"Fertigungshalle",
				"Gyártócsarnok");
		add(rows, "spaceport.intersolarsystem.facility.fuel",
				"Fuel Depot",
				"Treibstofflager",
				"Üzemanyagtelep");
		add(rows, "spaceport.intersolarsystem.facility.crane",
				"Assembly Crane",
				"Montagekran",
				"Szerelődaru");
		add(rows, "spaceport.intersolarsystem.checklist",
				"%s - restoration checklist",
				"%s - Wiederherstellungsliste",
				"%s - helyreállítási lista");
		add(rows, "spaceport.intersolarsystem.needs_previous",
				"%s must be working first",
				"%s muss zuerst laufen",
				"Előbb a(z) %s működjön");
		add(rows, "spaceport.intersolarsystem.debris",
				"Debris near the cabinet: %s blocks left (rubble and broken concrete within %s blocks)",
				"Schutt am Schrank: noch %s Blöcke (Schutt und Bruchbeton im Umkreis von %s)",
				"Törmelék a szekrény körül: még %s blokk (törmelék és törött beton %s blokkon belül)");
		add(rows, "spaceport.intersolarsystem.material",
				"%s: %s/%s",
				"%s: %s/%s",
				"%s: %s/%s");
		add(rows, "spaceport.intersolarsystem.calibrate_now",
				"Everything is in place: calibrating now",
				"Alles bereit: Kalibrierung beginnt",
				"Minden a helyén: indul a kalibrálás");
		add(rows, "spaceport.intersolarsystem.calibrate_later",
				"Calibration: once everything above is done (right click with an empty hand)",
				"Kalibrierung: wenn alles oben erledigt ist (Rechtsklick mit leerer Hand)",
				"Kalibrálás: ha a fentiek megvannak (jobb klikk üres kézzel)");
		add(rows, "spaceport.intersolarsystem.delivered",
				"%s delivered (%s/%s)",
				"%s geliefert (%s/%s)",
				"%s leadva (%s/%s)");
		add(rows, "spaceport.intersolarsystem.enough",
				"Enough %s",
				"Genug %s",
				"Elég %s");
		add(rows, "spaceport.intersolarsystem.not_needed",
				"%s does not need that",
				"%s braucht das nicht",
				"A(z) %s ezt nem kéri");
		add(rows, "spaceport.intersolarsystem.online",
				"%s is back online!",
				"%s läuft wieder!",
				"A(z) %s újra működik!");
		add(rows, "spaceport.intersolarsystem.calibration_failed",
				"Calibration failed. Try again.",
				"Kalibrierung fehlgeschlagen. Nochmal versuchen.",
				"A kalibrálás nem sikerült. Próbáld újra.");
		add(rows, "spaceport.intersolarsystem.reactor_status",
				"Reactor running: fuel for %s min (uranium fuel rods top it up)",
				"Reaktor läuft: Brennstoff für %s min (Uranbrennstäbe nachlegen)",
				"A reaktor működik: üzemanyag %s percre (urán fűtőelemmel tölthető)");
		add(rows, "spaceport.intersolarsystem.reactor_fault",
				"Reactor fault! The spaceport is down: repair it with a repair kit at the power plant cabinet",
				"Reaktorstörung! Der Raumhafen steht: mit einem Reparaturset am Kraftwerksschrank reparieren",
				"Reaktorhiba! Az űrkikötő leállt: javítókészlettel javítsd az erőmű szekrényénél");
		add(rows, "spaceport.intersolarsystem.reactor_repaired",
				"Reactor repaired",
				"Reaktor repariert",
				"Reaktor megjavítva");
		add(rows, "spaceport.intersolarsystem.reactor_empty",
				"The reactor is out of fuel: the spaceport is down",
				"Der Reaktor hat keinen Brennstoff mehr: der Raumhafen steht",
				"A reaktor kifogyott: az űrkikötő leállt");
		add(rows, "spaceport.intersolarsystem.propellant",
				"Propellant %s/%s (the mothership needs %s; bitumen +2, helium-3 regolith +12)",
				"Treibstoff %s/%s (das Mutterschiff braucht %s; Bitumen +2, Helium-3-Regolith +12)",
				"Hajtóanyag %s/%s (az anyahajónak %s kell; bitumen +2, hélium-3 regolit +12)");
		add(rows, "spaceport.intersolarsystem.working",
				"%s is working",
				"%s läuft",
				"A(z) %s működik");
		add(rows, "spaceport.intersolarsystem.no_power",
				"%s has no power (the power plant is down)",
				"%s hat keinen Strom (das Kraftwerk steht)",
				"A(z) %s nem kap áramot (az erőmű áll)");
		add(rows, "spaceport.intersolarsystem.needs_working",
				"The %s must be restored and powered first",
				"Zuerst %s wiederherstellen und mit Strom versorgen",
				"Előbb a(z) %s legyen helyreállítva és áram alatt");
		add(rows, "spaceport.intersolarsystem.recipe",
				"Fabricating: %s (sneak + right click: next recipe)",
				"Fertigung: %s (Schleichen + Rechtsklick: nächstes Rezept)",
				"Gyártás: %s (guggolás + jobb klikk: következő recept)");
		add(rows, "spaceport.intersolarsystem.made",
				"%s fabricated",
				"%s gefertigt",
				"%s elkészült");
		add(rows, "spaceport.intersolarsystem.crane_building",
				"The crane is building: %s, %s%%",
				"Der Kran baut: %s, %s%%",
				"A daru épít: %s, %s%%");
		add(rows, "spaceport.intersolarsystem.crane_next",
				"Mothership: %s of %s sections built. Next: %s",
				"Mutterschiff: %s von %s Sektionen gebaut. Als Nächstes: %s",
				"Anyahajó: %s/%s szekció kész. Következik: %s");
		add(rows, "spaceport.intersolarsystem.crane_started",
				"The crane starts building: %s",
				"Der Kran beginnt: %s",
				"A daru nekiáll: %s");
		add(rows, "spaceport.intersolarsystem.section_done",
				"Section built: %s (%s/%s)",
				"Sektion fertig: %s (%s/%s)",
				"Szekció kész: %s (%s/%s)");
		add(rows, "spaceport.intersolarsystem.mothership_complete",
				"The mothership is complete. Propellant %s/%s for launch.",
				"Das Mutterschiff ist fertig. Treibstoff %s/%s für den Start.",
				"Az anyahajó kész. Hajtóanyag %s/%s az indításhoz.");
		add(rows, "spaceport.intersolarsystem.printer_paper",
				"The printer needs paper (right click with paper)",
				"Der Drucker braucht Papier (Rechtsklick mit Papier)",
				"A nyomtatónak papír kell (jobb klikk papírral)");
		add(rows, "block.intersolarsystem.mothership_helm",
				"Mothership Helm",
				"Steuerstand des Mutterschiffs",
				"Anyahajó-kormányállás");
		add(rows, "block.intersolarsystem.hangar_lift",
				"Hangar Lift",
				"Hangarlift",
				"Hangárlift");
		add(rows, "spaceport.intersolarsystem.mothership_exists",
				"This world already has a mothership",
				"Diese Welt hat schon ein Mutterschiff",
				"Ebben a világban már van anyahajó");
		add(rows, "mothership.intersolarsystem.aboard",
				"You are aboard the mothership",
				"Du bist an Bord des Mutterschiffs",
				"Az anyahajó fedélzetén vagy");
		add(rows, "mothership.intersolarsystem.place.earth_orbit",
				"Earth orbit",
				"Erdorbit",
				"Föld körüli pálya");
		add(rows, "mothership.intersolarsystem.place.moon_orbit",
				"Lunar orbit",
				"Mondorbit",
				"Hold körüli pálya");
		add(rows, "mothership.intersolarsystem.travelling",
				"Under way to %s, %s min left",
				"Unterwegs nach %s, noch %s min",
				"Úton: %s, még %s perc");
		add(rows, "mothership.intersolarsystem.course",
				"Course: %s (right click to set off)",
				"Kurs: %s (Rechtsklick zum Aufbruch)",
				"Irány: %s (jobb klikk: indulás)");
		add(rows, "mothership.intersolarsystem.here",
				"We are already in %s (sneak + right click: choose a course)",
				"Wir sind schon im %s (Schleichen + Rechtsklick: Kurs wählen)",
				"Már itt vagyunk: %s (guggolás + jobb klikk: irányválasztás)");
		add(rows, "mothership.intersolarsystem.departing",
				"The mothership sets off for %s (%s min)",
				"Das Mutterschiff bricht auf nach %s (%s min)",
				"Az anyahajó elindul: %s (%s perc)");
		add(rows, "mothership.intersolarsystem.arrived",
				"The mothership has arrived: %s",
				"Das Mutterschiff ist angekommen: %s",
				"Az anyahajó megérkezett: %s");
		add(rows, "mothership.intersolarsystem.event.micrometeors",
				"Micrometeors rattle on the hull",
				"Mikrometeoriten prasseln auf den Rumpf",
				"Mikrometeoritok kopognak a törzsön");
		add(rows, "mothership.intersolarsystem.event.solar_flare",
				"Solar flare: stay away from the windows",
				"Sonneneruption: weg von den Fenstern",
				"Napkitörés: maradj távol az ablakoktól");
		add(rows, "mothership.intersolarsystem.event.drift",
				"Course drift corrected",
				"Kursabweichung korrigiert",
				"Pályaeltérés korrigálva");
		add(rows, "mothership.intersolarsystem.event.systems",
				"All systems nominal",
				"Alle Systeme normal",
				"Minden rendszer rendben");
		add(rows, "mothership.intersolarsystem.lift_travelling",
				"The lift is locked while the ship is under way",
				"Der Lift ist während der Fahrt gesperrt",
				"Utazás közben a lift zárva");
		add(rows, "mothership.intersolarsystem.lift_down",
				"The lift takes you down",
				"Der Lift bringt dich hinunter",
				"A lift levisz");
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
