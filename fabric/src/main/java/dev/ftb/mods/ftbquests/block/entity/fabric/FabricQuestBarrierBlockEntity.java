package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.CompoundTag;

public class FabricQuestBarrierBlockEntity extends QuestBarrierBlockEntity implements BlockEntityClientSerializable {
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
