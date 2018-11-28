package com.feed_the_beast.ftbquests.quest.reward;

/**
 * @author LatvianModder
 */
public class WeightedReward
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
}