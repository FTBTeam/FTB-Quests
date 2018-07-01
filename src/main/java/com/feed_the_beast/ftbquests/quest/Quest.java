package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends ProgressingQuestObject
{
	public final QuestChapter chapter;
	public ITextComponent title;
	public ITextComponent description;
	public Icon icon;
	public QuestType type;
	public int x, y;
	public final List<ITextComponent> text;
	public final IntCollection dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;

	public Quest(QuestChapter c, int id)
	{
		super(id);
		chapter = c;
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

	public ITextComponent getTitle()
	{
		return title == null ? new TextComponentString("#" + id) : title;
	}

	public ITextComponent getDescription()
	{
		return description == null ? new TextComponentString("") : description;
	}
}