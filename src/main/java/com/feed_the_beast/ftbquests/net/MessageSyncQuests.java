package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageToClient
{
	public static class TeamInst
	{
		public static final DataOut.Serializer<TeamInst> SERIALIZER = (data, t) -> {
			data.writeString(t.name);
			data.writeShort(t.taskKeys.length);

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				data.writeShort(t.taskKeys[i]);
				data.writeNBTBase(t.taskValues[i]);
			}

			data.writeShort(t.variableKeys.length);

			for (int i = 0; i < t.variableKeys.length; i++)
			{
				data.writeShort(t.variableKeys[i]);
				data.writeLong(t.variableValues[i]);
			}
		};

		public static final DataIn.Deserializer<TeamInst> DESERIALIZER = data -> {
			TeamInst t = new TeamInst();
			t.name = data.readString();
			t.taskKeys = new short[data.readUnsignedShort()];
			t.taskValues = new NBTBase[t.taskKeys.length];

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				t.taskKeys[i] = data.readShort();
				t.taskValues[i] = data.readNBTBase();
			}

			t.variableKeys = new short[data.readUnsignedShort()];
			t.variableValues = new long[t.variableKeys.length];

			for (int i = 0; i < t.variableKeys.length; i++)
			{
				t.variableKeys[i] = data.readShort();
				t.variableValues[i] = data.readLong();
			}

			return t;
		};

		public String name;
		public short[] taskKeys;
		public NBTBase[] taskValues;
		public short[] variableKeys;
		public long[] variableValues;
	}

	public NBTTagCompound quests;
	public String team;
	public Collection<TeamInst> teamData;
	public boolean editingMode;
	public IntCollection rewards;

	public MessageSyncQuests()
	{
	}

	public MessageSyncQuests(NBTTagCompound n, String t, Collection<TeamInst> td, boolean e, IntCollection r)
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
		data.writeCollection(teamData, TeamInst.SERIALIZER);
		data.writeBoolean(editingMode);
		data.writeIntList(rewards);
	}

	@Override
	public void readData(DataIn data)
	{
		quests = data.readNBT();
		team = data.readString();
		teamData = data.readCollection(TeamInst.DESERIALIZER);
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