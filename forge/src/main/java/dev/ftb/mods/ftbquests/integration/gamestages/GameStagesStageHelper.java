package dev.ftb.mods.ftbquests.integration.gamestages;

import dev.ftb.mods.ftbquests.integration.StageHelper;
import dev.ftb.mods.ftbquests.quest.task.StageTask;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class GameStagesStageHelper extends StageHelper {
	public GameStagesStageHelper() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGameStageAdded(GameStageEvent.Added event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			StageTask.checkStages(player);
		}
	}

	@SubscribeEvent
	public void onGameStageRemoved(GameStageEvent.Removed event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			StageTask.checkStages(player);
		}
	}

	@Override
	public boolean has(Player player, String stage) {
		return GameStageHelper.hasStage(player, stage);
	}

	@Override
	public void add(ServerPlayer player, String stage) {
		GameStageHelper.addStage(player, stage);
		GameStageHelper.syncPlayer(player);
	}

	@Override
	public void remove(ServerPlayer player, String stage) {
		GameStageHelper.removeStage(player, stage);
		GameStageHelper.syncPlayer(player);
	}
}
