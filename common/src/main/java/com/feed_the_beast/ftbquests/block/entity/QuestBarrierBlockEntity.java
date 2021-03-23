package com.feed_the_beast.ftbquests.block.entity;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.DependencyRequirement;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import me.shedaniel.architectury.extensions.BlockEntityExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.feed_the_beast.ftbquests.block.QuestBarrierBlock.COMPLETED;

/**
 * @author LatvianModder
 */
public class QuestBarrierBlockEntity extends BlockEntity implements TickableBlockEntity, BlockEntityExtension {

	public long object = 0;
	public DependencyRequirement requirement = DependencyRequirement.ALL_COMPLETED;

	public boolean completed = false;

	public QuestBarrierBlockEntity() {
		super(FTBQuestsBlockEntities.BARRIER.get());
	}

	@Override
	public void loadClientData(@NotNull BlockState state, @NotNull CompoundTag tag) {
		object = tag.getLong("object");
		requirement = DependencyRequirement.NAME_MAP.get(tag.getString("requirement"));
	}

	@Override
	public CompoundTag saveClientData(@NotNull CompoundTag tag) {
		tag.putLong("object", object);
		tag.putString("requirement", requirement.id);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		loadClientData(state, tag);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		syncData();
		return super.getUpdatePacket();
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		super.save(tag);
		return saveClientData(tag);
	}

	@Override
	public void tick() {
		if (level != null && level.isClientSide && level.getGameTime() % 5L == 0L) {
			PlayerData data = ClientQuestFile.INSTANCE.self;
			completed = isComplete(data);

			if (completed != getBlockState().getValue(COMPLETED)) {
				level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(COMPLETED, completed));
				setChanged();
			}
		}
	}

	public void updateObject(long id) {
		object = id;
		syncData();
	}

	@Nullable
	public QuestObject getObject(QuestFile file) {
		return file.get(object);
	}

	public boolean isComplete(PlayerData data) {
		QuestObject o = getObject(data.file);
		return (o != null) && (data.getCanEdit() || (requirement.completed ? data.isComplete(o) : data.isStarted(o)));
	}

}
