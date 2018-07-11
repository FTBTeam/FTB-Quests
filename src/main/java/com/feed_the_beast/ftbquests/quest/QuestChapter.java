package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends ProgressingQuestObject
{
	public final QuestList list;
	public String title;
	public final List<String> description;
	public Icon icon;
	public final List<Quest> quests;
	public final IntCollection dependencies;

	public QuestChapter(QuestList l, int id)
	{
		super(id);
		list = l;
		title = "#" + id;
		description = new ArrayList<>();
		icon = Icon.EMPTY;
		quests = new ArrayList<>();
		dependencies = new IntOpenHashSet();
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
	public double getRelativeProgress(IProgressData data)
	{
		double progress = 0D;

		for (Quest quest : quests)
		{
			progress += quest.getRelativeProgress(data);
		}

		return progress / (double) quests.size();
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		for (Quest quest : quests)
		{
			quest.resetProgress(data);
		}
	}

	public Icon getIcon()
	{
		if (!icon.isEmpty())
		{
			return icon;
		}

		List<Icon> list = new ArrayList<>();

		for (Quest quest : quests)
		{
			list.add(quest.getIcon());
		}

		return new IconAnimation(list);
	}
}