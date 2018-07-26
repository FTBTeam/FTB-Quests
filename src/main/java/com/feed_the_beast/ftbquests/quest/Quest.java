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
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.rewards.UnknownReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import com.feed_the_beast.ftbquests.quest.tasks.UnknownTask;
import net.minecraft.item.ItemStack;
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
	public final ConfigList<ConfigInt> dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;

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
		text = new ConfigList<>(new ConfigString(""));

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(new ConfigString(list.getStringTagAt(k)));
		}

		dependencies = new ConfigList<>(new ConfigInt(1, 1, Integer.MAX_VALUE));
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();

		list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestTask task = QuestTasks.createTask(this, list.getCompoundTagAt(k), false);
			tasks.add(task);
			chapter.list.objectMap.put(task.id, task);
		}

		list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestReward reward = QuestRewards.createReward(this, list.getCompoundTagAt(k), false);
			rewards.add(reward);
			chapter.list.objectMap.put(reward.id, reward);
		}

		for (int d : nbt.getIntArray("dependencies"))
		{
			dependencies.add(new ConfigInt(d));
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
			nbt.setTag("icon", icon.getStack().serializeNBT());
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
			int[] ai = new int[dependencies.getList().size()];

			for (int i = 0; i < dependencies.getList().size(); i++)
			{
				ai[i] = dependencies.getList().get(i).getInt();
			}

			nbt.setIntArray("dependencies", ai);
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

				if (reward.teamReward)
				{
					rewardNBT.setBoolean("team_reward", true);
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
		if (type.getValue() == QuestType.NORMAL || dependencies.isEmpty())
		{
			return true;
		}

		QuestList list = getQuestList();

		if (type.getValue() == QuestType.SECRET)
		{
			for (ConfigInt value : dependencies)
			{
				QuestObject object = list.get(value.getInt());

				if (object instanceof ProgressingQuestObject && ((ProgressingQuestObject) object).isComplete(data))
				{
					return true;
				}
			}

			return false;
		}

		for (ConfigInt value : dependencies)
		{
			QuestObject object = list.get(value.getInt());

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
		Icon i = ItemIcon.getItemIcon(icon.getStack());

		if (i.isEmpty())
		{
			List<Icon> list = new ArrayList<>();

			for (QuestTask task : tasks)
			{
				list.add(task.getIcon());
			}

			i = IconAnimation.fromList(list, false);
		}

		return i;
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

		for (QuestTask task : new ArrayList<>(tasks))
		{
			task.delete();
		}

		for (QuestReward reward : new ArrayList<>(rewards))
		{
			reward.delete();
		}
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("title", title, new ConfigString(""));
		group.add("x", x, new ConfigInt(0));
		group.add("y", y, new ConfigInt(0));
		group.add("type", type, new ConfigEnum<>(QuestType.NAME_MAP));
		group.add("icon", icon, new ConfigItemStack(ItemStack.EMPTY));
		group.add("description", description, new ConfigString(""));
		group.add("text", text, new ConfigList<>(new ConfigString("")));
		group.add("dependencies", dependencies, new ConfigList<>(new ConfigInt(1, 1, Integer.MAX_VALUE)));
	}
}