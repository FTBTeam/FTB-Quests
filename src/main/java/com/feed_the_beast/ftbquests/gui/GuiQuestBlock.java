package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageSelectQuestTask;
import com.feed_the_beast.ftbquests.quest.IProgressing;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.client.gui.GuiScreen;

/**
 * @author LatvianModder
 */
public class GuiQuestBlock extends GuiBase
{
	private final ContainerQuestBlock container;

	public GuiQuestBlock(ContainerQuestBlock c)
	{
		container = c;
	}

	@Override
	public void addWidgets()
	{
		QuestTask task = container.tile.getTask();

		if (task == null)
		{
			return;
		}

		SimpleTextButton button = new SimpleTextButton(this, task.getDisplayName(), task.getIcon())
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				if (container.tile.canEdit())
				{
					if (isShiftKeyDown())
					{
						new GuiSelectQuestTask(container.tile.getPos()).openGui();
						return;
					}

					QuestTask task = container.tile.getTask();
					QuestTask task1 = task.parent.tasks.get((task.key.index + 1) % task.parent.tasks.size());

					if (task != task1)
					{
						container.tile.setTask(task1);
						new MessageSelectQuestTask(container.tile.getPos(), task1.key).sendToServer();
						getGui().refreshWidgets();
					}
				}
			}
		};

		button.setPos((width - button.width) / 2, 7);

		add(button);
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container);
	}

	@Override
	public void drawBackground()
	{
		super.drawBackground();

		QuestTask task = container.tile.getTask();

		if (task == null)
		{
			return;
		}

		int ax = getAX();
		int ay = getAY();

		int max = task.getMaxProgress();
		int progress = Math.min(max, task.getProgress(ClientQuestList.INSTANCE));

		String s = IProgressing.getCompletionString(progress, max);
		int sw = getStringWidth(s);

		Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 60, sw + 8, 13);
		Color4I.LIGHT_BLUE.draw(ax + (width - sw - 6) / 2, ay + 61, (sw + 6) * progress / max, 11);

		drawString(s, ax + width / 2, ay + 63, Color4I.WHITE, CENTERED);
	}
}