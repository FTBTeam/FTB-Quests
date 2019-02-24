package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectResponse;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandImportRewardsFromChest extends CommandFTBQuestsBase
{
	@Override
	public String getName()
	{
		return "import_rewards_from_chest";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		if (args.length == 3)
		{
			return getListOfStringsMatchingLastWord(args, "true", "false");
		}
		else if (args.length == 1)
		{
			List<String> list = new ArrayList<>(ServerQuestFile.INSTANCE.rewardTables.size());

			for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null)
				{
					list.add(table.lootCrate.stringID);
				}
			}

			for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate == null)
				{
					list.add(table.getCodeString());
				}
			}

			return getListOfStringsMatchingLastWord(args, list);
		}

		return super.getTabCompletions(server, sender, args, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);

		if (args.length < 1)
		{
			throw new WrongUsageException(getUsage(sender));
		}

		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(args[0]);

		if (table == null)
		{
			throw FTBLib.error(sender, "commands.ftbquests.import_rewards_from_chest.invalid_id", args[0]);
		}

		int weight = args.length >= 2 ? parseInt(args[1], 1, Integer.MAX_VALUE) : 1;
		boolean replace = args.length >= 3 && parseBoolean(args[2]);

		RayTraceResult ray = MathUtils.rayTrace(player, false);

		if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			TileEntity tileEntity = player.world.getTileEntity(ray.getBlockPos());

			if (tileEntity != null)
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ray.sideHit);

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
							ItemReward itemReward = new ItemReward(table.fakeQuest);
							itemReward.stack = stack.copy();
							table.rewards.add(new WeightedReward(itemReward, weight));
							r++;
						}
					}

					ServerQuestFile.INSTANCE.clearCachedData();
					new MessageEditObjectResponse(table).sendToAll();
					ServerQuestFile.INSTANCE.save();
					sender.sendMessage(new TextComponentTranslation("commands.ftbquests.import_rewards_from_chest.text", r, table.getDisplayName()));
				}
			}
		}
	}
}