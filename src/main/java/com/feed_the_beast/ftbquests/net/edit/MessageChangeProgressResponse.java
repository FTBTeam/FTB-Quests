package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageChangeProgressResponse extends MessageToClient
{
	private short team;
	private int id;
	private EnumChangeProgress type;

	public MessageChangeProgressResponse()
	{
	}

	public MessageChangeProgressResponse(short t, int i, EnumChangeProgress ty)
	{
		team = t;
		id = i;
		type = ty;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(team);
		data.writeInt(id);
		data.write(type, EnumChangeProgress.NAME_MAP);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readShort();
		id = data.readInt();
		type = EnumChangeProgress.NAME_MAP.read(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestObject object = ClientQuestFile.INSTANCE.get(id);

		if (object != null)
		{
			ITeamData t = ClientQuestFile.INSTANCE.getData(team);

			if (t != null)
			{
				EnumChangeProgress.sendUpdates = false;
				object.changeProgress(t, type);
				EnumChangeProgress.sendUpdates = true;
			}
		}
	}
}