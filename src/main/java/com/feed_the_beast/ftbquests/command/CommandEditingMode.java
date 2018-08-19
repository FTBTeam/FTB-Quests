package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author LatvianModder
 */
public class CommandEditingMode extends CommandBase
{
	@Override
	public String getName()
	{
		return "editing_mode";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.ftbquests.editing_mode.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return server.isSinglePlayer() || super.checkPermission(server, sender);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
		boolean mode;

		if (args.length == 0 || args[0].equals("toggle"))
		{
			mode = !player.getEntityData().getBoolean("ftbquests_editing_mode");
		}
		else
		{
			mode = parseBoolean(args[0]);
		}

		player.getEntityData().setBoolean("ftbquests_editing_mode", mode);
		new MessageSyncEditingMode(mode).sendTo(player);

		if (mode)
		{
			sender.sendMessage(new TextComponentTranslation("commands.ftbquests.editing_mode.enabled", player.getDisplayName()));
		}
		else
		{
			sender.sendMessage(new TextComponentTranslation("commands.ftbquests.editing_mode.disabled", player.getDisplayName()));
		}
	}
}