package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author LatvianModder
 */
public class MessageSelectTask extends MessageToServer
{
	private BlockPos pos;
	private int task;

	public MessageSelectTask()
	{
	}

	public MessageSelectTask(BlockPos p, int t)
	{
		pos = p;
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
		data.writePos(pos);
		data.writeInt(task);
	}

	@Override
	public void readData(DataIn data)
	{
		pos = data.readPos();
		task = data.readInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (player.world.isBlockLoaded(pos))
		{
			TileEntity tileEntity = player.world.getTileEntity(pos);

			if (tileEntity instanceof TileQuest)
			{
				TileQuest tile = (TileQuest) tileEntity;

				if (tile.canEdit() && tile.getOwner() != null && Universe.get().getPlayer(player).team.equalsTeam(((FTBQuestsTeamData) tile.getOwner()).team))
				{
					QuestTask t = ServerQuestList.INSTANCE.getTask(task);

					if (t != null && t.quest.isVisible(tile.getOwner()))
					{
						tile.setTask(t.id);
					}
				}
			}
		}
	}
}