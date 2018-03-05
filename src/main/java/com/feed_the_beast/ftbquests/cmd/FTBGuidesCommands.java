package com.feed_the_beast.ftbquests.cmd;

import com.feed_the_beast.ftblib.events.RegisterFTBClientCommandsEvent;
import com.feed_the_beast.ftblib.events.RegisterFTBCommandsEvent;
import com.feed_the_beast.ftblib.lib.EventHandler;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
@EventHandler
public class FTBGuidesCommands
{
	@SubscribeEvent
	public static void registerCommands(RegisterFTBCommandsEvent event)
	{
		if (FTBQuestsConfig.general.editing_mode)
		{
			event.add(new CmdEditQuests());
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerClientCommands(RegisterFTBClientCommandsEvent event)
	{
		event.add(new CmdQuests());
	}
}