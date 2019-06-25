package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class ClientQuestData extends QuestData
{
	private final short teamUID;
	private final String teamID;
	private final ITextComponent displayName;

	public ClientQuestData(short uid, String id, ITextComponent n)
	{
		teamUID = uid;
		teamID = id;
		displayName = n;
	}

	@Override
	public short getTeamUID()
	{
		return teamUID;
	}

	@Override
	public String getTeamID()
	{
		return teamID;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return displayName;
	}

	@Override
	public QuestFile getFile()
	{
		return ClientQuestFile.INSTANCE;
	}

	public boolean isRewardClaimedSelf(QuestReward reward)
	{
		return isRewardClaimed(Minecraft.getMinecraft().player.getUniqueID(), reward);
	}
}