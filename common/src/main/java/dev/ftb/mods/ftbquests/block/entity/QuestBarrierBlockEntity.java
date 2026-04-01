package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class QuestBarrierBlockEntity extends BaseBarrierBlockEntity {
	@Nullable
	private Quest cachedQuest = null;

	public QuestBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntityTypes.BARRIER.get(), blockPos, blockState);
	}

	@Override
	protected void applySavedData(BarrierSavedData data) {
		super.applySavedData(data);

		cachedQuest = null;  // force recalc
	}

	@Override
	public void updateFromString(String objStr) {
		super.updateFromString(objStr);

		cachedQuest = null;
		setChanged();
	}

	@Override
	public boolean isOpen(Player player) {
		Quest quest = getQuest();
		return quest != null &&
				quest.getQuestFile().getTeamData(player).map(d -> d.isCompleted(quest)).orElse(false);
	}

	@Override
	protected void addConfigEntries(EditableConfigGroup cg) {
		cg.add("quest", new EditableQuestObject<>(QuestObjectType.QUEST), getQuest(), this::setQuest, null)
				.setNameKey("ftbquests.quest");
	}

	@Nullable
	public Quest getQuest() {
		if (cachedQuest == null && !objStr.isEmpty() || cachedQuest != null && !cachedQuest.getCodeString().equals(objStr)) {
			long objId = BaseQuestFile.parseCodeString(objStr);
			cachedQuest = FTBQuestsAPI.api().getQuestFile(level.isClientSide()).getQuest(objId);
		}

		return cachedQuest;
	}

	public void setQuest(@Nullable Quest quest) {
		this.cachedQuest = quest;
		objStr = quest == null ? "" : quest.getCodeString();
		setChanged();
	}
}
