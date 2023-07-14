package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class StageBarrierBlockEntity extends BlockEntity implements BarrierBlockEntity {
	public String stage = "";

	public StageBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(FTBQuestsBlockEntities.STAGE_BARRIER.get(), blockPos, blockState);
	}

	public void readBarrier(CompoundTag tag) {
		stage = tag.getString("Stage");
	}

	public CompoundTag writeBarrier(CompoundTag tag) {
		tag.putString("Stage", stage);
		return tag;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		readBarrier(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		writeBarrier(tag);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level != null && !level.isClientSide) {
			((ServerLevel) level).getChunkSource().blockChanged(getBlockPos());
		}
	}

	@Override
	public void update(String s) {
		stage = s;
		setChanged();
	}

	@Override
	public boolean isOpen(Player player) {
		return !stage.isEmpty() && StageHelper.INSTANCE.getProvider().has(player, stage);
	}
}
