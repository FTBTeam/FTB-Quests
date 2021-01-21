package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.net.MessageCreateObjectResponse;
import com.feed_the_beast.ftbquests.net.MessageEditObjectResponse;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class FTBQuestsCommands
{
	private static final Predicate<CommandSourceStack> permission = s -> s.getServer().isSingleplayer() || s.hasPermission(2);

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("ftbquests")
				.then(Commands.literal("editing_mode")
						.requires(permission)
						.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), null))
						.then(Commands.argument("mode", BoolArgumentType.bool())
								.requires(permission)
								.executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "mode")))
								.then(Commands.argument("player", EntityArgument.player())
										.requires(permission)
										.executes(c -> editingMode(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "mode")))
								)
						)
				)
				.then(Commands.literal("change_progress")
				)
				.then(Commands.literal("export_rewards_to_chest")
						.requires(permission)
						.then(Commands.argument("reward_table", StringArgumentType.word())
								.requires(permission)
								.executes(c -> exportRewards(c.getSource(), StringArgumentType.getString(c, "reward_table")))
						)
				)
				.then(Commands.literal("import_rewards_from_chest")
						.requires(permission)
						.then(Commands.argument("reward_table", StringArgumentType.word())
								.requires(permission)
								.then(Commands.argument("weight", IntegerArgumentType.integer(1))
										.requires(permission)
										.then(Commands.argument("replace", BoolArgumentType.bool())
												.requires(permission)
												.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), IntegerArgumentType.getInteger(c, "weight"), BoolArgumentType.getBool(c, "replace")))
										)
										.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), IntegerArgumentType.getInteger(c, "weight"), false))
								)
								.executes(c -> importRewards(c.getSource(), StringArgumentType.getString(c, "reward_table"), 1, false))
						)
				)
				.then(Commands.literal("generate_chapter_with_all_items_in_game")
						.requires(permission)
						.executes(context -> generateAllItemChapter(context.getSource()))
				)
		);
	}

	private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit)
	{
		PlayerData data = ServerQuestFile.INSTANCE.getData(player);

		if (canEdit == null)
		{
			canEdit = !data.getCanEdit();
		}

		data.setCanEdit(canEdit);

		if (canEdit)
		{
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
		}
		else
		{
			source.sendSuccess(new TranslatableComponent("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int changeProgress(CommandSourceStack source, ServerPlayer player, ChangeProgress type)
	{
		/*
		Collection<ForgeTeam> teams;

		if (args.length == 1)
		{
			teams = Collections.singleton(Universe.get().getPlayer(getCommandSenderAsPlayer(sender)).team);
		}
		else if (args[1].equals("*"))
		{
			teams = Universe.get().getTeams();
		}
		else
		{
			ForgeTeam team = Universe.get().getTeam(args[1]);

			if (!team.isValid())
			{
				throw new CommandException("ftblib.lang.team.error.not_found", args[1]);
			}

			teams = Collections.singleton(team);
		}

		QuestObject object = args.length == 2 ? ServerQuestFile.INSTANCE : ServerQuestFile.INSTANCE.get(ServerQuestFile.INSTANCE.getID(args[2]));

		if (object == null)
		{
			throw CommandUtils.error(SidedUtils.lang(sender, FTBQuests.MOD_ID, "commands.ftbquests.change_progress.invalid_id", args[2]));
		}

		for (ForgeTeam team : teams)
		{
			object.forceProgress(ServerQuestData.get(team), type, true);
		}
		 */

		source.sendSuccess(new TranslatableComponent("commands.ftbquests.change_progress.text"), true);
		return 1;
	}

	private static HitResult rayTrace(ServerPlayer player)
	{
		float f = player.xRot;
		float f1 = player.yRot;
		Vec3 vec3d = player.getEyePosition(1.0F);
		float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		Vec3 vec3d1 = vec3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return player.level.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}

	private static int importRewards(CommandSourceStack source, String tableId, int weight, boolean replace) throws CommandSyntaxException
	{
		ServerPlayer player = source.getPlayerOrException();
		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(tableId);

		HitResult ray = rayTrace(player);

		if (ray instanceof BlockHitResult)
		{
			BlockEntity tileEntity = player.level.getBlockEntity(((BlockHitResult) ray).getBlockPos());

			if (tileEntity != null)
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ((BlockHitResult) ray).getDirection()).orElse(null);

				if (handler != null)
				{
					if (replace)
					{
						table.rewards.clear();
					}

					int r = 0;

					for (int i = 0; i < handler.getSlots(); i++)
					{
						ItemStack stack = handler.getStackInSlot(i);

						if (!stack.isEmpty())
						{
							table.rewards.add(new WeightedReward(new ItemReward(table.fakeQuest, stack.copy()), weight));
							r++;
						}
					}

					ServerQuestFile.INSTANCE.clearCachedData();
					new MessageEditObjectResponse(table).sendToAll();
					ServerQuestFile.INSTANCE.save();
					source.sendSuccess(new TranslatableComponent("commands.ftbquests.import_rewards_from_chest.text", r, table.toString()), true);
					return 1;
				}
			}
		}

		return 0;
	}

	private static int exportRewards(CommandSourceStack source, String tableId) throws CommandSyntaxException
	{
		ServerPlayer player = source.getPlayerOrException();
		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(tableId);

		HitResult ray = rayTrace(player);

		if (ray instanceof BlockHitResult)
		{
			BlockEntity tileEntity = player.level.getBlockEntity(((BlockHitResult) ray).getBlockPos());

			if (tileEntity != null)
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ((BlockHitResult) ray).getDirection()).orElse(null);

				if (handler != null)
				{
					int r = 0;

					for (WeightedReward reward : table.rewards)
					{
						Object object = reward.reward.getIngredient();

						if (object instanceof ItemStack && !((ItemStack) object).isEmpty())
						{
							ItemStack stack1 = ((ItemStack) object).copy();
							stack1.setCount(1);

							if (ItemHandlerHelper.insertItem(handler, stack1, false).isEmpty())
							{
								r++;
							}
						}
					}

					source.sendSuccess(new TranslatableComponent("commands.ftbquests.export_rewards_to_chest.text", Integer.toString(r), Integer.toString(table.rewards.size()), table.toString()), true);
				}
			}
		}

		return 0;
	}

	private static int generateAllItemChapter(CommandSourceStack source)
	{
		NonNullList<ItemStack> nonNullList = NonNullList.create();

		for (Map.Entry<ResourceKey<Item>, Item> entry : Registry.ITEM.entrySet())
		{
			Item item = entry.getValue();
			try
			{
				int s = nonNullList.size();
				item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);

				if (s == nonNullList.size())
				{
					nonNullList.add(new ItemStack(item));
				}
			}
			catch (Throwable ex)
			{
				FTBQuests.LOGGER.warn("Failed to get items from " + entry.getKey() + ": " + ex);
			}
		}

		Chapter chapter = new Chapter(ServerQuestFile.INSTANCE);
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

		if (list.isEmpty())
		{
			return 0;
		}

		int col = 0;
		int row = 0;
		String modid = Registries.getId(list.get(0).getItem(), Registry.ITEM_REGISTRY).getNamespace();

		for (ItemStack stack : list)
		{
			ResourceLocation id = Registries.getId(stack.getItem(), Registry.ITEM_REGISTRY);
			if (!modid.equals(id.getNamespace()))
			{
				modid = id.getNamespace();
				col = 0;
				row += 2;
			}
			else if (col >= 40)
			{
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