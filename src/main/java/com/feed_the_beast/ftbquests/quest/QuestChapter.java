package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends QuestObject
{
	public final QuestFile file;
	public final List<Quest> quests;
	public final List<String> description;

	public QuestChapter(QuestFile f)
	{
		file = f;
		description = new ArrayList<>(0);
		quests = new ArrayList<>();
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.CHAPTER;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return file;
	}

	@Override
	public QuestChapter getQuestChapter()
	{
		return this;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (!description.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (String v : description)
			{
				list.appendTag(new NBTTagString(v));
			}

			nbt.setTag("description", list);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		description.clear();

		NBTTagList desc = nbt.getTagList("description", Constants.NBT.TAG_STRING);

		for (int i = 0; i < desc.tagCount(); i++)
		{
			description.add(desc.getStringTagAt(i));
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(description, DataOut.STRING);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(description, DataIn.STRING);
	}

	public int getIndex()
	{
		return file.chapters.indexOf(this);
	}

	@Override
	public long getProgress(ITeamData data)
	{
		long progress = 0L;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				progress += quest.getProgress(data);
			}
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				maxProgress += quest.getMaxProgress();
			}
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				progress += quest.getRelativeProgress(data);
			}
		}

		return fixRelativeProgress(progress, quests.size());
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		for (Quest quest : quests)
		{
			if (!quest.canRepeat && !quest.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	public boolean getOnlyHasRepeatableQuests()
	{
		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data, List<EntityPlayerMP> onlineMembers)
	{
		super.onCompleted(data, onlineMembers);
		new ObjectCompletedEvent.ChapterEvent(data, this).post();

		if (!getOnlyHasRepeatableQuests())
		{
			for (EntityPlayerMP player : onlineMembers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		if (file.isComplete(data))
		{
			file.onCompleted(data, onlineMembers);
		}
	}

	@Override
	public void changeProgress(ITeamData data, EnumChangeProgress type)
	{
		for (Quest quest : quests)
		{
			quest.changeProgress(data, type);
		}
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (Quest quest : quests)
		{
			list.add(quest.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		file.chapters.remove(this);
	}

	@Override
	public void deleteChildren()
	{
		for (Quest quest : quests)
		{
			quest.deleteChildren();
			quest.invalid = true;
		}

		quests.clear();
	}

	@Override
	public void onCreated()
	{
		file.chapters.add(this);

		if (!quests.isEmpty())
		{
			for (Quest quest : ListUtils.clearAndCopy(quests))
			{
				quest.onCreated();
			}
		}
	}

	@Override
	public File getFile(File folder)
	{
		return new File(folder, "chapters/" + getCodeString());
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("description", description, new ConfigString(""), ConfigString::new, ConfigString::getString);
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		for (Quest quest : quests)
		{
			v = v.strongest(quest.getVisibility(data));

			if (v.isInvisible())
			{
				break;
			}
		}

		return v;
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (Quest quest : quests)
		{
			quest.clearCachedData();
		}
	}
}