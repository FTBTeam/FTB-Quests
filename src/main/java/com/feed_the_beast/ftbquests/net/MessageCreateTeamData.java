package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageCreateTeamData extends MessageToClient
{
	private short uid;
	private String id;
	private ITextComponent name;

	public MessageCreateTeamData()
	{
	}

	public MessageCreateTeamData(ForgeTeam team)
	{
		uid = team.getUID();
		id = team.getID();
		name = team.getTitle();
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(uid);
		data.writeString(id);
		data.writeTextComponent(name);
	}

	@Override
	public void readData(DataIn data)
	{
		uid = data.readShort();
		id = data.readString();
		name = data.readTextComponent();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestProgress data = new ClientQuestProgress(uid, id, name);

			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}

			ClientQuestFile.INSTANCE.addData(data);
		}
	}
}