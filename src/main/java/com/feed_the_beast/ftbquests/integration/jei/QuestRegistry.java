package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public enum QuestRegistry
{
	INSTANCE;

	public final ArrayList<QuestWrapper> list = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public void refresh()
	{
		if (FTBQuestsJEIIntegration.runtime != null && !list.isEmpty())
		{
			for (QuestWrapper wrapper : list)
			{
				FTBQuestsJEIIntegration.runtime.getRecipeRegistry().removeRecipe(wrapper, QuestCategory.UID);
			}
		}

		list.clear();

		if (ClientQuestFile.exists())
		{
			for (Chapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					List<Reward> rewards = new ArrayList<>();

					for (Reward reward : quest.rewards)
					{
						if (!reward.invisible && reward.getIngredient() != null)
						{
							rewards.add(reward);
						}
					}

					if (!rewards.isEmpty())
					{
						QuestWrapper wrapper = new QuestWrapper(quest, rewards);
						list.add(wrapper);
						FTBQuestsJEIIntegration.runtime.getRecipeRegistry().addRecipe(wrapper, QuestCategory.UID);
					}
				}
			}
		}
	}
}