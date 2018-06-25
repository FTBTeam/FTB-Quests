package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author LatvianModder
 */
public class MessageSelectQuestTask extends MessageToServer
{
	private BlockPos pos;
	private QuestTaskKey key;

	public MessageSelectQuestTask()
	{
	}

	public MessageSelectQuestTask(BlockPos p, QuestTaskKey k)
	{
		pos = p;
		key = k;
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
		QuestTaskKey.SERIALIZER.write(data, key);
	}

	@Override
	public void readData(DataIn data)
	{
		pos = data.readPos();
		key = QuestTaskKey.DESERIALIZER.read(data);
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

				if (tile.canEdit() && tile.getOwner() != null && Universe.get().getPlayer(player).team.equalsTeam(tile.getOwner().team))
				{
					QuestTask task = ServerQuestList.INSTANCE.getTask(key);

					if (task != null && task.parent.isVisible(tile.getOwner()))
					{
						tile.setTask(task);
					}
				}
			}
		}
	}
}