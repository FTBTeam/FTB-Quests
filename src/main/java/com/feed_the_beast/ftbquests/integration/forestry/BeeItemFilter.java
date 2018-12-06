package com.feed_the_beast.ftbquests.integration.forestry;

import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.quest.task.filter.ItemFilter;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class BeeItemFilter extends ItemFilter
{
	private ItemStack beeStack = ItemStack.EMPTY;
	private boolean checkSecondary = false;

	private EnumBeeType beeType = null;
	private String primary = null;
	private String secondary = null;

	@Override
	public NBTBase toNBT(boolean forceTagCompound)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("bee", ItemMissing.write(beeStack, false));

		if (checkSecondary)
		{
			nbt.setBoolean("check_secondary", true);
		}

		return nbt;
	}

	@Override
	public void fromNBT(NBTBase n)
	{
		if (n instanceof NBTTagCompound)
		{
			NBTTagCompound nbt = (NBTTagCompound) n;
			beeStack = ItemMissing.read(nbt.getTag("bee"));
			checkSecondary = nbt.getBoolean("check_secondary");
		}
	}

	public void setBee(ItemStack stack)
	{
		if (BeeManager.beeRoot != null && BeeManager.beeRoot.getType(stack) != null)
		{
			beeStack = stack.copy();
			clearCache();
		}
	}

	@Override
	public boolean test(ItemStack stack)
	{
		if (BeeManager.beeRoot == null)
		{
			return false;
		}
		else if (beeType == null)
		{
			beeType = BeeManager.beeRoot.getType(beeStack);

			if (beeType == null)
			{
				return false;
			}

			IBee bee = BeeManager.beeRoot.getMember(beeStack);
			primary = bee.getGenome().getPrimary().getUID();
			secondary = bee.getGenome().getSecondary().getUID();
		}

		else if (BeeManager.beeRoot.isMember(stack, beeType))
		{
			IBee b = BeeManager.beeRoot.getMember(stack);
			return b != null && (!checkSecondary || b.getGenome().getSecondary().getUID().equals(secondary)) && b.getGenome().getPrimary().getUID().equals(primary);
		}

		return false;
	}

	@Override
	public boolean isValid()
	{
		return false;
	}

	@Override
	public void clearCache()
	{
		beeType = null;
		primary = null;
		secondary = null;
	}

	@Override
	public void getAllStacks(Collection<ItemStack> stacks)
	{
		stacks.add(beeStack);
	}
}
