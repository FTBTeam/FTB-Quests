package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.config.IIteratingConfig;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditObjectQuickResponse extends MessageToClient
{
	private int id;
	private String configId;
	private boolean next;

	public MessageEditObjectQuickResponse()
	{
	}

	public MessageEditObjectQuickResponse(int i, String c, boolean n)
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
		data.writeInt(id);
		data.writeString(configId);
		data.writeBoolean(next);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		configId = data.readString();
		next = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObject object = ClientQuestFile.INSTANCE.get(id);

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
					//ClientQuestFile.INSTANCE.refreshGui();
				}
			}
		}
	}
}