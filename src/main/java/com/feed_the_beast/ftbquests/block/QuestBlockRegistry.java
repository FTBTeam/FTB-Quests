package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.block.ItemBlockBase;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BooleanSupplier;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
@GameRegistry.ObjectHolder(FTBQuests.MOD_ID)
public class QuestBlockRegistry
{
	public static final Block QUEST_BLOCK = Blocks.AIR;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		if (FTBQuestsConfig.general.add_block)
		{
			event.getRegistry().register(new BlockQuest(FTBQuests.MOD_ID, "quest_block"));
			GameRegistry.registerTileEntity(TileQuest.class, FTBQuests.MOD_ID + ":quest_block");
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		if (FTBQuestsConfig.general.add_block)
		{
			event.getRegistry().register(new ItemBlockBase(QUEST_BLOCK));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event)
	{
		if (FTBQuestsConfig.general.add_block)
		{
			ClientUtils.registerModel(QUEST_BLOCK);
		}
	}

	public static class RecipeCondition implements IConditionFactory
	{
		@Override
		public BooleanSupplier parse(JsonContext context, JsonObject json)
		{
			return () -> FTBQuestsConfig.general.add_block;
		}
	}
}