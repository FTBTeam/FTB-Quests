package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.google.gson.JsonObject;
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
	private final JsonObject json;
	private QuestReward reward = null;

	public QuestRewardEvent(Quest q, int i, JsonObject j)
	{
		quest = q;
		id = i;
		json = j;
	}

	public Quest getQuest()
	{
		return quest;
	}

	public int getID()
	{
		return id;
	}

	public JsonObject getJson()
	{
		return json;
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