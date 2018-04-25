package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;

public class FTBQuestsCommon
{
	public void preInit()
	{
		FTBQuestsConfig.sync();
		FTBQuestsNetHandler.init();
	}

	public void postInit()
	{
	}
}