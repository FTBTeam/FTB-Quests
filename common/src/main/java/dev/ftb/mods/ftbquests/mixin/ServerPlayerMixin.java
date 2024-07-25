package dev.ftb.mods.ftbquests.mixin;

import dev.ftb.mods.ftbquests.util.FTBQuestsServerPlayer;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements FTBQuestsServerPlayer {
    @Unique
    private String ftbquests$language = "en_us";

    @Inject(method = "updateOptions", at = @At("HEAD"))
    public void ftbquests$updateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        this.ftbquests$language = packet.language().toLowerCase(Locale.ROOT);
    }

    @Override
    public String ftbquests$language() {
        return this.ftbquests$language;
    }
}
