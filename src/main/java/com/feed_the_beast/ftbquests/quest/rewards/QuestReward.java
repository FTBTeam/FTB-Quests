package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.events.QuestRewardEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestReward
{
	@Nullable
	public static QuestReward createReward(@Nullable JsonElement json0)
	{
		QuestReward reward = null;

		if (!JsonUtils.isNull(json0) && json0.isJsonObject())
		{
			JsonObject json = json0.getAsJsonObject();

			if (json.has("item"))
			{
				ItemStack stack = ItemStackSerializer.deserialize(json.get("item"));

				if (!stack.isEmpty())
				{
					reward = new ItemReward(stack);
				}
			}
			else if (json.has("xp"))
			{
				reward = new ExperienceReward(json.get("xp").getAsInt());
			}
			else if (json.has("xp_levels"))
			{
				reward = new ExperienceLevelReward(json.get("xp_levels").getAsInt());
			}
			else
			{
				QuestRewardEvent event = new QuestRewardEvent(json);
				event.post();
				reward = event.getReward();
			}

			if (reward != null)
			{
				if (json.has("team_reward"))
				{
					reward.teamReward = json.get("team_reward").getAsBoolean();
				}
			}
		}

		return reward;
	}

	public boolean teamReward = false;

	public abstract boolean reward(EntityPlayerMP player);

	public abstract QuestReward copy();

	public abstract Icon getIcon();

	public abstract JsonObject toJson();

	public String toString()
	{
		return toJson().toString();
	}
}