package hu.kriwdi.intersolarsystem.content;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.space.Worlds;

/**
 * The flight computer of NASA's Dragonfly rotorcraft (launched 2028, on Titan from 2034), still in its lander at
 * Selk crater. Dead until a repair kit brings it back; then it hops: each use flies whoever stands at it to the next
 * of Titan's sites (thick air and low gravity make a rotorcraft easy there), and they come down slowly by its
 * parachute line.
 */
public class DragonflyBlock extends ShipFacingBlock {
	public static final BooleanProperty REPAIRED = BlockStateProperties.POWERED;

	public DragonflyBlock(Properties properties, double[]... boxes) {
		super(properties, boxes);
		registerDefaultState(defaultBlockState().setValue(REPAIRED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(REPAIRED);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.REPAIR_KIT) || state.getValue(REPAIRED)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (player instanceof ServerPlayer sp) {
			stack.shrink(1);
			level.setBlock(pos, state.setValue(REPAIRED, true), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.4F);
			sp.sendSystemMessage(Component.translatable("titan.intersolarsystem.dragonfly_repaired")
					.withStyle(ChatFormatting.GREEN));
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) {
			return InteractionResult.SUCCESS;
		}
		if (!state.getValue(REPAIRED)) {
			sp.sendOverlayMessage(Component.translatable("titan.intersolarsystem.dragonfly_dead"));
			return InteractionResult.SUCCESS_SERVER;
		}
		// The next site round the list from the nearest one, so every use goes somewhere new.
		List<Worlds.Site> sites = Worlds.TITAN.sites();
		int nearest = 0;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < sites.size(); i++) {
			double d = player.position().distanceToSqr(Worlds.TITAN.landX() + sites.get(i).dx(), player.getY(),
					Worlds.TITAN.landZ() + sites.get(i).dz());
			if (d < best) {
				best = d;
				nearest = i;
			}
		}
		Worlds.Site to = sites.get((nearest + 1) % sites.size());
		BlockPos ground = MoonSites.surface(sl, Worlds.TITAN.landX() + to.dx() + 6, Worlds.TITAN.landZ() + to.dz() + 6);
		sl.playSound(null, pos, SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 0.7F, 1.6F);
		sp.teleport(new TeleportTransition(sl, Vec3.atBottomCenterOf(ground.above(24)), Vec3.ZERO, sp.getYRot(), 20.0F,
				TeleportTransition.DO_NOTHING));
		sp.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, true, false, true));
		sp.sendSystemMessage(Component.translatable("titan.intersolarsystem.dragonfly_hop",
				Component.translatable("site.intersolarsystem." + to.template().replace('/', '.'))));
		return InteractionResult.SUCCESS_SERVER;
	}
}
