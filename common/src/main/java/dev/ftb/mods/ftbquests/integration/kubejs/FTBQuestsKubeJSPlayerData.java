package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.latvian.kubejs.player.PlayerDataJS;

/**
 * @author LatvianModder
 */
public class FTBQuestsKubeJSPlayerData {
	private final PlayerDataJS playerData;

	public FTBQuestsKubeJSPlayerData(PlayerDataJS p) {
		playerData = p;
	}

	public QuestFile getFile() {
		return FTBQuests.PROXY.getQuestFile(playerData.getOverworld().minecraftWorld.isClientSide());
	}

	public TeamData getData() {
		return getFile().getData(FTBTeamsAPI.getPlayerTeamID(playerData.getId()));
	}

	public void addProgress(Object id, long progress) {
		TeamData data = getData();
		Task task = data.file.getTask(data.file.getID(id));

		if (task != null) {
			data.getTaskData(task).addProgress(progress);
		}
	}

	public void complete(Object id) {
		TeamData data = getData();
		Task task = data.file.getTask(data.file.getID(id));

		if (task != null) {
			data.getTaskData(task).complete();
		}
	}

	public void reset(Object id) {
		TeamData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));

		if (object != null) {
			object.forceProgress(data, ChangeProgress.RESET, false);
		}
	}

	public boolean isCompleted(Object id) {
		TeamData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null && data.isComplete(object);
	}

	public boolean isStarted(Object id) {
		TeamData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null && data.isStarted(object);
	}

	public boolean canStartQuest(Object id) {
		TeamData data = getData();
		Quest quest = data.file.getQuest(data.file.getID(id));
		return quest != null && data.canStartTasks(quest);
	}

	public int getRelativeProgress(Object id) {
		TeamData data = getData();
		QuestObject object = data.file.get(data.file.getID(id));
		return object != null ? data.getRelativeProgress(object) : 0;
	}

	public long getTaskProgress(Object id) {
		TeamData data = getData();
		Task task = data.file.getTask(data.file.getID(id));
		return task != null ? data.getTaskData(task).progress : 0L;
	}
}