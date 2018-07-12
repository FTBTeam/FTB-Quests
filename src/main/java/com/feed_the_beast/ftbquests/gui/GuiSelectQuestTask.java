package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageSelectTask;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.UnknownTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiSelectQuestTask extends GuiButtonListBase
{
	private final BlockPos pos;

	private class SelectorButton extends Button
	{
		protected String filterText = "";

		public SelectorButton(Panel panel, String n, Icon i)
		{
			super(panel, n, i);
			setHeight(12);
		}

		@Override
		public WidgetType getWidgetType()
		{
			return WidgetType.NORMAL;
		}

		@Override
		public void onClicked(MouseButton button)
		{
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
		}

		@Override
		public void draw()
		{
			int ax = getAX();
			int ay = getAY();
			getIcon().draw(ax + 2, ay + 2, 8, 8);
			drawString(getTitle(), ax + 12, ay + 2, getTheme().getContentColor(getWidgetType()), SHADOW);
		}
	}

	private class ChapterButton extends SelectorButton
	{
		public ChapterButton(Panel panel, QuestChapter c)
		{
			super(panel, TextFormatting.BOLD.toString() + TextFormatting.ITALIC + c.title, c.getIcon());
		}
	}

	private class QuestButton extends SelectorButton
	{
		public QuestButton(Panel panel, Quest c)
		{
			super(panel, c.title, c.getIcon());
			setX(10);
		}
	}

	private class TaskButton extends SelectorButton
	{
		protected final QuestTask task;

		public TaskButton(Panel panel, QuestTask t)
		{
			super(panel, "", t.getIcon());
			task = t;
			String s = t.getDisplayName();

			if (task.isComplete(ClientQuestList.INSTANCE))
			{
				s = TextFormatting.GREEN + s;
			}

			setTitle(s);
			setX(20);
			filterText = t.getDisplayName();
		}

		@Override
		public WidgetType getWidgetType()
		{
			return task.isInvalid() ? WidgetType.DISABLED : WidgetType.mouseOver(isMouseOver());
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (task instanceof UnknownTask)
			{
				list.add(((UnknownTask) task).getHover());
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			if (task.isInvalid() || task.isComplete(ClientQuestList.INSTANCE))
			{
				return;
			}

			GuiHelper.playClickSound();
			new MessageSelectTask(pos, task.id).sendToServer();
			getGui().closeGui(false);
		}
	}

	private class CombinedQuestTaskButton extends TaskButton
	{
		public CombinedQuestTaskButton(Panel panel, QuestTask t)
		{
			super(panel, t);
			String s = task.quest.title;

			if (task.isComplete(ClientQuestList.INSTANCE))
			{
				s = TextFormatting.GREEN + s;
			}

			setTitle(s);
			setX(10);

			filterText = task.getDisplayName() + " " + task.quest.title;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			list.add(task.getDisplayName());
			super.addMouseOverText(list);
		}
	}

	public GuiSelectQuestTask(BlockPos p)
	{
		pos = p;
		setTitle(I18n.format("tile.ftbquests.quest_block.select_task"));
		setHasSearchBox(true);
	}

	@Override
	public void addButtons(Panel panel)
	{
		for (QuestChapter chapter : ClientQuestList.INSTANCE.chapters)
		{
			panel.add(new ChapterButton(panel, chapter));

			for (Quest quest : chapter.quests)
			{
				if (quest.tasks.size() == 1)
				{
					panel.add(new CombinedQuestTaskButton(panel, quest.tasks.get(0)));
				}
				else
				{
					panel.add(new QuestButton(panel, quest));

					for (QuestTask task : quest.tasks)
					{
						panel.add(new TaskButton(panel, task));
					}
				}
			}
		}
	}

	@Override
	public String getFilterText(Widget widget)
	{
		return ((SelectorButton) widget).filterText;
	}
}