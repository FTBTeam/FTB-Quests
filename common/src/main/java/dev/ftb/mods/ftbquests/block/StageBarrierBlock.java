package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class StageBarrierBlock extends QuestBarrierBlock {
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter bg) {
		return FTBQuestsBlockEntities.createStageBarrierEntity();
	}
}
