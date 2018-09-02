package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageEditVariable extends MessageToServer
{
	private int variable;
	private long maxValue;
	private boolean team;

	public MessageEditVariable()
	{
	}

	public MessageEditVariable(int id, long v, boolean t)
	{
		variable = id;
		maxValue = v;
		team = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(variable);
		data.writeLong(maxValue);
		data.writeBoolean(team);
	}

	@Override
	public void readData(DataIn data)
	{
		variable = data.readUnsignedShort();
		maxValue = data.readShort();
		team = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (variable < ServerQuestFile.INSTANCE.variables.size() && FTBQuests.canEdit(player))
		{
			QuestVariable v = ServerQuestFile.INSTANCE.variables.get(variable);
			v.maxValue = maxValue;
			v.team = team;
			new MessageEditVariableResponse(v.index, v.maxValue).sendToAll();

			if (v.maxValue <= 0L)
			{
				ServerQuestFile.INSTANCE.variables.remove(v.index);
				ServerQuestFile.INSTANCE.refreshIDMap();
			}

			ServerQuestFile.INSTANCE.clearCachedData();
			ServerQuestFile.INSTANCE.save();
		}
	}
}