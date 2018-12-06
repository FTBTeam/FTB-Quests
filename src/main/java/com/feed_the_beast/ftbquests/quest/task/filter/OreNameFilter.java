package com.feed_the_beast.ftbquests.quest.task.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class OreNameFilter extends ItemFilter
{
	private String ore = "";
	private int oreID = -1;

	@Override
	public NBTBase toNBT(boolean forceTagCompound)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("ore", ore);
		return nbt;
	}

	@Override
	public void fromNBT(NBTBase nbt)
	{
		if (nbt instanceof NBTTagCompound)
		{
			ore = ((NBTTagCompound) nbt).getString("ore");
		}
		else
		{
			ore = "";
		}
	}

	@Override
	public boolean test(ItemStack stack)
	{
		if (oreID == -1)
		{
			oreID = OreDictionary.getOreID(ore);
		}

		for (int i : OreDictionary.getOreIDs(stack))
		{
			if (i == oreID)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isValid()
	{
		return !ore.isEmpty();
	}

	@Override
	public void clearCache()
	{
		oreID = -1;
	}

	@Override
	public void getAllStacks(Collection<ItemStack> stacks)
	{
		stacks.addAll(OreDictionary.getOres(ore, false));
	}
}