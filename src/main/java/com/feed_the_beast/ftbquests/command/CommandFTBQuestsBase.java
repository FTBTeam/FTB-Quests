package com.feed_the_beast.ftbquests.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/**
 * @author LatvianModder
 */
public abstract class CommandFTBQuestsBase extends CommandBase
{
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests." + getName() + ".usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}
}