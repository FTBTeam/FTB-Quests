package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClientQuestProgress implements ITeamData
{
	private final String teamID;
	public final Map<QuestTask, QuestTaskData> taskData;

	public ClientQuestProgress(String t)
	{
		teamID = t;
		taskData = new HashMap<>();
	}

	@Override
	public String getTeamID()
	{
		return teamID;
	}

	@Override
	public QuestTaskData getQuestTaskData(QuestTask task)
	{
		QuestTaskData data = taskData.get(task);

		if (data == null)
		{
			return task.createData(this);
		}

		return data;
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
	}

	@Override
	public void removeTask(QuestTask task)
	{
		taskData.remove(task);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		taskData.put(task, task.createData(this));
	}

	@Override
	public IntCollection getClaimedRewards(UUID player)
	{
		return ClientQuestFile.INSTANCE.rewards;
	}

	@Override
	public boolean isRewardClaimed(UUID player, QuestReward reward)
	{
		return ClientQuestFile.INSTANCE.rewards.contains(reward.uid);
	}

	@Override
	public void claimReward(EntityPlayer player, QuestReward reward)
	{
		ClientQuestFile.INSTANCE.rewards.add(reward.uid);
	}

	@Override
	public void unclaimRewards(Collection<QuestReward> rewards)
	{
		for (QuestReward reward : rewards)
		{
			ClientQuestFile.INSTANCE.rewards.rem(reward.uid);
		}
	}

	public static String getCompletionSuffix(@Nullable ClientQuestProgress progress, QuestObject object)
	{
		if (!GuiScreen.isShiftKeyDown())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(' ');
		builder.append(TextFormatting.DARK_GRAY);

		if (progress == null)
		{
			builder.append("???");
		}
		else
		{
			builder.append(object.getRelativeProgress(progress));
			builder.append('%');
		}

		if (GuiScreen.isCtrlKeyDown())
		{
			builder.append(' ');
			builder.append(object.getID());
		}

		return builder.toString();
	}
}