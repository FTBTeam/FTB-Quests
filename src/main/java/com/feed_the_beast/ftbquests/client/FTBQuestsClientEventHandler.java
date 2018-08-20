package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
	public static TextureAtlasSprite inputBlockSprite;

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FTBQuestsItems.SCREEN), 0, new ModelResourceLocation(FTBQuestsItems.SCREEN.getRegistryName(), "facing=north"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FTBQuestsItems.PROGRESS_DETECTOR), 0, new ModelResourceLocation(FTBQuestsItems.PROGRESS_DETECTOR.getRegistryName(), "normal"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FTBQuestsItems.PROGRESS_SCREEN), 0, new ModelResourceLocation(FTBQuestsItems.PROGRESS_SCREEN.getRegistryName(), "facing=north"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FTBQuestsItems.CHEST), 0, new ModelResourceLocation(FTBQuestsItems.CHEST.getRegistryName(), "facing=north"));

		ClientRegistry.bindTileEntitySpecialRenderer(TileScreenCore.class, new RenderScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileProgressScreenCore.class, new RenderProgressScreen());
	}

	@SubscribeEvent
	public static void onCustomSidebarButtonText(CustomSidebarButtonTextEvent event)
	{
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.self != null && event.getButton().id.equals(QUESTS_BUTTON))
		{
			int r = 0;

			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestFile.INSTANCE.self))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.self.isRewardClaimed(ClientUtils.MC.player, reward))
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
			if (ClientQuestFile.exists())
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
					if (ClientQuestFile.exists())
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

	@SubscribeEvent
	public static void onTextureStitchPre(TextureStitchEvent.Pre event)
	{
		inputBlockSprite = event.getMap().registerSprite(new ResourceLocation(FTBQuests.MOD_ID, "blocks/screen_front_input"));
	}
}