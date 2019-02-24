package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandEditingMode extends CommandFTBQuestsBase
{
	@Override
	public String getName()
	{
		return "editing_mode";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return server.isSinglePlayer() || super.checkPermission(server, sender);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "true", "false");
		}

		return Collections.emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
		boolean mode;

		if (args.length == 0 || args[0].equals("toggle"))
		{
			mode = !NBTUtils.getPersistedData(player, false).getBoolean("ftbquests_editing_mode");
		}
		else
		{
			mode = parseBoolean(args[0]);
		}

		NBTUtils.getPersistedData(player, mode).setBoolean("ftbquests_editing_mode", mode);
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