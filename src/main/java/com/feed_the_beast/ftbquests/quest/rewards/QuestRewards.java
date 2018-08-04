package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class QuestRewards
{
	public interface RewardProvider
	{
		QuestReward create(Quest quest, NBTTagCompound nbt);
	}

	private static final Map<String, RewardProvider> MAP0 = new HashMap<>();
	public static final Map<String, RewardProvider> MAP = Collections.unmodifiableMap(MAP0);

	public static void add(String type, RewardProvider provider)
	{
		MAP0.put(type, provider);
	}

	public static void init()
	{
		add(UnknownReward.ID, UnknownReward::new);
		add(ItemReward.ID, ItemReward::new);
		add(ExperienceReward.ID, ExperienceReward::new);
		add(ExperienceLevelsReward.ID_LEVELS, ExperienceLevelsReward::new);
		add(CommandReward.ID, CommandReward::new);
	}

	public static QuestReward createReward(Quest quest, NBTTagCompound nbt, boolean allowInvalid)
	{
		RewardProvider provider = MAP0.get(nbt.getString("type"));

		if (provider != null)
		{
			QuestReward reward = provider.create(quest, nbt);

			if (allowInvalid || !reward.isInvalid())
			{
				reward.teamReward.setBoolean(nbt.getBoolean("team_reward"));
				return reward;
			}
		}

		QuestReward reward = new UnknownReward(quest, nbt);
		reward.teamReward.setBoolean(nbt.getBoolean("team_reward"));
		return reward;
	}
}