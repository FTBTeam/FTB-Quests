package dev.ftb.mods.ftbquests.fabric.mixin;

import dev.ftb.mods.ftbquests.fabric.FTBQuestsFabric;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "addEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;addNewEntity(Lnet/minecraft/world/level/entity/EntityAccess;)Z"))
    private void addEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        FTBQuestsFabric.getEventHandler().onEntityJoinLevel(entity);
    }
}
