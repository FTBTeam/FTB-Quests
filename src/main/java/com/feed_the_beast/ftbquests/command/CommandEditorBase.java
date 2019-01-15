package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * @author LatvianModder
 */
public abstract class CommandEditorBase extends CommandBase
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

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof EntityPlayerMP ? FTBQuests.canEdit((EntityPlayerMP) sender) : super.checkPermission(server, sender);
	}
}