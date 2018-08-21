package com.feed_the_beast.ftbquests.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class ItemXPVial extends Item
{
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			int xpLevels = nbt.getInteger("xp_levels");

			if (xpLevels > 0)
			{
				player.addExperienceLevel(xpLevels);
			}
			else
			{
				player.addExperience(nbt.getInteger("xp"));
			}
		}

		stack.shrink(1);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}
}