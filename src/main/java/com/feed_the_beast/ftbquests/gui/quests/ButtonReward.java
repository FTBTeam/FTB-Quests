package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.ContextMenuItem;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
	public ITextComponent getTitle()
	{
		if (reward.isTeamReward())
		{
			return super.getTitle().deepCopy().mergeStyle(TextFormatting.BLUE);
		}

		return super.getTitle();
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
		if (isShiftKeyDown() && isCtrlKeyDown())
		{
			list.add(new StringTextComponent(reward.toString()).mergeStyle(TextFormatting.DARK_GRAY));
		}

		if (reward.addTitleInMouseOverText())
		{
			list.add(getTitle());
		}

		if (reward.isTeamReward())
		{
			list.add(new StringTextComponent("").append(new StringTextComponent("[").append(new TranslationTextComponent("ftbquests.reward.team_reward")).appendString("]")).mergeStyle(TextFormatting.BLUE));
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
	public void drawBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			super.drawBackground(matrixStack, theme, x, y, w, h);
		}
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		int bs = h >= 32 ? 32 : 16;
		drawBackground(matrixStack, theme, x, y, w, h);
		drawIcon(matrixStack, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		matrixStack.push();
		matrixStack.translate(0F, 0F, 500F);
		RenderSystem.enableBlend();
		boolean completed = false;

		if (!ClientQuestFile.exists())
		{
			GuiIcons.CLOSE.draw(matrixStack, x + w - 9, y + 1, 8, 8);
		}
		else if (ClientQuestFile.INSTANCE.self.getClaimType(reward).isClaimed())
		{
			ThemeProperties.CHECK_ICON.get().draw(matrixStack, x + w - 9, y + 1, 8, 8);
			completed = true;
		}
		else if (ClientQuestFile.INSTANCE.self.isComplete(reward.quest))
		{
			ThemeProperties.ALERT_ICON.get().draw(matrixStack, x + w - 9, y + 1, 8, 8);
		}

		matrixStack.pop();

		if (!completed)
		{
			String s = reward.getButtonText();

			if (!s.isEmpty())
			{
				matrixStack.push();
				matrixStack.translate(x + 19F - theme.getStringWidth(s) / 2F, y + 15F, 500F);
				matrixStack.scale(0.5F, 0.5F, 1F);
				theme.drawString(matrixStack, s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				matrixStack.pop();
			}
		}
	}
}