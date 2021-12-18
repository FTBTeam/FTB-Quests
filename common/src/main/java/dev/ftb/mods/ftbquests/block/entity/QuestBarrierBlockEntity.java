package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.OPEN;

/**
 * @author LatvianModder
 */
public class QuestBarrierBlockEntity extends BlockEntity implements TickableBlockEntity, BarrierBlockEntity {
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
		writeBarrier(tag);
	}

	@Override
	public void tick() {
		if (level != null && level.isClientSide() && FTBQuests.PROXY.isClientDataLoaded() && level.getGameTime() % 5L == 0L) {
			boolean completed = isOpen(FTBQuests.PROXY.getClientPlayer());

			if (completed != getBlockState().getValue(OPEN)) {
				level.setBlock(getBlockPos(), getBlockState().setValue(OPEN, completed), 2 | 8);
				clearCache();
			}
		}
	}

	@Override
	public void update(String s) {
		object = ServerQuestFile.INSTANCE.getID(s);
		syncData();
	}

	public void syncData() {
	}

	@Override
	public boolean isOpen(Player player) {
		TeamData data = FTBQuests.PROXY.getQuestFile(player.level.isClientSide()).getData(player);
		QuestObject o = data.file.get(object);
		return o != null && data.isCompleted(o);
	}
}
