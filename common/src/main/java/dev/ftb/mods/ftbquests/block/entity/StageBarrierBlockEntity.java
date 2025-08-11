package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class StageBarrierBlockEntity extends BaseBarrierBlockEntity {
	public StageBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntityTypes.STAGE_BARRIER.get(), blockPos, blockState);
	}

	@Override
	public boolean isOpen(Player player) {
		return !objStr.isEmpty() && StageHelper.INSTANCE.getProvider().has(player, objStr);
	}

	@Override
	protected void addConfigEntries(ConfigGroup cg) {
		cg.addString("stage", objStr, s -> objStr = s, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}
}
