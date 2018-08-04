package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbquests.block.BlockQuest;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.block.BlockScreenPart;
import com.feed_the_beast.ftbquests.block.ItemBlockQuest;
import com.feed_the_beast.ftbquests.block.ItemBlockScreen;
import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.block.TileScreen;
import com.feed_the_beast.ftbquests.block.TileScreenPart;
import com.feed_the_beast.ftbquests.client.RenderScreen;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
@GameRegistry.ObjectHolder(FTBQuests.MOD_ID)
public class FTBQuestsItems
{
	public static final Block QUEST_BLOCK = Blocks.AIR;
	public static final Block SCREEN = Blocks.AIR;
	public static final Block SCREEN_PART = Blocks.AIR;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(
				new BlockQuest(FTBQuests.MOD_ID, "quest_block"),
				new BlockScreen(FTBQuests.MOD_ID, "screen"),
				new BlockScreenPart(FTBQuests.MOD_ID, "screen_part")
		);

		GameRegistry.registerTileEntity(TileQuest.class, new ResourceLocation(FTBQuests.MOD_ID, "quest_block"));
		GameRegistry.registerTileEntity(TileScreen.class, new ResourceLocation(FTBQuests.MOD_ID, "screen"));
		GameRegistry.registerTileEntity(TileScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(
				new ItemBlockQuest(QUEST_BLOCK),
				new ItemBlockScreen(SCREEN),
				new ItemBlockBase(SCREEN_PART)
		);
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(QUEST_BLOCK), 0, new ModelResourceLocation(QUEST_BLOCK.getRegistryName(), "normal"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(SCREEN), 0, new ModelResourceLocation(SCREEN.getRegistryName(), "facing=north"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(SCREEN_PART), 0, new ModelResourceLocation(SCREEN_PART.getRegistryName(), "facing=north"));

		ClientRegistry.bindTileEntitySpecialRenderer(TileScreen.class, new RenderScreen());
	}
}