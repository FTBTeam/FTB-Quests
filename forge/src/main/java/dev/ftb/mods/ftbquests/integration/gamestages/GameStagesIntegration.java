package dev.ftb.mods.ftbquests.integration.gamestages;

import dev.ftb.mods.ftbquests.integration.StageHelper;
import dev.ftb.mods.ftbquests.quest.task.StageTask;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

/**
 * @author LatvianModder
 */
public class GameStagesIntegration extends StageHelper {
	public final GameStageHelperCommon proxy;

	public GameStagesIntegration() {
		proxy = DistExecutor.safeRunForDist(() -> GameStageHelperClient::new, () -> GameStageHelperCommon::new);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGameStageAdded(GameStageEvent.Added event) {
		if (event.getPlayer() instanceof ServerPlayer) {
			StageTask.checkStages((ServerPlayer) event.getPlayer());
		}
	}

	@SubscribeEvent
	public void onGameStageRemoved(GameStageEvent.Removed event) {
		if (event.getPlayer() instanceof ServerPlayer) {
			StageTask.checkStages((ServerPlayer) event.getPlayer());
		}
	}

	@Override
	public boolean has(Player player, String stage) {
		return proxy.hasStage(player, stage);
	}

	@Override
	public void add(ServerPlayer player, String stage) {
		GameStageHelper.addStage(player, stage);
		// GameStageHelper.syncPlayer(player);
	}

	@Override
	public void remove(ServerPlayer player, String stage) {
		GameStageHelper.removeStage(player, stage);
		// GameStageHelper.syncPlayer(player);
	}
}