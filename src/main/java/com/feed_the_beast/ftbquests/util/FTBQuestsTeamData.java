package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageUpdateQuestTaskProgress;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class FTBQuestsTeamData implements INBTSerializable<NBTTagCompound>, IProgressData
{
	public static FTBQuestsTeamData get(ForgeTeam team)
	{
		return team.getData(FTBQuests.MOD_ID);
	}

	public final ForgeTeam team;
	private final Map<QuestTaskKey, Integer> questProgress = new HashMap<>();
	public final List<QuestReward> rewards = new ArrayList<>();

	public FTBQuestsTeamData(ForgeTeam t)
	{
		team = t;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagCompound questProgressTag = new NBTTagCompound();

		for (Map.Entry<QuestTaskKey, Integer> entry : questProgress.entrySet())
		{
			if (entry.getValue() > 0)
			{
				NBTTagList nbt1 = questProgressTag.getTagList(entry.getKey().quest.getResourceDomain(), Constants.NBT.TAG_COMPOUND);

				if (nbt1.hasNoTags())
				{
					questProgressTag.setTag(entry.getKey().quest.getResourceDomain(), nbt1);
				}

				NBTTagCompound nbt2 = new NBTTagCompound();
				nbt2.setString("Quest", entry.getKey().quest.getResourcePath());
				nbt2.setInteger("Task", entry.getKey().index);
				nbt2.setInteger("Progress", entry.getValue());
				nbt1.appendTag(nbt2);
			}
		}

		nbt.setTag("Progress", questProgressTag);

		NBTTagList rewardsTag = new NBTTagList();

		for (QuestReward reward : rewards)
		{
			NBTBase base = JsonUtils.toNBT(reward.toJson());

			if (base != null)
			{
				rewardsTag.appendTag(base);
			}
		}

		nbt.setTag("Rewards", rewardsTag);

		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		questProgress.clear();

		NBTTagCompound questProgressTag = nbt.getCompoundTag("Progress");

		for (String s : questProgressTag.getKeySet())
		{
			NBTTagList nbt1 = questProgressTag.getTagList(s, Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < nbt1.tagCount(); i++)
			{
				NBTTagCompound nbt2 = nbt1.getCompoundTagAt(i);
				questProgress.put(new QuestTaskKey(new ResourceLocation(s, nbt2.getString("Quest")), nbt2.getInteger("Index")), nbt2.getInteger("Progress"));
			}
		}

		rewards.clear();

		for (NBTBase base : nbt.getTagList("Rewards", Constants.NBT.TAG_COMPOUND))
		{
			QuestReward reward = QuestReward.createReward(JsonUtils.toJson(base));

			if (reward != null)
			{
				rewards.add(reward);
			}
		}
	}

	@Override
	public int getQuestTaskProgress(QuestTaskKey task)
	{
		Integer p = questProgress.get(task);
		return p == null ? 0 : p;
	}

	@Override
	public boolean setQuestTaskProgress(QuestTaskKey key, int progress)
	{
		int prev = getQuestTaskProgress(key);

		if (progress < 0)
		{
			progress = 0;
		}

		if (prev == progress)
		{
			return false;
		}

		if (progress == 0)
		{
			questProgress.remove(key);
		}
		else
		{
			questProgress.put(key, progress);
		}

		team.markDirty();

		for (EntityPlayerMP player : team.universe.server.getPlayerList().getPlayers())
		{
			if (team.universe.getPlayer(player).team.equalsTeam(team))
			{
				new MessageUpdateQuestTaskProgress(key, progress).sendTo(player);
			}
		}

		return true;
	}

	public void reset()
	{
		rewards.clear();
		questProgress.clear();
	}
}