package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.SidebarButtonCreatedEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemLootCrateNew;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
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

		ModelResourceLocation lootCrateModel = new ModelResourceLocation(FTBQuestsItems.LOOTCRATE.getRegistryName(), "#inventory");
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.COMMON_LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.UNCOMMON_LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.RARE_LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.EPIC_LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.LEGENDARY_LOOTCRATE, 0, lootCrateModel);
		ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.MISSING, 0, new ModelResourceLocation(FTBQuestsItems.MISSING.getRegistryName(), "inventory"));

		ClientRegistry.bindTileEntitySpecialRenderer(TileTaskScreenCore.class, new RenderTaskScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileProgressScreenCore.class, new RenderProgressScreen());
	}

	public static void postInit()
	{
		ItemColors c = Minecraft.getMinecraft().getItemColors();

		c.registerItemColorHandler((stack, tintIndex) -> {
			LootCrate crate = ItemLootCrateNew.getCrate(null, stack);
			return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.color.rgb());
		}, FTBQuestsItems.LOOTCRATE);

		c.registerItemColorHandler((stack, tintIndex) -> 0xFF92999A, FTBQuestsItems.COMMON_LOOTCRATE);
		c.registerItemColorHandler((stack, tintIndex) -> 0xFF37AA69, FTBQuestsItems.UNCOMMON_LOOTCRATE);
		c.registerItemColorHandler((stack, tintIndex) -> 0xFF0094FF, FTBQuestsItems.RARE_LOOTCRATE);
		c.registerItemColorHandler((stack, tintIndex) -> 0xFF8000FF, FTBQuestsItems.EPIC_LOOTCRATE);
		c.registerItemColorHandler((stack, tintIndex) -> 0xFFFFC147, FTBQuestsItems.LEGENDARY_LOOTCRATE);
	}

	@SubscribeEvent
	public static void onSidebarButtonCreated(SidebarButtonCreatedEvent event)
	{
		if (event.getButton().id.equals(QUESTS_BUTTON))
		{
			event.getButton().setCustomTextHandler(() ->
			{
				if (ClientQuestFile.exists())
				{
					if (!ClientQuestFile.existsWithTeam())
					{
						return "[!]";
					}

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
						return Integer.toString(r);
					}
				}

				return "";
			});

			event.getButton().setTooltipHandler(list -> {
				if (ClientQuestFile.exists() && !ClientQuestFile.existsWithTeam())
				{
					list.add(TextFormatting.GRAY + I18n.format("sidebar_button.ftbquests.quests.no_team"));
				}
			});
		}
	}

	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event)
	{
		if (FTBQuestsClient.KEY_QUESTS.isPressed())
		{
			ClientQuestFile.INSTANCE.openQuestGui(Minecraft.getMinecraft().player);
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
					ClientQuestFile.INSTANCE.openQuestGui(Minecraft.getMinecraft().player);
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