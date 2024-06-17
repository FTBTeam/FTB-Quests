package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbquests.block.entity.DetectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class DetectorBlock extends BaseEntityBlock {
	private static final MapCodec<DetectorBlock> CODEC = simpleCodec(DetectorBlock::new);
	public static final Properties PROPS = Properties.of().strength(0.3F);

	public DetectorBlock(BlockBehaviour.Properties props) {
		super(props);

		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new DetectorBlockEntity(blockPos, blockState);
	}

	@Override
	protected MapCodec<DetectorBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide()) {
			boolean wasPowered = blockState.getValue(BlockStateProperties.POWERED);

			if (wasPowered != level.hasNeighborSignal(blockPos)) {
				level.setBlock(blockPos, blockState.setValue(BlockStateProperties.POWERED, !wasPowered), Block.UPDATE_CLIENTS);
				if (!wasPowered) {
					if (level.getBlockEntity(blockPos) instanceof DetectorBlockEntity dbe) {
						dbe.onPowered(level, blockPos);
					}
				}
			}
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (!level.isClientSide() && stack.has(DataComponents.CUSTOM_NAME) && level.getBlockEntity(pos) instanceof DetectorBlockEntity dbe) {
			dbe.update(stack.getHoverName().getString());
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.POWERED);
	}
}
