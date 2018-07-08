package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
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
		if (ClientQuestList.exists() && event.getButton().id.equals(QUESTS_BUTTON))
		{
			int r = 0;

			for (QuestChapter chapter : ClientQuestList.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestList.INSTANCE))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestList.INSTANCE.isRewardClaimed(ClientUtils.MC.player, reward))
							{
								r++;
							}
						}
					}
				}
			}

			if (r > 0)
			{
				event.setText(Integer.toString(r));
			}
		}
	}

	@SubscribeEvent
	public static void onCustomClick(CustomClickEvent event)
	{
		if (event.getID().getResourceDomain().equals(FTBQuests.MOD_ID))
		{
			switch (event.getID().getResourcePath())
			{
				case "open_gui":
					if (ClientQuestList.exists())
					{
						ClientQuestList.INSTANCE.openQuestGui();
					}

					break;
			}

			event.setCanceled(true);
		}
	}
}