package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Pure rules for hidden faults, diagnostics and the takeoff risk. All numbers are here so balancing is one-stop.
 * Every roll is deterministic from a seed, so reloading the world cannot re-roll an outcome.
 */
public final class ShipFaultRules {
	/** Below this condition a landing can leave a hidden fault behind. */
	public static final int FAULT_CONDITION = 70;
	/** Divisor of the fault chance: (70 - condition) / 140, so 50% at 0%. */
	public static final double FAULT_DIVISOR = 140.0;
	/** Ticks between two diagnostic scans. */
	public static final int SCAN_COOLDOWN_TICKS = 1200;
	/** Hidden faults on these subsystems can wreck a takeoff. */
	public static final Set<ShipSubsystem> LAUNCH_CRITICAL = EnumSet.of(ShipSubsystem.ENGINE, ShipSubsystem.FUEL_ENERGY,
			ShipSubsystem.LANDING_LAUNCH);
	public static final double RISK_PER_FAULT = 0.15;
	/** Below this condition a launch-critical subsystem adds risk on its own. */
	public static final int RISK_CONDITION = 40;
	public static final double MAX_RISK = 0.6;
	public static final double TEST_RISK_FACTOR = 0.5;

	private static final long STEP = 2 * 0x9E3779B97F4A7C15L;
	// Separate streams so a landing roll, a scan and a launch roll with the same seed never share numbers.
	private static final long LANDING_SALT = 0x5DEECE66DL;
	private static final long SCAN_SALT = 0x2545F4914F6CDD1DL;
	private static final long LAUNCH_SALT = 0x9E3779B97F4A7C15L ^ 0xBF58476D1CE4E5B9L;

	private ShipFaultRules() {
	}

	/** Chance that a landing leaves a new hidden fault in a subsystem at this condition. */
	public static double faultChance(int condition) {
		int clamped = ShipState.clamp(condition);
		if (clamped >= FAULT_CONDITION) {
			return 0.0;
		}
		return (FAULT_CONDITION - clamped) / FAULT_DIVISOR;
	}

	/** Subsystems that get a new hidden fault at a landing; subsystems that already have one are skipped. */
	public static Set<ShipSubsystem> rollLandingFaults(long seed, Map<ShipSubsystem, Integer> conditions,
			Set<ShipSubsystem> existing) {
		SplittableRandom random = new SplittableRandom(seed ^ LANDING_SALT);
		Set<ShipSubsystem> hit = EnumSet.noneOf(ShipSubsystem.class);
		for (ShipSubsystem s : ShipSubsystem.values()) {
			// Always draw, so one subsystem's state never shifts the numbers of the next.
			double draw = random.nextDouble();
			if (!existing.contains(s) && draw < faultChance(conditions.getOrDefault(s, ShipState.MAX_CONDITION))) {
				hit.add(s);
			}
		}
		return hit;
	}

	/** Chance that one scan finds a given hidden fault: 50% plus half the navigation/comms condition. */
	public static double scanChance(int navigationCondition) {
		return Math.min(1.0, 0.5 + ShipState.clamp(navigationCondition) / 200.0);
	}

	/** The faults after a scan: each unrevealed one is found with {@link #scanChance}. */
	public static List<ShipLatentFault> scan(long seed, int scanIndex, int navigationCondition,
			List<ShipLatentFault> faults) {
		SplittableRandom random = new SplittableRandom((seed ^ SCAN_SALT) + scanIndex * STEP);
		double chance = scanChance(navigationCondition);
		List<ShipLatentFault> result = new ArrayList<>();
		for (ShipLatentFault fault : faults) {
			double draw = random.nextDouble();
			result.add(!fault.revealed() && draw < chance ? fault.reveal() : fault);
		}
		return List.copyOf(result);
	}

	/**
	 * Chance that a takeoff ends in disaster. Launch-critical subsystems add risk for a hidden fault and for a condition
	 * below {@link #RISK_CONDITION}. With {@code knownOnly} unrevealed faults are left out: that is the estimate the
	 * diagnostic console can give.
	 */
	public static double launchRisk(Map<ShipSubsystem, Integer> conditions, List<ShipLatentFault> faults,
			boolean knownOnly, boolean test) {
		double risk = 0.0;
		for (ShipSubsystem s : LAUNCH_CRITICAL) {
			int condition = ShipState.clamp(conditions.getOrDefault(s, ShipState.MAX_CONDITION));
			if (condition < RISK_CONDITION) {
				risk += (RISK_CONDITION - condition) / 200.0;
			}
			boolean faulty = faults.stream().anyMatch(f -> f.subsystem() == s && (f.revealed() || !knownOnly));
			if (faulty) {
				risk += RISK_PER_FAULT;
			}
		}
		risk = Math.min(MAX_RISK, risk);
		return test ? risk * TEST_RISK_FACTOR : risk;
	}

	/** Whether this takeoff is a disaster. */
	public static boolean isDisaster(long flightSeed, double risk) {
		return risk > 0.0 && new SplittableRandom(flightSeed ^ LAUNCH_SALT).nextDouble() < risk;
	}

	/** Damage of a failed takeoff, per subsystem (positive amounts to subtract). */
	public static Map<ShipSubsystem, Integer> disasterDamage() {
		Map<ShipSubsystem, Integer> damage = new java.util.EnumMap<>(ShipSubsystem.class);
		for (ShipSubsystem s : ShipSubsystem.values()) {
			damage.put(s, 10);
		}
		damage.put(ShipSubsystem.ENGINE, 30);
		damage.put(ShipSubsystem.LANDING_LAUNCH, 30);
		damage.put(ShipSubsystem.HULL_ARMOR, 20);
		return damage;
	}

	/** Risk as a whole percentage, rounded up so a small risk never shows as 0%. */
	public static int percent(double risk) {
		return (int) Math.ceil(Math.max(0.0, risk) * 100.0 - 1e-9);
	}
}
