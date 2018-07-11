package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
@Cancelable
public class QuestRewardEvent extends FTBQuestsEvent
{
	private final Quest quest;
	private final int id;
	private final NBTTagCompound nbt;
	private QuestReward reward = null;

	public QuestRewardEvent(Quest q, int i, NBTTagCompound n)
	{
		quest = q;
		id = i;
		nbt = n;
	}

	public Quest getQuest()
	{
		return quest;
	}

	public int getID()
	{
		return id;
	}

	public NBTTagCompound getNBT()
	{
		return nbt;
	}

	@Nullable
	public QuestReward getReward()
	{
		return reward;
	}

	public void setReward(QuestReward r)
	{
		reward = r;
	}
}