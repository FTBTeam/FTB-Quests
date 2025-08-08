package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.world.entity.player.Player;

public interface IEditable {
    boolean hasPermissionToEdit(Player player);
}
