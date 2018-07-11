package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends ProgressingQuestObject
{
	public final QuestChapter chapter;
	public String title;
	public String description;
	public Icon icon;
	public QuestType type;
	public int x, y;
	public final List<String> text;
	public final IntCollection dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;

	public Quest(QuestChapter c, int id)
	{
		super(id);
		chapter = c;
		title = "#" + id;
		description = "";
		icon = Icon.EMPTY;
		type = QuestType.NORMAL;
		text = new ArrayList<>();
		dependencies = new IntOpenHashSet();
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();
	}

	@Override
	public QuestList getQuestList()
	{
		return chapter.getQuestList();
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (QuestTask task : tasks)
		{
			progress += task.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (QuestTask task : tasks)
		{
			maxProgress += task.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public double getRelativeProgress(IProgressData data)
	{
		double progress = 0D;

		for (QuestTask quest : tasks)
		{
			progress += quest.getRelativeProgress(data);
		}

		return progress / (double) tasks.size();
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		for (QuestTask task : tasks)
		{
			task.resetProgress(data);
		}
	}

	public boolean isVisible(IProgressData data)
	{
		if (type == QuestType.NORMAL)
		{
			return true;
		}

		QuestList list = getQuestList();

		if (type == QuestType.SECRET)
		{
			for (int d : dependencies)
			{
				QuestObject object = list.get(d);

				if (object instanceof ProgressingQuestObject && ((ProgressingQuestObject) object).isComplete(data))
				{
					return true;
				}
			}
		}

		for (int d : dependencies)
		{
			QuestObject object = list.get(d);

			if (object instanceof ProgressingQuestObject && !((ProgressingQuestObject) object).isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	public Icon getIcon()
	{
		if (!icon.isEmpty())
		{
			return icon;
		}

		List<Icon> list = new ArrayList<>();

		for (QuestTask task : tasks)
		{
			list.add(task.getIcon());
		}

		return new IconAnimation(list);
	}
}