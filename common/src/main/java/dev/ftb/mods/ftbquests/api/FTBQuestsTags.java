package dev.ftb.mods.ftbquests.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class FTBQuestsTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> NO_LOOT_CRATES
                = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(FTBQuestsAPI.MOD_ID, "no_loot_crates"));
    }

    public static class Items {
        public static final TagKey<Item> CHECK_NBT
                = TagKey.create(Registries.ITEM, FTBQuestsAPI.rl("match_nbt"));

        @Deprecated
        // this will disappear in 1.20.2+
        public static final TagKey<Item> CHECK_NBT_ITEM_FILTERS
                = TagKey.create(Registries.ITEM, new ResourceLocation("itemfilters", "check_nbt"));
    }
}
