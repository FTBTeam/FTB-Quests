package dev.ftb.mods.ftbquests.registry;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity.BarrierSavedData;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity.TaskScreenSaveData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES
            = DeferredRegister.create(FTBQuestsAPI.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static RegistrySupplier<DataComponentType<ResourceLocation>> CUSTOM_ICON
            = COMPONENT_TYPES.register("icon", () -> new DataComponentType.Builder<ResourceLocation>()
            .persistent(ResourceLocation.CODEC)
            .networkSynchronized(ResourceLocation.STREAM_CODEC)
            .build());

    public static RegistrySupplier<DataComponentType<String>> LOOT_CRATE
            = COMPONENT_TYPES.register("loot_crate", () -> new DataComponentType.Builder<String>()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build());

    public static RegistrySupplier<DataComponentType<ItemContainerContents>> LOOT_CRATE_ITEMS
            = COMPONENT_TYPES.register("loot_crate_items", () -> new DataComponentType.Builder<ItemContainerContents>()
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
            .build());

    public static RegistrySupplier<DataComponentType<GlobalPos>> SCREEN_POS
            = COMPONENT_TYPES.register("screen_pos", () -> new DataComponentType.Builder<GlobalPos>()
            .persistent(GlobalPos.CODEC)
            .networkSynchronized(GlobalPos.STREAM_CODEC)
            .build());

    public static RegistrySupplier<DataComponentType<String>> MISSING_ITEM_DESC
            = COMPONENT_TYPES.register("missing_item", () -> new DataComponentType.Builder<String>()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build());

    public static RegistrySupplier<DataComponentType<TaskScreenSaveData>> TASK_SCREEN_SAVED
            = COMPONENT_TYPES.register("task_screen_saved", () -> new DataComponentType.Builder<TaskScreenSaveData>()
            .persistent(TaskScreenSaveData.CODEC)
            .networkSynchronized(TaskScreenSaveData.STREAM_CODEC)
            .build());

    public static RegistrySupplier<DataComponentType<BarrierSavedData>> BARRIER_SAVED
            = COMPONENT_TYPES.register("barrier_saved", () -> new DataComponentType.Builder<BarrierSavedData>()
            .persistent(BarrierSavedData.CODEC)
            .networkSynchronized(BarrierSavedData.STREAM_CODEC)
            .build());

    public static void register() {
        COMPONENT_TYPES.register();
    }
}
