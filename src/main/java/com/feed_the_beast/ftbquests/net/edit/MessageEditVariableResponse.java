package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditVariableResponse extends MessageToClient
{
	private int variable;
	private long maxValue;

	public MessageEditVariableResponse()
	{
	}

	public MessageEditVariableResponse(int id, long v)
	{
		variable = id;
		maxValue = v;
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
	}

	@Override
	public void readData(DataIn data)
	{
		variable = data.readUnsignedShort();
		maxValue = data.readShort();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (variable < ClientQuestFile.INSTANCE.variables.size())
		{
			ClientQuestFile.INSTANCE.variables.get(variable).maxValue = maxValue;

			if (maxValue <= 0L)
			{
				ClientQuestFile.INSTANCE.variables.remove(variable);
				ClientQuestFile.INSTANCE.refreshIDMap();
			}

			ClientQuestFile.INSTANCE.clearCachedData();
		}
	}
}