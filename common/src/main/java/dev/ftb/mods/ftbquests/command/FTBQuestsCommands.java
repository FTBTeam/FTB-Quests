package dev.ftb.mods.ftbquests.command;

import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.MessageCreateObjectResponse;
import dev.ftb.mods.ftbquests.net.MessageDeleteObjectResponse;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.util.FileUtils;
import me.shedaniel.architectury.registry.Registries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class FTBQuestsCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ftbquests")
				.requires(s -> s.getServer().isSingleplayer() || s.hasPermission(2))
				.then(Commands.literal("editing_mode")
						.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(Commands.argument("mode", BoolArgumentType.bool())
								.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "mode")))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(c -> editingMode(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "mode")))
								)
						)
				)
				.then(Commands.literal("delete_empty_reward_tables")
						.executes(context -> deleteEmptyRewardTables(context.getSource()))
				)
				.then(Commands.literal("change_progress")
						.requires(s -> s.hasPermission(2))
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("type", ChangeProgressArgument.changeProgress())
										.then(Commands.argument("quest_object", QuestObjectArgument.questObject())
												.executes(ctx -> {
													Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
													ChangeProgress type = ctx.getArgument("type", ChangeProgress.class);
													QuestObjectBase questObject = ctx.getArgument("quest_object", QuestObjectBase.class);
													return changeProgress(ctx.getSource(), players, type, questObject);
												})
										)
								)
						)
				)
				/*.then(Commands.literal("export_rewards_to_chest")
						.then(Commands.argument("reward_table", StringArgumentType.word())
								.executes(c -> exportRewards(c.getSource(), StringArgumentType.getString(c, "reward_table")))
						)
				)
				.then(Commands.literal("import_rewards_from_chest")
						.requires(permission)
						.then(Commands.argument("reward_table", StringArgumentType.word())
								.then(Commands.argument("weight", IntegerArgumentType.integer(1))
										.then(Commands.argument("replace", BoolArgumentType.bool())
												.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), IntegerArgumentType.getInteger(c, "weight"), BoolArgumentType.getBool(c, "replace")))
										)
										.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), IntegerArgumentType.getInteger(c, "weight"), false))
								)
								.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), 1, false))
						)
				)*/
				.then(Commands.literal("generate_chapter_with_all_items_in_game")
						.executes(context -> generateAllItemChapter(context.getSource()))
				)
		);
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

	private static int changeProgress(CommandSourceStack source, Collection<ServerPlayer> players, ChangeProgress type, QuestObjectBase questObject) {
		for (ServerPlayer player : players) {
			questObject.forceProgress(new Date(), ServerQuestFile.INSTANCE.getData(player), player.getUUID(), type);
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
				new MessageDeleteObjectResponse(table.id).sendToAll();
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

		new MessageCreateObjectResponse(chapter, null).sendToAll();

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

			new MessageCreateObjectResponse(quest, null).sendToAll();

			ItemTask task = new ItemTask(quest);
			task.id = chapter.file.newID();
			task.onCreated();

			task.consumeItems = Tristate.TRUE;
			task.item = stack;

			CompoundTag extra = new CompoundTag();
			extra.putString("type", task.getType().getTypeForNBT());
			new MessageCreateObjectResponse(task, extra).sendToAll();

			col++;
		}

		ServerQuestFile.INSTANCE.save();
		ServerQuestFile.INSTANCE.saveNow();
		source.sendSuccess(new TextComponent("Done!"), false);
		return 1;
	}
}