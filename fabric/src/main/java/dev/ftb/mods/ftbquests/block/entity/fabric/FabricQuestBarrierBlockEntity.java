package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class FabricQuestBarrierBlockEntity extends QuestBarrierBlockEntity implements BlockEntityClientSerializable {

	public FabricQuestBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
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
