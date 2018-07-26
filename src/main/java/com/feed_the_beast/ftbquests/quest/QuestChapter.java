package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
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
public final class QuestChapter extends ProgressingQuestObject
{
	public final QuestList list;
	public final ConfigString title;
	public final ConfigList<ConfigString> description;
	public final ConfigItemStack icon;
	public final List<Quest> quests;
	public final IntCollection dependencies;

	public QuestChapter(QuestList l, NBTTagCompound nbt)
	{
		super(l.getID(nbt));
		list = l;
		title = new ConfigString(nbt.getString("title"));
		description = new ConfigList<>(new ConfigString(""));
		icon = new ConfigItemStack(new ItemStack(nbt.getCompoundTag("icon")));
		quests = new ArrayList<>();

		NBTTagList desc = nbt.getTagList("description", Constants.NBT.TAG_STRING);

		for (int i = 0; i < desc.tagCount(); i++)
		{
			description.add(new ConfigString(desc.getStringTagAt(i)));
		}

		dependencies = new IntOpenHashSet();

		for (int d : nbt.getIntArray("depends_on"))
		{
			dependencies.add(d);
		}

		NBTTagList questsList = nbt.getTagList("quests", Constants.NBT.TAG_COMPOUND);

		for (int j = 0; j < questsList.tagCount(); j++)
		{
			Quest quest = new Quest(this, questsList.getCompoundTagAt(j));
			quests.add(quest);
			list.objectMap.put(quest.id, quest);
		}
	}

	@Override
	public QuestList getQuestList()
	{
		return list;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.CHAPTER;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("id", id);
		nbt.setString("title", title.getString());

		if (!description.getList().isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (ConfigString v : description)
			{
				list.appendTag(new NBTTagString(v.getString()));
			}

			nbt.setTag("description", list);
		}

		if (!icon.getStack().isEmpty())
		{
			nbt.setTag("icon", icon.getStack().serializeNBT());
		}

		if (!dependencies.isEmpty())
		{
			nbt.setIntArray("dependencies", dependencies.toIntArray());
		}

		if (!quests.isEmpty())
		{
			NBTTagList questsList = new NBTTagList();

			for (Quest quest : quests)
			{
				NBTTagCompound questNBT = new NBTTagCompound();
				quest.writeData(questNBT);
				questsList.appendTag(questNBT);
			}

			nbt.setTag("quests", questsList);
		}
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

	@Override
	public Icon getIcon()
	{
		Icon i = ItemIcon.getItemIcon(icon.getStack());

		if (i.isEmpty())
		{
			List<Icon> list = new ArrayList<>();

			for (Quest quest : quests)
			{
				list.add(quest.getIcon());
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
		list.chapters.remove(this);

		for (Quest quest : quests)
		{
			quest.delete();
		}
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("title", title, new ConfigString(""));
		group.add("icon", icon, new ConfigItemStack(ItemStack.EMPTY));
		group.add("description", description, new ConfigList<>(new ConfigString("")));
	}
}