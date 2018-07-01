package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

/**
 * @author LatvianModder
 */
public class MessageOpenTask extends MessageToServer
{
	private int task;

	public MessageOpenTask()
	{
	}

	public MessageOpenTask(int t)
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
		data.writeInt(task);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		openGUI(FTBQuestsTeamData.get(Universe.get().getPlayer(player).team).getQuestTaskData(task), player);
	}

	public static void openGUI(QuestTaskData data, EntityPlayerMP player)
	{
		player.getNextWindowId();
		player.closeContainer();
		player.openContainer = data.getContainer(player);
		player.openContainer.windowId = player.currentWindowId;
		player.openContainer.addListener(player);
		new MessageOpenTaskGui(data.task.id, player.currentWindowId).sendTo(player);
		MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
	}
}