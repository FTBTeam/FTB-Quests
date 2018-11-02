package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageAddReward extends MessageToServer
{
	private int quest;
	private NBTTagCompound nbt;

	public MessageAddReward()
	{
	}

	public MessageAddReward(int q, NBTTagCompound n)
	{
		quest = q;
		nbt = n;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(quest);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (nbt != null && FTBQuests.canEdit(player))
		{
			Quest q = ServerQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				QuestReward r = QuestRewardType.createReward(q, nbt);

				if (r != null)
				{
					r.uid = ServerQuestFile.INSTANCE.readID(0);
					q.rewards.add(r);
					ServerQuestFile.INSTANCE.refreshIDMap();
					ServerQuestFile.INSTANCE.save();
					new MessageAddRewardResponse(quest, r.uid, nbt).sendToAll();
				}
			}
		}
	}
}