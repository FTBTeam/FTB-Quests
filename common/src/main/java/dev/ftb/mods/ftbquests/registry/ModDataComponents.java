package dev.ftb.mods.ftbquests.registry;

import com.mojang.serialization.Codec;
import de.marhali.json5.Json5Element;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity.BarrierSavedData;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity.TaskScreenSaveData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.ItemContainerContents;

public class ModDataComponents {
    public static final XRegistry<DataComponentType<?>> COMPONENT_TYPES
            = XRegistry.create(FTBQuestsAPI.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static XRegistryRef<DataComponentType<Identifier>> CUSTOM_ICON
            = COMPONENT_TYPES.register("icon", () -> new DataComponentType.Builder<Identifier>()
            .persistent(Identifier.CODEC)
            .networkSynchronized(Identifier.STREAM_CODEC)
            .build());

    public static XRegistryRef<DataComponentType<Identifier>> ENTITY_FACE_ICON
            = COMPONENT_TYPES.register("entity_face", () -> new DataComponentType.Builder<Identifier>()
            .persistent(Identifier.CODEC)
            .networkSynchronized(Identifier.STREAM_CODEC)
            .build());

    public static XRegistryRef<DataComponentType<String>> LOOT_CRATE
            = COMPONENT_TYPES.register("loot_crate", () -> new DataComponentType.Builder<String>()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build());

    public static XRegistryRef<DataComponentType<ItemContainerContents>> LOOT_CRATE_ITEMS
            = COMPONENT_TYPES.register("loot_crate_items", () -> new DataComponentType.Builder<ItemContainerContents>()
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
            .build());

    public static XRegistryRef<DataComponentType<GlobalPos>> SCREEN_POS
            = COMPONENT_TYPES.register("screen_pos", () -> new DataComponentType.Builder<GlobalPos>()
            .persistent(GlobalPos.CODEC)
            .networkSynchronized(GlobalPos.STREAM_CODEC)
            .build());

    public static XRegistryRef<DataComponentType<String>> MISSING_ITEM_ID
            = COMPONENT_TYPES.register("missing_item", () -> new DataComponentType.Builder<String>()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build());
    public static XRegistryRef<DataComponentType<Integer>> MISSING_ITEM_COUNT
            = COMPONENT_TYPES.register("missing_item_count", () -> new DataComponentType.Builder<Integer>()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build());
    public static XRegistryRef<DataComponentType<Json5Element>> MISSING_ITEM_DATA
            = COMPONENT_TYPES.register("missing_item_data", () -> new DataComponentType.Builder<Json5Element>()
            .persistent(ExtraCodecs.converter(Json5Ops.INSTANCE))
            .networkSynchronized(Json5NetPacker.CODEC)
            .build());

    public static XRegistryRef<DataComponentType<TaskScreenSaveData>> TASK_SCREEN_SAVED
            = COMPONENT_TYPES.register("task_screen_saved", () -> new DataComponentType.Builder<TaskScreenSaveData>()
            .persistent(TaskScreenSaveData.CODEC)
            .networkSynchronized(TaskScreenSaveData.STREAM_CODEC)
            .build());

    public static XRegistryRef<DataComponentType<BarrierSavedData>> BARRIER_SAVED
            = COMPONENT_TYPES.register("barrier_saved", () -> new DataComponentType.Builder<BarrierSavedData>()
            .persistent(BarrierSavedData.CODEC)
            .networkSynchronized(BarrierSavedData.STREAM_CODEC)
            .build());

    public static void register() {
        COMPONENT_TYPES.init();
    }
}
