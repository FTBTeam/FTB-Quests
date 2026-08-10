package dev.ftb.mods.ftbquests.client;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public enum LootCrateTintSource implements ItemTintSource {
    INSTANCE;

    public static final Identifier ID = FTBQuestsAPI.id("loot_crate");
    public static final MapCodec<LootCrateTintSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        LootCrate crate = LootCrateItem.getCrate(itemStack, true);
        return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.getColor().rgb());
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
