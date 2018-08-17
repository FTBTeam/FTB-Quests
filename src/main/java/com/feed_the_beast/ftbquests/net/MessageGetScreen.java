package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class MessageGetScreen extends MessageToServer
{
	private String task;
	private int size;

	public MessageGetScreen()
	{
	}

	public MessageGetScreen(String t, int s)
	{
		task = t;
		size = s;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(task);
		data.writeByte(size);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readString();
		size = data.readByte();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (size >= 0 && size <= 4 && (size == 0 || FTBQuests.canEdit(player)))
		{
			QuestTask t = ServerQuestFile.INSTANCE.getTask(task);

			if (t != null)
			{
				FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
				ItemStack stack = new ItemStack(FTBQuestsItems.SCREEN);
				TileScreenCore tile = new TileScreenCore();
				tile.quest = t.quest.getID();
				tile.task = t.id;
				tile.team = teamData.team.getName();
				tile.size = size;
				tile.writeToItem(stack);
				ItemHandlerHelper.giveItemToPlayer(player, stack);
			}
		}
	}
}