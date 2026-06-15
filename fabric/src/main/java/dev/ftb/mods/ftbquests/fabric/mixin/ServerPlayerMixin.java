package dev.ftb.mods.ftbquests.fabric.mixin;

import dev.ftb.mods.ftbquests.fabric.FTBQuestsFabric;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "openMenu", at = @At("RETURN"))
    private void openMenu(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        if (cir.getReturnValue().isPresent()) {
            ServerPlayer player = ((ServerPlayer) (Object) this);
            FTBQuestsFabric.getEventHandler().onContainerOpened(player, player.containerMenu);
        }
    }
}
