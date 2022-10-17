package dev.ftb.mods.ftbquests;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class FTBQuestsTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> NO_LOOT_CRATES
                = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(FTBQuests.MOD_ID, "no_loot_crates"));
    }
}
