package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.block.BlockQuest;
import com.feed_the_beast.ftbquests.block.ItemBlockQuest;
import com.feed_the_beast.ftbquests.block.TileQuest;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
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

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(new BlockQuest(FTBQuests.MOD_ID, "quest_block"));
		GameRegistry.registerTileEntity(TileQuest.class, new ResourceLocation(FTBQuests.MOD_ID, "quest_block"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().register(new ItemBlockQuest(QUEST_BLOCK));
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(QUEST_BLOCK), 0, new ModelResourceLocation(QUEST_BLOCK.getRegistryName(), "normal"));
	}
}