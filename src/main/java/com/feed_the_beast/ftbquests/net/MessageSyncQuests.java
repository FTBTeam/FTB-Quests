package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageToClient
{
	public NBTTagCompound quests;
	public String team;
	public NBTTagCompound teamData;
	public boolean editingMode;
	public IntCollection rewards;

	public MessageSyncQuests()
	{
	}

	public MessageSyncQuests(NBTTagCompound n, String t, NBTTagCompound td, boolean e, IntCollection r)
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
		data.writeIntList(rewards);
	}

	@Override
	public void readData(DataIn data)
	{
		quests = data.readNBT();
		team = data.readString();
		teamData = data.readNBT();
		editingMode = data.readBoolean();
		rewards = data.readIntList();
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