package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.events.FTBLibPreInitRegistryEvent;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.block.BlockProgressDetector;
import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.block.BlockProgressScreenPart;
import com.feed_the_beast.ftbquests.block.BlockQuestChest;
import com.feed_the_beast.ftbquests.block.BlockTaskScreen;
import com.feed_the_beast.ftbquests.block.BlockTaskScreenPart;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.block.ItemBlockProgressScreen;
import com.feed_the_beast.ftbquests.block.ItemBlockScreen;
import com.feed_the_beast.ftbquests.item.ItemLootcrate;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.item.ItemQuestBook;
import com.feed_the_beast.ftbquests.item.LootRarity;
import com.feed_the_beast.ftbquests.quest.EntityLootTable;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.CommandReward;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.LootReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import com.feed_the_beast.ftbquests.quest.reward.XPReward;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.FluidTask;
import com.feed_the_beast.ftbquests.quest.task.ForgeEnergyTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.KillTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.quest.task.StatTask;
import com.feed_the_beast.ftbquests.quest.task.XPTask;
import com.feed_the_beast.ftbquests.tile.TileProgressDetector;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Collections;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsEventHandler
{
	private static Block withName(Block block, String name)
	{
		block.setCreativeTab(FTBQuests.TAB);
		block.setRegistryName(name);
		block.setTranslationKey(FTBQuests.MOD_ID + "." + name);
		return block;
	}

	private static Item withName(Item item, String name)
	{
		item.setCreativeTab(FTBQuests.TAB);
		item.setRegistryName(name);
		item.setTranslationKey(FTBQuests.MOD_ID + "." + name);
		return item;
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(
				withName(new BlockTaskScreen(), "screen"),
				withName(new BlockTaskScreenPart(), "screen_part"),
				withName(new BlockProgressDetector(), "progress_detector"),
				withName(new BlockProgressScreen(), "progress_screen"),
				withName(new BlockProgressScreenPart(), "progress_screen_part"),
				withName(new BlockQuestChest(), "chest")
		);

		GameRegistry.registerTileEntity(TileTaskScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core"));
		GameRegistry.registerTileEntity(TileTaskScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part"));
		GameRegistry.registerTileEntity(TileProgressDetector.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_detector"));
		GameRegistry.registerTileEntity(TileProgressScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_core"));
		GameRegistry.registerTileEntity(TileProgressScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_part"));
		GameRegistry.registerTileEntity(TileQuestChest.class, new ResourceLocation(FTBQuests.MOD_ID, "chest"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(
				new ItemBlockScreen(FTBQuestsBlocks.SCREEN).setRegistryName("screen"),
				new ItemBlock(FTBQuestsBlocks.PROGRESS_DETECTOR).setRegistryName("progress_detector"),
				new ItemBlockProgressScreen(FTBQuestsBlocks.PROGRESS_SCREEN).setRegistryName("progress_screen"),
				new ItemBlock(FTBQuestsBlocks.CHEST).setRegistryName("chest"),

				withName(new ItemQuestBook(), "book"),
				withName(new ItemLootcrate(LootRarity.COMMON), "common_lootcrate").setTranslationKey(FTBQuests.MOD_ID + ".lootcrate"),
				withName(new ItemLootcrate(LootRarity.UNCOMMON), "uncommon_lootcrate").setTranslationKey(FTBQuests.MOD_ID + ".lootcrate"),
				withName(new ItemLootcrate(LootRarity.RARE), "rare_lootcrate").setTranslationKey(FTBQuests.MOD_ID + ".lootcrate"),
				withName(new ItemLootcrate(LootRarity.EPIC), "epic_lootcrate").setTranslationKey(FTBQuests.MOD_ID + ".lootcrate"),
				withName(new ItemLootcrate(LootRarity.LEGENDARY), "legendary_lootcrate").setTranslationKey(FTBQuests.MOD_ID + ".lootcrate"),
				withName(new ItemMissing(), "missing")
		);
	}

	@SubscribeEvent
	public static void registerFTBLib(FTBLibPreInitRegistryEvent event)
	{
		event.getRegistry().registerConfigValueProvider(ConfigQuestObject.ID, () -> new ConfigQuestObject(null, null, Collections.emptyList()));
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsTasks.ITEM = new QuestTaskType(ItemTask::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:items/diamond")),
				FTBQuestsTasks.FLUID = new QuestTaskType(FluidTask::new).setRegistryName("fluid").setIcon(Icon.getIcon(FluidRegistry.WATER.getStill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME)).toString()).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))),
				FTBQuestsTasks.FORGE_ENERGY = new QuestTaskType(ForgeEnergyTask::new).setRegistryName("forge_energy").setIcon(Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString()))),
				FTBQuestsTasks.XP = new QuestTaskType(XPTask::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsTasks.DIMENSION = new QuestTaskType(DimensionTask::new).setRegistryName("dimension").setIcon(Icon.getIcon("minecraft:blocks/portal")),
				FTBQuestsTasks.STAT = new QuestTaskType(StatTask::new).setRegistryName("stat").setIcon(Icon.getIcon("minecraft:items/iron_sword")),
				FTBQuestsTasks.KILL = new QuestTaskType(KillTask::new).setRegistryName("kill").setIcon(Icon.getIcon("minecraft:items/diamond_sword"))
		);

		FTBQuests.PROXY.setTaskGuiProviders();
	}

	@SubscribeEvent
	public static void registerRewards(RegistryEvent.Register<QuestRewardType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsRewards.ITEM = new QuestRewardType(ItemReward::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:items/diamond")),
				FTBQuestsRewards.CHOICE = new QuestRewardType(ChoiceReward::new).setRegistryName("choice").setIcon(GuiIcons.COLOR_RGB).setExcludeFromListRewards(true),
				FTBQuestsRewards.RANDOM = new QuestRewardType(RandomReward::new).setRegistryName("random").setIcon(GuiIcons.DICE).setExcludeFromListRewards(true),
				FTBQuestsRewards.LOOT = new QuestRewardType(LootReward::new).setRegistryName("loot").setIcon(GuiIcons.MONEY_BAG).setExcludeFromListRewards(true),
				FTBQuestsRewards.XP = new QuestRewardType(XPReward::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsRewards.XP_LEVELS = new QuestRewardType(XPLevelsReward::new).setRegistryName("xp_levels").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsRewards.COMMAND = new QuestRewardType(CommandReward::new).setRegistryName("command").setIcon(Icon.getIcon("minecraft:blocks/command_block_back"))
		);

		FTBQuests.PROXY.setRewardGuiProviders();
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();

		if (e.world.isRemote || e instanceof EntityPlayer)
		{
			return;
		}

		if (!ServerQuestFile.INSTANCE.entityLootEnabled)
		{
			return;
		}

		EntityLootTable rt;

		if (!e.isNonBoss())
		{
			rt = ServerQuestFile.INSTANCE.entityLootBoss;
		}
		else if (e instanceof IMob)
		{
			rt = ServerQuestFile.INSTANCE.entityLootMonster;
		}
		else
		{
			rt = ServerQuestFile.INSTANCE.entityLootPassive;
		}

		LootRarity r = rt.getRarity(e.world.rand);

		if (r != null)
		{
			EntityItem ei = new EntityItem(e.world, e.posX, e.posY, e.posZ, new ItemStack(r.getItem()));
			ei.setPickupDelay(10);
			event.getDrops().add(ei);
		}
	}

	@SubscribeEvent
	public static void onPlayerKillEvent(LivingDeathEvent event)
	{
		if (event.getSource().getTrueSource() instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();
			ITeamData data = ServerQuestFile.INSTANCE.getData(FTBLibAPI.getTeamID(player.getUniqueID()));

			if (data == null)
			{
				return;
			}

			for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (hasKillTasks(quest) && quest.canStartTasks(data))
					{
						for (QuestTask task : quest.tasks)
						{
							if (task instanceof KillTask)
							{
								((KillTask.Data) data.getQuestTaskData(task)).kill(event.getEntityLiving());
							}
						}
					}
				}
			}
		}
	}

	private static boolean hasKillTasks(Quest quest)
	{
		for (QuestTask task : quest.tasks)
		{
			if (task instanceof KillTask)
			{
				return true;
			}
		}

		return false;
	}
}