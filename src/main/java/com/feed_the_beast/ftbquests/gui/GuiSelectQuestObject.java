package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
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
		public final QuestObject object;

		public ButtonQuestObject(Panel panel, QuestObject o)
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
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + ((Quest) object).chapter.getDisplayName().getUnformattedText());
			}
			else if (object instanceof QuestTask)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + ((QuestTask) object).quest.chapter.getDisplayName().getUnformattedText());
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + QuestObjectType.QUEST.getColor() + ((QuestTask) object).quest.getDisplayName().getUnformattedText());
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			config.setObject(object);
			callback.openGui();
		}
	}

	public final ConfigQuestObject config;
	public final IOpenableGui callback;

	public GuiSelectQuestObject(ConfigQuestObject c, IOpenableGui g)
	{
		setTitle(I18n.format("ftbquests.gui.select_quest_object"));
		setHasSearchBox(true);
		config = c;
		callback = g;
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

		boolean addChapters = config.isValid(QuestObjectType.CHAPTER);
		boolean addQuests = config.isValid(QuestObjectType.QUEST);
		boolean addTasks = config.isValid(QuestObjectType.TASK);

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
			}
		}
	}
}