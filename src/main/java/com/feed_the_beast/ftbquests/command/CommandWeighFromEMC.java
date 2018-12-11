package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObjectResponse;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.RewardTable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandWeighFromEMC extends CommandBase
{
	@Override
	public String getName()
	{
		return "weigh_from_emc";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests.weigh_from_emc.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof EntityPlayerMP ? FTBQuests.canEdit((EntityPlayerMP) sender) : super.checkPermission(server, sender);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		if (args.length == 1)
		{
			List<String> list = new ArrayList<>(ServerQuestFile.INSTANCE.rewardTables.size() + 1);

			for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables)
			{
				list.add(table.getCodeString());
			}

			return getListOfStringsMatchingLastWord(args, list);
		}

		return super.getTabCompletions(server, sender, args, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 2)
		{
			throw new WrongUsageException(getUsage(sender));
		}

		RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(ServerQuestFile.INSTANCE.getID(args[0]));

		if (table == null)
		{
			throw FTBLib.error(sender, "commands.ftbquests.weigh_from_emc.invalid_id", args[0]);
		}

		ServerQuestFile.INSTANCE.clearCachedData();
		new MessageEditObjectResponse(table).sendToAll();
		ServerQuestFile.INSTANCE.save();
		sender.sendMessage(new TextComponentTranslation("commands.ftbquests.weigh_from_emc.text", table.getDisplayName()));
	}
}