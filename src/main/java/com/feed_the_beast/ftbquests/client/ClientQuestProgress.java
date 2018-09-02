package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class ClientQuestProgress implements ITeamData
{
	private final String teamID;
	public final Map<QuestTask, QuestTaskData> taskData;
	public final Object2LongOpenHashMap<QuestVariable> variables;

	public ClientQuestProgress(String t)
	{
		teamID = t;
		taskData = new HashMap<>();
		variables = new Object2LongOpenHashMap<>();
		variables.defaultReturnValue(0L);
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
	public void unclaimRewards(Collection<QuestReward> rewards)
	{
		for (QuestReward reward : rewards)
		{
			ClientQuestFile.INSTANCE.rewards.rem(reward.uid);
		}
	}

	@Override
	public long getVariable(QuestVariable variable)
	{
		return variables.getLong(variable);
	}

	@Override
	public void setVariable(QuestVariable variable, long value)
	{
		if (value <= 0L)
		{
			variables.removeLong(variable);
		}
		else
		{
			variables.put(variable, value);
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