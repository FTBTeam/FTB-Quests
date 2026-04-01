package dev.ftb.mods.ftbquests;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftbquests.command.FTBQuestsCommands;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.DimensionTask;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamPlayerLoggedInEvent;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FTBQuestsEventHandler {
	@Nullable
	private List<KillTask> killTasks = null;
	@Nullable
	private List<Task> autoSubmitTasks = null;

	void init() {
//		LifecycleEvent.SERVER_BEFORE_START.register(this::serverAboutToStart);
//		CommandRegistrationEvent.EVENT.register(this::registerCommands);
//		LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
//		LifecycleEvent.SERVER_STOPPING.register(this::serverStopped);
//		LifecycleEvent.SERVER_LEVEL_SAVE.register(this::worldSaved);
//		ModDataComponents.register();
//		ModBlocks.register();
//		ModItems.register();
//		ModBlockEntityTypes.register();
//		ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
//		TeamEvent.PLAYER_LOGGED_IN.register(this::playerLoggedIn);
//		TeamEvent.CREATED.register(this::teamCreated);
//		TeamEvent.PLAYER_CHANGED.register(this::playerChangedTeam);
//		EntityEvent.LIVING_DEATH.register(this::playerKill);
//		TickEvent.PLAYER_POST.register(this::playerTick);
//		PlayerEvent.CRAFT_ITEM.register(this::itemCrafted);
//		PlayerEvent.SMELT_ITEM.register(this::itemSmelted);
//		PlayerEvent.PLAYER_CLONE.register(this::cloned);
//		PlayerEvent.CHANGE_DIMENSION.register(this::changedDimension);
//		PlayerEvent.OPEN_MENU.register(this::containerOpened);
//		TickEvent.SERVER_POST.register(DeferredInventoryDetection::tick);
//		TickEvent.SERVER_POST.register(QuestBarrierBlock.TeleportTicker::tick);
//		TickEvent.SERVER_POST.register(CustomTask.TaskSync::tick);
	}

	public void serverAboutToStart(MinecraftServer server) {
		ServerQuestFile.startup(server);
	}

	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
		FTBQuestsCommands.register(dispatcher);
	}

	public void serverStarted(MinecraftServer server) {
		ServerQuestFile.getInstance().load(true, true);
	}

	public void serverStopped(MinecraftServer server) {
		clearCachedData();

		ServerQuestFile.shutdown();
	}

	public void worldSaved(ServerLevel level) {
		ServerQuestFile.ifExists(ServerQuestFile::saveNow);
	}

	public void fileCacheClear(BaseQuestFile file) {
		if (file.isServerSide()) {
			clearCachedData();
		}
	}

	public void clearCachedData() {
		killTasks = null;
		autoSubmitTasks = null;
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

	public void playerKill(LivingEntity entity, DamageSource source) {
		// `source` should never be null, this is a defensive check against badly behaved mods.
        //noinspection ConstantValue
        if (source == null) {
			return;
		}

		if (source.getEntity() instanceof ServerPlayer player && !Platform.get().misc().isFakePlayer(player)) {
			if (killTasks == null) {
				killTasks = ServerQuestFile.getInstance().collect(KillTask.class);
			}

			if (killTasks.isEmpty()) {
				return;
			}

			ServerQuestFile.getInstance().getTeamData(player).ifPresent(data -> {
				for (KillTask task : killTasks) {
					if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.getQuest())) {
						task.kill(data, entity);
					}
				}
			});
		}
	}

	public void playerTick(Player player) {
		ServerQuestFile.ifExists(file -> {
			if (player instanceof ServerPlayer serverPlayer && !Platform.get().misc().isFakePlayer(player)) {
				if (autoSubmitTasks == null) {
					autoSubmitTasks = file.collect(Task.class, t -> t.autoSubmitOnPlayerTick() > 0);
				}

				// Don't be deceived, it's somehow possible to be null here
				//noinspection ConstantValue
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
		});
	}

	public void itemCrafted(Player player, ItemStack crafted, Container inventory) {
		if (player instanceof ServerPlayer && !crafted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, crafted, 0);
		}
	}

	public void itemSmelted(Player player, ItemStack smelted) {
		if (player instanceof ServerPlayer && !smelted.isEmpty()) {
			FTBQuestsInventoryListener.detect((ServerPlayer) player, smelted, 0);
		}
	}

	public void cloned(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
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

	public void changedDimension(ServerPlayer player, ResourceKey<Level> oldLevel, ResourceKey<Level> newLevel) {
		if (!Platform.get().misc().isFakePlayer(player)) {
			ServerQuestFile file = ServerQuestFile.getInstance();
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

	public void containerOpened(Player player, AbstractContainerMenu menu) {
		if (player instanceof ServerPlayer && !Platform.get().misc().isFakePlayer(player) && !(menu instanceof InventoryMenu)) {
			menu.addSlotListener(new FTBQuestsInventoryListener((ServerPlayer) player));
		}
	}
}
