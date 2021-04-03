package dev.ftb.mods.ftbquests.block.forge;

import dev.ftb.mods.ftbquests.FTBQuestsForge;
import dev.ftb.mods.ftbquests.integration.gamestages.GameStagesIntegration;
import net.minecraft.world.entity.player.Player;

public class StageBarrierBlockImpl {
	public static boolean hasStage0(Player player, String stage) {
		if (FTBQuestsForge.hasGameStages) {
			return GameStagesIntegration.hasStage(player, stage);
		}

		return player.getTags().contains(stage);
	}
}
