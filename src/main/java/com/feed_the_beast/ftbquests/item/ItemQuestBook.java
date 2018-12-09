package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class ItemQuestBook extends Item
{
	public ItemQuestBook()
	{
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if (world.isRemote)
		{
			openGui();
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	private void openGui()
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestFile.INSTANCE.openQuestGui();
		}
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		return stack.copy();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}
}