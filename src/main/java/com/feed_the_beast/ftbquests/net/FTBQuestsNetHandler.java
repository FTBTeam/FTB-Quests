package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;

public class FTBQuestsNetHandler
{
	static final NetworkWrapper QUESTS = NetworkWrapper.newWrapper(FTBQuests.MOD_ID);

	public static void init()
	{
		QUESTS.register(1, new MessageSyncQuests());
	}
}