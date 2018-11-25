package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClientQuestProgress implements ITeamData
{
	private final short teamUID;
	private final String teamID;
	private final ITextComponent displayName;
	public final Int2ObjectOpenHashMap<QuestTaskData> taskData;
	public final Int2LongOpenHashMap variables;

	public ClientQuestProgress(short uid, String id, ITextComponent n)
	{
		teamUID = uid;
		teamID = id;
		displayName = n;
		taskData = new Int2ObjectOpenHashMap<>();
		variables = new Int2LongOpenHashMap();
		variables.defaultReturnValue(0L);
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
	public QuestTaskData getQuestTaskData(QuestTask task)
	{
		QuestTaskData data = taskData.get(task.uid);

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
		taskData.remove(task.uid);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		taskData.put(task.uid, task.createData(this));
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
	public long getVariable(int variable)
	{
		return variables.get(variable);
	}

	@Override
	public void setVariable(int variable, long value)
	{
		if (value <= 0L)
		{
			variables.remove(variable);
		}
		else
		{
			variables.put(variable, value);
		}
	}

	@Override
	public boolean isRewardClaimed(UUID player, QuestReward reward)
	{
		return ClientQuestFile.INSTANCE.rewards.contains(reward.uid);
	}

	public static String getCompletionSuffix(@Nullable ClientQuestProgress progress, QuestObject object)
	{
		if (!GuiScreen.isShiftKeyDown() && !(object instanceof QuestTask))
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatting.DARK_GRAY);

		if (progress == null)
		{
			builder.append(' ');
			builder.append("???");
		}
		else
		{
			if (object instanceof QuestTask)
			{
				QuestTask task = (QuestTask) object;

				if (!task.hideProgressNumbers())
				{
					QuestTaskData data = progress.getQuestTaskData(task);
					builder.append(" [");
					builder.append(data.getProgressString());
					builder.append(" / ");
					builder.append(task.getMaxProgressString());
					builder.append(']');
				}
			}
			else
			{
				builder.append(' ');
				builder.append(object.getRelativeProgress(progress));
				builder.append('%');
			}
		}

		if (GuiScreen.isCtrlKeyDown())
		{
			builder.append(' ');
			builder.append(object.toString());
		}

		return builder.toString();
	}
}