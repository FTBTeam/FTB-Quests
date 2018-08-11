package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageSetDepResponse extends MessageToClient
{
	private String id;
	private String dep;
	private boolean add;

	public MessageSetDepResponse()
	{
	}

	public MessageSetDepResponse(String i, String d, boolean a)
	{
		id = i;
		dep = d;
		add = a;
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
		data.writeString(dep);
		data.writeBoolean(add);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
		dep = data.readString();
		add = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest quest = ClientQuestFile.INSTANCE.getQuest(id);
			QuestObject d = ClientQuestFile.INSTANCE.get(dep);

			if (quest != null && d instanceof ProgressingQuestObject && quest.setDependency((ProgressingQuestObject) d, add))
			{
				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					gui.quests.refreshWidgets();
				}
			}
		}
	}
}