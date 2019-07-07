package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.net.MessageToggleFavorite;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * @author LatvianModder
 */
public class CommandAutoFavorite extends CommandFTBQuestsBase
{
	@Override
	public String getName()
	{
		return "auto_favorite";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		new MessageToggleFavorite(1).onMessage(getCommandSenderAsPlayer(sender));
	}
}