package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;

public class FTBQuestsEditNetHandler
{
	static final NetworkWrapper EDIT = NetworkWrapper.newWrapper(FTBQuests.MOD_ID + "_edit");

	public static void init()
	{
		EDIT.register(new MessageChangeProgress());
		EDIT.register(new MessageChangeProgressResponse());
		EDIT.register(new MessageCreateObject());
		EDIT.register(new MessageCreateObjectResponse());
		EDIT.register(new MessageCreateTaskAt());
		EDIT.register(new MessageDeleteObject());
		EDIT.register(new MessageDeleteObjectResponse());
		EDIT.register(new MessageEditObject());
		EDIT.register(new MessageEditObjectResponse());
		EDIT.register(new MessageEditObjectQuick());
		EDIT.register(new MessageMoveChapter());
		EDIT.register(new MessageMoveChapterResponse());
		EDIT.register(new MessageMoveQuest());
		EDIT.register(new MessageMoveQuestResponse());
	}
}