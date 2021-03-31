package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.COMPLETED;

/**
 * @author LatvianModder
 */
public class QuestBarrierBlockEntity extends BlockEntity implements TickableBlockEntity {
	public long object = 0L;

	public QuestBarrierBlockEntity() {
		super(FTBQuestsBlockEntities.BARRIER.get());
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
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		readBarrier(tag);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return writeBarrier(super.save(tag));
	}

	@Override
	public void tick() {
		if (level != null && level.isClientSide() && FTBQuests.PROXY.isClientDataLoaded() && level.getGameTime() % 5L == 0L) {
			boolean completed = isComplete(FTBQuests.PROXY.getClientPlayerData());

			if (completed != getBlockState().getValue(COMPLETED)) {
				level.setBlock(getBlockPos(), getBlockState().setValue(COMPLETED, completed), 2 | 8);
				clearCache();
			}
		}
	}

	public void updateObject(long id) {
		object = id;

		if (!level.isClientSide()) {
			syncData();
		}
	}

	public void syncData() {
	}

	public boolean isComplete(TeamData data) {
		QuestObject o = data.file.get(object);
		return o != null && data.isCompleted(o);
	}
}
