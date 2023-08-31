package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncEditorPermissionMessage;
import dev.ftb.mods.ftbquests.net.SyncQuestsMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author LatvianModder
 */
public class FTBQuestsCommands {

	private static final SimpleCommandExceptionType NO_INVENTORY = new SimpleCommandExceptionType(Component.translatable("commands.ftbquests.command.error.no_inventory"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		//noinspection ConstantValue
		dispatcher.register(Commands.literal("ftbquests")
				// s.getServer() *can* be null here, whatever the IDE thinks!
				.requires(s -> s.getServer() != null && s.getServer().isSingleplayer() || hasEditorPermission(s))
				.then(Commands.literal("editing_mode")
						.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(c -> editingMode(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
				.then(Commands.literal("locked")
						.executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(c -> locked(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
				.then(Commands.literal("delete_empty_reward_tables")
						.executes(context -> deleteEmptyRewardTables(context.getSource()))
				)
				.then(Commands.literal("change_progress")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.literal("reset")
										.then(Commands.argument("quest_object", QuestObjectArgument.questObject())
												.executes(ctx -> {
													Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
													QuestObjectBase questObject = ctx.getArgument("quest_object", QuestObjectBase.class);
													return changeProgress(ctx.getSource(), players, true, questObject);
												})
										)
								)
								.then(Commands.literal("complete")
										.then(Commands.argument("quest_object", QuestObjectArgument.questObject())
												.executes(ctx -> {
													Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
													QuestObjectBase questObject = ctx.getArgument("quest_object", QuestObjectBase.class);
													return changeProgress(ctx.getSource(), players, false, questObject);
												})
										)
								)
						)
				)
				.then(Commands.literal("export_reward_table_to_chest")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(Commands.argument("reward_table", QuestObjectArgument.questObject())
								.executes(ctx -> {
									QuestObjectBase table = ctx.getArgument("reward_table", QuestObjectBase.class);
									if (!(table instanceof RewardTable)) {
										throw QuestObjectArgument.NO_OBJECT.create(table.getCodeString());
									}
									return exportRewards(ctx.getSource(), (RewardTable) table, null);
								})
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(ctx -> {
											QuestObjectBase table = ctx.getArgument("reward_table", QuestObjectBase.class);
											BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
											if (!(table instanceof RewardTable)) {
												throw QuestObjectArgument.NO_OBJECT.create(table.getCodeString());
											}
											return exportRewards(ctx.getSource(), (RewardTable) table, pos);
										})
								)
						)
				)
				.then(Commands.literal("import_reward_table_from_chest")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.then(Commands.argument("name", StringArgumentType.string())
								.executes(ctx -> {
									String name = StringArgumentType.getString(ctx, "name");
									return importRewards(ctx.getSource(), name, null);
								})
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(ctx -> {
											String name = StringArgumentType.getString(ctx, "name");
											BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
											return importRewards(ctx.getSource(), name, pos);
										})
								)
						)
				)
				.then(Commands.literal("generate_chapter_with_all_items_in_game")
						.executes(context -> generateAllItemChapter(context.getSource()))
				)
				.then(Commands.literal("reload")
						.requires(FTBQuestsCommands::hasEditorPermission)
						.executes(context -> doReload(context.getSource()))
				)
				.then(Commands.literal("block_rewards")
						.executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
								.then(Commands.argument("player", EntityArgument.player())
										.requires(FTBQuestsCommands::hasEditorPermission)
										.executes(c -> toggleRewardBlocking(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
								)
						)
				)
		);
	}

	private static boolean hasEditorPermission(CommandSourceStack stack) {
		//noinspection DataFlowIssue
		return stack.hasPermission(2)
				|| stack.isPlayer() && PermissionsHelper.hasEditorPermission(stack.getPlayer(), false);
	}

	private static int exportRewards(CommandSourceStack source, RewardTable table, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();

		if (pos == null) {
			pos = BlockPos.containing(player.pick(10, 1F, false).getLocation());
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof BaseContainerBlockEntity container)) {
			throw NO_INVENTORY.create();
		}

		container.clearContent();

		int s = 0;
		for (WeightedReward wr : table.getWeightedRewards()) {
			if (s >= container.getContainerSize()) {
				source.sendFailure(Component.translatable("commands.ftbquests.command.feedback.table_too_many_items", table.getTitle()));
				return 0;
			} else if (wr.getReward() instanceof ItemReward itemReward) {
				container.setItem(s++, itemReward.getItem());
			}
		}

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_imported", table.getTitle(), table.getWeightedRewards().size()), false);

		return 1;
	}

	private static int importRewards(CommandSourceStack source, String name, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (pos == null) {
			pos = BlockPos.containing(player.pick(10, 1F, false).getLocation());
		}

		RewardTable table = new RewardTable(file.newID(), file);
		table.setRawTitle(name);
		table.setRawIcon(Items.CHEST.getDefaultInstance());

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof BaseContainerBlockEntity container)) {
			throw NO_INVENTORY.create();
		}

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				table.addReward(table.makeWeightedItemReward(stack, 1f));
			}
		}

		file.addRewardTable(table);

		new CreateObjectResponseMessage(table, null).sendToAll(level.getServer());

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_imported", name, table.getWeightedRewards().size()), false);

		return 1;
	}

	private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit) {
		TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);

		if (canEdit == null) {
			canEdit = !data.getCanEdit(player);
		}

		data.setCanEdit(player, canEdit);

		if (canEdit) {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int locked(CommandSourceStack source, ServerPlayer player, @Nullable Boolean locked) {
		TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);

		if (locked == null) {
			locked = !data.isLocked();
		}

		data.setLocked(locked);

		if (locked) {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int changeProgress(CommandSourceStack source, Collection<ServerPlayer> players, boolean reset, QuestObjectBase questObject) {
		for (ServerPlayer player : players) {
			ProgressChange progressChange = new ProgressChange(ServerQuestFile.INSTANCE, questObject, player.getUUID()).setReset(reset);
			questObject.forceProgress(ServerQuestFile.INSTANCE.getOrCreateTeamData(player), progressChange);
		}

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.change_progress.text"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int deleteEmptyRewardTables(CommandSourceStack source) {
		int removed = ServerQuestFile.INSTANCE.removeEmptyRewardTables(source);

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.delete_empty_reward_tables.text", removed), false);

		return 1;
	}

	private static int generateAllItemChapter(CommandSourceStack source) {
		if (!CreativeModeTabs.searchTab().hasAnyItems()) {
			CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, source.getLevel().registryAccess());
		}
		Collection<ItemStack> allItems = CreativeModeTabs.searchTab().getSearchTabDisplayItems();

		long newId = ServerQuestFile.INSTANCE.newID();
		Chapter chapter = new Chapter(newId, ServerQuestFile.INSTANCE, ServerQuestFile.INSTANCE.getDefaultChapterGroup());
		chapter.onCreated();

		chapter.setRawTitle("Generated chapter of all items in search creative tab [" + allItems.size() + "]");
		chapter.setRawIcon(new ItemStack(Items.COMPASS));
		chapter.setDefaultQuestShape("rsquare");

		new CreateObjectResponseMessage(chapter, null).sendToAll(source.getServer());

		//noinspection DataFlowIssue
		List<ItemStack> list = allItems.stream()
				.filter(stack -> !stack.isEmpty() && RegistrarManager.getId(stack.getItem(), Registries.ITEM) != null)
				.sorted(Comparator.comparing(a -> RegistrarManager.getId(a.getItem(), Registries.ITEM)))
				.toList();
		FTBQuests.LOGGER.info("Found " + allItems.size() + " items in total, chapter ID: " + chapter);

		if (list.isEmpty()) {
			return 0;
		}

		int col = 0;
		int row = 0;
		String modid = RegistrarManager.getId(list.get(0).getItem(), Registries.ITEM).getNamespace();

		for (ItemStack stack : list) {
			ResourceLocation id = RegistrarManager.getId(stack.getItem(), Registries.ITEM);
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
			quest.setRawSubtitle(stack.save(new CompoundTag()).toString());

			new CreateObjectResponseMessage(quest, null).sendToAll(source.getServer());

			ItemTask task = new ItemTask(chapter.file.newID(), quest);
			task.onCreated();
			task.setStackAndCount(stack, 1).setConsumeItems(Tristate.TRUE);

			CompoundTag extra = new CompoundTag();
			extra.putString("type", task.getType().getTypeForNBT());
			new CreateObjectResponseMessage(task, extra).sendToAll(source.getServer());

			col++;
		}

		ServerQuestFile.INSTANCE.markDirty();
		ServerQuestFile.INSTANCE.saveNow();
		source.sendSuccess(() -> Component.literal("Done!"), false);
		return 1;
	}

	private static final Set<UUID> warnedPlayers = new HashSet<>();

	private static int doReload(CommandSourceStack source) {
		ServerQuestFile instance = ServerQuestFile.INSTANCE;
		ServerPlayer sender = source.getPlayer();

		if (sender != null && !instance.getOrCreateTeamData(sender).getCanEdit(sender)) {
			source.sendFailure(Component.translatable("commands.ftbquests.command.error.not_editing"));
			return 1;
		}

		instance.load();
		new SyncQuestsMessage(instance).sendToAll(source.getServer());
		source.getServer().getPlayerList().getPlayers()
				.forEach(p -> new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(p, false)).sendTo(p));

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded"), false);
		UUID id = sender == null ? Util.NIL_UUID : sender.getUUID();
		if (!warnedPlayers.contains(id)) {
			source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded.disclaimer").withStyle(ChatFormatting.GOLD), false);
			warnedPlayers.add(id);
		}

		return 1;
	}

	private static int toggleRewardBlocking(CommandSourceStack source, ServerPlayer player, Boolean doBlocking) {
		TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);

		if (doBlocking == null) {
			doBlocking = !data.areRewardsBlocked();
		}

		data.setRewardsBlocked(doBlocking);

		source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.rewards_blocked", data, data.areRewardsBlocked()), false);

		return 1;
	}
}
