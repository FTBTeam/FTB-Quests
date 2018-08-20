package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class ClientQuestProgress implements IProgressData
{
	private final String teamID;
	public final Map<QuestTask, QuestTaskData> taskData;
	public final Collection<QuestReward> claimedRewards;

	public ClientQuestProgress(String t)
	{
		teamID = t;
		taskData = new HashMap<>();
		claimedRewards = new HashSet<>();
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
			throw new IllegalArgumentException("Missing data for task " + task);
		}

		return data;
	}

	public void setRewardStatus(QuestReward reward, boolean status)
	{
		if (status)
		{
			claimedRewards.add(reward);
		}
		else
		{
			claimedRewards.remove(reward);
		}
	}

	@Override
	public boolean claimReward(EntityPlayer player, QuestReward reward)
	{
		if (!claimedRewards.contains(reward) && reward.quest.isComplete(this))
		{
			claimedRewards.add(reward);
			return true;
		}

		return false;
	}

	@Override
	public Collection<QuestReward> getClaimedRewards(EntityPlayer player)
	{
		return claimedRewards;
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
	public void unclaimReward(QuestReward reward)
	{
		claimedRewards.remove(reward);
	}

	public static String getCompletionSuffix(@Nullable ClientQuestProgress progress, ProgressingQuestObject object)
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
			double d = object.getRelativeProgress(progress);

			if (d <= 0D)
			{
				builder.append("0%");
			}
			else if (d >= 1D)
			{
				builder.append("100%");
			}
			else
			{
				builder.append((int) (d * 100D));
				builder.append('%');
			}
		}

		if (GuiScreen.isCtrlKeyDown())
		{
			builder.append(' ');
			builder.append(object.getID());
		}

		return builder.toString();
	}
}