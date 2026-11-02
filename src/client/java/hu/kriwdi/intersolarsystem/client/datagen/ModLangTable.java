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
