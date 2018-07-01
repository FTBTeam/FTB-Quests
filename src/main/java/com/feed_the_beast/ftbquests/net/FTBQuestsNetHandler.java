package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;

public class FTBQuestsNetHandler
{
	static final NetworkWrapper GENERAL = NetworkWrapper.newWrapper(FTBQuests.MOD_ID);

	public static void init()
	{
		GENERAL.register(new MessageSyncQuests());
		GENERAL.register(new MessageEditQuests());
		GENERAL.register(new MessageOpenTaskGui());
		GENERAL.register(new MessageOpenTask());
		GENERAL.register(new MessageUpdateTaskProgress());
		GENERAL.register(new MessageUpdateRewardStatus());
		GENERAL.register(new MessageSelectTaskGui());
		GENERAL.register(new MessageSelectTask());
		GENERAL.register(new MessageClaimReward());
	}
}