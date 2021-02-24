package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;
import dev.latvian.kubejs.player.PlayerDataJS;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSPlayerData
{
	private final PlayerDataJS playerData;

	public FTBQuestsKubeJSPlayerData(PlayerDataJS p)
	{
		playerData = p;
	}

	public QuestFile getFile()
	{
		return FTBQuests.PROXY.getQuestFile(playerData.getOverworld().minecraftWorld.isClientSide());
	}

	public PlayerData getData()
	{
		return getFile().getData(playerData.getId());
	}

	public void addProgress(Object id, long progress)
	{
		PlayerData data = getData();
		Task task = data.file.getTask(data.file.getID(id));

		if (task != null)
		{
			data.getTaskData(task).addProgress(progress);
		}
	}

	public void complete(Object id)
	{
		PlayerData data = getData();
		Task task = data.file.getTask(data.file.getID(id));

		if (task != null)
		{
			data.getTaskData(task).complete();
		}
	}

	public void reset(Object id)
	{
		PlayerData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));

		if (object != null)
		{
			object.forceProgress(data, ChangeProgress.RESET, false);
		}
	}

	public boolean isCompleted(Object id)
	{
		PlayerData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null && data.isComplete(object);
	}

	public boolean isStarted(Object id)
	{
		PlayerData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null && data.isStarted(object);
	}

	public boolean canStartQuest(Object id)
	{
		PlayerData data = getData();
		Quest quest = data.file.getQuest(data.file.getID(id));
		return quest != null && data.canStartTasks(quest);
	}

	public int getRelativeProgress(Object id)
	{
		PlayerData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null ? data.getRelativeProgress(object) : 0;
	}

	public long getTaskProgress(Object id)
	{
		PlayerData data = getData();
		Task task = data.file.getTask(data.file.getID(id));
		return task != null ? data.getTaskData(task).progress : 0L;
	}
}