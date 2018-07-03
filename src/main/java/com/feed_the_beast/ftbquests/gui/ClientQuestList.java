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
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ClientQuestList extends QuestList implements IProgressData
{
	public static ClientQuestList INSTANCE;

	public static boolean exists()
	{
		return INSTANCE != null && !INSTANCE.isInvalid() && !INSTANCE.teamId.isEmpty();
	}

	public String teamId;
	private final Int2ObjectOpenHashMap<QuestTaskData> taskData;
	private final IntCollection claimedRewards;
	public final GuiQuestTree questTreeGui;
	public GuiBase questGui;

	public ClientQuestList(MessageSyncQuests message, @Nullable ClientQuestList prev)
	{
		super(message.quests.isJsonObject() ? message.quests.getAsJsonObject() : new JsonObject());
		teamId = message.team;

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

		for (QuestTaskData data : taskData.values())
		{
			data.readFromNBT(message.taskData.getCompoundTag(Integer.toString(data.task.id)));
		}

		for (int reward : message.claimedRewards)
		{
			claimedRewards.add(reward);
		}

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
	public void syncTaskProgress(QuestTask task, int progress)
	{
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

	public String getCompletionSuffix(ProgressingQuestObject object)
	{
		if (!GuiScreen.isShiftKeyDown())
		{
			return "";
		}

		double d = object.getRelativeProgress(this);

		if (d <= 0D)
		{
			return TextFormatting.DARK_GRAY + " 0%";
		}
		else if (d >= 1D)
		{
			return TextFormatting.DARK_GRAY + " 100%";
		}

		return TextFormatting.DARK_GRAY + " " + (int) (d * 100D) + "%";
	}
}