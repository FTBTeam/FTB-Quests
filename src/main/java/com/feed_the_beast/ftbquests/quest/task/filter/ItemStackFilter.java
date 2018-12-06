package com.feed_the_beast.ftbquests.quest.task.filter;

import com.feed_the_beast.ftbquests.item.ItemMissing;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ItemStackFilter extends ItemFilter
{
	public static final ItemStackFilter EMPTY = new ItemStackFilter()
	{
		@Override
		public void setIgnoreDamage(boolean v)
		{
		}

		@Override
		public void setNBTMode(NBTMatchingMode v)
		{
		}

		@Override
		public boolean test(ItemStack stack)
		{
			return stack.isEmpty();
		}

		@Override
		public boolean isValid()
		{
			return true;
		}

		@Override
		public void getAllStacks(Collection<ItemStack> stacks)
		{
		}
	};

	private ItemStack stack = ItemStack.EMPTY;
	private boolean ignoreDamage = false;
	private NBTMatchingMode nbtMode = NBTMatchingMode.MATCH;

	private Item item;
	private int meta;
	private NBTTagCompound nbt;

	public void setStack(ItemStack is)
	{
		stack = is;
		clearCache();
	}

	public void setIgnoreDamage(boolean v)
	{
		ignoreDamage = v;
		clearCache();
	}

	public void setNBTMode(NBTMatchingMode v)
	{
		nbtMode = v;
		clearCache();
	}

	@Override
	public NBTBase toNBT(boolean forceTagCompound)
	{
		if (ignoreDamage || nbtMode != NBTMatchingMode.MATCH || forceTagCompound)
		{
			NBTTagCompound nbt = (NBTTagCompound) ItemMissing.write(stack, true);

			if (ignoreDamage)
			{
				nbt.setBoolean("ignore_damage", true);
			}

			if (nbtMode != NBTMatchingMode.MATCH)
			{
				nbt.setByte("ignore_nbt", (byte) nbtMode.ordinal());
			}

			return nbt;
		}

		return ItemMissing.write(stack, false);
	}

	@Override
	public void fromNBT(NBTBase n)
	{
		stack = ItemMissing.read(n);

		if (n instanceof NBTTagCompound)
		{
			NBTTagCompound nbt = (NBTTagCompound) n;
			ignoreDamage = nbt.getBoolean("ignore_damage");
			nbtMode = NBTMatchingMode.NAME_MAP.get(nbt.getByte("ignore_nbt"));
		}
		else
		{
			ignoreDamage = false;
			nbtMode = NBTMatchingMode.MATCH;
		}
	}

	@Override
	public boolean test(ItemStack is)
	{
		if (is == stack)
		{
			return true;
		}

		if (item == null)
		{
			item = stack.getItem();
			meta = ignoreDamage ? 0 : stack.getMetadata();
			nbt = nbtMode == NBTMatchingMode.CONTAIN ? stack.getTagCompound() : item.getNBTShareTag(stack);
		}

		if (item == is.getItem() && (ignoreDamage || meta == is.getMetadata()))
		{
			switch (nbtMode)
			{
				case MATCH:
					return Objects.equals(nbt, is.getItem().getNBTShareTag(is));
				case IGNORE:
					return true;
				case CONTAIN:
				{
					NBTTagCompound nbt1 = is.getTagCompound();

					if (nbt1 == null || nbt1.isEmpty())
					{
						return true;
					}
					else if (nbt == null || nbt.isEmpty())
					{
						return false;
					}

					for (String s : nbt1.getKeySet())
					{
						if (!Objects.equals(nbt.getTag(s), nbt1.getTag(s)))
						{
							return false;
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isValid()
	{
		return !stack.isEmpty();
	}

	@Override
	public void clearCache()
	{
		item = null;
		meta = 0;
		nbt = null;
	}

	@Override
	public void getAllStacks(Collection<ItemStack> stacks)
	{
		stacks.add(stack);
	}
}