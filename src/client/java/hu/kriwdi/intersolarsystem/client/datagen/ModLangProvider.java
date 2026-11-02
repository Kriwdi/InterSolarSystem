package hu.kriwdi.intersolarsystem.client.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.minecraft.core.HolderLookup;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.ship.ShipCommands;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipHullClass;
import hu.kriwdi.intersolarsystem.ship.ShipIncidentType;
import hu.kriwdi.intersolarsystem.ship.ShipSubsystem;

public class ModLangProvider extends FabricLanguageProvider {
	/**
	 * All translatable strings of one language. {@code hulls} and {@code subsystems} follow the enum declaration order.
	 */
	private record Texts(
			String scrapSensor,
			String rustedPanel,
			String shipInfo,
			String subsystemLine,
			String shipSet,
			String unknownSubsystem,
			String readOnly,
			String shipHatch,
			String boarded,
			String exited,
			String exitFallback,
			String interiorMissing,
			String[] phases,
			String phase,
			String phaseRemaining,
			String destination,
			String flyStarted,
			String flyNotInside,
			String flyNotLanded,
			String flyOutOfBorder,
			String busy,
			String notConnected,
			String cannotLeave,
			String redocked,
			String countdownTakeoff,
			String countdownFlight,
			String countdownLanding,
			String landed,
			String landedSpawn,
			String flyTooDamaged,
			String[] incidents,
			String incident,
			String incidentExpired,
			String alertLine,
			String incidentLine,
			String repairStation,
			String repairKit,
			String stationNeedsKit,
			String repairIncident,
			String repairSubsystem,
			String repairNothing,
			String[] hulls,
			String[] subsystems
	) {
	}

	private static final Texts ENGLISH = new Texts("Scrap Sensor", "Rusted Panel",
			"Hull: %s", "%s: %s%%", "Set %s to %s%%", "Unknown subsystem: %s",
			"Ship data was saved by a newer version and is read-only",
			"Ship Hatch", "You board the ship", "You leave the ship",
			"The landing site is gone; you were put at the world spawn", "The ship interior is not available",
			new String[] {"Landed", "Taking off", "In flight", "Landing"},
			"Phase: %s", "Time left: %s", "Destination: %s, %s", "Takeoff sequence started. Destination: %s, %s",
			"You must be inside the ship to fly it", "The ship is not ready to take off",
			"That destination is outside the world border", "The ship is away or busy",
			"This hatch is not connected to the ship", "You cannot leave the ship right now",
			"The ship docks at this hatch", "Takeoff in %s", "Arrival in %s", "Landing in %s",
			"The ship has landed at %s, %s",
			"No dry landing site nearby; the ship landed near the world spawn at %s, %s",
			"The engine or the landing gear is too damaged to take off (below 20%)",
			new String[] {"Leak", "Overheating", "Short circuit", "Fire", "Pressure loss"},
			"ALERT: %s in %s! Fix it with a repair kit at the repair station", "The %s failure damaged %s",
			"%s: %s (%s)", "Incident: %s, %s (%s left)",
			"Repair Station", "Repair Kit", "Use a repair kit here. Worst subsystem: %s", "You fixed the %s on %s",
			"%s restored by %s%%", "Nothing needs repair",
			new String[] {"Rescue Capsule", "Research Vessel", "Cargo/Mining Ship", "Scout", "Mothership"},
			new String[] {"Engine", "Fuel & Energy", "Hull Armor", "Life Support", "Navigation & Comms",
					"Stabilizer", "Landing & Launch", "Cargo Hold"});

	private static final Texts GERMAN = new Texts("Schrottsensor", "Rostige Platte",
			"Rumpf: %s", "%s: %s%%", "%s auf %s%% gesetzt", "Unbekanntes Subsystem: %s",
			"Die Schiffsdaten stammen von einer neueren Version und sind schreibgeschützt",
			"Schiffsluke", "Du betrittst das Schiff", "Du verlässt das Schiff",
			"Der Landeplatz existiert nicht mehr; du wurdest zum Weltspawn gesetzt", "Das Schiffsinnere ist nicht verfügbar",
			new String[] {"Gelandet", "Startet", "Im Flug", "Landet"},
			"Phase: %s", "Verbleibende Zeit: %s", "Ziel: %s, %s", "Startsequenz eingeleitet. Ziel: %s, %s",
			"Du musst im Schiff sein, um es zu fliegen", "Das Schiff ist nicht startbereit",
			"Dieses Ziel liegt außerhalb der Weltgrenze", "Das Schiff ist abwesend oder beschäftigt",
			"Diese Luke ist nicht mit dem Schiff verbunden", "Du kannst das Schiff gerade nicht verlassen",
			"Das Schiff dockt an dieser Luke an", "Start in %s", "Ankunft in %s", "Landung in %s",
			"Das Schiff ist bei %s, %s gelandet",
			"Kein trockener Landeplatz in der Nähe; das Schiff landete nahe dem Weltspawn bei %s, %s",
			"Das Triebwerk oder das Landegestell ist zu stark beschädigt für den Start (unter 20 %)",
			new String[] {"Leck", "Überhitzung", "Kurzschluss", "Feuer", "Druckverlust"},
			"ALARM: %s bei %s! Mit einem Reparaturset an der Reparaturstation beheben", "Der Ausfall (%s) hat %s beschädigt",
			"%s: %s (%s)", "Störung: %s, %s (noch %s)",
			"Reparaturstation", "Reparaturset", "Benutze hier ein Reparaturset. Schlechtestes Subsystem: %s",
			"Du hast %s bei %s behoben", "%s um %s%% wiederhergestellt", "Nichts muss repariert werden",
			new String[] {"Rettungskapsel", "Forschungsschiff", "Fracht-/Bergbauschiff", "Aufklärer", "Mutterschiff"},
			new String[] {"Antrieb", "Treibstoff & Energie", "Rumpfpanzerung", "Lebenserhaltung",
					"Navigation & Kommunikation", "Stabilisator", "Landung & Start", "Laderaum"});

	private static final Texts HUNGARIAN = new Texts("Roncsérzékelő", "Rozsdás panel",
			"Hajótest: %s", "%s: %s%%", "%s beállítva: %s%%", "Ismeretlen alrendszer: %s",
			"A hajó adatait egy újabb verzió mentette, ezért csak olvashatók",
			"Hajónyílás", "Beszállsz a hajóba", "Kiszállsz a hajóból",
			"A leszállóhely megszűnt; a világ spawnjára kerültél", "A hajó belső tere nem érhető el",
			new String[] {"Leszállva", "Felszállás", "Repülés", "Leszállás"},
			"Fázis: %s", "Hátralévő idő: %s", "Cél: %s, %s", "A felszállási sorozat elindult. Cél: %s, %s",
			"A hajó belsejében kell lenned a repüléshez", "A hajó nem áll készen a felszállásra",
			"Ez a cél a világhatáron kívül van", "A hajó távol van vagy foglalt",
			"Ez a nyílás nincs a hajóhoz kötve", "Most nem tudsz kiszállni a hajóból",
			"A hajó ehhez a nyíláshoz dokkol", "Felszállás: %s", "Érkezés: %s", "Leszállás: %s",
			"A hajó leszállt itt: %s, %s",
			"Nincs száraz leszállóhely a közelben; a hajó a világ spawnja közelében szállt le: %s, %s",
			"A hajtómű vagy a leszállóegység túl sérült a felszálláshoz (20% alatt)",
			new String[] {"Szivárgás", "Túlmelegedés", "Rövidzárlat", "Tűz", "Nyomásvesztés"},
			"RIASZTÁS: %s itt: %s! Javítsd javítókészlettel a javítóállomáson", "A(z) %s hiba megrongálta ezt: %s",
			"%s: %s (%s)", "Incidens: %s, %s (még %s)",
			"Javítóállomás", "Javítókészlet", "Használj itt javítókészletet. Legrosszabb alrendszer: %s",
			"Kijavítottad: %s itt: %s", "%s helyreállítva: %s%%", "Nincs mit javítani",
			new String[] {"Mentőkapszula", "Kutatóhajó", "Teher-/bányászhajó", "Felderítő", "Anyahajó"},
			new String[] {"Hajtómű", "Üzemanyag és energia", "Burkolat és páncél", "Életfenntartás",
					"Navigáció és kommunikáció", "Stabilizáló egység", "Leszálló- és kilövőegység", "Raktér"});

	private final Texts texts;

	private ModLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
			String languageCode, Texts texts) {
		super(output, languageCode, lookup);
		this.texts = texts;
	}

	static ModLangProvider english(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, "en_us", ENGLISH);
	}

	static ModLangProvider german(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, "de_de", GERMAN);
	}

	static ModLangProvider hungarian(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, "hu_hu", HUNGARIAN);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider holderLookup, TranslationBuilder builder) {
		builder.add(ModItems.SCRAP_SENSOR, texts.scrapSensor());
		builder.add(ModBlocks.RUSTED_PANEL, texts.rustedPanel());
		builder.add("command.intersolarsystem.ship.info", texts.shipInfo());
		builder.add("command.intersolarsystem.ship.subsystem_line", texts.subsystemLine());
		builder.add("command.intersolarsystem.ship.set", texts.shipSet());
		builder.add("command.intersolarsystem.ship.unknown_subsystem", texts.unknownSubsystem());
		builder.add("command.intersolarsystem.ship.read_only", texts.readOnly());
		builder.add(ModBlocks.SHIP_HATCH, texts.shipHatch());
		builder.add("message.intersolarsystem.ship.boarded", texts.boarded());
		builder.add("message.intersolarsystem.ship.exited", texts.exited());
		builder.add("message.intersolarsystem.ship.exit_fallback", texts.exitFallback());
		builder.add("message.intersolarsystem.ship.interior_missing", texts.interiorMissing());
		builder.add("command.intersolarsystem.ship.phase", texts.phase());
		builder.add("command.intersolarsystem.ship.phase_remaining", texts.phaseRemaining());
		builder.add("command.intersolarsystem.ship.destination", texts.destination());
		builder.add("command.intersolarsystem.ship.fly_started", texts.flyStarted());
		builder.add("command.intersolarsystem.ship.fly_not_inside", texts.flyNotInside());
		builder.add("command.intersolarsystem.ship.fly_not_landed", texts.flyNotLanded());
		builder.add("command.intersolarsystem.ship.fly_out_of_border", texts.flyOutOfBorder());
		builder.add("message.intersolarsystem.ship.busy", texts.busy());
		builder.add("message.intersolarsystem.ship.not_connected", texts.notConnected());
		builder.add("message.intersolarsystem.ship.cannot_leave", texts.cannotLeave());
		builder.add("message.intersolarsystem.ship.redocked", texts.redocked());
		builder.add("message.intersolarsystem.ship.countdown.takeoff", texts.countdownTakeoff());
		builder.add("message.intersolarsystem.ship.countdown.flight", texts.countdownFlight());
		builder.add("message.intersolarsystem.ship.countdown.landing", texts.countdownLanding());
		builder.add("message.intersolarsystem.ship.landed", texts.landed());
		builder.add("message.intersolarsystem.ship.landed_spawn", texts.landedSpawn());
		builder.add("command.intersolarsystem.ship.fly_too_damaged", texts.flyTooDamaged());
		builder.add(ModBlocks.REPAIR_STATION, texts.repairStation());
		builder.add(ModItems.REPAIR_KIT, texts.repairKit());
		builder.add("message.intersolarsystem.ship.station_needs_kit", texts.stationNeedsKit());
		builder.add("message.intersolarsystem.ship.repair_incident", texts.repairIncident());
		builder.add("message.intersolarsystem.ship.repair_subsystem", texts.repairSubsystem());
		builder.add("message.intersolarsystem.ship.repair_nothing", texts.repairNothing());
		builder.add("message.intersolarsystem.ship.incident", texts.incident());
		builder.add("message.intersolarsystem.ship.incident_expired", texts.incidentExpired());
		builder.add("message.intersolarsystem.ship.alert_line", texts.alertLine());
		builder.add("command.intersolarsystem.ship.incident_line", texts.incidentLine());
		for (ShipIncidentType type : ShipIncidentType.values()) {
			builder.add(ShipCommands.incidentKey(type), texts.incidents()[type.ordinal()]);
		}
		for (ShipFlightPhase phase : ShipFlightPhase.values()) {
			builder.add(ShipCommands.phaseKey(phase), texts.phases()[phase.ordinal()]);
		}
		for (ShipHullClass hull : ShipHullClass.values()) {
			builder.add(ShipCommands.hullKey(hull), texts.hulls()[hull.ordinal()]);
		}
		for (ShipSubsystem subsystem : ShipSubsystem.values()) {
			builder.add(ShipCommands.subsystemKey(subsystem), texts.subsystems()[subsystem.ordinal()]);
		}
	}
}
