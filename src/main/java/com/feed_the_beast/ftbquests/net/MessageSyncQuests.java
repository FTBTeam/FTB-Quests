package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.nbt.NBTBase;
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
			data.writeShort(t.uid);
			data.writeString(t.name);
			data.writeVarInt(t.taskKeys.length);

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				data.writeInt(t.taskKeys[i]);
				data.writeNBTBase(t.taskValues[i]);
			}

			data.writeVarInt(t.variableKeys.length);

			for (int i = 0; i < t.variableKeys.length; i++)
			{
				data.writeInt(t.variableKeys[i]);
				data.writeVarLong(t.variableValues[i]);
			}
		};

		public static final DataIn.Deserializer<TeamInst> DESERIALIZER = data -> {
			TeamInst t = new TeamInst();
			t.uid = data.readShort();
			t.name = data.readString();
			t.taskKeys = new int[data.readVarInt()];
			t.taskValues = new NBTBase[t.taskKeys.length];

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				t.taskKeys[i] = data.readInt();
				t.taskValues[i] = data.readNBTBase();
			}

			t.variableKeys = new int[data.readVarInt()];
			t.variableValues = new long[t.variableKeys.length];

			for (int i = 0; i < t.variableKeys.length; i++)
			{
				t.variableKeys[i] = data.readInt();
				t.variableValues[i] = data.readVarLong();
			}

			return t;
		};

		public short uid;
		public String name;
		public int[] taskKeys;
		public NBTBase[] taskValues;
		public int[] variableKeys;
		public long[] variableValues;
	}

	public QuestFile file;
	public short team;
	public Collection<TeamInst> teamData;
	public boolean editingMode;
	public IntCollection rewards;

	public MessageSyncQuests()
	{
	}

	public MessageSyncQuests(QuestFile f, short t, Collection<TeamInst> td, boolean e, IntCollection r)
	{
		file = f;
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
		file.writeNetDataFull(data);
		data.writeShort(team);
		data.writeCollection(teamData, TeamInst.SERIALIZER);
		data.writeBoolean(editingMode);
		data.writeIntList(rewards);
	}

	@Override
	public void readData(DataIn data)
	{
		file = new ClientQuestFile();
		file.readNetDataFull(data);
		team = data.readShort();
		teamData = data.readCollection(TeamInst.DESERIALIZER);
		editingMode = data.readBoolean();
		rewards = data.readIntList();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		((ClientQuestFile) file).load(this);
	}
}