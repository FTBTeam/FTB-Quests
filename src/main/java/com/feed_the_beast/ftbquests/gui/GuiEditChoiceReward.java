package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetVerticalSpace;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.I18n;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEditChoiceReward extends GuiButtonListBase
{
	private class ButtonEditChoiceReward extends SimpleTextButton
	{
		private ButtonEditChoiceReward(Panel panel)
		{
			super(panel, I18n.format("gui.settings"), GuiIcons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			new MessageEditObject(choiceReward.uid).sendToServer();
		}
	}

	private class ButtonAcceptChoiceReward extends SimpleTextButton
	{
		private ButtonAcceptChoiceReward(Panel panel)
		{
			super(panel, I18n.format("gui.accept"), GuiIcons.ACCEPT);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			closeGui();
			callback.run();
		}
	}

	private class ButtonAddChoiceReward extends SimpleTextButton
	{
		private ButtonAddChoiceReward(Panel panel)
		{
			super(panel, I18n.format("gui.add"), GuiIcons.ADD);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ObjectArrayList<>();

			for (QuestRewardType type : QuestRewardType.getRegistry())
			{
				if (!type.getExcludeFromListRewards())
				{
					contextMenu.add(new ContextMenuItem(type.getDisplayName().getFormattedText(), type.getIcon(), () -> {
						GuiHelper.playClickSound();
						type.getGuiProvider().openCreationGui(this, choiceReward.quest, reward -> {
							choiceReward.rewards.add(reward);
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class ButtonChoiceReward extends SimpleTextButton
	{
		private final QuestReward reward;

		private ButtonChoiceReward(Panel panel, QuestReward r)
		{
			super(panel, r.getDisplayName().getFormattedText(), r.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			reward.addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ObjectArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, reward::onEditButtonClicked));
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> {
				choiceReward.rewards.remove(reward);
				GuiEditChoiceReward.this.refreshWidgets();
			}).setYesNo(I18n.format("delete_item", reward.getDisplayName().getFormattedText())));
			//GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), reward.reward);
			GuiEditChoiceReward.this.openContextMenu(contextMenu);
		}
	}

	private final ChoiceReward choiceReward;
	private final Runnable callback;

	public GuiEditChoiceReward(ChoiceReward r, Runnable c)
	{
		choiceReward = r;
		callback = c;
		setTitle(I18n.format("ftbquests.reward.ftbquests.choice"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		panel.add(new ButtonEditChoiceReward(panel));
		panel.add(new ButtonAcceptChoiceReward(panel));
		panel.add(new ButtonAddChoiceReward(panel));
		panel.add(new WidgetVerticalSpace(panel, 1));

		for (QuestReward r : choiceReward.rewards)
		{
			panel.add(new ButtonChoiceReward(panel, r));
		}
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}