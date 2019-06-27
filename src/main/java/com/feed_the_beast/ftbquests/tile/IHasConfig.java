package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public interface IHasConfig extends IConfigCallback
{
	void editConfig(EntityPlayerMP player, boolean editor);
}