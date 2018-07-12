package com.feed_the_beast.ftbquests;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
@Config(modid = FTBQuests.MOD_ID, category = "", name = "ftbquests/config")
public class FTBQuestsConfig
{
	@Config.LangKey("stat.generalButton")
	public static final General general = new General();

	public static class General
	{
		@Config.Comment("Enabled editing mode.")
		public boolean editing_mode = false;

		@Config.Comment("Allow to obtain Quest Blocks from the Quests GUI.")
		public boolean allow_take_quest_blocks = true;
	}

	public static boolean sync()
	{
		ConfigManager.sync(FTBQuests.MOD_ID, Config.Type.INSTANCE);
		return true;
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(FTBQuests.MOD_ID))
		{
			sync();
		}
	}
}