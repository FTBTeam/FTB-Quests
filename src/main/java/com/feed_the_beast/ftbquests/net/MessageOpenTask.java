package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageOpenTask extends MessageToServer
{
	private short task;

	public MessageOpenTask()
	{
	}

	public MessageOpenTask(short t)
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
		FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		QuestTaskData data = teamData.getQuestTaskData(task);

		if (data != null && data.task.quest.canStartTasks(teamData))
		{
			openGUI(data, player, null);
		}
	}

	public static void openGUI(QuestTaskData data, EntityPlayerMP player, @Nullable TileQuest tile)
	{
		player.getNextWindowId();
		player.closeContainer();
		player.openContainer = data.getContainer(player);
		player.openContainer.windowId = player.currentWindowId;

		if (tile != null)
		{
			((ContainerTaskBase) player.openContainer).tile = tile;
		}

		player.openContainer.addListener(player);
		new MessageOpenTaskGui(data.task.id, player.currentWindowId, tile != null, tile != null ? tile.getPos() : null).sendTo(player);
		MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
	}
}