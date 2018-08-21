package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageToClient
{
	public NBTTagCompound quests;
	public String team;
	public NBTTagCompound teamData;
	public boolean editingMode;
	public Collection<ItemStack> rewards;

	public MessageSyncQuests()
	{
	}

	public MessageSyncQuests(NBTTagCompound n, String t, NBTTagCompound td, boolean e, Collection<ItemStack> r)
	{
		quests = n;
		team = t;
		teamData = td;
		editingMode = e;
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
		data.writeNBT(quests);
		data.writeString(team);
		data.writeNBT(teamData);
		data.writeBoolean(editingMode);
		data.writeCollection(rewards, DataOut.ITEM_STACK);
	}

	@Override
	public void readData(DataIn data)
	{
		quests = data.readNBT();
		team = data.readString();
		teamData = data.readNBT();
		editingMode = data.readBoolean();
		rewards = data.readCollection(DataIn.ITEM_STACK);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			ClientQuestFile.INSTANCE.deleteChildren();
			ClientQuestFile.INSTANCE.deleteSelf();
		}

		ClientQuestFile.INSTANCE = new ClientQuestFile(this, ClientQuestFile.INSTANCE);
	}
}