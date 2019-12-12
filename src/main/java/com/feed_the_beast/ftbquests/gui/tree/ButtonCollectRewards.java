package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class ButtonCollectRewards extends ButtonTab
{
	public ButtonCollectRewards(Panel panel, int r)
	{
		super(panel, I18n.format("ftbquests.gui.collect_rewards", TextFormatting.GOLD.toString() + r), ThemeProperties.COLLECT_REWARDS_ICON.get());
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
}