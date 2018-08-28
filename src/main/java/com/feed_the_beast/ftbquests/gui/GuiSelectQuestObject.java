package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.resources.I18n;

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
			super(panel, o.getDisplayName().getFormattedText(), o.getIcon());
			object = o;

			switch (object.getObjectType())
			{
				case FILE:
				case CHAPTER:
					setHeight(24);
					break;
				case QUEST:
					setHeight(20);
					break;
				case TASK:
					setHeight(12);
					break;
			}
		}

		@Override
		public WidgetType getWidgetType()
		{
			if (!config.hasValidType(object.getObjectType()))
			{
				return WidgetType.DISABLED;
			}

			return super.getWidgetType();
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			config.setString(object.getID());
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
		if (config.hasValidType(QuestObjectType.FILE))
		{
			panel.add(new ButtonQuestObject(panel, ClientQuestFile.INSTANCE));
			panel.add(new Widget(panel).setPosAndSize(0, 0, 0, 4));
		}

		boolean addTasks = config.hasValidType(QuestObjectType.TASK);
		boolean addQuests = addTasks | config.hasValidType(QuestObjectType.QUEST);

		for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
		{
			panel.add(new ButtonQuestObject(panel, chapter));

			for (Quest quest : chapter.quests)
			{
				if (addQuests)
				{
					panel.add(new ButtonQuestObject(panel, quest));

					if (addTasks)
					{
						for (QuestTask task : quest.tasks)
						{
							panel.add(new ButtonQuestObject(panel, task));
						}

						panel.add(new Widget(panel).setPosAndSize(0, 0, 0, 4));
					}
				}
			}
		}
	}
}