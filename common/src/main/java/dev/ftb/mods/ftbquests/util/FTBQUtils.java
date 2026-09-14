package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class FTBQUtils {
    public static <T> Optional<T> getComponent(ItemStack stack, Supplier<DataComponentType<T>> componentType) {
        return Optional.ofNullable(stack.get(componentType.get()));
    }

    public static <T> Optional<T> getComponent(ItemStack stack, DataComponentType<T> componentType) {
        return Optional.ofNullable(stack.get(componentType));
    }

    public static Identifier modDefaultedId(String idStr, @Nullable String fallback) {
        if (idStr.isEmpty()) {
            idStr = FTBQuestsAPI.MOD_ID + ":" + fallback;
        } else if (!idStr.contains(":")) {
            idStr = FTBQuestsAPI.MOD_ID + ":" + idStr;
        }
        return Identifier.parse(idStr);
    }

    public static String modDefaultedString(Identifier id) {
        return id.getNamespace().equals(FTBQuestsAPI.MOD_ID) ? id.getPath() : id.toString();
    }
}
