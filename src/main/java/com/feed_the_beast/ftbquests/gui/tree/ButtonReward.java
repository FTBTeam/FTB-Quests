package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.platform.GlStateManager;
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
	public final GuiQuests treeGui;
	public final Reward reward;

	public ButtonReward(Panel panel, Reward r)
	{
		super(panel, r.getTitle(), r.getIcon());
		treeGui = (GuiQuests) panel.getGui();
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
		if (!ClientQuestFile.exists() || !ClientQuestFile.INSTANCE.self.isComplete(reward.quest))
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
			if (ClientQuestFile.exists())
			{
				reward.onButtonClicked(this, ClientQuestFile.INSTANCE.self.getClaimType(reward).canClaim());
			}
		}
		else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit())
		{
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuests.addObjectMenuItems(contextMenu, getGui(), reward);
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
		int bs = h >= 32 ? 32 : 16;
		drawBackground(theme, x, y, w, h);
		drawIcon(theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0F, 0F, 500F);
		boolean completed = false;

		if (!ClientQuestFile.exists())
		{
			GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
		}
		else if (ClientQuestFile.INSTANCE.self.getClaimType(reward).isClaimed())
		{
			ThemeProperties.CHECK_ICON.get().draw(x + w - 9, y + 1, 8, 8);
			completed = true;
		}
		else if (ClientQuestFile.INSTANCE.self.isComplete(reward.quest))
		{
			ThemeProperties.ALERT_ICON.get().draw(x + w - 9, y + 1, 8, 8);
		}

		GlStateManager.popMatrix();

		if (!completed)
		{
			String s = reward.getButtonText();

			if (!s.isEmpty())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translatef(x + 19F - theme.getStringWidth(s) / 2F, y + 15F, 500F);
				GlStateManager.scalef(0.5F, 0.5F, 1F);
				theme.drawString(s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				GlStateManager.popMatrix();
			}
		}
	}
}