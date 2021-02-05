package com.feed_the_beast.ftbquests.core.mixin.common;

import com.feed_the_beast.ftbquests.core.DisplayInfoFTBQ;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author LatvianModder
 */
@Mixin(DisplayInfo.class)
public abstract class DisplayInfoMixin implements DisplayInfoFTBQ
{
	@Override
	@Accessor("icon")
	public abstract ItemStack getIconStackFTBQ();
}
