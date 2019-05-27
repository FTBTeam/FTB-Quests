package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends Button
{
	public final QuestReward reward;

	public ButtonReward(Panel panel, QuestReward r)
	{
		super(panel, r.getDisplayName().getFormattedText(), r.getIcon());
		reward = r;
		setSize(18, 18);
	}

	@Override
	public String getTitle()
	{
		if (reward.isTeamReward())
		{
			return TextFormatting.BLUE + super.getTitle();
		}

		return super.getTitle();
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(TextFormatting.DARK_GRAY + reward.toString());
		}

		if (reward.addTitleInMouseOverText())
		{
			list.add(getTitle());
		}

		if (reward.isTeamReward())
		{
			list.add(TextFormatting.BLUE + "[" + I18n.format("ftbquests.reward.team_reward") + "]");
		}

		reward.addMouseOverText(list);
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
		if (!ClientQuestFile.existsWithTeam() || !(reward.parent instanceof QuestObject) || !((QuestObject) reward.parent).isComplete(ClientQuestFile.INSTANCE.self))
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
			if (ClientQuestFile.existsWithTeam() && reward.parent instanceof QuestObject && ((QuestObject) reward.parent).isComplete(ClientQuestFile.INSTANCE.self) && !ClientQuestFile.INSTANCE.isRewardClaimed(reward))
			{
				GuiHelper.playClickSound();
				reward.onButtonClicked();
			}
		}
		else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit())
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), reward);
			getGui().openContextMenu(contextMenu);
		}
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse()
	{
		return reward.getIngredient();
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
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 0F, 500F);

		if (!ClientQuestFile.existsWithTeam())
		{
			GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
		}
		else if (ClientQuestFile.INSTANCE.isRewardClaimed(reward))
		{
			FTBQuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
		}
		else if (reward.parent instanceof QuestObject && ((QuestObject) reward.parent).isComplete(ClientQuestFile.INSTANCE.self))
		{
			FTBQuestsTheme.ALERT.draw(x + w - 9, y + 1, 8, 8);
		}

		GlStateManager.popMatrix();
	}
}