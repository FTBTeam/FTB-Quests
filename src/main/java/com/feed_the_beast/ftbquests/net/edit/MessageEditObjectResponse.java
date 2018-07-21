package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.google.gson.JsonElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditObjectResponse extends MessageToClient
{
	private int id;
	private JsonElement json;

	public MessageEditObjectResponse()
	{
	}

	public MessageEditObjectResponse(int i, JsonElement j)
	{
		id = i;
		json = j;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
		data.writeJson(json);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		json = data.readJson();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			QuestObject object = ClientQuestList.INSTANCE.get(id);

			if (object != null)
			{
				ConfigGroup group = new ConfigGroup(null);
				object.getConfig(group);
				group.fromJson(json);
				ClientQuestList.INSTANCE.refreshGui(ClientQuestList.INSTANCE);
			}
		}
	}
}