package dev.ftb.mods.ftbquests;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import dev.ftb.mods.ftbquests.command.FTBQuestsCommands;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.DimensionTask;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskData;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.EntityEvent;
import me.shedaniel.architectury.event.events.LifecycleEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TickEvent;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBQuestsEventHandler {
	private List<KillTask> killTasks = null;
	private List<Task> autoSubmitTasks = null;

	public void init() {
		LifecycleEvent.SERVER_BEFORE_START.register(this::serverAboutToStart);
		CommandRegistrationEvent.EVENT.register(this::registerCommands);
		LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
		LifecycleEvent.SERVER_STOPPING.register(this::serverStopped);
		LifecycleEvent.SERVER_WORLD_SAVE.register(this::worldSaved);
		FTBQuestsBlocks.register();
		FTBQuestsItems.register();
		FTBQuestsBlockEntities.register();
		ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
		PlayerEvent.PLAYER_JOIN.register(this::playerLoggedIn);
		EntityEvent.LIVING_DEATH.register(this::playerKill);
		TickEvent.PLAYER_POST.register(this::playerTick);
		PlayerEvent.CRAFT_ITEM.register(this::itemCrafted);
		PlayerEvent.SMELT_ITEM.register(this::itemSmelted);
		PlayerEvent.PLAYER_CLONE.register(this::cloned);
		PlayerEvent.CHANGE_DIMENSION.register(this::changedDimension);
		PlayerEvent.OPEN_MENU.register(this::containerOpened);
	}

	private void serverAboutToStart(MinecraftServer server) {
		ServerQuestFile.INSTANCE = new ServerQuestFile(server);
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
		FTBQuestsCommands.register(dispatcher);
	}

	private void serverStarted(MinecraftServer server) {
		ServerQuestFile.INSTANCE.load();
	}

	private void serverStopped(MinecraftServer server) {
		ServerQuestFile.INSTANCE.saveNow();
		ServerQuestFile.INSTANCE.unload();
		ServerQuestFile.INSTANCE = null;
	}

	private void worldSaved(ServerLevel level) {
		if (ServerQuestFile.INSTANCE != null) {
			ServerQuestFile.INSTANCE.saveNow();
		}
	}

	private void fileCacheClear(ClearFileCacheEvent event) {
		killTasks = null;
		autoSubmitTasks = null;
	}

	private void playerLoggedIn(ServerPlayer player) {
		ServerQuestFile.INSTANCE.onLoggedIn(player);
	}

	private InteractionResult playerKill(LivingEntity entity, DamageSource source) {
		if (source.getDirectEntity() instanceof ServerPlayer && !PlayerHooks.isFake((Player) source.getDirectEntity())) {
			if (killTasks == null) {
				killTasks = ServerQuestFile.INSTANCE.collect(KillTask.class);
			}

			if (killTasks.isEmpty()) {
				return InteractionResult.PASS;
			}

			ServerPlayer player = (ServerPlayer) source.getDirectEntity();
			TeamData data = ServerQuestFile.INSTANCE.getData(player);

			for (KillTask task : killTasks) {
				TaskData taskData = data.getTaskData(task);

				if (taskData.progress < task.getMaxProgress() && data.canStartTasks(task.quest)) {
					((KillTask.Data) taskData).kill(entity);
				}
			}
		}

		return InteractionResult.PASS;
	}

	private void playerTick(Player player) {
		if (!player.level.isClientSide && ServerQuestFile.INSTANCE != null && !PlayerHooks.isFake(player)) {
			if (autoSubmitTasks == null) {
				autoSubmitTasks = ServerQuestFile.INSTANCE.collect(o -> o instanceof Task && ((Task) o).autoSubmitOnPlayerTick() > 0);
			}

			if (autoSubmitTasks == null || autoSubmitTasks.isEmpty()) // Don't be deceived, its somehow possible to be null here
			{
				return;
			}

			TeamData data = ServerQuestFile.INSTANCE.getData(player);
			long t = player.level.getGameTime();

			for (Task task : autoSubmitTasks) {
				long d = task.autoSubmitOnPlayerTick();

				if (d > 0L && t % d == 0L) {
					TaskData taskData = data.getTaskData(task);

					if (!taskData.isComplete() && data.canStartTasks(task.quest)) {
						taskData.submitTask((ServerPlayer) player);
					}
				}
			}
		}
	}

	private void itemCrafted(Player player, ItemStack crafted, Container inventory) {
		if (player instanceof ServerPlayer && !crafted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, crafted, 0);
		}
	}

	private void itemSmelted(Player player, ItemStack smelted) {
		if (player instanceof ServerPlayer && !smelted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, smelted, 0);
		}
	}

	private void cloned(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
		newPlayer.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(newPlayer));

		if (wonGame) {
			return;
		}

		if (PlayerHooks.isFake(newPlayer) || newPlayer.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}

		for (int i = 0; i < oldPlayer.inventory.items.size(); i++) {
			ItemStack stack = oldPlayer.inventory.items.get(i);

			if (stack.getItem() == FTBQuestsItems.BOOK.get() && newPlayer.addItem(stack)) {
				oldPlayer.inventory.items.set(i, ItemStack.EMPTY);
			}
		}
	}

	private void changedDimension(ServerPlayer player, ResourceKey<Level> oldLevel, ResourceKey<Level> newLevel) {
		if (!PlayerHooks.isFake(player)) {
			TeamData data = ServerQuestFile.INSTANCE.getData(player);

			for (DimensionTask task : ServerQuestFile.INSTANCE.collect(DimensionTask.class)) {
				data.getTaskData(task).submitTask(player);
			}
		}
	}

	private void containerOpened(Player player, AbstractContainerMenu menu) {
		if (player instanceof ServerPlayer && !PlayerHooks.isFake(player) && !(menu instanceof InventoryMenu)) {
			menu.addSlotListener(new FTBQuestsInventoryListener((ServerPlayer) player));
		}
	}
}