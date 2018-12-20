package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftbquests.quest.reward.QuestReward;

/**
 * @author LatvianModder
 */
public class WeightedReward implements Comparable<WeightedReward>
{
	public final QuestReward reward;
	public int weight;

	public WeightedReward(QuestReward r, int w)
	{
		reward = r;
		weight = Math.max(w, 1);
	}

	public static String chanceString(int weight, int totalWeight)
	{
		if (totalWeight <= 0)
		{
			return "??%";
		}

		int chance = weight * 100 / totalWeight;

		if (chance == 0)
		{
			return String.format("%.2f%%", weight * 100D / (double) totalWeight);
		}

		return chance + "%";
	}

	@Override
	public int compareTo(WeightedReward o)
	{
		return Integer.compare(weight, o.weight);
	}
}