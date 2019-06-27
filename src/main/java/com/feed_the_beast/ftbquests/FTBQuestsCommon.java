package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FTBQuestsCommon
{
	public void preInit()
	{
	}

	@Nullable
	public QuestFile getQuestFile(@Nullable World world)
	{
		return ServerQuestFile.INSTANCE;
	}

	@Nullable
	public QuestFile getQuestFile(boolean clientSide)
	{
		return ServerQuestFile.INSTANCE;
	}

	public void setTaskGuiProviders()
	{
	}

	public void setRewardGuiProviders()
	{
	}
}