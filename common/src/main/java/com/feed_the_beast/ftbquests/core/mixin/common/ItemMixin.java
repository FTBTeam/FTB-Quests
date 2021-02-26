package com.feed_the_beast.ftbquests.core.mixin.common;

import com.feed_the_beast.ftbquests.core.ItemFTBQ;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemFTBQ {
	@Override
	@Accessor("craftingRemainingItem")
	public abstract void setCraftingRemainingItemFTBQ(Item i);
}
