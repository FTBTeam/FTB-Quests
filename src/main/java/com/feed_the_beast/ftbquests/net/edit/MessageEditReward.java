package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageEditReward extends MessageToServer
{
	private int uid;
	private NBTTagCompound nbt;

	public MessageEditReward()
	{
	}

	public MessageEditReward(QuestReward reward)
	{
		uid = reward.uid;
		nbt = new NBTTagCompound();
		reward.writeData(nbt);
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(uid);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		uid = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestReward q = ServerQuestFile.INSTANCE.allRewards.get(uid);

			if (q != null)
			{
				if (nbt == null)
				{
					q.quest.rewards.remove(q);
					ServerQuestFile.INSTANCE.allRewards.remove(q.uid);
				}
				else
				{

				}

				ServerQuestFile.INSTANCE.save();
				new MessageEditRewardResponse(uid, nbt).sendToAll();
			}
		}
	}
}