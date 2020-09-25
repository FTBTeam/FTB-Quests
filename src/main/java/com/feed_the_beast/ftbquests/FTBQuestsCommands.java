package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.net.MessageEditObjectResponse;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class FTBQuestsCommands
{
	private static final Predicate<CommandSource> permission = s -> s.getServer().isSinglePlayer() || s.hasPermissionLevel(2);

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("ftbquests")
				.then(Commands.literal("editing_mode")
						.requires(permission)
						.executes(c -> editingMode(c.getSource(), c.getSource().asPlayer(), null))
						.then(Commands.argument("mode", BoolArgumentType.bool())
								.requires(permission)
								.executes(c -> editingMode(c.getSource(), c.getSource().asPlayer(), BoolArgumentType.getBool(c, "mode")))
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
		);
	}

	private static int editingMode(CommandSource source, ServerPlayerEntity player, @Nullable Boolean canEdit)
	{
		PlayerData data = ServerQuestFile.INSTANCE.getData(player);

		if (canEdit == null)
		{
			canEdit = !data.getCanEdit();
		}

		data.setCanEdit(canEdit);

		if (canEdit)
		{
			source.sendFeedback(new TranslationTextComponent("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
		}
		else
		{
			source.sendFeedback(new TranslationTextComponent("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
		}

		return 1;
	}

	private static int changeProgress(CommandSource source, ServerPlayerEntity player, ChangeProgress type)
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

		source.sendFeedback(new TranslationTextComponent("commands.ftbquests.change_progress.text"), true);
		return 1;
	}

	private static RayTraceResult rayTrace(ServerPlayerEntity player)
	{
		float f = player.rotationPitch;
		float f1 = player.rotationYaw;
		Vector3d vec3d = player.getEyePosition(1.0F);
		float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
		float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		Vector3d vec3d1 = vec3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return player.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d1, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
	}

	private static int importRewards(CommandSource source, String tableId, int weight, boolean replace) throws CommandSyntaxException
	{
		ServerPlayerEntity player = source.asPlayer();
		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(tableId);

		RayTraceResult ray = rayTrace(player);

		if (ray instanceof BlockRayTraceResult)
		{
			TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult) ray).getPos());

			if (tileEntity != null)
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ((BlockRayTraceResult) ray).getFace()).orElse(null);

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
					source.sendFeedback(new TranslationTextComponent("commands.ftbquests.import_rewards_from_chest.text", r, table.toString()), true);
					return 1;
				}
			}
		}

		return 0;
	}

	private static int exportRewards(CommandSource source, String tableId) throws CommandSyntaxException
	{
		ServerPlayerEntity player = source.asPlayer();
		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(tableId);

		RayTraceResult ray = rayTrace(player);

		if (ray instanceof BlockRayTraceResult)
		{
			TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult) ray).getPos());

			if (tileEntity != null)
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ((BlockRayTraceResult) ray).getFace()).orElse(null);

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

					source.sendFeedback(new TranslationTextComponent("commands.ftbquests.export_rewards_to_chest.text", Integer.toString(r), Integer.toString(table.rewards.size()), table.toString()), true);
				}
			}
		}

		return 0;
	}
}