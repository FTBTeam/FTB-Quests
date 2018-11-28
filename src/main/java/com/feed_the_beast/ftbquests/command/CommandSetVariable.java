package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.command.CommandUtils;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.SidedUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandSetVariable extends CommandBase
{
	@Override
	public String getName()
	{
		return "set_variable";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests.set_variable.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, Universe.get().getTeams());
		}
		else if (args.length == 2)
		{
			List<String> list = new ArrayList<>();

			for (QuestVariable variable : ServerQuestFile.INSTANCE.variables)
			{
				list.add(variable.toString());
			}

			list.sort(null);
			return getListOfStringsMatchingLastWord(args, list);
		}

		return super.getTabCompletions(server, sender, args, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 3)
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

		int var = ServerQuestFile.INSTANCE.getID(args[1]);

		if (ServerQuestFile.INSTANCE.getVariable(var) == null)
		{
			throw CommandUtils.error(SidedUtils.lang(sender, FTBQuests.MOD_ID, "commands.ftbquests.set_variable.invalid_id", args[1]));
		}

		boolean add = args[2].startsWith("~");
		long value = parseLong(add ? args[2].substring(1) : args[2]);

		for (ForgeTeam team : teams)
		{
			FTBQuestsTeamData teamData = FTBQuestsTeamData.get(team);

			if (add)
			{
				teamData.setVariable(var, teamData.getVariable(var) + value);
			}
			else
			{
				teamData.setVariable(var, value);
			}
		}

		sender.sendMessage(new TextComponentTranslation("commands.ftbquests.set_variable.set"));
	}
}