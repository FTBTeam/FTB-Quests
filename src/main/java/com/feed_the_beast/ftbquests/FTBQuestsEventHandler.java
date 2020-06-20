package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftblib.events.FTBLibPreInitRegistryEvent;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.block.BlockDetector;
import com.feed_the_beast.ftbquests.block.BlockLootCrateOpener;
import com.feed_the_beast.ftbquests.block.BlockLootCrateStorage;
import com.feed_the_beast.ftbquests.block.BlockProgressDetector;
import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.block.BlockProgressScreenPart;
import com.feed_the_beast.ftbquests.block.BlockQuestBarrier;
import com.feed_the_beast.ftbquests.block.BlockQuestChest;
import com.feed_the_beast.ftbquests.block.BlockTaskScreen;
import com.feed_the_beast.ftbquests.block.BlockTaskScreenPart;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.block.ItemBlockBarrier;
import com.feed_the_beast.ftbquests.block.ItemBlockDetector;
import com.feed_the_beast.ftbquests.block.ItemBlockProgressScreen;
import com.feed_the_beast.ftbquests.block.ItemBlockTaskScreen;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemCustomIcon;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.item.ItemQuestBook;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.reward.AdvancementReward;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.CommandReward;
import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.LootReward;
import com.feed_the_beast.ftbquests.quest.reward.RandomReward;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.reward.ToastReward;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import com.feed_the_beast.ftbquests.quest.reward.XPReward;
import com.feed_the_beast.ftbquests.quest.task.AdvancementTask;
import com.feed_the_beast.ftbquests.quest.task.CheckmarkTask;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.FluidTask;
import com.feed_the_beast.ftbquests.quest.task.ForgeEnergyTask;
import com.feed_the_beast.ftbquests.quest.task.InteractionTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.KillTask;
import com.feed_the_beast.ftbquests.quest.task.LocationTask;
import com.feed_the_beast.ftbquests.quest.task.ObservationTask;
import com.feed_the_beast.ftbquests.quest.task.StatTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.XPTask;
import com.feed_the_beast.ftbquests.tile.TileLootCrateOpener;
import com.feed_the_beast.ftbquests.tile.TileLootCrateStorage;
import com.feed_the_beast.ftbquests.tile.TileProgressDetector;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import com.feed_the_beast.ftbquests.tile.TileQuestBarrier;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.ftbquests.util.RayMatcher;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Iterator;
import java.util.List;

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

	// Registry Events //

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(
				withName(new BlockTaskScreen(), "screen"),
				withName(new BlockTaskScreenPart(), "screen_part"),
				withName(new BlockProgressDetector(), "progress_detector"),
				withName(new BlockDetector(), "detector"),
				withName(new BlockProgressScreen(), "progress_screen"),
				withName(new BlockProgressScreenPart(), "progress_screen_part"),
				withName(new BlockQuestChest(), "chest"),
				withName(new BlockLootCrateStorage(), "loot_crate_storage"),
				withName(new BlockLootCrateOpener(), "loot_crate_opener"),
				withName(new BlockQuestBarrier(), "barrier")
		);

		GameRegistry.registerTileEntity(TileTaskScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core"));
		GameRegistry.registerTileEntity(TileTaskScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part"));
		GameRegistry.registerTileEntity(TileProgressDetector.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_detector"));
		GameRegistry.registerTileEntity(TileProgressScreenCore.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_core"));
		GameRegistry.registerTileEntity(TileProgressScreenPart.class, new ResourceLocation(FTBQuests.MOD_ID, "progress_screen_part"));
		GameRegistry.registerTileEntity(TileQuestChest.class, new ResourceLocation(FTBQuests.MOD_ID, "chest"));
		GameRegistry.registerTileEntity(TileLootCrateStorage.class, new ResourceLocation(FTBQuests.MOD_ID, "loot_crate_storage"));
		GameRegistry.registerTileEntity(TileLootCrateOpener.class, new ResourceLocation(FTBQuests.MOD_ID, "loot_crate_opener"));
		GameRegistry.registerTileEntity(TileQuestBarrier.class, new ResourceLocation(FTBQuests.MOD_ID, "barrier"));

		for (BlockDetector.Variant variant : BlockDetector.Variant.VALUES)
		{
			GameRegistry.registerTileEntity(variant.clazz, new ResourceLocation(FTBQuests.MOD_ID, variant.getName() + "_detector"));
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(
				new ItemBlockTaskScreen(FTBQuestsBlocks.SCREEN).setRegistryName("screen"),
				new ItemBlock(FTBQuestsBlocks.PROGRESS_DETECTOR).setRegistryName("progress_detector"),
				new ItemBlockDetector(FTBQuestsBlocks.DETECTOR).setRegistryName("detector"),
				new ItemBlockProgressScreen(FTBQuestsBlocks.PROGRESS_SCREEN).setRegistryName("progress_screen"),
				new ItemBlock(FTBQuestsBlocks.CHEST).setRegistryName("chest"),
				new ItemBlock(FTBQuestsBlocks.LOOT_CRATE_STORAGE).setRegistryName("loot_crate_storage"),
				new ItemBlock(FTBQuestsBlocks.LOOT_CRATE_OPENER).setRegistryName("loot_crate_opener"),
				new ItemBlockBarrier(FTBQuestsBlocks.BARRIER).setRegistryName("barrier"),

				withName(new ItemQuestBook(), "book"),
				withName(new ItemLootCrate(), "lootcrate"),
				withName(new ItemCustomIcon(), "custom_icon")
		);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsTasks.ITEM = new TaskType(ItemTask::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:items/diamond")),
				FTBQuestsTasks.FLUID = new TaskType(FluidTask::new).setRegistryName("fluid").setIcon(Icon.getIcon(FluidRegistry.WATER.getStill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME)).toString()).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))),
				FTBQuestsTasks.FORGE_ENERGY = new TaskType(ForgeEnergyTask::new).setRegistryName("forge_energy").setIcon(Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString()))),
				FTBQuestsTasks.CUSTOM = new TaskType(CustomTask::new).setRegistryName("custom").setIcon(GuiIcons.COLOR_HSB),
				FTBQuestsTasks.XP = new TaskType(XPTask::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsTasks.DIMENSION = new TaskType(DimensionTask::new).setRegistryName("dimension").setIcon(Icon.getIcon("minecraft:blocks/portal")),
				FTBQuestsTasks.STAT = new TaskType(StatTask::new).setRegistryName("stat").setIcon(Icon.getIcon("minecraft:items/iron_sword")),
				FTBQuestsTasks.KILL = new TaskType(KillTask::new).setRegistryName("kill").setIcon(Icon.getIcon("minecraft:items/diamond_sword")),
				FTBQuestsTasks.LOCATION = new TaskType(LocationTask::new).setRegistryName("location").setIcon(Icon.getIcon("minecraft:items/compass_00")),
				FTBQuestsTasks.CHECKMARK = new TaskType(CheckmarkTask::new).setRegistryName("checkmark").setIcon(GuiIcons.ACCEPT_GRAY),
				FTBQuestsTasks.ADVANCEMENT = new TaskType(AdvancementTask::new).setRegistryName("advancement").setIcon(Icon.getIcon("minecraft:items/wheat")),
				FTBQuestsTasks.OBSERVATION = new TaskType(ObservationTask::new).setRegistryName("observation").setIcon(GuiIcons.ART),
				FTBQuestsTasks.INTERACTION = new TaskType(InteractionTask::new).setRegistryName("interaction").setIcon(GuiIcons.BELL)
		);

		FTBQuests.PROXY.setTaskGuiProviders();
	}

	@SubscribeEvent
	public static void registerRewards(RegistryEvent.Register<RewardType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsRewards.ITEM = new RewardType(ItemReward::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:items/diamond")),
				FTBQuestsRewards.CHOICE = new RewardType(ChoiceReward::new).setRegistryName("choice").setIcon(GuiIcons.COLOR_RGB).setExcludeFromListRewards(true),
				FTBQuestsRewards.RANDOM = new RewardType(RandomReward::new).setRegistryName("random").setIcon(GuiIcons.DICE).setExcludeFromListRewards(true),
				FTBQuestsRewards.LOOT = new RewardType(LootReward::new).setRegistryName("loot").setIcon(GuiIcons.MONEY_BAG).setExcludeFromListRewards(true),
				FTBQuestsRewards.COMMAND = new RewardType(CommandReward::new).setRegistryName("command").setIcon(Icon.getIcon("minecraft:blocks/command_block_back")),
				FTBQuestsRewards.CUSTOM = new RewardType(CustomReward::new).setRegistryName("custom").setIcon(GuiIcons.COLOR_HSB),
				FTBQuestsRewards.XP = new RewardType(XPReward::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsRewards.XP_LEVELS = new RewardType(XPLevelsReward::new).setRegistryName("xp_levels").setIcon(Icon.getIcon("minecraft:items/experience_bottle")),
				FTBQuestsRewards.ADVANCEMENT = new RewardType(AdvancementReward::new).setRegistryName("advancement").setIcon(Icon.getIcon("minecraft:items/wheat")),
				FTBQuestsRewards.TOAST = new RewardType(ToastReward::new).setRegistryName("toast").setIcon(Icon.getIcon("minecraft:items/sign"))
		);

		FTBQuests.PROXY.setRewardGuiProviders();
	}

	// FTB Library Events //

	@SubscribeEvent
	public static void registerFTBLib(FTBLibPreInitRegistryEvent event)
	{
		event.getRegistry().registerConfigValueProvider(ConfigQuestObject.ID, () -> new ConfigQuestObject(ClientQuestFile.INSTANCE, 0, IntSets.EMPTY_SET));
	}

	// Game Events //

	private static List<KillTask> killTasks = null;
	private static List<Task> autoSubmitTasks = null;
	private static List<InteractionTask> interactionTasks = null;

	@SubscribeEvent
	public static void onFileCacheClear(ClearFileCacheEvent event)
	{
		killTasks = null;
		autoSubmitTasks = null;
		interactionTasks = null;
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();

		if (e.world.isRemote || e instanceof EntityPlayer)
		{
			return;
		}

		if (ServerQuestFile.INSTANCE == null || !ServerQuestFile.INSTANCE.dropLootCrates)
		{
			return;
		}

		LootCrate crate = ServerQuestFile.INSTANCE.getRandomLootCrate(e, e.world.rand);

		if (crate != null)
		{
			EntityItem ei = new EntityItem(e.world, e.posX, e.posY, e.posZ, crate.createStack());
			ei.setPickupDelay(10);
			event.getDrops().add(ei);
		}
	}

	@SubscribeEvent
	public static void onPlayerKill(LivingDeathEvent event)
	{
		if (event.getSource().getTrueSource() instanceof EntityPlayerMP)
		{
			if (killTasks == null)
			{
				killTasks = ServerQuestFile.INSTANCE.collect(KillTask.class);
			}

			if (killTasks.isEmpty())
			{
				return;
			}

			EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();

			QuestData data = ServerQuestFile.INSTANCE.getData(player);

			if (data == null)
			{
				return;
			}

			for (KillTask task : killTasks)
			{
				TaskData taskData = data.getTaskData(task);

				if (taskData.progress < task.getMaxProgress() && task.quest.canStartTasks(data))
				{
					((KillTask.Data) taskData).kill(event.getEntityLiving());
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote && ServerQuestFile.INSTANCE != null)
		{
			if (autoSubmitTasks == null)
			{
				autoSubmitTasks = ServerQuestFile.INSTANCE.collect(Task.class, o -> o instanceof Task && ((Task) o).autoSubmitOnPlayerTick() > 0);
			}

			if (autoSubmitTasks.isEmpty())
			{
				return;
			}

			QuestData data = ServerQuestFile.INSTANCE.getData(event.player);

			if (data == null)
			{
				return;
			}

			if (autoSubmitTasks == null || autoSubmitTasks.isEmpty())
			{
				return;
			}

			long t = event.player.world.getTotalWorldTime();

			for (Task task : autoSubmitTasks)
			{
				long d = task.autoSubmitOnPlayerTick();

				if (d > 0L && t % d == 0L)
				{
					TaskData taskData = data.getTaskData(task);

					if (!taskData.isComplete() && task.quest.canStartTasks(data))
					{
						taskData.submitTask((EntityPlayerMP) event.player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
	{
		if (event.player instanceof EntityPlayerMP && !event.crafting.isEmpty())
		{
			FTBQuestsInventoryListener.detect((EntityPlayerMP) event.player, event.crafting);
		}
	}

	@SubscribeEvent
	public static void onItemSmelt(PlayerEvent.ItemSmeltedEvent event)
	{
		if (event.player instanceof EntityPlayerMP && !event.smelting.isEmpty())
		{
			FTBQuestsInventoryListener.detect((EntityPlayerMP) event.player, event.smelting);
		}
	}

	/**
	 * Modified version of CoFH Soulbound enchantment code
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
	{
		if (!event.isWasDeath())
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		EntityPlayer oldPlayer = event.getOriginal();

		if (player instanceof FakePlayer || player.world.getGameRules().getBoolean("keepInventory"))
		{
			return;
		}

		for (int i = 0; i < oldPlayer.inventory.mainInventory.size(); i++)
		{
			ItemStack stack = oldPlayer.inventory.mainInventory.get(i);

			if (stack.getItem() == FTBQuestsItems.BOOK && player.addItemStackToInventory(stack))
			{
				oldPlayer.inventory.mainInventory.set(i, ItemStack.EMPTY);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlayerDropsEvent(PlayerDropsEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();

		if (player instanceof FakePlayer || player.world.getGameRules().getBoolean("keepInventory"))
		{
			return;
		}

		Iterator<EntityItem> iterator = event.getDrops().listIterator();

		while (iterator.hasNext())
		{
			EntityItem drop = iterator.next();
			ItemStack stack = drop.getItem();

			if (stack.getItem() == FTBQuestsItems.BOOK && player.addItemStackToInventory(stack))
			{
				iterator.remove();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.getWorld().isRemote)
		{
			return;
		}

		if (interactionTasks == null)
		{
			interactionTasks = ServerQuestFile.INSTANCE.collect(InteractionTask.class);
		}

		if (interactionTasks.isEmpty())
		{
			return;
		}

		QuestData data = ServerQuestFile.INSTANCE.getData(event.getEntityPlayer());

		if (data == null)
		{
			return;
		}

		RayMatcher.Data matcherData = RayMatcher.Data.get(event.getWorld().getBlockState(event.getPos()), event.getWorld().getTileEntity(event.getPos()), null);

		for (InteractionTask task : interactionTasks)
		{
			TaskData taskData = data.getTaskData(task);

			if (!taskData.isComplete() && task.matcher.matches(matcherData) && task.quest.canStartTasks(data))
			{
				taskData.submitTask((EntityPlayerMP) event.getEntityPlayer());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onEntityRightClick(PlayerInteractEvent.EntityInteract event)
	{
		if (event.getWorld().isRemote)
		{
			return;
		}

		if (interactionTasks == null)
		{
			interactionTasks = ServerQuestFile.INSTANCE.collect(InteractionTask.class);
		}

		if (interactionTasks.isEmpty())
		{
			return;
		}

		QuestData data = ServerQuestFile.INSTANCE.getData(event.getEntityPlayer());

		if (data == null)
		{
			return;
		}

		RayMatcher.Data matcherData = RayMatcher.Data.get(BlockUtils.AIR_STATE, null, event.getTarget());

		for (InteractionTask task : interactionTasks)
		{
			TaskData taskData = data.getTaskData(task);

			if (!taskData.isComplete() && task.matcher.matches(matcherData) && task.quest.canStartTasks(data))
			{
				taskData.submitTask((EntityPlayerMP) event.getEntityPlayer());
			}
		}
	}
}