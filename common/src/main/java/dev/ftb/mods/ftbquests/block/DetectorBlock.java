package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.DetectorBlockEntity;
import net.minecraft.core.BlockPos;
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
	public DetectorBlock() {
		super(BlockBehaviour.Properties.of().strength(0.3F));
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new DetectorBlockEntity(blockPos, blockState);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			boolean bl2 = blockState.getValue(BlockStateProperties.POWERED);

			if (bl2 != level.hasNeighborSignal(blockPos)) {
				level.setBlock(blockPos, blockState.setValue(BlockStateProperties.POWERED, !bl2), 2);

				if (!bl2) {
					BlockEntity blockEntity = level.getBlockEntity(blockPos);

					if (blockEntity instanceof DetectorBlockEntity) {
						((DetectorBlockEntity) blockEntity).powered(level, blockPos);
					}
				}
			}
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (!level.isClientSide() && stack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);

			if (blockEntity instanceof DetectorBlockEntity) {
				((DetectorBlockEntity) blockEntity).update(stack.getHoverName().getString());
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.POWERED);
	}
}
