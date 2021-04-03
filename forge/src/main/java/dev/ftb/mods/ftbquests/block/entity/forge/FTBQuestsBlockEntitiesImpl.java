package dev.ftb.mods.ftbquests.block.entity.forge;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;

public class FTBQuestsBlockEntitiesImpl {
	public static QuestBarrierBlockEntity createQuestBarrierEntity() {
		return new ForgeQuestBarrierBlockEntity();
	}

	public static StageBarrierBlockEntity createStageBarrierEntity() {
		return new ForgeStageBarrierBlockEntity();
	}
}
