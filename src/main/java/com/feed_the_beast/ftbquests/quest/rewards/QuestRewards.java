package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class QuestRewards
{
	public interface RewardProvider
	{
		@Nullable
		QuestReward create(Quest quest, int id, NBTTagCompound nbt);
	}

	public static final Map<String, RewardProvider> MAP = new HashMap<>();

	public static void add(String type, RewardProvider provider)
	{
		MAP.put(type, provider);
	}

	public static void init()
	{
		add("item", (quest, id, nbt) -> {
			ItemStack stack = new ItemStack(nbt);

			if (!stack.isEmpty())
			{
				return new ItemReward(quest, id, stack);
			}

			return null;
		});

		add("xp", (quest, id, nbt) -> new ExperienceReward(quest, id, nbt.getInteger("xp")));
		add("xp_levels", (quest, id, nbt) -> new ExperienceLevelsReward(quest, id, nbt.getInteger("xp_levels")));
	}

	public static QuestReward createReward(Quest quest, int id, NBTTagCompound nbt)
	{
		RewardProvider provider = MAP.get(nbt.getString("type"));

		if (provider != null)
		{
			QuestReward reward = provider.create(quest, id, nbt);

			if (reward != null)
			{
				reward.teamReward = nbt.getBoolean("team_reward");
				return reward;
			}
		}

		return new UnknownReward(quest, id, nbt);
	}
}