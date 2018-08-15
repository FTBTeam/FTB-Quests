package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftblib.lib.util.Folders;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ModifyBaseFileLocationEvent;
import io.sommers.packmode.api.PackModeAPI;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class PackModeIntegration
{
	@SubscribeEvent
	public static void modifyBaseFileLocation(ModifyBaseFileLocationEvent event)
	{
		if (Loader.isModLoaded("packmode"))
		{
			modifyBaseFileLocation0(event);
		}
	}

	private static void modifyBaseFileLocation0(ModifyBaseFileLocationEvent event)
	{
		event.setFile(new File(Folders.getMinecraft(), "questpacks/" + PackModeAPI.getInstance().getCurrentPackMode() + ".nbt"));
	}
}