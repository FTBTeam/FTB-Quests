package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class ClientQuestProgress implements IProgressData
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
			throw new IllegalArgumentException("Missing data for task " + task);
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