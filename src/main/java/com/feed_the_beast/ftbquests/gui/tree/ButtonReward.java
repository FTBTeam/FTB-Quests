package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.net.edit.MessageEditReward;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends SimpleTextButton
{
	public final QuestReward reward;

	public ButtonReward(Panel panel, QuestReward r)
	{
		super(panel, (r.team ? TextFormatting.BLUE.toString() : "") + r.stack.getDisplayName(), ItemIcon.getItemIcon(r.stack));
		reward = r;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(TextFormatting.DARK_GRAY + reward.toString());
		}

		if (reward.team)
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.reward.team_reward"));
		}
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
	public WidgetType getWidgetType()
	{
		if (!ClientQuestFile.existsWithTeam() || !reward.quest.canStartTasks(ClientQuestFile.INSTANCE.self))
		{
			return WidgetType.DISABLED;
		}

		return super.getWidgetType();
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (button.isLeft())
		{
			if (ClientQuestFile.existsWithTeam())
			{
				GuiHelper.playClickSound();
				new MessageClaimReward(reward.uid).sendToServer();
			}
		}
		else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> {
				ConfigValueInstance value = new ConfigValueInstance("item", ConfigGroup.DEFAULT, new ConfigItemStack(ItemStack.EMPTY)
				{
					@Override
					public ItemStack getStack()
					{
						return reward.stack;
					}

					@Override
					public void setStack(ItemStack stack)
					{
						reward.stack = stack;
						new MessageEditReward(reward.uid, reward.team, stack).sendToServer();
					}
				});

				new GuiSelectItemStack(value, this).openGui();
			}));

			contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.reward.team_reward") + ": " + (reward.team ? TextFormatting.BLUE + "true" : "false"), reward.team ? GuiIcons.LOCK : GuiIcons.LOCK_OPEN, () -> new MessageEditReward(reward.uid, !reward.team, reward.stack).sendToServer()));
			contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> new MessageEditReward(reward.uid, reward.team, ItemStack.EMPTY).sendToServer()).setYesNo(I18n.format("delete_item", reward.stack.getDisplayName())));
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (!ClientQuestFile.existsWithTeam())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
			GlStateManager.popMatrix();
		}
		else if (ClientQuestFile.INSTANCE.isRewardClaimed(reward))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			QuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
			GlStateManager.popMatrix();
		}
	}
}