package dev.ftb.mods.ftbquests.integration.gamestages;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class GameStageHelperClient extends GameStageHelperCommon {
	@Override
	public boolean hasStage(Player player, String stage) {
		if (player instanceof AbstractClientPlayer) {
			return player instanceof LocalPlayer && GameStageSaveHandler.getClientData().hasStage(stage);
		}

		return super.hasStage(player, stage);
	}
}
