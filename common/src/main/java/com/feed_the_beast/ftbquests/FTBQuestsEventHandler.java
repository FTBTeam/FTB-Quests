package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.item.ItemQuestBook;
import com.feed_the_beast.ftbquests.quest.PlayerData;
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
import com.feed_the_beast.ftbquests.quest.task.BiomeTask;
import com.feed_the_beast.ftbquests.quest.task.CheckmarkTask;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.FluidTask;
import com.feed_the_beast.ftbquests.quest.task.ForgeEnergyTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.KillTask;
import com.feed_the_beast.ftbquests.quest.task.LocationTask;
import com.feed_the_beast.ftbquests.quest.task.ObservationTask;
import com.feed_the_beast.ftbquests.quest.task.StatTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.XPTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.EntityEvent;
import me.shedaniel.architectury.event.events.LifecycleEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TickEvent;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Iterator;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBQuestsEventHandler
{
	private List<KillTask> killTasks = null;
	private List<Task> autoSubmitTasks = null;

	public void init()
	{
		LifecycleEvent.SERVER_BEFORE_START.register(this::serverAboutToStart);
		CommandRegistrationEvent.EVENT.register(this::registerCommands);
        LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
        LifecycleEvent.SERVER_STOPPING.register(this::serverStopped);
        LifecycleEvent.SERVER_WORLD_SAVE.register(this::worldSaved);
		FTBQuestsItems.register();
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TaskType.class, this::registerTasks);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RewardType.class, this::registerRewards);
        ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
		PlayerEvent.PLAYER_JOIN.register(this::playerLoggedIn);
        EntityEvent.LIVING_DEATH.register(this::playerKill);
		TickEvent.PLAYER_POST.register(this::playerTick);
		MinecraftForge.EVENT_BUS.register(this::livingDrops);
		PlayerEvent.CRAFT_ITEM.register(this::itemCrafted);
		PlayerEvent.SMELT_ITEM.register(this::itemSmelted);
		PlayerEvent.PLAYER_CLONE.register(this::cloned);
		MinecraftForge.EVENT_BUS.register(EventPriority.HIGH, this::dropsEvent);
		MinecraftForge.EVENT_BUS.register(this::changedDimension);
		PlayerEvent.OPEN_MENU.register(this::containerOpened);
	}

	private void serverAboutToStart(MinecraftServer server)
	{
		ServerQuestFile.INSTANCE = new ServerQuestFile(server);
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection)
	{
		FTBQuestsCommands.register(dispatcher);
	}

	private void serverStarted(MinecraftServer server)
	{
		ServerQuestFile.INSTANCE.load();
	}

	private void serverStopped(MinecraftServer server)
	{
		ServerQuestFile.INSTANCE.unload();
		ServerQuestFile.INSTANCE = null;
	}

	private void worldSaved(ServerLevel level)
	{
		ServerQuestFile.INSTANCE.saveNow();
	}

	private void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsTasks.ITEM = new TaskType(ItemTask::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:item/diamond")),
				FTBQuestsTasks.FLUID = new TaskType(FluidTask::new).setRegistryName("fluid").setIcon(Icon.getIcon(Fluids.WATER.getAttributes().getStillTexture(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME)).toString()).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()))),
				FTBQuestsTasks.FORGE_ENERGY = new TaskType(ForgeEnergyTask::new).setRegistryName("forge_energy").setIcon(Icon.getIcon(ForgeEnergyTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(ForgeEnergyTask.FULL_TEXTURE.toString()))),
				FTBQuestsTasks.CUSTOM = new TaskType(CustomTask::new).setRegistryName("custom").setIcon(GuiIcons.COLOR_HSB),
				FTBQuestsTasks.XP = new TaskType(XPTask::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:item/experience_bottle")),
				FTBQuestsTasks.DIMENSION = new TaskType(DimensionTask::new).setRegistryName("dimension").setIcon(Icon.getIcon("minecraft:block/nether_portal")),
				FTBQuestsTasks.STAT = new TaskType(StatTask::new).setRegistryName("stat").setIcon(Icon.getIcon("minecraft:item/iron_sword")),
				FTBQuestsTasks.KILL = new TaskType(KillTask::new).setRegistryName("kill").setIcon(Icon.getIcon("minecraft:item/diamond_sword")),
				FTBQuestsTasks.LOCATION = new TaskType(LocationTask::new).setRegistryName("location").setIcon(Icon.getIcon("minecraft:item/compass_00")),
				FTBQuestsTasks.CHECKMARK = new TaskType(CheckmarkTask::new).setRegistryName("checkmark").setIcon(GuiIcons.ACCEPT_GRAY),
				FTBQuestsTasks.ADVANCEMENT = new TaskType(AdvancementTask::new).setRegistryName("advancement").setIcon(Icon.getIcon("minecraft:item/wheat")),
				FTBQuestsTasks.OBSERVATION = new TaskType(ObservationTask::new).setRegistryName("observation").setIcon(GuiIcons.ART),
				FTBQuestsTasks.BIOME = new TaskType(BiomeTask::new).setRegistryName("biome").setIcon(Icon.getIcon("minecraft:blocks/sapling_oak"))
		);

		FTBQuests.PROXY.setTaskGuiProviders();
	}

	private void registerRewards(RegistryEvent.Register<RewardType> event)
	{
		event.getRegistry().registerAll(
				FTBQuestsRewards.ITEM = new RewardType(ItemReward::new).setRegistryName("item").setIcon(Icon.getIcon("minecraft:item/diamond")),
				FTBQuestsRewards.CHOICE = new RewardType(ChoiceReward::new).setRegistryName("choice").setIcon(GuiIcons.COLOR_RGB).setExcludeFromListRewards(true),
				FTBQuestsRewards.RANDOM = new RewardType(RandomReward::new).setRegistryName("random").setIcon(GuiIcons.DICE).setExcludeFromListRewards(true),
				FTBQuestsRewards.LOOT = new RewardType(LootReward::new).setRegistryName("loot").setIcon(GuiIcons.MONEY_BAG).setExcludeFromListRewards(true),
				FTBQuestsRewards.COMMAND = new RewardType(CommandReward::new).setRegistryName("command").setIcon(Icon.getIcon("minecraft:block/command_block_back")),
				FTBQuestsRewards.CUSTOM = new RewardType(CustomReward::new).setRegistryName("custom").setIcon(GuiIcons.COLOR_HSB),
				FTBQuestsRewards.XP = new RewardType(XPReward::new).setRegistryName("xp").setIcon(Icon.getIcon("minecraft:item/experience_bottle")),
				FTBQuestsRewards.XP_LEVELS = new RewardType(XPLevelsReward::new).setRegistryName("xp_levels").setIcon(Icon.getIcon("minecraft:item/experience_bottle")),
				FTBQuestsRewards.ADVANCEMENT = new RewardType(AdvancementReward::new).setRegistryName("advancement").setIcon(Icon.getIcon("minecraft:item/wheat")),
				FTBQuestsRewards.TOAST = new RewardType(ToastReward::new).setRegistryName("toast").setIcon(Icon.getIcon("minecraft:item/oak_sign"))
		);

		FTBQuests.PROXY.setRewardGuiProviders();
	}

	private void fileCacheClear(ClearFileCacheEvent event)
	{
		killTasks = null;
		autoSubmitTasks = null;
	}

	private void playerLoggedIn(ServerPlayer player)
	{
		ServerQuestFile.INSTANCE.onLoggedIn(player);
	}

	private InteractionResult playerKill(LivingEntity entity, DamageSource source)
	{
		if (source.getDirectEntity() instanceof ServerPlayer && !PlayerHooks.isFake((Player) source.getDirectEntity()))
		{
			if (killTasks == null)
			{
				killTasks = ServerQuestFile.INSTANCE.collect(KillTask.class);
			}

			if (killTasks.isEmpty())
			{
				return InteractionResult.PASS;
			}

			ServerPlayer player = (ServerPlayer) source.getDirectEntity();
			PlayerData data = ServerQuestFile.INSTANCE.getData(player);

			for (KillTask task : killTasks)
			{
				TaskData taskData = data.getTaskData(task);

				if (taskData.progress < task.getMaxProgress() && data.canStartTasks(task.quest))
				{
					((KillTask.Data) taskData).kill(entity);
				}
			}
		}
        
        return InteractionResult.PASS;
	}

	private void playerTick(Player player)
	{
		if (!player.level.isClientSide && ServerQuestFile.INSTANCE != null && !PlayerHooks.isFake(player))
		{
			if (autoSubmitTasks == null)
			{
				autoSubmitTasks = ServerQuestFile.INSTANCE.collect(Task.class, o -> o instanceof Task && ((Task) o).autoSubmitOnPlayerTick() > 0);
			}

			if (autoSubmitTasks == null || autoSubmitTasks.isEmpty())
			{
				return;
			}

			PlayerData data = ServerQuestFile.INSTANCE.getData(player);
			long t = player.level.getGameTime();

			for (Task task : autoSubmitTasks)
			{
				long d = task.autoSubmitOnPlayerTick();

				if (d > 0L && t % d == 0L)
				{
					TaskData taskData = data.getTaskData(task);

					if (!taskData.isComplete() && data.canStartTasks(task.quest))
					{
						taskData.submitTask((ServerPlayer) player);
					}
				}
			}
		}
	}

	private void livingDrops(LivingDropsEvent event)
	{
		LivingEntity e = event.getEntityLiving();

		if (e.level.isClientSide || e instanceof Player)
		{
			return;
		}

		if (ServerQuestFile.INSTANCE == null || !ServerQuestFile.INSTANCE.dropLootCrates)
		{
			return;
		}

		LootCrate crate = ServerQuestFile.INSTANCE.getRandomLootCrate(e, e.level.random);

		if (crate != null)
		{
			ItemEntity ei = new ItemEntity(e.level, e.getX(), e.getY(), e.getZ(), crate.createStack());
			ei.setPickUpDelay(10);
			event.getDrops().add(ei);
		}
	}

	private void itemCrafted(Player player, ItemStack crafted, Container inventory)
	{
		if (player instanceof ServerPlayer && !crafted.isEmpty())
		{
			FTBQuestsInventoryListener.detect((ServerPlayer) player, crafted, 0);
		}
	}

	private void itemSmelted(Player player, ItemStack smelted)
	{
		if (player instanceof ServerPlayer && !smelted.isEmpty())
		{
			FTBQuestsInventoryListener.detect((ServerPlayer) player, smelted, 0);
		}
	}

	private void cloned(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame)
	{
		newPlayer.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener((ServerPlayer) newPlayer));

		if (wonGame)
		{
			return;
		}

		if (PlayerHooks.isFake(newPlayer) || newPlayer.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
		{
			return;
		}

		for (int i = 0; i < oldPlayer.inventory.items.size(); i++)
		{
			ItemStack stack = oldPlayer.inventory.items.get(i);

			if (stack.getItem() == FTBQuestsItems.BOOK && newPlayer.addItem(stack))
			{
				oldPlayer.inventory.items.set(i, ItemStack.EMPTY);
			}
		}
	}

	private void dropsEvent(LivingDropsEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer))
		{
			return;
		}

		ServerPlayer player = (ServerPlayer) event.getEntity();

		if (player instanceof FakePlayer || player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
		{
			return;
		}

		Iterator<ItemEntity> iterator = event.getDrops().iterator();

		while (iterator.hasNext())
		{
			ItemEntity drop = iterator.next();
			ItemStack stack = drop.getItem();

			if (stack.getItem() == FTBQuestsItems.BOOK && player.addItem(stack))
			{
				iterator.remove();
			}
		}
	}

	private void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		if (event.getPlayer() instanceof ServerPlayer && !(event.getPlayer() instanceof FakePlayer))
		{
			PlayerData data = ServerQuestFile.INSTANCE.getData(event.getPlayer());

			for (DimensionTask task : ServerQuestFile.INSTANCE.collect(DimensionTask.class))
			{
				data.getTaskData(task).submitTask((ServerPlayer) event.getPlayer());
			}
		}
	}

	private void containerOpened(Player player, AbstractContainerMenu menu)
	{
		if (player instanceof ServerPlayer && !PlayerHooks.isFake(player) && !(menu instanceof InventoryMenu))
		{
			menu.addSlotListener(new FTBQuestsInventoryListener((ServerPlayer) player));
		}
	}
}