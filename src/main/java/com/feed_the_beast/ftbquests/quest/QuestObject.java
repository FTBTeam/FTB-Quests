package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public abstract class QuestObject
{
	public final short id;
	public boolean invalid = false;

	public QuestObject(short i)
	{
		id = i;
	}

	public abstract QuestFile getQuestFile();

	public abstract QuestObjectType getObjectType();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getIcon();

	public abstract ITextComponent getDisplayName();

	public void deleteSelf()
	{
		getQuestFile().map.remove(id);
		invalid = true;
	}

	public void deleteChildren()
	{
	}

	@Override
	public final String toString()
	{
		return getClass().getSimpleName() + '#' + QuestFile.formatID(id);
	}

	@Override
	public final int hashCode()
	{
		return id;
	}

	@Override
	public final boolean equals(Object o)
	{
		return o == this || o instanceof QuestObject && id == o.hashCode();
	}

	public void getConfig(ConfigGroup config)
	{
	}
}