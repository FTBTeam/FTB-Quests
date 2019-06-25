package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.lib.command.CommandUtils;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.SidedUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
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
public class CommandChangeProgress extends CommandFTBQuestsBase
{
	@Override
	public String getName()
	{
		return "change_progress";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, EnumChangeProgress.NAME_MAP.values);
		}
		else if (args.length == 2)
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

		EnumChangeProgress type = EnumChangeProgress.NAME_MAP.get(args[0]);

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

		QuestObject object = args.length == 2 ? ServerQuestFile.INSTANCE : ServerQuestFile.INSTANCE.get(QuestFile.getID(args[2]));

		if (object == null)
		{
			throw CommandUtils.error(SidedUtils.lang(sender, FTBQuests.MOD_ID, "commands.ftbquests.change_progress.invalid_id", args[2]));
		}

		for (ForgeTeam team : teams)
		{
			object.forceProgress(ServerQuestData.get(team), type, true);
		}

		sender.sendMessage(new TextComponentTranslation("commands.ftbquests.change_progress.text"));
	}
}