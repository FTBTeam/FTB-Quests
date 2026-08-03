package dev.ftb.mods.ftbquests.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyMappingMixin {
    @Accessor("ALL")
    static Map<String, KeyMapping> getAll() {
        throw new AssertionError();
    }
}
