package dev.ftb.mods.ftbquests.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class TaskScreenRenderState extends BlockEntityRenderState {
    boolean shouldRender;
    @Nullable UUID teamId;
    ItemStackRenderState taskItem = new ItemStackRenderState();
    boolean isInputOnly;
    float @Nullable [] fakeTextureUV = null;
    boolean textHasShadow;
    Component taskName = Component.empty();
    Component questName = Component.empty();
    Component progressText = Component.empty();
    float interpolatedProgress;
    @Nullable ResourceSprite resourceSprite;
    @Nullable ResourceSprite overlaySprite;
    public int resourceSpriteTint = 0xFFFFFFFF;
    public int screenSize = 1;
    public float rotationAngle = 0f;

    public record ResourceSprite(TextureAtlasSprite sprite, boolean interpolateHeight) {}
}
