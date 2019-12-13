package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonRewards extends ButtonTab
{
	private final boolean unclaimedRewards;

	public ButtonRewards(Panel panel)
	{
		super(panel, "", ThemeProperties.COLLECT_REWARDS_ICON.get());
		title = treeGui.file.getTitle();
		unclaimedRewards = treeGui.file.self.hasUnclaimedRewards();
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();

		if (ClientQuestFile.exists())
		{
			new GuiRewardNotifications().openGui();
			new MessageClaimAllRewards().sendToServer();
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		super.addMouseOverText(list);

		if (unclaimedRewards)
		{
			list.add("");
			list.add(TextFormatting.GOLD + I18n.format("ftbquests.gui.collect_rewards"));
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		super.draw(theme, x, y, w, h);

		if (unclaimedRewards)
		{
			double s = w / 2D;//(int) (treeGui.getZoom() / 2 * quest.size);
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + w - s, y, 500);
			GlStateManager.scaled(s, s, 1D);
			ThemeProperties.ALERT_ICON.get(treeGui.file).draw(0, 0, 1, 1);
			GlStateManager.popMatrix();
		}
	}
}