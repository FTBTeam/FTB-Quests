package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class FTBQuestsTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> NO_LOOT_CRATES
                = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(FTBQuestsAPI.MOD_ID, "no_loot_crates"));
    }
}
