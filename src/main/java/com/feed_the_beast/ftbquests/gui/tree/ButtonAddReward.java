package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonAddReward extends SimpleTextButton
{
	private final Quest quest;

	public ButtonAddReward(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		quest = q;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		List<ContextMenuItem> contextMenu = new ArrayList<>();

		for (QuestRewardType type : QuestRewardType.getRegistry())
		{
			contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
				GuiHelper.playClickSound();
				type.getGuiProvider().openCreationGui(this, quest);
			}));
		}

		getGui().openContextMenu(contextMenu);
	}
}