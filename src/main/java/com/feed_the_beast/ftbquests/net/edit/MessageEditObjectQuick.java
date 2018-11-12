package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageEditObjectQuick extends MessageToServer
{
	private int id;
	private String configId;
	private ConfigValue value;

	public MessageEditObjectQuick()
	{
	}

	public MessageEditObjectQuick(int i, String c, ConfigValue v)
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
		ConfigValue value = FTBLibAPI.createConfigValueFromId(data.readString());
		value.readData(data);
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup g = object.createSubGroup(group);
				object.getConfig(g);
				object.getExtraConfig(g);

				ConfigValueInstance inst = g.getValueInstance(configId);

				if (inst != null)
				{
					inst.getValue().setValueFromOtherValue(value);
					new MessageEditObjectQuickResponse(id, configId, value).sendToAll();
					ServerQuestFile.INSTANCE.save();
				}
			}
		}
	}
}