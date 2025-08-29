package dev.ftb.mods.ftbquests;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.command.FTBQuestsCommands;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.DimensionTask;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.DeferredInventoryDetection;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;

public enum FTBQuestsEventHandler {
	INSTANCE;

	private List<KillTask> killTasks = null;
	private List<Task> autoSubmitTasks = null;

	void init() {
		LifecycleEvent.SERVER_BEFORE_START.register(this::serverAboutToStart);
		CommandRegistrationEvent.EVENT.register(this::registerCommands);
		LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
		LifecycleEvent.SERVER_STOPPING.register(this::serverStopped);
		LifecycleEvent.SERVER_LEVEL_SAVE.register(this::worldSaved);
		ModDataComponents.register();
		ModBlocks.register();
		ModItems.register();
		ModBlockEntityTypes.register();
		ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
		TeamEvent.PLAYER_LOGGED_IN.register(this::playerLoggedIn);
		TeamEvent.CREATED.register(this::teamCreated);
		TeamEvent.PLAYER_CHANGED.register(this::playerChangedTeam);
		EntityEvent.LIVING_DEATH.register(this::playerKill);
		TickEvent.PLAYER_POST.register(this::playerTick);
		PlayerEvent.CRAFT_ITEM.register(this::itemCrafted);
		PlayerEvent.SMELT_ITEM.register(this::itemSmelted);
		PlayerEvent.PLAYER_CLONE.register(this::cloned);
		PlayerEvent.CHANGE_DIMENSION.register(this::changedDimension);
		PlayerEvent.OPEN_MENU.register(this::containerOpened);
		TickEvent.SERVER_POST.register(DeferredInventoryDetection::tick);
		TickEvent.SERVER_POST.register(QuestBarrierBlock.TeleportTicker::tick);
	}

	private void serverAboutToStart(MinecraftServer server) {
		ServerQuestFile.INSTANCE = new ServerQuestFile(server);
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
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

	private void fileCacheClear(BaseQuestFile file) {
		if (file.isServerSide()) {
			killTasks = null;
			autoSubmitTasks = null;
		}
	}

	private void playerLoggedIn(PlayerLoggedInAfterTeamEvent event) {
		ServerQuestFile.INSTANCE.playerLoggedIn(event);
	}

	private void teamCreated(TeamCreatedEvent event) {
		ServerQuestFile.INSTANCE.teamCreated(event);
	}

	private void playerChangedTeam(PlayerChangedTeamEvent event) {
		ServerQuestFile.INSTANCE.playerChangedTeam(event);
	}

	private EventResult playerKill(LivingEntity entity, DamageSource source) {
		// `source` should never be null, this is a defensive check against badly behaved mods.
		if (source == null) {
			return EventResult.pass();
		}

		if (source.getEntity() instanceof ServerPlayer player && !PlayerHooks.isFake(player)) {
			if (killTasks == null) {
				killTasks = ServerQuestFile.INSTANCE.collect(KillTask.class);
			}

			if (killTasks.isEmpty()) {
				return EventResult.pass();
			}

			ServerQuestFile.INSTANCE.getTeamData(player).ifPresent(data -> {
				for (KillTask task : killTasks) {
					if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.getQuest())) {
						task.kill(data, entity);
					}
				}
			});
		}

		return EventResult.pass();
	}

	private void playerTick(Player player) {
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (player instanceof ServerPlayer serverPlayer && file != null && !PlayerHooks.isFake(player)) {
			if (autoSubmitTasks == null) {
				autoSubmitTasks = file.collect(o -> o instanceof Task t && t.autoSubmitOnPlayerTick() > 0);
			}

			// Don't be deceived, it's somehow possible to be null here
			if (autoSubmitTasks == null || autoSubmitTasks.isEmpty()) {
				return;
			}

			file.getTeamData(player).ifPresent(data -> {
				long now = player.level().getGameTime();

				file.withPlayerContext(serverPlayer, () -> {
					for (Task task : autoSubmitTasks) {
						long interval = task.autoSubmitOnPlayerTick();
						if (interval > 0L && now % interval == 0L) {
							if (!data.isCompleted(task) && data.canStartTasks(task.getQuest())) {
								task.submitTask(data, serverPlayer);
							}
						}
					}
				});
			});
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

		if (PlayerHooks.isFake(newPlayer) || newPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}

		if (!ServerQuestFile.INSTANCE.dropBookOnDeath()) {
			for (int i = 0; i < oldPlayer.getInventory().items.size(); i++) {
				ItemStack stack = oldPlayer.getInventory().items.get(i);

				if (stack.getItem() == ModItems.BOOK.get() && newPlayer.addItem(stack)) {
					oldPlayer.getInventory().items.set(i, ItemStack.EMPTY);
				}
			}
		}
	}

	private void changedDimension(ServerPlayer player, ResourceKey<Level> oldLevel, ResourceKey<Level> newLevel) {
		if (!PlayerHooks.isFake(player)) {
			ServerQuestFile file = ServerQuestFile.INSTANCE;
			file.getTeamData(player).ifPresent(data -> {
				if (!data.isLocked()) {
					file.withPlayerContext(player, () -> {
						for (DimensionTask task : file.collect(DimensionTask.class)) {
							if (data.canStartTasks(task.getQuest())) {
								task.submitTask(data, player);
							}
						}
					});
				}
			});
		}
	}

	private void containerOpened(Player player, AbstractContainerMenu menu) {
		if (player instanceof ServerPlayer && !PlayerHooks.isFake(player) && !(menu instanceof InventoryMenu)) {
			menu.addSlotListener(new FTBQuestsInventoryListener((ServerPlayer) player));
		}
	}
}
