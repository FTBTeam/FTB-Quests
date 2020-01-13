package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;

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
		super(panel, I18n.format("gui.add"), ThemeProperties.ADD_ICON.get());
		quest = q;
		setSize(18, 18);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (TaskType type : TaskType.getRegistry())
		{
			contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
				playClickSound();
				type.getGuiProvider().openCreationGui(this, quest, task -> {
					CompoundNBT extra = new CompoundNBT();
					extra.putString("type", type.getTypeForNBT());
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