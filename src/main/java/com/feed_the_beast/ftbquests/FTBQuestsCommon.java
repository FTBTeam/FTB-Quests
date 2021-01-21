package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import javax.annotation.Nullable;
import net.minecraft.world.level.LevelAccessor;

public class FTBQuestsCommon
{
	public void init()
	{
	}

	public QuestFile getQuestFile(LevelAccessor world)
	{
		return ServerQuestFile.INSTANCE;
	}

	@Nullable
	public QuestFile getQuestFile(Tristate clientSide)
	{
		return ServerQuestFile.INSTANCE;
	}

	public void setTaskGuiProviders()
	{
	}

	public void setRewardGuiProviders()
	{
	}

	public PlayerData getClientPlayerData()
	{
		throw new IllegalStateException("Can't access client data from server side!");
	}

	public QuestFile createClientQuestFile()
	{
		throw new IllegalStateException("Can't create client quest file on server side!");
	}
}