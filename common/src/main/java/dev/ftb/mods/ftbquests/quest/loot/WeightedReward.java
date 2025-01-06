package dev.ftb.mods.ftbquests.quest.loot;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;

public class WeightedReward implements Comparable<WeightedReward> {
	private final Reward reward;
	private float weight;

	public WeightedReward(Reward reward, float weight) {
		this.reward = reward;
		this.weight = Math.max(weight, 0f);
	}

	public Reward getReward() {
		return reward;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public static String chanceString(float weight, float totalWeight, boolean empty) {
		if (totalWeight <= 0f) {
			return "??%";
		} else if (weight <= 0f) {
			return empty ? "0%" : "100%";
		} else if (weight >= totalWeight) {
			return "100%";
		}

		int chance = (int) (weight * 100 / totalWeight);
		float chanced = weight * 100f / totalWeight;

		if (chance != chanced) {
			return chanced < 0.01f ? "<0.01%" : String.format("%.2f%%", chanced);
		}

		return chance + "%";
	}

	public static String chanceString(float weight, float totalWeight) {
		return chanceString(weight, totalWeight, false);
	}

	@Override
	public int compareTo(WeightedReward o) {
		return Float.compare(weight, o.weight);
	}

	public WeightedReward copy() {
		Reward r = QuestObjectBase.copy(reward, () -> RewardType.createReward(reward.id, reward.getQuest(), reward.getType().getTypeId().toString()));
		return new WeightedReward(r, weight);
	}
}