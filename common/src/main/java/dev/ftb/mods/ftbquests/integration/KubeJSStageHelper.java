package dev.ftb.mods.ftbquests.integration;

import dev.latvian.mods.kubejs.stages.Stages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class KubeJSStageHelper extends StageHelper {
	@Override
	public boolean has(Player player, String stage) {
		return Stages.get(player).has(stage);
	}

	@Override
	public void add(ServerPlayer player, String stage) {
		Stages.get(player).add(stage);
	}

	@Override
	public void remove(ServerPlayer player, String stage) {
		Stages.get(player).remove(stage);
	}
}
