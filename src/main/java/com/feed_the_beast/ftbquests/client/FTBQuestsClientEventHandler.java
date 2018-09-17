package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.CustomSidebarButtonTextEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.SCREEN, 0, new ModelResourceLocation(FTBQuestsItems.SCREEN.getRegistryName(), "facing=north"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.PROGRESS_DETECTOR, 0, new ModelResourceLocation(FTBQuestsItems.PROGRESS_DETECTOR.getRegistryName(), "normal"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.PROGRESS_SCREEN, 0, new ModelResourceLocation(FTBQuestsItems.PROGRESS_SCREEN.getRegistryName(), "facing=north"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.CHEST, 0, new ModelResourceLocation(FTBQuestsItems.CHEST.getRegistryName(), "facing=north"));

		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.BOOK, 0, new ModelResourceLocation(FTBQuestsItems.BOOK.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.XP_VIAL, 0, new ModelResourceLocation(FTBQuestsItems.XP_VIAL.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.SCRIPT, 0, new ModelResourceLocation(FTBQuestsItems.SCRIPT.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.COMMON_LOOTCRATE, 0, new ModelResourceLocation(FTBQuestsItems.COMMON_LOOTCRATE.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.UNCOMMON_LOOTCRATE, 0, new ModelResourceLocation(FTBQuestsItems.UNCOMMON_LOOTCRATE.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.RARE_LOOTCRATE, 0, new ModelResourceLocation(FTBQuestsItems.RARE_LOOTCRATE.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.EPIC_LOOTCRATE, 0, new ModelResourceLocation(FTBQuestsItems.EPIC_LOOTCRATE.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.LEGENDARY_LOOTCRATE, 0, new ModelResourceLocation(FTBQuestsItems.LEGENDARY_LOOTCRATE.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.MISSING, 0, new ModelResourceLocation(FTBQuestsItems.MISSING.getRegistryName(), "inventory"));

		ClientRegistry.bindTileEntitySpecialRenderer(TileScreenCore.class, new RenderScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileProgressScreenCore.class, new RenderProgressScreen());
	}

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
					if (quest.isComplete(ClientQuestFile.INSTANCE.self))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.isRewardClaimed(reward))
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