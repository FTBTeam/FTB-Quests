package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.gui.IRewardListenerGui;
import com.feed_the_beast.ftbquests.gui.RewardKey;
import com.feed_the_beast.ftbquests.gui.RewardToast;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class MessageDisplayItemRewardToast extends MessageBase
{
	private final ItemStack stack;

	MessageDisplayItemRewardToast(PacketBuffer buffer)
	{
		stack = buffer.readItemStack();
	}

	public MessageDisplayItemRewardToast(ItemStack is)
	{
		stack = is;
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeItemStack(stack);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(stack, 1);
		Icon icon = ItemIcon.getItemIcon(stack1);

		if (!IRewardListenerGui.add(new RewardKey(stack.getDisplayName().getString(), icon).setStack(stack1), stack.getCount()))
		{
			String s = stack.getDisplayName().getFormattedText();

			if (stack.getCount() > 1)
			{
				s = stack.getCount() + "x " + s;
			}

			Minecraft.getInstance().getToastGui().add(new RewardToast(stack.getRarity().color + s, icon));
		}
	}
}