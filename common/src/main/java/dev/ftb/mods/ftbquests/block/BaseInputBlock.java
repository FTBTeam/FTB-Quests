package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.BaseInputBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BaseInputBlock extends BaseEntityBlock {
	public BaseInputBlock() {
		super(BlockBehaviour.Properties.of().strength(0.3F));
	}

	@Override
	public abstract BaseInputBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState);

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (entity instanceof ServerPlayer) {
			BlockEntity blockEntity = level.getBlockEntity(pos);

			if (blockEntity instanceof BaseInputBlockEntity) {
				((BaseInputBlockEntity) blockEntity).selectTask((ServerPlayer) entity);
			}
		}
	}
}
