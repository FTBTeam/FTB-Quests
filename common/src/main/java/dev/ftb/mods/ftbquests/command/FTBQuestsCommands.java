package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.CreateObjectResponsePacket;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponsePacket;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import me.shedaniel.architectury.registry.Registries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class FTBQuestsCommands {

	private static SimpleCommandExceptionType NO_INVENTORY = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbquests.command.error.no_inventory"));

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
											BlockPos pos = BlockPosArgument.getOrLoadBlockPos(ctx, "pos");
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
											BlockPos pos = BlockPosArgument.getOrLoadBlockPos(ctx, "pos");
											return importRewards(ctx.getSource(), name, pos);
										})
								)
						)
				)
				.then(Commands.literal("generate_chapter_with_all_items_in_game")
						.executes(context -> generateAllItemChapter(context.getSource()))
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
		if (!(be instanceof BaseContainerBlockEntity)) {
			throw NO_INVENTORY.create();
		}

		BaseContainerBlockEntity container = (BaseContainerBlockEntity) be;
		container.clearContent();

		int s = 0;
		for (WeightedReward reward : table.rewards) {
			if (s >= container.getContainerSize()) {
				source.sendSuccess(new TranslatableComponent("commands.ftbquests.command.feedback.table_too_many_items", table.getTitle()), false);
				return 0;
			} else if (!(reward.reward instanceof ItemReward)) {
				continue;
			}
			container.setItem(s++, ((ItemReward) reward.reward).item);
		}

		source.sendSuccess(new TranslatableComponent("commands.ftbquests.command.feedback.table_imported", table.getTitle(), table.rewards.size()), false);

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
		if (!(be instanceof BaseContainerBlockEntity)) {
			throw NO_INVENTORY.create();
		}

		BaseContainerBlockEntity container = (BaseContainerBlockEntity) be;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				table.rewards.add(new WeightedReward(new ItemReward(table.fakeQuest, stack), 1));
			}
		}


		file.rewardTables.add(table);
		file.save();

		new CreateObjectResponsePacket(table, null).sendToAll(level.getServer());

		source.sendSuccess(new TranslatableComponent("commands.ftbquests.command.feedback.table_imported", name, table.rewards.size()), false);

		return 1;
	}

	private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit) {
		TeamData data = ServerQuestFile.INSTANCE.getData(player);

		if (canEdit == null) {
			canEdit = !data.getCanEdit();
		}

		data.setCanEdit(canEdit);

		if (canEdit) {
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
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
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.locked.enabled", player.getDisplayName()), true);
		} else {
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.locked.disabled", player.getDisplayName()), true);
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

		source.sendSuccess(new TranslatableComponent("commands.ftbquests.change_progress.text"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int deleteEmptyRewardTables(CommandSourceStack source) {
		int del = 0;

		for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables) {
			if (table.rewards.isEmpty()) {
				del++;
				table.invalid = true;
				FileUtils.delete(ServerQuestFile.INSTANCE.getFolder().resolve(table.getPath()).toFile());
				new DeleteObjectResponsePacket(table.id).sendToAll(source.getServer());
			}
		}

		ServerQuestFile.INSTANCE.rewardTables.removeIf(rewardTable -> rewardTable.invalid);
		ServerQuestFile.INSTANCE.refreshIDMap();
		ServerQuestFile.INSTANCE.save();

		source.sendSuccess(new TextComponent("Deleted " + del + " empty tables"), false);
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

		new CreateObjectResponsePacket(chapter, null).sendToAll(source.getServer());

		List<ItemStack> list = nonNullList.stream()
				.filter(stack -> !stack.isEmpty() && Registries.getId(stack.getItem(), Registry.ITEM_REGISTRY) != null)
				.sorted(Comparator.comparing(a -> Registries.getId(a.getItem(), Registry.ITEM_REGISTRY)))
				.collect(Collectors.toList());
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

			new CreateObjectResponsePacket(quest, null).sendToAll(source.getServer());

			ItemTask task = new ItemTask(quest);
			task.id = chapter.file.newID();
			task.onCreated();

			task.consumeItems = Tristate.TRUE;
			task.item = stack;

			CompoundTag extra = new CompoundTag();
			extra.putString("type", task.getType().getTypeForNBT());
			new CreateObjectResponsePacket(task, extra).sendToAll(source.getServer());

			col++;
		}

		ServerQuestFile.INSTANCE.save();
		ServerQuestFile.INSTANCE.saveNow();
		source.sendSuccess(new TextComponent("Done!"), false);
		return 1;
	}
}