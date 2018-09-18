package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.config.IIteratingConfig;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageEditObjectQuick extends MessageToServer
{
	private String id;
	private String configId;
	private boolean next;

	public MessageEditObjectQuick()
	{
	}

	public MessageEditObjectQuick(String i, String c, boolean n)
	{
		id = i;
		configId = c;
		next = n;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(id);
		data.writeString(configId);
		data.writeBoolean(next);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
		configId = data.readString();
		next = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				ConfigGroup group1 = group.getGroup(object.getObjectType().getName());
				ConfigGroup g = group1;

				if (object instanceof QuestTask)
				{
					QuestTaskType type = QuestTaskType.getType(object.getClass());
					g = group1.getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
				}

				object.getConfig(g);
				object.getExtraConfig(g);

				ConfigValueInstance inst = g.getValueInstance(configId);

				if (inst != null && inst.getValue() instanceof IIteratingConfig)
				{
					((IIteratingConfig) inst.getValue()).iterate(inst, next);
					new MessageEditObjectQuickResponse(id, configId, next).sendToAll();
					ServerQuestFile.INSTANCE.save();
				}
			}
		}
	}
}