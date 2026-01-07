package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class TaskScreenRenderState extends BlockEntityRenderState {
    UUID teamId;
    Task task;
    boolean isInputOnly;
    ItemStack inputIcon;
    float @Nullable [] fakeTextureUV = null;
    boolean textHasShadow;
}
