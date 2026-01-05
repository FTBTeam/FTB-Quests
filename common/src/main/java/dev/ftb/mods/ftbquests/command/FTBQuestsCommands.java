package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.InventoryUtil;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class FTBQuestsCommands {
	public static final SimpleCommandExceptionType NO_FILE = new SimpleCommandExceptionType(
			Component.translatable("commands.ftbquests.command.error.no_file"));
	public static final DynamicCommandExceptionType NO_OBJECT = new DynamicCommandExceptionType(
			(object) -> Component.translatable("commands.ftbquests.command.error.no_object", object));
	public static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
			(id) -> Component.translatable("commands.ftbquests.command.error.invalid_id", id));
	private static final SimpleCommandExceptionType NO_INVENTORY = new SimpleCommandExceptionType(
			Component.translatable("commands.ftbquests.command.error.no_inventory"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("ftbquests")
				.then(literal("editing_mode")
						.requires(FTBQuestsCommands::isSSPOrEditor)
						.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(argument("enabled", BoolArgumentType.bool())
								.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(argument("player", EntityArgument.player())
										.executes(c -> editingMode(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
				.then(literal("locked")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(argument("enabled", BoolArgumentType.bool())
								.executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(argument("player", EntityArgument.player())
										.executes(c -> locked(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
				.then(literal("delete_empty_reward_tables")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(context -> deleteEmptyRewardTables(context.getSource()))
				)
				.then(literal("change_progress")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(argument("players", EntityArgument.players())
								.then(literal("reset")
										.then(argument("quest_object", StringArgumentType.string())
												.executes(ctx -> {
													Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
													return changeProgress(ctx.getSource(), players, true, StringArgumentType.getString(ctx, "quest_object"));
												})
										)
								)
								.then(literal("reset-all")
										.executes(ctx -> {
											Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
											return changeProgress(ctx.getSource(), players, true, "1");
										})
								)
								.then(literal("complete")
										.then(argument("quest_object", StringArgumentType.string())
												.executes(ctx -> {
													Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
													return changeProgress(ctx.getSource(), players, false, StringArgumentType.getString(ctx, "quest_object"));
												})
										)
								)
								.then(literal("complete-all")
										.executes(ctx -> {
											Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
											return changeProgress(ctx.getSource(), players, false, "1");
										})
								)
						)
				)
				.then(literal("export_reward_table_to_chest")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(argument("reward_table", StringArgumentType.string())
								.executes(ctx ->
										exportRewards(ctx.getSource(), StringArgumentType.getString(ctx, "reward_table"), null)
								)
								.then(argument("pos", BlockPosArgument.blockPos())
										.executes(ctx -> {
											BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
											return exportRewards(ctx.getSource(), StringArgumentType.getString(ctx, "reward_table"), pos);
										})
								)
						)
				)
				.then(literal("import_reward_table_from_chest")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(argument("name", StringArgumentType.string())
								.executes(ctx -> {
									String name = StringArgumentType.getString(ctx, "name");
									return importRewards(ctx.getSource(), name, null);
								})
								.then(argument("pos", BlockPosArgument.blockPos())
										.executes(ctx -> {
											String name = StringArgumentType.getString(ctx, "name");
											BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
											return importRewards(ctx.getSource(), name, pos);
										})
								)
						)
				)
				.then(literal("generate_chapter")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(literal("from_entire_creative_list")
								.executes(context -> generateAllItemChapter(context.getSource()))
						)
						.then(literal("from_player_inventory")
								.executes(context -> generateChapterFromPlayerInv(context.getSource()))
						)
						.then(literal("from_inventory")
								.then(argument("pos", BlockPosArgument.blockPos())
										.executes(context -> generateChapterFromInv(context.getSource(), BlockPosArgument.getSpawnablePos(context, "pos")))
								)
						)
				)
				.then(literal("reload")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(context -> doReload(context.getSource(), true, true))
						.then(literal("quests")
								.executes(context -> doReload(context.getSource(), true, false))
						)
						.then(literal("team_progress")
								.executes(context -> doReload(context.getSource(), false, true))
						)
				)
				.then(literal("block_rewards")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(argument("enabled", BoolArgumentType.bool())
								.executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(argument("player", EntityArgument.player())
										.requires(FTBQuestsCommands::hasEditorPermission)
										.executes(c -> toggleRewardBlocking(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
				.then(literal("open_book")
						.executes(c -> openQuest(c.getSource().getPlayerOrException(), null))
						.then(argument("quest_object", StringArgumentType.string())
								.executes(c -> openQuest(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "quest_object"))))
				)
				.then(literal("clear_item_display_cache")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(c -> clearDisplayCache(c.getSource()))
				)
		);
	}

	private static boolean isSSPOrEditor(CommandSourceStack s) {
		// s.getServer() *can* be null here, whatever the IDE thinks!
		//noinspection ConstantValue
		return s.getServer() != null && s.getServer().isSingleplayer() || hasEditorPermission(s);
	}

	private static boolean hasEditorPermission(CommandSourceStack stack) {
		//noinspection DataFlowIssue
		return stack.hasPermission(2)
				|| stack.isPlayer() && PermissionsHelper.hasEditorPermission(stack.getPlayer(), false);
	}

	private static QuestObjectBase getQuestObjectForString(String idStr) throws CommandSyntaxException {
		ServerQuestFile file = ServerQuestFile.INSTANCE;
		if (file == null) {
			throw NO_FILE.create();
		}

		if (idStr.startsWith("#")) {
			String val = idStr.substring(1);
			for (QuestObjectBase qob : file.getAllObjects()) {
				if (qob.hasTag(val)) {
					return qob;
				}
			}
			throw NO_OBJECT.create(idStr);
		} else {
			long id = QuestObjectBase.parseHexId(idStr).orElseThrow(() -> INVALID_ID.create(idStr));
			QuestObjectBase qob = file.getBase(id);
			if (qob == null) {
				throw NO_OBJECT.create(idStr);
			}
			return qob;
		}
	}

	private static boolean playerCanSeeQuestObject(ServerPlayer player, QuestObject qo) {
		if (qo instanceof Chapter) {
			return true;
		}
		return ServerQuestFile.INSTANCE.getTeamData(player).map(data -> {
			Quest quest = qo.getRelatedQuest();
			return quest != null && (data.getCanEdit(player) || !quest.hideDetailsUntilStartable() || data.canStartTasks(quest));
		}).orElse(false);
	}

	private static int openQuest(ServerPlayer player, String qobId) throws CommandSyntaxException {
		if (qobId == null) {
			NetworkManager.sendToPlayer(player, OpenQuestBookMessage.lastOpenedQuest());
			return Command.SINGLE_SUCCESS;
		} else {
			if (getQuestObjectForString(qobId) instanceof Quest quest && playerCanSeeQuestObject(player, quest)) {
				NetworkManager.sendToPlayer(player, new OpenQuestBookMessage(quest.id));
				return Command.SINGLE_SUCCESS;
			}
		}
		return 0;
	}

	private static int exportRewards(CommandSourceStack source, String idStr, @Nullable BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();

		if (!(getQuestObjectForString(idStr) instanceof RewardTable table)) {
			throw NO_OBJECT.create(idStr);
		}

		pos = Objects.requireNonNullElse(pos, BlockPos.containing(player.pick(10, 1F, false).getLocation()));
		if (level.getBlockEntity(pos) == null) {
			throw NO_INVENTORY.create();
		}

		List<ItemStack> items = new ArrayList<>();
		for (WeightedReward wr : table.getWeightedRewards()) {
			if (wr.getReward() instanceof ItemReward itemReward) {
				items.add(itemReward.getItem());
			}
		}
		InventoryUtil.putItemsInInventory(items, level, pos, Direction.UP, true);

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_exported", table.getTitle(), items.size()), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int importRewards(CommandSourceStack source, String name, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (pos == null) {
			pos = BlockPos.containing(player.pick(10, 1F, false).getLocation());
		}

		if (level.getBlockEntity(pos) == null) {
			throw NO_INVENTORY.create();
		}

		RewardTable table = new RewardTable(file.newID(), file);
		table.setRawTitle(name);
		table.setRawIcon(Items.CHEST.getDefaultInstance());

		for (ItemStack stack : InventoryUtil.getItemsInInventory(level, pos, Direction.UP)) {
			if (!stack.isEmpty()) {
				table.addReward(table.makeWeightedItemReward(stack, 1f));
			}
		}

		file.addRewardTable(table);
		file.refreshIDMap();
		file.clearCachedData();
		file.markDirty();

		NetworkHelper.sendToAll(level.getServer(), CreateObjectResponseMessage.create(table, null));
		NetworkHelper.sendToAll(level.getServer(), SyncTranslationMessageToClient.create(table, file.getLocale(), TranslationKey.TITLE, name));

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_imported", name, table.getWeightedRewards().size()), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit) {
		return ServerQuestFile.INSTANCE.getTeamData(player).map(data -> {
			boolean newCanEdit = Objects.requireNonNullElse(canEdit, !data.getCanEdit(player));

			data.setCanEdit(player, newCanEdit);

			if (newCanEdit) {
				source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
			}

			return Command.SINGLE_SUCCESS;
		}).orElse(0);
	}

	private static int locked(CommandSourceStack source, ServerPlayer player, @Nullable Boolean locked) {
		return ServerQuestFile.INSTANCE.getTeamData(player).map(data -> {
			boolean newLocked = Objects.requireNonNullElse(locked, !data.isLocked());

			data.setLocked(newLocked);

			if (newLocked) {
				source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.enabled", player.getDisplayName()), true);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.disabled", player.getDisplayName()), true);
			}

			return Command.SINGLE_SUCCESS;
		}).orElse(0);
	}

	private static int changeProgress(CommandSourceStack source, Collection<ServerPlayer> players, boolean reset, String idStr) throws CommandSyntaxException {
		QuestObjectBase questObject = getQuestObjectForString(idStr);
		for (ServerPlayer player : players) {
			ServerQuestFile.INSTANCE.getTeamData(player).ifPresent(data -> {
				ProgressChange progressChange = new ProgressChange(questObject, player.getUUID()).setReset(reset);
				questObject.forceProgress(data, progressChange);
				if (questObject instanceof Quest quest && reset) {
                    data.clearRepeatCooldown(quest);
					ClearRepeatCooldownMessage.sendToTeam(quest, data.getTeamId());
				}
			});
		}

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.change_progress.text"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int deleteEmptyRewardTables(CommandSourceStack source) {
		int removed = ServerQuestFile.INSTANCE.removeEmptyRewardTables(source);

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.delete_empty_reward_tables.text", removed), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int generateAllItemChapter(CommandSourceStack source) {
		if (!CreativeModeTabs.searchTab().hasAnyItems()) {
			CreativeModeTabs.tryRebuildTabContents(source.enabledFeatures(), true, source.getLevel().registryAccess());
		}

		return generateMultiItemChapter(source, CreativeModeTabs.searchTab().getSearchTabDisplayItems());
	}

	private static int generateChapterFromInv(CommandSourceStack source, BlockPos pos) {
		return generateMultiItemChapter(source, InventoryUtil.getItemsInInventory(source.getLevel(), pos, Direction.UP));
	}

	private static int generateChapterFromPlayerInv(CommandSourceStack source) throws CommandSyntaxException {
		return generateMultiItemChapter(source, source.getPlayerOrException().getInventory().items);
	}

	private static int generateMultiItemChapter(CommandSourceStack source, Collection<ItemStack> allItems) {
		//noinspection DataFlowIssue
		List<ItemStack> list = allItems.stream()
				.filter(stack -> !stack.isEmpty() && RegistrarManager.getId(stack.getItem(), Registries.ITEM) != null)
				.sorted(Comparator.comparing(a -> RegistrarManager.getId(a.getItem(), Registries.ITEM)))
				.toList();

		if (list.isEmpty()) {
			FTBQuests.LOGGER.warn("No suitable items found for chapter auto-creation");
			return 0;
		}

		ServerQuestFile file = ServerQuestFile.INSTANCE;

		long newId = file.newID();
		Chapter chapter = new Chapter(newId, file, file.getDefaultChapterGroup());
		chapter.onCreated();

		FTBQuests.LOGGER.info("Adding {} items to new chapter ID: {}", list.size(), newId);

		chapter.setRawTitle("Auto-generated chapter [" + list.size() + " items]");
		chapter.setRawIcon(new ItemStack(Items.COMPASS));
		chapter.setDefaultQuestShape("rsquare");

		NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(chapter, null));
		NetworkHelper.sendToAll(source.getServer(), SyncTranslationMessageToClient.create(chapter, file.getLocale(), TranslationKey.TITLE, chapter.getRawTitle()));

		addItemsToChapter(source, list, chapter);

		file.markDirty();
		file.saveNow();

		source.sendSuccess(() -> Component.literal("Done!"), false);

		return Command.SINGLE_SUCCESS;
	}

	private static void addItemsToChapter(CommandSourceStack source, Collection<ItemStack> list, Chapter chapter) {
		int col = 0;
		int row = 0;
		String modid = null;

		for (ItemStack stack : list) {
			ResourceLocation id = RegistrarManager.getId(stack.getItem(), Registries.ITEM);
			if (modid == null) {
				modid = id.getNamespace();
			}
			if (!modid.equals(id.getNamespace())) {
				modid = id.getNamespace();
				col = 0;
				row += 2;
			} else if (col >= 40) {
				col = 0;
				row++;
			}

			Quest quest = new Quest(chapter.file.newID(), chapter);
			quest.onCreated();
			quest.setX(col);
			quest.setY(row);
			quest.setRawSubtitle(stack.save(source.registryAccess(), new CompoundTag()).toString());

			NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(quest, null));

			ItemTask task = new ItemTask(chapter.file.newID(), quest);
			task.onCreated();
			task.setStackAndCount(stack, 1);//.setConsumeItems(Tristate.TRUE);

			NetworkHelper.sendToAll(source.getServer(), CreateObjectResponseMessage.create(task, task.getType().makeExtraNBT()));

			col++;
		}
	}

	private static final Set<UUID> warnedPlayers = new HashSet<>();

	private static int doReload(CommandSourceStack source, boolean quests, boolean progression) {
		if (!quests && !progression) {
			return 0;
		}

		ServerQuestFile instance = ServerQuestFile.INSTANCE;
		ServerPlayer sender = source.getPlayer();

		instance.load(quests, progression);
		NetworkHelper.sendToAll(source.getServer(), new SyncQuestsMessage(instance));
		source.getServer().getPlayerList().getPlayers().forEach(p -> {
			NetworkManager.sendToPlayer(p, SyncEditorPermissionMessage.forPlayer(p));
			instance.getTranslationManager().sendTranslationsToPlayer(p);
		});

		String suffix = quests && progression ? "" : (quests ? "_quest" : "_progress");
		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded" + suffix), false);
		UUID id = sender == null ? Util.NIL_UUID : sender.getUUID();
		if (!warnedPlayers.contains(id)) {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded.disclaimer")
					.withStyle(ChatFormatting.GOLD), false);
			warnedPlayers.add(id);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int toggleRewardBlocking(CommandSourceStack source, ServerPlayer player, Boolean doBlocking) {
		return ServerQuestFile.INSTANCE.getTeamData(player).map(data -> {
			boolean shouldBlock = Objects.requireNonNullElse(doBlocking, !data.areRewardsBlocked());
			data.setRewardsBlocked(shouldBlock);

			source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.rewards_blocked", data, data.areRewardsBlocked()), false);

			return Command.SINGLE_SUCCESS;
		}).orElse(0);

	}

	private static int clearDisplayCache(CommandSourceStack source) {
		ClearDisplayCacheMessage.clearForAll(source.getServer());
		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.clear_display_cache"), false);
		return Command.SINGLE_SUCCESS;
	}
}
