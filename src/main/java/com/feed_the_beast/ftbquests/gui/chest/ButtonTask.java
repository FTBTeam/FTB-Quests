package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageSubmitItems;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

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
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + TextFormatting.YELLOW + taskData.task.quest.chapter.getDisplayName().getFormattedText());
		}

		list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + TextFormatting.YELLOW + taskData.task.quest.getDisplayName().getFormattedText());
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.progress") + ": " + TextFormatting.BLUE + taskData.getProgressString() + " / " + taskData.task.getMaxProgressString());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new MessageSubmitItems(taskData.task.getID()).sendToServer();
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
		theme.drawString(taskData.task.getDisplayName().getFormattedText(), x + 11, y, theme.getContentColor(getWidgetType()), Theme.SHADOW);
	}
}