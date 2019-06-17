package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonAddTask extends Button
{
	public final Quest quest;

	public ButtonAddTask(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), FTBQuestsTheme.ADD);
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestTaskType type : QuestTaskType.getRegistry())
		{
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
				GuiHelper.playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, task -> {
					NBTTagCompound extra = new NBTTagCompound();
					extra.setString("type", type.getTypeForNBT());
					new MessageCreateObject(task, extra).sendToServer();
				});
			}));
		}

		getGui().openContextMenu(contextMenu);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			super.drawBackground(theme, x, y, w, h);
		}
	}
}