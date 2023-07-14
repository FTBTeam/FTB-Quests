package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface ITaskScreen {
    Optional<TaskScreenBlockEntity> getCoreScreen();

    @NotNull
    UUID getTeamId();

    boolean isInputOnly();

    boolean isIndestructible();

    ItemStack getSkin();
}
