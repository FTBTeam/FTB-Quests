package com.feed_the_beast.ftbquests.handlers;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.lib.EventHandler;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author LatvianModder
 */
@EventHandler(Side.CLIENT)
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
}