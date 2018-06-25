package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageSelectQuestTask;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

/**
 * @author LatvianModder
 */
public class GuiSelectQuestTask extends GuiButtonListBase
{
	private final BlockPos pos;

	public GuiSelectQuestTask(BlockPos p)
	{
		pos = p;
		setTitle(I18n.format("tile.ftbquests.quest_block.select_chapter"));
		setHasSearchBox(true);
	}

	@Override
	public void addButtons(Panel panel)
	{
		for (QuestChapter chapter : ClientQuestList.INSTANCE.chapters.values())
		{
			panel.add(new SimpleTextButton(panel, chapter.title.getFormattedText(), chapter.icon)
			{
				@Override
				public void onClicked(MouseButton button)
				{
					GuiHelper.playClickSound();
					new GuiSelectQuestFromChapter(chapter).openGui();
				}
			});
		}
	}

	public class GuiSelectQuestFromChapter extends GuiButtonListBase
	{
		private final QuestChapter chapter;

		private GuiSelectQuestFromChapter(QuestChapter c)
		{
			chapter = c;
			setTitle(I18n.format("tile.ftbquests.quest_block.select_quest"));
			setHasSearchBox(true);
		}

		@Override
		public void addButtons(Panel panel)
		{
			for (Quest quest : chapter.quests.values())
			{
				panel.add(new SimpleTextButton(panel, quest.title.getFormattedText(), quest.icon)
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						new GuiSelectTaskFromQuest(quest).openGui();
					}
				});
			}
		}
	}

	public class GuiSelectTaskFromQuest extends GuiButtonListBase
	{
		private final Quest quest;

		private GuiSelectTaskFromQuest(Quest q)
		{
			quest = q;
			setTitle(I18n.format("tile.ftbquests.quest_block.select_task"));
			setHasSearchBox(true);
		}

		@Override
		public void addButtons(Panel panel)
		{
			for (QuestTask task : quest.tasks)
			{
				panel.add(new SimpleTextButton(panel, task.getDisplayName(), task.getIcon())
				{
					@Override
					public void onClicked(MouseButton button)
					{
						GuiHelper.playClickSound();
						new MessageSelectQuestTask(pos, task.key).sendToServer();
						getGui().closeGui(false);
					}
				});
			}
		}
	}
}