package com.feed_the_beast.ftbquests.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

/**
 * @author LatvianModder
 */
public class CommandFTBQuests extends CommandTreeBase
{
	public CommandFTBQuests()
	{
		addSubcommand(new CommandEditingMode());
	}

	@Override
	public String getName()
	{
		return "ftbquests";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}