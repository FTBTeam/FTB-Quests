package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends ProgressingQuestObject
{
	public final QuestList list;
	public ITextComponent title;
	public final List<ITextComponent> description;
	public Icon icon;
	public final List<Quest> quests;
	public final IntCollection dependencies;
	public final List<QuestReward> rewards;

	public QuestChapter(QuestList l, int id)
	{
		super(id);
		list = l;
		title = new TextComponentString(Integer.toString(id));
		description = new ArrayList<>();
		icon = Icon.EMPTY;
		quests = new ArrayList<>();
		dependencies = new IntOpenHashSet();
		rewards = new ArrayList<>();
	}

	@Override
	public QuestList getQuestList()
	{
		return list;
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (Quest quest : quests)
		{
			progress += quest.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (Quest quest : quests)
		{
			maxProgress += quest.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		for (Quest quest : quests)
		{
			quest.resetProgress(data);
		}
	}
}