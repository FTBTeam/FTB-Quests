package com.feed_the_beast.ftbquests.block.entity;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import me.shedaniel.architectury.extensions.BlockEntityExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static com.feed_the_beast.ftbquests.block.QuestBarrierBlock.COMPLETED;

/**
 * @author LatvianModder
 */
public class QuestBarrierBlockEntity extends BlockEntity implements TickableBlockEntity, BlockEntityExtension {
	public long object = 0L;

	public QuestBarrierBlockEntity() {
		super(FTBQuestsBlockEntities.BARRIER.get());
	}

	private void readBarrier(CompoundTag tag) {
		object = QuestObjectBase.parseCodeString(tag.getString("Object"));

		if (object == 0L) {
			object = tag.getLong("object");
		}
	}

	private CompoundTag writeBarrier(CompoundTag tag) {
		tag.putString("Object", QuestObjectBase.getCodeString(object));
		return tag;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void loadClientData(@NotNull BlockState state, @NotNull CompoundTag tag) {
		readBarrier(tag);
	}

	@Override
	public CompoundTag saveClientData(@NotNull CompoundTag tag) {
		return writeBarrier(tag);
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
		syncData();
	}

	public boolean isComplete(PlayerData data) {
		QuestObject o = data.file.get(object);
		return o != null && data.isComplete(o);
	}
}
