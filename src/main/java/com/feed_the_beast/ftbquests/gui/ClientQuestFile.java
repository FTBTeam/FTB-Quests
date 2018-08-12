package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

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

	public final String teamId;
	public final ClientQuestProgress self;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;

	public ClientQuestFile(MessageSyncQuests message, @Nullable ClientQuestFile prev)
	{
		teamId = message.team;
		self = teamId.isEmpty() ? null : new ClientQuestProgress(message.team);
		editingMode = message.editingMode;

		readData(message.quests);

		if (self != null)
		{
			for (QuestChapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						self.taskData.put(task.index, task.createData(self));
					}
				}
			}

			FTBQuestsTeamData.deserializeTaskData(self.taskData.values(), message.taskData);
			FTBQuestsTeamData.deserializeRewardData(this, self.claimedRewards, message.claimedRewards);
		}

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
		if (self != null && team.equals(self.teamID))
		{
			return self;
		}

		return null;
	}

	@Override
	public Collection<IProgressData> getAllData()
	{
		if (self == null)
		{
			return Collections.emptyList();
		}

		return Collections.singleton(self);
	}
}