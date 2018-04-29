package com.feed_the_beast.ftbquests.cmd;

import com.feed_the_beast.ftblib.events.RegisterFTBCommandsEvent;
import com.feed_the_beast.ftblib.lib.EventHandler;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
@EventHandler
public class FTBQuestsCommands
{
	@SubscribeEvent
	public static void registerCommands(RegisterFTBCommandsEvent event)
	{
		if (FTBQuestsConfig.general.editing_mode)
		{
			event.add(new CmdEditQuests());
		}
	}
}