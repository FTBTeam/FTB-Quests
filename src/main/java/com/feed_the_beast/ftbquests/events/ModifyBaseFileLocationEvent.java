package com.feed_the_beast.ftbquests.events;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author LatvianModder
 */
public class ModifyBaseFileLocationEvent extends FTBQuestsEvent
{
	private File file = null;

	public void setFile(File f)
	{
		file = f;
	}

	@Nullable
	public File getFile()
	{
		return file;
	}
}