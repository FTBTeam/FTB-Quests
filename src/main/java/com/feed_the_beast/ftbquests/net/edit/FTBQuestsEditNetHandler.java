package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;

public class FTBQuestsEditNetHandler
{
	static final NetworkWrapper EDIT = NetworkWrapper.newWrapper(FTBQuests.MOD_ID + "_edit");

	public static void init()
	{
		EDIT.register(new MessageDeleteObject());
		EDIT.register(new MessageDeleteObjectResponse());
		EDIT.register(new MessageCreateChapter());
		EDIT.register(new MessageCreateChapterResponse());
		EDIT.register(new MessageCreateQuest());
		EDIT.register(new MessageCreateQuestResponse());
	}
}