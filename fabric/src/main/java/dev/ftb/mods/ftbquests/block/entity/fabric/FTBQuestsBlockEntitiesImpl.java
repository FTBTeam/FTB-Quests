package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;

public class FTBQuestsBlockEntitiesImpl {
	public static QuestBarrierBlockEntity createBarrierEntity() {
		return new FabricQuestBarrierBlockEntity();
	}
}
