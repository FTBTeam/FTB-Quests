package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.tile.TileRewardCollector;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * @author LatvianModder
 */
public class RenderRewardCollector extends TileEntitySpecialRenderer<TileRewardCollector>
{
	@Override
	public void render(TileRewardCollector screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (!ClientQuestFile.exists())
		{
			return;
		}
	}
}