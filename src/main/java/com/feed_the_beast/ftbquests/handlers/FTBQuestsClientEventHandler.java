package com.feed_the_beast.ftbquests.handlers;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID, value = Side.CLIENT)
public class FTBQuestsClientEventHandler
{
	private static final ResourceLocation QUESTS_BUTTON = new ResourceLocation(FTBQuests.MOD_ID, "quests");

	@SubscribeEvent
	public static void onCustomSidebarButtonText(CustomSidebarButtonTextEvent event)
	{
		/*
		if (event.getButton().id.equals(QUESTS_BUTTON))
		{
			int i = 0;

			if (FTBGuidesClient.questTreeGui != null)
			{
				for (QuestChapter chapter : ClientQuestList.INSTANCE.chapters.values())
				{
					i += chapter.quests.size();
				}
			}

			if (i > 0)
			{
				event.setText(Integer.toString(i));
			}
		}
		*/
	}

	@SubscribeEvent
	public static void onCustomClick(CustomClickEvent event)
	{
		if (event.getID().getResourceDomain().equals(FTBQuests.MOD_ID))
		{
			switch (event.getID().getResourcePath())
			{
				case "open_gui":
					FTBQuestsClient.openQuestGui();
					break;
			}

			event.setCanceled(true);
		}
	}
}