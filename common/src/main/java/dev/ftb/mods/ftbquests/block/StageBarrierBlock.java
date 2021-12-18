package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StageBarrierBlock extends QuestBarrierBlock {
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return FTBQuestsBlockEntities.createStageBarrierEntity(blockPos, blockState);
	}
}
