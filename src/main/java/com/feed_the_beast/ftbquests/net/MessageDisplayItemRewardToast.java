package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.IRewardListenerGui;
import com.feed_the_beast.ftbquests.gui.RewardKey;
import com.feed_the_beast.ftbquests.gui.RewardToast;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class MessageDisplayItemRewardToast extends MessageToClient
{
	private ItemStack stack;
	public int count;

	public MessageDisplayItemRewardToast()
	{
	}

	public MessageDisplayItemRewardToast(ItemStack is, int s)
	{
		stack = is;
		count = s;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeItemStack(stack);
		data.writeVarInt(count);
	}

	@Override
	public void readData(DataIn data)
	{
		stack = data.readItemStack();
		count = data.readVarInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(stack, 1);
		Icon icon = ItemIcon.getItemIcon(stack1);

		if (!IRewardListenerGui.add(new RewardKey(stack.getDisplayName(), icon).setStack(stack1), count))
		{
			String s = stack.getDisplayName();

			if (count > 1)
			{
				s = count + "x " + s;
			}

			Minecraft.getMinecraft().getToastGui().add(new RewardToast(stack.getRarity().color + s, icon));
		}
	}
}