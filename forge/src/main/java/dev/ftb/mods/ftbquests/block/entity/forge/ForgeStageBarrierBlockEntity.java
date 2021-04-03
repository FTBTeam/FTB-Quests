package dev.ftb.mods.ftbquests.block.entity.forge;

import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ForgeStageBarrierBlockEntity extends StageBarrierBlockEntity {
	@Override
	public CompoundTag getUpdateTag() {
		return writeBarrier(super.getUpdateTag());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundTag tag) {
		readBarrier(tag);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(worldPosition, 0, writeBarrier(new CompoundTag()));
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		readBarrier(pkt.getTag());
	}

	@Override
	public void syncData() {
		((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}
}
