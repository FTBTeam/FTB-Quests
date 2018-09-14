package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends Button
{
	public QuestReward reward = null;

	public ButtonReward(Panel panel)
	{
		super(panel);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (reward != null && reward.quest.isComplete(ClientQuestFile.INSTANCE.self) && !ClientQuestFile.INSTANCE.isRewardClaimed(reward))
		{
			new MessageClaimReward(reward.uid).sendToServer();
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (reward == null)
		{
			list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.output"));
			list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.output_desc"));
		}
		else
		{
			List<String> tooltip = reward.stack.getTooltip(ClientUtils.MC.player, ITooltipFlag.TooltipFlags.NORMAL);
			list.add(reward.stack.getRarity().rarityColor + tooltip.get(0));

			for (int i = 1; i < tooltip.size(); i++)
			{
				list.add(TextFormatting.GRAY + tooltip.get(i));
			}
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (reward != null)
		{
			GuiHelper.drawItem(reward.stack, x, y, true, Icon.EMPTY);
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			Color4I.WHITE.withAlpha(150).draw(x, y, w, h);
			GlStateManager.popMatrix();
		}
	}
}