package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageClaimChoiceReward extends MessageToServer
{
	private int id;
	private int index;

	public MessageClaimChoiceReward()
	{
	}

	public MessageClaimChoiceReward(int i, int idx)
	{
		id = i;
		index = idx;
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
		data.writeVarInt(index);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		index = data.readVarInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		QuestReward reward = ServerQuestFile.INSTANCE.getReward(id);

		if (reward instanceof ChoiceReward)
		{
			FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);

			if (reward.quest instanceof QuestObject && reward.quest.isComplete(teamData))
			{
				ChoiceReward r = (ChoiceReward) reward;

				if (index >= 0 && index < r.getTable().rewards.size())
				{
					r.getTable().rewards.get(index).reward.claim(player);
					teamData.claimReward(player, reward);
				}
			}
		}
	}
}