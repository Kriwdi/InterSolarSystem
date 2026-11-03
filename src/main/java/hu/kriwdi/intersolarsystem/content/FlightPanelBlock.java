package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The pilot's switch panel: four toggle switches that must all be on before the throttle can launch the ship (main
 * power, fuel pumps, avionics, life support). The switch is chosen by where the panel is clicked, left to right as
 * the pilot sees it.
 */
public class FlightPanelBlock extends ShipFacingBlock {
	public static final BooleanProperty POWER = BooleanProperty.create("power");
	public static final BooleanProperty FUEL = BooleanProperty.create("fuel");
	public static final BooleanProperty AVIONICS = BooleanProperty.create("avionics");
	public static final BooleanProperty LIFE = BooleanProperty.create("life");
	public static final BooleanProperty[] SWITCHES = {POWER, FUEL, AVIONICS, LIFE};
	public static final String[] NAMES = {"power", "fuel", "avionics", "life"};

	public FlightPanelBlock(Properties properties) {
		// Pedestal and the sloping switch desk.
		super(properties, new double[] {2, 0, 3, 14, 9, 14}, new double[] {0, 9, 0, 16, 13, 14});
		registerDefaultState(defaultBlockState().setValue(POWER, false).setValue(FUEL, false).setValue(AVIONICS, false)
				.setValue(LIFE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POWER, FUEL, AVIONICS, LIFE);
	}

	public static boolean allOn(BlockState state) {
		for (BooleanProperty p : SWITCHES) {
			if (!state.getValue(p)) {
				return false;
			}
		}
		return true;
	}

	/** Which switch (0..3, left to right for someone in front of the panel) a hit is closest to. */
	static int switchAt(BlockState state, BlockPos pos, Vec3 hit) {
		Direction looking = state.getValue(FACING).getOpposite();
		Direction right = looking.getClockWise();
		double offset = (hit.x - pos.getX() - 0.5) * right.getStepX() + (hit.z - pos.getZ() - 0.5) * right.getStepZ();
		return Math.max(0, Math.min(3, (int) Math.floor((offset + 0.5) * 4)));
	}

	public static BlockState toggle(BlockState state, int index) {
		return state.setValue(SWITCHES[index], !state.getValue(SWITCHES[index]));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level instanceof ServerLevel) || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		int index = switchAt(state, pos, hit.getLocation());
		BlockState next = toggle(state, index);
		level.setBlock(pos, next, Block.UPDATE_ALL);
		boolean on = next.getValue(SWITCHES[index]);
		level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, on ? 0.9F : 0.6F);
		serverPlayer.sendOverlayMessage(Component.translatable("message.intersolarsystem.switch." + NAMES[index],
				Component.translatable(on ? "message.intersolarsystem.switch.on" : "message.intersolarsystem.switch.off")));
		return InteractionResult.SUCCESS_SERVER;
	}
}
