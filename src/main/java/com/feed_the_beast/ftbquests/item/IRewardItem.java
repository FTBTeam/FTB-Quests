package com.feed_the_beast.ftbquests.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public interface IRewardItem
{
	void reward(EntityPlayer player, ItemStack stack);
}