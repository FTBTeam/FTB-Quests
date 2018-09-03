package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.gui.GuiQuest;
import com.feed_the_beast.ftbquests.gui.GuiQuestTree;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
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

	public ClientQuestFile(MessageSyncQuests message, @Nullable ClientQuestFile prev)
	{
		readData(message.quests);
		teamData = new HashMap<>();

		for (MessageSyncQuests.TeamInst team : message.teamData)
		{
			ClientQuestProgress data = new ClientQuestProgress(team.name);

			for (QuestTask task : allTasks)
			{
				data.createTaskData(task);
			}

			for (int i = 0; i < team.taskKeys.length; i++)
			{
				data.getQuestTaskData(allTasks[team.taskKeys[i]]).fromNBT(team.taskValues[i]);
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

		refreshGui(prev);
	}

	public boolean canEdit()
	{
		return editingMode;
	}

	public void refreshGui(@Nullable ClientQuestFile prev)
	{
		boolean guiOpen = false;
		int scrollX = 0, scrollY = 0;
		String selectedChapter = "";

		if (prev != null)
		{
			scrollX = prev.questTreeGui.quests.getScrollX();
			scrollY = prev.questTreeGui.quests.getScrollY();
			selectedChapter = prev.questTreeGui.selectedChapter == null ? "" : prev.questTreeGui.selectedChapter.getID();

			if (ClientUtils.getCurrentGuiAs(GuiQuestTree.class) != null)
			{
				guiOpen = true;
			}
		}

		questTreeGui = new GuiQuestTree(this);
		questGui = questTreeGui;

		if (prev != null)
		{
			questTreeGui.selectChapter(getChapter(selectedChapter));

			if (guiOpen)
			{
				questTreeGui.openGui();
			}
		}

		GuiQuest guiQuest = ClientUtils.getCurrentGuiAs(GuiQuest.class);

		if (guiQuest != null)
		{
			guiQuest.refreshWidgets();
		}
		else
		{
			GuiQuestTree guiQuestTree = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

			if (guiQuestTree != null)
			{
				guiQuestTree.refreshWidgets();
			}
			else
			{
				GuiVariables guiVariables = ClientUtils.getCurrentGuiAs(GuiVariables.class);

				if (guiVariables != null)
				{
					guiVariables.refreshWidgets();
				}
			}
		}

		if (prev != null)
		{
			questTreeGui.quests.setScrollX(scrollX);
			questTreeGui.quests.setScrollY(scrollY);
		}
	}

	public void openQuestGui()
	{
		questGui.openGui();
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
	public void deleteObject(String id)
	{
		new MessageDeleteObject(id).sendToServer();
	}

	public boolean isRewardClaimed(QuestReward reward)
	{
		return rewards.contains(reward.uid);
	}
}