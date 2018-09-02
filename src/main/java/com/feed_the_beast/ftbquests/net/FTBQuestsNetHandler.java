package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.FTBQuestsEditNetHandler;

public class FTBQuestsNetHandler
{
	static final NetworkWrapper GENERAL = NetworkWrapper.newWrapper(FTBQuests.MOD_ID);

	public static void init()
	{
		GENERAL.register(new MessageSyncQuests());
		GENERAL.register(new MessageUpdateTaskProgress());
		GENERAL.register(new MessageOpenTask());
		GENERAL.register(new MessageOpenTaskResponse());
		GENERAL.register(new MessageClaimReward());
		GENERAL.register(new MessageClaimRewardResponse());
		GENERAL.register(new MessageUpdateVariable());
		GENERAL.register(new MessageSyncEditingMode());
		GENERAL.register(new MessageGetEmergencyItems());
		GENERAL.register(new MessageCreateTeamData());
		GENERAL.register(new MessageDeleteTeamData());
		GENERAL.register(new MessageChangedTeam());
		GENERAL.register(new MessageResetProgress());
		GENERAL.register(new MessageResetProgressResponse());
		GENERAL.register(new MessageCompleteInstantly());

		FTBQuestsEditNetHandler.init();
	}
}