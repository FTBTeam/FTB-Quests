package com.feed_the_beast.ftbquests.cmd;

import com.feed_the_beast.ftblib.lib.cmd.CmdBase;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * @author LatvianModder
 */
public class CmdQuests extends CmdBase
{
	public CmdQuests()
	{
		super("quests", Level.ALL);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		FTBQuestsClient.openQuestGui();
	}
}