package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public interface ITaskScreen extends IEditable {
    Optional<TaskScreenBlockEntity> getCoreScreen();

    UUID getTeamId();

    boolean isInputOnly();

    boolean isIndestructible();

    ItemStack getSkin();
}
