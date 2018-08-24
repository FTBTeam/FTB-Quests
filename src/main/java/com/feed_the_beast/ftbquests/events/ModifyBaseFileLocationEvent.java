package com.feed_the_beast.ftbquests.events;

import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author LatvianModder
 */
public class ModifyBaseFileLocationEvent extends FTBQuestsEvent
{
	private final MinecraftServer server;
	private File file = null;

	public ModifyBaseFileLocationEvent(MinecraftServer s)
	{
		server = s;
	}

	public void setFile(File f)
	{
		file = f;
	}

	public MinecraftServer getServer()
	{
		return server;
	}

	@Nullable
	public File getFile()
	{
		return file;
	}
}