package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.events.QuestTaskEvent;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.IProgressing;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTask implements IProgressing, ICapabilityProvider
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
		return data.getQuestTaskProgress(key);
	}

	@Override
	public int getMaxProgress()
	{
		return 1;
	}

	public abstract Icon getIcon();

	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return toJson().toString();
	}

	public abstract JsonObject toJson();

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return false;
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return null;
	}

	public ItemStack processItem(IProgressData data, ItemStack stack)
	{
		return stack;
	}

	public String toString()
	{
		return key.toString();
	}
}