package dev.ftb.mods.ftbquests.block.fabric;

import net.minecraft.world.entity.player.Player;

public class StageBarrierBlockImpl {
	public static boolean hasStage0(Player player, String stage) {
		return player.getTags().contains(stage);
	}
}
