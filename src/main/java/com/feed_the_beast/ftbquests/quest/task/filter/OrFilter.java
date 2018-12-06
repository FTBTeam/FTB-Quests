package com.feed_the_beast.ftbquests.quest.task.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class OrFilter extends ItemFilter
{
	public final List<ItemFilter> filters = new ArrayList<>();

	@Override
	public NBTBase toNBT(boolean forceTagCompound)
	{
		if (filters.size() == 1 && filters.get(0).isValid())
		{
			return filters.get(0).toNBT(forceTagCompound);
		}

		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();

		for (ItemFilter filter : filters)
		{
			if (filter.isValid())
			{
				list.appendTag(filter.toNBT(true));
			}
		}

		nbt.setTag("or", list);
		return nbt;
	}

	@Override
	public void fromNBT(NBTBase nbt)
	{
		filters.clear();

		if (nbt instanceof NBTTagCompound)
		{
			NBTTagList list = ((NBTTagCompound) nbt).getTagList("or", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemFilter filter = ItemFilterRegistry.createFilter(list.getCompoundTagAt(i));

				if (filter.isValid())
				{
					filters.add(filter);
				}
			}
		}
	}

	@Override
	public boolean test(ItemStack stack)
	{
		for (ItemFilter filter : filters)
		{
			if (filter.isValid() && !filter.test(stack))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isValid()
	{
		for (ItemFilter filter : filters)
		{
			if (filter.isValid())
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void clearCache()
	{
		for (ItemFilter filter : filters)
		{
			filter.clearCache();
		}
	}

	@Override
	public void getAllStacks(Collection<ItemStack> stacks)
	{
		for (ItemFilter filter : filters)
		{
			filter.getAllStacks(stacks);
		}
	}
}