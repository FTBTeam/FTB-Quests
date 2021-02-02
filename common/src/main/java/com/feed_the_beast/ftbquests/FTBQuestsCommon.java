package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class FTBQuestsCommon
{
	public void init()
	{
	}

	public QuestFile getQuestFile(boolean isClient)
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

	public void openGui()
	{
	}

	public void openCustomIconGui(Player player, InteractionHand hand)
	{
	}
}