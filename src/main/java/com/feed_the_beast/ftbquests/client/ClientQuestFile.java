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
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Collection;

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

	private final Short2ObjectOpenHashMap<ClientQuestProgress> teamData;
	public ClientQuestProgress self;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;
	public final IntCollection rewards;

	public ClientQuestFile()
	{
		teamData = new Short2ObjectOpenHashMap<>();
		rewards = new IntOpenHashSet();
	}

	public void load(MessageSyncQuests message)
	{
		if (INSTANCE != null)
		{
			INSTANCE.deleteChildren();
			INSTANCE.deleteSelf();
		}

		INSTANCE = this;

		for (MessageSyncQuests.TeamInst team : message.teamData)
		{
			ClientQuestProgress data = new ClientQuestProgress(team.uid, team.id, team.name);

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
				data.variables.put(team.variableKeys[i], team.variableValues[i]);
			}

			teamData.put(data.getTeamUID(), data);
		}

		self = message.team == 0 ? null : teamData.get(message.team);
		editingMode = message.editingMode;
		rewards.addAll(message.rewards);
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
		int selectedChapter = 0;
		int[] selectedQuests = new int[0];

		if (questTreeGui != null)
		{
			hasPrev = true;
			zoom = questTreeGui.zoom;
			scrollX = questTreeGui.quests.getScrollX();
			scrollY = questTreeGui.quests.getScrollY();
			selectedChapter = questTreeGui.selectedChapter == null ? 0 : questTreeGui.selectedChapter.id;
			selectedQuests = new int[questTreeGui.selectedQuests.size()];
			int i = 0;

			for (Quest q : questTreeGui.selectedQuests)
			{
				selectedQuests[i] = q.id;
				i++;
			}

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

			for (int i : selectedQuests)
			{
				questTreeGui.selectedQuests.add(getQuest(i));
			}

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
	public ClientQuestProgress getData(short team)
	{
		return team == 0 ? null : teamData.get(team);
	}

	public ClientQuestProgress removeData(short team)
	{
		return teamData.remove(team);
	}

	public void addData(ClientQuestProgress data)
	{
		teamData.put(data.getTeamUID(), data);
	}

	@Nullable
	@Override
	public ClientQuestProgress getData(String team)
	{
		if (team.isEmpty())
		{
			return null;
		}

		for (ClientQuestProgress data : teamData.values())
		{
			if (team.equals(data.getTeamID()))
			{
				return data;
			}
		}

		return null;
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
		return rewards.contains(reward.id);
	}
}