package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonTask extends SimpleTextButton
{
	public final GuiQuestTree treeGui;
	public QuestTask task;

	public ButtonTask(Panel panel, QuestTask t)
	{
		super(panel, t.getDisplayName().getFormattedText(), GuiIcons.ACCEPT);
		treeGui = (GuiQuestTree) panel.getGui();
		task = t;
	}

	@Override
	public boolean hasIcon()
	{
		return true;
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (isMouseOver())
		{
			if (button.isRight() || getWidgetType() != WidgetType.DISABLED)
			{
				onClicked(button);
			}

			return true;
		}

		return false;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (button.isLeft())
		{
			GuiHelper.playClickSound();
			task.onButtonClicked();
		}
		else if (button.isRight() && treeGui.questFile.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			treeGui.addObjectMenuItems(contextMenu, getGui(), task);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(TextFormatting.DARK_GRAY + task.getID());
		}

		QuestTaskData data;

		if (treeGui.questFile.self != null && task.quest.canStartTasks(treeGui.questFile.self))
		{
			data = treeGui.questFile.self.getQuestTaskData(task);

			if (task.hideProgressNumbers())
			{
				list.add(TextFormatting.DARK_GREEN + "[" + data.getRelativeProgress() + "%]");
			}
			else
			{
				list.add(TextFormatting.DARK_GREEN + data.getProgressString() + " / " + task.getMaxProgressString() + " [" + data.getRelativeProgress() + "%]");
			}
		}
		else
		{
			data = null;
			list.add(TextFormatting.DARK_GRAY + "[0%]");
		}

		task.addMouseOverText(list, data);
	}

	@Override
	public WidgetType getWidgetType()
	{
		if (task.invalid || treeGui.questFile.self == null || !task.quest.canStartTasks(treeGui.questFile.self))
		{
			return WidgetType.DISABLED;
		}

		return super.getWidgetType();
	}

	@Override
	public void drawIcon(Theme theme, int x, int y, int w, int h)
	{
		task.drawGUI(treeGui.questFile.self == null ? null : treeGui.questFile.self.getQuestTaskData(task), x, y, w, h);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (treeGui.questFile.self != null && task.isComplete(treeGui.questFile.self))
		{
			QuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
		}
	}
}