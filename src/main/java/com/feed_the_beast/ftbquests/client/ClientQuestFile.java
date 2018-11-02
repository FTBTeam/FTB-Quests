package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class ClientQuestFile extends QuestFile
{
	public static ClientQuestFile INSTANCE;

	public static boolean exists()
	{
		return INSTANCE != null && !INSTANCE.invalid;
	}

	public static boolean existsWithTeam()
	{
		return exists() && INSTANCE.self != null;
	}

	public final Map<String, ClientQuestProgress> teamData;
	public ClientQuestProgress self;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;
	public final IntCollection rewards;

	public ClientQuestFile(MessageSyncQuests message)
	{
		readData(message.quests);
		teamData = new HashMap<>();

		for (MessageSyncQuests.TeamInst team : message.teamData)
		{
			ClientQuestProgress data = new ClientQuestProgress(team.name);

			for (QuestChapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}

			for (int i = 0; i < team.taskKeys.length; i++)
			{
				QuestTask task = getTask(team.taskKeys[i]);

				if (task != null)
				{
					data.getQuestTaskData(task).fromNBT(team.taskValues[i]);
				}
			}

			for (int i = 0; i < team.variableKeys.length; i++)
			{
				data.variables.put(variables.get(team.variableKeys[i]), team.variableValues[i]);
			}

			teamData.put(data.getTeamID(), data);
		}

		self = message.team.isEmpty() ? null : teamData.get(message.team);
		editingMode = message.editingMode;

		rewards = new IntOpenHashSet(message.rewards);

		refreshGui();
	}

	public boolean canEdit()
	{
		return editingMode;
	}

	public void refreshGui()
	{
		clearCachedData();

		boolean hasPrev = false;
		boolean guiOpen = false;
		int zoom = 0;
		int scrollX = 0, scrollY = 0;
		String selectedChapter = "";
		String selectedQuest = "";

		if (questTreeGui != null)
		{
			hasPrev = true;
			zoom = questTreeGui.zoom;
			scrollX = questTreeGui.quests.getScrollX();
			scrollY = questTreeGui.quests.getScrollY();
			selectedChapter = questTreeGui.selectedChapter == null ? "" : questTreeGui.selectedChapter.getID();
			selectedQuest = questTreeGui.selectedQuest == null ? "" : questTreeGui.selectedQuest.getID();

			if (ClientUtils.getCurrentGuiAs(GuiQuestTree.class) != null)
			{
				guiOpen = true;
			}
		}

		questTreeGui = new GuiQuestTree(this);
		questGui = questTreeGui;

		if (hasPrev)
		{
			questTreeGui.zoom = zoom;
			questTreeGui.selectChapter(getChapter(selectedChapter));
			questTreeGui.selectQuest(getQuest(selectedQuest));

			if (guiOpen)
			{
				questTreeGui.openGui();
			}
		}

		questTreeGui.refreshWidgets();

		GuiVariables guiVariables = ClientUtils.getCurrentGuiAs(GuiVariables.class);

		if (guiVariables != null)
		{
			guiVariables.refreshWidgets();
		}

		if (hasPrev)
		{
			questTreeGui.quests.setScrollX(scrollX);
			questTreeGui.quests.setScrollY(scrollY);
		}
	}

	public void openQuestGui()
	{
		questGui.openGui();
	}

	@Override
	public boolean isClient()
	{
		return true;
	}

	@Nullable
	@Override
	public ClientQuestProgress getData(String team)
	{
		return teamData.get(team);
	}

	@Override
	public Collection<ClientQuestProgress> getAllData()
	{
		return teamData.values();
	}

	@Override
	public void deleteObject(int id)
	{
		new MessageDeleteObject(id).sendToServer();
	}

	public boolean isRewardClaimed(QuestReward reward)
	{
		return rewards.contains(reward.uid);
	}
}