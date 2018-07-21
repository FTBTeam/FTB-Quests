package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.rewards.UnknownReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import com.feed_the_beast.ftbquests.quest.tasks.UnknownTask;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends ProgressingQuestObject
{
	public final QuestChapter chapter;
	public final ConfigString title;
	public final ConfigString description;
	public final ConfigItemStack icon;
	public final ConfigEnum<QuestType> type;
	public final ConfigInt x, y;
	public final ConfigList<ConfigString> text;
	public final IntCollection dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	private Icon cachedIcon;

	public Quest(QuestChapter c, NBTTagCompound nbt)
	{
		super(c.list.getID(nbt));
		chapter = c;
		title = new ConfigString(nbt.getString("title"));
		description = new ConfigString(nbt.getString("description"));
		icon = new ConfigItemStack(QuestList.getIcon(nbt));
		type = new ConfigEnum<>(QuestType.NAME_MAP);
		type.setValue(nbt.getString("type"));
		x = new ConfigInt(nbt.getByte("x"), -127, 127);
		y = new ConfigInt(nbt.getByte("y"), -127, 127);
		text = new ConfigList<>(ConfigString.ID);

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(new ConfigString(list.getStringTagAt(k)));
		}

		dependencies = new IntOpenHashSet();
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();

		list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestTask task = QuestTasks.createTask(this, list.getCompoundTagAt(k));
			tasks.add(task);
			chapter.list.objectMap.put(task.id, task);
		}

		list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestReward reward = QuestRewards.createReward(this, list.getCompoundTagAt(k));
			rewards.add(reward);
			chapter.list.objectMap.put(reward.id, reward);
		}

		for (int d : nbt.getIntArray("depends_on"))
		{
			dependencies.add(d);
		}
	}

	@Override
	public QuestList getQuestList()
	{
		return chapter.getQuestList();
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.QUEST;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("id", id);

		if (type.getValue() != QuestType.NORMAL)
		{
			nbt.setString("type", type.getValue().getName());
		}

		nbt.setByte("x", (byte) x.getInt());
		nbt.setByte("y", (byte) y.getInt());
		nbt.setString("title", title.getString());

		if (!description.isEmpty())
		{
			nbt.setString("description", description.getString());
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", icon.getItem().serializeNBT());
		}

		if (!text.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (ConfigString s : text)
			{
				array.appendTag(new NBTTagString(s.getString()));
			}

			nbt.setTag("text", array);
		}

		if (!dependencies.isEmpty())
		{
			nbt.setIntArray("dependencies", dependencies.toIntArray());
		}

		if (!tasks.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestTask task : tasks)
			{
				NBTTagCompound taskNBT = new NBTTagCompound();
				task.writeData(taskNBT);
				taskNBT.setInteger("id", task.id);

				if (!(task instanceof UnknownTask))
				{
					taskNBT.setString("type", task.getName());
				}

				array.appendTag(taskNBT);
			}

			nbt.setTag("tasks", array);
		}

		if (!rewards.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestReward reward : rewards)
			{
				NBTTagCompound rewardNBT = new NBTTagCompound();
				reward.writeData(rewardNBT);
				rewardNBT.setInteger("id", reward.id);

				if (!(reward instanceof UnknownReward))
				{
					rewardNBT.setString("type", reward.getName());
				}

				array.appendTag(rewardNBT);
			}

			nbt.setTag("rewards", array);
		}
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
		if (type.getValue() == QuestType.NORMAL)
		{
			return true;
		}

		QuestList list = getQuestList();

		if (type.getValue() == QuestType.SECRET)
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

	@Override
	public Icon getIcon()
	{
		if (cachedIcon != null)
		{
			return cachedIcon;
		}

		cachedIcon = ItemIcon.getItemIcon(icon.getItem());

		if (cachedIcon.isEmpty())
		{
			List<Icon> list = new ArrayList<>();

			for (QuestTask task : tasks)
			{
				list.add(task.getIcon());
			}

			cachedIcon = IconAnimation.fromList(list, false);
		}

		return cachedIcon;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(title.getString());
	}

	@Override
	public void delete()
	{
		super.delete();
		chapter.quests.remove(this);

		for (QuestTask task : tasks)
		{
			task.delete();
		}

		for (QuestReward reward : rewards)
		{
			reward.delete();
		}
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add(FTBQuests.MOD_ID, "title", title);
		group.add(FTBQuests.MOD_ID, "description", description);
		group.add(FTBQuests.MOD_ID, "icon", icon);
		group.add(FTBQuests.MOD_ID, "type", type);
		group.add(FTBQuests.MOD_ID, "x", x);
		group.add(FTBQuests.MOD_ID, "y", y);
		group.add(FTBQuests.MOD_ID, "text", text);
	}
}