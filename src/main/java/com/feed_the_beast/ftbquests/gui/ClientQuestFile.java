package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class ClientQuestFile extends QuestFile implements IProgressData
{
	public static ClientQuestFile INSTANCE;

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
	private final ShortCollection claimedRewards;
	public GuiQuestTree questTreeGui;
	public GuiBase questGui;
	public boolean editingMode;

	public ClientQuestFile(MessageSyncQuests message, @Nullable ClientQuestFile prev)
	{
		super(message.quests);
		teamId = message.team;
		editingMode = message.editingMode;

		taskData = new Int2ObjectOpenHashMap<>();
		claimedRewards = new ShortOpenHashSet();

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

		for (short reward : message.claimedRewards)
		{
			claimedRewards.add(reward);
		}

		refreshGui(prev);
	}

	public void refreshGui(@Nullable ClientQuestFile prev)
	{
		boolean guiOpen = false;
		int scrollX = 0, scrollY = 0;
		short selectedChapter = 0;

		if (prev != null)
		{
			scrollX = prev.questTreeGui.quests.getScrollX();
			scrollY = prev.questTreeGui.quests.getScrollY();
			selectedChapter = prev.questTreeGui.selectedChapter == null ? 0 : prev.questTreeGui.selectedChapter.id;

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

	@Override
	public String getTeamID()
	{
		return teamId;
	}

	@Override
	public QuestTaskData getQuestTaskData(short task)
	{
		return taskData.get(task);
	}

	public void setRewardStatus(short reward, boolean status)
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
	public ShortCollection getClaimedRewards(EntityPlayer player)
	{
		return claimedRewards;
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
	}

	@Override
	public void removeTask(short task)
	{
		taskData.remove(task);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		taskData.put(task.id, task.createData(this));
	}

	@Override
	public void unclaimReward(short reward)
	{
		claimedRewards.rem(reward);
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
			builder.append(formatID(object.id));
		}

		return builder.toString();
	}
}