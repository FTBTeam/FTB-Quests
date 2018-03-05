package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.events.QuestTaskEvent;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.IProgressing;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestTask implements IProgressing
{
	@Nullable
	public static QuestTask createTask(Quest parent, int index, @Nullable JsonElement json0)
	{
		QuestTask task = null;

		if (!JsonUtils.isNull(json0) && json0.isJsonObject())
		{
			JsonObject json = json0.getAsJsonObject();

			if (json.has("item"))
			{
				int count = json.has("count") ? json.get("count").getAsInt() : 1;

				if (count > 0)
				{
					ItemTask.QuestItem item = ItemTask.QuestItem.fromJson(json.get("item"));

					if (!item.isEmpty())
					{
						task = new ItemTask(parent, index, item, count);
					}
				}
			}
			else
			{
				QuestTaskEvent event = new QuestTaskEvent(parent, index, json);
				event.post();
				task = event.getTask();
			}
		}

		return task;
	}

	public final Quest parent;
	public final QuestTaskKey key;

	public QuestTask(Quest p, int i)
	{
		parent = p;
		key = new QuestTaskKey(parent.id, i);
	}

	@Override
	public int getProgress(IProgressData data)
	{
		return data.getQuestTaskProgress(this);
	}

	@Override
	public int getMaxProgress()
	{
		return 1;
	}

	public abstract Icon getIcon();

	public void addText(List<String> text)
	{
		text.add(toJson().toString());
	}

	public abstract JsonObject toJson();
}