package com.feed_the_beast.ftbquests.core.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor
{
	@Accessor
	void setCraftingRemainingItem(Item i);
}
