package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
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
		if (ClientQuestFile.existsWithTeam() && event.getButton().id.equals(QUESTS_BUTTON))
		{
			int r = 0;

			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestFile.INSTANCE))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.isRewardClaimed(ClientUtils.MC.player, reward))
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
	public static void onKeyEvent(InputEvent.KeyInputEvent event)
	{
		if (FTBQuestsClient.KEY_QUESTS.isPressed())
		{
			if (ClientQuestFile.existsWithTeam())
			{
				ClientQuestFile.INSTANCE.openQuestGui();
			}
			else
			{
				ClientUtils.MC.player.sendStatusMessage(new TextComponentTranslation("ftblib.lang.team.error.no_team"), true);
			}
		}
	}

	@SubscribeEvent
	public static void onCustomClick(CustomClickEvent event)
	{
		if (event.getID().getNamespace().equals(FTBQuests.MOD_ID))
		{
			switch (event.getID().getPath())
			{
				case "open_gui":
					if (ClientQuestFile.existsWithTeam())
					{
						ClientQuestFile.INSTANCE.openQuestGui();
					}
					else
					{
						ClientUtils.MC.player.sendStatusMessage(new TextComponentTranslation("ftblib.lang.team.error.no_team"), true);
					}

					break;
			}

			event.setCanceled(true);
		}
	}
}