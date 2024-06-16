package dev.ftb.mods.ftbquests.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class FTBQuestsTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> NO_LOOT_CRATES = TagKey.create(Registries.ENTITY_TYPE, FTBQuestsAPI.rl("no_loot_crates"));
    }

    public static class Items {
        public static final TagKey<Item> CHECK_NBT = TagKey.create(Registries.ITEM, FTBQuestsAPI.rl("match_nbt"));
    }
}
