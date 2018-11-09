package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.command.CommandUtils;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.SidedUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandResetProgress extends CommandBase
{
	@Override
	public String getName()
	{
		return "reset_progress";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests.reset_progress.usage";
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
			return getListOfStringsMatchingLastWord(args, Universe.get().getTeams());
		}

		return super.getTabCompletions(server, sender, args, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 1)
		{
			throw new WrongUsageException(getUsage(sender));
		}

		Collection<ForgeTeam> teams;

		if (args[0].equals("*"))
		{
			teams = Universe.get().getTeams();
		}
		else
		{
			ForgeTeam team = Universe.get().getTeam(args[0]);

			if (!team.isValid())
			{
				throw FTBLib.error(sender, "ftblib.lang.team.error.not_found", args[0]);
			}

			teams = Collections.singleton(team);
		}

		QuestObject object = args.length == 1 ? ServerQuestFile.INSTANCE : ServerQuestFile.INSTANCE.get(ServerQuestFile.INSTANCE.getID(args[1]));

		if (object == null)
		{
			throw CommandUtils.error(SidedUtils.lang(sender, FTBQuests.MOD_ID, "commands.ftbquests.reset_progress.invalid_id", args[1]));
		}

		for (ForgeTeam team : teams)
		{
			object.resetProgress(FTBQuestsTeamData.get(team), true);
		}

		sender.sendMessage(new TextComponentTranslation("commands.ftbquests.reset_progress.reset"));
	}
}