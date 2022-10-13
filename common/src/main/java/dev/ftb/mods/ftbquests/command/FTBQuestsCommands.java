package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncQuestsMessage;
import dev.ftb.mods.ftbquests.net.SyncTeamDataMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
		dispatcher.register(Commands.literal("ftbquests")
				.requires(s -> s.getServer().isSingleplayer() || s.hasPermission(2))
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
						.requires(s -> s.hasPermission(2))
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
						.requires(s -> s.hasPermission(2))
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
						.requires(s -> s.hasPermission(2))
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
						.requires(s -> s.hasPermission(2))
						.executes(context -> doReload(context.getSource()))
				)
		);
	}

	private static int exportRewards(CommandSourceStack source, RewardTable table, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();

		if (pos == null) {
			pos = new BlockPos(player.pick(10, 1F, false).getLocation());
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof BaseContainerBlockEntity container)) {
			throw NO_INVENTORY.create();
		}

		container.clearContent();

		int s = 0;
		for (WeightedReward reward : table.rewards) {
			if (s >= container.getContainerSize()) {
				source.sendFailure(Component.translatable("commands.ftbquests.command.feedback.table_too_many_items", table.getTitle()));
				return 0;
			} else if (!(reward.reward instanceof ItemReward)) {
				continue;
			}
			container.setItem(s++, ((ItemReward) reward.reward).item);
		}

		source.sendSuccess(Component.translatable("commands.ftbquests.command.feedback.table_imported", table.getTitle(), table.rewards.size()), false);

		return 1;
	}

	private static int importRewards(CommandSourceStack source, String name, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (pos == null) {
			pos = new BlockPos(player.pick(10, 1F, false).getLocation());
		}

		RewardTable table = new RewardTable(file);
		table.id = file.newID();
		table.title = name;
		table.icon = Items.CHEST.getDefaultInstance();

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof BaseContainerBlockEntity container)) {
			throw NO_INVENTORY.create();
		}

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				table.rewards.add(new WeightedReward(new ItemReward(table.fakeQuest, stack), 1));
			}
		}


		file.rewardTables.add(table);
		file.save();

		new CreateObjectResponseMessage(table, null).sendToAll(level.getServer());

		source.sendSuccess(Component.translatable("commands.ftbquests.command.feedback.table_imported", name, table.rewards.size()), false);

		return 1;
	}

	private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit) {
		TeamData data = ServerQuestFile.INSTANCE.getData(player);

		if (canEdit == null) {
			canEdit = !data.getCanEdit();
		}

		data.setCanEdit(canEdit);

		if (canEdit) {
			source.sendSuccess(Component.translatable("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(Component.translatable("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int locked(CommandSourceStack source, ServerPlayer player, @Nullable Boolean locked) {
		TeamData data = ServerQuestFile.INSTANCE.getData(player);

		if (locked == null) {
			locked = !data.isLocked();
		}

		data.setLocked(locked);

		if (locked) {
			source.sendSuccess(Component.translatable("commands.ftbquests.locked.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(Component.translatable("commands.ftbquests.locked.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int changeProgress(CommandSourceStack source, Collection<ServerPlayer> players, boolean reset, QuestObjectBase questObject) {
		ProgressChange progressChange = new ProgressChange(ServerQuestFile.INSTANCE);
		progressChange.origin = questObject;
		progressChange.reset = reset;

		for (ServerPlayer player : players) {
			progressChange.player = player.getUUID();
			questObject.forceProgress(ServerQuestFile.INSTANCE.getData(player), progressChange);
		}

		source.sendSuccess(Component.translatable("commands.ftbquests.change_progress.text"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int deleteEmptyRewardTables(CommandSourceStack source) {
		int del = 0;

		for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables) {
			if (table.rewards.isEmpty()) {
				del++;
				table.invalid = true;
				FileUtils.delete(ServerQuestFile.INSTANCE.getFolder().resolve(table.getPath()).toFile());
				new DeleteObjectResponseMessage(table.id).sendToAll(source.getServer());
			}
		}

		ServerQuestFile.INSTANCE.rewardTables.removeIf(rewardTable -> rewardTable.invalid);
		ServerQuestFile.INSTANCE.refreshIDMap();
		ServerQuestFile.INSTANCE.save();

		source.sendSuccess(Component.literal("Deleted " + del + " empty tables"), false);
		return 1;
	}

	private static int generateAllItemChapter(CommandSourceStack source) {
		NonNullList<ItemStack> nonNullList = NonNullList.create();

		for (Map.Entry<ResourceKey<Item>, Item> entry : Registry.ITEM.entrySet()) {
			Item item = entry.getValue();
			try {
				int s = nonNullList.size();
				item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);

				if (s == nonNullList.size()) {
					nonNullList.add(new ItemStack(item));
				}
			} catch (Throwable ex) {
				FTBQuests.LOGGER.warn("Failed to get items from " + entry.getKey() + ": " + ex);
			}
		}

		Chapter chapter = new Chapter(ServerQuestFile.INSTANCE, ServerQuestFile.INSTANCE.defaultChapterGroup);
		chapter.id = chapter.file.newID();
		chapter.onCreated();

		chapter.title = "Generated chapter of all items in search creative tab [" + nonNullList.size() + "]";
		chapter.icon = new ItemStack(Items.COMPASS);
		chapter.defaultQuestShape = "rsquare";

		new CreateObjectResponseMessage(chapter, null).sendToAll(source.getServer());

		List<ItemStack> list = nonNullList.stream()
				.filter(stack -> !stack.isEmpty() && Registries.getId(stack.getItem(), Registry.ITEM_REGISTRY) != null)
				.sorted(Comparator.comparing(a -> Registries.getId(a.getItem(), Registry.ITEM_REGISTRY)))
				.toList();
		FTBQuests.LOGGER.info("Found " + nonNullList.size() + " items in total, chapter ID: " + chapter);

		if (list.isEmpty()) {
			return 0;
		}

		int col = 0;
		int row = 0;
		String modid = Registries.getId(list.get(0).getItem(), Registry.ITEM_REGISTRY).getNamespace();

		for (ItemStack stack : list) {
			ResourceLocation id = Registries.getId(stack.getItem(), Registry.ITEM_REGISTRY);
			if (!modid.equals(id.getNamespace())) {
				modid = id.getNamespace();
				col = 0;
				row += 2;
			} else if (col >= 40) {
				col = 0;
				row++;
			}

			Quest quest = new Quest(chapter);
			quest.id = chapter.file.newID();
			quest.onCreated();
			quest.x = col;
			quest.y = row;
			quest.subtitle = stack.save(new CompoundTag()).toString();

			new CreateObjectResponseMessage(quest, null).sendToAll(source.getServer());

			ItemTask task = new ItemTask(quest);
			task.id = chapter.file.newID();
			task.onCreated();

			task.consumeItems = Tristate.TRUE;
			task.item = stack;

			CompoundTag extra = new CompoundTag();
			extra.putString("type", task.getType().getTypeForNBT());
			new CreateObjectResponseMessage(task, extra).sendToAll(source.getServer());

			col++;
		}

		ServerQuestFile.INSTANCE.save();
		ServerQuestFile.INSTANCE.saveNow();
		source.sendSuccess(Component.literal("Done!"), false);
		return 1;
	}

	private static final Set<UUID> warnedPlayers = new HashSet<>();
	private static int doReload(CommandSourceStack source) throws CommandSyntaxException {
		ServerQuestFile instance = ServerQuestFile.INSTANCE;
		ServerPlayer sender = source.getPlayerOrException();

		if (!instance.getData(sender).getCanEdit()) {
			source.sendFailure(Component.translatable("commands.ftbquests.command.error.not_editing"));
			return 1;
		}

		instance.load();
		new SyncQuestsMessage(instance).sendToAll(source.getServer());
		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			TeamData data = instance.getData(player);
			for (TeamData teamData: instance.getAllData()) {
				new SyncTeamDataMessage(teamData, teamData == data).sendTo(player);
			}
		}

		source.sendSuccess(Component.translatable("commands.ftbquests.command.feedback.reloaded"), false);
		if (!warnedPlayers.contains(sender.getUUID())) {
			source.sendSuccess(Component.translatable("commands.ftbquests.command.feedback.reloaded.disclaimer").withStyle(ChatFormatting.GOLD), false);
			warnedPlayers.add(sender.getUUID());
		}

		return 1;
	}
}
