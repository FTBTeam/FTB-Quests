package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageAddReward extends MessageToServer
{
	private String quest;
	private boolean team;
	private ItemStack stack;

	public MessageAddReward()
	{
	}

	public MessageAddReward(String q, boolean t, ItemStack is)
	{
		quest = q;
		team = t;
		stack = is;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(quest);
		data.writeBoolean(team);
		data.writeNBT(stack.isEmpty() ? null : stack.serializeNBT());
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		team = data.readBoolean();
		NBTTagCompound nbt = data.readNBT();
		stack = nbt == null ? ItemStack.EMPTY : new ItemStack(nbt);
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (!stack.isEmpty() && !quest.isEmpty() && FTBQuests.canEdit(player))
		{
			Quest q = ServerQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				QuestReward r = new QuestReward(q, System.identityHashCode(stack));
				r.team = team;
				r.stack = stack;
				q.rewards.add(r);
				ServerQuestFile.INSTANCE.allRewards.put(r.uid, r);
				ServerQuestFile.INSTANCE.save();
				new MessageAddRewardResponse(quest, r.uid, team, stack).sendToAll();
			}
		}
	}
}