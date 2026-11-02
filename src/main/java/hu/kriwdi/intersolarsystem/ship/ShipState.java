package hu.kriwdi.intersolarsystem.ship;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.saveddata.SavedData;

import hu.kriwdi.intersolarsystem.core.ModIds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent state of the world's single ship. Server-owned; clients never store or modify it.
 */
public class ShipState extends SavedData {
	public static final int CURRENT_FORMAT_VERSION = 5;
	public static final int MIN_CONDITION = 0;
	public static final int MAX_CONDITION = 100;

	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	/** The ship's authoritative position: the hatch it last landed at. */
	public record LandingSite(String dimension, int x, int y, int z) {
	}

	public record Destination(int x, int z) {
	}

	// The landing site and interior flag use lenientOptionalFieldOf: a wrong-typed value is dropped instead of failing
	// the whole record, because a failed load would reset the hull and subsystems to defaults.
	// Raw field types on purpose: sanitizing happens in fromRaw, so out-of-range values never abort the load.
	private static final Codec<ShipState> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			// Optional<> (not optionalFieldOf with a default) so these fields are always written, even at their defaults.
			Codec.INT.optionalFieldOf("format_version").forGetter(s -> Optional.of(s.formatVersion())),
			Codec.STRING.optionalFieldOf("hull_class").forGetter(s -> Optional.of(s.hullClass().id())),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("subsystems", Map.of())
					.forGetter(ShipState::rawSubsystems),
			Codec.STRING.lenientOptionalFieldOf("landing_dimension").forGetter(s -> s.landingSite.map(LandingSite::dimension)),
			Codec.INT.lenientOptionalFieldOf("landing_x").forGetter(s -> s.landingSite.map(LandingSite::x)),
			Codec.INT.lenientOptionalFieldOf("landing_y").forGetter(s -> s.landingSite.map(LandingSite::y)),
			Codec.INT.lenientOptionalFieldOf("landing_z").forGetter(s -> s.landingSite.map(LandingSite::z)),
			Codec.BOOL.lenientOptionalFieldOf("interior_built").forGetter(s -> Optional.of(s.interiorBuilt)),
			Codec.STRING.lenientOptionalFieldOf("phase").forGetter(s -> Optional.of(s.phase.id())),
			Codec.LONG.lenientOptionalFieldOf("phase_started_tick").forGetter(s -> Optional.of(s.phaseStartedTick)),
			Codec.INT.lenientOptionalFieldOf("destination_x").forGetter(s -> s.destination.map(Destination::x)),
			Codec.INT.lenientOptionalFieldOf("destination_z").forGetter(s -> s.destination.map(Destination::z)),
			ShipHazards.CODEC.lenientOptionalFieldOf("hazards").forGetter(s -> Optional.of(s.hazards)),
			Codec.STRING.lenientOptionalFieldOf("flight_kind").forGetter(s -> Optional.of(s.flightKind.id())),
			ShipDiagnostics.CODEC.lenientOptionalFieldOf("diagnostics").forGetter(s -> Optional.of(s.diagnostics))
	).apply(instance, ShipState::fromRaw));

	/** Decodes through the migrations first, so older saves reach {@link #RAW_CODEC} in the current format. */
	private static final Decoder<ShipState> MIGRATING_DECODER = new Decoder<>() {
		@Override
		public <T> DataResult<Pair<ShipState, T>> decode(DynamicOps<T> ops, T input) {
			Dynamic<T> migrated = ShipStateMigrations.migrate(new Dynamic<>(ops, input));
			return RAW_CODEC.decode(ops, migrated.getValue());
		}
	};

	public static final Codec<ShipState> CODEC = Codec.of(RAW_CODEC, MIGRATING_DECODER);

	// Set when the save was written by a newer mod version: edits are refused so the newer data is never overwritten.
	private boolean readOnly;
	private Optional<LandingSite> landingSite = Optional.empty();
	private boolean interiorBuilt;
	private ShipFlightPhase phase = ShipFlightPhase.LANDED;
	private long phaseStartedTick;
	private Optional<Destination> destination = Optional.empty();
	private ShipHazards hazards = ShipHazards.NONE;
	private ShipFlightKind flightKind = ShipFlightKind.NORMAL;
	private ShipDiagnostics diagnostics = ShipDiagnostics.NONE;
	private ShipHullClass hullClass = ShipHullClass.RESCUE_CAPSULE;
	private final Map<ShipSubsystem, Integer> conditions = new EnumMap<>(ShipSubsystem.class);

	public ShipState() {
		for (ShipSubsystem s : ShipSubsystem.values()) {
			conditions.put(s, MAX_CONDITION);
		}
	}

	public static int clamp(int value) {
		return Math.max(MIN_CONDITION, Math.min(MAX_CONDITION, value));
	}

	private static ShipState fromRaw(Optional<Integer> rawVersion, Optional<String> rawHullId,
			Map<String, Integer> rawSubsystems, Optional<String> rawDimension, Optional<Integer> rawX,
			Optional<Integer> rawY, Optional<Integer> rawZ, Optional<Boolean> rawInteriorBuilt,
			Optional<String> rawPhase, Optional<Long> rawPhaseStartedTick, Optional<Integer> rawDestX,
			Optional<Integer> rawDestZ, Optional<ShipHazards> rawHazards, Optional<String> rawFlightKind,
			Optional<ShipDiagnostics> rawDiagnostics) {
		int version = rawVersion.orElse(CURRENT_FORMAT_VERSION);
		String hullId = rawHullId.orElse(ShipHullClass.RESCUE_CAPSULE.id());
		ShipState state = new ShipState();
		if (version > CURRENT_FORMAT_VERSION) {
			LOGGER.warn("Ship state has format version {} but this mod only knows {}; using defaults",
					version, CURRENT_FORMAT_VERSION);
			state.readOnly = true;
			return state;
		}
		ShipHullClass.fromId(hullId).ifPresentOrElse(c -> state.hullClass = c,
				() -> LOGGER.warn("Unknown ship hull class '{}', using {}", hullId, state.hullClass.id()));
		rawSubsystems.forEach((key, value) -> ShipSubsystem.fromId(key).ifPresentOrElse(
				s -> state.conditions.put(s, clamp(value)),
				() -> LOGGER.warn("Ignoring unknown ship subsystem '{}'", key)));
		if (rawDimension.isPresent() && rawX.isPresent() && rawY.isPresent() && rawZ.isPresent()) {
			state.landingSite = Optional.of(new LandingSite(rawDimension.get(), rawX.get(), rawY.get(), rawZ.get()));
		} else if (rawDimension.isPresent() || rawX.isPresent() || rawY.isPresent() || rawZ.isPresent()) {
			LOGGER.warn("Ignoring incomplete ship landing site");
		}
		state.interiorBuilt = rawInteriorBuilt.orElse(false);
		state.phase = rawPhase.flatMap(ShipFlightPhase::fromId).orElseGet(() -> {
			rawPhase.ifPresent(id -> LOGGER.warn("Unknown ship flight phase '{}', using landed", id));
			return ShipFlightPhase.LANDED;
		});
		state.phaseStartedTick = rawPhaseStartedTick.orElse(0L);
		if (rawDestX.isPresent() && rawDestZ.isPresent()) {
			state.destination = Optional.of(new Destination(rawDestX.get(), rawDestZ.get()));
		} else if (rawDestX.isPresent() || rawDestZ.isPresent()) {
			LOGGER.warn("Ignoring incomplete ship destination");
		}
		state.hazards = rawHazards.orElse(ShipHazards.NONE);
		state.flightKind = rawFlightKind.flatMap(ShipFlightKind::fromId).orElseGet(() -> {
			rawFlightKind.ifPresent(id -> LOGGER.warn("Unknown ship flight kind '{}', using normal", id));
			return ShipFlightKind.NORMAL;
		});
		state.diagnostics = rawDiagnostics.orElse(ShipDiagnostics.NONE);
		return state;
	}

	private Map<String, Integer> rawSubsystems() {
		Map<String, Integer> raw = new LinkedHashMap<>();
		conditions.forEach((s, v) -> raw.put(s.id(), v));
		return raw;
	}

	public int formatVersion() {
		return CURRENT_FORMAT_VERSION;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public ShipFlightPhase phase() {
		return phase;
	}

	public long phaseStartedTick() {
		return phaseStartedTick;
	}

	public ShipHazards hazards() {
		return hazards;
	}

	public void setRollsDone(int rollsDone) {
		if (!readOnly && hazards.rollsDone() != rollsDone) {
			hazards = new ShipHazards(hazards.seed(), rollsDone, hazards.incidents());
			setDirty();
		}
	}

	/** Adds an incident unless the save is read-only, the subsystem already has one, or the limit is reached. */
	public boolean addIncident(ShipIncident incident) {
		if (readOnly || hazards.incidents().size() >= ShipIncidentRules.MAX_ACTIVE
				|| hazards.incidents().stream().anyMatch(i -> i.subsystem() == incident.subsystem())) {
			return false;
		}
		java.util.List<ShipIncident> next = new java.util.ArrayList<>(hazards.incidents());
		next.add(incident);
		hazards = new ShipHazards(hazards.seed(), hazards.rollsDone(), next);
		setDirty();
		return true;
	}

	/** Fixes an incident; a hidden fault behind it on the same subsystem is fixed with it. */
	public boolean resolveIncident(ShipIncident incident) {
		if (readOnly || !hazards.incidents().contains(incident)) {
			return false;
		}
		java.util.List<ShipIncident> next = new java.util.ArrayList<>(hazards.incidents());
		next.remove(incident);
		hazards = new ShipHazards(hazards.seed(), hazards.rollsDone(), next);
		removeFault(incident.subsystem());
		setDirty();
		return true;
	}

	public ShipFlightKind flightKind() {
		return flightKind;
	}

	public ShipDiagnostics diagnostics() {
		return diagnostics;
	}

	/** Adds hidden faults (unrevealed) to subsystems that have none yet. */
	public void addFaults(java.util.Set<ShipSubsystem> subsystems) {
		if (readOnly || subsystems.isEmpty()) {
			return;
		}
		java.util.List<ShipLatentFault> next = new java.util.ArrayList<>(diagnostics.faults());
		for (ShipSubsystem s : ShipSubsystem.values()) {
			if (subsystems.contains(s) && !diagnostics.hasFault(s)) {
				next.add(new ShipLatentFault(s, false));
			}
		}
		diagnostics = diagnostics.withFaults(next);
		setDirty();
	}

	public boolean removeFault(ShipSubsystem subsystem) {
		if (readOnly || !diagnostics.hasFault(subsystem)) {
			return false;
		}
		diagnostics = diagnostics.withFaults(diagnostics.faults().stream().filter(f -> f.subsystem() != subsystem).toList());
		setDirty();
		return true;
	}

	/** Marks every hidden fault as known (test hop, failed takeoff); returns the ones that were new to the crew. */
	public java.util.List<ShipLatentFault> revealAllFaults() {
		if (readOnly) {
			return java.util.List.of();
		}
		java.util.List<ShipLatentFault> fresh = diagnostics.faults().stream().filter(f -> !f.revealed()).toList();
		if (!fresh.isEmpty()) {
			diagnostics = diagnostics.withFaults(diagnostics.faults().stream().map(ShipLatentFault::reveal).toList());
			setDirty();
		}
		return fresh;
	}

	/** Stores the result of a diagnostic scan and starts its cooldown. */
	public void recordScan(java.util.List<ShipLatentFault> faultsAfterScan, long nowTick) {
		if (readOnly) {
			return;
		}
		diagnostics = new ShipDiagnostics(faultsAfterScan, diagnostics.scansDone() + 1, nowTick);
		setDirty();
	}

	/**
	 * A takeoff that ended in disaster: the ship never left, so it is back to landed at the same site, with no
	 * destination; the caller applies the damage.
	 */
	public void abortTakeoff(long nowTick) {
		if (readOnly || phase != ShipFlightPhase.TAKEOFF) {
			return;
		}
		phase = ShipFlightPhase.LANDED;
		phaseStartedTick = nowTick;
		destination = Optional.empty();
		flightKind = ShipFlightKind.NORMAL;
		setDirty();
	}

	/** Removes incidents whose deadline has passed, damaging their subsystems; returns the expired ones. */
	public java.util.List<ShipIncident> expireIncidents(long nowTick) {
		if (readOnly) {
			return java.util.List.of();
		}
		java.util.List<ShipIncident> expired = hazards.incidents().stream().filter(i -> i.deadlineTick() <= nowTick).toList();
		if (expired.isEmpty()) {
			return expired;
		}
		java.util.List<ShipIncident> remaining = hazards.incidents().stream().filter(i -> i.deadlineTick() > nowTick).toList();
		hazards = new ShipHazards(hazards.seed(), hazards.rollsDone(), remaining);
		expired.forEach(i -> setCondition(i.subsystem(), condition(i.subsystem()) - ShipIncidentRules.EXPIRY_DAMAGE));
		setDirty();
		return expired;
	}

	/** A deadline further in the future than any incident can have (corrupted or restored save) is pulled in. */
	public boolean clampIncidentDeadlines(long nowTick) {
		long latest = nowTick + ShipIncidentRules.INCIDENT_DEADLINE_TICKS;
		if (readOnly || hazards.incidents().stream().noneMatch(i -> i.deadlineTick() > latest)) {
			return false;
		}
		java.util.List<ShipIncident> next = hazards.incidents().stream()
				.map(i -> i.deadlineTick() > latest ? new ShipIncident(i.subsystem(), i.type(), latest) : i).toList();
		hazards = new ShipHazards(hazards.seed(), hazards.rollsDone(), next);
		setDirty();
		return true;
	}

	public Optional<Destination> destination() {
		return destination;
	}

	/** Starts a flight. Only possible while landed and when the save is writable. */
	public boolean beginTakeoff(int x, int z, long nowTick, long seed) {
		return beginTakeoff(x, z, nowTick, seed, ShipFlightKind.NORMAL);
	}

	/** Starts a flight of the given kind. A test hop's destination is where the ship already stands. */
	public boolean beginTakeoff(int x, int z, long nowTick, long seed, ShipFlightKind kind) {
		if (readOnly || phase != ShipFlightPhase.LANDED) {
			return false;
		}
		flightKind = kind;
		phase = ShipFlightPhase.TAKEOFF;
		phaseStartedTick = nowTick;
		destination = Optional.of(new Destination(x, z));
		hazards = new ShipHazards(seed, 0, hazards.incidents());
		setDirty();
		return true;
	}

	/**
	 * A phase that started "in the future" (restored backup, corrupted save) would never run out. Rebases the start to
	 * {@code nowTick} so the phase simply runs its normal length from now. Returns whether anything changed.
	 */
	public boolean rebasePhaseStartIfInFuture(long nowTick) {
		if (readOnly || phase == ShipFlightPhase.LANDED || phaseStartedTick <= nowTick) {
			return false;
		}
		phaseStartedTick = nowTick;
		setDirty();
		return true;
	}

	/** Moves to the next flight phase; arriving at LANDED clears the destination. */
	public void advancePhase(long nowTick) {
		if (readOnly) {
			return;
		}
		ShipFlightTimeline.next(phase).ifPresent(next -> {
			phase = next;
			phaseStartedTick = nowTick;
			if (next == ShipFlightPhase.LANDED) {
				destination = Optional.empty();
				flightKind = ShipFlightKind.NORMAL;
			}
			setDirty();
		});
	}

	public Optional<LandingSite> landingSite() {
		return landingSite;
	}

	public void setLandingSite(LandingSite site) {
		Optional<LandingSite> next = Optional.of(site);
		if (!readOnly && !landingSite.equals(next)) {
			landingSite = next;
			setDirty();
		}
	}

	public boolean interiorBuilt() {
		return interiorBuilt;
	}

	public void setInteriorBuilt(boolean built) {
		if (!readOnly && interiorBuilt != built) {
			interiorBuilt = built;
			setDirty();
		}
	}

	public ShipHullClass hullClass() {
		return hullClass;
	}

	public void setHullClass(ShipHullClass hullClass) {
		if (!readOnly && this.hullClass != hullClass) {
			this.hullClass = hullClass;
			setDirty();
		}
	}

	public int condition(ShipSubsystem subsystem) {
		return conditions.get(subsystem);
	}

	/** Wears subsystems out by the given positive amounts (clamped at 0). */
	public void applyWear(java.util.Map<ShipSubsystem, Integer> amounts) {
		amounts.forEach((subsystem, amount) -> setCondition(subsystem, condition(subsystem) - amount));
	}

	public void setCondition(ShipSubsystem subsystem, int value) {
		if (readOnly) {
			return;
		}
		int clamped = clamp(value);
		if (conditions.put(subsystem, clamped) != clamped) {
			setDirty();
		}
	}
}
