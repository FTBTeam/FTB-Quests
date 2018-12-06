package com.feed_the_beast.ftbquests.quest.task.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public abstract class ItemFilter implements Predicate<ItemStack>
{
	public abstract NBTBase toNBT(boolean forceTagCompound);

	public abstract void fromNBT(NBTBase nbt);

	@Override
	public abstract boolean test(ItemStack stack);

	public boolean isValid()
	{
		return true;
	}

	public void clearCache()
	{
	}

	public abstract void getAllStacks(Collection<ItemStack> stacks);
}