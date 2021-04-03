package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.world.entity.player.Player;

public interface BarrierBlockEntity {
	void update(String s);

	boolean isOpen(Player player);
}
