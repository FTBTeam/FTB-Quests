package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
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
public class ButtonAddTask extends SimpleTextButton
{
	public final Quest quest;

	public ButtonAddTask(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		quest = q;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestTaskType type : QuestTaskType.getRegistry())
		{
			contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
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
}