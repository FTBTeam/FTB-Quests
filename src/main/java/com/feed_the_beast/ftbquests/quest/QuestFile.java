package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends ProgressingQuestObject
{
	public static final int MAX_ID = 65535;

	public static String formatID(short id0)
	{
		int id = id0 & 0xFFFF;

		StringBuilder builder = new StringBuilder(4);

		if (id < 10000)
		{
			builder.append('0');
		}

		if (id < 1000)
		{
			builder.append('0');
		}

		if (id < 100)
		{
			builder.append('0');
		}

		if (id < 10)
		{
			builder.append('0');
		}

		builder.append(id);
		return builder.toString();
	}

	public final ConfigString title;
	public final ConfigItemStack icon;
	public final List<QuestChapter> chapters;
	public final Short2ObjectMap<QuestObject> map;
	public final ConfigBoolean allowTakeQuestBlocks;
	public final ConfigList<ConfigItemStack> emergencyItems;
	public final ConfigTimer emergencyItemsCooldown;

	public QuestFile()
	{
		super((short) 0);
		title = new ConfigString("");
		icon = new ConfigItemStack(ItemStack.EMPTY);
		chapters = new ArrayList<>();
		map = new Short2ObjectOpenHashMap<>();
		allowTakeQuestBlocks = new ConfigBoolean(true);
		emergencyItems = new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE)));
		emergencyItemsCooldown = new ConfigTimer(Ticks.MINUTE.x(5));
	}

	@Override
	public QuestFile getQuestFile()
	{
		return this;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.FILE;
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (QuestChapter chapter : chapters)
		{
			maxProgress += chapter.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.resetProgress(data);
		}
	}

	@Override
	public void deleteSelf()
	{
		invalid = true;
	}

	@Override
	public void deleteChildren()
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.deleteChildren();
			chapter.invalid = true;
		}

		chapters.clear();
	}

	@Nullable
	public QuestObject get(short id)
	{
		return id == 0 ? this : map.get(id);
	}

	@Nullable
	public QuestChapter getChapter(short id)
	{
		QuestObject object = get(id);
		return object instanceof QuestChapter ? (QuestChapter) object : null;
	}

	@Nullable
	public Quest getQuest(short id)
	{
		QuestObject object = get(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public QuestTask getTask(short id)
	{
		QuestObject object = get(id);
		return object instanceof QuestTask ? (QuestTask) object : null;
	}

	@Nullable
	public QuestReward getReward(short id)
	{
		QuestObject object = get(id);
		return object instanceof QuestReward ? (QuestReward) object : null;
	}

	public short requestID()
	{
		return 0;
	}

	public short getID(NBTTagCompound nbt)
	{
		short id = nbt.getShort("id");

		if (id == 0 || map.containsKey(id))
		{
			id = requestID();
			nbt.setShort("id", id);
		}

		return id;
	}

	@Nullable
	public QuestObject createAndAdd(QuestObjectType type, short parent, NBTTagCompound nbt)
	{
		switch (type)
		{
			case CHAPTER:
			{
				QuestChapter chapter = new QuestChapter(this, nbt);
				chapter.index = chapter.file.chapters.size();
				chapter.file.chapters.add(chapter);
				map.put(chapter.id, chapter);
				return chapter;
			}
			case QUEST:
			{
				QuestChapter chapter = getChapter(parent);

				if (chapter != null)
				{
					Quest quest = new Quest(chapter, nbt);
					chapter.quests.add(quest);
					map.put(quest.id, quest);
					return quest;
				}

				return null;
			}
			case TASK:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					if (quest.tasks.size() >= 256)
					{
						return null;
					}

					QuestTask task = QuestTasks.createTask(quest, nbt);
					quest.tasks.add(task);
					map.put(task.id, task);

					for (IProgressData data : getAllData())
					{
						data.createTaskData(task);
					}

					return task;
				}

				return null;
			}
			case REWARD:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					if (quest.rewards.size() >= 256)
					{
						return null;
					}

					QuestReward reward = QuestRewards.createReward(quest, nbt);
					quest.rewards.add(reward);
					map.put(reward.id, reward);
					return reward;
				}

				return null;
			}
			default:
				return null;
		}
	}

	public static ItemStack getIcon(NBTTagCompound nbt)
	{
		ItemStack stack;

		if (nbt.hasKey("icon", Constants.NBT.TAG_STRING))
		{
			stack = ItemStackSerializer.parseItem(nbt.getString("icon"));
		}
		else
		{
			stack = new ItemStack(nbt.getCompoundTag("icon"));
		}

		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("title", title.getString());
		nbt.setTag("icon", icon.getStack().serializeNBT());

		NBTTagList chaptersList = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapter.writeData(chapterNBT);
			chaptersList.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", chaptersList);
		nbt.setBoolean("allow_take_quest_blocks", allowTakeQuestBlocks.getBoolean());

		NBTTagList emergencyItemsList = new NBTTagList();

		for (ConfigItemStack value : emergencyItems)
		{
			emergencyItemsList.appendTag(value.getStack().serializeNBT());
		}

		nbt.setTag("emergency_items", emergencyItemsList);
		nbt.setString("emergency_items_cooldown", emergencyItemsCooldown.getTimer().toString());
	}

	protected void readData(NBTTagCompound nbt)
	{
		chapters.clear();
		map.clear();
		map.put((short) 0, this);

		title.setString(nbt.getString("title"));
		icon.setStack(new ItemStack(nbt.getCompoundTag("icon")));

		NBTTagList chapterList = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < chapterList.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this, chapterList.getCompoundTagAt(i));
			chapter.index = chapters.size();
			chapters.add(chapter);
			map.put(chapter.id, chapter);
		}

		allowTakeQuestBlocks.setBoolean(!nbt.hasKey("allow_take_quest_blocks") || nbt.getBoolean("allow_take_quest_blocks"));
		emergencyItems.list.clear();

		NBTTagList emergencyItemsList = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < emergencyItemsList.tagCount(); i++)
		{
			ItemStack stack = new ItemStack(emergencyItemsList.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyItems.add(new ConfigItemStack(stack));
			}
		}

		Ticks t = Ticks.get(nbt.getString("emergency_items_cooldown"));
		emergencyItemsCooldown.setTimer(t.hasTicks() ? t : Ticks.MINUTE.x(5));
	}

	@Nullable
	public abstract IProgressData getData(String owner);

	public abstract Collection<IProgressData> getAllData();

	@Override
	public Icon getIcon()
	{
		return GuiIcons.BOOK_RED;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.file");
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.add("title", title, new ConfigString(""));
		config.add("icon", icon, new ConfigItemStack(ItemStack.EMPTY));
		config.add("allow_take_quest_blocks", allowTakeQuestBlocks, new ConfigBoolean(true));
		config.add("emergency_items", emergencyItems, new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE))));
		config.add("emergency_items_cooldown", emergencyItemsCooldown, new ConfigTimer(Ticks.MINUTE.x(5)));
	}
}