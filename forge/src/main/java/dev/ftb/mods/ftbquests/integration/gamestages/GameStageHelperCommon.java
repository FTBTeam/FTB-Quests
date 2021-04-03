package dev.ftb.mods.ftbquests.integration.gamestages;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;

public class GameStageHelperCommon {
	public boolean hasStage(Player player, String stage) {
		if (player instanceof ServerPlayer) {
			IStageData stageData = player instanceof FakePlayer ? null : GameStageSaveHandler.getPlayerData(player.getUUID());
			return stageData != null && stageData.hasStage(stage);
		}

		return false;
	}
}
