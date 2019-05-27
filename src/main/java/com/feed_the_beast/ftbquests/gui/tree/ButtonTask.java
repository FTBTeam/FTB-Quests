package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonTask extends Button
{
	public final GuiQuestTree treeGui;
	public QuestTask task;

	public ButtonTask(Panel panel, QuestTask t)
	{
		super(panel, t.getDisplayName().getFormattedText(), GuiIcons.ACCEPT);
		treeGui = (GuiQuestTree) panel.getGui();
		task = t;
		setSize(18, 18);
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
			task.onButtonClicked(!(task.invalid || treeGui.file.self == null || !task.quest.canStartTasks(treeGui.file.self) || task.isComplete(treeGui.file.self)));
		}
		else if (button.isRight() && treeGui.file.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), task);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse()
	{
		return task.getIngredient();
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(TextFormatting.DARK_GRAY + task.toString());
		}

		if (task.addTitleInMouseOverText())
		{
			list.add(getTitle());
		}

		QuestTaskData data;

		if (treeGui.file.self != null && task.quest.canStartTasks(treeGui.file.self))
		{
			data = treeGui.file.self.getQuestTaskData(task);
			long maxp = task.getMaxProgress();

			if (maxp > 1L)
			{
				if (task.hideProgressNumbers())
				{
					list.add(TextFormatting.DARK_GREEN + "[" + data.getRelativeProgress() + "%]");
				}
				else
				{
					String max = isShiftKeyDown() ? Long.toUnsignedString(maxp) : task.getMaxProgressString();
					String prog = isShiftKeyDown() ? Long.toUnsignedString(data.getProgress()) : data.getProgressString();

					if (maxp < 100L)
					{
						list.add(TextFormatting.DARK_GREEN + (data.getProgress() > maxp ? max : prog) + " / " + max);
					}
					else
					{
						list.add(TextFormatting.DARK_GREEN + (data.getProgress() > maxp ? max : prog) + " / " + max + " [" + data.getRelativeProgress() + "%]");
					}

				}
			}
		}
		else
		{
			data = null;
			//list.add(TextFormatting.DARK_GRAY + "[0%]");
		}

		task.addMouseOverText(list, data);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			super.drawBackground(theme, x, y, w, h);
		}
	}

	@Override
	public void drawIcon(Theme theme, int x, int y, int w, int h)
	{
		task.drawGUI(treeGui.file.self == null ? null : treeGui.file.self.getQuestTaskData(task), x, y, w, h);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (treeGui.file.self != null && task.isComplete(treeGui.file.self))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 500F);
			FTBQuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
			GlStateManager.popMatrix();
		}
	}
}