package com.feed_the_beast.ftbquests.quest;

import net.minecraft.item.ItemStack;

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
}