package dev.ftb.mods.ftbquests.fabric.mixin;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;

import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
    @Inject(method = "upload", at = @At("RETURN"))
    private void onUpload(SpriteLoader.Preparations arg, CallbackInfo ci) {
        //noinspection DataFlowIssue
        FTBQuestsClientEventHandler.onTextureStitchPost((TextureAtlas) (Object) this);
    }
}
