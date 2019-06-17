package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonTask extends Button
{
	private final QuestTaskData taskData;

	public ButtonTask(Panel panel, QuestTaskData d)
	{
		super(panel);
		taskData = d;
		setSize(panel.width, 8);
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (taskData.task.quest.chapter.file.chapters.size() > 1)
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + taskData.task.quest.chapter.getYellowDisplayName());
		}

		list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + taskData.task.quest.getYellowDisplayName());
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + taskData.getProgressString() + " / " + taskData.task.getMaxProgressString());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		taskData.task.onButtonClicked(true);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		int r = (int) (taskData.getRelativeProgress() * width / 100L);

		if (r > 0L)
		{
			GuiQuestChest.TEXTURE.withUVfromCoords(0, r >= width ? 199 : 190, r, 8, 256, 256).draw(x, y, r, 8);
		}

		taskData.task.getIcon().draw(x + 1, y, 8, 8);
		theme.drawString(taskData.task.getTitle(), x + 11, y, theme.getContentColor(getWidgetType()), Theme.SHADOW);
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse()
	{
		return taskData.task.getIngredient();
	}
}