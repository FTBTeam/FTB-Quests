package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;

public class FTBQuestsNetHandler
{
	static final NetworkWrapper GENERAL = NetworkWrapper.newWrapper(FTBQuests.MOD_ID);

	public static void init()
	{
		GENERAL.register(new MessageSyncQuests());
		GENERAL.register(new MessageUpdateQuestTaskProgress());
		GENERAL.register(new MessageSelectQuestTaskOpenGui());
		GENERAL.register(new MessageSelectQuestTask());
	}
}