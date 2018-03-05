package com.feed_the_beast.ftbquests.events;

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
	private final JsonObject json;
	private QuestReward reward = null;

	public QuestRewardEvent(JsonObject j)
	{
		json = j;
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