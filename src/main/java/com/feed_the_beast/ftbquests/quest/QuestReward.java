package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.item.IRewardItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public final class QuestReward
{
	public final Quest quest;
	public final int uid;

	public ItemStack stack = ItemStack.EMPTY;
	public boolean team = false;

	public QuestReward(Quest q, int i)
	{
		quest = q;
		uid = i;
	}

	public int hashCode()
	{
		return uid;
	}

	public boolean equals(Object object)
	{
		return object == this || object != null && uid == object.hashCode();
	}

	public String toString()
	{
		return String.format("%s#%08x", quest.getID(), uid);
	}

	public void claim(EntityPlayer player)
	{
		if (stack.getItem() instanceof IRewardItem)
		{
			((IRewardItem) stack.getItem()).reward(player, stack);
		}
		else
		{
			ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
		}
	}
}