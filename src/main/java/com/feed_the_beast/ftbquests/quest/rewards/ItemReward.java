package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ItemReward extends QuestReward
{
	public final ItemStack stack;

	public ItemReward(ItemStack is)
	{
		stack = is;
	}

	@Override
	public boolean reward(EntityPlayerMP player)
	{
		return player.addItemStackToInventory(stack);
	}

	@Override
	public QuestReward copy()
	{
		return new ItemReward(stack.copy());
	}

	@Override
	public Icon getIcon()
	{
		return ItemIcon.getItemIcon(stack);
	}

	@Override
	public JsonObject toJson()
	{
		return ItemStackSerializer.serialize(stack, true, false).getAsJsonObject();
	}

	public String toString()
	{
		if (stack.getCount() > 1)
		{
			return stack.getCount() + "x " + stack.getDisplayName();
		}

		return stack.getDisplayName();
	}
}