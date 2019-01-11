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
		else if (weight <= 0)
		{
			return "0%";
		}
		else if (weight >= totalWeight)
		{
			return "100%";
		}

		int chance = weight * 100 / totalWeight;
		double chanced = weight * 100D / (double) totalWeight;

		if (chance != chanced)
		{
			if (chanced < 0.01D)
			{
				return "<0.01%";
			}

			return String.format("%.2f%%", chanced);
		}

		return chance + "%";
	}

	@Override
	public int compareTo(WeightedReward o)
	{
		return Integer.compare(weight, o.weight);
	}
}