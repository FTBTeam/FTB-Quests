package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;

/**
 * @author LatvianModder
 */
public class PanelOtherButtonsTop extends PanelOtherButtons
{
	public PanelOtherButtonsTop(Panel panel)
	{
		super(panel);
	}

	@Override
	public void addWidgets()
	{
		int r = treeGui.file.getUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), treeGui.file.self, false);

		if (r > 0)
		{
			add(new ButtonCollectRewards(this, r));
		}

		if (Loader.isModLoaded("ftbguides"))
		{
			add(new ButtonOpenGuides(this));
		}

		if (!treeGui.file.emergencyItems.isEmpty() && (treeGui.file.self != null || treeGui.file.canEdit()))
		{
			add(new ButtonEmergencyItems(this));
		}

		add(new ButtonWiki(this));
	}

	@Override
	public void alignWidgets()
	{
		setPosAndSize(treeGui.width - width, 1, width, align(WidgetLayout.VERTICAL));
	}
}