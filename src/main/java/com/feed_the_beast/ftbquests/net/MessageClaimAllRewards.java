package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageClaimAllRewards extends MessageToServer
{
	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
	}

	@Override
	public void readData(DataIn data)
	{
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		ServerQuestData teamData = ServerQuestData.get(Universe.get().getPlayer(player).team);

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(teamData))
				{
					for (Reward reward : quest.rewards)
					{
						if (!reward.getExcludeFromClaimAll())
						{
							teamData.claimReward(player, reward);
						}
					}
				}
			}
		}
	}
}