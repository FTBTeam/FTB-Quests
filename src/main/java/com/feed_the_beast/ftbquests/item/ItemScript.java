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
public class ItemScript extends Item
{
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote)
		{
			NBTTagCompound nbt = stack.getTagCompound();

			if (nbt != null)
			{
				player.getServer().getCommandManager().executeCommand(player.getServer(), nbt.getString("command").replace("@p", player.getName()));
			}
		}

		stack.shrink(1);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}
}