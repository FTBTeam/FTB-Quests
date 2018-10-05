package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateVariable extends MessageToClient
{
	private String team;
	private int variable;
	private long value;

	public MessageUpdateVariable()
	{
	}

	public MessageUpdateVariable(String t, int k, long v)
	{
		team = t;
		variable = k;
		value = v;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(team);
		data.writeShort(variable);
		data.writeLong(value);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readString();
		variable = data.readUnsignedShort();
		value = data.readLong();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestVariable v = variable < 0 || variable >= ClientQuestFile.INSTANCE.variables.size() ? null : ClientQuestFile.INSTANCE.variables.get(variable);

		if (v != null)
		{
			ClientQuestProgress data = ClientQuestFile.INSTANCE.getData(team);

			if (data != null)
			{
				data.setVariable(v, value);
			}
		}
	}
}