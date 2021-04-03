package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;

public class FTBQuestsBlockEntitiesImpl {
	public static QuestBarrierBlockEntity createQuestBarrierEntity() {
		return new FabricQuestBarrierBlockEntity();
	}

	public static StageBarrierBlockEntity createStageBarrierEntity() {
		return new FabricStageBarrierBlockEntity();
	}
}
