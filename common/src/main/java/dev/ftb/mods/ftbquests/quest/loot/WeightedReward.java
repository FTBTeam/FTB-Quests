package dev.ftb.mods.ftbquests.quest.loot;

import dev.ftb.mods.ftbquests.quest.reward.Reward;

/**
 * @author LatvianModder
 */
public class WeightedReward implements Comparable<WeightedReward> {
	public final Reward reward;
	public int weight;

	public WeightedReward(Reward r, int w) {
		reward = r;
		weight = Math.max(w, 0);
	}

	public static String chanceString(int weight, int totalWeight, boolean empty) {
		if (totalWeight <= 0) {
			return "??%";
		} else if (weight <= 0) {
			return empty ? "0%" : "100%";
		} else if (weight >= totalWeight) {
			return "100%";
		}

		int chance = weight * 100 / totalWeight;
		double chanced = weight * 100D / (double) totalWeight;

		if (chance != chanced) {
			if (chanced < 0.01D) {
				return "<0.01%";
			}

			return String.format("%.2f%%", chanced);
		}

		return chance + "%";
	}

	public static String chanceString(int weight, int totalWeight) {
		return chanceString(weight, totalWeight, false);
	}

	@Override
	public int compareTo(WeightedReward o) {
		return Integer.compare(weight, o.weight);
	}
}