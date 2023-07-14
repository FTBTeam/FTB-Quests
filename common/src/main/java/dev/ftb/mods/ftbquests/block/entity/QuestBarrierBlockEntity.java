package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
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
public class QuestBarrierBlockEntity extends BlockEntity implements BarrierBlockEntity {
	public long object = 0L;

	public QuestBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(FTBQuestsBlockEntities.BARRIER.get(), blockPos, blockState);
	}

	public void readBarrier(CompoundTag tag) {
		object = QuestObjectBase.parseCodeString(tag.getString("Object"));

		if (object == 0L) {
			object = tag.getLong("object");
		}
	}

	public CompoundTag writeBarrier(CompoundTag tag) {
		tag.putString("Object", QuestObjectBase.getCodeString(object));
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
		object = ServerQuestFile.INSTANCE.getID(s);
		setChanged();
	}

	@Override
	public boolean isOpen(Player player) {
		TeamData data = FTBQuests.PROXY.getQuestFile(player.level().isClientSide()).getData(player);
		QuestObject o = data.file.get(object);
		return o != null && data.isCompleted(o);
	}
}
