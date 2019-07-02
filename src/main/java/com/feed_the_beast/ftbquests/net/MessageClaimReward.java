package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageClaimReward extends MessageToServer
{
	private int id;
	private boolean notify;

	public MessageClaimReward()
	{
	}

	public MessageClaimReward(int i, boolean n)
	{
		id = i;
		notify = n;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
		data.writeBoolean(notify);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		notify = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);

		if (reward != null)
		{
			ServerQuestData teamData = ServerQuestData.get(Universe.get().getPlayer(player).team);

			if (reward.quest.isComplete(teamData))
			{
				teamData.claimReward(player, reward, notify);
			}
		}
	}
}