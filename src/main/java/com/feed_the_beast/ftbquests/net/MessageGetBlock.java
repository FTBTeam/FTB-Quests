package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.QuestBlockData;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class MessageGetBlock extends MessageToServer
{
	private short task;

	public MessageGetBlock()
	{
	}

	public MessageGetBlock(short t)
	{
		task = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(task);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readShort();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuestsConfig.general.allow_take_quest_blocks)
		{
			QuestTask t = ServerQuestList.INSTANCE.getTask(task);
			FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);

			if (t != null && t.quest.isVisible(teamData) && !t.isComplete(teamData))
			{
				ItemStack stack = new ItemStack(FTBQuestsItems.QUEST_BLOCK);
				QuestBlockData data = QuestBlockData.get(stack);
				data.setTask(task);
				data.setOwner(teamData.team.getName());
				ItemHandlerHelper.giveItemToPlayer(player, stack);
			}
		}
	}
}