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

	public QuestObject(short i)
	{
		id = i;
	}

	public abstract QuestList getQuestList();

	public abstract QuestObjectType getObjectType();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getIcon();

	public abstract ITextComponent getDisplayName();

	public boolean isInvalid()
	{
		return getQuestList().isInvalid();
	}

	public void delete()
	{
		getQuestList().objectMap.remove(id);
	}

	@Override
	public final String toString()
	{
		return getClass().getSimpleName() + '#' + QuestList.formatID(id);
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