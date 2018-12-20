package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiSelectQuestObject extends GuiButtonListBase
{
	public class ButtonQuestObject extends SimpleTextButton
	{
		public final QuestObjectBase object;

		public ButtonQuestObject(Panel panel, QuestObjectBase o)
		{
			super(panel, o.getObjectType().getColor() + o.getDisplayName().getUnformattedText(), o.getIcon());
			object = o;
			setSize(200, 14);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(object.getDisplayName().getFormattedText());
			list.add(TextFormatting.GRAY + "ID: " + TextFormatting.DARK_GRAY + object);
			list.add(TextFormatting.GRAY + "Type: " + object.getObjectType().getColor() + I18n.format(object.getObjectType().getTranslationKey()));

			if (object instanceof Quest)
			{
				Quest quest = (Quest) object;
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + quest.chapter.getDisplayName().getUnformattedText());

				if (quest.rewards.size() == 1)
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.reward") + ": " + QuestObjectType.REWARD.getColor() + quest.rewards.get(0).getDisplayName().getUnformattedText());
				}
				else if (!quest.rewards.isEmpty())
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.rewards"));

					for (QuestReward reward : quest.rewards)
					{
						list.add("  " + QuestObjectType.REWARD.getColor() + reward.getDisplayName().getUnformattedText());
					}
				}
			}
			else if (object instanceof QuestTask)
			{
				Quest quest = ((QuestTask) object).quest;
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + quest.chapter.getDisplayName().getUnformattedText());
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + QuestObjectType.QUEST.getColor() + quest.getDisplayName().getUnformattedText());

				if (quest.rewards.size() == 1)
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.reward") + ": " + QuestObjectType.REWARD.getColor() + quest.rewards.get(0).getDisplayName().getUnformattedText());
				}
				else if (!quest.rewards.isEmpty())
				{
					list.add(TextFormatting.GRAY + I18n.format("ftbquests.rewards"));

					for (QuestReward reward : quest.rewards)
					{
						list.add("  " + QuestObjectType.REWARD.getColor() + reward.getDisplayName().getUnformattedText());
					}
				}
			}
			else if (object instanceof RewardTable)
			{
				((RewardTable) object).addMouseOverText(list, true, true);
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			callbackGui.openGui();
			config.setObject(object);
			callback.run();
		}
	}

	private final ConfigQuestObject config;
	private final IOpenableGui callbackGui;
	private final Runnable callback;

	public GuiSelectQuestObject(ConfigQuestObject c, IOpenableGui g, Runnable cb)
	{
		StringBuilder builder = new StringBuilder(I18n.format("ftbquests.gui.select_quest_object"));
		builder.append(" [");

		boolean first = true;

		for (QuestObjectType type : c.getTypes())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				builder.append(", ");
			}

			builder.append(I18n.format(type.getTranslationKey()));
		}

		builder.append(']');
		setTitle(builder.toString());
		setHasSearchBox(true);
		setBorder(1, 1, 1);
		config = c;
		callbackGui = g;
		callback = cb;
	}

	@Override
	public void addButtons(Panel panel)
	{
		if (config.isValid(QuestObjectType.FILE))
		{
			panel.add(new ButtonQuestObject(panel, ClientQuestFile.INSTANCE));
		}

		if (config.isValid(QuestObjectType.VARIABLE))
		{
			for (QuestVariable variable : ClientQuestFile.INSTANCE.variables)
			{
				panel.add(new ButtonQuestObject(panel, variable));
			}
		}

		if (config.isValid(QuestObjectType.REWARD_TABLE))
		{
			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				panel.add(new ButtonQuestObject(panel, table));
			}
		}

		boolean addChapters = config.isValid(QuestObjectType.CHAPTER);
		boolean addQuests = config.isValid(QuestObjectType.QUEST);
		boolean addTasks = config.isValid(QuestObjectType.TASK);
		boolean addRewards = config.isValid(QuestObjectType.REWARD);

		for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
		{
			if (addChapters)
			{
				panel.add(new ButtonQuestObject(panel, chapter));
			}

			for (Quest quest : chapter.quests)
			{
				if (addQuests)
				{
					panel.add(new ButtonQuestObject(panel, quest));
				}

				if (addTasks)
				{
					for (QuestTask task : quest.tasks)
					{
						panel.add(new ButtonQuestObject(panel, task));
					}
				}

				if (addRewards)
				{
					for (QuestReward reward : quest.rewards)
					{
						panel.add(new ButtonQuestObject(panel, reward));
					}
				}
			}
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}