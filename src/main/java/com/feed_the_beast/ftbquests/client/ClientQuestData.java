package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

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

	@Override
	public List<EntityPlayer> getOnlineMembers()
	{
		if (this == ClientQuestFile.INSTANCE.self)
		{
			return Collections.singletonList(Minecraft.getMinecraft().player);
		}

		return Collections.emptyList();
	}

	public boolean isRewardClaimedSelf(Reward reward)
	{
		return isRewardClaimed(Minecraft.getMinecraft().player.getUniqueID(), reward);
	}
}