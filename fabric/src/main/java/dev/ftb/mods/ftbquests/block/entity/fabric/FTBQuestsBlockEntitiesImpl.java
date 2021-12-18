package dev.ftb.mods.ftbquests.block.entity.fabric;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FTBQuestsBlockEntitiesImpl {
	public static QuestBarrierBlockEntity createQuestBarrierEntity(BlockPos blockPos, BlockState blockState) {
		return new FabricQuestBarrierBlockEntity(blockPos, blockState);
	}

	public static StageBarrierBlockEntity createStageBarrierEntity(BlockPos blockPos, BlockState blockState) {
		return new FabricStageBarrierBlockEntity(blockPos, blockState);
	}
}
