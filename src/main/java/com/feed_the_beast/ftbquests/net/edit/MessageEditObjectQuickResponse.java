package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditObjectQuickResponse extends MessageToClient
{
	private int id;
	private String configId;
	private ConfigValue value;

	public MessageEditObjectQuickResponse()
	{
	}

	public MessageEditObjectQuickResponse(int i, String c, ConfigValue v)
	{
		id = i;
		configId = c;
		value = v;
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
		data.writeString(configId);
		data.writeString(value.getID());
		value.writeData(data);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		configId = data.readString();
		value = FTBLibAPI.createConfigValueFromId(data.readString());
		value.readData(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup g = object.createSubGroup(group);
				object.getConfig(g);

				ConfigValueInstance inst = g.getValueInstance(configId);

				if (inst != null)
				{
					inst.getValue().setValueFromOtherValue(value);
				}

				ClientQuestFile.INSTANCE.clearCachedData();
			}
		}
	}
}