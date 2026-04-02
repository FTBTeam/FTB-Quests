package dev.ftb.mods.ftbquests;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.command.FTBQuestsCommands;
import dev.ftb.mods.ftbquests.api.event.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.quest.task.DimensionTask;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.DeferredInventoryDetection;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamPlayerLoggedInEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.List;

public class FTBQuestsEventHandler {
	private final Lazy<List<KillTask>> killTasks = Lazy.of(() ->
			ServerQuestFile.getInstance().collect(KillTask.class));
	private final Lazy<List<DimensionTask>> dimensionTasks = Lazy.of(() ->
			ServerQuestFile.getInstance().collect(DimensionTask.class));
	private final Lazy<List<Task>> autoSubmitTasks = Lazy.of(() ->
			ServerQuestFile.getInstance().collect(Task.class, t -> t.autoSubmitOnPlayerTick() > 0));

	public void serverAboutToStart(MinecraftServer server) {
		ServerQuestFile.startup(server);
	}

	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredCtx, Commands.CommandSelection ignoredSelection) {
		FTBQuestsCommands.register(dispatcher);
	}

	public void serverStarted(MinecraftServer ignoredServer) {
		ServerQuestFile.getInstance().load(true, true);
	}

	public void serverStopped(MinecraftServer ignoredServer) {
		clearCachedData();

		ServerQuestFile.shutdown();
	}

	public void onWorldSaved() {
		ServerQuestFile.ifExists(ServerQuestFile::saveNow);
	}

	public void onFileCacheClear(ClearFileCacheEvent.Data data) {
		if (data.file().isServerSide()) {
			clearCachedData();
		}
	}

	public void clearCachedData() {
		killTasks.invalidate();
		dimensionTasks.invalidate();
		autoSubmitTasks.invalidate();
	}

	public void onServerTickPost(MinecraftServer server) {
		DeferredInventoryDetection.tick(server);
		QuestBarrierBlock.TeleportTicker.tick(server);
		CustomTask.TaskSync.tick(server);
	}

	public void playerLoggedIn(TeamPlayerLoggedInEvent.Data event) {
		ServerQuestFile.getInstance().playerLoggedIn(event);
	}

	public void teamCreated(TeamCreatedEvent.Data event) {
		ServerQuestFile.getInstance().teamCreated(event);
	}

	public void playerChangedTeam(PlayerChangedTeamEvent.Data event) {
		ServerQuestFile.getInstance().playerChangedTeam(event);
	}

	public void onPlayerKilledEntity(LivingEntity entity, DamageSource source) {
		// `source` should never be null, this is a defensive check against badly behaved mods.
		//noinspection ConstantValue
		if (source == null) {
			return;
		}

		if (source.getEntity() instanceof ServerPlayer player && !Platform.get().misc().isFakePlayer(player)) {
			if (!killTasks.get().isEmpty()) {
				ServerQuestFile.getInstance().getTeamData(player).ifPresent(data -> {
					for (KillTask task : killTasks.get()) {
						if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.getQuest())) {
							task.kill(data, entity);
						}
					}
				});
			}
		}
	}

	public void onPlayerTick(Player player) {
		ServerQuestFile.ifExists(file -> {
			if (player instanceof ServerPlayer serverPlayer && !Platform.get().misc().isFakePlayer(player)) {
				if (!autoSubmitTasks.get().isEmpty()) {
					file.getTeamData(player).ifPresent(data -> {
						long now = player.level().getGameTime();

						file.withPlayerContext(serverPlayer, () -> {
							for (Task task : autoSubmitTasks.get()) {
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
		});
	}

	public void onItemCrafted(Player player, ItemStack crafted, Container ignoredInventory) {
		if (player instanceof ServerPlayer && !crafted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, crafted, 0);
		}
	}

	public void onItemSmelted(Player player, ItemStack smelted) {
		if (player instanceof ServerPlayer && !smelted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, smelted, 0);
		}
	}

	public void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
		newPlayer.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(newPlayer));

		if (wonGame || Platform.get().misc().isFakePlayer(newPlayer) || newPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}

		if (!ServerQuestFile.getInstance().dropBookOnDeath()) {
			for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
				ItemStack stack = oldPlayer.getInventory().getItem(i);

				if (stack.getItem() == ModItems.BOOK.get() && newPlayer.addItem(stack)) {
					oldPlayer.getInventory().setItem(i, ItemStack.EMPTY);
				}
			}
		}
	}

	public void onEntityJoinLevel(Entity entity) {
		if (entity instanceof ServerPlayer player && !Platform.get().misc().isFakePlayer(player)) {
			ServerQuestFile.ifExists(file -> file.getTeamData(player).ifPresent(data -> {
				if (!data.isLocked()) {
					file.withPlayerContext(player, () -> {
						for (DimensionTask task : dimensionTasks.get()) {
							if (data.canStartTasks(task.getQuest())) {
								task.submitTask(data, player);
							}
						}
					});
				}
			}));
		}
	}

	public void onContainerOpened(Player player, AbstractContainerMenu menu) {
		if (player instanceof ServerPlayer sp && !Platform.get().misc().isFakePlayer(sp) && !(menu instanceof InventoryMenu)) {
			menu.addSlotListener(new FTBQuestsInventoryListener(sp));
		}
	}
}
