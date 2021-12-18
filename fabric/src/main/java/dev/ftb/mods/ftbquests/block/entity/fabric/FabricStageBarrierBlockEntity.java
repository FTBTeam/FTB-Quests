package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class FabricStageBarrierBlockEntity extends StageBarrierBlockEntity implements BlockEntityClientSerializable {

	public FabricStageBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(blockPos, blockState);
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		readBarrier(tag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return writeBarrier(tag);
	}

	@Override
	public void syncData() {
		sync();
	}
}
