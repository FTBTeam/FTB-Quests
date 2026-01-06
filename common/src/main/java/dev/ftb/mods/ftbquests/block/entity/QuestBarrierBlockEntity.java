package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class QuestBarrierBlockEntity extends BaseBarrierBlockEntity {
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
	protected void addConfigEntries(ConfigGroup cg) {
		cg.add("quest", new ConfigQuestObject<>(QuestObjectType.QUEST), getQuest(), this::setQuest, null)
				.setNameKey("ftbquests.quest");
	}

	public Quest getQuest() {
		if (cachedQuest == null && !objStr.isEmpty() || cachedQuest != null && !cachedQuest.getCodeString().equals(objStr)) {
			long objId = BaseQuestFile.parseCodeString(objStr);
			cachedQuest = FTBQuestsAPI.api().getQuestFile(level.isClientSide()).getQuest(objId);
		}

		return cachedQuest;
	}

	public void setQuest(Quest quest) {
		this.cachedQuest = quest;
		objStr = quest == null ? "" : quest.getCodeString();
		setChanged();
	}
}
