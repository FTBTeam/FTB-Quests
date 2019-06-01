package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class ButtonCollectRewards extends ButtonTab
{
	public ButtonCollectRewards(Panel panel, int r)
	{
		super(panel, I18n.format("ftbquests.gui.collect_rewards", TextFormatting.GOLD.toString() + r), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/collect_rewards.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		if (ClientQuestFile.existsWithTeam())
		{
			new GuiRewardNotifications().openGui();
			new MessageClaimAllRewards().sendToServer();
		}
	}
}