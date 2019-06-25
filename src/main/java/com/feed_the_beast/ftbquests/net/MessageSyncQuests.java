package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageToClient
{
	public static class TeamInst
	{
		public static final DataOut.Serializer<TeamInst> SERIALIZER = (data, t) -> {
			data.writeShort(t.uid);
			data.writeString(t.id);
			data.writeTextComponent(t.name);
			data.writeVarInt(t.taskKeys.length);

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				data.writeInt(t.taskKeys[i]);
				data.writeNBTBase(t.taskValues[i]);
			}

			data.writeVarInt(t.playerRewardUUIDs.length);

			for (int i = 0; i < t.playerRewardUUIDs.length; i++)
			{
				data.writeUUID(t.playerRewardUUIDs[i]);
				data.writeVarInt(t.playerRewardIDs[i].length);

				for (int j : t.playerRewardIDs[i])
				{
					data.writeInt(j);
				}
			}

			data.writeVarInt(t.teamRewards.length);

			for (int i : t.teamRewards)
			{
				data.writeInt(i);
			}
		};

		public static final DataIn.Deserializer<TeamInst> DESERIALIZER = data -> {
			TeamInst t = new TeamInst();
			t.uid = data.readShort();
			t.id = data.readString();
			t.name = data.readTextComponent();
			t.taskKeys = new int[data.readVarInt()];
			t.taskValues = new NBTBase[t.taskKeys.length];

			for (int i = 0; i < t.taskKeys.length; i++)
			{
				t.taskKeys[i] = data.readInt();
				t.taskValues[i] = data.readNBTBase();
			}

			t.playerRewardUUIDs = new UUID[data.readVarInt()];
			t.playerRewardIDs = new int[t.playerRewardUUIDs.length][];

			for (int i = 0; i < t.playerRewardUUIDs.length; i++)
			{
				t.playerRewardUUIDs[i] = data.readUUID();
				t.playerRewardIDs[i] = new int[data.readVarInt()];

				for (int j = 0; j < t.playerRewardIDs[i].length; j++)
				{
					t.playerRewardIDs[i][j] = data.readInt();
				}
			}

			t.teamRewards = new int[data.readVarInt()];

			for (int i = 0; i < t.teamRewards.length; i++)
			{
				t.teamRewards[i] = data.readInt();
			}

			return t;
		};

		public short uid;
		public String id;
		public ITextComponent name;
		public int[] taskKeys;
		public NBTBase[] taskValues;
		public UUID[] playerRewardUUIDs;
		public int[][] playerRewardIDs;
		public int[] teamRewards;
	}

	public QuestFile file;
	public short team;
	public Collection<TeamInst> teamData;
	public boolean editingMode;

	public MessageSyncQuests()
	{
	}

	public MessageSyncQuests(QuestFile f, short t, Collection<TeamInst> td, boolean e)
	{
		file = f;
		team = t;
		teamData = td;
		editingMode = e;
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
	}

	@Override
	public void readData(DataIn data)
	{
		file = new ClientQuestFile();
		file.readNetDataFull(data);
		team = data.readShort();
		teamData = data.readCollection(TeamInst.DESERIALIZER);
		editingMode = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		((ClientQuestFile) file).load(this);
	}
}