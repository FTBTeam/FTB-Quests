package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class MessageSyncRewards extends MessageToClient
{
	public Collection<ItemStack> rewards;

	public MessageSyncRewards()
	{
	}

	public MessageSyncRewards(Collection<ItemStack> r)
	{
		rewards = r;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeCollection(rewards, DataOut.ITEM_STACK);
	}

	@Override
	public void readData(DataIn data)
	{
		rewards = data.readCollection(DataIn.ITEM_STACK);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestFile.INSTANCE.rewards.items.clear();
			ClientQuestFile.INSTANCE.rewards.items.addAll(rewards);
			ClientQuestFile.INSTANCE.refreshGui(ClientQuestFile.INSTANCE);
		}
	}
}