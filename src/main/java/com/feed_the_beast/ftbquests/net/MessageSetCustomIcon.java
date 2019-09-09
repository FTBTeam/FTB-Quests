package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;

/**
 * @author LatvianModder
 */
public class MessageSetCustomIcon extends MessageToServer
{
	private String icon;

	public MessageSetCustomIcon()
	{
	}

	public MessageSetCustomIcon(String i)
	{
		icon = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(icon);
	}

	@Override
	public void readData(DataIn data)
	{
		icon = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

		if (stack.getItem() != FTBQuestsItems.CUSTOM_ICON)
		{
			stack = player.getHeldItem(EnumHand.OFF_HAND);
		}

		if (stack.getItem() == FTBQuestsItems.CUSTOM_ICON)
		{
			stack.setTagInfo("icon", new NBTTagString(icon));
			player.inventoryContainer.detectAndSendChanges();
		}
	}
}