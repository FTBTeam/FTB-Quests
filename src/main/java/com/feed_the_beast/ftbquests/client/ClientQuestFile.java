package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.net.MessageMyTeamGui;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObject;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

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

	private final Short2ObjectOpenHashMap<ClientQuestData> teamData;
	public ClientQuestData self;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;

	public ClientQuestFile()
	{
		teamData = new Short2ObjectOpenHashMap<>();
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
			ClientQuestData data = new ClientQuestData(team.uid, team.id, team.name);

			for (Chapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (Task task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}

			for (int i = 0; i < team.taskKeys.length; i++)
			{
				Task task = getTask(team.taskKeys[i]);

				if (task != null)
				{
					TaskData taskData = data.getTaskData(task);
					taskData.progress = team.taskValues[i];
					taskData.isComplete = taskData.isComplete();
				}
			}

			for (int i = 0; i < team.playerRewardUUIDs.length; i++)
			{
				data.claimedPlayerRewards.put(team.playerRewardUUIDs[i], fromArray(team.playerRewardIDs[i]));
			}

			data.claimedTeamRewards.addAll(fromArray(team.teamRewards));
			teamData.put(data.getTeamUID(), data);
		}

		self = message.team == 0 ? null : teamData.get(message.team);
		editingMode = message.editingMode;
		//FIXME: rewards.addAll(message.rewards);
		refreshGui();
		FTBQuestsJEIHelper.refresh(this);
	}

	private IntOpenHashSet fromArray(int[] array)
	{
		IntOpenHashSet set = new IntOpenHashSet(array.length);

		for (int i : array)
		{
			set.add(i);
		}

		return set;
	}

	@Override
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
			scrollX = questTreeGui.questPanel.getScrollX();
			scrollY = questTreeGui.questPanel.getScrollY();
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

		if (hasPrev)
		{
			questTreeGui.questPanel.setScrollX(scrollX);
			questTreeGui.questPanel.setScrollY(scrollY);
		}
	}

	public void openQuestGui(EntityPlayer player)
	{
		if (disableGui && !editingMode)
		{
			player.sendStatusMessage(new TextComponentTranslation("item.ftbquests.book.disabled"), true);
		}
		else if (existsWithTeam())
		{
			questGui.openGui();
		}
		else
		{
			new MessageMyTeamGui().sendToServer();
			//player.sendStatusMessage(new TextComponentTranslation("ftblib.lang.team.error.no_team"), true);
		}
	}

	@Override
	public boolean isClient()
	{
		return true;
	}

	@Nullable
	@Override
	public ClientQuestData getData(short team)
	{
		return team == 0 ? null : teamData.get(team);
	}

	public ClientQuestData removeData(short team)
	{
		return teamData.remove(team);
	}

	public void addData(ClientQuestData data)
	{
		teamData.put(data.getTeamUID(), data);
	}

	@Nullable
	@Override
	public ClientQuestData getData(String team)
	{
		if (team.isEmpty())
		{
			return null;
		}

		for (ClientQuestData data : teamData.values())
		{
			if (team.equals(data.getTeamID()))
			{
				return data;
			}
		}

		return null;
	}

	@Override
	public Collection<ClientQuestData> getAllData()
	{
		return teamData.values();
	}

	@Override
	public void deleteObject(int id)
	{
		new MessageDeleteObject(id).sendToServer();
	}
}