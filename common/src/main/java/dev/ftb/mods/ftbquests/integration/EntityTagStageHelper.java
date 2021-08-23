package dev.ftb.mods.ftbquests.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EntityTagStageHelper extends StageHelper {
	@Override
	public boolean has(Player player, String stage) {
		return player.getTags().contains(stage);
	}

	@Override
	public void add(ServerPlayer player, String stage) {
		player.addTag(stage);
	}

	@Override
	public void remove(ServerPlayer player, String stage) {
		player.removeTag(stage);
	}
}
