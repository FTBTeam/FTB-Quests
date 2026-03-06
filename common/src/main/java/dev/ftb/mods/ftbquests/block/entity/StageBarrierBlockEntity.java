package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;

public class StageBarrierBlockEntity extends BaseBarrierBlockEntity {
	public StageBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntityTypes.STAGE_BARRIER.get(), blockPos, blockState);
	}

	@Override
	public boolean isOpen(Player player) {
		return !objStr.isEmpty() &&
				(StageHelper.INSTANCE.getProvider().has(player, objStr) || TeamStagesHelper.hasTeamStage(player, objStr));
	}

	@Override
	protected void addConfigEntries(EditableConfigGroup cg) {
		cg.addString("stage", objStr, s -> objStr = s, "").setNameKey("ftbquests.task.ftbquests.gamestage");
	}
}
