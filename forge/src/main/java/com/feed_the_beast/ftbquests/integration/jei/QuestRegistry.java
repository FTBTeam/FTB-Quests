package com.feed_the_beast.ftbquests.integration.jei;

import java.util.ArrayList;

/**
 * @author LatvianModder
 */
public enum QuestRegistry
{
	INSTANCE;

	public final ArrayList<QuestWrapper> list = new ArrayList<>();

	public void refresh()
	{
		/*
		if (FTBQuestsJEIIntegration.runtime != null && !list.isEmpty())
		{
			for (QuestWrapper wrapper : list)
			{
				FTBQuestsJEIIntegration.runtime.getRecipeManager().removeRecipe(wrapper, QuestCategory.UID);
			}
		}

		list.clear();

		if (ClientQuestFile.exists())
		{
			for (Chapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.rewards.isEmpty() || quest.disableJEI.get(ClientQuestFile.INSTANCE.defaultQuestDisableJEI))
					{
						continue;
					}

					List<Reward> rewards = new ArrayList<>();

					for (Reward reward : quest.rewards)
					{
						if (reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE && reward.getIngredient() != null)
						{
							rewards.add(reward);
						}
					}

					if (!rewards.isEmpty())
					{
						QuestWrapper wrapper = new QuestWrapper(quest, rewards);
						list.add(wrapper);
						FTBQuestsJEIIntegration.runtime.getRecipeManager().addRecipe(wrapper, QuestCategory.UID);
					}
				}
			}
		}
		*/
	}
}