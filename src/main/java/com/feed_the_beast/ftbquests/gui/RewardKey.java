package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class RewardKey
{
	public final String title;
	public final Icon icon;
	public ItemStack stack;

	public RewardKey(String t, Icon i)
	{
		title = t;
		icon = i;
		stack = ItemStack.EMPTY;
	}

	public RewardKey setStack(ItemStack is)
	{
		stack = is;
		return this;
	}

	public int hashCode()
	{
		return title.hashCode();
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof RewardKey)
		{
			RewardKey e = (RewardKey) o;

			if (!stack.isEmpty())
			{
				return ItemStack.areItemStacksEqualUsingNBTShareTag(stack, e.stack);
			}
			else
			{
				return title.equals(e.title) && icon.equals(e.icon);
			}
		}

		return false;
	}
}