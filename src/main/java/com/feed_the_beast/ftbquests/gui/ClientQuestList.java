package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class ClientQuestList extends QuestList implements IProgressData
{
	public static ClientQuestList INSTANCE;

	public static boolean exists()
	{
		return INSTANCE != null && !INSTANCE.isInvalid();
	}

	public static boolean existsWithTeam()
	{
		return exists() && !INSTANCE.teamId.isEmpty();
	}

	public String teamId;
	private final Int2ObjectOpenHashMap<QuestTaskData> taskData;
	private final IntCollection claimedRewards;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;

	public ClientQuestList(MessageSyncQuests message, @Nullable ClientQuestList prev)
	{
		super(message.quests);
		teamId = message.team;
		editingMode = message.editingMode;

		taskData = new Int2ObjectOpenHashMap<>();
		claimedRewards = new IntOpenHashSet();

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					taskData.put(task.id, task.createData(this));
				}
			}
		}

		FTBQuestsTeamData.deserializeTaskData(taskData.values(), message.taskData);

		for (int reward : message.claimedRewards)
		{
			claimedRewards.add(reward);
		}

		refreshGui(prev);
	}

	public void refreshGui(@Nullable ClientQuestList prev)
	{
		boolean oldData = false;
		boolean guiOpen = false;
		int zoom = 0, scrollX = 0, scrollY = 0;
		int selectedChapter = 0;

		if (prev != null)
		{
			oldData = true;
			zoom = prev.questTreeGui.zoom;
			scrollX = prev.questTreeGui.quests.getScrollX();
			scrollY = prev.questTreeGui.quests.getScrollY();
			selectedChapter = prev.questTreeGui.selectedChapter == null ? 0 : prev.questTreeGui.selectedChapter.chapter.id;

			if (ClientUtils.getCurrentGuiAs(GuiQuestTree.class) != null)
			{
				guiOpen = true;
			}
		}

		questTreeGui = new GuiQuestTree(this);
		questGui = questTreeGui;

		if (oldData)
		{
			questTreeGui.zoom = zoom;
			questTreeGui.quests.setScrollX(scrollX);
			questTreeGui.quests.setScrollY(scrollY);

			for (GuiQuestTree.ButtonChapter b : questTreeGui.chapterButtons)
			{
				if (b.chapter.id == selectedChapter)
				{
					questTreeGui.selectedChapter = b;
				}
			}

			if (guiOpen)
			{
				questTreeGui.openGui();
			}
		}
	}

	public void openQuestGui()
	{
		questGui.openGui();
	}

	@Override
	public String getTeamID()
	{
		return teamId;
	}

	@Override
	public QuestTaskData getQuestTaskData(int task)
	{
		return taskData.get(task);
	}

	public void setRewardStatus(int reward, boolean status)
	{
		if (status)
		{
			claimedRewards.add(reward);
		}
		else
		{
			claimedRewards.rem(reward);
		}
	}

	@Override
	public boolean claimReward(EntityPlayer player, QuestReward reward)
	{
		if (!claimedRewards.contains(reward.id) && reward.quest.isComplete(this))
		{
			claimedRewards.add(reward.id);
			return true;
		}

		return false;
	}

	@Override
	public IntCollection getClaimedRewards(EntityPlayer player)
	{
		return claimedRewards;
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
	}

	@Override
	public void removeTask(int task)
	{
		taskData.remove(task);
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		claimedRewards.clear();

		for (QuestTaskData d : taskData.values())
		{
			d.setProgress(0, false);
		}
	}

	@Nullable
	@Override
	public IProgressData getData(String owner)
	{
		return isInvalid() || !teamId.equals(owner) ? null : this;
	}

	@Override
	public Collection<IProgressData> getAllData()
	{
		return Collections.singleton(this);
	}

	public String getCompletionSuffix(ProgressingQuestObject object)
	{
		if (!GuiScreen.isShiftKeyDown())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatting.DARK_GRAY);
		builder.append(' ');

		double d = object.getRelativeProgress(this);

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

		if (GuiScreen.isCtrlKeyDown())
		{
			builder.append(' ');
			builder.append('#');
			builder.append(object.id);
		}

		return builder.toString();
	}
}