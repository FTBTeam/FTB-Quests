package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.events.FTBLibPreInitRegistryEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.AdminPanelAction;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.block.BlockProgressDetector;
import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.block.BlockProgressScreenPart;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.block.BlockScreenPart;
import com.feed_the_beast.ftbquests.block.ItemBlockProgressDetector;
import com.feed_the_beast.ftbquests.block.ItemBlockProgressScreen;
import com.feed_the_beast.ftbquests.block.ItemBlockScreen;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.tile.TileProgressDetector;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.ftbquests.util.FTBQuestsWorldData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsEventHandler
{
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(
				new BlockScreen().setRegistryName("screen").setTranslationKey(FTBQuests.MOD_ID + ".screen"),
				new BlockScreenPart().setRegistryName("screen_part").setTranslationKey(FTBQuests.MOD_ID + ".screen"),
				new BlockProgressDetector().setRegistryName("progress_detector").setTranslationKey(FTBQuests.MOD_ID + ".progress_detector"),
				new BlockProgressScreen().setRegistryName("progress_screen").setTranslationKey(FTBQuests.MOD_ID + ".progress_screen"),
				new BlockProgressScreenPart().setRegistryName("progress_screen_part").setTranslationKey(FTBQuests.MOD_ID + ".progress_screen")
		);

		GameRegistry.registerTileEntity(TileScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core"));
		GameRegistry.registerTileEntity(TileScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part"));
		GameRegistry.registerTileEntity(TileProgressDetector.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_detector"));
		GameRegistry.registerTileEntity(TileProgressScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_core"));
		GameRegistry.registerTileEntity(TileProgressScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_part"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(
				new ItemBlockScreen(FTBQuestsItems.SCREEN).setRegistryName("screen"),
				new ItemBlockProgressDetector(FTBQuestsItems.PROGRESS_DETECTOR).setRegistryName("progress_detector"),
				new ItemBlockProgressScreen(FTBQuestsItems.PROGRESS_SCREEN).setRegistryName("progress_screen")
		);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		ServerQuestFile.INSTANCE.sync(event.getPlayer().getPlayer());
	}

	@SubscribeEvent
	public static void onRegistryEvent(FTBLibPreInitRegistryEvent event)
	{
		event.getRegistry().registerAdminPanelAction(new AdminPanelAction(FTBQuests.MOD_ID, "edit_settings", GuiIcons.BOOK_RED, 0)
		{
			@Override
			public Type getType(ForgePlayer player, NBTTagCompound data)
			{
				return Type.fromBoolean(player.hasPermission(FTBQuests.PERM_EDIT_SETTINGS));
			}

			@Override
			public void onAction(ForgePlayer player, NBTTagCompound data)
			{
				ConfigGroup main = ConfigGroup.newGroup("admin_panel");
				main.setDisplayName(getTitle());
				ConfigGroup group = main.getGroup("ftbquests.edit_settings");
				group.add("editing_mode", FTBQuestsWorldData.INSTANCE.editingMode, new ConfigBoolean(false));
				FTBLibAPI.editServerConfig(player.getPlayer(), main, FTBQuestsWorldData.INSTANCE);
			}
		});

		event.getRegistry().registerConfigValueProvider(ConfigQuestObject.ID, () -> new ConfigQuestObject(""));
	}
}