package dev.ftb.mods.ftbquests.block.entity;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public interface ITaskScreen {
    Optional<TaskScreenBlockEntity> getCoreScreen();

    @Nonnull
    UUID getTeamId();

    boolean isInputOnly();

    boolean isIndestructible();

    ItemStack getSkin();
}
